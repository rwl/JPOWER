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

package edu.cornell.pserc.jpower.opf;

import java.util.Map;

import cern.colt.matrix.AbstractMatrix;
import cern.colt.matrix.tdcomplex.DComplexFactory2D;
import cern.colt.matrix.tdcomplex.DComplexMatrix1D;
import cern.colt.matrix.tdcomplex.DComplexMatrix2D;
import cern.colt.matrix.tdouble.DoubleFactory1D;
import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.SparseRCDoubleMatrix2D;

import static edu.emory.mathcs.utils.Utils.dfunc;
import static edu.emory.mathcs.utils.Utils.cfunc;
import static edu.emory.mathcs.utils.Utils.irange;
import static edu.emory.mathcs.utils.Utils.icat;
import static edu.emory.mathcs.utils.Utils.polar;
import static edu.emory.mathcs.utils.Utils.complex;

import edu.cornell.pserc.jips.ConstraintEvaluator;
import edu.cornell.pserc.jpower.jpc.Branch;
import edu.cornell.pserc.jpower.jpc.Bus;
import edu.cornell.pserc.jpower.jpc.Gen;
import edu.cornell.pserc.jpower.jpc.JPC;
import edu.cornell.pserc.jpower.opf.Djp_opf_model.Set;

import static edu.cornell.pserc.jpower.pf.Djp_dSbus_dV.dSbus_dV;
import static edu.cornell.pserc.jpower.pf.Djp_makeSbus.makeSbus;

/**
 * Evaluates nonlinear constraints and their Jacobian for OPF.
 *
 * @author Ray Zimmerman
 * @author Richard Lincoln
 *
 */
public class Djp_opf_consfcn implements ConstraintEvaluator {

	private Djp_opf_model om;
	private DComplexMatrix2D Ybus;
	private DComplexMatrix2D Yf;
	private DComplexMatrix2D Yt;
	private Map<String, Double> jpopt;
	private int[] il;

	/**
	 *
	 * @param x optimization vector
	 * @param om OPF model object
	 * @param Ybus bus admittance matrix
	 * @param Yf admittance matrix for "from" end of constrained branches
	 * @param Yt admittance matrix for "to" end of constrained branches
	 * @param jpopt JPOWER options vector
	 */
	public Djp_opf_consfcn(Djp_opf_model om, DComplexMatrix2D Ybus, DComplexMatrix2D Yf, DComplexMatrix2D Yt,
			Map<String, Double> jpopt) {
		super();
		this.om = om;
		this.Ybus = Ybus;
		this.Yf = Yf;
		this.Yt = Yt;
		this.jpopt = jpopt;
		int nl = om.get_jpc().branch.size();	// all lines have limits by default
		this.il = irange(nl);
	}

	/**
	 *
	 * @param x optimization vector
	 * @param om OPF model object
	 * @param Ybus bus admittance matrix
	 * @param Yf admittance matrix for "from" end of constrained branches
	 * @param Yt admittance matrix for "to" end of constrained branches
	 * @param jpopt JPOWER options vector
	 * @param il vector of branch indices corresponding to
	 * branches with flow limits (all others are assumed to be
	 * unconstrained). The default is [1:nl] (all branches).
	 * YF and YT contain only the rows corresponding to IL.
	 */
	public Djp_opf_consfcn(Djp_opf_model om, DComplexMatrix2D Ybus, DComplexMatrix2D Yf, DComplexMatrix2D Yt,
			Map<String, Double> jpopt, int[] il) {
		super();
		this.om = om;
		this.Ybus = Ybus;
		this.Yf = Yf;
		this.Yt = Yt;
		this.jpopt = jpopt;
		this.il = il;
	}

	@SuppressWarnings("static-access")
	public DoubleMatrix1D[] gh(DoubleMatrix1D x) {
		int nb, nl;
		double baseMVA;
		JPC jpc;
		Bus bus;
		Gen gen;
		Branch branch;
		Map<String, Set> vv;
		DoubleMatrix1D Pg, Qg, Va, Vm, g, h, flow_max;
		DComplexMatrix1D Sbus, V, mis, If, It, Sf, St;

		/* unpack data */
		jpc = om.get_jpc();
		baseMVA = jpc.baseMVA;
		bus = jpc.bus;
		gen = jpc.gen;
		branch = jpc.branch;
		vv = om.get_idx()[0];

		/* problem dimensions */
		nb = bus.size();		// number of buses
		nl = branch.size();		// number of branches

		/* grab Pg & Qg */
		Pg = x.viewPart(vv.get("Pg").i0, vv.get("Pg").N);	// active generation in p.u.
		Qg = x.viewPart(vv.get("Qg").i0, vv.get("Qg").N);	// reactive generation in p.u.

		/* put Pg & Qg back in gen */
		gen.Pg.assign(Pg.assign(dfunc.mult(baseMVA)));	// active generation in MW
		gen.Qg.assign(Qg.assign(dfunc.mult(baseMVA)));	// reactive generation in MVAr

		/* rebuild Sbus */
		Sbus = makeSbus(baseMVA, bus, gen);

		/* ----- evaluate constraints ----- */

		Va = DoubleFactory1D.dense.make(nb);
		Va.assign(x.viewPart(vv.get("Va").i0, vv.get("Va").N));
		Vm = x.viewPart(vv.get("Vm").i0, vv.get("Vm").N).copy();
		V = polar(Vm, Va);

		/* evaluate power flow equations */
		mis = Ybus.zMult(V, null).assign(cfunc.conj);
		mis.assign(V, cfunc.mult).assign(Sbus, cfunc.minus);

		/* ----- evaluate constraint function values ----- */

		/* first, the equality constraints (power flow) */
		g = DoubleFactory1D.dense.make(new DoubleMatrix1D[] {
			mis.getRealPart(),			// active power mismatch for all buses
			mis.getImaginaryPart()		// reactive power mismatch for all buses
		});

		/* then, the inequality constraints (branch flow limits) */
		if (nl > 0) {
			flow_max = branch.rate_a.viewSelection(il).copy().assign(dfunc.div(baseMVA)).assign(dfunc.square);
			flow_max.assign(dfunc.isEqual(0), Double.POSITIVE_INFINITY);

			if (jpopt.get("OPF_FLOW_LIM") == 2) {	// current magnitude limit, |I|
				If = Yf.zMult(V, null);
				It = Yt.zMult(V, null);
				h = DoubleFactory1D.dense.append(
						If.assign(If.copy().assign(cfunc.conj), cfunc.mult).getRealPart().assign(flow_max, dfunc.minus),	// branch current limits (from bus)
						It.assign(It.copy().assign(cfunc.conj), cfunc.mult).getRealPart().assign(flow_max, dfunc.minus)		// branch current limits (to bus)
				);
			} else {
				/* compute branch power flows */
				// complex power injected at "from" bus (p.u.)
				Sf = V.viewSelection( branch.f_bus.viewSelection(il).toArray() ).assign(Yf.zMult(V, null).assign(cfunc.conj), cfunc.mult);
				// complex power injected at "to" bus (p.u.)
				St = V.viewSelection( branch.t_bus.viewSelection(il).toArray() ).assign(Yt.zMult(V, null).assign(cfunc.conj), cfunc.mult);
				if (jpopt.get("OPF_FLOW_LIM") == 1) {	// active power limit, P (Pan Wei)
					h = DoubleFactory1D.dense.append(
							Sf.getRealPart().assign(dfunc.square).assign(flow_max, dfunc.minus),
							St.getRealPart().assign(dfunc.square).assign(flow_max, dfunc.minus)
					);
				} else {	// apparent power limit, |S|
					h = DoubleFactory1D.dense.append(
							Sf.assign(Sf.copy().assign(cfunc.conj), cfunc.mult).getRealPart().assign(flow_max, dfunc.minus),	// branch apparent power limits (from bus)
							St.assign(St.copy().assign(cfunc.conj), cfunc.mult).getRealPart().assign(flow_max, dfunc.minus)		// branch apparent power limits (to bus)
					);
				}
			}
		} else {
			h = DoubleFactory1D.dense.make(0);
		}

		return new DoubleMatrix1D[] {g, h};
	}


	public DoubleMatrix2D[] dgh(DoubleMatrix1D x) {
		int nb, ng, nxyz, nl2;
		int[] iVa, iVm, iPg, iQg, idg;
		JPC jpc;
		Bus bus;
		Gen gen;
		Branch branch;
		Map<String, Set> vv;

		AbstractMatrix[] dIbr_dV, dSbr_dV;
		DoubleMatrix1D Va, Vm;
		DoubleMatrix2D neg_Cg, dg, dh, df_dVa, df_dVm, dt_dVa, dt_dVm;
		DoubleMatrix2D[] dAbr_dV;

		DComplexMatrix1D Ff, Ft, V;
		DComplexMatrix2D dSbus_dVm, dSbus_dVa, dgX1, dFf_dVa, dFf_dVm, dFt_dVa, dFt_dVm;
		DComplexMatrix2D[] dSbus_dV;


		/* unpack data */
		jpc = om.get_jpc();
		bus = jpc.bus;
		gen = jpc.gen;
		branch = jpc.branch;
		vv = om.get_idx()[0];

		/* problem dimensions */
		nb = bus.size();		// number of buses
		ng = gen.size();		// number of dispatchable injections
		nxyz = (int) x.size();	// total number of control vars of all types

		nl2 = il.length;		// number of constrained lines

		Va = DoubleFactory1D.dense.make(nb);
		Va.assign(x.viewPart(vv.get("Va").i0, vv.get("Va").N));
		Vm = x.viewPart(vv.get("Vm").i0, vv.get("Vm").N).copy();
		V = polar(Vm, Va);


		/* ----- evaluate partials of constraints ----- */

		/* index ranges */
		iVa = irange(vv.get("Va").i0, vv.get("Va").iN);
		iVm = irange(vv.get("Vm").i0, vv.get("Vm").iN);
		iPg = irange(vv.get("Pg").i0, vv.get("Pg").iN);
		iQg = irange(vv.get("Qg").i0, vv.get("Qg").iN);

		/* compute partials of injected bus powers */
		dSbus_dV = dSbus_dV(Ybus, V);	// w.r.t. V
		dSbus_dVm = dSbus_dV[0]; dSbus_dVa = dSbus_dV[1];
		// Pbus w.r.t. Pg, Qbus w.r.t. Qg
		neg_Cg = new SparseRCDoubleMatrix2D(nb, ng, gen.gen_bus.toArray(), irange(ng), -1, false, false);

		/* construct Jacobian of equality constraints (power flow) and transpose it */
		dg = DoubleFactory2D.sparse.make(nxyz, 2*nb);
		dgX1 = DComplexFactory2D.sparse.appendColumns(dSbus_dVa, dSbus_dVm);
		idg = icat(iVa, icat(iVm, icat(iPg, iQg)));
		dg.viewSelection(idg, null).assign( DoubleFactory2D.sparse.compose(new DoubleMatrix2D[][] {
				{dgX1.getRealPart(), neg_Cg, DoubleFactory2D.sparse.make(nb, ng)},			// P mismatch w.r.t Va, Vm, Pg, Qg
				{dgX1.getImaginaryPart(), DoubleFactory2D.sparse.make(nb, ng), neg_Cg} }).viewDice() );	// Q mismatch w.r.t Va, Vm, Pg, Qg

		if (nl2 > 0) {
			/* compute partials of Flows w.r.t. V */
			if (jpopt.get("OPF_FLOW_LIM") == 2) {	// current
				dIbr_dV = Djp_dIbr_dV.dIbr_dV(branch.copy(il), Yf, Yt, V);
				dFf_dVa = (DComplexMatrix2D) dIbr_dV[0];
				dFf_dVm = (DComplexMatrix2D) dIbr_dV[1];
				dFt_dVa = (DComplexMatrix2D) dIbr_dV[2];
				dFt_dVm = (DComplexMatrix2D) dIbr_dV[3];
				Ff = (DComplexMatrix1D) dIbr_dV[4];
				Ft = (DComplexMatrix1D) dIbr_dV[5];
			} else {								// power
				dSbr_dV = Djp_dSbr_dV.dSbr_dV(branch.copy(il), Yf, Yt, V);
				dFf_dVa = (DComplexMatrix2D) dSbr_dV[0];
				dFf_dVm = (DComplexMatrix2D) dSbr_dV[1];
				dFt_dVa = (DComplexMatrix2D) dSbr_dV[2];
				dFt_dVm = (DComplexMatrix2D) dSbr_dV[3];
				Ff = (DComplexMatrix1D) dSbr_dV[4];
				Ft = (DComplexMatrix1D) dSbr_dV[5];
			}
			if (jpopt.get("OPF_FLOW_LIM") == 1) {	// real part of flow (active power)
				dFf_dVa = complex(dFf_dVa.getRealPart(), null);
				dFf_dVm = complex(dFf_dVm.getRealPart(), null);
				dFt_dVa = complex(dFt_dVa.getRealPart(), null);
				dFt_dVm = complex(dFt_dVm.getRealPart(), null);
				Ff = complex(Ff.getRealPart(), null);
				Ft = complex(Ft.getRealPart(), null);
			}

			/* squared magnitude of flow (of complex power or current, or real power) */
			dAbr_dV = Djp_dAbr_dV.dAbr_dV(dFf_dVa, dFf_dVm, dFt_dVa, dFt_dVm, Ff, Ft);
			df_dVa = dAbr_dV[0]; df_dVm = dAbr_dV[1]; dt_dVa = dAbr_dV[2]; dt_dVm = dAbr_dV[3];

			/* construct Jacobian of inequality constraints (branch limits)
			 * and transpose it so fmincon likes it
			 */
			dh = DoubleFactory2D.sparse.make(nxyz, 2 * nl2);
			dh.viewSelection(icat(iVa, iVm), null).assign(DoubleFactory2D.sparse.compose(new DoubleMatrix2D[][] {
					{df_dVa, df_dVm},		// "from" flow limit
					{dt_dVa, dt_dVm}		// "to" flow limit
			}));
		} else {
			dh = DoubleFactory2D.sparse.make(nxyz, 0);
		}

		return new DoubleMatrix2D[] {dh, dg};
	}

}
