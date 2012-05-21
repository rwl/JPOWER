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

package edu.cornell.pserc.jpower.opf;

import cern.colt.matrix.tdcomplex.DComplexFactory2D;
import cern.colt.matrix.tdcomplex.DComplexMatrix1D;
import cern.colt.matrix.tdcomplex.DComplexMatrix2D;

import static edu.emory.mathcs.utils.Utils.j;
import static edu.emory.mathcs.utils.Utils.cfunc;

/**
 * Computes 2nd derivatives of power injection w.r.t. voltage.
 *
 * @author Ray Zimmerman
 * @author Richard Lincoln
 *
 */
public class Djp_d2Sbus_dV2 {

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
	public static DComplexMatrix2D[] d2Sbus_dV2(DComplexMatrix2D Ybus, DComplexMatrix1D V, DComplexMatrix1D lam) {
		DComplexMatrix1D Ibus;
		DComplexMatrix2D diaglam, diagV, A, B, C, D, E, E_rhs, F, G, Gaa, Gva, Gav, Gvv;

		Ibus = Ybus.zMult(V, null);
		diaglam = DComplexFactory2D.sparse.diagonal(lam);
		diagV = DComplexFactory2D.sparse.diagonal(V);

		A = DComplexFactory2D.sparse.diagonal(lam.copy().assign(V, cfunc.mult));
		B = Ybus.zMult(diagV, null);
		C = A.zMult(B.copy().assign(cfunc.conj), null);
		D = Ybus.getConjugateTranspose().zMult(diagV, null);
		E_rhs = D.zMult(diaglam, null).assign(DComplexFactory2D.sparse.diagonal(D.zMult(lam, null)), cfunc.minus);
		E = diagV.copy().assign(cfunc.conj).zMult(E_rhs, null);
		F = C.assign(A.zMult(DComplexFactory2D.sparse.diagonal(Ibus.assign(cfunc.conj)), null), cfunc.minus);
		G = DComplexFactory2D.sparse.diagonal(V.copy().assign(cfunc.abs).assign(cfunc.inv));

		Gaa = E.copy().assign(F, cfunc.plus);
		Gva = G.zMult(E.assign(F, cfunc.minus), null).assign(cfunc.mult(j));
		Gav = Gva.viewDice().copy();
		Gvv = G.zMult(C.assign(C.viewDice(), cfunc.plus), null).zMult(G, null);

		return new DComplexMatrix2D[] {Gaa, Gav, Gva, Gvv};
	}

}
