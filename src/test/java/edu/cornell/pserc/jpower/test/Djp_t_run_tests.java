/*
 * Copyright (C) 1996-2010 Power System Engineering Research Center
 * Copyright (C) 2010-2011 Richard Lincoln
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package edu.cornell.pserc.jpower.test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import cern.colt.matrix.tint.IntFactory1D;
import cern.colt.matrix.tint.IntMatrix1D;
import cern.jet.math.tint.IntFunctions;

public class Djp_t_run_tests {

	private static final IntFunctions ifunc = IntFunctions.intFunctions;

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
			run_test(test_names.get(k), verbose);

		    num_of_tests    = num_of_tests  + TestGlobals.t_num_of_tests;
		    counter         = counter       + TestGlobals.t_counter;
		    ok_cnt          = ok_cnt        + TestGlobals.t_ok_cnt;
		    not_ok_cnt      = not_ok_cnt    + TestGlobals.t_not_ok_cnt;
		    skip_cnt        = skip_cnt      + TestGlobals.t_skip_cnt;
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

	private static void run_test(String name, boolean verbose) {
		ClassLoader classLoader = Djp_t_run_tests.class.getClassLoader();

		try {
			Class<?> testClass = classLoader.loadClass("edu.cornell.pserc.jpower.test.Djp_" + name);

			try {
				Method func = testClass.getMethod(name, Boolean.TYPE);
				func.invoke(testClass, !verbose);
			} catch (SecurityException e) {
				System.out.println("Failed to get test function: " + name);
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				System.out.println("Failed to get test function: " + name);
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				System.out.println("Failed to invoke test function: " + name);
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				System.out.println("Failed to invoke test function: " + name);
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				System.out.println("Failed to invoke test function: " + name);
				e.printStackTrace();
			}
		} catch (ClassNotFoundException e) {
			System.out.println("Failed to load test: " + name);
			e.printStackTrace();
		}
	}

}
