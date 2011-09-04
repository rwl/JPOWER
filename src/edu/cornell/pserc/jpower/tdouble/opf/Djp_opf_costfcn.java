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

import java.util.Map;

import cern.colt.matrix.tdouble.DoubleFactory1D;
import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.SparseRCDoubleMatrix2D;

import static cern.colt.util.tdouble.Djp_util.ifunc;
import static cern.colt.util.tdouble.Djp_util.dfunc;
import static cern.colt.util.tdouble.Djp_util.ones;
import static cern.colt.util.tdouble.Djp_util.irange;
import static cern.colt.util.tdouble.Djp_util.nonzero;
import static cern.colt.util.tdouble.Djp_util.intm;
import static cern.colt.util.tdouble.Djp_util.icat;

import static edu.cornell.pserc.jpower.tdouble.opf.Djp_totcost.totcost;
import static edu.cornell.pserc.jpower.tdouble.opf.Djp_polycost.polycost;
import static edu.cornell.pserc.jpower.tdouble.jpc.Djp_jpc.POLYNOMIAL;

import edu.cornell.pserc.jips.tdouble.ObjectiveEvaluator;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_gen;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_gencost;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_jpc;
import edu.cornell.pserc.jpower.tdouble.opf.Djp_opf_model.Cost;
import edu.cornell.pserc.jpower.tdouble.opf.Djp_opf_model.Set;

/**
 * Evaluates objective function, gradient and Hessian for OPF.
 *
 * @author Ray Zimmerman
 * @author Richard Lincoln
 *
 */
public class Djp_opf_costfcn implements ObjectiveEvaluator {

	private boolean numericalCheck;

	private Djp_opf_model om;

	private DoubleMatrix1D ccost, w, HwC;
	private DoubleMatrix2D diagrr, LL, QQ, M, AA;

	public Djp_opf_costfcn(Djp_opf_model om) {
		super();
		this.om = om;
		this.numericalCheck = false;
	}

	@SuppressWarnings("static-access")
	public double f(DoubleMatrix1D x) {
		int ny, nxyz, nw;
		int[] ipol, iLT, iEQ, iGT, iND, iL, iQ;
		double baseMVA, f;
		Djp_jpc jpc;
		Djp_gencost gencost;
		Cost cp;

		Map<String, Set> vv;

		DoubleMatrix1D Cw, dd, rh, kk, mm, Pg, Qg, xx, r, kbar, rr;
		DoubleMatrix1D[] kbar_v;
		DoubleMatrix2D N, H;


		/* unpack data */
		jpc = om.get_jpc();
		baseMVA = jpc.baseMVA;
		gencost = jpc.gencost;
		cp = om.get_cost_params();
		N = cp.N; H = cp.H;
		Cw = cp.Cw; dd = cp.dd; rh = cp.rh; kk = cp.kk; mm = cp.mm;
		vv = om.get_idx()[0];

		/* problem dimensions */
		ny = om.getN("var", "y");	// number of piece-wise linear costs
		nxyz = (int) x.size();		// total number of control vars of all types

		/* grab Pg & Qg */
		Pg = x.viewPart(vv.get("Pg").i0, vv.get("Pg").N);	// active generation in p.u.
		Qg = x.viewPart(vv.get("Qg").i0, vv.get("Qg").N);	// reactive generation in p.u.

		/* ----- evaluate objective function ----- */

		/* polynomial cost of P and Q */
		// use totcost only on polynomial cost; in the minimization problem
		// formulation, pwl cost is the sum of the y variables.
		ipol = nonzero(gencost.model.copy().assign(ifunc.equals(POLYNOMIAL)));	// poly MW and MVAr costs
		xx = DoubleFactory1D.dense.append(Pg, Qg).assign(dfunc.mult(baseMVA));
		f = 0;
		if (ipol.length > 0)
			f = totcost(gencost.copy(ipol), xx.viewSelection(ipol)).aggregate(dfunc.plus, dfunc.identity);

		/* piecewise linear cost of P and Q */
//		DoubleMatrix1D ccost;
		if (ny > 0) {
			ccost = DoubleFactory1D.dense.make(new SparseRCDoubleMatrix2D(1, nxyz, ones(ny),
					irange(vv.get("y").i0, vv.get("y").iN), 1, false, false).viewRow(0).toArray());
			f += ccost.zDotProduct(x);
		} else {
			ccost = DoubleFactory1D.dense.make(nxyz);
		}

		/* generalized cost term */
//		DoubleMatrix1D w = null;
//		DoubleMatrix2D diagrr = null, LL = null, QQ = null, M = null;
		if (N != null) {
			nw = N.rows();
			r = N.zMult(x, null).assign(rh, dfunc.minus);  // generalized cost
			iLT = nonzero(r.copy().assign(kk.copy().assign(dfunc.neg), dfunc.less));  // below dead zone
			iEQ = nonzero( intm(r.copy().assign(dfunc.equals(0))).assign(intm(kk.copy().assign(dfunc.equals(0))), ifunc.and) );  // dead zone doesn't exist
			iGT = nonzero(r.copy().assign(kk, dfunc.less));	// above dead zone
			iND = icat(iLT, icat(iEQ, iGT));			// rows that are Not in the Dead region
			iL  = nonzero(dd.copy().assign(dfunc.equals(1)));	// rows using linear function
			iQ  = nonzero(dd.copy().assign(dfunc.equals(2)));	// rows using quadratic function
			LL = new SparseRCDoubleMatrix2D(nw, nw, iL, iL, 1, false, false);
			QQ = new SparseRCDoubleMatrix2D(nw, nw, iQ, iQ, 1, false, false);
			kbar_v = new DoubleMatrix1D[] {
					DoubleFactory1D.dense.make(iLT.length, 1),
					DoubleFactory1D.dense.make(iEQ.length),
					DoubleFactory1D.dense.make(iGT.length, -1)};
			kbar = new SparseRCDoubleMatrix2D(nw, nw, iND, iND,
					DoubleFactory1D.dense.make(kbar_v).toArray(), false, false, false).zMult(kk, null);
			rr = r.assign(kbar, dfunc.plus);	// apply non-dead zone shift
			M = new SparseRCDoubleMatrix2D(nw, nw, iND, iND,
					mm.viewSelection(iND).toArray(), false, false, false);	// dead zone or scale
			diagrr = DoubleFactory2D.sparse.diagonal(rr);

			/* linear rows multiplied by rr(i), quadratic rows by rr(i)^2 */
			w = LL.copy().assign(QQ.zMult(diagrr, null), dfunc.plus).zMult(M, null).zMult(rr, null);

			f += w.zDotProduct(H.zMult(w, null)) / 2 + Cw.zDotProduct(w);
		}
		return f;
	}

	@SuppressWarnings("static-access")
	public DoubleMatrix1D df(DoubleMatrix1D x) {
		int ng, ny, nxyz;
		int[] iPg, iQg, ipol, idx;
		double baseMVA, step, tol, df_diff_max;
		Djp_jpc jpc;
		Djp_gen gen;
		Djp_gencost gencost;
		Cost cp;

		Map<String, Set> vv;

		DoubleMatrix2D N, H;
		DoubleMatrix1D Cw, dd, rh, kk, mm, Pg, Qg, df_dPgQg, xx, df, ddff, xxx, df_diff;


		/* unpack data */
		jpc = om.get_jpc();
		baseMVA = jpc.baseMVA;
		gen = jpc.gen;
		gencost = jpc.gencost;
		cp = om.get_cost_params();
		N = cp.N; H = cp.H;
		Cw = cp.Cw; dd = cp.dd; rh = cp.rh; kk = cp.kk; mm = cp.mm;
		vv = om.get_idx()[0];

		/* problem dimensions */
		ng = gen.size();			// number of dispatchable injections
		ny = om.getN("var", "y");	// number of piece-wise linear costs
		nxyz = (int) x.size();		// total number of control vars of all types

		/* grab Pg & Qg */
		Pg = x.viewPart(vv.get("Pg").i0, vv.get("Pg").N);	// active generation in p.u.
		Qg = x.viewPart(vv.get("Qg").i0, vv.get("Qg").N);	// reactive generation in p.u.

		/* ----- evaluate cost gradient ----- */

		/* index ranges */
		iPg = irange(vv.get("Pg").i0, vv.get("Pg").iN);
		iQg = irange(vv.get("Qg").i0, vv.get("Qg").iN);

		/* polynomial cost of P and Q */
		df_dPgQg = DoubleFactory1D.dense.make(2 * ng);
		ipol = nonzero(gencost.model.copy().assign(ifunc.equals(POLYNOMIAL)));
		xx = DoubleFactory1D.dense.append(Pg, Qg).assign(dfunc.mult(baseMVA));
		df_dPgQg.viewSelection(ipol).assign( polycost(gencost.copy(ipol), xx.viewSelection(ipol), 1).assign(dfunc.mult(baseMVA)) );
		df = DoubleFactory1D.dense.make(nxyz);
		df.viewSelection(iPg).assign( df_dPgQg.viewPart(0, ng) );
		df.viewSelection(iQg).assign( df_dPgQg.viewPart(ng, ng) );

		/* piecewise linear cost of P and Q */
		df.assign(ccost, dfunc.plus);	// As in MINOS, the linear cost row is additive wrt
										// any nonlinear cost.

		/* generalized cost term */
		if (N != null) {
			HwC = H.zMult(w, null).assign(Cw, dfunc.plus);
			AA = QQ.zMult(diagrr, null).assign(dfunc.mult(2)).assign(LL, dfunc.plus).zMult(M, null).zMult(N.viewDice(), null);

			/* numerical check */
			if (numericalCheck) {	// true to check, false to skip check
				ddff = DoubleFactory1D.dense.make((int) df.size());
				step = 1e-7;
				tol  = 1e-3;
				for (int k = 0; k < x.size(); k++) {
					xxx = x.copy();
					xxx.set(k, xxx.get(k) + step);
//					ddff.set(k, opf_costfcn(xx, om) - f / step);
				}
				df_diff = ddff.copy().assign(df, dfunc.minus).assign(dfunc.abs);
				df_diff_max = df_diff.aggregate(dfunc.max, dfunc.identity);
				if (df_diff_max > tol) {
					idx = nonzero( df_diff.assign(dfunc.equals(df_diff_max)) );
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
		return df;
	}

	@SuppressWarnings("static-access")
	public DoubleMatrix2D d2f(DoubleMatrix1D x) {
		int ng, nxyz;
		int[] iPg, iQg, ipolp, ipolq, i;
		double baseMVA;
		Djp_jpc jpc;
		Djp_gen gen;
		Djp_gencost gencost;
		Djp_gencost pcost, qcost;
		Cost cp;

		Map<String, Set> vv;

		DoubleMatrix1D Pg, Qg, d2f_dPg2, d2f_dQg2;
		DoubleMatrix2D N, H, d2f, diagHwC;

		/* unpack data */
		jpc = om.get_jpc();
		baseMVA = jpc.baseMVA;
		gen = jpc.gen;
		gencost = jpc.gencost;
		cp = om.get_cost_params();
		N = cp.N; H = cp.H;
		vv = om.get_idx()[0];

		/* problem dimensions */
		ng = gen.size();			// number of dispatchable injections
		nxyz = (int) x.size();		// total number of control vars of all types

		/* grab Pg & Qg */
		Pg = x.viewPart(vv.get("Pg").i0, vv.get("Pg").N);	// active generation in p.u.
		Qg = x.viewPart(vv.get("Qg").i0, vv.get("Qg").N);	// reactive generation in p.u.

		/* index ranges */
		iPg = irange(vv.get("Pg").i0, vv.get("Pg").iN);
		iQg = irange(vv.get("Qg").i0, vv.get("Qg").iN);

		/* ----- evaluate cost Hessian ----- */

		pcost = gencost.copy(irange(ng));
		qcost = null;
		if (gencost.size() > ng)
			qcost = gencost.copy(irange(ng, 2*ng));

		/* polynomial generator costs */
		d2f_dPg2 = DoubleFactory1D.sparse.make(ng);	// w.r.t. p.u. Pg
		d2f_dQg2 = DoubleFactory1D.sparse.make(ng);	// w.r.t. p.u. Qg
		ipolp = nonzero(pcost.model.copy().assign(ifunc.equals(POLYNOMIAL)));
		d2f_dPg2.assign( Djp_polycost.polycost(pcost.copy(ipolp), Pg.viewSelection(ipolp).copy().assign(dfunc.mult(baseMVA)), 2).assign(dfunc.mult(Math.pow(baseMVA, 2))) );
		if (qcost != null) {	// Qg is not free
			ipolq = nonzero(qcost.model.copy().assign(ifunc.equals(POLYNOMIAL)));
			d2f_dQg2.assign( Djp_polycost.polycost(qcost.copy(ipolq), Qg.viewSelection(ipolq).copy().assign(dfunc.mult(baseMVA)), 2).assign(dfunc.mult(Math.pow(baseMVA, 2))) );
		}
		i = icat(iPg, iQg);
		d2f = new SparseRCDoubleMatrix2D(nxyz, nxyz, i, i,
				DoubleFactory1D.sparse.append(d2f_dPg2, d2f_dQg2).toArray(), false, false, false);

		/* generalized cost */
		if (N != null) {
			diagHwC = DoubleFactory2D.sparse.diagonal(HwC);
			d2f.assign(AA.zMult(H, null).zMult(AA.viewDice(), null).assign(N.viewDice().copy().zMult(M, null).zMult(QQ, null).zMult(diagHwC, null).zMult(N, null), dfunc.plus), dfunc.plus);
		}

		return d2f;
	}

}
