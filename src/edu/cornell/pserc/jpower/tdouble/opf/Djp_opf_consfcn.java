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
import cern.jet.math.tdcomplex.DComplexFunctions;
import cern.jet.math.tdouble.DoubleFunctions;
import edu.cornell.pserc.jips.tdouble.ConstraintEvaluator;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_branch;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_bus;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_gen;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_jpc;
import edu.cornell.pserc.jpower.tdouble.opf.Djp_opf_model.Set;
import edu.cornell.pserc.jpower.tdouble.pf.Djp_dSbus_dV;
import edu.cornell.pserc.jpower.tdouble.pf.Djp_makeSbus;
import edu.cornell.pserc.util.tdouble.Djp_util;

/**
 * Evaluates nonlinear constraints and their Jacobian for OPF.
 *
 * @author Ray Zimmerman (rz10@cornell.edu)
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class Djp_opf_consfcn implements ConstraintEvaluator {

	private static final Djp_util util = new Djp_util();
	private static final DoubleFunctions dfunc = DoubleFunctions.functions;
	private static final DComplexFunctions cfunc = DComplexFunctions.functions;

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
		this.il = Djp_util.irange(nl);
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

		/* unpack data */
		Djp_jpc jpc = om.get_jpc();
		double baseMVA = jpc.baseMVA;
		Djp_bus bus = jpc.bus;
		Djp_gen gen = jpc.gen;
		Djp_branch branch = jpc.branch;
		Map<String, Set> vv = om.get_idx()[0];

		/* problem dimensions */
		int nb = bus.size();		// number of buses
		int nl = branch.size();		// number of branches

		/* grab Pg & Qg */
		DoubleMatrix1D Pg = x.viewPart(vv.get("Pg").i0, vv.get("Pg").N);	// active generation in p.u.
		DoubleMatrix1D Qg = x.viewPart(vv.get("Qg").i0, vv.get("Qg").N);	// reactive generation in p.u.

		/* put Pg & Qg back in gen */
		gen.Pg.assign(Pg.assign(dfunc.mult(baseMVA)));	// active generation in MW
		gen.Qg.assign(Qg.assign(dfunc.mult(baseMVA)));	// reactive generation in MVAr

		/* rebuild Sbus */
		DComplexMatrix1D Sbus = Djp_makeSbus.jp_makeSbus(baseMVA, bus, gen);

		/* ----- evaluate constraints ----- */

		DoubleMatrix1D Va = DoubleFactory1D.dense.make(nb);
		Va.assign(x.viewPart(vv.get("Va").i0, vv.get("Va").N));
		DoubleMatrix1D Vm = x.viewPart(vv.get("Vm").i0, vv.get("Vm").N).copy();
		DComplexMatrix1D V = util.polar(Vm, Va);

		/* evaluate power flow equations */
		DComplexMatrix1D mis = Ybus.zMult(V, null).assign(cfunc.conj);
		mis.assign(V, cfunc.mult).assign(Sbus, cfunc.minus);

		/* ----- evaluate constraint function values ----- */

		/* first, the equality constraints (power flow) */
		DoubleMatrix1D g = DoubleFactory1D.dense.make(new DoubleMatrix1D[] {
			mis.getRealPart(),			// active power mismatch for all buses
			mis.getImaginaryPart()		// reactive power mismatch for all buses
		});

		/* then, the inequality constraints (branch flow limits) */
		DoubleMatrix1D h;
		if (nl > 0) {
			DoubleMatrix1D flow_max = branch.rate_a.viewSelection(il).copy().assign(dfunc.div(baseMVA)).assign(dfunc.square);
			flow_max.assign(dfunc.isEqual(0), Double.POSITIVE_INFINITY);

			if (jpopt.get("OPF_FLOW_LIM") == 2) {	// current magnitude limit, |I|
				DComplexMatrix1D If = Yf.zMult(V, null);
				DComplexMatrix1D It = Yt.zMult(V, null);
				h = DoubleFactory1D.dense.append(
						If.assign(If.copy().assign(cfunc.conj), cfunc.mult).getRealPart().assign(flow_max, dfunc.minus),	// branch current limits (from bus)
						It.assign(It.copy().assign(cfunc.conj), cfunc.mult).getRealPart().assign(flow_max, dfunc.minus)		// branch current limits (to bus)
				);
			} else {
				/* compute branch power flows */
				// complex power injected at "from" bus (p.u.)
				DComplexMatrix1D Sf = V.viewSelection( branch.f_bus.viewSelection(il).toArray() ).assign(Yf.zMult(V, null).assign(cfunc.conj), cfunc.mult);
				// complex power injected at "to" bus (p.u.)
				DComplexMatrix1D St = V.viewSelection( branch.t_bus.viewSelection(il).toArray() ).assign(Yt.zMult(V, null).assign(cfunc.conj), cfunc.mult);
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


	@SuppressWarnings("static-access")
	public DoubleMatrix2D[] dgh(DoubleMatrix1D x) {

		/* unpack data */
		Djp_jpc jpc = om.get_jpc();
		Djp_bus bus = jpc.bus;
		Djp_gen gen = jpc.gen;
		Djp_branch branch = jpc.branch;
		Map<String, Set> vv = om.get_idx()[0];

		/* problem dimensions */
		int nb = bus.size();		// number of buses
		int ng = gen.size();		// number of dispatchable injections
		int nxyz = (int) x.size();	// total number of control vars of all types

		int nl2 = il.length;		// number of constrained lines

		DoubleMatrix1D Va = DoubleFactory1D.dense.make(nb);
		Va.assign(x.viewPart(vv.get("Va").i0, vv.get("Va").N));
		DoubleMatrix1D Vm = x.viewPart(vv.get("Vm").i0, vv.get("Vm").N).copy();
		DComplexMatrix1D V = util.polar(Vm, Va);


		/* ----- evaluate partials of constraints ----- */

		/* index ranges */
		int[] iVa = util.irange(vv.get("Va").i0, vv.get("Va").iN);
		int[] iVm = util.irange(vv.get("Vm").i0, vv.get("Vm").iN);
		int[] iPg = util.irange(vv.get("Pg").i0, vv.get("Pg").iN);
		int[] iQg = util.irange(vv.get("Qg").i0, vv.get("Qg").iN);

		/* compute partials of injected bus powers */
		DComplexMatrix2D[] dSbus_dV = Djp_dSbus_dV.jp_dSbus_dV(Ybus, V);	// w.r.t. V
		DComplexMatrix2D dSbus_dVm = dSbus_dV[0], dSbus_dVa = dSbus_dV[1];
		// Pbus w.r.t. Pg, Qbus w.r.t. Qg
		DoubleMatrix2D neg_Cg = new SparseRCDoubleMatrix2D(nb, ng, gen.gen_bus.toArray(), util.irange(ng), -1, false, false);

		/* construct Jacobian of equality constraints (power flow) and transpose it */
		DoubleMatrix2D dg = DoubleFactory2D.sparse.make(nxyz, 2*nb);
		DComplexMatrix2D dgX1 = DComplexFactory2D.sparse.appendColumns(dSbus_dVa, dSbus_dVm);
		int[] idg = util.icat(iVa, util.icat(iVm, util.icat(iPg, iQg)));
		dg.viewSelection(idg, null).assign( DoubleFactory2D.sparse.compose(new DoubleMatrix2D[][] {
				{dgX1.getRealPart(), neg_Cg, DoubleFactory2D.sparse.make(nb, ng)},			// P mismatch w.r.t Va, Vm, Pg, Qg
				{dgX1.getImaginaryPart(), DoubleFactory2D.sparse.make(nb, ng), neg_Cg} }).viewDice() );	// Q mismatch w.r.t Va, Vm, Pg, Qg

		DoubleMatrix2D dh;
		if (nl2 > 0) {
			/* compute partials of Flows w.r.t. V */
			DComplexMatrix2D dFf_dVa, dFf_dVm, dFt_dVa, dFt_dVm;
			DComplexMatrix1D Ff, Ft;
			if (jpopt.get("OPF_FLOW_LIM") == 2) {	// current
				AbstractMatrix[] dIbr_dV = Djp_dIbr_dV.jp_dIbr_dV(branch.copy(il), Yf, Yt, V);
				dFf_dVa = (DComplexMatrix2D) dIbr_dV[0];
				dFf_dVm = (DComplexMatrix2D) dIbr_dV[1];
				dFt_dVa = (DComplexMatrix2D) dIbr_dV[2];
				dFt_dVm = (DComplexMatrix2D) dIbr_dV[3];
				Ff = (DComplexMatrix1D) dIbr_dV[4];
				Ft = (DComplexMatrix1D) dIbr_dV[5];
			} else {								// power
				AbstractMatrix[] dSbr_dV = Djp_dSbr_dV.jp_dSbr_dV(branch.copy(il), Yf, Yt, V);
				dFf_dVa = (DComplexMatrix2D) dSbr_dV[0];
				dFf_dVm = (DComplexMatrix2D) dSbr_dV[1];
				dFt_dVa = (DComplexMatrix2D) dSbr_dV[2];
				dFt_dVm = (DComplexMatrix2D) dSbr_dV[3];
				Ff = (DComplexMatrix1D) dSbr_dV[4];
				Ft = (DComplexMatrix1D) dSbr_dV[5];
			}
			if (jpopt.get("OPF_FLOW_LIM") == 1) {	// real part of flow (active power)
				dFf_dVa = util.complex(dFf_dVa.getRealPart(), null);
				dFf_dVm = util.complex(dFf_dVm.getRealPart(), null);
				dFt_dVa = util.complex(dFt_dVa.getRealPart(), null);
				dFt_dVm = util.complex(dFt_dVm.getRealPart(), null);
				Ff = util.complex(Ff.getRealPart(), null);
				Ft = util.complex(Ft.getRealPart(), null);
			}

			/* squared magnitude of flow (of complex power or current, or real power) */
			DoubleMatrix2D[] dAbr_dV = Djp_dAbr_dV.jp_dAbr_dV(dFf_dVa, dFf_dVm, dFt_dVa, dFt_dVm, Ff, Ft);
			DoubleMatrix2D df_dVa = dAbr_dV[0], df_dVm = dAbr_dV[1], dt_dVa = dAbr_dV[2], dt_dVm = dAbr_dV[3];

			/* construct Jacobian of inequality constraints (branch limits)
			 * and transpose it so fmincon likes it
			 */
			dh = DoubleFactory2D.sparse.make(nxyz, 2 * nl2);
			dh.viewSelection(util.icat(iVa, iVm), null).assign(DoubleFactory2D.sparse.compose(new DoubleMatrix2D[][] {
					{df_dVa, df_dVm},		// "from" flow limit
					{dt_dVa, dt_dVm}		// "to" flow limit
			}));
		} else {
			dh = DoubleFactory2D.sparse.make(nxyz, 0);
		}

		return new DoubleMatrix2D[] {dh, dg};
	}

}
