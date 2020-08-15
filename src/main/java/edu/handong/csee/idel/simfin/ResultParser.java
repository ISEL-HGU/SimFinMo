package edu.handong.csee.idel.simfin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

public class ResultParser {
	
	enum Measure{Precision,Recall, F1, TP};
	//Measure measure = Measure.TP;
	double initialCutoff = 0.001;
	double increment = 0.01;
	double maxCutoff = 3.0;
	//String folder = "";
	//String folder = "nonezero";
	
	int kOffset = 1;
	int maxK = 1;

	public static void main(String[] args) {
		
		new ResultParser().run(args);

	}

	private void run(String[] args) {
		String resultFilePath = args[0]; //"data" + File.separator + folder + File.separator + "maven_result.csv";
		initialCutoff = Double.parseDouble(args[1]);
		increment = Double.parseDouble(args[2]);
		maxCutoff = Double.parseDouble(args[3]);
		kOffset = Integer.parseInt(args[4]);
		maxK = kOffset; // just consider only one rank
		
		HashMap<String,ArrayList<SimFinData>> data = new HashMap<String,ArrayList<SimFinData>>();
		ArrayList<String> keys = new ArrayList<String>();
		
		Reader in;
		try {
			in = new FileReader(resultFilePath);
			Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(in);
			for (CSVRecord record : records) {
			    String commitID = record.get(0);
			    String sourceFilePath = record.get(1);
			    String key = commitID+sourceFilePath;
			    keys.add(key);
			    
			    int rank = Integer.parseInt(record.get(6));
			    Double distance = Double.parseDouble(record.get(7));
			    int label = Integer.parseInt(record.get(9));
			    
			    SimFinData newRecord = new SimFinData(key, rank, distance, label);
			    
			    if(data.containsKey(key))
			    	data.get(key).add(newRecord);
			    else {
			    	ArrayList<SimFinData> newSimFinData = new ArrayList<SimFinData>();
			    	newSimFinData.add(newRecord);
			    	data.put(key, newSimFinData);
			    }
			}
			
			// header
			System.out.print("cutoff-rank" + kOffset + ",TP,FP,FN,TN,precision,recall,f1,mcc");	
			
			for(double cutoff = initialCutoff; cutoff <= maxCutoff; cutoff=cutoff+increment) {
				//System.out.println(cutoff);
				
				System.out.print(cutoff);
				for(int k=kOffset; k <= maxK; k++) {
					//System.out.println(k);
					System.out.print("," + evaluate(keys, data, kOffset,k, cutoff));
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

	private String evaluate(ArrayList<String> keys, HashMap<String, ArrayList<SimFinData>> data, int kOffset, int k, double cutoff) {

		int TP = 0; // 1 -> 1
		int FP = 0; // 0 -> 1
		int TN = 0; // 0 -> 0
		int FN = 0; // 1 -> 0
		
		for(String key:keys) {
			
			ArrayList<SimFinData> simFinData = data.get(key);			
			double averagDistance = averageDistance(simFinData, kOffset, k);
			
			int label = simFinData.get(0).getLabel();
			
			if(label == 1) {
				if(averagDistance<cutoff)
					TP++;
				else
					FN++;
			}
			else {
				if(averagDistance<cutoff)
					FP++;
				else
					TN++;
			}
			
		}
		
		double precision = TP/((double)TP+FP);
		double recall = TP/((double)TP+FN);
		double f1 = (2*precision*recall)/(precision+recall);
		double mcc = (double)(TP*TN-FP*FN)/(Math.sqrt((double)(TP+FP)*(TP+FN)*(TN+FP)*(TN+FN)));
		
		return TP + "," + FP + "," + FN + "," + TN + "," + precision + "," + recall +"," + f1+"," + mcc;
	}

	private double averageDistance(ArrayList<SimFinData> simFinData, int kOffset, int k) {
		
		double sum = 0.0;
		
		for(int i=kOffset-1; i < k; i++) {
			sum = sum + simFinData.get(i).getDistance();
		}
		
		return sum/(k-kOffset+1);
	}

}
