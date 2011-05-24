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

import cern.colt.matrix.tdcomplex.algo.DComplexProperty;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.algo.DoubleProperty;
import cern.colt.matrix.tint.algo.IntProperty;
import cern.colt.util.tdouble.Djp_mm;
import cern.jet.math.tdouble.DoubleFunctions;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_jpc;
import junit.framework.TestCase;

abstract class Djp_base_test extends TestCase {

//	protected final Djp_mm mm = new Djp_mm();
//	protected final Djp_util util = Djp_util.util;
	protected IntProperty iprop = new IntProperty();
	protected DoubleProperty dprop;
	protected DComplexProperty cprop;

	// tolerance for floating point equality
	private double precision = 1e-12;

	// test data directory
	private final File DATADIR = new File("matrix");
	// name of the sub-directory (e.g. case6ww)
	private final String caseName;
	// function name (e.g. 'ext2int')
	private final String funcName;

	protected File fdir;

	public Djp_base_test(String name, String caseName, String funcName) {
		super(name);
		this.caseName = caseName;
		this.funcName = funcName;
	}

	protected void setUp() {
		this.dprop = new DoubleProperty(precision);
		this.cprop = new DComplexProperty(precision);

		this.fdir = new File(new File(DATADIR, caseName), funcName);
	}

	/**
	 * Test a JPOWER case against MATPOWER case data.
	 *
	 * @param jpc JPOWER case to test
	 * @param pf Does the case have PF results?
	 * @param opf Does the case have OPF results?
	 */
	protected void test_jpc(Djp_jpc jpc, boolean pf, boolean opf) {
		File mm_version, mm_baseMVA, mm_bus, mm_gen, mm_branch;
		DoubleMatrix1D version, baseMVA;
		DoubleMatrix2D bus, gen, branch;

		mm_version = new File(fdir, "version.mtx");
		mm_baseMVA = new File(fdir, "baseMVA.mtx");
		mm_bus = new File(fdir, "bus.mtx");
		mm_gen = new File(fdir, "gen.mtx");
		mm_branch = new File(fdir, "branch.mtx");

		version = (DoubleMatrix1D) Djp_mm.readMatrix(mm_version);
		baseMVA = (DoubleMatrix1D) Djp_mm.readMatrix(mm_baseMVA);
		bus     = (DoubleMatrix2D) Djp_mm.readMatrix(mm_bus);
		gen     = (DoubleMatrix2D) Djp_mm.readMatrix(mm_gen);
		branch  = (DoubleMatrix2D) Djp_mm.readMatrix(mm_branch);

		/* Matlab indexing starts at 1 */
		bus   .viewColumn(0).assign(DoubleFunctions.minus(1));
		gen   .viewColumn(0).assign(DoubleFunctions.minus(1));
		branch.viewColumn(0).assign(DoubleFunctions.minus(1));
		branch.viewColumn(1).assign(DoubleFunctions.minus(1));

		assertEquals(Double.valueOf(jpc.version), version.get(0), precision);
		assertEquals(jpc.baseMVA, baseMVA.get(0), precision);
		assertTrue(dprop.equals(jpc.bus.toMatrix(opf), bus));
		assertTrue(dprop.equals(jpc.gen.toMatrix(opf), gen));
		assertTrue(dprop.equals(jpc.branch.toMatrix(pf, opf), branch));
	}

	protected void test_jpc(Djp_jpc jpc) {
		test_jpc(jpc, false, false);
	}

	protected void test_jpc(Djp_jpc jpc, boolean pf) {
		test_jpc(jpc, pf, false);
	}

	public void setPrecision(double precision) {
		this.precision = precision;
	}

}
