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

import edu.cornell.pserc.jpower.tdouble.jpc.JPC;

/**
 *
 * @author Richard Lincoln
 *
 */
public abstract class Djp_int2ext_test extends Djp_base_test {

	private JPC jpc;

	public Djp_int2ext_test(String name, String caseName, JPC jpc) {
		super(name, caseName, "int2ext");
		this.jpc = jpc;
	}

	public void test_int2ext__jpc() {
		JPC jpc;

		jpc = Djp_loadcase.loadcase(this.jpc);
		jpc = Djp_ext2int.ext2int(jpc);

		test_jpc(Djp_int2ext.int2ext(jpc));
	}

	// TODO: test overloaded methods.

}
