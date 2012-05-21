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

package edu.cornell.pserc.jpower;

import cern.colt.matrix.tint.IntFactory1D;
import cern.colt.matrix.tint.IntMatrix1D;
import cern.colt.matrix.tint.impl.SparseRCIntMatrix2D;

import static edu.emory.mathcs.utils.Utils.ifunc;
import static edu.emory.mathcs.utils.Utils.irange;
import static edu.emory.mathcs.utils.Utils.nonzero;

import edu.cornell.pserc.jpower.jpc.Bus;
import edu.cornell.pserc.jpower.jpc.Gen;

import static edu.cornell.pserc.jpower.jpc.JPC.PQ;
import static edu.cornell.pserc.jpower.jpc.JPC.PV;
import static edu.cornell.pserc.jpower.jpc.JPC.REF;

/**
 * Builds index lists for each type of bus (REF, PV, PQ).
 *
 * @author Ray Zimmerman
 * @author Richard Lincoln
 *
 */
public class Djp_bustypes {

	/**
	 * Generators with "out-of-service" status are treated as PQ buses with
	 * zero generation (regardless of Pg/Qg values in gen). Expects BUS and
	 * GEN have been converted to use internal consecutive bus numbering.
	 *
	 * @param bus
	 * @param gen
	 * @return
	 */
	@SuppressWarnings("static-access")
	public static IntMatrix1D[] bustypes(Bus bus, Gen gen) {
		int nb, ng;
		SparseRCIntMatrix2D Cg;
		IntMatrix1D bus_gen_status, ref_types, ref, pv_types, pv, pq_types, pq;

		/* get generator status */
		nb = bus.size();
		ng = gen.size();

		/* gen connection matrix, element i, j is 1 if, generator j at bus i is ON */
		Cg = new SparseRCIntMatrix2D(nb, ng,
				gen.gen_bus.toArray(), irange(ng),
				gen.gen_status.copy().assign(ifunc.equals(1)).toArray(), false, false, false);

		/* number of generators at each bus that are ON */
		bus_gen_status = Cg.zMult(IntFactory1D.dense.make(ng, 1), null);
		/* make boolean for 'and' operation */
		bus_gen_status.assign(ifunc.equals(0)).assign(ifunc.equals(0));

		/* form index lists for slack, PV, and PQ buses */
		ref_types = bus.bus_type.copy().assign(ifunc.equals(REF));
		ref_types.assign(bus_gen_status, ifunc.and);		// reference bus index
		ref = IntFactory1D.dense.make(nonzero(ref_types));

		pv_types = bus.bus_type.copy().assign(ifunc.equals(PV));
		pv_types.assign(bus_gen_status, ifunc.and);		// PV bus indices
		pv = IntFactory1D.dense.make(nonzero(pv_types));

		pq_types = bus.bus_type.copy().assign(ifunc.equals(PQ));
		pq_types.assign(bus_gen_status.assign(ifunc.equals(0)), ifunc.or);
		pq = IntFactory1D.dense.make(nonzero(pq_types));

		/* pick a new reference bus if for some reason there is none (may have been shut down) */
		if (ref.size() == 0) {
			ref = IntFactory1D.dense.make(1, pv.get(0));		// use the first PV bus
			pv = pv.viewPart(1, (int) (pv.size() - 1)).copy();	// take it off PV list
		}

		return new IntMatrix1D[] {ref, pv, pq};
	}

}
