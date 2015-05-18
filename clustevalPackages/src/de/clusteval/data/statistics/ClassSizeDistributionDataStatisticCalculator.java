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
package de.clusteval.data.statistics;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.clusteval.cluster.Cluster;
import de.clusteval.cluster.ClusterItem;
import de.clusteval.cluster.Clustering;
import de.clusteval.data.DataConfig;
import de.clusteval.data.dataset.DataSet;
import de.clusteval.data.goldstandard.GoldStandard;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;

/**
 * @author Christian Wiwie
 * 
 */
public class ClassSizeDistributionDataStatisticCalculator
		extends
			DataStatisticCalculator<ClassSizeDistributionDataStatistic> {

	/**
	 * @param repository
	 * @param changeDate
	 * @param absPath
	 * @param dataConfig
	 * @throws RegisterException
	 */
	public ClassSizeDistributionDataStatisticCalculator(Repository repository,
			long changeDate, File absPath, final DataConfig dataConfig)
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
	public ClassSizeDistributionDataStatisticCalculator(
			final ClassSizeDistributionDataStatisticCalculator other)
			throws RegisterException {
		super(other);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see data.statistics.DataStatisticCalculator#calculate(data.DataConfig)
	 */
	@Override
	protected ClassSizeDistributionDataStatistic calculateResult()
			throws DataStatisticCalculateException {
		try {
			DataSet ds = dataConfig.getDatasetConfig().getDataSet();
			ds.loadIntoMemory();
			List<String> dataSetIds = ds.getIds();
			ds.unloadFromMemory();
			GoldStandard gs = dataConfig.getGoldstandardConfig()
					.getGoldstandard();
			gs.loadIntoMemory();
			Clustering clazzes = gs.getClustering();
			gs.unloadFromMemory();

			double[] fuzzySizes = new double[clazzes.getClusters().size()];
			String[] classLabels = new String[clazzes.getClusters().size()];
			Iterator<Cluster> it = clazzes.getClusters().iterator();
			for (int i = 0; i < classLabels.length; i++) {
				Cluster clazz = it.next();
				classLabels[i] = clazz.getId();

				Map<ClusterItem, Float> fuzzyItems = clazz.getFuzzyItems();
				for (Map.Entry<ClusterItem, Float> item : fuzzyItems.entrySet())
					if (dataSetIds.contains(item.getKey().getId()))
						fuzzySizes[i] += item.getValue();
			}

			ClassSizeDistributionDataStatistic result = new ClassSizeDistributionDataStatistic(
					repository, false, changeDate, absPath, classLabels,
					fuzzySizes);
			lastResult = result;
			return result;
		} catch (Exception e) {
			throw new DataStatisticCalculateException(e);
		}
	}

	@SuppressWarnings("unused")
	@Override
	public void writeOutputTo(final File absFolderPath) {
	}
}
