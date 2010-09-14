/*
 * Copyright (C) 1996-2010 Power System Engineering Research Center (PSERC)
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

package edu.cornell.pserc.jpower.tdouble;

import java.lang.reflect.Field;

import cern.colt.matrix.AbstractMatrix;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tint.IntFactory1D;
import cern.colt.matrix.tint.IntMatrix1D;
import cern.colt.matrix.tint.algo.IntSorting;
import cern.colt.matrix.tint.impl.SparseRCIntMatrix2D;
import cern.jet.math.tint.IntFunctions;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_areas;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_branch;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_bus;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_gen;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_jpc;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_order;
import edu.cornell.pserc.jpower.tdouble.util.Djp_util;

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
 * @author Ray Zimmerman (rz10@cornell.edu)
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class Djp_ext2int {

	private static final Djp_util util = new Djp_util();
	private static final IntFunctions ifunc = IntFunctions.intFunctions;

	private static final int REF = Djp_jpc.REF;
	private static final int PV = Djp_jpc.PV;
	private static final int PQ = Djp_jpc.PQ;
	private static final int NONE = Djp_jpc.NONE;

	/**
	 *
	 * @param bus
	 * @param gen
	 * @param branch
	 * @return
	 */
	public static Object[] jp_ext2int(Djp_bus bus, Djp_gen gen, Djp_branch branch) {
		return jp_ext2int(bus, gen, branch, null);
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
	@SuppressWarnings("static-access")
	public static Object[] jp_ext2int(Djp_bus bus, Djp_gen gen, Djp_branch branch, Djp_areas areas) {

		int[] i2e = bus.bus_i.toArray();
		IntMatrix1D e2i = IntFactory1D.sparse.make(util.max(i2e));
		e2i.viewSelection(i2e).assign(util.irange(bus.size()));

		bus.bus_i.assign( e2i.viewSelection(bus.bus_i.toArray()) );
		gen.gen_bus.assign( e2i.viewSelection(gen.gen_bus.toArray()) );
		branch.f_bus.assign( e2i.viewSelection(branch.f_bus.toArray()) );
		branch.t_bus.assign( e2i.viewSelection(branch.t_bus.toArray()) );

		if (areas != null && areas.size() != 0)
			areas.price_ref_bus.assign( e2i.viewSelection(areas.price_ref_bus.toArray()) );

		return new Object[] {bus, gen, branch, areas};
	}

	/**
	 *
	 * @param jpc
	 * @return
	 */
	@SuppressWarnings("static-access")
	public static Djp_jpc jp_ext2int(Djp_jpc jpc) {
		boolean first = (jpc.order == null);
		if (first || jpc.order.state.equals('e')) {
			/* initialize order */
			Djp_order o = first ? new Djp_order() : jpc.order;

			/* sizes */
			int nb = jpc.bus.size();
			int ng = jpc.gen.size();
			int ng0 = ng;
			boolean dc;
			if (jpc.A != null && jpc.A.columns() < 2*nb + 2*ng) {
				dc = true;
			} else if (jpc.N != null && jpc.N.columns() < 2*nb + 2*ng) {
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
			for (int i = 0; i < nb; i++)
				if (!(bt[i] == PQ || bt[i] == PV || bt[i] == REF || bt[i] == NONE))
					System.err.printf("ext2int: bus %d has an invalid BUS_TYPE [%d]\n", i, bt[i]);
					// TODO: Throw invalid bus type exception.

			/* determine which buses, branches, gens are connected & in-service */
			int[] bi = jpc.bus.bus_i.toArray();
			IntMatrix1D n2i = new SparseRCIntMatrix2D(util.max(bi) + 1, 1,
				bi, util.ones(nb), util.irange(nb), false, false, false).viewColumn(0);

			/* bus status */
			IntMatrix1D bs = jpc.bus.bus_type.copy();
			bs.assign(ifunc.equals(NONE));
			o.bus.status.off = util.nonzero(bs);	// isolated
			bs.assign(ifunc.equals(0));
			o.bus.status.on = util.nonzero(bs);		// connected

			/* gen status */
			IntMatrix1D gs = jpc.gen.gen_status.copy();
			int[] gbus = jpc.gen.gen_bus.toArray();
			gs.assign(ifunc.equals(1));				// Assume boolean status
			gs.assign(bs.viewSelection(n2i.viewSelection(gbus).toArray()), ifunc.and);
			o.gen.status.on = util.nonzero(gs);		// on and connected
			gs.assign(ifunc.equals(0));
			o.gen.status.off = util.nonzero(gs);	// off or isolated

			/* branch status */
			IntMatrix1D brs = jpc.branch.br_status.copy();
			int[] fbus = jpc.branch.f_bus.toArray();
			int[] tbus = jpc.branch.t_bus.toArray();
			brs.assign(bs.viewSelection(n2i.viewSelection(fbus).toArray()), ifunc.and);
			brs.assign(bs.viewSelection(n2i.viewSelection(tbus).toArray()), ifunc.and);
			o.branch.status.on = util.nonzero(brs);	// on and connected
			brs.assign(ifunc.equals(0));
			o.branch.status.off = util.nonzero(brs);

			if (jpc.areas != null) {
				int[] prbus = jpc.areas.price_ref_bus.toArray();
				IntMatrix1D as = bs.viewSelection(n2i.viewSelection(prbus).toArray());
				o.areas.status.on = util.nonzero(as);
			}

			/* delete stuff that is "out" */
			if (o.bus.status.off.length > 0)
				jpc.bus = jpc.bus.copy(o.bus.status.on);
			if (o.branch.status.off.length > 0)
				jpc.branch = jpc.branch.copy(o.branch.status.on);
			if (o.gen.status.off.length > 0)
				jpc.gen = jpc.gen.copy(o.gen.status.on);
			if (jpc.areas != null && o.areas.status.off.length > 0)
				jpc.areas = jpc.areas.copy(o.areas.status.on);

			/* update size */
			nb = jpc.bus.size();

			/* apply consecutive bus numbering */
			o.bus.i2e = jpc.bus.bus_i.copy();
			o.bus.e2i = IntFactory1D.sparse.make(o.bus.i2e.aggregate(ifunc.max, ifunc.identity) + 1);
			o.bus.e2i.viewSelection( o.bus.i2e.toArray() ).assign(util.irange(nb));
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
				String[] ordering;
				if (jpc.gencost.size() == 2*ng0) {
					ordering = new String[] {"gen", "gen"}; // include Qg cost
				} else {
					ordering = new String[] {"gen"}; // Pg cost only
				}
				jpc.order.external.gencost = jpc.gencost.copy();	// Save with external ordering.
				jpc.gencost.fromMatrix( (DoubleMatrix2D) jp_ext2int(jpc, jpc.gencost.toMatrix(), ordering) );
			}
			if (jpc.A != null || jpc.N != null) {
				String[] ordering;
				if (dc) {
					ordering = new String[] {"bus", "gen"};
				} else {
					ordering = new String[] {"bus", "bus", "gen", "gen"};
				}
				if (jpc.A != null)
					jpc = jp_ext2int(jpc, "A", ordering, 2);
				if (jpc.N != null)
					jpc = jp_ext2int(jpc, "N", ordering, 2);

				/* execute userfcn callbacks for 'ext2int' stage */
				if (jpc.userfcn != null)
					jpc = Djp_run_userfcn.jp_run_userfcn(jpc.userfcn, "ext2int", jpc);
			}
		}
		return jpc;
	}


	public static Djp_jpc jp_ext2int(Djp_jpc jpc, String field, String[] ordering) {
		int dim = 1;
		return jp_ext2int(jpc, field, ordering, dim);
	}

	public static Djp_jpc jp_ext2int(Djp_jpc jpc, String field, String[] ordering, int dim) {
		return jp_ext2int(jpc, new String[] {field}, ordering, dim);
	}

	public static Djp_jpc jp_ext2int(Djp_jpc jpc, String[] field, String[] ordering) {
		int dim = 1;
		return jp_ext2int(jpc, field, ordering, dim);
	}

	/**
	 *
	 * @param jpc
	 * @param field
	 * @param ordering
	 * @param dim
	 * @return
	 */
	public static Djp_jpc jp_ext2int(Djp_jpc jpc, String[] field, String[] ordering, int dim) {
		for (String f : field) {
			try {
				Field fld = jpc.getClass().getField(f);
				Class<?> type = fld.getType();
				if (type == DoubleMatrix1D.class) {
					DoubleMatrix1D val = (DoubleMatrix1D) fld.get(jpc);
					fld.set(jpc.order.external, val.copy());
					fld.set(jpc, jp_ext2int(jpc, val, ordering, dim));
				} else if (type == DoubleMatrix2D.class) {
					DoubleMatrix2D val = (DoubleMatrix2D) fld.get(jpc);
					fld.set(jpc.order.external, val.copy());
					fld.set(jpc, jp_ext2int(jpc, val, ordering, dim));
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


	public static DoubleMatrix1D jp_ext2int(Djp_jpc jpc, DoubleMatrix1D val, String ordering) {
		int dim = 1;
		return jp_ext2int(jpc, val, new String[] {ordering}, dim);
	}

	public static DoubleMatrix1D jp_ext2int(Djp_jpc jpc, DoubleMatrix1D val, String ordering, int dim) {
		return jp_ext2int(jpc, val, new String[] {ordering}, dim);
	}

	public static DoubleMatrix1D jp_ext2int(Djp_jpc jpc, DoubleMatrix1D val, String[] ordering) {
		int dim = 1;
		return jp_ext2int(jpc, val, ordering, dim);
	}

	/**
	 *
	 * @param jpc
	 * @param val
	 * @param ordering
	 * @param dim
	 * @return
	 */
	@SuppressWarnings("static-access")
	public static DoubleMatrix1D jp_ext2int(Djp_jpc jpc, DoubleMatrix1D val, String[] ordering, int dim) {
		Djp_order o = jpc.order;

		if (ordering.length == 1) {		// single set
			int[] idx;
			if (ordering.equals("gen")) {
				int[] e2i = o.gen.e2i.toArray();
				idx = IntFactory1D.dense.make(o.gen.status.on).viewSelection(e2i).toArray();
			} else if (ordering.equals("bus")) {
				idx = o.bus.status.on;
			} else {					// TODO: enum
				idx = o.branch.status.on;
			}
			DoubleMatrix1D i2e = Djp_get_reorder.jp_get_reorder(val, idx, dim);
		} else {
			int b = 0;		// base
			for (String ordr : ordering) {

				try {
					Field fld = o.external.getClass().getField(ordr);
					DoubleMatrix1D v;
					if (ordr.equals("gen")) {
						Djp_gen ref = (Djp_gen) fld.get(o.external);
						v = Djp_get_reorder.jp_get_reorder(val, util.irange(b, b + ref.size()), dim);
					} else if (ordr.equals("bus")) {
						Djp_bus ref = (Djp_bus) fld.get(o.external);
						v = Djp_get_reorder.jp_get_reorder(val, util.irange(b, b + ref.size()), dim);
					} else {				// TODO: enum
						Djp_branch ref = (Djp_branch) fld.get(o.external);
						v = Djp_get_reorder.jp_get_reorder(val, util.irange(b, b + ref.size()), dim);
					}
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (SecurityException e1) {
					e1.printStackTrace();
				} catch (NoSuchFieldException e1) {
					e1.printStackTrace();
				}
			}
//            int n;
//            if (dim == 1) {
//                n = val.rows();
//            } else if (dim == 2) {
//                n = val.columns();
//            } else if (dim == 3) {
//                n = val.slices();
//            }
//            if (n > b) {	// the rest
//                DoubleMatrix1D v = Djp_get_reorder.jp_get_reorder(val, irange(b, b+n), dim);
//
//            }
		}

		return null;
	}




	public static DoubleMatrix2D jp_ext2int(Djp_jpc jpc, DoubleMatrix2D val, String ordering) {
		int dim = 1;
		return jp_ext2int(jpc, val, new String[] {ordering}, dim);
	}

	public static DoubleMatrix2D jp_ext2int(Djp_jpc jpc, DoubleMatrix2D val, String ordering, int dim) {
		return jp_ext2int(jpc, val, new String[] {ordering}, dim);
	}

	public static DoubleMatrix2D jp_ext2int(Djp_jpc jpc, DoubleMatrix2D val, String[] ordering) {
		int dim = 1;
		return jp_ext2int(jpc, val, ordering, dim);
	}

	/**
	 *
	 * @param jpc
	 * @param val
	 * @param ordering
	 * @param dim
	 * @return
	 */
	@SuppressWarnings("static-access")
	public static DoubleMatrix2D jp_ext2int(Djp_jpc jpc, DoubleMatrix2D val, String[] ordering, int dim) {
		Djp_order o = jpc.order;

		if (ordering.length == 1) {		// single set
			int[] idx;
			if (ordering.equals("gen")) {
				int[] e2i = o.gen.e2i.toArray();
				idx = IntFactory1D.dense.make(o.gen.status.on).viewSelection(e2i).toArray();
			} else if (ordering.equals("bus")) {
				idx = o.bus.status.on;
			} else {					// TODO: enum
				idx = o.branch.status.on;
			}
			DoubleMatrix2D i2e = Djp_get_reorder.jp_get_reorder(val, idx, dim);
		} else {
			int b = 0;		// base
			for (String ordr : ordering) {

				try {
					Field fld = o.external.getClass().getField(ordr);
					DoubleMatrix2D v;
					if (ordr.equals("gen")) {
						Djp_gen ref = (Djp_gen) fld.get(o.external);
						v = Djp_get_reorder.jp_get_reorder(val, util.irange(b, b + ref.size()), dim);
					} else if (ordr.equals("bus")) {
						Djp_bus ref = (Djp_bus) fld.get(o.external);
						v = Djp_get_reorder.jp_get_reorder(val, util.irange(b, b + ref.size()), dim);
					} else {				// TODO: enum
						Djp_branch ref = (Djp_branch) fld.get(o.external);
						v = Djp_get_reorder.jp_get_reorder(val, util.irange(b, b + ref.size()), dim);
					}
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (SecurityException e1) {
					e1.printStackTrace();
				} catch (NoSuchFieldException e1) {
					e1.printStackTrace();
				}
			}
//            int n;
//            if (dim == 1) {
//                n = val.rows();
//            } else if (dim == 2) {
//                n = val.columns();
//            } else if (dim == 3) {
//                n = val.slices();
//            }
//            if (n > b) {	// the rest
//                DoubleMatrix1D v = Djp_get_reorder.jp_get_reorder(val, irange(b, b+n), dim);
//
//            }
		}

		return null;
	}

}
