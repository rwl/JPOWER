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

package edu.cornell.pserc.jpower.tdouble.pf;

import cern.colt.list.tint.IntArrayList;
import cern.colt.matrix.tdcomplex.DComplexFactory1D;
import cern.colt.matrix.tdcomplex.DComplexMatrix1D;
import cern.colt.matrix.tdcomplex.impl.SparseRCDComplexMatrix2D;
import cern.jet.math.tdcomplex.DComplexFunctions;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_bus;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_gen;
import edu.cornell.pserc.util.tdouble.Djp_util;

/**
 * Builds the vector of complex bus power injections.
 *
 * @author Ray Zimmerman (rz10@cornell.edu)
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class Djp_makeSbus {

	private static final Djp_util util = new Djp_util();
	private static final DComplexFunctions cfunc = DComplexFunctions.functions;

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
		int[] on = util.nonzero(gen.gen_status);		// which generators are on?
		int[] gbus = gen.gen_bus.viewSelection(on).toArray();

		/* form net complex bus power injection vector */
		int nb = bus.size();
		int ngon = on.length;

		// connection matrix, element i, j is 1 if gen on(j) at bus i is ON
		SparseRCDComplexMatrix2D Cg = new SparseRCDComplexMatrix2D(nb, ngon,
				gbus, util.irange(ngon), 1, 0, false);

		DComplexMatrix1D Sg = util.complex(gen.Pg.viewSelection(on), gen.Qg.viewSelection(on));
		DComplexMatrix1D Sd = util.complex(bus.Pd, bus.Qd);
		DComplexMatrix1D Sbus = Cg.zMult(Sg, null);	// power injected by generators
		Sbus.assign(Sd, cfunc.minus);				// plus power injected by loads
		Sbus.assign(cfunc.div(baseMVA));			// converted to p.u.

		return Sbus;
	}

}
