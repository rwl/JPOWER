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
import cern.colt.matrix.tdcomplex.DComplexMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tint.IntMatrix1D;
import cern.colt.util.tdouble.Djp_mm;
import cern.jet.math.tdouble.DoubleFunctions;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_jpc;
import edu.cornell.pserc.jpower.tdouble.pf.Djp_dcpf;
import edu.cornell.pserc.jpower.tdouble.pf.Djp_makeBdc;
import edu.cornell.pserc.jpower.tdouble.pf.Djp_makeSbus;

/**
 *
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public abstract class Djp_dcpf_test extends Djp_base_test {

	protected Djp_jpc jpc;

	public Djp_dcpf_test(String name) {
		super(name);
		this.fname = "dcpf";
		/* Set 'jpc' in subclasses. */
	}

	@SuppressWarnings("static-access")
	public void test_makeBdc() {
		DoubleFunctions dfunc = DoubleFunctions.functions;

		Djp_jpc jpc = Djp_loadcase.jp_loadcase(this.jpc);
		jpc = Djp_ext2int.jp_ext2int(jpc);
		IntMatrix1D[] bustypes = Djp_bustypes.jp_bustypes(jpc.bus, jpc.gen);
		int ref = bustypes[0].get(0);
		int[] pv = bustypes[1].toArray();
		int[] pq = bustypes[2].toArray();
		AbstractMatrix[] Bdc = Djp_makeBdc.jp_makeBdc(jpc.baseMVA, jpc.bus, jpc.branch);
		DoubleMatrix2D Bbus = (DoubleMatrix2D) Bdc[0];
		DComplexMatrix1D Sbus = Djp_makeSbus.jp_makeSbus(jpc.baseMVA, jpc.bus, jpc.gen);
		DoubleMatrix1D Pbus = Sbus.getRealPart();
		DoubleMatrix1D Va0 = jpc.bus.Va.copy();
		Va0.assign(dfunc.chain(dfunc.mult(Math.PI), dfunc.div(180)));

		DoubleMatrix1D Va = Djp_dcpf.jp_dcpf(Bbus, Pbus, Va0, ref, pv, pq);

		File Va_file = new File(fdir, "Va.mtx");;

		DoubleMatrix1D mpVa = (DoubleMatrix1D) Djp_mm.readMatrix(Va_file);

		assertTrue(dprop.equals(Va, mpVa));
	}

}
