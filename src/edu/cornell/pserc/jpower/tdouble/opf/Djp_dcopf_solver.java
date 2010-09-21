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

import java.util.HashMap;
import java.util.Map;

import cern.colt.matrix.AbstractMatrix;
import cern.colt.matrix.tdouble.DoubleFactory1D;
import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.SparseRCDoubleMatrix2D;
import cern.colt.matrix.tint.IntFactory1D;
import cern.jet.math.tdouble.DoubleFunctions;
import cern.jet.math.tint.IntFunctions;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_branch;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_bus;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_gen;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_gencost;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_jpc;
import edu.cornell.pserc.jpower.tdouble.opf.Djp_opf_model.Cost;
import edu.cornell.pserc.jpower.tdouble.opf.Djp_opf_model.Set;
import edu.cornell.pserc.util.tdouble.Djp_util;

/**
 * Solves a DC optimal power flow.
 *
 * @author Ray Zimmerman (rz10@cornell.edu)
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class Djp_dcopf_solver {

	private static final Djp_util util = new Djp_util();
	private static final DoubleFunctions dfunc = DoubleFunctions.functions;
	private static final IntFunctions ifunc = IntFunctions.intFunctions;

	private static final int POLYNOMIAL = Djp_jpc.POLYNOMIAL;
	private static final int PW_LINEAR = Djp_jpc.PW_LINEAR;
	private static final int REF = Djp_jpc.REF;

	public static Object[] jp_dcopf_solver(Djp_opf_model om, Map<String, Double> jpopt) {
		return jp_dcopf_solver(om, jpopt, new HashMap<String, AbstractMatrix>());
	}

	/**
	 *
	 * @param om an OPF model object
	 * @param jpopt a JPOWER options map
	 * @param out_opt a map containing keys (can be empty) for each of the desired
	 * optional output fields.
	 * @return a RESULTS struct, SUCCESS flag and RAW output struct.
	 */
	@SuppressWarnings("static-access")
	public static Object[] jp_dcopf_solver(Djp_opf_model om, Map<String, Double> jpopt, Map<String, AbstractMatrix> out_opt) {

		/* ----- initialization ----- */

		/* options */
		int verbose = jpopt.get("VERBOSE").intValue();
		int alg = jpopt.get("OPF_ALG_DC").intValue();

		if (alg == 0)
			alg = 200;		// JIPS

		/* unpack data */
		Djp_jpc jpc = om.get_jpc();
		double baseMVA = jpc.baseMVA;
		Djp_bus bus = jpc.bus;
		Djp_gen gen = jpc.gen;
		Djp_branch branch = jpc.branch;
		Djp_gencost gencost = jpc.gencost;
		Cost cp = om.get_cost_params();
		DoubleMatrix2D N = cp.N, H = cp.H;
		DoubleMatrix1D Cw = cp.Cw;
		DoubleMatrix2D fparm = DoubleFactory2D.dense.make((int) cp.dd.size(), 4);
		fparm.viewColumn(0).assign(cp.dd);
		fparm.viewColumn(1).assign(cp.rh);
		fparm.viewColumn(2).assign(cp.kk);
		fparm.viewColumn(3).assign(cp.mm);
		DoubleMatrix2D Bf = (DoubleMatrix2D) om.userdata("Bf");
		DoubleMatrix1D Pfinj = (DoubleMatrix1D) om.userdata("Pfinj");
		Map<String, Set>[] idx = om.get_idx();
		Map<String, Set> vv = idx[0], ll = idx[1];

		/* problem dimensions */
		int[] ipol = util.nonzero( gencost.model.copy().assign(ifunc.equals(POLYNOMIAL)) );	// polynomial costs
		int[] ipwl = util.nonzero( gencost.model.copy().assign(ifunc.equals(PW_LINEAR)) );	// piece-wise linear costs
		int nb = bus.size();	// number of buses
		int nl = branch.size();	// number of branches
		int nw = N.rows();		// number of general cost vars, w
		int ny = om.getN("var", "y");	// number of piece-wise linear costs
		int nxyz = om.getN("var");		// total number of control vars of all types

		/* linear constraints & variable bounds */
		AbstractMatrix[] Alu = om.linear_constraints();
		DoubleMatrix2D A = (DoubleMatrix2D) Alu[0];
		DoubleMatrix1D l = (DoubleMatrix1D) Alu[1];
		DoubleMatrix1D u = (DoubleMatrix1D) Alu[2];

		DoubleMatrix1D[] xx = om.getv();
		DoubleMatrix1D x0 = xx[0], xmin = xx[1], xmax = xx[2];

		/* set up objective function of the form: f = 1/2 * X'*HH*X + CC'*X
		 * where X = [x;y;z]. First set up as quadratic function of w,
		 * f = 1/2 * w'*HHw*w + CCw'*w, where w = diag(M) * (N*X - Rhat). We
		 * will be building on the (optionally present) user supplied parameters.
		 */

		/* piece-wise linear costs */
		int any_pwl = (ny > 0) ? 1 : 0;
		DoubleMatrix2D Npwl, Hpwl, fparm_pwl;
		DoubleMatrix1D Cpwl;
		if (any_pwl > 0) {
			Npwl = new SparseRCDoubleMatrix2D(1, nxyz, util.ones(ny),
					IntFactory1D.dense.make(ipwl).assign(ifunc.plus(vv.get("y").i0)).toArray(), 1, false, false);	// sum of y vars
			Hpwl = DoubleFactory2D.sparse.make(1, 1);
			Cpwl = DoubleFactory1D.dense.make(1, 1);
			fparm_pwl = DoubleFactory2D.dense.make(new double[][] {{1, 0, 0, 1}});
		} else {
			Npwl = DoubleFactory2D.sparse.make(0, nxyz);
			Hpwl = DoubleFactory2D.sparse.make(0, 0);
			Cpwl = DoubleFactory1D.dense.make(0);
			fparm_pwl = DoubleFactory2D.dense.make(0, 0);
		}

		/* quadratic costs */
		int npol = ipol.length;
		if (util.any( util.nonzero(util.dblm(gencost.ncost.viewSelection(ipol)).assign(dfunc.greater(3))) ))
			System.err.println("DC opf cannot handle polynomial costs with higher than quadratic order.");
			// TODO: throw invalid cost exception

		int[] iqdr = util.nonzero( gencost.ncost.viewSelection(ipol).copy().assign(ifunc.equals(3)) );
		int[] ilin = util.nonzero( gencost.ncost.viewSelection(ipol).copy().assign(ifunc.equals(2)) );
		DoubleMatrix2D polycf = DoubleFactory2D.dense.make(npol, 3);	// quadratic coeffs for Pg
		if (iqdr.length > 0)
			polycf.viewSelection(iqdr, null).assign( gencost.cost.viewSelection(ipol, null).viewSelection(iqdr, util.irange(0, 2)) );
		polycf.viewSelection(ilin, util.irange(1, 2)).assign( gencost.cost.viewSelection(ipol, null).viewSelection(ilin, util.irange(0, 2)) );
		polycf.assign(DoubleFactory2D.dense.diagonal(new double[] {Math.pow(baseMVA, 2), baseMVA, 1}), dfunc.mult);	// convert to p.u.
		DoubleMatrix2D Npol = new SparseRCDoubleMatrix2D(npol, nxyz, util.irange(npol),
				IntFactory1D.dense.make(ipol).assign(ifunc.plus(vv.get("Pg").i0)).toArray(), 1, false, false);
		DoubleMatrix2D Hpol = new SparseRCDoubleMatrix2D(npol, npol, util.irange(npol), util.irange(npol),
				polycf.viewColumn(0).copy().assign(dfunc.mult(2)).toArray(), false, false, false);
		DoubleMatrix1D Cpol = polycf.viewColumn(1);
		DoubleMatrix2D fparm_pol = DoubleFactory2D.dense.repeat(DoubleFactory2D.dense.make(new double[][] {{1, 0, 0, 1}}), 0, npol);

		/* combine with user costs */
		DoubleMatrix2D NN = DoubleFactory2D.sparse.compose(new DoubleMatrix2D[][] {{Npwl, Npol, N}});
		DoubleMatrix2D HHw = DoubleFactory2D.sparse.compose(new DoubleMatrix2D[][] {
				{Hpwl, DoubleFactory2D.sparse.make(any_pwl, npol+nw)},
				{DoubleFactory2D.sparse.make(npol, any_pwl), Hpol, DoubleFactory2D.sparse.make(npol, nw)},
				{DoubleFactory2D.sparse.make(nw, any_pwl+npol), H}
		});
		DoubleMatrix1D CCw = DoubleFactory1D.dense.make(new DoubleMatrix1D[] {Cpwl, Cpol, Cw});
		DoubleMatrix2D ffparm = DoubleFactory2D.dense.compose(new DoubleMatrix2D[][] {{fparm_pwl, fparm_pol, fparm}});

		/* transform quadratic coefficients for w into coefficients for X */
		int nnw = any_pwl+npol+nw;
		DoubleMatrix2D M = new SparseRCDoubleMatrix2D(nnw, nnw, util.irange(nnw), util.irange(nnw), ffparm.viewColumn(3).toArray(), false, false, false);
		DoubleMatrix1D MR = M.zMult(ffparm.viewColumn(1), null);
		DoubleMatrix1D HMR = HHw.zMult(MR, null);
		DoubleMatrix2D MN = M.zMult(NN, null);
		DoubleMatrix2D HH = MN.viewDice().zMult(HHw.zMult(MN, null), null);
		DoubleMatrix1D CC = MN.viewDice().zMult(CCw.copy().assign(HMR, dfunc.minus), null);
		double C0 = 1/2 * MR.zDotProduct(HMR) + polycf.viewColumn(2).aggregate(dfunc.plus, dfunc.identity);

		/* set up input for QP solver */
		Map<String, Object> opt = new HashMap<String, Object>();
		opt.put("alg", (double) alg);
		opt.put("verbose", (double) verbose);
		if (alg == 200 || alg == 250) {
			/* try to select an interior initial point */

			DoubleMatrix1D Varefs = bus.Va.viewSelection( bus.bus_type.copy().assign(ifunc.equals(REF)).toArray() ).assign(dfunc.mult(Math.PI)).assign(dfunc.div(180));
			DoubleMatrix1D lb = xmin.copy(), ub = xmax.copy();
			lb.viewSelection( util.intm( xmin.copy().assign(dfunc.equals(Double.NEGATIVE_INFINITY)) ).toArray() ).assign(-1e10);	// replace Inf with numerical proxies
			ub.viewSelection( util.intm( xmax.copy().assign(dfunc.equals(Double.POSITIVE_INFINITY)) ).toArray() ).assign( 1e10);
			x0 = lb.copy().assign(ub, dfunc.plus).assign(dfunc.div(2));
			x0.viewSelection(util.irange(vv.get("Va").i0, vv.get("Va").iN)).assign(Varefs.get(0));	// angles set to first reference angle
			if (ny > 0) {
				ipwl = util.nonzero( gencost.model.copy().assign(ifunc.equals(PW_LINEAR)) );
				double c = gencost.cost.viewSelection(ipwl, null).getMaxLocation()[0];		// largest y-value in CCV data
				// TODO: compute c using sub2ind
				x0.viewPart(vv.get("y").i0, vv.get("y").N).assign(c + 0.1 * Math.abs(c));
			}

			/* set up options */
			double feastol = jpopt.get("PDIPM_FEASTOL");
			double gradtol = jpopt.get("PDIPM_GRADTOL");
			double comptol = jpopt.get("PDIPM_COMPTOL");
			double costtol = jpopt.get("PDIPM_COSTTOL");
			int max_it = jpopt.get("PDIPM_MAX_IT").intValue();
			int max_red = jpopt.get("SCPDIPM_RED_IT").intValue();
			if (feastol == 0)
				feastol = jpopt.get("OPF_VIOLATION");	// = OPF_VIOLATION by default
			Map<String, Object> jips_opt = new HashMap<String, Object>();
			jips_opt.put("feastol", feastol);
			jips_opt.put("gradtol", gradtol);
			jips_opt.put("comptol", comptol);
			jips_opt.put("costtol", costtol);
			jips_opt.put("max_it", max_it);
			jips_opt.put("max_red", max_red);
			opt.put("jips_opt", jips_opt);
		}

		/* -----  run opf  ----- */

		Object[] qps = Djp_qps_jpower.jp_qps_jpower(HH, CC, A, l, u, xmin, xmax, x0, opt);
		DoubleMatrix1D x = (DoubleMatrix1D) qps[0];
		double f = (Double) qps[1];
		int info = (Integer) qps[2];
		Map<String, Object> output = (Map<String, Object>) qps[3];
		Map<String, DoubleMatrix1D> lambda = (Map<String, DoubleMatrix1D>) qps[4];

		boolean success = (info == 1);

		/* update solution data */
		DoubleMatrix1D Va = x.viewPart(vv.get("Va").i0, vv.get("Va").N).copy();
		DoubleMatrix1D Pg = x.viewPart(vv.get("Pg").i0, vv.get("Pg").N).copy();
		f += C0;

		/* -----  calculate return values  ----- */

		/* update voltages & generator outputs */
		bus.Va = Va.assign(dfunc.mult(180)).assign(dfunc.div(Math.PI));
		gen.Pg = Pg.assign(dfunc.mult(baseMVA));

		/* compute branch flows */
		branch.Qf = DoubleFactory1D.dense.make(nl);
		branch.Qt = DoubleFactory1D.dense.make(nl);
		branch.Pf = Bf.zMult(Va, null).assign(Pfinj, dfunc.plus).assign(dfunc.mult(baseMVA));
		branch.Pt = branch.Pf.copy().assign(dfunc.neg);

		/* package up results */
		DoubleMatrix1D mu_l = lambda.get("mu_l");
		DoubleMatrix1D mu_u = lambda.get("mu_u");
		DoubleMatrix1D muLB = lambda.get("lower");
		DoubleMatrix1D muUB = lambda.get("upper");

		/* update Lagrange multipliers */
		int[] il = util.nonzero( util.intm( branch.rate_a.copy().assign(dfunc.equals(0)) ).assign(ifunc.not).assign(util.intm( branch.rate_a.assign(dfunc.less(1e10)) ), ifunc.and) );
		bus.lam_P = DoubleFactory1D.dense.make(nb);
		bus.lam_Q = DoubleFactory1D.dense.make(nb);
		bus.mu_Vmin = DoubleFactory1D.dense.make(nb);
		bus.mu_Vmax = DoubleFactory1D.dense.make(nb);
		gen.mu_Pmin = DoubleFactory1D.dense.make(gen.size());
		gen.mu_Pmax = DoubleFactory1D.dense.make(gen.size());
		gen.mu_Qmin = DoubleFactory1D.dense.make(gen.size());
		gen.mu_Qmax = DoubleFactory1D.dense.make(gen.size());
		branch.mu_Sf = DoubleFactory1D.dense.make(nl);
		branch.mu_St = DoubleFactory1D.dense.make(nl);

		bus.lam_P = mu_u.viewPart(ll.get("Pmis").i0, ll.get("Pmis").N).copy().assign(mu_l.viewPart(ll.get("Pmis").i0, ll.get("Pmis").N), dfunc.minus).assign(dfunc.div(baseMVA));
		branch.mu_Sf.viewSelection(il).assign( mu_u.viewPart(ll.get("Pf").i0, ll.get("Pf").N).copy().assign(dfunc.div(baseMVA) ));
		branch.mu_St.viewSelection(il).assign( mu_u.viewPart(ll.get("Pt").i0, ll.get("Pt").N).copy().assign(dfunc.div(baseMVA) ));
		gen.mu_Pmin = muLB.viewPart(vv.get("Pg").i0, vv.get("Pg").N).copy().assign(dfunc.div(baseMVA));
		gen.mu_Pmax = muUB.viewPart(vv.get("Pg").i0, vv.get("Pg").N).copy().assign(dfunc.div(baseMVA));

		Map<String, Map<String, DoubleMatrix1D>> mu = new HashMap<String, Map<String,DoubleMatrix1D>>();
		Map<String, DoubleMatrix1D> var = new HashMap<String, DoubleMatrix1D>();
		var.put("l", muLB);
		var.put("u", muUB);
		mu.put("var", var);
		Map<String, DoubleMatrix1D> lin = new HashMap<String, DoubleMatrix1D>();
		lin.put("l", mu_l);
		lin.put("u", mu_u);
		mu.put("lin", lin);

		Djp_jpc results = jpc.copy();
		results.bus = bus.copy();
		results.branch = branch.copy();
		results.gen = gen.copy();
//		results.om = om;
//		results.x = x.copy();
//		results.mu = mu;
//		results.f = f;

		/* optional fields */
		/* 1st one is always computed anyway, just include it */
//		if (out_opt.containsKey("g"))
//			results.g = A.zMult(x, null);
//		results.dg = A;
//		if (out_opt.containsKey("df"))
//			results.df = null;
//		if (out_opt.containsKey("d2f"))
//			results.d2f = null;

		DoubleMatrix1D pimul = DoubleFactory1D.dense.make(new DoubleMatrix1D[] {
				mu_l.copy().assign(mu_u, dfunc.minus),
				DoubleFactory1D.dense.make((ny>0) ? 1:0, -1),	// dummy entry corresponding to linear cost row in A (in MINOS)
				muLB.copy().assign(muUB, dfunc.minus)
		});

		Map<String, Object> raw = new HashMap<String, Object>();
		raw.put("xr", x);
		raw.put("pimul", pimul);
		raw.put("info", info);
		raw.put("output", output);

		return new Object[] {results, success, raw};
	}
}
