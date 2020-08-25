package edu.handong.csee.idel.simfin;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Scanner;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

public class CSVFileMerger {

	public static void main(String[] args) {
		new CSVFileMerger().run(args);

	}

	private void run(String[] args) {
		String buggyFile = args[0];
		String cleanFile = args[1];
		
		ArrayList<String> buggyLines = getLines(buggyFile,false);
		ArrayList<String> cleanLines = getLines(cleanFile,false);
		
		addLabel(buggyLines, "buggy");
		addLabel(cleanLines, "clean");
		
		removeFirstColumn(buggyLines);
		removeFirstColumn(cleanLines);
		
		merge(buggyLines,cleanLines);
		
		
	}



	private void merge(ArrayList<String> buggyLines, ArrayList<String> cleanLines) {
		
		cleanLines.remove(0);
		
		for(String line:cleanLines) {
			buggyLines.add(line);
		}
		
		writeAFile(buggyLines,"merged.csv");
	}
	
	public static void writeAFile(ArrayList<String> lines, String targetFileName){
		try {
			File file= new File(targetFileName);
			FileOutputStream fos = new FileOutputStream(file);
			DataOutputStream dos=new DataOutputStream(fos);
			
			for(String line:lines){
				dos.write((line+"\n").getBytes());
			}
			//dos.writeBytes();
			dos.close();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

	private void removeFirstColumn(ArrayList<String> lines) {
		for(int i=0; i<lines.size();i++) {
			lines.set(i, lines.get(i).substring(lines.get(i).indexOf(",")+1));
		}
	}

	private void addLabel(ArrayList<String> lines, String label) {
		
		lines.set(0, lines.get(0) + ",label");

		for(int i=1; i<lines.size();i++) {
			lines.set(i, lines.get(i) + "," + label);
		}
		
	}

	public ArrayList<String> getLines(String file,boolean removeHeader){
		ArrayList<String> lines = new ArrayList<String>();
		String thisLine="";
		//Open the file for reading
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			while ((thisLine = br.readLine()) != null) { // while loop begins here
				lines.add(thisLine);
			} // end while 
			br.close();
		} // end try
		catch (IOException e) {
			System.err.println("Error: " + e);
			//System.exit(0);
		}
		
		if(removeHeader)
			lines.remove(0);
		
		return lines;
	}
}
