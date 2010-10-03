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
import cern.colt.matrix.tdcomplex.impl.SparseRCDComplexMatrix2D;
import cern.colt.matrix.tdouble.DoubleFactory1D;
import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.SparseRCDoubleMatrix2D;
import cern.colt.matrix.tint.IntMatrix1D;
import cern.colt.util.tdouble.Djp_util;
import cern.jet.math.tdouble.DoubleFunctions;
import cern.jet.math.tint.IntFunctions;
import edu.cornell.pserc.jips.tdouble.HessianEvaluator;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_branch;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_bus;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_gen;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_gencost;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_jpc;
import edu.cornell.pserc.jpower.tdouble.opf.Djp_opf_model.Cost;
import edu.cornell.pserc.jpower.tdouble.opf.Djp_opf_model.Set;

/**
 * Evaluates Hessian of Lagrangian for AC OPF.
 *
 * @author Ray Zimmerman (rz10@cornell.edu)
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class Djp_opf_hessfcn implements HessianEvaluator {

	private static final Djp_util util = new Djp_util();
	private static final DoubleFunctions dfunc = DoubleFunctions.functions;
	private static final IntFunctions ifunc = IntFunctions.intFunctions;

	private static final int POLYNOMIAL = Djp_jpc.POLYNOMIAL;

	private Djp_opf_model om;
	private DComplexMatrix2D Ybus;
	private DComplexMatrix2D Yf;
	private DComplexMatrix2D Yt;
	private Map<String, Double> jpopt;
	private int[] il;
	private double cost_mult;

	public Djp_opf_hessfcn(Djp_opf_model om, DComplexMatrix2D Ybus, DComplexMatrix2D Yf, DComplexMatrix2D Yt,
			Map<String, Double> jpopt) {
		super();
		this.om = om;
		this.Ybus = Ybus;
		this.Yf = Yf;
		this.Yt = Yt;
		this.jpopt = jpopt;
		int nl = om.get_jpc().branch.size();	// all lines have limits by default
		this.il = Djp_util.irange(nl);
		this.cost_mult = 1;
	}

	public Djp_opf_hessfcn(Djp_opf_model om, DComplexMatrix2D Ybus, DComplexMatrix2D Yf, DComplexMatrix2D Yt,
			Map<String, Double> jpopt, int[] il) {
		super();
		this.om = om;
		this.Ybus = Ybus;
		this.Yf = Yf;
		this.Yt = Yt;
		this.jpopt = jpopt;
		this.il = il;
		this.cost_mult = 1;
	}

	public Djp_opf_hessfcn(Djp_opf_model om, DComplexMatrix2D Ybus, DComplexMatrix2D Yf, DComplexMatrix2D Yt,
			Map<String, Double> jpopt, int[] il, double cost_mult) {
		super();
		this.om = om;
		this.Ybus = Ybus;
		this.Yf = Yf;
		this.Yt = Yt;
		this.jpopt = jpopt;
		this.il = il;
		this.cost_mult = cost_mult;
	}

	@SuppressWarnings("static-access")
	public DoubleMatrix2D h(DoubleMatrix1D x, Map<String, DoubleMatrix1D> lambda) {

		/* unpack data */
		Djp_jpc jpc = om.get_jpc();
		double baseMVA = jpc.baseMVA;
		Djp_bus bus = jpc.bus;
		Djp_gen gen = jpc.gen;
		Djp_branch branch = jpc.branch;
		Djp_gencost gencost = jpc.gencost;
		Cost cp = om.get_cost_params();
		DoubleMatrix2D N = cp.N, H = cp.H;
		DoubleMatrix1D Cw = cp.Cw, dd = cp.dd, rh = cp.rh, kk = cp.kk, mm = cp.mm;
		Map<String, Set> vv = om.get_idx()[0];

		/* unpack needed parameters */
		int nb = bus.size();		// number of buses
		int ng = gen.size();		// number of dispatchable injections
		int nxyz = (int) x.size();	// total number of control vars of all types

		int nl2 = il.length;		// number of constrained lines

		/* grab Pg & Qg */
		DoubleMatrix1D Pg = x.viewPart(vv.get("Pg").i0, vv.get("Pg").N);	// active generation in p.u.
		DoubleMatrix1D Qg = x.viewPart(vv.get("Qg").i0, vv.get("Qg").N);	// reactive generation in p.u.

		/* put Pg & Qg back in gen */
		gen.Pg.assign(Pg.assign(dfunc.mult(baseMVA)));	// active generation in MW
		gen.Qg.assign(Qg.assign(dfunc.mult(baseMVA)));	// reactive generation in MVAr

		/* reconstruct V */
		DoubleMatrix1D Va = DoubleFactory1D.dense.make(nb);
		Va.assign(x.viewPart(vv.get("Va").i0, vv.get("Va").N));
		DoubleMatrix1D Vm = x.viewPart(vv.get("Vm").i0, vv.get("Vm").N).copy();
		DComplexMatrix1D V = util.polar(Vm, Va);
		int nxtra = nxyz - 2*nb;
		Djp_gencost pcost = gencost.copy(util.irange(ng));
		Djp_gencost qcost = null;
		if (gencost.size() > ng)
			qcost = gencost.copy(util.irange(ng, 2*ng));

		/* ----- evaluate d2f ----- */

		DoubleMatrix1D d2f_dPg2 = DoubleFactory1D.sparse.make(ng);	// w.r.t. p.u. Pg
		DoubleMatrix1D d2f_dQg2 = DoubleFactory1D.sparse.make(ng);	// w.r.t. p.u. Qg
		int[] ipolp = util.nonzero(pcost.model.copy().assign(ifunc.equals(POLYNOMIAL)));
		d2f_dPg2.assign( Djp_polycost.jp_polycost(pcost.copy(ipolp), Pg.viewSelection(ipolp).copy().assign(dfunc.mult(baseMVA)), 2).assign(dfunc.mult(Math.pow(baseMVA, 2))) );
		if (qcost != null) {	// Qg is not free
			int[] ipolq = util.nonzero(qcost.model.copy().assign(ifunc.equals(POLYNOMIAL)));
			d2f_dQg2.assign( Djp_polycost.jp_polycost(qcost.copy(ipolq), Qg.viewSelection(ipolq).copy().assign(dfunc.mult(baseMVA)), 2).assign(dfunc.mult(Math.pow(baseMVA, 2))) );
		}
		int[] i = util.icat(util.irange(vv.get("Pg").i0, vv.get("Pg").iN), util.irange(vv.get("Qg").i0, vv.get("Qg").iN));
		DoubleMatrix2D d2f = new SparseRCDoubleMatrix2D(nxyz, nxyz, i, i,
				DoubleFactory1D.sparse.append(d2f_dPg2, d2f_dQg2).toArray(), false, false, false);

		/* generalized cost */
		if (N != null) {
			int nw = N.rows();
			DoubleMatrix1D r = N.zMult(x, null).assign(rh, dfunc.minus);	// generalized cost
			int[] iLT = util.nonzero(r.copy().assign(kk.copy().assign(dfunc.neg), dfunc.less));	// below dead zone
			int[] iEQ = util.nonzero( util.intm(r.copy().assign(dfunc.equals(0))).assign(util.intm(kk.copy().assign(dfunc.equals(0))), ifunc.and) );	// dead zone doesn't exist
			int[] iGT = util.nonzero(r.copy().assign(kk, dfunc.less));	// above dead zone
			int[] iND = util.icat(iLT, util.icat(iEQ, iGT));			// rows that are Not in the Dead region
			int[] iL = util.nonzero(dd.copy().assign(dfunc.equals(1)));	// rows using linear function
			int[] iQ = util.nonzero(dd.copy().assign(dfunc.equals(2)));	// rows using quadratic function
			DoubleMatrix2D LL = new SparseRCDoubleMatrix2D(nw, nw, iL, iL, 1, false, false);
			DoubleMatrix2D QQ = new SparseRCDoubleMatrix2D(nw, nw, iQ, iQ, 1, false, false);
			DoubleMatrix1D[] kbar_v = new DoubleMatrix1D[] {
					DoubleFactory1D.dense.make(iLT.length, 1),
					DoubleFactory1D.dense.make(iEQ.length),
					DoubleFactory1D.dense.make(iGT.length, -1)};
			DoubleMatrix1D kbar = new SparseRCDoubleMatrix2D(nw, nw, iND, iND,
					DoubleFactory1D.dense.make(kbar_v).toArray(), false, false, false).zMult(kk, null);
			DoubleMatrix1D rr = r.assign(kbar, dfunc.plus);	// apply non-dead zone shift
			DoubleMatrix2D M = new SparseRCDoubleMatrix2D(nw, nw, iND, iND,
					mm.viewSelection(iND).toArray(), false, false, false);	// dead zone or scale
			DoubleMatrix2D diagrr = DoubleFactory2D.sparse.diagonal(rr);

			/* linear rows multiplied by rr(i), quadratic rows by rr(i)^2 */
			DoubleMatrix1D w = LL.copy().assign(QQ.zMult(diagrr, null), dfunc.plus).zMult(M, null).zMult(rr, null);
			DoubleMatrix1D HwC = H.zMult(w, null).assign(Cw, dfunc.plus);
			DoubleMatrix2D AA = QQ.zMult(diagrr, null).assign(dfunc.mult(2)).assign(LL, dfunc.plus).zMult(M, null).zMult(N.viewDice(), null);
			DoubleMatrix2D diagHwC = DoubleFactory2D.sparse.diagonal(HwC);
			d2f.assign(AA.zMult(H, null).zMult(AA.viewDice(), null).assign(N.viewDice().copy().zMult(M, null).zMult(QQ, null).zMult(diagHwC, null).zMult(N, null), dfunc.plus), dfunc.plus);
		}
		d2f.assign(dfunc.mult(cost_mult));

		/* ----- evaluate Hessian of power balance constraints ----- */

		int nlam = (int) (lambda.get("eqnonlin").size() / 2);
		DoubleMatrix1D lamP = lambda.get("eqnonlin").viewPart(0, nlam);
		DoubleMatrix1D lamQ = lambda.get("eqnonlin").viewPart(nlam, nlam);
		DComplexMatrix2D[] Gp = Djp_d2Sbus_dV2.jp_d2Sbus_dV2(Ybus, V, util.complex(lamP, null));
		DComplexMatrix2D Gpaa = Gp[0], Gpav = Gp[1], Gpva = Gp[2], Gpvv = Gp[3];
		DComplexMatrix2D[] Gq = Djp_d2Sbus_dV2.jp_d2Sbus_dV2(Ybus, V, util.complex(lamQ, null));
		DComplexMatrix2D Gqaa = Gq[0], Gqav = Gq[1], Gqva = Gq[2], Gqvv = Gq[3];

		DoubleMatrix2D d2G_p = DComplexFactory2D.sparse.compose(new DComplexMatrix2D[][] {{Gpaa, Gpav}, {Gpva, Gpvv}}).getRealPart();
		DoubleMatrix2D d2G_q = DComplexFactory2D.sparse.compose(new DComplexMatrix2D[][] {{Gqaa, Gqav}, {Gqva, Gqvv}}).getImaginaryPart();
		DoubleMatrix2D[][] d2G_parts = new DoubleMatrix2D[][] {
				{d2G_p.assign(d2G_q, dfunc.plus), DoubleFactory2D.sparse.make(2*nb, nxtra)},
				{DoubleFactory2D.sparse.make(nxtra, 2*nb + nxtra)} };
		DoubleMatrix2D d2G = DoubleFactory2D.sparse.compose(d2G_parts);

		/* ----- evaluate Hessian of flow constraints ----- */

		int nmu = (int) lambda.get("ineqnonlin").size() / 2;
		DComplexMatrix1D muF = util.complex(lambda.get("ineqnonlin").viewPart(0, nmu), null);
		DComplexMatrix1D muT = util.complex(lambda.get("ineqnonlin").viewPart(nmu, nmu), null);
		DoubleMatrix2D[] Hf, Ht;
		DoubleMatrix2D Hfaa, Hfav, Hfva, Hfvv, Htaa, Htav, Htva, Htvv;
		if (jpopt.get("OPF_FLOW_LIM") == 2) {	// current
			AbstractMatrix[] dIbr_dV = Djp_dIbr_dV.jp_dIbr_dV(branch.copy(il), Yf, Yt, V);
			DComplexMatrix2D dIf_dVa = (DComplexMatrix2D) dIbr_dV[0];
			DComplexMatrix2D dIf_dVm = (DComplexMatrix2D) dIbr_dV[1];
			DComplexMatrix2D dIt_dVa = (DComplexMatrix2D) dIbr_dV[2];
			DComplexMatrix2D dIt_dVm = (DComplexMatrix2D) dIbr_dV[3];
			DComplexMatrix1D If = (DComplexMatrix1D) dIbr_dV[4];
			DComplexMatrix1D It = (DComplexMatrix1D) dIbr_dV[5];
			Hf = Djp_d2AIbr_dV2.jp_d2AIbr_dV2(dIf_dVa, dIf_dVm, If, Yf, V, muF);
			Ht = Djp_d2AIbr_dV2.jp_d2AIbr_dV2(dIt_dVa, dIt_dVm, It, Yt, V, muT);
		} else {
			IntMatrix1D f = branch.f_bus.viewSelection(il);	// list of "from" buses
			IntMatrix1D t = branch.t_bus.viewSelection(il);	// list of "to" buses
			DComplexMatrix2D Cf = new SparseRCDComplexMatrix2D(nl2, nb, util.irange(nl2), f.toArray(), new double[] {1, 0}, false, false);
			DComplexMatrix2D Ct = new SparseRCDComplexMatrix2D(nl2, nb, util.irange(nl2), t.toArray(), new double[] {1, 0}, false, false);
			AbstractMatrix[] dSbr_dV = Djp_dSbr_dV.jp_dSbr_dV(branch.copy(il), Yf, Yt, V);
			DComplexMatrix2D dSf_dVa = (DComplexMatrix2D) dSbr_dV[0];
			DComplexMatrix2D dSf_dVm = (DComplexMatrix2D) dSbr_dV[1];
			DComplexMatrix2D dSt_dVa = (DComplexMatrix2D) dSbr_dV[2];
			DComplexMatrix2D dSt_dVm = (DComplexMatrix2D) dSbr_dV[3];
			DComplexMatrix1D Sf = (DComplexMatrix1D) dSbr_dV[4];
			DComplexMatrix1D St = (DComplexMatrix1D) dSbr_dV[5];
			if (jpopt.get("OPF_FLOW_LIM") == 1) {	// real power
				Hf = Djp_d2ASbr_dV2.jp_d2ASbr_dV2(util.complex(dSf_dVa.getRealPart(), null),
						util.complex(dSf_dVm.getRealPart(), null), util.complex(Sf.getRealPart(), null), Cf, Yf, V, muF);
				Ht = Djp_d2ASbr_dV2.jp_d2ASbr_dV2(util.complex(dSt_dVa.getRealPart(), null),
						util.complex(dSt_dVm.getRealPart(), null), util.complex(St.getRealPart(), null), Ct, Yt, V, muT);
			} else {	// apparent power
				Hf = Djp_d2ASbr_dV2.jp_d2ASbr_dV2(dSf_dVa, dSf_dVm, Sf, Cf, Yf, V, muF);
				Ht = Djp_d2ASbr_dV2.jp_d2ASbr_dV2(dSt_dVa, dSt_dVm, St, Ct, Yt, V, muT);
			}
		}
		Hfaa = Hf[0]; Hfav = Hf[1]; Hfva = Hf[2]; Hfvv = Hf[3];
		Htaa = Ht[0]; Htav = Ht[1]; Htva = Ht[2]; Htvv = Ht[3];
		DoubleMatrix2D d2H_f = DoubleFactory2D.sparse.compose(new DoubleMatrix2D[][] {{Hfaa, Hfav}, {Hfva, Hfvv}});
		DoubleMatrix2D d2H_t = DoubleFactory2D.sparse.compose(new DoubleMatrix2D[][] {{Htaa, Htav}, {Htva, Htvv}});
		DoubleMatrix2D[][] d2H_parts = new DoubleMatrix2D[][] {
				{d2H_f.assign(d2H_t, dfunc.plus), DoubleFactory2D.sparse.make(2*nb, nxtra)},
				{DoubleFactory2D.sparse.make(nxtra, 2*nb + nxtra)} };
		DoubleMatrix2D d2H = DoubleFactory2D.sparse.compose(d2H_parts);

		/* -----  do numerical check using (central) finite differences  ----- */

//		if (false) {
//			int nx = (int) x.size();
//			double step = 1e-05;
//			DoubleMatrix2D num_d2f = DoubleFactory2D.sparse.make(nx, nx);
//			DoubleMatrix2D num_d2G = DoubleFactory2D.sparse.make(nx, nx);
//			DoubleMatrix2D num_d2H = DoubleFactory2D.sparse.make(nx, nx);
//			for (int ii = 0; ii < nx; ii++) {
//				DoubleMatrix1D xp = x.copy();
//				DoubleMatrix1D xm = x.copy();
//				xp.set(ii, x.get(ii) + step/2);
//				xm.set(ii, x.get(ii) - step/2);
//				/* evaluate cost & gradients */
////				fp, dfp = Djp_opf_costfcn.jp_opf_costfcn(xp, om);
////				fm, dfm = Djp_opf_costfcn.jp_opf_costfcn(xm, om);
//				/* evaluate constraints & gradients */
//
//				// FIXME
//			}
//		}

		return d2f.assign(d2G, dfunc.plus).assign(d2H, dfunc.plus);
	}
}
