/*
 * Copyright (C) 1996-2010 Power System Engineering Research Center
 * Copyright (C) 2010-2011 Richard Lincoln
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package edu.cornell.pserc.jpower.pf;

import java.util.Map;

import static edu.cornell.pserc.jpower.Djp_jpoption.jpoption;
import static edu.cornell.pserc.jpower.Djp_loadcase.loadcase;

import edu.cornell.pserc.jpower.jpc.JPC;

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
