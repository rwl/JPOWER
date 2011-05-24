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

import edu.cornell.pserc.jpower.tdouble.jpc.Djp_jpc;

/**
 *
 * @author Richard Lincoln
 *
 */
public abstract class Djp_ext2int_test extends Djp_base_test {

	private Djp_jpc jpc;

	public Djp_ext2int_test(String name, String caseName, Djp_jpc jpc) {
		super(name, caseName, "ext2int");
		this.jpc = jpc;
	}

	public void test_ext2int__jpc() {
		Djp_jpc jpc;

		jpc = Djp_loadcase.jp_loadcase(this.jpc);
		test_jpc(Djp_ext2int.jp_ext2int(jpc));
	}

	// TODO: test overloaded methods.

}
