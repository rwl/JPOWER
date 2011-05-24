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

package edu.cornell.pserc.jpower.tdouble;

import java.lang.reflect.Field;

import cern.colt.matrix.tdouble.DoubleFactory1D;
import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tint.IntMatrix1D;
import cern.colt.util.tdouble.Djp_util;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_areas;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_branch;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_bus;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_gen;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_jpc;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_order;

/**
 * Converts internal to external bus numbering.
 *
 * @author Ray Zimmerman
 * @author Richard Lincoln
 *
 */
public class Djp_int2ext {

	public static Object[] jp_int2ext(IntMatrix1D i2e, Djp_bus bus,
			Djp_gen gen, Djp_branch branch) {
		return jp_int2ext(i2e, bus, gen, branch, null);
	}

	/**
	 * old form
	 *
	 * @param bus
	 * @param gen
	 * @param branch
	 * @param areas
	 * @return
	 */
	public static Object[] jp_int2ext(IntMatrix1D i2e, Djp_bus bus,
			Djp_gen gen, Djp_branch branch, Djp_areas areas) {

		bus.bus_i.assign( i2e.viewSelection(bus.bus_i.toArray()) );
		gen.gen_bus.assign( i2e.viewSelection(gen.gen_bus.toArray()) );
		branch.f_bus.assign( i2e.viewSelection(branch.f_bus.toArray()) );
		branch.t_bus.assign( i2e.viewSelection(branch.t_bus.toArray()) );
		if (areas != null)
			areas.price_ref_bus.assign( i2e.viewSelection(areas.price_ref_bus.toArray()) );

		return new Object[] {bus, gen, branch, areas};
	}


	/**
	 * If the input is a single JPOWER case object, then it restores all
	 * buses, generators and branches that were removed because of being
	 * isolated or off-line, and reverts to the original generator ordering
	 * and original bus numbering. This requires that the 'order' field
	 * created by EXT2INT be in place.
	 *
	 * @param jpc
	 * @return
	 */
	public static Djp_jpc jp_int2ext(Djp_jpc jpc) {

		if (jpc.order == null)
			System.err.println("int2ext: jpc does not have the 'order' set, as required for conversion back to external numbering.");
			// TODO: throw missing null order exception
		Djp_order o = jpc.order;

		if (o.state == "i") {
			/* execute userfcn callbacks for 'int2ext' stage */
			if (jpc.userfcn != null)
				jpc = Djp_run_userfcn.jp_run_userfcn(jpc.userfcn, "int2ext", jpc);

			/* save data matrices with internal ordering & restore originals */
			o.internal = new Djp_jpc();
			o.internal.bus    = jpc.bus.copy();
			o.internal.branch = jpc.branch.copy();
			o.internal.gen    = jpc.gen.copy();
			jpc.bus    = o.external.bus.copy();
			jpc.branch = o.external.branch.copy();
			jpc.gen    = o.external.gen.copy();
			if (jpc.gencost != null) {
				o.internal.gencost = jpc.gencost.copy();
				jpc.gencost = o.external.gencost.copy();
			}
			if (jpc.areas != null) {
				o.internal.areas = jpc.areas.copy();
				jpc.areas = o.external.areas.copy();
			}
			if (jpc.A != null) {
				o.internal.A = jpc.A.copy();
				jpc.A = o.external.A.copy();
			}
			if (jpc.N != null) {
				o.internal.N = jpc.N.copy();
				jpc.N = o.external.N.copy();
			}

			/* update data (in bus, branch and gen only) */
			jpc.bus.update(o.internal.bus, o.bus.status.on);
			jpc.branch.update(o.internal.branch, o.branch.status.on);
			jpc.gen.update(o.internal.gen.copy(o.gen.i2e.toArray()), o.branch.status.on);
			if (jpc.areas != null)
				jpc.areas.update(o.internal.areas, o.areas.status.on);

			/* revert to original bus numbers */
			jpc.bus.bus_i.viewSelection(o.bus.status.on).assign( o.bus.i2e.viewSelection(jpc.bus.bus_i.viewSelection(o.bus.status.on).toArray()) );
			jpc.branch.f_bus.viewSelection(o.branch.status.on).assign( o.bus.i2e.viewSelection(jpc.branch.f_bus.viewSelection(o.branch.status.on).toArray()) );
			jpc.branch.t_bus.viewSelection(o.branch.status.on).assign( o.bus.i2e.viewSelection(jpc.branch.t_bus.viewSelection(o.branch.status.on).toArray()) );
			jpc.gen.gen_bus.viewSelection(o.gen.status.on).assign( o.bus.i2e.viewSelection(jpc.gen.gen_bus.viewSelection(o.gen.status.on).toArray()) );
			if (jpc.areas != null)
				jpc.areas.price_ref_bus.viewSelection(o.areas.status.on).assign( o.bus.i2e.viewSelection(jpc.areas.price_ref_bus.viewSelection(o.areas.status.on).toArray()) );
			if (o.external != null)
				o.external = null;
			o.state = "e";
			jpc.order = o;
		} else {
			System.err.println("int2ext: jpc claims it is already using external numbering.");
		}
		return jpc;
	}

	public static Djp_jpc jp_int2ext(Djp_jpc jpc, String field, String[] ordering) {
		int dim = 1;
		return jp_int2ext(jpc, field, ordering, dim);
	}

	public static Djp_jpc jp_int2ext(Djp_jpc jpc, String field, String[] ordering, int dim) {
		return jp_int2ext(jpc, new String[] {field}, ordering, dim);
	}

	public static Djp_jpc jp_int2ext(Djp_jpc jpc, String[] field, String[] ordering) {
		int dim = 1;
		return jp_int2ext(jpc, field, ordering, dim);
	}

	/**
	 *
	 * @param jpc
	 * @param field
	 * @param ordering
	 * @param dim
	 * @return
	 */
	public static Djp_jpc jp_int2ext(Djp_jpc jpc, String[] field, String[] ordering, int dim) {
		for (String f : field) {
			try {
				Field fld = jpc.getClass().getField(f);
				Class<?> type = fld.getType();
				if (type == DoubleMatrix1D.class) {
					DoubleMatrix1D val = (DoubleMatrix1D) fld.get(jpc);
					DoubleMatrix1D oldval = (DoubleMatrix1D) fld.get(jpc.order.external);
					fld.set(jpc.order.internal, val.copy());
					fld.set(jpc, jp_int2ext(jpc, val, oldval, ordering, dim));
				} else if (type == DoubleMatrix2D.class) {
					DoubleMatrix2D val = (DoubleMatrix2D) fld.get(jpc);
					DoubleMatrix2D oldval = (DoubleMatrix2D) fld.get(jpc.order.external);
					fld.set(jpc.order.internal, val.copy());
					fld.set(jpc, jp_int2ext(jpc, val, oldval, ordering, dim));
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
		}
		return jpc;
	}


	public static DoubleMatrix1D jp_int2ext(Djp_jpc jpc, DoubleMatrix1D val, DoubleMatrix1D oldval, String ordering) {
		int dim = 1;
		return jp_int2ext(jpc, val, oldval, new String[] {ordering}, dim);
	}

	public static DoubleMatrix1D jp_int2ext(Djp_jpc jpc, DoubleMatrix1D val, DoubleMatrix1D oldval, String ordering, int dim) {
		return jp_int2ext(jpc, val, oldval, new String[] {ordering}, dim);
	}

	public static DoubleMatrix1D jp_int2ext(Djp_jpc jpc, DoubleMatrix1D val, DoubleMatrix1D oldval, String[] ordering) {
		int dim = 1;
		return jp_int2ext(jpc, val, oldval, ordering, dim);
	}

	/**
	 *
	 * @param jpc
	 * @param val
	 * @param ordering
	 * @param dim
	 * @return
	 */
	public static DoubleMatrix1D jp_int2ext(Djp_jpc jpc, DoubleMatrix1D val, DoubleMatrix1D oldval, String[] ordering, int dim) {
		Djp_order o = jpc.order;
		DoubleMatrix1D int_val;

		if (ordering.length == 1) {		// single set
			DoubleMatrix1D v;
			if (ordering.equals("gen")) {
				int[] i2e = o.gen.i2e.toArray();
				v = Djp_get_reorder.jp_get_reorder(val, i2e);
			} else {
				v = val;
			}
			int[] idx;
			if (ordering.equals("gen")) {
				idx = o.gen.status.on;
			} else if (ordering.equals("bus")) {
				idx = o.bus.status.on;
			} else {					// TODO: enum
				idx = o.branch.status.on;
			}
			int_val = Djp_set_reorder.jp_set_reorder(oldval, v, idx);
		} else {
			int_val = DoubleFactory1D.dense.make(0);
			int be = 0;		// base, external indexing
			int bi = 0;		// base, internal indexing
			int ne, ni;
			for (int k = 0; k < ordering.length; k++) {
				String order = ordering[k];
				DoubleMatrix1D v;
				DoubleMatrix1D oldv;
				if (order.equals("gen")) {
					ne = (int) o.external.gen.size();
					ni = (int) jpc.gen.size();
				} else if (order.equals("bus")) {
					ne = (int) o.external.bus.size();
					ni = (int) jpc.bus.size();
				} else {				// branch, TODO: enum
					ne = (int) o.external.branch.size();
					ni = (int) jpc.branch.size();
				}
				v = Djp_get_reorder.jp_get_reorder(val, Djp_util.irange(bi, bi + ni));
				oldv = Djp_get_reorder.jp_get_reorder(oldval, Djp_util.irange(be, be + ne));

				DoubleMatrix1D new_v = jp_int2ext(jpc, v, oldv, order, dim);
				int_val = DoubleFactory1D.dense.append(int_val, new_v);
				be = be + ne;
				bi = bi + ni;
			}
			ni = (int) val.size();
			if (ni > bi) {				// the rest
				DoubleMatrix1D new_v = Djp_get_reorder.jp_get_reorder(val, Djp_util.irange(bi, bi + ni));
				int_val = DoubleFactory1D.dense.append(int_val, new_v);
			}
		}
		return int_val;
	}




	public static DoubleMatrix2D jp_int2ext(Djp_jpc jpc, DoubleMatrix2D val, DoubleMatrix2D oldval, String ordering) {
		int dim = 1;
		return jp_int2ext(jpc, val, oldval, new String[] {ordering}, dim);
	}

	public static DoubleMatrix2D jp_int2ext(Djp_jpc jpc, DoubleMatrix2D val, DoubleMatrix2D oldval, String ordering, int dim) {
		return jp_int2ext(jpc, val, oldval, new String[] {ordering}, dim);
	}

	public static DoubleMatrix2D jp_int2ext(Djp_jpc jpc, DoubleMatrix2D val, DoubleMatrix2D oldval, String[] ordering) {
		int dim = 1;
		return jp_int2ext(jpc, val, oldval, ordering, dim);
	}

	/**
	 *
	 * @param jpc
	 * @param val
	 * @param ordering
	 * @param dim
	 * @return
	 */
	public static DoubleMatrix2D jp_int2ext(Djp_jpc jpc, DoubleMatrix2D val, DoubleMatrix2D oldval, String[] ordering, int dim) {
		Djp_order o = jpc.order;
		DoubleMatrix2D int_val;

		if (ordering.length == 1) {		// single set
			DoubleMatrix2D v;
			if (ordering.equals("gen")) {
				int[] i2e = o.gen.i2e.toArray();
				v = Djp_get_reorder.jp_get_reorder(val, i2e, dim);
			} else {
				v = val;
			}
			int[] idx;
			if (ordering.equals("gen")) {
				idx = o.gen.status.on;
			} else if (ordering.equals("bus")) {
				idx = o.bus.status.on;
			} else {					// TODO: enum
				idx = o.branch.status.on;
			}
			int_val = Djp_set_reorder.jp_set_reorder(oldval, v, idx, dim);
		} else {
			if (dim == 1) {
				int_val = DoubleFactory2D.dense.make(val.rows(), 0);
			} else if (dim == 2) {
				int_val = DoubleFactory2D.dense.make(0, val.columns());
			} else {
				throw new UnsupportedOperationException();
			}
			int be = 0;		// base, external indexing
			int bi = 0;		// base, internal indexing
			int ne, ni;
			for (int k = 0; k < ordering.length; k++) {
				String order = ordering[k];
				DoubleMatrix2D v;
				DoubleMatrix2D oldv;
				if (order.equals("gen")) {
					ne = (int) o.external.gen.size();
					ni = (int) jpc.gen.size();
				} else if (order.equals("bus")) {
					ne = (int) o.external.bus.size();
					ni = (int) jpc.bus.size();
				} else {				// branch, TODO: enum
					ne = (int) o.external.branch.size();
					ni = (int) jpc.branch.size();
				}
				v = Djp_get_reorder.jp_get_reorder(val, Djp_util.irange(bi, bi + ni), dim);
				oldv = Djp_get_reorder.jp_get_reorder(oldval, Djp_util.irange(be, be + ne), dim);

				DoubleMatrix2D new_v = jp_int2ext(jpc, v, oldv, order, dim);
				if (dim == 1) {
					int_val = DoubleFactory2D.dense.appendRows(int_val, new_v);
				} else if (dim == 2) {
					int_val = DoubleFactory2D.dense.appendColumns(int_val, new_v);
				} else {
					throw new UnsupportedOperationException();
				}
				be = be + ne;
				bi = bi + ni;
			}
			ni = (int) val.size();
			if (ni > bi) {				// the rest
				DoubleMatrix2D new_v = Djp_get_reorder.jp_get_reorder(val, Djp_util.irange(bi, bi + ni), dim);
				if (dim == 1) {
					int_val = DoubleFactory2D.dense.appendRows(int_val, new_v);
				} else if (dim == 2) {
					int_val = DoubleFactory2D.dense.appendColumns(int_val, new_v);
				} else {
					throw new UnsupportedOperationException();
				}
			}
		}
		return int_val;
	}
}
