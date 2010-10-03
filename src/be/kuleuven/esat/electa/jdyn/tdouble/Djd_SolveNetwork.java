/*
 * Copyright (C) 2009 Stijn Cole <stijn.cole@esat.kuleuven.be>
 * Copyright (C) 2010 Richard Lincoln <r.w.lincoln@gmail.com>
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

package be.kuleuven.esat.electa.jdyn.tdouble;

import cern.colt.matrix.tdcomplex.DComplexFactory1D;
import cern.colt.matrix.tdcomplex.DComplexMatrix1D;
import cern.colt.matrix.tdcomplex.algo.decomposition.SparseDComplexLUDecomposition;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tint.IntFactory1D;
import cern.colt.matrix.tint.IntMatrix1D;
import cern.colt.util.tdouble.Djp_util;
import cern.jet.math.tdcomplex.DComplexFunctions;
import cern.jet.math.tint.IntFunctions;

/**
 * Solves the network.
 *
 * @author Stijn Cole (stijn.cole@esat.kuleuven.be)
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class Djd_SolveNetwork {

	private static final Djp_util util = new Djp_util();
	private static final IntFunctions ifunc = IntFunctions.intFunctions;
	private static final DComplexFunctions cfunc = DComplexFunctions.functions;

	/**
	 *
	 * @param Xgen state variables of generators
	 * @param Pgen parameters of generators
	 * @param invYbus factorised augmented bus admittance matrix
	 * @param gbus generator buses
	 * @param gentype generator models
	 * @return bus voltages
	 */
	@SuppressWarnings("static-access")
	public static DComplexMatrix1D jp_SolveNetwork(DoubleMatrix2D Xgen, DoubleMatrix2D Pgen,
			SparseDComplexLUDecomposition invYbus, int[] gbus, IntMatrix1D gentype) {

		/* Init */
		int ngen = gbus.length;
		DComplexMatrix1D Igen = DComplexFactory1D.dense.make(ngen);

		int s = invYbus.getPivot().length;

		DComplexMatrix1D Ig = DComplexFactory1D.dense.make(s);
		IntMatrix1D d = IntFactory1D.dense.make(util.irange((int) gentype.size()));

		/* Define types */
		int[] type1 = d.viewSelection( gentype.copy().assign(ifunc.equals(1)).toArray() ).toArray();
		int[] type2 = d.viewSelection( gentype.copy().assign(ifunc.equals(2)).toArray() ).toArray();

		DoubleMatrix1D delta, Eq_tr, Ed_tr, xd_tr;

		/* Generator type 1: classical model */
		delta = Xgen.viewColumn(0).viewSelection(type1).copy();
		Eq_tr = Xgen.viewColumn(2).viewSelection(type1).copy();

		xd_tr = Pgen.viewColumn(6).viewSelection(type1).copy();

		// Calculate generator currents
		Igen.viewSelection(type1).assign( util.polar(Eq_tr, delta).assign(util.complex(null, xd_tr), cfunc.div) );

		/* Generator type 2: 4th order model */
		delta = Xgen.viewColumn(0).viewSelection(type2).copy();
		Eq_tr = Xgen.viewColumn(2).viewSelection(type2).copy();
		Ed_tr = Xgen.viewColumn(3).viewSelection(type2).copy();

		xd_tr = Pgen.viewColumn(7).viewSelection(type2).copy();

		// Calculate generator currents
		Igen.viewSelection(type2).assign( util.complex(Eq_tr, Ed_tr).assign(util.complex(null, delta).assign(cfunc.exp), cfunc.mult) );
		Igen.viewSelection(type2).assign( util.complex(null, xd_tr), cfunc.div );  // Padiyar, p.417.

		/* Calculations */
		// Generator currents
		Ig.viewSelection(gbus).assign(Igen);

		// Calculate network voltages: U = Y/Ig
		invYbus.solve(Ig);
		DComplexMatrix1D U = Ig;
		
		return U;
	}
}
