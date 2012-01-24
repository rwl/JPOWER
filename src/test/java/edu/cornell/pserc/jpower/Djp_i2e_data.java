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

import cern.colt.matrix.tdouble.DoubleMatrix2D;
import edu.cornell.pserc.jpower.jpc.JPC;

public class Djp_i2e_data {

	public static DoubleMatrix2D i2e_data(JPC jpc, DoubleMatrix2D val,
			DoubleMatrix2D oldval, Ordering[] ordering, int dim) {
		// TODO Auto-generated method stub
		return null;
	}

	public static DoubleMatrix2D i2e_data(JPC jpc, DoubleMatrix2D val,
			DoubleMatrix2D oldval, Ordering[] ordering) {
		return i2e_data(jpc, val, oldval, ordering, 0);
	}

	public static DoubleMatrix2D i2e_data(JPC jpc, DoubleMatrix2D val,
			DoubleMatrix2D oldval, Ordering ordering, int dim) {
		return i2e_data(jpc, val, oldval, new Ordering[] {ordering}, dim);
	}

	public static DoubleMatrix2D i2e_data(JPC jpc, DoubleMatrix2D val,
			DoubleMatrix2D oldval, Ordering ordering) {
		return i2e_data(jpc, val, oldval, new Ordering[] {ordering}, 0);
	}

}
