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

package edu.cornell.pserc.jpower.tdouble.opf;

import java.util.Map;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_jpc;
import edu.cornell.pserc.jpower.tdouble.opf.Djp_opf_model.Cost;

public class Djp_opf_model {

	public abstract class Set {
		public String name;
		public int i0;
		public int iN;
		public int N;
		public int NS;
		public Set[] order;
	}

	/**
	 * A set of variables.
	 */
	public class VariableSet extends Set {
		/** Initial value of variables.  Zero by default. */
		public DoubleMatrix1D v0;
		/** Lower bound on the variables. Unbounded below be default. */
		public DoubleMatrix1D vl;
		/** Upper bound on the variables. Unbounded above by default. */
		public DoubleMatrix1D vu;
	}

	public class LinearConstraintSet extends Set {
		public int N;
		public DoubleMatrix2D A;
		public DoubleMatrix1D l;
		public DoubleMatrix1D u;
		public VariableSet[] vs;
	}

	public class NonLinearConstraintSet extends Set {}

	public class Cost extends Set {
		public DoubleMatrix2D N;
		public DoubleMatrix2D H;
		public DoubleMatrix1D Cw;
		public DoubleMatrix1D dd;
		public DoubleMatrix1D rh;
		public DoubleMatrix1D kk;
		public DoubleMatrix1D mm;
		public DoubleMatrix1D vs;
		public DoubleMatrix1D params;
	}

	public Djp_jpc get_jpc() {
		return null;
	}

	public Map<String, Set> get_idx() {
		return null;
	}

	public Cost get_cost_params() {
		return null;
	}

}
