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

import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tint.IntMatrix1D;
import cern.colt.util.tdouble.Djp_util;

/**
 *
 * @author Richard Lincoln
 *
 */
public class Djp_gen {

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
		return (int) gen_bus.size();
	}

	/**
	 *
	 * @return a full copy of the generator data.
	 */
	public Djp_gen copy() {
		return copy(null);
	}

	/**
	 *
	 * @return a copy of the generator data for the given indexes.
	 */
	public Djp_gen copy(int[] indexes) {
		Djp_gen other = new Djp_gen();

		other.gen_bus = gen_bus.viewSelection(indexes).copy();
		other.Pg = Pg.viewSelection(indexes).copy();
		other.Qg = Qg.viewSelection(indexes).copy();
		other.Qmax = Qmax.viewSelection(indexes).copy();
		other.Qmin = Qmin.viewSelection(indexes).copy();
		other.Vg = Vg.viewSelection(indexes).copy();
		other.mBase = mBase.viewSelection(indexes).copy();
		other.gen_status = gen_status.viewSelection(indexes).copy();
		other.Pmax = Pmax.viewSelection(indexes).copy();
		other.Pmin = Pmin.viewSelection(indexes).copy();
		other.Pc1 = Pc1.viewSelection(indexes).copy();
		other.Pc2 = Pc2.viewSelection(indexes).copy();
		other.Qc1min = Qc1min.viewSelection(indexes).copy();
		other.Qc1max = Qc1max.viewSelection(indexes).copy();
		other.Qc2min = Qc2min.viewSelection(indexes).copy();
		other.Qc2max = Qc2max.viewSelection(indexes).copy();
		other.ramp_agc = ramp_agc.viewSelection(indexes).copy();
		other.ramp_10 = ramp_10.viewSelection(indexes).copy();
		other.ramp_30 = ramp_30.viewSelection(indexes).copy();
		other.ramp_q = ramp_q.viewSelection(indexes).copy();
		other.apf = apf.viewSelection(indexes).copy();

		if (mu_Pmax != null)
			other.mu_Pmax = mu_Pmax.viewSelection(indexes).copy();
		if (mu_Pmin != null)
			other.mu_Pmin = mu_Pmin.viewSelection(indexes).copy();
		if (mu_Qmax != null)
			other.mu_Qmax = mu_Qmax.viewSelection(indexes).copy();
		if (mu_Qmin != null)
			other.mu_Qmin = mu_Qmin.viewSelection(indexes).copy();

		return other;
	}

	/**
	 * Updates the generator data for the given indexes.
	 *
	 * @param other generator data source
	 * @param indexes generator indexes
	 */
	public void update(Djp_gen other, int[] indexes) {

		gen_bus.viewSelection(indexes).assign(other.gen_bus.viewSelection(indexes));
		Pg.viewSelection(indexes).assign(other.Pg.viewSelection(indexes));
		Qg.viewSelection(indexes).assign(other.Qg.viewSelection(indexes));
		Qmax.viewSelection(indexes).assign(other.Qmax.viewSelection(indexes));
		Qmin.viewSelection(indexes).assign(other.Qmin.viewSelection(indexes));
		Vg.viewSelection(indexes).assign(other.Vg.viewSelection(indexes));
		mBase.viewSelection(indexes).assign(other.mBase.viewSelection(indexes));
		gen_status.viewSelection(indexes).assign(other.gen_status.viewSelection(indexes));
		Pmax.viewSelection(indexes).assign(other.Pmax.viewSelection(indexes));
		Pmin.viewSelection(indexes).assign(other.Pmin.viewSelection(indexes));
		Pc1.viewSelection(indexes).assign(other.Pc1.viewSelection(indexes));
		Pc2.viewSelection(indexes).assign(other.Pc2.viewSelection(indexes));
		Qc1min.viewSelection(indexes).assign(other.Qc1min.viewSelection(indexes));
		Qc1max.viewSelection(indexes).assign(other.Qc1max.viewSelection(indexes));
		Qc2min.viewSelection(indexes).assign(other.Qc2min.viewSelection(indexes));
		Qc2max.viewSelection(indexes).assign(other.Qc2max.viewSelection(indexes));
		ramp_agc.viewSelection(indexes).assign(other.ramp_agc.viewSelection(indexes));
		ramp_10.viewSelection(indexes).assign(other.ramp_10.viewSelection(indexes));
		ramp_30.viewSelection(indexes).assign(other.ramp_30.viewSelection(indexes));
		ramp_q.viewSelection(indexes).assign(other.ramp_q.viewSelection(indexes));
		apf.viewSelection(indexes).assign(other.apf.viewSelection(indexes));

		if (mu_Pmax != null)
			mu_Pmax.viewSelection(indexes).assign(other.mu_Pmax.viewSelection(indexes));
		if (mu_Pmin != null)
			mu_Pmin.viewSelection(indexes).assign(other.mu_Pmin.viewSelection(indexes));
		if (mu_Qmax != null)
			mu_Qmax.viewSelection(indexes).assign(other.mu_Qmax.viewSelection(indexes));
		if (mu_Qmin != null)
			mu_Qmin.viewSelection(indexes).assign(other.mu_Qmin.viewSelection(indexes));
	}

	/**
	 *
	 * @param other
	 */
//	public void fromMatrix(DoubleMatrix2D other) {
//
//		gen_bus = Djp_util.intm(other.viewColumn(GEN_BUS));
//		Pg = other.viewColumn(PG);
//		Qg = other.viewColumn(QG);
//		Qmax = other.viewColumn(QMAX);
//		Qmin = other.viewColumn(QMIN);
//		Vg = other.viewColumn(VG);
//		mBase = other.viewColumn(MBASE);
//		gen_status = Djp_util.intm(other.viewColumn(GEN_STATUS));
//		Pmax = other.viewColumn(PMAX);
//		Pmin = other.viewColumn(PMIN);
//		Pc1 = other.viewColumn(PC1);
//		Pc2 = other.viewColumn(PC2);
//		Qc1min = other.viewColumn(QC1MIN);
//		Qc1max = other.viewColumn(QC1MAX);
//		Qc2min = other.viewColumn(QC2MIN);
//		Qc2max = other.viewColumn(QC2MAX);
//		ramp_agc = other.viewColumn(RAMP_AGC);
//		ramp_10 = other.viewColumn(RAMP_10);
//		ramp_30 = other.viewColumn(RAMP_30);
//		ramp_q = other.viewColumn(RAMP_Q);
//		apf = other.viewColumn(APF);
//
//		if (other.columns() > APF + 1) {
//			mu_Pmax = other.viewColumn(MU_PMAX);
//			mu_Pmin = other.viewColumn(MU_PMIN);
//			mu_Qmax = other.viewColumn(MU_QMAX);
//			mu_Qmin = other.viewColumn(MU_QMIN);
//		}
//	}

	/**
	 *
	 * @param other
	 */
	public static Djp_gen fromMatrix(DoubleMatrix2D other) {
		Djp_gen gen = new Djp_gen();

		gen.gen_bus = Djp_util.intm(other.viewColumn(GEN_BUS));
		gen.Pg = other.viewColumn(PG);
		gen.Qg = other.viewColumn(QG);
		gen.Qmax = other.viewColumn(QMAX);
		gen.Qmin = other.viewColumn(QMIN);
		gen.Vg = other.viewColumn(VG);
		gen.mBase = other.viewColumn(MBASE);
		gen.gen_status = Djp_util.intm(other.viewColumn(GEN_STATUS));
		gen.Pmax = other.viewColumn(PMAX);
		gen.Pmin = other.viewColumn(PMIN);
		gen.Pc1 = other.viewColumn(PC1);
		gen.Pc2 = other.viewColumn(PC2);
		gen.Qc1min = other.viewColumn(QC1MIN);
		gen.Qc1max = other.viewColumn(QC1MAX);
		gen.Qc2min = other.viewColumn(QC2MIN);
		gen.Qc2max = other.viewColumn(QC2MAX);
		gen.ramp_agc = other.viewColumn(RAMP_AGC);
		gen.ramp_10 = other.viewColumn(RAMP_10);
		gen.ramp_30 = other.viewColumn(RAMP_30);
		gen.ramp_q = other.viewColumn(RAMP_Q);
		gen.apf = other.viewColumn(APF);

		if (other.columns() > APF + 1) {
			gen.mu_Pmax = other.viewColumn(MU_PMAX);
			gen.mu_Pmin = other.viewColumn(MU_PMIN);
			gen.mu_Qmax = other.viewColumn(MU_QMAX);
			gen.mu_Qmin = other.viewColumn(MU_QMIN);
		}

		return gen;
	}

	public DoubleMatrix2D toMatrix() {
		return toMatrix(true);
	}

	/**
	 *
	 * @param opf include OPF solution data
	 * @return generator data matrix
	 */
	public DoubleMatrix2D toMatrix(boolean opf) {
		DoubleMatrix2D matrix;
		if (opf) {
			matrix = DoubleFactory2D.dense.make(size(), 25);
		} else {
			matrix = DoubleFactory2D.dense.make(size(), 21);
		}

		matrix.viewColumn(GEN_BUS).assign( Djp_util.dblm(gen_bus) );
		matrix.viewColumn(PG).assign(Pg);
		matrix.viewColumn(QG).assign(Qg);
		matrix.viewColumn(QMAX).assign(Qmax);
		matrix.viewColumn(QMIN).assign(Qmin);
		matrix.viewColumn(VG).assign(Vg);
		matrix.viewColumn(MBASE).assign(mBase);
		matrix.viewColumn(GEN_STATUS).assign( Djp_util.dblm(gen_status) );
		matrix.viewColumn(PMAX).assign(Pmax);
		matrix.viewColumn(PMIN).assign(Pmin);
		matrix.viewColumn(PC1).assign(Pc1);
		matrix.viewColumn(PC2).assign(Pc2);
		matrix.viewColumn(QC1MIN).assign(Qc1min);
		matrix.viewColumn(QC1MAX).assign(Qc1max);
		matrix.viewColumn(QC2MIN).assign(Qc2min);
		matrix.viewColumn(QC2MAX).assign(Qc2max);
		matrix.viewColumn(RAMP_AGC).assign(ramp_agc);
		matrix.viewColumn(RAMP_10).assign(ramp_10);
		matrix.viewColumn(RAMP_30).assign(ramp_30);
		matrix.viewColumn(RAMP_Q).assign(ramp_q);
		matrix.viewColumn(APF).assign(apf);

		if (opf) {
			matrix.viewColumn(MU_PMAX).assign(mu_Pmax);
			matrix.viewColumn(MU_PMIN).assign(mu_Pmin);
			matrix.viewColumn(MU_QMAX).assign(mu_Qmax);
			matrix.viewColumn(MU_QMIN).assign(mu_Qmin);
		}

		return matrix;
	}

}
