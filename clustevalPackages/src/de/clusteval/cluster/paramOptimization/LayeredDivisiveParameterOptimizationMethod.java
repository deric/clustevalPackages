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
import java.util.List;
import java.util.Map;

import utils.Pair;
import de.clusteval.cluster.quality.ClusteringQualityMeasure;
import de.clusteval.cluster.quality.ClusteringQualitySet;
import de.clusteval.data.DataConfig;
import de.clusteval.data.dataset.format.DataSetFormat;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.program.DoubleProgramParameter;
import de.clusteval.program.IntegerProgramParameter;
import de.clusteval.program.ParameterSet;
import de.clusteval.program.ProgramConfig;
import de.clusteval.program.ProgramParameter;
import de.clusteval.program.StringProgramParameter;
import de.clusteval.run.ParameterOptimizationRun;
import de.clusteval.run.result.RunResultParseException;
import de.clusteval.utils.InternalAttributeException;

/**
 * @author Christian Wiwie
 * 
 */
@LoadableClassParentAnnotation(parent = "DivisiveParameterOptimizationMethod")
public class LayeredDivisiveParameterOptimizationMethod
		extends
			ParameterOptimizationMethod {

	protected int remainingIterationCount;

	/**
	 * The number of layers.
	 */
	protected int layerCount;
	protected int iterationsPerLayer;
	protected int currentLayer;
	protected DivisiveParameterOptimizationMethod currentDivisiveMethod;
	protected List<ProgramParameter<?>> originalParameters;
	// private int totalIterationCount;
	protected Map<String, Pair<?, ?>> paramToValueRange;

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
	 * @param totalIterations
	 * @param isResume
	 * @throws RegisterException
	 */
	public LayeredDivisiveParameterOptimizationMethod(final Repository repo,
			final boolean register, final long changeDate, final File absPath,
			final ParameterOptimizationRun run,
			final ProgramConfig programConfig, final DataConfig dataConfig,
			List<ProgramParameter<?>> params,
			ClusteringQualityMeasure optimizationCriterion,
			int totalIterations, final boolean isResume)
			throws RegisterException {
		super(repo, false, changeDate, absPath, run, programConfig, dataConfig,
				params, optimizationCriterion, totalIterations, isResume);
		this.originalParameters = params;
		// this.totalIterationCount = (int) ArraysExt
		// .product(this.iterationPerParameter);
		this.layerCount = (int) Math.sqrt(this.totalIterationCount);
		// this.iterationsPerLayer = this.totalIterationCount / this.layerCount;
		// this.layerCount = (int) Math
		// .round(Math.log10(this.totalIterationCount));
		this.iterationsPerLayer = this.totalIterationCount / this.layerCount;
		this.paramToValueRange = new HashMap<String, Pair<?, ?>>();

		if (register)
			this.register();
	}

	/**
	 * The copy constructor for this method.
	 * 
	 * <p>
	 * Cloning of this method does not keep potentially already initialized
	 * parameter value ranges
	 * 
	 * @param other
	 *            The object to clone.
	 * @throws RegisterException
	 */
	public LayeredDivisiveParameterOptimizationMethod(
			final LayeredDivisiveParameterOptimizationMethod other)
			throws RegisterException {
		super(other);

		this.originalParameters = ProgramParameter
				.cloneParameterList(other.params);
		// this.totalIterationCount = (int) ArraysExt
		// .product(this.iterationPerParameter);
		// this.layerCount = (int) Math.sqrt(this.iterationPerParameter[0]);
		// this.iterationsPerLayer = this.totalIterationCount / this.layerCount;
		this.layerCount = other.layerCount;
		this.iterationsPerLayer = other.iterationsPerLayer;
		this.paramToValueRange = new HashMap<String, Pair<?, ?>>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cluster.paramOptimization.ParameterOptimizationMethod#getNextParameterSet
	 * ()
	 */
	@Override
	protected synchronized ParameterSet getNextParameterSet(
			final ParameterSet forcedParameterSet)
			throws InternalAttributeException, RegisterException,
			NoParameterSetFoundException, InterruptedException,
			ParameterSetAlreadyEvaluatedException {
		if (this.currentDivisiveMethod == null
				|| (!this.currentDivisiveMethod.hasNext() && this.currentLayer < this.layerCount)) {
			boolean allParamSetsFinished = false;
			while (!allParamSetsFinished) {
				allParamSetsFinished = true;
				for (ParameterSet set : this.getResult().getParameterSets())
					if (this.getResult().get(set) == null) {
						allParamSetsFinished = false;
						// this.log.warn("null parameter set: " + set);
						this.wait();
					}
			}
			this.applyNextDivisiveMethod();
		}
		try {
			ParameterSet result = this.currentDivisiveMethod.next(
					forcedParameterSet,
					this.currentDivisiveMethod.getCurrentCount() + 1);

			if (this.getResult().getParameterSets().contains(result))
				this.currentDivisiveMethod.giveQualityFeedback(result, this
						.getResult().get(result));
			return result;
		} catch (ParameterSetAlreadyEvaluatedException e) {
			// 09.05.2014: we have to adapt the iteration number of the current
			// divisive method to the iteration number of this layered method
			throw new ParameterSetAlreadyEvaluatedException(
					++this.currentCount, this.getResult()
							.getIterationNumberForParameterSet(
									e.getParameterSet()), e.getParameterSet());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cluster.paramOptimization.ParameterOptimizationMethod#initParameterValues
	 * ()
	 */
	@Override
	protected void initParameterValues() throws ParameterOptimizationException,
			InternalAttributeException {
		super.initParameterValues();

		for (ProgramParameter<?> param : params)
			this.paramToValueRange.put(param.getName(), Pair.getPair(
					param.evaluateMinValue(dataConfig, programConfig),
					param.evaluateMaxValue(dataConfig, programConfig)));
	}

	/**
	 * @throws InternalAttributeException
	 * @throws RegisterException
	 * @throws InterruptedException
	 * 
	 */
	protected void applyNextDivisiveMethod() throws InternalAttributeException,
			RegisterException, InterruptedException {
		/*
		 * First we take the new optimum of the last divisive layer, if there
		 * was one
		 */
		if (this.currentDivisiveMethod != null) {
			for (ClusteringQualityMeasure measure : this.currentDivisiveMethod
					.getResult().getOptimalCriterionValue().keySet()) {
				if (measure.isBetterThan(this.currentDivisiveMethod.getResult()
						.getOptimalCriterionValue().get(measure), this
						.getResult().getOptimalCriterionValue().get(measure))) {
					this.getResult()
							.getOptimalCriterionValue()
							.put(measure,
									this.currentDivisiveMethod.getResult()
											.getOptimalCriterionValue()
											.get(measure));
					this.getResult()
							.getOptimalParameterSets()
							.put(measure,
									this.currentDivisiveMethod.getResult()
											.getOptimalParameterSets()
											.get(measure));
				}
			}
		}
		/*
		 * We adapt the ranges of the parameters to control which points the
		 * divisive method evaluates
		 */
		List<ProgramParameter<?>> newParams = new ArrayList<ProgramParameter<?>>();
		for (ProgramParameter<?> p : params) {
			ProgramParameter<?> param = p.clone();
			newParams.add(param);
			// if this is the first layer or the parameter is a string parameter
			// with options, we do not change the value range
			if (this.currentDivisiveMethod != null
					&& !(param instanceof StringProgramParameter && param
							.isOptionsSet())) {
				/*
				 * In the next layer we half the domains of every parameter
				 * centered around that point with maximal quality
				 */
				double paramOptValue = Double.valueOf(this.getResult()
						.getOptimalParameterSets()
						.get(this.optimizationCriterion).get(param.getName()));

				double oldMinValue;
				double oldMaxValue;

				try {
					if (param instanceof DoubleProgramParameter)
						oldMinValue = (Double) (paramToValueRange.get(param
								.getName()).getFirst());
					else
						oldMinValue = (Integer) (paramToValueRange.get(param
								.getName()).getFirst());
					if (param instanceof DoubleProgramParameter)
						oldMaxValue = (Double) (paramToValueRange.get(param
								.getName()).getSecond());
					else
						oldMaxValue = (Integer) (paramToValueRange.get(param
								.getName()).getSecond());
				} catch (ClassCastException e) {
					System.out.println(param);
					System.out.println(paramToValueRange.get(param.getName()));
					System.out.println(paramToValueRange.get(param.getName())
							.getFirst().getClass()
							+ " "
							+ paramToValueRange.get(param.getName())
									.getSecond().getClass());
					System.out.println(paramToValueRange.get(param.getName())
							.getFirst());
					throw e;
				}

				double oldRange = oldMaxValue - oldMinValue;

				double newMinValue = paramOptValue - oldRange / 4;
				double newMaxValue = paramOptValue + oldRange / 4;

				double origParamMinValue;
				if (param instanceof DoubleProgramParameter)
					origParamMinValue = ((DoubleProgramParameter) param)
							.evaluateMinValue(dataConfig, programConfig);
				else
					origParamMinValue = ((IntegerProgramParameter) param)
							.evaluateMinValue(dataConfig, programConfig);
				double origParamMaxValue;
				if (param instanceof DoubleProgramParameter)
					origParamMaxValue = ((DoubleProgramParameter) param)
							.evaluateMaxValue(dataConfig, programConfig);
				else
					origParamMaxValue = ((IntegerProgramParameter) param)
							.evaluateMaxValue(dataConfig, programConfig);

				/*
				 * If we are outside the old minvalue - maxvalue range, we shift
				 * the new range.
				 */
				if (newMinValue < origParamMinValue) {
					// newMaxValue += (origParamMinValue - newMinValue);
					newMinValue += (origParamMinValue - newMinValue);
				} else if (newMaxValue > origParamMaxValue) {
					// newMinValue -= (newMaxValue - origParamMaxValue);
					newMaxValue -= (newMaxValue - origParamMaxValue);
				}

				if (param.getClass().equals(DoubleProgramParameter.class)) {
					paramToValueRange.put(param.getName(),
							Pair.getPair(newMinValue, newMaxValue));

					param.setMinValue(newMinValue + "");
					param.setMaxValue(newMaxValue + "");
					param.setDefault(newMinValue + "");
				} else if (param.getClass().equals(
						IntegerProgramParameter.class)) {
					paramToValueRange.put(param.getName(),
							Pair.getPair((int) newMinValue, (int) newMaxValue));

					param.setMinValue(newMinValue + "");
					param.setMaxValue(newMaxValue + "");
					param.setDefault(newMinValue + "");
				}
			}
			/*
			 * If this is the first layer, we just operate on the whole ranges
			 * of the parameters ( do not change them here)
			 */
		}

		int newIterationsPerParameter = getNextIterationsPerLayer();
		try {
			this.currentDivisiveMethod = createDivisiveMethod(newParams,
					newIterationsPerParameter);
			this.currentDivisiveMethod.reset(new File(this.getResult()
					.getAbsolutePath()));
		} catch (ParameterOptimizationException e) {
			e.printStackTrace();
		} catch (RunResultParseException e) {
			e.printStackTrace();
		}
		this.currentLayer++;
	}

	protected int getNextIterationsPerLayer() {
		int newLayerIterations;

		double remainingIterationCount = this.iterationsPerLayer;
		int remainingParams = this.params.size();
		final List<Integer> iterations = new ArrayList<Integer>();

		// parameters that have a fixed number of options
		for (int i = 0; i < params.size(); i++) {
			final ProgramParameter<?> param = this.params.get(i);
			if (param.getOptions() != null && param.getOptions().length > 0) {
				iterations.add(param.getOptions().length);
				remainingIterationCount /= param.getOptions().length;
				remainingParams--;
			}
		}

		// the iterations for the remaining parameters
		newLayerIterations = (int) Math.pow(Math.floor(Math.pow(
				remainingIterationCount, 1.0 / remainingParams)),
				remainingParams);
		for (Integer i : iterations)
			newLayerIterations *= i;

		if (currentLayer < layerCount - 1) {
			this.remainingIterationCount -= newLayerIterations;
		} else {
			/*
			 * If this is the last layer, do the remaining number of iterations
			 */
			this.remainingIterationCount = 0;
		}
		return newLayerIterations;
	}

	protected DivisiveParameterOptimizationMethod createDivisiveMethod(
			List<ProgramParameter<?>> newParams, int newIterationsPerParameter)
			throws ParameterOptimizationException, RegisterException {
		return new DivisiveParameterOptimizationMethod(repository, false,
				System.currentTimeMillis(), new File(
						"DivisiveParameterOptimizationMethod"), run,
				programConfig, dataConfig, newParams, optimizationCriterion,
				newIterationsPerParameter, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cluster.paramOptimization.ParameterOptimizationMethod#hasNext()
	 */
	@Override
	public boolean hasNext() {
		boolean layerHasNext = (this.currentDivisiveMethod != null
				? this.currentDivisiveMethod.hasNext()
				: false);
		if (!layerHasNext) {
			return this.currentLayer < this.layerCount;
		}
		return layerHasNext;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cluster.paramOptimization.ParameterOptimizationMethod#giveQualityFeedback
	 * (java.util.Map)
	 */
	@Override
	public synchronized void giveQualityFeedback(final ParameterSet paramSet,
			ClusteringQualitySet qualities) {
		try {
			super.giveQualityFeedback(paramSet, qualities);
			this.currentDivisiveMethod.giveQualityFeedback(paramSet, qualities);
			// wake up all threads, which are waiting for the parameter sets of
			// the
			// last divisive method to finish.
		} finally {
			this.notifyAll();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cluster.paramOptimization.ParameterOptimizationMethod#reset()
	 */
	@Override
	public void reset(final File absResultPath)
			throws ParameterOptimizationException, InternalAttributeException,
			RegisterException, RunResultParseException, InterruptedException {
		this.currentLayer = 0;
		this.remainingIterationCount = getTotalIterationCount();
		if (this.originalParameters != null)
			this.params = this.originalParameters;
		this.currentDivisiveMethod = null;
		super.reset(absResultPath);
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
		return this.totalIterationCount;
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
