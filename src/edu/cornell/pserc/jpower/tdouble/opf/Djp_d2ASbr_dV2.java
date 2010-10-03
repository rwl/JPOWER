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
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.util.tdouble.Djp_util;
import cern.jet.math.tdcomplex.DComplexFunctions;
import cern.jet.math.tdouble.DoubleFunctions;

/**
 * Computes 2nd derivatives of |complex power flow|^2 w.r.t. V.
 *
 * @author Ray Zimmerman (rz10@cornell.edu)
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class Djp_d2ASbr_dV2 {

	private static final DoubleFunctions dfunc = DoubleFunctions.functions;
	private static final DComplexFunctions cfunc = DComplexFunctions.functions;

	/**
	 * Returns 4 matrices containing the partial derivatives w.r.t. voltage
	 * angle and magnitude of the product of a vector LAM with the 1st partial
	 * derivatives of the square of the magnitude of branch complex power flows.
	 * Takes sparse first derivative matrices of complex flow, complex flow
	 * vector, sparse connection matrix CBR, sparse branch admittance matrix YBR,
	 * voltage vector V and nl x 1 vector of multipliers LAM. Output matrices
	 * are sparse.
	 *
	 * @param dSbr_dVa
	 * @param dSbr_dVm
	 * @param Sbr
	 * @param Cbr
	 * @param Ybr
	 * @param V
	 * @param lam
	 * @return
	 */
	@SuppressWarnings("static-access")
	public static DoubleMatrix2D[] jp_d2ASbr_dV2(DComplexMatrix2D dSbr_dVa, DComplexMatrix2D dSbr_dVm,
			DComplexMatrix1D Sbr, DComplexMatrix2D Cbr, DComplexMatrix2D Ybr, DComplexMatrix1D V, DComplexMatrix1D lam) {

		DComplexMatrix2D diaglam = DComplexFactory2D.sparse.diagonal(lam);
		DComplexMatrix2D conj_diagSbr = DComplexFactory2D.sparse.diagonal(Sbr.copy().assign(cfunc.conj));

		DComplexMatrix2D[] d2Sbr_dV2 = Djp_d2Sbr_dV2.jp_d2Sbr_dV2(Cbr, Ybr, V, conj_diagSbr.zMult(lam, null));
		DComplexMatrix2D Saa = d2Sbr_dV2[0];
		DComplexMatrix2D Sav = d2Sbr_dV2[1];
		DComplexMatrix2D Sva = d2Sbr_dV2[2];
		DComplexMatrix2D Svv = d2Sbr_dV2[3];

		DComplexMatrix2D conj_dSbr_dVa = dSbr_dVa.copy().assign(cfunc.conj);
		DComplexMatrix2D conj_dSbr_dVm = dSbr_dVm.copy().assign(cfunc.conj);

		Saa.assign(dSbr_dVa.viewDice(), cfunc.plus);
		DoubleMatrix2D Haa = Saa.zMult(diaglam, null).zMult(conj_dSbr_dVa, null).getRealPart().assign(dfunc.mult(2));

		Sva.assign(dSbr_dVm.viewDice(), cfunc.plus);
		DoubleMatrix2D Hva = Sva.zMult(diaglam, null).zMult(conj_dSbr_dVa, null).getRealPart().assign(dfunc.mult(2));

		Sav.assign(dSbr_dVa.viewDice(), cfunc.plus);
		DoubleMatrix2D Hav = Sav.zMult(diaglam, null).zMult(conj_dSbr_dVm, null).getRealPart().assign(dfunc.mult(2));

		Svv.assign(dSbr_dVm.viewDice(), cfunc.plus);
		DoubleMatrix2D Hvv = Svv.zMult(diaglam, null).zMult(conj_dSbr_dVm, null).getRealPart().assign(dfunc.mult(2));

		return new DoubleMatrix2D[] {Haa, Hav, Hva, Hvv};
	}
}
