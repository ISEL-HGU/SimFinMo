package edu.handong.csee.idel.simfin;

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
	// String folder = "";
	// String folder = "nonezero";
	ArrayList<Double> TPR_list = new ArrayList<>();
	ArrayList<Double> FPR_list = new ArrayList<>();

	int kOffset = 2;
	int maxK = 2;

	public static void main(String[] args) {

		new ResultParser().run(args);

	}

	private void run(String[] args) {
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
				String commitID = buggy_list.get(i).get(0);
				String sourceFilePath = buggy_list.get(i).get(1);
				String key = commitID + sourceFilePath;
				String similarSHA = buggy_list.get(i).get(10);
				String similarPath = buggy_list.get(i).get(11);

				int buggy_rank = Integer.parseInt(buggy_list.get(i).get(6));
				Double buggy_distance = Double.parseDouble(buggy_list.get(i).get(7));

				int label = Integer.parseInt(buggy_list.get(i).get(9));

				SimFinData buggy_record = new SimFinData(key, buggy_rank, buggy_distance, label);
				buggy_record.setSimilarChangeSHA(similarSHA);
				buggy_record.setSimilarChangePath(similarPath);
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
				String commitID = clean_list.get(i).get(0);
				String sourceFilePath = clean_list.get(i).get(1);
				String key = commitID + sourceFilePath;
				String similarSHA = clean_list.get(i).get(10);
				String similarPath = clean_list.get(i).get(11);

				int clean_rank = Integer.parseInt(clean_list.get(i).get(6));
				Double clean_distance = Double.parseDouble(clean_list.get(i).get(7));
				int label = Integer.parseInt(clean_list.get(i).get(9));

				SimFinData clean_record = new SimFinData(key, clean_rank, clean_distance, label);
				clean_record.setSimilarChangeSHA(similarSHA);
				clean_record.setSimilarChangePath(similarPath);

				if (clean_data.containsKey(key))
					clean_data.get(key).add(clean_record);
				else {
					ArrayList<SimFinData> newSimFinData = new ArrayList<SimFinData>();
					newSimFinData.add(clean_record);
					clean_data.put(key, newSimFinData);
				}
			}

			// writing the evalated scores
			// System.out.println("cutoff-rank" + kOffset + ",precision,recall,f1,mcc");
			for (double cutoff = initialCutoff; cutoff <= maxCutoff; cutoff = cutoff + increment) {
				// System.out.println(cutoff);
				System.out.print(cutoff);
				for (int k = kOffset; k <= maxK; k++) {
					// System.out.println(k);
					System.out.print("," + evaluate_bnc(keys, buggy_data, clean_data, kOffset, k, cutoff));
//					System.out.print("," + evaluate_clean(keys, clean_data, kOffset, k, cutoff));
//					System.out.print("," + evaluate_buggy(keys, buggy_data, kOffset, k, cutoff));
				}
				System.out.println();
			}

//			System.out.println("max_p," + res.max_p + ",cutoff," + res.cutoff_p);
//			System.out.println("max_r," + res.max_r + ",cutoff," + res.cutoff_r);
//			System.out.println("max_f1," + res.max_f1 + ",cutoff," + res.cutoff_f1);
//			System.out.println("max_mcc," + res.max_mcc + ",cutoff," + res.cutoff_mcc);

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
			int label = simFinDataBuggy.get(0).getLabel();

			double averageDistanceBuggy = averageDistance(simFinDataBuggy, 2, 3, true);
			double averageDistanceClean = averageDistance(simFinDataClean, 2, 24, false);

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
//		double TPR = TP / ((double) TP + FN);
//		double FPR = FP / ((double) FP + TN);
//
//		getTPR(TPR);
//		getFPR(FPR);

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

	private double averageDistance(ArrayList<SimFinData> simFinData, int kOffset, int k, boolean isBuggyDataIterating) {
		int label = simFinData.get(0).getLabel();
		double sum = 0.0;
		// kOffset = 2 // k = 2
//		try {
		if ((isBuggyDataIterating && label == 0) || (!isBuggyDataIterating && label == 1) ) {
			kOffset = 1;
			k = k - 1;
		}
		for (int i = kOffset - 1; i < k; i++) {
			sum = sum + simFinData.get(i).getDistance();
		}
//		} catch (Exception e) {
//			System.out.println(simFinData.get(0).getKey());
//		}
		return sum / (k - kOffset + 1);
	}

}
