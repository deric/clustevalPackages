/**
 * 
 */
package de.clusteval.data.statistics;

import java.io.File;
import java.io.IOException;

import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RserveException;

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
public class SimilarityDistributionDataStatisticCalculator
		extends
			DataStatisticCalculator<SimilarityDistributionDataStatistic> {

	/**
	 * @param repository
	 * @param changeDate
	 * @param absPath
	 * @param dataConfig
	 * @throws RegisterException
	 */
	public SimilarityDistributionDataStatisticCalculator(Repository repository,
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
	public SimilarityDistributionDataStatisticCalculator(
			final SimilarityDistributionDataStatisticCalculator other)
			throws RegisterException {
		super(other);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see data.statistics.DataStatisticCalculator#calculate(data.DataConfig)
	 */
	@Override
	protected SimilarityDistributionDataStatistic calculateResult()
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

		Pair<double[], int[]> distribution = simMatrix.toDistributionArray(100);

		double[] distr = ArraysExt.toDoubleArray(distribution.getSecond());

		distr = ArraysExt.scaleBy(distr, ArraysExt.sum(distr));

		SimilarityDistributionDataStatistic result = new SimilarityDistributionDataStatistic(
				repository, false, changeDate, absPath,
				distribution.getFirst(), distr);
		lastResult = result;
		return result;
	}

	@Override
	public void writeOutputTo(final File absFolderPath) {
		try {
			MyRengine rEngine = new MyRengine("");
			try {

				rEngine.eval("plotSimilarityDistribution <- function(title, path, xlabels, distr) {"
						+ "names(distr) <- xlabels;"
						+ "svg(filename=paste(path,'.svg',sep=''));"
						+ "barplot(main=title, distr,legend = c('similarities'));"
						+ "dev.off()" + "}");

				double[] xlabels = this.getStatistic().xlabels;
				double[] distr = this.getStatistic().distribution;

				rEngine.assign("xlabels", xlabels);
				rEngine.assign("distr", distr);

				rEngine.eval("plotSimilarityDistribution("
						+ "'similiarity distribution "
						+ dataConfig.getName()
						+ "',path="
						+ "'"
						+ FileUtils.buildPath(absFolderPath.getAbsolutePath(),
								dataConfig.getName()) + "_similarityDistr"
						+ "',xlabels=xlabels, " + "distr=distr)");

				/*
				 * Create a log-plot
				 */
				distr = ArraysExt.scaleBy(distr, 100.0, false);
				distr = ArraysExt.add(distr, 1.0);
				distr = ArraysExt.log(distr, true, 0.0);

				rEngine.assign("distr", distr);

				rEngine.eval("plotSimilarityDistribution("
						+ "'similiarity distribution"
						+ dataConfig.getName()
						+ "',path="
						+ "'"
						+ FileUtils.buildPath(absFolderPath.getAbsolutePath(),
								dataConfig.getName()) + "_similarityDistr_log"
						+ "',xlabels=xlabels, " + "distr=distr)");
			} catch (REngineException e) {
				e.printStackTrace();
			} finally {
				rEngine.close();
			}
		} catch (RserveException e) {
			e.printStackTrace();
		}
	}
}
