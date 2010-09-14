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

package edu.cornell.pserc.jpower.tdouble;

import java.util.Map;

import cern.colt.matrix.Norm;
import cern.colt.matrix.tdcomplex.DComplexMatrix1D;
import cern.colt.matrix.tdcomplex.DComplexMatrix2D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.algo.DenseDoubleAlgebra;
import cern.colt.matrix.tdouble.algo.SparseDoubleAlgebra;
import cern.colt.matrix.tdouble.algo.decomposition.SparseDoubleLUDecomposition;
import cern.colt.matrix.tdouble.impl.SparseCCDoubleMatrix2D;
import cern.jet.math.tdcomplex.DComplexFunctions;
import cern.jet.math.tdouble.DoubleFunctions;
import edu.cornell.pserc.jpower.tdouble.util.Djp_util;

/**
 * Solves the power flow using a fast decoupled method.
 *
 * @author Ray Zimmerman (rz10@cornell.edu)
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class Djp_fdpf {

	private static final Djp_util util = new Djp_util();
	private static final DoubleFunctions dfunc = DoubleFunctions.functions;
	private static final DComplexFunctions cfunc = DComplexFunctions.functions;

	public static Object[] jp_fdpf(DComplexMatrix2D Ybus, DComplexMatrix1D Sbus, DComplexMatrix1D V0,
			DoubleMatrix2D Bp, DoubleMatrix2D Bpp, int ref, int[] pv, int[] pq) {
		return jp_fdpf(Ybus, Sbus, V0, Bp, Bpp, ref, pv, pq, Djp_jpoption.jp_jpoption());
	}

	/**
	 * Solves for bus voltages given the full system admittance matrix (for
	 * all buses), the complex bus power injection vector (for all buses),
	 * the initial vector of complex bus voltages, the FDPF matrices B prime
	 * and B double prime, and column vectors with the lists of bus indices
	 * for the swing bus, PV buses, and PQ buses, respectively. The bus voltage
	 * vector contains the set point for generator (including ref bus)
	 * buses, and the reference angle of the swing bus, as well as an initial
	 * guess for remaining magnitudes and angles. MPOPT is a MATPOWER options
	 * vector which can be used to set the termination tolerance, maximum
	 * number of iterations, and output options (see MPOPTION for details).
	 * Uses default options if this parameter is not given. Returns the
	 * final complex voltages, a flag which indicates whether it converged
	 * or not, and the number of iterations performed.
	 *
	 * @param Ybus
	 * @param Sbus
	 * @param V0
	 * @param Bp
	 * @param Bpp
	 * @param ref
	 * @param pv
	 * @param pq
	 * @param jpopt
	 * @return
	 */
	@SuppressWarnings("static-access")
	public static Object[] jp_fdpf(DComplexMatrix2D Ybus, DComplexMatrix1D Sbus, DComplexMatrix1D V0,
			DoubleMatrix2D Bp, DoubleMatrix2D Bpp, int ref, int[] pv, int[] pq, Map<String, Double> jpopt) {

		/* options */
		double tol	= jpopt.get("PF_TOL");
		int max_it	= jpopt.get("PF_MAX_IT").intValue();
		int verbose	= jpopt.get("VERBOSE").intValue();

		/* initialize */
		int[] pvpq = util.cat(pv, pq);
		boolean converged = false;
		int i = 0;
		DComplexMatrix1D V = V0;
		DComplexMatrix1D Va = V.copy().assign(cfunc.arg);
		DComplexMatrix1D Vm = V.copy().assign(cfunc.abs);

		/* evaluate initial mismatch */
		DComplexMatrix1D mis = Ybus.zMult(V, null).assign(cfunc.conj);
		mis.assign(V, cfunc.mult).assign(Sbus, cfunc.minus).assign(Vm, cfunc.div);
		DoubleMatrix1D P = mis.viewSelection(pvpq).getRealPart();
		DoubleMatrix1D Q = mis.viewSelection(pq).getImaginaryPart();

		/* check tolerance */
		double normP = DenseDoubleAlgebra.DEFAULT.norm(P, Norm.Infinity);
		double normQ = DenseDoubleAlgebra.DEFAULT.norm(Q, Norm.Infinity);

		if (verbose > 0) {
			int alg = jpopt.get("PF_ALG").intValue();
			String s = (alg == 2) ? "XB" : "BX";
			System.out.printf("(fast-decoupled, %s)\n", s);
		}
		if (verbose > 1) {
			System.out.printf("\niteration     max mismatch (p.u.)  ");
			System.out.printf("\ntype   #        P            Q     ");
			System.out.printf("\n---- ----  -----------  -----------");
			System.out.printf("\n  -  %3d   %10.3e   %10.3e", i, normP, normQ);
		}
		if (normP < tol && normQ < tol) {
			converged = true;
			if (verbose > 1)
				System.out.printf("\nConverged!\n");
		}

		/* reduce B matrices */
		// column-compressed format for factorisation
		SparseCCDoubleMatrix2D CCBp = new SparseCCDoubleMatrix2D(pvpq.length, pvpq.length);
		CCBp.assign(Bp.viewSelection(pvpq, pvpq));
		SparseCCDoubleMatrix2D CCBpp = new SparseCCDoubleMatrix2D(pq.length, pq.length);
		CCBpp.assign(Bpp.viewSelection(pq, pq));

		/* factor B matrices */
		SparseDoubleLUDecomposition luP = SparseDoubleAlgebra.DEFAULT.lu(CCBp, 0);
		SparseDoubleLUDecomposition luQ = SparseDoubleAlgebra.DEFAULT.lu(CCBpp, 0);

		/* do P and Q iterations */
		DComplexMatrix1D dVa;
		DComplexMatrix1D dVm;
		while (!converged && i < max_it) {
			/* update iteration counter */
			i += 1;

			/* -----  do P iteration, update Va  ----- */
			luP.solve(P);
			dVa = util.complex(P.assign(dfunc.neg), null);

			/* update voltage */
			Va.viewSelection(pvpq).assign(dVa, DComplexFunctions.plus);
			V = util.complex(Vm.getRealPart(), Va.getRealPart());

			/* evalute mismatch */
			mis = Ybus.zMult(V, null).assign(cfunc.conj);
			mis.assign(V, cfunc.mult).assign(Sbus, cfunc.minus).assign(Vm, cfunc.div);
			P = mis.viewSelection(pvpq).getRealPart();
			Q = mis.viewSelection(pq).getImaginaryPart();

			/* check tolerance */
			normP = DenseDoubleAlgebra.DEFAULT.norm(P, Norm.Infinity);
			normQ = DenseDoubleAlgebra.DEFAULT.norm(Q, Norm.Infinity);
			if (verbose > 1)
				System.out.printf("\n  P  %3d   %10.3e   %10.3e", i, normP, normQ);
			if (normP < tol && normQ < tol) {
				converged = true;
				if (verbose > 0)
					System.out.printf("\nFast-decoupled power flow converged in %d P-iterations and %d Q-iterations.\n", i, i-1);
				break;
			}

			/* -----  do Q iteration, update Vm  ----- */
			luQ.solve(Q);
			dVm = util.complex(P.assign(dfunc.neg), null);

			/* update voltage */
			Vm.viewSelection(pq).assign(dVm, cfunc.plus);
			V = util.complex(Vm.getRealPart(), Va.getRealPart());

			/* evalute mismatch */
			mis = Ybus.zMult(V, null).assign(cfunc.conj);
			mis.assign(V, cfunc.mult).assign(Sbus, cfunc.minus).assign(Vm, cfunc.div);
			P = mis.viewSelection(pvpq).getRealPart();
			Q = mis.viewSelection(pq).getImaginaryPart();

			/* check tolerance */
			normP = DenseDoubleAlgebra.DEFAULT.norm(P, Norm.Infinity);
			normQ = DenseDoubleAlgebra.DEFAULT.norm(Q, Norm.Infinity);
			if (verbose > 1)
				System.out.printf("\n  Q  %3d   %10.3e   %10.3e", i, normP, normQ);
			if (normP < tol && normQ < tol) {
				converged = true;
				if (verbose > 0)
					System.out.printf("\nFast-decoupled power flow converged in %d P-iterations and %d Q-iterations.\n", i, i);
				break;
			}
		}

		if (verbose > 0)
			if (!converged)
				System.out.printf("\nFast-decoupled power flow did not converge in %d iterations.\n", i);

		return new Object[] {V, converged, i};
	}
}
