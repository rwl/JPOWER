/*
 * Copyright (C) 1996-2010 Power System Engineering Research Center (PSERC)
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

package edu.cornell.pserc.jpower.tdouble.test;

import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.algo.DoubleProperty;

/**
 * Tests if two matrices are identical to some tolerance.
 *
 * @author Ray Zimmerman (rz10@cornell.edu)
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class Djp_t_is {

	public static void jp_t_is(DoubleMatrix2D got, DoubleMatrix2D expected) {
		jp_t_is(got, expected, 5);
	}

	public static void jp_t_is(DoubleMatrix2D got, DoubleMatrix2D expected, double prec) {
		jp_t_is(got, expected, prec, "");
	}

	/**
	 * Increments the global test count
	 * and if the maximum difference between corresponding elements of
	 * GOT and EXPECTED is less than 10^(-PREC) then it increments the
	 * passed tests count, otherwise increments the failed tests count.
	 * Prints 'ok' or 'not ok' followed by the MSG, unless the global
	 * variable t_quiet is true. Intended to be called between calls to
	 * T_BEGIN and T_END.
	 *
	 * @param got
	 * @param expected
	 * @param prec
	 * @param msg
	 */
	public static void jp_t_is(DoubleMatrix2D got, DoubleMatrix2D expected, double prec, String msg) {
		DoubleProperty prop = new DoubleProperty(Math.pow(10, -prec));
		boolean condition = prop.equals(got, expected);

		Djp_t_ok.jp_t_ok(condition, msg);
//		if (!condition && !Djp_t_begin.t_quiet) {
//			if (max_diff != 0.0) {
//				System.out.printf("max diff = %g (allowed tol = %g)\n\n", max_diff, Math.pow(10, -prec));
//			} else {
//				System.out.printf("    dimension mismatch:\n");
//				System.out.printf("             got: %d x %d\n", size(got));
//				System.out.printf("        expected: %d x %d\n\n", size(expected));
//			}
//		}
	}
}
