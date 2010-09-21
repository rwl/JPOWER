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
import cern.colt.matrix.tdcomplex.DComplexMatrix1D;
import cern.colt.matrix.tdcomplex.DComplexMatrix2D;
import cern.colt.matrix.tdouble.DoubleFactory1D;
import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.jet.math.tdcomplex.DComplexFunctions;
import cern.jet.math.tdouble.DoubleFunctions;
import cern.jet.math.tint.IntFunctions;
import edu.cornell.pserc.jips.tdouble.ConstraintEvaluator;
import edu.cornell.pserc.jips.tdouble.Dips_jips;
import edu.cornell.pserc.jips.tdouble.HessianEvaluator;
import edu.cornell.pserc.jips.tdouble.ObjectiveEvaluator;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_branch;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_bus;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_gen;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_gencost;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_jpc;
import edu.cornell.pserc.jpower.tdouble.opf.Djp_opf_model.Set;
import edu.cornell.pserc.jpower.tdouble.pf.Djp_makeYbus;
import edu.cornell.pserc.util.tdouble.Djp_util;

/**
 * Solves AC optimal power flow using JIPS.
 *
 * @author Ray Zimmerman (rz10@cornell.edu)
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class Djp_jipsopf_solver {

	private static final Djp_util util = new Djp_util();
	private static final DoubleFunctions dfunc = DoubleFunctions.functions;
	private static final IntFunctions ifunc = IntFunctions.intFunctions;
	private static final DComplexFunctions cfunc = DComplexFunctions.functions;

	private static final int PW_LINEAR = Djp_jpc.PW_LINEAR;
	private static final int REF = Djp_jpc.REF;

	public static Object[] jp_jipsopf_solver(Djp_opf_model om, Map<String, Double> jpopt) {
		return jp_jipsopf_solver(om, jpopt, new HashMap<String, AbstractMatrix>());
	}

	/**
	 * Solves AC optimal power flow using JIPS.
	 *
	 * @param om
	 * @param jpopt
	 * @param output
	 * @return
	 */
	@SuppressWarnings("static-access")
	public static Object[] jp_jipsopf_solver(Djp_opf_model om, Map<String, Double> jpopt, Map<String, AbstractMatrix> out_opt) {

		/* ----- initialization ----- */

		/* options */
		boolean verbose = jpopt.get("VERBOSE") == 1;
		double feastol = jpopt.get("PDIPM_FEASTOL");
		double gradtol = jpopt.get("PDIPM_GRADTOL");
		double comptol = jpopt.get("PDIPM_COMPTOL");
		double costtol = jpopt.get("PDIPM_COSTTOL");
		double max_it = jpopt.get("PDIPM_MAX_IT");
		double max_red = jpopt.get("SCPDIPM_RED_IT");
		double step_control = jpopt.get("OPF_ALG") == 565 ? 1:0;
		if (feastol == 0)
			feastol = jpopt.get("OPF_VIOLATION");	// = OPF_VIOLATION by default
		Map<String, Double> opt = new HashMap<String, Double>();
		opt.put("feastol", feastol);
		opt.put("gradtol", gradtol);
		opt.put("comptol", comptol);
		opt.put("costtol", costtol);
		opt.put("max_it", max_it);
		opt.put("max_red", max_red);
		opt.put("step_control", step_control);
		opt.put("cost_mult", 1e-4);
		opt.put("verbose", verbose ? 1.0 : 0.0);

		/* unpack data */
		Djp_jpc jpc = om.get_jpc();
		double baseMVA = jpc.baseMVA;
		Djp_bus bus = jpc.bus;
		Djp_gen gen = jpc.gen;
		Djp_branch branch = jpc.branch;
		Djp_gencost gencost = jpc.gencost;
		Map<String, Set>[] idx = om.get_idx();
		Map<String, Set> vv = idx[0], ll = idx[1], nn = idx[2];

		/* problem dimensions */
		int nb = bus.size();			// number of buses
		int nl = branch.size();			// number of branches
		int ny = om.getN("var", "y");	// number of piece-wise linear costs

		/* linear constraints */
		AbstractMatrix[] Alu = om.linear_constraints();
		DoubleMatrix2D A = (DoubleMatrix2D) Alu[0];
		DoubleMatrix1D l = (DoubleMatrix1D) Alu[1];
		DoubleMatrix1D u = (DoubleMatrix1D) Alu[2];

		DoubleMatrix1D[] xx = om.getv();
		DoubleMatrix1D x0 = xx[0], xmin = xx[1], xmax = xx[2];

		/* build admittance matrices */
		DComplexMatrix2D[] Y = Djp_makeYbus.jp_makeYbus(baseMVA, bus, branch);
		DComplexMatrix2D Ybus = Y[0], Yf = Y[1], Yt = Y[2];

		/* try to select an interior initial point */
		DoubleMatrix1D lb = xmin.copy(), ub = xmax.copy();
		lb.viewSelection( util.intm( xmin.copy().assign(dfunc.equals(Double.NEGATIVE_INFINITY)) ).toArray() ).assign(-1e10);	// replace Inf with numerical proxies
		ub.viewSelection( util.intm( xmax.copy().assign(dfunc.equals(Double.POSITIVE_INFINITY)) ).toArray() ).assign( 1e10);
		x0 = lb.copy().assign(ub, dfunc.plus).assign(dfunc.div(2));
		DoubleMatrix1D Varefs = bus.Va.viewSelection( bus.bus_type.copy().assign(ifunc.equals(REF)).toArray() ).assign(dfunc.mult(Math.PI)).assign(dfunc.div(180));
		x0.viewSelection(util.irange(vv.get("Va").i0, vv.get("Va").iN)).assign(Varefs.get(0));	// angles set to first reference angle
		if (ny > 0) {
			int[] ipwl = util.nonzero( gencost.model.copy().assign(ifunc.equals(PW_LINEAR)) );
			double c = gencost.cost.viewSelection(ipwl, null).getMaxLocation()[0];		// largest y-value in CCV data
			// TODO: compute c using sub2ind
			x0.viewPart(vv.get("y").i0, vv.get("y").N).assign(c + 0.1 * Math.abs(c));
		}

		/* find branches with flow limits */
		int[] il = util.nonzero( util.intm( branch.rate_a.copy().assign(dfunc.equals(0)) ).assign(ifunc.not).assign(util.intm( branch.rate_a.assign(dfunc.less(1e10)) ), ifunc.and) );
		int nl2 = il.length;	// number of constrained lines

		/* -----  run opf  ----- */
		ObjectiveEvaluator f_fcn = new Djp_opf_costfcn(om);
		HessianEvaluator hess_fcn = new Djp_opf_hessfcn(om, Ybus, Yf.viewSelection(il, null), Yt.viewSelection(il, null), jpopt, il, opt.get("cost_mult"));
		ConstraintEvaluator gh_fcn = new Djp_opf_consfcn(om, Ybus, Yf.viewSelection(il, null), Yt.viewSelection(il, null), jpopt, il);
		Object[] jips = Dips_jips.ips_jips(f_fcn, x0, A, l, u, xmin, xmax, gh_fcn, hess_fcn, opt);
		DoubleMatrix1D x = (DoubleMatrix1D) jips[0];
		double f = (Double) jips[1];
		int info = (Integer) jips[2];
		Map<String, Object> Output = (Map<String, Object>) jips[3];
		Map<String, DoubleMatrix1D> Lambda = (Map<String, DoubleMatrix1D>) jips[4];

		boolean success = (info > 0);

		/* update solution data */
		DoubleMatrix1D Va = x.viewPart(vv.get("Va").i0, vv.get("Va").N).copy();
		DoubleMatrix1D Vm = x.viewPart(vv.get("Vm").i0, vv.get("Vm").N).copy();
		DoubleMatrix1D Pg = x.viewPart(vv.get("Pg").i0, vv.get("Pg").N).copy();
		DoubleMatrix1D Qg = x.viewPart(vv.get("Qg").i0, vv.get("Qg").N).copy();
		DComplexMatrix1D V = util.complex(Vm, Va);

		/* -----  calculate return values  ----- */

		/* update voltages & generator outputs */
		bus.Va = Va.assign(dfunc.mult(180)).assign(dfunc.div(Math.PI));
		bus.Vm = Vm;
		gen.Pg = Pg.assign(dfunc.mult(baseMVA));
		gen.Qg = Qg.assign(dfunc.mult(baseMVA));
		gen.Vg = Vm.viewSelection(gen.gen_bus.toArray());

		/* compute branch flows */
		DComplexMatrix1D Sf = Yf.zMult(V, null).assign(cfunc.conj).assign(V.viewSelection(branch.f_bus.toArray()), cfunc.mult);	// cplx pwr at "from" bus, p.u.
		DComplexMatrix1D St = Yt.zMult(V, null).assign(cfunc.conj).assign(V.viewSelection(branch.t_bus.toArray()), cfunc.mult);	// cplx pwr at "to" bus, p.u.
		branch.Pf = Sf.getRealPart().assign(dfunc.mult(baseMVA));
		branch.Qf = Sf.getImaginaryPart().assign(dfunc.mult(baseMVA));
		branch.Pt = St.getRealPart().assign(dfunc.mult(baseMVA));
		branch.Qt = St.getImaginaryPart().assign(dfunc.mult(baseMVA));

		/* line constraint is actually on square of limit, so we must fix multipliers */
		DoubleMatrix1D muSf = DoubleFactory1D.dense.make(nl);
		DoubleMatrix1D muSt = DoubleFactory1D.dense.make(nl);
		if (il.length > 0) {
			muSf.viewSelection(il).assign( Lambda.get("ineqnonlin").viewPart(0,   nl2).copy().assign(branch.rate_a.viewSelection(il), dfunc.mult).assign(dfunc.mult(2)).assign(dfunc.div(baseMVA)) );
			muSt.viewSelection(il).assign( Lambda.get("ineqnonlin").viewPart(nl2, nl2).copy().assign(branch.rate_a.viewSelection(il), dfunc.mult).assign(dfunc.mult(2)).assign(dfunc.div(baseMVA)) );
		}

		/* update Lagrange multipliers */
		bus.mu_Vmax = Lambda.get("upper").viewPart(vv.get("Vm").i0, vv.get("Vm").N).copy();
		bus.mu_Vmin = Lambda.get("lower").viewPart(vv.get("Vm").i0, vv.get("Vm").N).copy();
		gen.mu_Pmax = Lambda.get("upper").viewPart(vv.get("Pg").i0, vv.get("Pg").N).copy().assign(dfunc.div(baseMVA));
		gen.mu_Pmin = Lambda.get("lower").viewPart(vv.get("Pg").i0, vv.get("Pg").N).copy().assign(dfunc.div(baseMVA));
		gen.mu_Qmax = Lambda.get("upper").viewPart(vv.get("Qg").i0, vv.get("Qg").N).copy().assign(dfunc.div(baseMVA));
		gen.mu_Qmin = Lambda.get("lower").viewPart(vv.get("Qg").i0, vv.get("Qg").N).copy().assign(dfunc.div(baseMVA));
		bus.lam_P = Lambda.get("eqnonlin").viewPart(nn.get("Pmis").i0, nn.get("Pmis").N).copy().assign(dfunc.div(baseMVA));
		bus.lam_Q = Lambda.get("eqnonlin").viewPart(nn.get("Qmis").i0, nn.get("Qmis").N).copy().assign(dfunc.div(baseMVA));
		branch.mu_Sf = muSf.copy().assign(dfunc.div(baseMVA));
		branch.mu_St = muSt.copy().assign(dfunc.div(baseMVA));

		/* package up results */
		int nlnN = om.getN("nln");

		/* extract multipliers for non-linear constraints */
		int[] kl = util.nonzero( Lambda.get("eqnonlin").copy().assign(dfunc.less(0)) );
		int[] ku = util.nonzero( Lambda.get("eqnonlin").copy().assign(dfunc.greater(0)) );
		DoubleMatrix1D nl_mu_l = DoubleFactory1D.dense.make(nlnN);
		DoubleMatrix1D nl_mu_u = DoubleFactory1D.dense.make(new DoubleMatrix1D[] {DoubleFactory1D.dense.make(nb), muSf, muSt});
		nl_mu_l.viewSelection(kl).assign( Lambda.get("eqnonlin").viewSelection(kl).copy().assign(dfunc.neg) );
		nl_mu_u.viewSelection(ku).assign( Lambda.get("eqnonlin").viewSelection(ku) );

		Map<String, Map<String, DoubleMatrix1D>> mu = new HashMap<String, Map<String,DoubleMatrix1D>>();
		Map<String, DoubleMatrix1D> var = new HashMap<String, DoubleMatrix1D>();
		var.put("l", Lambda.get("lower"));
		var.put("u", Lambda.get("upper"));
		mu.put("var", var);
		Map<String, DoubleMatrix1D> nln = new HashMap<String, DoubleMatrix1D>();
		nln.put("l", nl_mu_l);
		nln.put("u", nl_mu_u);
		mu.put("nln", nln);
		Map<String, DoubleMatrix1D> lin = new HashMap<String, DoubleMatrix1D>();
		lin.put("l", Lambda.get("mu_l"));
		lin.put("u", Lambda.get("mu_u"));
		mu.put("lin", lin);

		Djp_jpc results = jpc.copy();
		results.bus = bus.copy();
		results.branch = branch.copy();
		results.gen = gen.copy();
		results.om = om;
		results.x = x.copy();
		results.mu = mu;
		results.f = f;

		/* optional fields */
		if (out_opt.containsKey("dg")) {
			ConstraintEvaluator geq_fcn = new Djp_opf_consfcn(om, Ybus, Yf, Yt, jpopt);
			DoubleMatrix1D[] geq = geq_fcn.gh(x);
			results.g  = DoubleFactory1D.dense.append(geq[1], geq[0]);	// include this since we computed it anyway
			DoubleMatrix2D[] dgeq = geq_fcn.dgh(x);
			results.dg = DoubleFactory2D.sparse.appendRows(dgeq[1].viewDice(), dgeq[0].viewDice());	// true Jacobian organization
		}
		if (out_opt.containsKey("g") && !out_opt.containsKey("dg")) {
			ConstraintEvaluator geq_fcn = new Djp_opf_consfcn(om, Ybus, Yf, Yt, jpopt);
			DoubleMatrix1D[] geq = geq_fcn.gh(x);
			results.g = DoubleFactory1D.dense.append(geq[1], geq[0]);
		}
		if (out_opt.containsKey("df"))
			results.df = null;
		if (out_opt.containsKey("d2f"))
			results.d2f = null;

		DoubleMatrix1D pimul = DoubleFactory1D.dense.make(new DoubleMatrix1D[] {
				results.mu.get("nln").get("l").copy().assign(results.mu.get("nln").get("u"), dfunc.minus),
				results.mu.get("lin").get("l").copy().assign(results.mu.get("lin").get("u"), dfunc.minus),
				DoubleFactory1D.dense.make((ny>0) ? 1:0, -1),
				results.mu.get("var").get("l").copy().assign(results.mu.get("var").get("u"), dfunc.minus)
		});

		Map<String, Object> raw = new HashMap<String, Object>();
		raw.put("xr", x);
		raw.put("pimul", pimul);
		raw.put("info", info);
		raw.put("output", Output);

		return new Object[] {results, success, raw};
	}
}
