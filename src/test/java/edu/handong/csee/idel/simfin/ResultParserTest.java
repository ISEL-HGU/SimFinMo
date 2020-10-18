package edu.handong.csee.idel.simfin;

import org.junit.Test;

public class ResultParserTest {

	@Test
	public void testJena() {
		// 1: data path, 2: initial cutoff, 3: cutoff increment, 4: max cutoff, 5:
		// target rank
		String[] args = { "/Users/jihoshin/Desktop/eval_rm_dups/", "jena", "0.0000001", "0.0000005", "0.00001", "2" };

		ResultParser.main(args);

	}
	
	@Test
	public void testMaven() {
		// 1: data path, 2: initial cutoff, 3: cutoff increment, 4: max cutoff, 5:
		// target rank
		String[] args = { "/Users/jihoshin/Desktop/eval_bnc/", "maven", "0.001", "0.001", "1", "2" };

		ResultParser.main(args);

	}

	@Test
	public void testRanger() {
		// 1: data path, 2: initial cutoff, 3: cutoff increment, 4: max cutoff, 5:
		// target rank
		String[] args = { "/Users/jihoshin/Desktop/eval_bnc/", "ranger", "0.001", "0.001", "1", "2" };

		ResultParser.main(args);

	}

	@Test
	public void testSentry() {
		// 1: data path, 2: initial cutoff, 3: cutoff increment, 4: max cutoff, 5:
		// target rank
		String[] args = { "/Users/jihoshin/Desktop/eval_bnc/", "sentry", "0.001", "0.001", "1", "2" };

		ResultParser.main(args);

	}

	@Test
	public void testSqoop() {
		// 1: data path, 2: initial cutoff, 3: cutoff increment, 4: max cutoff, 5:
		// target rank
		String[] args = { "/Users/jihoshin/Desktop/eval_bnc/", "sqoop", "0.001", "0.001", "1", "2" };

		ResultParser.main(args);

	}

	@Test
	public void testSyncope() {
		// 1: data path, 2: initial cutoff, 3: cutoff increment, 4: max cutoff, 5:
		// target rank
		String[] args = { "/Users/jihoshin/Desktop/eval_bnc/", "syncope", "0.001", "0.001", "1", "2" };

		ResultParser.main(args);

	}

	@Test
	public void testTez() {
		// 1: data path, 2: initial cutoff, 3: cutoff increment, 4: max cutoff, 5:
		// target rank
		String[] args = { "/Users/jihoshin/Desktop/eval_bnc/", "tez", "0.001", "0.001", "1", "2" };

		ResultParser.main(args);

	}

}
