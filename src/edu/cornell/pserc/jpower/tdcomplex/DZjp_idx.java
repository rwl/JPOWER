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

import cern.colt.matrix.tdouble.DoubleMatrix1D;

public class DZjp_idx {

    /* define bus types */
    public static final int PQ = 1;
    public static final int PV = 2;
    public static final int REF = 3;
    public static final int NONE = 4;

    /* define bus indices */
    public static final int BUS_I		= 1;	// bus number (1 to 29997)
    public static final int BUS_TYPE	= 2;	// bus type (1 - PQ bus, 2 - PV bus, 3 - reference bus, 4 - isolated bus)
    public static final int PD			= 3;	// Pd, real power demand (MW)
    public static final int QD			= 4;	// Qd, reactive power demand (MVAr)
    public static final int GS			= 5;	// Gs, shunt conductance (MW at V = 1.0 p.u.)
    public static final int BS			= 6;	// Bs, shunt susceptance (MVAr at V = 1.0 p.u.)
    public static final int BUS_AREA	= 7;	// area number, 1-100
    public static final int VM			= 8;	// Vm, voltage magnitude (p.u.)
    public static final int VA			= 9;	// Va, voltage angle (degrees)
    public static final int BASE_KV		= 10;	// baseKV, base voltage (kV)
    public static final int ZONE		= 11;	// zone, loss zone (1-999)
    public static final int VMAX		= 12;	// maxVm, maximum voltage magnitude (p.u.)	  (not in PTI format)
    public static final int VMIN		= 13;	// minVm, minimum voltage magnitude (p.u.)	  (not in PTI format)

    // included in opf solution, not necessarily in input
    // assume objective function has units, u
    public static final int LAM_P		= 14;	// Lagrange multiplier on real power mismatch (u/MW)
    public static final int LAM_Q		= 15;	// Lagrange multiplier on reactive power mismatch (u/MVAr)
    public static final int MU_VMAX		= 16;	// Kuhn-Tucker multiplier on upper voltage limit (u/p.u.)
    public static final int MU_VMIN		= 17;	// Kuhn-Tucker multiplier on lower voltage limit (u/p.u.)


    /* define gen indices */
    public static final int GEN_BUS		= 1;	// bus number
    public static final int PG			= 2;	// Pg, real power output (MW)
    public static final int QG			= 3;	// Qg, reactive power output (MVAr)
    public static final int QMAX		= 4;	// Qmax, maximum reactive power output at Pmin (MVAr)
    public static final int QMIN		= 5;	// Qmin, minimum reactive power output at Pmin (MVAr)
    public static final int VG			= 6;	// Vg, voltage magnitude setpopublic static final int (p.u.)
    public static final int MBASE		= 7;	// mBase, total MVA base of this machine, defaults to baseMVA
    public static final int GEN_STATUS	= 8;	// status, 1 - machine in service, 0 - machine out of service
    public static final int PMAX		= 9;	// Pmax, maximum real power output (MW)
    public static final int PMIN		= 10;	// Pmin, minimum real power output (MW)
    public static final int PC1			= 11;	// Pc1, lower real power output of PQ capability curve (MW)
    public static final int PC2			= 12;	// Pc2, upper real power output of PQ capability curve (MW)
    public static final int QC1MIN		= 13;	// Qc1min, minimum reactive power output at Pc1 (MVAr)
    public static final int QC1MAX		= 14;	// Qc1max, maximum reactive power output at Pc1 (MVAr)
    public static final int QC2MIN		= 15;	// Qc2min, minimum reactive power output at Pc2 (MVAr)
    public static final int QC2MAX		= 16;	// Qc2max, maximum reactive power output at Pc2 (MVAr)
    public static final int RAMP_AGC	= 17;	// ramp rate for load following/AGC (MW/min)
    public static final int RAMP_10		= 18;	// ramp rate for 10 minute reserves (MW)
    public static final int RAMP_30		= 19;	// ramp rate for 30 minute reserves (MW)
    public static final int RAMP_Q		= 20;	// ramp rate for reactive power (2 sec timescale) (MVAr/min)
    public static final int APF			= 21;	// area participation factor

    // included in opf solution, not necessarily in input
    // assume objective function has units, u
    public static final int MU_PMAX		= 22;	// Kuhn-Tucker multiplier on upper Pg limit (u/MW)
    public static final int MU_PMIN		= 23;	// Kuhn-Tucker multiplier on lower Pg limit (u/MW)
    public static final int MU_QMAX		= 24;	// Kuhn-Tucker multiplier on upper Qg limit (u/MVAr)
    public static final int MU_QMIN		= 25;	// Kuhn-Tucker multiplier on lower Qg limit (u/MVAr)


    /* define branch indices */
    public static final int F_BUS		= 1;	// f, from bus number
    public static final int T_BUS		= 2;	// t, to bus number
    public static final int BR_R		= 3;	// r, resistance (p.u.)
    public static final int BR_X		= 4;	// x, reactance (p.u.)
    public static final int BR_B		= 5;	// b, total line charging susceptance (p.u.)
    public static final int RATE_A		= 6;	// rateA, MVA rating A (long term rating)
    public static final int RATE_B		= 7;	// rateB, MVA rating B (short term rating)
    public static final int RATE_C		= 8;	// rateC, MVA rating C (emergency rating)
    public static final int TAP			= 9;	// ratio, transformer off nominal turns ratio
    public static final int SHIFT		= 10;	// angle, transformer phase shift angle (degrees)
    public static final int BR_STATUS	= 11;	// initial branch status, 1 - in service, 0 - out of service
    public static final int ANGMIN		= 12;	// minimum angle difference, angle(Vf) - angle(Vt) (degrees)
    public static final int ANGMAX		= 13;	// maximum angle difference, angle(Vf) - angle(Vt) (degrees)

    // included in power flow solution, not necessarily in input
    public static final int PF			= 14;	// real power injected at "from" bus end (MW)	   (not in PTI format)
    public static final int QF			= 15;	// reactive power injected at "from" bus end (MVAr) (not in PTI format)
    public static final int PT			= 16;	// real power injected at "to" bus end (MW)		 (not in PTI format)
    public static final int QT			= 17;	// reactive power injected at "to" bus end (MVAr)   (not in PTI format)

    // included in opf solution, not necessarily in input
    // assume objective function has units, u
    public static final int MU_SF		= 18;	// Kuhn-Tucker multiplier on MVA limit at "from" bus (u/MVA)
    public static final int MU_ST		= 19;	// Kuhn-Tucker multiplier on MVA limit at "to" bus (u/MVA)
    public static final int MU_ANGMIN	= 20;	// Kuhn-Tucker multiplier lower angle difference limit (u/degree)
    public static final int MU_ANGMAX	= 21;	// Kuhn-Tucker multiplier upper angle difference limit (u/degree)


    /* define area indices */
    public static final int AREA_I		= 1;	// area number
    public static final int PRICE_REF_BUS = 2;	// price reference bus for this area


    /* define cost models */
    public static final int PW_LINEAR	= 1;
    public static final int POLYNOMIAL	= 2;

    // define cost indices
    public static final int MODEL		= 1;	// cost model, 1 = piecewise linear, 2 = polynomial
    public static final int STARTUP		= 2;	// startup cost in US dollars
    public static final int SHUTDOWN	= 3;	// shutdown cost in US dollars
    public static final int NCOST		= 4;	// number breakpoints in piecewise linear cost function,
                                                // or number of coefficients in polynomial cost function
    public static final int COST		= 5;	// parameters defining total cost function begin in this col
                                                // (MODEL = 1) : p0, f0, p1, f1, ..., pn, fn
                                                //	  where p0 < p1 < ... < pn and the cost f(p) is defined
                                                //	  by the coordinates (p0,f0), (p1,f1), ..., (pn,fn) of
                                                //	  the end/break-points of the piecewise linear cost
                                                // (MODEL = 2) : cn, ..., c1, c0
                                                //	  n+1 coefficients of an n-th order polynomial cost fcn,
                                                //	  starting with highest order, where cost is
                                                //	  f(p) = cn*p^2 + ... + c1*p + c0

    /**
     *
     * @param stop
     * @return
     */
    protected static int[] rg(int stop) {
        return rg(0, stop);
    }

    /**
     *
     * @param start
     * @param stop
     * @return
     */
    protected static int[] rg(int start, int stop) {
        return rg(start, stop, 1);
    }

    /**
     *
     * @param start
     * @param stop
     * @param step
     * @return
     */
    protected static int[] rg(int start, int stop, int step) {
        int[] r = new int[stop - start];
        int v = start;
        for (int i = 0; i < r.length; i++) {
            r[i] = v;
            v += step;
        }
        return r;
    }

    /**
     *
     * @param n
     * @return
     */
    protected static int[] zeros(int n) {
        int[] z = new int[n];
        for (int i : z)
            z[i] = 0;
        return z;
    }

    /**
     *
     * @param n array length
     * @return an integer array with all elements = 1.
     */
    protected static int[] ones(int n) {
        int[] one = new int[n];
        for (int i : one)
            one[i] = 1;
        return one;
    }

    /**
     *
     * @param d
     * @return
     */
    protected static int[] idx(DoubleMatrix1D d) {
        int[] ix = new int[(int) d.size()];
        for (int i = 0; i < ix.length; i++)
            ix[i] = (int) d.getQuick(i);
        return ix;
    }

    /**
     *
     * @param t
     * @return
     */
    protected static int max(int[] t) {
        int maximum = t[0];
        for (int i=1; i < t.length; i++)
            if (t[i] > maximum)
                maximum = t[i];
        return maximum;
    }
}
