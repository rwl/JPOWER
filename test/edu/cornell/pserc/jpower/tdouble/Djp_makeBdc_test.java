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

import cern.colt.matrix.AbstractMatrix;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_jpc;
import edu.cornell.pserc.util.tdouble.Djp_mm;

/**
 *
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public abstract class Djp_makeBdc_test extends Djp_base_test {

	protected Djp_jpc jpc;

	public Djp_makeBdc_test(String name) {
		super(name);
		this.fname = "makeBdc";
		/* Set 'jpc' in subclasses. */
	}

	public void test_makeBdc() {
		Djp_jpc jpc = Djp_loadcase.jp_loadcase(this.jpc);
		jpc = Djp_ext2int.jp_ext2int(jpc);
		AbstractMatrix[] Bdc = Djp_makeBdc.jp_makeBdc(jpc.baseMVA, jpc.bus, jpc.branch);

		File Bbus_file = new File(fdir, "Bbus.mtx");
		File Bf_file = new File(fdir, "Bf.mtx");
		File Pbusinj_file = new File(fdir, "Pbusinj.mtx");
		File Pfinj_file = new File(fdir, "Pfinj.mtx");

		DoubleMatrix2D Bbus = (DoubleMatrix2D) Djp_mm.readMatrix(Bbus_file);
		DoubleMatrix2D Bf = (DoubleMatrix2D) Djp_mm.readMatrix(Bf_file);
		DoubleMatrix1D Pbusinj = (DoubleMatrix1D) Djp_mm.readMatrix(Pbusinj_file);
		DoubleMatrix1D Pfinj = (DoubleMatrix1D) Djp_mm.readMatrix(Pfinj_file);

		assertTrue(dprop.equals((DoubleMatrix2D) Bdc[0], Bbus));
		assertTrue(dprop.equals((DoubleMatrix2D) Bdc[1], Bf));
		assertTrue(dprop.equals((DoubleMatrix1D) Bdc[2], Pbusinj));
		assertTrue(dprop.equals((DoubleMatrix1D) Bdc[3], Pfinj));
	}

}
