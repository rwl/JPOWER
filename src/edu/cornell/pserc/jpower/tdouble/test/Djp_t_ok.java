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

import static edu.cornell.pserc.jpower.tdouble.test.TestGlobals.t_quiet;
import static edu.cornell.pserc.jpower.tdouble.test.TestGlobals.t_counter;
import static edu.cornell.pserc.jpower.tdouble.test.TestGlobals.t_ok_cnt;
import static edu.cornell.pserc.jpower.tdouble.test.TestGlobals.t_not_ok_cnt;

/**
 * Tests if a condition is true.
 *
 * @author Ray Zimmerman
 * @author Richard Lincoln
 *
 */
public class Djp_t_ok {

	public static void t_ok(boolean cond) {
		t_ok(cond, null);
	}

	/**
	 * Increments the global test count and if the EXPR
	 * is true it increments the passed tests count, otherwise increments
	 * the failed tests count. Prints 'ok' or 'not ok' followed by the
	 * MSG, unless the global variable t_quiet is true. Intended to be
	 * called between calls to T_BEGIN and T_END.
	 *
	 * @param cond
	 * @param msg
	 */
	public static void t_ok(boolean cond, String msg) {
		if (msg == null) {
			msg = "";
		} else {
			msg = " - " + msg;
		}

		if (cond) {
			t_ok_cnt += 1;
		} else {
			t_not_ok_cnt += 1;
			if (!t_quiet)
				System.out.printf("not ");
		}

		if (!t_quiet)
			System.out.printf("ok %d%s\n", t_counter, msg);

		t_counter += 1;
	}

}
