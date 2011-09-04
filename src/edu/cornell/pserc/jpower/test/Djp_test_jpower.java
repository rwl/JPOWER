package edu.cornell.pserc.jpower.test;

import java.util.ArrayList;
import java.util.List;

import static edu.cornell.pserc.jpower.test.Djp_t_run_tests.t_run_tests;

public class Djp_test_jpower {

	public static void test_jpower() {
		test_jpower(false);
	}

	public static void test_jpower(boolean verbose) {
		List<String> tests = new ArrayList<String>();

		tests.add("t_loadcase");
		tests.add("t_ext2int2ext");
		tests.add("t_jacobian");
		tests.add("t_pf");

		t_run_tests(tests, verbose);
	}

	public static void main(String[] args) {
		test_jpower(true);
	}

}
