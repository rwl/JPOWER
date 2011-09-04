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

package edu.cornell.pserc.jpower.tdouble.opf;

import cern.colt.matrix.tdcomplex.DComplexFactory2D;
import cern.colt.matrix.tdcomplex.DComplexMatrix1D;
import cern.colt.matrix.tdcomplex.DComplexMatrix2D;

import static cern.colt.util.tdouble.Util.cfunc;
import static cern.colt.util.tdouble.Util.j;

/**
 * Computes 2nd derivatives of complex power flow w.r.t. voltage.
 *
 * @author Ray Zimmerman
 * @author Richard Lincoln
 *
 */
public class Djp_d2Sbr_dV2 {

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
	public static DComplexMatrix2D[] d2Sbr_dV2(DComplexMatrix2D Cbr, DComplexMatrix2D Ybr,
			DComplexMatrix1D V, DComplexMatrix1D lam) {
		DComplexMatrix1D conjV;
		DComplexMatrix2D diaglam, diagV, A, B, D, E, F, G, Haa, Hva, Hav, Hvv;

		diaglam = DComplexFactory2D.sparse.diagonal(lam);
		diagV = DComplexFactory2D.sparse.diagonal(V);

		conjV = V.copy().assign(cfunc.conj);

		A = Ybr.getConjugateTranspose().zMult(diaglam, null).zMult(Cbr, null);
		B = diagV.copy().assign(cfunc.conj).zMult(A, null).zMult(diagV, null);
		D = DComplexFactory2D.sparse.diagonal(A.zMult(V, null).assign(conjV, cfunc.mult));
		E = DComplexFactory2D.sparse.diagonal(A.viewDice().zMult(conjV, null).assign(V, cfunc.mult));
		F = B.viewDice().copy().assign(B, cfunc.plus);
		G = DComplexFactory2D.sparse.diagonal(V.copy().assign(cfunc.abs).assign(cfunc.inv));

		Haa = F.copy().assign(D, cfunc.minus).assign(E, cfunc.minus);
		B.assign(B.viewDice().copy(), cfunc.minus).assign(D.assign(E, cfunc.plus), cfunc.minus);
		Hva = G.zMult(B, null).assign(cfunc.mult(j));
		Hav = Hva.viewDice().copy();
		Hvv = G.zMult(F, null).zMult(G, null);

		return new DComplexMatrix2D[] {Haa, Hav, Hva, Hvv};
	}

}
