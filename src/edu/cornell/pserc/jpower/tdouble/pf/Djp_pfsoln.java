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

import cern.colt.matrix.tdcomplex.DComplexMatrix1D;
import cern.colt.matrix.tdcomplex.DComplexMatrix2D;
import cern.colt.matrix.tdouble.DoubleFactory1D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.impl.SparseRCDoubleMatrix2D;
import cern.colt.matrix.tint.IntFactory1D;
import cern.colt.matrix.tint.IntMatrix1D;
import cern.jet.math.tdcomplex.DComplexFunctions;
import cern.jet.math.tdouble.DoubleFunctions;
import cern.jet.math.tint.IntFunctions;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_branch;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_bus;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_gen;
import edu.cornell.pserc.util.tdouble.Djp_util;

/**
 * Updates bus, gen, branch data structures to match power flow soln.
 *
 * @author Ray Zimmerman (rz10@cornell.edu)
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class Djp_pfsoln {

	private static final Djp_util util = new Djp_util();
	private static final IntFunctions ifunc = IntFunctions.intFunctions;
	private static final DoubleFunctions dfunc = DoubleFunctions.functions;
	private static final DComplexFunctions cfunc = DComplexFunctions.functions;

	/**
	 * Updates bus, gen, branch data structures to match power flow soln.
	 *
	 * @param baseMVA
	 * @param bus0
	 * @param gen0
	 * @param branch0
	 * @param Ybus
	 * @param Yf
	 * @param Yt
	 * @param V
	 * @param ref
	 * @param pv
	 * @param pq
	 * @return
	 */
	@SuppressWarnings("static-access")
	public static Object[] jp_pfsoln(double baseMVA, Djp_bus bus0, Djp_gen gen0, Djp_branch branch0,
			DComplexMatrix2D Ybus, DComplexMatrix2D Yf, DComplexMatrix2D Yt, DComplexMatrix1D V, int ref, int[] pv, int[] pq) {

		/* initialize return values */
		Djp_bus bus = bus0.copy();
		Djp_gen gen = gen0.copy();
		Djp_branch branch = branch0.copy();

		/* ----- update bus voltages ----- */
		bus.Vm.assign(V.copy().assign(cfunc.abs).getRealPart());
		DoubleMatrix1D Va = V.copy().assign(cfunc.arg).getRealPart();
		bus.Va.assign(Va.assign(dfunc.mult(Math.PI)).assign(dfunc.div(180)));

		/* ----- update Qg for all gens and Pg for swing bus ----- */
		// generator info
		int[] on = util.nonzero(gen.gen_status);	// which generators are on?
		IntMatrix1D ggbus = gen.gen_bus.viewSelection(on);
		int[] gbus = ggbus.toArray();
		IntMatrix1D refgen = IntFactory1D.dense.make(util.nonzero( ggbus.assign(ifunc.equals(ref)) ));

		/* compute total injected bus powers */
		DComplexMatrix1D Sg = Ybus.viewSelection(gbus, null).zMult(V, null);
		Sg.assign(cfunc.conj).assign(V.viewSelection(gbus), cfunc.mult);

		/* update Qg for all generators */
		gen.Qg.assign(0);					// zero out all Qg
		gen.Qg.viewSelection(on).assign(Sg.getImaginaryPart().assign(dfunc.mult(baseMVA)));	// inj Q
		gen.Qg.viewSelection(on).assign(bus.Qd.viewSelection(gbus), dfunc.plus);			// + local Qd

		/* ... at this point any buses with more than one generator will have
		 * the total Q dispatch for the bus assigned to each generator. This
		 * must be split between them. We do it first equally, then in proportion
		 * to the reactive range of the generator.
		 */
		if (on.length > 0) {
			// build connection matrix, element i, j is 1 if gen on(i) at bus j is ON
			int nb = bus.size();
			int ngon = on.length;
			SparseRCDoubleMatrix2D Cg = new SparseRCDoubleMatrix2D(ngon, nb, util.irange(ngon), gbus, 1, false, false);

			// divide Qg by number of generators at the bus to distribute equally
			DoubleMatrix1D ngb = DoubleFactory1D.sparse.make(nb);
			for (int k = 0; k < nb; k++)
				ngb.set(k, Cg.viewColumn(k).zSum());
			DoubleMatrix1D ngg = Cg.zMult(ngb, null);	// ngon x 1, number of gens at this gen's bus
			gen.Qg.viewSelection(on).assign(ngg, dfunc.div);

			// divide proportionally
			SparseRCDoubleMatrix2D Cmin = new SparseRCDoubleMatrix2D(ngon, nb,
					util.irange(ngon), gbus, gen.Qmin.viewSelection(on).toArray(), false, false, false);
			SparseRCDoubleMatrix2D Cmax = new SparseRCDoubleMatrix2D(ngon, nb,
					util.irange(ngon), gbus, gen.Qmax.viewSelection(on).toArray(), false, false, false);
			// nb x 1 vector of total Qg at each bus
			DoubleMatrix1D Qg_tot = Cg.viewDice().zMult(gen.Qg.viewSelection(on), null);
			DoubleMatrix1D Qg_min = DoubleFactory1D.sparse.make(nb);	// nb x 1 vector of min total Qg at each bus
			DoubleMatrix1D Qg_max = DoubleFactory1D.sparse.make(nb);	// nb x 1 vector of max total Qg at each bus
			for (int k = 0; k < nb; k++) {
				Qg_min.set(k, Cmin.viewColumn(k).zSum());
				Qg_max.set(k, Cmax.viewColumn(k).zSum());
			}
			// gens at buses with Qg range = 0
			int[] ig = util.nonzero( Cg.zMult(Qg_min, null).assign(Cg.zMult(Qg_max, null), dfunc.equals) );
			DoubleMatrix1D Qg_on = gen.Qg.viewSelection(on);
			DoubleMatrix1D Qg_save = Qg_on.viewSelection(ig).copy();

			DoubleMatrix1D Qg = Cg.zMult(Qg_tot.assign(Qg_min, dfunc.minus).assign(Qg_max.assign(Qg_min, dfunc.minus).assign(dfunc.plus(util.EPS)), dfunc.div), null);
			Qg.assign(gen.Qmax.viewSelection(on).copy().assign(gen.Qmin.viewSelection(on), dfunc.minus), dfunc.mult);                    //   ^ avoid div by 0
			gen.Qg.viewSelection(on).assign(gen.Qmin.viewSelection(on)).assign(Qg, dfunc.plus);

			Qg_on.assign(Qg_save);
		}				// (terms are mult by 0 anyway)

		// update Pg for swing bus
		gen.Pg.set(on[refgen.get(0)], Sg.getRealPart().get(refgen.get(0)) * baseMVA + bus.Pd.get(ref));	// inj P + local Pd
		if (refgen.size() > 1) {	// more than one generator at the ref bus
			// subtract off what is generated by other gens at this bus
			double Pg_o = gen.Pg.viewSelection(on).viewSelection(refgen.viewPart(1, (int) (refgen.size() - 1)).toArray()).zSum();
			gen.Pg.set(on[refgen.get(0)], gen.Pg.get(on[refgen.get(0)]) - Pg_o);
		}

		/* ----- update/compute branch power flows ----- */
		int[] out = util.nonzero(branch.br_status.copy().assign(ifunc.equals(0)));	// out-of-service branches
		int[] br = util.nonzero(branch.br_status);									// in-service branches
		// complex power at "from" bus
		DComplexMatrix1D Sf = Yf.viewSelection(br, null).zMult(V, null).assign(cfunc.conj);
		Sf.assign(V.viewSelection(branch.f_bus.viewSelection(br).toArray()), cfunc.mult);
		Sf.assign(cfunc.mult(baseMVA));
		// complex power injected at "to" bus
		DComplexMatrix1D St = Yt.viewSelection(br, null).zMult(V, null).assign(cfunc.conj);
		St.assign(V.viewSelection(branch.t_bus.viewSelection(br).toArray()), cfunc.mult);
		St.assign(cfunc.mult(baseMVA));

		branch.Pf.viewSelection(br).assign(Sf.getRealPart());
		branch.Qf.viewSelection(br).assign(Sf.getImaginaryPart());
		branch.Pt.viewSelection(br).assign(St.getRealPart());
		branch.Qt.viewSelection(br).assign(St.getImaginaryPart());

		branch.Pf.viewSelection(out).assign(0);
		branch.Qf.viewSelection(out).assign(0);
		branch.Pt.viewSelection(out).assign(0);
		branch.Qt.viewSelection(out).assign(0);

		return null;
	}
}
