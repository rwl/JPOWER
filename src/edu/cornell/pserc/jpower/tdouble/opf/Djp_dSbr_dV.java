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

import cern.colt.matrix.AbstractMatrix;
import cern.colt.matrix.tdcomplex.DComplexMatrix1D;
import cern.colt.matrix.tdcomplex.DComplexMatrix2D;
import cern.colt.matrix.tdcomplex.impl.SparseRCDComplexMatrix2D;

import static cern.colt.util.tdouble.Djp_util.cfunc;
import static cern.colt.util.tdouble.Djp_util.irange;
import static cern.colt.util.tdouble.Djp_util.j;

import edu.cornell.pserc.jpower.tdouble.jpc.Djp_branch;

/**
 * Computes partial derivatives of power flows w.r.t. voltage.
 *
 * @author Ray Zimmerman
 * @author Richard Lincoln
 *
 */
public class Djp_dSbr_dV {


	/**
	 * Returns four matrices containing partial derivatives of the complex
	 * branch power flows at "from" and "to" ends of each branch w.r.t voltage
	 * magnitude and voltage angle respectively (for all buses). If YF is a
	 * sparse matrix, the partial derivative matrices will be as well. Optionally
	 * returns vectors containing the power flows themselves. The following
	 * explains the expressions used to form the matrices:
	 *
	 * 	If = Yf * V;
	 * 	Sf = diag(Vf) * conj(If) = diag(conj(If)) * Vf
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
	 * Partials of Sf w.r.t. voltage angles
	 * 	dSf/dVa = diag(Vf) * conj(dIf/dVa)
	 * 	                + diag(conj(If)) * dVf/dVa
	 * 	        = diag(Vf) * conj(Yf * j * diag(V))
	 * 	                + conj(diag(If)) * j * sparse(1:nl, f, V(f))
	 * 	        = -j * diag(Vf) * conj(Yf * diag(V))
	 * 	                + j * conj(diag(If)) * sparse(1:nl, f, V(f))
	 * 	        = j * (conj(diag(If)) * sparse(1:nl, f, V(f))
	 * 	                - diag(Vf) * conj(Yf * diag(V)))
	 *
	 * Partials of Sf w.r.t. voltage magnitudes
	 * 	dSf/dVm = diag(Vf) * conj(dIf/dVm)
	 * 	                + diag(conj(If)) * dVf/dVm
	 * 	        = diag(Vf) * conj(Yf * diag(V./abs(V)))
	 * 	                + conj(diag(If)) * sparse(1:nl, f, V(f)./abs(V(f)))
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
	public static AbstractMatrix[] dSbr_dV(Djp_branch branch, DComplexMatrix2D Yf, DComplexMatrix2D Yt, DComplexMatrix1D V) {
		int nl, nb;
		int[] f, t, il, ib;
		DComplexMatrix1D If, It, Vnorm, Sf, St;
		SparseRCDComplexMatrix2D diagVf, diagVt, diagIf, diagIt,
				diagV, diagVnorm, Vf, Vt, dSf_dVa, diagIf2, dSt_dVa, diagIt2,
				Vfnorm, Vtnorm, Ifnorm, dSf_dVm, Itnorm, dSt_dVm;

		/* define */
		f = branch.f_bus.toArray();		// list of "from" buses
		t = branch.t_bus.toArray();		// list of "to" buses
		nl = branch.size();
		nb = (int) V.size();
		il = irange(nl);
		ib = irange(nb);

		/* compute currents */
		If = Yf.zMult(V, null);
		It = Yt.zMult(V, null);

		Vnorm = V.copy().assign(cfunc.abs).assign(V, cfunc.swapArgs(cfunc.div));

		diagVf = new SparseRCDComplexMatrix2D(nl, nl, il, il, V.viewSelection(f).toArray(), false, false);
		diagVt = new SparseRCDComplexMatrix2D(nl, nl, il, il, V.viewSelection(t).toArray(), false, false);
		diagIf = new SparseRCDComplexMatrix2D(nl, nl, il, il, If.toArray(), false, false);
		diagIt = new SparseRCDComplexMatrix2D(nl, nl, il, il, It.toArray(), false, false);
		diagV = new SparseRCDComplexMatrix2D(nb, nb, ib, ib, V.toArray(), false, false);
		diagVnorm = new SparseRCDComplexMatrix2D(nb, nb, ib, ib, Vnorm.toArray(), false, false);

		Vf = new SparseRCDComplexMatrix2D(nl, nb, il, f, V.viewSelection(f).toArray(), false, false);
		Vt = new SparseRCDComplexMatrix2D(nl, nb, il, t, V.viewSelection(t).toArray(), false, false);

		/* Partial derivative of S w.r.t voltage phase angle. */
		diagIf.assign(cfunc.conj);
		dSf_dVa = (SparseRCDComplexMatrix2D) diagIf.zMult(Vf, null);
		diagIf2 = (SparseRCDComplexMatrix2D) Yf.zMult(diagV, null).assign(cfunc.conj);
		dSf_dVa.assign(diagVf.zMult(diagIf2, null), cfunc.minus).assign(cfunc.mult(j));

		diagIt.assign(cfunc.conj);
		dSt_dVa = (SparseRCDComplexMatrix2D) diagIt.zMult(Vt, null);
		diagIt2 = (SparseRCDComplexMatrix2D) Yt.zMult(diagV, null).assign(cfunc.conj);
		dSt_dVa.assign(diagVt.zMult(diagIt2, null), cfunc.minus).assign(cfunc.mult(j));

		Vfnorm = new SparseRCDComplexMatrix2D(nl, nb, il, f, Vnorm.viewSelection(f).toArray(), false, false);
		Vtnorm = new SparseRCDComplexMatrix2D(nl, nb, il, t, Vnorm.viewSelection(t).toArray(), false, false);

		/* Partial derivative of S w.r.t. voltage amplitude. */
		Ifnorm = (SparseRCDComplexMatrix2D) Yf.zMult(diagVnorm, null).assign(cfunc.conj);
		dSf_dVm = (SparseRCDComplexMatrix2D) diagVf.zMult(Ifnorm, null);
		dSf_dVm.assign(diagIf.zMult(Vfnorm, null), cfunc.plus);

		Itnorm = (SparseRCDComplexMatrix2D) Yt.zMult(diagVnorm, null).assign(cfunc.conj);
		dSt_dVm = (SparseRCDComplexMatrix2D) diagVt.zMult(Itnorm, null);
		dSt_dVm.assign(diagIt.zMult(Vtnorm, null), cfunc.plus);

		/* compute power flows */
		Sf = V.viewSelection(f).copy().assign(If.assign(cfunc.conj), cfunc.mult);
		St = V.viewSelection(t).copy().assign(It.assign(cfunc.conj), cfunc.mult);

		return new AbstractMatrix[] {dSf_dVa, dSf_dVm, dSt_dVa, dSt_dVm, Sf, St};
	}

}
