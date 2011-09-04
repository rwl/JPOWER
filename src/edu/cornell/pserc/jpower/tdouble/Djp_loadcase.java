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

package edu.cornell.pserc.jpower.tdouble;

import cern.colt.matrix.tdouble.DoubleFactory1D;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_branch;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_gen;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_jpc;

public class Djp_loadcase {

	public static Djp_jpc loadcase(String casefile) {
		// TODO Implement this method
		throw new UnsupportedOperationException();
	}

	public static Djp_jpc loadcase(Djp_jpc jpc) {
		jpc = jpc.copy();

		// TODO: -----  check contents of case  -----

		if (jpc.version.equals("1")) {
			jpc.gen = jpc_1to2(jpc.gen);
			jpc.branch =  jpc_1to2(jpc.branch);
		}

		return jpc;
	}

	private static Djp_gen jpc_1to2(Djp_gen gen) {
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

	private static Djp_branch jpc_1to2(Djp_branch branch) {
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
