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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RserveException;

import de.clusteval.cluster.quality.ClusteringQualityMeasure;
import de.clusteval.data.DataConfig;
import de.clusteval.data.dataset.AbsoluteDataSet;
import de.clusteval.data.dataset.format.AbsoluteDataSetFormat;
import de.clusteval.data.dataset.format.DataSetFormat;
import de.clusteval.data.dataset.format.InvalidDataSetFormatVersionException;
import de.clusteval.data.dataset.format.UnknownDataSetFormatException;
import de.clusteval.framework.RLibraryRequirement;
import de.clusteval.framework.repository.MyRengine;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.program.ParameterSet;
import de.clusteval.program.ProgramConfig;
import de.clusteval.program.ProgramParameter;
import de.clusteval.run.ParameterOptimizationRun;

/**
 * 
 * TODO: This parameter optimization method does not support resumption of old
 * values, because the forced parameter set is ignored.
 * 
 * @author Christian Wiwie
 * 
 */
@RLibraryRequirement(requiredRLibraries = {"cluster"})
public class GapStatisticParameterOptimizationMethod
		extends
			ParameterOptimizationMethod {

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
	public GapStatisticParameterOptimizationMethod(final Repository repo,
			final boolean register, final long changeDate, final File absPath,
			ParameterOptimizationRun run, ProgramConfig programConfig,
			DataConfig dataConfig, List<ProgramParameter<?>> params,
			ClusteringQualityMeasure optimizationCriterion,
			int iterationPerParameter, boolean isResume)
			throws RegisterException {
		super(repo, false, changeDate, absPath, run, programConfig, dataConfig,
				params, optimizationCriterion, iterationPerParameter, isResume);

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
	public GapStatisticParameterOptimizationMethod(
			final GapStatisticParameterOptimizationMethod other)
			throws RegisterException {
		super(other);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cluster.paramOptimization.ParameterOptimizationMethod#getNextParameterSet
	 * (program.ParameterSet)
	 */
	@SuppressWarnings("unused")
	@Override
	protected ParameterSet getNextParameterSet(ParameterSet forcedParameterSet)
			throws InterruptedException {

		AbsoluteDataSet dataSet = (AbsoluteDataSet) (dataConfig
				.getDatasetConfig().getDataSet().getOriginalDataSet());

		try {
			MyRengine rEngine = repository.getRengineForCurrentThread();
			try {
				dataSet.loadIntoMemory();
				double[][] coords = dataSet.getDataSetContent().getData();
				List<String> ids = dataSet.getIds();
				rEngine.assign("ids", ids.toArray(new String[0]));
				dataSet.unloadFromMemory();
				rEngine.eval("x <- c()");
				for (int i = 0; i < coords.length; i++) {
					rEngine.assign("x_" + i, coords[i]);
					rEngine.eval("x <- rbind(x, x_" + i + ")");
					rEngine.eval("remove(x_" + i + ")");
				}
				rEngine.eval("rownames(x) <- ids");
				rEngine.eval("library(cluster)");
				rEngine.eval("result <- clusGap(x, FUNcluster=kmeans, K.max=10, B=100)");
				rEngine.eval("result_tab <- cbind(result$Tab,result$Tab[,3]-result$Tab[,4])");
				REXP result = rEngine
						.eval("maxSE(f=result_tab[,3],SE.f=result_tab[,4])");
				int noOfClusters = result.asInteger();

				ParameterSet res = new ParameterSet();
				res.put("k", (double) noOfClusters + "");

				return res;

			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvalidDataSetFormatVersionException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (REngineException e) {
				e.printStackTrace();
			} catch (REXPMismatchException e) {
				e.printStackTrace();
			} catch (UnknownDataSetFormatException e) {
				e.printStackTrace();
			}
		} catch (RserveException e) {
			e.printStackTrace();
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cluster.paramOptimization.ParameterOptimizationMethod#hasNext()
	 */
	@Override
	public boolean hasNext() {
		return currentCount < getTotalIterationCount();
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
		return 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cluster.paramOptimization.ParameterOptimizationMethod#
	 * getCompatibleDataSetFormatBaseClasses()
	 */
	@Override
	public List<Class<? extends DataSetFormat>> getCompatibleDataSetFormatBaseClasses() {
		List<Class<? extends DataSetFormat>> result = new ArrayList<Class<? extends DataSetFormat>>();
		result.add(AbsoluteDataSetFormat.class);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cluster.paramOptimization.ParameterOptimizationMethod#
	 * getCompatibleProgramClasses()
	 */
	@Override
	public List<String> getCompatibleProgramNames() {
		List<String> result = new ArrayList<String>();
		result.add("FannyClusteringRProgram");
		result.add("KMeansClusteringRProgram");
		result.add("HierarchicalClusteringRProgram");
		result.add("SpectralClusteringRProgram");
		return result;
	}
}
