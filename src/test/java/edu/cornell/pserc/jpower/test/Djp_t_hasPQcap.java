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
import edu.cornell.pserc.jpower.jpc.Gen;

import static edu.cornell.pserc.jpower.opf.Djp_hasPQcap.hasPQcap;

import static edu.cornell.pserc.jpower.test.Djp_t_begin.t_begin;
import static edu.cornell.pserc.jpower.test.Djp_t_is.t_is;
import static edu.cornell.pserc.jpower.test.Djp_t_end.t_end;

/**
 * Tests for hasPQcap.
 */
public class Djp_t_hasPQcap {

	public static void t_hasPQcap() {
		t_hasPQcap(false);
	}

	public static void t_hasPQcap(boolean quiet) {
		String t;

		t_begin(4, quiet);

		Gen gen = Gen.fromMatrix( DoubleFactory2D.dense.make(new double[][] {
			{1,	10,	0,	10,	-10,	1,	100,	1,	10,	2,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0},
			{1,	10,	0,	10,	-10,	1,	100,	1,	10,	2,	0,	20,	0,	12,	0,	2,	0,	0,	0,	0,	0},
			{1,	10,	0,	10,	-10,	1,	100,	1,	10,	2,	0,	20,	-15,	12,	-15,	2,	0,	0,	0,	0,	0},
			{1,	10,	0,	10,	-10,	1,	100,	1,	10,	2,	0,	20,	-12,	0,	-2,	0,	0,	0,	0,	0,	0},
			{1,	10,	0,	10,	-10,	1,	100,	1,	10,	2,	0,	20,	-12,	15,	-2,	15,	0,	0,	0,	0,	0},
			{1,	10,	0,	10,	-10,	1,	100,	1,	10,	2,	0,	20,	-12,	12,	-2,	2,	0,	0,	0,	0,	0},
			{1,	10,	0,	10,	-10,	1,	100,	1,	10,	2,	0,	20,	0,	12,	0,	8,	0,	0,	0,	0,	0},
			{1,	10,	0,	10,	-10,	1,	100,	1,	10,	2,	0,	20,	-15,	12,	-15,	8,	0,	0,	0,	0,	0},
			{1,	10,	0,	10,	-10,	1,	100,	1,	10,	2,	0,	20,	-12,	0,	-8,	0,	0,	0,	0,	0,	0},
			{1,	10,	0,	10,	-10,	1,	100,	1,	10,	2,	0,	20,	-12,	15,	-8,	15,	0,	0,	0,	0,	0},
			{1,	10,	0,	10,	-10,	1,	100,	1,	10,	2,	0,	20,	-12,	12,	-8,	8,	0,	0,	0,	0,	0}
		}) );

		t = "hasPQcap(gen)";
		t_is(hasPQcap(gen), new int[] {0, 1, 1, 1, 1, 1, 1, 0, 1, 0, 0}, 12, t);

		t = "hasPQcap(gen, \"B\")";
		t_is(hasPQcap(gen, "B"), new int[] {0, 1, 1, 1, 1, 1, 1, 0, 1, 0, 0}, 12, t);

		t = "hasPQcap(gen, \"U\")";
		t_is(hasPQcap(gen, "U"), new int[] {0, 1, 1, 1, 0, 1, 0, 0, 1, 0, 0}, 12, t);

		t = "hasPQcap(gen, \"L\")";
		t_is(hasPQcap(gen, "L"), new int[] {0, 1, 0, 1, 1, 1, 1, 0, 0, 0, 0}, 12, t);

		t_end();
	}

}
