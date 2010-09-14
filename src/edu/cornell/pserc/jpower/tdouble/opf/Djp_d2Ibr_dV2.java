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
 * Computes 2nd derivatives of complex branch current w.r.t. voltage.
 *
 * @author Ray Zimmerman (rz10@cornell.edu)
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class Djp_d2Ibr_dV2 {

	private static final DComplexFunctions cfunc = DComplexFunctions.functions;

	/**
	 * Returns 4 matrices
	 * containing the partial derivatives w.r.t. voltage angle and magnitude
	 * of the product of a vector LAM with the 1st partial derivatives of the
	 * complex branch currents. Takes sparse branch admittance matrix YBR,
	 * voltage vector V and nl x 1 vector of multipliers LAM. Output matrices
	 * are sparse.
	 *
	 * @param Ybr
	 * @param V
	 * @param lam
	 * @return
	 */
	@SuppressWarnings("static-access")
	public static DComplexMatrix2D[] jp_d2Ibr_dV2(DComplexMatrix2D Ybr, DComplexMatrix1D V, DComplexMatrix1D lam) {
		double[] j = {0.0, 1.0};
		int nb = (int) V.size();

		DComplexMatrix2D diaginvVm = DComplexFactory2D.sparse.diagonal(V.copy().assign(cfunc.abs).assign(cfunc.inv));

		DComplexMatrix2D Haa = DComplexFactory2D.sparse.diagonal(Ybr.viewDice().zMult(lam, null).assign(cfunc.neg).assign(V, cfunc.mult));
		DComplexMatrix2D Hva = Haa.zMult(diaginvVm, null).assign(cfunc.mult(j));
		DComplexMatrix2D Hav = Hva.copy();
		DComplexMatrix2D Hvv = DComplexFactory2D.sparse.make(nb, nb);

		return new DComplexMatrix2D[] {Haa, Hav, Hva, Hvv};
	}

}
