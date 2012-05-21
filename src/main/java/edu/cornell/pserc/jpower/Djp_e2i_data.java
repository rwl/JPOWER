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

package edu.cornell.pserc.jpower;

import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tint.IntFactory1D;
import edu.cornell.pserc.jpower.jpc.JPC;
import edu.cornell.pserc.jpower.jpc.Order;

import static edu.cornell.pserc.jpower.Djp_get_reorder.get_reorder;

import static edu.emory.mathcs.utils.Utils.irange;

public class Djp_e2i_data {

	public static DoubleMatrix2D e2i_data(JPC jpc, DoubleMatrix2D val,
			Ordering[] ordering, int dim) {

		int b, n, k;
		int[] idx;
		Ordering ordr;
		Order o;
		DoubleMatrix2D int_val2, v2, new_v2;

		o = jpc.order;

		if (dim < 0 || dim > 1)
			throw new UnsupportedOperationException();

		if (ordering.length == 1) {		// single set
			if (ordering[0] == Ordering.GEN) {
				int[] e2i = o.gen.e2i.toArray();
				idx = IntFactory1D.dense.make(o.gen.status.on).viewSelection(e2i).toArray();
			} else if (ordering[0] == Ordering.BUS) {
				idx = o.bus.status.on;
			} else {
				idx = o.branch.status.on;
			}

			int_val2 = get_reorder(val, idx, dim);
		} else {				// multiple sets
			if (dim == 0) {
				int_val2 = DoubleFactory2D.dense.make(val.rows(), 0);
			} else {
				int_val2 = DoubleFactory2D.dense.make(0, val.columns());
			}

			b = 0;				// base
			for (k = 0; k < ordering.length; k++) {
				ordr = ordering[k];

				if (ordr == Ordering.GEN) {
					n = o.external.gen.size();
				} else if (ordr == Ordering.BUS) {
					n = o.external.bus.size();
				} else {
					n = o.external.branch.size();
				}

				v2 = get_reorder(val, irange(b, b + n), dim);
				new_v2 = e2i_data(jpc, v2, ordering[k], dim);

				if (dim == 0) {
					int_val2 = DoubleFactory2D.dense.appendRows(int_val2, new_v2);
				} else {
					int_val2 = DoubleFactory2D.dense.appendColumns(int_val2, new_v2);
				}

				b += n;
			}

			if (dim == 0) {
				n = val.rows();
			} else {
				n = val.columns();
			}

			if (n > b) {				// the rest
				new_v2 = get_reorder(val, irange(b, b + n), dim);
				if (dim == 0) {
					int_val2 = DoubleFactory2D.dense.appendRows(int_val2, new_v2);
				} else {
					int_val2 = DoubleFactory2D.dense.appendColumns(int_val2, new_v2);
				}
			}
		}

		return int_val2;
	}

	public static DoubleMatrix2D e2i_data(JPC jpc, DoubleMatrix2D val,
			Ordering ordering) {
		return e2i_data(jpc, val, new Ordering[] {ordering}, 0);
	}

	public static DoubleMatrix2D e2i_data(JPC jpc, DoubleMatrix2D val,
			Ordering ordering, int dim) {
		return e2i_data(jpc, val, new Ordering[] {ordering}, dim);
	}

	public static DoubleMatrix2D e2i_data(JPC jpc, DoubleMatrix2D val,
			Ordering[] ordering) {
		return e2i_data(jpc, val, ordering, 0);
	}

}
