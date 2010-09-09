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
import edu.cornell.pserc.jpower.tdouble.util.DZjp_util;

/**
 *
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class DZjp_gen {

	private static final DZjp_util util = new DZjp_util();

	private static final int GEN_BUS	= 0;
	private static final int PG			= 1;
	private static final int QG			= 2;
	private static final int QMAX		= 3;
	private static final int QMIN		= 4;
	private static final int VG			= 5;
	private static final int MBASE		= 6;
	private static final int GEN_STATUS	= 7;
	private static final int PMAX		= 8;
	private static final int PMIN		= 9;
	private static final int PC1		= 10;
	private static final int PC2		= 11;
	private static final int QC1MIN		= 12;
	private static final int QC1MAX		= 13;
	private static final int QC2MIN		= 14;
	private static final int QC2MAX		= 15;
	private static final int RAMP_AGC	= 16;
	private static final int RAMP_10	= 17;
	private static final int RAMP_30	= 18;
	private static final int RAMP_Q		= 19;
	private static final int APF		= 20;

	private static final int MU_PMAX	= 21;
	private static final int MU_PMIN	= 22;
	private static final int MU_QMAX	= 23;
	private static final int MU_QMIN	= 24;

	/** bus number */
	public IntMatrix1D gen_bus;

	/** Pg, real power output (MW) */
	public DoubleMatrix1D Pg;

	/** Qg, reactive power output (MVAr) */
	public DoubleMatrix1D Qg;

	/** Qmax, maximum reactive power output at Pmin (MVAr) */
	public DoubleMatrix1D Qmax;

	/** Qmin, minimum reactive power output at Pmin (MVAr) */
	public DoubleMatrix1D Qmin;

	/** Vg, voltage magnitude setpoprotected static final int (p.u.) */
	public DoubleMatrix1D Vg;

	/** mBase, total MVA base of this machine, defaults to baseMVA */
	public DoubleMatrix1D mBase;

	/** status, 1 - machine in service, 0 - machine out of service */
	public IntMatrix1D gen_status;

	/** Pmax, maximum real power output (MW) */
	public DoubleMatrix1D Pmax;

	/** Pmin, minimum real power output (MW) */
	public DoubleMatrix1D Pmin;

	/** Pc1, lower real power output of PQ capability curve (MW) */
	public DoubleMatrix1D Pc1;

	/** Pc2, upper real power output of PQ capability curve (MW) */
	public DoubleMatrix1D Pc2;

	/** Qc1min, minimum reactive power output at Pc1 (MVAr) */
	public DoubleMatrix1D Qc1min;

	/** Qc1max, maximum reactive power output at Pc1 (MVAr) */
	public DoubleMatrix1D Qc1max;

	/** Qc2min, minimum reactive power output at Pc2 (MVAr) */
	public DoubleMatrix1D Qc2min;

	/** Qc2max, maximum reactive power output at Pc2 (MVAr) */
	public DoubleMatrix1D Qc2max;

	/** ramp rate for load following/AGC (MW/min) */
	public DoubleMatrix1D ramp_agc;

	/** ramp rate for 10 minute reserves (MW) */
	public DoubleMatrix1D ramp_10;

	/** ramp rate for 30 minute reserves (MW) */
	public DoubleMatrix1D ramp_30;

	/** ramp rate for reactive power (2 sec timescale) (MVAr/min) */
	public DoubleMatrix1D ramp_q;

	/** area participation factor */
	public DoubleMatrix1D apf;

	/*
	 * included in opf solution, not necessarily in input
	 * assume objective function has units, u
	 *
	 */

	/** Kuhn-Tucker multiplier on upper Pg limit (u/MW) */
	public DoubleMatrix1D mu_Pmax;

	/** Kuhn-Tucker multiplier on lower Pg limit (u/MW) */
	public DoubleMatrix1D mu_Pmin;

	/** Kuhn-Tucker multiplier on upper Qg limit (u/MVAr) */
	public DoubleMatrix1D mu_Qmax;

	/** Kuhn-Tucker multiplier on lower Qg limit (u/MVAr) */
	public DoubleMatrix1D mu_Qmin;

	/**
	 *
	 * @return the number of generators.
	 */
	public int size() {
		return (int) this.gen_bus.size();
	}

	/**
	 *
	 * @return a full copy of the generator data.
	 */
	public DZjp_gen copy() {
		return copy(null);
	}

	/**
	 *
	 * @return a copy of the generator data for the given indexes.
	 */
	public DZjp_gen copy(int[] indexes) {
		DZjp_gen other = new DZjp_gen();

		other.gen_bus = this.gen_bus.viewSelection(indexes).copy();
		other.Pg = this.Pg.viewSelection(indexes).copy();
		other.Qg = this.Qg.viewSelection(indexes).copy();
		other.Qmax = this.Qmax.viewSelection(indexes).copy();
		other.Qmin = this.Qmin.viewSelection(indexes).copy();
		other.Vg = this.Vg.viewSelection(indexes).copy();
		other.mBase = this.mBase.viewSelection(indexes).copy();
		other.gen_status = this.gen_status.viewSelection(indexes).copy();
		other.Pmax = this.Pmax.viewSelection(indexes).copy();
		other.Pmin = this.Pmin.viewSelection(indexes).copy();
		other.Pc1 = this.Pc1.viewSelection(indexes).copy();
		other.Pc2 = this.Pc2.viewSelection(indexes).copy();
		other.Qc1min = this.Qc1min.viewSelection(indexes).copy();
		other.Qc1max = this.Qc1max.viewSelection(indexes).copy();
		other.Qc2min = this.Qc2min.viewSelection(indexes).copy();
		other.Qc2max = this.Qc2max.viewSelection(indexes).copy();
		other.ramp_agc = this.ramp_agc.viewSelection(indexes).copy();
		other.ramp_10 = this.ramp_10.viewSelection(indexes).copy();
		other.ramp_30 = this.ramp_30.viewSelection(indexes).copy();
		other.ramp_q = this.ramp_q.viewSelection(indexes).copy();
		other.apf = this.apf.viewSelection(indexes).copy();

		other.mu_Pmax = this.mu_Pmax.viewSelection(indexes).copy();
		other.mu_Pmin = this.mu_Pmin.viewSelection(indexes).copy();
		other.mu_Qmax = this.mu_Qmax.viewSelection(indexes).copy();
		other.mu_Qmin = this.mu_Qmin.viewSelection(indexes).copy();

		return other;
	}

	/**
	 * Updates the generator data for the given indexes.
	 *
	 * @param other generator data source
	 * @param indexes generator indexes
	 */
	public void update(DZjp_gen other, int[] indexes) {

		this.gen_bus.viewSelection(indexes).assign(other.gen_bus.viewSelection(indexes));
		this.Pg.viewSelection(indexes).assign(other.Pg.viewSelection(indexes));
		this.Qg.viewSelection(indexes).assign(other.Qg.viewSelection(indexes));
		this.Qmax.viewSelection(indexes).assign(other.Qmax.viewSelection(indexes));
		this.Qmin.viewSelection(indexes).assign(other.Qmin.viewSelection(indexes));
		this.Vg.viewSelection(indexes).assign(other.Vg.viewSelection(indexes));
		this.mBase.viewSelection(indexes).assign(other.mBase.viewSelection(indexes));
		this.gen_status.viewSelection(indexes).assign(other.gen_status.viewSelection(indexes));
		this.Pmax.viewSelection(indexes).assign(other.Pmax.viewSelection(indexes));
		this.Pmin.viewSelection(indexes).assign(other.Pmin.viewSelection(indexes));
		this.Pc1.viewSelection(indexes).assign(other.Pc1.viewSelection(indexes));
		this.Pc2.viewSelection(indexes).assign(other.Pc2.viewSelection(indexes));
		this.Qc1min.viewSelection(indexes).assign(other.Qc1min.viewSelection(indexes));
		this.Qc1max.viewSelection(indexes).assign(other.Qc1max.viewSelection(indexes));
		this.Qc2min.viewSelection(indexes).assign(other.Qc2min.viewSelection(indexes));
		this.Qc2max.viewSelection(indexes).assign(other.Qc2max.viewSelection(indexes));
		this.ramp_agc.viewSelection(indexes).assign(other.ramp_agc.viewSelection(indexes));
		this.ramp_10.viewSelection(indexes).assign(other.ramp_10.viewSelection(indexes));
		this.ramp_30.viewSelection(indexes).assign(other.ramp_30.viewSelection(indexes));
		this.ramp_q.viewSelection(indexes).assign(other.ramp_q.viewSelection(indexes));
		this.apf.viewSelection(indexes).assign(other.apf.viewSelection(indexes));

		this.mu_Pmax.viewSelection(indexes).assign(other.mu_Pmax.viewSelection(indexes));
		this.mu_Pmin.viewSelection(indexes).assign(other.mu_Pmin.viewSelection(indexes));
		this.mu_Qmax.viewSelection(indexes).assign(other.mu_Qmax.viewSelection(indexes));
		this.mu_Qmin.viewSelection(indexes).assign(other.mu_Qmin.viewSelection(indexes));
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

		this.gen_bus.viewSelection(indexes).assign( util.intm(other.viewColumn(GEN_BUS).viewSelection(indexes)) );
		this.Pg.viewSelection(indexes).assign(other.viewColumn(PG).viewSelection(indexes));
		this.Qg.viewSelection(indexes).assign(other.viewColumn(QG).viewSelection(indexes));
		this.Qmax.viewSelection(indexes).assign(other.viewColumn(QMAX).viewSelection(indexes));
		this.Qmin.viewSelection(indexes).assign(other.viewColumn(QMIN).viewSelection(indexes));
		this.Vg.viewSelection(indexes).assign(other.viewColumn(VG).viewSelection(indexes));
		this.mBase.viewSelection(indexes).assign(other.viewColumn(MBASE).viewSelection(indexes));
		this.gen_status.viewSelection(indexes).assign( util.intm(other.viewColumn(GEN_STATUS).viewSelection(indexes)) );
		this.Pmax.viewSelection(indexes).assign(other.viewColumn(PMAX).viewSelection(indexes));
		this.Pmin.viewSelection(indexes).assign(other.viewColumn(PMIN).viewSelection(indexes));
		this.Pc1.viewSelection(indexes).assign(other.viewColumn(PC1).viewSelection(indexes));
		this.Pc2.viewSelection(indexes).assign(other.viewColumn(PC2).viewSelection(indexes));
		this.Qc1min.viewSelection(indexes).assign(other.viewColumn(QC1MIN).viewSelection(indexes));
		this.Qc1max.viewSelection(indexes).assign(other.viewColumn(QC1MAX).viewSelection(indexes));
		this.Qc2min.viewSelection(indexes).assign(other.viewColumn(QC2MIN).viewSelection(indexes));
		this.Qc2max.viewSelection(indexes).assign(other.viewColumn(QC2MAX).viewSelection(indexes));
		this.ramp_agc.viewSelection(indexes).assign(other.viewColumn(RAMP_AGC).viewSelection(indexes));
		this.ramp_10.viewSelection(indexes).assign(other.viewColumn(RAMP_10).viewSelection(indexes));
		this.ramp_30.viewSelection(indexes).assign(other.viewColumn(RAMP_30).viewSelection(indexes));
		this.ramp_q.viewSelection(indexes).assign(other.viewColumn(RAMP_Q).viewSelection(indexes));
		this.apf.viewSelection(indexes).assign(other.viewColumn(APF).viewSelection(indexes));

		if (other.columns() > APF) {
			this.mu_Pmax.viewSelection(indexes).assign(other.viewColumn(MU_PMAX).viewSelection(indexes));
			this.mu_Pmin.viewSelection(indexes).assign(other.viewColumn(MU_PMIN).viewSelection(indexes));
			this.mu_Qmax.viewSelection(indexes).assign(other.viewColumn(MU_QMAX).viewSelection(indexes));
			this.mu_Qmin.viewSelection(indexes).assign(other.viewColumn(MU_QMIN).viewSelection(indexes));
		}
	}

	public DoubleMatrix2D toMatrix() {
		return toMatrix(true);
	}

	/**
	 *
	 * @param opf include OPF solution data
	 * @return generator data matrix
	 */
	@SuppressWarnings("static-access")
	public DoubleMatrix2D toMatrix(boolean opf) {
		DoubleMatrix2D matrix;
		if (opf) {
			matrix = DoubleFactory2D.dense.make(size(), 25);
		} else {
			matrix = DoubleFactory2D.dense.make(size(), 21);
		}

		matrix.viewColumn(GEN_BUS).assign( util.dblm(this.gen_bus) );
		matrix.viewColumn(PG).assign(this.Pg);
		matrix.viewColumn(QG).assign(this.Qg);
		matrix.viewColumn(QMAX).assign(this.Qmax);
		matrix.viewColumn(QMIN).assign(this.Qmin);
		matrix.viewColumn(VG).assign(this.Vg);
		matrix.viewColumn(MBASE).assign(this.mBase);
		matrix.viewColumn(GEN_STATUS).assign( util.dblm(this.gen_status) );
		matrix.viewColumn(PMAX).assign(this.Pmax);
		matrix.viewColumn(PMIN).assign(this.Pmin);
		matrix.viewColumn(PC1).assign(this.Pc1);
		matrix.viewColumn(PC2).assign(this.Pc2);
		matrix.viewColumn(QC1MIN).assign(this.Qc1min);
		matrix.viewColumn(QC1MAX).assign(this.Qc1max);
		matrix.viewColumn(QC2MIN).assign(this.Qc2min);
		matrix.viewColumn(QC2MAX).assign(this.Qc2max);
		matrix.viewColumn(RAMP_AGC).assign(this.ramp_agc);
		matrix.viewColumn(RAMP_10).assign(this.ramp_10);
		matrix.viewColumn(RAMP_30).assign(this.ramp_30);
		matrix.viewColumn(RAMP_Q).assign(this.ramp_q);
		matrix.viewColumn(APF).assign(this.apf);

		if (opf) {
			matrix.viewColumn(MU_PMAX).assign(this.mu_Pmax);
			matrix.viewColumn(MU_PMIN).assign(this.mu_Pmin);
			matrix.viewColumn(MU_QMAX).assign(this.mu_Qmax);
			matrix.viewColumn(MU_QMIN).assign(this.mu_Qmin);
		}

		return matrix;
	}

}
