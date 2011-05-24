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

import edu.cornell.pserc.jpower.tdouble.case4gs.Djp_case4gs_tests;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class Djp_all_tests extends TestSuite {

	private static TestSuite suite;

	public static void main(String[] args) {
		TestRunner.run(suite());
	}

	public Djp_all_tests(String name) {
		super(name);
	}

	public static Test suite() {
		suite = new Djp_all_tests("Djp tests");
		suite.addTest(Djp_case4gs_tests.suite());

		return suite;
	}

}
