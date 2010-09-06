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

package edu.cornell.pserc.jpower.tdcomplex;

import cern.colt.list.tint.IntArrayList;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tint.IntMatrix1D;
import edu.cornell.pserc.jpower.tdcomplex.jpc.DZjp_areas;
import edu.cornell.pserc.jpower.tdcomplex.jpc.DZjp_branch;
import edu.cornell.pserc.jpower.tdcomplex.jpc.DZjp_bus;
import edu.cornell.pserc.jpower.tdcomplex.jpc.DZjp_gen;
import edu.cornell.pserc.jpower.tdcomplex.jpc.DZjp_gencost;

public class DZjp_order {

	class Case {
		DZjp_areas areas;
		DZjp_bus bus;
		DZjp_branch branch;
		DZjp_gen gen;
		DZjp_gencost gencost;
		DoubleMatrix2D A;
		DoubleMatrix2D N;
	}

	class Status {
		public IntArrayList on = new IntArrayList();
		public IntArrayList off = new IntArrayList();
	}

	class BusGen {
		IntMatrix1D e2i;
		IntMatrix1D i2e;
		Status status = new Status();
	}

	class BranchAreas {
		Status status = new Status();
	}

	public String state;		// 'i' or 'e'
	public Case internal = new Case();
	public Case external = new Case();
	public BusGen bus = new BusGen();
	public BusGen gen = new BusGen();
	public BranchAreas branch = new BranchAreas();
	public BranchAreas areas = new BranchAreas();

}
