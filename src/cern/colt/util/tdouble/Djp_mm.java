/*
 * Copyright (C) 2010-2011 Richard Lincoln
 *
 * JPOWER is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * JPOWER is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JPOWER. If not, see <http://www.gnu.org/licenses/>.
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
 * @author Richard Lincoln
 *
 */
public class Djp_mm {

	private static int i, j;
	private static int[] row, col;
	private static double[] data, dataR, dataI;

	private static FileReader fileReader;
	private static MatrixVectorReader reader;
	private static MatrixInfo info;
	private static MatrixSize size;

	private static AbstractMatrix m;

	/**
	 *
	 * @param uri
	 * @return
	 */
	public static AbstractMatrix readMatrix(String fileName) {

		try {
			fileReader = new FileReader(fileName);
			reader = new MatrixVectorReader(fileReader);

			info = reader.readMatrixInfo();
			size = reader.readMatrixSize(info);

			data  = new double[size.numEntries()];
			dataR = new double[size.numEntries()];
			dataI = new double[size.numEntries()];

			row = new int[size.numEntries()];
			col = new int[size.numEntries()];

			if (info.isArray()) {
				if (info.isComplex()) {
					try {
						reader.readArray(dataR, dataI);
					} catch (IOException e) {
						e.printStackTrace();
					}
					if (info.isDense()) {
						m = DComplexFactory1D.dense.make(size.numEntries());
						for (i = 0; i < size.numEntries(); i++)
							((DenseDComplexMatrix1D) m).setQuick(i, dataR[i], dataI[i]);
					} else if (info.isSparse()) {
						m = DComplexFactory1D.sparse.make(size.numEntries());
						for (i = 0; i < size.numEntries(); i++)
							((SparseDComplexMatrix1D) m).setQuick(i, dataR[i], dataI[i]);
					} else {
						throw new UnsupportedOperationException();
					}
				} else {
					reader.readArray(data);

					if (size.numRows() == 1 || size.numColumns() == 1) {
						if (info.isDense()) {
							m = DoubleFactory1D.dense.make(size.numEntries());
							for (i = 0; i < size.numEntries(); i++)
								((DenseDoubleMatrix1D) m).setQuick(i, data[i]);
						} else if (info.isSparse()) {
							m = DoubleFactory1D.sparse.make(size.numEntries());
							for (i = 0; i < size.numEntries(); i++)
								((SparseDoubleMatrix1D) m).setQuick(i, data[i]);
						} else {
							throw new UnsupportedOperationException();
						}
					} else {
						if (info.isDense()) {
							m = DoubleFactory2D.dense.make(size.numRows(), size.numColumns());
							for (i = 0; i < size.numColumns(); i++) {
								for (j = 0; j < size.numRows(); j++) {
									((DenseDoubleMatrix2D) m).setQuick(j, i, data[i * size.numRows() + j]);
								}
							}
						} else if (info.isSparse()) {
							m = DoubleFactory2D.sparse.make(size.numRows(), size.numColumns());
							for (i = 0; i < size.numColumns(); i++) {
								for (j = 0; j < size.numRows(); j++) {
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
					reader.readCoordinate(row, col, dataR, dataI);

					if (info.isDense()) {
						m = DComplexFactory2D.dense.make(size.numRows(), size.numColumns());
						for (i = 0; i < size.numEntries(); i++) {
							((DenseDComplexMatrix2D) m).setQuick(row[i], col[i], dataR[i], dataI[i]);
							if (info.isSymmetric())
								((DenseDComplexMatrix2D) m).setQuick(col[i], row[i], dataR[i], dataI[i]);
						}
					} else if (info.isSparse()) {
						m = DComplexFactory2D.sparse.make(size.numRows(), size.numColumns());
						for (i = 0; i < size.numEntries(); i++) {
							((SparseDComplexMatrix2D) m).setQuick(row[i], col[i], dataR[i], dataI[i]);
							if (info.isSymmetric())
								((SparseDComplexMatrix2D) m).setQuick(col[i], row[i], dataR[i], dataI[i]);
						}
					} else {
						throw new UnsupportedOperationException();
					}
				} else {
					reader.readCoordinate(row, col, data);

					if (info.isDense()) {
						m = DoubleFactory2D.dense.make(size.numRows(), size.numColumns());
						for (i = 0; i < size.numEntries(); i++) {
							((DenseDoubleMatrix2D) m).setQuick(row[i], col[i], data[i]);
							if (info.isSymmetric())
								((DenseDoubleMatrix2D) m).setQuick(col[i], row[i], data[i]);
						}
					} else if (info.isSparse()) {
						m = DoubleFactory2D.sparse.make(size.numRows(), size.numColumns());
						for (i = 0; i < size.numEntries(); i++) {
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

			fileReader.close();
			reader.close();
		} catch (FileNotFoundException e) {
			// TODO Handle exception
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return m;
	}

	public static AbstractMatrix readMatrix(File file) {
		return readMatrix(file.getAbsolutePath());
	}

}
