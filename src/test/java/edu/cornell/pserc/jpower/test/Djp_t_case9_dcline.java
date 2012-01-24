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

import edu.cornell.pserc.jpower.jpc.Areas;
import edu.cornell.pserc.jpower.jpc.Branch;
import edu.cornell.pserc.jpower.jpc.Bus;
import edu.cornell.pserc.jpower.jpc.DCLine;
import edu.cornell.pserc.jpower.jpc.Gen;
import edu.cornell.pserc.jpower.jpc.Cost;
import edu.cornell.pserc.jpower.jpc.JPC;

/**
 * Same as t_case9_opfv2 with addition of DC line data.
 *
 * @author Ray Zimmerman
 * @author Richard Lincoln
 *
 */
public class Djp_t_case9_dcline {

	/**
	 * Based on data from Joe H. Chow's book, p. 70.
	 *
	 * @return a 9 bus, 3 generator case, with OPF data.
	 */
	public static JPC t_case9_dcline() {

		JPC jpc = new JPC();

		/* JPOWER Case Format : Version 2 */
		jpc.version = "2";

		/* -----  Power Flow Data  ----- */

		/* system MVA base */
		jpc.baseMVA = 100;

		/* bus data */
		//	bus_i	type	Pd	Qd	Gs	Bs	area	Vm	Va	baseKV	zone	Vmax	Vmin
		jpc.bus = Bus.fromMatrix( new double[][] {
			{1,	3,	0,	0,	0,	0,	1,	1,	0,	345,	1,	1.1,	0.9},
			{2,	2,	0,	0,	0,	0,	1,	1,	0,	345,	1,	1.1,	0.9},
			{30,	2,	0,	0,	0,	0,	1,	1,	0,	345,	1,	1.1,	0.9},
			{4,	1,	0,	0,	0,	0,	1,	1,	0,	345,	1,	1.1,	0.9},
			{5,	1,	90,	30,	0,	0,	1,	1,	0,	345,	1,	1.1,	0.9},
			{6,	1,	0,	0,	0,	0,	1,	1,	0,	345,	1,	1.1,	0.9},
			{7,	1,	100,	35,	0,	0,	1,	1,	0,	345,	1,	1.1,	0.9},
			{8,	1,	0,	0,	0,	0,	1,	1,	0,	345,	1,	1.1,	0.9},
			{9,	1,	125,	50,	0,	0,	1,	1,	0,	345,	1,	1.1,	0.9},
		});

		/* generator data */
		//	bus	Pg	Qg	Qmax	Qmin	Vg	mBase	status	Pmax	Pmin	Pc1	Pc2	Qc1min	Qc1max	Qc2min	Qc2max	ramp_ag	ramp_10	ramp_30	ramp_q	apf
		jpc.gen = Gen.fromMatrix( new double[][] {
			{1,	0,	0,	300,	-300,	1,	100,	1,	250,	90,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0},
			{2,	163,	0,	300,	-300,	1,	100,	1,	300,	10,	0,	200,	-20,	20,	-10,	10,	0,	0,	0,	0,	0},
			{30,	85,	0,	300,	-300,	1,	100,	1,	270,	10,	0,	200,	-30,	30,	-15,	15,	0,	0,	0,	0,	0},
		});

		/* branch data */
		//	fbus	tbus	r	x	b	rateA	rateB	rateC	ratio	angle	status	angmin	angmax
		jpc.branch = Branch.fromMatrix( new double[][] {
			{1,	4,	0,	0.0576,	0,	0,	250,	250,	0,	0,	1,	-360,	2.48},
			{4,	5,	0.017,	0.092,	0.158,	0,	250,	250,	0,	0,	1,	-360,	360},
			{5,	6,	0.039,	0.17,	0.358,	150,	150,	150,	0,	0,	1,	-360,	360},
			{30,	6,	0,	0.0586,	0,	0,	300,	300,	0,	0,	1,	-360,	360},
			{6,	7,	0.0119,	0.1008,	0.209,	40,	150,	150,	0,	0,	1,	-360,	360},
			{7,	8,	0.0085,	0.072,	0.149,	250,	250,	250,	0,	0,	1,	-360,	360},
			{8,	2,	0,	0.0625,	0,	250,	250,	250,	0,	0,	1,	-360,	360},
			{8,	9,	0.032,	0.161,	0.306,	250,	250,	250,	0,	0,	1,	-360,	360},
			{9,	4,	0.01,	0.085,	0.176,	250,	250,	250,	0,	0,	1,	-2,	360},
		});

		/* -----  OPF Data  ----- */

		/* area data */
		//	area	refbus
		jpc.areas = Areas.fromMatrix( new double[][] {
			{1,	5}
		});

		/* generator cost data */
		//	1	startup	shutdow	n	x1	y1	...	xn	yn
		//	2	startup	shutdow	n	c(n-1)	...	c0
		jpc.gencost = Cost.fromMatrix( new double[][] {
			{1,	0,	0,	4,	0,	0,	100,	2500,	200,	5500,	250,	7250},
			{1,	0,	0,	4,	0,	0,	100,	2000,	200,	4403.5,	270,	6363.5},
			{2,	0,	0,	2,	15,	0,	0,	0,	0,	0,	0,	0},
		});

		/* -----  DC Line Data  ----- */
		//	fbus	tbus	status	Pf	Pt	Qf	Qt	Vf	Vt	Pmin	Pmax	QminF	QmaxF	QminT	QmaxT	loss0	loss1
		jpc.dcline = DCLine.fromMatrix( new double[][] {
			{30,	4,	1,	10,	8.9,	0,	0,	1.01,	1,	1,	10,	-10,	10,	-10,	10,	1,	0.01},
			{7,	9,	1,	2,	1.96,	0,	0,	1,	1,	2,	10,	0,	0,	0,	0,	0,	0},
			{5,	8,	0,	0,	0,	0,	0,	1,	1,	1,	10,	-10,	10,	-10,	10,	0,	0},
			{5,	9,	1,	10,	9.5,	0,	0,	1,	0.98,	0,	10,	-10,	10,	-10,	10,	0,	0.05}
		});

		/* DC line cost data */
		// 1	startup	shutdown	n	x1	y1	...	xn	yn
		// 2	startup	shutdown	n	c(n-1)	...	c0
		jpc.dclinecost = Cost.fromMatrix( new double[][] {
			{2,	0,	0,	2,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0},
			{2,	0,	0,	2,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0},
			{2,	0,	0,	2,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0},
			{2,	0,	0,	2,	7.3,	0,	0,	0,	0,	0,	0,	0,	0,	0}
		});

		return jpc;
	}
}
