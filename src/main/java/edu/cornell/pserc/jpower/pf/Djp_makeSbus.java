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

import cern.colt.matrix.tdcomplex.DComplexMatrix1D;
import cern.colt.matrix.tdcomplex.impl.SparseRCDComplexMatrix2D;

import edu.cornell.pserc.jpower.jpc.Bus;
import edu.cornell.pserc.jpower.jpc.Gen;

import static edu.emory.mathcs.utils.Utils.cfunc;
import static edu.emory.mathcs.utils.Utils.nonzero;
import static edu.emory.mathcs.utils.Utils.complex;
import static edu.emory.mathcs.utils.Utils.irange;

/**
 * Builds the vector of complex bus power injections.
 *
 * @author Ray Zimmerman
 * @author Richard Lincoln
 *
 */
@SuppressWarnings("static-access")
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
	public static DComplexMatrix1D makeSbus(double baseMVA, Bus bus, Gen gen) {
		int nb, ngon;
		int[] on, gbus;
		DComplexMatrix1D Sg, Sd, Sbus;
		SparseRCDComplexMatrix2D Cg;

		/* generator info */
		on = nonzero(gen.gen_status);		// which generators are on?
		gbus = gen.gen_bus.viewSelection(on).toArray();

		/* form net complex bus power injection vector */
		nb = bus.size();
		ngon = on.length;

		// connection matrix, element i, j is 1 if gen on(j) at bus i is ON
		Cg = new SparseRCDComplexMatrix2D(nb, ngon, gbus, irange(ngon), 1, 0, false);

		Sg = complex(gen.Pg.viewSelection(on), gen.Qg.viewSelection(on));
		Sd = complex(bus.Pd, bus.Qd);
		Sbus = Cg.zMult(Sg, null);	// power injected by generators
		Sbus.assign(Sd, cfunc.minus);				// plus power injected by loads
		Sbus.assign(cfunc.div(baseMVA));			// converted to p.u.

		return Sbus;
	}

}
