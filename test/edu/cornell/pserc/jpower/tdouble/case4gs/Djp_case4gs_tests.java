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

package edu.cornell.pserc.jpower.tdouble.case4gs;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class Djp_case4gs_tests extends TestSuite {

	public static void main(String[] args) {
		TestRunner.run(suite());
	}

	public Djp_case4gs_tests(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new Djp_case4gs_tests("case4gs Tests");
		suite.addTestSuite(Djp_bustypes_test.class);
		suite.addTestSuite(Djp_dcpf_test.class);
		suite.addTestSuite(Djp_ext2int_test.class);
		suite.addTestSuite(Djp_int2ext_test.class);
		suite.addTestSuite(Djp_loadcase_test.class);
		suite.addTestSuite(Djp_makeBdc_test.class);
		suite.addTestSuite(Djp_makeSbus_test.class);
		suite.addTestSuite(Djp_rundcpf_test.class);
		return suite;
	}

}
