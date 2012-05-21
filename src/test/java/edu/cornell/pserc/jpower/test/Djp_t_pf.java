/*
 * Copyright (C) 1996-2010 Power System Engineering Research Center
 * Copyright (C) 2010-2011 Richard Lincoln
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package edu.cornell.pserc.jpower.test;

import java.io.InputStream;
import java.io.ObjectInputStream.GetField;
import java.net.URL;
import java.util.Map;

import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;

import static edu.emory.mathcs.utils.MatrixMarketUtils.readMatrix;

import edu.cornell.pserc.jpower.Djp_jpoption;
import edu.cornell.pserc.jpower.Djp_loadcase;
import edu.cornell.pserc.jpower.jpc.Branch;
import edu.cornell.pserc.jpower.jpc.Bus;
import edu.cornell.pserc.jpower.jpc.Gen;
import edu.cornell.pserc.jpower.jpc.JPC;
import edu.cornell.pserc.jpower.pf.Djp_rundcpf;
import edu.cornell.pserc.jpower.pf.Djp_runpf;

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
	public static void t_pf(boolean quiet) {

		String t;
		JPC jpc, r;
		DoubleMatrix2D bus_soln, gen_soln, branch_soln;
		Map<String, Double> jpopt;

		Djp_t_begin.t_begin(25, quiet);

		JPC casefile = Djp_t_case9_pf.t_case9_pf();
		jpopt = Djp_jpoption.jpoption("OUT_ALL", 0.0, "VERBOSE", quiet ? 0.0 : 1.0);

		/* get solved AC power flow case from MatrixMarket file. */
		bus_soln = (DoubleMatrix2D) readMatrix(Djp_t_pf.class.getResource(BUS_SOLN9).getFile());
		gen_soln = (DoubleMatrix2D) readMatrix(Djp_t_pf.class.getResource(GEN_SOLN9).getFile());
		branch_soln = (DoubleMatrix2D) readMatrix(Djp_t_pf.class.getResource(BRANCH_SOLN9).getFile());

		/* run Newton PF */
		t = "Newton PF : ";
		jpopt = Djp_jpoption.jpoption(jpopt, "PF_ALG", 1.0);
		jpc = Djp_runpf.runpf(casefile.copy(), jpopt);
		Djp_t_ok.t_ok(jpc.success, t + "success");
		Djp_t_is.t_is(jpc.bus.toMatrix(), bus_soln, 6, t + "bus");
		Djp_t_is.t_is(jpc.gen.toMatrix(), gen_soln, 6, t + "gen");
		Djp_t_is.t_is(jpc.branch.toMatrix(), branch_soln, 6, t + "branch");

		/* run fast-decoupled PF (XB version) */
		t = "Fast Decoupled (XB) PF : ";
		jpopt = Djp_jpoption.jpoption(jpopt, "PF_ALG", 2.0);
		jpc = Djp_runpf.runpf(casefile.copy(), jpopt);
		Djp_t_ok.t_ok(jpc.success, t + "success");
		Djp_t_is.t_is(jpc.bus.toMatrix(), bus_soln, 6, t + "bus");
		Djp_t_is.t_is(jpc.gen.toMatrix(), gen_soln, 6, t + "gen");
		Djp_t_is.t_is(jpc.branch.toMatrix(), branch_soln, 6, t + "branch");

		/* run fast-decoupled PF (BX version) */
		t = "Fast Decoupled (BX) PF : ";
		jpopt = Djp_jpoption.jpoption(jpopt, "PF_ALG", 3.0);
		jpc = Djp_runpf.runpf(casefile.copy(), jpopt);
		Djp_t_ok.t_ok(jpc.success, t + "success");
		Djp_t_is.t_is(jpc.bus.toMatrix(), bus_soln, 6, t + "bus");
		Djp_t_is.t_is(jpc.gen.toMatrix(), gen_soln, 6, t + "gen");
		Djp_t_is.t_is(jpc.branch.toMatrix(), branch_soln, 6, t + "branch");

//		/* run Gauss-Seidel PF */
//		t = "Gauss-Seidel PF : ";
//		jpopt = Djp_jpoption.jp_jpoption(jpopt, "PF_ALG", 4.0);
//		jpc = Djp_runpf.jp_runpf(casefile, jpopt);
//		Djp_t_ok.jp_t_ok(jpc.success, t + "success");
//		Djp_t_is.jp_t_is(jpc.bus.toMatrix(), bus_soln, 6, t + "bus");
//		Djp_t_is.jp_t_is(jpc.gen.toMatrix(), gen_soln, 6, t + "gen");
//		Djp_t_is.jp_t_is(jpc.branch.toMatrix(), branch_soln, 6, t + "branch");
//
//		/* get solved DC power flow case from MAT-file */
//		bus_soln = (DoubleMatrix2D) Djp_mm.readMatrix(BUS_SOLN9_DC);
//		gen_soln = (DoubleMatrix2D) Djp_mm.readMatrix(GEN_SOLN9_DC);
//		branch_soln = (DoubleMatrix2D) Djp_mm.readMatrix(BRANCH_SOLN9_DC);
//
//		/* run DC PF */
//		t = "DC PF : ";
//		jpc = Djp_rundcpf.jp_rundcpf(casefile, jpopt);
//		Djp_t_ok.jp_t_ok(jpc.success, t + "success");
//		Djp_t_is.jp_t_is(jpc.bus.toMatrix(), bus_soln, 6, t + "bus");
//		Djp_t_is.jp_t_is(jpc.gen.toMatrix(), gen_soln, 6, t + "gen");
//		Djp_t_is.jp_t_is(jpc.branch.toMatrix(), branch_soln, 6, t + "branch");

		/* check Qg distribution, when Qmin = Qmax */
		t = "check Qg : ";
		jpopt = Djp_jpoption.jpoption(jpopt, "PF_ALG", 1.0, "VERBOSE", 0.0);
		jpc = Djp_loadcase.loadcase(casefile.copy());
		jpc.gen.Qmin.set(0, 20);
		jpc.gen.Qmax.set(0, 20);
		r = Djp_runpf.runpf(jpc, jpopt);
		Djp_t_is.t_is(r.gen.Qg.get(0), 24.07, 2, t + "single gen, Qmin = Qmax");

		jpc.gen = Gen.fromMatrix( DoubleFactory2D.dense.appendRows(
			jpc.gen.toMatrix().viewSelection(new int[] {0}, null),
			jpc.gen.toMatrix()
		) );
		jpc.gen.Qmin.set(0, 10);
		jpc.gen.Qmax.set(0, 10);
		jpc.gen.Qmin.set(1, 0);
		jpc.gen.Qmax.set(1, 50);
		r = Djp_runpf.runpf(jpc, jpopt);
		Djp_t_is.t_is(r.gen.Qg.viewSelection(new int[] {0, 1}),
				new double[] {10, 14.07}, 2,
				t + "2 gens, Qmin = Qmax for one");

		jpc.gen.Qmin.set(0, 10);
		jpc.gen.Qmax.set(0, 10);
		jpc.gen.Qmin.set(1, -50);
		jpc.gen.Qmax.set(1, -50);
		r = Djp_runpf.runpf(jpc, jpopt);
		Djp_t_is.t_is(r.gen.Qg.viewSelection(new int[] {0, 1}),
				new double[] {12.03, 12.03}, 2,
				t + "2 gens, Qmin = Qmax for both");

		jpc.gen.Qmin.set(0, 0);
		jpc.gen.Qmax.set(0, 50);
		jpc.gen.Qmin.set(1, 0);
		jpc.gen.Qmax.set(1, 100);
		r = Djp_runpf.runpf(jpc, jpopt);
		Djp_t_is.t_is(r.gen.Qg.viewSelection(new int[] {0, 1}),
				new double[] {8.02, 16.05}, 2,
				t + "2 gens, proportional");

		jpc.gen.Qmin.set(0, -50);
		jpc.gen.Qmax.set(0, 0);
		jpc.gen.Qmin.set(1, 50);
		jpc.gen.Qmax.set(1, 150);
		r = Djp_runpf.runpf(jpc, jpopt);
		Djp_t_is.t_is(r.gen.Qg.viewSelection(new int[] {0, 1}),
				new double[] {-50+8.02, 50+16.05}, 2,
				t + "2 gens, proportional");

		Djp_t_end.t_end();
	}

	public static void main(String[] args) {
		t_pf(false);
	}

}
