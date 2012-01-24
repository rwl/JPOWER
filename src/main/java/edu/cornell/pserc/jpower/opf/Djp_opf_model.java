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

package edu.cornell.pserc.jpower.opf;

import java.util.Map;

import cern.colt.matrix.AbstractMatrix;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import edu.cornell.pserc.jpower.jpc.JPC;

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

	public class CostParams extends Set {
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

	public Djp_opf_model(JPC jpc) {
		// TODO Auto-generated constructor stub
	}

	public JPC get_jpc() {
		return null;
	}

	public Map<String, Set>[] get_idx() {
		return null;
	}

	public CostParams get_cost_params() {
		return null;
	}

	public int getN(String selector, String name) {
		return 0;
	}

	public void add_vars(String string, int nb, DoubleMatrix1D va, DoubleMatrix1D val, DoubleMatrix1D vau) {
		// TODO Auto-generated method stub

	}

	public void add_constraints(String string, DoubleMatrix2D aang, DoubleMatrix1D lang, DoubleMatrix1D uang, String[] strings) {
		// TODO Auto-generated method stub

	}

	public void userdata(String string, DoubleMatrix1D pfinj) {
		// TODO Auto-generated method stub

	}

	public void userdata(String string, DoubleMatrix2D bf) {
		// TODO Auto-generated method stub

	}

	public void add_constraints(String string, int nb, String string2) {
		// TODO Auto-generated method stub

	}

	public void add_vars(String string, int ny) {
		// TODO Auto-generated method stub

	}

	public void add_costs(String string, CostParams user_cost, String[] user_vars) {
		// TODO Auto-generated method stub

	}

	public Djp_opf_model build_cost_params() {
		return null;
		// TODO Auto-generated method stub

	}

	public AbstractMatrix userdata(String string) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getN(String string) {
		// TODO Auto-generated method stub
		return 0;
	}

	public CostParams get_cost_params(String name) {
		return null;
	}

	public AbstractMatrix[] linear_constraints() {
		// TODO Auto-generated method stub
		return null;
	}

	public DoubleMatrix1D[] getv() {
		// TODO Auto-generated method stub
		return null;
	}

}
