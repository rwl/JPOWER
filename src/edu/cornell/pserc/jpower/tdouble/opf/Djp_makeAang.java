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

import java.util.Map;

import cern.colt.matrix.AbstractMatrix;
import cern.colt.matrix.tdouble.DoubleFactory1D;
import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.SparseRCDoubleMatrix2D;
import cern.colt.matrix.tint.IntFactory1D;
import cern.colt.matrix.tint.IntMatrix1D;

import static cern.colt.util.tdouble.Util.dfunc;
import static cern.colt.util.tdouble.Util.icat;
import static cern.colt.util.tdouble.Util.nonzero;
import static cern.colt.util.tdouble.Util.irange;

import edu.cornell.pserc.jpower.tdouble.jpc.Branch;

/**
 * Construct constraints for branch angle difference limits.
 *
 * @author Ray Zimmerman
 * @author Richard Lincoln
 *
 */
public class Djp_makeAang {

	/**
	 * Constructs the parameters for the following linear constraint limiting
	 * the voltage angle differences across branches, where Va is the vector
	 * of bus voltage angles. NB is the number of buses.
	 *
	 * 	LANG <= AANG * Va <= UANG
	 *
	 * IANG is the vector of indices of branches with angle difference limits.
	 *
	 * @param baseMVA
	 * @param branch
	 * @param nb
	 * @param mpopt
	 * @return
	 */
	@SuppressWarnings("static-access")
	public static AbstractMatrix[] makeAang(double baseMVA, Branch branch, int nb, Map<String, Double> jpopt) {
		int nang;
		int[] iang_min, iang_max, iang_a, iangl, iangh, ii, jj;
		boolean ignore_ang_lim;

		IntMatrix1D iang;
		DoubleMatrix2D Aang;
		DoubleMatrix1D lang, uang, v;

		/* options */
		ignore_ang_lim = jpopt.get("OPF_IGNORE_ANG_LIM") == 1;

		if (ignore_ang_lim) {
			Aang = DoubleFactory2D.sparse.make(0, nb);
			lang = DoubleFactory1D.dense.make(0);
			uang = DoubleFactory1D.dense.make(0);
			iang = IntFactory1D.dense.make(0);
		} else {
			iang_min = new int[0]; iang_max = new int[0];
			if (branch.ang_min != null)
				iang_min = nonzero(branch.ang_min.copy().assign(dfunc.greater(-360)));
			if (branch.ang_max != null)
				iang_max = nonzero(branch.ang_max.copy().assign(dfunc.less(360)));

			iang_a = icat(iang_min, iang_max);
			iang = IntFactory1D.dense.make(iang_a);

			iangl = new int[0]; iangh = new int[0];
			if (branch.ang_min != null)
				iangl = nonzero(branch.ang_min.viewSelection(iang_a));
			if (branch.ang_max != null)
				iangh = nonzero(branch.ang_max.viewSelection(iang_a));
			nang = iang_a.length;

			if (nang > 0) {
				ii = icat(irange(nang), irange(nang));
				jj = icat(branch.f_bus.viewSelection(iang_a).toArray(), branch.t_bus.viewSelection(iang_a).toArray());
				v = DoubleFactory1D.dense.append(DoubleFactory1D.dense.make(nang, 1), DoubleFactory1D.dense.make(nang, -1));
				Aang = new SparseRCDoubleMatrix2D(nang, nb, ii, jj, v.toArray(), false, false, false);
				uang = DoubleFactory1D.dense.make(nang, Double.POSITIVE_INFINITY);
				lang = DoubleFactory1D.dense.make(nang, Double.NEGATIVE_INFINITY);
				lang.viewSelection(iangl).assign( branch.ang_min.viewSelection(iang.viewSelection(iangl).toArray()).assign(dfunc.mult(Math.PI)).assign(dfunc.div(180)) );
				uang.viewSelection(iangh).assign( branch.ang_max.viewSelection(iang.viewSelection(iangh).toArray()).assign(dfunc.mult(Math.PI)).assign(dfunc.div(180)) );
			} else {
				Aang = DoubleFactory2D.sparse.make(0, nb);
				lang = DoubleFactory1D.dense.make(0);
				uang = DoubleFactory1D.dense.make(0);
			}
		}

		return new AbstractMatrix[] {Aang, lang, uang, iang};
	}

}
