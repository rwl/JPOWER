/*
 * Copyright (C) 2009 Stijn Cole
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

package be.kuleuven.esat.electa.jdyn.tdouble;

import cern.colt.matrix.tdcomplex.DComplexFactory1D;
import cern.colt.matrix.tdcomplex.DComplexMatrix1D;
import cern.colt.matrix.tdcomplex.DComplexMatrix2D;
//import cern.colt.matrix.tdcomplex.algo.SparseDComplexAlgebra;
//import cern.colt.matrix.tdcomplex.algo.decomposition.SparseDComplexLUDecomposition;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.util.tdouble.Djp_util;
import cern.jet.math.tdcomplex.DComplexFunctions;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_branch;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_bus;
import edu.cornell.pserc.jpower.tdouble.pf.Djp_makeYbus;

/**
 * Constructs augmented bus admittance matrix Ybus
 *
 * @author Stijn Cole
 * @author Richard Lincoln
 *
 */
public class Djd_AugYbus {

	private static final DComplexFunctions cfunc = DComplexFunctions.functions;

	private static DComplexMatrix1D yload, ygen;
	private static DComplexMatrix2D Ybus;
	private static DComplexMatrix2D[] Y;

	/**
	 *
	 * @param baseMVA power base
	 * @param bus bus data
	 * @param branch branch data
	 * @param xd_tr d component of transient reactance
	 * @param gbus generator buses
	 * @param P load active power
	 * @param Q load reactive power
	 * @param U0 steady-state bus voltages
	 * @return factorised augmented bus admittance matrix
	 */
//	@SuppressWarnings("static-access")
//	public static SparseDComplexLUDecomposition jd_AugYbus(double baseMVA, Djp_bus bus, Djp_branch branch,
//			DoubleMatrix1D xd_tr, int[] gbus, DoubleMatrix1D P, DoubleMatrix1D Q, DComplexMatrix1D U0) {
//
//		/* Calculate bus admittance matrix */
//		Y = Djp_makeYbus.jp_makeYbus(baseMVA, bus, branch);
//		Ybus = Y[0];
//
//		/* Calculate equivalent load admittance */
//		yload = Djp_util.complex(P, Q).assign(cfunc.conj);
//		yload.assign(U0.copy().assign(cfunc.abs).assign(cfunc.square), cfunc.div);
//
//		/* Calculate equivalent generator admittance */
//		ygen = DComplexFactory1D.dense.make(Ybus.rows());
//		ygen.viewSelection(gbus).assign( Djp_util.complex(null, xd_tr).assign(cfunc.inv) );
//
//		/* Add equivalent load and generator admittance to Ybus matrix */
//		for (int i = 0; i < Ybus.rows(); i++)
//			Ybus.set(i, i, cfunc.plus.apply( cfunc.plus.apply(Ybus.get(i, i), ygen.get(i)), yload.get(i) ));
//
//		return SparseDComplexAlgebra.DEFAULT.lu(Ybus, 0);
//	}

}
