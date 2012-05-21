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
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tint.IntMatrix1D;

import static edu.emory.mathcs.utils.Utils.intm;
import static edu.emory.mathcs.utils.Utils.dblm;

/**
 *
 * @author Richard Lincoln
 *
 */
public class Branch {

	public static final int F_BUS		= 0;
	public static final int T_BUS		= 1;
	public static final int BR_R		= 2;
	public static final int BR_X		= 3;
	public static final int BR_B		= 4;
	public static final int RATE_A		= 5;
	public static final int RATE_B		= 6;
	public static final int RATE_C		= 7;
	public static final int TAP		= 8;
	public static final int SHIFT		= 9;
	public static final int BR_STATUS	= 10;
	public static final int ANGMIN		= 11;
	public static final int ANGMAX		= 12;

	public static final int PF			= 13;
	public static final int QF			= 14;
	public static final int PT			= 15;
	public static final int QT			= 16;

	public static final int MU_SF		= 17;
	public static final int MU_ST		= 18;
	public static final int MU_ANGMIN	= 19;
	public static final int MU_ANGMAX	= 20;

	/** f, from bus number */
	public IntMatrix1D f_bus;

	/** t, to bus number */
	public IntMatrix1D t_bus;

	/** r, resistance (p.u.) */
	public DoubleMatrix1D br_r;

	/** x, reactance (p.u.) */
	public DoubleMatrix1D br_x;

	/** b, total line charging susceptance (p.u.) */
	public DoubleMatrix1D br_b;

	/** rateA, MVA rating A (long term rating) */
	public DoubleMatrix1D rate_a;

	/** rateB, MVA rating B (short term rating) */
	public DoubleMatrix1D rate_b;

	/** rateC, MVA rating C (emergency rating) */
	public DoubleMatrix1D rate_c;

	/** ratio, transformer off nominal turns ratio */
	public DoubleMatrix1D tap;

	/** angle, transformer phase shift angle (degrees) */
	public DoubleMatrix1D shift;

	/** initial branch status, 1 - in service, 0 - out of service */
	public IntMatrix1D br_status;

	/** minimum angle difference, angle(Vf) - angle(Vt) (degrees) */
	public DoubleMatrix1D ang_min;

	/** maximum angle difference, angle(Vf) - angle(Vt) (degrees) */
	public DoubleMatrix1D ang_max;

	/*
	 * included in power flow solution, not necessarily in input
	 */

	/** real power injected at "from" bus end (MW)	   (not in PTI format) */
	public DoubleMatrix1D Pf;

	/** reactive power injected at "from" bus end (MVAr) (not in PTI format) */
	public DoubleMatrix1D Qf;

	/** real power injected at "to" bus end (MW)		 (not in PTI format) */
	public DoubleMatrix1D Pt;

	/** reactive power injected at "to" bus end (MVAr)   (not in PTI format) */
	public DoubleMatrix1D Qt;

	/*
	 * included in opf solution, not necessarily in input
	 * assume objective function has units, u
	 *
	 */

	/** Kuhn-Tucker multiplier on MVA limit at "from" bus (u/MVA) */
	public DoubleMatrix1D mu_Sf;

	/** Kuhn-Tucker multiplier on MVA limit at "to" bus (u/MVA) */
	public DoubleMatrix1D mu_St;

	/** Kuhn-Tucker multiplier lower angle difference limit (u/degree) */
	public DoubleMatrix1D mu_angmin;

	/** Kuhn-Tucker multiplier upper angle difference limit (u/degree) */
	public DoubleMatrix1D mu_angmax;

	/**
	 *
	 * @return the number of branches.
	 */
	public int size() {
		return (int) this.f_bus.size();
	}

	/**
	 *
	 * @return a full copy of the branch data.
	 */
	public Branch copy() {
		return copy(null);
	}

	/**
	 *
	 * @param indexes
	 * @return
	 */
	public Branch copy(int[] indexes) {
		Branch other = new Branch();

		other.f_bus = f_bus.viewSelection(indexes).copy();
		other.t_bus = t_bus.viewSelection(indexes).copy();
		other.br_r = br_r.viewSelection(indexes).copy();
		other.br_x = br_x.viewSelection(indexes).copy();
		other.br_b = br_b.viewSelection(indexes).copy();
		other.rate_a = rate_a.viewSelection(indexes).copy();
		other.rate_b = rate_b.viewSelection(indexes).copy();
		other.rate_c = rate_c.viewSelection(indexes).copy();
		other.tap = tap.viewSelection(indexes).copy();
		other.shift = shift.viewSelection(indexes).copy();
		other.br_status = br_status.viewSelection(indexes).copy();

		if (ang_min != null)
			other.ang_min = ang_min.viewSelection(indexes).copy();
		if (ang_max != null)
			other.ang_max = ang_max.viewSelection(indexes).copy();

		if (Pf != null)
			other.Pf = Pf.viewSelection(indexes).copy();
		if (Qf != null)
			other.Qf = Qf.viewSelection(indexes).copy();
		if (Pt != null)
			other.Pt = Pt.viewSelection(indexes).copy();
		if (Qt != null)
			other.Qt = Qt.viewSelection(indexes).copy();

		if (mu_Sf != null)
			other.mu_Sf = mu_Sf.viewSelection(indexes).copy();
		if (mu_St != null)
			other.mu_St = mu_St.viewSelection(indexes).copy();
		if (mu_angmin != null)
			other.mu_angmin = mu_angmin.viewSelection(indexes).copy();
		if (mu_angmax != null)
			other.mu_angmax = mu_angmax.viewSelection(indexes).copy();

		return other;
	}

	/**
	 * Updates the branch data for the given indexes.
	 *
	 * @param other branch data source
	 * @param indexes branch indexes
	 */
	public void update(Branch other, int[] indexes) {

//		f_bus.viewSelection(indexes).assign(other.f_bus.viewSelection(indexes));
//		t_bus.viewSelection(indexes).assign(other.t_bus.viewSelection(indexes));
//		br_r.viewSelection(indexes).assign(other.br_r.viewSelection(indexes));
//		br_x.viewSelection(indexes).assign(other.br_x.viewSelection(indexes));
//		br_b.viewSelection(indexes).assign(other.br_b.viewSelection(indexes));
//		rate_a.viewSelection(indexes).assign(other.rate_a.viewSelection(indexes));
//		rate_b.viewSelection(indexes).assign(other.rate_b.viewSelection(indexes));
//		rate_c.viewSelection(indexes).assign(other.rate_c.viewSelection(indexes));
//		tap.viewSelection(indexes).assign(other.tap.viewSelection(indexes));
//		shift.viewSelection(indexes).assign(other.shift.viewSelection(indexes));
//		br_status.viewSelection(indexes).assign(other.br_status.viewSelection(indexes));
//		ang_min.viewSelection(indexes).assign(other.ang_min.viewSelection(indexes));
//		ang_max.viewSelection(indexes).assign(other.ang_max.viewSelection(indexes));
//
//		if (Pf != null)
//			Pf.viewSelection(indexes).assign(other.Pf.viewSelection(indexes));
//		if (Qf != null)
//			Qf.viewSelection(indexes).assign(other.Qf.viewSelection(indexes));
//		if (Pt != null)
//			Pt.viewSelection(indexes).assign(other.Pt.viewSelection(indexes));
//		if (Qt != null)
//			Qt.viewSelection(indexes).assign(other.Qt.viewSelection(indexes));
//
//		if (mu_Sf != null)
//			mu_Sf.viewSelection(indexes).assign(other.mu_Sf.viewSelection(indexes));
//		if (mu_St != null)
//			mu_St.viewSelection(indexes).assign(other.mu_St.viewSelection(indexes));
//		if (mu_angmin != null)
//			mu_angmin.viewSelection(indexes).assign(other.mu_angmin.viewSelection(indexes));
//		if (mu_angmax != null)
//			mu_angmax.viewSelection(indexes).assign(other.mu_angmax.viewSelection(indexes));

		f_bus.viewSelection(indexes).assign(other.f_bus);
		t_bus.viewSelection(indexes).assign(other.t_bus);
		br_r.viewSelection(indexes).assign(other.br_r);
		br_x.viewSelection(indexes).assign(other.br_x);
		br_b.viewSelection(indexes).assign(other.br_b);
		rate_a.viewSelection(indexes).assign(other.rate_a);
		rate_b.viewSelection(indexes).assign(other.rate_b);
		rate_c.viewSelection(indexes).assign(other.rate_c);
		tap.viewSelection(indexes).assign(other.tap);
		shift.viewSelection(indexes).assign(other.shift);
		br_status.viewSelection(indexes).assign(other.br_status);

		if (ang_min != null)
			ang_min.viewSelection(indexes).assign(other.ang_min);
		if (ang_max != null)
			ang_max.viewSelection(indexes).assign(other.ang_max);

		if (Pf != null)
			Pf.viewSelection(indexes).assign(other.Pf);
		if (Qf != null)
			Qf.viewSelection(indexes).assign(other.Qf);
		if (Pt != null)
			Pt.viewSelection(indexes).assign(other.Pt);
		if (Qt != null)
			Qt.viewSelection(indexes).assign(other.Qt);

		if (mu_Sf != null)
			mu_Sf.viewSelection(indexes).assign(other.mu_Sf);
		if (mu_St != null)
			mu_St.viewSelection(indexes).assign(other.mu_St);
		if (mu_angmin != null)
			mu_angmin.viewSelection(indexes).assign(other.mu_angmin);
		if (mu_angmax != null)
			mu_angmax.viewSelection(indexes).assign(other.mu_angmax);
	}

	/**
	 *
	 * @param data
	 */
//	public void fromMatrix(DoubleMatrix2D other) {
//
//		f_bus = Djp_util.intm(other.viewColumn(F_BUS));
//		t_bus = Djp_util.intm(other.viewColumn(T_BUS));
//		br_r = other.viewColumn(BR_R);
//		br_x = other.viewColumn(BR_X);
//		br_b = other.viewColumn(BR_B);
//		rate_a = other.viewColumn(RATE_A);
//		rate_b = other.viewColumn(RATE_B);
//		rate_c = other.viewColumn(RATE_C);
//		tap = other.viewColumn(TAP);
//		shift = other.viewColumn(SHIFT);
//		br_status = Djp_util.intm(other.viewColumn(BR_STATUS));
//		ang_min = other.viewColumn(ANGMIN);
//		ang_max = other.viewColumn(ANGMAX);
//
//		if (other.columns() > ANGMAX + 1) {
//			Pf = other.viewColumn(PF);
//			Qf = other.viewColumn(QF);
//			Pt = other.viewColumn(PT);
//			Qt = other.viewColumn(QT);
//		}
//
//		if (other.columns() > QT + 1) {
//			mu_Sf = other.viewColumn(MU_SF);
//			mu_St = other.viewColumn(MU_ST);
//			mu_angmin = other.viewColumn(MU_ANGMIN);
//			mu_angmax = other.viewColumn(MU_ANGMAX);
//		}
//	}

	public static Branch fromMatrix(DoubleMatrix2D data) {
		Branch branch = new Branch();

		branch.f_bus = intm(data.viewColumn(F_BUS));
		branch.t_bus = intm(data.viewColumn(T_BUS));
		branch.br_r = data.viewColumn(BR_R);
		branch.br_x = data.viewColumn(BR_X);
		branch.br_b = data.viewColumn(BR_B);
		branch.rate_a = data.viewColumn(RATE_A);
		branch.rate_b = data.viewColumn(RATE_B);
		branch.rate_c = data.viewColumn(RATE_C);
		branch.tap = data.viewColumn(TAP);
		branch.shift = data.viewColumn(SHIFT);
		branch.br_status = intm(data.viewColumn(BR_STATUS));

		if (data.columns() > BR_STATUS + 1) {
			branch.ang_min = data.viewColumn(ANGMIN);
			branch.ang_max = data.viewColumn(ANGMAX);
		}

		if (data.columns() > ANGMAX + 1) {
			branch.Pf = data.viewColumn(PF);
			branch.Qf = data.viewColumn(QF);
			branch.Pt = data.viewColumn(PT);
			branch.Qt = data.viewColumn(QT);
		}

		if (data.columns() > QT + 1) {
			branch.mu_Sf = data.viewColumn(MU_SF);
			branch.mu_St = data.viewColumn(MU_ST);
			branch.mu_angmin = data.viewColumn(MU_ANGMIN);
			branch.mu_angmax = data.viewColumn(MU_ANGMAX);
		}

		return branch;
	}

	public static Branch fromMatrix(double[][] data) {
		return fromMatrix(DoubleFactory2D.dense.make(data));
	}

	public DoubleMatrix2D toMatrix() {
		boolean pf = (Pf != null);
		boolean opf = (mu_Sf != null);
		return toMatrix(pf, opf);
	}

	/**
	 *
	 * @param pf include power flow solution data
	 * @param opf include optimal power flow solution data
	 * @return branch data matrix
	 */
	public DoubleMatrix2D toMatrix(boolean pf, boolean opf) {
		DoubleMatrix2D matrix;
		if (pf && opf) {
			matrix = DoubleFactory2D.dense.make(size(), 21);
		} else if (opf) {
			matrix = DoubleFactory2D.dense.make(size(), 17);
		} else if (pf) {
			matrix = DoubleFactory2D.dense.make(size(), 17);
		} else {
			matrix = DoubleFactory2D.dense.make(size(), 13);
		}

		matrix.viewColumn(F_BUS).assign( dblm(f_bus) );
		matrix.viewColumn(T_BUS).assign( dblm(t_bus) );
		matrix.viewColumn(BR_R).assign(br_r);
		matrix.viewColumn(BR_X).assign(br_x);
		matrix.viewColumn(BR_B).assign(br_b);
		matrix.viewColumn(RATE_A).assign(rate_a);
		matrix.viewColumn(RATE_B).assign(rate_b);
		matrix.viewColumn(RATE_C).assign(rate_c);
		matrix.viewColumn(TAP).assign(tap);
		matrix.viewColumn(SHIFT).assign(shift);
		matrix.viewColumn(BR_STATUS).assign( dblm(br_status) );

		if (ang_min != null)
			matrix.viewColumn(ANGMIN).assign(ang_min);
		if (ang_max != null)
			matrix.viewColumn(ANGMAX).assign(ang_max);

		if (pf) {
			matrix.viewColumn(PF).assign(Pf);
			matrix.viewColumn(QF).assign(Qf);
			matrix.viewColumn(PT).assign(Pt);
			matrix.viewColumn(QT).assign(Qt);
		}

		if (opf) {
			matrix.viewColumn(MU_SF).assign(mu_Sf);
			matrix.viewColumn(MU_ST).assign(mu_St);
			matrix.viewColumn(MU_ANGMIN).assign(mu_angmin);
			matrix.viewColumn(MU_ANGMAX).assign(mu_angmax);
		}

		return matrix;
	}

	public double[][] toArray() {
		boolean pf = (Pf != null);
		boolean opf = (mu_Sf != null);
		return toArray(pf, opf);
	}

	public double[][] toArray(boolean pf, boolean opf) {
		return toMatrix(pf, opf).toArray();
	}

	@Override
	public String toString() {
		return toMatrix().toString();
	}

}
