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

import cern.colt.matrix.tdcomplex.DComplexMatrix1D;
import cern.colt.matrix.tdcomplex.DComplexMatrix2D;
import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.jet.math.tdouble.DoubleFunctions;

/**
 * Partial derivatives of squared flow magnitudes w.r.t voltage.
 *
 * @author Ray Zimmerman
 * @author Richard Lincoln
 *
 */
public class Djp_dAbr_dV {

	private static final DoubleFunctions dfunc = DoubleFunctions.functions;

	private static DoubleMatrix2D dAf_dPf, dAf_dQf, dAt_dPt, dAt_dQt, dAf_dVa, dAt_dVa, dAf_dVm, dAt_dVm;

	/**
	 * Returns four matrices containing partial derivatives of the square of
	 * the branch flow magnitudes at "from" & "to" ends of each branch w.r.t
	 * voltage magnitude and voltage angle respectively (for all buses), given
	 * the flows and flow sensitivities. Flows could be complex current or
	 * complex or real power. Notation below is based on complex power. The
	 * following explains the expressions used to form the matrices:
	 *
	 * Let Af refer to the square of the apparent power at the "from" end of
	 * each branch,
	 *
	 * 	Af = abs(Sf).^2
	 * 	   = Sf .* conj(Sf)
	 * 	   = Pf.^2 + Qf.^2
	 *
	 * then ...
	 *
	 * Partial w.r.t real power,
	 * 	dAf/dPf = 2 * diag(Pf)
	 *
	 * Partial w.r.t reactive power,
	 * 	dAf/dQf = 2 * diag(Qf)
	 *
	 * Partial w.r.t Vm & Va
	 * 	dAf/dVm = dAf/dPf * dPf/dVm + dAf/dQf * dQf/dVm
	 * 	dAf/dVa = dAf/dPf * dPf/dVa + dAf/dQf * dQf/dVa
	 *
	 * Derivations for "to" bus are similar.
	 *
	 * @param dSf_dVa
	 * @param dSf_dVm
	 * @param dSt_dVa
	 * @param dSt_dVm
	 * @param Sf
	 * @param St
	 * @return
	 */
	@SuppressWarnings("static-access")
	public static DoubleMatrix2D[] jp_dAbr_dV(DComplexMatrix2D dSf_dVa, DComplexMatrix2D dSf_dVm,
			DComplexMatrix2D dSt_dVa, DComplexMatrix2D dSt_dVm, DComplexMatrix1D Sf, DComplexMatrix1D St) {

		dAf_dPf = DoubleFactory2D.sparse.diagonal(Sf.getRealPart().assign(dfunc.mult(2)));
		dAf_dQf = DoubleFactory2D.sparse.diagonal(Sf.getImaginaryPart().assign(dfunc.mult(2)));
		dAt_dPt = DoubleFactory2D.sparse.diagonal(St.getRealPart().assign(dfunc.mult(2)));
		dAt_dQt = DoubleFactory2D.sparse.diagonal(St.getImaginaryPart().assign(dfunc.mult(2)));

		// Partial derivative of apparent power magnitude w.r.t voltage phase angle.
		dAf_dVa = dAf_dPf.zMult(dSf_dVa.getRealPart(), null);
		dAf_dVa.assign(dAf_dQf.zMult(dSf_dVa.getImaginaryPart(), null), dfunc.plus);
		dAt_dVa = dAt_dPt.zMult(dSt_dVa.getRealPart(), null);
		dAt_dVa.assign(dAt_dQt.zMult(dSt_dVa.getImaginaryPart(), null), dfunc.plus);

		// Partial derivative of apparent power magnitude w.r.t. voltage amplitude.
		dAf_dVm = dAf_dPf.zMult(dSf_dVm.getRealPart(), null);
		dAf_dVm.assign(dAf_dQf.zMult(dSf_dVm.getImaginaryPart(), null), dfunc.plus);
		dAt_dVm = dAt_dPt.zMult(dSt_dVm.getRealPart(), null);
		dAt_dVm.assign(dAt_dQt.zMult(dSt_dVm.getImaginaryPart(), null), dfunc.plus);

		return new DoubleMatrix2D[] {dAf_dVa, dAf_dVm, dAt_dVa, dAt_dVm};
	}

}
