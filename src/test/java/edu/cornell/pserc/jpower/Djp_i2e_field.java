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

package edu.cornell.pserc.jpower;

import edu.cornell.pserc.jpower.jpc.JPC;

public class Djp_i2e_field {

	public static JPC i2e_field(edu.cornell.pserc.jpower.jpc.JPC jpc, String[] field,
			Ordering[] ordering, int dim) {
		// TODO Auto-generated method stub
		return null;
	}

	public static JPC i2e_field(JPC jpc, String[] field,
			Ordering ordering, int dim) {
		return i2e_field(jpc, field, new Ordering[] {ordering}, dim);
	}

	public static JPC i2e_field(JPC jpc, String[] field,
			Ordering ordering) {
		return i2e_field(jpc, field, new Ordering[] {ordering}, 0);
	}

	public static JPC i2e_field(JPC jpc, String field,
			Ordering[] ordering, int dim) {
		return i2e_field(jpc, new String[] {field}, ordering, dim);
	}

	public static JPC i2e_field(JPC jpc, String field,
			Ordering[] ordering) {
		return i2e_field(jpc, new String[] {field}, ordering, 0);
	}

	public static JPC i2e_field(JPC jpc, String field,
			Ordering ordering, int dim) {
		return i2e_field(jpc, new String[] {field}, new Ordering[] {ordering}, dim);
	}

	public static JPC i2e_field(JPC jpc, String field,
			Ordering ordering) {
		return i2e_field(jpc, new String[] {field}, new Ordering[] {ordering}, 0);
	}

}
