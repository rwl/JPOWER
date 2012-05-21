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

package edu.cornell.pserc.jpower.cases;

import cern.colt.matrix.tdouble.DoubleFactory2D;
import edu.cornell.pserc.jpower.jpc.Branch;
import edu.cornell.pserc.jpower.jpc.Bus;
import edu.cornell.pserc.jpower.jpc.Gen;
import edu.cornell.pserc.jpower.jpc.GenCost;
import edu.cornell.pserc.jpower.jpc.JPC;

/**
 * Power flow data for 6 bus, 3 gen case from Wood & Wollenberg.
 *
 * @author Ray Zimmerman
 * @author Richard Lincoln
 *
 */
public class Djp_case6ww {

	/**
	 * This is the 6 bus example from pp. 104, 112, 119, 123-124, 549 of
	 * "Power Generation, Operation, and Control, 2nd Edition",
	 * by Allen. J. Wood and Bruce F. Wollenberg, John Wiley & Sons, NY, Jan 1996.
	 *
	 * @return a 6 bus, 3 gen case from Wood & Wollenberg.
	 */
	public static JPC jp_case6ww() {

		JPC jpc = new JPC();

		/* JPOWER Case Format : Version 2 */
		jpc.version = "2";

		/* -----  Power Flow Data  ----- */

		/* system MVA base */
		jpc.baseMVA = 100;

		/* bus data */
		//	bus_i	type	Pd	Qd	Gs	Bs	area	Vm	Va	baseKV	zone	Vmax	Vmin
		jpc.bus = Bus.fromMatrix( DoubleFactory2D.dense.make(new double[][] {
			{1,	3,	0,	0,	0,	0,	1,	1.05,	0,	230,	1,	1.05,	1.05},
			{2,	2,	0,	0,	0,	0,	1,	1.05,	0,	230,	1,	1.05,	1.05},
			{3,	2,	0,	0,	0,	0,	1,	1.07,	0,	230,	1,	1.07,	1.07},
			{4,	1,	70,	70,	0,	0,	1,	1,	0,	230,	1,	1.05,	0.95},
			{5,	1,	70,	70,	0,	0,	1,	1,	0,	230,	1,	1.05,	0.95},
			{6,	1,	70,	70,	0,	0,	1,	1,	0,	230,	1,	1.05,	0.95}
		}) );

		/* generator data */
		//	bus	Pg	Qg	Qmax	Qmin	Vg	mBase	status	Pmax	Pmin	Pc1	Pc2	Qc1min	Qc1max	Qc2min	Qc2max	ramp_ag	ramp_10	ramp_30	ramp_q	apf
		jpc.gen = Gen.fromMatrix( DoubleFactory2D.dense.make(new double[][] {
			{1,	0,	0,	100,	-100,	1.05,	100,	1,	200,	50,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0},
			{2,	50,	0,	100,	-100,	1.05,	100,	1,	150,	37.5,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0},
			{3,	60,	0,	100,	-100,	1.07,	100,	1,	180,	45,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0}
		}) );

		/* branch data */
		//	fbus	tbus	r	x	b	rateA	rateB	rateC	ratio	angle	status	angmin	angmax
		jpc.branch = Branch.fromMatrix( DoubleFactory2D.dense.make(new double[][] {
			{1,	2,	0.1,	0.2,	0.04,	40,	40,	40,	0,	0,	1,	-360,	360},
			{1,	4,	0.05,	0.2,	0.04,	60,	60,	60,	0,	0,	1,	-360,	360},
			{1,	5,	0.08,	0.3,	0.06,	40,	40,	40,	0,	0,	1,	-360,	360},
			{2,	3,	0.05,	0.25,	0.06,	40,	40,	40,	0,	0,	1,	-360,	360},
			{2,	4,	0.05,	0.1,	0.02,	60,	60,	60,	0,	0,	1,	-360,	360},
			{2,	5,	0.1,	0.3,	0.04,	30,	30,	30,	0,	0,	1,	-360,	360},
			{2,	6,	0.07,	0.2,	0.05,	90,	90,	90,	0,	0,	1,	-360,	360},
			{3,	5,	0.12,	0.26,	0.05,	70,	70,	70,	0,	0,	1,	-360,	360},
			{3,	6,	0.02,	0.1,	0.02,	80,	80,	80,	0,	0,	1,	-360,	360},
			{4,	5,	0.2,	0.4,	0.08,	20,	20,	20,	0,	0,	1,	-360,	360},
			{5,	6,	0.1,	0.3,	0.06,	40,	40,	40,	0,	0,	1,	-360,	360},
		}) );

		/* -----  OPF Data  ----- */

		/* generator cost data */
		//	1	startup	shutdow	n	x1	y1	...	xn	yn
		//	2	startup	shutdow	n	c(n-1)	...	c0
		jpc.gencost = GenCost.fromMatrix( DoubleFactory2D.dense.make(new double[][] {
			{2,	0,	0,	3,	.00533,	11.669,	213.1},
			{2,	0,	0,	3,	.00889,	10.333,	200},
			{2,	0,	0,	3,	.00741,	10.833,	240}
		}) );

		return jpc;
	}
}
