/*
 * Copyright (C) 1996-2010 Power System Engineering Research Center
 * Copyright (C) 2010-2011 Richard Lincoln
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package edu.cornell.pserc.jpower.pf;

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

import static edu.cornell.pserc.jpower.Djp_jpoption.jpoption;

import static edu.emory.mathcs.utils.Utils.dfunc;
import static edu.emory.mathcs.utils.Utils.cfunc;
import static edu.emory.mathcs.utils.Utils.icat;
import static edu.emory.mathcs.utils.Utils.polar;
import static edu.emory.mathcs.utils.Utils.complex;

/**
 * Solves the power flow using a fast decoupled method.
 *
 * @author Ray Zimmerman
 * @author Richard Lincoln
 *
 */
@SuppressWarnings("static-access")
public class Djp_fdpf {

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
	public static Object[] fdpf(DComplexMatrix2D Ybus, DComplexMatrix1D Sbus, DComplexMatrix1D V0,
			DoubleMatrix2D Bp, DoubleMatrix2D Bpp, int ref, int[] pv, int[] pq, Map<String, Double> jpopt) {
		double tol, normP, normQ;
		int i, max_it, alg, verbose;
		int[] pvpq;
		boolean converged;
		DComplexMatrix1D V, Va, Vm, mis;
		DoubleMatrix1D P, Q;
		SparseCCDoubleMatrix2D CCBp, CCBpp;
		SparseDoubleLUDecomposition luP, luQ;
		DComplexMatrix1D dVa, dVm;

		/* options */
		tol	= jpopt.get("PF_TOL");
		max_it	= jpopt.get("PF_MAX_IT").intValue();
		verbose	= jpopt.get("VERBOSE").intValue();

		/* initialize */
		pvpq = icat(pv, pq);
		converged = false;
		i = 0;
		V = V0.copy();
		Va = V.copy().assign(cfunc.arg);
		Vm = V.copy().assign(cfunc.abs);

		/* evaluate initial mismatch */
		mis = Ybus.zMult(V, null).assign(cfunc.conj);
		mis.assign(V, cfunc.mult).assign(Sbus, cfunc.minus).assign(Vm, cfunc.div);
		P = mis.viewSelection(pvpq).getRealPart();
		Q = mis.viewSelection(pq).getImaginaryPart();

		/* check tolerance */
		normP = DenseDoubleAlgebra.DEFAULT.norm(P, Norm.Infinity);
		normQ = DenseDoubleAlgebra.DEFAULT.norm(Q, Norm.Infinity);

		if (verbose > 0) {
			alg = jpopt.get("PF_ALG").intValue();
			System.out.printf("(fast-decoupled, %s)\n", (alg == 2) ? "XB" : "BX");
		}
		if (verbose > 1) {
			System.out.printf("\niteration     max mismatch (p.u.)  ");
			System.out.printf("\ntype   #        P            Q     ");
			System.out.printf("\n---- ----  -----------  -----------");
			System.out.printf("\n  -  %3d   %10.3e   %10.3e", i, normP, normQ);
		}
		if ((normP < tol) & (normQ < tol)) {
			converged = true;
			if (verbose > 1)
				System.out.printf("\nConverged!\n");
		}

		/* reduce B matrices */
		// column-compressed format for factorisation
		CCBp = new SparseCCDoubleMatrix2D(pvpq.length, pvpq.length);
		CCBp.assign(Bp.viewSelection(pvpq, pvpq));
		CCBpp = new SparseCCDoubleMatrix2D(pq.length, pq.length);
		CCBpp.assign(Bpp.viewSelection(pq, pq));

		/* factor B matrices */
		luP = SparseDoubleAlgebra.DEFAULT.lu(CCBp, 0);
		luQ = SparseDoubleAlgebra.DEFAULT.lu(CCBpp, 0);

		/* do P and Q iterations */
		while ((!converged) & (i < max_it)) {
			/* update iteration counter */
			i += 1;

			/* -----  do P iteration, update Va  ----- */
			luP.solve(P);
			dVa = complex(P.assign(dfunc.neg), null);

			/* update voltage */
			Va.viewSelection(pvpq).assign(dVa, cfunc.plus);
			V = polar(Vm.getRealPart(), Va.getRealPart());

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
			if ((normP < tol) & (normQ < tol)) {
				converged = true;
				if (verbose > 0)
					System.out.printf("\nFast-decoupled power flow converged in %d P-iterations and %d Q-iterations.\n", i, i-1);
				break;
			}

			/* -----  do Q iteration, update Vm  ----- */
			luQ.solve(Q);
			dVm = complex(Q.assign(dfunc.neg), null);

			/* update voltage */
			Vm.viewSelection(pq).assign(dVm, cfunc.plus);
			V = polar(Vm.getRealPart(), Va.getRealPart());

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

	public static Object[] fdpf(DComplexMatrix2D Ybus, DComplexMatrix1D Sbus, DComplexMatrix1D V0,
			DoubleMatrix2D Bp, DoubleMatrix2D Bpp, int ref, int[] pv, int[] pq) {
		return fdpf(Ybus, Sbus, V0, Bp, Bpp, ref, pv, pq, jpoption());
	}

}
