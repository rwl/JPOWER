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

package edu.cornell.pserc.jpower.tdcomplex.jpc;

import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tint.IntMatrix1D;
import edu.cornell.pserc.jpower.tdcomplex.DZjp_idx;

public class DZjp_bus extends DZjp_idx {

	/** bus number (1 to 29997) */
	public IntMatrix1D bus_i;

	/** bus type (1 - PQ bus, 2 - PV bus, 3 - reference bus, 4 - isolated bus) */
	public IntMatrix1D bus_type;

	/** Pd, real power demand (MW) */
	public DoubleMatrix1D Pd;

	/** Qd, reactive power demand (MVAr) */
	public DoubleMatrix1D Qd;

	/** Gs, shunt conductance (MW at V = 1.0 p.u.) */
	public DoubleMatrix1D Gs;

	/** Bs, shunt susceptance (MVAr at V = 1.0 p.u.) */
	public DoubleMatrix1D Bs;

	/** area number, 1-100 */
	public IntMatrix1D bus_area;

	/** Vm, voltage magnitude (p.u.) */
	public DoubleMatrix1D Vm;

	/** Va, voltage angle (degrees) */
	public DoubleMatrix1D Va;

	/** baseKV, base voltage (kV) */
	public DoubleMatrix1D base_kV;

	/** zone, loss zone (1-999) */
	public IntMatrix1D zone;

	/** maxVm, maximum voltage magnitude (p.u.)	  (not in PTI format) */
	public DoubleMatrix1D Vmax;

	/** minVm, minimum voltage magnitude (p.u.)	  (not in PTI format) */
	public DoubleMatrix1D Vmin;

	/*
	 * included in opf solution, not necessarily in input
	 * assume objective function has units, u
	 *
	 */

	/** Lagrange multiplier on real power mismatch (u/MW) */
	public DoubleMatrix1D lam_P;

	/** Lagrange multiplier on reactive power mismatch (u/MVAr) */
	public DoubleMatrix1D lam_Q;

	/** Kuhn-Tucker multiplier on upper voltage limit (u/p.u.) */
	public DoubleMatrix1D mu_Vmax;

	/** Kuhn-Tucker multiplier on lower voltage limit (u/p.u.) */
	public DoubleMatrix1D mu_Vmin;

//	private static final int BUS_I		= 1;
//	private static final int BUS_TYPE	= 2;
//	private static final int PD			= 3;
//	private static final int QD			= 4;
//	private static final int GS			= 5;
//	private static final int BS			= 6;
//	private static final int BUS_AREA	= 7;
//	private static final int VM			= 8;
//	private static final int VA			= 9;
//	private static final int BASE_KV	= 10;
//	private static final int ZONE		= 11;
//	private static final int VMAX		= 12;
//	private static final int VMIN		= 13;
//
//	private static final int LAM_P		= 14;
//	private static final int LAM_Q		= 15;
//	private static final int MU_VMAX	= 16;
//	private static final int MU_VMIN	= 17;

	/**
	 *
	 * @return the number of buses.
	 */
	public int size() {
		return (int) this.bus_i.size();
	}

	/**
	 *
	 * @return a full copy of the bus data.
	 */
	public DZjp_bus copy() {
		return copy(null);
	}

	/**
	 *
	 * @param indexes
	 * @return a copy of the bus data for the given indexes.
	 */
	public DZjp_bus copy(int[] indexes) {
		DZjp_bus other = new DZjp_bus();

		other.bus_i = this.bus_i.viewSelection(indexes).copy();
		other.bus_type = this.bus_type.viewSelection(indexes).copy();
		other.Pd = this.Pd.viewSelection(indexes).copy();
		other.Qd = this.Qd.viewSelection(indexes).copy();
		other.Gs = this.Gs.viewSelection(indexes).copy();
		other.Bs = this.Bs.viewSelection(indexes).copy();
		other.bus_area = this.bus_area.viewSelection(indexes).copy();
		other.Vm = this.Vm.viewSelection(indexes).copy();
		other.Va = this.Va.viewSelection(indexes).copy();
		other.base_kV = this.base_kV.viewSelection(indexes).copy();
		other.zone = this.zone.viewSelection(indexes).copy();
		other.Vmax = this.Vmax.viewSelection(indexes).copy();
		other.Vmin = this.Vmin.viewSelection(indexes).copy();

		other.lam_P = this.lam_P.viewSelection(indexes).copy();
		other.lam_Q = this.lam_Q.viewSelection(indexes).copy();
		other.mu_Vmax = this.mu_Vmax.viewSelection(indexes).copy();
		other.mu_Vmin = this.mu_Vmin.viewSelection(indexes).copy();

		return other;
	}

	/**
	 * Updates the bus data for the given indices.
	 *
	 * @param other source bus data.
	 * @param indexes bus indices.
	 */
	public void update(DZjp_bus other, int[] indexes) {

		this.bus_i.viewSelection(indexes).assign(other.bus_i.viewSelection(indexes));
		this.bus_type.viewSelection(indexes).assign(other.bus_type.viewSelection(indexes));
		this.Pd.viewSelection(indexes).assign(other.Pd.viewSelection(indexes));
		this.Qd.viewSelection(indexes).assign(other.Qd.viewSelection(indexes));
		this.Gs.viewSelection(indexes).assign(other.Gs.viewSelection(indexes));
		this.Bs.viewSelection(indexes).assign(other.Bs.viewSelection(indexes));
		this.bus_area.viewSelection(indexes).assign(other.bus_area.viewSelection(indexes));
		this.Vm.viewSelection(indexes).assign(other.Vm.viewSelection(indexes));
		this.Va.viewSelection(indexes).assign(other.Va.viewSelection(indexes));
		this.base_kV.viewSelection(indexes).assign(other.base_kV.viewSelection(indexes));
		this.zone.viewSelection(indexes).assign(other.zone.viewSelection(indexes));
		this.Vmax.viewSelection(indexes).assign(other.Vmax.viewSelection(indexes));
		this.Vmin.viewSelection(indexes).assign(other.Vmin.viewSelection(indexes));

		this.lam_P.viewSelection(indexes).assign(other.lam_P.viewSelection(indexes));
		this.lam_Q.viewSelection(indexes).assign(other.lam_Q.viewSelection(indexes));
		this.mu_Vmax.viewSelection(indexes).assign(other.mu_Vmax.viewSelection(indexes));
		this.mu_Vmin.viewSelection(indexes).assign(other.mu_Vmin.viewSelection(indexes));
	}

	/**
	 *
	 * @param bus
	 */
	public void update(DoubleMatrix2D bus) {
		update(bus, null);
	}

	/**
	 *
	 * @param bus
	 * @param indexes
	 */
	public void update(DoubleMatrix2D bus, int[] indexes) {

		this.bus_i.viewSelection(indexes).assign( intm(bus.viewColumn(BUS_I).viewSelection(indexes)) );
		this.bus_type.viewSelection(indexes).assign( intm(bus.viewColumn(BUS_TYPE).viewSelection(indexes)) );
		this.Pd.viewSelection(indexes).assign(bus.viewColumn(PD).viewSelection(indexes));
		this.Qd.viewSelection(indexes).assign(bus.viewColumn(QD).viewSelection(indexes));
		this.Gs.viewSelection(indexes).assign(bus.viewColumn(GS).viewSelection(indexes));
		this.Bs.viewSelection(indexes).assign(bus.viewColumn(BS).viewSelection(indexes));
		this.bus_area.viewSelection(indexes).assign( intm(bus.viewColumn(BUS_AREA).viewSelection(indexes)) );
		this.Vm.viewSelection(indexes).assign(bus.viewColumn(VM).viewSelection(indexes));
		this.Va.viewSelection(indexes).assign(bus.viewColumn(VA).viewSelection(indexes));
		this.base_kV.viewSelection(indexes).assign(bus.viewColumn(BASE_KV).viewSelection(indexes));
		this.zone.viewSelection(indexes).assign( intm(bus.viewColumn(ZONE).viewSelection(indexes)) );
		this.Vmax.viewSelection(indexes).assign(bus.viewColumn(VMAX).viewSelection(indexes));
		this.Vmin.viewSelection(indexes).assign(bus.viewColumn(VMIN).viewSelection(indexes));

		if (bus.columns() > VMIN) {
			this.lam_P.viewSelection(indexes).assign(bus.viewColumn(LAM_P).viewSelection(indexes));
			this.lam_Q.viewSelection(indexes).assign(bus.viewColumn(LAM_Q).viewSelection(indexes));
			this.mu_Vmax.viewSelection(indexes).assign(bus.viewColumn(MU_VMAX).viewSelection(indexes));
			this.mu_Vmin.viewSelection(indexes).assign(bus.viewColumn(MU_VMIN).viewSelection(indexes));
		}
	}

	public DoubleMatrix2D toMatrix() {
		return toMatrix(true);
	}

	/**
	 *
	 * @param opf include opf data
	 * @return bus data matrix
	 */
	public DoubleMatrix2D toMatrix(boolean opf) {
		DoubleMatrix2D matrix;
		if (opf) {
			matrix = DoubleFactory2D.dense.make(size(), 17);
		} else {
			matrix = DoubleFactory2D.dense.make(size(), 13);
		}

		matrix.viewColumn(BUS_I).assign( dblm(this.bus_i) );
		matrix.viewColumn(BUS_TYPE).assign( dblm(this.bus_type) );
		matrix.viewColumn(PD).assign(this.Pd);
		matrix.viewColumn(QD).assign(this.Qd);
		matrix.viewColumn(GS).assign(this.Gs);
		matrix.viewColumn(BS).assign(this.Bs);
		matrix.viewColumn(BUS_AREA).assign( dblm(this.bus_area) );
		matrix.viewColumn(VM).assign(this.Vm);
		matrix.viewColumn(VA).assign(this.Va);
		matrix.viewColumn(BASE_KV).assign(this.base_kV);
		matrix.viewColumn(ZONE).assign( dblm(this.zone) );
		matrix.viewColumn(VMAX).assign(this.Vmax);
		matrix.viewColumn(VMIN).assign(this.Vmin);

		if (opf) {
			matrix.viewColumn(LAM_P).assign(this.lam_P);
			matrix.viewColumn(LAM_Q).assign(this.lam_Q);
			matrix.viewColumn(MU_VMAX).assign(this.mu_Vmax);
			matrix.viewColumn(MU_VMIN).assign(this.mu_Vmin);
		}

		return matrix;
	}

}
