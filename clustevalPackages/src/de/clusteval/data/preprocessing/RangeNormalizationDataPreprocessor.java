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

import de.clusteval.data.dataset.AbsoluteDataSet;
import de.clusteval.data.dataset.DataMatrix;
import de.clusteval.data.dataset.DataSet;
import de.clusteval.data.dataset.RelativeDataSet;
import de.clusteval.data.dataset.format.InvalidDataSetFormatVersionException;
import de.clusteval.framework.MyRengine;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;

/**
 * Normalize every feature of the dataset (columns of the data matrix) to values
 * between 0 and 1.
 * 
 * @author Christian Wiwie
 * 
 */
public class RangeNormalizationDataPreprocessor extends DataPreprocessor {

	/**
	 * @param repository
	 * @param register
	 * @param changeDate
	 * @param absPath
	 * @throws RegisterException
	 */
	public RangeNormalizationDataPreprocessor(Repository repository,
			boolean register, long changeDate, File absPath)
			throws RegisterException {
		super(repository, register, changeDate, absPath);
	}

	/**
	 * @param other
	 * @throws RegisterException
	 */
	public RangeNormalizationDataPreprocessor(
			RangeNormalizationDataPreprocessor other) throws RegisterException {
		super(other);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.clusteval.data.preprocessing.DataPreprocessor#getRequiredRlibraries()
	 */
	@Override
	public Set<String> getRequiredRlibraries() {
		return new HashSet<String>();
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
		if (data instanceof RelativeDataSet)
			throw new IllegalArgumentException(
					"The range normalization is only applicable to absolute coordinates");
		final AbsoluteDataSet dataSet = (AbsoluteDataSet) data;
		try {
			dataSet.loadIntoMemory();
			DataMatrix matrix = dataSet.getDataSetContent();
			try {
				MyRengine rEngine = new MyRengine("");
				try {
					rEngine.assign("x", matrix.getData());
					rEngine.eval("x.norm <- t(x) - apply(x,MARGIN=2,min)");
					rEngine.eval("x.norm <- t(x.norm / apply(x.norm,MARGIN=1,max))");
					double[][] result = rEngine.eval("x.norm").asDoubleMatrix();

					DataMatrix newMatrix = new DataMatrix(matrix.getIds(),
							result);
					DataSet newDataSet = dataSet.clone();
					newDataSet.setAbsolutePath(new File(dataSet
							.getAbsolutePath() + ".rangeNorm"));
					newDataSet.setDataSetContent(newMatrix);
					newDataSet.writeToFile(true);
					newDataSet.unloadFromMemory();
					return newDataSet;
				} catch (REngineException e) {
					e.printStackTrace();
				} catch (REXPMismatchException e) {
					e.printStackTrace();
				} finally {
					rEngine.close();
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
				Arrays.asList(new String[]{"MatrixDataSetFormat"}));
	}
}
