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

import cern.colt.matrix.tdcomplex.DComplexMatrix2D;
import cern.colt.matrix.tdcomplex.algo.DComplexProperty;
import cern.colt.matrix.tdouble.DoubleFactory1D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.algo.DoubleProperty;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_areas;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_branch;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_bus;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_gen;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_gencost;

/**
 * Tests if two matrices are identical to some tolerance.
 *
 * @author Ray Zimmerman
 * @author Richard Lincoln
 *
 */
public class Djp_t_is {

	public static void jp_t_is(DoubleMatrix2D got, DoubleMatrix2D expected) {
		jp_t_is(got, expected, 5);
	}

	public static void jp_t_is(DoubleMatrix2D got, DoubleMatrix2D expected, int prec) {
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
	public static void jp_t_is(DoubleMatrix2D got, DoubleMatrix2D expected, int prec, String msg) {
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

	public static void jp_t_is(DoubleMatrix1D got, double[] expected, int prec, String msg) {
		DoubleProperty prop = new DoubleProperty(Math.pow(10, -prec));
		DoubleMatrix1D expected_matrix = DoubleFactory1D.dense.make(expected);
		Djp_t_ok.jp_t_ok(prop.equals(got, expected_matrix), msg);
	}

	public static void jp_t_is(double got, double expected, int prec, String msg) {
		Djp_t_ok.jp_t_ok(got == expected, msg);
	}

	public static void jp_t_is(Djp_bus got, Djp_bus expected, int prec, String msg) {
		DoubleProperty prop = new DoubleProperty(Math.pow(10, -prec));
		Djp_t_ok.jp_t_ok(prop.equals(got.toMatrix(), expected.toMatrix()), msg);
	}

	public static void jp_t_is(Djp_branch got, Djp_branch expected, int prec, String msg) {
		DoubleProperty prop = new DoubleProperty(Math.pow(10, -prec));
		Djp_t_ok.jp_t_ok(prop.equals(got.toMatrix(), expected.toMatrix()), msg);
	}

	public static void jp_t_is(Djp_gen got, Djp_gen expected, int prec, String msg) {
		DoubleProperty prop = new DoubleProperty(Math.pow(10, -prec));
		Djp_t_ok.jp_t_ok(prop.equals(got.toMatrix(), expected.toMatrix()), msg);
	}

	public static void jp_t_is(Djp_gencost got, Djp_gencost expected, int prec, String msg) {
		DoubleProperty prop = new DoubleProperty(Math.pow(10, -prec));
		Djp_t_ok.jp_t_ok(prop.equals(got.toMatrix(), expected.toMatrix()), msg);
	}

	public static void jp_t_is(Djp_areas got, Djp_areas expected, int prec, String msg) {
		DoubleProperty prop = new DoubleProperty(Math.pow(10, -prec));
		Djp_t_ok.jp_t_ok(prop.equals(got.toMatrix(), expected.toMatrix()), msg);
	}

	public static void jp_t_is(DComplexMatrix2D got, DComplexMatrix2D expected, int prec, String msg) {
		DComplexProperty cprop = new DComplexProperty(Math.pow(10, -prec));
		Djp_t_ok.jp_t_ok(cprop.equals(got, expected), msg);
	}
}
