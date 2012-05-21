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

package edu.cornell.pserc.jpower.test;

import static edu.cornell.pserc.jpower.Djp_jpoption.jpoption;
import static edu.cornell.pserc.jpower.pf.Djp_runpf.runpf;

import java.util.Map;

import cern.colt.matrix.AbstractMatrix;
import cern.colt.matrix.tdcomplex.DComplexFactory1D;
import cern.colt.matrix.tdcomplex.DComplexFactory2D;
import cern.colt.matrix.tdcomplex.DComplexMatrix1D;
import cern.colt.matrix.tdcomplex.DComplexMatrix2D;
import cern.colt.matrix.tdcomplex.impl.SparseCCDComplexMatrix2D;
import cern.colt.matrix.tdouble.DoubleFactory1D;
import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tint.impl.SparseCCIntMatrix2D;

import static edu.emory.mathcs.utils.Utils.dfunc;
import static edu.emory.mathcs.utils.Utils.cfunc;
import static edu.emory.mathcs.utils.Utils.polar;
import static edu.emory.mathcs.utils.Utils.ones;
import static edu.emory.mathcs.utils.Utils.irange;

import edu.cornell.pserc.jpower.jpc.Branch;
import edu.cornell.pserc.jpower.jpc.Bus;
import edu.cornell.pserc.jpower.jpc.Gen;
import edu.cornell.pserc.jpower.jpc.JPC;

import static edu.cornell.pserc.jpower.cases.Djp_case30.case30;
import static edu.cornell.pserc.jpower.Djp_ext2int.ext2int;
import static edu.cornell.pserc.jpower.pf.Djp_makeYbus.makeYbus;
import static edu.cornell.pserc.jpower.pf.Djp_dSbus_dV.dSbus_dV;
import static edu.cornell.pserc.jpower.opf.Djp_d2Sbus_dV2.d2Sbus_dV2;
import static edu.cornell.pserc.jpower.opf.Djp_dSbr_dV.dSbr_dV;
import static edu.cornell.pserc.jpower.opf.Djp_d2Sbr_dV2.d2Sbr_dV2;
import static edu.cornell.pserc.jpower.opf.Djp_dIbr_dV.dIbr_dV;
import static edu.cornell.pserc.jpower.opf.Djp_d2Ibr_dV2.d2Ibr_dV2;
import static edu.cornell.pserc.jpower.opf.Djp_dAbr_dV.dAbr_dV;
import static edu.cornell.pserc.jpower.opf.Djp_d2ASbr_dV2.d2ASbr_dV2;
import static edu.cornell.pserc.jpower.opf.Djp_d2AIbr_dV2.d2AIbr_dV2;

import static edu.cornell.pserc.jpower.test.Djp_t_begin.t_begin;
import static edu.cornell.pserc.jpower.test.Djp_t_is.t_is;
import static edu.cornell.pserc.jpower.test.Djp_t_end.t_end;

/**
 * Numerical tests of 2nd derivative code.
 */
public class Djp_t_hessian {

	public static void t_hasPQcap() {
		t_hasPQcap(false);
	}

	@SuppressWarnings("static-access")
	public static void t_hasPQcap(boolean quiet) {
		String tt;
		Map<String, Double> opt;
		double baseMVA, pert;
		JPC r;
		Bus bus;
		Gen gen;
		Branch branch;
		Object[] jpc;
		DoubleMatrix1D Vm, Va;
		DoubleMatrix2D[] dAft_dV, Gf2, Gt2, dAft_dV_ap, dAft_dV_mp;
		DoubleMatrix2D dAf_dVm, dAf_dVa, dAt_dVm, dAt_dVa,
			Gfaa2, Gfav2, Gfva2, Gfvv2,
			Gtaa2, Gtav2, Gtva2, Gtvv2,
			dAf_dVm_ap, dAf_dVa_ap, dAt_dVm_ap, dAt_dVa_ap,
			dAf_dVm_mp, dAf_dVa_mp, dAt_dVm_mp, dAt_dVa_mp;
		DComplexMatrix2D[] Y, dSbus_dV, H, dSbus_dV_ap, dSbus_dV_mp, Gf, Gt;
		DComplexMatrix2D Ybus, Yf, Yt, Cf, Ct, num_Haa, num_Hav, num_Hva, num_Hvv,
			dSbus_dVm, dSbus_dVa, Haa, Hav, Hva, Hvv,
			dSbus_dVm_ap, dSbus_dVa_ap,
			dSbus_dVm_mp, dSbus_dVa_mp,
			num_Gfaa, num_Gfva, num_Gfav, num_Gfvv,
			num_Gtaa, num_Gtva, num_Gtav, num_Gtvv,
			dSf_dVa, dSf_dVm, dSt_dVa, dSt_dVm,
			Gfaa, Gfav, Gfva, Gfvv,
			Gtaa, Gtav, Gtva, Gtvv,
			dSf_dVm_ap, dSf_dVa_ap, dSt_dVm_ap, dSt_dVa_ap,
			dSf_dVm_mp, dSf_dVa_mp, dSt_dVm_mp, dSt_dVa_mp,
			dIf_dVm, dIf_dVa, dIt_dVm, dIt_dVa,
			dIf_dVm_ap, dIf_dVa_ap, dIt_dVm_ap, dIt_dVa_ap,
			dIf_dVm_mp, dIf_dVa_mp, dIt_dVm_mp, dIt_dVa_mp;
		DComplexMatrix1D V, lam, Vap, Vmp, Sf, St, Sf_ap, St_ap,
			Sf_mp, St_mp, If, It, If_ap, It_ap, If_mp, It_mp;
		int[] f, t;
		int nl, nb, i;
		AbstractMatrix[] dSft_dV, dSft_dV_ap, dSft_dV_mp, dIft_dV,
			dIft_dV_ap, dIft_dV_mp;

		t_begin(44, quiet);

		// run powerflow to get solved case
		opt = jpoption("OUT_ALL", 0.0, "VERBOSE", quiet ? 0.0 : 1.0);
		r = runpf(case30(), opt);
		baseMVA = r.baseMVA; bus = r.bus; gen = r.gen; branch = r.branch;

		// switch to internal bus numbering and build admittance matrices
		jpc = ext2int(bus, gen, branch);
		bus = (Bus) jpc[1]; gen = (Gen) jpc[2]; branch = (Branch) jpc[3];
		Y = makeYbus(baseMVA, bus, branch);
		Ybus = Y[0]; Yf = Y[1]; Yt = Y[2];
		Vm = bus.Vm.copy();
		Va = bus.Va.copy().assign( dfunc.chain(dfunc.div(180.0), dfunc.mult(Math.PI)) );
		V = polar(Vm, Va);
		f = branch.f_bus.toArray();       // list of "from" buses
		t = branch.t_bus.toArray();       // list of "to" buses
		nl = f.length;
		nb = (int) V.size();
		// connection matrix for line & from buses
		Cf = new SparseCCDComplexMatrix2D(nl, nb, irange(nl), f, 1.0, 0.0, false);
		// connection matrix for line & to buses
		Ct = new SparseCCDComplexMatrix2D(nl, nb, irange(nl), t, 1.0, 0.0, false);
		pert = 1e-8;

		//-----  check d2Sbus_dV2 code  -----
		tt = " - d2Sbus_dV2 (complex power injections)";
		lam = DComplexFactory1D.dense.make(nb);
		lam.assignReal( DoubleFactory1D.dense.random(nb).assign(dfunc.mult(10)) );
		num_Haa = DComplexFactory2D.sparse.make(nb, nb);
		num_Hav = DComplexFactory2D.sparse.make(nb, nb);
		num_Hva = DComplexFactory2D.sparse.make(nb, nb);
		num_Hvv = DComplexFactory2D.sparse.make(nb, nb);
		dSbus_dV = dSbus_dV(Ybus, V);
		dSbus_dVm = dSbus_dV[0]; dSbus_dVa = dSbus_dV[1];
		H = d2Sbus_dV2(Ybus, V, lam);
		Haa = H[0]; Hav = H[1]; Hva = H[2]; Hvv = H[3];
		for (i = 0; i < nb; i++) {
			Vap = V.copy();
			Vap.set(i, Vm.get(i) * Math.exp(1j * (Va.get(i) + pert));
			dSbus_dV_ap = dSbus_dV(Ybus, Vap);
			dSbus_dVm_ap = dSbus_dV_ap[0]; dSbus_dVa_ap = dSbus_dV_ap[1];
			num_Haa.viewColumn(i).assign( dSbus_dVa_ap.copy().assign(dSbus_dVa, cfunc.minus).viewDice().zMult(lam, null) );
			num_Haa.viewColumn(i).assign( cfunc.div(pert) );

			num_Hva.viewColumn(i).assign( dSbus_dVm_ap.copy().assign(dSbus_dVm, cfunc.minus).viewDice().zMult(lam, null) );
			num_Hva.viewColumn(i).assign( cfunc.div(pert) );

			Vmp = V.copy();
			Vmp.set(i, (Vm.get(i) + pert) * exp(1j * Va.get(i)));
			dSbus_dV_mp = dSbus_dV(Ybus, Vmp);
			dSbus_dVm_mp = dSbus_dV_mp[0]; dSbus_dVa_mp = dSbus_dV_mp[1];
			num_Hav.viewColumn(i).assign( dSbus_dVa_mp.copy().assign(dSbus_dVa, cfunc.minus).viewDice().zMult(lam, null) );
			num_Hav.viewColumn(i).assign( cfunc.div(pert) );

			num_Hvv.viewColumn(i).assign( dSbus_dVm_mp.copy().assign(dSbus_dVm, cfunc.minus).viewDice().zMult(lam, null) );
			num_Hvv.viewColumn(i).assign( cfunc.div(pert) );
		}

		t_is(DComplexFactory2D.dense.make(Haa.toArray()), num_Haa, 4, "Haa" + tt);
		t_is(DComplexFactory2D.dense.make(Hav.toArray()), num_Hav, 4, "Hav" + tt);
		t_is(DComplexFactory2D.dense.make(Hva.toArray()), num_Hva, 4, "Hva" + tt);
		t_is(DComplexFactory2D.dense.make(Hvv.toArray()), num_Hvv, 4, "Hvv" + tt);

		//-----  check d2Sbr_dV2 code  -----
		tt = " - d2Sbr_dV2 (complex power flows)";
		lam = DComplexFactory1D.dense.make(nl);
		lam.assignReal( DoubleFactory1D.dense.random(nl).assign(dfunc.mult(10)) );
		// lam = [1; zeros(nl-1, 1)];
		num_Gfaa = DComplexFactory2D.sparse.make(nb, nb);
		num_Gfav = DComplexFactory2D.sparse.make(nb, nb);
		num_Gfva = DComplexFactory2D.sparse.make(nb, nb);
		num_Gfvv = DComplexFactory2D.sparse.make(nb, nb);
		num_Gtaa = DComplexFactory2D.sparse.make(nb, nb);
		num_Gtav = DComplexFactory2D.sparse.make(nb, nb);
		num_Gtva = DComplexFactory2D.sparse.make(nb, nb);
		num_Gtvv = DComplexFactory2D.sparse.make(nb, nb);
		dSft_dV = dSbr_dV(branch, Yf, Yt, V);
		dSf_dVa = (DComplexMatrix2D) dSft_dV[0];
		dSf_dVm = (DComplexMatrix2D) dSft_dV[1];
		dSt_dVa = (DComplexMatrix2D) dSft_dV[2];
		dSt_dVm = (DComplexMatrix2D) dSft_dV[3];
		Gf = d2Sbr_dV2(Cf, Yf, V, lam);
		Gfaa = Gf[0]; Gfav = Gf[1]; Gfva = Gf[2]; Gfvv = Gf[3];
		Gt = d2Sbr_dV2(Ct, Yt, V, lam);
		Gtaa = Gt[0]; Gtav = Gt[1]; Gtva = Gt[2]; Gtvv = Gt[3];
		for (i = 0; i < nb; i++) {
			Vap = V.copy();
			Vap.set(i, Vm.get(i) * Math.exp(1j * (Va.get(i) + pert));

			dSft_dV_ap = dSbr_dV(branch, Yf, Yt, Vap);
			dSf_dVa_ap = (DComplexMatrix2D) dSft_dV_ap[0];
			dSf_dVm_ap = (DComplexMatrix2D) dSft_dV_ap[1];
			dSt_dVa_ap = (DComplexMatrix2D) dSft_dV_ap[2];
			dSt_dVm_ap = (DComplexMatrix2D) dSft_dV_ap[3];
			Sf_ap = (DComplexMatrix1D) dSft_dV_ap[4];
			St_ap = (DComplexMatrix1D) dSft_dV_ap[5];

			num_Gfaa.viewColumn(i).assign( dSf_dVa_ap.copy().assign(dSf_dVa, cfunc.minus).viewDice().zMult(lam, null) );
			num_Gfaa.viewColumn(i).assign( cfunc.div(pert) );
			num_Gfva.viewColumn(i).assign( dSf_dVm_ap.copy().assign(dSf_dVm, cfunc.minus).viewDice().zMult(lam, null) );
			num_Gfva.viewColumn(i).assign( cfunc.div(pert) );
			num_Gtaa.viewColumn(i).assign( dSt_dVa_ap.copy().assign(dSt_dVa, cfunc.minus).viewDice().zMult(lam, null) );
			num_Gtaa.viewColumn(i).assign( cfunc.div(pert) );
			num_Gtva.viewColumn(i).assign( dSt_dVm_ap.copy().assign(dSt_dVm, cfunc.minus).viewDice().zMult(lam, null) );
			num_Gtva.viewColumn(i).assign( cfunc.div(pert) );

			Vmp = V.copy();
			Vmp.set(i) = (Vm.get(i) + pert) * exp(1j * Va.get(i));

			dSft_dV_mp = dSbr_dV(branch, Yf, Yt, Vmp);
			dSf_dVa_mp = (DComplexMatrix2D) dSft_dV_mp[0];
			dSf_dVm_mp = (DComplexMatrix2D) dSft_dV_mp[1];
			dSt_dVa_mp = (DComplexMatrix2D) dSft_dV_mp[2];
			dSt_dVm_mp = (DComplexMatrix2D) dSft_dV_mp[3];
			Sf_mp = (DComplexMatrix1D) dSft_dV_mp[4];
			St_mp = (DComplexMatrix1D) dSft_dV_mp[5];

			num_Gfav.viewColumn(i).assign( dSf_dVa_mp.copy().assign(dSf_dVa, cfunc.minus).viewDice().zMult(lam, null) );
			num_Gfav.viewColumn(i).assign( cfunc.div(pert) );
			num_Gfvv.viewColumn(i).assign( dSf_dVm_mp.copy().assign(dSf_dVm, cfunc.minus).viewDice().zMult(lam, null) );
			num_Gfvv.viewColumn(i).assign( cfunc.div(pert) );
			num_Gtav.viewColumn(i).assign( dSt_dVa_mp.copy().assign(dSt_dVa, cfunc.minus).viewDice().zMult(lam, null) );
			num_Gtav.viewColumn(i).assign( cfunc.div(pert) );
			num_Gtvv.viewColumn(i).assign( dSt_dVm_mp.copy().assign(dSt_dVm, cfunc.minus).viewDice().zMult(lam, null) );
			num_Gtvv.viewColumn(i).assign( cfunc.div(pert) );
		}

		t_is(DComplexFactory2D.dense.make(Gfaa.toArray()), num_Gfaa, 4, "Gfaa" + tt);
		t_is(DComplexFactory2D.dense.make(Gfav.toArray()), num_Gfav, 4, "Gfav" + tt);
		t_is(DComplexFactory2D.dense.make(Gfva.toArray()), num_Gfva, 4, "Gfva" + tt);
		t_is(DComplexFactory2D.dense.make(Gfvv.toArray()), num_Gfvv, 4, "Gfvv" + tt);

		t_is(DComplexFactory2D.dense.make(Gtaa.toArray()), num_Gtaa, 4, "Gtaa" + tt);
		t_is(DComplexFactory2D.dense.make(Gtav.toArray()), num_Gtav, 4, "Gtav" + tt);
		t_is(DComplexFactory2D.dense.make(Gtva.toArray()), num_Gtva, 4, "Gtva" + tt);
		t_is(DComplexFactory2D.dense.make(Gtvv.toArray()), num_Gtvv, 4, "Gtvv" + tt);

		//-----  check d2Ibr_dV2 code  -----
		tt = " - d2Ibr_dV2 (complex currents)";
		lam = DComplexFactory1D.dense.make(nl);
		lam.assignReal( DoubleFactory1D.dense.random(nl).assign(dfunc.mult(10)) );
		// lam = [1; zeros(nl-1, 1)];
		num_Gfaa = DComplexFactory2D.sparse.make(nb, nb);
		num_Gfav = DComplexFactory2D.sparse.make(nb, nb);
		num_Gfva = DComplexFactory2D.sparse.make(nb, nb);
		num_Gfvv = DComplexFactory2D.sparse.make(nb, nb);
		num_Gtaa = DComplexFactory2D.sparse.make(nb, nb);
		num_Gtav = DComplexFactory2D.sparse.make(nb, nb);
		num_Gtva = DComplexFactory2D.sparse.make(nb, nb);
		num_Gtvv = DComplexFactory2D.sparse.make(nb, nb);

		dIft_dV = dIbr_dV(branch, Yf, Yt, V);
		dIf_dVa = (DComplexMatrix2D) dIft_dV[0];
		dIf_dVm = (DComplexMatrix2D) dIft_dV[1];
		dIt_dVa = (DComplexMatrix2D) dIft_dV[2];
		dIt_dVm = (DComplexMatrix2D) dIft_dV[3];
		If = (DComplexMatrix1D) dIft_dV[4];
		It = (DComplexMatrix1D) dIft_dV[5];

		Gf = d2Ibr_dV2(Yf, V, lam);
		Gfaa = Gf[0]; Gfav = Gf[1]; Gfva = Gf[2]; Gfvv = Gf[3];
		Gt = d2Ibr_dV2(Yt, V, lam);
		Gtaa = Gt[0]; Gtav = Gt[1]; Gtva = Gt[2]; Gtvv = Gt[3];

		for (i = 0; i < nb; i++) {
			Vap = V.copy();
			Vap.set(i, Vm.get(i) * Math.exp(1j * (Va.get(i) + pert));

			dIft_dV_ap = dIbr_dV(branch, Yf, Yt, Vap);
			dIf_dVa_ap = (DComplexMatrix2D) dIft_dV_ap[0];
			dIf_dVm_ap = (DComplexMatrix2D) dIft_dV_ap[1];
			dIt_dVa_ap = (DComplexMatrix2D) dIft_dV_ap[2];
			dIt_dVm_ap = (DComplexMatrix2D) dIft_dV_ap[3];
			If_ap = (DComplexMatrix1D) dIft_dV_ap[4];
			It_ap = (DComplexMatrix1D) dIft_dV_ap[5];

			num_Gfaa.viewColumn(i).assign( dIf_dVa_ap.copy().assign(dIf_dVa, cfunc.minus).viewDice().zMult(lam, null) );
			num_Gfaa.viewColumn(i).assign( cfunc.div(pert) );
			num_Gfva.viewColumn(i).assign( dIf_dVm_ap.copy().assign(dIf_dVm, cfunc.minus).viewDice().zMult(lam, null) );
			num_Gfva.viewColumn(i).assign( cfunc.div(pert) );
			num_Gtaa.viewColumn(i).assign( dIt_dVa_ap.copy().assign(dIt_dVa, cfunc.minus).viewDice().zMult(lam, null) );
			num_Gtaa.viewColumn(i).assign( cfunc.div(pert) );
			num_Gtva.viewColumn(i).assign( dIt_dVm_ap.copy().assign(dIt_dVm, cfunc.minus).viewDice().zMult(lam, null) );
			num_Gtva.viewColumn(i).assign( cfunc.div(pert) );

			Vmp = V.copy();
			Vmp.set(i, (Vm.get(i) + pert) * Math.exp(1j * Va.get(i));

			dIft_dV_mp = dIbr_dV(branch, Yf, Yt, Vmp);
			dIf_dVa_mp = (DComplexMatrix2D) dIft_dV_mp[0];
			dIf_dVm_mp = (DComplexMatrix2D) dIft_dV_mp[1];
			dIt_dVa_mp = (DComplexMatrix2D) dIft_dV_mp[2];
			dIt_dVm_mp = (DComplexMatrix2D) dIft_dV_mp[3];
			If_mp = (DComplexMatrix1D) dIft_dV_mp[4];
			It_mp = (DComplexMatrix1D) dIft_dV_mp[5];

			num_Gfav.viewColumn(i).assign( dIf_dVa_mp.copy().assign(dIf_dVa, cfunc.minus).viewDice().zMult(lam, null) );
			num_Gfav.viewColumn(i).assign( cfunc.div(pert) );
			num_Gfvv.viewColumn(i).assign( dIf_dVm_mp.copy().assign(dIf_dVm, cfunc.minus).viewDice().zMult(lam, null) );
			num_Gfvv.viewColumn(i).assign( cfunc.div(pert) );
			num_Gtav.viewColumn(i).assign( dIt_dVa_mp.copy().assign(dIt_dVa, cfunc.minus).viewDice().zMult(lam, null) );
			num_Gtav.viewColumn(i).assign( cfunc.div(pert) );
			num_Gtvv.viewColumn(i).assign( dIt_dVm_mp.copy().assign(dIt_dVm, cfunc.minus).viewDice().zMult(lam, null) );
			num_Gtvv.viewColumn(i).assign( cfunc.div(pert) );
		}

		t_is(DComplexFactory2D.dense.make(Gfaa.toArray()), num_Gfaa, 4, "Gfaa" + tt);
		t_is(DComplexFactory2D.dense.make(Gfav.toArray()), num_Gfav, 4, "Gfav" + tt);
		t_is(DComplexFactory2D.dense.make(Gfva.toArray()), num_Gfva, 4, "Gfva" + tt);
		t_is(DComplexFactory2D.dense.make(Gfvv.toArray()), num_Gfvv, 4, "Gfvv" + tt);

		t_is(DComplexFactory2D.dense.make(Gtaa.toArray()), num_Gtaa, 4, "Gtaa" + tt);
		t_is(DComplexFactory2D.dense.make(Gtav.toArray()), num_Gtav, 4, "Gtav" + tt);
		t_is(DComplexFactory2D.dense.make(Gtva.toArray()), num_Gtva, 4, "Gtva" + tt);
		t_is(DComplexFactory2D.dense.make(Gtvv.toArray()), num_Gtvv, 4, "Gtvv" + tt);

		//-----  check d2ASbr_dV2 code  -----
		tt = " - d2ASbr_dV2 (squared apparent power flows)";
		lam = DComplexFactory1D.dense.make(nl);
		lam.assignReal( DoubleFactory1D.dense.random(nl).assign(dfunc.mult(10)) );
		// lam = [1; zeros(nl-1, 1)];
		num_Gfaa = DComplexFactory2D.sparse.make(nb, nb);
		num_Gfav = DComplexFactory2D.sparse.make(nb, nb);
		num_Gfva = DComplexFactory2D.sparse.make(nb, nb);
		num_Gfvv = DComplexFactory2D.sparse.make(nb, nb);
		num_Gtaa = DComplexFactory2D.sparse.make(nb, nb);
		num_Gtav = DComplexFactory2D.sparse.make(nb, nb);
		num_Gtva = DComplexFactory2D.sparse.make(nb, nb);
		num_Gtvv = DComplexFactory2D.sparse.make(nb, nb);

		dSft_dV = dSbr_dV(branch, Yf, Yt, V);
		dSf_dVa = (DComplexMatrix2D) dSft_dV[0];
		dSf_dVm = (DComplexMatrix2D) dSft_dV[1];
		dSt_dVa = (DComplexMatrix2D) dSft_dV[2];
		dSt_dVm = (DComplexMatrix2D) dSft_dV[3];
		Sf = (DComplexMatrix1D) dSft_dV[4];
		St = (DComplexMatrix1D) dSft_dV[5];

		dAft_dV = dAbr_dV(dSf_dVa, dSf_dVm, dSt_dVa, dSt_dVm, Sf, St);
		dAf_dVa = dAft_dV[0]; dAf_dVm = dAft_dV[1];
		dAt_dVa = dAft_dV[2]; dAt_dVm = dAft_dV[3];

		Gf2 = d2ASbr_dV2(dSf_dVa, dSf_dVm, Sf, Cf, Yf, V, lam);
		Gfaa2 = Gf2[0]; Gfav2 = Gf2[1]; Gfva2 = Gf2[2]; Gfvv2 = Gf2[3];

		Gt2 = d2ASbr_dV2(dSt_dVa, dSt_dVm, St, Ct, Yt, V, lam);
		Gtaa2 = Gt2[0]; Gtav2 = Gt2[1]; Gtva2 = Gt2[2]; Gtvv2 = Gt2[3];
		for (i = 0; i < nb; i++) {
			Vap = V.copy();
			Vap.set(i, Vm.get(i) * Math.exp(1j * (Va.get(i) + pert));

			dSft_dV_ap = dSbr_dV(branch, Yf, Yt, Vap);
			dSf_dVa_ap = (DComplexMatrix2D) dSft_dV_ap[0];
			dSf_dVm_ap = (DComplexMatrix2D) dSft_dV_ap[1];
			dSt_dVa_ap = (DComplexMatrix2D) dSft_dV_ap[2];
			dSt_dVm_ap = (DComplexMatrix2D) dSft_dV_ap[3];
			Sf_ap = (DComplexMatrix1D) dSft_dV_ap[4];
			St_ap = (DComplexMatrix1D) dSft_dV_ap[5];

			dAft_dV_ap = dAbr_dV(dSf_dVa_ap, dSf_dVm_ap, dSt_dVa_ap, dSt_dVm_ap, Sf_ap, St_ap);
			dAf_dVa_ap = dAft_dV_ap[0]; dAf_dVm_ap = dAft_dV_ap[1];
			dAt_dVa_ap = dAft_dV_ap[2]; dAt_dVm_ap = dAft_dV_ap[3];

			num_Gfaa.viewColumn(i).assignReal( dAf_dVa_ap.copy().assign(dAf_dVa, dfunc.minus).viewDice().zMult(lam.getRealPart(), null) );
			num_Gfaa.viewColumn(i).assign( cfunc.div(pert) );
			num_Gfva.viewColumn(i).assignReal( dAf_dVm_ap.copy().assign(dAf_dVm, dfunc.minus).viewDice().zMult(lam.getRealPart(), null) );
			num_Gfva.viewColumn(i).assign( cfunc.div(pert) );
			num_Gtaa.viewColumn(i).assignReal( dAt_dVa_ap.copy().assign(dAt_dVa, dfunc.minus).viewDice().zMult(lam.getRealPart(), null) );
			num_Gtaa.viewColumn(i).assign( cfunc.div(pert) );
			num_Gtva.viewColumn(i).assignReal( dAt_dVm_ap.copy().assign(dAt_dVm, dfunc.minus).viewDice().zMult(lam.getRealPart(), null) );
			num_Gtva.viewColumn(i).assign( cfunc.div(pert) );

			Vmp = V.copy();
			Vmp.set(i, (Vm.get(i) + pert) * Math.exp(1j * Va.get(i)));

			dSft_dV_mp = dSbr_dV(branch, Yf, Yt, Vmp);
			dSf_dVa_mp = (DComplexMatrix2D) dSft_dV_mp[0];
			dSf_dVm_mp = (DComplexMatrix2D) dSft_dV_mp[1];
			dSt_dVa_mp = (DComplexMatrix2D) dSft_dV_mp[2];
			dSt_dVm_mp = (DComplexMatrix2D) dSft_dV_mp[3];
			Sf_mp = (DComplexMatrix1D) dSft_dV_mp[4];
			St_mp = (DComplexMatrix1D) dSft_dV_mp[5];

			dAft_dV_mp = dAbr_dV(dSf_dVa_mp, dSf_dVm_mp, dSt_dVa_mp, dSt_dVm_mp, Sf_mp, St_mp);
			dAf_dVa_mp = dAft_dV_mp[0]; dAf_dVm_mp = dAft_dV_mp[1];
			dAt_dVa_mp = dAft_dV_mp[2]; dAt_dVm_mp = dAft_dV_mp[3];

			num_Gfav.viewColumn(i).assignReal( dAf_dVa_mp.copy().assign(dAf_dVa, dfunc.minus).viewDice().zMult(lam.getRealPart(), null) );
			num_Gfav.viewColumn(i).assign( cfunc.div(pert) );
			num_Gfvv.viewColumn(i).assignReal( dAf_dVm_mp.copy().assign(dAf_dVm, dfunc.minus).viewDice().zMult(lam.getRealPart(), null) );
			num_Gfvv.viewColumn(i).assign( cfunc.div(pert) );
			num_Gtav.viewColumn(i).assignReal( dAt_dVa_mp.copy().assign(dAt_dVa, dfunc.minus).viewDice().zMult(lam.getRealPart(), null) );
			num_Gtav.viewColumn(i).assign( cfunc.div(pert) );
			num_Gtvv.viewColumn(i).assignReal( dAt_dVm_mp.copy().assign(dAt_dVm, dfunc.minus).viewDice().zMult(lam.getRealPart(), null) );
			num_Gtvv.viewColumn(i).assign( cfunc.div(pert) );
		}

		t_is(DComplexFactory2D.dense.make(Gfaa.toArray()), num_Gfaa, 2, "Gfaa" + tt);
		t_is(DComplexFactory2D.dense.make(Gfav.toArray()), num_Gfav, 2, "Gfav" + tt);
		t_is(DComplexFactory2D.dense.make(Gfva.toArray()), num_Gfva, 2, "Gfva" + tt);
		t_is(DComplexFactory2D.dense.make(Gfvv.toArray()), num_Gfvv, 2, "Gfvv" + tt);

		t_is(DComplexFactory2D.dense.make(Gtaa.toArray()), num_Gtaa, 2, "Gtaa" + tt);
		t_is(DComplexFactory2D.dense.make(Gtav.toArray()), num_Gtav, 2, "Gtav" + tt);
		t_is(DComplexFactory2D.dense.make(Gtva.toArray()), num_Gtva, 2, "Gtva" + tt);
		t_is(DComplexFactory2D.dense.make(Gtvv.toArray()), num_Gtvv, 2, "Gtvv" + tt);

		//-----  check d2ASbr_dV2 code  -----
		tt = " - d2ASbr_dV2 (squared real power flows)";
		lam = DComplexFactory1D.dense.make(nl);
		lam.assignReal( DoubleFactory1D.dense.random(nl).assign(dfunc.mult(10)) );
		// lam = [1; zeros(nl-1, 1)];
		num_Gfaa = DComplexFactory2D.sparse.make(nb, nb);
		num_Gfav = DComplexFactory2D.sparse.make(nb, nb);
		num_Gfva = DComplexFactory2D.sparse.make(nb, nb);
		num_Gfvv = DComplexFactory2D.sparse.make(nb, nb);
		num_Gtaa = DComplexFactory2D.sparse.make(nb, nb);
		num_Gtav = DComplexFactory2D.sparse.make(nb, nb);
		num_Gtva = DComplexFactory2D.sparse.make(nb, nb);
		num_Gtvv = DComplexFactory2D.sparse.make(nb, nb);

		dSft_dV = dSbr_dV(branch, Yf, Yt, V);
		dSf_dVa = (DComplexMatrix2D) dSft_dV[0];
		dSf_dVm = (DComplexMatrix2D) dSft_dV[1];
		dSt_dVa = (DComplexMatrix2D) dSft_dV[2];
		dSt_dVm = (DComplexMatrix2D) dSft_dV[3];
		Sf = (DComplexMatrix1D) dSft_dV[4];
		St = (DComplexMatrix1D) dSft_dV[5];

		// zero imaginary parts
		dSf_dVa.assignImaginary( DoubleFactory2D.dense.make(nl, nl) );
		dSf_dVm.assignImaginary( DoubleFactory2D.dense.make(nl, nl) );
		dSt_dVa.assignImaginary( DoubleFactory2D.dense.make(nl, nl) );
		dSt_dVm.assignImaginary( DoubleFactory2D.dense.make(nl, nl) );
		Sf.assignImaginary( DoubleFactory1D.dense.make(nl) );
		St.assignImaginary( DoubleFactory1D.dense.make(nl) );

		dAft_dV = dAbr_dV(dSf_dVa, dSf_dVm, dSt_dVa, dSt_dVm, Sf, St);
		dAf_dVa = dAft_dV[0]; dAf_dVm = dAft_dV[1];
		dAt_dVa = dAft_dV[2]; dAt_dVm = dAft_dV[3];

		Gf2 = d2ASbr_dV2(dSf_dVa, dSf_dVm, Sf, Cf, Yf, V, lam);
		Gfaa2 = Gf2[0]; Gfav2 = Gf2[1]; Gfva2 = Gf2[2]; Gfvv2 = Gf2[3];

		Gt2 = d2ASbr_dV2(dSt_dVa, dSt_dVm, St, Ct, Yt, V, lam);
		Gtaa2 = Gt2[0]; Gtav2 = Gt2[1]; Gtva2 = Gt2[2]; Gtvv2 = Gt2[3];

		for (i = 0; i < nb; i++) {
			Vap = V.copy();
			Vap.set(i, Vm.get(i) * Math.exp(1j * (Va.get(i) + pert));

			dSft_dV_ap = dSbr_dV(branch, Yf, Yt, Vap);
			dSf_dVa_ap = (DComplexMatrix2D) dSft_dV_ap[0];
			dSf_dVm_ap = (DComplexMatrix2D) dSft_dV_ap[1];
			dSt_dVa_ap = (DComplexMatrix2D) dSft_dV_ap[2];
			dSt_dVm_ap = (DComplexMatrix2D) dSft_dV_ap[3];
			Sf_ap = (DComplexMatrix1D) dSft_dV_ap[4];
			St_ap = (DComplexMatrix1D) dSft_dV_ap[5];

			// zero imaginary parts
			dSf_dVa_ap.assignImaginary( DoubleFactory2D.dense.make(nl, nl) );
			dSf_dVm_ap.assignImaginary( DoubleFactory2D.dense.make(nl, nl) );
			dSt_dVa_ap.assignImaginary( DoubleFactory2D.dense.make(nl, nl) );
			dSt_dVm_ap.assignImaginary( DoubleFactory2D.dense.make(nl, nl) );
			Sf_ap.assignImaginary( DoubleFactory1D.dense.make(nl) );
			St_ap.assignImaginary( DoubleFactory1D.dense.make(nl) );

			dAft_dV_ap = dAbr_dV(dSf_dVa_ap, dSf_dVm_ap, dSt_dVa_ap, dSt_dVm_ap, Sf_ap, St_ap);
			dAf_dVa_ap = dAft_dV_ap[0]; dAf_dVm_ap = dAft_dV_ap[1];
			dAt_dVa_ap = dAft_dV_ap[2]; dAt_dVm_ap = dAft_dV_ap[3];

			num_Gfaa.viewColumn(i).assignReal( dAf_dVa_ap.copy().assign(dAf_dVa, dfunc.minus).viewDice().zMult(lam.getRealPart(), null) );
			num_Gfaa.viewColumn(i).assign( cfunc.div(pert) );
			num_Gfva.viewColumn(i).assignReal( dAf_dVm_ap.copy().assign(dAf_dVm, dfunc.minus).viewDice().zMult(lam.getRealPart(), null) );
			num_Gfva.viewColumn(i).assign( cfunc.div(pert) );
			num_Gtaa.viewColumn(i).assignReal( dAt_dVa_ap.copy().assign(dAt_dVa, dfunc.minus).viewDice().zMult(lam.getRealPart(), null) );
			num_Gtaa.viewColumn(i).assign( cfunc.div(pert) );
			num_Gtva.viewColumn(i).assignReal( dAt_dVm_ap.copy().assign(dAt_dVm, dfunc.minus).viewDice().zMult(lam.getRealPart(), null) );
			num_Gtva.viewColumn(i).assign( cfunc.div(pert) );

			Vmp = V.copy();
			Vmp.set(i, (Vm.get(i) + pert) * Math.exp(1j * Va.get(i));

			dSft_dV_mp = dSbr_dV(branch, Yf, Yt, Vmp);
			dSf_dVa_mp = (DComplexMatrix2D) dSft_dV_mp[0];
			dSf_dVm_mp = (DComplexMatrix2D) dSft_dV_mp[1];
			dSt_dVa_mp = (DComplexMatrix2D) dSft_dV_mp[2];
			dSt_dVm_mp = (DComplexMatrix2D) dSft_dV_mp[3];
			Sf_mp = (DComplexMatrix1D) dSft_dV_mp[4];
			St_mp = (DComplexMatrix1D) dSft_dV_mp[5];

			// zero imaginary parts
			dSf_dVa_mp.assignImaginary( DoubleFactory2D.dense.make(nl, nl) );
			dSf_dVm_mp.assignImaginary( DoubleFactory2D.dense.make(nl, nl) );
			dSt_dVa_mp.assignImaginary( DoubleFactory2D.dense.make(nl, nl) );
			dSt_dVm_mp.assignImaginary( DoubleFactory2D.dense.make(nl, nl) );
			Sf_mp.assignImaginary( DoubleFactory1D.dense.make(nl) );
			St_mp.assignImaginary( DoubleFactory1D.dense.make(nl) );

			dAft_dV_mp = dAbr_dV(dSf_dVa_mp, dSf_dVm_mp, dSt_dVa_mp, dSt_dVm_mp, Sf_mp, St_mp);
			dAf_dVa_mp = dAft_dV_mp[0]; dAf_dVm_mp = dAft_dV_mp[1];
			dAt_dVa_mp = dAft_dV_mp[2]; dAt_dVm_mp = dAft_dV_mp[3];

			num_Gfav.viewColumn(i).assignReal( dAf_dVa_mp.copy().assign(dAf_dVa, dfunc.minus).viewDice().zMult(lam.getRealPart(), null) );
			num_Gfav.viewColumn(i).assign( cfunc.div(pert) );
			num_Gfvv.viewColumn(i).assignReal( dAf_dVm_mp.copy().assign(dAf_dVm, dfunc.minus).viewDice().zMult(lam.getRealPart(), null) );
			num_Gfvv.viewColumn(i).assign( cfunc.div(pert) );
			num_Gtav.viewColumn(i).assignReal( dAt_dVa_mp.copy().assign(dAt_dVa, dfunc.minus).viewDice().zMult(lam.getRealPart(), null) );
			num_Gtav.viewColumn(i).assign( cfunc.div(pert) );
			num_Gtvv.viewColumn(i).assignReal( dAt_dVm_mp.copy().assign(dAt_dVm, dfunc.minus).viewDice().zMult(lam.getRealPart(), null) );
			num_Gtvv.viewColumn(i).assign( cfunc.div(pert) );
		}

		t_is(DComplexFactory2D.dense.make(Gfaa.toArray()), num_Gfaa, 2, "Gfaa" + tt);
		t_is(DComplexFactory2D.dense.make(Gfav.toArray()), num_Gfav, 2, "Gfav" + tt);
		t_is(DComplexFactory2D.dense.make(Gfva.toArray()), num_Gfva, 2, "Gfva" + tt);
		t_is(DComplexFactory2D.dense.make(Gfvv.toArray()), num_Gfvv, 2, "Gfvv" + tt);

		t_is(DComplexFactory2D.dense.make(Gtaa.toArray()), num_Gtaa, 2, "Gtaa" + tt);
		t_is(DComplexFactory2D.dense.make(Gtav.toArray()), num_Gtav, 2, "Gtav" + tt);
		t_is(DComplexFactory2D.dense.make(Gtva.toArray()), num_Gtva, 2, "Gtva" + tt);
		t_is(DComplexFactory2D.dense.make(Gtvv.toArray()), num_Gtvv, 2, "Gtvv" + tt);

		//-----  check d2AIbr_dV2 code  -----
		tt = " - d2AIbr_dV2 (squared current magnitudes)";
		lam = DComplexFactory1D.dense.make(nl);
		lam.assignReal( DoubleFactory1D.dense.random(nl).assign(dfunc.mult(10)) );
		// lam = [1; zeros(nl-1, 1)];
		num_Gfaa = DComplexFactory2D.sparse.make(nb, nb);
		num_Gfav = DComplexFactory2D.sparse.make(nb, nb);
		num_Gfva = DComplexFactory2D.sparse.make(nb, nb);
		num_Gfvv = DComplexFactory2D.sparse.make(nb, nb);
		num_Gtaa = DComplexFactory2D.sparse.make(nb, nb);
		num_Gtav = DComplexFactory2D.sparse.make(nb, nb);
		num_Gtva = DComplexFactory2D.sparse.make(nb, nb);
		num_Gtvv = DComplexFactory2D.sparse.make(nb, nb);

		dIft_dV = dIbr_dV(branch, Yf, Yt, V);
		dIf_dVa = (DComplexMatrix2D) dIft_dV[0];
		dIf_dVm = (DComplexMatrix2D) dIft_dV[1];
		dIt_dVa = (DComplexMatrix2D) dIft_dV[2];
		dIt_dVm = (DComplexMatrix2D) dIft_dV[3];
		If = (DComplexMatrix1D) dIft_dV[4];
		It = (DComplexMatrix1D) dIft_dV[5];

		dAft_dV = dAbr_dV(dIf_dVa, dIf_dVm, dIt_dVa, dIt_dVm, If, It);
		dAf_dVa = dAft_dV[0]; dAf_dVm = dAft_dV[1];
		dAt_dVa = dAft_dV[2]; dAt_dVm = dAft_dV[3];

		Gf2 = d2AIbr_dV2(dIf_dVa, dIf_dVm, If, Yf, V, lam);
		Gfaa2 = Gf2[0]; Gfav2 = Gf2[1]; Gfva2 = Gf2[2]; Gfvv2 = Gf2[3];

		Gt2 = d2AIbr_dV2(dIt_dVa, dIt_dVm, It, Yt, V, lam);
		Gtaa2 = Gt2[0]; Gtav2 = Gt2[1]; Gtva2 = Gt2[2]; Gtvv2 = Gt2[3];

		for (i = 0; i < nb; i++) {
			Vap = V.copy();
			Vap.set(i, Vm.get(i) * Math.exp(1j * (Va.get(i) + pert));

			dIft_dV_ap = dIbr_dV(branch, Yf, Yt, Vap);
			dIf_dVa_ap = (DComplexMatrix2D) dIft_dV_ap[0];
			dIf_dVm_ap = (DComplexMatrix2D) dIft_dV_ap[1];
			dIt_dVa_ap = (DComplexMatrix2D) dIft_dV_ap[2];
			dIt_dVm_ap = (DComplexMatrix2D) dIft_dV_ap[3];
			If_ap = (DComplexMatrix1D) dIft_dV_ap[4];
			It_ap = (DComplexMatrix1D) dIft_dV_ap[5];

			dAft_dV_ap = dAbr_dV(dIf_dVa_ap, dIf_dVm_ap, dIt_dVa_ap, dIt_dVm_ap, If_ap, It_ap);
			dAf_dVa_ap = dAft_dV_ap[0]; dAf_dVm_ap = dAft_dV_ap[1];
			dAt_dVa_ap = dAft_dV_ap[2]; dAt_dVm_ap = dAft_dV_ap[3];

			num_Gfaa.viewColumn(i).assignReal( dAf_dVa_ap.copy().assign(dAf_dVa, dfunc.minus).viewDice().zMult(lam.getRealPart(), null) );
			num_Gfaa.viewColumn(i).assign( cfunc.div(pert) );
			num_Gfva.viewColumn(i).assignReal( dAf_dVm_ap.copy().assign(dAf_dVm, dfunc.minus).viewDice().zMult(lam.getRealPart(), null) );
			num_Gfva.viewColumn(i).assign( cfunc.div(pert) );
			num_Gtaa.viewColumn(i).assignReal( dAt_dVa_ap.copy().assign(dAt_dVa, dfunc.minus).viewDice().zMult(lam.getRealPart(), null) );
			num_Gtaa.viewColumn(i).assign( cfunc.div(pert) );
			num_Gtva.viewColumn(i).assignReal( dAt_dVm_ap.copy().assign(dAt_dVm, dfunc.minus).viewDice().zMult(lam.getRealPart(), null) );
			num_Gtva.viewColumn(i).assign( cfunc.div(pert) );

			Vmp = V.copy();
			Vmp.set(i, (Vm.get(i) + pert) * Math.exp(1j * Va.get(i));

			dIft_dV_mp = dIbr_dV(branch, Yf, Yt, Vmp);
			dIf_dVa_mp = (DComplexMatrix2D) dIft_dV_mp[0];
			dIf_dVm_mp = (DComplexMatrix2D) dIft_dV_mp[1];
			dIt_dVa_mp = (DComplexMatrix2D) dIft_dV_mp[2];
			dIt_dVm_mp = (DComplexMatrix2D) dIft_dV_mp[3];
			If_mp = (DComplexMatrix1D) dIft_dV_mp[4];
			It_mp = (DComplexMatrix1D) dIft_dV_mp[5];

			dAft_dV_mp = dAbr_dV(dIf_dVa_mp, dIf_dVm_mp, dIt_dVa_mp, dIt_dVm_mp, If_mp, It_mp);
			dAf_dVa_mp = dAft_dV_mp[0]; dAf_dVm_mp = dAft_dV_mp[1];
			dAt_dVa_mp = dAft_dV_mp[2]; dAt_dVm_mp = dAft_dV_mp[3];

			num_Gfav.viewColumn(i).assignReal( dAf_dVa_mp.copy().assign(dAf_dVa, dfunc.minus).viewDice().zMult(lam.getRealPart(), null) );
			num_Gfav.viewColumn(i).assign( cfunc.div(pert) );
			num_Gfvv.viewColumn(i).assignReal( dAf_dVm_mp.copy().assign(dAf_dVm, dfunc.minus).viewDice().zMult(lam.getRealPart(), null) );
			num_Gfvv.viewColumn(i).assign( cfunc.div(pert) );
			num_Gtav.viewColumn(i).assignReal( dAt_dVa_mp.copy().assign(dAt_dVa, dfunc.minus).viewDice().zMult(lam.getRealPart(), null) );
			num_Gtav.viewColumn(i).assign( cfunc.div(pert) );
			num_Gtvv.viewColumn(i).assignReal( dAt_dVm_mp.copy().assign(dAt_dVm, dfunc.minus).viewDice().zMult(lam.getRealPart(), null) );
			num_Gtvv.viewColumn(i).assign( cfunc.div(pert) );
		}

		t_is(DComplexFactory2D.dense.make(Gfaa.toArray()), num_Gfaa, 3, "Gfaa" + tt);
		t_is(DComplexFactory2D.dense.make(Gfav.toArray()), num_Gfav, 3, "Gfav" + tt);
		t_is(DComplexFactory2D.dense.make(Gfva.toArray()), num_Gfva, 3, "Gfva" + tt);
		t_is(DComplexFactory2D.dense.make(Gfvv.toArray()), num_Gfvv, 2, "Gfvv" + tt);

		t_is(DComplexFactory2D.dense.make(Gtaa.toArray()), num_Gtaa, 3, "Gtaa" + tt);
		t_is(DComplexFactory2D.dense.make(Gtav.toArray()), num_Gtav, 3, "Gtav" + tt);
		t_is(DComplexFactory2D.dense.make(Gtva.toArray()), num_Gtva, 3, "Gtva" + tt);
		t_is(DComplexFactory2D.dense.make(Gtvv.toArray()), num_Gtvv, 2, "Gtvv" + tt);

		t_end();
	}

}
