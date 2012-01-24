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

package edu.cornell.pserc.jpower.test;

import cern.colt.matrix.tdcomplex.DComplexMatrix2D;
import cern.colt.matrix.tdcomplex.algo.DComplexProperty;
import cern.colt.matrix.tdouble.DoubleFactory1D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.algo.DoubleProperty;
import cern.colt.matrix.tint.IntFactory1D;
import cern.colt.matrix.tint.IntMatrix1D;
import cern.colt.matrix.tint.algo.IntProperty;

import edu.cornell.pserc.jpower.jpc.Areas;
import edu.cornell.pserc.jpower.jpc.Branch;
import edu.cornell.pserc.jpower.jpc.Bus;
import edu.cornell.pserc.jpower.jpc.Gen;
import edu.cornell.pserc.jpower.jpc.Cost;
import static edu.cornell.pserc.jpower.test.Djp_t_ok.t_ok;

/**
 * Tests if two matrices are identical to some tolerance.
 *
 * @author Ray Zimmerman
 * @author Richard Lincoln
 *
 */
public class Djp_t_is {

	public static void t_is(DoubleMatrix2D got, DoubleMatrix2D expected) {
		t_is(got, expected, 5);
	}

	public static void t_is(DoubleMatrix2D got, DoubleMatrix2D expected, int prec) {
		t_is(got, expected, prec, "");
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
	public static void t_is(DoubleMatrix2D got, DoubleMatrix2D expected, int prec, String msg) {
		DoubleProperty prop = new DoubleProperty(Math.pow(10, -prec));
		boolean condition = prop.equals(got, expected);

		t_ok(condition, msg);
//		if (!condition && !t_quiet) {
//			if (max_diff != 0.0) {
//				System.out.printf("max diff = %g (allowed tol = %g)\n\n", max_diff, Math.pow(10, -prec));
//			} else {
//				System.out.printf("    dimension mismatch:\n");
//				System.out.printf("             got: %d x %d\n", size(got));
//				System.out.printf("        expected: %d x %d\n\n", size(expected));
//			}
//		}
	}

	public static void t_is(DoubleMatrix1D got, double[] expected, int prec, String msg) {
		DoubleProperty prop = new DoubleProperty(Math.pow(10, -prec));
		DoubleMatrix1D expected_matrix = DoubleFactory1D.dense.make(expected);
		t_ok(prop.equals(got, expected_matrix), msg);
	}

	public static void t_is(IntMatrix1D got, IntMatrix1D expected, int prec, String msg) {
		IntProperty prop = new IntProperty();
		boolean condition = prop.equals(got, expected);

		t_ok(condition, msg);
	}

	public static void t_is(IntMatrix1D got, int[] expected, int prec, String msg) {
		t_is(got, IntFactory1D.dense.make(expected), prec, msg);
	}

	public static void t_is(double got, double expected, int prec, String msg) {
		DoubleProperty prop = new DoubleProperty(Math.pow(10, -prec));
		DoubleMatrix1D expected_matrix = DoubleFactory1D.dense.make(1, expected);
		t_ok(prop.equals(expected_matrix, got), msg);
	}

	public static void t_is(Bus got, Bus expected, int prec, String msg) {
		DoubleProperty prop = new DoubleProperty(Math.pow(10, -prec));
		t_ok(prop.equals(got.toMatrix(), expected.toMatrix()), msg);
	}

	public static void t_is(Branch got, Branch expected, int prec, String msg) {
		DoubleProperty prop = new DoubleProperty(Math.pow(10, -prec));
		t_ok(prop.equals(got.toMatrix(), expected.toMatrix()), msg);
	}

	public static void t_is(Gen got, Gen expected, int prec, String msg) {
		DoubleProperty prop = new DoubleProperty(Math.pow(10, -prec));
		t_ok(prop.equals(got.toMatrix(), expected.toMatrix()), msg);
	}

	public static void t_is(Cost got, Cost expected, int prec, String msg) {
		DoubleProperty prop = new DoubleProperty(Math.pow(10, -prec));
		t_ok(prop.equals(got.toMatrix(), expected.toMatrix()), msg);
	}

	public static void t_is(Areas got, Areas expected, int prec, String msg) {
		DoubleProperty prop = new DoubleProperty(Math.pow(10, -prec));
		t_ok(prop.equals(got.toMatrix(), expected.toMatrix()), msg);
	}

	public static void t_is(DComplexMatrix2D got, DComplexMatrix2D expected, int prec, String msg) {
		DComplexProperty cprop = new DComplexProperty(Math.pow(10, -prec));
		t_ok(cprop.equals(got, expected), msg);
	}

}
