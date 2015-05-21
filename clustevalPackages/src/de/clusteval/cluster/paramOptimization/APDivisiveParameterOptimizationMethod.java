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
import java.util.ArrayList;
import java.util.List;

import de.clusteval.cluster.quality.ClusteringQualityMeasure;
import de.clusteval.cluster.quality.ClusteringQualitySet;
import de.clusteval.data.DataConfig;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.program.ParameterSet;
import de.clusteval.program.ProgramConfig;
import de.clusteval.program.ProgramParameter;
import de.clusteval.run.ParameterOptimizationRun;
import de.clusteval.run.result.RunResultParseException;
import de.clusteval.utils.InternalAttributeException;

/**
 * @author Christian Wiwie
 * 
 */
@LoadableClassParentAnnotation(parent = "DivisiveParameterOptimizationMethod")
public class APDivisiveParameterOptimizationMethod
		extends
			DivisiveParameterOptimizationMethod
		implements
			IDivergingParameterOptimizationMethod {

	protected boolean lastIterationNotTerminated;
	protected int numberTriesOnNotTerminated;
	protected DivisiveParameterOptimizationMethod iterationParamMethod;
	protected List<ProgramParameter<?>> allParams;

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
	 * @param iterationPerParameter
	 *            This array holds the number of iterations that are to be
	 *            performed for each optimization parameter.
	 * @param isResume
	 *            This boolean indiciates, whether the run is a resumption of a
	 *            previous run execution or a completely new execution.
	 * @throws ParameterOptimizationException
	 * @throws RegisterException
	 */
	public APDivisiveParameterOptimizationMethod(final Repository repo,
			final boolean register, final long changeDate, final File absPath,
			final ParameterOptimizationRun run, ProgramConfig programConfig,
			DataConfig dataConfig, List<ProgramParameter<?>> params,
			ClusteringQualityMeasure optimizationCriterion,
			int iterationPerParameter, final boolean isResume)
			throws ParameterOptimizationException, RegisterException {
		super(repo, false, changeDate, absPath, run, programConfig, dataConfig,
				getPreferenceParam(params), optimizationCriterion,
				iterationPerParameter,
				// TODO: why?
				// new int[]{iterationPerParameter[0]},
				isResume);
		this.allParams = params;
		this.numberTriesOnNotTerminated = 3; // TODO

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
	public APDivisiveParameterOptimizationMethod(
			final APDivisiveParameterOptimizationMethod other)
			throws RegisterException {
		super(other);

		this.allParams = ProgramParameter.cloneParameterList(other.allParams);
		this.numberTriesOnNotTerminated = other.numberTriesOnNotTerminated;
	}

	/**
	 * @param params
	 * @return A list containing only the preference parameter.
	 */
	public static List<ProgramParameter<?>> getPreferenceParam(
			List<ProgramParameter<?>> params) {
		/*
		 * Only add the preference-parameter to this list
		 */
		List<ProgramParameter<?>> result = new ArrayList<ProgramParameter<?>>();
		for (ProgramParameter<?> param : params)
			if (param.getName().equals("preference")) {
				result.add(param);
				break;
			}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cluster.paramOptimization.LayeredDivisiveParameterOptimizationMethod#
	 * hasNext()
	 */
	@Override
	public boolean hasNext() {
		boolean hasNext = super.hasNext();
		if (this.lastIterationNotTerminated) {
			if (this.iterationParamMethod.hasNext()) {
				return true;
			}
		}
		return hasNext;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cluster.paramOptimization.LayeredDivisiveParameterOptimizationMethod#
	 * getNextParameterSet()
	 */
	@Override
	protected ParameterSet getNextParameterSet(
			final ParameterSet forcedParameterSet)
			throws InternalAttributeException, RegisterException,
			NoParameterSetFoundException, InterruptedException {
		ParameterSet iterationParamSet = null;
		ParameterSet preferenceParamSet = null;

		if (this.lastIterationNotTerminated) {

			/*
			 * We ensure that we have a method for the iteration parameters
			 */
			if (this.iterationParamMethod == null) {
				/*
				 * If we do not have another iteration parameter, we create our
				 * next preference parameter set.
				 */
				try {
					List<ProgramParameter<?>> iterationParams = new ArrayList<ProgramParameter<?>>(
							this.allParams);
					iterationParams.removeAll(this.params);
					this.iterationParamMethod = new DivisiveParameterOptimizationMethod(
							repository, false, changeDate, absPath, run,
							programConfig, dataConfig, iterationParams,
							optimizationCriterion, (int) Math.pow(
									this.numberTriesOnNotTerminated,
									iterationParams.size()), isResume);
				} catch (ParameterOptimizationException e) {
					e.printStackTrace();
				}
			}
			/*
			 * If we have another iteration parameter set we just merge it with
			 * our current one.
			 */
			if (this.iterationParamMethod.hasNext()) {
				try {
					iterationParamSet = this.iterationParamMethod.next(
							forcedParameterSet,
							this.iterationParamMethod.getStartedCount() + 1);
					preferenceParamSet = this
							.getResult()
							.getParameterSets()
							.get(this.getResult().getParameterSets().size() - 1);

					ParameterSet newParamSet = new ParameterSet();
					newParamSet.putAll(preferenceParamSet);
					newParamSet.putAll(iterationParamSet);
					return newParamSet;
				} catch (NoParameterSetFoundException e) {
				} catch (ParameterSetAlreadyEvaluatedException e) {
					// cannot happen
				}
			}
		}

		/*
		 * The last iteration terminated or we have no other iteration parameter
		 * left.
		 */
		try {
			List<ProgramParameter<?>> iterationParams = new ArrayList<ProgramParameter<?>>(
					this.allParams);
			iterationParams.removeAll(this.params);
			this.iterationParamMethod = new DivisiveParameterOptimizationMethod(
					repository, false, changeDate, absPath, run, programConfig,
					dataConfig, iterationParams, optimizationCriterion,
					(int) Math.pow(this.numberTriesOnNotTerminated,
							iterationParams.size()), isResume);
			this.iterationParamMethod.reset(new File(this.getResult()
					.getAbsolutePath()));
			iterationParamSet = this.iterationParamMethod.next(
					forcedParameterSet,
					this.iterationParamMethod.getStartedCount() + 1);
			preferenceParamSet = super.getNextParameterSet(forcedParameterSet);
		} catch (ParameterOptimizationException e) {
			e.printStackTrace();
		} catch (RunResultParseException e) {
			e.printStackTrace();
		} catch (ParameterSetAlreadyEvaluatedException e) {
			// cannot happen
		}
		ParameterSet newParamSet = new ParameterSet();
		newParamSet.putAll(iterationParamSet);
		newParamSet.putAll(preferenceParamSet);
		return newParamSet;
	}

	@Override
	public void giveFeedbackNotTerminated(final ParameterSet parameterSet,
			ClusteringQualitySet minimalQualities) {
		super.giveQualityFeedback(parameterSet, minimalQualities);
		this.iterationParamMethod.giveQualityFeedback(parameterSet,
				minimalQualities);
		lastIterationNotTerminated = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cluster.paramOptimization.LayeredDivisiveParameterOptimizationMethod#
	 * giveQualityFeedback(cluster.quality.ClusteringQualitySet)
	 */
	@Override
	public void giveQualityFeedback(final ParameterSet parameterSet,
			ClusteringQualitySet qualities) {
		super.giveQualityFeedback(parameterSet, qualities);
		this.lastIterationNotTerminated = false;
		this.iterationParamMethod = null;
	}
}
