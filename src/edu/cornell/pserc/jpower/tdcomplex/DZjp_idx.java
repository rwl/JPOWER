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

import java.util.concurrent.Future;

import cern.colt.matrix.tdouble.DoubleFactory1D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tint.IntFactory1D;
import cern.colt.matrix.tint.IntMatrix1D;
import cern.jet.math.tdcomplex.DComplexFunctions;
import cern.jet.math.tdouble.DoubleFunctions;
import cern.jet.math.tint.IntFunctions;
import edu.emory.mathcs.utils.ConcurrencyUtils;

public class DZjp_idx {

    /* define bus types */
    protected static final int PQ = 1;
    protected static final int PV = 2;
    protected static final int REF = 3;
    protected static final int NONE = 4;

    /* define bus indices */
    protected static final int BUS_I		= 1;	// bus number (1 to 29997)
    protected static final int BUS_TYPE	= 2;	// bus type (1 - PQ bus, 2 - PV bus, 3 - reference bus, 4 - isolated bus)
    protected static final int PD			= 3;	// Pd, real power demand (MW)
    protected static final int QD			= 4;	// Qd, reactive power demand (MVAr)
    protected static final int GS			= 5;	// Gs, shunt conductance (MW at V = 1.0 p.u.)
    protected static final int BS			= 6;	// Bs, shunt susceptance (MVAr at V = 1.0 p.u.)
    protected static final int BUS_AREA	= 7;	// area number, 1-100
    protected static final int VM			= 8;	// Vm, voltage magnitude (p.u.)
    protected static final int VA			= 9;	// Va, voltage angle (degrees)
    protected static final int BASE_KV		= 10;	// baseKV, base voltage (kV)
    protected static final int ZONE		= 11;	// zone, loss zone (1-999)
    protected static final int VMAX		= 12;	// maxVm, maximum voltage magnitude (p.u.)	  (not in PTI format)
    protected static final int VMIN		= 13;	// minVm, minimum voltage magnitude (p.u.)	  (not in PTI format)

    // included in opf solution, not necessarily in input
    // assume objective function has units, u
    protected static final int LAM_P		= 14;	// Lagrange multiplier on real power mismatch (u/MW)
    protected static final int LAM_Q		= 15;	// Lagrange multiplier on reactive power mismatch (u/MVAr)
    protected static final int MU_VMAX		= 16;	// Kuhn-Tucker multiplier on upper voltage limit (u/p.u.)
    protected static final int MU_VMIN		= 17;	// Kuhn-Tucker multiplier on lower voltage limit (u/p.u.)


    /* define gen indices */
    protected static final int GEN_BUS		= 1;	// bus number
    protected static final int PG			= 2;	// Pg, real power output (MW)
    protected static final int QG			= 3;	// Qg, reactive power output (MVAr)
    protected static final int QMAX		= 4;	// Qmax, maximum reactive power output at Pmin (MVAr)
    protected static final int QMIN		= 5;	// Qmin, minimum reactive power output at Pmin (MVAr)
    protected static final int VG			= 6;	// Vg, voltage magnitude setpoprotected static final int (p.u.)
    protected static final int MBASE		= 7;	// mBase, total MVA base of this machine, defaults to baseMVA
    protected static final int GEN_STATUS	= 8;	// status, 1 - machine in service, 0 - machine out of service
    protected static final int PMAX		= 9;	// Pmax, maximum real power output (MW)
    protected static final int PMIN		= 10;	// Pmin, minimum real power output (MW)
    protected static final int PC1			= 11;	// Pc1, lower real power output of PQ capability curve (MW)
    protected static final int PC2			= 12;	// Pc2, upper real power output of PQ capability curve (MW)
    protected static final int QC1MIN		= 13;	// Qc1min, minimum reactive power output at Pc1 (MVAr)
    protected static final int QC1MAX		= 14;	// Qc1max, maximum reactive power output at Pc1 (MVAr)
    protected static final int QC2MIN		= 15;	// Qc2min, minimum reactive power output at Pc2 (MVAr)
    protected static final int QC2MAX		= 16;	// Qc2max, maximum reactive power output at Pc2 (MVAr)
    protected static final int RAMP_AGC	= 17;	// ramp rate for load following/AGC (MW/min)
    protected static final int RAMP_10		= 18;	// ramp rate for 10 minute reserves (MW)
    protected static final int RAMP_30		= 19;	// ramp rate for 30 minute reserves (MW)
    protected static final int RAMP_Q		= 20;	// ramp rate for reactive power (2 sec timescale) (MVAr/min)
    protected static final int APF			= 21;	// area participation factor

    // included in opf solution, not necessarily in input
    // assume objective function has units, u
    protected static final int MU_PMAX		= 22;	// Kuhn-Tucker multiplier on upper Pg limit (u/MW)
    protected static final int MU_PMIN		= 23;	// Kuhn-Tucker multiplier on lower Pg limit (u/MW)
    protected static final int MU_QMAX		= 24;	// Kuhn-Tucker multiplier on upper Qg limit (u/MVAr)
    protected static final int MU_QMIN		= 25;	// Kuhn-Tucker multiplier on lower Qg limit (u/MVAr)


    /* define branch indices */
    protected static final int F_BUS		= 1;	// f, from bus number
    protected static final int T_BUS		= 2;	// t, to bus number
    protected static final int BR_R		= 3;	// r, resistance (p.u.)
    protected static final int BR_X		= 4;	// x, reactance (p.u.)
    protected static final int BR_B		= 5;	// b, total line charging susceptance (p.u.)
    protected static final int RATE_A		= 6;	// rateA, MVA rating A (long term rating)
    protected static final int RATE_B		= 7;	// rateB, MVA rating B (short term rating)
    protected static final int RATE_C		= 8;	// rateC, MVA rating C (emergency rating)
    protected static final int TAP			= 9;	// ratio, transformer off nominal turns ratio
    protected static final int SHIFT		= 10;	// angle, transformer phase shift angle (degrees)
    protected static final int BR_STATUS	= 11;	// initial branch status, 1 - in service, 0 - out of service
    protected static final int ANGMIN		= 12;	// minimum angle difference, angle(Vf) - angle(Vt) (degrees)
    protected static final int ANGMAX		= 13;	// maximum angle difference, angle(Vf) - angle(Vt) (degrees)

    // included in power flow solution, not necessarily in input
    protected static final int PF			= 14;	// real power injected at "from" bus end (MW)	   (not in PTI format)
    protected static final int QF			= 15;	// reactive power injected at "from" bus end (MVAr) (not in PTI format)
    protected static final int PT			= 16;	// real power injected at "to" bus end (MW)		 (not in PTI format)
    protected static final int QT			= 17;	// reactive power injected at "to" bus end (MVAr)   (not in PTI format)

    // included in opf solution, not necessarily in input
    // assume objective function has units, u
    protected static final int MU_SF		= 18;	// Kuhn-Tucker multiplier on MVA limit at "from" bus (u/MVA)
    protected static final int MU_ST		= 19;	// Kuhn-Tucker multiplier on MVA limit at "to" bus (u/MVA)
    protected static final int MU_ANGMIN	= 20;	// Kuhn-Tucker multiplier lower angle difference limit (u/degree)
    protected static final int MU_ANGMAX	= 21;	// Kuhn-Tucker multiplier upper angle difference limit (u/degree)


    /* define area indices */
    protected static final int AREA_I		= 1;	// area number
    protected static final int PRICE_REF_BUS = 2;	// price reference bus for this area


    /* define cost models */
    protected static final int PW_LINEAR	= 1;
    protected static final int POLYNOMIAL	= 2;

    // define cost indices
    protected static final int MODEL		= 1;	// cost model, 1 = piecewise linear, 2 = polynomial
    protected static final int STARTUP		= 2;	// startup cost in US dollars
    protected static final int SHUTDOWN	= 3;	// shutdown cost in US dollars
    protected static final int NCOST		= 4;	// number breakpoints in piecewise linear cost function,
                                                // or number of coefficients in polynomial cost function
    protected static final int COST		= 5;	// parameters defining total cost function begin in this col
                                                // (MODEL = 1) : p0, f0, p1, f1, ..., pn, fn
                                                //	  where p0 < p1 < ... < pn and the cost f(p) is defined
                                                //	  by the coordinates (p0,f0), (p1,f1), ..., (pn,fn) of
                                                //	  the end/break-points of the piecewise linear cost
                                                // (MODEL = 2) : cn, ..., c1, c0
                                                //	  n+1 coefficients of an n-th order polynomial cost fcn,
                                                //	  starting with highest order, where cost is
                                                //	  f(p) = cn*p^2 + ... + c1*p + c0

    /* aliases */
    protected static final DoubleFunctions dfunc = DoubleFunctions.functions;
    protected static final IntFunctions ifunc = IntFunctions.intFunctions;
    protected static final DComplexFunctions cfunc = DComplexFunctions.functions;

    /**
     *
     * @param stop
     * @return
     */
    protected static int[] irange(int stop) {
        return irange(0, stop);
    }

    /**
     *
     * @param start
     * @param stop
     * @return
     */
    protected static int[] irange(int start, int stop) {
        return irange(start, stop, 1);
    }

    /**
     *
     * @param start
     * @param stop
     * @param step
     * @return
     */
    protected static int[] irange(int start, int stop, int step) {
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
     * @param stop
     * @return
     */
    protected static double[] drange(int stop) {
        return drange(0, stop);
    }

    /**
     *
     * @param start
     * @param stop
     * @return
     */
    protected static double[] drange(int start, int stop) {
        return drange(start, stop, 1);
    }

    /**
     *
     * @param start
     * @param stop
     * @param step
     * @return
     */
    protected static double[] drange(int start, int stop, int step) {
        double[] r = new double[stop - start];
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
    protected static int[] zeros(int size) {
        final int[] values = new int[size];
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if ((nthreads > 1) && (size >= ConcurrencyUtils.getThreadsBeginN_1D())) {
            nthreads = Math.min(nthreads, size);
            Future<?>[] futures = new Future[nthreads];
            int k = size / nthreads;
            for (int j = 0; j < nthreads; j++) {
                final int firstIdx = j * k;
                final int lastIdx = (j == nthreads - 1) ? size : firstIdx + k;
                futures[j] = ConcurrencyUtils.submit(new Runnable() {
                    public void run() {
                        for (int i = firstIdx; i < lastIdx; i++) {
                            values[i] = 0;
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
        } else {
            for (int i = 0; i < size; i++) {
                values[i] = 0;
            }
        }
        return values;
    }

    /**
     *
     * @param size array length
     * @return an integer array with all elements = 1.
     */
    protected static int[] ones(int size) {
        final int[] values = new int[size];
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if ((nthreads > 1) && (size >= ConcurrencyUtils.getThreadsBeginN_1D())) {
            nthreads = Math.min(nthreads, size);
            Future<?>[] futures = new Future[nthreads];
            int k = size / nthreads;
            for (int j = 0; j < nthreads; j++) {
                final int firstIdx = j * k;
                final int lastIdx = (j == nthreads - 1) ? size : firstIdx + k;
                futures[j] = ConcurrencyUtils.submit(new Runnable() {
                    public void run() {
                        for (int i = firstIdx; i < lastIdx; i++) {
                            values[i] = 1;
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
        } else {
            for (int i = 0; i < size; i++) {
                values[i] = 1;
            }
        }
        return values;
    }

    /**
     *
     * @param d
     * @return
     */
    protected static int[] inta(final DoubleMatrix1D d) {
        int size = (int) d.size();
        final int[] values = new int[size];
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if ((nthreads > 1) && (size >= ConcurrencyUtils.getThreadsBeginN_1D())) {
            nthreads = Math.min(nthreads, size);
            Future<?>[] futures = new Future[nthreads];
            int k = size / nthreads;
            for (int j = 0; j < nthreads; j++) {
                final int firstIdx = j * k;
                final int lastIdx = (j == nthreads - 1) ? size : firstIdx + k;
                futures[j] = ConcurrencyUtils.submit(new Runnable() {
                    public void run() {
                        for (int i = firstIdx; i < lastIdx; i++) {
                            values[i] = (int) d.getQuick(i);
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
        } else {
            for (int i = 0; i < size; i++) {
                values[i] = (int) d.getQuick(i);
            }
        }
        return values;
    }

    /**
     *
     * @param d
     * @return
     */
    protected static IntMatrix1D intm(final DoubleMatrix1D d) {
        int size = (int) d.size();
        final IntMatrix1D values = IntFactory1D.dense.make(size);
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if ((nthreads > 1) && (size >= ConcurrencyUtils.getThreadsBeginN_1D())) {
            nthreads = Math.min(nthreads, size);
            Future<?>[] futures = new Future[nthreads];
            int k = size / nthreads;
            for (int j = 0; j < nthreads; j++) {
                final int firstIdx = j * k;
                final int lastIdx = (j == nthreads - 1) ? size : firstIdx + k;
                futures[j] = ConcurrencyUtils.submit(new Runnable() {
                    public void run() {
                        for (int i = firstIdx; i < lastIdx; i++) {
                            values.setQuick(i, (int) d.getQuick(i));
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
        } else {
            for (int i = 0; i < size; i++) {
                values.setQuick(i, (int) d.getQuick(i));
            }
        }
        return values;
    }

    /**
     *
     * @param d
     * @return
     */
    protected static DoubleMatrix1D dbla(final int[] ix) {
        int size = ix.length;
        final DoubleMatrix1D values = DoubleFactory1D.dense.make(size);
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if ((nthreads > 1) && (size >= ConcurrencyUtils.getThreadsBeginN_1D())) {
            nthreads = Math.min(nthreads, size);
            Future<?>[] futures = new Future[nthreads];
            int k = size / nthreads;
            for (int j = 0; j < nthreads; j++) {
                final int firstIdx = j * k;
                final int lastIdx = (j == nthreads - 1) ? size : firstIdx + k;
                futures[j] = ConcurrencyUtils.submit(new Runnable() {
                    public void run() {
                        for (int i = firstIdx; i < lastIdx; i++) {
                            values.setQuick(i, ix[i]);
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
        } else {
            for (int i = 0; i < size; i++) {
                values.setQuick(i, ix[i]);
            }
        }
        return values;
    }

    /**
     *
     * @param d
     * @return
     */
    protected static DoubleMatrix1D dblm(final IntMatrix1D ix) {
        int size = (int) ix.size();
        final DoubleMatrix1D values = DoubleFactory1D.dense.make(size);
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if ((nthreads > 1) && (size >= ConcurrencyUtils.getThreadsBeginN_1D())) {
            nthreads = Math.min(nthreads, size);
            Future<?>[] futures = new Future[nthreads];
            int k = size / nthreads;
            for (int j = 0; j < nthreads; j++) {
                final int firstIdx = j * k;
                final int lastIdx = (j == nthreads - 1) ? size : firstIdx + k;
                futures[j] = ConcurrencyUtils.submit(new Runnable() {
                    public void run() {
                        for (int i = firstIdx; i < lastIdx; i++) {
                            values.setQuick(i, ix.getQuick(i));
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
        } else {
            for (int i = 0; i < size; i++) {
                values.setQuick(i, ix.getQuick(i));
            }
        }
        return values;
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

    /**
     *
     * @param a
     * @param b
     * @return
     */
    protected static int[] cat(int[] a, int[] b) {
        int[] c = new int[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

}
