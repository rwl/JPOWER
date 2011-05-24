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

package edu.cornell.pserc.jpower.tdouble.jpc;

import java.util.Map;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import edu.cornell.pserc.jpower.tdouble.opf.Djp_opf_model;

public class Djp_jpc {

	/* define bus types */
	public static final int PQ = 1;
	public static final int PV = 2;
	public static final int REF = 3;
	public static final int NONE = 4;

	/* define cost models */
	public static final int PW_LINEAR	= 1;
	public static final int POLYNOMIAL	= 2;

	public double baseMVA = 100;
	public Djp_bus bus = new Djp_bus();
	public Djp_gen gen = new Djp_gen();
	public Djp_branch branch = new Djp_branch();
	public Djp_areas areas;
	public Djp_gencost gencost;
	public DoubleMatrix2D A;
	public DoubleMatrix2D N;
	public String userfcn;

	public String version = "2";
	public double et;	// elapsed time in seconds
	public boolean success;

	public Djp_opf_model om;
	public DoubleMatrix1D x;
	public Map<String, Map<String, DoubleMatrix1D>> mu;
	public double f;

	public DoubleMatrix1D g, df;
	public DoubleMatrix2D dg, d2f;

	public Djp_order order;

	public Djp_jpc copy() {
		Djp_jpc cpy = new Djp_jpc();

		cpy.baseMVA = this.baseMVA;
		cpy.bus = this.bus.copy();
		cpy.gen = this.gen.copy();
		cpy.branch = this.branch.copy();
		cpy.areas = this.areas.copy();
		cpy.gencost = this.gencost.copy();
		cpy.A = this.A.copy();
		cpy.N = this.N.copy();
		cpy.userfcn = this.userfcn;

		cpy.version = this.version;
		cpy.et = this.et;
		cpy.success = this.success;

		cpy.f = this.f;

		cpy.order = this.order.copy();

		return cpy;
	}

}
