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
import cern.colt.util.tdouble.MMUtil;
import cern.colt.util.tdouble.Util;
import cern.jet.math.tint.IntFunctions;
import edu.cornell.pserc.jpower.tdouble.jpc.JPC;

/**
 *
 * @author Richard Lincoln
 *
 */
public abstract class Djp_bustypes_test extends Djp_base_test {

	private JPC jpc;

	public Djp_bustypes_test(String name, String caseName, JPC jpc) {
		super(name, caseName, "bustypes");
		this.jpc = jpc;
	}

	public void test_bustypes() {
		JPC jpc;
		IntMatrix1D[] bustypes;
		File ref_file, pv_file, pq_file;
		IntMatrix1D ref, pv, pq;

		jpc = Djp_loadcase.loadcase(this.jpc);
		jpc = Djp_ext2int.ext2int(jpc);
		bustypes = Djp_bustypes.bustypes(jpc.bus, jpc.gen);

		ref_file = new File(fdir, "ref.mtx");
		pv_file = new File(fdir, "pv.mtx");
		pq_file = new File(fdir, "pq.mtx");

		ref = Util.intm((DoubleMatrix1D) MMUtil.readMatrix(ref_file));
		pv  = Util.intm((DoubleMatrix1D) MMUtil.readMatrix(pv_file));
		pq  = Util.intm((DoubleMatrix1D) MMUtil.readMatrix(pq_file));

		ref.assign(IntFunctions.minus(1)); // Correct for Matlab indexing.
		pv .assign(IntFunctions.minus(1));
		pq .assign(IntFunctions.minus(1));

		assertTrue(iprop.equals(bustypes[0], ref));
		assertTrue(iprop.equals(bustypes[1], pv));
		assertTrue(iprop.equals(bustypes[2], pq));
	}

}
