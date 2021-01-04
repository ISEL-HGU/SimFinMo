package edu.handong.csee.idel.simfin;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
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

		if (args[0].equals("all")) {
			np.run_all(args);
		} else if (args[0].equals("tps")) {
			np.run_tps(args);
		} else if (args[0].equals("bnc")) {
			np.run_bnc(args);
		} else {
			System.out.println("No command selected!");
		}
	}

	// ./ADP/bin/ADP all /data/jihoshin/sentry/ 10 0.01 0.01 1.0
	private void run_all(String[] args) throws IOException {
		String filePath = args[1]; // "./data/jihoshin/sqoop/"
		int k_neighbors = Integer.parseInt(args[2]); // 10
		increment = Double.parseDouble(args[3]);
		initialCutoff = Double.parseDouble(args[4]);
		maxCutoff = Double.parseDouble(args[5]);
		boolean is_top_only = false;
		if (args[6].equals("true"))
			is_top_only = true;

		if (k_neighbors > 100)
			k_neighbors = 100;
		File file = new File(filePath);
		File[] files = file.listFiles(new FileFilter() {
			@Override
			public boolean accept(File f) {
				return f.isDirectory();
			}
		});
		int instanceNum = files.length;
		HashMap<String, ArrayList<SimFinData>> all_data = new HashMap<String, ArrayList<SimFinData>>();
//		System.out.println("Test Instance Numbers: " + instanceNum);

		// iterate every num of test instance numbers == num of folders.
		for (int j = 0; j < instanceNum; j++) {
			FileReader csv_in = new FileReader(filePath + "test" + j + "/result.csv");
			Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(csv_in);
			Iterator<CSVRecord> csv_iter = records.iterator();
			List<CSVRecord> csv_list = new ArrayList<CSVRecord>();
			while (csv_iter.hasNext()) {
				csv_list.add(csv_iter.next());
			}
			// data preparation of instances.
			for (int i = 0; i < k_neighbors; i++) {
				String yBicSha = csv_list.get(i).get(0);
				String yBicPath = csv_list.get(i).get(1);
				String key = yBicSha + yBicPath;
				String yBfcSha = csv_list.get(i).get(3);
				String yBfcPath = csv_list.get(i).get(4);
				int yLabel = Integer.parseInt(csv_list.get(i).get(6));
				int rank = Integer.parseInt(csv_list.get(i).get(7));
				double dist = Double.parseDouble(csv_list.get(i).get(8));
				String project = csv_list.get(i).get(9);
				String yhBicSha = csv_list.get(i).get(10);
				String yhBicPath = csv_list.get(i).get(11);
				String yhBfcSha = csv_list.get(i).get(13);
				String yhBfcPath = csv_list.get(i).get(14);
				int yhLabel = Integer.parseInt(csv_list.get(i).get(16));

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

				if (all_data.containsKey(key))
					all_data.get(key).add(data);
				else {
					ArrayList<SimFinData> newSimFinData = new ArrayList<SimFinData>();
					newSimFinData.add(data);
					all_data.put(key, newSimFinData);
				}
			}
		}

		ArrayList<String> keys = Lists.newArrayList(all_data.keySet());

		// writing the evalated scores
		System.out.println("cutoff-rank" + kOffset + ",TP,FN,TN,FP,precision,recall,f1,mcc");
		for (double cutoff = initialCutoff; cutoff <= maxCutoff; cutoff = cutoff + increment) {
			// System.out.println(cutoff);
			System.out.print(cutoff);
			for (int k = kOffset; k <= maxK; k++) {
				if (is_top_only) {
					System.out.print("," + evaluate_top_only(keys, all_data, kOffset, k, cutoff));
				} else {
					System.out.print("," + evaluate_bnc(keys, all_data, kOffset, k, cutoff));
				}
				// System.out.print("," + evaluate_clean(keys, clean_data, kOffset, k, cutoff));
				// System.out.print("," + evaluate_buggy(keys, buggy_data, kOffset, k, cutoff));
			}
			System.out.println();
		}
	}

	private void run_bnc(String[] args) {
		String resultFilePath = args[0]; // "data" + File.separator + folder + File.separator + "maven_result.csv";
		String projectName = args[1];
		initialCutoff = Double.parseDouble(args[2]);
		increment = Double.parseDouble(args[3]);
		maxCutoff = Double.parseDouble(args[4]);
		kOffset = Integer.parseInt(args[5]);
		maxK = kOffset; // just consider only one rank

		HashMap<String, ArrayList<SimFinData>> buggy_data = new HashMap<String, ArrayList<SimFinData>>();
		HashMap<String, ArrayList<SimFinData>> clean_data = new HashMap<String, ArrayList<SimFinData>>();

		Reader buggy_in;
		Reader clean_in;
		try {
			buggy_in = new FileReader(resultFilePath + projectName + "_buggy_result.csv");
			clean_in = new FileReader(resultFilePath + projectName + "_clean_result.csv");
			Iterable<CSVRecord> buggy_records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(buggy_in);
			Iterable<CSVRecord> clean_records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(clean_in);
			List<CSVRecord> buggy_list = new ArrayList<CSVRecord>();
			List<CSVRecord> clean_list = new ArrayList<CSVRecord>();
			Iterator<CSVRecord> iter_buggy = buggy_records.iterator();
			Iterator<CSVRecord> iter_clean = clean_records.iterator();
			while (iter_buggy.hasNext()) {
				buggy_list.add(iter_buggy.next());
			}
			while (iter_clean.hasNext()) {
				clean_list.add(iter_clean.next());
			}

			// data preparation for both buggy and clean instances.
			for (int i = 0; i < buggy_list.size(); i++) {
				String yBicSha = buggy_list.get(i).get(0);
				String yBicPath = buggy_list.get(i).get(1);
				String key = yBicSha + yBicPath;
				String yBfcSha = buggy_list.get(i).get(3);
				String yBfcPath = buggy_list.get(i).get(4);
				int yLabel = Integer.parseInt(buggy_list.get(i).get(6));
				int rank = Integer.parseInt(buggy_list.get(i).get(7));
				double dist = Double.parseDouble(buggy_list.get(i).get(8));
				String project = buggy_list.get(i).get(9);
				String yhBicSha = buggy_list.get(i).get(10);
				String yhBicPath = buggy_list.get(i).get(11);
				String yhBfcSha = buggy_list.get(i).get(13);
				String yhBfcPath = buggy_list.get(i).get(14);
				int yhLabel = Integer.parseInt(buggy_list.get(i).get(16));

				SimFinData buggy_record = new SimFinData();
				buggy_record.setYBicSha(yBicSha);
				buggy_record.setYBicPath(yBicPath);
				buggy_record.setKey(key);
				buggy_record.setYBfcSha(yBfcSha);
				buggy_record.setYBfcPath(yBfcPath);
				buggy_record.setYLabel(yLabel);
				buggy_record.setRank(rank);
				buggy_record.setDist(dist);
				buggy_record.setProject(project);
				buggy_record.setYhBicSha(yhBicSha);
				buggy_record.setYhBicPath(yhBicPath);
				buggy_record.setYhBfcSha(yhBfcSha);
				buggy_record.setYhBfcPath(yhBfcPath);
				buggy_record.setYhLabel(yhLabel);

				if (buggy_data.containsKey(key))
					buggy_data.get(key).add(buggy_record);
				else {
					ArrayList<SimFinData> newSimFinData = new ArrayList<SimFinData>();
					newSimFinData.add(buggy_record);
					buggy_data.put(key, newSimFinData);
				}
			}

			ArrayList<String> keys = Lists.newArrayList(buggy_data.keySet());
			for (int i = 0; i < clean_list.size(); i++) {
				String yBicSha = buggy_list.get(i).get(0);
				String yBicPath = buggy_list.get(i).get(1);
				String key = yBicSha + yBicPath;
				String yBfcSha = buggy_list.get(i).get(3);
				String yBfcPath = buggy_list.get(i).get(4);
				int yLabel = Integer.parseInt(buggy_list.get(i).get(6));
				int rank = Integer.parseInt(buggy_list.get(i).get(7));
				double dist = Double.parseDouble(buggy_list.get(i).get(8));
				String project = buggy_list.get(i).get(9);
				String yhBicSha = buggy_list.get(i).get(10);
				String yhBicPath = buggy_list.get(i).get(11);
				String yhBfcSha = buggy_list.get(i).get(13);
				String yhBfcPath = buggy_list.get(i).get(14);
				int yhLabel = Integer.parseInt(buggy_list.get(i).get(16));

				SimFinData clean_record = new SimFinData();
				clean_record.setYBicSha(yBicSha);
				clean_record.setYBicPath(yBicPath);
				clean_record.setKey(key);
				clean_record.setYBfcSha(yBfcSha);
				clean_record.setYBfcPath(yBfcPath);
				clean_record.setYLabel(yLabel);
				clean_record.setRank(rank);
				clean_record.setDist(dist);
				clean_record.setProject(project);
				clean_record.setYhBicSha(yhBicSha);
				clean_record.setYhBicPath(yhBicPath);
				clean_record.setYhBfcSha(yhBfcSha);
				clean_record.setYhBfcPath(yhBfcPath);
				clean_record.setYhLabel(yhLabel);

				if (clean_data.containsKey(key))
					clean_data.get(key).add(clean_record);
				else {
					ArrayList<SimFinData> newSimFinData = new ArrayList<SimFinData>();
					newSimFinData.add(clean_record);
					clean_data.put(key, newSimFinData);
				}
			}

			// writing the evalated scores
			System.out.println("cutoff-rank" + kOffset + ",TP,FN,TN,FP,precision,recall,f1,mcc");
			for (double cutoff = initialCutoff; cutoff <= maxCutoff; cutoff = cutoff + increment) {
				// System.out.println(cutoff);
				System.out.print(cutoff);
				for (int k = kOffset; k <= maxK; k++) {
					System.out.print("," + evaluate_bnc(keys, buggy_data, clean_data, kOffset, k, cutoff));
					// System.out.print("," + evaluate_clean(keys, clean_data, kOffset, k, cutoff));
					// System.out.print("," + evaluate_buggy(keys, buggy_data, kOffset, k, cutoff));
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

	private void getTPs(ArrayList<String> keys, HashMap<String, ArrayList<SimFinData>> buggy_data,
			HashMap<String, ArrayList<SimFinData>> clean_data) {
		for (String key : keys) {

			ArrayList<SimFinData> simFinDataBuggy = buggy_data.get(key);
			ArrayList<SimFinData> simFinDataClean = clean_data.get(key);
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
	private String evaluate_top_only(ArrayList<String> keys, HashMap<String, ArrayList<SimFinData>> all_data,
			int kOffset, int k, double cutoff) {

		int TP = 0; // 1 -> 1
		int FP = 0; // 0 -> 1
		int TN = 0; // 0 -> 0
		int FN = 0; // 1 -> 0
		// int no_clean = 0;
		// int no_buggy = 0;

		for (String key : keys) {

			ArrayList<SimFinData> simFinData = all_data.get(key);

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
				// no_buggy++;
				if (yLabel == 1) {
					FN++;
				} else {
					TN++;
				}
			} else {
				// no_clean++;
				if (yLabel == 1) {
					TP++;
				} else {
					FP++;
				}
			}
		}
		// System.out.println(",no buggy," + no_buggy + ",no clean," + no_clean + ",");
		double precision = TP / ((double) TP + FP);
		double recall = TP / ((double) TP + FN);
		double f1 = (2 * precision * recall) / (precision + recall);
		double mcc = (double) (TP * TN - FP * FN) / (Math.sqrt((double) (TP + FP) * (TP + FN) * (TN + FP) * (TN + FN)));

		return TP + ", " + FN + ", " + TN + "," + FP + "," + precision + "," + recall + "," + f1 + "," + mcc;
	}

	// evaluate by getting the average distance of both buggy and clean
	private String evaluate_bnc(ArrayList<String> keys, HashMap<String, ArrayList<SimFinData>> all_data, int kOffset,
			int k, double cutoff) {

		int TP = 0; // 1 -> 1
		int FP = 0; // 0 -> 1
		int TN = 0; // 0 -> 0
		int FN = 0; // 1 -> 0

		for (String key : keys) {

			ArrayList<SimFinData> simFinData = all_data.get(key);

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

	private String evaluate_bnc(ArrayList<String> keys, HashMap<String, ArrayList<SimFinData>> buggy_data,
			HashMap<String, ArrayList<SimFinData>> clean_data, int kOffset, int k, double cutoff) {

		int TP = 0; // 1 -> 1
		int FP = 0; // 0 -> 1
		int TN = 0; // 0 -> 0
		int FN = 0; // 1 -> 0

		for (String key : keys) {

			ArrayList<SimFinData> simFinDataBuggy = buggy_data.get(key);
			ArrayList<SimFinData> simFinDataClean = clean_data.get(key);
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

//	private String evaluate_buggy(ArrayList<String> keys, HashMap<String, ArrayList<SimFinData>> data, int kOffset,
//			int k, double cutoff) {
//
//		int TP = 0; // 1 -> 1
//		int FP = 0; // 0 -> 1
//		int TN = 0; // 0 -> 0
//		int FN = 0; // 1 -> 0
//
//		for (String key : keys) {
//
//			ArrayList<SimFinData> simFinData = data.get(key);
//			double averagDistance = averageDistance(simFinData, kOffset, k);
//
//			int label = simFinData.get(0).getLabel();
//
//			if (label == 1) {
//				if (averagDistance < cutoff)
//					TP++;
//				else
//					FN++;
//			} else {
//				if (averagDistance < cutoff)
//					FP++;
//				else
//					TN++;
//			}
//
//		}
//
//		double precision = TP / ((double) TP + FP);
//		double recall = TP / ((double) TP + FN);
//		double f1 = (2 * precision * recall) / (precision + recall);
//		double mcc = (double) (TP * TN - FP * FN) / (Math.sqrt((double) (TP + FP) * (TP + FN) * (TN + FP) * (TN + FN)));
//		double TPR = TP / ((double) TP + FN);
//		double FPR = FP / ((double) FP + TN);
//
//		getTPR(TPR);
//		getFPR(FPR);
//
//		return TP + "," + FP + "," + FN + "," + TN + "," + precision + "," + recall + "," + f1 + "," + mcc + "," + TPR
//				+ "," + FPR;
//	}

//	private String evaluate_clean(ArrayList<String> keys, HashMap<String, ArrayList<SimFinData>> data, int kOffset,
//			int k, double cutoff) {
//
//		int TP = 0; // 1 -> 1
//		int FP = 0; // 0 -> 1
//		int TN = 0; // 0 -> 0
//		int FN = 0; // 1 -> 0
//
//		for (String key : keys) {
//
//			ArrayList<SimFinData> simFinData = data.get(key);
//			double averagDistance = averageDistance(simFinData, kOffset, k);
//
//			int label = simFinData.get(0).getLabel();
//
//			if (label == 1) {
//				if (averagDistance > cutoff)
//					TP++;
//				else
//					FN++;
//			} else {
//				if (averagDistance > cutoff)
//					FP++;
//				else
//					TN++;
//			}
//
//		}
//
//		double precision = TP / ((double) TP + FP);
//		double recall = TP / ((double) TP + FN);
//		double f1 = (2 * precision * recall) / (precision + recall);
//		double mcc = (double) (TP * TN - FP * FN) / (Math.sqrt((double) (TP + FP) * (TP + FN) * (TN + FP) * (TN + FN)));
//		double TPR = TP / ((double) TP + FN);
//		double FPR = FP / ((double) FP + TN);
//
//		getTPR(TPR);
//		getFPR(FPR);
//
//		return TP + "," + FP + "," + FN + "," + TN + "," + precision + "," + recall + "," + f1 + "," + mcc + "," + TPR
//				+ "," + FPR;
//	}

	private void run_tps(String[] args) {
		String resultFilePath = args[0]; // "data" + File.separator + folder + File.separator + "maven_result.csv";
		String projectName = args[1];
		initialCutoff = Double.parseDouble(args[2]);
		increment = Double.parseDouble(args[3]);
		maxCutoff = Double.parseDouble(args[4]);
		kOffset = Integer.parseInt(args[5]);
		maxK = kOffset; // just consider only one rank

		HashMap<String, ArrayList<SimFinData>> buggy_data = new HashMap<String, ArrayList<SimFinData>>();
		HashMap<String, ArrayList<SimFinData>> clean_data = new HashMap<String, ArrayList<SimFinData>>();

		Reader buggy_in;
		Reader clean_in;
		try {
			buggy_in = new FileReader(resultFilePath + projectName + "_buggy_result.csv");
			clean_in = new FileReader(resultFilePath + projectName + "_clean_result.csv");
			Iterable<CSVRecord> buggy_records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(buggy_in);
			Iterable<CSVRecord> clean_records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(clean_in);
			List<CSVRecord> buggy_list = new ArrayList<CSVRecord>();
			List<CSVRecord> clean_list = new ArrayList<CSVRecord>();
			Iterator<CSVRecord> iter_buggy = buggy_records.iterator();
			Iterator<CSVRecord> iter_clean = clean_records.iterator();
			while (iter_buggy.hasNext()) {
				buggy_list.add(iter_buggy.next());
			}
			while (iter_clean.hasNext()) {
				clean_list.add(iter_clean.next());
			}

			// data preparation for buggy instances.
			for (int i = 0; i < buggy_list.size(); i++) {
				String yBicSha = buggy_list.get(i).get(0);
				String yBicPath = buggy_list.get(i).get(1);
				String key = yBicSha + yBicPath;
				String yBfcSha = buggy_list.get(i).get(3);
				String yBfcPath = buggy_list.get(i).get(4);
				int yLabel = Integer.parseInt(buggy_list.get(i).get(6));
				int rank = Integer.parseInt(buggy_list.get(i).get(7));
				double dist = Double.parseDouble(buggy_list.get(i).get(8));
				String project = buggy_list.get(i).get(9);
				String yhBicSha = buggy_list.get(i).get(10);
				String yhBicPath = buggy_list.get(i).get(11);
				String yhBfcSha = buggy_list.get(i).get(13);
				String yhBfcPath = buggy_list.get(i).get(14);
				int yhLabel = Integer.parseInt(buggy_list.get(i).get(16));

				SimFinData buggy_record = new SimFinData();
				buggy_record.setYBicSha(yBicSha);
				buggy_record.setYBicPath(yBicPath);
				buggy_record.setKey(key);
				buggy_record.setYBfcSha(yBfcSha);
				buggy_record.setYBfcPath(yBfcPath);
				buggy_record.setYLabel(yLabel);
				buggy_record.setRank(rank);
				buggy_record.setDist(dist);
				buggy_record.setProject(project);
				buggy_record.setYhBicSha(yhBicSha);
				buggy_record.setYhBicPath(yhBicPath);
				buggy_record.setYhBfcSha(yhBfcSha);
				buggy_record.setYhBfcPath(yhBfcPath);
				buggy_record.setYhLabel(yhLabel);

				if (buggy_data.containsKey(key))
					buggy_data.get(key).add(buggy_record);
				else {
					ArrayList<SimFinData> newSimFinData = new ArrayList<SimFinData>();
					newSimFinData.add(buggy_record);
					buggy_data.put(key, newSimFinData);
				}
			}

			// data preparation for clean instances.
			ArrayList<String> keys = Lists.newArrayList(buggy_data.keySet());
			for (int i = 0; i < clean_list.size(); i++) {
				String yBicSha = buggy_list.get(i).get(0);
				String yBicPath = buggy_list.get(i).get(1);
				String key = yBicSha + yBicPath;
				String yBfcSha = buggy_list.get(i).get(3);
				String yBfcPath = buggy_list.get(i).get(4);
				int yLabel = Integer.parseInt(buggy_list.get(i).get(6));
				int rank = Integer.parseInt(buggy_list.get(i).get(7));
				double dist = Double.parseDouble(buggy_list.get(i).get(8));
				String project = buggy_list.get(i).get(9);
				String yhBicSha = buggy_list.get(i).get(10);
				String yhBicPath = buggy_list.get(i).get(11);
				String yhBfcSha = buggy_list.get(i).get(13);
				String yhBfcPath = buggy_list.get(i).get(14);
				int yhLabel = Integer.parseInt(buggy_list.get(i).get(16));

				SimFinData clean_record = new SimFinData();
				clean_record.setYBicSha(yBicSha);
				clean_record.setYBicPath(yBicPath);
				clean_record.setKey(key);
				clean_record.setYBfcSha(yBfcSha);
				clean_record.setYBfcPath(yBfcPath);
				clean_record.setYLabel(yLabel);
				clean_record.setRank(rank);
				clean_record.setDist(dist);
				clean_record.setProject(project);
				clean_record.setYhBicSha(yhBicSha);
				clean_record.setYhBicPath(yhBicPath);
				clean_record.setYhBfcSha(yhBfcSha);
				clean_record.setYhBfcPath(yhBfcPath);
				clean_record.setYhLabel(yhLabel);

				if (clean_data.containsKey(key))
					clean_data.get(key).add(clean_record);
				else {
					ArrayList<SimFinData> newSimFinData = new ArrayList<SimFinData>();
					newSimFinData.add(clean_record);
					clean_data.put(key, newSimFinData);
				}
			}

			getTPs(keys, buggy_data, clean_data);

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

//	private double averageDistance(ArrayList<SimFinData> simFinData, int kOffset, int k, boolean isBuggyDataIterating) {
//		int label = simFinData.get(0).getYLabel();
//		double sum = 0.0;
//		// kOffset = 2 // k = 2
////		try {
//		if ((isBuggyDataIterating && label == 0) || (!isBuggyDataIterating && label == 1)) {
//			kOffset = 1;
//			k = k - 1;
//		}
//		for (int i = kOffset - 1; i < k; i++) {
//			sum = sum + simFinData.get(i).getDist();
//		}
////		} catch (Exception e) {
////			System.out.println(simFinData.get(0).getKey());
////		}
//		return sum / (k - kOffset + 1);
//	}

}
