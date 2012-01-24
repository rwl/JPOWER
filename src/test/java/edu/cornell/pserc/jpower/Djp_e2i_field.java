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

import java.lang.reflect.Field;

import cern.colt.matrix.tdouble.DoubleMatrix2D;

import edu.cornell.pserc.jpower.jpc.JPC;

import static edu.cornell.pserc.jpower.Djp_e2i_data.e2i_data;

public class Djp_e2i_field {

	public static JPC e2i_field(JPC jpc, String[] field,
			Ordering[] ordering, int dim) {
		int i;
		DoubleMatrix2D val2;
		String fieldName;
		Field fld = null;
		Class<?> type;
		Object obj = jpc;

		try {
			for (i = 0; i < field.length; i++) {
				fieldName = field[i];
				fld = obj.getClass().getField(fieldName);
				if (i < field.length - 1)
					obj = fld.get(obj);
			}

			type = fld.getType();
			if (type == DoubleMatrix2D.class) {
				val2 = (DoubleMatrix2D) fld.get(obj);
				fld.set(jpc.order.external, val2.copy());
				fld.set(jpc, e2i_data(jpc, val2, ordering, dim));
			} else {
				throw new UnsupportedOperationException();
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		return jpc;
	}

	public static JPC e2i_field(JPC jpc, String field,
			Ordering[] ordering, int dim) {
		return e2i_field(jpc, new String[] {field}, ordering, dim);
	}

	public static JPC e2i_field(JPC jpc, String field, Ordering ordering) {
		return e2i_field(jpc, new String[] {field},
			new Ordering[] {ordering}, 0);
	}

	public static JPC e2i_field(JPC jpc, String field, Ordering ordering,
			int dim) {
		return e2i_field(jpc, new String[] {field}, new Ordering[] {ordering}, dim);
	}

	public static JPC e2i_field(JPC jpc, String field, Ordering[] ordering) {
		return e2i_field(jpc, new String[] {field}, ordering, 0);
	}

	public static JPC e2i_field(JPC jpc, String[] field, Ordering ordering) {
		return e2i_field(jpc, field, new Ordering[] {ordering}, 0);
	}

	public static JPC e2i_field(JPC jpc, String[] field,
			Ordering ordering, int dim) {
		return e2i_field(jpc, field, new Ordering[] {ordering}, dim);
	}

}
