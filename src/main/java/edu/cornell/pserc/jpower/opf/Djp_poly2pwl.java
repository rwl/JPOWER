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
import cern.colt.util.tdouble.Util;

import static edu.cornell.pserc.jpower.jpc.JPC.PW_LINEAR;
import static edu.cornell.pserc.jpower.opf.Djp_totcost.totcost;

import edu.cornell.pserc.jpower.jpc.Cost;

/**
 * Converts polynomial cost variable to piecewise linear.
 *
 * @author Ray Zimmerman
 * @author Richard Lincoln
 *
 */
public class Djp_poly2pwl {

	/**
	 * Converts the polynomial
	 * cost variable POLYCOST into a piece-wise linear cost by evaluating at
	 * zero and then at NPTS evenly spaced points between PMIN and PMAX. If
	 * PMIN <= 0 (such as for reactive power, where P really means Q) it just
	 * uses NPTS evenly spaced points between PMIN and PMAX.
	 *
	 * @param polycost
	 * @param Pmin
	 * @param Pmax
	 * @param npts
	 * @return
	 */
	public static Cost poly2pwl(Cost polycost, DoubleMatrix1D Pmin, DoubleMatrix1D Pmax, int npts) {
		int i, m;
		double pmin, pmax, step;
		Cost pwlcost;
		DoubleMatrix1D xx, yy;

		pwlcost = polycost.copy();
		m = polycost.size();

		pwlcost.model.assign(PW_LINEAR);  // change cost model
		pwlcost.cost = DoubleFactory2D.dense.make(m, 2 * npts + 1);  // zero out old data
		pwlcost.ncost.assign(npts);			// change number of data points

		for (i = 0; i < m; i++) {
			pmin = Pmin.get(i); pmax = Pmax.get(i);

			if (pmin == 0) {
				step = (pmax - pmin) / (npts - 1);
				xx = DoubleFactory1D.dense.make(Util.drange(pmin, pmax, step));
			} else if (pmin > 0) {
				step = (pmax - pmin) / (npts - 1);
				double[] x = Util.dcat(new double[] {0}, Util.drange(pmin, pmax, step));
				xx = DoubleFactory1D.dense.make(x);
			} else if (pmin < 0 && pmax > 0) {
				step = (pmax - pmin) / (npts - 1);
				xx = DoubleFactory1D.dense.make(Util.drange(pmin, pmax, step));
			} else {
				// FIXME Pmin < 0 && Pmax <= 0
				xx = DoubleFactory1D.dense.make(0);
			}
			yy = totcost(polycost.copy(new int[] {i}), xx);
			pwlcost.cost.viewRow(i).viewSelection(Util.irange(0, 2 * (npts - 1)    , 2)).assign(xx);
			pwlcost.cost.viewRow(i).viewSelection(Util.irange(1, 2 * (npts - 1) + 1, 2)).assign(yy);
		}
		return pwlcost;
	}

}
