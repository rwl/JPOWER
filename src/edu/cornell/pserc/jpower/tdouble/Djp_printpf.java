/*
 * Copyright (C) 1996-2010 by Power System Engineering Research Center (PSERC)
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

package edu.cornell.pserc.jpower.tdouble;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Map;

import cern.colt.matrix.tdcomplex.DComplexFactory1D;
import cern.colt.matrix.tdcomplex.DComplexMatrix1D;
import cern.colt.matrix.tdouble.DoubleFactory1D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tint.IntFactory1D;
import cern.colt.matrix.tint.IntMatrix1D;
import cern.colt.matrix.tint.algo.IntSorting;
import cern.jet.math.tdcomplex.DComplexFunctions;
import cern.jet.math.tdouble.DoubleFunctions;
import cern.jet.math.tint.IntFunctions;

import edu.cornell.pserc.jpower.tdouble.jpc.Djp_branch;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_bus;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_gen;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_jpc;
import edu.cornell.pserc.jpower.tdouble.util.Djp_util;

public class Djp_printpf {

	private static final Djp_util util = new Djp_util();
	private static final IntFunctions ifunc = IntFunctions.intFunctions;
	private static final DoubleFunctions dfunc = DoubleFunctions.functions;
	private static final DComplexFunctions cfunc = DComplexFunctions.functions;

	public static void jp_printpf(Djp_jpc results) {
		jp_printpf(results, System.out);
	}

	public static void jp_printpf(Djp_jpc results, OutputStream output) {
		jp_printpf(results, output, Djp_jpoption.jp_jpoption());
	}

	@SuppressWarnings("static-access")
	public static void jp_printpf(Djp_jpc results, OutputStream output, Map<String, Double> jpopt) {
		PrintWriter pw = new PrintWriter(output);

		if (jpopt.get("OUT_ALL").equals(0) || jpopt.get("OUT_RAW").equals(0))
			return;

		double baseMVA = results.baseMVA;
		Djp_bus bus = results.bus.copy();
		Djp_gen gen = results.gen.copy();
		Djp_branch branch = results.branch.copy();
		boolean success = results.success;
		double et = results.et;
		Double f = results.f;

		boolean isOPF = (f != null);	/* FALSE -> only simple PF data, TRUE -> OPF data */

		/* options */
		boolean isDC			= jpopt.get("PF_DC") == 1;	// use DC formulation?
		int OUT_ALL				= jpopt.get("OUT_ALL").intValue();
		boolean OUT_ANY			= OUT_ALL == 1;     // set to true if any pretty output is to be generated
		boolean OUT_SYS_SUM		= OUT_ALL == 1 || (OUT_ALL == -1 && jpopt.get("OUT_SYS_SUM") == 1);
		boolean OUT_AREA_SUM	= OUT_ALL == 1 || (OUT_ALL == -1 && jpopt.get("OUT_AREA_SUM") == 1);
		boolean OUT_BUS			= OUT_ALL == 1 || (OUT_ALL == -1 && jpopt.get("OUT_BUS") == 1);
		boolean OUT_BRANCH		= OUT_ALL == 1 || (OUT_ALL == -1 && jpopt.get("OUT_BRANCH") == 1);
		boolean OUT_GEN			= OUT_ALL == 1 || (OUT_ALL == -1 && jpopt.get("OUT_GEN") == 1);
		OUT_ANY					= OUT_ANY || (OUT_ALL == -1 &&
			(OUT_SYS_SUM || OUT_AREA_SUM || OUT_BUS || OUT_BRANCH || OUT_GEN));
		int OUT_ALL_LIM;
		if (OUT_ALL == -1) {
			OUT_ALL_LIM = jpopt.get("OUT_ALL_LIM").intValue();
		} else if (OUT_ALL == 1) {
			OUT_ALL_LIM = 2;
		} else {
			OUT_ALL_LIM = 0;
		}
		OUT_ANY         = OUT_ANY || OUT_ALL_LIM >= 1;

		int OUT_V_LIM;
		boolean OUT_LINE_LIM;
		boolean OUT_PG_LIM;
		boolean OUT_QG_LIM;
		if (OUT_ALL_LIM == -1) {
			OUT_V_LIM       = jpopt.get("OUT_V_LIM").intValue();
			OUT_LINE_LIM    = jpopt.get("OUT_LINE_LIM") == 1;
			OUT_PG_LIM      = jpopt.get("OUT_PG_LIM") == 1;
			OUT_QG_LIM      = jpopt.get("OUT_QG_LIM") == 1;
		} else {
			OUT_V_LIM       = OUT_ALL_LIM;
			OUT_LINE_LIM    = OUT_ALL_LIM == 1;
			OUT_PG_LIM      = OUT_ALL_LIM == 1;
			OUT_QG_LIM      = OUT_ALL_LIM == 1;
		}
		OUT_ANY			= OUT_ANY || (OUT_ALL_LIM == -1 && (OUT_V_LIM > 0 || OUT_LINE_LIM || OUT_PG_LIM || OUT_QG_LIM));
		boolean OUT_RAW	= jpopt.get("OUT_RAW") == 1;
		double ptol = 1e-6;		// tolerance for displaying shadow prices

		/* internal bus number */
		IntMatrix1D i2e = bus.bus_i.copy();
		IntMatrix1D e2i = IntFactory1D.sparse.make(i2e.aggregate(ifunc.max, ifunc.identity));
		e2i.viewSelection(i2e.toArray()).assign( Djp_util.irange(bus.size()) );

		/* sizes */
		int nb = bus.size();		// number of buses
		int nl = branch.size();		// number of branches
		int ng = gen.size();		// number of generators

		/* zero out some data to make printout consistent for DC case */
		if (isDC) {
			bus.Qd.assign(0);
			bus.Bs.assign(0);
			gen.Qg.assign(0);
			gen.Qmax.assign(0);
			gen.Qmin.assign(0);
			branch.br_r.assign(0);
			branch.br_b.assign(0);
		}

		/* parameters */
		IntMatrix1D ties = bus.bus_area.viewSelection(e2i.viewSelection(branch.f_bus.toArray()).toArray());
		ties.assign(bus.bus_area.viewSelection(e2i.viewSelection(branch.t_bus.toArray()).toArray()), IntFunctions.equals);
		ties.assign(IntFunctions.equals(0));			// area inter-ties

		DComplexMatrix1D tap = DComplexFactory1D.dense.make(nl, new double[] {1, 0});	// default tap ratio = 1 for lines
		int[] xfmr = util.nonzero(branch.tap);							// indices of transformers
		tap.viewSelection(xfmr).assignReal(branch.tap.viewSelection(xfmr));	// include transformer tap ratios
		tap.assign(util.polar(tap.getRealPart(), branch.shift, false));	// add phase shifters

		IntMatrix1D ld = util.intm(bus.Pd);
		ld.assign(util.intm(bus.Qd), ifunc.or);
		int[] nzld = util.nonzero(ld);

		IntMatrix1D sorted_areas = IntSorting.quickSort.sort(bus.bus_area);
//		s_areas = sorted_areas([1; find(diff(sorted_areas))+1]);    // area numbers

		IntMatrix1D shunt = util.intm(bus.Gs);
		shunt.assign(util.intm(bus.Bs), ifunc.or);
		int[] nzsh = util.nonzero(shunt);

		IntMatrix1D isload = Djp_isload.jp_isload(gen);
		isload.assign(ifunc.equals(0));
		int[] allg = util.nonzero(isload);

		IntMatrix1D gs = gen.gen_status.copy();
		gs.assign(isload, ifunc.and);
		int[] ong  = util.nonzero(gs);
		gs = gen.gen_status.copy();
		isload.assign(ifunc.equals(0));
		gs.assign(isload, ifunc.and);
		int[] onld = util.nonzero(gs);

		DComplexMatrix1D V = util.polar(bus.Vm, bus.Va, false);
		IntMatrix1D bs = branch.br_status.copy();

		int[] out = util.nonzero( bs.assign(ifunc.equals(0)) );		// out-of-service branches
		int nout = out.length;

		DComplexMatrix1D loss;
		if (isDC) {
			loss = DComplexFactory1D.dense.make(nl);
		} else {
//			loss = baseMVA * abs(V(e2i(branch(:, F_BUS))) ./ tap - V(e2i(branch(:, T_BUS)))) .^ 2 ./ ...
//						(branch(:, BR_R) - 1j * branch(:, BR_X));
		}
//		fchg = abs(V(e2i(branch(:, F_BUS))) ./ tap) .^ 2 .* branch(:, BR_B) * baseMVA / 2;
//		tchg = abs(V(e2i(branch(:, T_BUS)))       ) .^ 2 .* branch(:, BR_B) * baseMVA / 2;
//		loss.viewSelection(out).assign( DComplexFactory1D.dense.make(nout) );
//		fchg.viewSelection(out).assign( DComplexFactory1D.dense.make(nout) );
//		tchg.viewSelection(out).assign( DComplexFactory1D.dense.make(nout) );
	}

}
