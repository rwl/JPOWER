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

import cern.colt.matrix.tdouble.DoubleFactory1D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tint.IntMatrix1D;
import cern.colt.util.tdouble.Djp_util;
import cern.jet.math.tdouble.DoubleFunctions;
import cern.jet.math.tint.IntFunctions;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_gen;

/**
 * Checks for P-Q capability curve constraints.
 *
 * @author Ray Zimmerman
 * @author Richard Lincoln
 *
 */
public class Djp_hasPQcap {

	private static final DoubleFunctions dfunc = DoubleFunctions.functions;
	private static final IntFunctions ifunc = IntFunctions.intFunctions;

	private static int[] k;
	private static DoubleMatrix1D L, U, Qmin_at_Pmax, Qmax_at_Pmax;

	/**
	 * Returns a column vector of 1's and 0's. The 1's
	 * correspond to rows of the GEN matrix which correspond to generators which
	 * have defined a capability curve (with sloped upper and/or lower bound on
	 * Q) and require that additional linear constraints be added to the OPF.
	 *
	 * The GEN matrix in version 2 of the MATPOWER case format includes columns
	 * for specifying a P-Q capability curve for a generator defined as the
	 * intersection of two half-planes and the box constraints on P and Q. The
	 * two half planes are defined respectively as the area below the line
	 * connecting (Pc1, Qc1max) and (Pc2, Qc2max) and the area above the line
	 * connecting (Pc1, Qc1min) and (Pc2, Qc2min).
	 *
	 * If the optional 2nd argument is 'U' this function returns true only for
	 * rows corresponding to generators that require the upper constraint on Q.
	 * If it is 'L', only for those requiring the lower constraint. If the 2nd
	 * argument is not specified or has any other value it returns true for rows
	 * corresponding to gens that require either or both of the constraints.
	 *
	 * It is smart enough to return true only if the corresponding linear
	 * constraint is not redundant w.r.t the box constraints.
	 *
	 * @param gen
	 * @param string
	 * @return
	 */
	@SuppressWarnings("static-access")
	public static IntMatrix1D hasPQcap(Djp_gen gen, String hilo) {

		/* check for errors capability curve data */
		if ( Djp_util.any(gen.Pc1.copy().assign(gen.Pc2, dfunc.greater)) )
			System.err.println("hasPQcap: Pc1 > Pc2");
			// TODO: throw invalid capability curve data exception

		if ( Djp_util.any(gen.Qc2max.copy().assign(gen.Qc1max, dfunc.greater)) )
			System.err.println("hasPQcap: Qc2max > Qc1max");

		if ( Djp_util.any(gen.Qc2min.copy().assign(gen.Qc1min, dfunc.less)) )
			System.err.println("hasPQcap: Qc2min < Qc1min");

		L = DoubleFactory1D.dense.make(gen.size());
		U = DoubleFactory1D.dense.make(gen.size());
		k = Djp_util.nonzero(Djp_util.intm( gen.Pc1.copy().assign(gen.Pc2, dfunc.equals) ).assign(ifunc.not));

		if (hilo != "U") {		// include lower constraint
			Qmin_at_Pmax = gen.Qc2min.viewSelection(k).copy().assign(gen.Qc1min, dfunc.minus);
			Qmin_at_Pmax.assign(gen.Pc2.viewSelection(k).copy().assign(gen.Pc1.viewSelection(k), dfunc.minus), dfunc.div);
			Qmin_at_Pmax.assign(gen.Pmax.viewSelection(k).copy().assign(gen.Pc1, dfunc.minus), dfunc.mult);
			Qmin_at_Pmax.assign(gen.Qc1min.viewSelection(k).copy(), dfunc.plus);

			L.viewSelection(k).assign( Qmin_at_Pmax.assign(gen.Qmin.viewSelection(k), dfunc.greater) );
		}

		if (hilo != "U") {		// include upper constraint
			Qmax_at_Pmax = gen.Qc2max.viewSelection(k).copy().assign(gen.Qc1max, dfunc.minus);
			Qmax_at_Pmax.assign( gen.Pc2.viewSelection(k).copy().assign(gen.Pc1.viewSelection(k), dfunc.minus), dfunc.div );
			Qmax_at_Pmax.assign( gen.Pmax.viewSelection(k).copy().assign(gen.Pc1, dfunc.minus), dfunc.mult );
			Qmax_at_Pmax.assign( gen.Qc1max.viewSelection(k).copy(), dfunc.plus );

			U.viewSelection(k).assign( Qmax_at_Pmax.assign(gen.Qmax.viewSelection(k), dfunc.less) );
		}

		return Djp_util.intm(L).assign(Djp_util.intm(U), ifunc.or);
	}

	public static IntMatrix1D hasPQcap(Djp_gen gen) {
		return hasPQcap(gen, "B");	// look at both top and bottom by default
	}

}
