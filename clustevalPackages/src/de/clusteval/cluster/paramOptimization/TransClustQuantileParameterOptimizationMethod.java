/**
 * 
 */
package de.clusteval.cluster.paramOptimization;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import utils.RangeCreationException;
import de.clusteval.cluster.quality.ClusteringQualityMeasure;
import de.clusteval.data.DataConfig;
import de.clusteval.data.dataset.RelativeDataSet;
import de.clusteval.data.dataset.format.InvalidDataSetFormatVersionException;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.program.ProgramConfig;
import de.clusteval.program.ProgramParameter;
import de.clusteval.run.ParameterOptimizationRun;

/**
 * @author Christian Wiwie
 * 
 */
public class TransClustQuantileParameterOptimizationMethod
		extends
			DivisiveParameterOptimizationMethod {

	/**
	 * @param repo
	 * @param register
	 * @param changeDate
	 * @param absPath
	 * @param run
	 * @param programConfig
	 * @param dataConfig
	 * @param params
	 * @param optimizationCriterion
	 * @param terminateCount
	 * @param isResume
	 * @throws ParameterOptimizationException
	 * @throws RegisterException
	 */
	public TransClustQuantileParameterOptimizationMethod(final Repository repo,
			final boolean register, final long changeDate, final File absPath,
			ParameterOptimizationRun run, ProgramConfig programConfig,
			DataConfig dataConfig, List<ProgramParameter<?>> params,
			ClusteringQualityMeasure optimizationCriterion,
			int[] terminateCount, boolean isResume)
			throws ParameterOptimizationException, RegisterException {
		super(repo, false, changeDate, absPath, run, programConfig, dataConfig,
				params, optimizationCriterion, terminateCount, isResume);

		if (register)
			this.register();
	}

	/**
	 * The copy constructor for this method.
	 * 
	 * @param other
	 *            The object to clone.
	 * @throws RegisterException
	 */
	public TransClustQuantileParameterOptimizationMethod(
			final TransClustQuantileParameterOptimizationMethod other)
			throws RegisterException {
		super(other);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cluster.paramOptimization.DivisiveParameterOptimizationMethod#
	 * initParameterValues()
	 */
	@Override
	protected void initParameterValues() throws ParameterOptimizationException {
		this.parameterValues = new HashMap<ProgramParameter<?>, double[]>();
		currentPos = new HashMap<ProgramParameter<?>, Integer>();
		try {
			RelativeDataSet dataSet = (RelativeDataSet) (this.dataConfig
					.getDatasetConfig().getDataSet().getInStandardFormat());
			dataSet.loadIntoMemory();
			double[] quantiles = dataSet.getDataSetContent().getQuantiles(
					iterationPerParameter[0]);
			dataSet.unloadFromMemory();

			ProgramParameter<?> tParam = this.params.get(0);
			parameterValues.put(tParam, quantiles);
			currentPos.put(tParam, -1);
		} catch (RangeCreationException e1) {
			throw new ParameterOptimizationException(
					"Could not create parameter range for the next iteration: "
							+ e1.getMessage());
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidDataSetFormatVersionException e) {
			e.printStackTrace();
		}
	}
}
