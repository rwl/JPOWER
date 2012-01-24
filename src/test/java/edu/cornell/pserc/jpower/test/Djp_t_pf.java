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

package edu.cornell.pserc.jpower.test;

import java.util.Map;

import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;

import static edu.cornell.pserc.jpower.Djp_jpoption.jpoption;
import static edu.cornell.pserc.jpower.Djp_loadcase.loadcase;
import static edu.cornell.pserc.jpower.pf.Djp_runpf.runpf;
import static edu.cornell.pserc.jpower.pf.Djp_rundcpf.rundcpf;

import edu.cornell.pserc.jpower.jpc.Branch;
import edu.cornell.pserc.jpower.jpc.Bus;
import edu.cornell.pserc.jpower.jpc.Gen;
import edu.cornell.pserc.jpower.jpc.JPC;

import static edu.cornell.pserc.jpower.jpc.Bus.VA;
import static edu.cornell.pserc.jpower.jpc.Gen.PG;

import static edu.cornell.pserc.jpower.test.Djp_t_begin.t_begin;
import static edu.cornell.pserc.jpower.test.Djp_t_end.t_end;
import static edu.cornell.pserc.jpower.test.Djp_t_is.t_is;
import static edu.cornell.pserc.jpower.test.Djp_t_ok.t_ok;
import static edu.cornell.pserc.jpower.test.Djp_t_case9_pf.t_case9_pf;

import static cern.colt.util.tdouble.MMUtil.readMatrix;
import static cern.colt.util.tdouble.Util.irange;
import static cern.colt.util.tdouble.Util.ifunc;

/**
 * Tests for power flow solvers.
 *
 * @author Ray Zimmerman
 * @author Richard Lincoln
 *
 */
public class Djp_t_pf {

	private static final String BUS_SOLN9 = "soln9_pf/bus_soln.mtx";
	private static final String GEN_SOLN9 = "soln9_pf/gen_soln.mtx";
	private static final String BRANCH_SOLN9 = "soln9_pf/branch_soln.mtx";

	private static final String BUS_SOLN9_DC = "soln9_dcpf/bus_soln.mtx";
	private static final String GEN_SOLN9_DC = "soln9_dcpf/gen_soln.mtx";
	private static final String BRANCH_SOLN9_DC = "soln9_dcpf/branch_soln.mtx";

	public static void t_pf() {
		t_pf(false);
	}

	/**
	 * Tests for power flow solvers.
	 *
	 * @param quiet
	 */
	@SuppressWarnings("static-access")
	public static void t_pf(boolean quiet) {

		String t;
		int nb;
		double verbose;
		double[] Pg;
		JPC jpc, jpc0, jpc1, r;
		DoubleMatrix2D bus_soln, gen_soln, branch_soln;
		Map<String, Double> jpopt;

		t_begin(33, quiet);

		JPC casefile = t_case9_pf();
		verbose = quiet ? 0.0 : 1.0;
		jpopt = jpoption("OUT_ALL", 0.0, "VERBOSE", verbose);

		/* get solved AC power flow case from MatrixMarket file. */
		bus_soln = (DoubleMatrix2D) readMatrix(Djp_t_pf.class.getResource(BUS_SOLN9).getFile());
		gen_soln = (DoubleMatrix2D) readMatrix(Djp_t_pf.class.getResource(GEN_SOLN9).getFile());
		branch_soln = (DoubleMatrix2D) readMatrix(Djp_t_pf.class.getResource(BRANCH_SOLN9).getFile());

		/* run Newton PF */
		t = "Newton PF : ";
		jpopt = jpoption(jpopt, "PF_ALG", 1.0);
		jpc = runpf(casefile.copy(), jpopt);
		t_ok(jpc.success, t + "success");
		t_is(jpc.bus.toMatrix(), bus_soln, 6, t + "bus");
		t_is(jpc.gen.toMatrix(), gen_soln, 6, t + "gen");
		t_is(jpc.branch.toMatrix(), branch_soln, 6, t + "branch");

		/* run fast-decoupled PF (XB version) */
		t = "Fast Decoupled (XB) PF : ";
		jpopt = jpoption(jpopt, "PF_ALG", 2.0);
		jpc = runpf(casefile.copy(), jpopt);
		t_ok(jpc.success, t + "success");
		t_is(jpc.bus.toMatrix(), bus_soln, 6, t + "bus");
		t_is(jpc.gen.toMatrix(), gen_soln, 6, t + "gen");
		t_is(jpc.branch.toMatrix(), branch_soln, 6, t + "branch");

		/* run fast-decoupled PF (BX version) */
		t = "Fast Decoupled (BX) PF : ";
		jpopt = jpoption(jpopt, "PF_ALG", 3.0);
		jpc = runpf(casefile.copy(), jpopt);
		t_ok(jpc.success, t + "success");
		t_is(jpc.bus.toMatrix(), bus_soln, 6, t + "bus");
		t_is(jpc.gen.toMatrix(), gen_soln, 6, t + "gen");
		t_is(jpc.branch.toMatrix(), branch_soln, 6, t + "branch");

//		/* run Gauss-Seidel PF */
//		t = "Gauss-Seidel PF : ";
//		jpopt = jpoption(jpopt, "PF_ALG", 4.0);
//		jpc = runpf(casefile, jpopt);
//		t_ok(jpc.success, t + "success");
//		t_is(jpc.bus.toMatrix(), bus_soln, 6, t + "bus");
//		t_is(jpc.gen.toMatrix(), gen_soln, 6, t + "gen");
//		t_is(jpc.branch.toMatrix(), branch_soln, 6, t + "branch");
//
//		/* get solved DC power flow case from MAT-file */
//		bus_soln = (DoubleMatrix2D) Djp_mm.readMatrix(BUS_SOLN9_DC);
//		gen_soln = (DoubleMatrix2D) Djp_mm.readMatrix(GEN_SOLN9_DC);
//		branch_soln = (DoubleMatrix2D) Djp_mm.readMatrix(BRANCH_SOLN9_DC);
//
//		/* run DC PF */
//		t = "DC PF : ";
//		jpc = rundcpf(casefile, jpopt);
//		t_ok(jpc.success, t + "success");
//		t_is(jpc.bus.toMatrix(), bus_soln, 6, t + "bus");
//		t_is(jpc.gen.toMatrix(), gen_soln, 6, t + "gen");
//		t_is(jpc.branch.toMatrix(), branch_soln, 6, t + "branch");

		/* check Qg distribution, when Qmin = Qmax */
		t = "check Qg : ";
		jpopt = jpoption(jpopt, "PF_ALG", 1.0, "VERBOSE", 0.0);
		jpc = loadcase(casefile.copy());
		jpc.gen.Qmin.set(0, 20);
		jpc.gen.Qmax.set(0, 20);
		r = runpf(jpc, jpopt);
		t_is(r.gen.Qg.get(0), 24.07, 2, t + "single gen, Qmin = Qmax");

		jpc.gen = Gen.fromMatrix( DoubleFactory2D.dense.appendRows(
			jpc.gen.toMatrix().viewSelection(new int[] {0}, null),
			jpc.gen.toMatrix()
		) );
		jpc.gen.Qmin.set(0, 10);
		jpc.gen.Qmax.set(0, 10);
		jpc.gen.Qmin.set(1, 0);
		jpc.gen.Qmax.set(1, 50);
		r = runpf(jpc, jpopt);
		t_is(r.gen.Qg.viewSelection(new int[] {0, 1}),
				new double[] {10, 14.07}, 2,
				t + "2 gens, Qmin = Qmax for one");

		jpc.gen.Qmin.set(0, 10);
		jpc.gen.Qmax.set(0, 10);
		jpc.gen.Qmin.set(1, -50);
		jpc.gen.Qmax.set(1, -50);
		r = runpf(jpc, jpopt);
		t_is(r.gen.Qg.viewSelection(new int[] {0, 1}),
				new double[] {12.03, 12.03}, 2,
				t + "2 gens, Qmin = Qmax for both");

		jpc.gen.Qmin.set(0, 0);
		jpc.gen.Qmax.set(0, 50);
		jpc.gen.Qmin.set(1, 0);
		jpc.gen.Qmax.set(1, 100);
		r = runpf(jpc, jpopt);
		t_is(r.gen.Qg.viewSelection(new int[] {0, 1}),
				new double[] {8.02, 16.05}, 2,
				t + "2 gens, proportional");

		jpc.gen.Qmin.set(0, -50);
		jpc.gen.Qmax.set(0, 0);
		jpc.gen.Qmin.set(1, 50);
		jpc.gen.Qmax.set(1, 150);
		r = runpf(jpc, jpopt);
		t_is(r.gen.Qg.viewSelection(new int[] {0, 1}),
				new double[] {-50+8.02, 50+16.05}, 2,
				t + "2 gens, proportional");


		/* Network with islands */
		t = "network w/islands : DC PF : ";
		jpc0 = loadcase(casefile);
		jpc0.gen.Pg.set(0, 60);
		jpc0.gen.Pmin.set(0, jpc0.gen.Pmin.get(0) / 2);
		jpc0.gen.Pmax.set(0, jpc0.gen.Pmax.get(0) / 2);
		jpc0.gen.Qmin.set(0, jpc0.gen.Qmin.get(0) / 2);
		jpc0.gen.Qmax.set(0, jpc0.gen.Qmax.get(0) / 2);
		jpc0.gen.Pg.set(0, jpc0.gen.Pg.get(0) / 2);
		jpc0.gen.Qg.set(0, jpc0.gen.Qg.get(0) / 2);

		jpc0.gen = Gen.fromMatrix(DoubleFactory2D.dense.appendRows(
			jpc0.gen.toMatrix().viewSelection(new int[] {0}, null),
			jpc0.gen.toMatrix())
		);
		jpc1 = jpc0.copy();
		jpc  = jpc0.copy();
		nb = jpc.bus.size();
		jpc1.bus.bus_i.assign(ifunc.plus(nb));
		jpc1.branch.f_bus.assign(ifunc.plus(nb));
		jpc1.branch.t_bus.assign(ifunc.plus(nb));
		jpc1.gen.gen_bus.assign(ifunc.plus(nb));
		jpc.bus = Bus.fromMatrix(
			DoubleFactory2D.dense.appendRows(jpc.bus.toMatrix(), jpc1.bus.toMatrix()));
		jpc.branch = Branch.fromMatrix(
			DoubleFactory2D.dense.appendRows(jpc.branch.toMatrix(), jpc1.branch.toMatrix()));
		jpc.gen = Gen.fromMatrix(
			DoubleFactory2D.dense.appendRows(jpc.gen.toMatrix(), jpc1.gen.toMatrix()));
		//jpopt = jpoption(jpopt, "OUT_BUS", 1, "OUT_GEN", 1, "OUT_ALL", -1, "VERBOSE", 2);
		jpopt = jpoption(jpopt, "VERBOSE", verbose);
		r = rundcpf(jpc, jpopt);
		t_is(r.bus.Va.viewSelection(irange(9)), bus_soln.viewColumn(VA),
				8, t + "voltage angles 1");
		t_is(r.bus.Va.viewSelection(irange(9, 18)), bus_soln.viewColumn(VA),
				8, t + "voltage angles 2");
		Pg = new double[] {gen_soln.get(0, PG) - 30, 30, gen_soln.get(1, PG), gen_soln.get(2, PG)};
		t_is(r.gen.Pg.viewSelection(irange(4)), Pg, 8, t + "active power generation 1");
		t_is(r.gen.Pg.viewSelection(irange(4, 8)), Pg, 8, t + "active power generation 1");

		t = "network w/islands : AC PF : ";
		/* get solved AC power flow case from MM-files */
		bus_soln = (DoubleMatrix2D) readMatrix(Djp_t_pf.class.getResource(BUS_SOLN9).getFile());
		gen_soln = (DoubleMatrix2D) readMatrix(Djp_t_pf.class.getResource(GEN_SOLN9).getFile());
		branch_soln = (DoubleMatrix2D) readMatrix(Djp_t_pf.class.getResource(BRANCH_SOLN9).getFile());
		r = runpf(jpc, jpopt);
		t_is(r.bus.Va.viewSelection(irange(9)), bus_soln.viewColumn(VA), 8, t + "voltage angles 1");
		t_is(r.bus.Va.viewSelection(irange(9, 18)), bus_soln.viewColumn(VA), 8, t + "voltage angles 2");
		Pg = new double[] {gen_soln.get(0, PG) - 30, 30, gen_soln.get(1, PG), gen_soln.get(2, PG)};
		t_is(r.gen.Pg.viewSelection(irange(4)), Pg, 8, t + "active power generation 1");
		t_is(r.gen.Pg.viewSelection(irange(4, 8)), Pg, 8, t + "active power generation 1");

		t_end();
	}

	public static void main(String[] args) {
		t_pf(false);
	}

}
