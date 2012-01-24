package edu.cornell.pserc.jpower.jpc;

import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tint.IntMatrix1D;

public class DCLine {

	public static final int F_BUS	= 0;
	public static final int T_BUS	= 1;
	public static final int BR_STATUS	= 2;
	public static final int PF	= 3;
	public static final int PT	= 4;
	public static final int QF	= 5;
	public static final int QT	= 6;
	public static final int VF	= 7;
	public static final int VT	= 8;
	public static final int PMIN	= 9;
	public static final int PMAX	= 10;
	public static final int QMINF	= 11;
	public static final int QMAXF	= 12;
	public static final int QMINT	= 13;
	public static final int QMAXT	= 14;
	public static final int LOSS0	= 15;
	public static final int LOSS1	= 16;
	public static final int MU_PMIN	= 17;
	public static final int MU_PMAX	= 18;
	public static final int MU_QMINF	= 19;
	public static final int MU_QMAXF	= 20;
	public static final int MU_QMINT	= 21;
	public static final int MU_QMAXT	= 22;

	/** f, "from" bus number */
	public IntMatrix1D f_bus;

	/** t,  "to"  bus number */
	public IntMatrix1D t_bus;

	/** initial branch status, 1 - in service, 0 - out of service */
	public IntMatrix1D br_status;

	/** MW flow at "from" bus ("from" -> "to") */
	public DoubleMatrix1D Pf;

	/** MW flow at  "to"  bus ("from" -> "to") */
	public DoubleMatrix1D Pt;

	/** MVAr injection at "from" bus ("from" -> "to") */
	public DoubleMatrix1D Qf;

	/** MVAr injection at  "to"  bus ("from" -> "to") */
	public DoubleMatrix1D Qt;

	/** voltage setpoint at "from" bus (p.u.) */
	public DoubleMatrix1D Vf;

	/** voltage setpoint at  "to"  bus (p.u.) */
	public DoubleMatrix1D Vt;

	/** lower limit on PF (MW flow at "from" end) */
	public DoubleMatrix1D Pmin;

	/** upper limit on PF (MW flow at "from" end) */
	public DoubleMatrix1D Pmax;

	/** lower limit on MVAr injection at "from" bus */
	public DoubleMatrix1D Qminf;

	/** upper limit on MVAr injection at "from" bus */
	public DoubleMatrix1D Qmaxf;

	/** lower limit on MVAr injection at  "to"  bus */
	public DoubleMatrix1D Qmint;

	/** upper limit on MVAr injection at  "to"  bus */
	public DoubleMatrix1D Qmaxt;

	/** constant term of linear loss function (MW) */
	public DoubleMatrix1D loss0;

	/** linear term of linear loss function (MW) */
	public DoubleMatrix1D loss1;

	/** Kuhn-Tucker multiplier on lower flow lim at "from" bus (u/MW) */
	public DoubleMatrix1D mu_Pmin;

	/** Kuhn-Tucker multiplier on upper flow lim at "from" bus (u/MW) */
	public DoubleMatrix1D mu_Pmax;

	/** Kuhn-Tucker multiplier on lower VAr lim at "from" bus (u/MVAr) */
	public DoubleMatrix1D mu_Qminf;

	/** Kuhn-Tucker multiplier on upper VAr lim at "from" bus (u/MVAr) */
	public DoubleMatrix1D mu_Qmaxf;

	/** Kuhn-Tucker multiplier on lower VAr lim at  "to"  bus (u/MVAr) */
	public DoubleMatrix1D mu_Qmint;

	/** Kuhn-Tucker multiplier on upper VAr lim at  "to"  bus (u/MVAr) */
	public DoubleMatrix1D mu_Qmaxt;

	/**
	 *
	 * @return the number of buses.
	 */
	public int size() {
		return (int) this.f.size();
	}

	/**
	 *
	 * @return a full copy of the bus data.
	 */
	public DCLine copy() {
		return copy(null);
	}

	/**
	 *
	 * @param indexes
	 * @return a copy of the DC line data for the given indexes.
	 */
	public DCLine copy(int[] indexes) {
		return null;
	}

	public static DCLine fromMatrix(DoubleMatrix2D data) {
		return null;
	}

	public static DCLine fromMatrix(double[][] data) {
		return fromMatrix(DoubleFactory2D.dense.make(data));
	}

	public DoubleMatrix2D toMatrix() {
		return null;
	}

}
