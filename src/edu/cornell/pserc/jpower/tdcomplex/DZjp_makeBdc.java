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
import cern.colt.matrix.AbstractMatrix;
import cern.colt.matrix.tdouble.DoubleFactory1D;
import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.SparseRCDoubleMatrix2D;

public class DZjp_makeBdc extends DZjp_idx {

    /**
     * Builds the B matrices and phase shift injections for DC power flow.
     * Returns the
     * B matrices and phase shift injection vectors needed for a DC power flow.
     * The bus real power injections are related to bus voltage angles by
     *     P = BBUS * Va + PBUSINJ
     * The real power flows at the from end the lines are related to the bus
     * voltage angles by
     *     Pf = BF * Va + PFINJ
     * Does appropriate conversions to p.u.
     *
     * @param baseMVA
     * @param bus
     * @param branch
     * @return
     */
    @SuppressWarnings("static-access")
    public static AbstractMatrix[] jp_makeBdc(double baseMVA,
            DoubleMatrix2D bus, DoubleMatrix2D branch) {

        /* constants */
        int nb = bus.rows();		// number of buses
        int nl = branch.rows();		// number of lines

        /* check that bus numbers are equal to indices to bus (one set of bus numbers) */
        DoubleMatrix1D bus_i = bus.viewColumn(BUS_I);
        for (int i = 0; i < nb; i++)
            if (bus_i.getQuick(i) != i)
                System.err.println("makeBdc: buses must be numbered consecutively in bus matrix");
                // TODO: throw non consecutive bus numbers exception.

        // for each branch, compute the elements of the branch B matrix and the phase
        // shift "quiescent" injections, where
        //
        //      | Pf |   | Bff  Bft |   | Vaf |   | Pfinj |
        //      |    | = |          | * |     | + |       |
        //      | Pt |   | Btf  Btt |   | Vat |   | Ptinj |

        // ones at in-service branches
        DoubleMatrix1D stat = branch.viewColumn(BR_STATUS);
        // series susceptance
        DoubleMatrix1D b = stat.copy().assign(branch.viewColumn(BR_X), dfunc.div);
        // default tap ratio = 1
        DoubleMatrix1D tap = DoubleFactory1D.dense.make(nl, 1);
        // indices of non-zero tap ratios
        IntArrayList i = new IntArrayList();
        branch.viewColumn(TAP).getNonZeros(i, null);
        // assign non-zero tap ratios
        tap.viewSelection(i.elements()).assign(branch.viewColumn(TAP).viewSelection(i.elements()));
        b.assign(tap, dfunc.div);

        /* build connection matrix Cft = Cf - Ct for line and from - to buses */
        int[] f = inta(branch.viewColumn(F_BUS));
        int[] t = inta(branch.viewColumn(T_BUS));
        int[] il = irange(nl);
//        SparseRCDoubleMatrix2D Cf = new SparseRCDoubleMatrix2D(nl, nb, irange(nl),
//                f, 1.0, false, false);
//        SparseRCDoubleMatrix2D Ct = new SparseRCDoubleMatrix2D(nl, nb, irange(nl),
//                t, 1.0, false, false);

        SparseRCDoubleMatrix2D Cft = new SparseRCDoubleMatrix2D(nl, nb, il, f, 1.0, false, false);
        Cft.viewSelection(il, t).assign(dfunc.minus(1));

        /* build Bf such that Bf * Va is the vector of real branch powers injected
           at each branch's "from" bus */
//        SparseRCDoubleMatrix2D Bf = new SparseRCDoubleMatrix2D(nl, nl,
//                il, f, b.toArray(), false, false, false);
//        Bf.viewSelection(il, t).assign(b.copy().assign(dfunc.neg));
        DoubleMatrix2D Bf = DoubleFactory2D.sparse.diagonal(b).zMult(Cft, null);

        /* build Bbus */
        DoubleMatrix2D Bbus = Cft.viewDice().zMult(Bf, null);

        /* build phase shift injection vectors */
        DoubleMatrix1D Pfinj = branch.viewColumn(SHIFT).copy();
        Pfinj.assign(dfunc.chain(dfunc.mult(Math.PI), dfunc.div(180)));
        Pfinj.assign(dfunc.neg);
        Pfinj.assign(b, dfunc.mult);		// injected at the from bus
        // DoubleMatrix1D Ptinj = Pfinj.assign(dfunc.neg);	// and extracted at the to bus

        DoubleMatrix1D Pbusinj = Cft.viewDice().zMult(Pfinj, null);

        return new AbstractMatrix[] {Bbus, Bf, Pbusinj, Pfinj};
    }

}
