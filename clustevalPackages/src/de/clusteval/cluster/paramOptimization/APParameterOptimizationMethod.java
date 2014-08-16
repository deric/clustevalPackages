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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
@LoadableClassParentAnnotation(parent = "LayeredDivisiveParameterOptimizationMethod")
public class APParameterOptimizationMethod
		extends
			LayeredDivisiveParameterOptimizationMethod
		implements
			IDivergingParameterOptimizationMethod {

	protected int numberTriesOnNotTerminated;
	protected Map<ParameterSet, DivisiveParameterOptimizationMethod> iterationParamMethods;
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
	 * @throws RegisterException
	 */
	public APParameterOptimizationMethod(final Repository repo,
			final boolean register, final long changeDate, final File absPath,
			final ParameterOptimizationRun run,
			final ProgramConfig programConfig, final DataConfig dataConfig,
			final List<ProgramParameter<?>> params,
			final ClusteringQualityMeasure optimizationCriterion,
			int iterationPerParameter, final boolean isResume)
			throws RegisterException {
		super(repo, false, changeDate, absPath, run, programConfig, dataConfig,
				getPreferenceParam(params), optimizationCriterion,
				// TODO: why?
				// new int[]{iterationPerParameter[0]},
				iterationPerParameter, isResume);
		this.allParams = params;
		this.numberTriesOnNotTerminated = 3; // TODO
		this.iterationParamMethods = new HashMap<ParameterSet, DivisiveParameterOptimizationMethod>();

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
	public APParameterOptimizationMethod(
			final APParameterOptimizationMethod other) throws RegisterException {
		super(other);

		this.allParams = ProgramParameter.cloneParameterList(params);
		this.numberTriesOnNotTerminated = other.numberTriesOnNotTerminated; // TODO
		this.iterationParamMethods = new HashMap<ParameterSet, DivisiveParameterOptimizationMethod>(
				other.iterationParamMethods);
	}

	/**
	 * @param params
	 *            The complete list of optimization parameters
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
	public synchronized boolean hasNext() {
		boolean hasNext = super.hasNext();
		for (DivisiveParameterOptimizationMethod method : this.iterationParamMethods
				.values())
			if (method.hasNext()) {
				return true;
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
	protected synchronized ParameterSet getNextParameterSet(
			final ParameterSet forcedParameterSet)
			throws InternalAttributeException, RegisterException,
			NoParameterSetFoundException, InterruptedException,
			ParameterSetAlreadyEvaluatedException {
		ParameterSet iterationParamSet = null;
		ParameterSet preferenceParamSet = null;

		// if there are old parameters which didn't terminate, first finish
		// those with more iterations
		Iterator<ParameterSet> existingMethods = this.iterationParamMethods
				.keySet().iterator();
		while (existingMethods.hasNext()) {
			ParameterSet notTerminatedParameterSet = existingMethods.next();
			DivisiveParameterOptimizationMethod method = this.iterationParamMethods
					.get(notTerminatedParameterSet);

			/*
			 * If we have another iteration parameter set we just merge it with
			 * our current one.
			 */
			if (method.hasNext()) {
				try {
					iterationParamSet = method.next(forcedParameterSet,
							method.getCurrentCount() + 1);
					preferenceParamSet = notTerminatedParameterSet;

					ParameterSet newParamSet = new ParameterSet();
					newParamSet.putAll(preferenceParamSet);
					newParamSet.putAll(iterationParamSet);
					return newParamSet;
				} catch (NoParameterSetFoundException e) {
				} catch (ParameterSetAlreadyEvaluatedException e) {
					// cannot happen
				}
			} else
				this.iterationParamMethods.remove(notTerminatedParameterSet);
		}

		DivisiveParameterOptimizationMethod method = null;

		// at this point we know, that no old parameter sets exist we can or
		// have to finish, so we create a new one
		try {
			List<ProgramParameter<?>> iterationParams = new ArrayList<ProgramParameter<?>>(
					this.allParams);
			iterationParams.removeAll(this.params);
			method = new DivisiveParameterOptimizationMethod(repository, false,
					changeDate, absPath, run, programConfig, dataConfig,
					iterationParams, optimizationCriterion, (int) Math.pow(
							this.numberTriesOnNotTerminated,
							iterationParams.size()), isResume);
			method.reset(new File(this.getResult().getAbsolutePath()));
			iterationParamSet = method.next(forcedParameterSet,
					method.getCurrentCount() + 1);
			preferenceParamSet = super.getNextParameterSet(forcedParameterSet);
		} catch (ParameterOptimizationException e) {
			e.printStackTrace();
		} catch (RunResultParseException e) {
			e.printStackTrace();
		}
		ParameterSet newParamSet = new ParameterSet();
		newParamSet.putAll(iterationParamSet);
		newParamSet.putAll(preferenceParamSet);

		this.iterationParamMethods.put(newParamSet, method);

		return newParamSet;
	}

	@Override
	public synchronized void giveFeedbackNotTerminated(
			final ParameterSet parameterSet,
			ClusteringQualitySet minimalQualities) {
		super.giveQualityFeedback(parameterSet, minimalQualities);

		// we don't have a param method for this parameter set if we discovered
		// earlier, that there are no new possible iteration params
		if (this.iterationParamMethods.containsKey(parameterSet))
			this.iterationParamMethods.get(parameterSet).giveQualityFeedback(
					parameterSet, minimalQualities);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cluster.paramOptimization.LayeredDivisiveParameterOptimizationMethod#
	 * giveQualityFeedback(cluster.quality.ClusteringQualitySet)
	 */
	@Override
	public synchronized void giveQualityFeedback(
			final ParameterSet parameterSet, ClusteringQualitySet qualities) {
		super.giveQualityFeedback(parameterSet, qualities);
		this.iterationParamMethods.remove(parameterSet);
	}
}
