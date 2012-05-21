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

/**
 * Begin running tests.
 *
 * @author Ray Zimmerman
 * @author Richard Lincoln
 *
 */
public class Djp_t_begin {

	public static void t_begin(int num_of_tests) {
		t_begin(num_of_tests, false);
	}

	/**
	 * Initializes the global test counters,
	 * setting everything up to execute NUM_OF_TESTS tests using T_OK
	 * and T_IS. If QUIET is true, it will not print anything for the
	 * individual tests, only a summary when T_END is called.
	 *
	 * @param quiet
	 */
	public static void t_begin(int num_of_tests, boolean quiet) {
		TestGlobals.t_quiet = quiet;
		TestGlobals.t_num_of_tests = num_of_tests;
		TestGlobals.t_counter = 1;
		TestGlobals.t_ok_cnt = 0;
		TestGlobals.t_not_ok_cnt = 0;
		TestGlobals.t_skip_cnt = 0;
		TestGlobals.t_clock = System.currentTimeMillis();

		if (TestGlobals.t_quiet == false)
			System.out.printf("1..%d\n", num_of_tests);
	}
}
