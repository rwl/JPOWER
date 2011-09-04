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

package edu.cornell.pserc.jpower.tdouble.pf;

import cern.colt.matrix.tdcomplex.DComplexMatrix1D;
import cern.colt.matrix.tdcomplex.DComplexMatrix2D;
import cern.colt.matrix.tdcomplex.impl.SparseRCDComplexMatrix2D;

import static cern.colt.util.tdouble.Util.cfunc;
import static cern.colt.util.tdouble.Util.irange;

/**
 * Computes partial derivatives of power injection w.r.t. voltage.
 *
 * @author Ray Zimmerman
 * @author Richard Lincoln
 *
 */
public class Djp_dSbus_dV {

	/**
	 * Returns two matrices containing
	 * partial derivatives of the complex bus power injections w.r.t voltage
	 * magnitude and voltage angle respectively (for all buses). If YBUS is a
	 * sparse matrix, the return values will be also. The following explains
	 * the expressions used to form the matrices:
	 *
	 * S = diag(V) * conj(Ibus) = diag(conj(Ibus)) * V
	 *
	 * Partials of V & Ibus w.r.t. voltage magnitudes
	 * 	dV/dVm = diag(V./abs(V))
	 * 	dI/dVm = Ybus * dV/dVm = Ybus * diag(V./abs(V))
	 *
	 * Partials of V & Ibus w.r.t. voltage angles
	 * 	dV/dVa = j * diag(V)
	 * 	dI/dVa = Ybus * dV/dVa = Ybus * j * diag(V)
	 *
	 * Partials of S w.r.t. voltage magnitudes
	 * 	dS/dVm = diag(V) * conj(dI/dVm) + diag(conj(Ibus)) * dV/dVm
	 * 	       = diag(V) * conj(Ybus * diag(V./abs(V)))
	 * 	                                + conj(diag(Ibus)) * diag(V./abs(V))
	 *
	 * Partials of S w.r.t. voltage angles
	 * 	dS/dVa = diag(V) * conj(dI/dVa) + diag(conj(Ibus)) * dV/dVa
	 * 	       = diag(V) * conj(Ybus * j * diag(V))
	 *                                  + conj(diag(Ibus)) * j * diag(V)
	 *	       = -j * diag(V) * conj(Ybus * diag(V))
	 *	                                + conj(diag(Ibus)) * j * diag(V)
	 *	       = j * diag(V) * conj(diag(Ibus) - Ybus * diag(V))
	 *
	 * @param Ybus
	 * @param V
	 * @return
	 */
	@SuppressWarnings("static-access")
	public static DComplexMatrix2D[] dSbus_dV(DComplexMatrix2D Ybus, DComplexMatrix1D V) {
		int n;
		int[] ib;
		DComplexMatrix1D Ibus, absV, Vnorm;
		DComplexMatrix2D[] dSbus_dV;
		SparseRCDComplexMatrix2D diagV, diagIbus, diagVnorm, rhs, dS_dVa, conjInorm, dS_dVm, addend;

		n = (int) V.size();
		ib = irange(n);
		Ibus = Ybus.zMult(V, null);

		diagV = new SparseRCDComplexMatrix2D(n, n, ib, ib, V.toArray(), false, false);
		diagIbus = new SparseRCDComplexMatrix2D(n, n, ib, ib, Ibus.toArray(), false, false);

		absV = V.copy().assign(cfunc.abs);
		Vnorm = V.copy().assign(absV, cfunc.div);
		diagVnorm = new SparseRCDComplexMatrix2D(n, n, ib, ib, Vnorm.toArray(), false, false);

		rhs = (SparseRCDComplexMatrix2D) Ybus.zMult(diagV, null);

		rhs.assign(diagIbus, cfunc.swapArgs(cfunc.minus)).assign(cfunc.conj);
		dS_dVa = (SparseRCDComplexMatrix2D) diagV.zMult(rhs, null);
		dS_dVa.assign( cfunc.mult(new double[] {0, 1}) );

		conjInorm = (SparseRCDComplexMatrix2D) Ybus.zMult(diagVnorm, null);
		conjInorm.assign(cfunc.conj);
		dS_dVm = (SparseRCDComplexMatrix2D) diagV.zMult(conjInorm, null);

		diagIbus.assign(cfunc.conj);
		addend = (SparseRCDComplexMatrix2D) diagVnorm.zMult(diagIbus, null);
		dS_dVm.assign(addend, cfunc.plus);

		dSbus_dV = new DComplexMatrix2D[] {dS_dVm, dS_dVa};

		return dSbus_dV;
	}

}
