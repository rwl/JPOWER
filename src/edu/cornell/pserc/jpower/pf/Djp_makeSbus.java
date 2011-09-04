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

package edu.cornell.pserc.jpower.pf;

import cern.colt.matrix.tdcomplex.DComplexMatrix1D;
import cern.colt.matrix.tdcomplex.impl.SparseRCDComplexMatrix2D;

import edu.cornell.pserc.jpower.jpc.Bus;
import edu.cornell.pserc.jpower.jpc.Gen;

import static cern.colt.util.tdouble.Util.cfunc;
import static cern.colt.util.tdouble.Util.nonzero;
import static cern.colt.util.tdouble.Util.complex;
import static cern.colt.util.tdouble.Util.irange;

/**
 * Builds the vector of complex bus power injections.
 *
 * @author Ray Zimmerman
 * @author Richard Lincoln
 *
 */
public class Djp_makeSbus {

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
	public static DComplexMatrix1D makeSbus(double baseMVA, Bus bus, Gen gen) {
		int nb, ngon;
		int[] on, gbus;
		DComplexMatrix1D Sg, Sd, Sbus;
		SparseRCDComplexMatrix2D Cg;

		/* generator info */
		on = nonzero(gen.gen_status);  // which generators are on?
		gbus = gen.gen_bus.viewSelection(on).toArray();

		/* form net complex bus power injection vector */
		nb = bus.size();
		ngon = on.length;

		// connection matrix, element i, j is 1 if gen on(j) at bus i is ON
		Cg = new SparseRCDComplexMatrix2D(nb, ngon, gbus, irange(ngon), 1, 0, false);

		Sg = complex(gen.Pg.viewSelection(on), gen.Qg.viewSelection(on));
		Sd = complex(bus.Pd, bus.Qd);
		Sbus = Cg.zMult(Sg, null);	  // power injected by generators
		Sbus.assign(Sd, cfunc.minus);	  // plus power injected by loads
		Sbus.assign(cfunc.div(baseMVA));  // converted to p.u.

		return Sbus;
	}

}
