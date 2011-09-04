package edu.cornell.pserc.jpower.tdouble.test;

import java.util.List;

import cern.colt.matrix.tint.IntFactory1D;
import cern.colt.matrix.tint.IntMatrix1D;

import static edu.cornell.pserc.jpower.tdouble.test.TestGlobals.t_num_of_tests;
import static edu.cornell.pserc.jpower.tdouble.test.TestGlobals.t_counter;
import static edu.cornell.pserc.jpower.tdouble.test.TestGlobals.t_ok_cnt;
import static edu.cornell.pserc.jpower.tdouble.test.TestGlobals.t_not_ok_cnt;
import static edu.cornell.pserc.jpower.tdouble.test.TestGlobals.t_skip_cnt;

import static cern.colt.util.tdouble.Djp_util.ifunc;

public class Djp_t_run_tests {

	public static void t_run_tests(List<String> test_names) {
		t_run_tests(test_names, false);
	}

	@SuppressWarnings("static-access")
	public static void t_run_tests(List<String> test_names, boolean verbose) {
		IntMatrix1D len;
		int maxlen = 0, num_of_tests, counter, ok_cnt, not_ok_cnt, skip_cnt, pad;
		long t0;

		/* figure out padding for printing */
		if (!verbose) {
			len = IntFactory1D.dense.make(test_names.size());
			for (int k = 0; k < test_names.size(); k++)
				len.set(k, test_names.get(k).length());
			maxlen = len.aggregate(ifunc.min, ifunc.identity);
		}

		/* initialize statistics */
		num_of_tests = 0;
		counter = 0;
		ok_cnt = 0;
		not_ok_cnt = 0;
		skip_cnt = 0;

		t0 = System.currentTimeMillis();
		for (int k = 0; k < test_names.size(); k++) {
			if (verbose) {
				System.out.printf("\n----------  %s  ----------\n", test_names.get(k));
			} else {
				pad = maxlen + 4 - test_names.get(k).length();
				System.out.print(String.format("%s", test_names.get(k)));
				for (int m = 0; m < pad; m++)
					System.out.print(".");
			}
			//feval( test_names{k}, ~verbose );

			num_of_tests    = num_of_tests  + t_num_of_tests;
			counter         = counter       + t_counter;
			ok_cnt          = ok_cnt        + t_ok_cnt;
			not_ok_cnt      = not_ok_cnt    + t_not_ok_cnt;
			skip_cnt        = skip_cnt      + t_skip_cnt;
		}

		if (verbose)
			System.out.print("\n\n----------  Summary  ----------\n");

		if ((counter == num_of_tests) & (counter == ok_cnt + skip_cnt) & (not_ok_cnt == 0)) {
			if (skip_cnt > 0) {
				System.out.printf("All tests successful (%d passed, %d skipped of %d)",
						ok_cnt, skip_cnt, num_of_tests);
			} else {
				System.out.printf("All tests successful (%d of %d)", ok_cnt, num_of_tests);
			}
		} else {
			System.out.printf("Ran %d of %d tests: %d passed, %d failed",
					counter, num_of_tests, ok_cnt, not_ok_cnt);
			if (skip_cnt > 0)
				System.out.printf(", %d skipped", skip_cnt);
		}
		System.out.printf("\nElapsed time %.2f seconds.\n", ((System.currentTimeMillis() - t0) / 1000F));
	}

}
