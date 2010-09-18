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

import cern.colt.matrix.tdouble.DoubleFactory1D;
import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.jet.math.tdouble.DoubleFunctions;
import cern.jet.math.tint.IntFunctions;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_gencost;
import edu.cornell.pserc.util.tdouble.Djp_util;

/**
 * Evaluates polynomial generator cost & derivatives.
 *
 * @author Ray Zimmerman (rz10@cornell.edu)
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class Djp_polycost {

	private static final Djp_util util = new Djp_util();
	private static final IntFunctions ifunc = IntFunctions.intFunctions;
	private static final DoubleFunctions dfunc = DoubleFunctions.functions;

	/**
	 *
	 * @param gencost must contain only polynomial costs.
	 * @param Pg is in MW, not p.u. (works for QG too).
	 * @return the vector of costs evaluated at Pg.
	 */
	public static DoubleMatrix1D jp_polycost(Djp_gencost gencost, DoubleMatrix1D Pg) {
		return jp_polycost(gencost, Pg, 0);
	}

	/**
	 *
	 * @param gencost gencost must contain only polynomial costs.
	 * @param Pg is in MW, not p.u. (works for QG too).
	 * @param der 1 - first derivative, 2 - second derivative
	 * @return the vector of derivatives of costs evaluated at PG
	 */
	@SuppressWarnings("static-access")
	public static DoubleMatrix1D jp_polycost(Djp_gencost gencost, DoubleMatrix1D Pg, int der) {

		int ng = (int) Pg.size();
		int maxN = gencost.ncost.aggregate(ifunc.max, ifunc.identity);
		int minN = gencost.ncost.aggregate(ifunc.min, ifunc.identity);

		/* form coefficient matrix where 1st column is constant term, 2nd linear, etc. */
		DoubleMatrix2D c = DoubleFactory2D.dense.make(ng, maxN);
		for (int n : util.irange(minN, maxN)) {
			int[] k = util.nonzero( gencost.ncost.copy().assign(ifunc.equals(n)) );	// cost with n coefficients
			c.viewSelection(k, util.irange(n)).assign( gencost.cost.viewSelection(k, util.irange(n - 1, 0, -1)) );
		}

		/* do derivatives */
		for (int d = 0; d < der; d++) {
			if (c.columns() >= 2) {
				c.assign( c.viewSelection(null, util.irange(1, maxN - d + 1)) );
			} else {
				c = DoubleFactory2D.dense.make(ng, 1);
				break;
			}
			for (int k : util.irange(1, maxN - d)) {
				c.viewColumn(k).assign(dfunc.mult(k));
			}
		}

		/* evaluate polynomial */
		DoubleMatrix1D f;
		if (c.size() == 0) {
			f = DoubleFactory1D.dense.make(ng);
		} else {
			f = c.viewColumn(0).copy();		// constant term
			for (int k : util.irange(1, c.columns()))
				f.assign(c.viewColumn(k).assign( Pg.copy().assign(dfunc.pow(k-1)), dfunc.mult ), dfunc.plus);
		}
		return f;
	}
}
