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

import cern.colt.matrix.tint.IntMatrix1D;

import static edu.emory.mathcs.utils.Utils.ifunc;
import static edu.emory.mathcs.utils.Utils.dfunc;
import static edu.emory.mathcs.utils.Utils.intm;

import edu.cornell.pserc.jpower.jpc.Gen;

/**
 * Generator of dispatchable load test.
 */
@SuppressWarnings("static-access")
public class Djp_isload {

	/**
	 * @return a vector of 1's and 0's. The 1's correspond to rows
	 * of the gen matrix which represent dispatchable loads.
	 * The current test is Pmin < 0 AND Pmax == 0.
	 */
	public static IntMatrix1D isload(Gen gen) {
		IntMatrix1D lt, eq;

		lt = intm(gen.Pmin.copy().assign( dfunc.less(0) ));
		eq = intm(gen.Pmax.copy().assign( dfunc.equals(0) ));

		return lt.assign(eq, ifunc.and);
	}

}
