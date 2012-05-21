/*
 * Copyright (C) 1996-2010 Power System Engineering Research Center
 * Copyright (C) 2010-2011 Richard Lincoln
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package edu.cornell.pserc.jpower;

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
	 * @param dim 1 - index rows, 2 - index columns.
	 * @return a copy of A indexed in dimension dim.
	 */
	public static DoubleMatrix2D get_reorder(DoubleMatrix2D A, int[] idx, int dim) {
		if (dim == 1) {
			return A.viewSelection(idx, null).copy();
		} else if (dim == 2) {
			return A.viewSelection(null, idx).copy();
		} else {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * Returns A(:, ..., :, IDX, :, ..., :), where DIM determines
	 * in which dimension to place the IDX.
	 *
	 * @param A
	 * @param idx
	 * @return an indexed copy of A.
	 */
	public static DoubleMatrix1D get_reorder(DoubleMatrix1D A, int[] idx) {
		return A.viewSelection(idx).copy();
	}

}
