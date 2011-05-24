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

import cern.colt.matrix.tdcomplex.DComplexMatrix1D;
import cern.colt.matrix.tdcomplex.impl.SparseRCDComplexMatrix2D;
import cern.colt.util.tdouble.Djp_util;
import cern.jet.math.tdcomplex.DComplexFunctions;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_bus;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_gen;

/**
 * Builds the vector of complex bus power injections.
 *
 * @author Ray Zimmerman
 * @author Richard Lincoln
 *
 */
public class Djp_makeSbus {

	private static final DComplexFunctions cfunc = DComplexFunctions.functions;

	private static int nb, ngon;
	private static int[] on, gbus;
	private static DComplexMatrix1D Sg, Sd, Sbus;
	private static SparseRCDComplexMatrix2D Cg;

	/**
	 * Returns the vector of complex bus
	 * power injections, that is, generation minus load. Power is expressed
	 * in per unit.
	 *
	 * @see makeYbus
	 * @param baseMVA system base MVA
	 * @param bus bus data
	 * @param gen generator data
	 * @return vector of complex bus power injections
	 */
	@SuppressWarnings("static-access")
	public static DComplexMatrix1D jp_makeSbus(double baseMVA, Djp_bus bus, Djp_gen gen) {

		/* generator info */
		on = Djp_util.nonzero(gen.gen_status);		// which generators are on?
		gbus = gen.gen_bus.viewSelection(on).toArray();

		/* form net complex bus power injection vector */
		nb = bus.size();
		ngon = on.length;

		// connection matrix, element i, j is 1 if gen on(j) at bus i is ON
		Cg = new SparseRCDComplexMatrix2D(nb, ngon, gbus, Djp_util.irange(ngon), 1, 0, false);

		Sg = Djp_util.complex(gen.Pg.viewSelection(on), gen.Qg.viewSelection(on));
		Sd = Djp_util.complex(bus.Pd, bus.Qd);
		Sbus = Cg.zMult(Sg, null);	// power injected by generators
		Sbus.assign(Sd, cfunc.minus);				// plus power injected by loads
		Sbus.assign(cfunc.div(baseMVA));			// converted to p.u.

		return Sbus;
	}

}
