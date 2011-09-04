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

public class Djp_t_skip {

	public static void t_skip(int cnt) {
		t_skip(cnt, "");
	}

	/**
	 * Skips a number of tests.
	 *
	 * @param cnt
	 * @param msg
	 */
	public static void t_skip(int cnt, String msg) {
		if (msg.length() > 0)
			msg = " : " + msg;

		TestGlobals.t_skip_cnt = TestGlobals.t_skip_cnt + cnt;
		if (!TestGlobals.t_quiet)
		    System.out.printf("skipped tests %d..%d%s\n",
		    		TestGlobals.t_counter, TestGlobals.t_counter + cnt-1, msg);

		TestGlobals.t_counter = TestGlobals.t_counter + cnt;
	}

}
