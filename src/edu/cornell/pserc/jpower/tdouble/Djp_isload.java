package edu.cornell.pserc.jpower.tdouble;

import cern.colt.matrix.tint.IntMatrix1D;
import cern.colt.util.tdouble.Djp_util;
import cern.jet.math.tdouble.DoubleFunctions;
import cern.jet.math.tint.IntFunctions;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_gen;

public class Djp_isload {

	private static final DoubleFunctions dfunc = DoubleFunctions.functions;
	private static final IntFunctions ifunc = IntFunctions.intFunctions;

	private static IntMatrix1D lt, eq;

	@SuppressWarnings("static-access")
	public static IntMatrix1D jp_isload(Djp_gen gen) {

		lt = Djp_util.intm(gen.Pmin.copy().assign( dfunc.less(0) ));
		eq = Djp_util.intm(gen.Pmax.copy().assign( dfunc.equals(0) ));

		return lt.assign(eq, ifunc.and);
	}

}
