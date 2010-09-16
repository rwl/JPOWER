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

package edu.cornell.pserc.jpower.tdouble;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;

/**
 * Assigns B to A with one of the dimensions of A indexed.
 *
 * @author Ray Zimmerman (rz10@cornell.edu)
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class Djp_set_reorder {

	/**
	 *
	 * @param A
	 * @param idx
	 * @return an indexed copy of A.
	 */
	public static DoubleMatrix1D jp_set_reorder(DoubleMatrix1D A, DoubleMatrix1D B, int[] idx) {
		return A.viewSelection(idx).assign(B);
	}

	/**
	 * Returns A after doing A(:, ..., :, IDX, :, ..., :) = B
	 * where DIM determines in which dimension to place the IDX.
	 *
	 * @param A
	 * @param idx
	 * @param dim 1 - index rows, 2 - index columns
	 * @return
	 */
	public static DoubleMatrix2D jp_set_reorder(DoubleMatrix2D A, DoubleMatrix2D B, int[] idx, int dim) {
		if (dim == 1) {
			return A.viewSelection(idx, null).assign(B);
		} else if (dim == 2) {
			return A.viewSelection(null, idx).assign(B);
		} else {
			throw new UnsupportedOperationException();
		}
	}
}
