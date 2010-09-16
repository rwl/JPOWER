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

package edu.cornell.pserc.jpower.tdouble.pf;

import cern.colt.matrix.tdouble.DoubleFactory1D;
import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_gencost;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_jpc;
import edu.cornell.pserc.jpower.tdouble.opf.Djp_totcost;
import edu.cornell.pserc.util.tdouble.Djp_util;

/**
 * Converts polynomial cost variable to piecewise linear.
 *
 * @author Ray Zimmerman (rz10@cornell.edu)
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class Djp_poly2pwl {

	private static final Djp_util util = new Djp_util();

	private static final int PW_LINEAR = Djp_jpc.PW_LINEAR;

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
	@SuppressWarnings("static-access")
	public static Djp_gencost jp_poly2pwl(Djp_gencost polycost, DoubleMatrix1D Pmin, DoubleMatrix1D Pmax, int npts) {

		Djp_gencost pwlcost = polycost.copy();
		int m = polycost.size();

		pwlcost.model.assign(PW_LINEAR);	// change cost model
		pwlcost.cost = DoubleFactory2D.dense.make(m, 2 * npts + 1);	// zero out old data
		pwlcost.ncost.assign(npts);			// change number of data points

		for (int i = 0; i < m; i++) {
			double pmin = Pmin.get(i), pmax = Pmax.get(i);
			DoubleMatrix1D xx = null;
			if (pmin == 0) {
				double step = (pmax - pmin) / (npts - 1);
				xx = DoubleFactory1D.dense.make(util.drange(pmin, pmax, step));
			} else if (pmin > 0) {
				double step = (pmax - pmin) / (npts - 1);
				double[] x = util.dcat(new double[] {0}, util.drange(pmin, pmax, step));
				xx = DoubleFactory1D.dense.make(x);
			} else if (pmin < 0 && pmax > 0) {
				double step = (pmax - pmin) / (npts - 1);
				xx = DoubleFactory1D.dense.make(util.drange(pmin, pmax, step));
			}
			DoubleMatrix1D yy = Djp_totcost.jp_totcost(polycost.copy(new int[] {i}), xx);
			pwlcost.cost.viewRow(i).viewSelection(util.irange(0, 2*(npts-1), 2)).assign(xx);
			pwlcost.cost.viewRow(i).viewSelection(util.irange(1, 2*(npts-1)+1, 2)).assign(yy);
		}
		return pwlcost;
	}
}
