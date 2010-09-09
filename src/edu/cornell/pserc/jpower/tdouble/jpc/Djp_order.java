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

package edu.cornell.pserc.jpower.tdouble.jpc;

import cern.colt.matrix.tdouble.DoubleMatrix2D;
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
	}

	public class BusGen {
		public IntMatrix1D e2i;
		public IntMatrix1D i2e;
		public Status status = new Status();
	}

	public class BranchAreas {
		public Status status = new Status();
	}

	public String state;		// 'i' or 'e'
	public Djp_jpc internal = new Djp_jpc();
	public Djp_jpc external = new Djp_jpc();
	public BusGen bus = new BusGen();
	public BusGen gen = new BusGen();
	public BranchAreas branch = new BranchAreas();
	public BranchAreas areas = new BranchAreas();

}
