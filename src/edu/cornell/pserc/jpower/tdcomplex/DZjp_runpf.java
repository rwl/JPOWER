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

/**
 * Runs a power flow
 *
 * @author Ray Zimmerman (rz10@cornell.edu)
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
package edu.cornell.pserc.jpower.tdcomplex;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cern.colt.list.tint.IntArrayList;
import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tint.IntFactory1D;
import cern.colt.matrix.tint.IntMatrix1D;

public class DZjp_runpf {

	/**
	 *
	 * @return
	 */
	public static DoubleMatrix1D[] jp_runpf() {

		return jp_runpf("case9");
	}

	/**
	 *
	 * @param casedata
	 * @return
	 */
	public static DoubleMatrix1D[] jp_runpf(String casedata) {

		return jp_runpf(casedata, DZjp_jpoption.jp_jpoption());
	}

	/**
	 *
	 * @param casedata
	 * @param jpopt
	 * @return
	 */
	public static DoubleMatrix1D[] jp_runpf(String casedata, DoubleMatrix1D jpopt) {

		return jp_runpf(casedata, jpopt, "");
	}

	/**
	 *
	 * @param casedata
	 * @param jpopt
	 * @param fname
	 * @return
	 */
	public static DoubleMatrix1D[] jp_runpf(String casedata, DoubleMatrix1D jpopt,
			String fname) {

		return jp_runpf(casedata, jpopt, fname, "");
	}

	/**
	 *
	 * @param casedata
	 * @param jpopt
	 * @param fname
	 * @param solvedcase
	 * @return
	 */
	public static DoubleMatrix1D[] jp_runpf(String casedata, DoubleMatrix1D jpopt,
			String fname, String solvedcase) {

		return jp_runpf(casedata, jpopt, fname, "");
	}

	/**
	 *
	 * @param casedata
	 * @return
	 */
	public static DoubleMatrix1D[] jp_runpf(Map<String, DoubleMatrix2D> casedata) {

		return jp_runpf(casedata, DZjp_jpoption.jp_jpoption());
	}

	/**
	 *
	 * @param casedata
	 * @param jpopt
	 * @return
	 */
	public static DoubleMatrix1D[] jp_runpf(Map<String, DoubleMatrix2D> casedata,
			DoubleMatrix1D jpopt) {

		return jp_runpf(casedata, jpopt, "");
	}

	/**
	 *
	 * @param casedata
	 * @param jpopt
	 * @param fname
	 * @return
	 */
	public static DoubleMatrix1D[] jp_runpf(Map<String, DoubleMatrix2D> casedata,
			DoubleMatrix1D jpopt, String fname) {

		return jp_runpf(casedata, jpopt, "", "");
	}

	/**
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
	 */
	public static DoubleMatrix1D[] jp_runpf(Map<String, DoubleMatrix2D> casedata,
			DoubleMatrix1D jpopt, String fname, String solvedcase) {

		/* options */
		int verbose = (int) jpopt.get(31);
		boolean qlim = jpopt.get(6) != 0.0;     /* enforce Q limits on gens? */
		boolean dc = jpopt.get(10) != 0.0;      /* use DC formulation? */

		/* read data */
		Map<String, DoubleMatrix2D> jpc = DZjp_loadcase.loadcase(casedata);

		/* add zero columns to branch for flows if needed */
		DoubleMatrix2D branch = jpc.get("branch");
		if (branch.columns() < DZjp_idx_brch.QT) {
			DoubleMatrix2D flows = DoubleFactory2D.dense.make(branch.rows(),
					DZjp_idx_brch.QT - branch.columns());
			jpc.put("branch", DoubleFactory2D.dense.appendColumns(branch, flows));
		}

		/* convert to internal indexing */
		jpc = DZjp_ext2int.jp_ext2int(jpc);
		double baseMVA = jpc.get("baseMVA").get(0, 0);
		DoubleMatrix2D bus = jpc.get("bus");
		DoubleMatrix2D gen = jpc.get("gen");
		branch = jpc.get("branch");

		/* get bus index lists of each type of bus */
		List<int[]> bustypes = DZjp_bustypes.jp_bustypes(bus, gen);
		int[] ref = bustypes.get(0);
		int[] pv = bustypes.get(1);
		int[] pq = bustypes.get(2);

		/* generator info */
		IntArrayList on = new IntArrayList(); // which generators are on?
		gen.viewColumn(DZjp_idx_gen.GEN_STATUS).getPositiveValues(on, null);
		// what buses are they at?
		DoubleMatrix1D gbus = gen.viewColumn(DZjp_idx_gen.GEN_BUS).viewSelection(on.elements());
		gbus.toArray();

		return null;
	}

}
