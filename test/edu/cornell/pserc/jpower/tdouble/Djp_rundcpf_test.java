/*
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

package edu.cornell.pserc.jpower.tdouble;

import edu.cornell.pserc.jpower.jpc.JPC;
import edu.cornell.pserc.jpower.pf.Djp_rundcpf;

/**
 *
 * @author Richard Lincoln
 *
 */
public abstract class Djp_rundcpf_test extends Djp_base_test {

	protected JPC jpc;

	public Djp_rundcpf_test(String name, String caseName, JPC jpc) {
		super(name, caseName, "rundcpf");
		this.jpc = jpc;
	}

//	public void test_rundcpf__string() {
//		test_jpc(Djp_rundcpf.jp_rundcpf(casename));  FIXME Run PF from file path
//	}

	public void test_rundcpf__jpc() {
		test_jpc(Djp_rundcpf.rundcpf(this.jpc), true);
	}

}
