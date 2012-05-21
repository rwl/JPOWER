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

package edu.cornell.pserc.jpower;

import cern.colt.matrix.tdouble.DoubleFactory1D;

import edu.cornell.pserc.jpower.jpc.Branch;
import edu.cornell.pserc.jpower.jpc.Gen;
import edu.cornell.pserc.jpower.jpc.JPC;

/**
 * Load JPOWER case data.
 */
public class Djp_loadcase {

	public static JPC loadcase(String casefile) {
		// TODO Implement this method
		throw new UnsupportedOperationException();
	}

	public static JPC loadcase(JPC jpc) {
		jpc = jpc.copy();

		// TODO: -----  check contents of case  -----

		if (jpc.version.equals("1")) {
			jpc.gen = jpc_1to2(jpc.gen);
			jpc.branch = jpc_1to2(jpc.branch);
		}

		return jpc;
	}

	private static Gen jpc_1to2(Gen gen) {
		gen = gen.copy();
		int ng = gen.size();

		gen.Pc1 = DoubleFactory1D.dense.make(ng);
		gen.Pc2 = DoubleFactory1D.dense.make(ng);
		gen.Qc1min = DoubleFactory1D.dense.make(ng);
		gen.Qc1max = DoubleFactory1D.dense.make(ng);
		gen.Qc2min = DoubleFactory1D.dense.make(ng);
		gen.Qc2max = DoubleFactory1D.dense.make(ng);
		gen.ramp_agc = DoubleFactory1D.dense.make(ng);
		gen.ramp_10 = DoubleFactory1D.dense.make(ng);
		gen.ramp_30 = DoubleFactory1D.dense.make(ng);
		gen.ramp_q = DoubleFactory1D.dense.make(ng);
		gen.apf = DoubleFactory1D.dense.make(ng);

		if (gen.mu_Pmax != null)
			gen.mu_Pmax = DoubleFactory1D.dense.make(ng);
		if (gen.mu_Pmin != null)
			gen.mu_Pmin = DoubleFactory1D.dense.make(ng);
		if (gen.mu_Qmax != null)
			gen.mu_Qmax = DoubleFactory1D.dense.make(ng);
		if (gen.mu_Qmin != null)
			gen.mu_Qmin = DoubleFactory1D.dense.make(ng);

		return gen;
	}

	private static Branch jpc_1to2(Branch branch) {
		branch = branch.copy();
		int nl = branch.size();

		branch.ang_min = DoubleFactory1D.dense.make(nl, -360);
		branch.ang_max = DoubleFactory1D.dense.make(nl,  360);

		if (branch.Pf != null)
			branch.Pf = DoubleFactory1D.dense.make(nl);
		if (branch.Qf != null)
			branch.Qf = DoubleFactory1D.dense.make(nl);
		if (branch.Pt != null)
			branch.Pt = DoubleFactory1D.dense.make(nl);
		if (branch.Qt != null)
			branch.Qt = DoubleFactory1D.dense.make(nl);

		if (branch.mu_Sf != null)
			branch.mu_Sf = DoubleFactory1D.dense.make(nl);
		if (branch.mu_St != null)
			branch.mu_St = DoubleFactory1D.dense.make(nl);
		if (branch.mu_angmin != null)
			branch.mu_angmin = DoubleFactory1D.dense.make(nl);
		if (branch.mu_angmax != null)
			branch.mu_angmax = DoubleFactory1D.dense.make(nl);

		return branch;
	}

}
