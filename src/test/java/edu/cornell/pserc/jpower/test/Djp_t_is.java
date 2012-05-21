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

import cern.colt.matrix.tdcomplex.DComplexMatrix2D;
import cern.colt.matrix.tdcomplex.algo.DComplexProperty;
import cern.colt.matrix.tdouble.DoubleFactory1D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.algo.DoubleProperty;
import edu.cornell.pserc.jpower.jpc.Areas;
import edu.cornell.pserc.jpower.jpc.Branch;
import edu.cornell.pserc.jpower.jpc.Bus;
import edu.cornell.pserc.jpower.jpc.Gen;
import edu.cornell.pserc.jpower.jpc.GenCost;

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

		Djp_t_ok.t_ok(condition, msg);
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

	public static void t_is(DoubleMatrix1D got, double[] expected, int prec, String msg) {
		DoubleProperty prop = new DoubleProperty(Math.pow(10, -prec));
		DoubleMatrix1D expected_matrix = DoubleFactory1D.dense.make(expected);
		Djp_t_ok.t_ok(prop.equals(got, expected_matrix), msg);
	}

	public static void t_is(double got, double expected, int prec, String msg) {
		DoubleProperty prop = new DoubleProperty(Math.pow(10, -prec));
		DoubleMatrix1D expected_matrix = DoubleFactory1D.dense.make(1, expected);
		Djp_t_ok.t_ok(prop.equals(expected_matrix, got), msg);
	}

	public static void t_is(Bus got, Bus expected, int prec, String msg) {
		DoubleProperty prop = new DoubleProperty(Math.pow(10, -prec));
		Djp_t_ok.t_ok(prop.equals(got.toMatrix(), expected.toMatrix()), msg);
	}

	public static void t_is(Branch got, Branch expected, int prec, String msg) {
		DoubleProperty prop = new DoubleProperty(Math.pow(10, -prec));
		Djp_t_ok.t_ok(prop.equals(got.toMatrix(), expected.toMatrix()), msg);
	}

	public static void t_is(Gen got, Gen expected, int prec, String msg) {
		DoubleProperty prop = new DoubleProperty(Math.pow(10, -prec));
		Djp_t_ok.t_ok(prop.equals(got.toMatrix(), expected.toMatrix()), msg);
	}

	public static void t_is(GenCost got, GenCost expected, int prec, String msg) {
		DoubleProperty prop = new DoubleProperty(Math.pow(10, -prec));
		Djp_t_ok.t_ok(prop.equals(got.toMatrix(), expected.toMatrix()), msg);
	}

	public static void t_is(Areas got, Areas expected, int prec, String msg) {
		DoubleProperty prop = new DoubleProperty(Math.pow(10, -prec));
		Djp_t_ok.t_ok(prop.equals(got.toMatrix(), expected.toMatrix()), msg);
	}

	public static void t_is(DComplexMatrix2D got, DComplexMatrix2D expected, int prec, String msg) {
		DComplexProperty cprop = new DComplexProperty(Math.pow(10, -prec));
		Djp_t_ok.t_ok(cprop.equals(got, expected), msg);
	}
}
