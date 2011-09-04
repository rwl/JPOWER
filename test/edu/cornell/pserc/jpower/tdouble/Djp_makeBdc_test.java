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

import cern.colt.matrix.AbstractMatrix;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.util.tdouble.MMUtil;
import edu.cornell.pserc.jpower.Djp_ext2int;
import edu.cornell.pserc.jpower.Djp_loadcase;
import edu.cornell.pserc.jpower.jpc.JPC;
import edu.cornell.pserc.jpower.pf.Djp_makeBdc;

/**
 *
 * @author Richard Lincoln
 *
 */
public abstract class Djp_makeBdc_test extends Djp_base_test {

	private JPC jpc;

	public Djp_makeBdc_test(String name, String caseName, JPC jpc) {
		super(name, caseName, "makeBdc");
		this.jpc = jpc;
	}

	public void test_makeBdc() {
		JPC jpc;
		AbstractMatrix[] Bdc;
		File Bbus_file, Bf_file, Pbusinj_file, Pfinj_file;
		DoubleMatrix2D Bbus, Bf;
		DoubleMatrix1D Pbusinj, Pfinj;

		jpc = Djp_loadcase.loadcase(this.jpc);
		jpc = Djp_ext2int.ext2int(jpc);
		Bdc = Djp_makeBdc.makeBdc(jpc.baseMVA, jpc.bus, jpc.branch);

		Bbus_file    = new File(fdir, "Bbus.mtx");
		Bf_file      = new File(fdir, "Bf.mtx");
		Pbusinj_file = new File(fdir, "Pbusinj.mtx");
		Pfinj_file   = new File(fdir, "Pfinj.mtx");

		Bbus    = (DoubleMatrix2D) MMUtil.readMatrix(Bbus_file);
		Bf      = (DoubleMatrix2D) MMUtil.readMatrix(Bf_file);
		Pbusinj = (DoubleMatrix1D) MMUtil.readMatrix(Pbusinj_file);
		Pfinj   = (DoubleMatrix1D) MMUtil.readMatrix(Pfinj_file);

		assertTrue(dprop.equals((DoubleMatrix2D) Bdc[0], Bbus));
		assertTrue(dprop.equals((DoubleMatrix2D) Bdc[1], Bf));
		assertTrue(dprop.equals((DoubleMatrix1D) Bdc[2], Pbusinj));
		assertTrue(dprop.equals((DoubleMatrix1D) Bdc[3], Pfinj));
	}

}
