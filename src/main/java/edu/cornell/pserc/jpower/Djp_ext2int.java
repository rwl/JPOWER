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

import cern.colt.matrix.tint.IntFactory1D;
import cern.colt.matrix.tint.IntMatrix1D;
import cern.colt.matrix.tint.algo.IntSorting;
import cern.colt.matrix.tint.impl.SparseCCIntMatrix2D;

import static cern.colt.util.tdouble.Util.ifunc;
import static cern.colt.util.tdouble.Util.irange;
import static cern.colt.util.tdouble.Util.max;
import static cern.colt.util.tdouble.Util.nonzero;
import static cern.colt.util.tdouble.Util.zeros;

import edu.cornell.pserc.jpower.jpc.Areas;
import edu.cornell.pserc.jpower.jpc.Branch;
import edu.cornell.pserc.jpower.jpc.Bus;
import edu.cornell.pserc.jpower.jpc.Gen;
import edu.cornell.pserc.jpower.jpc.JPC;
import edu.cornell.pserc.jpower.jpc.Order;

import static edu.cornell.pserc.jpower.jpc.JPC.NONE;
import static edu.cornell.pserc.jpower.jpc.JPC.PQ;
import static edu.cornell.pserc.jpower.jpc.JPC.PV;
import static edu.cornell.pserc.jpower.jpc.JPC.REF;
import static edu.cornell.pserc.jpower.Djp_e2i_field.e2i_field;
import static edu.cornell.pserc.jpower.Djp_run_userfcn.run_userfcn;

/**
 * Converts external to internal indexing.
 *
 * This function has two forms, (1) the old form that operates on
 * and returns individual matrices and (2) the new form that operates
 * on and returns an entire JPOWER case object.
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
 * 2.  jpc = ext2int(jpc)
 *
 * If the input is a single JPOWER case object, then all isolated
 * buses, off-line generators and branches are removed along with any
 * generators, branches or areas connected to isolated buses. Then the
 * buses are renumbered consecutively, beginning at 1, and the
 * generators are sorted by increasing bus number. Any 'ext2int'
 * callback routines registered in the case are also invoked
 * automatically.All of the related indexing information and the original
 * data matrices are stored in an 'order' field in the object to be used
 * by INT2EXT to perform the reverse conversions. If the case is already
 * using internal numbering it is returned unchanged.
 *
 * Example:
 *     jpc = ext2int(jpc);
 *
 * @author Ray Zimmerman
 * @author Richard Lincoln
 *
 */
public class Djp_ext2int {

	/**
	 *
	 * @param jpc
	 * @return
	 */
	@SuppressWarnings("static-access")
	public static JPC ext2int(JPC jpc) {
		int i, nb, ng, ng0;
		boolean first, dc;
		Order o;
		IntMatrix1D n2i;
		Ordering[] ordering;

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
				if (jpc.gencost.size() == 2 * ng0) {
					ordering = new Ordering[] {Ordering.GEN, Ordering.GEN};  // include Qg cost

				} else {
					ordering = new Ordering[] {Ordering.GEN};	// Pg cost only
				}
				jpc = e2i_field(jpc, "gencost", ordering);
			}
			if (jpc.A != null | jpc.N != null) {
				if (dc) {
					ordering = new Ordering[] {Ordering.BUS, Ordering.GEN};
				} else {
					ordering = new Ordering[] {Ordering.BUS, Ordering.BUS, Ordering.GEN, Ordering.GEN};
				}
				if (jpc.A != null)
					jpc = e2i_field(jpc, "A", ordering, 1);
				if (jpc.N != null)
					jpc = e2i_field(jpc, "N", ordering, 1);

				/* execute userfcn callbacks for 'ext2int' stage */
				if (jpc.userfcn != null)
					jpc = run_userfcn(jpc.userfcn, "ext2int", jpc);
			}
		}
		return jpc;
	}

	/**
	 * Old form.
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

	public static Object[] ext2int(Bus bus, Gen gen, Branch branch) {
		return ext2int(bus, gen, branch, null);
	}

}
