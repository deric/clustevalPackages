/**
 * 
 */
package de.clusteval.run.statistics;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.rosuda.REngine.REXP;

import utils.Pair;
import de.clusteval.cluster.quality.ClusteringQualityMeasure;
import de.clusteval.cluster.quality.ClusteringQualityMeasureValue;
import de.clusteval.cluster.quality.ClusteringQualitySet;
import de.clusteval.data.DataConfig;
import de.clusteval.data.statistics.DataStatistic;
import de.clusteval.data.statistics.DoubleValueDataStatistic;
import de.clusteval.framework.MyRengine;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.program.ParameterSet;
import de.clusteval.program.Program;
import de.clusteval.run.result.DataAnalysisRunResult;
import de.clusteval.run.result.ParameterOptimizationResult;
import de.clusteval.run.result.RunResultParseException;
import file.FileUtils;

/**
 * @author Christian Wiwie
 * 
 */
public class LinearModelRidgeRunDataStatisticCalculator
		extends
			RunDataStatisticRCalculator<LinearModelRidgeRunDataStatistic> {

	/**
	 * @param repository
	 * @param changeDate
	 * @param absPath
	 * @param runIdentifiers
	 * @param dataIdentifiers
	 * @throws RegisterException
	 */
	public LinearModelRidgeRunDataStatisticCalculator(Repository repository,
			long changeDate, File absPath, final List<String> runIdentifiers,
			final List<String> dataIdentifiers) throws RegisterException {
		super(repository, changeDate, absPath, runIdentifiers, dataIdentifiers);
	}

	/**
	 * The copy constructor for this statistic calculator.
	 * 
	 * @param other
	 *            The object to clone.
	 * @throws RegisterException
	 */
	public LinearModelRidgeRunDataStatisticCalculator(
			final LinearModelRidgeRunDataStatisticCalculator other)
			throws RegisterException {
		super(other);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see run.statistics.RunDataStatisticCalculator#calculateResult()
	 */
	@Override
	protected LinearModelRidgeRunDataStatistic calculateResultHelper(
			final MyRengine rEngine) throws IllegalArgumentException,
			RegisterException, RunResultParseException {

		/*
		 * Get clustering results
		 */
		List<ParameterOptimizationResult> runResults = new ArrayList<ParameterOptimizationResult>();
		for (String runIdentifier : this.uniqueRunIdentifiers) {
			List<ParameterOptimizationResult> results = new ArrayList<ParameterOptimizationResult>();

			try {
				ParameterOptimizationResult.parseFromRunResultFolder(
						this.repository,
						new File(FileUtils.buildPath(
								this.repository.getRunResultBasePath(),
								runIdentifier)), results, false, false, false);
				runResults.addAll(results);
			} catch (Exception e) {
			}
		}

		try {
			for (ParameterOptimizationResult result : runResults)
				result.loadIntoMemory();

			/*
			 * Get data configs common for all data analysis runs
			 */
			final List<DataAnalysisRunResult> dataResults = new ArrayList<DataAnalysisRunResult>();
			List<DataConfig> commonDataConfigs = new ArrayList<DataConfig>();
			for (String dataIdentifier : this.uniqueDataIdentifiers) {
				try {
					DataAnalysisRunResult dataResult = DataAnalysisRunResult
							.parseFromRunResultFolder(
									this.repository,
									new File(FileUtils.buildPath(
											this.repository
													.getRunResultBasePath(),
											dataIdentifier)));
					if (dataResult != null) {
						dataResults.add(dataResult);
						commonDataConfigs.addAll(dataResult.getDataConfigs());
					}
				} catch (Exception e) {
				}
			}

			try {
				for (DataAnalysisRunResult result : dataResults)
					result.loadIntoMemory();

				List<String> commonDataConfigNames = new ArrayList<String>();
				for (DataConfig first : commonDataConfigs) {
					commonDataConfigNames.add(first.getName());
				}
				commonDataConfigNames = new ArrayList<String>(
						new LinkedHashSet<String>(commonDataConfigNames));

				/*
				 * Get data statistics calculated for dataconfigs
				 */
				// mapping from dataconfig,dataStatisticName -> data statistic
				final Set<String> dataStatisticNames = new HashSet<String>();

				final Map<String, Map<String, DataStatistic>> calculatedDataStatistics = new HashMap<String, Map<String, DataStatistic>>();
				for (DataAnalysisRunResult dataResult : dataResults) {
					for (DataConfig dataConfig : dataResult.getDataConfigs()) {
						final List<DataStatistic> dataStatistics = dataResult
								.getDataStatistics(dataConfig);

						// take only data statistics with a double value
						for (DataStatistic ds : dataStatistics) {
							if (ds instanceof DoubleValueDataStatistic) {
								dataStatisticNames.add(ds.getClass()
										.getSimpleName());
								// just overwrite old ones, assuming, that they
								// are
								// the
								// same
								if (!calculatedDataStatistics
										.containsKey(dataConfig.getName()))
									calculatedDataStatistics
											.put(dataConfig.getName(),
													new HashMap<String, DataStatistic>());
								calculatedDataStatistics.get(
										dataConfig.getName()).put(
										ds.getClass().getSimpleName(), ds);
							}
						}
					}
				}
				/*
				 * find data statistics common for all data configs in all data
				 * analysis runs
				 */
				final List<String> commonDataStatisticNames = new ArrayList<String>(
						new LinkedHashSet<String>(dataStatisticNames));
				for (String dataConfig : commonDataConfigNames) {
					commonDataStatisticNames.retainAll(calculatedDataStatistics
							.get(dataConfig).keySet());
				}

				final int colNum = commonDataStatisticNames.size();

				if (colNum == 0)
					return null;

				/*
				 * Build up the input matrix X
				 */
				final int rowNum = commonDataConfigs.size();
				if (rowNum > 0 && colNum > 0) {

					// fill matrix X
					double[][] x = new double[rowNum][colNum];

					for (int row = 0; row < rowNum; row++) {
						final DataConfig dc = commonDataConfigs.get(row);

						for (int col = 0; col < colNum; col++) {
							final DataStatistic ds = calculatedDataStatistics
									.get(dc.getName()).get(
											commonDataStatisticNames.get(col));

							DoubleValueDataStatistic dds = (DoubleValueDataStatistic) ds;
							x[row][col] = Double.isNaN(dds.getValue())
									? null
									: dds.getValue();

						}
					}

					/*
					 * Build vector y for every program and quality measure.
					 * ProgramFullName x ClusteringQualityMeasureName ->
					 * QualitiesOfProgramOnDataConfigs
					 */
					final Map<Pair<String, String>, Double[]> yMap = new HashMap<Pair<String, String>, Double[]>();

					// iterate over run results
					for (ParameterOptimizationResult paramResult : runResults) {
						final Program p = paramResult.getMethod()
								.getProgramConfig().getProgram();
						final DataConfig dc = paramResult.getDataConfig();

						final String programName = p.getFullName();
						final String dataConfigName = dc.getName();

						// get qualities for this run result
						final Map<ClusteringQualityMeasure, ParameterSet> optParamSet = paramResult
								.getOptimalParameterSets();
						for (ClusteringQualityMeasure measure : optParamSet
								.keySet()) {
							final String measureName = measure.toString();

							final ClusteringQualitySet qualSet = paramResult
									.get(optParamSet.get(measure));
							double value = qualSet.get(measure).isTerminated()
									? qualSet.get(measure).getValue()
									: measure.getMinimum();
							final Pair<String, String> pair = Pair.getPair(
									programName, measureName);
							if (!(yMap.containsKey(pair)))
								yMap.put(pair, new Double[rowNum]);
							int ind = commonDataConfigNames
									.indexOf(dataConfigName);
							if (ind != -1) {
								if (yMap.get(pair)[ind] == null
										|| measure
												.isBetterThan(
														ClusteringQualityMeasureValue
																.getForDouble(value),
														ClusteringQualityMeasureValue
																.getForDouble(yMap
																		.get(pair)[ind])))
									yMap.get(pair)[ind] = value;
							}
						}
					}

					Map<Pair<String, String>, double[]> coeffMap = new HashMap<Pair<String, String>, double[]>();

					/*
					 * Train models
					 */
					for (Pair<String, String> pair : yMap.keySet()) {
						final Double[] y = yMap.get(pair);

						// take only rows (data configurations), which are not
						// null
						// in y
						int newRowCount = 0;
						for (Double d : y)
							if (d != null)
								newRowCount++;
						final double[][] newX = new double[newRowCount][colNum];
						final double[] newY = new double[newRowCount];

						int currentPos = 0;
						for (int i = 0; i < y.length; i++) {
							if (y[i] != null) {
								newX[currentPos] = x[i];
								newY[currentPos] = y[i];
								currentPos++;
							}
						}

						try {
							rEngine.assign("x", newX);
							rEngine.assign("y", newY);
							rEngine.eval("library(MASS)");
							rEngine.eval("model <- lm.ridge(y ~ x,lambda=1)");
							REXP result = rEngine.eval("coef(model)");
							double[] coeffs = result.asDoubles();

							coeffMap.put(pair, coeffs);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					return new LinearModelRidgeRunDataStatistic(repository,
							false, changeDate, absPath,
							commonDataStatisticNames, coeffMap);
				}

				return null;
			} finally {
				for (DataAnalysisRunResult result : dataResults)
					result.unloadFromMemory();
			}
		} finally {
			for (ParameterOptimizationResult result : runResults)
				result.unloadFromMemory();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see run.statistics.RunStatisticCalculator#getStatistic()
	 */
	@Override
	public LinearModelRidgeRunDataStatistic getStatistic() {
		return this.lastResult;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.StatisticCalculator#writeOutputTo(java.io.File)
	 */
	@SuppressWarnings("unused")
	@Override
	public void writeOutputTo(File absFolderPath) {
	}

}
