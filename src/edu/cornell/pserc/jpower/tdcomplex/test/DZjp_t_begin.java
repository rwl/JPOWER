/*
 * Copyright (C) 1996-2010 by Power System Engineering Research Center (PSERC)
 * Copyright (C) 2010 Richard Lincoln
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 *
 */

package edu.cornell.pserc.jpower.tdcomplex.test;

/**
 * Begin running tests.
 *
 * @author Ray Zimmerman (rz10@cornell.edu)
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class DZjp_t_begin {

	public static boolean t_quiet;
	public static int t_num_of_tests;
	public static int t_counter;
	public static int t_ok_cnt;
	public static int t_not_ok_cnt;
	public static int t_skip_cnt;
	public static double t_clock;

	public static void jp_t_begin(int num_of_tests) {
		jp_t_begin(num_of_tests, false);
	}

	/**
	 * Initializes the global test counters,
	 * setting everything up to execute NUM_OF_TESTS tests using T_OK
	 * and T_IS. If QUIET is true, it will not print anything for the
	 * individual tests, only a summary when T_END is called.
	 *
	 * @param quiet
	 */
	public static void jp_t_begin(int num_of_tests, boolean quiet) {
		t_quiet = quiet;
		t_num_of_tests = num_of_tests;
		t_counter = 1;
		t_ok_cnt = 0;
		t_not_ok_cnt = 0;
		t_skip_cnt = 0;
		t_clock = System.currentTimeMillis() / 1000F;

		if (t_quiet == false)
			System.out.printf("1..%d\n", num_of_tests);
	}
}
