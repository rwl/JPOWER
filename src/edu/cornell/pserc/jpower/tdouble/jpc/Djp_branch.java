/*
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

package edu.cornell.pserc.jpower.tdouble.jpc;

import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tint.IntMatrix1D;
import edu.cornell.pserc.jpower.tdouble.util.Djp_util;

/**
 *
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class Djp_branch {

	private static final Djp_util util = new Djp_util();

	private static final int F_BUS		= 0;
	private static final int T_BUS		= 1;
	private static final int BR_R		= 2;
	private static final int BR_X		= 3;
	private static final int BR_B		= 4;
	private static final int RATE_A		= 5;
	private static final int RATE_B		= 6;
	private static final int RATE_C		= 7;
	private static final int TAP		= 8;
	private static final int SHIFT		= 9;
	private static final int BR_STATUS	= 10;
	private static final int ANGMIN		= 11;
	private static final int ANGMAX		= 12;

	private static final int PF			= 13;
	private static final int QF			= 14;
	private static final int PT			= 15;
	private static final int QT			= 16;

	private static final int MU_SF		= 17;
	private static final int MU_ST		= 18;
	private static final int MU_ANGMIN	= 19;
	private static final int MU_ANGMAX	= 20;

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
	public Djp_branch copy() {
		return copy(null);
	}

	/**
	 *
	 * @param indexes
	 * @return
	 */
	public Djp_branch copy(int[] indexes) {
		Djp_branch other = new Djp_branch();

		other.f_bus = this.f_bus.viewSelection(indexes).copy();
		other.t_bus = this.t_bus.viewSelection(indexes).copy();
		other.br_r = this.br_r.viewSelection(indexes).copy();
		other.br_x = this.br_x.viewSelection(indexes).copy();
		other.br_b = this.br_b.viewSelection(indexes).copy();
		other.rate_a = this.rate_a.viewSelection(indexes).copy();
		other.rate_b = this.rate_b.viewSelection(indexes).copy();
		other.rate_c = this.rate_c.viewSelection(indexes).copy();
		other.tap = this.tap.viewSelection(indexes).copy();
		other.shift = this.shift.viewSelection(indexes).copy();
		other.br_status = this.br_status.viewSelection(indexes).copy();
		other.ang_min = this.ang_min.viewSelection(indexes).copy();
		other.ang_max = this.ang_max.viewSelection(indexes).copy();

		other.Pf = this.Pf.viewSelection(indexes).copy();
		other.Qf = this.Qf.viewSelection(indexes).copy();
		other.Pt = this.Pt.viewSelection(indexes).copy();
		other.Qt = this.Qt.viewSelection(indexes).copy();

		other.mu_Sf = this.mu_Sf.viewSelection(indexes).copy();
		other.mu_St = this.mu_St.viewSelection(indexes).copy();
		other.mu_angmin = this.mu_angmin.viewSelection(indexes).copy();
		other.mu_angmax = this.mu_angmax.viewSelection(indexes).copy();

		return other;
	}

	/**
	 * Updates the branch data for the given indexes.
	 *
	 * @param other branch data source
	 * @param indexes branch indexes
	 */
	public void update(Djp_branch other, int[] indexes) {

		this.f_bus.viewSelection(indexes).assign(other.f_bus.viewSelection(indexes));
		this.t_bus.viewSelection(indexes).assign(other.t_bus.viewSelection(indexes));
		this.br_r.viewSelection(indexes).assign(other.br_r.viewSelection(indexes));
		this.br_x.viewSelection(indexes).assign(other.br_x.viewSelection(indexes));
		this.br_b.viewSelection(indexes).assign(other.br_b.viewSelection(indexes));
		this.rate_a.viewSelection(indexes).assign(other.rate_a.viewSelection(indexes));
		this.rate_b.viewSelection(indexes).assign(other.rate_b.viewSelection(indexes));
		this.rate_c.viewSelection(indexes).assign(other.rate_c.viewSelection(indexes));
		this.tap.viewSelection(indexes).assign(other.tap.viewSelection(indexes));
		this.shift.viewSelection(indexes).assign(other.shift.viewSelection(indexes));
		this.br_status.viewSelection(indexes).assign(other.br_status.viewSelection(indexes));
		this.ang_min.viewSelection(indexes).assign(other.ang_min.viewSelection(indexes));
		this.ang_max.viewSelection(indexes).assign(other.ang_max.viewSelection(indexes));

		this.Pf.viewSelection(indexes).assign(other.Pf.viewSelection(indexes));
		this.Qf.viewSelection(indexes).assign(other.Qf.viewSelection(indexes));
		this.Pt.viewSelection(indexes).assign(other.Pt.viewSelection(indexes));
		this.Qt.viewSelection(indexes).assign(other.Qt.viewSelection(indexes));

		this.mu_Sf.viewSelection(indexes).assign(other.mu_Sf.viewSelection(indexes));
		this.mu_St.viewSelection(indexes).assign(other.mu_St.viewSelection(indexes));
		this.mu_angmin.viewSelection(indexes).assign(other.mu_angmin.viewSelection(indexes));
		this.mu_angmax.viewSelection(indexes).assign(other.mu_angmax.viewSelection(indexes));
	}

	/**
	 *
	 * @param other
	 */
	public void update(DoubleMatrix2D other) {
		update(other, null);
	}

	/**
	 *
	 * @param other
	 * @param indexes
	 */
	@SuppressWarnings("static-access")
	public void update(DoubleMatrix2D other, int[] indexes) {

		this.f_bus.viewSelection(indexes).assign( util.intm(other.viewColumn(F_BUS).viewSelection(indexes)) );
		this.t_bus.viewSelection(indexes).assign( util.intm(other.viewColumn(T_BUS).viewSelection(indexes)) );
		this.br_r.viewSelection(indexes).assign(other.viewColumn(BR_R).viewSelection(indexes));
		this.br_x.viewSelection(indexes).assign(other.viewColumn(BR_X).viewSelection(indexes));
		this.br_b.viewSelection(indexes).assign(other.viewColumn(BR_B).viewSelection(indexes));
		this.rate_a.viewSelection(indexes).assign(other.viewColumn(RATE_A).viewSelection(indexes));
		this.rate_b.viewSelection(indexes).assign(other.viewColumn(RATE_B).viewSelection(indexes));
		this.rate_c.viewSelection(indexes).assign(other.viewColumn(RATE_C).viewSelection(indexes));
		this.tap.viewSelection(indexes).assign(other.viewColumn(TAP).viewSelection(indexes));
		this.shift.viewSelection(indexes).assign(other.viewColumn(SHIFT).viewSelection(indexes));
		this.br_status.viewSelection(indexes).assign( util.intm(other.viewColumn(BR_STATUS).viewSelection(indexes)) );
		this.ang_min.viewSelection(indexes).assign(other.viewColumn(ANGMIN).viewSelection(indexes));
		this.ang_max.viewSelection(indexes).assign(other.viewColumn(ANGMAX).viewSelection(indexes));

		if (other.columns() > ANGMAX) {
			this.Pf.viewSelection(indexes).assign(other.viewColumn(PF).viewSelection(indexes));
			this.Qf.viewSelection(indexes).assign(other.viewColumn(QF).viewSelection(indexes));
			this.Pt.viewSelection(indexes).assign(other.viewColumn(PT).viewSelection(indexes));
			this.Qt.viewSelection(indexes).assign(other.viewColumn(QT).viewSelection(indexes));
		}

		if (other.columns() > QT) {
			this.mu_Sf.viewSelection(indexes).assign(other.viewColumn(MU_SF).viewSelection(indexes));
			this.mu_St.viewSelection(indexes).assign(other.viewColumn(MU_ST).viewSelection(indexes));
			this.mu_angmin.viewSelection(indexes).assign(other.viewColumn(MU_ANGMIN).viewSelection(indexes));
			this.mu_angmax.viewSelection(indexes).assign(other.viewColumn(MU_ANGMAX).viewSelection(indexes));
		}
	}

	public DoubleMatrix2D toMatrix() {
		return toMatrix(true, true);
	}

	/**
	 *
	 * @param pf include power flow solution data
	 * @param opf include optimal power flow solution data
	 * @return branch data matrix
	 */
	@SuppressWarnings("static-access")
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

		matrix.viewColumn(F_BUS).assign( util.dblm(this.f_bus) );
		matrix.viewColumn(T_BUS).assign( util.dblm(this.t_bus) );
		matrix.viewColumn(BR_R).assign(this.br_r);
		matrix.viewColumn(BR_X).assign(this.br_x);
		matrix.viewColumn(BR_B).assign(this.br_b);
		matrix.viewColumn(RATE_A).assign(this.rate_a);
		matrix.viewColumn(RATE_B).assign(this.rate_b);
		matrix.viewColumn(RATE_C).assign(this.rate_c);
		matrix.viewColumn(TAP).assign(this.tap);
		matrix.viewColumn(SHIFT).assign(this.shift);
		matrix.viewColumn(BR_STATUS).assign( util.dblm(this.br_status) );
		matrix.viewColumn(ANGMIN).assign(this.ang_min);
		matrix.viewColumn(ANGMAX).assign(this.ang_max);

		if (pf) {
			matrix.viewColumn(PF).assign(this.Pf);
			matrix.viewColumn(QF).assign(this.Qf);
			matrix.viewColumn(PT).assign(this.Pt);
			matrix.viewColumn(QT).assign(this.Qt);
		}

		if (opf) {
			matrix.viewColumn(MU_SF).assign(this.mu_Sf);
			matrix.viewColumn(MU_ST).assign(this.mu_St);
			matrix.viewColumn(MU_ANGMIN).assign(this.mu_angmin);
			matrix.viewColumn(MU_ANGMAX).assign(this.mu_angmax);
		}

		return matrix;
	}
}
