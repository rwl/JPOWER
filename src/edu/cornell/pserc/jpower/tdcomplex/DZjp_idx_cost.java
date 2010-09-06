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

public interface DZjp_idx_cost {

	/* define cost models */
	public static final int PW_LINEAR   = 1;
	public static final int POLYNOMIAL  = 2;

	// define the indices
	public static final int MODEL       = 1;    // cost model, 1 = piecewise linear, 2 = polynomial
	public static final int STARTUP     = 2;    // startup cost in US dollars
	public static final int SHUTDOWN    = 3;    // shutdown cost in US dollars
	public static final int NCOST       = 4;    // number breakpoints in piecewise linear cost function,
												// or number of coefficients in polynomial cost function
	public static final int COST        = 5;    // parameters defining total cost function begin in this col
												// (MODEL = 1) : p0, f0, p1, f1, ..., pn, fn
												//      where p0 < p1 < ... < pn and the cost f(p) is defined
												//      by the coordinates (p0,f0), (p1,f1), ..., (pn,fn) of
												//      the end/break-points of the piecewise linear cost
												// (MODEL = 2) : cn, ..., c1, c0
												//      n+1 coefficients of an n-th order polynomial cost fcn,
												//      starting with highest order, where cost is
												//      f(p) = cn*p^2 + ... + c1*p + c0

}
