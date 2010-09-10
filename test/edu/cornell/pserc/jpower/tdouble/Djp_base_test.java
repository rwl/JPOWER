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
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.algo.DoubleProperty;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_jpc;
import edu.cornell.pserc.jpower.tdouble.util.Djp_mm;
import junit.framework.TestCase;

abstract class Djp_base_test extends TestCase {

	protected static Djp_mm mm = new Djp_mm();

	public File data = new File("matrix");
	public String fname;
	public String casename;
	public double precision = 1e-12;

	public Djp_base_test(String name) {
		super(name);
		/* Set 'fname' and 'casename' in subclasses. */
	}

	protected void test_jpc(Djp_jpc jpc) {
		test_jpc(jpc, false, false);
	}

	protected void test_jpc(Djp_jpc jpc, boolean pf) {
		test_jpc(jpc, pf, false);
	}

	protected void test_jpc(Djp_jpc jpc, boolean pf, boolean opf) {
		File casedir = new File(data, casename);
		File jpcdir = new File(casedir, fname);

		File mm_version = new File(jpcdir, "version.mtx");
		File mm_baseMVA = new File(jpcdir, "baseMVA.mtx");
		File mm_bus = new File(jpcdir, "bus.mtx");
		File mm_gen = new File(jpcdir, "gen.mtx");
		File mm_branch = new File(jpcdir, "branch.mtx");

		DoubleMatrix1D version = (DoubleMatrix1D) Djp_mm.readMatrix(mm_version);
		DoubleMatrix1D baseMVA = (DoubleMatrix1D) Djp_mm.readMatrix(mm_baseMVA);
		DoubleMatrix2D bus = (DoubleMatrix2D) Djp_mm.readMatrix(mm_bus);
		DoubleMatrix2D gen = (DoubleMatrix2D) Djp_mm.readMatrix(mm_gen);
		DoubleMatrix2D branch = (DoubleMatrix2D) Djp_mm.readMatrix(mm_branch);

		DoubleProperty prop = new DoubleProperty(precision);
		assertEquals(Double.valueOf(jpc.version), version.get(0), precision);
		assertEquals(jpc.baseMVA, baseMVA.get(0), precision);
		assertTrue(prop.equals(jpc.bus.toMatrix(opf), bus));
		assertTrue(prop.equals(jpc.gen.toMatrix(opf), gen));
		assertTrue(prop.equals(jpc.branch.toMatrix(pf, opf), branch));
	}

}
