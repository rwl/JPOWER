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

package edu.cornell.pserc.jpower.test;

import cern.colt.matrix.tdouble.DoubleFactory2D;
import edu.cornell.pserc.jpower.tdouble.jpc.Areas;
import edu.cornell.pserc.jpower.tdouble.jpc.Branch;
import edu.cornell.pserc.jpower.tdouble.jpc.Bus;
import edu.cornell.pserc.jpower.tdouble.jpc.Gen;
import edu.cornell.pserc.jpower.tdouble.jpc.GenCost;
import edu.cornell.pserc.jpower.tdouble.jpc.JPC;

public class Djp_t_case_int {

	public static JPC t_case_int() {

		JPC jpc = new JPC();

		/* JPOWER Case Format : Version 2 */
		jpc.version = "2";

		/* -----  Power Flow Data  ----- */

		/* system MVA base */
		jpc.baseMVA = 100;

		/* bus data */
		//	bus_i	type	Pd	Qd	Gs	Bs	area	Vm	Va	baseKV	zone	Vmax	Vmin
		jpc.bus = Bus.fromMatrix( DoubleFactory2D.dense.make(new double[][] {
			{0,	3,	0,	0,	0,	0,	1,	1,	0,	345,	1,	1.1,	0.9},
			{1,	2,	0,	0,	0,	0,	1,	1,	0,	345,	1,	1.1,	0.9},
			{2,	2,	0,	0,	0,	0,	1,	1,	0,	345,	1,	1.1,	0.9},
			{3,	1,	0,	0,	0,	0,	1,	1,	0,	345,	1,	1.1,	0.9},
			{4,	1,	90,	30,	0,	0,	1,	1,	0,	345,	1,	1.1,	0.9},
			{5,	1,	0,	0,	0,	0,	1,	1,	0,	345,	1,	1.1,	0.9},
			{6,	1,	100,	35,	0,	0,	1,	1,	0,	345,	1,	1.1,	0.9},
			{7,	1,	0,	0,	0,	0,	1,	1,	0,	345,	1,	1.1,	0.9},
			{8,	1,	125,	50,	0,	0,	1,	1,	0,	345,	1,	1.1,	0.9}
		}) );

		/* generator data */
		//	bus	Pg	Qg	Qmax	Qmin	Vg	mBase	status	Pmax	Pmin	Pc1	Pc2	Qc1min	Qc1max	Qc2min	Qc2max	ramp_ag	ramp_10	ramp_30	ramp_q	apf
		jpc.gen = Gen.fromMatrix( DoubleFactory2D.dense.make(new double[][] {
			{0,	0,	0,	300,	-300,	1,	100,	1,	250,	90,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0},
			{1,	163,	0,	300,	-300,	1,	100,	1,	300,	10,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0},
			{2,	85,	0,	300,	-300,	1,	100,	1,	270,	10,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0}
		}) );

		/* branch data */
		//	fbus	tbus	r	x	b	rateA	rateB	rateC	ratio	angle	status	angmin	angmax
		jpc.branch = Branch.fromMatrix( DoubleFactory2D.dense.make(new double[][] {
			{0,	3,	0,	0.0576,	0,	0,	250,	250,	0,	0,	1,	-360,	360},
			{3,	4,	0.017,	0.092,	0.158,	0,	250,	250,	0,	0,	1,	-360,	360},
			{4,	5,	0.039,	0.17,	0.358,	150,	150,	150,	0,	0,	1,	-360,	360},
			{2,	5,	0,	0.0586,	0,	0,	300,	300,	0,	0,	1,	-360,	360},
			{5,	6,	0.0119,	0.1008,	0.209,	40,	150,	150,	0,	0,	1,	-360,	360},
			{6,	7,	0.0085,	0.072,	0.149,	250,	250,	250,	0,	0,	1,	-360,	360},
			{7,	1,	0,	0.0625,	0,	250,	250,	250,	0,	0,	1,	-360,	360},
			{7,	8,	0.032,	0.161,	0.306,	250,	250,	250,	0,	0,	1,	-360,	360},
			{8,	3,	0.01,	0.085,	0.176,	250,	250,	250,	0,	0,	1,	-360,	360}
		}) );

		/* -----  OPF Data  ----- */

		/* area data */
		//	area	refbus
		jpc.areas = Areas.fromMatrix( DoubleFactory2D.dense.make(new double[][] {
			{1,	4},
		}) );

		/* generator cost data */
		//	1	startup	shutdow	n	x1	y1	...	xn	yn
		//	2	startup	shutdow	n	c(n-1)	...	c0
		jpc.gencost = GenCost.fromMatrix( DoubleFactory2D.dense.make(new double[][] {
			{1,	0,	0,	4,	0,	0,	100,	2000,	200,	4403.5,	270,	6363.5},
			{1,	0,	0,	4,	0,	0,	100,	2500,	200,	5500,	250,	7250},
			{2,	0,	0,	2,	15,	0,	0,	0,	0,	0,	0,	0}
		}) );

		jpc.A = DoubleFactory2D.dense.make(new double[][] {
			{1,	2,	3,	4,	5,	7,	8,	9,	10,	11,	12,	13,	14,	15,	17,	18,	19,	20,	24,	22,	21,	28,	26,	25,	29,	30},
			{2,	4,	6,	8,	10,	14,	16,	18,	20,	22,	24,	26,	28,	30,	34,	36,	38,	40,	48,	44,	42,	56,	52,	50,	58,	60}
		});

		jpc.N = DoubleFactory2D.dense.make(new double[][] {
			{30,	29,	28,	27,	26,	24,	23,	22,	21,	20,	19,	18,	17,	16,	14,	13,	12,	11,	7,	9,	10,	3,	5,	6,	2,	1},
			{60,	58,	56,	54,	52,	48,	46,	44,	42,	40,	38,	36,	34,	32,	28,	26,	24,	22,	14,	18,	20,	6,	10,	12,	4,	2}
		});

		return jpc;
	}

}
