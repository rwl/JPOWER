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

import cern.colt.matrix.tint.IntMatrix1D;
import cern.colt.util.tdouble.Djp_util;
import cern.jet.math.tdouble.DoubleFunctions;
import cern.jet.math.tint.IntFunctions;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_gen;

/**
 * Checks for dispatchable loads.
 *
 * @author Ray Zimmerman
 * @author Richard Lincoln
 *
 */
public class Djp_jp_isload {

	private static final DoubleFunctions dfunc = DoubleFunctions.functions;
	private static final IntFunctions ifunc = IntFunctions.intFunctions;

	/**
	 * Returns a column vector of 1's and 0's. The 1's
	 * correspond to rows of the GEN matrix which represent dispatchable loads.
	 * The current test is Pmin < 0 AND Pmax == 0.
	 * This may need to be revised to allow sensible specification
	 * of both elastic demand and pumped storage units.
	 *
	 * @param gen
	 * @return
	 */
	@SuppressWarnings("static-access")
	public static IntMatrix1D isload(Djp_gen gen) {

		return Djp_util.intm( gen.Pmin.copy().assign(dfunc.less(0)) ).assign(Djp_util.intm( gen.Pmax.copy().assign(dfunc.equals(0)) ), ifunc.and);
	}

}
