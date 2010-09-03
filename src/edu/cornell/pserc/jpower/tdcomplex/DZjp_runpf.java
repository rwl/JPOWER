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

import java.util.List;
import java.util.Map;

import cern.colt.list.tint.IntArrayList;
import cern.colt.matrix.AbstractMatrix;
import cern.colt.matrix.tdcomplex.DComplexMatrix1D;
import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.jet.math.tdcomplex.DComplexFunctions;
import cern.jet.math.tdouble.DoubleFunctions;

/**
 * Runs a power flow
 *
 * Runs a power flow (full AC Newton's method by default) and optionally
 * returns the solved values in the data matrices, a flag which is true if
 * the algorithm was successful in finding a solution, and the elapsed
 * time in seconds. All input arguments are optional. If casename is
 * provided it specifies the name of the input data file or struct
 * containing the power flow data. The default value is 'case9'.
 *
 * If the ppopt is provided it overrides the default PYPOWER options
 * vector and can be used to specify the solution algorithm and output
 * options among other things. If the 3rd argument is given the pretty
 * printed output will be appended to the file whose name is given in
 * fname. If solvedcase is specified the solved case will be written to a
 * case file in MATPOWER format with the specified name. If solvedcase
 * ends with '.mat' it saves the case as a MAT-file otherwise it saves it
 * as an M-file.
 *
 * If the ENFORCE_Q_LIMS options is set to true (default is false) then if
 * any generator reactive power limit is violated after running the AC
 * power flow, the corresponding bus is converted to a PQ bus, with Qg at
 * the limit, and the case is re-run. The voltage magnitude at the bus
 * will deviate from the specified value in order to satisfy the reactive
 * power limit. If the reference bus is converted to PQ, the first
 * remaining PV bus will be used as the slack bus for the next iteration.
 * This may result in the real power output at this generator being
 * slightly off from the specified values.
 *
 * @author Ray Zimmerman (rz10@cornell.edu)
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class DZjp_runpf extends DZjp_idx {

    /**
     *
     * @return
     */
    public static Object[] jp_runpf() {

        return jp_runpf("case9");
    }

    /**
     *
     * @param casedata
     * @return
     */
    public static Object[] jp_runpf(String casedata) {

        return jp_runpf(casedata, DZjp_jpoption.jp_jpoption());
    }

    /**
     *
     * @param casedata
     * @param jpopt
     * @return
     */
    public static Object[] jp_runpf(String casedata, DoubleMatrix1D jpopt) {

        return jp_runpf(casedata, jpopt, "");
    }

    /**
     *
     * @param casedata
     * @param jpopt
     * @param fname
     * @return
     */
    public static Object[] jp_runpf(String casedata, DoubleMatrix1D jpopt,
            String fname) {

        return jp_runpf(casedata, jpopt, fname, "");
    }

    /**
     *
     * @param casedata
     * @param jpopt
     * @param fname
     * @param solvedcase
     * @return
     */
    public static Object[] jp_runpf(String casedata, DoubleMatrix1D jpopt,
            String fname, String solvedcase) {

        return jp_runpf(casedata, jpopt, fname, "");
    }

    /**
     *
     * @param casedata
     * @return
     */
    public static Object[] jp_runpf(DZjp_jpc casedata) {

        return jp_runpf(casedata, DZjp_jpoption.jp_jpoption());
    }

    /**
     *
     * @param casedata
     * @param jpopt
     * @return
     */
    public static Object[] jp_runpf(DZjp_jpc casedata,
            DoubleMatrix1D jpopt) {

        return jp_runpf(casedata, jpopt, "");
    }

    /**
     *
     * @param casedata
     * @param jpopt
     * @param fname
     * @return
     */
    public static Object[] jp_runpf(DZjp_jpc casedata,
            DoubleMatrix1D jpopt, String fname) {

        return jp_runpf(casedata, jpopt, "", "");
    }

    /**
     *
     * @param casedata
     * @param jpopt
     * @param fname
     * @param solvedcase
     * @return
     */
    @SuppressWarnings("static-access")
    public static Object[] jp_runpf(DZjp_jpc casedata,
            DoubleMatrix1D jpopt, String fname, String solvedcase) {

        DoubleFunctions func = DoubleFunctions.functions;
        DComplexFunctions cfunc = DComplexFunctions.functions;

        /* options */
        int verbose = (int) jpopt.get(31);
        boolean qlim = jpopt.get(6) != 0.0;     /* enforce Q limits on gens? */
        boolean dc = jpopt.get(10) != 0.0;      /* use DC formulation? */

        /* read data */
        DZjp_jpc jpc = DZjp_loadcase.loadcase(casedata);

        /* add zero columns to branch for flows if needed */
        DoubleMatrix2D branch = (DoubleMatrix2D) jpc.branch;
        if (branch.columns() < QT) {
            DoubleMatrix2D flows = DoubleFactory2D.dense.make(branch.rows(),
                    QT - branch.columns());
            jpc.branch = DoubleFactory2D.dense.appendColumns(branch, flows);
        }

        /* convert to internal indexing */
        jpc = DZjp_ext2int.jp_ext2int(jpc);
        double baseMVA = jpc.baseMVA;
        DoubleMatrix2D bus = jpc.bus.copy();
        DoubleMatrix2D gen = jpc.gen.copy();
        branch = jpc.branch.copy();

        /* get bus index lists of each type of bus */
        List<int[]> bustypes = DZjp_bustypes.jp_bustypes(bus, gen);
        int ref = bustypes.get(0)[0];
        int[] pv = bustypes.get(1);
        int[] pq = bustypes.get(2);

        /* generator info */
        IntArrayList on = new IntArrayList();     // which generators are on?
        gen.viewColumn(GEN_STATUS).getPositiveValues(on, null);
        double[] dgbus = gen.viewColumn(GEN_BUS).viewSelection(on.elements()).toArray();
        int[] gbus = new int[dgbus.length];       // what buses are they at?
        for (int i = 0; i < dgbus.length; i++) { gbus[i] = (int) dgbus[i]; }

        /* -----  run the power flow  ----- */
        long t0 = System.currentTimeMillis();
        if (verbose > 0) {
            Map<String, String> v = DZjp_jpver.jp_jpver("all");
            System.out.printf("\nJPOWER Version %s, %s", v.get("Version"), v.get("Date"));
        }

        boolean success = false;
        if (dc) {                                 // DC formulation
            if (verbose > 0)
                System.out.printf(" -- DC Power Flow\n");

            /* initial state */
            DoubleMatrix1D Va0 = bus.viewColumn(VA).copy();
            Va0.assign(func.chain(func.mult(Math.PI), func.div(180)));

            /* build B matrices and phase shift injections */
            List<AbstractMatrix> Bdc = DZjp_makeBdc.jp_makeBdc(baseMVA, bus, branch);
            DoubleMatrix2D B = (DoubleMatrix2D) Bdc.get(0);
            DoubleMatrix2D Bf = (DoubleMatrix2D) Bdc.get(1);
            DComplexMatrix1D Pbusinj = (DComplexMatrix1D) Bdc.get(2);
            DoubleMatrix1D Pfinj = (DoubleMatrix1D) Bdc.get(3);

            /* compute complex bus power injections (generation - load) */
            /* adjusted for phase shifters and real shunts */
            DoubleMatrix1D Pbus = DZjp_makeSbus.jp_makeSbus(baseMVA, bus, gen).getRealPart();
            Pbus.assign(Pbusinj.getRealPart(), func.minus);
            Pbus.assign(bus.viewColumn(GS), func.chain(func.div(baseMVA), func.minus));

            /* "run" the power flow */
            DoubleMatrix1D Va = DZjp_dcpf.jp_dcpf(B, Pbus, Va0, ref, pv, pq);

            /* update data matrices with solution */
            branch.viewColumn(QF).assign(0);
            branch.viewColumn(QT).assign(0);
            branch.viewColumn(PF).assign(
                    Bf.zMult(Va, null).assign(Pfinj, func.plus).assign(func.mult(baseMVA)));
            branch.viewColumn(PT).assign(
                    branch.viewColumn(PT).copy().assign(func.neg));
            bus.viewColumn(VM).assign(1);
            bus.viewColumn(VA).assign(Va);
            bus.viewColumn(VA).assign(func.chain(func.mult(180), func.div(Math.PI)));
            // update Pg for swing generator (note: other gens at ref bus are accounted for in Pbus)
            //      Pg = Pinj + Pload + Gs
            //      newPg = oldPg + newPinj - oldPinj
            int refgen = 0;
            for (int i : gbus) if (i == ref) { refgen = i; break; }
            gen.set(on.get(refgen), PG, gen.get(on.get(refgen), PG) + (B.viewRow(ref).zDotProduct(Va) - Pbus.get(ref)) * baseMVA);

            success = true;
        } else {                                  // AC formulation

        }
        jpc.et = (System.currentTimeMillis() - t0) / 1000F;
        jpc.success = success;

        /* -----  output results  ----- */
        // convert back to original bus numbering & print results
        jpc.bus = bus;
        jpc.gen = gen;
        jpc.branch = branch;

        DZjp_jpc results = DZjp_int2ext.jp_int2ext(jpc);

        // zero out result fields of out-of-service gens & branches
        if (results.order.gen.status.off.size() > 0)
            results.gen.viewSelection(results.order.gen.status.off.elements(),
                    new int[] {PG, QG}).assign(0);
        if (results.order.branch.status.off.size() > 0)
            results.branch.viewSelection(results.order.branch.status.off.elements(),
                    new int[] {PF, QF, PT, QT}).assign(0);

        if (fname != "") {
            // TODO: printpf to file
        }
        DZjp_printpf.jp_printpf(results, 1, jpopt);

        /* save solved case */
        if (solvedcase != "")
            DZjp_savecase.jp_savecase(solvedcase, results);

        return new Object[] {baseMVA, jpc.bus, jpc.gen, jpc.branch, jpc.et};
    }

}
