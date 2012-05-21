package edu.cornell.pserc.jpower.options;

/**
 * Solver to use for OPF.
 */
public enum OPFAlgorithm {

	/**
	 * Choose default solver based on availability.
	 */
	DEFAULT,

	/**
	 * Interior Point OPTimizer
	 */
	IPOPT;
}
