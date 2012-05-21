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

public class Djp_t_end {

	public static void t_end() {
		boolean all_ok;

		TestGlobals.t_counter = TestGlobals.t_counter - 1;

		if ((TestGlobals.t_counter == TestGlobals.t_num_of_tests) &
				(TestGlobals.t_counter == TestGlobals.t_ok_cnt + TestGlobals.t_skip_cnt) &
		        (TestGlobals.t_not_ok_cnt == 0)) {
		    all_ok = true;
		} else {
		    all_ok = false;
		}

		if (TestGlobals.t_quiet) {
		    if (all_ok) {
		    	System.out.print("ok");
		        if (TestGlobals.t_skip_cnt > 0) {
		        	System.out.printf(" (%d of %d skipped)",
		        			TestGlobals.t_skip_cnt,
		        			TestGlobals.t_num_of_tests);
		        }
		    } else {
		    	System.out.print("not ok\n");
		    	System.out.printf("\t#####  Ran %d of %d tests: %d passed, %d failed",
		    			TestGlobals.t_counter, TestGlobals.t_num_of_tests,
		    			TestGlobals.t_ok_cnt, TestGlobals.t_not_ok_cnt);
		        if (TestGlobals.t_skip_cnt > 0) {
		        	System.out.printf(", %d skipped", TestGlobals.t_skip_cnt);
		        }
		    }
		    System.out.print("\n");
		} else {
		    if (all_ok) {
		        if (TestGlobals.t_skip_cnt > 0) {
		        	System.out.printf("All tests successful (%d passed, %d skipped of %d)",
		            		TestGlobals.t_ok_cnt, TestGlobals.t_skip_cnt, TestGlobals.t_num_of_tests);
		        } else {
		        	System.out.printf("All tests successful (%d of %d)",
		        			TestGlobals.t_ok_cnt, TestGlobals.t_num_of_tests);
		        }
		    } else {
		    	System.out.printf("Ran %d of %d tests: %d passed, %d failed",
		        		TestGlobals.t_counter, TestGlobals.t_num_of_tests,
		        		TestGlobals.t_ok_cnt, TestGlobals.t_not_ok_cnt);
		        if (TestGlobals.t_skip_cnt > 0) {
		        	System.out.printf(", %d skipped", TestGlobals.t_skip_cnt);
		        }
		    }
		    System.out.printf("\nElapsed time %.2f seconds.\n",
		    		((System.currentTimeMillis() - TestGlobals.t_clock) / 1000F));
		}
	}

}
