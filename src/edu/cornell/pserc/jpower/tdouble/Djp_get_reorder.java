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

package edu.cornell.pserc.jpower.tdouble;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;

/**
 * Returns A with one of its dimensions indexed.
 *
 * @author Ray Zimmerman
 * @author Richard Lincoln
 *
 */
public class Djp_get_reorder {

	/**
	 * Returns A(:, ..., :, IDX, :, ..., :), where DIM determines
	 * in which dimension to place the IDX.
	 *
	 * @param A
	 * @param idx
	 * @return an indexed copy of A.
	 */
	public static DoubleMatrix1D jp_get_reorder(DoubleMatrix1D A, int[] idx) {
		return A.viewSelection(idx).copy();
	}

	/**
	 * Returns A(:, ..., :, IDX, :, ..., :), where DIM determines
	 * in which dimension to place the IDX.
	 *
	 * @param A
	 * @param idx
	 * @param dim 1 - index rows, 2 - index columns.
	 * @return a copy of A indexed in dimension dim.
	 */
	public static DoubleMatrix2D jp_get_reorder(DoubleMatrix2D A, int[] idx, int dim) {
		if (dim == 1) {
			return A.viewSelection(idx, null).copy();
		} else if (dim == 2) {
			return A.viewSelection(null, idx).copy();
		} else {
			throw new UnsupportedOperationException();
		}
	}
}
