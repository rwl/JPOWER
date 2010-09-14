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

import cern.colt.matrix.AbstractMatrix;
import cern.colt.matrix.tdcomplex.DComplexFactory2D;
import cern.colt.matrix.tdcomplex.DComplexMatrix1D;
import cern.colt.matrix.tdcomplex.DComplexMatrix2D;
import cern.jet.math.tdcomplex.DComplexFunctions;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_branch;

/**
 * Computes partial derivatives of branch currents w.r.t. voltage.
 *
 * @author Ray Zimmerman (rz10@cornell.edu)
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class Djp_dIbr_dV {

	private static final DComplexFunctions cfunc = DComplexFunctions.functions;

	/**
	 * Returns four matrices containing partial derivatives of the complex
	 * branch currents at "from" and "to" ends of each branch w.r.t voltage
	 * magnitude and voltage angle respectively (for all buses). If YF is a
	 * sparse matrix, the partial derivative matrices will be as well.
	 * Also returns vectors containing the currents themselves. The following
	 * explains the expressions used to form the matrices:
	 *
	 * 	If = Yf * V;
	 *
	 * Partials of V, Vf & If w.r.t. voltage angles
	 * 	dV/dVa  = j * diag(V)
	 * 	dVf/dVa = sparse(1:nl, f, j * V(f)) = j * sparse(1:nl, f, V(f))
	 * 	dIf/dVa = Yf * dV/dVa = Yf * j * diag(V)
	 *
	 * Partials of V, Vf & If w.r.t. voltage magnitudes
	 * 	dV/dVm  = diag(V./abs(V))
	 * 	dVf/dVm = sparse(1:nl, f, V(f)./abs(V(f))
	 * 	dIf/dVm = Yf * dV/dVm = Yf * diag(V./abs(V))
	 *
	 * Derivations for "to" bus are similar.
	 *
	 * @param branch
	 * @param Yf
	 * @param Yt
	 * @param V
	 * @return
	 */
	@SuppressWarnings("static-access")
	public static AbstractMatrix[] jp_dIbr_dV(Djp_branch branch, DComplexMatrix2D Yf, DComplexMatrix2D Yt, DComplexMatrix1D V) {

		DComplexMatrix1D Vnorm = V.copy().assign(cfunc.abs).assign(V, cfunc.swapArgs(cfunc.div));
		DComplexMatrix2D diagV = DComplexFactory2D.sparse.diagonal(V);
		DComplexMatrix2D diagVnorm = DComplexFactory2D.sparse.diagonal(Vnorm);

		diagV.assign(cfunc.mult(new double[] {1, 0}));
		DComplexMatrix2D dIf_dVa = Yf.zMult(diagV, null);
		DComplexMatrix2D dIf_dVm = Yf.zMult(diagVnorm, null);
		DComplexMatrix2D dIt_dVa = Yt.zMult(diagV, null);
		DComplexMatrix2D dIt_dVm = Yf.zMult(diagVnorm, null);

		/* compute currents */
		DComplexMatrix1D If = Yf.zMult(V, null);
		DComplexMatrix1D It = Yt.zMult(V, null);

		return new AbstractMatrix[] {dIf_dVa, dIf_dVm, dIt_dVa, dIt_dVm, If, It};
	}
}
