/**
 * 
 */
package de.clusteval.data.statistics;

import java.io.File;
import java.io.IOException;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RserveException;

import utils.ArraysExt;
import utils.SimilarityMatrix;
import de.clusteval.data.DataConfig;
import de.clusteval.data.dataset.DataSetConfig;
import de.clusteval.data.dataset.RelativeDataSet;
import de.clusteval.data.dataset.format.InvalidDataSetFormatVersionException;
import de.clusteval.data.statistics.ClusteringCoefficientDataStatistic;
import de.clusteval.data.statistics.DataStatisticCalculator;
import de.clusteval.framework.MyRengine;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;

/**
 * @author Christian Wiwie
 * 
 */
public class ClusteringCoefficientDataStatisticCalculator
		extends
			DataStatisticCalculator<ClusteringCoefficientDataStatistic> {

	/**
	 * @param repository
	 * @param changeDate
	 * @param absPath
	 * @param dataConfig
	 * @throws RegisterException
	 */
	public ClusteringCoefficientDataStatisticCalculator(Repository repository,
			long changeDate, File absPath, DataConfig dataConfig)
			throws RegisterException {
		super(repository, changeDate, absPath, dataConfig);
	}

	/**
	 * The copy constructor for this statistic calculator.
	 * 
	 * @param other
	 *            The object to clone.
	 * @throws RegisterException
	 */
	public ClusteringCoefficientDataStatisticCalculator(
			final ClusteringCoefficientDataStatisticCalculator other)
			throws RegisterException {
		super(other);
	}

	@Override
	protected ClusteringCoefficientDataStatistic calculateResult()
			throws IllegalArgumentException, IOException,
			InvalidDataSetFormatVersionException, RegisterException {

		DataSetConfig dataSetConfig = dataConfig.getDatasetConfig();
		RelativeDataSet dataSet = (RelativeDataSet) (dataSetConfig.getDataSet()
				.getInStandardFormat());

		if (!dataSet.isInMemory())
			dataSet.loadIntoMemory();
		SimilarityMatrix simMatrix = dataSet.getDataSetContent();
		if (dataSet.isInMemory())
			dataSet.unloadFromMemory();

		double[][] similarities = simMatrix.toArray();
		similarities = ArraysExt.scaleBy(similarities,
				ArraysExt.max(similarities));

		try {
			MyRengine rEngine = new MyRengine("");
			try {
				rEngine.assign("simMatrix", similarities);
				rEngine.eval("library('igraph')");
				rEngine.eval("gr <- graph.adjacency(simMatrix,weighted=TRUE)");
				rEngine.eval("gr <- simplify(gr, remove.loops=TRUE, remove.multiple=TRUE)");
				rEngine.eval("trans <- transitivity(gr,type='global',vids=V(gr))");
				REXP result = rEngine.eval("trans");
				return new ClusteringCoefficientDataStatistic(repository,
						false, changeDate, absPath, result.asDouble());
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

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see data.statistics.DataStatisticCalculator#writeOutputTo(java.io.File)
	 */
	@SuppressWarnings("unused")
	@Override
	public void writeOutputTo(File absFolderPath) {
	}

}
