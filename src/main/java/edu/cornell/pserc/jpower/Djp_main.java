package edu.cornell.pserc.jpower;

import java.util.Map;

import edu.cornell.pserc.jpower.cases.Djp_case4gs;
import edu.cornell.pserc.jpower.cases.Djp_case57;
import edu.cornell.pserc.jpower.jpc.JPC;
import edu.cornell.pserc.jpower.pf.Djp_rundcpf;
import edu.cornell.pserc.jpower.pf.Djp_runpf;

public class Djp_main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		JPC jpc;
		Map<String, Double> jpopt;

		jpopt = Djp_jpoption.jpoption("VERBOSE", 2.0);

		jpc = Djp_case4gs.case4gs();
		Djp_rundcpf.rundcpf(jpc, jpopt);

		jpopt = Djp_jpoption.jpoption(jpopt, "PF_DC", 0.0, "PF_ALG", 1.0);

		jpc = Djp_case57.case57();
		Djp_runpf.runpf(jpc, jpopt);
	}

}
