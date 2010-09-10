package edu.cornell.pserc.jpower.tdouble;

import java.io.File;

import cern.colt.matrix.tdouble.DoubleFactory1D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.algo.DoubleProperty;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_jpc;
import edu.cornell.pserc.jpower.tdouble.util.Djp_mm;
import junit.framework.TestCase;

abstract class Djp_loadcase_test extends TestCase {

	protected static Djp_mm mm = new Djp_mm();

	public String casename;
	public double precision = 1e-12;

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void test_loadcase_string() {
		Djp_jpc jpc = Djp_loadcase.jp_loadcase(casename);

		String parent = casename + "/loadcase";
		File mm_version = new File(parent, "version.mtx");
		File mm_baseMVA = new File(parent, "baseMVA.mtx");
		File mm_bus = new File(parent, "bus.mtx");
		File mm_gen = new File(parent, "gen.mtx");
		File mm_branch = new File(parent, "branch.mtx");

		DoubleMatrix1D version = (DoubleMatrix1D) Djp_mm.readMatrix(mm_version);
		DoubleMatrix1D baseMVA = (DoubleMatrix1D) Djp_mm.readMatrix(mm_baseMVA);
		DoubleMatrix2D bus = (DoubleMatrix2D) Djp_mm.readMatrix(mm_bus);
		DoubleMatrix2D gen = (DoubleMatrix2D) Djp_mm.readMatrix(mm_gen);
		DoubleMatrix2D branch = (DoubleMatrix2D) Djp_mm.readMatrix(mm_branch);

		DoubleProperty prop = new DoubleProperty(precision);
		assertEquals(Double.valueOf(jpc.version), version.get(0), precision);
		assertEquals(jpc.baseMVA, baseMVA.get(0), precision);
		assertTrue(prop.equals(jpc.bus.toMatrix(), bus));
		assertTrue(prop.equals(jpc.gen.toMatrix(), gen));
		assertTrue(prop.equals(jpc.branch.toMatrix(), branch));
	}

}
