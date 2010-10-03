/*
 * Copyright (C) 2009 Stijn Cole <stijn.cole@esat.kuleuven.be>
 * Copyright (C) 2010 Richard Lincoln <r.w.lincoln@gmail.com>
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

package be.kuleuven.esat.electa.jdyn.tdouble;

import be.kuleuven.esat.electa.jdyn.tdouble.jdc.Djd_gen;
import cern.colt.matrix.tdcomplex.DComplexMatrix1D;
import cern.colt.matrix.tdouble.DoubleFactory1D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tint.IntFactory1D;
import cern.colt.matrix.tint.IntMatrix1D;
import cern.colt.util.tdouble.Djp_util;
import cern.jet.math.tdcomplex.DComplexFunctions;
import cern.jet.math.tdouble.DoubleFunctions;
import cern.jet.math.tint.IntFunctions;

/**
 * Calculates currents and electric power of generators
 *
 * @author Stijn Cole (stijn.cole@esat.kuleuven.be)
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class Djd_MachineCurrents {

	private static final Djp_util util = new Djp_util();
	private static final IntFunctions ifunc = IntFunctions.intFunctions;
	private static final DoubleFunctions dfunc = DoubleFunctions.functions;
	private static final DComplexFunctions cfunc = DComplexFunctions.functions;

	/**
	 *
	 * @param xgen0 state variables of generators
	 * @param pgen0 parameters of generators
	 * @param U generator voltages
	 * @param gentype generator models
	 * @return [Id = d-axis stator current,
	 * Iq = q-axis stator current,
	 * Pe = generator electric power]
	 */
	@SuppressWarnings("static-access")
	public static DoubleMatrix1D[] jd_MachineCurrents(Djd_gen xgen0, Djd_gen pgen0,
			DComplexMatrix1D U, IntMatrix1D gentype) {

		/* Init */
		int ngen = xgen0.rows();
		DoubleMatrix1D Id = DoubleFactory1D.dense.make(ngen);
		DoubleMatrix1D Iq = DoubleFactory1D.dense.make(ngen);
		DoubleMatrix1D Pe = DoubleFactory1D.dense.make(ngen);
		IntMatrix1D d = IntFactory1D.dense.make(util.irange((int) gentype.size()));

		/* Define types */
		int[] type1 = d.viewSelection( gentype.copy().assign(ifunc.equals(1)).toArray() ).toArray();
		int[] type2 = d.viewSelection( gentype.copy().assign(ifunc.equals(2)).toArray() ).toArray();

		DoubleMatrix1D delta, Eq_tr, Ed_tr;

		/* Generator type 1: classical model */
		delta = xgen0.viewColumn(0).viewSelection(type1).copy();
		Eq_tr = xgen0.viewColumn(2).viewSelection(type1).copy();

		DoubleMatrix1D xd = pgen0.D.viewSelection(type1).copy();

		DoubleMatrix1D Um = U.copy().assign(cfunc.abs).getRealPart();
		DoubleMatrix1D Ua = U.copy().assign(cfunc.arg).getRealPart();

		Pe.viewSelection(type1).assign( xd.assign(Um.viewSelection(type1), dfunc.mult) );
		Pe.viewSelection(type1).assign( Eq_tr.assign(dfunc.abs), dfunc.mult );
		Pe.viewSelection(type1).assign( delta.assign(Ua.viewSelection(type1), dfunc.minus).assign(dfunc.sin), dfunc.mult );
		Pe.viewSelection(type1).assign( dfunc.inv );

		/* Generator type 2: 4th order model */
		delta = xgen0.viewColumn(0).viewSelection(type2).copy();
		Eq_tr = xgen0.viewColumn(2).viewSelection(type2).copy();
		Ed_tr = xgen0.viewColumn(3).viewSelection(type2).copy();

		DoubleMatrix1D xd_tr = pgen0.xd_tr.viewSelection(type2).copy();
		DoubleMatrix1D xq_tr = pgen0.xq_tr.viewSelection(type2).copy();

		/* Tranform U to rotor frame of reference */
		DoubleMatrix1D vd = Um.viewSelection(type2).copy().assign(dfunc.neg);
		vd.assign(delta.copy().assign(Ua.viewSelection(type2), dfunc.minus).assign(dfunc.sin), dfunc.mult);
		DoubleMatrix1D vq = Um.viewSelection(type2).copy();
		vq.assign(delta.copy().assign(Ua.viewSelection(type2), dfunc.minus).assign(dfunc.cos), dfunc.mult);

		Id.viewSelection(type2).assign( vq.assign(Eq_tr, dfunc.minus).assign(xd_tr, dfunc.div) );
		Iq.viewSelection(type2).assign( vd.assign(Ed_tr, dfunc.minus).assign(xq_tr, dfunc.div) );

		Pe.viewSelection(type2).assign( Eq_tr.assign(Iq.viewSelection(type2), dfunc.mult) );
		Pe.viewSelection(type2).assign( Eq_tr.assign(Iq.viewSelection(type2), dfunc.mult), dfunc.plus );
		Pe.viewSelection(type2).assign( xd_tr.assign(xq_tr, dfunc.minus).assign(Id.viewSelection(type2), dfunc.mult).assign(Iq.viewSelection(type2), dfunc.mult), dfunc.plus );

		return new DoubleMatrix1D[] {Id, Iq, Pe};
	}
}
