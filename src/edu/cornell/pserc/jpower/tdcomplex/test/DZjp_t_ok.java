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
 * Tests if a condition is true.
 *
 * @author Ray Zimmerman (rz10@cornell.edu)
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class DZjp_t_ok {

	public static void jp_t_ok(boolean cond) {
		jp_t_ok(cond, null);
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
	public static void jp_t_ok(boolean cond, String msg) {
		if (msg == null) {
			msg = "";
		} else {
			msg = " - " + msg;
		}

		if (cond) {
			DZjp_t_begin.t_ok_cnt += 1;
		} else {
			DZjp_t_begin.t_not_ok_cnt += 1;
			if (!DZjp_t_begin.t_quiet)
				System.out.printf("not ");
		}

		if (!DZjp_t_begin.t_quiet)
			System.out.printf("ok %d%s\n", DZjp_t_begin.t_counter, msg);

		DZjp_t_begin.t_counter += 1;
	}

}
