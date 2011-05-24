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

import cern.colt.matrix.tdouble.DoubleFactory1D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.util.tdouble.Djp_util;
import cern.jet.math.tdouble.Polynomial;
import cern.jet.math.tint.IntFunctions;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_gencost;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_jpc;

/**
 * Computes total cost for generators at given output level.
 *
 * @author Ray Zimmerman
 * @author Richard Lincoln
 *
 */
public class Djp_totcost {

	private static final IntFunctions ifunc = IntFunctions.intFunctions;

	private static final int PW_LINEAR = Djp_jpc.PW_LINEAR;
	private static final int POLYNOMIAL = Djp_jpc.POLYNOMIAL;

	private static int ng, ncost;
	private static int[] ipwl, ipol;
	private static double p1,  p2, c1, c2, m, b, Pgen;
	private static DoubleMatrix1D totalcost;
	private static DoubleMatrix2D p, c;

	/**
	 * Computes total cost for generators at given output level.
	 *
	 * @param gencost
	 * @param Pg
	 * @return
	 */
	@SuppressWarnings("static-access")
	public static DoubleMatrix1D jp_totcost(Djp_gencost gencost, DoubleMatrix1D Pg) {

		ng = gencost.size();

		totalcost = DoubleFactory1D.dense.make(ng);

		ipwl = Djp_util.nonzero( gencost.model.copy().assign(ifunc.equals(PW_LINEAR)) );
		ipol = Djp_util.nonzero( gencost.model.copy().assign(ifunc.equals(POLYNOMIAL)) );

		if (ipwl.length != 0) {
			ncost = gencost.cost.columns();
			p = gencost.cost.viewSelection(null, Djp_util.irange(0, ncost - 1, 2));
			c = gencost.cost.viewSelection(null, Djp_util.irange(1, ncost, 2));
			for (int i : ipwl) {
				ncost = gencost.ncost.get(i);
				for (int k : Djp_util.irange(1, ncost - 1, 2)) {
					p1 = p.get(i, k); p2 = p.get(i, k + 1);
					c1 = c.get(i, k); c2 = c.get(i, k + 1);
					m = (c2 - c1) / (p2 - p1);
					b = c1 - m * p1;
					Pgen = Pg.get(i);
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
