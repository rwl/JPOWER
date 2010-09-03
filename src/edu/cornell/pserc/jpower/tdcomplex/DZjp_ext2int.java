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

import cern.colt.list.tint.IntArrayList;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tint.IntFactory1D;
import cern.colt.matrix.tint.IntFactory2D;
import cern.colt.matrix.tint.IntMatrix1D;
import cern.colt.matrix.tint.impl.SparseRCIntMatrix2D;
import cern.jet.math.tdouble.DoubleFunctions;
import cern.jet.math.tint.IntFunctions;

/**
 * Converts external to internal indexing.
 *
 * This function performs several different tasks, depending on the
 * arguments passed.
 *
 * 1.  [I2E, BUS, GEN, BRANCH, AREAS] = EXT2INT(BUS, GEN, BRANCH, AREAS)
 *     [I2E, BUS, GEN, BRANCH] = EXT2INT(BUS, GEN, BRANCH)
 *
 * If the first argument is a matrix, it simply converts from (possibly
 * non-consecutive) external bus numbers to consecutive internal bus
 * numbers which start at 1. Changes are made to BUS, GEN, BRANCH and
 * optionally AREAS matrices, which are returned along with a vector of
 * indices I2E that can be passed to INT2EXT to perform the reverse
 * conversion, where EXTERNAL_BUS_NUMBER = I2E(INTERNAL_BUS_NUMBER)
 *
 * Examples:
 *     [i2e, bus, gen, branch, areas] = ext2int(bus, gen, branch, areas);
 *     [i2e, bus, gen, branch] = ext2int(bus, gen, branch);
 *
 * 2.  ppc = EXT2INT(ppc)
 *
 * If the input is a single MATPOWER case struct, then all isolated
 * buses, off-line generators and branches are removed along with any
 * generators, branches or areas connected to isolated buses. Then the
 * buses are renumbered consecutively, beginning at 1, and the
 * generators are sorted by increasing bus number. All of the related
 * indexing information and the original data matrices are stored in
 * an 'order' field in the struct to be used by INT2EXT to perform
 * the reverse conversions. If the case is already using internal
 * numbering it is returned unchanged.
 *
 * Example:
 *     ppc = ext2int(ppc);
 *
 * 3.  VAL = EXT2INT(ppc, VAL, ORDERING)
 *     VAL = EXT2INT(ppc, VAL, ORDERING, DIM)
 *     ppc = EXT2INT(ppc, FIELD, ORDERING)
 *     ppc = EXT2INT(ppc, FIELD, ORDERING, DIM)
 *
 * When given a case struct that has already been converted to
 * internal indexing, this function can be used to convert other data
 * structures as well by passing in 2 or 3 extra parameters in
 * addition to the case struct. If the value passed in the 2nd
 * argument is a column vector, it will be converted according to the
 * ORDERING specified by the 3rd argument (described below). If VAL
 * is an n-dimensional matrix, then the optional 4th argument (DIM,
 * default = 1) can be used to specify which dimension to reorder.
 * The return value in this case is the value passed in, converted
 * to internal indexing.
 *
 * If the 2nd argument is a string or cell array of strings, it
 * specifies a field in the case struct whose value should be
 * converted as described above. In this case, the converted value
 * is stored back in the specified field, the original value is
 * saved for later use and the updated case struct is returned.
 * If FIELD is a cell array of strings, they specify nested fields.
 *
 * The 3rd argument, ORDERING, is used to indicate whether the data
 * corresponds to bus-, gen- or branch-ordered data. It can be one
 * of the following three strings: 'bus', 'gen' or 'branch'. For
 * data structures with multiple blocks of data, ordered by bus,
 * gen or branch, they can be converted with a single call by
 * specifying ORDERING as a cell array of strings.
 *
 * Any extra elements, rows, columns, etc. beyond those indicated
 * in ORDERING, are not disturbed.
 *
 * @author Ray Zimmerman (rz10@cornell.edu)
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class DZjp_ext2int extends DZjp_idx {

    /**
     *
     * @param bus
     * @param gen
     * @param branch
     * @return
     */
    public static DZjp_jpc jp_ext2int(DoubleMatrix2D bus, DoubleMatrix2D gen,
            DoubleMatrix2D branch) {
        return jp_ext2int(bus, gen, branch, null);
    }

    /**
     *
     * @param bus
     * @param gen
     * @param branch
     * @param areas
     * @return
     */
    public static DZjp_jpc jp_ext2int(DoubleMatrix2D bus,
            DoubleMatrix2D gen, DoubleMatrix2D branch, DoubleMatrix2D areas) {
        return null;
    }

    /**
     *
     * @param jpc
     * @return
     */
    public static DZjp_jpc jp_ext2int(DZjp_jpc jpc) {
        boolean first = jpc.order == null;
        if (first || jpc.order.state.equals('e')) {
            /* initialize order */
            DZjp_order o;
            if (first) {
                o = new DZjp_order();
            } else {
                o = jpc.order;
            }
            /* sizes */
            int nb = jpc.bus.rows();
            int ng = jpc.gen.rows();
            int ng0 = ng;
            boolean dc;
            if (jpc.A != null && jpc.A.columns() < 2*nb + 2*ng) {
                dc = true;
            } else if (jpc.N != null && jpc.N.columns() < 2*nb + 2*ng) {
                dc = true;
            } else {
                dc = false;
            }

            /* save data matrices with external ordering */
            o.external.bus    = jpc.bus;
            o.external.branch = jpc.branch;
            o.external.gen    = jpc.gen;
            if (jpc.areas != null) {
                // if areas field is empty delete it (so it gets ignored)
                if (jpc.areas.size() == 0) {
                    jpc.areas = null;
                } else {
                    // otherwise save it
                    o.external.areas = jpc.areas;
                }
            }

            /* check that all buses have a valid BUS_TYPE */
            int[] bt = idx(jpc.bus.viewColumn(BUS_I));
            for (int i = 0; i < nb; i++)
                if (bt[i] != PQ || bt[i] != PV || bt[i] != REF || bt[i] != NONE)
                    System.out.printf("ext2int: bus %d has an invalid BUS_TYPE", i);
                    // TODO: Throw invalid bus type exception.

            /* determine which buses, branches, gens are connected & in-service */
            int[] bi = idx(jpc.bus.viewColumn(BUS_I));
            SparseRCIntMatrix2D n2i = new SparseRCIntMatrix2D(max(bi), 1,
                  bi, ones(nb), rg(nb), false, false, false);

            /* bus status */
            IntMatrix1D bs = IntFactory1D.dense.make(bt);
            bs.assign(IntFunctions.compare(NONE));
            bs.getNonZeros(o.bus.status.on, null); // connected
            bs = IntFactory1D.dense.make(bt);
            bs.assign(IntFunctions.equals(NONE));
            bs.getPositiveValues(o.bus.status.off, null); // isolated

            /* gen status */
            int[] gbus = idx(jpc.gen.viewColumn(GEN_BUS));
            bs.viewSelection(n2i.viewSelection(gbus, null).viewColumn(0).toArray());

            DoubleMatrix1D gs = jpc.gen.viewColumn(GEN_STATUS);
        }

        return null;
    }

}
