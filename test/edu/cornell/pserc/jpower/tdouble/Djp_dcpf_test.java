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
import cern.colt.matrix.tdcomplex.DComplexMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tint.IntMatrix1D;
import cern.colt.util.tdouble.MMUtil;
import cern.jet.math.tdouble.DoubleFunctions;
import edu.cornell.pserc.jpower.tdouble.jpc.JPC;
import edu.cornell.pserc.jpower.tdouble.pf.Djp_dcpf;
import edu.cornell.pserc.jpower.tdouble.pf.Djp_makeBdc;
import edu.cornell.pserc.jpower.tdouble.pf.Djp_makeSbus;

/**
 *
 * @author Richard Lincoln
 *
 */
public abstract class Djp_dcpf_test extends Djp_base_test {

	private JPC jpc;

	public Djp_dcpf_test(String name, String caseName, JPC jpc) {
		super(name, caseName, "dcpf");
		this.jpc = jpc;
	}

	public void test_dcpf() {
		JPC jpc;
		IntMatrix1D[] bustypes;
		int ref;
		int[] pv, pq;
		AbstractMatrix[] Bdc;
		DoubleMatrix2D Bbus;
		DComplexMatrix1D Sbus;
		DoubleMatrix1D Pbus, Va0, Va, mpVa;

		DoubleFunctions dfunc = DoubleFunctions.functions;

		jpc = Djp_loadcase.loadcase(this.jpc);
		jpc = Djp_ext2int.ext2int(jpc);

		bustypes = Djp_bustypes.bustypes(jpc.bus, jpc.gen);
		ref = bustypes[0].get(0);
		pv  = bustypes[1].toArray();
		pq  = bustypes[2].toArray();

		Bdc = Djp_makeBdc.makeBdc(jpc.baseMVA, jpc.bus, jpc.branch);
		Bbus = (DoubleMatrix2D) Bdc[0];
		Sbus = Djp_makeSbus.makeSbus(jpc.baseMVA, jpc.bus, jpc.gen);
		Pbus = Sbus.getRealPart();

		Va0 = jpc.bus.Va.copy();
		Va0.assign(dfunc.chain(dfunc.mult(Math.PI), dfunc.div(180)));

		Va = Djp_dcpf.dcpf(Bbus, Pbus, Va0, ref, pv, pq);

		mpVa = (DoubleMatrix1D) MMUtil.readMatrix(new File(fdir, "Va.mtx"));

		assertTrue(dprop.equals(Va, mpVa));
	}

}
