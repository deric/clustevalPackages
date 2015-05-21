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
package de.clusteval.cluster.paramOptimization;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import utils.ArraysExt;
import utils.RangeCreationException;
import de.clusteval.cluster.quality.ClusteringQualityMeasure;
import de.clusteval.data.DataConfig;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.program.DoubleProgramParameter;
import de.clusteval.program.IntegerProgramParameter;
import de.clusteval.program.ProgramConfig;
import de.clusteval.program.ProgramParameter;
import de.clusteval.program.StringProgramParameter;
import de.clusteval.run.ParameterOptimizationRun;
import de.clusteval.utils.InternalAttributeException;

/**
 * @author Christian Wiwie
 * 
 * 
 */
public class MCODEDivisiveParameterOptimizationMethod
		extends
			DivisiveParameterOptimizationMethod {

	/**
	 * The copy constructor for this method.
	 * 
	 * @param other
	 *            The object to clone.
	 * @throws RegisterException
	 */
	public MCODEDivisiveParameterOptimizationMethod(
			final MCODEDivisiveParameterOptimizationMethod other)
			throws RegisterException {
		super(other);
	}

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
	public MCODEDivisiveParameterOptimizationMethod(final Repository repo,
			final boolean register, final long changeDate, final File absPath,
			final ParameterOptimizationRun run,
			final ProgramConfig programConfig, final DataConfig dataConfig,
			final List<ProgramParameter<?>> params,
			final ClusteringQualityMeasure optimizationCriterion,
			final int terminateCount, final boolean isResume)
			throws ParameterOptimizationException, RegisterException {
		super(repo, false, changeDate, absPath, run, programConfig, dataConfig,
				params, optimizationCriterion, terminateCount, isResume);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.clusteval.cluster.paramOptimization.DivisiveParameterOptimizationMethod
	 * #initParameterValues()
	 */
	@Override
	protected void initParameterValues() throws ParameterOptimizationException,
			InternalAttributeException {
		this.parameterValues = new HashMap<ProgramParameter<?>, String[]>();
		currentPos = new HashMap<ProgramParameter<?>, Integer>();
		for (int p = 0; p < this.params.size(); p++) {
			ProgramParameter<?> param = this.params.get(p);
			boolean inverse = param.getName().equals("cutoff");
			if (param.getClass().equals(DoubleProgramParameter.class)) {
				DoubleProgramParameter paCast = (DoubleProgramParameter) param;
				double nMinValue = paCast.evaluateMinValue(dataConfig,
						programConfig);
				double nMaxValue = paCast.evaluateMaxValue(dataConfig,
						programConfig);
				double[] paramValues = ArraysExt.range(nMinValue, nMaxValue,
						this.iterationPerParameter[p], true);
				if (inverse)
					paramValues = ArraysExt.rev(paramValues);
				String[] paramValuesStr = new String[paramValues.length];
				for (int i = 0; i < paramValues.length; i++)
					paramValuesStr[i] = Double.toString(paramValues[i]);
				parameterValues.put(param, paramValuesStr);
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
					if (inverse)
						paramValues = ArraysExt.rev(paramValues);
					this.iterationPerParameter[p] = paramValues.length;
					String[] paramValuesStr = new String[paramValues.length];
					for (int i = 0; i < paramValues.length; i++)
						paramValuesStr[i] = Integer.toString(paramValues[i]);
					parameterValues.put(param, paramValuesStr);
				} catch (RangeCreationException e) {
					// will never occur
				}
			} else if (param.getClass().equals(StringProgramParameter.class)) {
				StringProgramParameter paCast = (StringProgramParameter) param;
				String[] options = paCast.getOptions();
				// this.iterationPerParameter[p] = options.length;
				parameterValues.put(param, options);
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
}
