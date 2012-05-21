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

package edu.cornell.pserc.jpower.opf;

import cern.colt.matrix.tdouble.DoubleFactory1D;
import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;

import static edu.emory.mathcs.utils.Utils.ifunc;
import static edu.emory.mathcs.utils.Utils.dfunc;
import static edu.emory.mathcs.utils.Utils.irange;
import static edu.emory.mathcs.utils.Utils.nonzero;

import edu.cornell.pserc.jpower.jpc.Cost;

/**
 * Evaluates polynomial generator cost & derivatives.
 *
 * @author Ray Zimmerman
 * @author Richard Lincoln
 *
 */
public class Djp_polycost {

	/**
	 *
	 * @param gencost gencost must contain only polynomial costs.
	 * @param Pg is in MW, not p.u. (works for QG too).
	 * @param der 1 - first derivative, 2 - second derivative
	 * @return the vector of derivatives of costs evaluated at PG
	 */
	@SuppressWarnings("static-access")
	public static DoubleMatrix1D polycost(Cost gencost, DoubleMatrix1D Pg, int der) {
		int ng, maxN, minN;
		int[] k;
		DoubleMatrix1D f;
		DoubleMatrix2D c;

		ng = (int) Pg.size();
		maxN = gencost.ncost.aggregate(ifunc.max, ifunc.identity);
		minN = gencost.ncost.aggregate(ifunc.min, ifunc.identity);

		/* form coefficient matrix where 1st column is constant term, 2nd linear, etc. */
		c = DoubleFactory2D.dense.make(ng, maxN);
		for (int n : irange(minN, maxN)) {
			k = nonzero( gencost.ncost.copy().assign(ifunc.equals(n)) );	// cost with n coefficients
			c.viewSelection(k, irange(n)).assign( gencost.cost.viewSelection(k, irange(n - 1, 0, -1)) );
		}

		/* do derivatives */
		for (int d = 0; d < der; d++) {
			if (c.columns() >= 2) {
				c.assign( c.viewSelection(null, irange(1, maxN - d + 1)) );
			} else {
				c = DoubleFactory2D.dense.make(ng, 1);
				break;
			}
			for (int kk : irange(1, maxN - d)) {
				c.viewColumn(kk).assign(dfunc.mult(kk));
			}
		}

		/* evaluate polynomial */
		if (c.size() == 0) {
			f = DoubleFactory1D.dense.make(ng);
		} else {
			f = c.viewColumn(0).copy();		// constant term
			for (int kk : irange(1, c.columns()))
				f.assign(c.viewColumn(kk).assign( Pg.copy().assign(dfunc.pow(kk - 1)), dfunc.mult ), dfunc.plus);
		}
		return f;
	}

	/**
	 *
	 * @param gencost must contain only polynomial costs.
	 * @param Pg is in MW, not p.u. (works for QG too).
	 * @return the vector of costs evaluated at Pg.
	 */
	public static DoubleMatrix1D polycost(Cost gencost, DoubleMatrix1D Pg) {
		return polycost(gencost, Pg, 0);
	}

}
