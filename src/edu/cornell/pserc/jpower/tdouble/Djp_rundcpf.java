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

import java.util.HashMap;
import java.util.Map;

import edu.cornell.pserc.jpower.tdouble.jpc.Djp_jpc;

/**
 * Runs a DC power flow.
 *
 * @author Ray Zimmerman (rz10@cornell.edu)
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class Djp_rundcpf {

	/**
	 *
	 * @return
	 */
	public static Djp_jpc jp_rundcpf() {

		return jp_rundcpf("case9");
	}

	/**
	 *
	 * @param casedata
	 * @return
	 */
	public static Djp_jpc jp_rundcpf(String casedata) {

		return jp_rundcpf(casedata, Djp_jpoption.jp_jpoption());
	}

	/**
	 *
	 * @param casedata
	 * @param jpopt
	 * @return
	 */
	public static Djp_jpc jp_rundcpf(String casedata, Map<String, Double> jpopt) {

		return jp_rundcpf(casedata, jpopt, "");
	}

	/**
	 *
	 * @param casedata
	 * @param jpopt
	 * @param fname
	 * @return
	 */
	public static Djp_jpc jp_rundcpf(String casedata, Map<String, Double> jpopt,
			String fname) {

		return jp_rundcpf(casedata, jpopt, fname, "");
	}

	/**
	 *
	 * @param casedata
	 * @param jpopt
	 * @param fname
	 * @param solvedcase
	 * @return
	 */
	public static Djp_jpc jp_rundcpf(String casedata, Map<String, Double> jpopt,
			String fname, String solvedcase) {

		return jp_rundcpf(casedata, jpopt, fname, "");
	}

	/**
	 *
	 * @param casedata
	 * @return
	 */
	public static Djp_jpc jp_rundcpf(Djp_jpc casedata) {

		return jp_rundcpf(casedata, Djp_jpoption.jp_jpoption());
	}

	/**
	 *
	 * @param casedata
	 * @param jpopt
	 * @return
	 */
	public static Djp_jpc jp_rundcpf(Djp_jpc casedata, Map<String, Double> jpopt) {

		return jp_rundcpf(casedata, jpopt, "");
	}

	/**
	 *
	 * @param casedata
	 * @param jpopt
	 * @param fname
	 * @return
	 */
	public static Djp_jpc jp_rundcpf(Djp_jpc casedata, Map<String, Double> jpopt, String fname) {

		return jp_rundcpf(casedata, jpopt, "", "");
	}

	/**
	 *
	 * @param casedata
	 * @param jpopt
	 * @param fname
	 * @param solvedcase
	 * @return
	 */
	public static Djp_jpc jp_rundcpf(Djp_jpc casedata, Map<String, Double> jpopt,
			String fname, String solvedcase) {

		Map<String, Double> dc = new HashMap<String, Double>();
		dc.put("PF_DC", (double) 1);
		jpopt = Djp_jpoption.jp_jpoption(jpopt, dc);
		return Djp_runpf.jp_runpf(casedata, jpopt, fname, solvedcase);
	}

}
