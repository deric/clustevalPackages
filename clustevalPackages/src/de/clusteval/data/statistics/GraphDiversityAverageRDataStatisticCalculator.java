/**
 * 
 */
package de.clusteval.data.statistics;

import java.io.File;
import java.io.IOException;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;

import utils.ArraysExt;
import utils.SimilarityMatrix;
import de.clusteval.data.DataConfig;
import de.clusteval.data.dataset.DataSetConfig;
import de.clusteval.data.dataset.RelativeDataSet;
import de.clusteval.data.dataset.format.InvalidDataSetFormatVersionException;
import de.clusteval.framework.MyRengine;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;

/**
 * @author Christian Wiwie
 * 
 */
public class GraphDiversityAverageRDataStatisticCalculator
		extends
			DataStatisticRCalculator<GraphDiversityAverageRDataStatistic> {

	/**
	 * @param repository
	 * @param changeDate
	 * @param absPath
	 * @param dataConfig
	 * @throws RegisterException
	 */
	public GraphDiversityAverageRDataStatisticCalculator(Repository repository,
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
	public GraphDiversityAverageRDataStatisticCalculator(
			final GraphDiversityAverageRDataStatisticCalculator other)
			throws RegisterException {
		super(other);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see data.statistics.DataStatisticCalculator#calculate()
	 */
	@Override
	protected GraphDiversityAverageRDataStatistic calculateResultHelper(
			final MyRengine rEngine) throws IllegalArgumentException,
			IOException, InvalidDataSetFormatVersionException,
			RegisterException, REngineException, REXPMismatchException {

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

		rEngine.assign("simMatrix", similarities);
		rEngine.eval("library('igraph')");
		rEngine.eval("gr <- graph.adjacency(simMatrix,weighted=TRUE)");
		rEngine.eval("gr <- simplify(gr, remove.loops=TRUE, remove.multiple=TRUE)");
		rEngine.eval("div <- graph.diversity(gr)");
		REXP result = rEngine.eval("mean(div[!is.na(div)])");
		return new GraphDiversityAverageRDataStatistic(repository, false,
				changeDate, absPath, result.asDouble());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see data.statistics.DataStatisticCalculator#writeOutputTo(java.io.File)
	 */
	@SuppressWarnings("unused")
	@Override
	protected void writeOutputToHelper(File absFolderPath,
			final MyRengine rEngine) {
	}

}
