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
	Measure measure = Measure.TP;
	double initialCutoff = 0.001;
	double increment = 0.01;
	double maxCutoff = 1.0;
	String folder = "";
	//String folder = "nonezero";
	
	int kOffset = 2;
	int maxK = 2;

	public static void main(String[] args) {
		
		new ResultParser().run();

	}

	private void run() {
		String resultFilePath = "data" + File.separator + folder + File.separator + "opennlp_result.csv";
		
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
			
			System.out.print("cutoff");
			for(int k=kOffset; k <= maxK; k++) {
				System.out.print("," + k);	
			}
			System.out.println();	
			
			for(double cutoff = initialCutoff; cutoff <= maxCutoff; cutoff=cutoff+increment) {
				//System.out.println(cutoff);
				
				System.out.print(cutoff);
				for(int k=kOffset; k <= maxK; k++) {
					//System.out.println(k);
					System.out.print("," + evaluate(keys, data, kOffset,k, cutoff, measure));
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

	private String evaluate(ArrayList<String> keys, HashMap<String, ArrayList<SimFinData>> data, int kOffset, int k, double cutoff, Measure type) {

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
		double mcc = (TP*TN-FP*FN)/Math.sqrt((TP+FP)*(TP+FN)*(TN+FP)*(TN+FN));
		
		if(type == Measure.F1)
			return f1 + "";
		
		if(type == Measure.Precision)
			return precision + "" + "";
		
		if(type == Measure.Recall)
			return recall + "";
		
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
