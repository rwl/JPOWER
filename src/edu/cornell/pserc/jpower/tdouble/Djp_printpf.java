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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
import cern.colt.util.tdouble.Djp_util;
import cern.jet.math.tdcomplex.DComplexFunctions;
import cern.jet.math.tdouble.DoubleFunctions;
import cern.jet.math.tint.IntFunctions;

import edu.cornell.pserc.jpower.tdouble.jpc.Djp_branch;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_bus;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_gen;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_jpc;

/**
 * Prints power flow results.
 *
 * @author Ray Zimmerman
 * @author Richard Lincoln
 */
public class Djp_printpf {

	private static final IntFunctions ifunc = IntFunctions.intFunctions;
	private static final DoubleFunctions dfunc = DoubleFunctions.functions;
	private static final DComplexFunctions cfunc = DComplexFunctions.functions;

	public static void jp_printpf(Djp_jpc results) {
		jp_printpf(results, System.out);
	}

	public static void jp_printpf(Djp_jpc results, String fname) {
		jp_printpf(results, fname, Djp_jpoption.jp_jpoption());
	}

	public static void jp_printpf(Djp_jpc results, String fname, Map<String, Double> jpopt) {
		FileOutputStream output;
		try {
			output = new FileOutputStream(fname);

			jp_printpf(results, output, Djp_jpoption.jp_jpoption());

			output.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void jp_printpf(Djp_jpc results, OutputStream output) {
		jp_printpf(results, output, Djp_jpoption.jp_jpoption());
	}

	@SuppressWarnings("static-access")
	public static void jp_printpf(Djp_jpc results, OutputStream output, Map<String, Double> jpopt) {
		PrintWriter pw;

		int i, k, nb, nl, ng, nout, mini, maxi, a, nxfmr;
		int OUT_ALL, OUT_ALL_LIM, OUT_V_LIM, OUT_LINE_LIM, OUT_PG_LIM, OUT_QG_LIM;
		int[] ties, xfmr, nzld, s_areas, nzsh, allg, ong, onld, out,
				ib, ig, igon, ildon, inzld, inzsh, ibrch, in_tie, out_tie, g, vg;
		boolean success, isOPF, isDC, anyP, anyQ, anyP_ld, anyQ_ld, anyF;
		boolean OUT_ANY, OUT_SYS_SUM, OUT_AREA_SUM, OUT_BUS, OUT_BRANCH, OUT_GEN, OUT_RAW;
		double baseMVA, et, ptol;
		double[] min, max;
		String str;
		Double f;

		Djp_bus bus;
		Djp_gen gen;
		Djp_branch branch;

		IntMatrix1D i2e, e2i, tiesm, ld, sorted_areas, s_areasm, shunt,
				isload, notload, gs, bs, a_gbus, a_bus, hasload, hasshunt,
				a_fbus, a_tbus, _g, _vg, rated, Uf, Ut;
		DoubleMatrix1D fchg, tchg, Pinj, Qinj, Ptie, Qtie, Qlim,
				genlamP = null, genlamQ = null, Ff, Ft, F_tol;
		DComplexMatrix1D tap, V, loss, z, br_b, cfchg, ctchg, Sf, St;

		pw = new PrintWriter(output);

		if (jpopt.get("OUT_ALL").equals(0) || jpopt.get("OUT_RAW").equals(0))
			return;

		baseMVA = results.baseMVA;
		bus = results.bus.copy();
		gen = results.gen.copy();
		branch = results.branch.copy();
		success = results.success;
		et = results.et;
		f = results.f;

		isOPF = (f != null);	/* FALSE -> only simple PF data, TRUE -> OPF data */

		/* options */
		isDC			= jpopt.get("PF_DC") == 1;	// use DC formulation?
		OUT_ALL				= jpopt.get("OUT_ALL").intValue();
		OUT_ANY			= OUT_ALL == 1;     // set to true if any pretty output is to be generated
		OUT_SYS_SUM		= OUT_ALL == 1 || (OUT_ALL == -1 && jpopt.get("OUT_SYS_SUM") == 1);
		OUT_AREA_SUM	= OUT_ALL == 1 || (OUT_ALL == -1 && jpopt.get("OUT_AREA_SUM") == 1);
		OUT_BUS			= OUT_ALL == 1 || (OUT_ALL == -1 && jpopt.get("OUT_BUS") == 1);
		OUT_BRANCH		= OUT_ALL == 1 || (OUT_ALL == -1 && jpopt.get("OUT_BRANCH") == 1);
		OUT_GEN			= OUT_ALL == 1 || (OUT_ALL == -1 && jpopt.get("OUT_GEN") == 1);
		OUT_ANY					= OUT_ANY || (OUT_ALL == -1 &&
			(OUT_SYS_SUM || OUT_AREA_SUM || OUT_BUS || OUT_BRANCH || OUT_GEN));
		if (OUT_ALL == -1) {
			OUT_ALL_LIM = jpopt.get("OUT_ALL_LIM").intValue();
		} else if (OUT_ALL == 1) {
			OUT_ALL_LIM = 2;
		} else {
			OUT_ALL_LIM = 0;
		}
		OUT_ANY = OUT_ANY || OUT_ALL_LIM >= 1;

		if (OUT_ALL_LIM == -1) {
			OUT_V_LIM       = jpopt.get("OUT_V_LIM").intValue();
			OUT_LINE_LIM    = jpopt.get("OUT_LINE_LIM").intValue();
			OUT_PG_LIM      = jpopt.get("OUT_PG_LIM").intValue();
			OUT_QG_LIM      = jpopt.get("OUT_QG_LIM").intValue();
		} else {
			OUT_V_LIM       = OUT_ALL_LIM;
			OUT_LINE_LIM    = OUT_ALL_LIM;
			OUT_PG_LIM      = OUT_ALL_LIM;
			OUT_QG_LIM      = OUT_ALL_LIM;
		}
		OUT_ANY			= OUT_ANY || (OUT_ALL_LIM == -1 && (OUT_V_LIM > 0 || OUT_LINE_LIM > 0 || OUT_PG_LIM > 0 || OUT_QG_LIM > 0));
		OUT_RAW	= jpopt.get("OUT_RAW") == 1;
		ptol = 1e-6;		// tolerance for displaying shadow prices

		/* internal bus number */
		i2e = bus.bus_i.copy();
		e2i = IntFactory1D.sparse.make(i2e.aggregate(ifunc.max, ifunc.identity) + 1);
		e2i.viewSelection(i2e.toArray()).assign( Djp_util.irange(bus.size()) );

		/* sizes */
		nb = bus.size();		// number of buses
		nl = branch.size();		// number of branches
		ng = gen.size();		// number of generators

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
		tiesm = bus.bus_area.viewSelection(e2i.viewSelection(branch.f_bus.toArray()).toArray());
		tiesm.assign(bus.bus_area.viewSelection(e2i.viewSelection(branch.t_bus.toArray()).toArray()), IntFunctions.equals);
		tiesm.assign(IntFunctions.equals(0));
		ties = tiesm.toArray();	// area inter-ties

		tap = DComplexFactory1D.dense.make(nl).assign(1, 0);	// default tap ratio = 1 for lines
		xfmr = Djp_util.nonzero(branch.tap);							// indices of transformers
		tap.viewSelection(xfmr).assignReal(branch.tap.viewSelection(xfmr));	// include transformer tap ratios
		tap.assign(Djp_util.polar(tap.getRealPart(), branch.shift, false));	// add phase shifters

		ld = Djp_util.intm(bus.Pd);
		ld.assign(Djp_util.intm(bus.Qd), ifunc.or);
		nzld = Djp_util.nonzero(ld);

		sorted_areas = IntSorting.quickSort.sort(bus.bus_area);
		s_areasm = sorted_areas.viewSelection(Djp_util.nonzero(Djp_util.diff(sorted_areas))).copy();
		s_areas = s_areasm.toArray();		// area numbers

		shunt = Djp_util.intm(bus.Gs);
		shunt.assign(Djp_util.intm(bus.Bs), ifunc.or);
		nzsh = Djp_util.nonzero(shunt);

		isload = Djp_isload.jp_isload(gen);
		notload = isload.copy();
		notload.assign(ifunc.equals(0));

		allg = Djp_util.nonzero(notload);
		gs = gen.gen_status.copy();
		gs.assign(notload, ifunc.and);
		ong  = Djp_util.nonzero(gs);
		gs = gen.gen_status.copy();
		gs.assign(isload, ifunc.and);
		onld = Djp_util.nonzero(gs);

		V = Djp_util.polar(bus.Vm, bus.Va, false);
		bs = branch.br_status.copy();

		out = Djp_util.nonzero( bs.assign(ifunc.equals(0)) );		// out-of-service branches
		nout = out.length;

		loss = DComplexFactory1D.dense.make(nl);;
		if (!isDC) {
			z = Djp_util.complex(branch.br_r, branch.br_x);
			loss.assign(V.viewSelection(e2i.viewSelection(branch.f_bus.toArray()).toArray()));
			loss.assign(tap, cfunc.div);
			loss.assign(V.viewSelection(e2i.viewSelection(branch.t_bus.toArray()).toArray()), cfunc.minus);
			loss.assign(cfunc.abs).assign(cfunc.square).assign(z, cfunc.div);
			loss.assign(cfunc.mult(baseMVA));
		}
		br_b = DComplexFactory1D.dense.make(nl);
		br_b.assignReal(branch.br_b).assign(cfunc.mult(baseMVA)).assign(cfunc.div(2));

		cfchg = DComplexFactory1D.dense.make(nl);
		cfchg.assign(V.viewSelection(e2i.viewSelection(branch.f_bus.toArray()).toArray()));
		cfchg.assign(tap, cfunc.div).assign(cfunc.abs).assign(cfunc.square).assign(br_b, cfunc.mult);
		fchg = cfchg.getRealPart();

		ctchg = DComplexFactory1D.dense.make(nl);
		ctchg.assign(V.viewSelection(e2i.viewSelection(branch.t_bus.toArray()).toArray()));
		ctchg.assign(cfunc.abs).assign(cfunc.square).assign(br_b, cfunc.mult);
		tchg = ctchg.getRealPart();

		loss.viewSelection(out).assign( DComplexFactory1D.dense.make(nout) );
		fchg.viewSelection(out).assign( DoubleFactory1D.dense.make(nout) );
		tchg.viewSelection(out).assign( DoubleFactory1D.dense.make(nout) );

		/* ----- print the stuff ----- */
		if (OUT_ANY) {
			/* convergence & elapsed time */
			if (success) {
				pw.printf("\nConverged in %.2f seconds", et);
			} else {
				pw.printf("\nDid not converge (%.2f seconds)\n", et);
			}

			/* objective function value */
			if (isOPF)
				pw.printf("\nObjective Function Value = %.2f $/hr", f);
		}

		if (OUT_SYS_SUM) {
			pw.printf("\n================================================================================");
			pw.printf("\n|     System Summary                                                           |");
			pw.printf("\n================================================================================");
			pw.printf("\n\nHow many?                How much?              P (MW)            Q (MVAr)");
			pw.printf("\n---------------------    -------------------  -------------  -----------------");
			pw.printf("\nBuses         %6d     Total Gen Capacity   %7.1f       %7.1f to %.1f", nb, gen.Pmax.viewSelection(allg).zSum(), gen.Qmin.viewSelection(allg).zSum(), gen.Qmax.viewSelection(allg).zSum());
			pw.printf("\nGenerators     %5d     On-line Capacity     %7.1f       %7.1f to %.1f", allg.length, gen.Pmax.viewSelection(ong).zSum(), gen.Qmin.viewSelection(ong).zSum(), gen.Qmax.viewSelection(ong).zSum());
			pw.printf("\nCommitted Gens %5d     Generation (actual)  %7.1f           %7.1f", ong.length, gen.Pg.viewSelection(ong).zSum(), gen.Qg.viewSelection(ong).zSum());
			pw.printf("\nLoads          %5d     Load                 %7.1f           %7.1f", nzld.length+onld.length, bus.Pd.viewSelection(nzld).zSum()-gen.Pg.viewSelection(onld).zSum(), bus.Qd.viewSelection(nzld).zSum()-gen.Qg.viewSelection(onld).zSum());
			pw.printf("\n  Fixed        %5d       Fixed              %7.1f           %7.1f", nzld.length, bus.Pd.viewSelection(nzld).zSum(), bus.Qd.viewSelection(nzld).zSum());
			pw.printf("\n  Dispatchable %5d       Dispatchable       %7.1f of %-7.1f%7.1f", onld.length, -gen.Pg.viewSelection(onld).zSum(), -gen.Pmin.viewSelection(onld).zSum(), -gen.Pg.viewSelection(onld).zSum());
			Pinj = DoubleFactory1D.dense.make(bus.Vm.viewSelection(nzsh).toArray());
			Pinj.assign(dfunc.square).assign(bus.Gs.viewSelection(nzsh), dfunc.mult);
			Qinj = DoubleFactory1D.dense.make(bus.Vm.viewSelection(nzsh).toArray());
			Qinj.assign(dfunc.square).assign(bus.Bs.viewSelection(nzsh), dfunc.mult);
			pw.printf("\nShunts         %5d     Shunt (inj)          %7.1f           %7.1f", nzsh.length, -Pinj.zSum(), Qinj.zSum());
			pw.printf("\nBranches       %5d     Losses (I^2 * Z)     %8.2f          %8.2f", nl, loss.getRealPart().zSum(), loss.getImaginaryPart().zSum());
			pw.printf("\nTransformers   %5d     Branch Charging (inj)     -            %7.1f", xfmr.length, fchg.zSum() + tchg.zSum() );
			Ptie = DoubleFactory1D.dense.make(branch.Pf.viewSelection(ties).toArray());
			Ptie.assign(branch.Pt.viewSelection(ties), dfunc.minus).assign(dfunc.abs);
			Qtie = DoubleFactory1D.dense.make(branch.Qf.viewSelection(ties).toArray());
			Qtie.assign(branch.Qt.viewSelection(ties), dfunc.minus).assign(dfunc.abs);
			pw.printf("\nInter-ties     %5d     Total Inter-tie Flow %7.1f           %7.1f", ties.length, Ptie.zSum() / 2, Qtie.zSum() / 2);
			pw.printf("\nAreas          %5d", s_areas.length);
			pw.printf("\n");
			pw.printf("\n                          Minimum                      Maximum");
			pw.printf("\n                 -------------------------  --------------------------------");

			min = bus.Vm.getMinLocation();
			max = bus.Vm.getMaxLocation();
			mini = new Double(min[1]).intValue();
			maxi = new Double(max[1]).intValue();
			pw.printf("\nVoltage Magnitude %7.3f p.u. @ bus %-4d     %7.3f p.u. @ bus %-4d", min[0], bus.bus_i.get(mini), max[0], bus.bus_i.get(maxi));

			min = bus.Va.getMinLocation();
			max = bus.Va.getMaxLocation();
			mini = new Double(min[1]).intValue();
			maxi = new Double(max[1]).intValue();
			pw.printf("\nVoltage Angle   %8.2f deg   @ bus %-4d   %8.2f deg   @ bus %-4d", min[0], bus.bus_i.get(mini), max[0], bus.bus_i.get(maxi));

			if (!isDC) {
				min = loss.getRealPart().getMinLocation();
				max = loss.getRealPart().getMaxLocation();
				mini = new Double(min[1]).intValue();
				maxi = new Double(max[1]).intValue();
				pw.printf("\nP Losses (I^2*R)             -              %8.2f MW    @ line %d-%d", max[0], branch.f_bus.get(maxi), branch.t_bus.get(maxi));
				min = loss.getImaginaryPart().getMinLocation();
				max = loss.getImaginaryPart().getMaxLocation();
				mini = new Double(min[1]).intValue();
				maxi = new Double(max[1]).intValue();
				pw.printf("\nQ Losses (I^2*X)             -              %8.2f MVAr  @ line %d-%d", max[0], branch.f_bus.get(maxi), branch.t_bus.get(maxi));
			}
			if (isOPF) {
				min = bus.lam_P.getMinLocation();
				max = bus.lam_P.getMaxLocation();
				mini = new Double(min[1]).intValue();
				maxi = new Double(max[1]).intValue();
				pw.printf("\nLambda P        %8.2f $/MWh @ bus %-4d   %8.2f $/MWh @ bus %-4d", min[0], bus.bus_i.get(mini), max[0], bus.bus_i.get(maxi));
				min = bus.lam_Q.getMinLocation();
				max = bus.lam_Q.getMaxLocation();
				mini = new Double(min[1]).intValue();
				maxi = new Double(max[1]).intValue();
				pw.printf("\nLambda Q        %8.2f $/MWh @ bus %-4d   %8.2f $/MWh @ bus %-4d", min[0], bus.bus_i.get(mini), max[0], bus.bus_i.get(maxi));
			}
			pw.printf("\n");
		}

		if (OUT_AREA_SUM) {
			pw.printf("\n================================================================================");
			pw.printf("\n|     Area Summary                                                             |");
			pw.printf("\n================================================================================");
			pw.printf("\nArea  # of      # of Gens        # of Loads         # of    # of   # of   # of");
			pw.printf("\n Num  Buses   Total  Online   Total  Fixed  Disp    Shunt   Brchs  Xfmrs   Ties");
			pw.printf("\n----  -----   -----  ------   -----  -----  -----   -----   -----  -----  -----");
			for (i = 0; i < s_areas.length; i++) {
				a = s_areas[i];
				ib = Djp_util.nonzero(bus.bus_area.copy().assign(ifunc.equals(a)));

				a_gbus = bus.bus_area.viewSelection( e2i.viewSelection(gen.gen_bus.toArray()).toArray() ).copy().assign(ifunc.equals(a));
				ig = Djp_util.nonzero(a_gbus.copy().assign(notload, ifunc.and));
//				ig = find(bus(e2i(gen(:, GEN_BUS)), BUS_AREA) == a & ~isload(gen));
				igon = Djp_util.nonzero(a_gbus.copy().assign(gen.gen_status, ifunc.and).assign(notload, ifunc.and));
//				igon = find(bus(e2i(gen(:, GEN_BUS)), BUS_AREA) == a & gen(:, GEN_STATUS) > 0 & ~isload(gen));
				ildon = Djp_util.nonzero(a_gbus.copy().assign(gen.gen_status, ifunc.and).assign(isload, ifunc.and));
//				ildon = find(bus(e2i(gen(:, GEN_BUS)), BUS_AREA) == a & gen(:, GEN_STATUS) > 0 & isload(gen));

				a_bus = bus.bus_area.copy().assign(ifunc.equals(a));
				hasload = Djp_util.intm( bus.Pd.copy().assign(dfunc.equals(0)) ).assign(ifunc.equals(0));
				hasload.assign(Djp_util.intm( bus.Qd.copy().assign(dfunc.equals(0)) ).assign(ifunc.equals(0)), ifunc.or);
				inzld = Djp_util.nonzero(a_bus.copy().assign(hasload, ifunc.and));
//				inzld = find(bus(:, BUS_AREA) == a & (bus(:, PD) | bus(:, QD)));
				hasshunt = Djp_util.intm( bus.Gs.copy().assign(dfunc.equals(0)) ).assign(ifunc.equals(0));
				hasshunt.assign(Djp_util.intm( bus.Bs.copy().assign(dfunc.equals(0)) ).assign(ifunc.equals(0)), ifunc.or);
				inzsh = Djp_util.nonzero(a_bus.copy().assign(hasshunt, ifunc.and));
//				inzsh = find(bus(:, BUS_AREA) == a & (bus(:, GS) | bus(:, BS)));

				a_fbus = bus.bus_area.viewSelection( e2i.viewSelection(branch.f_bus.toArray()).toArray() ).copy().assign(ifunc.equals(a));
				a_tbus = bus.bus_area.viewSelection( e2i.viewSelection(branch.t_bus.toArray()).toArray() ).copy().assign(ifunc.equals(a));
				ibrch = Djp_util.nonzero(a_fbus.copy().assign(a_tbus, ifunc.and));
//				ibrch = find(bus(e2i(branch(:, F_BUS)), BUS_AREA) == a & bus(e2i(branch(:, T_BUS)), BUS_AREA) == a);
				in_tie = Djp_util.nonzero( a_fbus.copy().assign(a_tbus.copy().assign(ifunc.equals(0)), ifunc.and) );
//				in_tie = find(bus(e2i(branch(:, F_BUS)), BUS_AREA) == a & bus(e2i(branch(:, T_BUS)), BUS_AREA) ~= a);
				out_tie = Djp_util.nonzero( a_fbus.copy().assign(ifunc.equals(0)).assign(a_tbus, ifunc.and) );
//				out_tie = find(bus(e2i(branch(:, F_BUS)), BUS_AREA) ~= a & bus(e2i(branch(:, T_BUS)), BUS_AREA) == a);
				if (xfmr.length == 0) {
					nxfmr = 0;
				} else {
					a_fbus = bus.bus_area.viewSelection( e2i.viewSelection(branch.f_bus.viewSelection(xfmr).toArray()).toArray() ).copy().assign(ifunc.equals(a));
					a_tbus = bus.bus_area.viewSelection( e2i.viewSelection(branch.t_bus.viewSelection(xfmr).toArray()).toArray() ).copy().assign(ifunc.equals(a));
					nxfmr = Djp_util.nonzero(a_fbus.copy().assign(a_tbus, ifunc.and)).length;
				}
				pw.printf("\n%3d  %6d   %5d  %5d   %5d  %5d  %5d   %5d   %5d  %5d  %5d",
					a, ib.length, ig.length, igon.length,
					inzld.length + ildon.length, inzld.length, ildon.length,
					inzsh.length, ibrch.length, nxfmr, in_tie.length + out_tie.length);
			}
			pw.printf("\n----  -----   -----  ------   -----  -----  -----   -----   -----  -----  -----");
			pw.printf("\nTot: %6d   %5d  %5d   %5d  %5d  %5d   %5d   %5d  %5d  %5d",
				nb, allg.length, ong.length, nzld.length + onld.length,
				nzld.length, onld.length, nzsh.length, nl, xfmr.length, ties.length);
			pw.printf("\n");

			pw.printf("\nArea      Total Gen Capacity           On-line Gen Capacity         Generation");
			pw.printf("\n Num     MW           MVAr            MW           MVAr             MW    MVAr");
			pw.printf("\n----   ------  ------------------   ------  ------------------    ------  ------");
			for (i = 0; i < s_areas.length; i++) {
				a = s_areas[i];

				a_gbus = bus.bus_area.viewSelection( e2i.viewSelection(gen.gen_bus.toArray()).toArray() ).copy().assign(ifunc.equals(a));
				ig = Djp_util.nonzero(a_gbus.copy().assign(notload, ifunc.and));
				igon = Djp_util.nonzero(a_gbus.copy().assign(gen.gen_status, ifunc.and).assign(notload, ifunc.and));

				pw.printf("\n%3d   %7.1f  %7.1f to %-7.1f  %7.1f  %7.1f to %-7.1f   %7.1f %7.1f",
					a, gen.Pmax.viewSelection(ig).zSum(), gen.Qmin.viewSelection(ig).zSum(), gen.Qmax.viewSelection(ig).zSum(),
					gen.Pmax.viewSelection(igon).zSum(), gen.Qmin.viewSelection(igon).zSum(), gen.Qmax.viewSelection(igon).zSum(),
					gen.Pg.viewSelection(igon).zSum(), gen.Qg.viewSelection(igon).zSum() );
			}
			pw.printf("\n----   ------  ------------------   ------  ------------------    ------  ------");
			pw.printf("\nTot:  %7.1f  %7.1f to %-7.1f  %7.1f  %7.1f to %-7.1f   %7.1f %7.1f",
					gen.Pmax.viewSelection(allg).zSum(), gen.Qmin.viewSelection(allg).zSum(), gen.Qmax.viewSelection(allg).zSum(),
					gen.Pmax.viewSelection(ong).zSum(), gen.Qmin.viewSelection(ong).zSum(), gen.Qmax.viewSelection(ong).zSum(),
					gen.Pg.viewSelection(ong).zSum(), gen.Qg.viewSelection(ong).zSum() );
			pw.printf("\n");

			pw.printf("\nArea    Disp Load Cap       Disp Load         Fixed Load        Total Load");
			pw.printf("\n Num      MW     MVAr       MW     MVAr       MW     MVAr       MW     MVAr");
			pw.printf("\n----    ------  ------    ------  ------    ------  ------    ------  ------");
			Qlim = gen.Qmin.copy().assign(dfunc.equals(0)).assign(gen.Qmax, dfunc.mult);
			Qlim.assign(gen.Qmax.copy().assign(dfunc.equals(0)).assign(gen.Qmin, dfunc.mult), dfunc.plus);
//			Qlim = (gen(:, QMIN) == 0) .* gen(:, QMAX) + (gen(:, QMAX) == 0) .* gen(:, QMIN);
			for (i = 0; i < s_areas.length; i++) {
				a = s_areas[i];

				a_gbus = bus.bus_area.viewSelection( e2i.viewSelection(gen.gen_bus.toArray()).toArray() ).copy().assign(ifunc.equals(a));
				ildon = Djp_util.nonzero(a_gbus.copy().assign(gen.gen_status, ifunc.and).assign(isload, ifunc.and));
				a_bus = bus.bus_area.copy().assign(ifunc.equals(a));
				hasload = Djp_util.intm( bus.Pd.copy().assign(dfunc.equals(0)) ).assign(ifunc.equals(0));
				hasload.assign(Djp_util.intm( bus.Qd.copy().assign(dfunc.equals(0)) ).assign(ifunc.equals(0)), ifunc.or);
				inzld = Djp_util.nonzero(a_bus.copy().assign(hasload, ifunc.and));

//				ildon = find(bus(e2i(gen(:, GEN_BUS)), BUS_AREA) == a & gen(:, GEN_STATUS) > 0 & isload(gen));
//				inzld = find(bus(:, BUS_AREA) == a & (bus(:, PD) | bus(:, QD)));
				pw.printf("\n%3d    %7.1f %7.1f   %7.1f %7.1f   %7.1f %7.1f   %7.1f %7.1f",
					a, -gen.Pmin.viewSelection(ildon).zSum(),
					-Qlim.viewSelection(ildon).zSum(),
					-gen.Pg.viewSelection(ildon).zSum(), -gen.Qg.viewSelection(ildon).zSum(),
					bus.Pd.viewSelection(inzld).zSum(), bus.Qd.viewSelection(inzld).zSum(),
					-gen.Pg.viewSelection(ildon).zSum() + bus.Pd.viewSelection(inzld).zSum(),
					-gen.Qg.viewSelection(ildon).zSum() + bus.Qd.viewSelection(inzld).zSum() );
			}
			pw.printf("\n----    ------  ------    ------  ------    ------  ------    ------  ------");
			pw.printf("\nTot:   %7.1f %7.1f   %7.1f %7.1f   %7.1f %7.1f   %7.1f %7.1f",
					-gen.Pmin.viewSelection(onld).zSum(),
					-Qlim.viewSelection(onld).zSum(),
					-gen.Pg.viewSelection(onld).zSum(), -gen.Qg.viewSelection(onld).zSum(),
					bus.Pd.viewSelection(nzld).zSum(), bus.Qd.viewSelection(nzld).zSum(),
					-gen.Pg.viewSelection(onld).zSum() + bus.Pd.viewSelection(nzld).zSum(),
					-gen.Qg.viewSelection(onld).zSum() + bus.Qd.viewSelection(nzld).zSum() );
			pw.printf("\n");
			pw.printf("\nArea      Shunt Inj        Branch      Series Losses      Net Export");
			pw.printf("\n Num      MW     MVAr     Charging      MW     MVAr       MW     MVAr");
			pw.printf("\n----    ------  ------    --------    ------  ------    ------  ------");
			for (i = 0; i < s_areas.length; i++) {
				a = s_areas[i];
				a_bus = bus.bus_area.copy().assign(ifunc.equals(a));
				hasshunt = Djp_util.intm( bus.Gs.copy().assign(dfunc.equals(0)) ).assign(ifunc.equals(0));
				hasshunt.assign(Djp_util.intm( bus.Bs.copy().assign(dfunc.equals(0)) ).assign(ifunc.equals(0)), ifunc.or);
				inzsh = Djp_util.nonzero(a_bus.copy().assign(hasshunt, ifunc.and));

				a_fbus = bus.bus_area.viewSelection( e2i.viewSelection(branch.f_bus.toArray()).toArray() ).copy().assign(ifunc.equals(a));
				a_tbus = bus.bus_area.viewSelection( e2i.viewSelection(branch.t_bus.toArray()).toArray() ).copy().assign(ifunc.equals(a));
				ibrch   = Djp_util.nonzero( a_fbus.copy().assign(a_tbus, ifunc.and).assign(branch.br_status, ifunc.and) );
				in_tie  = Djp_util.nonzero( a_fbus.copy().assign(a_tbus.copy().assign(ifunc.equals(0)), ifunc.and).assign(branch.br_status, ifunc.and) );
				out_tie = Djp_util.nonzero( a_fbus.copy().assign(ifunc.equals(0)).assign(a_tbus, ifunc.and).assign(branch.br_status, ifunc.and) );

				pw.printf("\n%3d    %7.1f %7.1f    %7.1f    %7.2f %7.2f   %7.1f %7.1f",
					a, -bus.Vm.viewSelection(inzsh).copy().assign(dfunc.square).assign(bus.Gs.viewSelection(inzsh), dfunc.mult).zSum(),
					bus.Vm.viewSelection(inzsh).copy().assign(dfunc.square).assign(bus.Bs.viewSelection(inzsh), dfunc.mult).zSum(),
					fchg.viewSelection(ibrch).zSum() + tchg.viewSelection(ibrch).zSum() + fchg.viewSelection(out_tie).zSum() + tchg.viewSelection(in_tie).zSum(),
					loss.viewSelection(ibrch).getRealPart().zSum() + loss.viewSelection(Djp_util.icat(in_tie, out_tie)).getRealPart().zSum() / 2,
					loss.viewSelection(ibrch).getImaginaryPart().zSum() + loss.viewSelection(Djp_util.icat(in_tie, out_tie)).getImaginaryPart().zSum() / 2,
					branch.Pt.viewSelection(in_tie).zSum()+branch.Pf.viewSelection(out_tie).zSum() - loss.viewSelection(Djp_util.icat(in_tie, out_tie)).getRealPart().zSum() / 2,
					branch.Qt.viewSelection(in_tie).zSum()+branch.Qf.viewSelection(out_tie).zSum() - loss.viewSelection(Djp_util.icat(in_tie, out_tie)).getImaginaryPart().zSum() / 2 );
			}
			pw.printf("\n----    ------  ------    --------    ------  ------    ------  ------");
			pw.printf("\nTot:   %7.1f %7.1f    %7.1f    %7.2f %7.2f       -       -",
				-bus.Vm.viewSelection(nzsh).assign(dfunc.square).assign(bus.Gs.viewSelection(nzsh), dfunc.mult).zSum(),
				bus.Vm.viewSelection(nzsh).assign(dfunc.square).assign(bus.Bs.viewSelection(nzsh), dfunc.mult).zSum(),
				fchg.zSum() + tchg.zSum(), loss.getRealPart().zSum(), loss.getImaginaryPart().zSum() );
			pw.printf("\n");
		}

		/* generator data */
		if (OUT_GEN) {
			if (isOPF) {
				genlamP = bus.lam_P.viewSelection((e2i.viewSelection(gen.gen_bus.toArray())).toArray());
				genlamQ = bus.lam_Q.viewSelection((e2i.viewSelection(gen.gen_bus.toArray())).toArray());
			}
			pw.printf("\n================================================================================");
			pw.printf("\n|     Generator Data                                                           |");
			pw.printf("\n================================================================================");
			pw.printf("\n Gen   Bus   Status     Pg        Qg   ");
			if (isOPF) { pw.printf("   Lambda ($/MVA-hr)"); }
			pw.printf("\n  #     #              (MW)     (MVAr) ");
			if (isOPF) { pw.printf("     P         Q    "); }
			pw.printf("\n----  -----  ------  --------  --------");
			if (isOPF) { pw.printf("  --------  --------"); }
			for (k = 0; k < ong.length; k++) {
				i = ong[k];
				pw.printf("\n%3d %6d     %2d ", i, gen.gen_bus.get(i), gen.gen_status.get(i));
				if (gen.gen_status.get(i) > 0 && (gen.Pg.get(i) > 0 || gen.Qg.get(i) > 0)) {
					pw.printf("%10.2f%10.2f", gen.Pg.get(i), gen.Qg.get(i));
				} else {
					pw.printf("       -         -  ");
				}
				if (isOPF) { pw.printf("%10.2f%10.2f", genlamP.get(i), genlamQ.get(i)); }
			}
			pw.printf("\n                     --------  --------");
			pw.printf("\n            Total: %9.2f%10.2f", gen.Pg.viewSelection(ong).zSum(), gen.Qg.viewSelection(ong).zSum());
			pw.printf("\n");
			if (onld.length > 1) {
				pw.printf("\n================================================================================");
				pw.printf("\n|     Dispatchable Load Data                                                   |");
				pw.printf("\n================================================================================");
				pw.printf("\n Gen   Bus   Status     Pd        Qd   ");
				if (isOPF) { pw.printf("   Lambda ($/MVA-hr)"); }
				pw.printf("\n  #     #              (MW)     (MVAr) ");
				if (isOPF) { pw.printf("     P         Q    "); }
				pw.printf("\n----  -----  ------  --------  --------");
				if (isOPF) { pw.printf("  --------  --------"); }
				for (k = 0; k < onld.length; k++) {
					i = onld[k];
					pw.printf("\n%3d %6d     %2d ", i, gen.gen_bus.get(i), gen.gen_status.get(i));
					if (gen.gen_status.get(i) > 0 && (gen.Pg.get(i) > 0 || gen.Qg.get(i) > 0)) {
						pw.printf("%10.2f%10.2f", -gen.Pg.get(i), -gen.Qg.get(i));
					} else {
						pw.printf("       -         -  ");
					}
					if (isOPF) { pw.printf("%10.2f%10.2f", genlamP.get(i), genlamQ.get(i)); }
				}
				pw.printf("\n                     --------  --------");
				pw.printf("\n            Total: %9.2f%10.2f", -gen.Pg.viewSelection(onld).zSum(), -gen.Qg.viewSelection(onld).zSum());
				pw.printf("\n");
			}
		}

		/* bus data */
		if (OUT_BUS) {
			pw.printf("\n================================================================================");
			pw.printf("\n|     Bus Data                                                                 |");
			pw.printf("\n================================================================================");
			pw.printf("\n Bus      Voltage          Generation             Load        ");
			if (isOPF) { pw.printf("  Lambda($/MVA-hr)"); }
			pw.printf("\n  #   Mag(pu) Ang(deg)   P (MW)   Q (MVAr)   P (MW)   Q (MVAr)");
			if (isOPF) { pw.printf("     P        Q   "); }
			pw.printf("\n----- ------- --------  --------  --------  --------  --------");
			if (isOPF) { pw.printf("  -------  -------"); }
			for (i = 0; i < nb; i++) {
				pw.printf("\n%5d%7.3f%9.3f", bus.bus_i.get(i), bus.Vm.get(i), bus.Va.get(i));

				_g = gen.gen_bus.copy().assign(ifunc.equals(bus.bus_i.get(i)));
				g = _g.assign(gen.gen_status, ifunc.and).assign(notload, ifunc.and).toArray();
				_vg = gen.gen_bus.copy().assign(ifunc.equals(bus.bus_i.get(i)));
				vg = _vg.assign(gen.gen_status, ifunc.and).assign(isload, ifunc.and).toArray();

				if (g.length > 0) {
					pw.printf("%10.2f%10.2f", gen.Pg.viewSelection(g).zSum(), gen.Qg.viewSelection(g).zSum());
				} else {
					pw.printf("       -         -  ");
				}
				if (bus.Pd.get(i) > 0 || bus.Qd.get(i) > 0 || vg.length > 0) {
					if (vg.length > 0) {
						pw.printf("%10.2f*%9.2f*", bus.Pd.get(i) - gen.Pg.viewSelection(vg).zSum(),
								bus.Qd.get(i) - gen.Qg.viewSelection(vg).zSum());
					} else {
						pw.printf("%10.2f%10.2f ", bus.Pd.get(i), bus.Qd.get(i));
					}
				} else {
					pw.printf("       -         -   ");
				}
				if (isOPF) {
					pw.printf("%9.3f", bus.lam_P.get(i));
					if (dfunc.abs.apply(bus.lam_Q.get(i)) > ptol) {
						pw.printf("%8.3f", bus.lam_Q.get(i));
					} else {
						pw.printf("     -");
					}
				}
			}
			pw.printf("\n                        --------  --------  --------  --------");
			pw.printf("\n               Total: %9.2f %9.2f %9.2f %9.2f",
				gen.Pg.viewSelection(ong).zSum(), gen.Qg.viewSelection(ong).zSum(),
				bus.Pd.viewSelection(nzld).zSum() - gen.Pg.viewSelection(onld).zSum(),
				bus.Qd.viewSelection(nzld).zSum() - gen.Qg.viewSelection(onld).zSum() );
			pw.printf("\n");
		}

		/* branch data */
		if (OUT_BRANCH) {
			pw.printf("\n================================================================================");
			pw.printf("\n|     Branch Data                                                              |");
			pw.printf("\n================================================================================");
			pw.printf("\nBrnch   From   To    From Bus Injection   To Bus Injection     Loss (I^2 * Z)  ");
			pw.printf("\n  #     Bus    Bus    P (MW)   Q (MVAr)   P (MW)   Q (MVAr)   P (MW)   Q (MVAr)");
			pw.printf("\n-----  -----  -----  --------  --------  --------  --------  --------  --------");
			for (i = 0; i < nl; i++) {
				pw.printf("\n%4d%7d%7d%10.2f%10.2f%10.2f%10.2f%10.3f%10.2f",
						i, branch.f_bus.get(i), branch.t_bus.get(i),
						branch.Pf.get(i), branch.Qf.get(i), branch.Pt.get(i), branch.Qt.get(i),
						loss.getRealPart().get(i), loss.getImaginaryPart().get(i) );
			}
			pw.printf("\n                                                             --------  --------");
			pw.printf("\n                                                    Total:%10.3f%10.2f",
					loss.getRealPart().zSum(), loss.getImaginaryPart().zSum());
			pw.printf("\n");
		}

		/* -----  constraint data  ----- */
		if (isOPF) {
			double ctol = jpopt.get("OPF_VIOLATION");   // constraint violation tolerance
			// voltage constraints
			if (!isDC && ( OUT_V_LIM == 2 || (OUT_V_LIM == 1 &&
					(Djp_util.any( bus.Vm.copy().assign(bus.Vmin.assign(dfunc.plus(ctol)), dfunc.less) ) ||
							Djp_util.any( bus.Vm.copy().assign(bus.Vmax.assign(dfunc.minus(ctol)), dfunc.greater) ) ||
							Djp_util.any( bus.mu_Vmin.copy().assign(dfunc.greater(ptol)) ) ||
							Djp_util.any( bus.mu_Vmax.copy().assign(dfunc.greater(ptol)) )))) ) {
				pw.printf("\n================================================================================");
				pw.printf("\n|     Voltage Constraints                                                      |");
				pw.printf("\n================================================================================");
				pw.printf("\nBus #  Vmin mu    Vmin    |V|   Vmax    Vmax mu");
				pw.printf("\n-----  --------   -----  -----  -----   --------");
				for (i = 0; i < nb; i++) {
					if (OUT_V_LIM == 2 || (OUT_V_LIM == 1 &&
							(bus.Vm.get(i) < bus.Vmin.get(i) + ctol ||
									bus.Vm.get(i) > bus.Vmax.get(i) - ctol ||
									bus.mu_Vmin.get(i) > ptol || bus.mu_Vmax.get(i) > ptol)) ) {
						pw.printf("\n%5d", bus.bus_i.get(i));
						if (bus.Vm.get(i) < bus.Vmin.get(i) + ctol || bus.mu_Vmin.get(i) > ptol) {
							pw.printf("%10.3f", bus.mu_Vmin.get(i));
						} else {
							pw.printf("      -   ");
						}
						pw.printf("%8.3f%7.3f%7.3f", bus.Vmin.get(i), bus.Vm.get(i), bus.Vmax.get(i));
						if (bus.Vm.get(i) > bus.Vmax.get(i) - ctol || bus.mu_Vmax.get(i) > ptol) {
							pw.printf("%10.3f", bus.mu_Vmax.get(i));
						} else {
							pw.printf("      -    ");
						}
					}
				}
				pw.printf("\n");
			}

			/* generator constraints */
			anyP = ( Djp_util.any( gen.Pg.viewSelection(ong).copy().assign(gen.Pmin.viewSelection(ong).assign(dfunc.plus(ctol)) , dfunc.less) ) ||
					Djp_util.any( gen.Pg.viewSelection(ong).copy().assign(gen.Pmax.viewSelection(ong).assign(dfunc.minus(ctol)), dfunc.less) ) );
			if (gen.mu_Pmin != null)  // FIXME Should add zeros for result fields when loading case
				anyP = anyP || Djp_util.any( gen.mu_Pmin.viewSelection(ong).assign(dfunc.greater(ptol)) );
			if (gen.mu_Pmax != null)
				anyP = anyP || Djp_util.any( gen.mu_Pmax.viewSelection(ong).assign(dfunc.greater(ptol)) );

			anyQ = ( Djp_util.any( gen.Qg.viewSelection(ong).copy().assign(gen.Qmin.viewSelection(ong).assign(dfunc.plus(ctol)) , dfunc.less) ) ||
					Djp_util.any( gen.Qg.viewSelection(ong).copy().assign(gen.Qmax.viewSelection(ong).assign(dfunc.minus(ctol)), dfunc.less) ) );
			if (gen.mu_Qmin != null)
				anyQ = anyQ || Djp_util.any( gen.mu_Qmin.viewSelection(ong).assign(dfunc.greater(ptol)) );
			if (gen.mu_Qmax != null)
				anyQ = anyQ || Djp_util.any( gen.mu_Qmax.viewSelection(ong).assign(dfunc.greater(ptol)) );

			if (OUT_PG_LIM == 2 ||
					(OUT_PG_LIM == 1 && anyP) ||
					( !isDC && (OUT_QG_LIM == 2 || (OUT_QG_LIM == 1 && anyQ))) ) {
				pw.printf("\n================================================================================");
				pw.printf("\n|     Generation Constraints                                                   |");
				pw.printf("\n================================================================================");
			}
			/* generator P constraints */
			if (OUT_PG_LIM == 2 || (OUT_PG_LIM == 1 && anyP)) {
				pw.printf("\n Gen   Bus                Active Power Limits");
				pw.printf("\n  #     #    Pmin mu    Pmin       Pg       Pmax    Pmax mu");
				pw.printf("\n----  -----  -------  --------  --------  --------  -------");
				for (k = 0; k < ong.length; k++) {
					i = ong[k];
					if (OUT_PG_LIM == 2 || (OUT_PG_LIM == 1 &&
								(gen.Pg.get(i) < gen.Pmin.get(i) + ctol ||
								gen.Pg.get(i) > gen.Pmax.get(i) - ctol ||
								gen.mu_Pmin.get(i) > ptol || gen.mu_Pmax.get(i) > ptol))) {
						pw.printf("\n%4d%6d ", i, gen.gen_bus.get(i));
						if (gen.Pg.get(i) < gen.Pmin.get(i) + ctol || gen.mu_Pmin.get(i) > ptol) {
							pw.printf("%8.3f", gen.mu_Pmin.get(i));
						} else {
							pw.printf("     -  ");
						}
						if (gen.Pg.get(i) > 0) {
							pw.printf("%10.2f%10.2f%10.2f", gen.Pmin.get(i), gen.Pg.get(i), gen.Pmax.get(i));
						} else {
							pw.printf("%10.2f       -  %10.2f", gen.Pmin.get(i), gen.Pmax.get(i));
						}
						if (gen.Pg.get(i) > gen.Pmax.get(i) - ctol || gen.mu_Pmax.get(i) > ptol) {
							pw.printf("%9.3f", gen.mu_Pmax.get(i));
						} else {
							pw.printf("      -  ");
						}
					}
				}
				pw.printf("\n");
			}
			/* generator Q constraints */
			if (!isDC && (OUT_QG_LIM == 2 || (OUT_QG_LIM == 1 && anyQ))) {
				pw.printf("\nGen  Bus              Reactive Power Limits");
				pw.printf("\n #    #   Qmin mu    Qmin       Qg       Qmax    Qmax mu");
				pw.printf("\n---  ---  -------  --------  --------  --------  -------");
				for (k = 0; k < ong.length; k++) {
					i = ong[k];
					if (OUT_QG_LIM == 2 || (OUT_QG_LIM == 1 &&
								(gen.Qg.get(i) < gen.Qmin.get(i) + ctol ||
								gen.Qg.get(i) > gen.Qmax.get(i) - ctol ||
								gen.mu_Qmin.get(i) > ptol || gen.mu_Qmax.get(i) > ptol))) {
						pw.printf("\n%3d%5d", i, gen.gen_bus.get(i));
						if (gen.Qg.get(i) < gen.Qmin.get(i) + ctol || gen.mu_Qmin.get(i) > ptol) {
							pw.printf("%8.3f", gen.mu_Qmin.get(i));
						} else {
							pw.printf("     -  ");
						}
						if (gen.Qg.get(i) > 0) {
							pw.printf("%10.2f%10.2f%10.2f", gen.Qmin.get(i), gen.Qg.get(i), gen.Qmax.get(i));
						} else {
							pw.printf("%10.2f       -  %10.2f", gen.Qmin.get(i), gen.Qmax.get(i));
						}
						if (gen.Qg.get(i) > gen.Qmax.get(i) - ctol || gen.mu_Qmax.get(i) > ptol) {
							pw.printf("%9.3f", gen.mu_Qmax.get(i));
						} else {
							pw.printf("      -  ");
						}
					}
				}
				pw.printf("\n");
			}

			/* dispatchable load constraints */
			anyP_ld = ( Djp_util.any( gen.Pg.viewSelection(onld).copy().assign(gen.Pmin.viewSelection(onld).assign(dfunc.plus(ctol)) , dfunc.less) ) ||
					Djp_util.any( gen.Pg.viewSelection(onld).copy().assign(gen.Pmax.viewSelection(onld).assign(dfunc.minus(ctol)), dfunc.less) ) );
			if (gen.mu_Pmin != null)  // FIXME Should add zeros for result fields when loading case
				anyP_ld = anyP_ld || Djp_util.any( gen.mu_Pmin.viewSelection(onld).assign(dfunc.greater(ptol)) );
			if (gen.mu_Pmax != null)
				anyP_ld = anyP_ld || Djp_util.any( gen.mu_Pmax.viewSelection(onld).assign(dfunc.greater(ptol)) );

			anyQ_ld = ( Djp_util.any( gen.Qg.viewSelection(onld).copy().assign(gen.Qmin.viewSelection(onld).assign(dfunc.plus(ctol)) , dfunc.less) ) ||
					Djp_util.any( gen.Qg.viewSelection(onld).copy().assign(gen.Qmax.viewSelection(onld).assign(dfunc.minus(ctol)), dfunc.less) ) );
			if (gen.mu_Qmin != null)
				anyQ_ld = anyQ_ld || Djp_util.any( gen.mu_Qmin.viewSelection(onld).assign(dfunc.greater(ptol)) );
			if (gen.mu_Qmax != null)
				anyQ_ld = anyQ_ld || Djp_util.any( gen.mu_Qmax.viewSelection(onld).assign(dfunc.greater(ptol)) );

			if (OUT_PG_LIM == 2 || OUT_QG_LIM == 2 ||
					(OUT_PG_LIM == 1 && anyP_ld) ||
					(OUT_QG_LIM == 1 && (anyQ_ld))) {
				pw.printf("\n================================================================================");
				pw.printf("\n|     Dispatchable Load Constraints                                            |");
				pw.printf("\n================================================================================");
			}
			/* dispatchable load P constraints */
			if (OUT_PG_LIM == 2 || (OUT_PG_LIM == 1 && anyP_ld)) {
				pw.printf("\nGen  Bus               Active Power Limits");
				pw.printf("\n #    #   Pmin mu    Pmin       Pg       Pmax    Pmax mu");
				pw.printf("\n---  ---  -------  --------  --------  --------  -------");
				for (k = 0; k < onld.length; k++) {
					i = onld[k];
					if (OUT_PG_LIM == 2 || (OUT_PG_LIM == 1 &&
								(gen.Pg.get(i) < gen.Pmin.get(i) + ctol ||
								gen.Pg.get(i) > gen.Pmax.get(i) - ctol ||
								gen.mu_Pmin.get(i) > ptol || gen.mu_Pmax.get(i) > ptol))) {
						pw.printf("\n%3d%5d", i, gen.gen_bus.get(i));
						if (gen.Pg.get(i) < gen.Pmin.get(i) + ctol || gen.mu_Pmin.get(i) > ptol) {
							pw.printf("%8.3f", gen.mu_Pmin.get(i));
						} else {
							pw.printf("     -  ");
						}
						if (gen.Pg.get(i) > 0) {
							pw.printf("%10.2f%10.2f%10.2f", gen.Pmin.get(i), gen.Pg.get(i), gen.Pmax.get(i));
						} else {
							pw.printf("%10.2f       -  %10.2f", gen.Pmin.get(i), gen.Pmax.get(i));
						}
						if (gen.Pg.get(i) > gen.Pmax.get(i) - ctol || gen.mu_Pmax.get(i) > ptol) {
							pw.printf("%9.3f", gen.mu_Pmax.get(i));
						} else {
							pw.printf("      -  ");
						}
					}
				}
				pw.printf("\n");
			}

			/* dispatchable load Q constraints */
			if (!isDC && (OUT_QG_LIM == 2 || (OUT_QG_LIM == 1 && anyQ_ld))) {
				pw.printf("\nGen  Bus              Reactive Power Limits");
				pw.printf("\n #    #   Qmin mu    Qmin       Qg       Qmax    Qmax mu");
				pw.printf("\n---  ---  -------  --------  --------  --------  -------");
				for (k = 0; k < onld.length; k++) {
					i = onld[k];
					if (OUT_QG_LIM == 2 || (OUT_QG_LIM == 1 &&
								(gen.Qg.get(i) < gen.Qmin.get(i) + ctol ||
								gen.Qg.get(i) > gen.Qmax.get(i) - ctol ||
								gen.mu_Qmin.get(i) > ptol || gen.mu_Qmax.get(i) > ptol))) {
						pw.printf("\n%3d%5d", i, gen.gen_bus.get(i));
						if (gen.Qg.get(i) < gen.Qmin.get(i) + ctol || gen.mu_Qmin.get(i) > ptol) {
							pw.printf("%8.3f", gen.mu_Qmin.get(i));
						} else {
							pw.printf("     -  ");
						}
						if (gen.Qg.get(i) > 0) {
							pw.printf("%10.2f%10.2f%10.2f", gen.Qmin.get(i), gen.Qg.get(i), gen.Qmax.get(i));
						} else {
							pw.printf("%10.2f       -  %10.2f", gen.Qmin.get(i), gen.Qmax.get(i));
						}
						if (gen.Qg.get(i) > gen.Qmax.get(i) - ctol || gen.mu_Qmax.get(i) > ptol) {
							pw.printf("%9.3f", gen.mu_Qmax.get(i));
						} else {
							pw.printf("      -  ");
						}
					}
				}
				pw.printf("\n");
			}

			/* line flow constraints */
			if (jpopt.get("OPF_FLOW_LIM") == 1 || isDC) {  // P limit
				Ff = branch.Pf.copy();
				Ft = branch.Pt.copy();
				str = "\n  #     Bus    Pf  mu     Pf      |Pmax|      Pt      Pt  mu   Bus";
			} else if (jpopt.get("OPF_FLOW_LIM") == 2) {   // |I| limit
				Sf = Djp_util.complex(branch.Pf, branch.Qf);
				St = Djp_util.complex(branch.Pt, branch.Qt);
				Sf.assign(V.viewSelection(e2i.viewSelection(branch.f_bus.toArray()).toArray()), cfunc.div);
				St.assign(V.viewSelection(e2i.viewSelection(branch.t_bus.toArray()).toArray()), cfunc.div);
				Ff = Sf.assign(cfunc.abs).getRealPart();
				Ft = St.assign(cfunc.abs).getRealPart();
				str = "\n  #     Bus   |If| mu    |If|     |Imax|     |It|    |It| mu   Bus";
			} else {                // |S| limit
				Sf = Djp_util.complex(branch.Pf, branch.Qf);
				St = Djp_util.complex(branch.Pt, branch.Qt);
				Ff = Sf.assign(cfunc.abs).getRealPart();
				Ft = St.assign(cfunc.abs).getRealPart();
				str = "\n  #     Bus   |Sf| mu    |Sf|     |Smax|     |St|    |St| mu   Bus";
			}
			rated = Djp_util.intm( branch.rate_a.copy().assign(dfunc.equals(0)) ).assign(ifunc.equals(0));
			F_tol = branch.rate_a.copy().assign(dfunc.minus(ctol));
			Uf = Djp_util.intm(Ff.copy().assign(dfunc.abs).assign(F_tol, dfunc.greater));	// constrained from
			Ut = Djp_util.intm(Ft.copy().assign(dfunc.abs).assign(F_tol, dfunc.greater));	// constrained to

			anyF = (Djp_util.any( rated.copy().assign(Uf, ifunc.and) ) ||
					Djp_util.any( rated.copy().assign(Ut, ifunc.and) ));
			if (branch.mu_Sf != null)  // FIXME Should add zeros for result fields when loading case
				anyF = anyF || Djp_util.any( branch.mu_Sf.copy().assign(dfunc.greater(ptol)) );
			if (branch.mu_St != null)
				anyF = anyF || Djp_util.any( branch.mu_St.copy().assign(dfunc.greater(ptol)) );

			if (OUT_LINE_LIM == 2 || (OUT_LINE_LIM == 1 && anyF)) {
				pw.printf("\n================================================================================");
				pw.printf("\n|     Branch Flow Constraints                                                  |");
				pw.printf("\n================================================================================");
				pw.printf("\nBrnch   From     \"From\" End        Limit       \"To\" End        To");
				pw.printf(str);
				pw.printf("\n-----  -----  -------  --------  --------  --------  -------  -----");
				for (i = 0; i < nl; i++) {
					if (OUT_LINE_LIM == 2 || (OUT_LINE_LIM == 1 &&
						((branch.rate_a.get(i) != 0 && dfunc.abs.apply(Ff.get(i)) > branch.rate_a.get(i) - ctol) ||
							(branch.rate_a.get(i) != 0 && dfunc.abs.apply(Ft.get(i)) > branch.rate_a.get(i) - ctol) ||
							branch.mu_Sf.get(i) > ptol || branch.mu_St.get(i) > ptol))) {
						pw.printf("\n%4d%7d", i, branch.f_bus.get(i));
						if (Ff.get(i) > branch.rate_a.get(i) - ctol || branch.mu_Sf.get(i) > ptol) {
							pw.printf("%10.3f", branch.mu_Sf.get(i));
						} else {
							pw.printf("      -   ");
						}
						pw.printf("%9.2f%10.2f%10.2f", Ff.get(i), branch.rate_a.get(i), Ft.get(i));
						if (Ft.get(i) > branch.rate_a.get(i) - ctol || branch.mu_St.get(i) > ptol) {
							pw.printf("%10.3f", branch.mu_St.get(i));
						} else {
							pw.printf("      -   ");
						}
						pw.printf("%6d", branch.t_bus.get(i));
					}
				}
				pw.printf("\n");
			}
		}
		// TODO: execute userfcn callbacks for 'printpf' stage
	}

}
