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

import cern.colt.matrix.tdouble.DoubleFactory1D;
import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
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
public class Djs_jips {

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
			gn = gh[0]; hn = gh[1];
			DoubleMatrix2D dgn = dgh[0], dhn = dgh[1];
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

		return null;
	}

}
