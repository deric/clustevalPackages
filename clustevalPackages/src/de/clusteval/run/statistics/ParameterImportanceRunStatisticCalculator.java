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
package de.clusteval.run.statistics;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;

import utils.ArraysExt;
import utils.Pair;
import de.clusteval.cluster.paramOptimization.IncompatibleParameterOptimizationMethodException;
import de.clusteval.cluster.paramOptimization.InvalidOptimizationParameterException;
import de.clusteval.cluster.paramOptimization.UnknownParameterOptimizationMethodException;
import de.clusteval.cluster.quality.ClusteringQualityMeasure;
import de.clusteval.cluster.quality.ClusteringQualityMeasureValue;
import de.clusteval.cluster.quality.ClusteringQualitySet;
import de.clusteval.cluster.quality.UnknownClusteringQualityMeasureException;
import de.clusteval.context.IncompatibleContextException;
import de.clusteval.context.UnknownContextException;
import de.clusteval.data.DataConfigNotFoundException;
import de.clusteval.data.DataConfigurationException;
import de.clusteval.data.dataset.DataSetConfigNotFoundException;
import de.clusteval.data.dataset.DataSetConfigurationException;
import de.clusteval.data.dataset.DataSetNotFoundException;
import de.clusteval.data.dataset.IncompatibleDataSetConfigPreprocessorException;
import de.clusteval.data.dataset.NoDataSetException;
import de.clusteval.data.dataset.format.UnknownDataSetFormatException;
import de.clusteval.data.dataset.type.UnknownDataSetTypeException;
import de.clusteval.data.distance.UnknownDistanceMeasureException;
import de.clusteval.data.goldstandard.GoldStandardConfigNotFoundException;
import de.clusteval.data.goldstandard.GoldStandardConfigurationException;
import de.clusteval.data.goldstandard.GoldStandardNotFoundException;
import de.clusteval.data.goldstandard.format.UnknownGoldStandardFormatException;
import de.clusteval.data.preprocessing.UnknownDataPreprocessorException;
import de.clusteval.data.statistics.UnknownDataStatisticException;
import de.clusteval.framework.repository.InvalidRepositoryException;
import de.clusteval.framework.repository.NoRepositoryFoundException;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.framework.repository.RepositoryAlreadyExistsException;
import de.clusteval.framework.repository.config.RepositoryConfigNotFoundException;
import de.clusteval.framework.repository.config.RepositoryConfigurationException;
import de.clusteval.program.NoOptimizableProgramParameterException;
import de.clusteval.program.ParameterSet;
import de.clusteval.program.UnknownParameterType;
import de.clusteval.program.UnknownProgramParameterException;
import de.clusteval.program.UnknownProgramTypeException;
import de.clusteval.program.r.UnknownRProgramException;
import de.clusteval.run.InvalidRunModeException;
import de.clusteval.run.RunException;
import de.clusteval.run.result.ParameterOptimizationResult;
import de.clusteval.run.result.RunResultParseException;
import de.clusteval.run.result.format.UnknownRunResultFormatException;
import de.clusteval.utils.InvalidConfigurationFileException;
import file.FileUtils;

/**
 * @author Christian Wiwie
 * 
 */
public class ParameterImportanceRunStatisticCalculator
		extends
			RunStatisticCalculator<ParameterImportanceRunStatistic> {

	/**
	 * @param repository
	 * @param changeDate
	 * @param absPath
	 * @param uniqueRunIdentifier
	 * @throws RegisterException
	 */
	public ParameterImportanceRunStatisticCalculator(Repository repository,
			long changeDate, File absPath, final String uniqueRunIdentifier)
			throws RegisterException {
		super(repository, changeDate, absPath, uniqueRunIdentifier);
	}

	/**
	 * The copy constructor for this statistic calculator.
	 * 
	 * @param other
	 *            The object to clone.
	 * @throws RegisterException
	 */
	public ParameterImportanceRunStatisticCalculator(
			final ParameterImportanceRunStatisticCalculator other)
			throws RegisterException {
		super(other);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see run.statistics.RunStatisticCalculator#calculateResult()
	 */
	@Override
	protected ParameterImportanceRunStatistic calculateResult()
			throws UnknownGoldStandardFormatException,
			UnknownDataSetFormatException, IllegalArgumentException,
			IOException, GoldStandardConfigurationException,
			DataSetConfigurationException, DataSetNotFoundException,
			DataSetConfigNotFoundException,
			GoldStandardConfigNotFoundException, DataConfigurationException,
			DataConfigNotFoundException, UnknownRunResultFormatException,
			UnknownClusteringQualityMeasureException, InvalidRunModeException,
			UnknownParameterOptimizationMethodException,
			NoOptimizableProgramParameterException,
			UnknownProgramParameterException,
			InvalidConfigurationFileException,
			RepositoryAlreadyExistsException, InvalidRepositoryException,
			NoRepositoryFoundException, GoldStandardNotFoundException,
			InvalidOptimizationParameterException, RunException,
			UnknownDataStatisticException, UnknownProgramTypeException,
			UnknownRProgramException,
			IncompatibleParameterOptimizationMethodException,
			UnknownDistanceMeasureException, UnknownRunStatisticException,
			RepositoryConfigNotFoundException,
			RepositoryConfigurationException, ConfigurationException,
			RegisterException, UnknownDataSetTypeException, NoDataSetException,
			UnknownRunDataStatisticException, RunResultParseException,
			UnknownDataPreprocessorException,
			IncompatibleDataSetConfigPreprocessorException,
			UnknownContextException, IncompatibleContextException,
			UnknownParameterType, InterruptedException {

		List<ParameterOptimizationResult> results = new ArrayList<ParameterOptimizationResult>();

		ParameterOptimizationResult.parseFromRunResultFolder(
				this.repository,
				new File(FileUtils.buildPath(
						this.repository.getRunResultBasePath(),
						this.uniqueRunIdentifiers)), results, false, false,
				false);

		Map<Pair<String, String>, Map<String, Double>> paramImportances = new HashMap<Pair<String, String>, Map<String, Double>>();

		for (ParameterOptimizationResult result : results) {
			try {
				result.loadIntoMemory();

				String dataConfig = result.getDataConfig().getName();
				String programConfig = result.getProgramConfig().getName();

				// paramName x paramValue -> validityIndex x ( ..., qualities,
				// ...)
				Map<Pair<String, String>, Map<ClusteringQualityMeasure, List<ClusteringQualityMeasureValue>>> paramQualities = new HashMap<Pair<String, String>, Map<ClusteringQualityMeasure, List<ClusteringQualityMeasureValue>>>();

				for (Pair<ParameterSet, ClusteringQualitySet> qualities : result) {
					for (String paramName : qualities.getFirst().keySet()) {
						Pair<String, String> p = Pair.getPair(programConfig
								+ ":" + paramName,
								qualities.getFirst().get(paramName));

						Map<ClusteringQualityMeasure, List<ClusteringQualityMeasureValue>> quals;
						if (paramQualities.containsKey(p))
							quals = paramQualities.get(p);
						else {
							quals = new HashMap<ClusteringQualityMeasure, List<ClusteringQualityMeasureValue>>();
							paramQualities.put(p, quals);
						}
						for (ClusteringQualityMeasure m : qualities.getSecond()
								.keySet()) {
							if (!quals.containsKey(m))
								quals.put(
										m,
										new ArrayList<ClusteringQualityMeasureValue>());
							quals.get(m).add(qualities.getSecond().get(m));
						}
					}
				}

				Map<Pair<String, String>, Map<String, Double>> paramQualitiesMean = new HashMap<Pair<String, String>, Map<String, Double>>();

				// average over qualities for a certain parameter value
				for (Pair<String, String> p : paramQualities.keySet()) {
					Map<ClusteringQualityMeasure, List<ClusteringQualityMeasureValue>> quals = paramQualities
							.get(p);

					for (ClusteringQualityMeasure measure : quals.keySet()) {
						List<Double> q = new ArrayList<Double>();
						for (int i = 0; i < quals.get(measure).size(); i++) {
							if (quals.get(measure).get(i).isTerminated())
								q.add(quals.get(measure).get(i).getValue());
						}
						double mean = ArraysExt.mean(ArraysExt.toPrimitive(q
								.toArray(new Double[0])));

						if (!paramQualitiesMean.containsKey(p))
							paramQualitiesMean.put(p,
									new HashMap<String, Double>());
						// TODO
						assert !paramQualitiesMean.get(p).containsKey(
								measure.toString()) : "Measure war schon enthalten";
						if (!Double.isNaN(mean))
							paramQualitiesMean.get(p).put(measure.toString(),
									mean);
					}
				}

				// merge for different param values into one list
				Map<String, Map<String, List<Double>>> paramQualitiesLists = new HashMap<String, Map<String, List<Double>>>();
				for (Pair<String, String> p : paramQualitiesMean.keySet()) {
					String paramName = p.getFirst();
					if (!paramQualitiesLists.containsKey(paramName))
						paramQualitiesLists.put(paramName,
								new HashMap<String, List<Double>>());
					Map<String, List<Double>> map = paramQualitiesLists
							.get(paramName);

					for (String measure : paramQualitiesMean.get(p).keySet()) {
						if (!map.containsKey(measure))
							map.put(measure, new ArrayList<Double>());
						map.get(measure).add(
								paramQualitiesMean.get(p).get(measure));
					}
				}

				// calculate variances
				Map<String, Map<String, Double>> paramQualitiesVariances = new HashMap<String, Map<String, Double>>();

				for (String param : paramQualitiesLists.keySet()) {
					if (!paramQualitiesVariances.containsKey(param))
						paramQualitiesVariances.put(param,
								new HashMap<String, Double>());
					Map<String, Double> map = paramQualitiesVariances
							.get(param);
					// for every measure
					for (String measure : paramQualitiesLists.get(param)
							.keySet()) {
						List<Double> quals = paramQualitiesLists.get(param)
								.get(measure);
						double var = ArraysExt.variance(ArraysExt
								.toPrimitive(quals.toArray(new Double[0])));
						map.put(measure, var);
					}
				}

				for (String paramName : paramQualitiesVariances.keySet()) {
					Pair<String, String> p = Pair
							.getPair(paramName, dataConfig);
					if (!paramImportances.containsKey(p))
						paramImportances.put(p, new HashMap<String, Double>());
					for (String measure : paramQualitiesVariances
							.get(paramName).keySet()) {
						assert !paramImportances.get(p).containsKey(measure);
						paramImportances.get(p).put(
								measure,
								paramQualitiesVariances.get(paramName).get(
										measure));
					}
				}
			} finally {
				result.unloadFromMemory();
			}
		}

		return new ParameterImportanceRunStatistic(repository, false,
				changeDate, absPath, paramImportances);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see run.statistics.RunStatisticCalculator#getStatistic()
	 */
	@Override
	public ParameterImportanceRunStatistic getStatistic() {
		return this.lastResult;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.StatisticCalculator#writeOutputTo(java.io.File)
	 */
	@Override
	public void writeOutputTo(File absFolderPath) {
		// TODO needed ?
	}
}
