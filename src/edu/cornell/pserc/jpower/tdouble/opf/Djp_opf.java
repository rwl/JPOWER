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

import cern.colt.matrix.AbstractMatrix;
import cern.colt.matrix.tdouble.DoubleFactory1D;
import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.SparseRCDoubleMatrix2D;
import cern.jet.math.tdouble.DoubleFunctions;
import cern.jet.math.tint.IntFunctions;

import edu.cornell.pserc.jpower.tdouble.Djp_ext2int;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_branch;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_bus;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_gen;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_gencost;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_jpc;
import edu.cornell.pserc.jpower.tdouble.pf.Djp_makeBdc;
import edu.cornell.pserc.util.tdouble.Djp_util;

/**
 * Solves an optimal power flow.
 *
 * @author Ray Zimmerman (rz10@cornell.edu)
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class Djp_opf {

	private static final Djp_util util = new Djp_util();
	private static final DoubleFunctions dfunc = DoubleFunctions.functions;
	private static final IntFunctions ifunc = IntFunctions.intFunctions;

	private static final int PW_LINEAR = Djp_jpc.PW_LINEAR;
	private static final int POLYNOMIAL = Djp_jpc.POLYNOMIAL;

	private static final int REF = Djp_jpc.REF;

	public static Djp_jpc jp_opf(Djp_jpc jpc, Map<String, Double> jpopt) {
		return null;
	}

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
	public static Djp_jpc jp_opf(Djp_jpc jpc, DoubleMatrix2D A, DoubleMatrix1D l, DoubleMatrix1D u,
			Map<String, Double> jpopt, DoubleMatrix2D N, DoubleMatrix2D fparm, DoubleMatrix2D H,
			DoubleMatrix1D Cw, DoubleMatrix1D z0, DoubleMatrix1D zl, DoubleMatrix1D zu) {

		/* ----- initialization ----- */

		long t0 = System.currentTimeMillis();	// start timer

		/* process input arguments */
		Object[] opf_args = Djp_opf_args.jp_opf_args();
		jpc = (Djp_jpc) opf_args[0];
		jpopt = (Map<String, Double>) opf_args[1];

		/* options */
		boolean dc = jpopt.get("PF_DC") == 1;	// true = DC OPF, false = AC OPF
		int alg = jpopt.get("OPF_ALG").intValue();
		int verbose = jpopt.get("VERBOSE").intValue();

		/* set AC OPF algorithm code */
		if (!dc) {
			alg = 540;	// MIPS
			jpopt.put("OPF_ALG", (double) alg);
		}

		/* data dimensions */
		int nb = jpc.bus.size();					// number of buses
		int nl = jpc.branch.size();					// number of branches
		int ng = jpc.gen.size();					// number of dispatchable injections
		int nusr = jpc.A != null ? A.rows() : 0;	// number of linear user constraints
		int nw = jpc.N != null ? N.rows() : 0;		// number of general cost vars, w

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
			Djp_gencost[] pqcost = Djp_pqcost.jp_pqcost(jpc.gencost, ng);
			jpc.gencost = pqcost[0];

			/* reduce A and/or N from AC dimensions to DC dimensions, if needed */
			if (nusr > 0 || nw > 0) {
				int[] acc = util.icat(util.irange(nb, 2*nb), util.irange(2*nb+ng, 2*nb+2*ng));
				if (nusr > 0 && jpc.A.columns() >= 2*nb + 2*ng) {
					/* make sure there aren't any constraints on Vm or Qg */
					if (util.any(util.any(jpc.A.viewSelection(null, acc))))
						System.err.println("opf: attempting to solve DC OPF with user constraints on Vm or Qg");
//					jpc.A = jpc.A.viewSelection(null, acc);	// delete Vm and Qg columns
				}
				if (nw > 0 && jpc.N.columns() >= 2*nb + 2*ng) {
					/* make sure there aren't any costs on Vm or Qg */
					if (util.any(util.any(jpc.N.viewSelection(null, acc))))
						System.err.println("opf: attempting to solve DC OPF with user costs on Vm or Qg");
//					jpc.N = jpc.N.viewSelection(null, acc);	// delete Vm and Qg columns
				}
			}
		}

		/* convert single-block piecewise-linear costs into linear polynomial cost */
		int[] p1 = util.nonzero( jpc.gencost.model.copy().assign(ifunc.equals(PW_LINEAR)).assign(jpc.gencost.ncost.copy().assign(ifunc.equals(2)), ifunc.and) );
		//p1 = new int[0];
		if (p1.length > 0) {
			DoubleMatrix1D x0 = jpc.gencost.cost.viewSelection(p1, null).viewColumn(0);
			DoubleMatrix1D y0 = jpc.gencost.cost.viewSelection(p1, null).viewColumn(1);
			DoubleMatrix1D x1 = jpc.gencost.cost.viewSelection(p1, null).viewColumn(2);
			DoubleMatrix1D y1 = jpc.gencost.cost.viewSelection(p1, null).viewColumn(3);
			DoubleMatrix1D m = y1.assign(y0, dfunc.minus).assign(x1.assign(x0, dfunc.minus), dfunc.div);
			DoubleMatrix1D b = y0.assign(x0.assign(m, dfunc.mult), dfunc.minus);
			jpc.gencost.model.viewSelection(p1).assign(POLYNOMIAL);
			jpc.gencost.ncost.viewSelection(p1).assign(2);
			jpc.gencost.cost.viewSelection(p1, null).viewColumn(0).assign(m);
			jpc.gencost.cost.viewSelection(p1, null).viewColumn(1).assign(b);
		}

		/* convert to internal numbering, remove out-of-service stuff */
		jpc = Djp_ext2int.jp_ext2int(jpc);

		/* update dimensions */
		nb = jpc.bus.size();			// number of buses
		nl = jpc.branch.size();			// number of branches
		ng = jpc.gen.size();			// number of dispatchable injections

		/* create (read-only) copies of individual fields for convenience */
		opf_args = Djp_opf_args.jp_opf_args(jpc, jpopt);
		double baseMVA = (Double) opf_args[0];
		Djp_bus bus = (Djp_bus) opf_args[1];
		Djp_gen gen = (Djp_gen) opf_args[2];
		Djp_branch branch = (Djp_branch) opf_args[3];
		Djp_gencost gencost = (Djp_gencost) opf_args[4];
		DoubleMatrix2D Au = (DoubleMatrix2D) opf_args[5];
		DoubleMatrix1D lbu = (DoubleMatrix1D) opf_args[6];
		DoubleMatrix1D ubu = (DoubleMatrix1D) opf_args[7];
		Map<String, Double> mpopt = (Map<String, Double>) opf_args[8];
		N = (DoubleMatrix2D) opf_args[9];
		fparm = (DoubleMatrix2D) opf_args[10];
		H = (DoubleMatrix2D) opf_args[11];
		Cw = (DoubleMatrix1D) opf_args[12];
		z0 = (DoubleMatrix1D) opf_args[13];
		zl = (DoubleMatrix1D) opf_args[14];
		zu = (DoubleMatrix1D) opf_args[15];
		String userfcn = (String) opf_args[16];

		/* warn if there is more than one reference bus */
		int[] refs = util.nonzero(bus.bus_type.copy().assign(ifunc.equals(REF)));
		if (refs.length > 1 && verbose > 0)
			System.out.printf("\nopf: Warning: Multiple reference buses.\n" +
					"     For a system with islands, a reference bus in each island\n" +
					"     may help convergence, but in a fully connected system such\n" +
					"     a situation is probably not reasonable.\n\n");

		/* set up initial variables and bounds */
		DoubleMatrix1D Va = bus.Va.copy().assign(dfunc.mult(Math.PI)).assign(dfunc.div(180));
		DoubleMatrix1D Vm = bus.Vm.copy();
		Vm.viewSelection(gen.gen_bus.toArray()).assign(gen.Vg);	// buses with gens, init Vm from gen data
		DoubleMatrix1D Pg = gen.Pg.copy().assign(dfunc.div(baseMVA));
		DoubleMatrix1D Qg = gen.Qg.copy().assign(dfunc.div(baseMVA));
		DoubleMatrix1D Pmin = gen.Pmin.copy().assign(dfunc.div(baseMVA));
		DoubleMatrix1D Pmax = gen.Pmax.copy().assign(dfunc.div(baseMVA));
		DoubleMatrix1D Qmin = gen.Qmin.copy().assign(dfunc.div(baseMVA));
		DoubleMatrix1D Qmax = gen.Qmax.copy().assign(dfunc.div(baseMVA));

		int nv, nq, q1;
		if (dc) {		// DC model
			/* more problem dimensions */
			nv = 0;		// number of voltage magnitude vars
			nq = 0;		// number of Qg vars
//			q1 = null;	// index of 1st Qg column in Ay

			/* power mismatch constraints */
			AbstractMatrix[] Bdc = Djp_makeBdc.jp_makeBdc(baseMVA, bus, branch);
			DoubleMatrix2D B = (DoubleMatrix2D) Bdc[0];
			DoubleMatrix2D Bf = (DoubleMatrix2D) Bdc[1];
			DoubleMatrix1D Pbusinj = (DoubleMatrix1D) Bdc[2];
			DoubleMatrix1D Pfinj = (DoubleMatrix1D) Bdc[3];
			DoubleMatrix2D neg_Cg = new SparseRCDoubleMatrix2D(nb, ng, gen.gen_bus.toArray(), util.irange(ng), -1, false, false);
			DoubleMatrix2D Amis = DoubleFactory2D.sparse.appendColumns(B, neg_Cg);
			DoubleMatrix1D bmis = bus.Pd.copy().assign(bus.Gs, dfunc.plus).assign(dfunc.neg).assign(dfunc.div(baseMVA)).assign(Pbusinj, dfunc.minus);

			/* branch flow constraints */
		}

		return null;
	}
}
