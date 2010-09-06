/*
 * Copyright (C) 1996-2010 by Power System Engineering Research Center (PSERC)
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

package edu.cornell.pserc.jpower.tdcomplex;

import cern.colt.list.tint.IntArrayList;
import cern.colt.matrix.tdcomplex.DComplexFactory1D;
import cern.colt.matrix.tdcomplex.DComplexMatrix1D;
import cern.colt.matrix.tdcomplex.impl.SparseRCDComplexMatrix2D;
import edu.cornell.pserc.jpower.tdcomplex.jpc.DZjp_bus;
import edu.cornell.pserc.jpower.tdcomplex.jpc.DZjp_gen;

/**
 * Builds the vector of complex bus power injections.
 *
 * @author Ray Zimmerman (rz10@cornell.edu)
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class DZjp_makeSbus extends DZjp_idx {

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
	public static DComplexMatrix1D jp_makeSbus(double baseMVA, DZjp_bus bus, DZjp_gen gen) {

		/* generator info */
		IntArrayList on = new IntArrayList();		// which generators are on?
		gen.gen_status.assign(ifunc.equals(1)).getNonZeros(on, null);
		int[] gbus = gen.gen_bus.viewSelection(on.elements()).toArray();

		/* form net complex bus power injection vector */
		int nb = bus.size();
		int ngon = on.size();
		// connection matrix, element i, j is 1 if gen on(j) at bus i is ON
		SparseRCDComplexMatrix2D Cg = new SparseRCDComplexMatrix2D(nb, ngon,
				gbus, irange(ngon), 1, 0, false);

		DComplexMatrix1D Sg = DComplexFactory1D.dense.make(nb);
		Sg.assignReal(gen.Pg.viewSelection(on.elements()));
		Sg.assignImaginary(gen.Qg.viewSelection(on.elements()));

		DComplexMatrix1D Sd = DComplexFactory1D.dense.make(nb);
		Sd.assignReal(bus.Pd);
		Sd.assignImaginary(bus.Qd);

		// power injected by generators plus power injected by loads ...
		DComplexMatrix1D Sbus = Cg.zMult(Sg.assign(Sd, cfunc.minus), null);
		Sbus.assign(cfunc.div(baseMVA));	// converted to p.u.

		return Sbus;
	}

}
