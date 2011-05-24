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

import edu.cornell.pserc.jpower.tdouble.Djp_jpoption;
import edu.cornell.pserc.jpower.tdouble.Djp_printpf;
import edu.cornell.pserc.jpower.tdouble.Djp_savecase;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_jpc;

/**
 * Runs an optimal power flow.
 *
 * @author Ray Zimmerman
 * @author Richard Lincoln
 *
 */
public class Djp_runopf {

	private static Djp_jpc r;

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
	public static Djp_jpc jp_runopf(Djp_jpc casedata, Map<String, Double> jpopt,
			String fname, String solvedcase) {

		/* -----  run the optimal power flow  ----- */
		r = Djp_opf.jp_opf(casedata, jpopt);

		/* -----  output results  ----- */
		if (fname != "")
			Djp_printpf.jp_printpf(r, fname, jpopt);

		Djp_printpf.jp_printpf(r, System.out, jpopt);

		/* save solved case */
		if (solvedcase != "")
			Djp_savecase.jp_savecase(solvedcase, r);

		return r;
	}

	public static Djp_jpc jp_runopf(Djp_jpc casedata) {
		return jp_runopf(casedata, Djp_jpoption.jp_jpoption());
	}

	public static Djp_jpc jp_runopf(Djp_jpc casedata, Map<String, Double> jpopt) {
		return jp_runopf(casedata, jpopt, "");
	}

	public static Djp_jpc jp_runopf(Djp_jpc casedata, Map<String, Double> jpopt, String fname) {
		return jp_runopf(casedata, jpopt, fname, "");
	}

}
