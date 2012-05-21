package edu.cornell.pserc.jpower.options;

public enum PowerFlowAlgorithm {

	/**
	 * Newton's method
	 */
	NEWTONS_METHOD,

	/**
	 * Fast-Decoupled (XB version)
	 */
	FAST_DECOUPLED_XB,

	/**
	 * Fast-Decoupled (BX version)
	 */
	FAST_DECOUPLED_BX,

	/**
	 * Gauss-Seidel method
	 */
	GAUSS_SEIDEL;

}
