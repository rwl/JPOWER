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
 * Assigns B to A with one of the dimensions of A indexed.
 *
 * @author Ray Zimmerman
 * @author Richard Lincoln
 *
 */
public class Djp_set_reorder {

	/**
	 * Returns A after doing A(:, ..., :, IDX, :, ..., :) = B
	 * where DIM determines in which dimension to place the IDX.
	 *
	 * @param A
	 * @param idx
	 * @param dim 1 - index rows, 2 - index columns
	 * @return
	 */
	public static DoubleMatrix2D set_reorder(DoubleMatrix2D A, DoubleMatrix2D B, int[] idx, int dim) {
		if (dim == 1) {
			return A.viewSelection(idx, null).assign(B);
		} else if (dim == 2) {
			return A.viewSelection(null, idx).assign(B);
		} else {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 *
	 * @param A
	 * @param idx
	 * @return an indexed copy of A.
	 */
	public static DoubleMatrix1D set_reorder(DoubleMatrix1D A, DoubleMatrix1D B, int[] idx) {
		return A.viewSelection(idx).assign(B);
	}

}
