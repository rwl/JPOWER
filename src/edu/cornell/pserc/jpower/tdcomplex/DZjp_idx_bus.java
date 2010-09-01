/*
 * Copyright (C) 1996-2010 by Power System Engineering Research Center (PSERC)
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

public interface DZjp_idx_bus {

	/* define bus types */
	public static final int PQ = 1;
	public static final int PV = 2;
	public static final int REF = 3;
	public static final int NONE = 4;

	/* define the indices */
	public static final int BUS_I       = 1;    // bus number (1 to 29997)
	public static final int BUS_TYPE    = 2;    // bus type (1 - PQ bus, 2 - PV bus, 3 - reference bus, 4 - isolated bus)
	public static final int PD          = 3;    // Pd, real power demand (MW)
	public static final int QD          = 4;    // Qd, reactive power demand (MVAr)
	public static final int GS          = 5;    // Gs, shunt conductance (MW at V = 1.0 p.u.)
	public static final int BS          = 6;    // Bs, shunt susceptance (MVAr at V = 1.0 p.u.)
	public static final int BUS_AREA    = 7;    // area number, 1-100
	public static final int VM          = 8;    // Vm, voltage magnitude (p.u.)
	public static final int VA          = 9;    // Va, voltage angle (degrees)
	public static final int BASE_KV     = 10;   // baseKV, base voltage (kV)
	public static final int ZONE        = 11;   // zone, loss zone (1-999)
	public static final int VMAX        = 12;   // maxVm, maximum voltage magnitude (p.u.)      (not in PTI format)
	public static final int VMIN        = 13;   // minVm, minimum voltage magnitude (p.u.)      (not in PTI format)

	// included in opf solution, not necessarily in input
	// assume objective function has units, u
	public static final int LAM_P       = 14;   // Lagrange multiplier on real power mismatch (u/MW)
	public static final int LAM_Q       = 15;   // Lagrange multiplier on reactive power mismatch (u/MVAr)
	public static final int MU_VMAX     = 16;   // Kuhn-Tucker multiplier on upper voltage limit (u/p.u.)
	public static final int MU_VMIN     = 17;   // Kuhn-Tucker multiplier on lower voltage limit (u/p.u.)
}
