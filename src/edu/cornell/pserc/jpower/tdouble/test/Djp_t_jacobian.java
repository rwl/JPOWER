package edu.cornell.pserc.jpower.tdouble.test;

import java.util.Map;

import cern.colt.matrix.AbstractMatrix;
import cern.colt.matrix.tdcomplex.DComplexFactory1D;
import cern.colt.matrix.tdcomplex.DComplexFactory2D;
import cern.colt.matrix.tdcomplex.DComplexMatrix1D;
import cern.colt.matrix.tdcomplex.DComplexMatrix1DTest;
import cern.colt.matrix.tdcomplex.DComplexMatrix2D;
import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tint.IntMatrix1D;
import cern.colt.util.tdouble.Djp_util;
import cern.jet.math.tdouble.DoubleFunctions;

import edu.cornell.pserc.jpower.tdouble.Djp_jpoption;
import edu.cornell.pserc.jpower.tdouble.Djp_loadcase;
import edu.cornell.pserc.jpower.tdouble.cases.Djp_case30;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_branch;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_bus;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_gen;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_jpc;
import edu.cornell.pserc.jpower.tdouble.opf.Djp_dSbr_dV;
import edu.cornell.pserc.jpower.tdouble.pf.Djp_dSbus_dV;
import edu.cornell.pserc.jpower.tdouble.pf.Djp_makeYbus;
import edu.cornell.pserc.jpower.tdouble.pf.Djp_runpf;

public class Djp_t_jacobian {

	private static final DoubleFunctions dfunc = DoubleFunctions.functions;

	public static void jp_t_jacobian() {
		jp_t_jacobian(false);
	}

	@SuppressWarnings("static-access")
	public static void jp_t_jacobian(boolean quiet) {
		Map<String, Double> opt;
		Djp_jpc jpc, r;
		DComplexMatrix2D[] Y;
		DComplexMatrix2D Ybus, Yf, Yt, Ybus_full, Yf_full, Yt_full;
		DoubleMatrix1D Vm, Va, Vmp, Vap;
		DComplexMatrix1D V, Sf, St;
		int[] f, t;
		int nl, nb;
		DoubleMatrix2D[] dSbus_dV;
		DoubleMatrix2D dSbus_dVm_full, dSbus_dVa_full, dSbus_dVm, dSbus_dVa,
			dSbus_dVm_sp, dSbus_dVa_sp, num_dSbus_dVm, num_dSbus_dVa,
			dSf_dVa_full, dSf_dVm_full, dSt_dVa_full, dSt_dVm_full;
		AbstractMatrix[] dSbr_dV;

		Djp_t_begin.jp_t_begin(28, quiet);

		// run powerflow to get solved case
		opt = Djp_jpoption.jp_jpoption("VERBOSE", 0.0, "OUT_ALL", 0.0);
		jpc = Djp_loadcase.jp_loadcase(Djp_case30.jp_case30());
		r = Djp_runpf.jp_runpf(jpc, opt);

		// switch to internal bus numbering and build admittance matrices
		Object[] internal = ext2int(r.bus, r.gen, r.branch);
		IntMatrix1D i2e = (IntMatrix1D) internal[0];
		Djp_bus bus = (Djp_bus) internal[1];
		Djp_gen gen = (Djp_gen) internal[2];
		Djp_branch branch = (Djp_branch) internal[3];
		Y = Djp_makeYbus.jp_makeYbus(r.baseMVA, bus, branch);
		Ybus = Y[0];
		Yf = Y[1];
		Yt = Y[2];
		Ybus_full   = DComplexFactory2D.dense.make(Ybus.toArray());
		Yf_full     = DComplexFactory2D.dense.make(Yf.toArray());
		Yt_full     = DComplexFactory2D.dense.make(Yt.toArray());
		Vm = bus.Vm.copy();
		Va = bus.Va.copy().assign(dfunc.chain(dfunc.mult(Math.PI), dfunc.div(180)));
		V = Djp_util.polar(Vm, Va, false);
		f = branch.f_bus.toArray();       // list of "from" buses
		t = branch.t_bus.toArray();       // list of "to" buses
		nl = f.length;
		nb = (int) V.size();
		double pert = 1e-8;

		//-----  check dSbus_dV code  -----
		// full matrices
		dSbus_dV = Djp_dSbus_dV.jp_dSbus_dV(Ybus_full, V);
		dSbus_dVm_full = dSbus_dV[0];
		dSbus_dVa_full = dSbus_dV[1];

		// sparse matrices
		dSbus_dV = Djp_dSbus_dV.jp_dSbus_dV(Ybus, V);
		dSbus_dVm = dSbus_dV[0];
		dSbus_dVa = dSbus_dV[1];
		dSbus_dVm_sp = DoubleFactory2D.dense.make(dSbus_dVm.toArray());
		dSbus_dVa_sp = DoubleFactory2D.dense.make(dSbus_dVa.toArray());

		// compute numerically to compare
		Vmp = (Vm*ones(1,nb) + pert*eye(nb,nb)) .* (exp(1j * Va) * ones(1,nb));
		Vap = (Vm*ones(1,nb)) .* (exp(1j * (Va*ones(1,nb) + pert*eye(nb,nb))));
		num_dSbus_dVm = full( (Vmp .* conj(Ybus * Vmp) - V*ones(1,nb) .* conj(Ybus * V*ones(1,nb))) / pert );
		num_dSbus_dVa = full( (Vap .* conj(Ybus * Vap) - V*ones(1,nb) .* conj(Ybus * V*ones(1,nb))) / pert );

		Djp_t_is.jp_t_is(dSbus_dVm_sp, num_dSbus_dVm, 5, "dSbus_dVm (sparse)");
		Djp_t_is.jp_t_is(dSbus_dVa_sp, num_dSbus_dVa, 5, "dSbus_dVa (sparse)");
		Djp_t_is.jp_t_is(dSbus_dVm_full, num_dSbus_dVm, 5, "dSbus_dVm (full)");
		Djp_t_is.jp_t_is(dSbus_dVa_full, num_dSbus_dVa, 5, "dSbus_dVa (full)");

		//-----  check dSbr_dV code  -----
		// full matrices
		dSbr_dV = Djp_dSbr_dV.jp_dSbr_dV(branch, Yf_full, Yt_full, V);
		dSf_dVa_full = (DoubleMatrix2D) dSbr_dV[0];
		dSf_dVm_full = (DoubleMatrix2D) dSbr_dV[1];
		dSt_dVa_full = (DoubleMatrix2D) dSbr_dV[2];
		dSt_dVm_full = (DoubleMatrix2D) dSbr_dV[3];
		Sf = (DComplexMatrix1D) dSbr_dV[4];
		St = (DComplexMatrix1D) dSbr_dV[5];

		// sparse matrices
		dSbr_dV = Djp_dSbr_dV.jp_dSbr_dV(branch, Yf, Yt, V);
		dSf_dVa = dSbr_dV[0];
		dSf_dVm = dSbr_dV[1];
		dSt_dVa = dSbr_dV[2];
		dSt_dVm = dSbr_dV[3];
		Sf = dSbr_dV[4];
		St = dSbr_dV[5];
		dSf_dVa_sp = DoubleFactory2D.dense.make(dSf_dVa.toArray());
		dSf_dVm_sp = DoubleFactory2D.dense.make(dSf_dVm.toArray());
		dSt_dVa_sp = DoubleFactory2D.dense.make(dSt_dVa.toArray());
		dSt_dVm_sp = DoubleFactory2D.dense.make(dSt_dVm.toArray());

		// compute numerically to compare
		Vmpf = Vmp(f,:);
		Vapf = Vap(f,:);
		Vmpt = Vmp(t,:);
		Vapt = Vap(t,:);
		Sf2 = (V(f)*ones(1,nb)) .* conj(Yf * V*ones(1,nb));
		St2 = (V(t)*ones(1,nb)) .* conj(Yt * V*ones(1,nb));
		Smpf = Vmpf .* conj(Yf * Vmp);
		Sapf = Vapf .* conj(Yf * Vap);
		Smpt = Vmpt .* conj(Yt * Vmp);
		Sapt = Vapt .* conj(Yt * Vap);

		num_dSf_dVm = full( (Smpf - Sf2) / pert );
		num_dSf_dVa = full( (Sapf - Sf2) / pert );
		num_dSt_dVm = full( (Smpt - St2) / pert );
		num_dSt_dVa = full( (Sapt - St2) / pert );

		t_is(dSf_dVm_sp, num_dSf_dVm, 5, "dSf_dVm (sparse)");
		t_is(dSf_dVa_sp, num_dSf_dVa, 5, "dSf_dVa (sparse)");
		t_is(dSt_dVm_sp, num_dSt_dVm, 5, "dSt_dVm (sparse)");
		t_is(dSt_dVa_sp, num_dSt_dVa, 5, "dSt_dVa (sparse)");
		t_is(dSf_dVm_full, num_dSf_dVm, 5, "dSf_dVm (full)");
		t_is(dSf_dVa_full, num_dSf_dVa, 5, "dSf_dVa (full)");
		t_is(dSt_dVm_full, num_dSt_dVm, 5, "dSt_dVm (full)");
		t_is(dSt_dVa_full, num_dSt_dVa, 5, "dSt_dVa (full)");

		//-----  check dAbr_dV code  -----
		// full matrices
		[dAf_dVa_full, dAf_dVm_full, dAt_dVa_full, dAt_dVm_full] = ...
		                        dAbr_dV(dSf_dVa_full, dSf_dVm_full, dSt_dVa_full, dSt_dVm_full, Sf, St);
		// sparse matrices
		[dAf_dVa, dAf_dVm, dAt_dVa, dAt_dVm] = ...
		                        dAbr_dV(dSf_dVa, dSf_dVm, dSt_dVa, dSt_dVm, Sf, St);
		dAf_dVa_sp = full(dAf_dVa);
		dAf_dVm_sp = full(dAf_dVm);
		dAt_dVa_sp = full(dAt_dVa);
		dAt_dVm_sp = full(dAt_dVm);

		// compute numerically to compare
		num_dAf_dVm = full( (abs(Smpf).^2 - abs(Sf2).^2) / pert );
		num_dAf_dVa = full( (abs(Sapf).^2 - abs(Sf2).^2) / pert );
		num_dAt_dVm = full( (abs(Smpt).^2 - abs(St2).^2) / pert );
		num_dAt_dVa = full( (abs(Sapt).^2 - abs(St2).^2) / pert );

		t_is(dAf_dVm_sp, num_dAf_dVm, 4, "dAf_dVm (sparse)");
		t_is(dAf_dVa_sp, num_dAf_dVa, 4, "dAf_dVa (sparse)");
		t_is(dAt_dVm_sp, num_dAt_dVm, 4, "dAt_dVm (sparse)");
		t_is(dAt_dVa_sp, num_dAt_dVa, 4, "dAt_dVa (sparse)");
		t_is(dAf_dVm_full, num_dAf_dVm, 4, "dAf_dVm (full)");
		t_is(dAf_dVa_full, num_dAf_dVa, 4, "dAf_dVa (full)");
		t_is(dAt_dVm_full, num_dAt_dVm, 4, "dAt_dVm (full)");
		t_is(dAt_dVa_full, num_dAt_dVa, 4, "dAt_dVa (full)");

		//-----  check dIbr_dV code  -----
		// full matrices
		[dIf_dVa_full, dIf_dVm_full, dIt_dVa_full, dIt_dVm_full, If, It] = dIbr_dV(branch, Yf_full, Yt_full, V);

		// sparse matrices
		[dIf_dVa, dIf_dVm, dIt_dVa, dIt_dVm, If, It] = dIbr_dV(branch, Yf, Yt, V);
		dIf_dVa_sp = full(dIf_dVa);
		dIf_dVm_sp = full(dIf_dVm);
		dIt_dVa_sp = full(dIt_dVa);
		dIt_dVm_sp = full(dIt_dVm);

		// compute numerically to compare
		num_dIf_dVm = full( (Yf * Vmp - Yf * V*ones(1,nb)) / pert );
		num_dIf_dVa = full( (Yf * Vap - Yf * V*ones(1,nb)) / pert );
		num_dIt_dVm = full( (Yt * Vmp - Yt * V*ones(1,nb)) / pert );
		num_dIt_dVa = full( (Yt * Vap - Yt * V*ones(1,nb)) / pert );

		t_is(dIf_dVm_sp, num_dIf_dVm, 5, "dIf_dVm (sparse)");
		t_is(dIf_dVa_sp, num_dIf_dVa, 5, "dIf_dVa (sparse)");
		t_is(dIt_dVm_sp, num_dIt_dVm, 5, "dIt_dVm (sparse)");
		t_is(dIt_dVa_sp, num_dIt_dVa, 5, "dIt_dVa (sparse)");
		t_is(dIf_dVm_full, num_dIf_dVm, 5, "dIf_dVm (full)");
		t_is(dIf_dVa_full, num_dIf_dVa, 5, "dIf_dVa (full)");
		t_is(dIt_dVm_full, num_dIt_dVm, 5, "dIt_dVm (full)");
		t_is(dIt_dVa_full, num_dIt_dVa, 5, "dIt_dVa (full)");

		t_end;
	}

	private static AbstractMatrix[] ext2int(Djp_bus bus, Djp_gen gen, Djp_branch branch) {
		// TODO Auto-generated method stub
		return null;
	}

}
