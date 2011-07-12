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

package edu.cornell.pserc.jpower.tdouble.cases;

import cern.colt.matrix.tdouble.DoubleFactory2D;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_areas;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_branch;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_bus;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_gen;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_gencost;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_jpc;

/**
 * Power flow data for 9 bus, 3 generator case.
 *
 * @author Ray Zimmerman
 * @author Richard Lincoln
 *
 */
public class Djp_case9 {

	/**
	 * Based on data from Joe H. Chow's book, p. 70.
	 *
	 * @return a 9 bus, 3 generator case.
	 */
	public static Djp_jpc jp_case9() {

		Djp_jpc jpc = new Djp_jpc();

		/* JPOWER Case Format : Version 2 */
		jpc.version = "2";

		/* -----  Power Flow Data  ----- */

		/* system MVA base */
		jpc.baseMVA = 100;

		/* bus data */
		//	bus_i	type	Pd	Qd	Gs	Bs	area	Vm	Va	baseKV	zone	Vmax	Vmin
		jpc.bus = Djp_bus.fromMatrix( DoubleFactory2D.dense.make(new double[][] {
			{1,	3,	0,	0,	0,	0,	1,	1,	0,	345,	1,	1.1,	0.9},
			{2,	2,	0,	0,	0,	0,	1,	1,	0,	345,	1,	1.1,	0.9},
			{3,	2,	0,	0,	0,	0,	1,	1,	0,	345,	1,	1.1,	0.9},
			{4,	1,	0,	0,	0,	0,	1,	1,	0,	345,	1,	1.1,	0.9},
			{5,	1,	90,	30,	0,	0,	1,	1,	0,	345,	1,	1.1,	0.9},
			{6,	1,	0,	0,	0,	0,	1,	1,	0,	345,	1,	1.1,	0.9},
			{7,	1,	100,	35,	0,	0,	1,	1,	0,	345,	1,	1.1,	0.9},
			{8,	1,	0,	0,	0,	0,	1,	1,	0,	345,	1,	1.1,	0.9},
			{9,	1,	125,	50,	0,	0,	1,	1,	0,	345,	1,	1.1,	0.9}
		}) );

		/* generator data */
		//	bus	Pg	Qg	Qmax	Qmin	Vg	mBase	status	Pmax	Pmin	Pc1	Pc2	Qc1min	Qc1max	Qc2min	Qc2max	ramp_ag	ramp_10	ramp_30	ramp_q	apf
		jpc.gen = Djp_gen.fromMatrix( DoubleFactory2D.dense.make(new double[][] {
			{1,	0,	0,	300,	-300,	1,	100,	1,	250,	10,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0},
			{2,	163,	0,	300,	-300,	1,	100,	1,	300,	10,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0},
			{3,	85,	0,	300,	-300,	1,	100,	1,	270,	10,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0}
		}) );

		/* branch data */
		//	fbus	tbus	r	x	b	rateA	rateB	rateC	ratio	angle	status	angmin	angmax
		jpc.branch = Djp_branch.fromMatrix( DoubleFactory2D.dense.make(new double[][] {
			{1,	4,	0,	0.0576,	0,	250,	250,	250,	0,	0,	1,	-360,	360},
			{4,	5,	0.017,	0.092,	0.158,	250,	250,	250,	0,	0,	1,	-360,	360},
			{5,	6,	0.039,	0.17,	0.358,	150,	150,	150,	0,	0,	1,	-360,	360},
			{3,	6,	0,	0.0586,	0,	300,	300,	300,	0,	0,	1,	-360,	360},
			{6,	7,	0.0119,	0.1008,	0.209,	150,	150,	150,	0,	0,	1,	-360,	360},
			{7,	8,	0.0085,	0.072,	0.149,	250,	250,	250,	0,	0,	1,	-360,	360},
			{8,	2,	0,	0.0625,	0,	250,	250,	250,	0,	0,	1,	-360,	360},
			{8,	9,	0.032,	0.161,	0.306,	250,	250,	250,	0,	0,	1,	-360,	360},
			{9,	4,	0.01,	0.085,	0.176,	250,	250,	250,	0,	0,	1,	-360,	360},
		}) );

		/* -----  OPF Data  ----- */

		/* area data */
		//	area	refbus
		jpc.areas = Djp_areas.fromMatrix( DoubleFactory2D.dense.make(new double[][] {
			{1,	5}
		}) );

		/* generator cost data */
		//	1	startup	shutdow	n	x1	y1	...	xn	yn
		//	2	startup	shutdow	n	c(n-1)	...	c0
		jpc.gencost = Djp_gencost.fromMatrix( DoubleFactory2D.dense.make(new double[][] {
			{2,	1500,	0,	3,	0.11,	5,	150},
			{2,	2000,	0,	3,	0.085,	1.2,	600},
			{2,	3000,	0,	3,	0.1225,	1,	335}
		}) );

		return jpc;
	}
}
