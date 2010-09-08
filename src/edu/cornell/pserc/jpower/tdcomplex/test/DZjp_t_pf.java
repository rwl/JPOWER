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

package edu.cornell.pserc.jpower.tdcomplex.test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import cern.colt.matrix.tdouble.DoubleMatrix2D;

import edu.cornell.pserc.jpower.tdcomplex.DZjp_jpoption;
import edu.cornell.pserc.jpower.tdcomplex.DZjp_runpf;
import edu.cornell.pserc.jpower.tdcomplex.jpc.DZjp_jpc;
import edu.cornell.pserc.jpower.tdcomplex.util.MatrixMarketUtil;

/**
 * Tests for power flow solvers.
 *
 * @author Ray Zimmerman (rz10@cornell.edu)
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class DZjp_t_pf {

	private static final String BUS_SOLN9 = "soln9_pf/bus_soln.mtx";
	private static final String GEN_SOLN9 = "soln9_pf/gen_soln.mtx";
	private static final String BRANCH_SOLN9 = "soln9_pf/branch_soln.mtx";

	public static void jp_t_pf() {
		jp_t_pf(false);
	}

	/**
	 * Tests for power flow solvers.
	 *
	 * @param quiet
	 */
	public static void jp_t_pf(boolean quiet) {

		DZjp_t_begin.jp_t_begin(25, quiet);

		String casefile = "t_case9_pf";
		Map<String, Double> opt = new HashMap<String, Double>();
		opt.put("OUT_ALL", 0.0);
		opt.put("VERBOSE", quiet ? 0.0 : 1.0);
		Map<String, Double> jpopt = DZjp_jpoption.jp_jpoption();

		/* get solved AC power flow case from MatrixMarket file. */
		try {
			DoubleMatrix2D bus_soln = (DoubleMatrix2D) MatrixMarketUtil.readMatrix(BUS_SOLN9);
			DoubleMatrix2D gen_soln = (DoubleMatrix2D) MatrixMarketUtil.readMatrix(GEN_SOLN9);
			DoubleMatrix2D branch_soln = (DoubleMatrix2D) MatrixMarketUtil.readMatrix(BRANCH_SOLN9);
		} catch (IOException e) {
			e.printStackTrace();
		}

		DZjp_jpc jpc;
		Map<String, Double> alg = new HashMap<String, Double>();

		/* run Newton PF */
		String t = "Newton PF : ";
		alg.put("PF_ALG", 1.0);
		jpopt = DZjp_jpoption.jp_jpoption(jpopt, alg);
//		jpc = DZjp_runpf.jp_runpf(casefile, jpopt);
	}


}
