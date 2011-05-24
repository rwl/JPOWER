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

import cern.colt.matrix.tint.IntMatrix1D;

public class Djp_order {

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
	public Djp_jpc internal = new Djp_jpc();
	public Djp_jpc external = new Djp_jpc();
	public BusGen bus = new BusGen();
	public BusGen gen = new BusGen();
	public BranchAreas branch = new BranchAreas();
	public BranchAreas areas = new BranchAreas();

	public Djp_order copy() {
		Djp_order cpy = new Djp_order();

		cpy.state = this.state;
		cpy.internal = this.internal.copy();
		cpy.external = this.external.copy();

		return cpy;
	}

}
