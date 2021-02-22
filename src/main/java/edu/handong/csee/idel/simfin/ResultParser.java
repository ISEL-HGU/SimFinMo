package edu.handong.csee.idel.simfin;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import com.google.common.collect.Lists;

public class ResultParser {

	enum Measure {
		Precision, Recall, F1, TP
	};

	// Measure measure = Measure.TP;
	double initialCutoff = 0.001;
	double increment = 0.01;
	double maxCutoff = 3.0;
	int kOffset = 2;
	int maxK = 2;

	public static void main(String[] args) throws IOException {
		ResultParser np = new ResultParser();
		if (args[0].equals("topk")) {
			np.getTopK(args);
		} else if (args[0].equals("divided")) {
			np.runDivided(args);
		} else if (args[0].equals("tps")) {
			np.runTps(args);
		} else if (args[0].equals("simple")) {
			np.runSimple(args);
		} else {
			System.out.println("No command selected!");
		}
	}

	// ./SimFinMo/ADP/bin/ADP simple rm_dups sqoop > ./SimFinMo/out/sqoop_dups_simple.csv
	private void runSimple(String[] args) throws IOException {
		String versionName = args[1];
		String projectName = args[2]; // "sentry OR tez"
		String filePathDist = "/data/jihoshin/" + versionName + "/" + projectName + "/";
		String filePathTest = "./output/testset/Y_" + projectName + ".csv";
		int incrementK = 1;
		int initialK = 1;
		int maximumK = 1000;

		// Reading Y_projectName.csv
		BufferedReader inputStreamTest = new BufferedReader(new FileReader(filePathTest));
		Iterable<CSVRecord> recordsTest = CSVFormat.RFC4180.parse(inputStreamTest);
		Iterator<CSVRecord> csvIterTest = recordsTest.iterator();
		List<CSVRecord> testList = new ArrayList<CSVRecord>();

		while (csvIterTest.hasNext()) {
			testList.add(csvIterTest.next());
		}

		ArrayList<ArrayList<Boolean>> containsBuggy = new ArrayList<ArrayList<Boolean>>();

		// iterate through test instance folders and read sorted.csv
		for (int i = 0; i < testList.size(); i++) {
			BufferedReader inputStreamSort = new BufferedReader(
					new FileReader(filePathDist + "test" + i + "/sorted.csv"));

			Iterable<CSVRecord> recordsSorted = CSVFormat.RFC4180.parse(inputStreamSort);

			ArrayList<Boolean> oneTestOfBoolArray = new ArrayList<Boolean>();

			// iterate through the sorted.csv until maxK
			int counter = 0;
			for (CSVRecord test : recordsSorted) {
				if (counter > maximumK)
					break;

				int yhLabel = Integer.parseInt(test.get(2));

				if (yhLabel == 1) {
					oneTestOfBoolArray.add(true);
				} else {
					oneTestOfBoolArray.add(false);
				}

				counter++;
			}
			containsBuggy.add(oneTestOfBoolArray);
		}

//		for (int i = 0; i < testList.size(); i++) {
//			for (int j = 0; j < maximumK; j++) {
//				System.out.print(containsBuggy.get(i).get(j) + " ");
//			}
//			System.out.println();
//		}

		// writing the evalated scores
		System.out.println("k-rank,TP,FN,TN,FP,precision,recall,f1,mcc");
		for (int currentK = initialK; currentK <= maximumK; currentK = currentK + incrementK) {
			System.out.print(currentK);
			System.out.print("," + evalSimple(testList, currentK, containsBuggy));
			System.out.println();
		}
	}

	private String evalSimple(List<CSVRecord> testList, int kNeighbor, ArrayList<ArrayList<Boolean>> containsBug) {
		int TP = 0, FP = 0, TN = 0, FN = 0;

		for (int i = 0; i < testList.size(); i++) {
			int yLabel = Integer.parseInt(testList.get(i).get(11));

			if (yLabel == 1) {
				if (containsBug.get(i).get(kNeighbor))
					TP++;
				else
					FN++;
			} else {
				if (containsBug.get(i).get(kNeighbor))
					FP++;
				else
					TN++;
			}

		}

		double precision = TP / ((double) TP + FP);
		double recall = TP / ((double) TP + FN);
		double f1 = (2 * precision * recall) / (precision + recall);
		double mcc = (double) (TP * TN - FP * FN) / (Math.sqrt((double) (TP + FP) * (TP + FN) * (TN + FP) * (TN + FN)));

		return TP + "," + FN + "," + TN + "," + FP + "," + precision + "," + recall + "," + f1 + "," + mcc;
	}

	// ./SimFinMo/ADP/bin/ADP topk 1000 rm_dups sqoop 0.001 0.9 1.1 > ./SimFinMo/out/sqoop_dups_avg.csv
	private void getTopK(String[] args) throws IOException {
		int kKneighbor = Integer.parseInt(args[1]);
		String versionName = args[2];
		String projectName = args[3]; // "sentry OR tez"
		String filePathDist = "/data/jihoshin/" + versionName + "/" + projectName + "/";
		String filePathTest = "./output/testset/Y_" + projectName + ".csv";
		increment = Double.parseDouble(args[4]);
		initialCutoff = Double.parseDouble(args[5]);
		maxCutoff = Double.parseDouble(args[6]);

		BufferedReader inputStreamTest = new BufferedReader(new FileReader(filePathTest));
		Iterable<CSVRecord> recordsTest = CSVFormat.RFC4180.parse(inputStreamTest);
		Iterator<CSVRecord> csvIterTest = recordsTest.iterator();
		List<CSVRecord> testList = new ArrayList<CSVRecord>();
		while (csvIterTest.hasNext()) {
			testList.add(csvIterTest.next());
		}

		// Getting number of test instances by counting folders in the directory
		List<String> files = new ArrayList<>();
		try {
			DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(filePathDist));
			for (Path path : directoryStream) {
				files.add(path.toString());
			}
		} catch (IOException ex) {
			System.out.println("Directory Stream Exception: " + ex);
		}
		int testInstances = files.size() - 1;

		ArrayList<Double> distanceRatios = new ArrayList<Double>();

		// loop through test instance folders
		for (int i = 0; i < testInstances; i++) {

			BufferedReader inputStreamSort = new BufferedReader(
					new FileReader(filePathDist + "test" + i + "/sorted.csv"));
			Iterable<CSVRecord> recordsSorted = CSVFormat.RFC4180.parse(inputStreamSort);

			double avgBugDist = 0;
			double avgCleanDist = 0;
			int numOfBug = 0;
			int numOfClean = 0;

			// loop through top Ks in the sorted file
			int counter = 0;
			for (CSVRecord test : recordsSorted) {
				if (counter > kKneighbor)
					break;

				int yhLabel = Integer.parseInt(test.get(2));
				double dist = Double.parseDouble(test.get(0));

				if (yhLabel == 0) {
					avgCleanDist += dist;
					numOfClean++;
				} else {
					avgBugDist += dist;
					numOfBug++;
				}

				counter++;
			}
			avgBugDist /= numOfBug;
			avgCleanDist /= numOfClean;
			double distanceRate = avgBugDist / avgCleanDist;
			// System.out.println("DR," + distanceRate);
			distanceRatios.add(distanceRate);
		}

		// writing the evalated scores
		System.out.println("cutoff-rank,TP,FN,TN,FP,precision,recall,f1,mcc");
		for (double cutoff = initialCutoff; cutoff <= maxCutoff; cutoff = cutoff + increment) {
			System.out.print(cutoff);
			for (int k = kOffset; k <= maxK; k++) {
				System.out.print("," + evalFromDRs(distanceRatios, testList, cutoff));
			}
			System.out.println();
		}
	}

	private String evalFromDRs(ArrayList<Double> drs, List<CSVRecord> testList, double cutoff) {
		int TP = 0, FP = 0, TN = 0, FN = 0;

		for (int i = 0; i < drs.size(); i++) {
			int yLabel = Integer.parseInt(testList.get(i).get(11));
			if (yLabel == 1) {
				if (drs.get(i) < cutoff)
					TP++;
				else
					FN++;
			} else {
				if (drs.get(i) < cutoff)
					FP++;
				else
					TN++;
			}

		}

		double precision = TP / ((double) TP + FP);
		double recall = TP / ((double) TP + FN);
		double f1 = (2 * precision * recall) / (precision + recall);
		double mcc = (double) (TP * TN - FP * FN) / (Math.sqrt((double) (TP + FP) * (TP + FN) * (TN + FP) * (TN + FN)));

		return TP + "," + FN + "," + TN + "," + FP + "," + precision + "," + recall + "," + f1 + "," + mcc;
	}

	@SuppressWarnings("unused")
	private void runTopK(String[] args) throws IOException {
		int kKneighbor = Integer.parseInt(args[1]);
		String projectName = args[2]; // "sentry OR tez"
		String filePathDist = "/data/jihoshin/" + projectName + "/";
		increment = Double.parseDouble(args[3]);
		initialCutoff = Double.parseDouble(args[4]);
		maxCutoff = Double.parseDouble(args[5]);

		String filePathTest = "./output/testset/Y_" + projectName + ".csv";
		FileReader csvTest = new FileReader(filePathTest);
		Iterable<CSVRecord> recordsTest = CSVFormat.RFC4180.parse(csvTest);
		Iterator<CSVRecord> csvIterTest = recordsTest.iterator();
		List<CSVRecord> testList = new ArrayList<CSVRecord>();
		while (csvIterTest.hasNext()) {
			testList.add(csvIterTest.next());
		}

		List<String> files = new ArrayList<>();
		try {
			DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(filePathDist));
			for (Path path : directoryStream) {
				files.add(path.toString());
			}
		} catch (IOException ex) {
			System.out.println("Directory Stream Exception: " + ex);
		}
		int testInstances = files.size() - 1;

		// writing the evalated scores
		System.out.println("cutoff-rank,TP,FN,TN,FP,precision,recall,f1,mcc");
		for (double cutoff = initialCutoff; cutoff <= maxCutoff; cutoff = cutoff + increment) {
			System.out.print(cutoff);
			for (int k = kOffset; k <= maxK; k++) {
				System.out.print("," + evalTopK(testList, filePathDist, testInstances, cutoff, kKneighbor));
			}
			System.out.println();
		}
	}

	private String evalTopK(List<CSVRecord> testList, String filePathDist, int testInstances, double cutoff,
			int kNeighbor) throws IOException {
		int TP = 0;
		int TN = 0;
		int FP = 0;
		int FN = 0;

		// loop through # of test instance
		for (int i = 0; i < testInstances; i++) {
			FileReader csvSorted = new FileReader(filePathDist + "test" + i + "/sorted.csv");
			Iterable<CSVRecord> recordsSorted = CSVFormat.RFC4180.parse(csvSorted);
			Iterator<CSVRecord> csvIterSorted = recordsSorted.iterator();
			List<CSVRecord> sortedList = new ArrayList<CSVRecord>();
			while (csvIterSorted.hasNext()) {
				sortedList.add(csvIterSorted.next());
			}

			double avgBugDist = 0.0;
			double avgCleanDist = 0.0;
			int numOfBug = 0;
			int numOfClean = 0;
			int yLabel = Integer.parseInt(testList.get(i).get(11));

			// loop through all Ks (# of train instances)
			for (int j = 0; j < kNeighbor; j++) {
				int yhLabel = Integer.parseInt(sortedList.get(j).get(2));
				double dist = Double.parseDouble(sortedList.get(j).get(0));
				if (yhLabel == 0) {
					avgCleanDist += dist;
					numOfClean++;
				} else if (yhLabel == 1) {
					avgBugDist += dist;
					numOfBug++;
				}
			}
			avgCleanDist /= numOfClean;
			avgBugDist /= numOfBug;

			double distanceRate = avgBugDist / avgCleanDist;
			if (numOfClean > 0 && numOfBug > 0) {
				if (yLabel == 1) {
					if (distanceRate < cutoff)
						TP++;
					else
						FN++;
				} else {
					if (distanceRate < cutoff)
						FP++;
					else
						TN++;
				}
			} else if (numOfBug == 0) {
				if (yLabel == 1) {
					FN++;
				} else {
					TN++;
				}
			} else {
				if (yLabel == 1) {
					TP++;
				} else {
					FP++;
				}
			}
		}
		double precision = TP / ((double) TP + FP);
		double recall = TP / ((double) TP + FN);
		double f1 = (2 * precision * recall) / (precision + recall);
		double mcc = (double) (TP * TN - FP * FN) / (Math.sqrt((double) (TP + FP) * (TP + FN) * (TN + FP) * (TN + FN)));

		return TP + "," + FN + "," + TN + "," + FP + "," + precision + "," + recall + "," + f1 + "," + mcc;
	}

	// ./ADP/bin/ADP combined /data/jihoshin/sentry/ 10 0.01 0.01 1.0
	@SuppressWarnings("unused")
	private void runCombined(String[] args) throws IOException {
		String filePath = args[1]; // "/data/jihoshin/sqoop/"
		int kNeighbors = Integer.parseInt(args[2]);
		if (kNeighbors > 100)
			kNeighbors = 100;
		increment = Double.parseDouble(args[3]);
		initialCutoff = Double.parseDouble(args[4]);
		maxCutoff = Double.parseDouble(args[5]);

		File file = new File(filePath);
		File[] files = file.listFiles(new FileFilter() {
			@Override
			public boolean accept(File f) {
				return f.isDirectory();
			}
		});
		int instanceNum = files.length;
		HashMap<String, ArrayList<SimFinData>> allData = new HashMap<String, ArrayList<SimFinData>>();
		// System.out.println("Test Instance Numbers: " + instanceNum);

		// iterate every num of test instance numbers == num of folders.
		for (int j = 0; j < instanceNum; j++) {
			FileReader csvIn = new FileReader(filePath + "test" + j + "/result.csv");
			Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(csvIn);
			Iterator<CSVRecord> csvIter = records.iterator();
			List<CSVRecord> csvList = new ArrayList<CSVRecord>();
			while (csvIter.hasNext()) {
				csvList.add(csvIter.next());
			}
			// data preparation of instances.
			for (int i = 0; i < kNeighbors; i++) {
				String yBicSha = csvList.get(i).get(0);
				String yBicPath = csvList.get(i).get(1);
				String key = yBicSha + yBicPath;
				String yBfcSha = csvList.get(i).get(3);
				String yBfcPath = csvList.get(i).get(4);
				int yLabel = Integer.parseInt(csvList.get(i).get(6));
				int rank = Integer.parseInt(csvList.get(i).get(7));
				double dist = Double.parseDouble(csvList.get(i).get(8));
				String project = csvList.get(i).get(9);
				String yhBicSha = csvList.get(i).get(10);
				String yhBicPath = csvList.get(i).get(11);
				String yhBfcSha = csvList.get(i).get(13);
				String yhBfcPath = csvList.get(i).get(14);
				int yhLabel = Integer.parseInt(csvList.get(i).get(16));

				SimFinData data = new SimFinData();
				data.setYBicSha(yBicSha);
				data.setYBicPath(yBicPath);
				data.setKey(key);
				data.setYBfcSha(yBfcSha);
				data.setYBfcPath(yBfcPath);
				data.setYLabel(yLabel);
				data.setRank(rank);
				data.setDist(dist);
				data.setProject(project);
				data.setYhBicSha(yhBicSha);
				data.setYhBicPath(yhBicPath);
				data.setYhBfcSha(yhBfcSha);
				data.setYhBfcPath(yhBfcPath);
				data.setYhLabel(yhLabel);

				if (allData.containsKey(key))
					allData.get(key).add(data);
				else {
					ArrayList<SimFinData> newSimFinData = new ArrayList<SimFinData>();
					newSimFinData.add(data);
					allData.put(key, newSimFinData);
				}
			}
		}

		ArrayList<String> keys = Lists.newArrayList(allData.keySet());

		// writing the evalated scores
		System.out.println("cutoff-rank" + kOffset + ",TP,FN,TN,FP,precision,recall,f1,mcc");
		for (double cutoff = initialCutoff; cutoff <= maxCutoff; cutoff = cutoff + increment) {
			// System.out.println(cutoff);
			System.out.print(cutoff);
			for (int k = kOffset; k <= maxK; k++) {
				if (kNeighbors == 1) {
					System.out.print("," + evalCombinedTop1(keys, allData, kOffset, k, cutoff));
				} else {
					System.out.print("," + evalCombinedAvg(keys, allData, kOffset, k, cutoff));
				}
				// System.out.print("," + evaluateClean(keys, cleanData, kOffset, k, cutoff));
				// System.out.print("," + evaluateBuggy(keys, buggyData, kOffset, k, cutoff));
			}
			System.out.println();
		}
	}

	private void runDivided(String[] args) {
		String resultFilePath = args[0]; // "data" + File.separator + folder + File.separator + "maven_result.csv";
		String projectName = args[1];
		initialCutoff = Double.parseDouble(args[2]);
		increment = Double.parseDouble(args[3]);
		maxCutoff = Double.parseDouble(args[4]);
		kOffset = Integer.parseInt(args[5]);
		maxK = kOffset; // just consider only one rank

		HashMap<String, ArrayList<SimFinData>> buggyData = new HashMap<String, ArrayList<SimFinData>>();
		HashMap<String, ArrayList<SimFinData>> cleanData = new HashMap<String, ArrayList<SimFinData>>();

		Reader buggyIn;
		Reader cleanIn;
		try {
			buggyIn = new FileReader(resultFilePath + projectName + "_buggy_result.csv");
			cleanIn = new FileReader(resultFilePath + projectName + "_clean_result.csv");
			Iterable<CSVRecord> buggyRecords = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(buggyIn);
			Iterable<CSVRecord> cleanRecords = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(cleanIn);
			List<CSVRecord> buggyList = new ArrayList<CSVRecord>();
			List<CSVRecord> cleanList = new ArrayList<CSVRecord>();
			Iterator<CSVRecord> iterBuggy = buggyRecords.iterator();
			Iterator<CSVRecord> iterClean = cleanRecords.iterator();
			while (iterBuggy.hasNext()) {
				buggyList.add(iterBuggy.next());
			}
			while (iterClean.hasNext()) {
				cleanList.add(iterClean.next());
			}

			// data preparation for both buggy and clean instances.
			for (int i = 0; i < buggyList.size(); i++) {
				String yBicSha = buggyList.get(i).get(0);
				String yBicPath = buggyList.get(i).get(1);
				String key = yBicSha + yBicPath;
				String yBfcSha = buggyList.get(i).get(3);
				String yBfcPath = buggyList.get(i).get(4);
				int yLabel = Integer.parseInt(buggyList.get(i).get(6));
				int rank = Integer.parseInt(buggyList.get(i).get(7));
				double dist = Double.parseDouble(buggyList.get(i).get(8));
				String project = buggyList.get(i).get(9);
				String yhBicSha = buggyList.get(i).get(10);
				String yhBicPath = buggyList.get(i).get(11);
				String yhBfcSha = buggyList.get(i).get(13);
				String yhBfcPath = buggyList.get(i).get(14);
				int yhLabel = Integer.parseInt(buggyList.get(i).get(16));

				SimFinData buggyRecord = new SimFinData();
				buggyRecord.setYBicSha(yBicSha);
				buggyRecord.setYBicPath(yBicPath);
				buggyRecord.setKey(key);
				buggyRecord.setYBfcSha(yBfcSha);
				buggyRecord.setYBfcPath(yBfcPath);
				buggyRecord.setYLabel(yLabel);
				buggyRecord.setRank(rank);
				buggyRecord.setDist(dist);
				buggyRecord.setProject(project);
				buggyRecord.setYhBicSha(yhBicSha);
				buggyRecord.setYhBicPath(yhBicPath);
				buggyRecord.setYhBfcSha(yhBfcSha);
				buggyRecord.setYhBfcPath(yhBfcPath);
				buggyRecord.setYhLabel(yhLabel);

				if (buggyData.containsKey(key))
					buggyData.get(key).add(buggyRecord);
				else {
					ArrayList<SimFinData> newSimFinData = new ArrayList<SimFinData>();
					newSimFinData.add(buggyRecord);
					buggyData.put(key, newSimFinData);
				}
			}

			ArrayList<String> keys = Lists.newArrayList(buggyData.keySet());
			for (int i = 0; i < cleanList.size(); i++) {
				String yBicSha = buggyList.get(i).get(0);
				String yBicPath = buggyList.get(i).get(1);
				String key = yBicSha + yBicPath;
				String yBfcSha = buggyList.get(i).get(3);
				String yBfcPath = buggyList.get(i).get(4);
				int yLabel = Integer.parseInt(buggyList.get(i).get(6));
				int rank = Integer.parseInt(buggyList.get(i).get(7));
				double dist = Double.parseDouble(buggyList.get(i).get(8));
				String project = buggyList.get(i).get(9);
				String yhBicSha = buggyList.get(i).get(10);
				String yhBicPath = buggyList.get(i).get(11);
				String yhBfcSha = buggyList.get(i).get(13);
				String yhBfcPath = buggyList.get(i).get(14);
				int yhLabel = Integer.parseInt(buggyList.get(i).get(16));

				SimFinData cleanRecord = new SimFinData();
				cleanRecord.setYBicSha(yBicSha);
				cleanRecord.setYBicPath(yBicPath);
				cleanRecord.setKey(key);
				cleanRecord.setYBfcSha(yBfcSha);
				cleanRecord.setYBfcPath(yBfcPath);
				cleanRecord.setYLabel(yLabel);
				cleanRecord.setRank(rank);
				cleanRecord.setDist(dist);
				cleanRecord.setProject(project);
				cleanRecord.setYhBicSha(yhBicSha);
				cleanRecord.setYhBicPath(yhBicPath);
				cleanRecord.setYhBfcSha(yhBfcSha);
				cleanRecord.setYhBfcPath(yhBfcPath);
				cleanRecord.setYhLabel(yhLabel);

				if (cleanData.containsKey(key))
					cleanData.get(key).add(cleanRecord);
				else {
					ArrayList<SimFinData> newSimFinData = new ArrayList<SimFinData>();
					newSimFinData.add(cleanRecord);
					cleanData.put(key, newSimFinData);
				}
			}

			// writing the evalated scores
			System.out.println("cutoff-rank" + kOffset + ",TP,FN,TN,FP,precision,recall,f1,mcc");
			for (double cutoff = initialCutoff; cutoff <= maxCutoff; cutoff = cutoff + increment) {
				// System.out.println(cutoff);
				System.out.print(cutoff);
				for (int k = kOffset; k <= maxK; k++) {
					System.out.print("," + evaluateDivided(keys, buggyData, cleanData, kOffset, k, cutoff));
				}
				System.out.println();
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void getTPs(ArrayList<String> keys, HashMap<String, ArrayList<SimFinData>> buggyData,
			HashMap<String, ArrayList<SimFinData>> cleanData) {
		for (String key : keys) {

			ArrayList<SimFinData> simFinDataBuggy = buggyData.get(key);
			ArrayList<SimFinData> simFinDataClean = cleanData.get(key);
			int label = simFinDataBuggy.get(0).getYLabel();

			double averageDistanceBuggy = simFinDataBuggy.get(0).getDist();// averageDistance(simFinDataBuggy, 2, 2,
																			// true);
			double averageDistanceClean = simFinDataClean.get(0).getDist();// averageDistance(simFinDataClean, 2,
																			// 2,false); // 2, 24

			double distanceRate = averageDistanceBuggy / averageDistanceClean;
			if (label == 1) {
				if (distanceRate < 1) {
					for (int i = 0; i < 10; i++) {
						System.out.print(simFinDataBuggy.get(i).getYBfcSha() + ","
								+ simFinDataBuggy.get(i).getYBfcPath() + "," + simFinDataBuggy.get(i).getRank() + ","
								+ simFinDataBuggy.get(i).getProject() + "," + simFinDataBuggy.get(i).getYhBfcSha() + ","
								+ simFinDataBuggy.get(i).getYhBfcPath() + "\n");
					}
				}
			}
		}
	}

	// evaluate by getting the average distance of both buggy and clean
	private String evalCombinedTop1(ArrayList<String> keys, HashMap<String, ArrayList<SimFinData>> allData, int kOffset,
			int k, double cutoff) {

		int TP = 0; // 1 -> 1
		int FP = 0; // 0 -> 1
		int TN = 0; // 0 -> 0
		int FN = 0; // 1 -> 0
		// int noClean = 0;
		// int noBuggy = 0;

		// loop through test instances
		for (String key : keys) {

			ArrayList<SimFinData> simFinData = allData.get(key);

			double closestBuggy = Double.MAX_VALUE;
			double closestClean = Double.MAX_VALUE;
			int numOfBuggy = 0;
			int numOfClean = 0;

			for (SimFinData data : simFinData) {
				if (data.getYhLabel() == 0) {
					if (data.getDist() < closestClean) {
						closestClean = data.getDist();
						numOfClean++;
					}
				} else if (data.getYhLabel() == 1) {
					if (data.getDist() < closestBuggy) {
						closestBuggy = data.getDist();
						numOfBuggy++;
					}
				}
			}

			int yLabel = simFinData.get(0).getYLabel();

			if (numOfBuggy > 0 && numOfClean > 0) {
				double distanceRate = closestBuggy / closestClean;
				if (yLabel == 1) {
					if (distanceRate < cutoff)
						TP++;
					else
						FN++;
				} else {
					if (distanceRate < cutoff)
						FP++;
					else
						TN++;
				}
			} else if (numOfBuggy == 0) {
				// noBuggy++;
				if (yLabel == 1) {
					FN++;
				} else {
					TN++;
				}
			} else {
				// noClean++;
				if (yLabel == 1) {
					TP++;
				} else {
					FP++;
				}
			}
		}
		// System.out.println(",no buggy," + noBuggy + ",no clean," + noClean + ",");
		double precision = TP / ((double) TP + FP);
		double recall = TP / ((double) TP + FN);
		double f1 = (2 * precision * recall) / (precision + recall);
		double mcc = (double) (TP * TN - FP * FN) / (Math.sqrt((double) (TP + FP) * (TP + FN) * (TN + FP) * (TN + FN)));

		return TP + "," + FN + "," + TN + "," + FP + "," + precision + "," + recall + "," + f1 + "," + mcc;
	}

	// evaluate by getting the average distance of both buggy and clean
	private String evalCombinedAvg(ArrayList<String> keys, HashMap<String, ArrayList<SimFinData>> allData, int kOffset,
			int k, double cutoff) {

		int TP = 0; // 1 -> 1
		int FP = 0; // 0 -> 1
		int TN = 0; // 0 -> 0
		int FN = 0; // 1 -> 0

		for (String key : keys) {

			ArrayList<SimFinData> simFinData = allData.get(key);

			int numOfBuggy = 0;
			int numOfClean = 0;

			double averageDistanceBuggy = 0;
			double averageDistanceClean = 0;
			// averageDistance(simFinDataBuggy, 2, 2, true);
			// averageDistance(simFinDataClean, 2, 2, false); // 2, 24

			for (SimFinData data : simFinData) {
				if (data.getYhLabel() == 0) {
					averageDistanceClean += data.getDist();
					numOfClean++;
				} else if (data.getYhLabel() == 1) {
					averageDistanceBuggy += data.getDist();
					numOfBuggy++;
				}
			}
			averageDistanceClean /= numOfClean;
			averageDistanceBuggy /= numOfBuggy;

			int yLabel = simFinData.get(0).getYLabel();

			double distanceRate = averageDistanceBuggy / averageDistanceClean;
			if (numOfClean > 0 && numOfBuggy > 0) {
				if (yLabel == 1) {
					if (distanceRate < cutoff)
						TP++;
					else
						FN++;
				} else {
					if (distanceRate < cutoff)
						FP++;
					else
						TN++;
				}
			} else if (numOfBuggy == 0) {
				if (yLabel == 1) {
					FN++;
				} else {
					TN++;
				}
			} else {
				if (yLabel == 1) {
					TP++;
				} else {
					FP++;
				}
			}

		}

		double precision = TP / ((double) TP + FP);
		double recall = TP / ((double) TP + FN);
		double f1 = (2 * precision * recall) / (precision + recall);
		double mcc = (double) (TP * TN - FP * FN) / (Math.sqrt((double) (TP + FP) * (TP + FN) * (TN + FP) * (TN + FN)));

		return TP + ", " + FN + ", " + TN + "," + FP + "," + precision + "," + recall + "," + f1 + "," + mcc;
	}

	private String evaluateDivided(ArrayList<String> keys, HashMap<String, ArrayList<SimFinData>> buggyData,
			HashMap<String, ArrayList<SimFinData>> cleanData, int kOffset, int k, double cutoff) {

		int TP = 0; // 1 -> 1
		int FP = 0; // 0 -> 1
		int TN = 0; // 0 -> 0
		int FN = 0; // 1 -> 0

		for (String key : keys) {

			ArrayList<SimFinData> simFinDataBuggy = buggyData.get(key);
			ArrayList<SimFinData> simFinDataClean = cleanData.get(key);
			int label = simFinDataBuggy.get(0).getYLabel();

			double averageDistanceBuggy = simFinDataBuggy.get(0).getDist();// averageDistance(simFinDataBuggy, 2, 2,
																			// true);
			double averageDistanceClean = simFinDataClean.get(0).getDist();// averageDistance(simFinDataClean, 2,
																			// 2,false); // 2, 24

			double distanceRate = averageDistanceBuggy / averageDistanceClean;
			if (label == 1) {
				if (distanceRate < cutoff)
					TP++;
				else
					FN++;
			} else {
				if (distanceRate < cutoff)
					FP++;
				else
					TN++;
			}

		}

		double precision = TP / ((double) TP + FP);
		double recall = TP / ((double) TP + FN);
		double f1 = (2 * precision * recall) / (precision + recall);
		double mcc = (double) (TP * TN - FP * FN) / (Math.sqrt((double) (TP + FP) * (TP + FN) * (TN + FP) * (TN + FN)));

		return TP + ", " + FN + ", " + TN + "," + FP + "," + precision + "," + recall + "," + f1 + "," + mcc;
	}

	private void runTps(String[] args) {
		String resultFilePath = args[0]; // "data" + File.separator + folder + File.separator + "maven_result.csv";
		String projectName = args[1];
		initialCutoff = Double.parseDouble(args[2]);
		increment = Double.parseDouble(args[3]);
		maxCutoff = Double.parseDouble(args[4]);
		kOffset = Integer.parseInt(args[5]);
		maxK = kOffset; // just consider only one rank

		HashMap<String, ArrayList<SimFinData>> buggyData = new HashMap<String, ArrayList<SimFinData>>();
		HashMap<String, ArrayList<SimFinData>> cleanData = new HashMap<String, ArrayList<SimFinData>>();

		Reader buggyIn;
		Reader cleanIn;
		try {
			buggyIn = new FileReader(resultFilePath + projectName + "_buggy_result.csv");
			cleanIn = new FileReader(resultFilePath + projectName + "_clean_result.csv");
			Iterable<CSVRecord> buggyRecords = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(buggyIn);
			Iterable<CSVRecord> cleanRecords = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(cleanIn);
			List<CSVRecord> buggyList = new ArrayList<CSVRecord>();
			List<CSVRecord> cleanList = new ArrayList<CSVRecord>();
			Iterator<CSVRecord> iterBuggy = buggyRecords.iterator();
			Iterator<CSVRecord> iterClean = cleanRecords.iterator();
			while (iterBuggy.hasNext()) {
				buggyList.add(iterBuggy.next());
			}
			while (iterClean.hasNext()) {
				cleanList.add(iterClean.next());
			}

			// data preparation for buggy instances.
			for (int i = 0; i < buggyList.size(); i++) {
				String yBicSha = buggyList.get(i).get(0);
				String yBicPath = buggyList.get(i).get(1);
				String key = yBicSha + yBicPath;
				String yBfcSha = buggyList.get(i).get(3);
				String yBfcPath = buggyList.get(i).get(4);
				int yLabel = Integer.parseInt(buggyList.get(i).get(6));
				int rank = Integer.parseInt(buggyList.get(i).get(7));
				double dist = Double.parseDouble(buggyList.get(i).get(8));
				String project = buggyList.get(i).get(9);
				String yhBicSha = buggyList.get(i).get(10);
				String yhBicPath = buggyList.get(i).get(11);
				String yhBfcSha = buggyList.get(i).get(13);
				String yhBfcPath = buggyList.get(i).get(14);
				int yhLabel = Integer.parseInt(buggyList.get(i).get(16));

				SimFinData buggyRecord = new SimFinData();
				buggyRecord.setYBicSha(yBicSha);
				buggyRecord.setYBicPath(yBicPath);
				buggyRecord.setKey(key);
				buggyRecord.setYBfcSha(yBfcSha);
				buggyRecord.setYBfcPath(yBfcPath);
				buggyRecord.setYLabel(yLabel);
				buggyRecord.setRank(rank);
				buggyRecord.setDist(dist);
				buggyRecord.setProject(project);
				buggyRecord.setYhBicSha(yhBicSha);
				buggyRecord.setYhBicPath(yhBicPath);
				buggyRecord.setYhBfcSha(yhBfcSha);
				buggyRecord.setYhBfcPath(yhBfcPath);
				buggyRecord.setYhLabel(yhLabel);

				if (buggyData.containsKey(key))
					buggyData.get(key).add(buggyRecord);
				else {
					ArrayList<SimFinData> newSimFinData = new ArrayList<SimFinData>();
					newSimFinData.add(buggyRecord);
					buggyData.put(key, newSimFinData);
				}
			}

			// data preparation for clean instances.
			ArrayList<String> keys = Lists.newArrayList(buggyData.keySet());
			for (int i = 0; i < cleanList.size(); i++) {
				String yBicSha = buggyList.get(i).get(0);
				String yBicPath = buggyList.get(i).get(1);
				String key = yBicSha + yBicPath;
				String yBfcSha = buggyList.get(i).get(3);
				String yBfcPath = buggyList.get(i).get(4);
				int yLabel = Integer.parseInt(buggyList.get(i).get(6));
				int rank = Integer.parseInt(buggyList.get(i).get(7));
				double dist = Double.parseDouble(buggyList.get(i).get(8));
				String project = buggyList.get(i).get(9);
				String yhBicSha = buggyList.get(i).get(10);
				String yhBicPath = buggyList.get(i).get(11);
				String yhBfcSha = buggyList.get(i).get(13);
				String yhBfcPath = buggyList.get(i).get(14);
				int yhLabel = Integer.parseInt(buggyList.get(i).get(16));

				SimFinData cleanRecord = new SimFinData();
				cleanRecord.setYBicSha(yBicSha);
				cleanRecord.setYBicPath(yBicPath);
				cleanRecord.setKey(key);
				cleanRecord.setYBfcSha(yBfcSha);
				cleanRecord.setYBfcPath(yBfcPath);
				cleanRecord.setYLabel(yLabel);
				cleanRecord.setRank(rank);
				cleanRecord.setDist(dist);
				cleanRecord.setProject(project);
				cleanRecord.setYhBicSha(yhBicSha);
				cleanRecord.setYhBicPath(yhBicPath);
				cleanRecord.setYhBfcSha(yhBfcSha);
				cleanRecord.setYhBfcPath(yhBfcPath);
				cleanRecord.setYhLabel(yhLabel);

				if (cleanData.containsKey(key))
					cleanData.get(key).add(cleanRecord);
				else {
					ArrayList<SimFinData> newSimFinData = new ArrayList<SimFinData>();
					newSimFinData.add(cleanRecord);
					cleanData.put(key, newSimFinData);
				}
			}

			getTPs(keys, buggyData, cleanData);

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static int countLinesOfFile(String filename) throws IOException {
		InputStream is = new BufferedInputStream(new FileInputStream(filename));
		try {
			byte[] c = new byte[1024];

			int readChars = is.read(c);
			if (readChars == -1) {
				// bail out if nothing to read
				return 0;
			}

			// make it easy for the optimizer to tune this loop
			int count = 0;
			while (readChars == 1024) {
				for (int i = 0; i < 1024;) {
					if (c[i++] == '\n') {
						++count;
					}
				}
				readChars = is.read(c);
			}

			// count remaining characters
			while (readChars != -1) {
				System.out.println(readChars);
				for (int i = 0; i < readChars; ++i) {
					if (c[i] == '\n') {
						++count;
					}
				}
				readChars = is.read(c);
			}

			return count == 0 ? 1 : count;
		} finally {
			is.close();
		}
	}

	@SuppressWarnings("unused")
	private double averageDistance(ArrayList<SimFinData> simFinData, int kOffset, int k, boolean isBuggyDataIterating) {
		int label = simFinData.get(0).getYLabel();
		double sum = 0.0;
		// kOffset = 2 // k = 2
//		try {
		if ((isBuggyDataIterating && label == 0) || (!isBuggyDataIterating && label == 1)) {
			kOffset = 1;
			k = k - 1;
		}
		for (int i = kOffset - 1; i < k; i++) {
			sum = sum + simFinData.get(i).getDist();
		}
//		} catch (Exception e) {
//			System.out.println(simFinData.get(0).getKey());
//		}
		return sum / (k - kOffset + 1);
	}

}
