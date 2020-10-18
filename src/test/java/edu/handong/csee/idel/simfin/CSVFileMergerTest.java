package edu.handong.csee.idel.simfin;

import org.junit.Test;

public class CSVFileMergerTest {

	@Test
	public void test() {
		String[] args = {
				"/Users/jihoshin/PatchSuggestionTool/ChangeVectorCollector/assets/rm_zero/out/out1/out/buggy_syncope_encoded.csv",
				"/Users/jihoshin/PatchSuggestionTool/ChangeVectorCollector/assets/rm_zero/out/out1/out/clean_syncope_encoded.csv" };

		CSVFileMerger.main(args);
	}

}
