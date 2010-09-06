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

package edu.cornell.pserc.jpower.tdcomplex;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.algo.SparseDoubleAlgebra;

/**
 * Solves a DC power flow.
 *
 * @author Ray Zimmerman (rz10@cornell.edu)
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class DZjp_dcpf extends DZjp_idx {

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
	public static DoubleMatrix1D jp_dcpf(DoubleMatrix2D B, DoubleMatrix1D Pbus,
			DoubleMatrix1D Va0, int ref, int[] pv, int[] pq) {

		/* initialize result vector */
		DoubleMatrix1D Va = Va0.copy();

		/* update angles for non-reference buses */
		int[] pvpq = cat(pv, pq);
		DoubleMatrix2D A = B.viewSelection(pvpq, pvpq);
		DoubleMatrix1D b = B.viewColumn(ref).viewSelection(pvpq).copy();
		b.assign(dfunc.mult(Va.get(ref)));
		b.assign(Pbus.viewSelection(pvpq), dfunc.swapArgs(dfunc.minus));
		Va.viewSelection(pvpq).assign(SparseDoubleAlgebra.DEFAULT.solve(A, b));

		return Va;
	}

}
