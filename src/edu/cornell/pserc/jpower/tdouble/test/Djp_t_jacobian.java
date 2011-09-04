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

package edu.cornell.pserc.jpower.tdouble.test;

import java.util.Map;

import cern.colt.matrix.AbstractMatrix;
import cern.colt.matrix.tdcomplex.DComplexFactory2D;
import cern.colt.matrix.tdcomplex.DComplexMatrix1D;
import cern.colt.matrix.tdcomplex.DComplexMatrix2D;
import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tint.IntMatrix1D;
import cern.jet.math.tdcomplex.DComplexFunctions;
import cern.jet.math.tdouble.DoubleFunctions;

import static cern.colt.util.tdouble.Util.dfunc;
import static cern.colt.util.tdouble.Util.cfunc;
import static cern.colt.util.tdouble.Util.polar;

import edu.cornell.pserc.jpower.tdouble.jpc.Branch;
import edu.cornell.pserc.jpower.tdouble.jpc.Bus;
import edu.cornell.pserc.jpower.tdouble.jpc.Gen;
import edu.cornell.pserc.jpower.tdouble.jpc.JPC;

import static edu.cornell.pserc.jpower.tdouble.Djp_ext2int.ext2int;
import static edu.cornell.pserc.jpower.tdouble.Djp_jpoption.jpoption;
import static edu.cornell.pserc.jpower.tdouble.Djp_loadcase.loadcase;
import static edu.cornell.pserc.jpower.tdouble.cases.Djp_case30.case30;
import static edu.cornell.pserc.jpower.tdouble.opf.Djp_dAbr_dV.dAbr_dV;
import static edu.cornell.pserc.jpower.tdouble.opf.Djp_dIbr_dV.dIbr_dV;
import static edu.cornell.pserc.jpower.tdouble.opf.Djp_dSbr_dV.dSbr_dV;
import static edu.cornell.pserc.jpower.tdouble.pf.Djp_dSbus_dV.dSbus_dV;
import static edu.cornell.pserc.jpower.tdouble.pf.Djp_makeYbus.makeYbus;
import static edu.cornell.pserc.jpower.tdouble.pf.Djp_runpf.runpf;

import static edu.cornell.pserc.jpower.tdouble.test.Djp_t_begin.t_begin;
import static edu.cornell.pserc.jpower.tdouble.test.Djp_t_is.t_is;
import static edu.cornell.pserc.jpower.tdouble.test.Djp_t_ok.t_ok;
import static edu.cornell.pserc.jpower.tdouble.test.Djp_t_end.t_end;

public class Djp_t_jacobian {

	public static void t_jacobian() {
		t_jacobian(false);
	}

	@SuppressWarnings("static-access")
	public static void t_jacobian(boolean quiet) {
		Map<String, Double> opt;
		JPC jpc, r;
		DComplexMatrix2D[] Y, dSbus_dV;
		DComplexMatrix2D Ybus, Yf, Yt, Ybus_full, Yf_full, Yt_full,
			dSbus_dVm_full, dSbus_dVa_full, dSbus_dVm, dSbus_dVa,
			dSbus_dVm_sp, dSbus_dVa_sp, num_dSbus_dVm, num_dSbus_dVa,
			dSf_dVa_full, dSf_dVm_full, dSt_dVa_full, dSt_dVm_full,
			dSf_dVa, dSf_dVm, dSt_dVa, dSt_dVm,
			dSf_dVa_sp, dSf_dVm_sp, dSt_dVa_sp, dSt_dVm_sp,
			Vmp, Vap, Vmpf, Vapf, Vmpt, Vapt, Sf2, St2,
			Smpf, Sapf, Smpt, Sapt,
			num_dSf_dVm, num_dSf_dVa, num_dSt_dVm, num_dSt_dVa,
			dIf_dVa_full, dIf_dVm_full, dIt_dVa_full, dIt_dVm_full,
			dIf_dVa, dIf_dVm, dIt_dVa, dIt_dVm,
			dIf_dVa_sp, dIf_dVm_sp, dIt_dVa_sp, dIt_dVm_sp,
			num_dIf_dVm, num_dIf_dVa, num_dIt_dVm, num_dIt_dVa,
			V2;
		DComplexMatrix1D V, Sf, St, If, It;
		DoubleMatrix2D[] dAbr_dV;
		DoubleMatrix2D dAf_dVa_full, dAf_dVm_full, dAt_dVa_full, dAt_dVm_full,
			dAf_dVa, dAf_dVm, dAt_dVa, dAt_dVm,
			dAf_dVa_sp, dAf_dVm_sp, dAt_dVa_sp, dAt_dVm_sp,
			num_dAf_dVm, num_dAf_dVa, num_dAt_dVm, num_dAt_dVa,
			Vm, Va;
		int[] f, t;
		int nl, nb;
		AbstractMatrix[] dSbr_dV, dIbr_dV;

		t_begin(28, quiet);

		// run powerflow to get solved case
		opt = jpoption("VERBOSE", 0.0, "OUT_ALL", 0.0);
		jpc = loadcase(case30());
		r = runpf(jpc, opt);

		// switch to internal bus numbering and build admittance matrices
		Object[] internal = ext2int(r.bus, r.gen, r.branch);
		//int[] i2e = (int[]) internal[0];
		Bus bus = (Bus) internal[1];
		//Djp_gen gen = (Djp_gen) internal[2];
		Branch branch = (Branch) internal[3];

		Y = makeYbus(r.baseMVA, bus, branch);
		Ybus = Y[0];
		Yf = Y[1];
		Yt = Y[2];

		Ybus_full   = DComplexFactory2D.dense.make(Ybus.toArray());
		Yf_full     = DComplexFactory2D.dense.make(Yf.toArray());
		Yt_full     = DComplexFactory2D.dense.make(Yt.toArray());

		Vm = DoubleFactory2D.dense.make(bus.Vm.toArray(), 1).viewDice();
		Va = DoubleFactory2D.dense.make(
				bus.Va.copy().assign(dfunc.chain(dfunc.mult(Math.PI), dfunc.div(180))).toArray(),
				1).viewDice();
		V = polar(bus.Vm, bus.Va, false);
		V2 = DComplexFactory2D.dense.make((int) V.size(), 1);
		V2.viewColumn(0).assign(V.toArray());
		f = branch.f_bus.toArray();       // list of "from" buses
		t = branch.t_bus.toArray();       // list of "to" buses
		nl = f.length;
		nb = (int) V.size();
		double pert = 1e-8;

		//-----  check dSbus_dV code  -----
		// full matrices
//		dSbus_dV = Djp_dSbus_dV.jp_dSbus_dV(Ybus_full, V);
//		dSbus_dVm_full = dSbus_dV[0];
//		dSbus_dVa_full = dSbus_dV[1];

		// sparse matrices
		dSbus_dV = dSbus_dV(Ybus, V);
		dSbus_dVm = dSbus_dV[0];
		dSbus_dVa = dSbus_dV[1];
		dSbus_dVm_sp = DComplexFactory2D.dense.make(dSbus_dVm.toArray());
		dSbus_dVa_sp = DComplexFactory2D.dense.make(dSbus_dVa.toArray());

		// compute numerically to compare
		DoubleMatrix2D pert_eye = DoubleFactory2D.dense.identity(nb).assign(dfunc.mult(pert));

		DoubleMatrix2D nb1 = DoubleFactory2D.dense.make(1, nb, 1);
		Vm.zMult(nb1, null).assign(pert_eye, dfunc.plus);
		Va.zMult(nb1, null);
		Vmp = polar(Vm.zMult(nb1, null).assign(pert_eye, dfunc.plus),
				Va.zMult(nb1, null));
		/** Vmp = (Vm*ones(1,nb) + pert*eye(nb,nb)) .* (exp(1j * Va) * ones(1,nb)); */

		Vap = polar(Vm.zMult(nb1, null),
				Va.zMult(nb1, null).assign(pert_eye, dfunc.plus));
		/** Vap = (Vm*ones(1,nb)) .* (exp(1j * (Va*ones(1,nb) + pert*eye(nb,nb)))); */

		DComplexMatrix2D nb1c, arg1, Vsq, arg3;
		nb1c = DComplexFactory2D.dense.make(1, nb).assign(1, 0);
		Vsq = V2.zMult(nb1c, null);

		arg1 = Ybus.zMult(Vmp, null).assign(cfunc.conj).assign(Vmp, cfunc.mult);
		arg3 = Ybus.zMult(Vsq, null).assign(cfunc.conj);
		num_dSbus_dVm = arg1.assign(arg3.assign(Vsq, cfunc.mult), cfunc.minus).assign(cfunc.div(pert));
		/** num_dSbus_dVm = full( (Vmp .* conj(Ybus * Vmp) - V*ones(1,nb) .* conj(Ybus * V*ones(1,nb))) / pert ); */

		arg1 = Ybus.zMult(Vap, null).assign(cfunc.conj).assign(Vap, cfunc.mult);
		arg3 = Ybus.zMult(Vsq, null).assign(cfunc.conj);
		num_dSbus_dVa = arg1.assign(arg3.assign(Vsq, cfunc.mult), cfunc.minus).assign(cfunc.div(pert));
		/** num_dSbus_dVa = full( (Vap .* conj(Ybus * Vap) - V*ones(1,nb) .* conj(Ybus * V*ones(1,nb))) / pert ); */

		t_is(dSbus_dVm_sp, num_dSbus_dVm, 5, "dSbus_dVm (sparse)");
		t_is(dSbus_dVa_sp, num_dSbus_dVa, 5, "dSbus_dVa (sparse)");
//		t_is(dSbus_dVm_full, num_dSbus_dVm, 5, "dSbus_dVm (full)");
//		t_is(dSbus_dVa_full, num_dSbus_dVa, 5, "dSbus_dVa (full)");

//		//-----  check dSbr_dV code  -----
//		// full matrices
//		dSbr_dV = Djp_dSbr_dV.jp_dSbr_dV(branch, Yf_full, Yt_full, V);
//		dSf_dVa_full = (DComplexMatrix2D) dSbr_dV[0];
//		dSf_dVm_full = (DComplexMatrix2D) dSbr_dV[1];
//		dSt_dVa_full = (DComplexMatrix2D) dSbr_dV[2];
//		dSt_dVm_full = (DComplexMatrix2D) dSbr_dV[3];
//		Sf = (DComplexMatrix1D) dSbr_dV[4];
//		St = (DComplexMatrix1D) dSbr_dV[5];
//
//		// sparse matrices
//		dSbr_dV = Djp_dSbr_dV.jp_dSbr_dV(branch, Yf, Yt, V);
//		dSf_dVa = (DComplexMatrix2D) dSbr_dV[0];
//		dSf_dVm = (DComplexMatrix2D) dSbr_dV[1];
//		dSt_dVa = (DComplexMatrix2D) dSbr_dV[2];
//		dSt_dVm = (DComplexMatrix2D) dSbr_dV[3];
//		Sf = (DComplexMatrix1D) dSbr_dV[4];
//		St = (DComplexMatrix1D) dSbr_dV[5];
//		dSf_dVa_sp = DComplexFactory2D.dense.make(dSf_dVa.toArray());
//		dSf_dVm_sp = DComplexFactory2D.dense.make(dSf_dVm.toArray());
//		dSt_dVa_sp = DComplexFactory2D.dense.make(dSt_dVa.toArray());
//		dSt_dVm_sp = DComplexFactory2D.dense.make(dSt_dVm.toArray());
//
//		// compute numerically to compare
//		Vmpf = Vmp.viewSelection(f, null);
//		Vapf = Vap.viewSelection(f, null);
//		Vmpt = Vmp.viewSelection(t, null);
//		Vapt = Vap.viewSelection(t, null);
//		Sf2 = V2.viewSelection(f, null).zMult(nb1c, null).assign(
//				Yf.zMult(V2.zMult(nb1c, null), null).assign(cfunc.conj), cfunc.mult);
//		/** Sf2 = (V(f)*ones(1,nb)) .* conj(Yf * V*ones(1,nb)); */
//		St2 = V2.viewSelection(t, null).zMult(nb1c, null).assign(
//				Yt.zMult(V2.zMult(nb1c, null), null).assign(cfunc.conj), cfunc.mult);
//		/** St2 = (V(t)*ones(1,nb)) .* conj(Yt * V*ones(1,nb)); */
//		Smpf = Yf.zMult(Vmp, null).assign(cfunc.conj).assign(Vmpf, cfunc.mult);
//		Sapf = Yf.zMult(Vap, null).assign(cfunc.conj).assign(Vapf, cfunc.mult);
//		Smpt = Yt.zMult(Vmp, null).assign(cfunc.conj).assign(Vmpt, cfunc.mult);
//		Sapt = Yt.zMult(Vap, null).assign(cfunc.conj).assign(Vapt, cfunc.mult);
//
//		num_dSf_dVm = DComplexFactory2D.dense.make( Smpf.copy().assign(Sf2, cfunc.minus).assign(cfunc.div(pert)).toArray() );
//		num_dSf_dVa = DComplexFactory2D.dense.make( Sapf.copy().assign(Sf2, cfunc.minus).assign(cfunc.div(pert)).toArray() );
//		num_dSt_dVm = DComplexFactory2D.dense.make( Smpt.copy().assign(St2, cfunc.minus).assign(cfunc.div(pert)).toArray() );
//		num_dSt_dVa = DComplexFactory2D.dense.make( Sapt.copy().assign(St2, cfunc.minus).assign(cfunc.div(pert)).toArray() );
//
//		t_is(dSf_dVm_sp, num_dSf_dVm, 5, "dSf_dVm (sparse)");
//		t_is(dSf_dVa_sp, num_dSf_dVa, 5, "dSf_dVa (sparse)");
//		t_is(dSt_dVm_sp, num_dSt_dVm, 5, "dSt_dVm (sparse)");
//		t_is(dSt_dVa_sp, num_dSt_dVa, 5, "dSt_dVa (sparse)");
//		t_is(dSf_dVm_full, num_dSf_dVm, 5, "dSf_dVm (full)");
//		t_is(dSf_dVa_full, num_dSf_dVa, 5, "dSf_dVa (full)");
//		t_is(dSt_dVm_full, num_dSt_dVm, 5, "dSt_dVm (full)");
//		t_is(dSt_dVa_full, num_dSt_dVa, 5, "dSt_dVa (full)");
//
//		//-----  check dAbr_dV code  -----
//		// full matrices
//		dAbr_dV = Djp_dAbr_dV.jp_dAbr_dV(dSf_dVa_full, dSf_dVm_full, dSt_dVa_full, dSt_dVm_full, Sf, St);
//		dAf_dVa_full = dAbr_dV[0];
//		dAf_dVm_full = dAbr_dV[1];
//		dAt_dVa_full = dAbr_dV[2];
//		dAt_dVm_full = dAbr_dV[3];
//		// sparse matrices
//		dAbr_dV = Djp_dAbr_dV.jp_dAbr_dV(dSf_dVa, dSf_dVm, dSt_dVa, dSt_dVm, Sf, St);
//		dAf_dVa = dAbr_dV[0];
//		dAf_dVm = dAbr_dV[1];
//		dAt_dVa = dAbr_dV[2];
//		dAt_dVm = dAbr_dV[3];
//		dAf_dVa_sp = DoubleFactory2D.dense.make(dAf_dVa.toArray());
//		dAf_dVm_sp = DoubleFactory2D.dense.make(dAf_dVm.toArray());
//		dAt_dVa_sp = DoubleFactory2D.dense.make(dAt_dVa.toArray());
//		dAt_dVm_sp = DoubleFactory2D.dense.make(dAt_dVm.toArray());
//
//		// compute numerically to compare
//		num_dAf_dVm = DoubleFactory2D.dense.make( Smpf.assign(cfunc.abs).assign(cfunc.square).assign( Sf2.assign(cfunc.abs).assign(cfunc.square), cfunc.minus ).assign(cfunc.div(pert)).toArray() );
//		num_dAf_dVa = DoubleFactory2D.dense.make( Sapf.assign(cfunc.abs).assign(cfunc.square).assign( Sf2.assign(cfunc.abs).assign(cfunc.square), cfunc.minus ).assign(cfunc.div(pert)).toArray() );
//		num_dAt_dVm = DoubleFactory2D.dense.make( Smpt.assign(cfunc.abs).assign(cfunc.square).assign( St2.assign(cfunc.abs).assign(cfunc.square), cfunc.minus ).assign(cfunc.div(pert)).toArray() );
//		num_dAt_dVa = DoubleFactory2D.dense.make( Sapt.assign(cfunc.abs).assign(cfunc.square).assign( St2.assign(cfunc.abs).assign(cfunc.square), cfunc.minus ).assign(cfunc.div(pert)).toArray() );
//
//		t_is(dAf_dVm_sp, num_dAf_dVm, 4, "dAf_dVm (sparse)");
//		t_is(dAf_dVa_sp, num_dAf_dVa, 4, "dAf_dVa (sparse)");
//		t_is(dAt_dVm_sp, num_dAt_dVm, 4, "dAt_dVm (sparse)");
//		t_is(dAt_dVa_sp, num_dAt_dVa, 4, "dAt_dVa (sparse)");
//		t_is(dAf_dVm_full, num_dAf_dVm, 4, "dAf_dVm (full)");
//		t_is(dAf_dVa_full, num_dAf_dVa, 4, "dAf_dVa (full)");
//		t_is(dAt_dVm_full, num_dAt_dVm, 4, "dAt_dVm (full)");
//		t_is(dAt_dVa_full, num_dAt_dVa, 4, "dAt_dVa (full)");
//
//		//-----  check dIbr_dV code  -----
//		// full matrices
//		dIbr_dV = Djp_dIbr_dV.jp_dIbr_dV(branch, Yf_full, Yt_full, V);
//		dIf_dVa_full = (DComplexMatrix2D) dIbr_dV[0];
//		dIf_dVm_full = (DComplexMatrix2D) dIbr_dV[1];
//		dIt_dVa_full = (DComplexMatrix2D) dIbr_dV[2];
//		dIt_dVm_full = (DComplexMatrix2D) dIbr_dV[3];
//		//If = (DComplexMatrix1D) dIbr_dV[4];
//		//It = (DComplexMatrix1D) dIbr_dV[5];
//
//		// sparse matrices
//		dIbr_dV = Djp_dIbr_dV.jp_dIbr_dV(branch, Yf, Yt, V);
//		dIf_dVa = (DComplexMatrix2D) dIbr_dV[0];
//		dIf_dVm = (DComplexMatrix2D) dIbr_dV[1];
//		dIt_dVa = (DComplexMatrix2D) dIbr_dV[2];
//		dIt_dVm = (DComplexMatrix2D) dIbr_dV[3];
//		//If = (DComplexMatrix1D) dIbr_dV[4];
//		//It = (DComplexMatrix1D) dIbr_dV[5];
//		dIf_dVa_sp = DComplexFactory2D.dense.make(dIf_dVa.toArray());
//		dIf_dVm_sp = DComplexFactory2D.dense.make(dIf_dVm.toArray());
//		dIt_dVa_sp = DComplexFactory2D.dense.make(dIt_dVa.toArray());
//		dIt_dVm_sp = DComplexFactory2D.dense.make(dIt_dVm.toArray());
//
//		// compute numerically to compare
//		num_dIf_dVm = DComplexFactory2D.dense.make( Yf.zMult(Vmp, null).assign(Yf.zMult(V2.zMult(nb1c, null), null), cfunc.minus).assign(cfunc.div(pert)).toArray() );
//		num_dIf_dVa = DComplexFactory2D.dense.make( Yf.zMult(Vap, null).assign(Yf.zMult(V2.zMult(nb1c, null), null), cfunc.minus).assign(cfunc.div(pert)).toArray() );
//		num_dIt_dVm = DComplexFactory2D.dense.make( Yt.zMult(Vmp, null).assign(Yt.zMult(V2.zMult(nb1c, null), null), cfunc.minus).assign(cfunc.div(pert)).toArray() );
//		num_dIt_dVa = DComplexFactory2D.dense.make( Yt.zMult(Vap, null).assign(Yt.zMult(V2.zMult(nb1c, null), null), cfunc.minus).assign(cfunc.div(pert)).toArray() );
//
//		t_is(dIf_dVm_sp, num_dIf_dVm, 5, "dIf_dVm (sparse)");
//		t_is(dIf_dVa_sp, num_dIf_dVa, 5, "dIf_dVa (sparse)");
//		t_is(dIt_dVm_sp, num_dIt_dVm, 5, "dIt_dVm (sparse)");
//		t_is(dIt_dVa_sp, num_dIt_dVa, 5, "dIt_dVa (sparse)");
//		t_is(dIf_dVm_full, num_dIf_dVm, 5, "dIf_dVm (full)");
//		t_is(dIf_dVa_full, num_dIf_dVa, 5, "dIf_dVa (full)");
//		t_is(dIt_dVm_full, num_dIt_dVm, 5, "dIt_dVm (full)");
//		t_is(dIt_dVa_full, num_dIt_dVa, 5, "dIt_dVa (full)");

		t_end();
	}

	public static void main(String[] args) {
		t_jacobian(false);
	}

}
