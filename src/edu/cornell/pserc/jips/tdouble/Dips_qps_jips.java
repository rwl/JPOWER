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

package edu.cornell.pserc.jips.tdouble;

import java.util.HashMap;
import java.util.Map;

import cern.colt.matrix.tdouble.DoubleFactory1D;
import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.util.tdouble.Djp_util;
import cern.jet.math.tdouble.DoubleFunctions;

public class Dips_qps_jips {

	private static final DoubleFunctions dfunc = DoubleFunctions.functions;

	public static Object[] ips_qps_jips(DoubleMatrix2D H, DoubleMatrix1D c,
			DoubleMatrix2D A, DoubleMatrix1D l, DoubleMatrix1D u) {
		return ips_qps_jips(H, c, A, l, u, null, null);
	}

	public static Object[] ips_qps_jips(DoubleMatrix2D H, DoubleMatrix1D c,
			DoubleMatrix2D A, DoubleMatrix1D l, DoubleMatrix1D u,
			DoubleMatrix1D xmin, DoubleMatrix1D xmax) {
		return ips_qps_jips(H, c, A, l, u, xmin, xmax, null);
	}

	public static Object[] ips_qps_jips(DoubleMatrix2D H, DoubleMatrix1D c,
			DoubleMatrix2D A, DoubleMatrix1D l, DoubleMatrix1D u,
			DoubleMatrix1D xmin, DoubleMatrix1D xmax, DoubleMatrix1D x0) {
		return ips_qps_jips(H, c, A, l, u, xmin, xmax, x0, new HashMap<String, Double>());
	}

	public static Object[] ips_qps_jips(DoubleMatrix2D H, DoubleMatrix1D c,
			DoubleMatrix2D A, DoubleMatrix1D l, DoubleMatrix1D u,
			DoubleMatrix1D xmin, DoubleMatrix1D xmax, DoubleMatrix1D x0, Map<String, Double> opt) {

		/* define nx, set default values for H and c */
		int nx = 0;
		if (H == null || H.size() == 0 || !Djp_util.any(Djp_util.any(H))) {
			if (A == null || A.size() == 0 && xmin == null ||
					xmin.size() == 0 && xmax == null || xmax.size() == 0) {
				System.err.println("qps_mips: LP problem must include constraints or variable bounds");
			} else {
				if (A != null && A.size() != 0) {
					nx = A.columns();
				} else if (xmin != null && xmin.size() != 0) {
					nx = (int) xmin.size();
				} else { //if (xmax != null && xmax.size() != 0)
					nx = (int) xmax.size();
				}
			}
			H = DoubleFactory2D.sparse.make(nx, nx);
		} else {
			nx = H.rows();
		}
		if (c == null || c.size() == 0)
			c = DoubleFactory1D.dense.make(nx);
		if (x0 == null || x0.size() == 0)
			x0 = DoubleFactory1D.dense.make(nx);

		/* -----  run optimization  ----- */

		class Dips_qp_f implements ObjectiveEvaluator {

			DoubleMatrix1D x;
			DoubleMatrix2D H;
			DoubleMatrix1D c;

			public Dips_qp_f(DoubleMatrix1D x, DoubleMatrix2D H, DoubleMatrix1D c) {
				this.x = x;
				this.H = H;
				this.c = c;
			}

			public double f(DoubleMatrix1D x) {
				return 0.5 * H.zMult(x, null).zDotProduct(x) + c.zDotProduct(x);
			}

			public DoubleMatrix1D df(DoubleMatrix1D x) {
				return H.zMult(x, null).assign(c, dfunc.plus);
			}

			public DoubleMatrix2D d2f(DoubleMatrix1D x) {
				return H;
			}
		}

		Dips_qp_f f_fcn = new Dips_qp_f(x0, H, c);

		return Dips_jips.ips_jips(f_fcn, x0, A, l, u, xmin, xmax);
	}

}
