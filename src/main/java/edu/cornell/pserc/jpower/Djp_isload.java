package edu.cornell.pserc.jpower;

import cern.colt.matrix.tint.IntMatrix1D;

import static edu.emory.mathcs.utils.Utils.ifunc;
import static edu.emory.mathcs.utils.Utils.dfunc;
import static edu.emory.mathcs.utils.Utils.intm;

import edu.cornell.pserc.jpower.jpc.Gen;

public class Djp_isload {

	@SuppressWarnings("static-access")
	public static IntMatrix1D isload(Gen gen) {
		IntMatrix1D lt, eq;

		lt = intm(gen.Pmin.copy().assign( dfunc.less(0) ));
		eq = intm(gen.Pmax.copy().assign( dfunc.equals(0) ));

		return lt.assign(eq, ifunc.and);
	}

}
