package edu.cornell.pserc.jpower;

import edu.cornell.pserc.jpower.jpc.JPC;

import static edu.cornell.pserc.jpower.cases.Djp_case4gs.jp_case4gs;
import static edu.cornell.pserc.jpower.pf.Djp_rundcpf.rundcpf;

public class Djp_main {

	public static void main(String[] args) {
		JPC jpc = jp_case4gs();
		rundcpf(jpc);
	}

}
