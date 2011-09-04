package edu.cornell.pserc.jpower.tdouble;

import cern.colt.matrix.tint.IntMatrix1D;

import static cern.colt.util.tdouble.Djp_util.ifunc;
import static cern.colt.util.tdouble.Djp_util.dfunc;
import static cern.colt.util.tdouble.Djp_util.intm;

import edu.cornell.pserc.jpower.tdouble.jpc.Djp_gen;

public class Djp_isload {

	@SuppressWarnings("static-access")
	public static IntMatrix1D isload(Djp_gen gen) {
		IntMatrix1D lt, eq;

		lt = intm(gen.Pmin.copy().assign( dfunc.less(0) ));
		eq = intm(gen.Pmax.copy().assign( dfunc.equals(0) ));

		return lt.assign(eq, ifunc.and);
	}

}
