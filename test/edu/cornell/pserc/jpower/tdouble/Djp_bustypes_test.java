/*
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

package edu.cornell.pserc.jpower.tdouble;

import java.io.File;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tint.IntMatrix1D;
import cern.colt.util.tdouble.Djp_mm;
import cern.colt.util.tdouble.Djp_util;
import cern.jet.math.tint.IntFunctions;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_jpc;

/**
 *
 * @author Richard Lincoln
 *
 */
public abstract class Djp_bustypes_test extends Djp_base_test {

	private Djp_jpc jpc;

	public Djp_bustypes_test(String name, String caseName, Djp_jpc jpc) {
		super(name, caseName, "bustypes");
		this.jpc = jpc;
	}

	public void test_bustypes() {
		Djp_jpc jpc;
		IntMatrix1D[] bustypes;
		File ref_file, pv_file, pq_file;
		IntMatrix1D ref, pv, pq;

		jpc = Djp_loadcase.jp_loadcase(this.jpc);
		jpc = Djp_ext2int.jp_ext2int(jpc);
		bustypes = Djp_bustypes.jp_bustypes(jpc.bus, jpc.gen);

		ref_file = new File(fdir, "ref.mtx");
		pv_file = new File(fdir, "pv.mtx");
		pq_file = new File(fdir, "pq.mtx");

		ref = Djp_util.intm((DoubleMatrix1D) Djp_mm.readMatrix(ref_file));
		pv  = Djp_util.intm((DoubleMatrix1D) Djp_mm.readMatrix(pv_file));
		pq  = Djp_util.intm((DoubleMatrix1D) Djp_mm.readMatrix(pq_file));

		ref.assign(IntFunctions.minus(1)); // Correct for Matlab indexing.
		pv .assign(IntFunctions.minus(1));
		pq .assign(IntFunctions.minus(1));

		assertTrue(iprop.equals(bustypes[0], ref));
		assertTrue(iprop.equals(bustypes[1], pv));
		assertTrue(iprop.equals(bustypes[2], pq));
	}

}
