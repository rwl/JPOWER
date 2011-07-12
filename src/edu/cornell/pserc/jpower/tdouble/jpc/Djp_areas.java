/*
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

package edu.cornell.pserc.jpower.tdouble.jpc;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tint.IntMatrix1D;
import cern.colt.util.tdouble.Djp_util;

/**
 *
 * @author Richard Lincoln
 *
 */
public class Djp_areas {

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
	public Djp_areas copy() {
		return copy(null);
	}

	/**
	 *
	 * @param indexes
	 * @return a copy of the areas data.
	 */
	public Djp_areas copy(int[] indexes) {
		Djp_areas other = new Djp_areas();

		other.area_i = this.area_i.viewSelection(indexes).copy();
		other.price_ref_bus = this.price_ref_bus.viewSelection(indexes).copy();

		return other;
	}

	/**
	 *
	 * @param other
	 * @param indexes
	 */
	public void update(Djp_areas other, int[] indexes) {

		this.area_i.viewSelection(indexes).assign(other.area_i.viewSelection(indexes));
		this.price_ref_bus.viewSelection(indexes).assign(other.price_ref_bus.viewSelection(indexes));
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

	public static Djp_areas fromMatrix(DoubleMatrix2D other) {
		Djp_areas area = new Djp_areas();

		area.area_i = Djp_util.intm(other.viewColumn(AREA_I));
		area.price_ref_bus = Djp_util.intm(other.viewColumn(PRICE_REF_BUS));

		return area;
	}

	public DoubleMatrix1D toMatrix() {
		// TODO Auto-generated method stub
		return null;
	}

}
