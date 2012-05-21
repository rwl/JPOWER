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
public class Gen {

	public static final int GEN_BUS	= 0;
	public static final int PG			= 1;
	public static final int QG			= 2;
	public static final int QMAX		= 3;
	public static final int QMIN		= 4;
	public static final int VG			= 5;
	public static final int MBASE		= 6;
	public static final int GEN_STATUS	= 7;
	public static final int PMAX		= 8;
	public static final int PMIN		= 9;
	public static final int PC1		= 10;
	public static final int PC2		= 11;
	public static final int QC1MIN		= 12;
	public static final int QC1MAX		= 13;
	public static final int QC2MIN		= 14;
	public static final int QC2MAX		= 15;
	public static final int RAMP_AGC	= 16;
	public static final int RAMP_10	= 17;
	public static final int RAMP_30	= 18;
	public static final int RAMP_Q		= 19;
	public static final int APF		= 20;

	public static final int MU_PMAX	= 21;
	public static final int MU_PMIN	= 22;
	public static final int MU_QMAX	= 23;
	public static final int MU_QMIN	= 24;

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
	public Gen copy() {
		return copy(null);
	}

	/**
	 *
	 * @return a copy of the generator data for the given indexes.
	 */
	public Gen copy(int[] indexes) {
		Gen other = new Gen();

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

		if (Pc1 != null)
			other.Pc1 = Pc1.viewSelection(indexes).copy();
		if (Pc2 != null)
			other.Pc2 = Pc2.viewSelection(indexes).copy();
		if (Qc1min != null)
			other.Qc1min = Qc1min.viewSelection(indexes).copy();
		if (Qc1max != null)
			other.Qc1max = Qc1max.viewSelection(indexes).copy();
		if (Qc2min != null)
			other.Qc2min = Qc2min.viewSelection(indexes).copy();
		if (Qc2max != null)
			other.Qc2max = Qc2max.viewSelection(indexes).copy();
		if (ramp_agc != null)
			other.ramp_agc = ramp_agc.viewSelection(indexes).copy();
		if (ramp_10 != null)
			other.ramp_10 = ramp_10.viewSelection(indexes).copy();
		if (ramp_30 != null)
			other.ramp_30 = ramp_30.viewSelection(indexes).copy();
		if (ramp_q != null)
			other.ramp_q = ramp_q.viewSelection(indexes).copy();
		if (apf != null)
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
	public void update(Gen other, int[] indexes) {

//		gen_bus.viewSelection(indexes).assign(other.gen_bus.viewSelection(indexes));
//		Pg.viewSelection(indexes).assign(other.Pg.viewSelection(indexes));
//		Qg.viewSelection(indexes).assign(other.Qg.viewSelection(indexes));
//		Qmax.viewSelection(indexes).assign(other.Qmax.viewSelection(indexes));
//		Qmin.viewSelection(indexes).assign(other.Qmin.viewSelection(indexes));
//		Vg.viewSelection(indexes).assign(other.Vg.viewSelection(indexes));
//		mBase.viewSelection(indexes).assign(other.mBase.viewSelection(indexes));
//		gen_status.viewSelection(indexes).assign(other.gen_status.viewSelection(indexes));
//		Pmax.viewSelection(indexes).assign(other.Pmax.viewSelection(indexes));
//		Pmin.viewSelection(indexes).assign(other.Pmin.viewSelection(indexes));
//		Pc1.viewSelection(indexes).assign(other.Pc1.viewSelection(indexes));
//		Pc2.viewSelection(indexes).assign(other.Pc2.viewSelection(indexes));
//		Qc1min.viewSelection(indexes).assign(other.Qc1min.viewSelection(indexes));
//		Qc1max.viewSelection(indexes).assign(other.Qc1max.viewSelection(indexes));
//		Qc2min.viewSelection(indexes).assign(other.Qc2min.viewSelection(indexes));
//		Qc2max.viewSelection(indexes).assign(other.Qc2max.viewSelection(indexes));
//		ramp_agc.viewSelection(indexes).assign(other.ramp_agc.viewSelection(indexes));
//		ramp_10.viewSelection(indexes).assign(other.ramp_10.viewSelection(indexes));
//		ramp_30.viewSelection(indexes).assign(other.ramp_30.viewSelection(indexes));
//		ramp_q.viewSelection(indexes).assign(other.ramp_q.viewSelection(indexes));
//		apf.viewSelection(indexes).assign(other.apf.viewSelection(indexes));
//
//		if (mu_Pmax != null)
//			mu_Pmax.viewSelection(indexes).assign(other.mu_Pmax.viewSelection(indexes));
//		if (mu_Pmin != null)
//			mu_Pmin.viewSelection(indexes).assign(other.mu_Pmin.viewSelection(indexes));
//		if (mu_Qmax != null)
//			mu_Qmax.viewSelection(indexes).assign(other.mu_Qmax.viewSelection(indexes));
//		if (mu_Qmin != null)
//			mu_Qmin.viewSelection(indexes).assign(other.mu_Qmin.viewSelection(indexes));

		gen_bus.viewSelection(indexes).assign(other.gen_bus);
		Pg.viewSelection(indexes).assign(other.Pg);
		Qg.viewSelection(indexes).assign(other.Qg);
		Qmax.viewSelection(indexes).assign(other.Qmax);
		Qmin.viewSelection(indexes).assign(other.Qmin);
		Vg.viewSelection(indexes).assign(other.Vg);
		mBase.viewSelection(indexes).assign(other.mBase);
		gen_status.viewSelection(indexes).assign(other.gen_status);
		Pmax.viewSelection(indexes).assign(other.Pmax);
		Pmin.viewSelection(indexes).assign(other.Pmin);

		if (Pc1 != null)
			Pc1.viewSelection(indexes).assign(other.Pc1);
		if (Pc2 != null)
			Pc2.viewSelection(indexes).assign(other.Pc2);
		if (Qc1min != null)
			Qc1min.viewSelection(indexes).assign(other.Qc1min);
		if (Qc1max != null)
			Qc1max.viewSelection(indexes).assign(other.Qc1max);
		if (Qc2min != null)
			Qc2min.viewSelection(indexes).assign(other.Qc2min);
		if (Qc2max != null)
			Qc2max.viewSelection(indexes).assign(other.Qc2max);
		if (ramp_agc != null)
			ramp_agc.viewSelection(indexes).assign(other.ramp_agc);
		if (ramp_10 != null)
			ramp_10.viewSelection(indexes).assign(other.ramp_10);
		if (ramp_30 != null)
			ramp_30.viewSelection(indexes).assign(other.ramp_30);
		if (ramp_q != null)
			ramp_q.viewSelection(indexes).assign(other.ramp_q);
		if (apf != null)
			apf.viewSelection(indexes).assign(other.apf);

		if (mu_Pmax != null)
			mu_Pmax.viewSelection(indexes).assign(other.mu_Pmax);
		if (mu_Pmin != null)
			mu_Pmin.viewSelection(indexes).assign(other.mu_Pmin);
		if (mu_Qmax != null)
			mu_Qmax.viewSelection(indexes).assign(other.mu_Qmax);
		if (mu_Qmin != null)
			mu_Qmin.viewSelection(indexes).assign(other.mu_Qmin);
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
	public static Gen fromMatrix(DoubleMatrix2D other) {
		Gen gen = new Gen();

		gen.gen_bus = intm(other.viewColumn(GEN_BUS));
		gen.Pg = other.viewColumn(PG);
		gen.Qg = other.viewColumn(QG);
		gen.Qmax = other.viewColumn(QMAX);
		gen.Qmin = other.viewColumn(QMIN);
		gen.Vg = other.viewColumn(VG);
		gen.mBase = other.viewColumn(MBASE);
		gen.gen_status = intm(other.viewColumn(GEN_STATUS));
		gen.Pmax = other.viewColumn(PMAX);
		gen.Pmin = other.viewColumn(PMIN);

		if (other.columns() > PMIN + 1) {
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
		}

		if (other.columns() > APF + 1) {
			gen.mu_Pmax = other.viewColumn(MU_PMAX);
			gen.mu_Pmin = other.viewColumn(MU_PMIN);
			gen.mu_Qmax = other.viewColumn(MU_QMAX);
			gen.mu_Qmin = other.viewColumn(MU_QMIN);
		}

		return gen;
	}

	public static Gen fromMatrix(double[][] data) {
		return fromMatrix(DoubleFactory2D.dense.make(data));
	}

	public DoubleMatrix2D toMatrix() {
		boolean opf = (mu_Pmax != null);
		return toMatrix(opf);
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

		matrix.viewColumn(GEN_BUS).assign( dblm(gen_bus) );
		matrix.viewColumn(PG).assign(Pg);
		matrix.viewColumn(QG).assign(Qg);
		matrix.viewColumn(QMAX).assign(Qmax);
		matrix.viewColumn(QMIN).assign(Qmin);
		matrix.viewColumn(VG).assign(Vg);
		matrix.viewColumn(MBASE).assign(mBase);
		matrix.viewColumn(GEN_STATUS).assign( dblm(gen_status) );
		matrix.viewColumn(PMAX).assign(Pmax);
		matrix.viewColumn(PMIN).assign(Pmin);

		if (Pc1 != null)
			matrix.viewColumn(PC1).assign(Pc1);
		if (Pc2 != null)
			matrix.viewColumn(PC2).assign(Pc2);
		if (Qc1min != null)
			matrix.viewColumn(QC1MIN).assign(Qc1min);
		if (Qc1max != null)
			matrix.viewColumn(QC1MAX).assign(Qc1max);
		if (Qc2min != null)
			matrix.viewColumn(QC2MIN).assign(Qc2min);
		if (Qc2max != null)
			matrix.viewColumn(QC2MAX).assign(Qc2max);
		if (ramp_agc != null)
			matrix.viewColumn(RAMP_AGC).assign(ramp_agc);
		if (ramp_10 != null)
			matrix.viewColumn(RAMP_10).assign(ramp_10);
		if (ramp_30 != null)
			matrix.viewColumn(RAMP_30).assign(ramp_30);
		if (ramp_q != null)
			matrix.viewColumn(RAMP_Q).assign(ramp_q);
		if (apf != null)
			matrix.viewColumn(APF).assign(apf);

		if (opf) {
			matrix.viewColumn(MU_PMAX).assign(mu_Pmax);
			matrix.viewColumn(MU_PMIN).assign(mu_Pmin);
			matrix.viewColumn(MU_QMAX).assign(mu_Qmax);
			matrix.viewColumn(MU_QMIN).assign(mu_Qmin);
		}

		return matrix;
	}

	public double[][] toArray() {
		boolean opf = (mu_Pmax != null);
		return toArray(opf);
	}

	public double[][] toArray(boolean opf) {
		return toMatrix(opf).toArray();
	}

	@Override
	public String toString() {
		return toMatrix().toString();
	}

}
