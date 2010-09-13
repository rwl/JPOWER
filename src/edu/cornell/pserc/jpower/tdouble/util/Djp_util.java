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

package edu.cornell.pserc.jpower.tdouble.util;

import java.util.concurrent.Future;

import cern.colt.list.tdouble.DoubleArrayList;
import cern.colt.list.tint.IntArrayList;
import cern.colt.matrix.tdcomplex.DComplexFactory1D;
import cern.colt.matrix.tdcomplex.DComplexMatrix1D;
import cern.colt.matrix.tdouble.DoubleFactory1D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tint.IntFactory1D;
import cern.colt.matrix.tint.IntMatrix1D;
import cern.jet.math.tdouble.DoubleFunctions;
import cern.jet.math.tint.IntFunctions;
import edu.emory.mathcs.utils.ConcurrencyUtils;

/**
 *
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class Djp_util {

	public static IntFunctions ifunc = IntFunctions.intFunctions;
	public static DoubleFunctions dfunc = DoubleFunctions.functions;

	/**
	 *
	 * @param stop
	 * @return
	 */
	public static int[] irange(int stop) {
		return irange(0, stop);
	}

	/**
	 *
	 * @param start
	 * @param stop
	 * @return
	 */
	public static int[] irange(int start, int stop) {
		return irange(start, stop, 1);
	}

	/**
	 *
	 * @param start
	 * @param stop
	 * @param step
	 * @return
	 */
	public static int[] irange(int start, int stop, int step) {
		int[] r = new int[stop - start];
		int v = start;
		for (int i = 0; i < r.length; i++) {
			r[i] = v;
			v += step;
		}
		return r;
	}

	/**
	 *
	 * @param stop
	 * @return
	 */
	public static double[] drange(int stop) {
		return drange(0, stop);
	}

	/**
	 *
	 * @param start
	 * @param stop
	 * @return
	 */
	public static double[] drange(int start, int stop) {
		return drange(start, stop, 1);
	}

	/**
	 *
	 * @param start
	 * @param stop
	 * @param step
	 * @return
	 */
	public static double[] drange(int start, int stop, int step) {
		double[] r = new double[stop - start];
		int v = start;
		for (int i = 0; i < r.length; i++) {
			r[i] = v;
			v += step;
		}
		return r;
	}

	/**
	 *
	 * @param n
	 * @return
	 */
	public static int[] zeros(int size) {
		final int[] values = new int[size];
		int nthreads = ConcurrencyUtils.getNumberOfThreads();
		if ((nthreads > 1) && (size >= ConcurrencyUtils.getThreadsBeginN_1D())) {
			nthreads = Math.min(nthreads, size);
			Future<?>[] futures = new Future[nthreads];
			int k = size / nthreads;
			for (int j = 0; j < nthreads; j++) {
				final int firstIdx = j * k;
				final int lastIdx = (j == nthreads - 1) ? size : firstIdx + k;
				futures[j] = ConcurrencyUtils.submit(new Runnable() {
					public void run() {
						for (int i = firstIdx; i < lastIdx; i++) {
							values[i] = 0;
						}
					}
				});
			}
			ConcurrencyUtils.waitForCompletion(futures);
		} else {
			for (int i = 0; i < size; i++) {
				values[i] = 0;
			}
		}
		return values;
	}

	/**
	 *
	 * @param size array length
	 * @return an integer array with all elements = 1.
	 */
	public static int[] ones(int size) {
		final int[] values = new int[size];
		int nthreads = ConcurrencyUtils.getNumberOfThreads();
		if ((nthreads > 1) && (size >= ConcurrencyUtils.getThreadsBeginN_1D())) {
			nthreads = Math.min(nthreads, size);
			Future<?>[] futures = new Future[nthreads];
			int k = size / nthreads;
			for (int j = 0; j < nthreads; j++) {
				final int firstIdx = j * k;
				final int lastIdx = (j == nthreads - 1) ? size : firstIdx + k;
				futures[j] = ConcurrencyUtils.submit(new Runnable() {
					public void run() {
						for (int i = firstIdx; i < lastIdx; i++) {
							values[i] = 1;
						}
					}
				});
			}
			ConcurrencyUtils.waitForCompletion(futures);
		} else {
			for (int i = 0; i < size; i++) {
				values[i] = 1;
			}
		}
		return values;
	}

	/**
	 *
	 * @param d
	 * @return
	 */
	public static int[] inta(final DoubleMatrix1D d) {
		int size = (int) d.size();
		final int[] values = new int[size];
		int nthreads = ConcurrencyUtils.getNumberOfThreads();
		if ((nthreads > 1) && (size >= ConcurrencyUtils.getThreadsBeginN_1D())) {
			nthreads = Math.min(nthreads, size);
			Future<?>[] futures = new Future[nthreads];
			int k = size / nthreads;
			for (int j = 0; j < nthreads; j++) {
				final int firstIdx = j * k;
				final int lastIdx = (j == nthreads - 1) ? size : firstIdx + k;
				futures[j] = ConcurrencyUtils.submit(new Runnable() {
					public void run() {
						for (int i = firstIdx; i < lastIdx; i++) {
							values[i] = (int) d.getQuick(i);
						}
					}
				});
			}
			ConcurrencyUtils.waitForCompletion(futures);
		} else {
			for (int i = 0; i < size; i++) {
				values[i] = (int) d.getQuick(i);
			}
		}
		return values;
	}

	/**
	 *
	 * @param d
	 * @return
	 */
	public static IntMatrix1D intm(final DoubleMatrix1D d) {
		int size = (int) d.size();
		final IntMatrix1D values = IntFactory1D.dense.make(size);
		int nthreads = ConcurrencyUtils.getNumberOfThreads();
		if ((nthreads > 1) && (size >= ConcurrencyUtils.getThreadsBeginN_1D())) {
			nthreads = Math.min(nthreads, size);
			Future<?>[] futures = new Future[nthreads];
			int k = size / nthreads;
			for (int j = 0; j < nthreads; j++) {
				final int firstIdx = j * k;
				final int lastIdx = (j == nthreads - 1) ? size : firstIdx + k;
				futures[j] = ConcurrencyUtils.submit(new Runnable() {
					public void run() {
						for (int i = firstIdx; i < lastIdx; i++) {
							values.setQuick(i, (int) d.getQuick(i));
						}
					}
				});
			}
			ConcurrencyUtils.waitForCompletion(futures);
		} else {
			for (int i = 0; i < size; i++) {
				values.setQuick(i, (int) d.getQuick(i));
			}
		}
		return values;
	}

	/**
	 *
	 * @param d
	 * @return
	 */
	public static DoubleMatrix1D dbla(final int[] ix) {
		int size = ix.length;
		final DoubleMatrix1D values = DoubleFactory1D.dense.make(size);
		int nthreads = ConcurrencyUtils.getNumberOfThreads();
		if ((nthreads > 1) && (size >= ConcurrencyUtils.getThreadsBeginN_1D())) {
			nthreads = Math.min(nthreads, size);
			Future<?>[] futures = new Future[nthreads];
			int k = size / nthreads;
			for (int j = 0; j < nthreads; j++) {
				final int firstIdx = j * k;
				final int lastIdx = (j == nthreads - 1) ? size : firstIdx + k;
				futures[j] = ConcurrencyUtils.submit(new Runnable() {
					public void run() {
						for (int i = firstIdx; i < lastIdx; i++) {
							values.setQuick(i, ix[i]);
						}
					}
				});
			}
			ConcurrencyUtils.waitForCompletion(futures);
		} else {
			for (int i = 0; i < size; i++) {
				values.setQuick(i, ix[i]);
			}
		}
		return values;
	}

	/**
	 *
	 * @param d
	 * @return
	 */
	public static DoubleMatrix1D dblm(final IntMatrix1D ix) {
		int size = (int) ix.size();
		final DoubleMatrix1D values = DoubleFactory1D.dense.make(size);
		int nthreads = ConcurrencyUtils.getNumberOfThreads();
		if ((nthreads > 1) && (size >= ConcurrencyUtils.getThreadsBeginN_1D())) {
			nthreads = Math.min(nthreads, size);
			Future<?>[] futures = new Future[nthreads];
			int k = size / nthreads;
			for (int j = 0; j < nthreads; j++) {
				final int firstIdx = j * k;
				final int lastIdx = (j == nthreads - 1) ? size : firstIdx + k;
				futures[j] = ConcurrencyUtils.submit(new Runnable() {
					public void run() {
						for (int i = firstIdx; i < lastIdx; i++) {
							values.setQuick(i, ix.getQuick(i));
						}
					}
				});
			}
			ConcurrencyUtils.waitForCompletion(futures);
		} else {
			for (int i = 0; i < size; i++) {
				values.setQuick(i, ix.getQuick(i));
			}
		}
		return values;
	}

	/**
	 *
	 * @param t
	 * @return
	 */
	public static int max(int[] t) {
		int maximum = t[0];
		for (int i=1; i < t.length; i++)
			if (t[i] > maximum)
				maximum = t[i];
		return maximum;
	}

	/**
	 *
	 * @param a
	 * @param b
	 * @return
	 */
	public static int[] cat(int[] a, int[] b) {
		int[] c = new int[a.length + b.length];
		System.arraycopy(a, 0, c, 0, a.length);
		System.arraycopy(b, 0, c, a.length, b.length);
		return c;
	}

	/**
	 *
	 * @param a
	 * @return
	 */
	public static int[] nonzero(IntMatrix1D a) {
		IntArrayList indexList = new IntArrayList();
		int size = (int) a.size();
		int rem = size % 2;
		if (rem == 1) {
			int value = a.getQuick(0);
			if (value != 0)
				indexList.add(0);
		}

		for (int i = rem; i < size; i += 2) {
			int value = a.getQuick(i);
			if (value != 0)
				indexList.add(i);
			value = a.getQuick(i + 1);
			if (value != 0)
				indexList.add(i + 1);
		}
		indexList.trimToSize();
		return indexList.elements();
	}

	/**
	 *
	 * @param a
	 * @return
	 */
	public static int[] nonzero(DoubleMatrix1D a) {
		IntArrayList indexList = new IntArrayList();
		int size = (int) a.size();
		int rem = size % 2;
		if (rem == 1) {
			double value = a.getQuick(0);
			if (value != 0)
				indexList.add(0);
		}

		for (int i = rem; i < size; i += 2) {
			double value = a.getQuick(i);
			if (value != 0)
				indexList.add(i);
			value = a.getQuick(i + 1);
			if (value != 0)
				indexList.add(i + 1);
		}
		indexList.trimToSize();
		return indexList.elements();
	}

	/**
	 *
	 * @param r
	 * @param theta
	 * @return
	 */
	public static DComplexMatrix1D polar(DoubleMatrix1D r, DoubleMatrix1D theta) {
		return polar(r, theta, true);
	}

	/**
	 *
	 * @param r polar radius.
	 * @param theta polar angle.
	 * @param radians is 'theta' expressed in radians.
	 * @return complex polar representation.
	 */
	@SuppressWarnings("static-access")
	public static DComplexMatrix1D polar(DoubleMatrix1D r, DoubleMatrix1D theta, boolean radians) {
		DoubleMatrix1D real = theta.copy();
		DoubleMatrix1D imag = theta.copy();
		if (!radians) {
			real.assign(dfunc.chain(dfunc.mult(Math.PI), dfunc.div(180)));
			imag.assign(dfunc.chain(dfunc.mult(Math.PI), dfunc.div(180)));
		}
		real.assign(dfunc.cos);
		imag.assign(dfunc.sin);
		real.assign(r, dfunc.mult);
		imag.assign(r, dfunc.mult);

		DComplexMatrix1D cmplx = DComplexFactory1D.dense.make((int) r.size());
		cmplx.assignReal(real);
		cmplx.assignImaginary(imag);

		return cmplx;
	}

	/**
	 *
	 * @param x
	 * @return [x(1)-x(0)  x(2)-x(1) ... x(n)-x(n-1)]
	 */
	@SuppressWarnings("static-access")
	public static IntMatrix1D diff(IntMatrix1D x) {
		int size = (int) x.size() -1;
		IntMatrix1D d = IntFactory1D.dense.make(size);
		for (int i = 0; i < size; i++)
			d.set(i, ifunc.minus.apply(x.get(i+1), x.get(i)));
		return d;
	}

	/**
	 *
	 * @param x a vector of integers.
	 * @return true if any element of vector x is a nonzero number.
	 */
	public static boolean any(IntMatrix1D x) {
		IntArrayList indexList = new IntArrayList();
		x.getNonZeros(indexList, new IntArrayList());
		return indexList.size() > 0;
	}

	/**
	 *
	 * @param x a vector of doubles.
	 * @return true if any element of vector x is a nonzero number.
	 */
	public static boolean any(DoubleMatrix1D x) {
		IntArrayList indexList = new IntArrayList();
		x.getNonZeros(indexList, new DoubleArrayList());
		return indexList.size() > 0;
	}

	/**
	 *
	 * @param real real component, may be null
	 * @param imaginary, imaginary component, may be null
	 * @return a complex vector
	 */
	public static DComplexMatrix1D complex(DoubleMatrix1D real, DoubleMatrix1D imaginary) {
		DComplexMatrix1D cmplx = DComplexFactory1D.dense.make((int) real.size());
		if (real != null)
			cmplx.assignReal(real);
		if (imaginary != null)
			cmplx.assignImaginary(imaginary);
		return cmplx;
	}
}
