/*
 * Copyright (C) 2010 Richard Lincoln
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 *
 */

package edu.cornell.pserc.jpower.tdouble;

import edu.cornell.pserc.jpower.tdouble.case4gs.Djp_case4gs_tests;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class Djp_all_tests extends TestSuite {

	public static void main(String[] args) {
		TestRunner.run(suite());
	}

	public Djp_all_tests(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new Djp_all_tests("Djp tests");
		suite.addTest(Djp_case4gs_tests.suite());
		return suite;
	}

}
