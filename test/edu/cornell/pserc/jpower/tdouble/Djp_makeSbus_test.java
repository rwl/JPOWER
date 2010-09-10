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

import cern.colt.matrix.tdcomplex.DComplexMatrix1D;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_jpc;
import edu.cornell.pserc.jpower.tdouble.util.Djp_mm;

/**
 *
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public abstract class Djp_makeSbus_test extends Djp_base_test {

	protected Djp_jpc jpc;

	public Djp_makeSbus_test(String name) {
		super(name);
		this.fname = "makeSbus";
		/* Set 'jpc' in subclasses. */
	}

	public void test_makeBdc() {
		Djp_jpc jpc = Djp_loadcase.jp_loadcase(this.jpc);
		jpc = Djp_ext2int.jp_ext2int(jpc);
		DComplexMatrix1D Sbus = Djp_makeSbus.jp_makeSbus(jpc.baseMVA, jpc.bus, jpc.gen);

		File Sbus_file = new File(fdir, "Sbus.mtx");;

		DComplexMatrix1D mpSbus = (DComplexMatrix1D) Djp_mm.readMatrix(Sbus_file);

		assertTrue(cprop.equals(Sbus, mpSbus));
	}

}
