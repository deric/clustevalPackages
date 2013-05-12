/**
 * 
 */
package de.clusteval.cluster.paramOptimization;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import utils.ArraysExt;
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
public class APParameterOptimizationMethod
		extends
			LayeredDivisiveParameterOptimizationMethod
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
	 * @throws RegisterException
	 */
	public APParameterOptimizationMethod(final Repository repo,
			final boolean register, final long changeDate, final File absPath,
			final ParameterOptimizationRun run,
			final ProgramConfig programConfig, final DataConfig dataConfig,
			final List<ProgramParameter<?>> params,
			final ClusteringQualityMeasure optimizationCriterion,
			int[] iterationPerParameter, final boolean isResume)
			throws RegisterException {
		super(repo, false, changeDate, absPath, run, programConfig, dataConfig,
				getPreferenceParam(params), optimizationCriterion,
				new int[]{iterationPerParameter[0]}, isResume);
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
	public APParameterOptimizationMethod(
			final APParameterOptimizationMethod other) throws RegisterException {
		super(other);

		this.allParams = ProgramParameter.cloneParameterList(params);
		this.numberTriesOnNotTerminated = other.numberTriesOnNotTerminated; // TODO
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
			NoParameterSetFoundException {
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
							optimizationCriterion, ArraysExt.rep(
									this.numberTriesOnNotTerminated,
									iterationParams.size()), isResume);
					this.iterationParamMethod.reset(new File(this.getResult()
							.getAbsolutePath()));
				} catch (ParameterOptimizationException e) {
					e.printStackTrace();
				} catch (RunResultParseException e) {
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
							this.iterationParamMethod.getCurrentCount() + 1);
					preferenceParamSet = this
							.getResult()
							.getParameterSets()
							.get(this.getResult().getParameterSets().size() - 1);

					ParameterSet newParamSet = new ParameterSet();
					newParamSet.putAll(preferenceParamSet);
					newParamSet.putAll(iterationParamSet);
					return newParamSet;
				} catch (NoParameterSetFoundException e) {
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
					ArraysExt.rep(this.numberTriesOnNotTerminated,
							iterationParams.size()), isResume);
			this.iterationParamMethod.reset(new File(this.getResult()
					.getAbsolutePath()));
			iterationParamSet = this.iterationParamMethod.next(
					forcedParameterSet,
					this.iterationParamMethod.getCurrentCount() + 1);
			preferenceParamSet = super.getNextParameterSet(forcedParameterSet);
		} catch (ParameterOptimizationException e) {
			e.printStackTrace();
		} catch (RunResultParseException e) {
			e.printStackTrace();
		}
		ParameterSet newParamSet = new ParameterSet();
		newParamSet.putAll(iterationParamSet);
		newParamSet.putAll(preferenceParamSet);
		return newParamSet;
	}

	@Override
	public void giveFeedbackNotTerminated(ClusteringQualitySet minimalQualities) {
		super.giveQualityFeedback(minimalQualities);
		this.iterationParamMethod.giveQualityFeedback(minimalQualities);
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
	public void giveQualityFeedback(ClusteringQualitySet qualities) {
		super.giveQualityFeedback(qualities);
		this.lastIterationNotTerminated = false;
		this.iterationParamMethod = null;
	}
}
