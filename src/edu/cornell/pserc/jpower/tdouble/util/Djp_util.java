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

import cern.colt.matrix.tdouble.DoubleFactory1D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tint.IntFactory1D;
import cern.colt.matrix.tint.IntMatrix1D;
import edu.emory.mathcs.utils.ConcurrencyUtils;

/**
 *
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class Djp_util {

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
}
