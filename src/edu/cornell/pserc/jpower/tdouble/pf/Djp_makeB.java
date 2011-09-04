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

package edu.cornell.pserc.jpower.tdouble.pf;

import cern.colt.matrix.tdcomplex.DComplexMatrix2D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.jet.math.tdouble.DoubleFunctions;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_branch;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_bus;

/**
 * Builds the FDPF matrices, B prime and B double prime.
 *
 * @author Ray Zimmerman
 * @author Richard Lincoln
 *
 */
public class Djp_makeB {

	private static final DoubleFunctions dfunc = DoubleFunctions.functions;

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
	@SuppressWarnings("static-access")
	public static DoubleMatrix2D[] makeB(double baseMVA, Djp_bus bus, Djp_branch branch, int alg) {
		Djp_branch temp_branch;
		Djp_bus temp_bus;
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

		Ybus = Djp_makeYbus.makeYbus(baseMVA, temp_bus, temp_branch);
		Bp = Ybus[0].getImaginaryPart().assign(dfunc.neg);

		/* -----  form Bpp (B double prime)  ----- */
		temp_branch = branch.copy();				// modify a copy of branch
		temp_branch.shift.assign(0);				// zero out phase shifters
		if (alg == 3)								// if BX method
			temp_branch.br_b.assign(0);				// zero out line resistance

		Ybus = Djp_makeYbus.makeYbus(baseMVA, temp_bus, temp_branch);
		Bpp = Ybus[0].getImaginaryPart().assign(dfunc.neg);

		return new DoubleMatrix2D[] {Bp, Bpp};
	}

}
