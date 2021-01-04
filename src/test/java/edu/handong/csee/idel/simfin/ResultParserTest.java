package edu.handong.csee.idel.simfin;

import java.io.IOException;

import org.junit.Test;

public class ResultParserTest {

	@Test
	public void testJena() throws IOException {
		// 1: data path, 2: initial cutoff, 3: cutoff increment, 4: max cutoff, 5:
		// target rank
		String[] args = { "/Users/jihoshin/Desktop/eval_rm_dups/", "jena", "0.0000001", "0.0000005", "0.00001", "2" };

		ResultParser.main(args);

	}
	
	@Test
	public void testMaven() throws IOException {
		// 1: data path, 2: initial cutoff, 3: cutoff increment, 4: max cutoff, 5:
		// target rank
		String[] args = { "/Users/jihoshin/Desktop/eval_bnc/", "maven", "0.001", "0.001", "1", "2" };

		ResultParser.main(args);

	}

	@Test
	public void testRanger() throws IOException {
		// 1: data path, 2: initial cutoff, 3: cutoff increment, 4: max cutoff, 5:
		// target rank
		String[] args = { "/Users/jihoshin/Desktop/eval_bnc/", "ranger", "0.001", "0.001", "1", "2" };

		ResultParser.main(args);

	}

	@Test
	public void testSentry() throws IOException {
		// 1: data path, 2: initial cutoff, 3: cutoff increment, 4: max cutoff, 5:
		// target rank
		String[] args = { "/Users/jihoshin/Desktop/eval_bnc/", "sentry", "0.001", "0.001", "1", "2" };

		ResultParser.main(args);

	}

	@Test
	public void testSqoop() throws IOException {
		// 1: data path, 2: initial cutoff, 3: cutoff increment, 4: max cutoff, 5:
		// target rank
		String[] args = { "/Users/jihoshin/Desktop/eval_bnc/", "sqoop", "0.001", "0.001", "1", "2" };

		ResultParser.main(args);

	}

	@Test
	public void testSyncope() throws IOException {
		// 1: data path, 2: initial cutoff, 3: cutoff increment, 4: max cutoff, 5:
		// target rank
		String[] args = { "/Users/jihoshin/Desktop/eval_bnc/", "syncope", "0.001", "0.001", "1", "2" };

		ResultParser.main(args);

	}

	@Test
	public void testTez() throws IOException {
		// 1: data path, 2: initial cutoff, 3: cutoff increment, 4: max cutoff, 5:
		// target rank
		String[] args = { "/Users/jihoshin/Desktop/eval_bnc/", "tez", "0.001", "0.001", "1", "2" };

		ResultParser.main(args);

	}

}
