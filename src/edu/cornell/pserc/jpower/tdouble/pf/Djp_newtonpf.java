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

package edu.cornell.pserc.jpower.tdouble.pf;

import java.util.Map;

import cern.colt.matrix.Norm;
import cern.colt.matrix.tdcomplex.DComplexMatrix1D;
import cern.colt.matrix.tdcomplex.DComplexMatrix2D;
import cern.colt.matrix.tdouble.DoubleFactory1D;
import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.algo.DenseDoubleAlgebra;
import cern.colt.matrix.tdouble.algo.SparseDoubleAlgebra;
import cern.colt.matrix.tdouble.impl.SparseRCDoubleMatrix2D;
import cern.colt.util.tdouble.Djp_util;
import cern.jet.math.tdcomplex.DComplexFunctions;
import cern.jet.math.tdouble.DoubleFunctions;
import edu.cornell.pserc.jpower.tdouble.Djp_jpoption;

/**
 * Solves the power flow using a full Newton's method.
 *
 * @author Ray Zimmerman (rz10@cornell.edu)
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class Djp_newtonpf {

	private static final Djp_util util = new Djp_util();
	private static final DoubleFunctions dfunc = DoubleFunctions.functions;
	private static final DComplexFunctions cfunc = DComplexFunctions.functions;

	public static Object[] jp_newtonpf(DComplexMatrix2D Ybus, DComplexMatrix1D Sbus,
			DComplexMatrix1D V0, int ref, int[] pv, int[] pq) {
		return jp_newtonpf(Ybus, Sbus, V0, ref, pv, pq, Djp_jpoption.jp_jpoption());
	}

	/**
	 * Solves for bus voltages given the full system admittance matrix (for
	 * all buses), the complex bus power injection vector (for all buses),
	 * the initial vector of complex bus voltages, and column vectors with
	 * the lists of bus indices for the swing bus, PV buses, and PQ buses,
	 * respectively. The bus voltage vector contains the set point for
	 * generator (including ref bus) buses, and the reference angle of the
	 * swing bus, as well as an initial guess for remaining magnitudes and
	 * angles. JPOPT is a JPOWER options vector which can be used to
	 * set the termination tolerance, maximum number of iterations, and
	 * output options (see JPOPTION for details). Uses default options if
	 * this parameter is not given. Returns the final complex voltages, a
	 * flag which indicates whether it converged or not, and the number of
	 * iterations performed.
	 *
	 * @param Ybus
	 * @param Sbus
	 * @param V0
	 * @param ref
	 * @param pv
	 * @param pq
	 * @param jpopt
	 * @return
	 */
	@SuppressWarnings("static-access")
	public static Object[] jp_newtonpf(DComplexMatrix2D Ybus, DComplexMatrix1D Sbus,
			DComplexMatrix1D V0, int ref, int[] pv, int[] pq, Map<String, Double> jpopt) {

		/* options */
		double tol	= jpopt.get("PF_TOL");
		int max_it	= jpopt.get("PF_MAX_IT").intValue();
		int verbose	= jpopt.get("VERBOSE").intValue();

		/* initialize */
		int[] pvpq = util.icat(pv, pq);
		boolean converged = false;
		int i = 0;
		DComplexMatrix1D V = V0;
		DComplexMatrix1D Va = V.copy().assign(cfunc.arg);
		DComplexMatrix1D Vm = V.copy().assign(cfunc.abs);

		/* set up indexing for updating V */
		int npv = pv.length;
		int npq = pq.length;
//		int j1 = 0,			j2 = npv;			// j1:j2 - V angle of pv buses
//		int j3 = j2 + 1,	j4 = j2 + npq;		// j3:j4 - V angle of pq buses
//		int j5 = j4 + 1,	j6 = j4 + npq;		// j5:j6 - V mag of pq buses

		/* evaluate F(x0) */
		DComplexMatrix1D mis = Ybus.zMult(V, null).assign(cfunc.conj);
		mis.assign(V, cfunc.mult).assign(Sbus, cfunc.minus);
		DoubleMatrix1D F = DoubleFactory1D.sparse.make(new DoubleMatrix1D[] {
				mis.viewSelection(pvpq).getRealPart(),
				mis.viewSelection(pq).getImaginaryPart() });

		/* check tolerance */
		double normF = DenseDoubleAlgebra.DEFAULT.norm(F, Norm.Infinity);
		if (verbose > 0)
			System.out.print("(Newton)\n");
		if (verbose > 1) {
			System.out.printf("\n it    max P & Q mismatch (p.u.)");
			System.out.printf("\n----  ---------------------------");
			System.out.printf("\n%3d        %10.3e", i, normF);
		}
		if (normF < tol) {
			converged = true;
			if (verbose > 1)
				System.out.printf("\nConverged!\n");
		}

		/* do Newton iterations */
		while (!converged && i < max_it) {
			/* update iteration counter */
			i += 1;

			/* evaluate Jacobian */
			DComplexMatrix2D[] dSbus_dV = Djp_dSbus_dV.jp_dSbus_dV(Ybus, V);
			DComplexMatrix2D dSbus_dVm = dSbus_dV[0];
			DComplexMatrix2D dSbus_dVa = dSbus_dV[1];

			DoubleMatrix2D J11 = dSbus_dVa.getRealPart().viewSelection(pvpq, pvpq).copy();
			DoubleMatrix2D J12 = dSbus_dVm.getRealPart().viewSelection(pvpq, pq).copy();
			DoubleMatrix2D J21 = dSbus_dVa.getImaginaryPart().viewSelection(pq, pvpq).copy();
			DoubleMatrix2D J22 = dSbus_dVm.getImaginaryPart().viewSelection(pq, pq).copy();
			DoubleMatrix2D J1 = DoubleFactory2D.sparse.appendColumns(J11, J12);
			DoubleMatrix2D J2 = DoubleFactory2D.sparse.appendColumns(J21, J22);
			DoubleMatrix2D J = DoubleFactory2D.sparse.appendRows(J1, J2);
			SparseRCDoubleMatrix2D JJ = new SparseRCDoubleMatrix2D(J.rows(), J.columns());
			JJ.assign(J);

			DoubleMatrix1D dx = SparseDoubleAlgebra.DEFAULT.solve(JJ, F).assign(dfunc.neg);
			DComplexMatrix1D dxz = util.complex(dx, null);

			/* update voltage */
			if (npv > 0)
				Va.viewSelection(pv).assign(dxz.viewPart(0, npv), DComplexFunctions.plus);
			if (npq > 0) {
				Va.viewSelection(pq).assign(dxz.viewPart(npv, npq), cfunc.plus);
				Vm.viewSelection(pq).assign(dxz.viewPart(npv + npq, npq), cfunc.plus);
			}

			V = util.complex(Vm.getRealPart(), Va.getRealPart());
			/* update Vm and Va again in case we wrapped around with a negative Vm */
			Va = V.copy().assign(cfunc.arg);
			Vm = V.copy().assign(cfunc.abs);

			/* evalute F(x) */
			mis = Ybus.zMult(V, null).assign(cfunc.conj).assign(V, cfunc.mult).assign(Sbus, cfunc.minus);
			F = DoubleFactory1D.sparse.make(new DoubleMatrix1D[] {
					mis.viewSelection(pvpq).getRealPart(),
					mis.viewSelection(pq).getImaginaryPart() });

			/* check for convergence */
			normF = DenseDoubleAlgebra.DEFAULT.norm(F, Norm.Infinity);
			if (verbose > 1)
				System.out.printf("\n%3d        %10.3e", i, normF);
			if (normF < tol) {
				converged = true;
				if (verbose > 0)
					System.out.printf("\nNewton's method power flow converged in %d iterations.\n", i);
			}
		}
		if (verbose > 0)
			if (!converged)
				System.out.printf("\nNewton''s method power did not converge in %d iterations.\n", i);

		return new Object[] {V, converged, i};
	}
}
