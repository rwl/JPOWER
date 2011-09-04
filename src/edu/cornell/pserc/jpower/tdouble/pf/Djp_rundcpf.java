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

package edu.cornell.pserc.jpower.tdouble.pf;

import java.util.Map;

import static edu.cornell.pserc.jpower.tdouble.Djp_jpoption.jpoption;
import static edu.cornell.pserc.jpower.tdouble.Djp_loadcase.loadcase;
import edu.cornell.pserc.jpower.tdouble.jpc.JPC;

/**
 * Runs a DC power flow.
 *
 * @author Ray Zimmerman
 * @author Richard Lincoln
 *
 */
public class Djp_rundcpf {

	/**
	 *
	 * @param casedata
	 * @param jpopt
	 * @param fname
	 * @param solvedcase
	 * @return
	 */
	public static JPC rundcpf(JPC casedata, Map<String, Double> jpopt,
			String fname, String solvedcase) {

		jpopt = jpoption(jpopt, "PF_DC", 1.0);

		return Djp_runpf.runpf(casedata, jpopt, fname, solvedcase);
	}

	public static JPC rundcpf() {
		return rundcpf("case9");
	}

	public static JPC rundcpf(String casedata) {
		return rundcpf(casedata, jpoption());
	}

	public static JPC rundcpf(String casedata, Map<String, Double> jpopt) {
		return rundcpf(casedata, jpopt, "");
	}

	public static JPC rundcpf(String casedata, Map<String, Double> jpopt, String fname) {
		return rundcpf(casedata, jpopt, fname, "");
	}

	public static JPC rundcpf(String casedata, Map<String, Double> jpopt, String fname, String solvedcase) {
		JPC jpc = loadcase(casedata);
		return rundcpf(jpc, jpopt, fname, "");
	}

	public static JPC rundcpf(JPC casedata) {
		return rundcpf(casedata, jpoption());
	}

	public static JPC rundcpf(JPC casedata, Map<String, Double> jpopt) {
		return rundcpf(casedata, jpopt, "");
	}

	public static JPC rundcpf(JPC casedata, Map<String, Double> jpopt, String fname) {
		return rundcpf(casedata, jpopt, "", "");
	}

}
