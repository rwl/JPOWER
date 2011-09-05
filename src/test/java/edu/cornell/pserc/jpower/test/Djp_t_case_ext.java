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

import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;

import static cern.colt.util.tdouble.Util.drange;
import static cern.colt.util.tdouble.Util.irange;

import edu.cornell.pserc.jpower.jpc.Areas;
import edu.cornell.pserc.jpower.jpc.Branch;
import edu.cornell.pserc.jpower.jpc.Bus;
import edu.cornell.pserc.jpower.jpc.Gen;
import edu.cornell.pserc.jpower.jpc.GenCost;

public class Djp_t_case_ext {

	public static JPCExt t_case_ext() {

		JPCExt jpc = new JPCExt();

		/* JPOWER Case Format : Version 2 */
		jpc.version = "2";

		/* -----  Power Flow Data  ----- */

		/* system MVA base */
		jpc.baseMVA = 100;

		/* bus data */
		//	bus_i	type	Pd	Qd	Gs	Bs	area	Vm	Va	baseKV	zone	Vmax	Vmin
		jpc.bus = Bus.fromMatrix( DoubleFactory2D.dense.make(new double[][] {
			{1,	3,	0,	0,	0,	0,	1,	1,	0,	345,	1,	1.1,	0.9},
			{2,	2,	0,	0,	0,	0,	1,	1,	0,	345,	1,	1.1,	0.9},
			{30,	2,	0,	0,	0,	0,	1,	1,	0,	345,	1,	1.1,	0.9},
			{4,	1,	0,	0,	0,	0,	1,	1,	0,	345,	1,	1.1,	0.9},
			{5,	1,	90,	30,	0,	0,	1,	1,	0,	345,	1,	1.1,	0.9},
			{20,	4,	0,	0,	0,	0,	1,	1,	0,	345,	1,	1.1,	0.9},
			{6,	1,	0,	0,	0,	0,	1,	1,	0,	345,	1,	1.1,	0.9},
			{7,	1,	100,	35,	0,	0,	1,	1,	0,	345,	1,	1.1,	0.9},
			{8,	1,	0,	0,	0,	0,	1,	1,	0,	345,	1,	1.1,	0.9},
			{9,	1,	125,	50,	0,	0,	1,	1,	0,	345,	1,	1.1,	0.9}
		}) );

		/* generator data */
		//	bus	Pg	Qg	Qmax	Qmin	Vg	mBase	status	Pmax	Pmin	Pc1	Pc2	Qc1min	Qc1max	Qc2min	Qc2max	ramp_ag	ramp_10	ramp_30	ramp_q	apf
		jpc.gen = Gen.fromMatrix( DoubleFactory2D.dense.make(new double[][] {
			{30,	85,	0,	300,	-300,	1,	100,	1,	270,	10,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0},
			{2,	163,	0,	300,	-300,	1,	100,	1,	300,	10,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0},
			{20,	20,	0,	300,	-300,	1,	100,	1,	200,	90,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0},
			{1,	0,	0,	300,	-300,	1,	100,	1,	250,	90,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0}
		}) );

		/* branch data */
		//	fbus	tbus	r	x	b	rateA	rateB	rateC	ratio	angle	status	angmin	angmax
		jpc.branch = Branch.fromMatrix( DoubleFactory2D.dense.make(new double[][] {
			{1,	4,	0,	0.0576,	0,	0,	250,	250,	0,	0,	1,	-360,	360},
			{4,	5,	0.017,	0.092,	0.158,	0,	250,	250,	0,	0,	1,	-360,	360},
			{5,	6,	0.039,	0.17,	0.358,	150,	150,	150,	0,	0,	1,	-360,	360},
			{30,	6,	0,	0.0586,	0,	0,	300,	300,	0,	0,	1,	-360,	360},
			{6,	7,	0.0119,	0.1008,	0.209,	40,	150,	150,	0,	0,	1,	-360,	360},
			{7,	8,	0.0085,	0.072,	0.149,	250,	250,	250,	0,	0,	1,	-360,	360},
			{8,	20,	0,	0.1,	0,	250,	250,	250,	0,	0,	1,	-360,	360},
			{8,	2,	0,	0.0625,	0,	250,	250,	250,	0,	0,	1,	-360,	360},
			{8,	9,	0.032,	0.161,	0.306,	250,	250,	250,	0,	0,	1,	-360,	360},
			{9,	4,	0.01,	0.085,	0.176,	250,	250,	250,	0,	0,	1,	-360,	360}
		}) );

		/* -----  OPF Data  ----- */

		/* area data */
		//	area	refbus
		jpc.areas = Areas.fromMatrix( DoubleFactory2D.dense.make(new double[][] {
			{2,	20},
			{1,	5}
		}) );

		/* generator cost data */
		//	1	startup	shutdow	n	x1	y1	...	xn	yn
		//	2	startup	shutdow	n	c(n-1)	...	c0
		jpc.gencost = GenCost.fromMatrix( DoubleFactory2D.dense.make(new double[][] {
			{2,	0,	0,	2,	15,	0,	0,	0,	0,	0,	0,	0},
			{1,	0,	0,	4,	0,	0,	100,	2500,	200,	5500,	250,	7250},
			{2,	0,	0,	2,	20,	0,	0,	0,	0,	0,	0,	0},
			{1,	0,	0,	4,	0,	0,	100,	2000,	200,	4403.5,	270,	6363.5}
		}) );

		jpc.A = DoubleFactory2D.dense.make(new double[][] {
			{1,	2,	3,	4,	5,	0,	7,	8,	9,	10,	11,	12,	13,	14,	15,	0,	17,	18,	19,	20,	21,	22,	0,	24,	25,	26,	0,	28,	29,	30},
			{2,	4,	6,	8,	10,	0,	14,	16,	18,	20,	22,	24,	26,	28,	30,	0,	34,	36,	38,	40,	42,	44,	0,	48,	50,	52,	0,	56,	58,	60}
		});

		jpc.N = DoubleFactory2D.dense.make(new double[][] {
			{30,	29,	28,	27,	26,	25,	24,	23,	22,	21,	20,	19,	18,	17,	16,	15,	14,	13,	12,	11,	10,	9,	8,	7,	6,	5,	4,	3,	2,	1},
			{60,	58,	56,	54,	52,	50,	48,	46,	44,	42,	40,	38,	36,	34,	32,	30,	28,	26,	24,	22,	20,	18,	16,	14,	12,	10,	8,	6,	4,	2},
		});

		jpc.xbus = DoubleFactory2D.dense.make(10, 10).assign( drange(100) );

		jpc.xgen = DoubleFactory2D.dense.make(4, 4).assign( drange(16) );

		jpc.xbranch = jpc.xbus.copy();

		jpc.xrows = DoubleFactory2D.dense.compose(new DoubleMatrix2D[][] {
				{jpc.xbranch.viewSelection(null, irange(4)),
					jpc.xgen,
					jpc.xbus.viewSelection(null, irange(4)),
					DoubleFactory2D.dense.make(2, 4, -1.0)}
		});

		jpc.xcols = jpc.xrows.viewDice().copy();

		return jpc;
	}

}
