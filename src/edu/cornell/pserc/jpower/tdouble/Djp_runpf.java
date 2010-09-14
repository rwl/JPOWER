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

package edu.cornell.pserc.jpower.tdouble;

import java.util.Map;

import cern.colt.list.tint.IntArrayList;
import cern.colt.matrix.AbstractMatrix;
import cern.colt.matrix.tdouble.DoubleFactory1D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tint.IntMatrix1D;
import cern.jet.math.tdouble.DoubleFunctions;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_branch;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_bus;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_gen;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_jpc;
import edu.cornell.pserc.util.tdouble.Djp_util;

/**
 * Runs a power flow
 *
 * Runs a power flow (full AC Newton's method by default) and optionally
 * returns the solved values in the data matrices, a flag which is true if
 * the algorithm was successful in finding a solution, and the elapsed
 * time in seconds. All input arguments are optional. If casename is
 * provided it specifies the name of the input data file or struct
 * containing the power flow data. The default value is 'case9'.
 *
 * If the ppopt is provided it overrides the default PYPOWER options
 * vector and can be used to specify the solution algorithm and output
 * options among other things. If the 3rd argument is given the pretty
 * printed output will be appended to the file whose name is given in
 * fname. If solvedcase is specified the solved case will be written to a
 * case file in MATPOWER format with the specified name. If solvedcase
 * ends with '.mat' it saves the case as a MAT-file otherwise it saves it
 * as an M-file.
 *
 * If the ENFORCE_Q_LIMS options is set to true (default is false) then if
 * any generator reactive power limit is violated after running the AC
 * power flow, the corresponding bus is converted to a PQ bus, with Qg at
 * the limit, and the case is re-run. The voltage magnitude at the bus
 * will deviate from the specified value in order to satisfy the reactive
 * power limit. If the reference bus is converted to PQ, the first
 * remaining PV bus will be used as the slack bus for the next iteration.
 * This may result in the real power output at this generator being
 * slightly off from the specified values.
 *
 * @author Ray Zimmerman (rz10@cornell.edu)
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class Djp_runpf {

	private static final DoubleFunctions dfunc = DoubleFunctions.functions;

	public static Djp_jpc jp_runpf() {
		return jp_runpf("case9");
	}

	public static Djp_jpc jp_runpf(String casedata) {
		return jp_runpf(casedata, Djp_jpoption.jp_jpoption());
	}

	public static Djp_jpc jp_runpf(String casedata, Map<String, Double> jpopt) {
		return jp_runpf(casedata, jpopt, "");
	}

	public static Djp_jpc jp_runpf(String casedata, Map<String, Double> jpopt, String fname) {
		return jp_runpf(casedata, jpopt, fname, "");
	}

	public static Djp_jpc jp_runpf(String casedata, Map<String, Double> jpopt, String fname, String solvedcase) {
		Djp_jpc jpc = Djp_loadcase.jp_loadcase(casedata);
		return jp_runpf(jpc, jpopt, fname, "");
	}

	public static Djp_jpc jp_runpf(Djp_jpc casedata) {
		return jp_runpf(casedata, Djp_jpoption.jp_jpoption());
	}

	public static Djp_jpc jp_runpf(Djp_jpc casedata, Map<String, Double> jpopt) {
		return jp_runpf(casedata, jpopt, "");
	}

	public static Djp_jpc jp_runpf(Djp_jpc casedata, Map<String, Double> jpopt, String fname) {
		return jp_runpf(casedata, jpopt, "", "");
	}

	/**
	 *
	 * @param casedata
	 * @param jpopt
	 * @param fname
	 * @param solvedcase
	 * @return
	 */
	@SuppressWarnings("static-access")
	public static Djp_jpc jp_runpf(Djp_jpc casedata, Map<String, Double> jpopt,
			String fname, String solvedcase) {

		/* options */
		int verbose = jpopt.get("VERBOSE").intValue();
		boolean qlim = jpopt.get("ENFORCE_Q_LIMS") != 0.0;	/* enforce Q limits on gens? */
		boolean dc = jpopt.get("PF_DC") != 0.0;				/* use DC formulation? */

		/* read data */
		Djp_jpc jpc = Djp_loadcase.jp_loadcase(casedata);

		/* add zero columns to branch for flows if needed */
		Djp_branch branch = jpc.branch;
		if (branch.Qt == null) {
			int nl = branch.size();
			branch.Pf = DoubleFactory1D.dense.make(nl);
			branch.Qf = DoubleFactory1D.dense.make(nl);
			branch.Pt = DoubleFactory1D.dense.make(nl);
			branch.Qt = DoubleFactory1D.dense.make(nl);
		}

		/* convert to internal indexing */
		jpc = Djp_ext2int.jp_ext2int(jpc);
		double baseMVA = jpc.baseMVA;
		Djp_bus bus = jpc.bus;
		Djp_gen gen = jpc.gen;
		branch = jpc.branch;

		/* get bus index lists of each type of bus */
		IntMatrix1D[] bustypes = Djp_bustypes.jp_bustypes(bus, gen);
		int ref = bustypes[0].get(0);
		int[] pv = bustypes[1].toArray();
		int[] pq = bustypes[2].toArray();

		/* generator info */
		int[] on = Djp_util.nonzero(gen.gen_status);     // which generators are on?
		// what buses are they at?
		int[] gbus = gen.gen_bus.viewSelection(on).toArray();

		/* -----  run the power flow  ----- */
		long t0 = System.currentTimeMillis();
		if (verbose > 0) {
			Map<String, String> v = Djp_jpver.jp_jpver("all");
			System.out.printf("\nJPOWER Version %s, %s", v.get("Version"), v.get("Date"));
		}

		boolean success = false;
		if (dc) {                                 // DC formulation
			if (verbose > 0)
				System.out.printf(" -- DC Power Flow\n");

			/* initial state */
			DoubleMatrix1D Va0 = bus.Va.copy();
			Va0.assign(dfunc.chain(dfunc.mult(Math.PI), dfunc.div(180)));

			/* build B matrices and phase shift injections */
			AbstractMatrix[] Bdc = Djp_makeBdc.jp_makeBdc(baseMVA, bus, branch);
			DoubleMatrix2D B = (DoubleMatrix2D) Bdc[0];
			DoubleMatrix2D Bf = (DoubleMatrix2D) Bdc[1];
			DoubleMatrix1D Pbusinj = (DoubleMatrix1D) Bdc[2];
			DoubleMatrix1D Pfinj = (DoubleMatrix1D) Bdc[3];

			/* compute complex bus power injections (generation - load) */
			/* adjusted for phase shifters and real shunts */
			DoubleMatrix1D Pbus = Djp_makeSbus.jp_makeSbus(baseMVA, bus, gen).getRealPart();
			Pbus.assign(Pbusinj, dfunc.minus);
			Pbus.assign(bus.Gs, dfunc.chain(dfunc.div(baseMVA), dfunc.minus));

			/* "run" the power flow */
			DoubleMatrix1D Va = Djp_dcpf.jp_dcpf(B, Pbus, Va0, ref, pv, pq);

			/* update data matrices with solution */
			branch.Qf.assign(0);
			branch.Qt.assign(0);
			branch.Pf.assign( Bf.zMult(Va, null).assign(Pfinj, dfunc.plus).assign(dfunc.mult(baseMVA)) );
			branch.Pt.assign( branch.Pt.copy().assign(dfunc.neg) );
			bus.Vm.assign(1);
			bus.Va.assign(Va);
			bus.Va.assign(dfunc.chain(dfunc.mult(180), dfunc.div(Math.PI)));
			// update Pg for swing generator (note: other gens at ref bus are accounted for in Pbus)
			//      Pg = Pinj + Pload + Gs
			//      newPg = oldPg + newPinj - oldPinj
			int refgen = 0;
			for (int i : gbus) if (i == ref) { refgen = i; break; }
			gen.Pg.set(on[refgen], gen.Pg.get(on[refgen]) + (B.viewRow(ref).zDotProduct(Va) - Pbus.get(ref)) * baseMVA);

			success = true;
		} else {                                  // AC formulation

		}
		jpc.et = (System.currentTimeMillis() - t0) / 1000F;
		jpc.success = success;

		/* -----  output results  ----- */
		// convert back to original bus numbering & print results
		jpc.bus = bus;
		jpc.gen = gen;
		jpc.branch = branch;

		Djp_jpc results = Djp_int2ext.jp_int2ext(jpc);

		// zero out result fields of out-of-service gens & branches
		if (results.order.gen.status.off.length > 0) {
			results.gen.Pg.viewSelection(results.order.gen.status.off).assign(0);
			results.gen.Qg.viewSelection(results.order.gen.status.off).assign(0);
		}
		if (results.order.branch.status.off.length > 0) {
			results.branch.Pf.viewSelection(results.order.branch.status.off).assign(0);
			results.branch.Qf.viewSelection(results.order.branch.status.off).assign(0);
			results.branch.Pt.viewSelection(results.order.branch.status.off).assign(0);
			results.branch.Qt.viewSelection(results.order.branch.status.off).assign(0);
		}

		if (fname != "") {
			// TODO: printpf to file
		}
		Djp_printpf.jp_printpf(results, System.out, jpopt);

		/* save solved case */
		if (solvedcase != "")
			Djp_savecase.jp_savecase(solvedcase, results);

		return results;
	}

}
