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

import cern.colt.matrix.tdcomplex.DComplexMatrix1D;
import cern.colt.util.tdouble.MMUtil;
import edu.cornell.pserc.jpower.Djp_ext2int;
import edu.cornell.pserc.jpower.Djp_loadcase;
import edu.cornell.pserc.jpower.jpc.JPC;
import edu.cornell.pserc.jpower.pf.Djp_makeSbus;

/**
 *
 * @author Richard Lincoln
 *
 */
public abstract class Djp_makeSbus_test extends Djp_base_test {

	private JPC jpc;

	public Djp_makeSbus_test(String name, String caseName, JPC jpc) {
		super(name, caseName, "makeSbus");
		this.jpc = jpc;
	}

	public void test_makeBdc() {
		JPC jpc;
		DComplexMatrix1D Sbus, mpSbus;
		File Sbus_file;

		jpc = Djp_loadcase.loadcase(this.jpc);
		jpc = Djp_ext2int.ext2int(jpc);
		Sbus = Djp_makeSbus.makeSbus(jpc.baseMVA, jpc.bus, jpc.gen);

		Sbus_file = new File(fdir, "Sbus.mtx");;

		mpSbus = (DComplexMatrix1D) MMUtil.readMatrix(Sbus_file);

		assertTrue(cprop.equals(Sbus, mpSbus));
	}

}
