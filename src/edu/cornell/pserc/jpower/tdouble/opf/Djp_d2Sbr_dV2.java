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
 * Computes 2nd derivatives of complex power flow w.r.t. voltage.
 *
 * @author Ray Zimmerman (rz10@cornell.edu)
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class Djp_d2Sbr_dV2 {

	private static final DComplexFunctions cfunc = DComplexFunctions.functions;

	/**
	 * Returns 4 matrices
	 * containing the partial derivatives w.r.t. voltage angle and magnitude
	 * of the product of a vector LAM with the 1st partial derivatives of the
	 * complex branch power flows. Takes sparse connection matrix CBR, sparse
	 * branch admittance matrix YBR, voltage vector V and nl x 1 vector of
	 * multipliers LAM. Output matrices are sparse.
	 *
	 * @param Cbr
	 * @param Ybr
	 * @param V
	 * @param lam
	 * @return
	 */
	@SuppressWarnings("static-access")
	public static DComplexMatrix2D[] jp_d2Sbr_dV2(DComplexMatrix2D Cbr, DComplexMatrix2D Ybr,
			DComplexMatrix1D V, DComplexMatrix1D lam) {
		double[] j = {0.0, 1.0};

		DComplexMatrix2D diaglam = DComplexFactory2D.sparse.diagonal(lam);
		DComplexMatrix2D diagV = DComplexFactory2D.sparse.diagonal(V);

		DComplexMatrix1D conjV = V.copy().assign(cfunc.conj);

		DComplexMatrix2D A = Ybr.getConjugateTranspose().zMult(diaglam, null).zMult(Cbr, null);
		DComplexMatrix2D B = diagV.copy().assign(cfunc.conj).zMult(A, null).zMult(diagV, null);
		DComplexMatrix2D D = DComplexFactory2D.sparse.diagonal(A.zMult(V, null).assign(conjV, cfunc.mult));
		DComplexMatrix2D E = DComplexFactory2D.sparse.diagonal(A.viewDice().zMult(conjV, null).assign(V, cfunc.mult));
		DComplexMatrix2D F = B.viewDice().copy().assign(B, cfunc.plus);
		DComplexMatrix2D G = DComplexFactory2D.sparse.diagonal(V.copy().assign(cfunc.abs).assign(cfunc.inv));

		DComplexMatrix2D Haa = F.copy().assign(D, cfunc.minus).assign(E, cfunc.minus);
		B.assign(B.viewDice().copy(), cfunc.minus).assign(D.assign(E, cfunc.plus), cfunc.minus);
		DComplexMatrix2D Hva = G.zMult(B, null).assign(cfunc.mult(j));
		DComplexMatrix2D Hav = Hva.viewDice().copy();
		DComplexMatrix2D Hvv = G.zMult(F, null).zMult(G, null);

		return new DComplexMatrix2D[] {Haa, Hav, Hva, Hvv};
	}
}
