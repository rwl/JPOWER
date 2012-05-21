/*
 * Copyright (C) 2010-2011 Richard Lincoln
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package edu.cornell.pserc.jpower.jpc;

import java.util.Map;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;

public class JPC {

	/* define bus types */
	public static final int PQ = 1;
	public static final int PV = 2;
	public static final int REF = 3;
	public static final int NONE = 4;

	/* define cost models */
	public static final int PW_LINEAR	= 1;
	public static final int POLYNOMIAL	= 2;

	public double baseMVA = 100;
	public Bus bus = new Bus();
	public Gen gen = new Gen();
	public Branch branch = new Branch();
	public Areas areas;
	public GenCost gencost;
	public DoubleMatrix2D A;
	public DoubleMatrix2D N;
	public String userfcn;

	public String version = "2";
	public double et;	// elapsed time in seconds
	public boolean success;

	public DoubleMatrix1D x;
	public Map<String, Map<String, DoubleMatrix1D>> mu;
	public double f;

	public DoubleMatrix1D g, df;
	public DoubleMatrix2D dg, d2f;

	public Order order;

	public JPC copy() {
		JPC cpy = new JPC();

		cpy.baseMVA = this.baseMVA;
		cpy.bus = this.bus.copy();
		cpy.gen = this.gen.copy();
		cpy.branch = this.branch.copy();
		if (this.areas != null)
			cpy.areas = this.areas.copy();
		if (this.gencost != null)
			cpy.gencost = this.gencost.copy();
		if (this.A != null)
			cpy.A = this.A.copy();
		if (this.N != null)
			cpy.N = this.N.copy();
		if (this.userfcn != null)
			cpy.userfcn = this.userfcn;

		cpy.version = this.version;
		cpy.et = this.et;
		cpy.success = this.success;

		cpy.f = this.f;

		if (this.order != null)
			cpy.order = this.order.copy();

		return cpy;
	}

}
