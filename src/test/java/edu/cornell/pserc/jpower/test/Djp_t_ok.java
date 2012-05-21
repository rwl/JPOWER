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
			TestGlobals.t_ok_cnt += 1;
		} else {
			TestGlobals.t_not_ok_cnt += 1;
			if (!TestGlobals.t_quiet)
				System.out.printf("not ");
		}

		if (!TestGlobals.t_quiet)
			System.out.printf("ok %d%s\n", TestGlobals.t_counter, msg);

		TestGlobals.t_counter += 1;
	}

}
