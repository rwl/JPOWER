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

package edu.cornell.pserc.jips;

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

import static cern.colt.util.tdouble.Util.ifunc;
import static cern.colt.util.tdouble.Util.dfunc;
import static cern.colt.util.tdouble.Util.all;
import static cern.colt.util.tdouble.Util.intm;
import static cern.colt.util.tdouble.Util.nonzero;
import static cern.colt.util.tdouble.Util.EPS;
import static cern.colt.util.tdouble.Util.irange;
import static cern.colt.util.tdouble.Util.any;

/**
 * Java Interior Point Solver.
 *
 * @author Ray Zimmerman
 * @author Richard Lincoln
 *
 */
public class Dips_jips {

	@SuppressWarnings("static-access")
	public static Object[] jips(ObjectiveEvaluator f_fcn, DoubleMatrix1D x0,
			DoubleMatrix2D A, DoubleMatrix1D l, DoubleMatrix1D u, DoubleMatrix1D xmin, DoubleMatrix1D xmax,
			ConstraintEvaluator gh_fcn, HessianEvaluator hess_fcn, Map<String, Double> opt) {

		int i, j, nx, nA, eflag, neq, niq, neqnln, niqnln, nlt, ngt, nbx;
		int[] ieq, igt, ilt, ibx, k, kl, ku;
		boolean nonlinear, converged, sc;
		double xi, sigma, z0, alpha_min, rho_min, rho_max, mu_threshold,
				f, gamma, f0, L, normG, maxH, normX, normZ, feascond, normLx, normLam, normMu,
				gradcond, compcond, costcond, f1, feascond1, gradcond1, alpha, L1, rho, alphap, alphad, norm_dx;
		String s;

		Map<Integer, Map<String, Double>> hist;
		Map<String, Double> history;
		Map<String, String> v;
		Map<String, DoubleMatrix1D> lambda;
		Map<String, Object> output;

		IntMatrix1D igt_u, igt_l, ilt_l, ilt_u, non;

		DoubleMatrix1D ll, uu, diff, be, bi, x, df, gn = null, hn = null, h, g, lam, z, mu, e, Lx,
				egamma, N, dx, dlam, dz, dmu, bbb, dxdlam, gn1, hn1, h1, g1, x1, df1,
				Lx1, dx1, hz, zk, muk, lam_lin, mu_lin, mu_l, mu_u;

		DoubleMatrix1D[] bi_p, gh, gh1;

		DoubleMatrix2D eyex, AA, Ae, Ai, dh, dg, dhn, dgn, Lxx, zinvdiag, mudiag,
				dh_zinv, M, AAA, dh1, dg1, dhn1, dgn1;

		DoubleMatrix2D[] dgh, dgh1;

		DoubleMatrix2D[][] Ai_p, AAA_p;


		nx = (int) x0.size();		// number of optimization variables

		/* set default argument values if missing */
		if (A.size() > 0 &&
				(l.size() == 0 || all(l.copy().assign(dfunc.equals(Double.NEGATIVE_INFINITY)))) &&
				(u.size() == 0 || all(u.copy().assign(dfunc.equals(Double.POSITIVE_INFINITY)))) ) {
			A = DoubleFactory2D.sparse.make(0, nx);	// no limits => no linear constraints
		}
		nA = A.rows();							// number of original linear constraints
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
		hist = new HashMap<Integer, Map<String,Double>>();

		/* -----  set up problem  ----- */
		/* constants */
		xi = 0.99995;			// OPT_IPM_PHI
		sigma = 0.1;				// OPT_IPM_SIGMA
		z0 = 1;					// OPT_IPM_INIT_SLACK
		alpha_min = 1e-8;		// OPT_AP_AD_MIN
		rho_min = 0.95;			// OPT_IPM_QUAD_LOWTHRESH
		rho_max = 1.05;			// OPT_IPM_QUAD_HIGHTHRESH
		mu_threshold = 1e-5;		// SCOPF_MULTIPLIERS_FILTER_THRESH

		/* initialize */
		i = 0;						// iteration counter
		converged = false;		// flag
		eflag = 0;					// exit flag

		/* add var limits to linear constraints */
		eyex = DoubleFactory2D.sparse.diagonal(DoubleFactory1D.dense.make(nx, 1));
		AA = DoubleFactory2D.sparse.appendRows(eyex, A);
		ll = DoubleFactory1D.dense.append(xmin, l);
		uu = DoubleFactory1D.dense.append(xmax, u);

		/* split up linear constraints */
		// equality
		diff = uu.copy().assign(ll, dfunc.chain(dfunc.abs, dfunc.minus));
		ieq = nonzero( diff.copy().assign(dfunc.chain(dfunc.equals(0), dfunc.greater(EPS))) );
		// greater than, unbounded above
		igt_u = intm( uu.copy().assign(dfunc.chain(dfunc.equals(0), dfunc.less(1e10))) );
		igt_l = intm( ll.copy().assign(dfunc.greater(-1e10)) );
		igt = nonzero( igt_u.assign(igt_l, ifunc.and) );
		// less than, unbounded below
		ilt_l = intm( ll.copy().assign(dfunc.chain(dfunc.equals(0), dfunc.greater(-1e10))) );
		ilt_u = intm( uu.copy().assign(dfunc.less(1e10)) );
		ilt = nonzero( ilt_l.assign(ilt_u, ifunc.and) );
		// box constraints
		ibx = nonzero( intm(diff.assign(dfunc.greater(EPS))).assign(ilt_u, ifunc.and).assign(igt_l, ifunc.and) );

		Ae = AA.viewSelection(ieq, null).copy();
		be = uu.viewSelection(ieq).copy();
		Ai_p = new DoubleMatrix2D[][] {
				{AA.viewSelection(ilt, null)},
				{AA.viewSelection(igt, null).copy().assign(dfunc.neg)},
				{AA.viewSelection(ibx, null)},
				{AA.viewSelection(ibx, null).copy().assign(dfunc.neg)} };
		Ai = DoubleFactory2D.sparse.compose(Ai_p);

		bi_p = new DoubleMatrix1D[] {
				uu.viewSelection(ilt),
				ll.viewSelection(igt).copy().assign(dfunc.neg),
				uu.viewSelection(ibx),
				ll.viewSelection(ibx).copy().assign(dfunc.neg) };
		bi = DoubleFactory1D.dense.make(bi_p);

		/* evaluate cost f(x0) and constraints g(x0), h(x0) */
		x = x0.copy();
		f = f_fcn.f(x);
		df = f_fcn.df(x);
		f *= opt.get("cost_mult");
		df.assign(dfunc.mult(opt.get("cost_mult")));

		if (nonlinear) {
			// non-linear constraints
			gh = gh_fcn.gh(x);
			dgh = gh_fcn.dgh(x);
			hn = gh[0]; gn = gh[1];
			dhn = dgh[0]; dgn = dgh[1];
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
		neq = (int) g.size();			// number of equality constraints
		niq = (int) h.size();			// number of inequality constraints
		neqnln = gn == null ? 0 : (int) gn.size();    // number of non-linear equality constraints
		niqnln = (hn == null) ? 0 : (int) hn.size();  // number of non-linear inequality constraints
		nlt = ilt.length; // number of upper bounded linear inequalities
		ngt = igt.length; // number of lower bounded linear inequalities
		nbx = ibx.length; // number of doubly bounded linear inequalities

		/* initialize gamma, lam, mu, z, e */
		gamma = 1.0;  // barrier coefficient
		lam = DoubleFactory1D.dense.make(neq);
		z = DoubleFactory1D.dense.make(niq, z0);
		mu = z.copy();
		k = nonzero(h.copy().assign(dfunc.less(-z0)));
		z.viewSelection(k).assign(h.viewSelection(k).copy().assign(dfunc.neg));
		k = nonzero( z.copy().assign(dfunc.chain(dfunc.inv, dfunc.div(gamma))).assign(dfunc.greater(z0)) );
		// (seems k is always empty if gamma = z0 = 1)
		if (k.length > 0)
			mu.viewSelection(k).assign( z.viewSelection(k).copy().assign(dfunc.chain(dfunc.inv, dfunc.div(gamma))) );
		e = DoubleFactory1D.dense.make(niq, 1.0);

		/* check tolerance */
		f0 = f;
		L = 0.0;
		if (opt.get("step_control") > 0)
			L = f + lam.zDotProduct(g) + mu.zDotProduct(h.copy().assign(z, dfunc.plus)) - gamma * z.aggregate(dfunc.plus, dfunc.log);
		Lx = df.copy().assign(dg.zMult(lam, null), dfunc.plus).assign(dh.zMult(mu, null), dfunc.plus);

		normG = DenseDoubleAlgebra.DEFAULT.norm(g, Norm.Infinity);
		maxH = h.aggregate(dfunc.plus, dfunc.identity);
		normX = DenseDoubleAlgebra.DEFAULT.norm(x, Norm.Infinity);
		normZ = DenseDoubleAlgebra.DEFAULT.norm(z, Norm.Infinity);
		feascond = dfunc.max.apply(normG, maxH) / (1 + dfunc.max.apply(normX, normZ));

		normLx = DenseDoubleAlgebra.DEFAULT.norm(Lx, Norm.Infinity);
		normLam = DenseDoubleAlgebra.DEFAULT.norm(lam, Norm.Infinity);
		normMu = DenseDoubleAlgebra.DEFAULT.norm(mu, Norm.Infinity);
		gradcond = normLx / (1 + dfunc.max.apply(normLam, normMu));

		compcond = z.zDotProduct(mu) / (1 + normX);

		costcond = Math.abs(f - f0) / (1 + Math.abs(f0));

		/* save history */
		history = new HashMap<String, Double>();
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
			s = opt.get("step_control") > 0 ? "-sc" : "";
			v = Dips_jipsver.jipsver("all");
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
			lambda = new HashMap<String, DoubleMatrix1D>();
			lambda.put("eqnonlin", lam.viewSelection(IntFactory1D.dense.ascending(neqnln).toArray()));
			lambda.put("ineqnonlin", lam.viewSelection(IntFactory1D.dense.ascending(niqnln).toArray()));

			if (nonlinear) {
				if (hess_fcn == null)
					System.out.printf("jips: Hessian evaluation via finite differences not yet implemented.\n       Please provide your own hessian evaluation function.");
				Lxx = hess_fcn.h(x, lambda);
			} else {
				DoubleMatrix2D d2f = f_fcn.d2f(x);		// cost
				Lxx = d2f.assign(dfunc.mult(opt.get("cost_mult")));
			}

			zinvdiag = DoubleFactory2D.sparse.diagonal(z.copy().assign(dfunc.inv));
			mudiag = DoubleFactory2D.sparse.diagonal(mu);
			dh_zinv = dh.zMult(zinvdiag, null);
			M = Lxx.copy().assign(dh_zinv.zMult(mudiag, null).zMult(dh.viewDice(), null), dfunc.plus);
			egamma = e.copy().assign(dfunc.mult(gamma));
			DoubleMatrix1D rhs = mudiag.zMult(h, null).assign(egamma, dfunc.plus);
			N = Lx.copy().assign(dh_zinv.zMult(rhs, null), dfunc.plus);

			AAA_p = new DoubleMatrix2D[][] {
					{M, dg}, {dg.viewDice().copy(), DoubleFactory2D.sparse.make(neq, neq)} };
			AAA = DoubleFactory2D.sparse.compose(AAA_p);
			bbb = DoubleFactory1D.dense.append(N, g).assign(dfunc.mult(-1.0));
			dxdlam = SparseDoubleAlgebra.DEFAULT.solve(AAA, bbb);

			dx = dxdlam.viewPart(0, nx);
			dlam = dxdlam.viewPart(nx, neq);
			dz = h.copy().assign(dfunc.neg);
			dz.assign(z, dfunc.minus).assign(dh.zMult(dx, null), dfunc.minus);
			dmu = mu.copy().assign(dfunc.neg);
			egamma.assign(mudiag.zMult(dz, null), dfunc.minus);
			dmu.assign(zinvdiag.zMult(egamma, null), dfunc.plus);

			/* optional step-size control */
			sc = false;
			if (opt.get("step_control") > 0) {
				x1 = x.copy().assign(dx, dfunc.plus);

				/* evaluate cost, constraints, derivatives at x1 */
				f1 = f_fcn.f(x1);
				df1 = f_fcn.df(x1);
				f1 = f1 * opt.get("cost_mult");
				df1.assign(dfunc.mult(opt.get("cost_mult")));

				if (nonlinear) {
					// non-linear constraints
					gh1 = gh_fcn.gh(x1);
					dgh1 = gh_fcn.dgh(x1);
					hn1 = gh1[0]; gn1 = gh1[1];
					dhn1 = dgh1[0]; dgn1 = dgh1[1];
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
				Lx1 = df1.assign(dg1.zMult(lam, null), dfunc.plus).assign(dh1.zMult(mu, null), dfunc.plus);

				normG = DenseDoubleAlgebra.DEFAULT.norm(g1, Norm.Infinity);
				maxH = h1.aggregate(dfunc.plus, dfunc.identity);
				normX = DenseDoubleAlgebra.DEFAULT.norm(x1, Norm.Infinity);
				normZ = DenseDoubleAlgebra.DEFAULT.norm(z, Norm.Infinity);
				feascond1 = dfunc.max.apply(normG, maxH) / (1 + dfunc.max.apply(normX, normZ));

				normLx = DenseDoubleAlgebra.DEFAULT.norm(Lx1, Norm.Infinity);
				normLam = DenseDoubleAlgebra.DEFAULT.norm(lam, Norm.Infinity);
				normMu = DenseDoubleAlgebra.DEFAULT.norm(mu, Norm.Infinity);
				gradcond1 = normLx / (1 + dfunc.max.apply(normLam, normMu));

				if (feascond1 > feascond && gradcond1 > gradcond)
					sc = true;
			}
			if (sc) {
				alpha = 1.0;
				for (j = 0; j < opt.get("max_red"); j++) {
					dx1 = dx.copy().assign(dfunc.mult(alpha));
					x1 = x.copy().assign(dx1, dfunc.plus);
					f1 = f_fcn.f(x1);			// cost
					f1 = f1 * opt.get("cost_mult");
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

					hz = h1.copy().assign(z, dfunc.plus);
					L1 = f + lam.zDotProduct(g1) + mu.zDotProduct(hz) - gamma * z.aggregate(dfunc.plus, dfunc.log);
					if (opt.get("verbose") > 2)
						System.out.printf("\n   %3d            %10g", -j, DenseDoubleAlgebra.DEFAULT.norm(dx1, Norm.Infinity));
					rho = (L1 - L) / (Lx.zDotProduct(dx1) + 0.5 * dx1.zDotProduct(Lxx.zMult(dx1, null)));
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

			/* do the update */
			k = nonzero(dz.copy().assign(dfunc.less(0)));
			zk = z.viewSelection(k).copy();
			zk.assign(dz.viewSelection(k).copy().assign(dfunc.neg), dfunc.div);
			alphap = dfunc.min.apply(xi * zk.aggregate(dfunc.min, dfunc.identity), 1);

			k = nonzero(dmu.copy().assign(dfunc.less(0)));
			muk = mu.viewSelection(k).copy();
			muk.assign(dmu.viewSelection(k).copy().assign(dfunc.neg), dfunc.div);
			alphad = dfunc.min.apply(xi * muk.aggregate(dfunc.min, dfunc.identity), 1);

			x.assign(dx.assign(dfunc.mult(alphap)), dfunc.plus);
			z.assign(dz.assign(dfunc.mult(alphap)), dfunc.plus);
			lam.assign(dlam.assign(dfunc.mult(alphad)), dfunc.plus);
			mu.assign(dmu.assign(dfunc.mult(alphad)), dfunc.plus);
			if (niq > 0)
				gamma = sigma * z.zDotProduct(mu) / niq;

			/* evaluate cost, constraints, derivatives */
			f = f_fcn.f(x);					// cost
			df = f_fcn.df(x);
			f = f * opt.get("cost_mult");
			df.assign(dfunc.mult(opt.get("cost_mult")));

			if (nonlinear) {
				// non-linear constraints
				gh = gh_fcn.gh(x);
				dgh = gh_fcn.dgh(x);
				hn = gh[0]; gn = gh[1];
				dhn = dgh[0]; dgn = dgh[1];
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
				// 1st derivatives are constant, still dh = Ai', dg = Ae'
			}

			/* check tolerance */
			Lx = df.assign(dg.zMult(lam, null), dfunc.plus).assign(dh.zMult(mu, null), dfunc.plus);

			normG = DenseDoubleAlgebra.DEFAULT.norm(g, Norm.Infinity);
			maxH = h.aggregate(dfunc.plus, dfunc.identity);
			normX = DenseDoubleAlgebra.DEFAULT.norm(x, Norm.Infinity);
			normZ = DenseDoubleAlgebra.DEFAULT.norm(z, Norm.Infinity);
			feascond = dfunc.max.apply(normG, maxH) / (1 + dfunc.max.apply(normX, normZ));

			normLx = DenseDoubleAlgebra.DEFAULT.norm(Lx, Norm.Infinity);
			normLam = DenseDoubleAlgebra.DEFAULT.norm(lam, Norm.Infinity);
			normMu = DenseDoubleAlgebra.DEFAULT.norm(mu, Norm.Infinity);
			gradcond = normLx / (1 + dfunc.max.apply(normLam, normMu));

			compcond = z.zDotProduct(mu) / (1 + normX);

			costcond = Math.abs(f - f0) / (1 + Math.abs(f0));

			/* save history */
			history = new HashMap<String, Double>();
			history.put("feascond", feascond);
			history.put("gradcond", gradcond);
			history.put("compcond", compcond);
			history.put("costcond", costcond);
			history.put("gamma", gamma);
			norm_dx = DenseDoubleAlgebra.DEFAULT.norm(dx, Norm.Infinity);
			history.put("stepsize", norm_dx);
			history.put("obj", f / opt.get("cost_mult"));
			history.put("alphap", alphap);
			history.put("alphad", alphad);
			hist.put(i, history);

			if (opt.get("verbose") > 1)
				System.out.printf("\n%3d  %12.8g %10.5g %12g %12g %12g %12g",
					i, f / opt.get("cost_mult"), norm_dx, feascond, gradcond, compcond, costcond);

			if (feascond < opt.get("feastol") && gradcond < opt.get("gradtol") &&
							compcond < opt.get("comptol") && costcond < opt.get("costtol")) {
				converged = true;
				if (opt.get("verbose") > 0)
						System.out.printf("\nConverged!\n");
			} else {
				if (any(x.copy().assign(dfunc.equals(Double.NaN))) || alphap < alpha_min || alphad < alpha_min ||
						gamma < EPS || gamma > 1 / EPS) {
					if (opt.get("verbose") > 0)
						System.out.printf("\nNumerically Failed\n");
					eflag = -1;
					break;
				}
				f0 = f;
				if (opt.get("step_control") > 0)
					L = f + lam.zDotProduct(g) + mu.zDotProduct(h.copy().assign(z, dfunc.plus)) - gamma * z.aggregate(dfunc.plus, dfunc.log);
			}
		}

		if (opt.get("verbose") > 0)
			if (!converged)
				System.out.printf("\nDid not converge in %d iterations.\n", i);

		/* -----  package up results  ----- */
		if (eflag != -1)
			eflag = converged ? 1 : 0;
		output = new HashMap<String, Object>();
		output.put("iterations", i);
		output.put("hist", hist);
		if (eflag == 0) {
			output.put("message", "Did not converge");
		} else if (eflag == 1) {
			output.put("message", "Converged");
		} else if (eflag == -1) {
			output.put("message", "Numerically failed");
		} else {
			output.put("message", "Please hang up and dial again");
		}

		/* zero out multipliers on non-binding constraints */
		non = intm( h.copy().assign(dfunc.less(-opt.get("feastol"))) ).assign(
				intm( mu.copy().assign(dfunc.less(mu_threshold)) ), ifunc.and);
		mu.viewSelection(non.toArray()).assign(0);

		/* un-scale cost and prices */
		f = f / opt.get("cost_mult");
		lam.assign(dfunc.div(opt.get("cost_mult")));
		mu.assign(dfunc.div(opt.get("cost_mult")));

		/* re-package multipliers into struct */
		lam_lin = lam.viewSelection(irange(neqnln, neq));	// lambda for linear constraints
		mu_lin = mu.viewSelection(irange(niqnln, niq));		// mu for linear constraints
		kl = nonzero(lam_lin.copy().assign(dfunc.less(0)));			// lower bound binding
		ku = nonzero(mu_lin.copy().assign(dfunc.less(0)));			// upper bound binding

		mu_l = DoubleFactory1D.dense.make(nx + nA);
		mu_l.viewSelection(ieq).viewSelection(kl).assign(lam_lin.copy().viewSelection(kl).assign(dfunc.neg));
		mu_l.viewSelection(igt).assign(mu_lin.viewPart(nlt, ngt));
		mu_l.viewSelection(ibx).assign(mu_lin.viewPart(nlt+ngt+nbx, ngt));

		mu_u = DoubleFactory1D.dense.make(nx + nA);
		mu_u.viewSelection(ieq).viewSelection(ku).assign(lam_lin.viewSelection(ku));
		mu_u.viewSelection(ilt).assign(mu_lin.viewPart(0, nlt));
		mu_u.viewSelection(ibx).assign(mu_lin.viewPart(nlt+ngt, ngt));

		lambda = new HashMap<String, DoubleMatrix1D>();
		lambda.put("mu_l", mu_l.viewPart(nx, (int) (mu_l.size() - nx)));
		lambda.put("mu_u", mu_u.viewPart(nx, (int) (mu_u.size() - nx)));
		lambda.put("lower", mu_l.viewPart(0, nx));
		lambda.put("upper", mu_u.viewPart(0, nx));

		if (niqnln > 0)
			lambda.put("ineqnonlin", mu.viewPart(0, niqnln));

		if (neqnln > 0)
			lambda.put("neqnln", lam.viewPart(0, neqnln));

		return new Object[] {x, f, eflag, output, lambda};
	}

	public static Object[] jips(ObjectiveEvaluator f_fcn, DoubleMatrix1D x0) {
		DoubleMatrix2D A;
		DoubleMatrix1D l, u;

		int nx = (int) x0.size();
		A = DoubleFactory2D.sparse.make(0, nx);
		l = DoubleFactory1D.dense.make(0);
		u = DoubleFactory1D.dense.make(0);

		return jips(f_fcn, x0, A, l, u);
	}

	public static Object[] jips(ObjectiveEvaluator f_fcn, DoubleMatrix1D x0,
			DoubleMatrix2D A, DoubleMatrix1D l, DoubleMatrix1D u) {
		DoubleMatrix1D xmin, xmax;

		int nx = (int) x0.size();
		xmin = DoubleFactory1D.dense.make(nx, Double.NEGATIVE_INFINITY);
		xmax = DoubleFactory1D.dense.make(nx, Double.POSITIVE_INFINITY);

		return jips(f_fcn, x0, A, l, u, xmin, xmax);
	}

	public static Object[] jips(ObjectiveEvaluator f_fcn, DoubleMatrix1D x0,
			DoubleMatrix2D A, DoubleMatrix1D l, DoubleMatrix1D u, DoubleMatrix1D xmin, DoubleMatrix1D xmax) {
		return jips(f_fcn, x0, A, l, u, xmin, xmax, null, null);
	}

	public static Object[] jips(ObjectiveEvaluator f_fcn, DoubleMatrix1D x0,
			DoubleMatrix2D A, DoubleMatrix1D l, DoubleMatrix1D u, DoubleMatrix1D xmin, DoubleMatrix1D xmax,
			ConstraintEvaluator gh_fcn) {
		return jips(f_fcn, x0, A, l, u, xmin, xmax, gh_fcn, null);
	}

	public static Object[] jips(ObjectiveEvaluator f_fcn, DoubleMatrix1D x0,
			DoubleMatrix2D A, DoubleMatrix1D l, DoubleMatrix1D u, DoubleMatrix1D xmin, DoubleMatrix1D xmax,
			ConstraintEvaluator gh_fcn, HessianEvaluator hess_fcn) {
		return jips(f_fcn, x0, A, l, u, xmin, xmax, gh_fcn, null, new HashMap<String, Double>());
	}

}
