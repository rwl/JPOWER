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

package edu.cornell.pserc.jpower.tdouble.opf;

import cern.colt.matrix.AbstractMatrix;
import cern.colt.matrix.tdouble.DoubleFactory1D;
import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.SparseRCDoubleMatrix2D;
import cern.colt.matrix.tint.IntFactory1D;
import cern.colt.util.tdouble.Djp_util;
import cern.jet.math.tdouble.DoubleFunctions;
import cern.jet.math.tint.IntFunctions;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_gen;

/**
 * Construct linear constraints for constant power factor var loads.
 *
 * @author Ray Zimmerman (rz10@cornell.edu)
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class Djp_makeAvl {

	private static final Djp_util util = new Djp_util();
	private static final DoubleFunctions dfunc = DoubleFunctions.functions;
	private static final IntFunctions ifunc = IntFunctions.intFunctions;

	/**
	 * Constructs parameters for the following linear constraint enforcing a
	 * constant power factor constraint for dispatchable loads.
	 *
	 * 	LVL <= AVL * [Pg; Qg] <= UVL
	 *
	 * IVL is the vector of indices of generators representing variable loads.
	 *
	 * @param baseMVA
	 * @param gen
	 * @return
	 */
	@SuppressWarnings("static-access")
	public static AbstractMatrix[] jp_makeAvl(double baseMVA, Djp_gen gen) {

		/* data dimensions */
		int ng = gen.size();	// number of dispatchable injections
		DoubleMatrix1D Pg = gen.Pg.copy().assign(dfunc.div(baseMVA));
		DoubleMatrix1D Qg = gen.Qg.copy().assign(dfunc.div(baseMVA));
		DoubleMatrix1D Pmin = gen.Pmin.copy().assign(dfunc.div(baseMVA));
		DoubleMatrix1D Qmin = gen.Qmin.copy().assign(dfunc.div(baseMVA));
		DoubleMatrix1D Qmax = gen.Qmax.copy().assign(dfunc.div(baseMVA));

		/*
		 * Find out if any of these "generators" are actually dispatchable loads.
		 * (see 'help isload' for details on what constitutes a dispatchable load)
		 * Dispatchable loads are modeled as generators with an added constant
		 * power factor constraint. The power factor is derived from the original
		 * value of Pmin and either Qmin (for inductive loads) or Qmax (for capacitive
		 * loads). If both Qmin and Qmax are zero, this implies a unity power factor
		 * without the need for an additional constraint.
		 */

		int[] ivl = util.nonzero( Djp_jp_isload.jp_isload(gen).assign(util.intm( Qmax.copy().assign(dfunc.equals(0)) ).assign(ifunc.not).assign(util.intm( Qmin.copy().assign(dfunc.equals(0)) ).assign(ifunc.not), ifunc.or), ifunc.and) );
		int nvl = ivl.length;	// number of dispatchable loads

		/* at least one of the Q limits must be zero (corresponding to Pmax == 0) */
		if (util.any( util.intm( Qmin.viewSelection(ivl).copy().assign(dfunc.equals(0)) ).assign(ifunc.not).assign(util.intm( Qmax.viewSelection(ivl).copy().assign(dfunc.equals(0)) ).assign(ifunc.not), ifunc.and) ));
			System.err.println("makeAvl: either Qmin or Qmax must be equal to zero for each dispatchable load.");
			// TODO: throw invalid value exception

		/*
		 * Initial values of PG and QG must be consistent with specified power factor
		 * This is to prevent a user from unknowingly using a case file which would
		 * have defined a different power factor constraint under a previous version
		 * which used PG and QG to define the power factor.
		 */

		DoubleMatrix1D Qlim = Qmin.viewSelection(ivl).copy().assign(dfunc.equals(0)).assign(Qmax.viewSelection(ivl), dfunc.mult);
		Qlim.assign(Qmax.viewSelection(ivl).copy().assign(dfunc.equals(0)).assign(Qmin.viewSelection(ivl), dfunc.mult), dfunc.plus);


		if (util.any(Qg.viewSelection(ivl).copy().assign(Pg.viewSelection(ivl), dfunc.minus).assign(Qlim.copy().assign(Pmin.viewSelection(ivl), dfunc.div), dfunc.mult).assign(dfunc.abs)))
			System.out.println("makeAvl: %s\n" +
					"For a dispatchable load, PG and QG must be consistent" +
					"with the power factor defined by PMIN and the Q limits.");

		/* make Avl, lvl, uvl, for lvl <= Avl * [Pg; Qg] <= uvl */
		DoubleMatrix2D Avl;
		DoubleMatrix1D lvl, uvl;
		if (nvl > 0) {
			DoubleMatrix1D xx = Pmin.viewSelection(ivl).copy();
			DoubleMatrix1D yy = Qlim.copy();
			DoubleMatrix1D pftheta = yy.copy().assign(xx, dfunc.atan2);
			DoubleMatrix1D pc = pftheta.copy().assign(dfunc.sin);
			DoubleMatrix1D qc = pftheta.copy().assign(dfunc.cos).assign(dfunc.neg);

			DoubleMatrix2D Avl1 = new SparseRCDoubleMatrix2D(nvl, ng, util.irange(nvl), ivl, pc.toArray(), false, false, false);
			DoubleMatrix2D Avl2 = new SparseRCDoubleMatrix2D(nvl, ng, util.irange(nvl), ivl, qc.toArray(), false, false, false);
			Avl = DoubleFactory2D.sparse.appendColumns(Avl1, Avl2);
			lvl = DoubleFactory1D.dense.make(nvl);
			uvl = lvl.copy();
		} else {
			Avl = DoubleFactory2D.sparse.make(0, 2*ng);
			lvl = DoubleFactory1D.dense.make(0);
			uvl = DoubleFactory1D.dense.make(0);
		}

		return new AbstractMatrix[] {Avl, lvl, uvl, IntFactory1D.dense.make(ivl)};
	}
}
