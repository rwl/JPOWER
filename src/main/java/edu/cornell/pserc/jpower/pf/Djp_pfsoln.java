/*
 * Copyright (C) 1996-2010 Power System Engineering Research Center
 * Copyright (C) 2010-2011 Richard Lincoln
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package edu.cornell.pserc.jpower.pf;

import cern.colt.matrix.tdcomplex.DComplexMatrix1D;
import cern.colt.matrix.tdcomplex.DComplexMatrix2D;
import cern.colt.matrix.tdouble.DoubleFactory1D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.impl.SparseRCDoubleMatrix2D;
import cern.colt.matrix.tint.IntFactory1D;
import cern.colt.matrix.tint.IntMatrix1D;

import edu.cornell.pserc.jpower.jpc.Branch;
import edu.cornell.pserc.jpower.jpc.Bus;
import edu.cornell.pserc.jpower.jpc.Gen;

import static edu.emory.mathcs.utils.Utils.ifunc;
import static edu.emory.mathcs.utils.Utils.dfunc;
import static edu.emory.mathcs.utils.Utils.cfunc;
import static edu.emory.mathcs.utils.Utils.nonzero;
import static edu.emory.mathcs.utils.Utils.irange;
import static edu.emory.mathcs.utils.Utils.EPS;

/**
 * Updates bus, gen, branch data structures to match power flow soln.
 *
 * @author Ray Zimmerman
 * @author Richard Lincoln
 *
 */
public class Djp_pfsoln {

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
	public static Object[] pfsoln(double baseMVA, Bus bus0, Gen gen0, Branch branch0,
			DComplexMatrix2D Ybus, DComplexMatrix2D Yf, DComplexMatrix2D Yt, DComplexMatrix1D V, int ref, int[] pv, int[] pq) {
		Bus bus;
		Gen gen;
		Branch branch;
		int nb, ngon;
		int[] on, gbus, ig, out, br;
		double Pg_o;
		IntMatrix1D ggbus, refgen;
		DoubleMatrix1D Va, ngb, ngg, Qg_tot, Qg_min, Qg_max, Qg_on, Qg_save, Qg;
		DComplexMatrix1D Sg, Sf, St;
		SparseRCDoubleMatrix2D Cg, Cmin, Cmax;

		/* initialize return values */
		bus = bus0.copy();
		gen = gen0.copy();
		branch = branch0.copy();

		/* ----- update bus voltages ----- */
		bus.Vm.assign(V.copy().assign(cfunc.abs).getRealPart());
		Va = V.copy().assign(cfunc.arg).getRealPart();
		bus.Va.assign(Va.assign(dfunc.mult(180)).assign(dfunc.div(Math.PI)));

		/* ----- update Qg for all gens and Pg for swing bus ----- */
		// generator info
		on = nonzero(gen.gen_status);	// which generators are on?
		ggbus = gen.gen_bus.viewSelection(on).copy();
		gbus = ggbus.toArray();
		refgen = IntFactory1D.dense.make(nonzero( ggbus.assign(ifunc.equals(ref)) ));

		/* compute total injected bus powers */
		Sg = Ybus.viewSelection(gbus, null).zMult(V, null);
		Sg.assign(cfunc.conj).assign(V.viewSelection(gbus), cfunc.mult);

		/* update Qg for all generators */
		gen.Qg.assign(0);					// zero out all Qg
		gen.Qg.viewSelection(on).assign(Sg.copy().getImaginaryPart().assign(dfunc.mult(baseMVA)));	// inj Q
		gen.Qg.viewSelection(on).assign(bus.Qd.viewSelection(gbus), dfunc.plus);			// + local Qd

		/* ... at this point any buses with more than one generator will have
		 * the total Q dispatch for the bus assigned to each generator. This
		 * must be split between them. We do it first equally, then in proportion
		 * to the reactive range of the generator.
		 */
		if (on.length > 0) {
			// build connection matrix, element i, j is 1 if gen on(i) at bus j is ON
			nb = bus.size();
			ngon = on.length;
			Cg = new SparseRCDoubleMatrix2D(ngon, nb, irange(ngon), gbus, 1, false, false);

			// divide Qg by number of generators at the bus to distribute equally
			ngb = DoubleFactory1D.sparse.make(nb);
			for (int k = 0; k < nb; k++)
				ngb.set(k, Cg.viewColumn(k).zSum());
			ngg = Cg.zMult(ngb, null);	// ngon x 1, number of gens at this gen's bus
			gen.Qg.viewSelection(on).assign(ngg, dfunc.div);

			// divide proportionally
			Cmin = new SparseRCDoubleMatrix2D(ngon, nb,
					irange(ngon), gbus, gen.Qmin.viewSelection(on).toArray(), false, false, false);
			Cmax = new SparseRCDoubleMatrix2D(ngon, nb,
					irange(ngon), gbus, gen.Qmax.viewSelection(on).toArray(), false, false, false);
			// nb x 1 vector of total Qg at each bus
			Qg_tot = Cg.viewDice().zMult(gen.Qg.viewSelection(on), null);
			Qg_min = DoubleFactory1D.sparse.make(nb);	// nb x 1 vector of min total Qg at each bus
			Qg_max = DoubleFactory1D.sparse.make(nb);	// nb x 1 vector of max total Qg at each bus
			for (int k = 0; k < nb; k++) {
				Qg_min.set(k, Cmin.viewColumn(k).zSum());
				Qg_max.set(k, Cmax.viewColumn(k).zSum());
			}
			// gens at buses with Qg range = 0
			ig = nonzero( Cg.zMult(Qg_min, null).assign(Cg.zMult(Qg_max, null), dfunc.equals) );
			Qg_on = gen.Qg.viewSelection(on);
			Qg_save = Qg_on.viewSelection(ig).copy();

			Qg = Cg.zMult(Qg_tot.assign(Qg_min, dfunc.minus).assign(Qg_max.assign(Qg_min, dfunc.minus).assign(dfunc.plus(EPS)), dfunc.div), null);
			Qg.assign(gen.Qmax.viewSelection(on).copy().assign(gen.Qmin.viewSelection(on), dfunc.minus), dfunc.mult);                    //   ^ avoid div by 0
			gen.Qg.viewSelection(on).assign(gen.Qmin.viewSelection(on)).assign(Qg, dfunc.plus);

			Qg_on.viewSelection(ig).assign(Qg_save);
		}				// (terms are mult by 0 anyway)

		// update Pg for swing bus
		gen.Pg.set(on[refgen.get(0)], Sg.getRealPart().get(refgen.get(0)) * baseMVA + bus.Pd.get(ref));	// inj P + local Pd
		if (refgen.size() > 1) {	// more than one generator at the ref bus
			// subtract off what is generated by other gens at this bus
			Pg_o = gen.Pg.viewSelection(on).viewSelection(refgen.viewPart(1, (int) (refgen.size() - 1)).toArray()).zSum();
			gen.Pg.set(on[refgen.get(0)], gen.Pg.get(on[refgen.get(0)]) - Pg_o);
		}

		/* ----- update/compute branch power flows ----- */
		out = nonzero(branch.br_status.copy().assign(ifunc.equals(0)));	// out-of-service branches
		br  = nonzero(branch.br_status);									// in-service branches
		// complex power at "from" bus
		Sf = Yf.viewSelection(br, null).zMult(V, null).assign(cfunc.conj);
		Sf.assign(V.viewSelection(branch.f_bus.viewSelection(br).toArray()), cfunc.mult);
		Sf.assign(cfunc.mult(baseMVA));
		// complex power injected at "to" bus
		St = Yt.viewSelection(br, null).zMult(V, null).assign(cfunc.conj);
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

		return new Object[] {bus, gen, branch};
	}

}
