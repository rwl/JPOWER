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
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.jet.math.tdouble.Polynomial;
import cern.jet.math.tint.IntFunctions;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_gencost;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_jpc;
import edu.cornell.pserc.util.tdouble.Djp_util;

/**
 * Computes total cost for generators at given output level.
 *
 * @author Ray Zimmerman (rz10@cornell.edu)
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class Djp_totcost {

	private static final Djp_util util = new Djp_util();
	private static final IntFunctions ifunc = IntFunctions.intFunctions;

	private static final int PW_LINEAR = Djp_jpc.PW_LINEAR;
	private static final int POLYNOMIAL = Djp_jpc.POLYNOMIAL;

	/**
	 * Computes total cost for generators at given output level.
	 *
	 * @param gencost
	 * @param Pg
	 * @return
	 */
	@SuppressWarnings("static-access")
	public static DoubleMatrix1D jp_totcost(Djp_gencost gencost, DoubleMatrix1D Pg) {

		int ng = gencost.size();

		DoubleMatrix1D totalcost = DoubleFactory1D.dense.make(ng);

		int[] ipwl = util.nonzero( gencost.model.copy().assign(ifunc.equals(PW_LINEAR)) );
		int[] ipol = util.nonzero( gencost.model.copy().assign(ifunc.equals(POLYNOMIAL)) );

		if (ipwl.length != 0) {
			int ncost = gencost.cost.columns();
			DoubleMatrix2D p = gencost.cost.viewSelection(null, util.irange(0, ncost - 1, 2));
			DoubleMatrix2D c = gencost.cost.viewSelection(null, util.irange(1, ncost, 2));
			for (int i : ipwl) {
				ncost = gencost.ncost.get(i);
				for (int k : util.irange(1, ncost - 1, 2)) {
					double p1 = p.get(i, k), p2 = p.get(i, k + 1);
					double c1 = c.get(i, k), c2 = c.get(i, k + 1);
					double m = (c2 - c1) / (p2 - p1);
					double b = c1 - m * p1;
					double Pgen = Pg.get(i);
					if (Pgen < p2) {
						totalcost.set(i, m * Pgen + b);
						break;
					}
					totalcost.set(i, m * Pgen + b);
				}
			}
		}

		for (int i : ipol)
			totalcost.set(i, Polynomial.polevl(Pg.get(i),
					gencost.cost.viewRow(i).toArray(), gencost.ncost.get(i)));

		return totalcost;
	}
}
