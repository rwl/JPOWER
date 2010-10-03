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

import cern.colt.matrix.tdcomplex.algo.DComplexProperty;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.algo.DoubleProperty;
import cern.colt.matrix.tint.algo.IntProperty;
import cern.colt.util.tdouble.Djp_mm;
import cern.colt.util.tdouble.Djp_util;
import cern.jet.math.tdouble.DoubleFunctions;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_jpc;
import junit.framework.TestCase;

abstract class Djp_base_test extends TestCase {

	protected static Djp_mm mm = new Djp_mm();
	protected static Djp_util util = new Djp_util();
	protected static IntProperty iprop = new IntProperty();
	protected static DoubleProperty dprop;
	protected static DComplexProperty cprop;

	public File data = new File("matrix");
	public String fname;
	public String casename;
	public double precision = 1e-12;
	protected File fdir;

	public Djp_base_test(String name) {
		super(name);
		/* Set 'fname' and 'casename' in subclasses. */
	}

	@SuppressWarnings("static-access")
	protected void setUp() {
		this.dprop = new DoubleProperty(precision);
		this.cprop = new DComplexProperty(precision);

		File casedir = new File(data, casename);
		this.fdir = new File(casedir, fname);
	}

	protected void test_jpc(Djp_jpc jpc) {
		test_jpc(jpc, false, false);
	}

	protected void test_jpc(Djp_jpc jpc, boolean pf) {
		test_jpc(jpc, pf, false);
	}

	protected void test_jpc(Djp_jpc jpc, boolean pf, boolean opf) {
		File mm_version = new File(fdir, "version.mtx");
		File mm_baseMVA = new File(fdir, "baseMVA.mtx");
		File mm_bus = new File(fdir, "bus.mtx");
		File mm_gen = new File(fdir, "gen.mtx");
		File mm_branch = new File(fdir, "branch.mtx");

		DoubleMatrix1D version = (DoubleMatrix1D) Djp_mm.readMatrix(mm_version);
		DoubleMatrix1D baseMVA = (DoubleMatrix1D) Djp_mm.readMatrix(mm_baseMVA);
		DoubleMatrix2D bus = (DoubleMatrix2D) Djp_mm.readMatrix(mm_bus);
		DoubleMatrix2D gen = (DoubleMatrix2D) Djp_mm.readMatrix(mm_gen);
		DoubleMatrix2D branch = (DoubleMatrix2D) Djp_mm.readMatrix(mm_branch);

		/* Matlab indexing starts at 1 */
		bus.viewColumn(0).assign(DoubleFunctions.minus(1));
		gen.viewColumn(0).assign(DoubleFunctions.minus(1));
		branch.viewColumn(0).assign(DoubleFunctions.minus(1));
		branch.viewColumn(1).assign(DoubleFunctions.minus(1));

		assertEquals(Double.valueOf(jpc.version), version.get(0), precision);
		assertEquals(jpc.baseMVA, baseMVA.get(0), precision);
		assertTrue(dprop.equals(jpc.bus.toMatrix(opf), bus));
		assertTrue(dprop.equals(jpc.gen.toMatrix(opf), gen));
		assertTrue(dprop.equals(jpc.branch.toMatrix(pf, opf), branch));
	}

}
