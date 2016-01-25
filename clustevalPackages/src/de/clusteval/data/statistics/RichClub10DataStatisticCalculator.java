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
import java.io.IOException;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;

import de.wiwie.wiutils.utils.ArraysExt;
import de.wiwie.wiutils.utils.SimilarityMatrix;
import de.clusteval.data.DataConfig;
import de.clusteval.data.dataset.DataSetConfig;
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
public class RichClub10DataStatisticCalculator
		extends
			DataStatisticRCalculator<RichClub10DataStatistic> {

	/**
	 * @param repository
	 * @param changeDate
	 * @param absPath
	 * @param dataConfig
	 * @throws RegisterException
	 */
	public RichClub10DataStatisticCalculator(Repository repository,
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
	public RichClub10DataStatisticCalculator(
			final RichClub10DataStatisticCalculator other)
			throws RegisterException {
		super(other);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see data.statistics.DataStatisticCalculator#calculate()
	 */
	@Override
	protected RichClub10DataStatistic calculateResultHelper(
			final MyRengine rEngine) throws IllegalArgumentException,
			IOException, InvalidDataSetFormatVersionException,
			RegisterException, REngineException, REXPMismatchException,
			UnknownDataSetFormatException, InterruptedException {

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
				ArraysExt.max(similarities), true);

		rEngine.assign("simMatrix", similarities);
		rEngine.eval("library('igraph')");
		rEngine.eval("library('tnet')");
		rEngine.eval("gr <- graph.adjacency(simMatrix,weighted=TRUE)");
		rEngine.eval("gr <- simplify(as.undirected(gr,mode='collapse'), remove.loops=TRUE, remove.multiple=TRUE)");
		rEngine.eval("grnet <- get.edgelist(gr)");
		rEngine.eval("net <- as.data.frame(cbind(i <- grnet[,1],j <- grnet[,2],w <- E(gr)$weight))");
		rEngine.eval("net <- as.tnet(symmetrise_w(net))");
		rEngine.eval("prominence <- degree_w(net)[,c(1,3)]");
		rEngine.eval("result <- weighted_richclub_w(net, rich=\"s\", reshuffle=\"links\", NR=101, seed=1)");
		REXP result = rEngine.eval("result[result[,1]>=quantile(prominence[,2],0.9),][2]");
		return new RichClub10DataStatistic(repository, false,
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
