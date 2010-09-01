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

public interface DZjp_idx_gen {

	/* define the indices */
	public static final int GEN_BUS     = 1;    // bus number
	public static final int PG          = 2;    // Pg, real power output (MW)
	public static final int QG          = 3;    // Qg, reactive power output (MVAr)
	public static final int QMAX        = 4;    // Qmax, maximum reactive power output at Pmin (MVAr)
	public static final int QMIN        = 5;    // Qmin, minimum reactive power output at Pmin (MVAr)
	public static final int VG          = 6;    // Vg, voltage magnitude setpopublic static final int (p.u.)
	public static final int MBASE       = 7;    // mBase, total MVA base of this machine, defaults to baseMVA
	public static final int GEN_STATUS  = 8;    // status, 1 - machine in service, 0 - machine out of service
	public static final int PMAX        = 9;    // Pmax, maximum real power output (MW)
	public static final int PMIN        = 10;   // Pmin, minimum real power output (MW)
	public static final int PC1         = 11;   // Pc1, lower real power output of PQ capability curve (MW)
	public static final int PC2         = 12;   // Pc2, upper real power output of PQ capability curve (MW)
	public static final int QC1MIN      = 13;   // Qc1min, minimum reactive power output at Pc1 (MVAr)
	public static final int QC1MAX      = 14;   // Qc1max, maximum reactive power output at Pc1 (MVAr)
	public static final int QC2MIN      = 15;   // Qc2min, minimum reactive power output at Pc2 (MVAr)
	public static final int QC2MAX      = 16;   // Qc2max, maximum reactive power output at Pc2 (MVAr)
	public static final int RAMP_AGC    = 17;   // ramp rate for load following/AGC (MW/min)
	public static final int RAMP_10     = 18;   // ramp rate for 10 minute reserves (MW)
	public static final int RAMP_30     = 19;   // ramp rate for 30 minute reserves (MW)
	public static final int RAMP_Q      = 20;   // ramp rate for reactive power (2 sec timescale) (MVAr/min)
	public static final int APF         = 21;   // area participation factor

	// included in opf solution, not necessarily in input
	// assume objective function has units, u
	public static final int MU_PMAX     = 22;   // Kuhn-Tucker multiplier on upper Pg limit (u/MW)
	public static final int MU_PMIN     = 23;   // Kuhn-Tucker multiplier on lower Pg limit (u/MW)
	public static final int MU_QMAX     = 24;   // Kuhn-Tucker multiplier on upper Qg limit (u/MVAr)
	public static final int MU_QMIN     = 25;   // Kuhn-Tucker multiplier on lower Qg limit (u/MVAr)

}
