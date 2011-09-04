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

import static edu.cornell.pserc.jpower.tdouble.opf.Djp_polycost.polycost;
import static edu.cornell.pserc.jpower.tdouble.opf.Djp_d2Sbus_dV2.d2Sbus_dV2;
import static edu.cornell.pserc.jpower.tdouble.opf.Djp_dIbr_dV.dIbr_dV;
import static edu.cornell.pserc.jpower.tdouble.opf.Djp_d2AIbr_dV2.d2AIbr_dV2;
import static edu.cornell.pserc.jpower.tdouble.opf.Djp_dSbr_dV.dSbr_dV;
import static edu.cornell.pserc.jpower.tdouble.opf.Djp_d2ASbr_dV2.d2ASbr_dV2;

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
 * @author Ray Zimmerman
 * @author Richard Lincoln
 *
 */
public class Djp_opf_hessfcn implements HessianEvaluator {

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
		// all lines have limits by default
		this(om, Ybus, Yf, Yt, jpopt, Djp_util.irange(om.get_jpc().branch.size()));
	}

	public Djp_opf_hessfcn(Djp_opf_model om, DComplexMatrix2D Ybus, DComplexMatrix2D Yf, DComplexMatrix2D Yt,
			Map<String, Double> jpopt, int[] il) {
		this(om, Ybus, Yf, Yt, jpopt, il, 1);
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
		int nb, ng, nxyz, nl2, nxtra, nw, nlam, nmu;
		int[] i, ipolp, ipolq, iLT, iEQ, iGT, iND, iL, iQ;
		double baseMVA;
		Djp_jpc jpc;
		Djp_bus bus;
		Djp_gen gen;
		Djp_branch branch;
		Djp_gencost gencost, pcost, qcost;
		Cost cp;

		Map<String, Set> vv;

		IntMatrix1D f, t;

		DoubleMatrix1D Cw, dd, rh, kk, mm, Pg, Qg, Va, Vm, d2f_dPg2, d2f_dQg2, r, kbar, rr, w, HwC, lamP, lamQ;
		DoubleMatrix1D[] kbar_v;
		DoubleMatrix2D N, H, d2f, LL, QQ, M, diagrr, AA, diagHwC, d2G_p, d2G_q, d2G, d2H_f, d2H_t, d2H, Hfaa, Hfav, Hfva, Hfvv, Htaa, Htav, Htva, Htvv;
		DoubleMatrix2D[] Hf, Ht;

		AbstractMatrix[] dIbr_dV, dSbr_dV;

		DComplexMatrix1D V, muF, muT, If, It, Sf, St;
		DComplexMatrix2D Gpaa, Gpav, Gpva, Gpvv, Gqaa, Gqav, Gqva, Gqvv, dIf_dVa, dIf_dVm, dIt_dVa, dIt_dVm, Cf, Ct, dSf_dVa, dSf_dVm, dSt_dVa, dSt_dVm;
		DComplexMatrix2D[] Gp, Gq;
		DoubleMatrix2D[][] d2G_parts, d2H_parts;



		/* unpack data */
		jpc = om.get_jpc();
		baseMVA = jpc.baseMVA;
		bus = jpc.bus;
		gen = jpc.gen;
		branch = jpc.branch;
		gencost = jpc.gencost;
		cp = om.get_cost_params();
		N = cp.N; H = cp.H;
		Cw = cp.Cw; dd = cp.dd; rh = cp.rh; kk = cp.kk; mm = cp.mm;
		vv = om.get_idx()[0];

		/* unpack needed parameters */
		nb = bus.size();		// number of buses
		ng = gen.size();		// number of dispatchable injections
		nxyz = (int) x.size();	// total number of control vars of all types

		nl2 = il.length;		// number of constrained lines

		/* grab Pg & Qg */
		Pg = x.viewPart(vv.get("Pg").i0, vv.get("Pg").N);	// active generation in p.u.
		Qg = x.viewPart(vv.get("Qg").i0, vv.get("Qg").N);	// reactive generation in p.u.

		/* put Pg & Qg back in gen */
		gen.Pg.assign(Pg.assign(dfunc.mult(baseMVA)));	// active generation in MW
		gen.Qg.assign(Qg.assign(dfunc.mult(baseMVA)));	// reactive generation in MVAr

		/* reconstruct V */
		Va = DoubleFactory1D.dense.make(nb);
		Va.assign(x.viewPart(vv.get("Va").i0, vv.get("Va").N));
		Vm = x.viewPart(vv.get("Vm").i0, vv.get("Vm").N).copy();
		V = Djp_util.polar(Vm, Va);
		nxtra = nxyz - 2*nb;
		pcost = gencost.copy(Djp_util.irange(ng));
		qcost = null;
		if (gencost.size() > ng)
			qcost = gencost.copy(Djp_util.irange(ng, 2*ng));

		/* ----- evaluate d2f ----- */

		d2f_dPg2 = DoubleFactory1D.sparse.make(ng);	// w.r.t. p.u. Pg
		d2f_dQg2 = DoubleFactory1D.sparse.make(ng);	// w.r.t. p.u. Qg
		ipolp = Djp_util.nonzero(pcost.model.copy().assign(ifunc.equals(POLYNOMIAL)));
		d2f_dPg2.assign( polycost(pcost.copy(ipolp), Pg.viewSelection(ipolp).copy().assign(dfunc.mult(baseMVA)), 2).assign(dfunc.mult(Math.pow(baseMVA, 2))) );
		if (qcost != null) {	// Qg is not free
			ipolq = Djp_util.nonzero(qcost.model.copy().assign(ifunc.equals(POLYNOMIAL)));
			d2f_dQg2.assign( polycost(qcost.copy(ipolq), Qg.viewSelection(ipolq).copy().assign(dfunc.mult(baseMVA)), 2).assign(dfunc.mult(Math.pow(baseMVA, 2))) );
		}
		i = Djp_util.icat(Djp_util.irange(vv.get("Pg").i0, vv.get("Pg").iN), Djp_util.irange(vv.get("Qg").i0, vv.get("Qg").iN));
		d2f = new SparseRCDoubleMatrix2D(nxyz, nxyz, i, i,
				DoubleFactory1D.sparse.append(d2f_dPg2, d2f_dQg2).toArray(), false, false, false);

		/* generalized cost */
		if (N != null) {
			nw = N.rows();
			r = N.zMult(x, null).assign(rh, dfunc.minus);	// generalized cost
			iLT = Djp_util.nonzero(r.copy().assign(kk.copy().assign(dfunc.neg), dfunc.less));	// below dead zone
			iEQ = Djp_util.nonzero( Djp_util.intm(r.copy().assign(dfunc.equals(0))).assign(Djp_util.intm(kk.copy().assign(dfunc.equals(0))), ifunc.and) );	// dead zone doesn't exist
			iGT = Djp_util.nonzero(r.copy().assign(kk, dfunc.less));	// above dead zone
			iND = Djp_util.icat(iLT, Djp_util.icat(iEQ, iGT));			// rows that are Not in the Dead region
			iL  = Djp_util.nonzero(dd.copy().assign(dfunc.equals(1)));	// rows using linear function
			iQ  = Djp_util.nonzero(dd.copy().assign(dfunc.equals(2)));	// rows using quadratic function
			LL = new SparseRCDoubleMatrix2D(nw, nw, iL, iL, 1, false, false);
			QQ = new SparseRCDoubleMatrix2D(nw, nw, iQ, iQ, 1, false, false);
			kbar_v = new DoubleMatrix1D[] {
					DoubleFactory1D.dense.make(iLT.length, 1),
					DoubleFactory1D.dense.make(iEQ.length),
					DoubleFactory1D.dense.make(iGT.length, -1)};
			kbar = new SparseRCDoubleMatrix2D(nw, nw, iND, iND,
					DoubleFactory1D.dense.make(kbar_v).toArray(), false, false, false).zMult(kk, null);
			rr = r.assign(kbar, dfunc.plus);	// apply non-dead zone shift
			M = new SparseRCDoubleMatrix2D(nw, nw, iND, iND,
					mm.viewSelection(iND).toArray(), false, false, false);	// dead zone or scale
			diagrr = DoubleFactory2D.sparse.diagonal(rr);

			/* linear rows multiplied by rr(i), quadratic rows by rr(i)^2 */
			w = LL.copy().assign(QQ.zMult(diagrr, null), dfunc.plus).zMult(M, null).zMult(rr, null);
			HwC = H.zMult(w, null).assign(Cw, dfunc.plus);
			AA = QQ.zMult(diagrr, null).assign(dfunc.mult(2)).assign(LL, dfunc.plus).zMult(M, null).zMult(N.viewDice(), null);
			diagHwC = DoubleFactory2D.sparse.diagonal(HwC);
			d2f.assign(AA.zMult(H, null).zMult(AA.viewDice(), null).assign(N.viewDice().copy().zMult(M, null).zMult(QQ, null).zMult(diagHwC, null).zMult(N, null), dfunc.plus), dfunc.plus);
		}
		d2f.assign(dfunc.mult(cost_mult));

		/* ----- evaluate Hessian of power balance constraints ----- */

		nlam = (int) (lambda.get("eqnonlin").size() / 2);
		lamP = lambda.get("eqnonlin").viewPart(0, nlam);
		lamQ = lambda.get("eqnonlin").viewPart(nlam, nlam);
		Gp = d2Sbus_dV2(Ybus, V, Djp_util.complex(lamP, null));
		Gpaa = Gp[0]; Gpav = Gp[1]; Gpva = Gp[2]; Gpvv = Gp[3];
		Gq = d2Sbus_dV2(Ybus, V, Djp_util.complex(lamQ, null));
		Gqaa = Gq[0]; Gqav = Gq[1]; Gqva = Gq[2]; Gqvv = Gq[3];

		d2G_p = DComplexFactory2D.sparse.compose(new DComplexMatrix2D[][] {{Gpaa, Gpav}, {Gpva, Gpvv}}).getRealPart();
		d2G_q = DComplexFactory2D.sparse.compose(new DComplexMatrix2D[][] {{Gqaa, Gqav}, {Gqva, Gqvv}}).getImaginaryPart();
		d2G_parts = new DoubleMatrix2D[][] {
				{d2G_p.assign(d2G_q, dfunc.plus), DoubleFactory2D.sparse.make(2*nb, nxtra)},
				{DoubleFactory2D.sparse.make(nxtra, 2*nb + nxtra)} };
		d2G = DoubleFactory2D.sparse.compose(d2G_parts);

		/* ----- evaluate Hessian of flow constraints ----- */

		nmu = (int) lambda.get("ineqnonlin").size() / 2;
		muF = Djp_util.complex(lambda.get("ineqnonlin").viewPart(0, nmu), null);
		muT = Djp_util.complex(lambda.get("ineqnonlin").viewPart(nmu, nmu), null);
		if (jpopt.get("OPF_FLOW_LIM") == 2) {	// current
			dIbr_dV = dIbr_dV(branch.copy(il), Yf, Yt, V);
			dIf_dVa = (DComplexMatrix2D) dIbr_dV[0];
			dIf_dVm = (DComplexMatrix2D) dIbr_dV[1];
			dIt_dVa = (DComplexMatrix2D) dIbr_dV[2];
			dIt_dVm = (DComplexMatrix2D) dIbr_dV[3];
			If = (DComplexMatrix1D) dIbr_dV[4];
			It = (DComplexMatrix1D) dIbr_dV[5];
			Hf = d2AIbr_dV2(dIf_dVa, dIf_dVm, If, Yf, V, muF);
			Ht = d2AIbr_dV2(dIt_dVa, dIt_dVm, It, Yt, V, muT);
		} else {
			f = branch.f_bus.viewSelection(il);	// list of "from" buses
			t = branch.t_bus.viewSelection(il);	// list of "to" buses
			Cf = new SparseRCDComplexMatrix2D(nl2, nb, Djp_util.irange(nl2), f.toArray(), new double[] {1, 0}, false, false);
			Ct = new SparseRCDComplexMatrix2D(nl2, nb, Djp_util.irange(nl2), t.toArray(), new double[] {1, 0}, false, false);
			dSbr_dV = dSbr_dV(branch.copy(il), Yf, Yt, V);
			dSf_dVa = (DComplexMatrix2D) dSbr_dV[0];
			dSf_dVm = (DComplexMatrix2D) dSbr_dV[1];
			dSt_dVa = (DComplexMatrix2D) dSbr_dV[2];
			dSt_dVm = (DComplexMatrix2D) dSbr_dV[3];
			Sf = (DComplexMatrix1D) dSbr_dV[4];
			St = (DComplexMatrix1D) dSbr_dV[5];
			if (jpopt.get("OPF_FLOW_LIM") == 1) {	// real power
				Hf = d2ASbr_dV2(Djp_util.complex(dSf_dVa.getRealPart(), null),
						Djp_util.complex(dSf_dVm.getRealPart(), null), Djp_util.complex(Sf.getRealPart(), null), Cf, Yf, V, muF);
				Ht = d2ASbr_dV2(Djp_util.complex(dSt_dVa.getRealPart(), null),
						Djp_util.complex(dSt_dVm.getRealPart(), null), Djp_util.complex(St.getRealPart(), null), Ct, Yt, V, muT);
			} else {	// apparent power
				Hf = d2ASbr_dV2(dSf_dVa, dSf_dVm, Sf, Cf, Yf, V, muF);
				Ht = d2ASbr_dV2(dSt_dVa, dSt_dVm, St, Ct, Yt, V, muT);
			}
		}
		Hfaa = Hf[0]; Hfav = Hf[1]; Hfva = Hf[2]; Hfvv = Hf[3];
		Htaa = Ht[0]; Htav = Ht[1]; Htva = Ht[2]; Htvv = Ht[3];
		d2H_f = DoubleFactory2D.sparse.compose(new DoubleMatrix2D[][] {{Hfaa, Hfav}, {Hfva, Hfvv}});
		d2H_t = DoubleFactory2D.sparse.compose(new DoubleMatrix2D[][] {{Htaa, Htav}, {Htva, Htvv}});
		d2H_parts = new DoubleMatrix2D[][] {
				{d2H_f.assign(d2H_t, dfunc.plus), DoubleFactory2D.sparse.make(2*nb, nxtra)},
				{DoubleFactory2D.sparse.make(nxtra, 2*nb + nxtra)} };
		d2H = DoubleFactory2D.sparse.compose(d2H_parts);

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
//				// TODO Translate numerical check
//			}
//		}

		return d2f.assign(d2G, dfunc.plus).assign(d2H, dfunc.plus);
	}
}
