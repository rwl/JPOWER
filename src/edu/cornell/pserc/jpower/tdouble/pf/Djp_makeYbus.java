/*
 * Copyright (C) 1996-2010 Power System Engineering Research Center
 * Copyright (C) 2010-2011 Richard Lincoln
 *
 * JPOWER is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * JPOWER is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JPOWER. If not, see <http://www.gnu.org/licenses/>.
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
 * @author Ray Zimmerman
 * @author Richard Lincoln
 *
 */
public class Djp_makeYbus {

	private static final IntFunctions ifunc = IntFunctions.intFunctions;
	private static DoubleFunctions dfunc = DoubleFunctions.functions;
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
	public static DComplexMatrix2D[] makeYbus(double baseMVA, Djp_bus bus,
			Djp_branch branch) {
		int nb, nl;
		int[] i, f, t, il;
		DoubleMatrix1D dstat, Bc, dtap;
		DComplexMatrix1D cstat, Ys, tap, conj_tap, Ytt, Yff, Yft, Ytf, Ysh;
		SparseRCDComplexMatrix2D Cf, Ct, YYff, YYft, YYtf, YYtt, Yf, Yt;
		DComplexMatrix2D diagYsh, Ybr, Ybus;

		/* constants */
		nb = bus.size();		// number of buses
		nl = branch.size();		// number of lines

		/* check that bus numbers are equal to indices to bus (one set of bus numbers) */
		if ( Djp_util.any( bus.bus_i.copy().assign(IntFactory1D.dense.make(Djp_util.irange(nb)),
				ifunc.equals).assign(ifunc.equals(0))) )
			System.err.println("makeYbus: buses must appear in order by bus number");
			// TODO: throw non consecutive bus numbers exception.

		/* for each branch, compute the elements of the branch admittance matrix where
		 *
		 *		| If |   | Yff  Yft |   | Vf |
		 *		|    | = |          | * |    |
		 *		| It |   | Ytf  Ytt |   | Vt |
		 */
		// ones at in-service branches
		dstat = Djp_util.dblm(branch.br_status);
		cstat = Djp_util.complex(dstat, null);
		// series admittance
		Ys = cstat.assign( Djp_util.complex(branch.br_r, branch.br_x), cfunc.div);
		// line charging susceptance
		Bc = dstat.assign(branch.br_b, dfunc.mult);
		// default tap ratio = 1
		dtap = DoubleFactory1D.dense.make(nl, 1);
		// indices of non-zero tap ratios
		i = Djp_util.nonzero(branch.tap);
		// assign non-zero tap ratios
		dtap.viewSelection(i).assign(branch.tap.viewSelection(i));
		// add phase shifters
		tap = Djp_util.polar(dtap, branch.shift, false);
		conj_tap = tap.copy().assign(cfunc.conj);

		Ytt = Ys.copy().assign(Djp_util.complex(null, Bc.assign(dfunc.div(2))), cfunc.plus);
		Yff = Ytt.copy().assign(tap.copy().assign(conj_tap, cfunc.mult), cfunc.div);
		Ys.assign(cfunc.neg);
		Yft = Ys.copy().assign(conj_tap, cfunc.div);
		Ytf = Ys.assign(tap, cfunc.div);

		/* compute shunt admittance vector
		if Psh is the real power consumed by the shunt at V = 1.0 p.u.
		and Qsh is the reactive power injected by the shunt at V = 1.0 p.u.
		then Psh - j Qsh = V * conj(Ysh * V) = conj(Ysh) = Gs - j Bs,
		i.e. Ysh = Psh + j Qsh */
		Ysh = Djp_util.complex(bus.Gs, bus.Bs).assign(cfunc.div(baseMVA));

		// build connection matrices
		f = branch.f_bus.toArray();		// list of "from" buses
		t = branch.t_bus.toArray();		// list of "to" buses
		il = Djp_util.irange(nl);

		// connection matrix for line & from buses
		Cf = new SparseRCDComplexMatrix2D(nl, nb, il, f, 1, 0, false);
		// connection matrix for line & to buses
		Ct = new SparseRCDComplexMatrix2D(nl, nb, il, t, 1, 0, false);

		/* build Yf and Yt such that Yf * V is the vector of complex branch currents injected
		 * at each branch's "from" bus, and Yt is the same for the "to" bus end
		 */
		// Duplicate entries must be added.
		// FIXME: removeDuplicates sums duplicate entries
		YYff = new SparseRCDComplexMatrix2D(nl, nb, il, f, Yff.toArray(), false, false);
		YYft = new SparseRCDComplexMatrix2D(nl, nb, il, t, Yft.toArray(), false, false);
		Yf = new SparseRCDComplexMatrix2D(nl, nb);
		Yf.assign(YYff, cfunc.plus);
		Yf.assign(YYft, cfunc.plus);

		YYtf = new SparseRCDComplexMatrix2D(nl, nb, il, f, Ytf.toArray(), false, false);
		YYtt = new SparseRCDComplexMatrix2D(nl, nb, il, t, Ytt.toArray(), false, false);
		Yt = new SparseRCDComplexMatrix2D(nl, nb);
		Yt.assign(YYtf, cfunc.plus);
		Yt.assign(YYtt, cfunc.plus);
		//Yf = spdiags(Yff, 0, nl, nl) * Cf + spdiags(Yft, 0, nl, nl) * Ct;
		//Yt = spdiags(Ytf, 0, nl, nl) * Cf + spdiags(Ytt, 0, nl, nl) * Ct;

		/* build Ybus */
		// shunt admittance
		diagYsh = DComplexFactory2D.sparse.diagonal(Ysh);
		// branch admittances
		Ybr = Cf.getConjugateTranspose().zMult(Yf, null);
		Ybr.assign(Ct.getConjugateTranspose().zMult(Yt, null), cfunc.plus);
		// bus admittance
		Ybus = Ybr.assign(diagYsh, cfunc.plus);

		return new DComplexMatrix2D[] {Ybus, Yf, Yt};
	}

}
