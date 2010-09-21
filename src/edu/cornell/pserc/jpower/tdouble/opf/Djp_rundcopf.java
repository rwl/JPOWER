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

package edu.cornell.pserc.jpower.tdouble.opf;

import java.util.HashMap;
import java.util.Map;

import edu.cornell.pserc.jpower.tdouble.Djp_jpoption;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_jpc;

/**
 * Runs a DC optimal power flow.
 *
 * @author Ray Zimmerman (rz10@cornell.edu)
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class Djp_rundcopf {

	public static Djp_jpc jp_rundcopf(Djp_jpc casedata) {
		return jp_rundcopf(casedata, Djp_jpoption.jp_jpoption());
	}

	public static Djp_jpc jp_rundcopf(Djp_jpc casedata, Map<String, Double> jpopt) {
		return jp_rundcopf(casedata, jpopt, "");
	}

	public static Djp_jpc jp_rundcopf(Djp_jpc casedata, Map<String, Double> jpopt, String fname) {
		return jp_rundcopf(casedata, jpopt, fname, "");
	}

	/**
	 * Runs a DC optimal power flow, returning a RESULTS object.
	 *
	 * @param casedata
	 * @param jpopt
	 * @param fname
	 * @param solvedcase
	 * @return
	 */
	public static Djp_jpc jp_rundcopf(Djp_jpc casedata, Map<String, Double> jpopt, String fname, String solvedcase) {
		Map<String, Double> dcopf_opt = new HashMap<String, Double>();
		dcopf_opt.put("PF_DC", 1.0);
		jpopt = Djp_jpoption.jp_jpoption(jpopt, dcopf_opt);
		return Djp_runopf.jp_runopf(casedata, jpopt, fname, solvedcase);
	}
}
