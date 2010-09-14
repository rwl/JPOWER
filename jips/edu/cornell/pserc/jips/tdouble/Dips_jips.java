/*
 * Copyright (C) 1996-2010 by Power System Engineering Research Center (PSERC)
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

package edu.cornell.pserc.jips.tdouble;

import java.util.HashMap;
import java.util.Map;

import cern.colt.matrix.Norm;
import cern.colt.matrix.tdouble.DoubleFactory1D;
import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.algo.DenseDoubleAlgebra;
import cern.colt.matrix.tdouble.algo.SparseDoubleAlgebra;
import cern.colt.matrix.tint.IntFactory1D;
import cern.colt.matrix.tint.IntMatrix1D;
import cern.jet.math.tdouble.DoubleFunctions;
import cern.jet.math.tint.IntFunctions;
import edu.cornell.pserc.jpower.tdouble.util.Djp_util;

/**
 * Java Interior Point Solver.
 *
 * @author Ray Zimmerman (rz10@cornell.edu)
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class Dips_jips {

	private static final Djp_util util = new Djp_util();
	private static final IntFunctions ifunc = IntFunctions.intFunctions;
	private static final DoubleFunctions dfunc = DoubleFunctions.functions;

	public static Object[] ips_jips(ObjectiveEvaluator f_fcn, DoubleMatrix1D x0) {
		int nx = (int) x0.size();
		DoubleMatrix2D A = DoubleFactory2D.sparse.make(0, nx);
		DoubleMatrix1D l = DoubleFactory1D.dense.make(0);
		DoubleMatrix1D u = DoubleFactory1D.dense.make(0);
		return ips_jips(f_fcn, x0, A, l, u);
	}

	public static Object[] ips_jips(ObjectiveEvaluator f_fcn, DoubleMatrix1D x0,
			DoubleMatrix2D A, DoubleMatrix1D l, DoubleMatrix1D u) {
		int nx = (int) x0.size();
		DoubleMatrix1D xmin = DoubleFactory1D.dense.make(nx, Double.NEGATIVE_INFINITY);
		DoubleMatrix1D xmax = DoubleFactory1D.dense.make(nx, Double.POSITIVE_INFINITY);
		return ips_jips(f_fcn, x0, A, l, u, xmin, xmax);
	}

	public static Object[] ips_jips(ObjectiveEvaluator f_fcn, DoubleMatrix1D x0,
			DoubleMatrix2D A, DoubleMatrix1D l, DoubleMatrix1D u, DoubleMatrix1D xmin, DoubleMatrix1D xmax) {
		return ips_jips(f_fcn, x0, A, l, u, xmin, xmax, null, null);
	}

	public static Object[] ips_jips(ObjectiveEvaluator f_fcn, DoubleMatrix1D x0,
			DoubleMatrix2D A, DoubleMatrix1D l, DoubleMatrix1D u, DoubleMatrix1D xmin, DoubleMatrix1D xmax,
			ConstraintEvaluator gh_fcn) {
		return ips_jips(f_fcn, x0, A, l, u, xmin, xmax, gh_fcn, null);
	}

	public static Object[] ips_jips(ObjectiveEvaluator f_fcn, DoubleMatrix1D x0,
			DoubleMatrix2D A, DoubleMatrix1D l, DoubleMatrix1D u, DoubleMatrix1D xmin, DoubleMatrix1D xmax,
			ConstraintEvaluator gh_fcn, HessianEvaluator hess_fcn) {
		return ips_jips(f_fcn, x0, A, l, u, xmin, xmax, gh_fcn, null, new HashMap<String, Double>());
	}

	@SuppressWarnings("static-access")
	public static Object[] ips_jips(ObjectiveEvaluator f_fcn, DoubleMatrix1D x0,
			DoubleMatrix2D A, DoubleMatrix1D l, DoubleMatrix1D u, DoubleMatrix1D xmin, DoubleMatrix1D xmax,
			ConstraintEvaluator gh_fcn, HessianEvaluator hess_fcn, Map<String, Double> opt) {

		int nx = (int) x0.size();		// number of optimization variables
		boolean nonlinear;

		/* set default argument values if missing */
		if (A.size() > 0 &&
				(l.size() == 0 || util.all(l.copy().assign(dfunc.equals(Double.NEGATIVE_INFINITY)))) &&
				(u.size() == 0 || util.all(u.copy().assign(dfunc.equals(Double.POSITIVE_INFINITY)))) ) {
			A = DoubleFactory2D.sparse.make(0, nx);	// no limits => no linear constraints
		}
		int nA = A.rows();							// number of original linear constraints
		if (u.size() == 0)							// By default, linear inequalities are ...
			u = DoubleFactory1D.dense.make(nA, Double.POSITIVE_INFINITY);		// ... unbounded above and ...
		if (l.size() == 0)
			l = DoubleFactory1D.dense.make(nA, Double.NEGATIVE_INFINITY);		// ... unbounded below.
		if (xmin.size() == 0)						// By default, optimization variables are ...
			xmin = DoubleFactory1D.dense.make(nA, Double.POSITIVE_INFINITY);	// ... unbounded below and ...
		if (xmax.size() == 0)
			xmax = DoubleFactory1D.dense.make(nA, Double.NEGATIVE_INFINITY);	// ... unbounded above.
		if (gh_fcn == null) {
			nonlinear = false;						// no non-linear constraints present
		} else {
			nonlinear = true;						// we have some non-linear constraints
		}

		/* default options */
		if (!opt.containsKey("feastol"))
			opt.put("feastol", 1e-6);
		if (!opt.containsKey("gradtol"))
			opt.put("gradtol", 1e-6);
		if (!opt.containsKey("comptol"))
			opt.put("comptol", 1e-6);
		if (!opt.containsKey("costtol"))
			opt.put("costtol", 1e-6);
		if (!opt.containsKey("max_it"))
			opt.put("max_it", 150.0);
		if (!opt.containsKey("max_red"))
			opt.put("max_red", 20.0);
		if (!opt.containsKey("step_control"))
			opt.put("step_control", 0.0);
		if (!opt.containsKey("cost_mult"))
			opt.put("verbose", 1.0);
		if (!opt.containsKey("verbose"))
			opt.put("verbose", 0.0);

		/* initialize history */
		Map<Integer, Map<String, Double>> hist = new HashMap<Integer, Map<String,Double>>();

		/* -----  set up problem  ----- */
		/* constants */
		double xi = 0.99995;			// OPT_IPM_PHI
		double sigma = 0.1;				// OPT_IPM_SIGMA
		double z0 = 1;					// OPT_IPM_INIT_SLACK
		double alpha_min = 1e-8;		// OPT_AP_AD_MIN
		double rho_min = 0.95;			// OPT_IPM_QUAD_LOWTHRESH
		double rho_max = 1.05;			// OPT_IPM_QUAD_HIGHTHRESH
		double mu_threshold = 1e-5;		// SCOPF_MULTIPLIERS_FILTER_THRESH

		/* initialize */
		int i = 0;						// iteration counter
		boolean converged = false;		// flag
		int eflag = 0;					// exit flag

		/* add var limits to linear constraints */
		DoubleMatrix2D eyex = DoubleFactory2D.sparse.diagonal(DoubleFactory1D.dense.make(nx, 1));
		DoubleMatrix2D AA = DoubleFactory2D.sparse.appendRows(eyex, A);
		DoubleMatrix1D ll = DoubleFactory1D.dense.append(xmin, l);
		DoubleMatrix1D uu = DoubleFactory1D.dense.append(xmax, u);

		/* split up linear constraints */
		// equality
		DoubleMatrix1D diff = uu.copy().assign(ll, dfunc.chain(dfunc.abs, dfunc.minus));
		int[] ieq = util.nonzero( diff.copy().assign(dfunc.chain(dfunc.equals(0), dfunc.greater(util.EPS))) );
		// greater than, unbounded above
		IntMatrix1D igt_u = util.intm( uu.copy().assign(dfunc.chain(dfunc.equals(0), dfunc.less(1e10))) );
		IntMatrix1D igt_l = util.intm( ll.copy().assign(dfunc.greater(-1e10)) );
		int[] igt = util.nonzero( igt_u.assign(igt_l, ifunc.and) );
		// less than, unbounded below
		IntMatrix1D ilt_l = util.intm( ll.copy().assign(dfunc.chain(dfunc.equals(0), dfunc.greater(-1e10))) );
		IntMatrix1D ilt_u = util.intm( uu.copy().assign(dfunc.less(1e10)) );
		int[] ilt = util.nonzero( ilt_l.assign(ilt_u, ifunc.and) );
		// box constraints
		int[] ibx = util.nonzero( util.intm(diff.assign(dfunc.greater(util.EPS))).assign(ilt_u, ifunc.and).assign(igt_l, ifunc.and) );

		DoubleMatrix2D Ae = AA.viewSelection(ieq, null).copy();
		DoubleMatrix1D be = uu.viewSelection(ieq).copy();
		DoubleMatrix2D[][] Ai_p = new DoubleMatrix2D[][] {
				{AA.viewSelection(ilt, null)},
				{AA.viewSelection(igt, null).copy().assign(dfunc.neg)},
				{AA.viewSelection(ibx, null)},
				{AA.viewSelection(ibx, null).copy().assign(dfunc.neg)} };
		DoubleMatrix2D Ai = DoubleFactory2D.sparse.compose(Ai_p);

		DoubleMatrix1D[] bi_p = new DoubleMatrix1D[] {
				uu.viewSelection(ilt),
				ll.viewSelection(igt).copy().assign(dfunc.neg),
				uu.viewSelection(ibx),
				ll.viewSelection(ibx).copy().assign(dfunc.neg) };
		DoubleMatrix1D bi = DoubleFactory1D.dense.make(bi_p);

		/* evaluate cost f(x0) and constraints g(x0), h(x0) */
		DoubleMatrix1D x = x0.copy();
		double f = f_fcn.f(x);
		DoubleMatrix1D df = f_fcn.df(x);
		f *= opt.get("cost_mult");
		df.assign(dfunc.mult(opt.get("cost_mult")));

		DoubleMatrix1D gn = null, hn = null, h, g;
		DoubleMatrix2D dh, dg;

		if (nonlinear) {
			// non-linear constraints
			DoubleMatrix1D[] gh = gh_fcn.gh(x);
			DoubleMatrix2D[] dgh = gh_fcn.dgh(x);
			hn = gh[0]; gn = gh[1];
			DoubleMatrix2D dhn = dgh[0], dgn = dgh[1];
			// inequality constraints
			h = DoubleFactory1D.dense.append(hn, Ai.zMult(x, null).assign(bi, dfunc.minus));
			// equality constraints
			g = DoubleFactory1D.dense.append(gn, Ae.zMult(x, null).assign(be, dfunc.minus));
			// 1st derivative of inequalities
			dh = DoubleFactory2D.sparse.appendColumns(dhn, Ai.viewDice());
			// 1st derivative of equalities
			dg = DoubleFactory2D.sparse.appendColumns(dgn, Ae.viewDice());
		} else {
			h = Ai.zMult(x, null).assign(bi, dfunc.minus);	// inequality constraints
			g = Ae.zMult(x, null).assign(be, dfunc.minus);	// equality constraints
			dh = Ai.viewDice().copy();						// 1st derivative of inequalities
			dg = Ae.viewDice().copy();						// 1st derivative of equalities
		}

		/* grab some dimensions */
		int neq = (int) g.size();			// number of equality constraints
		int niq = (int) h.size();			// number of inequality constraints
		int neqnln = gn == null ? 0 : (int) gn.size();		// number of non-linear equality constraints
		int niqnln = (hn == null) ? 0 : (int) hn.size(); // number of non-linear inequality constraints
		int nlt = ilt.length; // number of upper bounded linear inequalities
		int ngt = igt.length; // number of lower bounded linear inequalities
		int nbx = ibx.length; // number of doubly bounded linear inequalities

		/* initialize gamma, lam, mu, z, e */
		double gamma = 1.0;  // barrier coefficient
		DoubleMatrix1D lam = DoubleFactory1D.dense.make(neq);
		DoubleMatrix1D z = DoubleFactory1D.dense.make(niq, z0);
		DoubleMatrix1D mu = z.copy();
		int[] k = util.nonzero(h.copy().assign(dfunc.less(-z0)));
		z.viewSelection(k).assign(h.viewSelection(k).copy().assign(dfunc.neg));
		k = util.nonzero( z.copy().assign(dfunc.chain(dfunc.inv, dfunc.div(gamma))).assign(dfunc.greater(z0)) );
		// (seems k is always empty if gamma = z0 = 1)
		if (k.length > 0)
			mu.viewSelection(k).assign( z.viewSelection(k).copy().assign(dfunc.chain(dfunc.inv, dfunc.div(gamma))) );
		DoubleMatrix1D e = DoubleFactory1D.dense.make(niq, 1.0);

		/* check tolerance */
		double f0 = f;
		double L = 0.0;
		if (opt.get("step_control") > 0)
			L = f + lam.zDotProduct(g) + mu.zDotProduct(h.copy().assign(z, dfunc.plus)) - gamma * z.aggregate(dfunc.plus, dfunc.log);
		DoubleMatrix1D Lx = df.copy().assign(dg.zMult(lam, null), dfunc.plus).assign(dh.zMult(mu, null), dfunc.plus);

		double normG = DenseDoubleAlgebra.DEFAULT.norm(g, Norm.Infinity);
		double maxH = h.aggregate(dfunc.plus, dfunc.identity);
		double normX = DenseDoubleAlgebra.DEFAULT.norm(x, Norm.Infinity);
		double normZ = DenseDoubleAlgebra.DEFAULT.norm(z, Norm.Infinity);
		double feascond = dfunc.max.apply(normG, maxH) / (1 + dfunc.max.apply(normX, normZ));

		double normLx = DenseDoubleAlgebra.DEFAULT.norm(Lx, Norm.Infinity);
		double normLam = DenseDoubleAlgebra.DEFAULT.norm(lam, Norm.Infinity);
		double normMu = DenseDoubleAlgebra.DEFAULT.norm(mu, Norm.Infinity);
		double gradcond = normLx / (1 + dfunc.max.apply(normLam, normMu));

		double compcond = z.zDotProduct(mu) / (1 + normX);

		double costcond = Math.abs(f - f0) / (1 + Math.abs(f0));

		/* save history */
		Map<String, Double> history = new HashMap<String, Double>();
		history.put("feascond", feascond);
		history.put("gradcond", gradcond);
		history.put("compcond", compcond);
		history.put("costcond", costcond);
		history.put("gamma", gamma);
		history.put("stepsize", 0.0);
		history.put("obj", f / opt.get("cost_mult"));
		history.put("alphap", 0.0);
		history.put("alphad", 0.0);
		hist.put(i, history);

		if (opt.get("verbose") > 0) {
			String s = opt.get("step_control") > 0 ? "-sc" : "";
			Map<String, String> v = Dips_jipsver.ips_jipsver("all");
			System.out.printf("Java Interior Point Solver -- JIPS%s, Version %s, %s",
				s, v.get("Version"), v.get("Date"));
			if (opt.get("verbose") > 1) {
				System.out.printf("\n it    objective   step size   feascond     gradcond     compcond     costcond  ");
				System.out.printf("\n----  ------------ --------- ------------ ------------ ------------ ------------");
				System.out.printf("\n%3d  %12.8g %10s %12g %12g %12g %12g",
					i, f / opt.get("cost_mult"), "", feascond, gradcond, compcond, costcond);
			}
		}
		if (feascond < opt.get("feastol") && gradcond < opt.get("gradtol") &&
				compcond < opt.get("comptol") && costcond < opt.get("costtol")) {
			converged = true;
			if (opt.get("verbose") > 0)
				System.out.printf("\nConverged!\n");
		}

		/* -----  do Newton iterations  ----- */
		while (!converged && i < opt.get("max_it")) {
			i += 1;

			/* compute update step */
			Map<String, DoubleMatrix1D> lambda = new HashMap<String, DoubleMatrix1D>();
			lambda.put("eqnonlin", lam.viewSelection(IntFactory1D.dense.ascending(neqnln).toArray()));
			lambda.put("ineqnonlin", lam.viewSelection(IntFactory1D.dense.ascending(niqnln).toArray()));

			DoubleMatrix2D Lxx;
			if (nonlinear) {
				if (hess_fcn == null)
					System.out.printf("jips: Hessian evaluation via finite differences not yet implemented.\n       Please provide your own hessian evaluation function.");
				Lxx = hess_fcn.h(x, lambda);
			} else {
				DoubleMatrix2D d2f = f_fcn.d2f(x);		// cost
				Lxx = d2f.assign(dfunc.mult(opt.get("cost_mult")));
			}

			DoubleMatrix2D zinvdiag = DoubleFactory2D.sparse.diagonal(z.copy().assign(dfunc.inv));
			DoubleMatrix2D mudiag = DoubleFactory2D.sparse.diagonal(mu);
			DoubleMatrix2D dh_zinv = dh.zMult(zinvdiag, null);
			DoubleMatrix2D M = Lxx.copy().assign(dh_zinv.zMult(mudiag, null).zMult(dh.viewDice(), null), dfunc.plus);
			DoubleMatrix1D egamma = e.copy().assign(dfunc.mult(gamma));
			DoubleMatrix1D rhs = mudiag.zMult(h, null).assign(egamma, dfunc.plus);
			DoubleMatrix1D N = Lx.copy().assign(dh_zinv.zMult(rhs, null), dfunc.plus);

			DoubleMatrix2D[][] AAA_p = new DoubleMatrix2D[][] {
					{M, dg}, {dg.viewDice().copy(), DoubleFactory2D.sparse.make(neq, neq)} };
			DoubleMatrix2D AAA = DoubleFactory2D.sparse.compose(AAA_p);
			DoubleMatrix1D bbb = DoubleFactory1D.dense.append(N, g).assign(dfunc.mult(-1.0));
			DoubleMatrix1D dxdlam = SparseDoubleAlgebra.DEFAULT.solve(AAA, bbb);

			DoubleMatrix1D dx = dxdlam.viewPart(0, nx);
			DoubleMatrix1D dlam = dxdlam.viewPart(nx, neq);
			DoubleMatrix1D dz = h.copy().assign(dfunc.neg);
			dz.assign(z, dfunc.minus).assign(dh.zMult(dx, null), dfunc.minus);
			DoubleMatrix1D dmu = mu.copy().assign(dfunc.neg);
			egamma.assign(mudiag.zMult(dz, null), dfunc.minus);
			dmu.assign(zinvdiag.zMult(egamma, null), dfunc.plus);

			/* optional step-size control */
			boolean sc = false;
			DoubleMatrix1D gn1 = null, hn1 = null, h1, g1;
			DoubleMatrix1D[] gh1;
			if (opt.get("step_control") > 0) {
				DoubleMatrix1D x1 = x.copy().assign(dx, dfunc.plus);

				/* evaluate cost, constraints, derivatives at x1 */
				double f1 = f_fcn.f(x1);
				DoubleMatrix1D df1 = f_fcn.df(x1);
				f1 *= opt.get("cost_mult");
				df1.assign(dfunc.mult(opt.get("cost_mult")));

				DoubleMatrix2D dh1, dg1;
				if (nonlinear) {
					// non-linear constraints
					gh1 = gh_fcn.gh(x1);
					DoubleMatrix2D[] dgh1 = gh_fcn.dgh(x1);
					hn1 = gh1[0]; gn1 = gh1[1];
					DoubleMatrix2D dhn1 = dgh1[0], dgn1 = dgh1[1];
					// inequality constraints
					h1 = DoubleFactory1D.dense.append(hn1, Ai.zMult(x1, null).assign(bi, dfunc.minus));
					// equality constraints
					g1 = DoubleFactory1D.dense.append(gn1, Ae.zMult(x1, null).assign(be, dfunc.minus));
					// 1st derivative of inequalities
					dh1 = DoubleFactory2D.sparse.appendColumns(dhn1, Ai.viewDice());
					// 1st derivative of equalities
					dg1 = DoubleFactory2D.sparse.appendColumns(dgn1, Ae.viewDice());
				} else {
					h1 = Ai.zMult(x1, null).assign(bi, dfunc.minus);	// inequality constraints
					g1 = Ae.zMult(x1, null).assign(be, dfunc.minus);	// equality constraints
					dh1 = Ai.viewDice().copy();						// 1st derivative of inequalities
					dg1 = Ae.viewDice().copy();						// 1st derivative of equalities
				}

				/* check tolerance */
				DoubleMatrix1D Lx1 = df1.assign(dg1.zMult(lam, null), dfunc.plus).assign(dh1.zMult(mu, null), dfunc.plus);

				normG = DenseDoubleAlgebra.DEFAULT.norm(g1, Norm.Infinity);
				maxH = h1.aggregate(dfunc.plus, dfunc.identity);
				normX = DenseDoubleAlgebra.DEFAULT.norm(x1, Norm.Infinity);
				normZ = DenseDoubleAlgebra.DEFAULT.norm(z, Norm.Infinity);
				double feascond1 = dfunc.max.apply(normG, maxH) / (1 + dfunc.max.apply(normX, normZ));

				normLx = DenseDoubleAlgebra.DEFAULT.norm(Lx1, Norm.Infinity);
				normLam = DenseDoubleAlgebra.DEFAULT.norm(lam, Norm.Infinity);
				normMu = DenseDoubleAlgebra.DEFAULT.norm(mu, Norm.Infinity);
				double gradcond1 = normLx / (1 + dfunc.max.apply(normLam, normMu));

				if (feascond1 > feascond && gradcond1 > gradcond)
					sc = true;
			}
			if (sc) {
				double alpha = 1.0;
				for (int j = 0; j < opt.get("max_red"); j++) {
					DoubleMatrix1D dx1 = dx.copy().assign(dfunc.mult(alpha));
					DoubleMatrix1D x1 = x.copy().assign(dx1, dfunc.plus);
					double f1 = f_fcn.f(x1);			// cost
					f1 *= opt.get("cost_mult");
					if (nonlinear) {
						gh1 = gh_fcn.gh(x1);			// non-linear constraints
						gn1 = gh1[0];
						hn1 = gh1[1];
						// inequality constraints
						h1 = DoubleFactory1D.dense.append(hn1, Ai.zMult(x1, null).assign(bi, dfunc.minus));
						// equality constraints
						g1 = DoubleFactory1D.dense.append(gn1, Ae.zMult(x1, null).assign(be, dfunc.minus));
					} else {
						h1 = Ai.zMult(x1, null).assign(bi, dfunc.minus);	// inequality constraints
						g1 = Ae.zMult(x1, null).assign(be, dfunc.minus);	// equality constraints
					}

					DoubleMatrix1D hz = h1.copy().assign(z, dfunc.plus);
					double L1 = f + lam.zDotProduct(g1) + mu.zDotProduct(hz) - gamma * z.aggregate(dfunc.plus, dfunc.log);
					if (opt.get("verbose") > 2)
						System.out.printf("\n   %3d            %10g", -j, DenseDoubleAlgebra.DEFAULT.norm(dx1, Norm.Infinity));
					double rho = (L1 - L) / (Lx.zDotProduct(dx1) + 0.5 * dx1.zDotProduct(Lxx.zMult(dx1, null)));
					if (rho > rho_min && rho < rho_max) {
						break;
					} else {
						alpha = alpha / 2.0;
					}
				}
				dx.assign(dfunc.mult(alpha));
				dz.assign(dfunc.mult(alpha));
				dlam.assign(dfunc.mult(alpha));
				dmu.assign(dfunc.mult(alpha));
			}

		}

		return null;
	}

}
