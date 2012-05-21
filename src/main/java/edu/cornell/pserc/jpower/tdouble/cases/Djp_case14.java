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

package edu.cornell.pserc.jpower.tdouble.cases;

import cern.colt.matrix.tdouble.DoubleFactory2D;
import edu.cornell.pserc.jpower.tdouble.jpc.Branch;
import edu.cornell.pserc.jpower.tdouble.jpc.Bus;
import edu.cornell.pserc.jpower.tdouble.jpc.Gen;
import edu.cornell.pserc.jpower.tdouble.jpc.GenCost;
import edu.cornell.pserc.jpower.tdouble.jpc.JPC;

/**
 * Power flow data for IEEE 14 bus test case.
 *
 * @author Ray Zimmerman
 * @author Richard Lincoln
 *
 */
public class Djp_case14 {

	/**
	 * This data was converted from IEEE Common Data Format
	 * (ieee14cdf.txt) on 20-Sep-2004 by cdf2matp, rev. 1.11
	 * See end of file for warnings generated during conversion.
	 *
	 * Converted from IEEE CDF file from:
	 * 	http://www.ee.washington.edu/research/pstca/
	 *
	 * @return power flow data for IEEE 14 bus test case.
	 */
	public static JPC jp_case14() {

		JPC jpc = new JPC();

		/* JPOWER Case Format : Version 2 */
		jpc.version = "2";

		/* -----  Power Flow Data  ----- */

		/* system MVA base */
		jpc.baseMVA = 100;

		/* bus data */
		//	bus_i	type	Pd	Qd	Gs	Bs	area	Vm	Va	baseKV	zone	Vmax	Vmin
		jpc.bus = Bus.fromMatrix( DoubleFactory2D.dense.make(new double[][] {
			{1,	3,	0,	0,	0,	0,	1,	1.06,	0,	0,	1,	1.06,	0.94},
			{2,	2,	21.7,	12.7,	0,	0,	1,	1.045,	-4.98,	0,	1,	1.06,	0.94},
			{3,	2,	94.2,	19,	0,	0,	1,	1.01,	-12.72,	0,	1,	1.06,	0.94},
			{4,	1,	47.8,	-3.9,	0,	0,	1,	1.019,	-10.33,	0,	1,	1.06,	0.94},
			{5,	1,	7.6,	1.6,	0,	0,	1,	1.02,	-8.78,	0,	1,	1.06,	0.94},
			{6,	2,	11.2,	7.5,	0,	0,	1,	1.07,	-14.22,	0,	1,	1.06,	0.94},
			{7,	1,	0,	0,	0,	0,	1,	1.062,	-13.37,	0,	1,	1.06,	0.94},
			{8,	2,	0,	0,	0,	0,	1,	1.09,	-13.36,	0,	1,	1.06,	0.94},
			{9,	1,	29.5,	16.6,	0,	19,	1,	1.056,	-14.94,	0,	1,	1.06,	0.94},
			{10,	1,	9,	5.8,	0,	0,	1,	1.051,	-15.1,	0,	1,	1.06,	0.94},
			{11,	1,	3.5,	1.8,	0,	0,	1,	1.057,	-14.79,	0,	1,	1.06,	0.94},
			{12,	1,	6.1,	1.6,	0,	0,	1,	1.055,	-15.07,	0,	1,	1.06,	0.94},
			{13,	1,	13.5,	5.8,	0,	0,	1,	1.05,	-15.16,	0,	1,	1.06,	0.94},
			{14,	1,	14.9,	5,	0,	0,	1,	1.036,	-16.04,	0,	1,	1.06,	0.94},
		}) );

		/* generator data */
		//	bus	Pg	Qg	Qmax	Qmin	Vg	mBase	status	Pmax	Pmin	Pc1	Pc2	Qc1min	Qc1max	Qc2min	Qc2max	ramp_ag	ramp_10	ramp_30	ramp_q	apf
		jpc.gen = Gen.fromMatrix( DoubleFactory2D.dense.make(new double[][] {
			{1,	232.4,	-16.9,	10,	0,	1.06,	100,	1,	332.4,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0},
			{2,	40,	42.4,	50,	-40,	1.045,	100,	1,	140,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0},
			{3,	0,	23.4,	40,	0,	1.01,	100,	1,	100,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0},
			{6,	0,	12.2,	24,	-6,	1.07,	100,	1,	100,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0},
			{8,	0,	17.4,	24,	-6,	1.09,	100,	1,	100,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0},
		}) );

		/* branch data */
		//	fbus	tbus	r	x	b	rateA	rateB	rateC	ratio	angle	status	angmin	angmax
		jpc.branch = Branch.fromMatrix( DoubleFactory2D.dense.make(new double[][] {
			{1,	2,	0.01938,	0.05917,	0.0528,	9900,	0,	0,	0,	0,	1,	-360,	360},
			{1,	5,	0.05403,	0.22304,	0.0492,	9900,	0,	0,	0,	0,	1,	-360,	360},
			{2,	3,	0.04699,	0.19797,	0.0438,	9900,	0,	0,	0,	0,	1,	-360,	360},
			{2,	4,	0.05811,	0.17632,	0.034,	9900,	0,	0,	0,	0,	1,	-360,	360},
			{2,	5,	0.05695,	0.17388,	0.0346,	9900,	0,	0,	0,	0,	1,	-360,	360},
			{3,	4,	0.06701,	0.17103,	0.0128,	9900,	0,	0,	0,	0,	1,	-360,	360},
			{4,	5,	0.01335,	0.04211,	0,	9900,	0,	0,	0,	0,	1,	-360,	360},
			{4,	7,	0,		0.20912,	0,	9900,	0,	0,	0.978,	0,	1,	-360,	360},
			{4,	9,	0,		0.55618,	0,	9900,	0,	0,	0.969,	0,	1,	-360,	360},
			{5,	6,	0,		0.25202,	0,	9900,	0,	0,	0.932,	0,	1,	-360,	360},
			{6,	11,	0.09498,	0.1989,		0,	9900,	0,	0,	0,	0,	1,	-360,	360},
			{6,	12,	0.12291,	0.25581,	0,	9900,	0,	0,	0,	0,	1,	-360,	360},
			{6,	13,	0.06615,	0.13027,	0,	9900,	0,	0,	0,	0,	1,	-360,	360},
			{7,	8,	0,		0.17615,	0,	9900,	0,	0,	0,	0,	1,	-360,	360},
			{7,	9,	0,		0.11001,	0,	9900,	0,	0,	0,	0,	1,	-360,	360},
			{9,	10,	0.03181,	0.0845,		0,	9900,	0,	0,	0,	0,	1,	-360,	360},
			{9,	14,	0.12711,	0.27038,	0,	9900,	0,	0,	0,	0,	1,	-360,	360},
			{10,	11,	0.08205,	0.19207,	0,	9900,	0,	0,	0,	0,	1,	-360,	360},
			{12,	13,	0.22092,	0.19988,	0,	9900,	0,	0,	0,	0,	1,	-360,	360},
			{13,	14,	0.17093,	0.34802,	0,	9900,	0,	0,	0,	0,	1,	-360,	360},
		}) );

		/* -----  OPF Data  ----- */

		/* generator cost data */
		//	1	startup	shutdow	n	x1	y1	...	xn	yn
		//	2	startup	shutdow	n	c(n-1)	...	c0
		jpc.gencost = GenCost.fromMatrix( DoubleFactory2D.dense.make(new double[][] {
			{2,	0,	0,	3,	0.0430293,	20,	0},
			{2,	0,	0,	3,	0.25,		20,	0},
			{2,	0,	0,	3,	0.01,		40,	0},
			{2,	0,	0,	3,	0.01,		40,	0},
			{2,	0,	0,	3,	0.01,		40,	0},
		}) );

		return jpc;
	}
}