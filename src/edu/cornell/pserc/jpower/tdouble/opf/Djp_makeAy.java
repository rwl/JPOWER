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

import cern.colt.matrix.AbstractMatrix;
import cern.colt.matrix.tdouble.DoubleFactory1D;
import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.SparseRCDoubleMatrix2D;
import cern.jet.math.tdouble.DoubleFunctions;
import cern.jet.math.tint.IntFunctions;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_gencost;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_jpc;
import edu.cornell.pserc.util.tdouble.Djp_util;

/**
 * Make the A matrix and RHS for the CCV formulation.
 *
 * @author Ray Zimmerman (rz10@cornell.edu)
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class Djp_makeAy {

	private static final Djp_util util = new Djp_util();
	private static final DoubleFunctions dfunc = DoubleFunctions.functions;
	private static final IntFunctions ifunc = IntFunctions.intFunctions;

	private static final int PW_INEAR = Djp_jpc.PW_LINEAR;

	/**
	 * Constructs the parameters for linear "basin constraints" on Pg, Qg
	 * and Y used by the CCV cost formulation, expressed as
	 *
	 * 	AY * X <= BY
	 *
	 * where X is the vector of optimization variables. The starting index
	 * within the X vector for the active, reactive sources and the Y
	 * variables should be provided in arguments PGBAS, QGBAS, YBAS. The
	 * number of generators is NG.
	 *
	 * Assumptions: All generators are in-service.  Filter any generators
	 * that are offline from the GENCOST matrix before calling MAKEAY.
	 * Efficiency depends on Qg variables being after Pg variables, and
	 * the Y variables must be the last variables within the vector X for
	 * the dimensions of the resulting AY to be conformable with X.
	 *
	 * @param baseMVA
	 * @param ng
	 * @param gencost
	 * @param i
	 * @param q1
	 * @param j
	 * @return
	 */
	@SuppressWarnings("static-access")
	public static AbstractMatrix[] jp_makeAy(double baseMVA, int ng, Djp_gencost gencost,
			int pgbas, int qgbas, int ybas) {

		/* find all pwl cost rows in gencost, either real or reactive */
		int[] iycost = util.nonzero( gencost.model.copy().assign(ifunc.equals(PW_INEAR)) );

		/* this is the number of extra "y" variables needed to model those costs */
		int ny = iycost.length;

		DoubleMatrix2D Ay;
		DoubleMatrix1D by;
		if (ny == 0) {
			Ay = DoubleFactory2D.sparse.make(ybas+ny-1, 0);
			by = DoubleFactory1D.dense.make(0);
			return new AbstractMatrix[] {Ay, by};
		}

		/*
		 * If p(i),p(i+1),c(i),c(i+1) define one of the cost segments, then
		 * the corresponding constraint on Pg (or Qg) and Y is
		 *                                              c(i+1) - c(i)
		 *   Y   >=   c(i) + m * (Pg - p(i)),      m = ---------------
		 *                                              p(i+1) - p(i)
		 *
		 * this becomes   m * Pg - Y   <=   m*p(i) - c(i)
		 *
		 * Form A matrix.  Use two different loops, one for the PG/Qg coefs,
		 * then another for the y coefs so that everything is filled in the
		 * same order as the compressed column sparse format used by matlab;
		 * this should be the quickest.
		 */

		int nnz = gencost.ncost.viewSelection(iycost).aggregate(ifunc.plus, ifunc.identity);	// total number of cost points
		Ay = new SparseRCDoubleMatrix2D(nnz-ny, ybas+ny-1, 2*(nnz-ny));
		by = DoubleFactory1D.dense.make(0);
		/* First fill the Pg or Qg coefficients (since their columns come first)
		 * and the rhs
		 */
		int k = 0;
		for (int i : iycost) {
			int ns = gencost.ncost.get(i);		// # of cost points; segments = ns-1
			DoubleMatrix1D p = gencost.cost.viewRow(i).copy().viewSelection(util.irange(0, 2*ns-1, 2));
			DoubleMatrix1D c = gencost.cost.viewRow(i).copy().viewSelection(util.irange(1, 2*ns, 2));
			DoubleMatrix1D m = util.diff(c).assign(util.diff(p), dfunc.div);	// slopes for Pg (or Qg)
			if (util.any(util.diff(p).assign(dfunc.equals(0))))
				System.out.printf("\nmakeAy: bad x axis data in row %i of gencost matrix\n", i);
			DoubleMatrix1D b = m.copy().assign(p.viewPart(0, ns-1), dfunc.mult).assign(c.viewPart(0, ns-1), dfunc.minus);	// and rhs
			by = DoubleFactory1D.dense.append(by, b);
			int sidx;
			if (i > ng) {
				sidx = qgbas + (i-ng) - 1;		// this was for a q cost
			} else {
				sidx = pgbas + i - 1;			// this was for a p cost
			}
			Ay.viewColumn(sidx).viewSelection(util.irange(k, k+ns-2)).assign(m);
			k += ns - 1;
		}
		/* Now fill the y columns with -1's */
		k = 0;
		int j = 0;
		for (int i : iycost) {
			int ns = gencost.ncost.get(i);
			Ay.viewColumn(ybas+j-1).viewSelection(util.irange(k, k+ns-2));
			k += ns - 1;
			j += 1;
		}

		return null;
	}
}
