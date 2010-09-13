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
			DoubleMatrix1D gn, hn;
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

		return null;
	}

}
