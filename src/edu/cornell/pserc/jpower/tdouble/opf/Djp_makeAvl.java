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

package edu.cornell.pserc.jpower.tdouble.opf;

import cern.colt.matrix.AbstractMatrix;
import cern.colt.matrix.tdouble.DoubleFactory1D;
import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.SparseRCDoubleMatrix2D;
import cern.colt.matrix.tint.IntFactory1D;

import static cern.colt.util.tdouble.Util.ifunc;
import static cern.colt.util.tdouble.Util.dfunc;
import static cern.colt.util.tdouble.Util.nonzero;
import static cern.colt.util.tdouble.Util.intm;
import static cern.colt.util.tdouble.Util.any;
import static cern.colt.util.tdouble.Util.irange;

import edu.cornell.pserc.jpower.tdouble.jpc.Gen;

import static edu.cornell.pserc.jpower.tdouble.opf.Djp_jp_isload.isload;

/**
 * Construct linear constraints for constant power factor var loads.
 *
 * @author Ray Zimmerman
 * @author Richard Lincoln
 *
 */
public class Djp_makeAvl {

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
	public static AbstractMatrix[] makeAvl(double baseMVA, Gen gen) {
		int ng, nvl;
		int[] ivl;
		DoubleMatrix1D Pg, Qg, Pmin, Qmin, Qmax, Qlim,
				lvl, uvl, xx, yy, pftheta, pc, qc;
		DoubleMatrix2D Avl, Avl1, Avl2;

		/* data dimensions */
		ng = gen.size();	// number of dispatchable injections
		Pg = gen.Pg.copy().assign(dfunc.div(baseMVA));
		Qg = gen.Qg.copy().assign(dfunc.div(baseMVA));
		Pmin = gen.Pmin.copy().assign(dfunc.div(baseMVA));
		Qmin = gen.Qmin.copy().assign(dfunc.div(baseMVA));
		Qmax = gen.Qmax.copy().assign(dfunc.div(baseMVA));

		/*
		 * Find out if any of these "generators" are actually dispatchable loads.
		 * (see 'help isload' for details on what constitutes a dispatchable load)
		 * Dispatchable loads are modeled as generators with an added constant
		 * power factor constraint. The power factor is derived from the original
		 * value of Pmin and either Qmin (for inductive loads) or Qmax (for capacitive
		 * loads). If both Qmin and Qmax are zero, this implies a unity power factor
		 * without the need for an additional constraint.
		 */

		ivl = nonzero( isload(gen).assign(intm( Qmax.copy().assign(dfunc.equals(0)) ).assign(ifunc.not).assign(intm( Qmin.copy().assign(dfunc.equals(0)) ).assign(ifunc.not), ifunc.or), ifunc.and) );
		nvl = ivl.length;	// number of dispatchable loads

		/* at least one of the Q limits must be zero (corresponding to Pmax == 0) */
		if (any( intm( Qmin.viewSelection(ivl).copy().assign(dfunc.equals(0)) ).assign(ifunc.not).assign(intm( Qmax.viewSelection(ivl).copy().assign(dfunc.equals(0)) ).assign(ifunc.not), ifunc.and) ));
			System.err.println("makeAvl: either Qmin or Qmax must be equal to zero for each dispatchable load.");
			// TODO: throw invalid value exception

		/*
		 * Initial values of PG and QG must be consistent with specified power factor
		 * This is to prevent a user from unknowingly using a case file which would
		 * have defined a different power factor constraint under a previous version
		 * which used PG and QG to define the power factor.
		 */

		Qlim = Qmin.viewSelection(ivl).copy().assign(dfunc.equals(0)).assign(Qmax.viewSelection(ivl), dfunc.mult);
		Qlim.assign(Qmax.viewSelection(ivl).copy().assign(dfunc.equals(0)).assign(Qmin.viewSelection(ivl), dfunc.mult), dfunc.plus);


		if (any(Qg.viewSelection(ivl).copy().assign(Pg.viewSelection(ivl), dfunc.minus).assign(Qlim.copy().assign(Pmin.viewSelection(ivl), dfunc.div), dfunc.mult).assign(dfunc.abs)))
			System.out.println("makeAvl: %s\n" +
					"For a dispatchable load, PG and QG must be consistent" +
					"with the power factor defined by PMIN and the Q limits.");

		/* make Avl, lvl, uvl, for lvl <= Avl * [Pg; Qg] <= uvl */
		if (nvl > 0) {
			xx = Pmin.viewSelection(ivl).copy();
			yy = Qlim.copy();
			pftheta = yy.copy().assign(xx, dfunc.atan2);
			pc = pftheta.copy().assign(dfunc.sin);
			qc = pftheta.copy().assign(dfunc.cos).assign(dfunc.neg);

			Avl1 = new SparseRCDoubleMatrix2D(nvl, ng, irange(nvl), ivl, pc.toArray(), false, false, false);
			Avl2 = new SparseRCDoubleMatrix2D(nvl, ng, irange(nvl), ivl, qc.toArray(), false, false, false);
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
