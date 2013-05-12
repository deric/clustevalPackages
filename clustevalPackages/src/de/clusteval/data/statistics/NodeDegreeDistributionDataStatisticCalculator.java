/**
 * 
 */
package de.clusteval.data.statistics;

import java.io.File;
import java.io.IOException;

import org.rosuda.REngine.REngineException;

import utils.ArraysExt;
import utils.Pair;
import utils.SimilarityMatrix;
import de.clusteval.data.DataConfig;
import de.clusteval.data.dataset.DataSetConfig;
import de.clusteval.data.dataset.RelativeDataSet;
import de.clusteval.data.dataset.format.InvalidDataSetFormatVersionException;
import de.clusteval.data.dataset.format.RelativeDataSetFormat;
import de.clusteval.framework.MyRengine;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import file.FileUtils;

/**
 * @author Christian Wiwie
 * 
 */
public class NodeDegreeDistributionDataStatisticCalculator
		extends
			DataStatisticRCalculator<NodeDegreeDistributionDataStatistic> {

	/**
	 * @param repository
	 * @param changeDate
	 * @param absPath
	 * @param dataConfig
	 * @throws RegisterException
	 */
	public NodeDegreeDistributionDataStatisticCalculator(Repository repository,
			long changeDate, File absPath, final DataConfig dataConfig)
			throws RegisterException {
		super(repository, changeDate, absPath, dataConfig);
		if (!(RelativeDataSetFormat.class.isAssignableFrom(dataConfig
				.getDatasetConfig().getDataSet().getDataSetFormat().getClass())))
			throw new IllegalArgumentException(
					"Similarity distribution can only be calculated for relative dataset formats");
	}

	/**
	 * The copy constructor for this statistic calculator.
	 * 
	 * @param other
	 *            The object to clone.
	 * @throws RegisterException
	 */
	public NodeDegreeDistributionDataStatisticCalculator(
			final NodeDegreeDistributionDataStatisticCalculator other)
			throws RegisterException {
		super(other);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see data.statistics.DataStatisticCalculator#calculate(data.DataConfig)
	 */
	@SuppressWarnings("unused")
	@Override
	protected NodeDegreeDistributionDataStatistic calculateResultHelper(
			final MyRengine rEngine) throws IllegalArgumentException,
			IOException, InvalidDataSetFormatVersionException,
			RegisterException {
		DataSetConfig dataSetConfig = dataConfig.getDatasetConfig();
		RelativeDataSet dataSet = (RelativeDataSet) (dataSetConfig.getDataSet()
				.getInStandardFormat());

		if (!dataSet.isInMemory())
			dataSet.loadIntoMemory();
		SimilarityMatrix simMatrix = dataSet.getDataSetContent();
		if (dataSet.isInMemory())
			dataSet.unloadFromMemory();

		double[] degrees = new double[simMatrix.getIds().size()];
		for (int i = 0; i < degrees.length; i++) {
			for (int j = 0; j < simMatrix.getIds().size(); j++) {
				degrees[i] += simMatrix.getSimilarity(i, j);
			}
		}

		Pair<double[], int[]> histogram = ArraysExt.toHistogram(degrees, 100);

		double[] distr = ArraysExt.scaleBy(histogram.getSecond(),
				ArraysExt.sum(histogram.getSecond()));

		lastResult = new NodeDegreeDistributionDataStatistic(repository, false,
				changeDate, absPath, histogram.getFirst(), distr);
		return lastResult;
	}

	@Override
	protected void writeOutputToHelper(final File absFolderPath,
			final MyRengine rEngine) throws REngineException {

		rEngine.eval("plotNodeDegreeDistribution <- function(title, path, xlabels, distr) {"
				+ "names(distr) <- xlabels;"
				+ "svg(filename=paste(path,'.svg',sep=''));"
				+ "barplot(main=title, distr,legend = c('node degrees'));"
				+ "dev.off()" + "}");

		double[] xlabels = this.getStatistic().xlabels;
		double[] distr = this.getStatistic().distribution;

		rEngine.assign("xlabels", xlabels);
		rEngine.assign("distr", distr);

		rEngine.eval("plotNodeDegreeDistribution("
				+ "'node degree distribution "
				+ dataConfig.getName()
				+ "',path="
				+ "'"
				+ FileUtils.buildPath(absFolderPath.getAbsolutePath(),
						dataConfig.getName()) + "_nodeDegreeDistr"
				+ "',xlabels=xlabels, " + "distr=distr)");

		/*
		 * Create a log-plot
		 */
		distr = ArraysExt.scaleBy(distr, 100.0, false);
		distr = ArraysExt.add(distr, 1.0);
		distr = ArraysExt.log(distr, true, 0.0);

		rEngine.assign("distr", distr);

		rEngine.eval("plotNodeDegreeDistribution("
				+ "'node degree distribution"
				+ dataConfig.getName()
				+ "',path="
				+ "'"
				+ FileUtils.buildPath(absFolderPath.getAbsolutePath(),
						dataConfig.getName()) + "_nodeDegreeDistr_log"
				+ "',xlabels=xlabels, " + "distr=distr)");
	}
}
