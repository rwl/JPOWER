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

package edu.cornell.pserc.jpower.jpc;

import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tint.IntMatrix1D;

import static edu.emory.mathcs.utils.Utils.intm;

/**
 *
 * @author Richard Lincoln
 *
 */
public class Areas {

	private static final int AREA_I			= 0;
	private static final int PRICE_REF_BUS	= 1;

	/** area number */
	public IntMatrix1D area_i;

	/** price reference bus for this area */
	public IntMatrix1D price_ref_bus;

	/**
	 *
	 * @return the number of areas.
	 */
	public int size() {
		return (int) this.area_i.size();
	}

	/**
	 *
	 * @return a full copy of the areas data.
	 */
	public Areas copy() {
		return copy(null);
	}

	/**
	 *
	 * @param indexes
	 * @return a copy of the areas data.
	 */
	public Areas copy(int[] indexes) {
		Areas other = new Areas();

		other.area_i = this.area_i.viewSelection(indexes).copy();
		other.price_ref_bus = this.price_ref_bus.viewSelection(indexes).copy();

		return other;
	}

	/**
	 *
	 * @param other
	 * @param indexes
	 */
	public void update(Areas other, int[] indexes) {

//		this.area_i.viewSelection(indexes).assign(other.area_i.viewSelection(indexes));
//		this.price_ref_bus.viewSelection(indexes).assign(other.price_ref_bus.viewSelection(indexes));

		this.area_i.viewSelection(indexes).assign(other.area_i);
		this.price_ref_bus.viewSelection(indexes).assign(other.price_ref_bus);
	}

	/**
	 *
	 * @param other
	 */
//	public void fromMatrix(DoubleMatrix2D other) {
//
//		this.area_i = Djp_util.intm(other.viewColumn(AREA_I));
//		this.price_ref_bus = Djp_util.intm(other.viewColumn(PRICE_REF_BUS));
//	}

	public static Areas fromMatrix(DoubleMatrix2D other) {
		Areas area = new Areas();

		area.area_i = intm(other.viewColumn(AREA_I));
		area.price_ref_bus = intm(other.viewColumn(PRICE_REF_BUS));

		return area;
	}

	public static Areas fromMatrix(double[][] data) {
		return fromMatrix(DoubleFactory2D.dense.make(data));
	}

	public DoubleMatrix2D toMatrix() {
		// TODO Auto-generated method stub
		return null;
	}

	public double[][] toArray() {
		return toMatrix().toArray();
	}

}
