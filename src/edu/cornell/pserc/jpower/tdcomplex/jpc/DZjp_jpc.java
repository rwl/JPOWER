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

package edu.cornell.pserc.jpower.tdcomplex.jpc;

import cern.colt.matrix.tdouble.DoubleMatrix2D;
import edu.cornell.pserc.jpower.tdcomplex.DZjp_order;

public class DZjp_jpc {

	/* define bus types */
	public static final int PQ = 1;
	public static final int PV = 2;
	public static final int REF = 3;
	public static final int NONE = 4;

	/* define cost models */
	public static final int PW_LINEAR	= 1;
	public static final int POLYNOMIAL	= 2;

	public double baseMVA = 100;
	public DZjp_bus bus = new DZjp_bus();
	public DZjp_gen gen = new DZjp_gen();
	public DZjp_branch branch = new DZjp_branch();
	public DZjp_areas areas;
	public DZjp_gencost gencost;
	public DoubleMatrix2D A;
	public DoubleMatrix2D N;
	public String userfcn;

	public String version = "2";
	public double et;	// elapsed time in seconds
	public boolean success;

	public DZjp_order order;

}
