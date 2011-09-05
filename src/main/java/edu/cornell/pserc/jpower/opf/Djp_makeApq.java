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

import java.util.HashMap;
import java.util.Map;

import cern.colt.matrix.AbstractMatrix;
import cern.colt.matrix.tdouble.DoubleFactory1D;
import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.algo.DenseDoubleAlgebra;
import cern.colt.matrix.tdouble.impl.SparseRCDoubleMatrix2D;
import cern.colt.matrix.tint.IntFactory1D;

import static cern.colt.util.tdouble.Util.dfunc;
import static cern.colt.util.tdouble.Util.nonzero;
import static cern.colt.util.tdouble.Util.irange;

import edu.cornell.pserc.jpower.jpc.Gen;

import static edu.cornell.pserc.jpower.opf.Djp_hasPQcap.hasPQcap;

/**
 * Construct linear constraints for generator capability curves.
 *
 * @author Ray Zimmerman
 * @author Richard Lincoln
 *
 */
public class Djp_makeApq {

	/**
	 * Constructs the parameters for the following linear constraints
	 * implementing trapezoidal generator capability curves, where
	 * Pg and Qg are the real and reactive generator injections.
	 *
	 * 	APQH * [Pg; Qg] <= UBPQH
	 * 	APQL * [Pg; Qg] <= UBPQL
	 *
	 * DATA constains additional information as shown below.
	 *
	 * 	data.h      [QC1MAX-QC2MAX, PC2-PC1]
	 * 	data.l      [QC2MIN-QC1MIN, PC1-PC2]
	 * 	data.ipqh   indices of gens with general PQ cap curves (upper)
	 * 	data.ipql   indices of gens with general PQ cap curves (lower)
	 *
	 * @param baseMVA
	 * @param gen
	 * @return
	 */
	@SuppressWarnings("static-access")
	public static Object[] makeApq(double baseMVA, Gen gen) {
		int i, ng, npqh, npql;
		int[] ipqh, ipql;
		double tmp;
		Map<String, AbstractMatrix> data;

		DoubleMatrix1D ubpqh, ubpql;
		DoubleMatrix2D Apqh, h, Apqh1, Apqh2, Apql, Apql1, Apql2;

		/* data dimensions */
		ng = gen.size();		// number of dispatchable injections

		/* which generators require additional linear constraints
		 * (in addition to simple box constraints) on (Pg,Qg) to correctly
		 * model their PQ capability curves
		 */
		ipqh = nonzero( hasPQcap(gen, "U") );
		ipql = nonzero( hasPQcap(gen, "L") );
		npqh = ipqh.length;		// number of general PQ capability curves (upper)
		npql = ipql.length;		// number of general PQ capability curves (lower)

		data = new HashMap<String, AbstractMatrix>();

		/* make Apqh if there is a need to add general PQ capability curves;
		 * use normalized coefficient rows so multipliers have right scaling
		 * in $$/pu
		 */
		if (npqh > 0) {
			h = DoubleFactory2D.dense.make(npqh, 2);
			data.put("h", h);
			h.viewColumn(0).assign( gen.Qc1max.viewSelection(ipqh).copy().assign(gen.Qc2max.viewSelection(ipqh), dfunc.minus) );
			h.viewColumn(1).assign( gen.Pc2.viewSelection(ipqh).copy().assign(gen.Pc1.viewSelection(ipqh), dfunc.minus) );

			ubpqh = h.viewColumn(0).copy().assign(gen.Pc1.viewSelection(ipqh), dfunc.mult).assign(h.viewColumn(1).copy().assign(gen.Qc1max.viewSelection(ipqh), dfunc.mult), dfunc.plus);
			for (i = 0; i < npqh; i++) {
				tmp = DenseDoubleAlgebra.DEFAULT.norm2(h.viewRow(i));
				h.viewRow(i).assign(dfunc.div(tmp));
				ubpqh.set(i, ubpqh.get(i) / tmp);
			}
			Apqh1 = new SparseRCDoubleMatrix2D(npqh, ng, irange(npqh), ipqh, h.viewColumn(0).toArray(), false, false, false);
			Apqh2 = new SparseRCDoubleMatrix2D(npqh, ng, irange(npqh), ipqh, h.viewColumn(1).toArray(), false, false, false);
			Apqh = DoubleFactory2D.sparse.appendColumns(Apqh1, Apqh2);
			ubpqh.assign(dfunc.div(baseMVA));
		} else {
			data.put("h", DoubleFactory2D.dense.make(0, 0));
			Apqh = DoubleFactory2D.sparse.make(0, 2*ng);
			ubpqh = DoubleFactory1D.dense.make(0);
		}

		/* similarly Apql */
		if (npql > 0) {
			DoubleMatrix2D l = DoubleFactory2D.dense.make(npql, 2);
			data.put("l", l);
			l.viewColumn(0).assign( gen.Qc2min.viewSelection(ipql).copy().assign(gen.Qc1min.viewSelection(ipql), dfunc.minus) );
			l.viewColumn(1).assign( gen.Pc1.viewSelection(ipql).copy().assign(gen.Pc2.viewSelection(ipql), dfunc.minus) );

			ubpql = l.viewColumn(0).copy().assign(gen.Pc1.viewSelection(ipql), dfunc.mult).assign(l.viewColumn(1).copy().assign(gen.Qc1min.viewSelection(ipql), dfunc.mult), dfunc.plus);
			for (i = 0; i < npql; i++) {
				tmp = DenseDoubleAlgebra.DEFAULT.norm2(l.viewRow(i));
				l.viewRow(i).assign(dfunc.div(tmp));
				ubpql.set(i, ubpql.get(i) / tmp);
			}
			Apql1 = new SparseRCDoubleMatrix2D(npql, ng, irange(npqh), ipql, l.viewColumn(0).toArray(), false, false, false);
			Apql2 = new SparseRCDoubleMatrix2D(npql, ng, irange(npqh), ipql, l.viewColumn(1).toArray(), false, false, false);
			Apql = DoubleFactory2D.sparse.appendColumns(Apql1, Apql2);
			ubpql.assign(dfunc.div(baseMVA));
		} else {
			data.put("l", DoubleFactory2D.dense.make(0, 0));
			Apql = DoubleFactory2D.sparse.make(0, 2*ng);
			ubpql = DoubleFactory1D.dense.make(0);
		}

		data.put("ipql", IntFactory1D.dense.make(ipql));
		data.put("ipqh", IntFactory1D.dense.make(ipqh));

		return new Object[] {Apqh, ubpqh, Apql, ubpql, data};
	}

}
