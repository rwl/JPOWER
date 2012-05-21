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

import cern.colt.matrix.tint.IntMatrix1D;

public class Order {

//	public class Case {
//		public Djp_areas areas;
//		public Djp_bus bus;
//		public Djp_branch branch;
//		public Djp_gen gen;
//		public Djp_gencost gencost;
//		public DoubleMatrix2D A;
//		public DoubleMatrix2D N;
//	}

	public class Status {
		public int[] on = new int[0];
		public int[] off = new int[0];

		public Status copy() {
			Status cpy = new Status();

			int[] on = new int[this.on.length];
			int[] off = new int[this.off.length];
			System.arraycopy(this.on, 0, on, 0, this.on.length);
			System.arraycopy(this.off, 0, off, 0, this.off.length);
			cpy.on = on;
			cpy.off = off;

			return cpy;
		}
	}

	public class BusGen {
		public IntMatrix1D e2i;
		public IntMatrix1D i2e;
		public Status status = new Status();

		public BusGen copy() {
			BusGen cpy = new BusGen();

			cpy.e2i = this.e2i.copy();
			cpy.i2e = this.i2e.copy();
			cpy.status = this.status.copy();

			return cpy;
		}
	}

	public class BranchAreas {
		public Status status = new Status();

		public BranchAreas copy() {
			BranchAreas cpy = new BranchAreas();

			cpy.status = this.status.copy();

			return cpy;
		}
	}

	public String state;		// 'i' or 'e'
	public JPC internal = new JPC();
	public JPC external = new JPC();
	public BusGen bus = new BusGen();
	public BusGen gen = new BusGen();
	public BranchAreas branch = new BranchAreas();
	public BranchAreas areas = new BranchAreas();

	public Order copy() {
		Order cpy = new Order();

		cpy.state = this.state;
		if (this.internal != null)
			cpy.internal = this.internal.copy();
		if (this.external != null)
			cpy.external = this.external.copy();
		cpy.bus = this.bus.copy();
		cpy.gen = this.gen.copy();
		cpy.branch = this.branch.copy();
		cpy.areas = this.areas.copy();

		return cpy;
	}

}
