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
public abstract class Djp_loadcase_test extends Djp_base_test {

	private Djp_jpc jpc;

	public Djp_loadcase_test(String name, String caseName, Djp_jpc jpc) {
		super(name, caseName, "loadcase");
		this.jpc = jpc;
	}

	public void test_loadcase__string() {
//		test_jpc(Djp_loadcase.jp_loadcase(casename));  TODO Load case from file
	}

	public void test_loadcase__jpc() {
		test_jpc(Djp_loadcase.loadcase(this.jpc));
	}

}
