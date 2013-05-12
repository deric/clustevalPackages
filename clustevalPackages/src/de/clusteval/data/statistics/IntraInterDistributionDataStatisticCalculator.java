/**
 * 
 */
package de.clusteval.data.statistics;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RserveException;

import utils.ArraysExt;
import utils.Pair;
import utils.SimilarityMatrix;
import de.clusteval.cluster.Cluster;
import de.clusteval.cluster.ClusterItem;
import de.clusteval.cluster.Clustering;
import de.clusteval.data.DataConfig;
import de.clusteval.data.dataset.DataSetConfig;
import de.clusteval.data.dataset.RelativeDataSet;
import de.clusteval.data.dataset.format.InvalidDataSetFormatVersionException;
import de.clusteval.data.dataset.format.RelativeDataSetFormat;
import de.clusteval.data.goldstandard.GoldStandard;
import de.clusteval.data.goldstandard.GoldStandardConfig;
import de.clusteval.data.goldstandard.format.UnknownGoldStandardFormatException;
import de.clusteval.framework.MyRengine;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import file.FileUtils;

/**
 * @author Christian Wiwie
 * 
 */
public class IntraInterDistributionDataStatisticCalculator
		extends
			DataStatisticCalculator<IntraInterDistributionDataStatistic> {

	/**
	 * @param repository
	 * @param changeDate
	 * @param absPath
	 * @param dataConfig
	 * @throws RegisterException
	 */
	public IntraInterDistributionDataStatisticCalculator(Repository repository,
			long changeDate, File absPath, final DataConfig dataConfig)
			throws RegisterException {
		super(repository, changeDate, absPath, dataConfig);
		if (!(RelativeDataSetFormat.class.isAssignableFrom(dataConfig
				.getDatasetConfig().getDataSet().getDataSetFormat().getClass())))
			throw new IllegalArgumentException(
					"Intra inter similarity distribution can only be calculated for relative dataset formats");
	}

	/**
	 * The copy constructor for this statistic calculator.
	 * 
	 * @param other
	 *            The object to clone.
	 * @throws RegisterException
	 */
	public IntraInterDistributionDataStatisticCalculator(
			final IntraInterDistributionDataStatisticCalculator other)
			throws RegisterException {
		super(other);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see data.statistics.DataStatisticCalculator#calculate(data.DataConfig)
	 */
	@Override
	protected IntraInterDistributionDataStatistic calculateResult()
			throws IncompatibleDataConfigDataStatisticException,
			UnknownGoldStandardFormatException, IllegalArgumentException,
			IOException, InvalidDataSetFormatVersionException,
			RegisterException {
		if (!dataConfig.hasGoldStandardConfig())
			throw new IncompatibleDataConfigDataStatisticException(
					"IntraInterDistribution requires a goldstandard, which the DataConfig "
							+ dataConfig + " does not provide.");

		GoldStandardConfig goldStandardConfig = dataConfig
				.getGoldstandardConfig();
		GoldStandard goldStandard = goldStandardConfig.getGoldstandard();
		Clustering gsClustering;

		gsClustering = goldStandard.getClustering();

		Map<String, Integer> idToClass = new HashMap<String, Integer>();
		int clId = 0;
		for (Cluster cl : gsClustering) {
			for (ClusterItem item : cl) {
				idToClass.put(item.getId(), clId);
			}
			clId++;
		}

		goldStandard.unloadFromMemory();

		DataSetConfig dataSetConfig = dataConfig.getDatasetConfig();
		RelativeDataSet dataSet = (RelativeDataSet) (dataSetConfig.getDataSet()
				.getInStandardFormat());

		if (!dataSet.isInMemory())
			dataSet.loadIntoMemory();
		SimilarityMatrix simMatrix = dataSet.getDataSetContent();
		if (dataSet.isInMemory())
			dataSet.unloadFromMemory();

		Pair<double[], int[][]> intraVsInterDistribution = simMatrix
				.toIntraInterDistributionArray(100, idToClass);

		double[] intraDistr = ArraysExt.toDoubleArray(intraVsInterDistribution
				.getSecond()[0]);
		double[] interDistr = ArraysExt.toDoubleArray(intraVsInterDistribution
				.getSecond()[1]);

		double totalSum = ArraysExt.sum(intraDistr) + ArraysExt.sum(interDistr);
		intraDistr = ArraysExt.scaleBy(intraDistr, totalSum);
		interDistr = ArraysExt.scaleBy(interDistr, totalSum);

		lastResult = new IntraInterDistributionDataStatistic(repository, false,
				changeDate, absPath, intraVsInterDistribution.getFirst(),
				intraDistr, interDistr);
		return lastResult;
	}

	@Override
	public void writeOutputTo(final File absFolderPath) {
		try {
			MyRengine rEngine = new MyRengine("");
			try {

				rEngine.eval("plotIntraVsInterDistribution <- function(title, path, xlabels, intraDistr, interDistr) {"
						+ "names(intraDistr) <- xlabels;"
						+ "names(interDistr) <- xlabels;"
						+ "svg(filename=paste(path,'.svg',sep=''));"
						+ "barplot(main=title, rbind(intraDistr, interDistr), beside = TRUE,legend = c('intra similarities', 'inter similarities'));"
						+ "dev.off()" + "}");

				double[] xlabels = this.getStatistic().xlabels;
				double[] intraDistr = this.getStatistic().intraDistribution;
				double[] interDistr = this.getStatistic().interDistribution;

				rEngine.assign("xlabels", xlabels);
				rEngine.assign("intraDistr", intraDistr);
				rEngine.assign("interDistr", interDistr);

				rEngine.eval("plotIntraVsInterDistribution("
						+ "'intra vs. inter similiarities "
						+ dataConfig.getName()
						+ "',path="
						+ "'"
						+ FileUtils.buildPath(absFolderPath.getAbsolutePath(),
								dataConfig.getName()) + "_intraVsInter"
						+ "',xlabels=xlabels, " + "intraDistr=intraDistr, "
						+ "interDistr=interDistr)");

				/*
				 * Create a log-plot
				 */
				intraDistr = ArraysExt.scaleBy(intraDistr, 100.0, false);
				intraDistr = ArraysExt.add(intraDistr, 1.0);
				intraDistr = ArraysExt.log(intraDistr, true, 0.0);
				interDistr = ArraysExt.scaleBy(interDistr, 100.0, false);
				interDistr = ArraysExt.add(interDistr, 1.0);
				interDistr = ArraysExt.log(interDistr, true, 0.0);

				rEngine.assign("intraDistr", intraDistr);
				rEngine.assign("interDistr", interDistr);

				rEngine.eval("plotIntraVsInterDistribution("
						+ "'intra vs. inter similiarities "
						+ dataConfig.getName()
						+ "',path="
						+ "'"
						+ FileUtils.buildPath(absFolderPath.getAbsolutePath(),
								dataConfig.getName()) + "_intraVsInter_log"
						+ "',xlabels=xlabels, " + "intraDistr=intraDistr, "
						+ "interDistr=interDistr)");
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
