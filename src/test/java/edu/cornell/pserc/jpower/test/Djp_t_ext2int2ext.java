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

import cern.colt.matrix.tdouble.DoubleFactory2D;
import edu.emory.mathcs.utils.Utils;
import edu.cornell.pserc.jpower.Djp_ext2int;
import edu.cornell.pserc.jpower.Djp_int2ext;
import edu.cornell.pserc.jpower.Djp_loadcase;
import edu.cornell.pserc.jpower.jpc.GenCost;
import edu.cornell.pserc.jpower.jpc.JPC;

public class Djp_t_ext2int2ext {

	public static void t_ext2int2ext() {
		t_ext2int2ext(false);
	}

	public static void t_ext2int2ext(boolean quiet) {
		String t;
		JPC jpce, jpci, jpc;
		int[] eVmQgcols, iVmQgcols;

		Djp_t_begin.t_begin(85, quiet);

		int verbose = quiet ? 0 : 1;

		/* -----  jpc = ext2int/int2ext(jpc)  ----- */
		t = "jpc = ext2int(jpc) : ";
		jpce = Djp_loadcase.loadcase(Djp_t_case_ext.t_case_ext());
		jpci = Djp_loadcase.loadcase(Djp_t_case_int.t_case_int());
		jpc = Djp_ext2int.ext2int(jpce);
		Djp_t_is.t_is(jpc.bus, jpci.bus, 12, t + "bus");
		Djp_t_is.t_is(jpc.branch, jpci.branch, 12, t + "branch");
		Djp_t_is.t_is(jpc.gen, jpci.gen, 12, t + "gen");
		Djp_t_is.t_is(jpc.gencost, jpci.gencost, 12, t + "gencost");
		Djp_t_is.t_is(jpc.areas, jpci.areas, 12, t + "areas");
//		Djp_t_is.jp_t_is(jpc.A, jpci.A, 12, t + "A");
//		Djp_t_is.jp_t_is(jpc.N, jpci.N, 12, t + "N");
		t = "jpc = ext2int(jpc) - repeat : ";
		jpc = Djp_ext2int.ext2int(jpc);
		Djp_t_is.t_is(jpc.bus, jpci.bus, 12, t + "bus");
		Djp_t_is.t_is(jpc.branch, jpci.branch, 12, t + "branch");
		Djp_t_is.t_is(jpc.gen, jpci.gen, 12, t + "gen");
		Djp_t_is.t_is(jpc.gencost, jpci.gencost, 12, t + "gencost");
		Djp_t_is.t_is(jpc.areas, jpci.areas, 12, t + "areas");
//		Djp_t_is.jp_t_is(jpc.A, jpci.A, 12, t + "A");
//		Djp_t_is.jp_t_is(jpc.N, jpci.N, 12, t + "N");
		t = "jpc = int2ext(jpc) : ";
		jpc = Djp_int2ext.int2ext(jpc);
		Djp_t_is.t_is(jpc.bus, jpce.bus, 12, t + "bus");
		Djp_t_is.t_is(jpc.branch, jpce.branch, 12, t + "branch");
		Djp_t_is.t_is(jpc.gen, jpce.gen, 12, t + "gen");
		Djp_t_is.t_is(jpc.gencost, jpce.gencost, 12, t + "gencost");
		Djp_t_is.t_is(jpc.areas, jpce.areas, 12, t + "areas");
//		Djp_t_is.jp_t_is(jpc.A, jpce.A, 12, t + "A");
//		Djp_t_is.jp_t_is(jpc.N, jpce.N, 12, t + "N");

		// TODO: -----  val = ext2int/int2ext(jpc, val, ...)  -----

		/* -----  more jpc = ext2int/int2ext(jpc)  ----- */
		t = "jpc = ext2int(jpc) - bus/gen/branch only : ";
		jpce = Djp_loadcase.loadcase(Djp_t_case_ext.t_case_ext());
		jpci = Djp_loadcase.loadcase(Djp_t_case_int.t_case_int());
		jpce.gencost = null;
		jpce.areas = null;
		jpce.A = null;
		jpce.N = null;
		jpci.gencost = null;
		jpci.areas = null;
		jpci.A = null;
		jpci.N = null;
		jpc = Djp_ext2int.ext2int(jpce);
		Djp_t_is.t_is(jpc.bus, jpci.bus, 12, t + "bus");
		Djp_t_is.t_is(jpc.branch, jpci.branch, 12, t + "branch");
		Djp_t_is.t_is(jpc.gen, jpci.gen, 12, t + "gen");

//		t = "jpc = ext2int(jpc) - no areas/A : ";
//		jpce = Djp_loadcase.jp_loadcase(Djp_t_case_ext.jp_t_case_ext());
//		jpci = Djp_loadcase.jp_loadcase(Djp_t_case_int.jp_t_case_int());
//		jpce.areas = null;
//		jpce.A = null;
//		jpci.areas = null;
//		jpci.A = null;
//		jpc = Djp_ext2int.jp_ext2int(jpce);
//		Djp_t_is.jp_t_is(jpc.bus, jpci.bus, 12, t + "bus");
//		Djp_t_is.jp_t_is(jpc.branch, jpci.branch, 12, t + "branch");
//		Djp_t_is.jp_t_is(jpc.gen, jpci.gen, 12, t + "gen");
//		Djp_t_is.jp_t_is(jpc.gencost, jpci.gencost, 12, t + "gencost");
//		Djp_t_is.jp_t_is(jpc.N, jpci.N, 12, t + "N");
//
//		t = "jpc = ext2int(jpc) - Qg cost, no N : ";
//		jpce = Djp_loadcase.jp_loadcase(Djp_t_case_ext.jp_t_case_ext());
//		jpci = Djp_loadcase.jp_loadcase(Djp_t_case_int.jp_t_case_int());
//		jpce.N = null;
//		jpci.N = null;
//		jpce.gencost = Djp_gencost.fromMatrix(DoubleFactory2D.dense.appendRows(
//				jpce.gencost.toMatrix(),
//				jpce.gencost.toMatrix()
//		));
//		jpci.gencost = Djp_gencost.fromMatrix(DoubleFactory2D.dense.appendRows(
//				jpci.gencost.toMatrix(),
//				jpci.gencost.toMatrix()
//		));
//		jpc = Djp_ext2int.jp_ext2int(jpce);
//		Djp_t_is.jp_t_is(jpc.bus, jpci.bus, 12, t + "bus");
//		Djp_t_is.jp_t_is(jpc.branch, jpci.branch, 12, t + "branch");
//		Djp_t_is.jp_t_is(jpc.gen, jpci.gen, 12, t + "gen");
//		Djp_t_is.jp_t_is(jpc.gencost, jpci.gencost, 12, t + "gencost");
//		Djp_t_is.jp_t_is(jpc.areas, jpci.areas, 12, t + "areas");
//		Djp_t_is.jp_t_is(jpc.A, jpci.A, 12, t + "A");
//
//		t = "jpc = ext2int(jpc) - A, N are DC sized : ";
//		jpce = Djp_loadcase.jp_loadcase(Djp_t_case_ext.jp_t_case_ext());
//		jpci = Djp_loadcase.jp_loadcase(Djp_t_case_int.jp_t_case_int());
//		eVmQgcols = Djp_util.icat(
//				Djp_util.irange(10, 20),
//				Djp_util.irange(24, 28)
//		);
//		iVmQgcols = Djp_util.icat(
//				Djp_util.irange(9, 18),
//				Djp_util.irange(21, 24)
//		);
//		jpce.A.(:, eVmQgcols) = [];
//		jpce.N(:, eVmQgcols) = [];
//		jpci.A(:, iVmQgcols) = [];
//		jpci.N(:, iVmQgcols) = [];
//		jpc = ext2int(jpce);
//		t_is(jpc.bus, jpci.bus, 12, t + "bus");
//		t_is(jpc.branch, jpci.branch, 12, t + "branch");
//		t_is(jpc.gen, jpci.gen, 12, t + "gen");
//		t_is(jpc.gencost, jpci.gencost, 12, t + "gencost");
//		t_is(jpc.areas, jpci.areas, 12, t + "areas");
//		t_is(jpc.A, jpci.A, 12, t + "A");
//		t_is(jpc.N, jpci.N, 12, t + "N");
//		t = "jpc = int2ext(jpc) - A, N are DC sized : ";
//		jpc = int2ext(jpc);
//		t_is(jpc.bus, jpce.bus, 12, t + "bus");
//		t_is(jpc.branch, jpce.branch, 12, t + "branch");
//		t_is(jpc.gen, jpce.gen, 12, t + "gen");
//		t_is(jpc.gencost, jpce.gencost, 12, t + "gencost");
//		t_is(jpc.areas, jpce.areas, 12, t + "areas");
//		t_is(jpc.A, jpce.A, 12, t + "A");
//		t_is(jpc.N, jpce.N, 12, t + "N");

		Djp_t_end.t_end();
	}

	public static void main(String[] args) {
		t_ext2int2ext(false);
	}

}
