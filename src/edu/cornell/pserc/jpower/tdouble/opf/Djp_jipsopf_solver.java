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

import java.util.HashMap;
import java.util.Map;

import cern.colt.matrix.AbstractMatrix;
import cern.colt.matrix.tdcomplex.DComplexMatrix1D;
import cern.colt.matrix.tdcomplex.DComplexMatrix2D;
import cern.colt.matrix.tdouble.DoubleFactory1D;
import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.util.tdouble.Djp_util;
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

/**
 * Solves AC optimal power flow using JIPS.
 *
 * @author Ray Zimmerman
 * @author Richard Lincoln
 *
 */
public class Djp_jipsopf_solver {

	private static final DoubleFunctions dfunc = DoubleFunctions.functions;
	private static final IntFunctions ifunc = IntFunctions.intFunctions;
	private static final DComplexFunctions cfunc = DComplexFunctions.functions;

	private static final int PW_LINEAR = Djp_jpc.PW_LINEAR;
	private static final int REF       = Djp_jpc.REF;

	private static int nb, nl, ny, nl2, info, nlnN;
	private static int[] ipwl, il, kl, ku;
	private static boolean verbose, success;
	private static double feastol, gradtol, comptol, costtol, max_it, max_red,
			step_control, baseMVA, c, f;
	private static Map<String, Double> opt;
	private static Djp_jpc jpc, results;
	private static Djp_bus bus;
	private static Djp_gen gen;
	private static Djp_branch branch;
	private static Djp_gencost gencost;

	private static Map<String, Set>[] idx;
	private static Map<String, Set> vv, ll, nn;
	private static Map<String, Object> Output, raw;
	private static Map<String, DoubleMatrix1D> Lambda, var, nln, lin;
	private static Map<String, Map<String, DoubleMatrix1D>> mu;

	private static AbstractMatrix[] Alu;
	private static DoubleMatrix1D l, u, x0, xmin, xmax, lb, ub, Varefs, x,
			Va, Vm, Pg, Qg, muSf, muSt, nl_mu_l, nl_mu_u, pimul;
	private static DoubleMatrix1D[] xx;
	private static DoubleMatrix1D[] geq;
	private static DoubleMatrix2D A;
	private static DComplexMatrix1D V, Sf, St;
	private static DComplexMatrix2D Ybus, Yf, Yt;
	private static DComplexMatrix2D[] Y;

	private static ObjectiveEvaluator f_fcn;
	private static HessianEvaluator hess_fcn;
	private static ConstraintEvaluator gh_fcn, geq_fcn;

	private static Object[] jips;

	/**
	 * Solves AC optimal power flow using JIPS.
	 *
	 * @param om
	 * @param jpopt
	 * @param output
	 * @return
	 */
	@SuppressWarnings("static-access")
	public static Object[] jipsopf_solver(Djp_opf_model om,
			Map<String, Double> jpopt, Map<String, AbstractMatrix> out_opt) {

		/* ----- initialization ----- */

		/* options */
		verbose = jpopt.get("VERBOSE") == 1;
		feastol = jpopt.get("PDIPM_FEASTOL");
		gradtol = jpopt.get("PDIPM_GRADTOL");
		comptol = jpopt.get("PDIPM_COMPTOL");
		costtol = jpopt.get("PDIPM_COSTTOL");
		max_it  = jpopt.get("PDIPM_MAX_IT");
		max_red = jpopt.get("SCPDIPM_RED_IT");
		step_control = jpopt.get("OPF_ALG") == 565 ? 1:0;
		if (feastol == 0)
			feastol = jpopt.get("OPF_VIOLATION");	// = OPF_VIOLATION by default
		opt = new HashMap<String, Double>();
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
		jpc = om.get_jpc();
		baseMVA = jpc.baseMVA;
		bus = jpc.bus;
		gen = jpc.gen;
		branch = jpc.branch;
		gencost = jpc.gencost;
		idx = om.get_idx();
		vv = idx[0]; ll = idx[1]; nn = idx[2];

		/* problem dimensions */
		nb = bus.size();			// number of buses
		nl = branch.size();			// number of branches
		ny = om.getN("var", "y");	// number of piece-wise linear costs

		/* linear constraints */
		Alu = om.linear_constraints();
		A = (DoubleMatrix2D) Alu[0];
		l = (DoubleMatrix1D) Alu[1];
		u = (DoubleMatrix1D) Alu[2];

		xx = om.getv();
		x0 = xx[0]; xmin = xx[1]; xmax = xx[2];

		/* build admittance matrices */
		Y = Djp_makeYbus.makeYbus(baseMVA, bus, branch);
		Ybus = Y[0]; Yf = Y[1]; Yt = Y[2];

		/* try to select an interior initial point */
		lb = xmin.copy(); ub = xmax.copy();
		lb.viewSelection( Djp_util.intm( xmin.copy().assign(dfunc.equals(Double.NEGATIVE_INFINITY)) ).toArray() ).assign(-1e10);	// replace Inf with numerical proxies
		ub.viewSelection( Djp_util.intm( xmax.copy().assign(dfunc.equals(Double.POSITIVE_INFINITY)) ).toArray() ).assign( 1e10);
		x0 = lb.copy().assign(ub, dfunc.plus).assign(dfunc.div(2));
		Varefs = bus.Va.viewSelection( bus.bus_type.copy().assign(ifunc.equals(REF)).toArray() ).assign(dfunc.mult(Math.PI)).assign(dfunc.div(180));
		x0.viewSelection(Djp_util.irange(vv.get("Va").i0, vv.get("Va").iN)).assign(Varefs.get(0));	// angles set to first reference angle
		if (ny > 0) {
			ipwl = Djp_util.nonzero( gencost.model.copy().assign(ifunc.equals(PW_LINEAR)) );
			c = gencost.cost.viewSelection(ipwl, null).getMaxLocation()[0];		// largest y-value in CCV data
			// TODO: compute c using sub2ind
			x0.viewPart(vv.get("y").i0, vv.get("y").N).assign(c + 0.1 * Math.abs(c));
		}

		/* find branches with flow limits */
		il = Djp_util.nonzero( Djp_util.intm( branch.rate_a.copy().assign(dfunc.equals(0)) ).assign(ifunc.not).assign(Djp_util.intm( branch.rate_a.assign(dfunc.less(1e10)) ), ifunc.and) );
		nl2 = il.length;	// number of constrained lines

		/* -----  run opf  ----- */
		f_fcn = new Djp_opf_costfcn(om);
		hess_fcn = new Djp_opf_hessfcn(om, Ybus, Yf.viewSelection(il, null), Yt.viewSelection(il, null), jpopt, il, opt.get("cost_mult"));
		gh_fcn = new Djp_opf_consfcn(om, Ybus, Yf.viewSelection(il, null), Yt.viewSelection(il, null), jpopt, il);
		jips = Dips_jips.jips(f_fcn, x0, A, l, u, xmin, xmax, gh_fcn, hess_fcn, opt);
		x = (DoubleMatrix1D) jips[0];
		f = (Double) jips[1];
		info = (Integer) jips[2];
		Output = (Map<String, Object>) jips[3];
		Lambda = (Map<String, DoubleMatrix1D>) jips[4];

		success = (info > 0);

		/* update solution data */
		Va = x.viewPart(vv.get("Va").i0, vv.get("Va").N).copy();
		Vm = x.viewPart(vv.get("Vm").i0, vv.get("Vm").N).copy();
		Pg = x.viewPart(vv.get("Pg").i0, vv.get("Pg").N).copy();
		Qg = x.viewPart(vv.get("Qg").i0, vv.get("Qg").N).copy();
		V  = Djp_util.complex(Vm, Va);

		/* -----  calculate return values  ----- */

		/* update voltages & generator outputs */
		bus.Va = Va.assign(dfunc.mult(180)).assign(dfunc.div(Math.PI));
		bus.Vm = Vm;
		gen.Pg = Pg.assign(dfunc.mult(baseMVA));
		gen.Qg = Qg.assign(dfunc.mult(baseMVA));
		gen.Vg = Vm.viewSelection(gen.gen_bus.toArray());

		/* compute branch flows */
		Sf = Yf.zMult(V, null).assign(cfunc.conj).assign(V.viewSelection(branch.f_bus.toArray()), cfunc.mult);	// cplx pwr at "from" bus, p.u.
		St = Yt.zMult(V, null).assign(cfunc.conj).assign(V.viewSelection(branch.t_bus.toArray()), cfunc.mult);	// cplx pwr at "to" bus, p.u.
		branch.Pf = Sf.getRealPart().assign(dfunc.mult(baseMVA));
		branch.Qf = Sf.getImaginaryPart().assign(dfunc.mult(baseMVA));
		branch.Pt = St.getRealPart().assign(dfunc.mult(baseMVA));
		branch.Qt = St.getImaginaryPart().assign(dfunc.mult(baseMVA));

		/* line constraint is actually on square of limit, so we must fix multipliers */
		muSf = DoubleFactory1D.dense.make(nl);
		muSt = DoubleFactory1D.dense.make(nl);
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
		nlnN = om.getN("nln");

		/* extract multipliers for non-linear constraints */
		kl = Djp_util.nonzero( Lambda.get("eqnonlin").copy().assign(dfunc.less(0)) );
		ku = Djp_util.nonzero( Lambda.get("eqnonlin").copy().assign(dfunc.greater(0)) );
		nl_mu_l = DoubleFactory1D.dense.make(nlnN);
		nl_mu_u = DoubleFactory1D.dense.make(new DoubleMatrix1D[] {DoubleFactory1D.dense.make(nb), muSf, muSt});
		nl_mu_l.viewSelection(kl).assign( Lambda.get("eqnonlin").viewSelection(kl).copy().assign(dfunc.neg) );
		nl_mu_u.viewSelection(ku).assign( Lambda.get("eqnonlin").viewSelection(ku) );

		mu = new HashMap<String, Map<String,DoubleMatrix1D>>();
		var = new HashMap<String, DoubleMatrix1D>();
		var.put("l", Lambda.get("lower"));
		var.put("u", Lambda.get("upper"));
		mu.put("var", var);
		nln = new HashMap<String, DoubleMatrix1D>();
		nln.put("l", nl_mu_l);
		nln.put("u", nl_mu_u);
		mu.put("nln", nln);
		lin = new HashMap<String, DoubleMatrix1D>();
		lin.put("l", Lambda.get("mu_l"));
		lin.put("u", Lambda.get("mu_u"));
		mu.put("lin", lin);

		results = jpc.copy();
		results.bus = bus.copy();
		results.branch = branch.copy();
		results.gen = gen.copy();
		results.om = om;
		results.x = x.copy();
		results.mu = mu;
		results.f = f;

		/* optional fields */
		if (out_opt.containsKey("dg")) {
			geq_fcn = new Djp_opf_consfcn(om, Ybus, Yf, Yt, jpopt);
			geq = geq_fcn.gh(x);
			results.g  = DoubleFactory1D.dense.append(geq[1], geq[0]);	// include this since we computed it anyway
			DoubleMatrix2D[] dgeq = geq_fcn.dgh(x);
			results.dg = DoubleFactory2D.sparse.appendRows(dgeq[1].viewDice(), dgeq[0].viewDice());	// true Jacobian organization
		}
		if (out_opt.containsKey("g") && !out_opt.containsKey("dg")) {
			geq_fcn = new Djp_opf_consfcn(om, Ybus, Yf, Yt, jpopt);
			geq = geq_fcn.gh(x);
			results.g = DoubleFactory1D.dense.append(geq[1], geq[0]);
		}
		if (out_opt.containsKey("df"))
			results.df = null;
		if (out_opt.containsKey("d2f"))
			results.d2f = null;

		pimul = DoubleFactory1D.dense.make(new DoubleMatrix1D[] {
				results.mu.get("nln").get("l").copy().assign(results.mu.get("nln").get("u"), dfunc.minus),
				results.mu.get("lin").get("l").copy().assign(results.mu.get("lin").get("u"), dfunc.minus),
				DoubleFactory1D.dense.make((ny>0) ? 1:0, -1),
				results.mu.get("var").get("l").copy().assign(results.mu.get("var").get("u"), dfunc.minus)
		});

		raw = new HashMap<String, Object>();
		raw.put("xr", x);
		raw.put("pimul", pimul);
		raw.put("info", info);
		raw.put("output", Output);

		return new Object[] {results, success, raw};
	}

	public static Object[] jipsopf_solver(Djp_opf_model om, Map<String, Double> jpopt) {
		return jipsopf_solver(om, jpopt, new HashMap<String, AbstractMatrix>());
	}

}
