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

package edu.cornell.pserc.jpower.pf;

import cern.colt.matrix.tdcomplex.DComplexMatrix2D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;

import static edu.emory.mathcs.utils.Utils.dfunc;

import edu.cornell.pserc.jpower.jpc.Branch;
import edu.cornell.pserc.jpower.jpc.Bus;

import static edu.cornell.pserc.jpower.pf.Djp_makeYbus.makeYbus;

/**
 * Builds the FDPF matrices, B prime and B double prime.
 *
 * @author Ray Zimmerman
 * @author Richard Lincoln
 *
 */
@SuppressWarnings("static-access")
public class Djp_makeB {

	/**
	 * Returns the two
	 * matrices B prime and B double prime used in the fast decoupled power
	 * flow. Does appropriate conversions to p.u. ALG is the value of the
	 * PF_ALG option specifying the power flow algorithm.
	 *
	 * @param baseMVA
	 * @param bus
	 * @param branch
	 * @param alg
	 * @return
	 */
	public static DoubleMatrix2D[] makeB(double baseMVA, Bus bus, Branch branch, int alg) {
		Branch temp_branch;
		Bus temp_bus;
		DComplexMatrix2D[] Ybus;
		DoubleMatrix2D Bp, Bpp;

		/* -----  form Bp (B prime)  ----- */
		temp_branch = branch.copy();		// modify a copy of branch
		temp_bus = bus.copy();				// modify a copy of bus
		temp_bus.Bs.assign(0);						// zero out shunts at buses
		temp_branch.br_b.assign(0);					// zero out line charging shunts
		temp_branch.tap.assign(1);					// cancel out taps
		if (alg == 2)								// if XB method
			temp_branch.br_r.assign(0);				// zero out line resistance

		Ybus = makeYbus(baseMVA, temp_bus, temp_branch);
		Bp = Ybus[0].getImaginaryPart().assign(dfunc.neg);

		/* -----  form Bpp (B double prime)  ----- */
		temp_branch = branch.copy();				// modify a copy of branch
		temp_branch.shift.assign(0);				// zero out phase shifters
		if (alg == 3)								// if BX method
			temp_branch.br_b.assign(0);				// zero out line resistance

		Ybus = makeYbus(baseMVA, temp_bus, temp_branch);
		Bpp = Ybus[0].getImaginaryPart().assign(dfunc.neg);

		return new DoubleMatrix2D[] {Bp, Bpp};
	}
}
