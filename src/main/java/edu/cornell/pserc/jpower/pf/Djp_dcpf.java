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

package edu.cornell.pserc.jpower.pf;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.algo.SparseDoubleAlgebra;

import static edu.emory.mathcs.utils.Utils.dfunc;
import static edu.emory.mathcs.utils.Utils.icat;

/**
 * Solves a DC power flow.
 *
 * @author Ray Zimmerman
 * @author Richard Lincoln
 *
 */
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
	@SuppressWarnings("static-access")
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
