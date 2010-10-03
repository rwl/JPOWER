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

package cern.colt.util.tdouble;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import cern.colt.matrix.AbstractMatrix;
import cern.colt.matrix.io.MatrixInfo;
import cern.colt.matrix.io.MatrixSize;
import cern.colt.matrix.io.MatrixVectorReader;
import cern.colt.matrix.tdcomplex.DComplexFactory1D;
import cern.colt.matrix.tdcomplex.DComplexFactory2D;
import cern.colt.matrix.tdcomplex.impl.DenseDComplexMatrix1D;
import cern.colt.matrix.tdcomplex.impl.DenseDComplexMatrix2D;
import cern.colt.matrix.tdcomplex.impl.SparseDComplexMatrix1D;
import cern.colt.matrix.tdcomplex.impl.SparseDComplexMatrix2D;
import cern.colt.matrix.tdouble.DoubleFactory1D;
import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix1D;
import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix2D;

/**
 *
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class Djp_mm {

	public static AbstractMatrix readMatrix(File file) {
		return readMatrix(file.getAbsolutePath());
	}

	/**
	 *
	 * @param uri
	 * @return
	 * @throws IOException
	 */
	public static AbstractMatrix readMatrix(String fileName) {

		FileReader fileReader = null;
		try {
			fileReader = new FileReader(fileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		MatrixVectorReader reader = new MatrixVectorReader(fileReader);

		MatrixInfo info = null;
		MatrixSize size = null;
		try {
			info = reader.readMatrixInfo();
			size = reader.readMatrixSize(info);
		} catch (IOException e) {
			e.printStackTrace();
		}

		double[] data = new double[size.numEntries()];
		double[] dataR = new double[size.numEntries()];
		double[] dataI = new double[size.numEntries()];

		int[] row = new int[size.numEntries()];
		int[] col = new int[size.numEntries()];

		AbstractMatrix m = null;

		if (info.isArray()) {
			if (info.isComplex()) {
				try {
					reader.readArray(dataR, dataI);
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (info.isDense()) {
					m = DComplexFactory1D.dense.make(size.numEntries());
					for (int i = 0; i < size.numEntries(); i++)
						((DenseDComplexMatrix1D) m).setQuick(i, dataR[i], dataI[i]);
				} else if (info.isSparse()) {
					m = DComplexFactory1D.sparse.make(size.numEntries());
					for (int i = 0; i < size.numEntries(); i++)
						((SparseDComplexMatrix1D) m).setQuick(i, dataR[i], dataI[i]);
				} else {
					throw new UnsupportedOperationException();
				}
			} else {
				try {
					reader.readArray(data);
				} catch (IOException e) {
					e.printStackTrace();
				}

				if (size.numRows() == 1 || size.numColumns() == 1) {
					if (info.isDense()) {
						m = DoubleFactory1D.dense.make(size.numEntries());
						for (int i = 0; i < size.numEntries(); i++)
							((DenseDoubleMatrix1D) m).setQuick(i, data[i]);
					} else if (info.isSparse()) {
						m = DoubleFactory1D.sparse.make(size.numEntries());
						for (int i = 0; i < size.numEntries(); i++)
							((SparseDoubleMatrix1D) m).setQuick(i, data[i]);
					} else {
						throw new UnsupportedOperationException();
					}
				} else {
					if (info.isDense()) {
						m = DoubleFactory2D.dense.make(size.numRows(), size.numColumns());
						for (int i = 0; i < size.numColumns(); i++) {
							for (int j = 0; j < size.numRows(); j++) {
								((DenseDoubleMatrix2D) m).setQuick(j, i, data[i * size.numRows() + j]);
							}
						}
					} else if (info.isSparse()) {
						m = DoubleFactory2D.sparse.make(size.numRows(), size.numColumns());
						for (int i = 0; i < size.numColumns(); i++) {
							for (int j = 0; j < size.numRows(); j++) {
								((SparseDoubleMatrix2D) m).setQuick(j, i, data[i * size.numRows() + j]);
							}
						}
					} else {
						throw new UnsupportedOperationException();
					}
				}
			}
		} else if (info.isCoordinate()) {
			if (info.isComplex()) {
				try {
					reader.readCoordinate(row, col, dataR, dataI);
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (info.isDense()) {
					m = DComplexFactory2D.dense.make(size.numRows(), size.numColumns());
					for (int i = 0; i < size.numEntries(); i++) {
						((DenseDComplexMatrix2D) m).setQuick(row[i], col[i], dataR[i], dataI[i]);
						if (info.isSymmetric())
							((DenseDComplexMatrix2D) m).setQuick(col[i], row[i], dataR[i], dataI[i]);
					}
				} else if (info.isSparse()) {
					m = DComplexFactory2D.sparse.make(size.numRows(), size.numColumns());
					for (int i = 0; i < size.numEntries(); i++) {
						((SparseDComplexMatrix2D) m).setQuick(row[i], col[i], dataR[i], dataI[i]);
						if (info.isSymmetric())
							((SparseDComplexMatrix2D) m).setQuick(col[i], row[i], dataR[i], dataI[i]);
					}
				} else {
					throw new UnsupportedOperationException();
				}
			} else {
				try {
					reader.readCoordinate(row, col, data);
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (info.isDense()) {
					m = DoubleFactory2D.dense.make(size.numRows(), size.numColumns());
					for (int i = 0; i < size.numEntries(); i++) {
						((DenseDoubleMatrix2D) m).setQuick(row[i], col[i], data[i]);
						if (info.isSymmetric())
							((DenseDoubleMatrix2D) m).setQuick(col[i], row[i], data[i]);
					}
				} else if (info.isSparse()) {
					m = DoubleFactory2D.sparse.make(size.numRows(), size.numColumns());
					for (int i = 0; i < size.numEntries(); i++) {
						((SparseDoubleMatrix2D) m).setQuick(row[i], col[i], data[i]);
						if (info.isSymmetric()) {
							((SparseDoubleMatrix2D) m).setQuick(col[i], row[i], data[i]);
						}
					}
				} else {
					throw new UnsupportedOperationException();
				}
			}
		} else {
			throw new UnsupportedOperationException();
		}

		return m;
	}
}
