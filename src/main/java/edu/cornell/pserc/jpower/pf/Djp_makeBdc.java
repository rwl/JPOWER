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

package edu.cornell.pserc.jpower.pf;

import cern.colt.matrix.AbstractMatrix;
import cern.colt.matrix.tdouble.DoubleFactory1D;
import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.SparseRCDoubleMatrix2D;
import cern.colt.matrix.tint.IntFactory1D;
import cern.colt.matrix.tint.IntMatrix1D;

import static edu.emory.mathcs.utils.Utils.ifunc;
import static edu.emory.mathcs.utils.Utils.dfunc;
import static edu.emory.mathcs.utils.Utils.any;
import static edu.emory.mathcs.utils.Utils.irange;
import static edu.emory.mathcs.utils.Utils.dblm;
import static edu.emory.mathcs.utils.Utils.nonzero;
import static edu.emory.mathcs.utils.Utils.icat;

import edu.cornell.pserc.jpower.jpc.Branch;
import edu.cornell.pserc.jpower.jpc.Bus;

public class Djp_makeBdc {

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
	public static AbstractMatrix[] makeBdc(double baseMVA, Bus bus, Branch branch) {
		int nb, nl;
		int[] xfmr, ft, il;
		double[] v;
		DoubleMatrix1D stat, b, tap, Pfinj, Pbusinj;
		SparseRCDoubleMatrix2D Cft;
		DoubleMatrix2D Bf, Bbus;

		/* constants */
		nb = bus.size();		// number of buses
		nl = branch.size();		// number of lines

		/* check that bus numbers are equal to indices to bus (one set of bus numbers) */
		if ( any( bus.bus_i.copy().assign(IntFactory1D.dense.make(irange(nb)),
				ifunc.equals).assign(ifunc.equals(0))) )
			System.err.println("makeBdc: buses must be numbered consecutively in bus matrix");
			// TODO: throw non consecutive bus numbers exception.

		/* for each branch, compute the elements of the branch B matrix and the
		 * phase shift "quiescent" injections, where
		 *
		 * 	| Pf |   | Bff  Bft |   | Vaf |   | Pfinj |
		 * 	|    | = |          | * |     | + |       |
		 * 	| Pt |   | Btf  Btt |   | Vat |   | Ptinj |
		 */
		// ones at in-service branches
		stat = dblm(branch.br_status);
		// series susceptance
		b = stat.assign(branch.br_x, dfunc.div);
		// default tap ratio = 1
		tap = DoubleFactory1D.dense.make(nl, 1);
		// indices of non-zero tap ratios
		xfmr = nonzero(branch.tap);
		// assign non-zero tap ratios
		tap.viewSelection(xfmr).assign(branch.tap.viewSelection(xfmr));
		b.assign(tap, dfunc.div);

		/* build connection matrix Cft = Cf - Ct for line and from - to buses */
		ft = IntFactory1D.dense.make(new IntMatrix1D[] {branch.f_bus, branch.t_bus}).toArray();
		il = icat(irange(nl), irange(nl));
		v = DoubleFactory1D.dense.make(new DoubleMatrix1D[] {
				DoubleFactory1D.dense.make(nl,  1),
				DoubleFactory1D.dense.make(nl, -1)
		}).toArray();

		Cft = new SparseRCDoubleMatrix2D(nl, nb, il, ft, v, false, false, false);

		/* build Bf such that Bf * Va is the vector of real branch powers
		 * injected at each branch's "from" bus
		 */
		Bf = DoubleFactory2D.sparse.diagonal(b).zMult(Cft, null);

		/* build Bbus */
		Bbus = Cft.viewDice().zMult(Bf, null);

		/* build phase shift injection vectors */
		Pfinj = branch.shift.copy();
		Pfinj.assign(dfunc.chain(dfunc.mult(Math.PI), dfunc.div(180)));
		Pfinj.assign(dfunc.neg);
		Pfinj.assign(b, dfunc.mult);		// injected at the from bus
		// DoubleMatrix1D Ptinj = Pfinj.assign(dfunc.neg);	// and extracted at the to bus

		Pbusinj = Cft.viewDice().zMult(Pfinj, null);

		return new AbstractMatrix[] {Bbus, Bf, Pbusinj, Pfinj};
	}

}
