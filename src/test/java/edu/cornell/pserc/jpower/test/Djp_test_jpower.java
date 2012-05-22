/*
 * Copyright (C) 1996-2010 Power System Engineering Research Center
 * Copyright (C) 2010-2011 Richard Lincoln
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package edu.cornell.pserc.jpower.test;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import static edu.cornell.pserc.jpower.test.Djp_t_run_tests.t_run_tests;


public class Djp_test_jpower extends TestCase {

	public void test_jpower() {
		List<String> tests = new ArrayList<String>();

		tests.add("t_loadcase");
		tests.add("t_ext2int2ext");
		tests.add("t_jacobian");
		tests.add("t_pf");

		t_run_tests(tests, false);

		assertEquals(0, TestGlobals.t_not_ok_cnt);
	}

}
