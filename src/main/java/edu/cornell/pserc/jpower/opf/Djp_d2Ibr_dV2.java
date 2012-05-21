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
 * Computes 2nd derivatives of complex branch current w.r.t. voltage.
 *
 * @author Ray Zimmerman
 * @author Richard Lincoln
 *
 */
public class Djp_d2Ibr_dV2 {

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
	public static DComplexMatrix2D[] d2Ibr_dV2(DComplexMatrix2D Ybr, DComplexMatrix1D V, DComplexMatrix1D lam) {
		int nb;
		DComplexMatrix2D diaginvVm, Haa, Hva, Hav, Hvv;

		nb = (int) V.size();

		diaginvVm = DComplexFactory2D.sparse.diagonal(V.copy().assign(cfunc.abs).assign(cfunc.inv));

		Haa = DComplexFactory2D.sparse.diagonal(Ybr.viewDice().zMult(lam, null).assign(cfunc.neg).assign(V, cfunc.mult));
		Hva = Haa.zMult(diaginvVm, null).assign(cfunc.mult(j));
		Hav = Hva.copy();
		Hvv = DComplexFactory2D.sparse.make(nb, nb);

		return new DComplexMatrix2D[] {Haa, Hav, Hva, Hvv};
	}

}
