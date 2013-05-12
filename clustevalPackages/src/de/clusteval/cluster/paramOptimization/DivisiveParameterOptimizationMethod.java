/**
 * 
 */
package de.clusteval.cluster.paramOptimization;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.clusteval.cluster.quality.ClusteringQualityMeasure;

import utils.ArraysExt;
import utils.RangeCreationException;
import de.clusteval.data.DataConfig;
import de.clusteval.data.dataset.format.DataSetFormat;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.program.DoubleProgramParameter;
import de.clusteval.program.IntegerProgramParameter;
import de.clusteval.program.ParameterSet;
import de.clusteval.program.ProgramConfig;
import de.clusteval.program.ProgramParameter;
import de.clusteval.run.ParameterOptimizationRun;
import de.clusteval.utils.InternalAttributeException;

/**
 * @author Christian Wiwie
 * 
 * 
 */
public class DivisiveParameterOptimizationMethod
		extends
			ParameterOptimizationMethod {

	protected Map<ProgramParameter<?>, Integer> currentPos;
	protected Map<ProgramParameter<?>, double[]> parameterValues;

	/**
	 * @param repo
	 * @param register
	 * @param changeDate
	 * @param absPath
	 * @param run
	 *            The run this method belongs to.
	 * @param programConfig
	 *            The program configuration this method was created for.
	 * @param dataConfig
	 *            The data configuration this method was created for.
	 * @param params
	 *            This list holds the program parameters that are to be
	 *            optimized by the parameter optimization run.
	 * @param optimizationCriterion
	 *            The quality measure used as the optimization criterion (see
	 *            {@link #optimizationCriterion}).
	 * @param terminateCount
	 *            This array holds the number of iterations that are to be
	 *            performed for each optimization parameter.
	 * @param isResume
	 *            This boolean indiciates, whether the run is a resumption of a
	 *            previous run execution or a completely new execution.
	 * @throws ParameterOptimizationException
	 * @throws RegisterException
	 */
	@SuppressWarnings("unused")
	public DivisiveParameterOptimizationMethod(final Repository repo,
			final boolean register, final long changeDate, final File absPath,
			final ParameterOptimizationRun run,
			final ProgramConfig programConfig, final DataConfig dataConfig,
			final List<ProgramParameter<?>> params,
			final ClusteringQualityMeasure optimizationCriterion,
			final int[] terminateCount, final boolean isResume)
			throws ParameterOptimizationException, RegisterException {
		super(repo, false, changeDate, absPath, run, programConfig, dataConfig,
				params, optimizationCriterion, terminateCount, isResume);

		this.iterationPerParameter = terminateCount;

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
	public DivisiveParameterOptimizationMethod(
			final DivisiveParameterOptimizationMethod other)
			throws RegisterException {
		super(other);

		this.iterationPerParameter = other.iterationPerParameter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cluster.paramOptimization.ParameterOptimizationMethod#getRequiredRlibraries
	 * ()
	 */
	@Override
	public Set<String> getRequiredRlibraries() {
		return new HashSet<String>();
	}

	@SuppressWarnings("unused")
	@Override
	protected void initParameterValues() throws ParameterOptimizationException,
			InternalAttributeException {
		this.parameterValues = new HashMap<ProgramParameter<?>, double[]>();
		currentPos = new HashMap<ProgramParameter<?>, Integer>();
		for (int p = 0; p < this.params.size(); p++) {
			ProgramParameter<?> param = this.params.get(p);
			if (param.getClass().equals(DoubleProgramParameter.class)) {
				DoubleProgramParameter paCast = (DoubleProgramParameter) param;
				double nMinValue = paCast.evaluateMinValue(dataConfig,
						programConfig);
				double nMaxValue = paCast.evaluateMaxValue(dataConfig,
						programConfig);
				double[] paramValues = ArraysExt.range(nMinValue, nMaxValue,
						this.iterationPerParameter[p], true);
				parameterValues.put(param, paramValues);
			} else if (param.getClass().equals(IntegerProgramParameter.class)) {
				IntegerProgramParameter paCast = (IntegerProgramParameter) param;
				int nMinValue = paCast.evaluateMinValue(dataConfig,
						programConfig);
				int nMaxValue = paCast.evaluateMaxValue(dataConfig,
						programConfig);

				try {
					int[] paramValues = ArraysExt.range(nMinValue, nMaxValue,
							this.iterationPerParameter[p], true, false);
					// 10.12.2012 changed in order to avoid multiple
					// evaluation of the same parameter values
					// TODO: check, how this affects the total iteration count
					// and whether it is calculated correctly -> progress
					paramValues = ArraysExt.unique(paramValues);
					this.iterationPerParameter[p] = paramValues.length;
					parameterValues.put(param,
							ArraysExt.toDoubleArray(paramValues));
				} catch (RangeCreationException e) {
					// will never occur
				}
			}
			/*
			 * We need to initialize the last parameter with -1, because it will
			 * be increased by 1 in the hasNext() method before anything was
			 * done.
			 */

			if (param.equals(this.params.get(this.params.size() - 1)))
				currentPos.put(param, -1);
			else
				currentPos.put(param, 0);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cluster.paramOptimization.ParameterOptimizationMethod#getNextParameterSet
	 * ()
	 */
	@SuppressWarnings("unused")
	@Override
	protected ParameterSet getNextParameterSet(
			final ParameterSet forcedParameterSet)
			throws InternalAttributeException, RegisterException,
			NoParameterSetFoundException {

		this.increaseCurrentPos();

		ParameterSet result = new ParameterSet();

		for (ProgramParameter<?> param : this.params) {
			if (forcedParameterSet != null) {
				result.put(param.getName(),
						forcedParameterSet.get(param.getName()));
			} else
				result.put(param.getName(),
						parameterValues.get(param)[currentPos.get(param)]);
		}

		return result;
	}

	@Override
	public boolean hasNext() {
		if (this.params.size() == 0)
			return false;
		int currentParamPos = this.params.size() - 1;
		boolean carry = true;
		while (carry) {
			ProgramParameter<?> param = this.params.get(currentParamPos);
			if (this.currentPos.get(param) < iterationPerParameter[currentParamPos] - 1) {
				carry = false;
			} else if (currentParamPos == 0) {
				return false;
			} else {
				currentParamPos--;
			}
		}
		return true;
	}

	/**
	 * @return True if the operation was successful, false otherwise.
	 */
	public boolean increaseCurrentPos() {
		if (this.params.size() == 0)
			return false;
		int currentParamPos = this.params.size() - 1;
		boolean carry = true;
		while (carry) {
			ProgramParameter<?> param = this.params.get(currentParamPos);
			if (this.currentPos.get(param) < iterationPerParameter[currentParamPos] - 1) {
				this.currentPos.put(param, this.currentPos.get(param) + 1);
				carry = false;
			} else if (currentParamPos == 0) {
				return false;
			} else {
				this.currentPos.put(param, 0);
				currentParamPos--;
			}
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cluster.paramOptimization.ParameterOptimizationMethod#getTotalIterationCount
	 * ()
	 */
	@Override
	public int getTotalIterationCount() {
		return (int) ArraysExt.sum(this.iterationPerParameter);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cluster.paramOptimization.ParameterOptimizationMethod#
	 * getCompatibleDataSetFormatBaseClasses()
	 */
	@Override
	public List<Class<? extends DataSetFormat>> getCompatibleDataSetFormatBaseClasses() {
		return new ArrayList<Class<? extends DataSetFormat>>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cluster.paramOptimization.ParameterOptimizationMethod#
	 * getCompatibleProgramClasses()
	 */
	@Override
	public List<String> getCompatibleProgramNames() {
		return new ArrayList<String>();
	}
}
