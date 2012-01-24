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

package edu.cornell.pserc.jpower.opf;

import java.util.HashMap;
import java.util.Map;

import cern.colt.matrix.AbstractMatrix;
import cern.colt.matrix.tdouble.DoubleFactory1D;
import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.SparseRCDoubleMatrix2D;

import static cern.colt.util.tdouble.Util.ifunc;
import static cern.colt.util.tdouble.Util.dfunc;
import static cern.colt.util.tdouble.Util.irange;
import static cern.colt.util.tdouble.Util.any;
import static cern.colt.util.tdouble.Util.icat;
import static cern.colt.util.tdouble.Util.nonzero;
import static cern.colt.util.tdouble.Util.intm;
import static cern.colt.util.tdouble.Util.scat;

import static edu.cornell.pserc.jpower.Djp_ext2int.ext2int;
import static edu.cornell.pserc.jpower.Djp_jpver.jpver;
import static edu.cornell.pserc.jpower.Djp_run_userfcn.run_userfcn;
import static edu.cornell.pserc.jpower.jpc.JPC.POLYNOMIAL;
import static edu.cornell.pserc.jpower.jpc.JPC.PW_LINEAR;
import static edu.cornell.pserc.jpower.jpc.JPC.REF;
import static edu.cornell.pserc.jpower.opf.Djp_dcopf_solver.dcopf_solver;
import static edu.cornell.pserc.jpower.opf.Djp_jipsopf_solver.jipsopf_solver;
import static edu.cornell.pserc.jpower.opf.Djp_makeAang.makeAang;
import static edu.cornell.pserc.jpower.opf.Djp_makeApq.makeApq;
import static edu.cornell.pserc.jpower.opf.Djp_makeAvl.makeAvl;
import static edu.cornell.pserc.jpower.opf.Djp_makeAy.makeAy;
import static edu.cornell.pserc.jpower.opf.Djp_opf_args.opf_args;
import static edu.cornell.pserc.jpower.opf.Djp_pqcost.pqcost;
import static edu.cornell.pserc.jpower.pf.Djp_makeBdc.makeBdc;

import edu.cornell.pserc.jpower.jpc.Branch;
import edu.cornell.pserc.jpower.jpc.Bus;
import edu.cornell.pserc.jpower.jpc.Gen;
import edu.cornell.pserc.jpower.jpc.Cost;
import edu.cornell.pserc.jpower.jpc.JPC;
import edu.cornell.pserc.jpower.opf.Djp_opf_model.CostParams;
import edu.cornell.pserc.jpower.opf.Djp_opf_model.Set;

/**
 * Solves an optimal power flow.
 *
 * @author Ray Zimmerman
 * @author Richard Lincoln
 *
 */
public class Djp_opf {

	/**
	 * Returns either a RESULTS struct and an optional SUCCESS flag, or individual
	 * data matrices, the objective function value and a SUCCESS flag. In the
	 * latter case, there are additional optional return values.
	 *
	 * @param casedata
	 * @param jpopt
	 * @return
	 */
	@SuppressWarnings("static-access")
	public static JPC opf(JPC jpc, DoubleMatrix2D A, DoubleMatrix1D l, DoubleMatrix1D u,
			Map<String, Double> jpopt, DoubleMatrix2D N, DoubleMatrix2D fparm, DoubleMatrix2D H,
			DoubleMatrix1D Cw, DoubleMatrix1D z0, DoubleMatrix1D zl, DoubleMatrix1D zu) {
		long t0;
		int alg, verbose, nb, nl, ng, nusr, nw, nl2, ny, nx, nz, nv, nq, q1;
		int[] acc, p1, refs, il = null;;
		boolean dc, success;
		double baseMVA;
		String userfcn;
		String[] user_vars, ycon_vars;

		Object[] opf_args, Alu_pq, r;

		Bus bus;
		Gen gen;
		Branch branch;
		Cost gencost;
		Cost[] pqcost;

		DoubleMatrix1D x0, y0, x1, y1, m, b, lbu, ubu, Va, Vm, Pg, Qg,
				Pmin, Pmax, Qmin, Qmax, Vau, Val, lang, uang, iang, by,
				Pbusinj, Pfinj = null, bmis = null, lpf = null, upf = null,
				upt = null, lvl = null, uvl = null, ubpqh = null, ubpql = null,
				Apqdata;
		DoubleMatrix2D Au, neg_Cg, Aang, Ay, B, Bf = null, Amis = null,
				Avl = null, Apqh = null, Apql = null;

		AbstractMatrix[] Bdc, Alu_vl, Alu_ang;;

		Djp_opf_model om;
		CostParams user_cost;

		Map<String, Set>[] idx;
		Map<String, Set> vv, ll, nn;
		Map<String, AbstractMatrix> output, results, raw;
		Map<String, String> v;


		/* ----- initialization ----- */

		t0 = System.currentTimeMillis();	// start timer

		/* process input arguments */
		opf_args = opf_args();
		jpc = (JPC) opf_args[0];
		jpopt = (Map<String, Double>) opf_args[1];

		/* options */
		dc = jpopt.get("PF_DC") == 1;	// true = DC OPF, false = AC OPF
		alg = jpopt.get("OPF_ALG").intValue();
		verbose = jpopt.get("VERBOSE").intValue();

		/* set AC OPF algorithm code */
		if (!dc) {
			alg = 540;	// MIPS
			jpopt.put("OPF_ALG", (double) alg);
		}

		/* data dimensions */
		nb = jpc.bus.size();					// number of buses
		nl = jpc.branch.size();					// number of branches
		ng = jpc.gen.size();					// number of dispatchable injections
		nusr = jpc.A != null ? A.rows() : 0;	// number of linear user constraints
		nw = jpc.N != null ? N.rows() : 0;		// number of general cost vars, w

		/* add zero columns to bus, gen, branch for multipliers, etc if needed */
		jpc.bus.mu_Vmax = DoubleFactory1D.dense.make(nb);
		jpc.bus.mu_Vmin = DoubleFactory1D.dense.make(nb);
		jpc.gen.mu_Pmax = DoubleFactory1D.dense.make(ng);
		jpc.gen.mu_Pmin = DoubleFactory1D.dense.make(ng);
		jpc.gen.mu_Qmax = DoubleFactory1D.dense.make(ng);
		jpc.gen.mu_Qmin = DoubleFactory1D.dense.make(ng);
		jpc.branch.mu_Sf = DoubleFactory1D.dense.make(nl);
		jpc.branch.mu_St = DoubleFactory1D.dense.make(nl);
		jpc.branch.mu_angmin = DoubleFactory1D.dense.make(nl);
		jpc.branch.mu_angmax = DoubleFactory1D.dense.make(nl);

		if (dc) {
			/* ignore reactive costs for DC */
			pqcost = pqcost(jpc.gencost, ng);
			jpc.gencost = pqcost[0];

			/* reduce A and/or N from AC dimensions to DC dimensions, if needed */
			if (nusr > 0 || nw > 0) {
				acc = icat(irange(nb, 2*nb), irange(2*nb+ng, 2*nb+2*ng));
				if (nusr > 0 && jpc.A.columns() >= 2*nb + 2*ng) {
					/* make sure there aren"t any constraints on Vm or Qg */
					if (any( any(jpc.A.viewSelection(null, acc)) ))
						System.err.println("opf: attempting to solve DC OPF with user constraints on Vm or Qg");
//					jpc.A = jpc.A.viewSelection(null, acc);	// delete Vm and Qg columns
				}
				if (nw > 0 && jpc.N.columns() >= 2*nb + 2*ng) {
					/* make sure there aren"t any costs on Vm or Qg */
					if (any( any(jpc.N.viewSelection(null, acc)) ))
						System.err.println("opf: attempting to solve DC OPF with user costs on Vm or Qg");
//					jpc.N = jpc.N.viewSelection(null, acc);	// delete Vm and Qg columns
				}
			}
		}

		/* convert single-block piecewise-linear costs into linear polynomial cost */
		p1 = nonzero( jpc.gencost.model.copy().assign(ifunc.equals(PW_LINEAR)).assign(jpc.gencost.ncost.copy().assign(ifunc.equals(2)), ifunc.and) );
		//p1 = new int[0];
		if (p1.length > 0) {
			x0 = jpc.gencost.cost.viewSelection(p1, null).viewColumn(0);
			y0 = jpc.gencost.cost.viewSelection(p1, null).viewColumn(1);
			x1 = jpc.gencost.cost.viewSelection(p1, null).viewColumn(2);
			y1 = jpc.gencost.cost.viewSelection(p1, null).viewColumn(3);
			m  = y1.assign(y0, dfunc.minus).assign(x1.assign(x0, dfunc.minus), dfunc.div);
			b  = y0.assign(x0.assign(m, dfunc.mult), dfunc.minus);
			jpc.gencost.model.viewSelection(p1).assign(POLYNOMIAL);
			jpc.gencost.ncost.viewSelection(p1).assign(2);
			jpc.gencost.cost.viewSelection(p1, null).viewColumn(0).assign(m);
			jpc.gencost.cost.viewSelection(p1, null).viewColumn(1).assign(b);
		}

		/* convert to internal numbering, remove out-of-service stuff */
		jpc = ext2int(jpc);

		/* update dimensions */
		nb = jpc.bus.size();			// number of buses
		nl = jpc.branch.size();			// number of branches
		ng = jpc.gen.size();			// number of dispatchable injections

		/* create (read-only) copies of individual fields for convenience */
		opf_args = opf_args(jpc, jpopt);
		baseMVA = (Double) opf_args[0];
		bus = (Bus) opf_args[1];
		gen = (Gen) opf_args[2];
		branch = (Branch) opf_args[3];
		gencost = (Cost) opf_args[4];
		Au  = (DoubleMatrix2D) opf_args[5];
		lbu = (DoubleMatrix1D) opf_args[6];
		ubu = (DoubleMatrix1D) opf_args[7];
		jpopt = (Map<String, Double>) opf_args[8];
		N = (DoubleMatrix2D) opf_args[9];
		fparm = (DoubleMatrix2D) opf_args[10];
		H  = (DoubleMatrix2D) opf_args[11];
		Cw = (DoubleMatrix1D) opf_args[12];
		z0 = (DoubleMatrix1D) opf_args[13];
		zl = (DoubleMatrix1D) opf_args[14];
		zu = (DoubleMatrix1D) opf_args[15];
		userfcn = (String) opf_args[16];

		/* warn if there is more than one reference bus */
		refs = nonzero(bus.bus_type.copy().assign(ifunc.equals(REF)));
		if (refs.length > 1 && verbose > 0)
			System.out.printf("\nopf: Warning: Multiple reference buses.\n" +
					"     For a system with islands, a reference bus in each island\n" +
					"     may help convergence, but in a fully connected system such\n" +
					"     a situation is probably not reasonable.\n\n");

		/* set up initial variables and bounds */
		Va = bus.Va.copy().assign(dfunc.mult(Math.PI)).assign(dfunc.div(180));
		Vm = bus.Vm.copy();
		Vm.viewSelection(gen.gen_bus.toArray()).assign(gen.Vg);	// buses with gens, init Vm from gen data
		Pg = gen.Pg.copy().assign(dfunc.div(baseMVA));
		Qg = gen.Qg.copy().assign(dfunc.div(baseMVA));
		Pmin = gen.Pmin.copy().assign(dfunc.div(baseMVA));
		Pmax = gen.Pmax.copy().assign(dfunc.div(baseMVA));
		Qmin = gen.Qmin.copy().assign(dfunc.div(baseMVA));
		Qmax = gen.Qmax.copy().assign(dfunc.div(baseMVA));

		if (dc) {		// DC model
			/* more problem dimensions */
			nv = 0;		// number of voltage magnitude vars
			nq = 0;		// number of Qg vars
			q1 = -1;	// index of 1st Qg column in Ay

			/* power mismatch constraints */
			Bdc = makeBdc(baseMVA, bus, branch);
			B = (DoubleMatrix2D) Bdc[0];
			Bf = (DoubleMatrix2D) Bdc[1];
			Pbusinj = (DoubleMatrix1D) Bdc[2];
			Pfinj = (DoubleMatrix1D) Bdc[3];
			neg_Cg = new SparseRCDoubleMatrix2D(nb, ng, gen.gen_bus.toArray(), irange(ng), -1, false, false);
			Amis = DoubleFactory2D.sparse.appendColumns(B, neg_Cg);
			bmis = bus.Pd.copy().assign(bus.Gs, dfunc.plus).assign(dfunc.neg).assign(dfunc.div(baseMVA)).assign(Pbusinj, dfunc.minus);

			/* branch flow constraints */
			il  = nonzero( intm(branch.rate_a.assign(dfunc.greater(0))).assign(intm(branch.rate_a.assign(dfunc.less(1e10))), ifunc.and) );
			nl2 = il.length;	// number of constrained lines
			lpf = DoubleFactory1D.dense.make(nl2, Double.NEGATIVE_INFINITY);
			upf = branch.rate_a.viewSelection(il).copy().assign(dfunc.div(baseMVA)).assign(Pfinj.viewSelection(il), dfunc.minus);
			upt = branch.rate_a.viewSelection(il).copy().assign(dfunc.div(baseMVA)).assign(Pfinj.viewSelection(il), dfunc.plus);

			user_vars = new String[] {"Va", "Pg"};
			ycon_vars = new String[] {"Pg", "y"};
		} else {		// AC model
			/* more problem dimensions */
			nv = nb;	// number of voltage magnitude vars
			nq = ng;	// number of Qg vars
			q1 = ng;	// index of 1st Qg column in Ay

			/* dispatchable load, constant power factor constraints */
			Alu_vl = makeAvl(baseMVA, gen);
			Avl = (DoubleMatrix2D) Alu_vl[0];
			lvl = (DoubleMatrix1D) Alu_vl[1];
			uvl = (DoubleMatrix1D) Alu_vl[2];

			/* generator PQ capability curve constraints */
			Alu_pq = makeApq(baseMVA, gen);
			Apqh  = (DoubleMatrix2D) Alu_pq[0];
			ubpqh = (DoubleMatrix1D) Alu_pq[1];
			Apql  = (DoubleMatrix2D) Alu_pq[2];
			ubpql = (DoubleMatrix1D) Alu_pq[3];
			Apqdata = (DoubleMatrix1D) Alu_pq[4];

			user_vars = new String[] {"Va", "Vm", "Pg", "Qg"};
			ycon_vars = new String[] {"Pg", "Qg", "y"};
		}

		/* voltage angle reference constraints */
		Vau = DoubleFactory1D.dense.make(nb, Double.POSITIVE_INFINITY);
		Val = DoubleFactory1D.dense.make(nb, Double.NEGATIVE_INFINITY);
		Vau.viewSelection(refs).assign(Va.viewSelection(refs));
		Val.viewSelection(refs).assign(Va.viewSelection(refs));

		/* branch voltage angle difference limits */
		Alu_ang = makeAang(baseMVA, branch, nb, jpopt);
		Aang = (DoubleMatrix2D) Alu_ang[0];
		lang = (DoubleMatrix1D) Alu_ang[1];
		uang = (DoubleMatrix1D) Alu_ang[2];
		iang = (DoubleMatrix1D) Alu_ang[3];

		/* basin constraints for piece-wise linear gen cost variables */
		if (alg == 545 || alg == 550) {		// SC-PDIPM or TRALM, no CCV cost vars
			ny = 0;
			Ay = DoubleFactory2D.sparse.make(0, ng+nq);
			by = DoubleFactory1D.dense.make(0);
		} else {
			int[] ipwl = nonzero( gencost.model.copy().assign(ifunc.equals(PW_LINEAR)) );	// piece-wise linear costs
			ny = ipwl.length;	// number of piece-wise linear cost vars
			AbstractMatrix[] Ab_y = makeAy(baseMVA, ng, gencost, 1, q1, ng+nq);
			Ay = (DoubleMatrix2D) Ab_y[0];
			by = (DoubleMatrix1D) Ab_y[1];
		}

		/* more problem dimensions */
		nx = nb+nv + ng+nq;	 // number of standard OPF control variables
		nz = 0;	 // number of user z variables
		if (nusr > 0) {
			nz = jpc.A.columns() - nx;
			if (nz < 0)
				System.err.println("opf: user supplied A matrix must have at least "+nx+" columns.");
				// TODO: throw invalid constraint exception
		} else {
			nz = 0;
			if (nw > 0) {  // still need to check number of columns of N
				if (jpc.N.columns() != nx)
					System.err.println("opf: user supplied N matrix must have "+nx+" columns.");
			}
		}

		/* construct OPF model object */
		om = new Djp_opf_model(jpc);
		if (dc) {
			om.userdata("Bf", Bf);
			om.userdata("Pfinj", Pfinj);
			om.add_vars("Va", nb, Va, Val, Vau);
			om.add_vars("Pg", ng, Pg, Pmin, Pmax);
			om.add_constraints("Pmis", Amis, bmis, bmis, new String[] {"Va", "Pg"});										// nb
			om.add_constraints("Pf",  Bf.viewSelection(il, null), lpf, upf, new String[] {"Va"});							// nl
			om.add_constraints("Pt", Bf.viewSelection(il, null).copy().assign(dfunc.neg), lpf, upt, new String[] {"Va"});	// nl
			om.add_constraints("ang", Aang, lang, uang, new String[] {"Va"});												// nang
		} else {
			om.add_vars("Va", nb, Va, Val, Vau);
			om.add_vars("Vm", nb, Vm, bus.Vmin, bus.Vmax);
			om.add_vars("Pg", ng, Pg, Pmin, Pmax);
			om.add_vars("Qg", ng, Qg, Qmin, Qmax);
			om.add_constraints("Pmis", nb, "non-linear");
			om.add_constraints("Qmis", nb, "non-linear");
			om.add_constraints("Sf", nl, "non-linear");
			om.add_constraints("St", nl, "non-linear");
			om.add_constraints("PQh", Apqh, null, ubpqh, new String[] {"Pg", "Qg"});	// npqh
			om.add_constraints("PQl", Apql, null, ubpql, new String[] {"Pg", "Qg"});	// npql
			om.add_constraints("vl",  Avl, lvl, uvl,   new String[] {"Pg", "Qg"});		// nvl
			om.add_constraints("ang", Aang, lang, uang, new String[] {"Va"});			// nang
		}

		/* y vars, constraints for piece-wise linear gen costs */
		if (ny > 0) {
			om.add_vars("y", ny);
			om.add_constraints("ycon", Ay, null, by, ycon_vars);	// ncony
		}

		/* add user vars, constraints and costs (as specified via A, ..., N, ...) */
		if (nz > 0) {
			om.add_vars("z", nz, z0, zl, zu);
			user_vars = scat(user_vars, new String[] {"z"});
		}
		if (nusr > 0)
			om.add_constraints("usr", jpc.A, lbu, ubu, user_vars);	// nusr
		if (nw > 0) {
			user_cost = om.new CostParams();
			user_cost.N = jpc.N;
			user_cost.Cw = Cw;
			if (fparm.size() > 0) {
				user_cost.dd = fparm.viewColumn(0);
				user_cost.rh = fparm.viewColumn(1);
				user_cost.kk = fparm.viewColumn(2);
				user_cost.mm = fparm.viewColumn(3);
			}
			if (H.size() > 0)
				user_cost.H = H;
			om.add_costs("usr", user_cost, user_vars);
		}

		/* execute userfcn callbacks for 'formulation' stage */
		om = run_userfcn(userfcn, "formulation", om);

		/* build user-defined costs */
		om = om.build_cost_params();

		/* get indexing */
		idx = om.get_idx();
		vv = idx[0]; ll = idx[1]; nn = idx[2];

		/* select optional solver output args */
		output = new HashMap<String, AbstractMatrix>();

		/* call the specific solver */
		if (verbose > 0) {
			v = jpver("all");
			System.out.printf("\nMATPOWER Version %s, %s", v.get("Version"), v.get("Date"));
		}
		if (dc) {
			if (verbose > 0)
				System.out.printf(" -- DC Optimal Power Flow\n");
			r = dcopf_solver(om, jpopt, output);
			results = (Map<String, AbstractMatrix>) r[0];
			success = (Boolean) r[1];
			raw = (Map<String, AbstractMatrix>) r[2];
		} else {
			/* -----  call specific AC OPF solver  ----- */
			if (verbose > 0)
				System.out.printf(" -- AC Optimal Power Flow\n");
			if (alg != 560 || alg != 565)
				System.err.println("Unsupported algorithm, using JIPS.");

			r = jipsopf_solver(om, jpopt, output);
			results = (Map<String, AbstractMatrix>) r[0];
			success = (Boolean) r[1];
			raw = (Map<String, AbstractMatrix>) r[2];
		}
		if (!raw.containsKey("output")) {

		}

		return null;
	}

	public static JPC opf(JPC jpc, Map<String, Double> jpopt) {
		return null;
	}

}
