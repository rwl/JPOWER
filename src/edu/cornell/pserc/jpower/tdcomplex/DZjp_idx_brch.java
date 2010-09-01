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

public interface DZjp_idx_brch {

	/* define the indices */
	public static final int F_BUS       = 1;    // f, from bus number
	public static final int T_BUS       = 2;    // t, to bus number
	public static final int BR_R        = 3;    // r, resistance (p.u.)
	public static final int BR_X        = 4;    // x, reactance (p.u.)
	public static final int BR_B        = 5;    // b, total line charging susceptance (p.u.)
	public static final int RATE_A      = 6;    // rateA, MVA rating A (long term rating)
	public static final int RATE_B      = 7;    // rateB, MVA rating B (short term rating)
	public static final int RATE_C      = 8;    // rateC, MVA rating C (emergency rating)
	public static final int TAP         = 9;    // ratio, transformer off nominal turns ratio
	public static final int SHIFT       = 10;   // angle, transformer phase shift angle (degrees)
	public static final int BR_STATUS   = 11;   // initial branch status, 1 - in service, 0 - out of service
	public static final int ANGMIN      = 12;   // minimum angle difference, angle(Vf) - angle(Vt) (degrees)
	public static final int ANGMAX      = 13;   // maximum angle difference, angle(Vf) - angle(Vt) (degrees)

	// included in power flow solution, not necessarily in input
	public static final int PF          = 14;   // real power injected at "from" bus end (MW)       (not in PTI format)
	public static final int QF          = 15;   // reactive power injected at "from" bus end (MVAr) (not in PTI format)
	public static final int PT          = 16;   // real power injected at "to" bus end (MW)         (not in PTI format)
	public static final int QT          = 17;   // reactive power injected at "to" bus end (MVAr)   (not in PTI format)

	// included in opf solution, not necessarily in input
	// assume objective function has units, u
	public static final int MU_SF       = 18;   // Kuhn-Tucker multiplier on MVA limit at "from" bus (u/MVA)
	public static final int MU_ST       = 19;   // Kuhn-Tucker multiplier on MVA limit at "to" bus (u/MVA)
	public static final int MU_ANGMIN   = 20;   // Kuhn-Tucker multiplier lower angle difference limit (u/degree)
	public static final int MU_ANGMAX   = 21;   // Kuhn-Tucker multiplier upper angle difference limit (u/degree)

}
