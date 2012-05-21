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

package edu.cornell.pserc.jpower.pf;

import java.util.Map;

import cern.colt.matrix.AbstractMatrix;
import cern.colt.matrix.tdcomplex.DComplexMatrix1D;
import cern.colt.matrix.tdcomplex.DComplexMatrix2D;
import cern.colt.matrix.tdouble.DoubleFactory1D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tint.IntMatrix1D;

import static edu.emory.mathcs.utils.Utils.ifunc;
import static edu.emory.mathcs.utils.Utils.dfunc;
import static edu.emory.mathcs.utils.Utils.cfunc;
import static edu.emory.mathcs.utils.Utils.nonzero;
import static edu.emory.mathcs.utils.Utils.polar;
import static edu.emory.mathcs.utils.Utils.complex;
import static edu.emory.mathcs.utils.Utils.intm;
import static edu.emory.mathcs.utils.Utils.icat;

import static edu.cornell.pserc.jpower.Djp_bustypes.bustypes;
import static edu.cornell.pserc.jpower.Djp_ext2int.ext2int;
import static edu.cornell.pserc.jpower.Djp_int2ext.int2ext;
import static edu.cornell.pserc.jpower.Djp_jpoption.jpoption;
import static edu.cornell.pserc.jpower.Djp_jpver.jpver;
import static edu.cornell.pserc.jpower.Djp_loadcase.loadcase;
import static edu.cornell.pserc.jpower.Djp_printpf.printpf;
import static edu.cornell.pserc.jpower.Djp_savecase.savecase;

import edu.cornell.pserc.jpower.jpc.Branch;
import edu.cornell.pserc.jpower.jpc.Bus;
import edu.cornell.pserc.jpower.jpc.Gen;
import edu.cornell.pserc.jpower.jpc.JPC;

/**
 * Runs a power flow
 *
 * Runs a power flow (full AC Newton's method by default) and optionally
 * returns the solved values in the data matrices, a flag which is true if
 * the algorithm was successful in finding a solution, and the elapsed
 * time in seconds. All input arguments are optional. If casename is
 * provided it specifies the name of the input data file or struct
 * containing the power flow data. The default value is 'case9'.
 *
 * If the ppopt is provided it overrides the default PYPOWER options
 * vector and can be used to specify the solution algorithm and output
 * options among other things. If the 3rd argument is given the pretty
 * printed output will be appended to the file whose name is given in
 * fname. If solvedcase is specified the solved case will be written to a
 * case file in MATPOWER format with the specified name. If solvedcase
 * ends with '.mat' it saves the case as a MAT-file otherwise it saves it
 * as an M-file.
 *
 * If the ENFORCE_Q_LIMS options is set to true (default is false) then if
 * any generator reactive power limit is violated after running the AC
 * power flow, the corresponding bus is converted to a PQ bus, with Qg at
 * the limit, and the case is re-run. The voltage magnitude at the bus
 * will deviate from the specified value in order to satisfy the reactive
 * power limit. If the reference bus is converted to PQ, the first
 * remaining PV bus will be used as the slack bus for the next iteration.
 * This may result in the real power output at this generator being
 * slightly off from the specified values.
 *
 * @author Ray Zimmerman
 * @author Richard Lincoln
 *
 */
@SuppressWarnings("static-access")
public class Djp_runpf {

	/**
	 *
	 * @param casedata
	 * @param jpopt
	 * @param fname
	 * @param solvedcase
	 * @return
	 */
	public static JPC runpf(JPC casedata, Map<String, Double> jpopt,
			String fname, String solvedcase) {
		int i, verbose, qlim, ref, refgen, ref0, /*iterations, */k, bi, ref_temp;
		int[] pv, pq, on, gbus, limited, mx, mn;
		long t0;
		boolean dc, repeat, success;
		double baseMVA, Varef0;
		double[] maxloc;
		JPC jpc, results;
		Bus bus;
		Gen gen;
		Branch branch;
		Map<String, String> v;
		IntMatrix1D[] bustypes;
		DoubleMatrix1D Va0, Pbusinj, Pfinj, Pbus, Va, fixedQg;
		DoubleMatrix2D B, Bf;
		AbstractMatrix[] Bdc;
		DoubleMatrix2D[] BB;
		DComplexMatrix1D V0, Sbus, V;
		DComplexMatrix2D Ybus, Yf, Yt;
		DComplexMatrix2D[] Y;
		Object[] soln, data;

		/* options */
		verbose = jpopt.get("VERBOSE").intValue();
		qlim = jpopt.get("ENFORCE_Q_LIMS").intValue();	/* enforce Q limits on gens? */
		dc = jpopt.get("PF_DC") != 0.0;				/* use DC formulation? */

		/* read data */
		jpc = loadcase(casedata);

		/* add zero columns to branch for flows if needed */
		branch = jpc.branch;
		if (branch.Qt == null) {
			int nl = branch.size();
			branch.Pf = DoubleFactory1D.dense.make(nl);
			branch.Qf = DoubleFactory1D.dense.make(nl);
			branch.Pt = DoubleFactory1D.dense.make(nl);
			branch.Qt = DoubleFactory1D.dense.make(nl);
		}

		/* convert to internal indexing */
		jpc = ext2int(jpc);
		baseMVA = jpc.baseMVA;
		bus = jpc.bus;
		gen = jpc.gen;
		branch = jpc.branch;

		/* get bus index lists of each type of bus */
		bustypes = bustypes(bus, gen);
		ref = bustypes[0].get(0);
		pv = bustypes[1].toArray();
		pq = bustypes[2].toArray();

		/* generator info */
		on = nonzero(gen.gen_status);     // which generators are on?
		// what buses are they at?
		gbus = gen.gen_bus.viewSelection(on).toArray();

		/* -----  run the power flow  ----- */
		t0 = System.currentTimeMillis();
		if (verbose > 0) {
			v = jpver("all");
			System.out.printf("\nJPOWER Version %s, %s", v.get("Version"), v.get("Date"));
		}

		success = false;
		if (dc) {                                 // DC formulation
			if (verbose > 0)
				System.out.printf(" -- DC Power Flow\n");

			/* initial state */
			Va0 = bus.Va.copy();
			Va0.assign(dfunc.chain(dfunc.mult(Math.PI), dfunc.div(180)));

			/* build B matrices and phase shift injections */
			Bdc = Djp_makeBdc.makeBdc(baseMVA, bus, branch);
			B = (DoubleMatrix2D) Bdc[0];
			Bf = (DoubleMatrix2D) Bdc[1];
			Pbusinj = (DoubleMatrix1D) Bdc[2];
			Pfinj = (DoubleMatrix1D) Bdc[3];

			/* compute complex bus power injections (generation - load) */
			/* adjusted for phase shifters and real shunts */
			Pbus = Djp_makeSbus.makeSbus(baseMVA, bus, gen).getRealPart();
			Pbus.assign(Pbusinj, dfunc.minus);
			Pbus.assign(bus.Gs.copy().assign(dfunc.div(baseMVA)), dfunc.minus);

			/* "run" the power flow */
			Va = Djp_dcpf.dcpf(B, Pbus, Va0, ref, pv, pq);

			/* update data matrices with solution */
			branch.Qf.assign(0);
			branch.Qt.assign(0);
			branch.Pf.assign( Bf.zMult(Va, null).assign(Pfinj, dfunc.plus).assign(dfunc.mult(baseMVA)) );
			branch.Pt.assign( branch.Pf.copy().assign(dfunc.neg) );
			bus.Vm.assign(1);
			bus.Va.assign(Va);
			bus.Va.assign(dfunc.chain(dfunc.mult(180), dfunc.div(Math.PI)));
			// update Pg for swing generator (note: other gens at ref bus are accounted for in Pbus)
			//      Pg = Pinj + Pload + Gs
			//      newPg = oldPg + newPinj - oldPinj
			refgen = 0;
			for (int ii : gbus) if (ii == ref) { refgen = ii; break; }
			gen.Pg.set(on[refgen], gen.Pg.get(on[refgen]) + (B.viewRow(ref).zDotProduct(Va) - Pbus.get(ref)) * baseMVA);

			success = true;
		} else {                                  // AC formulation
			if (verbose > 0)
				System.out.printf(" -- AC Power Flow ");    // solver name and \n added later

			/* initial state */
			//DComplexMatrix1D V0 = DComplexFactory1D.dense.make(bus.size(), new double[] {1, 0});	// flat start
			V0 = polar(bus.Vm, bus.Va, false);
			DComplexMatrix1D normV0g = V0.viewSelection(gbus).copy().assign(cfunc.abs).assign(V0.viewSelection(gbus), cfunc.mult);
			DComplexMatrix1D cVg = complex(gen.Vg.viewSelection(on), null);
			V0.viewSelection(gbus).assign(cVg.assign(normV0g, cfunc.div));

			ref0 = 0;
			Varef0 = 0;
			limited = null;					// list of indices of gens @ Q lims
			fixedQg = null;
			if (qlim > 0) {
				ref0 = ref;							// save index and angle of
				Varef0 = bus.Va.get(ref0);			//   original reference bus
				fixedQg = DoubleFactory1D.dense.make(gen.size());	// Qg of gens at Q limits
			}
			repeat = true;
			while (repeat) {
				/* build admittance matrices */
				Y = Djp_makeYbus.makeYbus(baseMVA, bus, branch);
				Ybus = Y[0]; Yf = Y[1]; Yt = Y[2];

				/* compute complex bus power injections (generation - load) */
				Sbus = Djp_makeSbus.makeSbus(baseMVA, bus, gen);

				/* run the power flow */
				int alg = jpopt.get("PF_ALG").intValue();
				soln = null;
				if (alg == 1) {
					soln = Djp_newtonpf.newtonpf(Ybus, Sbus, V0, ref, pv, pq, jpopt);
				} else if (alg == 2 || alg == 3) {
					BB = Djp_makeB.makeB(baseMVA, bus, branch, alg);
					soln = Djp_fdpf.fdpf(Ybus, Sbus, V0, BB[0], BB[1], ref, pv, pq, jpopt);
				} else if (alg == 4) {
					soln = Djp_gausspf.gausspf(Ybus, Sbus, V0, ref, pv, pq, jpopt);
				} else {
					System.err.println("Only Newton''s method, fast-decoupled, and Gauss-Seidel power flow algorithms currently implemented.");
					// TODO: throw unsupported algorithm exception.
				}
				V = (DComplexMatrix1D) soln[0];
				success = (Boolean) soln[1];
				//iterations = (Integer) soln[2];

				/* update data matrices with solution */
				data = Djp_pfsoln.pfsoln(baseMVA, bus, gen, branch, Ybus, Yf, Yt, V, ref, pv, pq);
				bus = (Bus) data[0];
				gen = (Gen) data[1];
				branch = (Branch) data[2];

				if (qlim > 0) {		// enforce generator Q limits
					/* find gens with violated Q constraints */
					mx = nonzero( gen.gen_status.copy().assign(intm( gen.Qg.copy().assign(gen.Qmax, dfunc.greater) ), ifunc.and) );
					mn = nonzero( gen.gen_status.copy().assign(intm( gen.Qg.copy().assign(gen.Qmin, dfunc.less) ), ifunc.and) );

					if (mx.length > 0 || mn.length > 0) {	// we have some Q limit violations
						if (pv.length == 0) {
							if (verbose > 0) {
								if (mx.length > 0) {
									System.out.printf("Gen %d (only one left) exceeds upper Q limit : INFEASIBLE PROBLEM\n", mx);
								} else {
									System.out.printf("Gen %d (only one left) exceeds lower Q limit : INFEASIBLE PROBLEM\n", mn);
								}
							}
							success = false;
							break;
						}

						// one at a time?
						if (qlim == 2) {	// fix largest violation, ignore the rest
							maxloc = DoubleFactory1D.dense.append(
									gen.Qg.viewSelection(mx).copy().assign(gen.Qmax.viewSelection(mx), dfunc.minus),
									gen.Qmin.viewSelection(mn).copy().assign(gen.Qg.viewSelection(mn), dfunc.minus)
							).getMaxLocation();
							k = new Double(maxloc[1]).intValue();
							if (k > mx.length) {
								mn = new int[] {mn[k - mn.length]};
								mx = new int[0];
							} else {
								mx = new int[] {mx[k]};
								mn = new int[0];
							}
						}

						if (verbose > 0 && mx.length > 0)
							System.out.printf("Gen %d at upper Q limit, converting to PQ bus\n", mx);
						if (verbose > 0 && mn.length > 0)
							System.out.printf("Gen %d at lower Q limit, converting to PQ bus\n", mn);

						/* save corresponding limit values */
						fixedQg.viewSelection(mx).assign(gen.Qmax.viewSelection(mx));
						fixedQg.viewSelection(mn).assign(gen.Qmin.viewSelection(mn));
						mx = icat(mx, mn);

						/* convert to PQ bus */
						gen.Qg.viewSelection(mx).assign(fixedQg.viewSelection(mx));	// set Qg to binding limit
						gen.gen_status.viewSelection(mx).assign(0);					// temporarily turn off gen
						for (i = 0; i < mx.length; i++) {						// (one at a time, since
							bi = gen.gen_bus.get(mx[i]);						//  they may be at same bus)
							bus.Pd.set(bi, bus.Pd.get(bi) - gen.Pg.get(mx[i]));		// adjust load accordingly,
							bus.Qd.set(bi, bus.Qd.get(bi) - gen.Qg.get(mx[i]));
							bus.bus_type.set(gen.gen_bus.get(mx[i]), JPC.PQ);	// & set bus type to PQ
						}

						/* update bus index lists of each type of bus */
						ref_temp = ref;
						bustypes = bustypes(bus, gen);
						ref = bustypes[0].get(0);
						pv = bustypes[1].toArray();
						pq = bustypes[2].toArray();
						if (verbose > 0 && ref != ref_temp)
							System.out.printf("Bus %d is new slack bus\n", ref);
						limited = icat(limited, mx);

					} else {
						repeat = false;	// no more generator Q limits violated
					}
				} else {
					repeat = false;		// don't enforce generator Q limits, once is enough
				}
				if (qlim > 0 && limited.length > 0) {
					// restore injections from limited gens (those at Q limits)
					gen.Qg.viewSelection(limited).assign(fixedQg.viewSelection(limited));	// restore Qg value,
					for (i = 0; i < limited.length; i++) {								// (one at a time, since
						bi = gen.gen_bus.get(limited[i]);								//  they may be at same bus)
						bus.Pd.set(bi, bus.Pd.get(bi) + gen.Pg.get(limited[i]));			// re-adjust load,
						bus.Qd.set(bi, bus.Qd.get(bi) + gen.Qg.get(limited[i]));
					}
					gen.gen_status.viewSelection(limited).assign(1);						// and turn gen back on
					if (ref != ref0) {
						/* adjust voltage angles to make original ref bus correct */
						bus.Va.assign(dfunc.minus(bus.Va.get(ref0) + Varef0));
					}
				}
			}
		}
		jpc.et = (System.currentTimeMillis() - t0) / 1000F;
		jpc.success = success;

		/* -----  output results  ----- */
		// convert back to original bus numbering & print results
		jpc.bus = bus;
		jpc.gen = gen;
		jpc.branch = branch;

		results = int2ext(jpc);

		// zero out result fields of out-of-service gens & branches
		if (results.order.gen.status.off.length > 0) {
			results.gen.Pg.viewSelection(results.order.gen.status.off).assign(0);
			results.gen.Qg.viewSelection(results.order.gen.status.off).assign(0);
		}
		if (results.order.branch.status.off.length > 0) {
			results.branch.Pf.viewSelection(results.order.branch.status.off).assign(0);
			results.branch.Qf.viewSelection(results.order.branch.status.off).assign(0);
			results.branch.Pt.viewSelection(results.order.branch.status.off).assign(0);
			results.branch.Qt.viewSelection(results.order.branch.status.off).assign(0);
		}

		if (fname != "")
			printpf(results, fname, jpopt);

		printpf(results, System.out, jpopt);

		/* save solved case */
		if (solvedcase != "")
			savecase(solvedcase, results);

		return results;
	}

	public static JPC runpf() {
		return runpf("case9");
	}

	public static JPC runpf(String casedata) {
		return runpf(casedata, jpoption());
	}

	public static JPC runpf(String casedata, Map<String, Double> jpopt) {
		return runpf(casedata, jpopt, "");
	}

	public static JPC runpf(String casedata, Map<String, Double> jpopt, String fname) {
		return runpf(casedata, jpopt, fname, "");
	}

	public static JPC runpf(String casedata, Map<String, Double> jpopt, String fname, String solvedcase) {
		JPC jpc = loadcase(casedata);
		return runpf(jpc, jpopt, fname, "");
	}

	public static JPC runpf(JPC casedata) {
		return runpf(casedata, jpoption());
	}

	public static JPC runpf(JPC casedata, Map<String, Double> jpopt) {
		return runpf(casedata, jpopt, "");
	}

	public static JPC runpf(JPC casedata, Map<String, Double> jpopt, String fname) {
		return runpf(casedata, jpopt, "", "");
	}
}
