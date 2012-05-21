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

package edu.cornell.pserc.jpower;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * The currently defined options are as follows:
 *
 * 	no. - NAME, default          description [options]
 * 	---   -------------          -----------------------------------------
 * 	power flow options
 * 	1  - PF_ALG, 1              AC power flow algorithm
 * 			[   1 - Newton's method                                         ]
 * 			[   2 - Fast-Decoupled (XB version)                             ]
 * 			[   3 - Fast-Decoupled (BX version)                             ]
 * 			[   4 - Gauss-Seidel                                            ]
 * 	2  - PF_TOL, 1e-8           termination tolerance on per unit
 * 								P & Q mismatch
 * 	3  - PF_MAX_IT, 10          maximum number of iterations for
 * 								Newton's method
 * 	4  - PF_MAX_IT_FD, 30       maximum number of iterations for
 * 								fast decoupled method
 * 	5  - PF_MAX_IT_GS, 1000     maximum number of iterations for
 * 								Gauss-Seidel method
 * 	6  - ENFORCE_Q_LIMS, 0      enforce gen reactive power limits
 * 								at expense of |V|
 * 			[    0 - do NOT enforce limits                                  ]
 * 			[    1 - enforce limits, simultaneous bus type conversion       ]
 * 			[    2 - enforce limits, one-at-a-time bus type conversion      ]
 * 	10 - PF_DC, 0               DC modeling for power flow & OPF
 * 			[    0 - use AC formulation & corresponding algorithm options   ]
 * 			[    1 - use DC formulation, ignore AC algorithm options        ]
 * 	OPF options
 * 	11 - OPF_ALG, 0             solver to use for AC OPF
 * 			[    0 - choose default solver based on availability in the     ]
 * 			[        following order, 540, 560                              ]
 * 			[  300 - constr, Matlab Optimization Toolbox 1.x and 2.x        ]
 * 			[  320 - dense successive LP                                    ]
 * 			[  340 - sparse successive LP (relaxed)                         ]
 * 			[  360 - sparse successive LP (full)                            ]
 * 			[  500 - MINOPF, MINOS-based solver, requires optional          ]
 * 			[        MEX-based MINOPF package, available from:              ]
 * 			[        http://www.pserc.cornell.edu/minopf/                   ]
 * 			[  520 - fmincon, Matlab Optimization Toolbox >= 2.x            ]
 * 			[  540 - PDIPM, primal/dual interior point method, requires     ]
 * 			[        optional MEX-based TSPOPF package, available from:     ]
 * 			[        http://www.pserc.cornell.edu/tspopf/                   ]
 * 			[  545 - SC-PDIPM, step-controlled variant of PDIPM, requires   ]
 * 			[        TSPOPF (see 540)                                       ]
 * 			[  550 - TRALM, trust region based augmented Langrangian        ]
 * 			[        method, requires TSPOPF (see 540)                      ]
 * 			[  560 - MIPS, Matlab Interior Point Solver                     ]
 * 			[        primal/dual interior point method (pure Matlab)        ]
 * 			[  565 - MIPS-sc, step-controlled variant of MIPS               ]
 * 	16 - OPF_VIOLATION, 5e-6    constraint violation tolerance
 * 	17 - CONSTR_TOL_X, 1e-4     termination tol on x for constr/fmincon
 * 	18 - CONSTR_TOL_F, 1e-4     termination tol on f for constr/fmincon
 * 	19 - CONSTR_MAX_IT, 0       max number of iterations for constr/fmincon
 * 									[       0 => 2*nb + 150                 ]
 * 	20 - LPC_TOL_GRAD, 3e-3     termination tolerance on gradient, LP-based
 * 								solver
 * 	21 - LPC_TOL_X, 1e-4        termination tolerance on x (min step size),
 * 								LP-based solver
 * 	22 - LPC_MAX_IT, 400        maximum number of iterations, LP-based slvr
 * 	23 - LPC_MAX_RESTART, 5     maximum number of restarts, LP-based solver
 * 	24 - OPF_FLOW_LIM, 0        qty to limit for branch flow constraints
 * 			[   0 - apparent power flow (limit in MVA)                      ]
 * 			[   1 - active power flow (limit in MW)                         ]
 * 			[   2 - current magnitude (limit in MVA at 1 p.u. voltage)      ]
 * 	25 - OPF_IGNORE_ANG_LIM, 0  ignore angle difference limits for branches
 * 									even if specified           [   0 or 1  ]
 * 	26 - OPF_ALG_DC, 0          solver to use for DC OPF
 * 			[    0 - choose default solver based on availability in the     ]
 * 			[        following order, 100, 200                              ]
 * 			[  100 - BPMPD, requires optional MEX-based BPMPD_MEX package   ]
 * 			[        available from: http://www.pserc.cornell.edu/bpmpd/    ]
 * 			[  200 - MIPS, Matlab Interior Point Solver                     ]
 * 			[        primal/dual interior point method (pure Matlab)        ]
 * 			[  250 - MIPS-sc, step-controlled variant of MIPS               ]
 * 			[  300 - Matlab Optimization Toolbox, QUADPROG, LINPROG         ]
 * 	output options
 * 	31 - VERBOSE, 1             amount of progress info printed
 * 			[   0 - print no progress info                                  ]
 * 			[   1 - print a little progress info                            ]
 * 			[   2 - print a lot of progress info                            ]
 * 			[   3 - print all progress info                                 ]
 * 	32 - OUT_ALL, -1            controls pretty-printing of results
 * 			[  -1 - individual flags control what prints                    ]
 * 			[   0 - do not print anything                                   ]
 * 			[       (overrides individual flags, except OUT_RAW)            ]
 * 			[   1 - print everything                                        ]
 * 			[       (overrides individual flags, except OUT_RAW)            ]
 * 	33 - OUT_SYS_SUM, 1         print system summary        [   0 or 1  ]
 * 	34 - OUT_AREA_SUM, 0        print area summaries        [   0 or 1  ]
 * 	35 - OUT_BUS, 1             print bus detail            [   0 or 1  ]
 * 	36 - OUT_BRANCH, 1          print branch detail         [   0 or 1  ]
 * 	37 - OUT_GEN, 0             print generator detail      [   0 or 1  ]
 * 									(OUT_BUS also includes gen info)
 * 	38 - OUT_ALL_LIM, -1        controls what constraint info is printed
 * 			[  -1 - individual flags control what constraint info prints    ]
 * 			[   0 - no constraint info (overrides individual flags)         ]
 * 			[   1 - binding constraint info (overrides individual flags)    ]
 * 			[   2 - all constraint info (overrides individual flags)        ]
 * 	39 - OUT_V_LIM, 1           control output of voltage limit info
 * 			[   0 - do not print                                            ]
 * 			[   1 - print binding constraints only                          ]
 * 			[   2 - print all constraints                                   ]
 * 			[   (same options for OUT_LINE_LIM, OUT_PG_LIM, OUT_QG_LIM)     ]
 * 	40 - OUT_LINE_LIM, 1        control output of line flow limit info
 * 	41 - OUT_PG_LIM, 1          control output of gen P limit info
 * 	42 - OUT_QG_LIM, 1          control output of gen Q limit info
 *
 * 	MIPS (including MIPS-sc), PDIPM, SC-PDIPM, and TRALM options
 * 	81 - PDIPM_FEASTOL, 0       feasibility (equality) tolerance
 * 									for MIPS, PDIPM and SC-PDIPM, set
 * 									to value of OPF_VIOLATION by default
 * 	82 - PDIPM_GRADTOL, 1e-6    gradient tolerance for MIPS, PDIPM
 * 									and SC-PDIPM
 * 	83 - PDIPM_COMPTOL, 1e-6    complementary condition (inequality)
 * 									tolerance for MIPS, PDIPM and SC-PDIPM
 * 	84 - PDIPM_COSTTOL, 1e-6    optimality tolerance for MIPS, PDIPM
 * 									and SC-PDIPM
 * 	85 - PDIPM_MAX_IT,  150     maximum number of iterations for MIPS,
 * 									PDIPM and SC-PDIPM
 * 	86 - SCPDIPM_RED_IT, 20     maximum number of MIPS-sc or SC-PDIPM
 * 								reductions per iteration
 * 	87 - TRALM_FEASTOL, 0       feasibility tolerance for TRALM
 * 								set to value of OPF_VIOLATION by default
 * 	88 - TRALM_PRIMETOL, 5e-4   primal variable tolerance for TRALM
 * 	89 - TRALM_DUALTOL, 5e-4    dual variable tolerance for TRALM
 * 	90 - TRALM_COSTTOL, 1e-5    optimality tolerance for TRALM
 * 	91 - TRALM_MAJOR_IT, 40     maximum number of major iterations
 * 	92 - TRALM_MINOR_IT, 100    maximum number of minor iterations
 * 	93 - SMOOTHING_RATIO, 0.04  piecewise linear curve smoothing ratio
 * 								used in SC-PDIPM and TRALM
 *
 * 	deprecated options
 * 	43 - OUT_RAW, 0             print raw data for Perl database
 * 								interface code              [   0 or 1  ]
 * 	51 - SPARSE_QP, 1           pass sparse matrices to QP and LP
 * 								solvers if possible         [   0 or 1  ]
 *
 * @author Ray Zimmerman
 * @author Richard Lincoln
 *
 */
public class Djp_jpoption {

	/**
	 *
	 * @param tuples odd elements option names, even elements are new option values
	 * @return the default options vector with new values for the given options
	 */
	public static Map<String, Double> jpoption(String name1, Double value1, String name2, Double value2, String name3, Double value3, String name4, Double value4, String name5, Double value5, String name6, Double value6, String name7, Double value7) {
		return jpoption(null, name1, value1, name2, value2, name3, value3, name4, value4, name5, value5, name6, value6, name7, value7);
	}

	public static Map<String, Double> jpoption(String name1, Double value1, String name2, Double value2, String name3, Double value3, String name4, Double value4, String name5, Double value5, String name6, Double value6) {
		return jpoption(null, name1, value1, name2, value2, name3, value3, name4, value4, name5, value5, name6, value6, null, null);
	}

	public static Map<String, Double> jpoption(String name1, Double value1, String name2, Double value2, String name3, Double value3, String name4, Double value4, String name5, Double value5) {
		return jpoption(null, name1, value1, name2, value2, name3, value3, name4, value4, name5, value5, null, null, null, null);
	}

	public static Map<String, Double> jpoption(String name1, Double value1, String name2, Double value2, String name3, Double value3, String name4, Double value4) {
		return jpoption(null, name1, value1, name2, value2, name3, value3, name4, value4, null, null, null, null, null, null);
	}

	public static Map<String, Double> jpoption(String name1, Double value1, String name2, Double value2, String name3, Double value3) {
		return jpoption(null, name1, value1, name2, value2, name3, value3, null, null, null, null, null, null, null, null);
	}

	public static Map<String, Double> jpoption(String name1, Double value1, String name2, Double value2) {
		return jpoption(null, name1, value1, name2, value2, null, null, null, null, null, null, null, null, null, null);
	}

	public static Map<String, Double> jpoption(String name1, Double value1) {
		return jpoption(null, name1, value1, null, null, null, null, null, null, null, null, null, null, null, null);
	}

	/**
	 *
	 * @return the default options vector.
	 */
	public static Map<String, Double> jpoption() {
		return jpoption(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
	}

	/**
	 * Same as above except it uses the options vector OPT as a base
	 * instead of the default options vector.
	 *
	 * @param options a base set of options
	 * @param specified odd elements option names, even elements are new option values
	 * @return the base options map with new values for the given options
	 */
	public static Map<String, Double> jpoption(Map<String, Double> options, String name1, Double value1, String name2, Double value2, String name3, Double value3, String name4, Double value4, String name5, Double value5, String name6, Double value6, String name7, Double value7) {
		if (options == null) {

			/* use defaults for base options vector */
			options = new HashMap<String, Double>();

			// power flow options
			options.put("PF_ALG", 1.0);
			options.put("PF_TOL", 1e-8);
			options.put("PF_MAX_IT", 10.0);
			options.put("PF_MAX_IT_FD", 30.0);
			options.put("PF_MAX_IT_GS", 1000.0);
			options.put("ENFORCE_Q_LIMS", 0.0);
			options.put("PF_DC", 0.0);

			// OPF options
			options.put("OPF_ALG", 0.0);
			options.put("OPF_ALG_POLY", 100.0);		// deprecated
			options.put("OPF_ALG_PWL", 200.0);		// deprecated
			options.put("OPF_POLY2PWL_PTS", 10.0);	// deprecated
			options.put("OPF_NEQ", 0.0);			// not a user option (number of eq constraints, set by program)
			options.put("OPF_VIOLATION", 5e-6);
			options.put("CONSTR_TOL_X", 1e-4);
			options.put("CONSTR_TOL_F", 1e-4);
			options.put("CONSTR_MAX_IT", 0.0);
			options.put("LPC_TOL_GRAD", 3e-3);
			options.put("LPC_TOL_X", 1e-4);
			options.put("LPC_MAX_IT", 400.0);
			options.put("LPC_MAX_RESTART", 5.0);
			options.put("OPF_FLOW_LIM", 0.0);
			options.put("OPF_IGNORE_ANG_LIM", 0.0);
			options.put("OPF_ALG_DC", 0.0);

			// output options
			options.put("VERBOSE", 1.0);
			options.put("OUT_ALL", 0.0);
			options.put("OUT_SYS_SUM", 1.0);
			options.put("OUT_AREA_SUM", 0.0);
			options.put("OUT_BUS", 1.0);
			options.put("OUT_BRANCH", 1.0);
			options.put("OUT_GEN", 0.0);
			options.put("OUT_ALL_LIM", -1.0);
			options.put("OUT_V_LIM", 1.0);
			options.put("OUT_LINE_LIM", 1.0);
			options.put("OUT_PG_LIM", 1.0);
			options.put("OUT_QG_LIM", 1.0);
			options.put("OUT_RAW", 0.0);			// deprecated

			// other options
			options.put("SPARSE_QP", 1.0);			// deprecated

			// MIPS, PDIPM, SC-PDIPM, and TRALM options
			options.put("PDIPM_FEASTOL", 0.0);
			options.put("PDIPM_GRADTOL", 1e-6);
			options.put("PDIPM_COMPTOL", 1e-6);
			options.put("PDIPM_COSTTOL", 1e-6);
			options.put("PDIPM_MAX_IT", 150.0);
			options.put("SCPDIPM_RED_IT", 20.0);
			options.put("TRALM_FEASTOL", 0.0);
			options.put("TRALM_PRIMETOL", 5e-4);
			options.put("TRALM_DUALTOL", 5e-4);
			options.put("TRALM_COSTTOL", 1e-5);
			options.put("TRALM_MAJOR_IT", 40.0);
			options.put("TRALM_MINOR_IT", 100.0);
			options.put("SMOOTHING_RATIO", 0.04);
		}

		if (name1 != null)
			options.put(name1, value1);
		if (name2 != null)
			options.put(name2, value2);
		if (name3 != null)
			options.put(name3, value3);
		if (name4 != null)
			options.put(name4, value4);
		if (name5 != null)
			options.put(name5, value5);
		if (name6 != null)
			options.put(name6, value6);
		if (name7 != null)
			options.put(name7, value7);

		return options;
	}

	public static Map<String, Double> jpoption(Map<String, Double> options, String name1, Double value1, String name2, Double value2, String name3, Double value3, String name4, Double value4, String name5, Double value5, String name6, Double value6) {
		return jpoption(options, name1, value1, name2, value2, name3, value3, name4, value4, name5, value5, name6, value6, null, null);
	}

	public static Map<String, Double> jpoption(Map<String, Double> options, String name1, Double value1, String name2, Double value2, String name3, Double value3, String name4, Double value4, String name5, Double value5) {
		return jpoption(options, name1, value1, name2, value2, name3, value3, name4, value4, name5, value5, null, null, null, null);
	}

	public static Map<String, Double> jpoption(Map<String, Double> options, String name1, Double value1, String name2, Double value2, String name3, Double value3, String name4, Double value4) {
		return jpoption(options, name1, value1, name2, value2, name3, value3, name4, value4, null, null, null, null, null, null);
	}

	public static Map<String, Double> jpoption(Map<String, Double> options, String name1, Double value1, String name2, Double value2, String name3, Double value3) {
		return jpoption(options, name1, value1, name2, value2, name3, value3, null, null, null, null, null, null, null, null);
	}

	public static Map<String, Double> jpoption(Map<String, Double> options, String name1, Double value1, String name2, Double value2) {
		return jpoption(options, name1, value1, name2, value2, null, null, null, null, null, null, null, null, null, null);
	}

	public static Map<String, Double> jpoption(Map<String, Double> options, String name1, Double value1) {
		return jpoption(options, name1, value1, null, null, null, null, null, null, null, null, null, null, null, null);
	}

}
