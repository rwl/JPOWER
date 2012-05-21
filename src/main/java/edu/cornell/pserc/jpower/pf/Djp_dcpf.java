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

package edu.cornell.pserc.jpower.pf;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.algo.SparseDoubleAlgebra;

import static edu.emory.mathcs.utils.Utils.icat;
import static edu.emory.mathcs.utils.Utils.dfunc;

/**
 * Solves a DC power flow.
 *
 * @author Ray Zimmerman
 * @author Richard Lincoln
 *
 */
@SuppressWarnings("static-access")
public class Djp_dcpf {

	/**
	 * Solves for the bus voltage angles at all but the reference bus.
	 *
	 * @param B the full system B matrix
	 * @param Pbus the vector of bus real power injections
	 * @param Va0 the initial vector of bus voltage angles (in radians)
	 * @param ref the swing bus index
	 * @param pv PV bus indicies
	 * @param pq PQ bus indices
	 * @return a vector of bus voltage angles in radians
	 */
	public static DoubleMatrix1D dcpf(DoubleMatrix2D B, DoubleMatrix1D Pbus,
			DoubleMatrix1D Va0, int ref, int[] pv, int[] pq) {
		DoubleMatrix1D Va, b;
		DoubleMatrix2D A;
		int[] pvpq;

		/* initialize result vector */
		Va = Va0.copy();

		/* update angles for non-reference buses */
		pvpq = icat(pv, pq);

		A = B.viewSelection(pvpq, pvpq).copy();
		b = B.viewSelection(pvpq, null).copy().viewColumn(ref);
		b.assign( dfunc.mult(Va.get(ref)) );
		b.assign(Pbus.viewSelection(pvpq), dfunc.swapArgs(dfunc.minus));
		Va.viewSelection(pvpq).assign( SparseDoubleAlgebra.DEFAULT.solve(A, b) );

		return Va;
	}

}
