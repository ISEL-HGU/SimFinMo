package edu.handong.csee.idel.simfin;

import static org.junit.Assert.*;

import org.junit.Test;

public class ResultParserTest {

	@Test
	public void testOpennlp() {
		
		String[] args = {"data/opennlp_result.csv", "0.01", "0.01", "1", "2"}; // 1: data path, 2: initial cutoff, 3: cutoff increment, 4: max cutoff, 5: target rank
		
		ResultParser.main(args);
		
	}
	
	@Test
	public void testSentry() {
		
		String[] args = {"data/sentry_result.csv", "0.01", "0.01", "1", "2"}; // 1: data path, 2: initial cutoff, 3: cutoff increment, 4: max cutoff, 5: target rank
		
		ResultParser.main(args);
		
	}

}
