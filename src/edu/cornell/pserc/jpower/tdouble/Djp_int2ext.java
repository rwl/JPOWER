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

package edu.cornell.pserc.jpower.tdouble;

import cern.colt.matrix.tdouble.DoubleMatrix2D;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_jpc;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_order;

/**
 * Converts internal to external bus numbering.
 *
 * @author Ray Zimmerman (rz10@cornell.edu)
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class Djp_int2ext {

	public static DoubleMatrix2D[] jp_int2ext(DoubleMatrix2D bus, DoubleMatrix2D gen,
			DoubleMatrix2D branch) {
		return jp_int2ext(bus, gen, branch, null);
	}

	/**
	 *
	 * @param bus
	 * @param gen
	 * @param branch
	 * @param areas
	 * @return
	 */
	public static DoubleMatrix2D[] jp_int2ext(DoubleMatrix2D bus,
			DoubleMatrix2D gen, DoubleMatrix2D branch, DoubleMatrix2D areas) {
		return null;
	}


	/**
	 * If the input is a single JPOWER case object, then it restores all
	 * buses, generators and branches that were removed because of being
	 * isolated or off-line, and reverts to the original generator ordering
	 * and original bus numbering. This requires that the 'order' field
	 * created by EXT2INT be in place.
	 *
	 * @param jpc
	 * @return
	 */
	public static Djp_jpc jp_int2ext(Djp_jpc jpc) {

		if (jpc.order == null)
			System.err.println("int2ext: jpc does not have the 'order' set, as required for conversion back to external numbering.");
			// TODO: throw missing null order exception
		Djp_order o = jpc.order;

		if (o.state == "i") {
			/* execute userfcn callbacks for 'int2ext' stage */
			if (jpc.userfcn != null)
				jpc = Djp_run_userfcn.jp_run_userfcn(jpc.userfcn, "int2ext", jpc);

			/* save data matrices with internal ordering & restore originals */
			o.internal = new Djp_jpc();
			o.internal.bus    = jpc.bus.copy();
			o.internal.branch = jpc.branch.copy();
			o.internal.gen    = jpc.gen.copy();
			jpc.bus    = o.external.bus.copy();
			jpc.branch = o.external.branch.copy();
			jpc.gen    = o.external.gen.copy();
			if (jpc.gencost != null) {
				o.internal.gencost = jpc.gencost.copy();
				jpc.gencost = o.external.gencost.copy();
			}
			if (jpc.areas != null) {
				o.internal.areas = jpc.areas.copy();
				jpc.areas = o.external.areas.copy();
			}
			if (jpc.A != null) {
				o.internal.A = jpc.A.copy();
				jpc.A = o.external.A.copy();
			}
			if (jpc.N != null) {
				o.internal.N = jpc.N.copy();
				jpc.N = o.external.N.copy();
			}

			/* update data (in bus, branch and gen only) */
			jpc.bus.update(o.internal.bus, o.bus.status.on);
			jpc.branch.update(o.internal.branch, o.branch.status.on);
			jpc.gen.update(o.internal.gen.copy(o.gen.i2e.toArray()), o.branch.status.on);
			if (jpc.areas != null)
				jpc.areas.update(o.internal.areas, o.areas.status.on);

			/* revert to original bus numbers */
		}

		return null;
	}

}
