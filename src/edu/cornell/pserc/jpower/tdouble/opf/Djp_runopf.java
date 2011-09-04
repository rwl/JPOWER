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

package edu.cornell.pserc.jpower.tdouble.opf;

import java.util.Map;

import static edu.cornell.pserc.jpower.tdouble.Djp_jpoption.jpoption;
import static edu.cornell.pserc.jpower.tdouble.Djp_printpf.printpf;
import static edu.cornell.pserc.jpower.tdouble.Djp_savecase.savecase;
import static edu.cornell.pserc.jpower.tdouble.opf.Djp_opf.opf;

import edu.cornell.pserc.jpower.tdouble.jpc.JPC;

/**
 * Runs an optimal power flow.
 *
 * @author Ray Zimmerman
 * @author Richard Lincoln
 *
 */
public class Djp_runopf {

	/**
	 * Runs an optimal power flow (AC OPF by default), returning a
	 * RESULTS object.
	 *
	 * @param casedata
	 * @param jpopt
	 * @param fname
	 * @param solvedcase
	 * @return
	 */
	public static JPC runopf(JPC casedata, Map<String, Double> jpopt,
			String fname, String solvedcase) {

		JPC r;

		/* -----  run the optimal power flow  ----- */
		r = opf(casedata, jpopt);

		/* -----  output results  ----- */
		if (fname != "")
			printpf(r, fname, jpopt);

		printpf(r, System.out, jpopt);

		/* save solved case */
		if (solvedcase != "")
			savecase(solvedcase, r);

		return r;
	}

	public static JPC runopf(JPC casedata) {
		return runopf(casedata, jpoption());
	}

	public static JPC runopf(JPC casedata, Map<String, Double> jpopt) {
		return runopf(casedata, jpopt, "");
	}

	public static JPC runopf(JPC casedata, Map<String, Double> jpopt, String fname) {
		return runopf(casedata, jpopt, fname, "");
	}

}
