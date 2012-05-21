/*
 * Copyright (C) 1996-2010 Power System Engineering Research Center
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

package edu.cornell.pserc.jpower;

import java.lang.reflect.Field;

import cern.colt.matrix.tdouble.DoubleFactory1D;
import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tint.IntFactory1D;
import cern.colt.matrix.tint.IntMatrix1D;
import cern.colt.matrix.tint.algo.IntSorting;
import cern.colt.matrix.tint.impl.SparseCCIntMatrix2D;

import static edu.emory.mathcs.utils.Utils.ifunc;
import static edu.emory.mathcs.utils.Utils.max;
import static edu.emory.mathcs.utils.Utils.irange;
import static edu.emory.mathcs.utils.Utils.zeros;
import static edu.emory.mathcs.utils.Utils.nonzero;

import edu.cornell.pserc.jpower.jpc.Areas;
import edu.cornell.pserc.jpower.jpc.Branch;
import edu.cornell.pserc.jpower.jpc.Bus;
import edu.cornell.pserc.jpower.jpc.Gen;
import edu.cornell.pserc.jpower.jpc.GenCost;
import edu.cornell.pserc.jpower.jpc.JPC;
import edu.cornell.pserc.jpower.jpc.Order;

import static edu.cornell.pserc.jpower.jpc.JPC.NONE;
import static edu.cornell.pserc.jpower.jpc.JPC.PQ;
import static edu.cornell.pserc.jpower.jpc.JPC.PV;
import static edu.cornell.pserc.jpower.jpc.JPC.REF;

/**
 * Converts external to internal indexing.
 *
 * This function performs several different tasks, depending on the
 * arguments passed.
 *
 * 1.  [I2E, BUS, GEN, BRANCH, AREAS] = EXT2INT(BUS, GEN, BRANCH, AREAS)
 *     [I2E, BUS, GEN, BRANCH] = EXT2INT(BUS, GEN, BRANCH)
 *
 * If the first argument is a matrix, it simply converts from (possibly
 * non-consecutive) external bus numbers to consecutive internal bus
 * numbers which start at 1. Changes are made to BUS, GEN, BRANCH and
 * optionally AREAS matrices, which are returned along with a vector of
 * indices I2E that can be passed to INT2EXT to perform the reverse
 * conversion, where EXTERNAL_BUS_NUMBER = I2E(INTERNAL_BUS_NUMBER)
 *
 * Examples:
 *     [i2e, bus, gen, branch, areas] = ext2int(bus, gen, branch, areas);
 *     [i2e, bus, gen, branch] = ext2int(bus, gen, branch);
 *
 * 2.  ppc = EXT2INT(ppc)
 *
 * If the input is a single MATPOWER case struct, then all isolated
 * buses, off-line generators and branches are removed along with any
 * generators, branches or areas connected to isolated buses. Then the
 * buses are renumbered consecutively, beginning at 1, and the
 * generators are sorted by increasing bus number. All of the related
 * indexing information and the original data matrices are stored in
 * an 'order' field in the struct to be used by INT2EXT to perform
 * the reverse conversions. If the case is already using internal
 * numbering it is returned unchanged.
 *
 * Example:
 *     ppc = ext2int(ppc);
 *
 * 3.  VAL = EXT2INT(ppc, VAL, ORDERING)
 *     VAL = EXT2INT(ppc, VAL, ORDERING, DIM)
 *     ppc = EXT2INT(ppc, FIELD, ORDERING)
 *     ppc = EXT2INT(ppc, FIELD, ORDERING, DIM)
 *
 * When given a case struct that has already been converted to
 * internal indexing, this function can be used to convert other data
 * structures as well by passing in 2 or 3 extra parameters in
 * addition to the case struct. If the value passed in the 2nd
 * argument is a column vector, it will be converted according to the
 * ORDERING specified by the 3rd argument (described below). If VAL
 * is an n-dimensional matrix, then the optional 4th argument (DIM,
 * default = 1) can be used to specify which dimension to reorder.
 * The return value in this case is the value passed in, converted
 * to internal indexing.
 *
 * If the 2nd argument is a string or cell array of strings, it
 * specifies a field in the case struct whose value should be
 * converted as described above. In this case, the converted value
 * is stored back in the specified field, the original value is
 * saved for later use and the updated case struct is returned.
 * If FIELD is a cell array of strings, they specify nested fields.
 *
 * The 3rd argument, ORDERING, is used to indicate whether the data
 * corresponds to bus-, gen- or branch-ordered data. It can be one
 * of the following three strings: 'bus', 'gen' or 'branch'. For
 * data structures with multiple blocks of data, ordered by bus,
 * gen or branch, they can be converted with a single call by
 * specifying ORDERING as a cell array of strings.
 *
 * Any extra elements, rows, columns, etc. beyond those indicated
 * in ORDERING, are not disturbed.
 *
 * @author Ray Zimmerman
 * @author Richard Lincoln
 *
 */
@SuppressWarnings("static-access")
public class Djp_ext2int {

	/**
	 *
	 * @param jpc
	 * @return
	 */
	public static JPC ext2int(JPC jpc) {
		int i, nb, ng, ng0;
		boolean first, dc;
		Order o;
		IntMatrix1D n2i;

		jpc = jpc.copy();

		first = (jpc.order == null);
		if (first || jpc.order.state.equals('e')) {
			/* initialize order */
			o = first ? new Order() : jpc.order;

			/* sizes */
			nb = jpc.bus.size();
			ng = jpc.gen.size();
			ng0 = ng;
			if ((jpc.A != null) && (jpc.A.columns() < 2*nb + 2*ng)) {
				dc = true;
			} else if ((jpc.N != null) && (jpc.N.columns() < 2*nb + 2*ng)) {
				dc = true;
			} else {
				dc = false;
			}

			/* save data with external ordering */
			o.external.bus    = jpc.bus.copy();
			o.external.branch = jpc.branch.copy();
			o.external.gen    = jpc.gen.copy();
			if (jpc.areas != null)
				if (jpc.areas.size() == 0) {
					jpc.areas = null;						// if areas field is empty delete it (so it gets ignored)
				} else {
					o.external.areas = jpc.areas.copy();	// otherwise save it
				}

			/* check that all buses have a valid BUS_TYPE */
			int[] bt = jpc.bus.bus_type.toArray();
			for (i = 0; i < nb; i++)
				if (!(bt[i] == PQ || bt[i] == PV || bt[i] == REF || bt[i] == NONE))
					// TODO: Throw invalid bus type exception.
					System.err.printf("ext2int: bus %d has an invalid BUS_TYPE [%d]\n", i, bt[i]);

			/* determine which buses, branches, gens are connected & in-service */
			int[] bi = jpc.bus.bus_i.toArray();
			n2i = new SparseCCIntMatrix2D(max(bi) + 1, 1,
					bi, zeros(nb), irange(nb), false, false, false).viewColumn(0);

			/* bus status */
			IntMatrix1D bs = jpc.bus.bus_type.copy().assign(ifunc.equals(NONE));
			o.bus.status.off = nonzero(bs);	// isolated
			bs.assign(ifunc.equals(0));
			o.bus.status.on = nonzero(bs);		// connected

			/* gen status */
			IntMatrix1D gs = jpc.gen.gen_status.copy();
			int[] gbus = jpc.gen.gen_bus.toArray();
			gs.assign(ifunc.equals(1));				// assume boolean status
			gs.assign(bs.viewSelection(n2i.viewSelection(gbus).toArray()), ifunc.and);
			o.gen.status.on = nonzero(gs);		// on and connected
			gs.assign(ifunc.equals(0));
			o.gen.status.off = nonzero(gs);	// off or isolated

			/* branch status */
			IntMatrix1D brs = jpc.branch.br_status.copy();
			int[] fbus = jpc.branch.f_bus.toArray();
			int[] tbus = jpc.branch.t_bus.toArray();
			brs.assign(bs.viewSelection(n2i.viewSelection(fbus).toArray()), ifunc.and);
			brs.assign(bs.viewSelection(n2i.viewSelection(tbus).toArray()), ifunc.and);
			o.branch.status.on = nonzero(brs);	// on and connected
			brs.assign(ifunc.equals(0));
			o.branch.status.off = nonzero(brs);

			if (jpc.areas != null) {
				int[] prbus = jpc.areas.price_ref_bus.toArray();
				IntMatrix1D as = bs.viewSelection(n2i.viewSelection(prbus).toArray());
				o.areas.status.on = nonzero(as);
				as.assign(ifunc.equals(0));
				o.areas.status.off = nonzero(as);
			}

			/* delete stuff that is "out" */
			if (o.bus.status.off.length > 0)
				jpc.bus = jpc.bus.copy(o.bus.status.on);
			if (o.branch.status.off.length > 0)
				jpc.branch = jpc.branch.copy(o.branch.status.on);
			if (o.gen.status.off.length > 0)
				jpc.gen = jpc.gen.copy(o.gen.status.on);
			if ((jpc.areas != null) & (o.areas.status.off.length > 0))
				jpc.areas = jpc.areas.copy(o.areas.status.on);

			/* update size */
			nb = jpc.bus.size();

			/* apply consecutive bus numbering */
			o.bus.i2e = jpc.bus.bus_i.copy();
			o.bus.e2i = IntFactory1D.sparse.make(o.bus.i2e.aggregate(ifunc.max, ifunc.identity) + 1);
			o.bus.e2i.viewSelection( o.bus.i2e.toArray() ).assign(irange(nb));
			jpc.bus.bus_i.assign( o.bus.e2i.viewSelection(jpc.bus.bus_i.toArray()) );
			jpc.gen.gen_bus.assign( o.bus.e2i.viewSelection(jpc.gen.gen_bus.toArray()) );
			jpc.branch.f_bus.assign( o.bus.e2i.viewSelection( jpc.branch.f_bus.toArray()) );
			jpc.branch.t_bus.assign( o.bus.e2i.viewSelection( jpc.branch.t_bus.toArray()) );
			if (jpc.areas != null)
				jpc.areas.price_ref_bus.assign( o.bus.e2i.viewSelection(jpc.areas.price_ref_bus.toArray()) );

			/* reorder gens in order of increasing bus number */
			o.gen.e2i = IntFactory1D.dense.make( IntSorting.quickSort.sortIndex(jpc.gen.gen_bus) );
			o.gen.i2e = IntFactory1D.dense.make( IntSorting.quickSort.sortIndex(o.gen.e2i) );
			jpc.gen = jpc.gen.copy(o.gen.e2i.toArray());

			if (o.internal != null)
				o.internal = null;
			o.state = "i";
			jpc.order = o;

			/* update gencost, A and N */
			if (jpc.gencost != null) {
				jpc.order.external.gencost = jpc.gencost.copy();	// Save with external ordering.
				if (jpc.gencost.size() == 2*ng0) {
					String[] ordering = new String[] {"gen", "gen"};  // include Qg cost
					jpc.gencost = GenCost.fromMatrix( ext2int(jpc, jpc.gencost.toMatrix(), ordering) );

				} else {
					String ordering = "gen";  // Pg cost only
					jpc.gencost = GenCost.fromMatrix( ext2int(jpc, jpc.gencost.toMatrix(), ordering) );
				}
			}
//			if (jpc.A != null | jpc.N != null) {
//				String[] ordering;
//				if (dc) {
//					ordering = new String[] {"bus", "gen"};
//				} else {
//					ordering = new String[] {"bus", "bus", "gen", "gen"};
//				}
//				if (jpc.A != null)
//					jpc = jp_ext2int(jpc, "A", ordering, 2);
//				if (jpc.N != null)
//					jpc = jp_ext2int(jpc, "N", ordering, 2);
//
//				/* execute userfcn callbacks for 'ext2int' stage */
//				if (jpc.userfcn != null)
//					jpc = Djp_run_userfcn.jp_run_userfcn(jpc.userfcn, "ext2int", jpc);
//			}
		}
		return jpc;
	}


	public static JPC ext2int(JPC jpc, String field, String[] ordering) {
		int dim = 1;
		return ext2int(jpc, field, ordering, dim);
	}

	public static JPC ext2int(JPC jpc, String field, String[] ordering, int dim) {
		DoubleMatrix1D val1;
		DoubleMatrix2D val2;
		Field fld;
		Class<?> type;

		try {
			fld = jpc.getClass().getField(field);
			type = fld.getType();
			if (type == DoubleMatrix1D.class) {
				val1 = (DoubleMatrix1D) fld.get(jpc);
				fld.set(jpc.order.external, val1.copy());
				fld.set(jpc, ext2int(jpc, val1, ordering, dim));
			} else if (type == DoubleMatrix2D.class) {
				val2 = (DoubleMatrix2D) fld.get(jpc);
				fld.set(jpc.order.external, val2.copy());
				fld.set(jpc, ext2int(jpc, val2, ordering, dim));
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

	public static JPC ext2int(JPC jpc, String[] field, String[] ordering) {
		return ext2int(jpc, field, ordering, 1);
	}

	/**
	 *
	 * @param jpc
	 * @param field
	 * @param ordering
	 * @param dim
	 * @return
	 */
	public static JPC ext2int(JPC jpc, String[] field, String[] ordering, int dim) {
//		DoubleMatrix1D val1;
//		DoubleMatrix2D val2;
//		Field fld;
//		Class<?> type;
//
//		for (String f : field) {
//			try {
//				fld = jpc.getClass().getField(f);
//				type = fld.getType();
//				if (type == DoubleMatrix1D.class) {
//					val1 = (DoubleMatrix1D) fld.get(jpc);
//					fld.set(jpc.order.external, val1.copy());
//					fld.set(jpc, jp_ext2int(jpc, val1, ordering, dim));
//				} else if (type == DoubleMatrix2D.class) {
//					val2 = (DoubleMatrix2D) fld.get(jpc);
//					fld.set(jpc.order.external, val2.copy());
//					fld.set(jpc, jp_ext2int(jpc, val2, ordering, dim));
//				} else {
//					throw new UnsupportedOperationException();
//				}
//			} catch (SecurityException e) {
//				e.printStackTrace();
//			} catch (NoSuchFieldException e) {
//				e.printStackTrace();
//			} catch (IllegalArgumentException e) {
//				e.printStackTrace();
//			} catch (IllegalAccessException e) {
//				e.printStackTrace();
//			}
//		}
//		return jpc;
		throw new UnsupportedOperationException();
	}


	public static DoubleMatrix1D ext2int(JPC jpc, DoubleMatrix1D val, String ordering) {
		return ext2int(jpc, val, ordering, 1);
	}

	public static DoubleMatrix1D ext2int(JPC jpc, DoubleMatrix1D val, String ordering, int dim) {
		int[] idx;
		Order o;
		DoubleMatrix1D int_val1;

		o = jpc.order;


		if (ordering.equals("gen")) {
			int[] e2i = o.gen.e2i.toArray();
			idx = IntFactory1D.dense.make(o.gen.status.on).viewSelection(e2i).toArray();
		} else if (ordering.equals("bus")) {
			idx = o.bus.status.on;
		} else if (ordering.equals("branch")) {
			idx = o.branch.status.on;
		} else {
			throw new UnsupportedOperationException();
		}
		int_val1 = Djp_get_reorder.get_reorder(val, idx);

		return int_val1;
	}

	public static DoubleMatrix1D ext2int(JPC jpc, DoubleMatrix1D val, String[] ordering) {
		return ext2int(jpc, val, ordering, 1);
	}

	/**
	 *
	 * @param jpc
	 * @param val
	 * @param ordering
	 * @param dim
	 * @return
	 */
	public static DoubleMatrix1D ext2int(JPC jpc, DoubleMatrix1D val, String[] ordering, int dim) {
		throw new UnsupportedOperationException();

//		int b, n, k;
//		int[] idx;
//		String ordr;
//		Djp_order o;
//		DoubleMatrix1D int_val1, v1, new_v1;
//
//		o = jpc.order;
//
//		if (ordering.length == 1) {		// single set
//			if (ordering[0].equals("gen")) {
//				int[] e2i = o.gen.e2i.toArray();
//				idx = IntFactory1D.dense.make(o.gen.status.on).viewSelection(e2i).toArray();
//			} else if (ordering[0].equals("bus")) {
//				idx = o.bus.status.on;
//			} else {					// TODO: enum
//				idx = o.branch.status.on;
//			}
//			int_val1 = Djp_get_reorder.jp_get_reorder(val, idx);
//		} else {
//			int_val1 = DoubleFactory1D.dense.make(0);
//			b = 0;		// base
//			for (k = 0; k < ordering.length; k++) {
//				ordr = ordering[k];
//				if (ordr.equals("gen")) {
//					n = o.external.gen.size();
//				} else if (ordr.equals("bus")) {
//					n = o.external.bus.size();
//				} else {				// TODO: enum
//					n= o.external.branch.size();
//				}
//				v1 = Djp_get_reorder.jp_get_reorder(val, Djp_util.irange(b, b + n));
//				new_v1 = jp_ext2int(jpc, v1, ordering[k], dim);
//				int_val1 = DoubleFactory1D.dense.append(int_val1, new_v1);
//				b += n;
//			}
//			n = (int) val.size();
//			if (n > b) {				// the rest
//				new_v1 = Djp_get_reorder.jp_get_reorder(val, Djp_util.irange(b, b + n));
//				int_val1 = DoubleFactory1D.dense.append(int_val1, new_v1);
//			}
//		}
//		return int_val1;
	}




	public static DoubleMatrix2D ext2int(JPC jpc, DoubleMatrix2D val, String ordering) {
		return ext2int(jpc, val, ordering, 1);
	}

	public static DoubleMatrix2D ext2int(JPC jpc, DoubleMatrix2D val, String ordering, int dim) {
		int[] idx;
		Order o;
		DoubleMatrix2D int_val2;

		o = jpc.order;

		if (ordering.equals("gen")) {
			int[] e2i = o.gen.e2i.toArray();
			idx = IntFactory1D.dense.make(o.gen.status.on).viewSelection(e2i).toArray();
		} else if (ordering.equals("bus")) {
			idx = o.bus.status.on;
		} else if (ordering.equals("branch")) {
			idx = o.branch.status.on;
		} else {
			throw new UnsupportedOperationException();
		}
		int_val2 = Djp_get_reorder.get_reorder(val, idx, dim);

		return int_val2;
	}

	public static DoubleMatrix2D ext2int(JPC jpc, DoubleMatrix2D val, String[] ordering) {
		return ext2int(jpc, val, ordering, 1);
	}

	/**
	 *
	 * @param jpc
	 * @param val
	 * @param ordering
	 * @param dim
	 * @return
	 */
	public static DoubleMatrix2D ext2int(JPC jpc, DoubleMatrix2D val, String[] ordering, int dim) {
		throw new UnsupportedOperationException();

//		int b, n, k;
//		int[] idx;
//		String ordr;
//		Djp_order o;
//		DoubleMatrix2D int_val2, v2, new_v2;
//
//		o = jpc.order;
//
//		if (ordering.length == 1) {		// single set
//			if (ordering[0].equals("gen")) {
//				int[] e2i = o.gen.e2i.toArray();
//				idx = IntFactory1D.dense.make(o.gen.status.on).viewSelection(e2i).toArray();
//			} else if (ordering[0].equals("bus")) {
//				idx = o.bus.status.on;
//			} else {		// TODO: enum
//				idx = o.branch.status.on;
//			}
//			int_val2 = Djp_get_reorder.jp_get_reorder(val, idx, dim);
//		} else {
//			if (dim == 1) {
//				int_val2 = DoubleFactory2D.dense.make(val.rows(), 0);
//			} else if (dim == 2) {
//				int_val2 = DoubleFactory2D.dense.make(0, val.columns());
//			} else {
//				throw new UnsupportedOperationException();
//			}
//			b = 0;		// base
//			for (k = 0; k < ordering.length; k++) {
//				ordr = ordering[k];
//				if (ordr.equals("gen")) {
//					n = o.external.gen.size();
//				} else if (ordr.equals("bus")) {
//					n = o.external.bus.size();
//				} else {	// TODO: enum
//					n = o.external.branch.size();
//				}
//				v2 = Djp_get_reorder.jp_get_reorder(val, Djp_util.irange(b, b + n), dim);
//				new_v2 = jp_ext2int(jpc, v2, ordering[k], dim);
//				if (dim == 1) {
//					int_val2 = DoubleFactory2D.dense.appendRows(int_val2, new_v2);
//				} else if (dim == 2) {
//					int_val2 = DoubleFactory2D.dense.appendColumns(int_val2, new_v2);
//				} else {
//					throw new UnsupportedOperationException();
//				}
//				b += n;
//			}
//			n = (int) val.size();
//			if (n > b) {				// the rest
//				new_v2 = Djp_get_reorder.jp_get_reorder(val, Djp_util.irange(b, b+n), dim);
//				if (dim == 1) {
//					int_val2 = DoubleFactory2D.dense.appendRows(int_val2, new_v2);
//				} else if (dim == 2) {
//					int_val2 = DoubleFactory2D.dense.appendColumns(int_val2, new_v2);
//				} else {
//					throw new UnsupportedOperationException();
//				}
//			}
//		}
//		return int_val2;
	}

	/**
	 * old form
	 *
	 * @param bus
	 * @param gen
	 * @param branch
	 * @return
	 */
	public static Object[] ext2int(Bus bus, Gen gen, Branch branch) {
		return ext2int(bus, gen, branch, null);
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
	public static Object[] ext2int(Bus bus, Gen gen, Branch branch, Areas areas) {
		int[] i2e;
		IntMatrix1D e2i;

		i2e = bus.bus_i.toArray();
		e2i = IntFactory1D.sparse.make(max(i2e) + 1);
		e2i.viewSelection(i2e).assign(irange(bus.size()));

		bus.bus_i.assign( e2i.viewSelection(bus.bus_i.toArray()) );
		gen.gen_bus.assign( e2i.viewSelection(gen.gen_bus.toArray()) );
		branch.f_bus.assign( e2i.viewSelection(branch.f_bus.toArray()) );
		branch.t_bus.assign( e2i.viewSelection(branch.t_bus.toArray()) );

		if (areas != null && areas.size() != 0)
			areas.price_ref_bus.assign( e2i.viewSelection(areas.price_ref_bus.toArray()) );

		return new Object[] {i2e, bus, gen, branch, areas};
	}

}
