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
import de.clusteval.cluster.quality.ClusteringQualitySet;

import utils.ArraysExt;
import utils.Pair;
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
	protected int currentLayer;
	protected DivisiveParameterOptimizationMethod currentDivisiveMethod;
	protected List<ProgramParameter<?>> originalParameters;
	private int totalIterationCount;
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
	 * @param iterationPerParameter
	 * @param isResume
	 * @throws RegisterException
	 */
	public LayeredDivisiveParameterOptimizationMethod(final Repository repo,
			final boolean register, final long changeDate, final File absPath,
			final ParameterOptimizationRun run,
			final ProgramConfig programConfig, final DataConfig dataConfig,
			List<ProgramParameter<?>> params,
			ClusteringQualityMeasure optimizationCriterion,
			int[] iterationPerParameter, final boolean isResume)
			throws RegisterException {
		super(repo, false, changeDate, absPath, run, programConfig, dataConfig,
				params, optimizationCriterion, iterationPerParameter, isResume);
		this.originalParameters = params;
		this.totalIterationCount = (int) ArraysExt
				.sum(this.iterationPerParameter);
		this.layerCount = (int) Math.sqrt(getTotalIterationCount());
		// this.layerCount =
		// ArraysExt.toIntArray(ArraysExt.sqrt(iterationPerParameter));
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
		this.totalIterationCount = (int) ArraysExt
				.sum(this.iterationPerParameter);
		this.layerCount = (int) Math.sqrt(getTotalIterationCount());
		// this.layerCount =
		// ArraysExt.toIntArray(ArraysExt.sqrt(iterationPerParameter));
		this.paramToValueRange = new HashMap<String, Pair<?, ?>>();
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cluster.paramOptimization.ParameterOptimizationMethod#getNextParameterSet
	 * ()
	 */
	@Override
	protected ParameterSet getNextParameterSet(
			final ParameterSet forcedParameterSet)
			throws InternalAttributeException, RegisterException,
			NoParameterSetFoundException {
		if (this.currentDivisiveMethod == null
				|| (!this.currentDivisiveMethod.hasNext() && this.currentLayer < this.layerCount)) {
			this.applyNextDivisiveMethod();
		}
		ParameterSet result = this.currentDivisiveMethod.next(
				forcedParameterSet,
				this.currentDivisiveMethod.getCurrentCount() + 1);
		if (this.getResult().getParameterSets().contains(result))
			this.currentDivisiveMethod.giveQualityFeedback(this.getResult()
					.get(result));
		return result;
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
	 * 
	 */
	protected void applyNextDivisiveMethod() throws InternalAttributeException,
			RegisterException {
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
			if (this.currentDivisiveMethod != null) {
				/*
				 * In the next layer we half the domains of every parameter
				 * centered around that point with maximal quality
				 */
				double paramOptValue = this.getResult()
						.getOptimalParameterSets()
						.get(this.optimizationCriterion).get(param.getName());

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

		int[] newIterationsPerParameter;
		if (currentLayer < layerCount)
			// newIterationsPerParameter = (int) (this.iterationPerParameter /
			// (double) this.layerCount);
			newIterationsPerParameter = ArraysExt.toIntArray(ArraysExt.scaleBy(
					this.iterationPerParameter, this.layerCount));
		else
			/*
			 * If this is the last layer, do the remaining number of iterations
			 */
			// newIterationsPerParameter = this.iterationPerParameter
			// - (this.layerCount - 1)
			// * (int) (this.iterationPerParameter / (double) this.layerCount);
			newIterationsPerParameter = ArraysExt.subtract(
					this.iterationPerParameter, ArraysExt.toIntArray(ArraysExt
							.scaleBy(ArraysExt.toIntArray(ArraysExt
									.scaleBy(this.iterationPerParameter,
											this.layerCount)),
									(this.layerCount - 1), false)));
		// int newLayerIterations = newIterationsPerParameter *
		// this.params.size();
		int newLayerIterations = (int) ArraysExt.sum(newIterationsPerParameter);
		this.remainingIterationCount -= newLayerIterations;
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

	protected DivisiveParameterOptimizationMethod createDivisiveMethod(
			List<ProgramParameter<?>> newParams, int[] newIterationsPerParameter)
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
	public void giveQualityFeedback(ClusteringQualitySet qualities) {
		super.giveQualityFeedback(qualities);
		this.currentDivisiveMethod.giveQualityFeedback(qualities);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cluster.paramOptimization.ParameterOptimizationMethod#reset()
	 */
	@Override
	public void reset(final File absResultPath)
			throws ParameterOptimizationException, InternalAttributeException,
			RegisterException, RunResultParseException {
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
