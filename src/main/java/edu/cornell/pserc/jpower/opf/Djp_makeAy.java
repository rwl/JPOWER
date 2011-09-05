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

import cern.colt.matrix.AbstractMatrix;
import cern.colt.matrix.tdouble.DoubleFactory1D;
import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.SparseRCDoubleMatrix2D;

import static cern.colt.util.tdouble.Util.ifunc;
import static cern.colt.util.tdouble.Util.dfunc;
import static cern.colt.util.tdouble.Util.irange;
import static cern.colt.util.tdouble.Util.diff;
import static cern.colt.util.tdouble.Util.nonzero;
import static cern.colt.util.tdouble.Util.any;

import edu.cornell.pserc.jpower.jpc.GenCost;

import static edu.cornell.pserc.jpower.jpc.JPC.PW_LINEAR;

/**
 * Make the A matrix and RHS for the CCV formulation.
 *
 * @author Ray Zimmerman
 * @author Richard Lincoln
 *
 */
public class Djp_makeAy {

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
	public static AbstractMatrix[] makeAy(double baseMVA, int ng, GenCost gencost,
			int pgbas, int qgbas, int ybas) {
		int ny, nnz, k, ns, sidx, j;
		int[] iycost;
		DoubleMatrix1D by, p, c, m, b;
		DoubleMatrix2D Ay;

		/* find all pwl cost rows in gencost, either real or reactive */
		iycost = nonzero( gencost.model.copy().assign(ifunc.equals(PW_LINEAR)) );

		/* this is the number of extra "y" variables needed to model those costs */
		ny = iycost.length;

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

		nnz = gencost.ncost.viewSelection(iycost).aggregate(ifunc.plus, ifunc.identity);	// total number of cost points
		Ay = new SparseRCDoubleMatrix2D(nnz-ny, ybas+ny-1, 2*(nnz-ny));
		by = DoubleFactory1D.dense.make(0);
		/* First fill the Pg or Qg coefficients (since their columns come first)
		 * and the rhs
		 */
		k = 0;
		for (int i : iycost) {
			ns = gencost.ncost.get(i);		// # of cost points; segments = ns-1
			p = gencost.cost.viewRow(i).copy().viewSelection(irange(0, 2*ns-1, 2));
			c = gencost.cost.viewRow(i).copy().viewSelection(irange(1, 2*ns, 2));
			m = diff(c).assign(diff(p), dfunc.div);	// slopes for Pg (or Qg)
			if (any(diff(p).assign(dfunc.equals(0))))
				System.out.printf("\nmakeAy: bad x axis data in row %i of gencost matrix\n", i);
			b = m.copy().assign(p.viewPart(0, ns-1), dfunc.mult).assign(c.viewPart(0, ns-1), dfunc.minus);	// and rhs
			by = DoubleFactory1D.dense.append(by, b);
			if (i > ng) {
				sidx = qgbas + (i-ng) - 1;		// this was for a q cost
			} else {
				sidx = pgbas + i - 1;			// this was for a p cost
			}
			Ay.viewColumn(sidx).viewSelection(irange(k, k+ns-2)).assign(m);
			k += ns - 1;
		}
		/* Now fill the y columns with -1's */
		k = j = 0;
		for (int i : iycost) {
			ns = gencost.ncost.get(i);
			Ay.viewColumn(ybas+j-1).viewSelection(irange(k, k+ns-2));
			k += ns - 1;
			j += 1;
		}

		return null;
	}

}
