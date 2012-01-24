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

import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import static edu.cornell.pserc.jpower.Djp_e2i_data.e2i_data;
import static edu.cornell.pserc.jpower.Djp_e2i_field.e2i_field;
import static edu.cornell.pserc.jpower.Djp_ext2int.ext2int;
import static edu.cornell.pserc.jpower.Djp_i2e_data.i2e_data;
import static edu.cornell.pserc.jpower.Djp_i2e_field.i2e_field;
import static edu.cornell.pserc.jpower.Djp_int2ext.int2ext;
import static edu.cornell.pserc.jpower.Djp_loadcase.loadcase;

import static cern.colt.util.tdouble.Util.icat;
import static cern.colt.util.tdouble.Util.delete;
import static cern.colt.util.tdouble.Util.irange;

import edu.cornell.pserc.jpower.Ordering;
import edu.cornell.pserc.jpower.jpc.Cost;
import edu.cornell.pserc.jpower.jpc.JPC;

import static edu.cornell.pserc.jpower.test.Djp_t_begin.t_begin;
import static edu.cornell.pserc.jpower.test.Djp_t_case_ext.t_case_ext;
import static edu.cornell.pserc.jpower.test.Djp_t_case_int.t_case_int;
import static edu.cornell.pserc.jpower.test.Djp_t_is.t_is;
import static edu.cornell.pserc.jpower.test.Djp_t_end.t_end;

public class Djp_t_ext2int2ext {

	public static void t_ext2int2ext() {
		t_ext2int2ext(false);
	}

	public static void t_ext2int2ext(boolean quiet) {
		String t;
		JPCExt jpce, got2;
		JPC jpci, jpc;
		int[] eVmQgcols, iVmQgcols;
		DoubleMatrix2D got, ex, tmp, tmp1, tmp2, tmp3;

		t_begin(85, quiet);

		int verbose = quiet ? 0 : 1;

		/* -----  jpc = ext2int/int2ext(jpc)  ----- */
		t = "jpc = ext2int(jpc) : ";
		jpce = (JPCExt) loadcase(t_case_ext());
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

		/* -----  val = e2i_data/i2e_data(jpc, val, ...)  ----- */
		t = "val = e2i_data(jpc, val, \"bus\")";
		jpc = ext2int(jpce);
		got = e2i_data(jpc, jpce.xbus, Ordering.BUS);
		ex = jpce.xbus;
		ex = delete(ex, 5);
		t_is(got, ex, 12, t);
		t = "val = i2e_data(jpc, val, oldval, \"bus\")";
		tmp = DoubleFactory2D.dense.make(jpce.xbus.rows(), jpce.xbus.columns(), 1.0);
		tmp.viewRow(5).assign( jpce.xbus.viewRow(5) );
		got = i2e_data(jpc, ex, tmp, Ordering.BUS);
		t_is(got, jpce.xbus, 12, t);

		t = "val = e2i_data(jpc, val, \"bus\", 1)";
		got = e2i_data(jpc, jpce.xbus, Ordering.BUS, 1);
		ex = jpce.xbus;
		ex = delete(ex, 5);
		t_is(got, ex, 12, t);
		t = "val = i2e_data(jpc, val, oldval, \"bus\", 1)";
		tmp = DoubleFactory2D.dense.make(jpce.xbus.rows(), jpce.xbus.columns(), 1.0);
		tmp.viewRow(5).assign( jpce.xbus.viewRow(5) );
		got = i2e_data(jpc, ex, tmp, Ordering.BUS, 1);
		t_is(got, jpce.xbus, 12, t);

		t = "val = e2i_data(jpc, val, \"gen\")";
		got = e2i_data(jpc, jpce.xgen, Ordering.GEN);
		ex = jpce.xgen.viewSelection(new int[] {3, 1, 0}, null);
		t_is(got, ex, 12, t);
		t = "val = i2e_data(jpc, val, oldval, \"gen\")";
		tmp = DoubleFactory2D.dense.make(jpce.xgen.rows(), jpce.xgen.columns(), 1.0);
		tmp.viewRow(2).assign( jpce.xgen.viewRow(2) );
		got = i2e_data(jpc, ex, tmp, Ordering.GEN);
		t_is(got, jpce.xgen, 12, t);

		t = "val = e2i_data(jpc, val, \"gen\", 1)";
		got = e2i_data(jpc, jpce.xgen, Ordering.GEN, 1);
		ex = jpce.xgen.viewSelection(null, new int[] {3, 1, 0});
		t_is(got, ex, 12, t);
		t = "val = i2e_data(jpc, val, oldval, \"gen\", 1)";
		tmp = DoubleFactory2D.dense.make(jpce.xgen.rows(), jpce.xgen.columns(), 1.0);
		tmp.viewRow(2).assign( jpce.xgen.viewRow(2) );
		got = i2e_data(jpc, ex, tmp, Ordering.GEN, 1);
		t_is(got, jpce.xgen, 12, t);

		t = "val = e2i_data(jpc, val, \"branch\")";
		got = e2i_data(jpc, jpce.xbranch, Ordering.BRANCH);
		ex = jpce.xbranch.copy();
		ex = delete(ex, 6);
		t_is(got, ex, 12, t);
		t = "val = i2e_data(jpc, val, oldval, \"branch\")";
		tmp = DoubleFactory2D.dense.make(jpce.xbranch.rows(), jpce.xbranch.columns(), 1.0);
		tmp.viewRow(6).assign( jpce.xbranch.viewRow(6) );
		got = i2e_data(jpc, ex, tmp, Ordering.BRANCH);
		t_is(got, jpce.xbranch, 12, t);

		t = "val = e2i_data(jpc, val, \"branch\", 1)";
		got = e2i_data(jpc, jpce.xbranch, Ordering.BRANCH, 1);
		ex = jpce.xbranch.copy();
		ex = delete(ex, 6);
		t_is(got, ex, 12, t);
		t = "val = i2e_data(jpc, val, oldval, \"branch\", 1)";
		tmp = DoubleFactory2D.dense.make(jpce.xbranch.rows(), jpce.xbranch.columns(), 1.0);
		tmp.viewRow(6).assign( jpce.xbranch.viewRow(6) );
		got = i2e_data(jpc, ex, tmp, Ordering.BRANCH, 1);
		t_is(got, jpce.xbranch, 12, t);

		t = "val = e2i_data(jpc, val, {\"branch\", \"gen\", \"bus\"})";
		got = e2i_data(jpc, jpce.xrows, new Ordering[] {Ordering.BRANCH, Ordering.GEN, Ordering.BUS});
		ex = DoubleFactory2D.dense.compose(new DoubleMatrix2D[][] {
				{jpce.xbranch.viewSelection(icat(irange(6), irange(7, 10)), irange(4))},
				{jpce.xgen.viewSelection(new int[] {4, 2, 1}, null)},
				{jpce.xbus.viewSelection(icat(irange(5), irange(6, 10)), irange(4))},
				{DoubleFactory2D.dense.make(2, 4, -1.0)}
		});
		t_is(got, ex, 12, t);
		t = "val = i2e_data(jpc, val, oldval, {\"branch\", \"gen\", \"bus\"})";
		tmp1 = DoubleFactory2D.dense.make(jpce.xbranch.rows(), 4, 1);
		tmp1.viewSelection(new int[] {6}, irange(4)).assign( jpce.xbranch.viewSelection(new int[] {6}, irange(4)) );
		tmp2 = DoubleFactory2D.dense.make(jpce.xgen.rows(), jpce.xgen.columns(), 1.0);
		tmp2.viewRow(2).assign( jpce.xgen.viewRow(2) );
		tmp3 = DoubleFactory2D.dense.make(jpce.xbus.rows(), 4, 1.0);
		tmp3.viewSelection(new int[] {5}, irange(4)).assign( jpce.xbus.viewSelection(new int[] {5}, irange(4)) );
		tmp = DoubleFactory2D.dense.compose(new DoubleMatrix2D[][] { {tmp1}, {tmp2}, {tmp3} });
		got = i2e_data(jpc, ex, tmp, new Ordering[] {Ordering.BRANCH, Ordering.GEN, Ordering.BUS});
		t_is(got, jpce.xrows, 12, t);

		t = "val = e2i_data(jpc, val, {\"branch\", \"gen\", \"bus\"}, 1)";
		got = e2i_data(jpc, jpce.xcols, new Ordering[] {Ordering.BRANCH, Ordering.GEN, Ordering.BUS}, 1);
		ex = DoubleFactory2D.dense.compose(new DoubleMatrix2D[][] {
				{jpce.xbranch.viewSelection(icat(irange(6), irange(7, 10)), irange(4))},
				{jpce.xgen.viewSelection(new int[] {3, 1, 0}, null)},
				{jpce.xgen.viewSelection(icat(irange(5), irange(6, 10)), irange(6, 10))},
				{DoubleFactory2D.dense.make(2, 4, -1.0)}
		});
		t_is(got, ex, 12, t);
		t = "val = i2e_data(jpc, val, oldval, {\"branch\", \"gen\", \"bus\"}, 1)";
		tmp1 = DoubleFactory2D.dense.make(jpce.xbranch.rows(), 4, 1.0);
		tmp1.viewSelection(new int[] {6}, irange(4)).assign( jpce.xbranch.viewSelection(new int[] {6}, irange(4)) );
		tmp2 = DoubleFactory2D.dense.make(jpce.xgen.rows(), jpce.xgen.columns(), 1.0);
		tmp2.viewRow(2).assign( jpce.xgen.viewRow(3) );
		tmp3 = DoubleFactory2D.dense.make(jpce.xbus.rows(), 4, 1.0);
		tmp3.viewSelection(new int[] {5}, irange(4)).assign( jpce.xbus.viewSelection(new int[] {5}, irange(4)) );
		tmp = DoubleFactory2D.dense.compose(new DoubleMatrix2D[][] { {tmp1}, {tmp2}, {tmp3} }).viewDice();
		got = i2e_data(jpc, ex, tmp, new Ordering[] {Ordering.BRANCH, Ordering.GEN, Ordering.BUS}, 1);
		t_is(got, jpce.xcols, 12, t);

		/* -----  jpc = e2i_field/i2e_field(jpc, field, ...)  ----- */
		t = "jpc = e2i_field(jpc, field, \"bus\")";
		jpc = ext2int(jpce);
		ex = jpce.xbus;
		ex = delete(ex, 5);
		got2 = (JPCExt) e2i_field(jpc, "xbus", Ordering.BUS);
		t_is(got2.xbus, ex, 12, t);
		t = "jpc = i2e_field(jpc, field, \"bus\")";
		got2 = (JPCExt) i2e_field(got2, "xbus", Ordering.BUS);
		t_is(got2.xbus, jpce.xbus, 12, t);

		t = "jpc = e2i_field(jpc, field, \"bus\", 1)";
		ex = jpce.xbus;
		ex = delete(ex, 5);
		got2 = (JPCExt) e2i_field(jpc, "xbus", Ordering.BUS, 1);
		t_is(got2.xbus, ex, 12, t);
		t = "jpc = i2e_field(jpc, field, \"bus\", 1)";
		got2 = (JPCExt) i2e_field(got2, "xbus", Ordering.BUS, 1);
		t_is(got2.xbus, jpce.xbus, 12, t);

		t = "jpc = e2i_field(jpc, field, \"gen\")";
		ex = jpce.xgen.viewSelection(new int[] {3, 1, 0}, null);
		got2 = (JPCExt) e2i_field(jpc, "xgen", Ordering.GEN);
		t_is(got2.xgen, ex, 12, t);
		t = "jpc = i2e_field(jpc, field, \"gen\")";
		got2 = (JPCExt) i2e_field(got2, "xgen", Ordering.GEN);
		t_is(got2.xgen, jpce.xgen, 12, t);

		t = "jpc = e2i_field(jpc, field, \"gen\", 1)";
		ex = jpce.xgen.viewSelection(null, new int[] {3, 1, 0});
		got2 = (JPCExt) e2i_field(jpc, "xgen", Ordering.GEN, 1);
		t_is(got2.xgen, ex, 12, t);
		t = "jpc = i2e_field(jpc, field, \"gen\", 1)";
		got2 = (JPCExt) i2e_field(got2, "xgen", Ordering.GEN, 1);
		t_is(got2.xgen, jpce.xgen, 12, t);

		t = "jpc = e2i_field(jpc, field, \"branch\")";
		ex = jpce.xbranch;
		ex = delete(ex, 6);
		got2 = (JPCExt) e2i_field(jpc, "xbranch", Ordering.BRANCH);
		t_is(got2.xbranch, ex, 12, t);
		t = "jpc = i2e_field(jpc, field, \"branch\")";
		got2 = (JPCExt) i2e_field(got2, "xbranch", Ordering.BRANCH);
		t_is(got2.xbranch, jpce.xbranch, 12, t);

		t = "jpc = e2i_field(jpc, field, \"branch\", 1)";
		ex = jpce.xbranch.copy();
		ex = delete(ex, 6);
		got2 = (JPCExt) e2i_field(jpc, "xbranch", Ordering.BRANCH, 1);
		t_is(got2.xbranch, ex, 12, t);
		t = "jpc = i2e_field(jpc, field, \"branch\", 1)";
		got2 = (JPCExt) i2e_field(got2, "xbranch", Ordering.BRANCH, 1);
		t_is(got2.xbranch, jpce.xbranch, 12, t);

		t = "jpc = e2i_field(jpc, field, {\"branch\", \"gen\", \"bus\"})";
		ex = DoubleFactory2D.dense.compose(new DoubleMatrix2D[][] {
				{jpce.xbranch.viewSelection(icat(irange(6), irange(7, 10)), irange(4))},
				{jpce.xgen.viewSelection(new int[] {3, 1, 0}, null)},
				{jpce.xbus.viewSelection(icat(irange(5), irange(6, 10)), irange(4))},
				{DoubleFactory2D.dense.make(2, 4, -1.0)}
		});
		got2 = (JPCExt) e2i_field(jpc, "xrows", new Ordering[] {Ordering.BRANCH, Ordering.GEN, Ordering.BUS});
		t_is(got2.xrows, ex, 12, t);
		t = "jpc = i2e_field(jpc, field, {\"branch\", \"gen\", \"bus\"})";
		got2 = (JPCExt) i2e_field(got2, "xrows", new Ordering[] {Ordering.BRANCH, Ordering.GEN, Ordering.BUS});
		t_is(got2.xrows, jpce.xrows, 12, t);

		t = "jpc = e2i_field(jpc, field, {\"branch\", \"gen\", \"bus\"}, 1)";
		ex = DoubleFactory2D.dense.compose(new DoubleMatrix2D[][] {
				{jpce.xbranch.viewSelection(icat(irange(6), irange(7, 10)), irange(4))},
				{jpce.xgen.viewSelection(new int[] {3, 1, 0}, null)},
				{jpce.xbus.viewSelection(icat(irange(5), irange(6, 10)), irange(4))},
				{DoubleFactory2D.dense.make(2, 4, -1.0)}
		}).viewDice();
		got2 = (JPCExt) e2i_field(jpc, "xcols", new Ordering[] {Ordering.BRANCH, Ordering.GEN, Ordering.BUS}, 1);
		t_is(got2.xcols, ex, 12, t);
		t = "jpc = i2e_field(jpc, field, {\"branch\", \"gen\", \"bus\"})";
		got2 = (JPCExt) i2e_field(got2, "xcols", new Ordering[] {Ordering.BRANCH, Ordering.GEN, Ordering.BUS}, 1);
		t_is(got2.xcols, jpce.xcols, 12, t);

		t = "jpc = e2i_field(jpc, {\"field1\", \"field2\"}, ordering)";
		ex = jpce.x.get("more").viewSelection(new int[] {3, 1, 0}, null);
		got2 = (JPCExt) e2i_field(jpc, new String[] {"x", "more"}, Ordering.GEN);
		t_is(got2.x.get("more"), ex, 12, t);
		t = "jpc = i2e_field(jpc, {\"field1\", \"field2\"}, ordering)";
		got2 = (JPCExt) i2e_field(got2, new String[] {"x", "more"}, Ordering.GEN);
		t_is(got2.x.get("more"), jpce.x.get("more"), 12, t);

		t = "jpc = e2i_field(jpc, {\"field1\", \"field2\"}, ordering, 1)";
		ex = jpce.x.get("more").viewSelection(null, new int[] {3, 1, 0});
		got2 = (JPCExt) e2i_field(jpc, new String[] {"x", "more"}, Ordering.GEN, 1);
		t_is(got2.x.get("more"), ex, 12, t);
		t = "jpc = i2e_field(jpc, {\"field1\", \"field2\"}, ordering, 1)";
		got2 = (JPCExt) i2e_field(got2, new String[] {"x", "more"}, Ordering.GEN, 1);
		t_is(got2.x.get("more"), jpce.x.get("more"), 12, t);

		/* -----  more jpc = ext2int/int2ext(jpc)  ----- */
		t = "jpc = ext2int(jpc) - bus/gen/branch only : ";
		jpce = (JPCExt) loadcase(t_case_ext());
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
		jpce = (JPCExt) loadcase(Djp_t_case_ext.t_case_ext());
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
		jpce = (JPCExt) loadcase(Djp_t_case_ext.t_case_ext());
		jpci = loadcase(Djp_t_case_int.t_case_int());
		jpce.N = null;
		jpci.N = null;
		jpce.gencost = Cost.fromMatrix(DoubleFactory2D.dense.appendRows(
				jpce.gencost.toMatrix(),
				jpce.gencost.toMatrix()
		));
		jpci.gencost = Cost.fromMatrix(DoubleFactory2D.dense.appendRows(
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
//		eVmQgcols = icat(
//				Djp_util.irange(10, 20),
//				Djp_util.irange(24, 28)
//		);
//		iVmQgcols = icat(
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

		t_end();
	}

	public static void main(String[] args) {
		t_ext2int2ext(false);
	}

}
