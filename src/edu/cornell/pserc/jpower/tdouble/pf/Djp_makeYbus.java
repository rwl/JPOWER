/*
 * Copyright (C) 1996-2010 Power System Engineering Research Center (PSERC)
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

package edu.cornell.pserc.jpower.tdouble.pf;

import cern.colt.matrix.tdcomplex.DComplexFactory2D;
import cern.colt.matrix.tdcomplex.DComplexMatrix1D;
import cern.colt.matrix.tdcomplex.DComplexMatrix2D;
import cern.colt.matrix.tdcomplex.impl.SparseRCDComplexMatrix2D;
import cern.colt.matrix.tdouble.DoubleFactory1D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tint.IntFactory1D;
import cern.colt.util.tdouble.Djp_util;
import cern.jet.math.tdcomplex.DComplexFunctions;
import cern.jet.math.tdouble.DoubleFunctions;
import cern.jet.math.tint.IntFunctions;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_branch;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_bus;

/**
 * Builds the bus admittance matrix and branch admittance matrices.
 *
 * @author Ray Zimmerman (rz10@cornell.edu)
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class Djp_makeYbus {

	private static final Djp_util util = new Djp_util();
	private static final IntFunctions ifunc = IntFunctions.intFunctions;
	public static DoubleFunctions dfunc = DoubleFunctions.functions;
	private static final DComplexFunctions cfunc = DComplexFunctions.functions;

	/**
	 * Returns the full
	 * bus admittance matrix (i.e. for all buses) and the matrices YF and YT
	 * which, when multiplied by a complex voltage vector, yield the vector
	 * currents injected into each line from the "from" and "to" buses
	 * respectively of each line. Does appropriate conversions to p.u.
	 *
	 * @see makeSbus
	 * @param baseMVA
	 * @param bus
	 * @param branch
	 * @return
	 */
	@SuppressWarnings("static-access")
	public static DComplexMatrix2D[] jp_makeYbus(double baseMVA, Djp_bus bus, Djp_branch branch) {
		/* constants */
		int nb = bus.size();		// number of buses
		int nl = branch.size();		// number of lines

		/* check that bus numbers are equal to indices to bus (one set of bus numbers) */
		if ( util.any( bus.bus_i.copy().assign(IntFactory1D.dense.make(util.irange(nb)), ifunc.equals).assign(ifunc.equals(0))) )
			System.err.println("makeYbus: buses must appear in order by bus number");
			// TODO: throw non consecutive bus numbers exception.

		/* for each branch, compute the elements of the branch admittance matrix where
		 *
		 *		| If |   | Yff  Yft |   | Vf |
		 *		|    | = |          | * |    |
		 *		| It |   | Ytf  Ytt |   | Vt |
		 */
		// ones at in-service branches
		DoubleMatrix1D dstat = util.dblm(branch.br_status);
		DComplexMatrix1D cstat = util.complex(dstat, null);
		// series admittance
		DComplexMatrix1D Ys = cstat.assign( util.complex(branch.br_r, branch.br_x), cfunc.div);
		// line charging susceptance
		DoubleMatrix1D Bc = dstat.assign(branch.br_b, dfunc.mult);
		// default tap ratio = 1
		DoubleMatrix1D dtap = DoubleFactory1D.dense.make(nl, 1);
		// indices of non-zero tap ratios
		int[] i = util.nonzero(branch.tap);
		// assign non-zero tap ratios
		dtap.viewSelection(i).assign(branch.tap.viewSelection(i));
		// add phase shifters
		DComplexMatrix1D tap = util.polar(dtap, branch.shift, false);
		DComplexMatrix1D conj_tap = tap.copy().assign(cfunc.conj);

		DComplexMatrix1D Ytt = Ys.copy().assign(util.complex(null, Bc.assign(dfunc.div(2))), cfunc.plus);
		DComplexMatrix1D Yff = Ytt.copy().assign(tap.copy().assign(conj_tap), cfunc.div);
		Ys.assign(cfunc.neg);
		DComplexMatrix1D Yft = Ys.copy().assign(conj_tap, cfunc.div);
		DComplexMatrix1D Ytf = Ys.assign(tap, cfunc.div);

		/* compute shunt admittance vector
		if Psh is the real power consumed by the shunt at V = 1.0 p.u.
		and Qsh is the reactive power injected by the shunt at V = 1.0 p.u.
		then Psh - j Qsh = V * conj(Ysh * V) = conj(Ysh) = Gs - j Bs,
		i.e. Ysh = Psh + j Qsh */
		DComplexMatrix1D Ysh = util.complex(bus.Gs, bus.Bs).assign(cfunc.div(baseMVA));

		// build connection matrices
		int[] f = branch.f_bus.toArray();		// list of "from" buses
		int[] t = branch.t_bus.toArray();		// list of "to" buses
		int[] il = util.irange(nl);

		// connection matrix for line & from buses
		SparseRCDComplexMatrix2D Cf = new SparseRCDComplexMatrix2D(nl, nb, il, f, 1, 0, false);
		// connection matrix for line & to buses
		SparseRCDComplexMatrix2D Ct = new SparseRCDComplexMatrix2D(nl, nb, il, t, 1, 0, false);

		/* build Yf and Yt such that Yf * V is the vector of complex branch currents injected
		 * at each branch's "from" bus, and Yt is the same for the "to" bus end
		 */
		// Duplicate entries must be added.
		SparseRCDComplexMatrix2D YYff = new SparseRCDComplexMatrix2D(nl, nb, il, f, Yff.toArray(), false, false);
		SparseRCDComplexMatrix2D YYft = new SparseRCDComplexMatrix2D(nl, nb, il, t, Yft.toArray(), false, false);
		SparseRCDComplexMatrix2D Yf = new SparseRCDComplexMatrix2D(nl, nb);
		Yf.assign(YYff, DComplexFunctions.plus);
		Yf.assign(YYft, DComplexFunctions.plus);

		SparseRCDComplexMatrix2D YYtf = new SparseRCDComplexMatrix2D(nl, nb, il, f, Ytf.toArray(), false, false);
		SparseRCDComplexMatrix2D YYtt = new SparseRCDComplexMatrix2D(nl, nb, il, t, Ytt.toArray(), false, false);
		SparseRCDComplexMatrix2D Yt = new SparseRCDComplexMatrix2D(nl, nb);
		Yt.assign(YYtf, DComplexFunctions.plus);
		Yt.assign(YYtt, DComplexFunctions.plus);
		//Yf = spdiags(Yff, 0, nl, nl) * Cf + spdiags(Yft, 0, nl, nl) * Ct;
		//Yt = spdiags(Ytf, 0, nl, nl) * Cf + spdiags(Ytt, 0, nl, nl) * Ct;

		/* build Ybus */
		// shunt admittance
		DComplexMatrix2D diagYsh = DComplexFactory2D.sparse.diagonal(Ysh);
		// branch admittances
		DComplexMatrix2D Ybr = Cf.getConjugateTranspose().zMult(Yf, null);
		Ybr.assign(Ct.getConjugateTranspose().zMult(Yt, null));
		// bus admittance
		DComplexMatrix2D Ybus = Ybr.assign(diagYsh, cfunc.plus);

		return new DComplexMatrix2D[] {Ybus, Yf, Yt};
	}
}
