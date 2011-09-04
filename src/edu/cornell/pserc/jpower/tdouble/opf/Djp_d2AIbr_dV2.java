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
import cern.colt.matrix.tdouble.DoubleMatrix2D;

import static cern.colt.util.tdouble.Djp_util.dfunc;
import static cern.colt.util.tdouble.Djp_util.cfunc;

/**
 * Computes 2nd derivatives of |complex current|^2 w.r.t. V.
 *
 * @author Ray Zimmerman
 * @author Richard Lincoln
 *
 */
public class Djp_d2AIbr_dV2 {

	/**
	 * Returns 4 matrices containing the partial derivatives w.r.t. voltage
	 * angle and magnitude of the product of a vector LAM with the 1st partial
	 * derivatives of the square of the magnitude of the branch currents.
	 * Takes sparse first derivative matrices of complex flow, complex flow
	 * vector, sparse branch admittance matrix YBR, voltage vector V and
	 * nl x 1 vector of multipliers LAM. Output matrices are sparse.
	 *
	 * @param dIbr_dVa
	 * @param dIbr_dVm
	 * @param Ibr
	 * @param Ybr
	 * @param V
	 * @param lam
	 * @return
	 */
	@SuppressWarnings("static-access")
	public static DoubleMatrix2D[] d2AIbr_dV2(DComplexMatrix2D dIbr_dVa, DComplexMatrix2D dIbr_dVm,
			DComplexMatrix1D Ibr, DComplexMatrix2D Ybr, DComplexMatrix1D V, DComplexMatrix1D lam) {

		DComplexMatrix2D diaglam, conj_diagIbr, conj_dIbr_dVa, conj_dIbr_dVm, Iaa, Iav, Iva, Ivv;
		DComplexMatrix2D[] d2Ibr_dV2;
		DoubleMatrix2D Haa, Hva, Hav, Hvv;

		diaglam = DComplexFactory2D.sparse.diagonal(lam);
		conj_diagIbr = DComplexFactory2D.sparse.diagonal(Ibr.copy().assign(cfunc.conj));

		d2Ibr_dV2 = Djp_d2Ibr_dV2.d2Ibr_dV2(Ybr, V, conj_diagIbr.zMult(lam, null));
		Iaa = d2Ibr_dV2[0];
		Iav = d2Ibr_dV2[1];
		Iva = d2Ibr_dV2[2];
		Ivv = d2Ibr_dV2[3];

		conj_dIbr_dVa = dIbr_dVa.copy().assign(cfunc.conj);
		conj_dIbr_dVm = dIbr_dVm.copy().assign(cfunc.conj);

		Iaa.assign(dIbr_dVa.viewDice(), cfunc.plus);
		Haa = Iaa.zMult(diaglam, null).zMult(conj_dIbr_dVa, null).getRealPart().assign(dfunc.mult(2));

		Iva.assign(dIbr_dVm.viewDice(), cfunc.plus);
		Hva = Iva.zMult(diaglam, null).zMult(conj_dIbr_dVa, null).getRealPart().assign(dfunc.mult(2));

		Iav.assign(dIbr_dVa.viewDice(), cfunc.plus);
		Hav = Iav.zMult(diaglam, null).zMult(conj_dIbr_dVm, null).getRealPart().assign(dfunc.mult(2));

		Ivv.assign(dIbr_dVm.viewDice(), cfunc.plus);
		Hvv = Ivv.zMult(diaglam, null).zMult(conj_dIbr_dVm, null).getRealPart().assign(dfunc.mult(2));

		return new DoubleMatrix2D[] {Haa, Hav, Hva, Hvv};
	}

}
