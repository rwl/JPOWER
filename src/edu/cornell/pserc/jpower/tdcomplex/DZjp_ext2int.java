/*
 * Copyright (C) 1996-2010 by Power System Engineering Research Center (PSERC)
 * Copyright (C) 2010 Richard Lincoln
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 *
 */

package edu.cornell.pserc.jpower.tdcomplex;

import java.util.Map;

import cern.colt.matrix.tdouble.DoubleMatrix2D;

public class DZjp_ext2int {

	/**
	 *
	 * @param bus
	 * @param gen
	 * @param branch
	 * @return
	 */
	public static Map<String, DoubleMatrix2D> jp_ext2int(DoubleMatrix2D bus,
			DoubleMatrix2D gen, DoubleMatrix2D branch) {
		return jp_ext2int(bus, gen, branch, null);
	}

	/**
	 *
	 * @param bus
	 * @param gen
	 * @param branch
	 * @param areas
	 * @return
	 */
	public static Map<String, DoubleMatrix2D> jp_ext2int(DoubleMatrix2D bus,
			DoubleMatrix2D gen, DoubleMatrix2D branch, DoubleMatrix2D areas) {
		return null;
	}

	/**
	 *
	 * @param jpc
	 * @return
	 */
	public static Map<String, DoubleMatrix2D> jp_ext2int(Map<String, DoubleMatrix2D> jpc) {
		return null;
	}

}
