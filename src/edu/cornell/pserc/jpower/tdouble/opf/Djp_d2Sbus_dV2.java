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

package edu.cornell.pserc.jpower.tdouble.opf;

import cern.colt.matrix.tdcomplex.DComplexFactory2D;
import cern.colt.matrix.tdcomplex.DComplexMatrix1D;
import cern.colt.matrix.tdcomplex.DComplexMatrix2D;
import cern.jet.math.tdcomplex.DComplexFunctions;

/**
 * Computes 2nd derivatives of power injection w.r.t. voltage.
 *
 * @author Ray Zimmerman (rz10@cornell.edu)
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class Djp_d2Sbus_dV2 {

	private static final DComplexFunctions cfunc = DComplexFunctions.functions;

	/**
	 * Returns 4 matrices
	 * containing the partial derivatives w.r.t. voltage angle and magnitude
	 * of the product of a vector LAM with the 1st partial derivatives of the
	 * complex bus power injections. Takes sparse bus admittance matrix YBUS,
	 * voltage vector V and nb x 1 vector of multipliers LAM. Output matrices
	 * are sparse.
	 *
	 * @param Ybus
	 * @param V
	 * @param lam
	 * @return
	 */
	@SuppressWarnings("static-access")
	public static DComplexMatrix2D[] jp_d2Sbus_dV2(DComplexMatrix2D Ybus, DComplexMatrix1D V, DComplexMatrix1D lam) {
		double[] j = {0.0, 1.0};

		DComplexMatrix1D Ibus = Ybus.zMult(V, null);
		DComplexMatrix2D diaglam = DComplexFactory2D.sparse.diagonal(lam);
		DComplexMatrix2D diagV = DComplexFactory2D.sparse.diagonal(V);

		DComplexMatrix2D A = DComplexFactory2D.sparse.diagonal(lam.copy().assign(V, cfunc.mult));
		DComplexMatrix2D B = Ybus.zMult(diagV, null);
		DComplexMatrix2D C = A.zMult(B.copy().assign(cfunc.conj), null);
		DComplexMatrix2D D = Ybus.getConjugateTranspose().zMult(diagV, null);
		DComplexMatrix2D E_rhs = D.zMult(diaglam, null).assign(DComplexFactory2D.sparse.diagonal(D.zMult(lam, null)), cfunc.minus);
		DComplexMatrix2D E = diagV.copy().assign(cfunc.conj).zMult(E_rhs, null);
		DComplexMatrix2D F = C.assign(A.zMult(DComplexFactory2D.sparse.diagonal(Ibus.assign(cfunc.conj)), null), cfunc.minus);
		DComplexMatrix2D G = DComplexFactory2D.sparse.diagonal(V.copy().assign(cfunc.abs).assign(cfunc.inv));

		DComplexMatrix2D Gaa = E.copy().assign(F, cfunc.plus);
		DComplexMatrix2D Gva = G.zMult(E.assign(F, cfunc.minus), null).assign(cfunc.mult(j));
		DComplexMatrix2D Gav = Gva.viewDice().copy();
		DComplexMatrix2D Gvv = G.zMult(C.assign(C.viewDice(), cfunc.plus), null).zMult(G, null);

		return new DComplexMatrix2D[] {Gaa, Gav, Gva, Gvv};
	}
}
