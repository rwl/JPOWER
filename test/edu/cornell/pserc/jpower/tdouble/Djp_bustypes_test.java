/*
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

import java.io.File;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tint.IntMatrix1D;
import cern.colt.matrix.tint.algo.IntProperty;
import cern.jet.math.tint.IntFunctions;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_jpc;
import edu.cornell.pserc.jpower.tdouble.util.Djp_mm;

/**
 *
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public abstract class Djp_bustypes_test extends Djp_base_test {

	protected Djp_jpc jpc;

	public Djp_bustypes_test(String name) {
		super(name);
		this.fname = "bustypes";
		/* Set 'jpc' in subclasses. */
	}

	@SuppressWarnings("static-access")
	public void test_bustypes() {
		Djp_jpc jpc = Djp_loadcase.jp_loadcase(this.jpc);
		jpc = Djp_ext2int.jp_ext2int(jpc);
		IntMatrix1D[] bustypes = Djp_bustypes.jp_bustypes(jpc.bus, jpc.gen);

		File ref_file = new File(fdir, "ref.mtx");
		File pv_file = new File(fdir, "pv.mtx");
		File pq_file = new File(fdir, "pq.mtx");

		IntMatrix1D ref = util.intm((DoubleMatrix1D) Djp_mm.readMatrix(ref_file));
		IntMatrix1D pv = util.intm((DoubleMatrix1D) Djp_mm.readMatrix(pv_file));
		IntMatrix1D pq = util.intm((DoubleMatrix1D) Djp_mm.readMatrix(pq_file));

		ref.assign(IntFunctions.minus(1)); // Correct for Matlab indexing.
		pv.assign(IntFunctions.minus(1));
		pq.assign(IntFunctions.minus(1));

		assertTrue(iprop.equals(bustypes[0], ref));
		assertTrue(iprop.equals(bustypes[1], pv));
		assertTrue(iprop.equals(bustypes[2], pq));
	}

}
