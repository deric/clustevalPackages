/*******************************************************************************
 * Copyright (c) 2013 Christian Wiwie.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Christian Wiwie - initial API and implementation
 ******************************************************************************/
/**
 * 
 */
package de.clusteval.data.preprocessing;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RserveException;

import utils.SimilarityMatrix;
import de.clusteval.data.dataset.AbsoluteDataSet;
import de.clusteval.data.dataset.DataSet;
import de.clusteval.data.dataset.RelativeDataSet;
import de.clusteval.data.dataset.format.InvalidDataSetFormatVersionException;
import de.clusteval.data.dataset.format.UnknownDataSetFormatException;
import de.clusteval.framework.repository.MyRengine;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;

/**
 * @author Christian Wiwie
 * 
 */
public class RemoveSymmetricZeroFeaturesDataPreprocessor extends DataPreprocessor {

	/**
	 * @param repository
	 * @param register
	 * @param changeDate
	 * @param absPath
	 * @throws RegisterException
	 */
	public RemoveSymmetricZeroFeaturesDataPreprocessor(Repository repository,
			boolean register, long changeDate, File absPath)
			throws RegisterException {
		super(repository, register, changeDate, absPath);
	}

	/**
	 * @param other
	 * @throws RegisterException
	 */
	public RemoveSymmetricZeroFeaturesDataPreprocessor(
			RemoveSymmetricZeroFeaturesDataPreprocessor other) throws RegisterException {
		super(other);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.clusteval.data.preprocessing.DataPreprocessor#preprocess(de.clusteval
	 * .data.DataConfig)
	 */
	@Override
	public DataSet preprocess(DataSet data) {
		if (data instanceof AbsoluteDataSet)
			throw new IllegalArgumentException(
					"The RemoveSymmetricZeroSamplesPreprocessor is only applicable to relative similarities");
		final RelativeDataSet dataSet = (RelativeDataSet) data;
		try {
			dataSet.loadIntoMemory();
			SimilarityMatrix matrix = dataSet.getDataSetContent();
			try {
				MyRengine rEngine = repository.getRengineForCurrentThread();
				try {
					rEngine.assign("matrix", matrix.toArray());
					rEngine.assign("ids", matrix.getIdsArray());
					rEngine.eval("row.names(matrix) <- ids");
					// make similarities symmetric
					rEngine.eval("matrix <- pmax(matrix,t(matrix))");
					// get 0-features
					rEngine.eval("zeroFeatures <- colSums(matrix != 0) > 0");
					rEngine.eval("matrix <- matrix[-c(zeroFeatures),-c(zeroFeatures)]");
					double[][] result = rEngine.eval("matrix").asDoubleMatrix();
					String[] ids = rEngine.eval("row.names(matrix)")
							.asStrings();

					SimilarityMatrix newMatrix = new SimilarityMatrix(ids,
							result);
					DataSet newDataSet = dataSet.clone();
					newDataSet.setAbsolutePath(new File(dataSet
							.getAbsolutePath() + ".remZeroSymmetric"));
					newDataSet.setDataSetContent(newMatrix);
					newDataSet.writeToFile(false);
					newDataSet.unloadFromMemory();
					return newDataSet;
				} catch (REngineException e) {
					e.printStackTrace();
				} catch (REXPMismatchException e) {
					e.printStackTrace();
				} finally {
					rEngine.clear();
				}
			} catch (RserveException e) {
				e.printStackTrace();
			}
		} catch (InvalidDataSetFormatVersionException e1) {
			e1.printStackTrace();
		} catch (IllegalArgumentException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (UnknownDataSetFormatException e) {
			e.printStackTrace();
		} finally {
			dataSet.unloadFromMemory();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.clusteval.data.preprocessing.DataPreprocessor#getCompatibleDataSetFormats
	 * ()
	 */
	@Override
	public Set<String> getCompatibleDataSetFormats() {
		return new HashSet<String>(
				Arrays.asList(new String[]{"SimMatrixDataSetFormat"}));
	}
}
