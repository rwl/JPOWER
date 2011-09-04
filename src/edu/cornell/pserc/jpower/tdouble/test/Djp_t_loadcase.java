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

import static edu.cornell.pserc.jpower.tdouble.test.Djp_t_begin.t_begin;

public class Djp_t_loadcase {

	public static void t_loadcase() {
		t_loadcase(false);
	}

	public static void t_loadcase(boolean quiet) {

		t_begin(240, quiet);

	}

	public static void main(String[] args) {
		t_loadcase(false);
	}

}
