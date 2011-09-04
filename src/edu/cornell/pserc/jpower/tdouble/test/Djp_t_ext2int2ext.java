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

package edu.cornell.pserc.jpower.tdouble.test;

import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.util.tdouble.Djp_util;
import static edu.cornell.pserc.jpower.tdouble.Djp_ext2int.ext2int;
import static edu.cornell.pserc.jpower.tdouble.Djp_int2ext.int2ext;
import static edu.cornell.pserc.jpower.tdouble.Djp_loadcase.loadcase;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_gencost;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_jpc;
import static edu.cornell.pserc.jpower.tdouble.test.Djp_t_is.t_is;
import static edu.cornell.pserc.jpower.tdouble.test.Djp_t_begin.t_begin;
import static edu.cornell.pserc.jpower.tdouble.test.Djp_t_case_int.t_case_int;
import static edu.cornell.pserc.jpower.tdouble.test.Djp_t_case_ext.t_case_ext;

public class Djp_t_ext2int2ext {

	public static void t_ext2int2ext() {
		t_ext2int2ext(false);
	}

	public static void t_ext2int2ext(boolean quiet) {
		String t;
		Djp_jpc jpce, jpci, jpc;
		int[] eVmQgcols, iVmQgcols;

		t_begin(85, quiet);

		int verbose = quiet ? 0 : 1;

		/* -----  jpc = ext2int/int2ext(jpc)  ----- */
		t = "jpc = ext2int(jpc) : ";
		jpce = loadcase(t_case_ext());
		jpci = loadcase(t_case_int());
		jpc = ext2int(jpce);
		t_is(jpc.bus, jpci.bus, 12, t + "bus");
		t_is(jpc.branch, jpci.branch, 12, t + "branch");
		t_is(jpc.gen, jpci.gen, 12, t + "gen");
		t_is(jpc.gencost, jpci.gencost, 12, t + "gencost");
		t_is(jpc.areas, jpci.areas, 12, t + "areas");
//		t_is(jpc.A, jpci.A, 12, t + "A");
//		t_is(jpc.N, jpci.N, 12, t + "N");
		t = "jpc = ext2int(jpc) - repeat : ";
		jpc = ext2int(jpc);
		t_is(jpc.bus, jpci.bus, 12, t + "bus");
		t_is(jpc.branch, jpci.branch, 12, t + "branch");
		t_is(jpc.gen, jpci.gen, 12, t + "gen");
		t_is(jpc.gencost, jpci.gencost, 12, t + "gencost");
		t_is(jpc.areas, jpci.areas, 12, t + "areas");
//		t_is(jpc.A, jpci.A, 12, t + "A");
//		t_is(jpc.N, jpci.N, 12, t + "N");
		t = "jpc = int2ext(jpc) : ";
		jpc = int2ext(jpc);
		t_is(jpc.bus, jpce.bus, 12, t + "bus");
		t_is(jpc.branch, jpce.branch, 12, t + "branch");
		t_is(jpc.gen, jpce.gen, 12, t + "gen");
		t_is(jpc.gencost, jpce.gencost, 12, t + "gencost");
		t_is(jpc.areas, jpce.areas, 12, t + "areas");
//		t_is(jpc.A, jpce.A, 12, t + "A");
//		t_is(jpc.N, jpce.N, 12, t + "N");

		// TODO: -----  val = ext2int/int2ext(jpc, val, ...)  -----

		/* -----  more jpc = ext2int/int2ext(jpc)  ----- */
		t = "jpc = ext2int(jpc) - bus/gen/branch only : ";
		jpce = loadcase(t_case_ext());
		jpci = loadcase(t_case_int());
		jpce.gencost = null;
		jpce.areas = null;
		jpce.A = null;
		jpce.N = null;
		jpci.gencost = null;
		jpci.areas = null;
		jpci.A = null;
		jpci.N = null;
		jpc = ext2int(jpce);
		t_is(jpc.bus, jpci.bus, 12, t + "bus");
		t_is(jpc.branch, jpci.branch, 12, t + "branch");
		t_is(jpc.gen, jpci.gen, 12, t + "gen");

		t = "jpc = ext2int(jpc) - no areas/A : ";
		jpce = loadcase(Djp_t_case_ext.t_case_ext());
		jpci = loadcase(Djp_t_case_int.t_case_int());
		jpce.areas = null;
		jpce.A = null;
		jpci.areas = null;
		jpci.A = null;
		jpc = ext2int(jpce);
		t_is(jpc.bus, jpci.bus, 12, t + "bus");
		t_is(jpc.branch, jpci.branch, 12, t + "branch");
		t_is(jpc.gen, jpci.gen, 12, t + "gen");
		t_is(jpc.gencost, jpci.gencost, 12, t + "gencost");
		t_is(jpc.N, jpci.N, 12, t + "N");

		t = "jpc = ext2int(jpc) - Qg cost, no N : ";
		jpce = loadcase(Djp_t_case_ext.t_case_ext());
		jpci = loadcase(Djp_t_case_int.t_case_int());
		jpce.N = null;
		jpci.N = null;
		jpce.gencost = Djp_gencost.fromMatrix(DoubleFactory2D.dense.appendRows(
				jpce.gencost.toMatrix(),
				jpce.gencost.toMatrix()
		));
		jpci.gencost = Djp_gencost.fromMatrix(DoubleFactory2D.dense.appendRows(
				jpci.gencost.toMatrix(),
				jpci.gencost.toMatrix()
		));
		jpc = ext2int(jpce);
		t_is(jpc.bus, jpci.bus, 12, t + "bus");
		t_is(jpc.branch, jpci.branch, 12, t + "branch");
		t_is(jpc.gen, jpci.gen, 12, t + "gen");
		t_is(jpc.gencost, jpci.gencost, 12, t + "gencost");
		t_is(jpc.areas, jpci.areas, 12, t + "areas");
		t_is(jpc.A, jpci.A, 12, t + "A");

//		t = "jpc = ext2int(jpc) - A, N are DC sized : ";
//		jpce = loadcase(Djp_t_case_ext.jp_t_case_ext());
//		jpci = loadcase(Djp_t_case_int.jp_t_case_int());
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
