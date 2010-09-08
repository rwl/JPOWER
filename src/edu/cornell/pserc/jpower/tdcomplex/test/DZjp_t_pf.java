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

import java.util.HashMap;
import java.util.Map;

import edu.cornell.pserc.jpower.tdcomplex.DZjp_jpoption;

/**
 * Tests for power flow solvers.
 *
 * @author Ray Zimmerman (rz10@cornell.edu)
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class DZjp_t_pf {

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
	}


}
