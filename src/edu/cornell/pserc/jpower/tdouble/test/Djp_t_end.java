/*
 * Copyright (C) 1996-2010 Power System Engineering Research Center
 * Copyright (C) 2010-2011 Richard Lincoln
 *
 * JPOWER is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * JPOWER is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JPOWER. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package edu.cornell.pserc.jpower.tdouble.test;

public class Djp_t_end {

	public static void jp_t_end() {
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
		    		(System.currentTimeMillis() - TestGlobals.t_clock) / 1000F);
		}
	}

}
