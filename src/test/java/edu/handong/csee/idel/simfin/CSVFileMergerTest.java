package edu.handong.csee.idel.simfin;

import static org.junit.Assert.*;

import org.junit.Test;

public class CSVFileMergerTest {

	@Test
	public void test() {
		String[] args = {"data/train_buggy_encoded.csv","data/train_clean_partial_encoded.csv"}; // 1: data path, 2: initial cutoff, 3: cutoff increment, 4: max cutoff, 5: target rank
		
		CSVFileMerger.main(args);
	}

}
