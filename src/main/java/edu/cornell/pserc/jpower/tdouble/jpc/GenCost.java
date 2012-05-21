/*
 * Copyright (C) 2010-2011 Richard Lincoln
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package edu.cornell.pserc.jpower.tdouble.jpc;

import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tint.IntMatrix1D;

import static edu.emory.mathcs.utils.Utils.intm;
import static edu.emory.mathcs.utils.Utils.dblm;
import static edu.emory.mathcs.utils.Utils.irange;

/**
 *
 * @author Richard Lincoln
 *
 */
public class GenCost {

	private static final int MODEL	= 0;
	private static final int STARTUP	= 1;
	private static final int SHUTDOWN	= 2;
	private static final int NCOST	= 3;
	private static final int COST		= 4;

	/** cost model, 1 = piecewise linear, 2 = polynomial */
	public IntMatrix1D model;

	/** startup cost in US dollars */
	public DoubleMatrix1D startup;

	/** shutdown cost in US dollars */
	public DoubleMatrix1D shutdown;

	/**
	 * number breakpoints in piecewise linear cost function,
	 * parameters defining total cost function begin in this col
	 */
	public IntMatrix1D ncost;

	/**
	 * (MODEL = 1) : p0, f0, p1, f1, ..., pn, fn
	 * where p0 < p1 < ... < pn and the cost f(p) is defined
	 * by the coordinates (p0,f0), (p1,f1), ..., (pn,fn) of
	 * the end/break-points of the piecewise linear cost
	 * (MODEL = 2) : cn, ..., c1, c0
	 * n+1 coefficients of an n-th order polynomial cost fcn,
	 * starting with highest order, where cost is
	 * f(p) = cn*p^2 + ... + c1*p + c0
	 */
	public DoubleMatrix2D cost;

	/**
	 *
	 * @return the number of generator costs.
	 */
	public int size() {
		return (int) this.model.size();
	}

	/**
	 *
	 * @return
	 */
	public GenCost copy() {
		return copy(null);
	}

	/**
	 *
	 * @return a copy of the gencost data.
	 */
	public GenCost copy(int[] indexes) {
		GenCost other = new GenCost();

		other.model = this.model.viewSelection(indexes).copy();
		other.startup = this.startup.viewSelection(indexes).copy();
		other.shutdown = this.shutdown.viewSelection(indexes).copy();
		other.ncost = this.ncost.viewSelection(indexes).copy();
		other.cost = this.cost.viewSelection(indexes, null).copy();

		return other;
	}

	/**
	 *
	 * @param other
	 */
//	public void fromMatrix(DoubleMatrix2D other) {
//
//		this.model = Djp_util.intm(other.viewColumn(MODEL));
//		this.startup = other.viewColumn(STARTUP);
//		this.shutdown = other.viewColumn(SHUTDOWN);
//		this.ncost = Djp_util.intm(other.viewColumn(NCOST));
//		this.cost = other.viewSelection(null, Djp_util.irange(COST, other.columns()));
//	}

	public static GenCost fromMatrix(DoubleMatrix2D other) {
		GenCost gencost = new GenCost();

		gencost.model = intm(other.viewColumn(MODEL));
		gencost.startup = other.viewColumn(STARTUP);
		gencost.shutdown = other.viewColumn(SHUTDOWN);
		gencost.ncost = intm(other.viewColumn(NCOST));
		gencost.cost = other.viewSelection(null, irange(COST, other.columns()));

		return gencost;
	}

	public static GenCost fromMatrix(double[][] data) {
		return fromMatrix(DoubleFactory2D.dense.make(data));
	}

	/**
	 *
	 * @return
	 */
	@SuppressWarnings("static-access")
	public DoubleMatrix2D toMatrix() {
//		DoubleMatrix2D matrix = DoubleFactory2D.dense.make(size(), ncost.aggregate(ifunc.min, ifunc.identity) + 4);
		DoubleMatrix2D matrix = DoubleFactory2D.dense.make(size(), this.cost.columns() + 4);

		matrix.viewColumn(MODEL).assign( dblm(this.model) );
		matrix.viewColumn(MODEL).assign(this.startup);
		matrix.viewColumn(SHUTDOWN).assign(this.shutdown);
		matrix.viewColumn(NCOST).assign( dblm(this.ncost) );
		matrix.viewSelection(null, irange(COST, COST + this.cost.columns())).assign(this.cost);

		return matrix;
	}

	public double[][] toArray() {
		return toMatrix().toArray();
	}

}
