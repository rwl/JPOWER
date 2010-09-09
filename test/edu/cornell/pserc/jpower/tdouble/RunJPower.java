package edu.cornell.pserc.jpower.tdouble;

import edu.cornell.pserc.jpower.tdouble.cases.Djp_case9;
import edu.cornell.pserc.jpower.tdouble.jpc.Djp_jpc;

public class RunJPower {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Djp_jpc jpc = Djp_case9.jp_case9();
		Djp_rundcpf.jp_rundcpf(jpc);

	}

}
