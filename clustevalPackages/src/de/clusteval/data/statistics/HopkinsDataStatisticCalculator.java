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
import de.clusteval.data.DataConfig;
import de.clusteval.data.dataset.AbsoluteDataSet;
import de.clusteval.data.dataset.DataMatrix;
import de.clusteval.data.dataset.format.InvalidDataSetFormatVersionException;
import de.clusteval.data.dataset.format.UnknownDataSetFormatException;
import de.clusteval.data.goldstandard.format.UnknownGoldStandardFormatException;
import de.clusteval.framework.MyRengine;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;

/**
 * @author Christian Wiwie
 * 
 */
public class HopkinsDataStatisticCalculator
		extends
			DataStatisticRCalculator<HopkinsDataStatistic> {

	/**
	 * @param repository
	 * @param changeDate
	 * @param absPath
	 * @param dataConfig
	 * @throws RegisterException
	 */
	public HopkinsDataStatisticCalculator(Repository repository,
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
	public HopkinsDataStatisticCalculator(
			final HopkinsDataStatisticCalculator other)
			throws RegisterException {
		super(other);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see data.statistics.DataStatisticCalculator#calculate()
	 */
	@SuppressWarnings("unused")
	@Override
	protected HopkinsDataStatistic calculateResultHelper(final MyRengine rEngine)
			throws IncompatibleDataConfigDataStatisticException,
			UnknownGoldStandardFormatException, UnknownDataSetFormatException,
			IllegalArgumentException, IOException,
			InvalidDataSetFormatVersionException, RegisterException,
			REngineException, REXPMismatchException {
		// check whether throw exception here or in constructor
		if (!(dataConfig.getDatasetConfig().getDataSet().getOriginalDataSet() instanceof AbsoluteDataSet))
			throw new IllegalArgumentException(
					"Hopkins statistic can only be calculated for absolute datasets");

		AbsoluteDataSet dataSet = (AbsoluteDataSet) dataConfig
				.getDatasetConfig().getDataSet().getOriginalDataSet();
		dataSet.loadIntoMemory();
		DataMatrix matrix = dataSet.getDataSetContent();
		double[][] absMatrix = matrix.getData();
		dataSet.unloadFromMemory();

		double[] hopkinsIterations = new double[10];
		rEngine.assign("absMatrix", absMatrix);
		// generate uniform datapoints
		rEngine.eval("minMax <- rbind(apply(absMatrix,MARGIN=2,FUN=min),apply(absMatrix,MARGIN=2,FUN=max))");
		for (int i = 0; i < hopkinsIterations.length; i++) {
			rEngine.eval("print('" + i + "')");
			rEngine.eval("newPoints <- apply(minMax,MARGIN=2,FUN=function(x) {return (runif(n=nrow(absMatrix),min=x[1],max=x[2]))})");
			rEngine.eval("library(fields)");
			rEngine.eval("origDistances <- rdist(absMatrix)");
			rEngine.eval("origMinDistances <- apply(origDistances,MARGIN=1,FUN=function(x) { return (sort(x,partial=2)[2])})");
			rEngine.eval("newDistances <- rdist(absMatrix,newPoints)");
			rEngine.eval("newMinDistances <- apply(newDistances,MARGIN=1,FUN=function(x) { return (sort(x,partial=2)[2])})");
			REXP result = rEngine
					.eval("sum(origMinDistances)/(sum(origMinDistances)+sum(newMinDistances))");
			hopkinsIterations[i] = result.asDouble();
		}
		return new HopkinsDataStatistic(repository, false, changeDate, absPath,
				ArraysExt.mean(hopkinsIterations));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.StatisticCalculator#writeOutputTo(java.io.File)
	 */
	@SuppressWarnings("unused")
	@Override
	protected void writeOutputToHelper(File absFolderPath,
			final MyRengine rEngine) {
	}

}
