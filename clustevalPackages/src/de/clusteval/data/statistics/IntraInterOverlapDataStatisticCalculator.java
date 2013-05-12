/**
 * 
 */
package de.clusteval.data.statistics;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;

/**
 * @author Christian Wiwie
 * 
 */
public class IntraInterOverlapDataStatisticCalculator
		extends
			DataStatisticCalculator<IntraInterOverlapDataStatistic> {

	/**
	 * @param repository
	 * @param changeDate
	 * @param absPath
	 * @param dataConfig
	 * @throws RegisterException
	 */
	public IntraInterOverlapDataStatisticCalculator(Repository repository,
			long changeDate, File absPath, final DataConfig dataConfig)
			throws RegisterException {
		super(repository, changeDate, absPath, dataConfig);
		if (!(RelativeDataSetFormat.class.isAssignableFrom(dataConfig
				.getDatasetConfig().getDataSet().getDataSetFormat().getClass())))
			throw new IllegalArgumentException(
					"Intra inter similarity distribution overlap can only be calculated for relative dataset formats");
	}

	/**
	 * The copy constructor for this statistic calculator.
	 * 
	 * @param other
	 *            The object to clone.
	 * @throws RegisterException
	 */
	public IntraInterOverlapDataStatisticCalculator(
			final IntraInterOverlapDataStatisticCalculator other)
			throws RegisterException {
		super(other);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see data.statistics.DataStatisticCalculator#calculate(data.DataConfig)
	 */
	@Override
	protected IntraInterOverlapDataStatistic calculateResult()
			throws IncompatibleDataConfigDataStatisticException,
			UnknownGoldStandardFormatException, IllegalArgumentException,
			IOException, InvalidDataSetFormatVersionException,
			RegisterException {
		if (!dataConfig.hasGoldStandardConfig())
			throw new IncompatibleDataConfigDataStatisticException(
					"IntraInterOverlap requires a goldstandard, which the DataConfig "
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

		// double totalSum = ArraysExt.sum(intraDistr) +
		// ArraysExt.sum(interDistr);
		// intraDistr = ArraysExt.scaleBy(intraDistr, totalSum);
		// interDistr = ArraysExt.scaleBy(interDistr, totalSum);

		double overlap = 0.0;
		for (int i = 0; i < intraDistr.length; i++)
			overlap += Math.min(intraDistr[i], interDistr[i]);
		overlap /= ArraysExt.sum(intraDistr) + ArraysExt.sum(interDistr);

		lastResult = new IntraInterOverlapDataStatistic(repository, false,
				changeDate, absPath, overlap);
		return lastResult;
	}

	@SuppressWarnings("unused")
	@Override
	public void writeOutputTo(final File absFolderPath) {
	}
}
