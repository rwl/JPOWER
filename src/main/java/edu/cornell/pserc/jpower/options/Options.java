package edu.cornell.pserc.jpower.options;

public class Options {

	/* power flow options */

	public PowerFlowAlgorithm pf_alg = PowerFlowAlgorithm.NEWTONS_METHOD;

	/**
	 * Termination tolerance on per unit P & Q mismatch.
	 */
	public double pf_tol = 1e-8;

	/**
	 * Maximum number of iterations for Newton's method.
	 */
	public int pf_max_it = 10;

	/**
	 * Maximum number of iterations for fast decoupled method.
	 */
	public int pf_max_it_fd = 30;

	/**
	 * Maximum number of iterations for Gauss-Seidel method.
	 */
	public int pf_max_it_gs = 1000;

	/**
	 * Enforce gen reactive power limits at expense of |V|.
	 *
	 * 0 - do not enforce limits
	 * 1 - enforce limits, simultaneous bus type conversion
	 * 2 - enforce limits, one-at-a-time bus type conversion
	 */
	public int enforce_q_lims = 0;

	/**
	 * DC modeling for power flow & OPF
	 *
	 * false - use AC formulation & corresponding algorithm options
	 * true - use DC formulation, ignore AC algorithm options
	 */
	public boolean pf_dc = false;

	/* OPF options */

	/**
	 * Solver to use for AC OPF.
	 */
	public OPFAlgorithm opf_alg = OPFAlgorithm.DEFAULT;

	/**
	 * Number of eq constraints, set by program (not a user option).
	 */
	public int n_eq = 0;

	/**
	 * Constraint violation tolerance.
	 */
	public double opf_violation = 5e-06;

	/**
	 * Solver to use for DC OPF.
	 */
	public OPFAlgorithm opf_alg_dc = OPFAlgorithm.DEFAULT;

	/* Output options */

	/**
	 * Amount of progress info printed.
	 */
	public Verbosity verbose = Verbosity.LITTLE;

	/**
	 * Print system summary.
	 */
	public OutputAll out_all = OutputAll.DELEGATE;

	/**
	 * Print system summary.
	 */
	public boolean out_sys_sum = true;

	/**
	 * Print area summary.
	 */
	public boolean out_area_sum = false;

	/**
	 * Print bus detail.
	 */
	public boolean out_bus = true;

	/**
	 * Print branch detail.
	 */
	public boolean out_branch = true;

	/**
	 * Print generator detail (out_bus also includes gen info).
	 */
	public boolean out_gen = false;

	/**
	 * Controls what constraint info is printed:
	 *
	 * -1 - individual flags control what constraint info prints
	 *  0 - no constraint info (overrides individual flags)
	 *  1 - binding constraint info (overrides individual flags)
	 *  2 - all constraint info (overrides individual flags)
	 */
	public int out_all_lim = -1;

	/**
	 * Control output of voltage limit info.
	 */
	public OutputConstraints out_v_lim = OutputConstraints.BINDING;

	/**
	 * Control output of line flow limit info.
	 */
	public OutputConstraints out_line_lim = OutputConstraints.BINDING;

	/**
	 * Control output of gen P limit info.
	 */
	public OutputConstraints out_pg_lim = OutputConstraints.BINDING;

	/**
	 * Control output of gen Q limit info.
	 */
	public OutputConstraints out_qg_lim = OutputConstraints.BINDING;

}
