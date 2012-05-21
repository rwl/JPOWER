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

import static edu.cornell.pserc.jpower.test.Djp_t_begin.t_begin;
import static edu.cornell.pserc.jpower.test.Djp_t_end.t_end;
import static edu.cornell.pserc.jpower.test.Djp_t_is.t_is;
import static edu.cornell.pserc.jpower.test.Djp_t_ok.t_ok;
import static edu.cornell.pserc.jpower.test.Djp_t_case9_dcline.t_case9_dcline;

import static edu.cornell.pserc.jpower.Djp_jpoption.jpoption;
import static edu.cornell.pserc.jpower.Djp_loadcase.loadcase;
import static edu.cornell.pserc.jpower.Djp_toggle_dcline.toggle_dcline;
import static edu.cornell.pserc.jpower.opf.Djp_runopf.runopf;
import static edu.cornell.pserc.jpower.pf.Djp_runpf.runpf;
import static edu.cornell.pserc.jpower.opf.Djp_rundcopf.rundcopf;
import static edu.cornell.pserc.jpower.pf.Djp_rundcpf.rundcpf;

import static edu.cornell.pserc.jpower.jpc.Bus.BUS_AREA;
import static edu.cornell.pserc.jpower.jpc.Bus.BASE_KV;
import static edu.cornell.pserc.jpower.jpc.Bus.VMIN;
import static edu.cornell.pserc.jpower.jpc.Bus.VM;
import static edu.cornell.pserc.jpower.jpc.Bus.VA;
import static edu.cornell.pserc.jpower.jpc.Bus.LAM_P;
import static edu.cornell.pserc.jpower.jpc.Bus.LAM_Q;
import static edu.cornell.pserc.jpower.jpc.Bus.MU_VMAX;
import static edu.cornell.pserc.jpower.jpc.Bus.MU_VMIN;

import static edu.cornell.pserc.jpower.jpc.Gen.GEN_BUS;
import static edu.cornell.pserc.jpower.jpc.Gen.QMAX;
import static edu.cornell.pserc.jpower.jpc.Gen.QMIN;
import static edu.cornell.pserc.jpower.jpc.Gen.MBASE;
import static edu.cornell.pserc.jpower.jpc.Gen.APF;
import static edu.cornell.pserc.jpower.jpc.Gen.PG;
import static edu.cornell.pserc.jpower.jpc.Gen.QG;
import static edu.cornell.pserc.jpower.jpc.Gen.VG;
import static edu.cornell.pserc.jpower.jpc.Gen.MU_PMAX;
import static edu.cornell.pserc.jpower.jpc.Gen.MU_QMIN;

import static edu.cornell.pserc.jpower.jpc.Branch.ANGMAX;
import static edu.cornell.pserc.jpower.jpc.Branch.PF;
import static edu.cornell.pserc.jpower.jpc.Branch.QT;
import static edu.cornell.pserc.jpower.jpc.Branch.MU_SF;
import static edu.cornell.pserc.jpower.jpc.Branch.MU_ST;
import static edu.cornell.pserc.jpower.jpc.Branch.MU_ANGMIN;
import static edu.cornell.pserc.jpower.jpc.Branch.MU_ANGMAX;

import static edu.emory.mathcs.utils.Utils.irange;
import static edu.emory.mathcs.utils.Utils.icat;
import static edu.emory.mathcs.utils.Utils.scat;
import static edu.emory.mathcs.utils.Utils.nonzero;
import static edu.emory.mathcs.utils.Utils.ifunc;
import static edu.emory.mathcs.utils.Utils.dfunc;

import java.util.Map;

import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;

import edu.cornell.pserc.jpower.Djp_toggle_dcline;
import edu.cornell.pserc.jpower.jpc.Branch;
import edu.cornell.pserc.jpower.jpc.Bus;
import edu.cornell.pserc.jpower.jpc.Cost;
import edu.cornell.pserc.jpower.jpc.DCLine;
import edu.cornell.pserc.jpower.jpc.Gen;
import edu.cornell.pserc.jpower.jpc.JPC;

/**
 * Tests for DC line extension in toggle_dcline.
 *
 * @author Ray Zimmerman
 * @author Richard Lincoln
 *
 */
public class Djp_t_dcline {

	/**
	 * Tests for DC line extension in toggle_dcline.
	 *
	 * @param quiet
	 */
	public static void t_pf(boolean quiet) {
		String t, t0;
		JPC jpc, jpc0, jpc1, r, r0, rp, casefile;
		Map<String, Double> jpopt;
		DoubleMatrix2D expected, expected1, expected2, tmp;
		int ndc;
		double verbose;
		int[] ib_data, ib_voltage, ib_lam, ib_mu, ig_data, ig_disp, ig_mu, ibr_data,
			ibr_flow, ibr_mu, ibr_angmu, ff, tt;
		double[] row;

		int num_tests = 50;

		t_begin(num_tests, quiet);


		casefile = t_case9_dcline();
		verbose = quiet ? 0.0 : 0.0;

		t0 = "";
		jpopt = jpoption("OPF_VIOLATION", 1e-6, "PDIPM_GRADTOL", 1e-8,
			"PDIPM_COMPTOL", 1e-8, "PDIPM_COSTTOL", 1e-9);
		jpopt = jpoption(jpopt, "OPF_ALG", 560.0, "OPF_ALG_DC", 200.0);
		jpopt = jpoption(jpopt, "OUT_ALL", 0.0, "VERBOSE", verbose);

		// set up indices
		ib_data     = icat(irange(BUS_AREA + 1), irange(BASE_KV, VMIN + 1));
		ib_voltage  = new int[] {VM, VA};
		ib_lam      = new int[] {LAM_P, LAM_Q};
		ib_mu       = new int[] {MU_VMAX, MU_VMIN};
		ig_data     = icat(new int[] {GEN_BUS, QMAX, QMIN}, irange(MBASE, APF + 1));
		ig_disp     = new int[] {PG, QG, VG};
		ig_mu       = irange(MU_PMAX, MU_QMIN + 1);
		ibr_data    = irange(ANGMAX + 1);
		ibr_flow    = irange(PF, QT + 1);
		ibr_mu      = new int[] {MU_SF, MU_ST};
		ibr_angmu   = new int[] {MU_ANGMIN, MU_ANGMAX};

		// load case
		jpc0 = loadcase(casefile);
		jpc0.dclinecost = null;
		jpc = jpc0.copy();
		jpc = toggle_dcline(jpc, "on");
		jpc = toggle_dcline(jpc, "off");
		ndc = jpc.dcline.size();

		// run AC OPF w/o DC lines
		t = t0 + "AC OPF (no DC lines) : ";
		r0 = runopf(jpc0, jpopt);
		t_ok(r0.success, t + "success");
		r = runopf(jpc, jpopt);
		t_ok(r.success, t + "success");
		t_is(r.f, r0.f, 8, t + "f");
		t_is(	r.bus.toMatrix().viewSelection(null, ib_data),
			r0.bus.toMatrix().viewSelection(null, ib_data),
			10, t + "bus data");
		t_is(	r.bus.toMatrix().viewSelection(null, ib_voltage),
			r0.bus.toMatrix().viewSelection(null, ib_voltage),
			3, t + "bus voltage");
		t_is(	r.bus.toMatrix().viewSelection(null, ib_lam),
			r0.bus.toMatrix().viewSelection(null, ib_lam),
			3, t + "bus lambda");
		t_is(	r.bus.toMatrix().viewSelection(null, ib_mu),
			r0.bus.toMatrix().viewSelection(null, ib_mu),
			2, t + "bus mu");
		t_is(	r.gen.toMatrix().viewSelection(null, ig_data),
			r0.gen.toMatrix().viewSelection(null, ig_data),
			10, t + "gen data");
		t_is(	r.gen.toMatrix().viewSelection(null, ig_disp),
			r0.gen.toMatrix().viewSelection(null, ig_disp),
			3, t + "gen dispatch");
		t_is(	r.gen.toMatrix().viewSelection(null, ig_mu),
			r0.gen.toMatrix().viewSelection(null, ig_mu),
			3, t + "gen mu");
		t_is(	r.branch.toMatrix().viewSelection(null, ibr_data),
			r0.branch.toMatrix().viewSelection(null, ibr_data),
			10, t + "branch data");
		t_is(	r.branch.toMatrix().viewSelection(null, ibr_flow),
			r0.branch.toMatrix().viewSelection(null, ibr_flow),
			3, t + "branch flow");
		t_is(	r.branch.toMatrix().viewSelection(null, ibr_mu),
			r0.branch.toMatrix().viewSelection(null, ibr_mu),
			2, t + "branch mu");

		t = t0 + "AC PF (no DC lines) : ";
		jpc1 = new JPC();
		jpc1.baseMVA = r.baseMVA;
		jpc1.bus = Bus.fromMatrix( r.bus.toMatrix().viewSelection(null, irange(VMIN + 1)) );
		jpc1.gen = Gen.fromMatrix( r.gen.toMatrix().viewSelection(null, irange(APF + 1)) );
		jpc1.branch = Branch.fromMatrix( r.branch.toMatrix().viewSelection(null, irange(ANGMAX + 1)) );
		jpc1.gencost = r.gencost.copy();
		jpc1.dcline = DCLine.fromMatrix( r.dcline.toMatrix().viewSelection(null, irange(DCLine.LOSS1 + 1)) );

		jpc1.bus.Vm.assign(1.0);
		jpc1.bus.Va.assign(0.0);
		rp = runpf(jpc1, jpopt);
		t_ok(rp.success, t + "success");
		t_is(	rp.bus.toMatrix().viewSelection(null, ib_voltage),
			r.bus.toMatrix().viewSelection(null, ib_voltage),
			3, t + "bus voltage");
		t_is(	rp.gen.toMatrix().viewSelection(null, ig_disp),
			r.gen.toMatrix().viewSelection(null, ig_disp),
			3, t + "gen dispatch");
		t_is(	rp.branch.toMatrix().viewSelection(null, ibr_flow),
			r.branch.toMatrix().viewSelection(null, ibr_flow),
			3, t + "branch flow");

		// run with DC lines
		t = t0 + "AC OPF (with DC lines) : ";
		jpc = toggle_dcline(jpc, "on");
		r = runopf(jpc, jpopt);
		t_ok(r.success, t + "success");
		expected = DoubleFactory2D.dense.make(new double[][] {
			{10,	8.9,	-10,	10,	1.0674,	1.0935},
			{2.2776,	2.2776,	0,	0,	1.0818,	1.0665},
			{0,	0,	0,	0,	1.0000,	1.0000},
			{10,	9.5,	0.0563,	-10,	1.0778,	1.0665}
		});
		t_is(r.dcline.toMatrix().viewSelection(null, irange(DCLine.PF, DCLine.VT + 1)),
				expected, 4, t + "P Q V");
		expected = DoubleFactory2D.dense.make(new double[][] {
			{0,	0.8490,	0.6165,	0,	0,	0.2938},
			{0,	0,	0,	0.4290,	0.0739,	0},
			{0,	0,	0,	0,	0,	0},
			{0,	7.2209,	0,	0,	0.0739,	0}
		});
		t_is(r.dcline.toMatrix().viewSelection(null, irange(DCLine.MU_PMIN, DCLine.MU_QMAXT + 1)),
				expected, 3, t + "mu");

		t = t0 + "AC PF (with DC lines) : ";
		jpc1 = new JPC();
		jpc1.baseMVA = r.baseMVA;
		jpc1.bus = Bus.fromMatrix( r.bus.toMatrix().viewSelection(null, irange(VMIN + 1)) );
		jpc1.gen = Gen.fromMatrix( r.gen.toMatrix().viewSelection(null, irange(APF + 1)) );
		jpc1.branch = Branch.fromMatrix( r.branch.toMatrix().viewSelection(null, irange(ANGMAX + 1)) );
		jpc1.gencost = r.gencost.copy();
		jpc1.dcline = DCLine.fromMatrix( r.dcline.toMatrix().viewSelection(null, irange(DCLine.LOSS1 + 1)) );
		jpc1 = toggle_dcline(jpc1, "on");
		jpc1.bus.Vm.assign(1.0);
		jpc1.bus.Va.assign(0.0);
		rp = runpf(jpc1, jpopt);
		t_ok(rp.success, t + "success");
		t_is(	rp.bus.toMatrix().viewSelection(null, ib_voltage),
			r.bus.toMatrix().viewSelection(null, ib_voltage),
			3, t + "bus voltage");
		//t_is(	rp.gen.toMatrix().viewSelection(null, ig_disp),
		//	r.gen.toMatrix().viewSelection(null, ig_disp),
		//	3, t + "gen dispatch");
		t_is(	rp.gen.toMatrix().viewSelection(irange(2), ig_disp),
			r.gen.toMatrix().viewSelection(irange(2), ig_disp),
			3, t + "gen dispatch");
		t_is(	rp.gen.Pg.get(2), r.gen.Pg.get(2), 3, t + "gen dispatch");
		t_is(	rp.gen.Qg.get(2) + rp.dcline.Qf.get(0),
			r.gen.Qg.get(2) + r.dcline.Qf.get(0),
			3, t + "gen dispatch");
		t_is(	rp.branch.toMatrix().viewSelection(null, ibr_flow),
			r.branch.toMatrix().viewSelection(null, ibr_flow),
			3, t + "branch flow");

		// add appropriate P and Q injections and check angles and generation when running PF
		t = t0 + "AC PF (with equivalent injections) : ";
		jpc1 = new JPC();
		jpc1.baseMVA = r.baseMVA;
		jpc1.bus = Bus.fromMatrix( r.bus.toMatrix().viewSelection(null, irange(VMIN + 1)) );
		jpc1.gen = Gen.fromMatrix( r.gen.toMatrix().viewSelection(null, irange(APF + 1)) );
		jpc1.branch = Branch.fromMatrix( r.branch.toMatrix().viewSelection(null, irange(ANGMAX + 1)) );
		jpc1.gencost = r.gencost.copy();
		jpc1.dcline = DCLine.fromMatrix( r.dcline.toMatrix().viewSelection(null, irange(DCLine.LOSS1 + 1)) );
		jpc1.bus.Vm.assign(1.0);
		jpc1.bus.Va.assign(0.0);
		for (int k = 0; k < ndc; k++) {
			if (jpc1.dcline.br_status.get(k) > 0) {
				ff = nonzero(jpc1.bus.bus_i.copy().assign(ifunc.equals( jpc1.dcline.f_bus.get(k) )));
				tt = nonzero(jpc1.bus.bus_i.copy().assign(ifunc.equals( jpc1.dcline.t_bus.get(k) )));

				jpc1.bus.Pd.viewSelection(ff).assign(jpc1.bus.Pd.viewSelection(ff));
				jpc1.bus.Pd.viewSelection(ff).assign(dfunc.plus( r.dcline.Pf.get(k) ));

				jpc1.bus.Qd.viewSelection(ff).assign(jpc1.bus.Qd.viewSelection(ff));
				jpc1.bus.Qd.viewSelection(ff).assign(dfunc.minus( r.dcline.Qf.get(k) ));

				jpc1.bus.Pd.viewSelection(tt).assign(jpc1.bus.Pd.viewSelection(tt));
				jpc1.bus.Pd.viewSelection(tt).assign(dfunc.minus( r.dcline.Pt.get(k) ));

				jpc1.bus.Qd.viewSelection(tt).assign(jpc1.bus.Qd.viewSelection(tt));
				jpc1.bus.Qd.viewSelection(tt).assign(dfunc.minus(r.dcline.Qt.get(k) ));

				jpc1.bus.Vm.viewSelection(ff).assign(r.dcline.Vf.get(k));
				jpc1.bus.Vm.viewSelection(tt).assign(r.dcline.Vt.get(k));

				jpc1.bus.bus_type.viewSelection(ff).assign(JPC.PV);
				jpc1.bus.bus_type.viewSelection(tt).assign(JPC.PV);
			}
		}
		rp = runpf(jpc1, jpopt);
		t_ok(rp.success, t + "success");
		t_is(	rp.bus.toMatrix().viewSelection(null, ib_voltage),
			r.bus.toMatrix().viewSelection(null, ib_voltage),
			3, t + "bus voltage");
		t_is(	rp.gen.toMatrix().viewSelection(null, ig_disp),
			r.gen.toMatrix().viewSelection(null, ig_disp),
			3, t + "gen dispatch");
		t_is(	rp.branch.toMatrix().viewSelection(null, ibr_flow),
			r.branch.toMatrix().viewSelection(null, ibr_flow),
			3, t + "branch flow");

		// test DC OPF
		t = t0 + "DC OPF (with DC lines) : ";
		jpc = jpc0.copy();
		jpc.gen.Pmin.set(0, 10);
		jpc.branch.rate_a.set(4, 100);
		jpc = toggle_dcline(jpc, "on");
		r = rundcopf(jpc, jpopt);
		t_ok(r.success, t + "success");
		expected = DoubleFactory2D.dense.make(new double[][] {
			{10,	8.9,	0,	0,	1.01,	1},
			{2,	2,	0,	0,	1,	1},
			{0,	0,	0,	0,	1,	1},
			{10,	9.5,	0,	0,	1,	0.98}
		});
		t_is(r.dcline.toMatrix().viewSelection(null, irange(DCLine.PF, DCLine.VT + 1)),
				expected, 4, t + "P Q V");
		expected = DoubleFactory2D.dense.make(new double[][] {
			{0,	1.8602,	0,	0,	0,	0},
			{1.8507,	0,	0,	0,	0,	0},
			{0,	0,	0,	0,	0,	0},
			{0,	0.2681,	0,	0,	0,	0}
		});
		t_is(r.dcline.toMatrix().viewSelection(null, irange(DCLine.MU_PMIN, DCLine.MU_QMAXT + 1)),
				expected, 3, t + "mu");

		t = t0 + "DC PF (with DC lines) : ";
		jpc1 = new JPC();
		jpc1.baseMVA = r.baseMVA;
		jpc1.bus = Bus.fromMatrix( r.bus.toMatrix().viewSelection(null, irange(VMIN + 1)) );
		jpc1.gen = Gen.fromMatrix( r.gen.toMatrix().viewSelection(null, irange(APF + 1)) );
		jpc1.branch = Branch.fromMatrix( r.branch.toMatrix().viewSelection(null, irange(ANGMAX + 1)) );
		jpc1.gencost = r.gencost.copy();
		jpc1.dcline = DCLine.fromMatrix( r.dcline.toMatrix().viewSelection(null, irange(DCLine.LOSS1 + 1)) );
		jpc1 = toggle_dcline(jpc1, "on");
		jpc1.bus.Va.assign(0.0);
		rp = rundcpf(jpc1, jpopt);
		t_ok(rp.success, t + "success");
		t_is(	rp.bus.toMatrix().viewSelection(null, ib_voltage),
			r.bus.toMatrix().viewSelection(null, ib_voltage),
			3, t + "bus voltage");
		t_is(	rp.gen.toMatrix().viewSelection(null, ig_disp),
			r.gen.toMatrix().viewSelection(null, ig_disp),
			3, t + "gen dispatch");
		t_is(	rp.branch.toMatrix().viewSelection(null, ibr_flow),
			r.branch.toMatrix().viewSelection(null, ibr_flow),
			3, t + "branch flow");

		// add appropriate P injections and check angles and generation when running PF
		t = t0 + "DC PF (with equivalent injections) : ";
		jpc1 = new JPC();
		jpc1.baseMVA = r.baseMVA;
		jpc1.bus = Bus.fromMatrix( r.bus.toMatrix().viewSelection(null, irange(VMIN + 1)) );
		jpc1.gen = Gen.fromMatrix( r.gen.toMatrix().viewSelection(null, irange(APF + 1)) );
		jpc1.branch = Branch.fromMatrix( r.branch.toMatrix().viewSelection(null, irange(ANGMAX + 1)) );
		jpc1.gencost = r.gencost.copy();
		jpc1.dcline = DCLine.fromMatrix( r.dcline.toMatrix().viewSelection(null, irange(DCLine.LOSS1 + 1)) );
		jpc1.bus.Va.assign(0.0);
		for (int k = 0; k < ndc; k++) {
			if (jpc1.dcline.br_status.get(k) > 0) {
				ff = nonzero(jpc1.bus.bus_i.copy().assign(ifunc.equals( jpc1.dcline.f_bus.get(k) )));
				tt = nonzero(jpc1.bus.bus_i.copy().assign(ifunc.equals( jpc1.dcline.t_bus.get(k) )));

				jpc1.bus.Pd.viewSelection(ff).assign(jpc1.bus.Pd.viewSelection(ff));
				jpc1.bus.Pd.viewSelection(ff).assign(dfunc.plus(r.dcline.Pf.get(k)));
				jpc1.bus.Pd.viewSelection(tt).assign(jpc1.bus.Pd.viewSelection(tt));
				jpc1.bus.Pd.viewSelection(tt).assign(dfunc.minus(r.dcline.Pt.get(k)));
				jpc1.bus.bus_type.viewSelection(ff).assign(JPC.PV);
				jpc1.bus.bus_type.viewSelection(tt).assign(JPC.PV);
			}
		}
		rp = rundcpf(jpc1, jpopt);
		t_ok(rp.success, t + "success");
		t_is(	rp.bus.toMatrix().viewSelection(null, ib_voltage),
			r.bus.toMatrix().viewSelection(null, ib_voltage),
			3, t + "bus voltage");
		t_is(	rp.gen.toMatrix().viewSelection(null, ig_disp),
			r.gen.toMatrix().viewSelection(null, ig_disp),
			3, t + "gen dispatch");
		t_is(	rp.branch.toMatrix().viewSelection(null, ibr_flow),
			r.branch.toMatrix().viewSelection(null, ibr_flow),
			3, t + "branch flow");

		// run with DC lines
		t = t0 + "AC OPF (with DC lines + poly cost) : ";
		jpc = loadcase(casefile);
		jpc = toggle_dcline(jpc, "on");
		r = runopf(jpc, jpopt);
		t_ok(r.success, t + "success");
		expected1 = DoubleFactory2D.dense.make(new double[][] {
			{10,	8.9,	-10,	10,	1.0663,	1.0936},
			{7.8429,	7.8429,	0,	0,	1.0809,	1.0667},
			{0,	0,	0,	0,	1.0000,	1.0000},
			{6.0549,	5.7522,	-0.5897,	-10,	1.0778,	1.0667}
		});
		t_is(r.dcline.toMatrix().viewSelection(null, irange(DCLine.PF, DCLine.VT + 1)),
			expected1, 4, t + "P Q V");
		expected2 = DoubleFactory2D.dense.make(new double[][] {
			{0,	0.7605,	0.6226,	0,	0,	0.2980},
			{0,	0,	0,	0.4275,	0.0792,	0},
			{0,	0,	0,	0,	0,	0},
			{0,	0,	0,	0,	0.0792,	0}
		});
		t_is(r.dcline.toMatrix().viewSelection(null, irange(DCLine.MU_PMIN, DCLine.MU_QMAXT + 1)),
			expected2, 3, t + "mu");

		row = new double[] {2, 0, 0, 4, 0, 0, 7.3, 0};
		tmp = jpc.dclinecost.toMatrix().viewSelection(new int[] {3}, irange(8)).assign(row);
		jpc.dclinecost = Cost.fromMatrix(tmp);
		r = runopf(jpc, jpopt);
		t_ok(r.success, t + "success");
		t_is(r.dcline.toMatrix().viewSelection(null, irange(DCLine.PF, DCLine.VT + 1)),
				expected1, 4, t + "P Q V");
		t_is(r.dcline.toMatrix().viewSelection(null, irange(DCLine.MU_PMIN, DCLine.MU_QMAXT + 1)),
				expected2, 3, t + "mu");

		t = t0 + "AC OPF (with DC lines + pwl cost) : ";
		row = new double[] {1, 0, 0, 2, 0, 0, 10, 73};
		tmp = jpc.dclinecost.toMatrix().viewSelection(new int[] {3}, irange(8)).assign(row);
		jpc.dclinecost = Cost.fromMatrix(tmp);
		r = runopf(jpc, jpopt);
		t_ok(r.success, t + "success");
		t_is(r.dcline.toMatrix().viewSelection(null, irange(DCLine.PF, DCLine.VT + 1)),
				expected1, 4, t + "P Q V");
		t_is(r.dcline.toMatrix().viewSelection(null, irange(DCLine.MU_PMIN, DCLine.MU_QMAXT + 1)),
				expected2, 3, t + "mu");

		t_end();
	}

}
