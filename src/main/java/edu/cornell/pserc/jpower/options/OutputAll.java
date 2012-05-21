package edu.cornell.pserc.jpower.options;

/**
 * Control pretty-printing of results.
 */
public enum OutputAll {

	/**
	 * Individual flags control what prints.
	 */
	DELEGATE,

	/**
	 * Do not print anything (overrides individual flags).
	 */
	NONE,

	/**
	 * Print everything (overrides individual flags).
	 */
	EVERYTHING;

}
