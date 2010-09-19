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

import java.util.Map;

import cern.colt.matrix.tdouble.DoubleFactory1D;
import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.SparseRCDoubleMatrix2D;
import cern.jet.math.tdouble.DoubleFunctions;
import cern.jet.math.tint.IntFunctions;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_gen;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_gencost;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_jpc;
import edu.cornell.pserc.jpower.tdouble.opf.Djp_opf_model.Cost;
import edu.cornell.pserc.jpower.tdouble.opf.Djp_opf_model.Set;
import edu.cornell.pserc.util.tdouble.Djp_util;

/**
 * Evaluates objective function, gradient and Hessian for OPF.
 *
 * @author Ray Zimmerman (rz10@cornell.edu)
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class Djp_opf_costfcn {

	private static final Djp_util util = new Djp_util();
	private static final DoubleFunctions dfunc = DoubleFunctions.functions;
	private static final IntFunctions ifunc = IntFunctions.intFunctions;

	private static final int POLYNOMIAL = Djp_jpc.POLYNOMIAL;

	@SuppressWarnings({ "static-access", "unused" })
	public static Object[] jp_opf_costfcn(DoubleMatrix1D x, Djp_opf_model om) {

		/* unpack data */
		Djp_jpc jpc = om.get_jpc();
		double baseMVA = jpc.baseMVA;
		Djp_gen gen = jpc.gen;
		Djp_gencost gencost = jpc.gencost;
		Cost cp = om.get_cost_params();
		DoubleMatrix2D N = cp.N, H = cp.H;
		DoubleMatrix1D Cw = cp.Cw, dd = cp.dd, rh = cp.rh, kk = cp.kk, mm = cp.mm;
		Map<String, Set> vv = om.get_idx();

		/* problem dimensions */
		int ng = gen.size();			// number of dispatchable injections
		int ny = om.getN("var", "y");	// number of piece-wise linear costs
		int nxyz = (int) x.size();		// total number of control vars of all types

		/* grab Pg & Qg */
		DoubleMatrix1D Pg = x.viewPart(vv.get("Pg").i0, vv.get("Pg").N);	// active generation in p.u.
		DoubleMatrix1D Qg = x.viewPart(vv.get("Qg").i0, vv.get("Qg").N);	// reactive generation in p.u.

		/* ----- evaluate objective function ----- */

		/* polynomial cost of P and Q */
		// use totcost only on polynomial cost; in the minimization problem
		// formulation, pwl cost is the sum of the y variables.
		int[] ipol = util.nonzero(gencost.model.copy().assign(ifunc.equals(POLYNOMIAL)));	// poly MW and MVAr costs
		DoubleMatrix1D xx = DoubleFactory1D.dense.append(Pg, Qg).assign(dfunc.mult(baseMVA));
		double f = 0;
		if (ipol.length > 0)
			f = Djp_totcost.jp_totcost(gencost.copy(ipol), xx.viewSelection(ipol)).aggregate(dfunc.plus, dfunc.identity);

		/* piecewise linear cost of P and Q */
		DoubleMatrix1D ccost;
		if (ny > 0) {
			ccost = DoubleFactory1D.dense.make(new SparseRCDoubleMatrix2D(1, nxyz, util.ones(ny),
					util.irange(vv.get("y").i0, vv.get("y").iN), 1, false, false).viewRow(0).toArray());
			f += ccost.zDotProduct(x);
		} else {
			ccost = DoubleFactory1D.dense.make(nxyz);
		}

		/* generalized cost term */
		DoubleMatrix1D w = null;
		DoubleMatrix2D diagrr = null, LL = null, QQ = null, M = null;
		if (N != null) {
			int nw = N.rows();
			DoubleMatrix1D r = N.zMult(x, null).assign(rh, dfunc.minus);	// generalized cost
			int[] iLT = util.nonzero(r.copy().assign(kk.copy().assign(dfunc.neg), dfunc.less));	// below dead zone
			int[] iEQ = util.nonzero( util.intm(r.copy().assign(dfunc.equals(0))).assign(util.intm(kk.copy().assign(dfunc.equals(0))), ifunc.and) );	// dead zone doesn't exist
			int[] iGT = util.nonzero(r.copy().assign(kk, dfunc.less));	// above dead zone
			int[] iND = util.icat(iLT, util.icat(iEQ, iGT));			// rows that are Not in the Dead region
			int[] iL = util.nonzero(dd.copy().assign(dfunc.equals(1)));	// rows using linear function
			int[] iQ = util.nonzero(dd.copy().assign(dfunc.equals(2)));	// rows using quadratic function
			LL = new SparseRCDoubleMatrix2D(nw, nw, iL, iL, 1, false, false);
			QQ = new SparseRCDoubleMatrix2D(nw, nw, iQ, iQ, 1, false, false);
			DoubleMatrix1D[] kbar_v = new DoubleMatrix1D[] {
					DoubleFactory1D.dense.make(iLT.length, 1),
					DoubleFactory1D.dense.make(iEQ.length),
					DoubleFactory1D.dense.make(iGT.length, -1)};
			DoubleMatrix1D kbar = new SparseRCDoubleMatrix2D(nw, nw, iND, iND,
					DoubleFactory1D.dense.make(kbar_v).toArray(), false, false, false).zMult(kk, null);
			DoubleMatrix1D rr = r.assign(kbar, dfunc.plus);	// apply non-dead zone shift
			M = new SparseRCDoubleMatrix2D(nw, nw, iND, iND,
					mm.viewSelection(iND).toArray(), false, false, false);	// dead zone or scale
			diagrr = DoubleFactory2D.sparse.diagonal(rr);

			/* linear rows multiplied by rr(i), quadratic rows by rr(i)^2 */
			w = LL.copy().assign(QQ.zMult(diagrr, null), dfunc.plus).zMult(M, null).zMult(rr, null);

			f += w.zDotProduct(H.zMult(w, null)) / 2 + Cw.zDotProduct(w);
		}

		/* ----- evaluate cost gradient ----- */

		/* index ranges */
		int[] iPg = util.irange(vv.get("Pg").i0, vv.get("Pg").iN);
		int[] iQg = util.irange(vv.get("Qg").i0, vv.get("Qg").iN);

		/* polynomial cost of P and Q */
		DoubleMatrix1D df_dPgQg = DoubleFactory1D.dense.make(2 * ng);
		df_dPgQg.viewSelection(ipol).assign( Djp_polycost.jp_polycost(gencost.copy(ipol), xx.viewSelection(ipol), 1).assign(dfunc.mult(baseMVA)) );
		DoubleMatrix1D df = DoubleFactory1D.dense.make(nxyz);
		df.viewSelection(iPg).assign( df_dPgQg.viewPart(0, ng) );
		df.viewSelection(iQg).assign( df_dPgQg.viewPart(ng, ng) );

		/* piecewise linear cost of P and Q */
		df.assign(ccost, dfunc.plus);	// As in MINOS, the linear cost row is additive wrt
										// any nonlinear cost.

		/* generalized cost term */
		DoubleMatrix1D HwC = null;
		DoubleMatrix2D AA = null;
		if (N != null) {
			HwC = H.zMult(w, null).assign(Cw, dfunc.plus);
			AA = QQ.zMult(diagrr, null).assign(dfunc.mult(2)).assign(LL, dfunc.plus).zMult(M, null).zMult(N.viewDice(), null);

			/* numerical check */
			if (false) {	// true to check, false to skip check
				DoubleMatrix1D ddff = DoubleFactory1D.dense.make((int) df.size());
				double step = 1e-7;
				double tol  = 1e-3;
				for (int k = 0; k < x.size(); k++) {
					DoubleMatrix1D xxx = x.copy();
					xxx.set(k, xxx.get(k) + step);
//					ddff.set(k, Djp_opf_costfcn.jp_opf_costfcn(xx, om) - f / step);
				}
				DoubleMatrix1D df_diff = ddff.copy().assign(df, dfunc.minus).assign(dfunc.abs);
				double df_diff_max = df_diff.aggregate(dfunc.max, dfunc.identity);
				if (df_diff_max > tol) {
					int[] idx = util.nonzero( df_diff.assign(dfunc.equals(df_diff_max)) );
					System.out.printf("\nMismatch in gradient\n");
					System.out.printf("idx             df(num)         df              diff\n");
					for (int k = 0; k < df.size(); k++)
						System.out.printf( "%4d%16g%16g%16g\n", k, ddff.get(k), df.get(k), df_diff.get(k) );
					System.out.printf("MAX\n");
					for (int k : idx)
						System.out.printf( "%4d%16g%16g%16g\n", k, ddff.get(k), df.get(k), df_diff.get(k) );
					System.out.printf("\n");
				}
			}
		}

		/* ----- evaluate cost Hessian ----- */

		Djp_gencost pcost = gencost.copy(util.irange(ng));
		Djp_gencost qcost = null;
		if (gencost.size() > ng)
			qcost = gencost.copy(util.irange(ng, 2*ng));

		/* polynomial generator costs */
		DoubleMatrix1D d2f_dPg2 = DoubleFactory1D.sparse.make(ng);	// w.r.t. p.u. Pg
		DoubleMatrix1D d2f_dQg2 = DoubleFactory1D.sparse.make(ng);	// w.r.t. p.u. Qg
		int[] ipolp = util.nonzero(pcost.model.copy().assign(ifunc.equals(POLYNOMIAL)));
		d2f_dPg2.assign( Djp_polycost.jp_polycost(pcost.copy(ipolp), Pg.viewSelection(ipolp).copy().assign(dfunc.mult(baseMVA)), 2).assign(dfunc.mult(Math.pow(baseMVA, 2))) );
		if (qcost != null) {	// Qg is not free
			int[] ipolq = util.nonzero(qcost.model.copy().assign(ifunc.equals(POLYNOMIAL)));
			d2f_dQg2.assign( Djp_polycost.jp_polycost(qcost.copy(ipolq), Qg.viewSelection(ipolq).copy().assign(dfunc.mult(baseMVA)), 2).assign(dfunc.mult(Math.pow(baseMVA, 2))) );
		}
		int[] i = util.icat(iPg, iQg);
		DoubleMatrix2D d2f = new SparseRCDoubleMatrix2D(nxyz, nxyz, i, i,
				DoubleFactory1D.sparse.append(d2f_dPg2, d2f_dQg2).toArray(), false, false, false);

		/* generalized cost */
		if (N != null) {
			DoubleMatrix2D diagHwC = DoubleFactory2D.sparse.diagonal(HwC);
			d2f.assign(AA.zMult(H, null).zMult(AA.viewDice(), null).assign(N.viewDice().copy().zMult(M, null).zMult(QQ, null).zMult(diagHwC, null).zMult(N, null), dfunc.plus), dfunc.plus);
		}

		return new Object[] {f, df, d2f};
	}
}
