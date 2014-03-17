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
package de.clusteval.program.r;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RserveException;

import utils.SimilarityMatrix;
import de.clusteval.cluster.Clustering;
import de.clusteval.context.Context;
import de.clusteval.context.UnknownContextException;
import de.clusteval.data.DataConfig;
import de.clusteval.data.dataset.AbsoluteDataSet;
import de.clusteval.data.dataset.DataMatrix;
import de.clusteval.data.dataset.RelativeDataSet;
import de.clusteval.data.dataset.format.DataSetFormat;
import de.clusteval.data.dataset.format.UnknownDataSetFormatException;
import de.clusteval.framework.RLibraryNotLoadedException;
import de.clusteval.framework.RLibraryRequirement;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.program.Program;
import de.clusteval.program.ProgramConfig;
import de.clusteval.run.result.format.RunResultFormat;
import de.clusteval.run.result.format.UnknownRunResultFormatException;
import de.clusteval.utils.RNotAvailableException;
import file.FileUtils;

/**
 * This class is an implementation of Spectral Clustering using the R-framework
 * implementation of the package <b>kernlab</b> in method <b>specc</b>.
 * 
 * @author Christian Wiwie
 * 
 */
@RLibraryRequirement(requiredRLibraries = {"kernlab"})
public class SpectralClusteringRProgram extends RProgram {

	/**
	 * @param repository
	 * @throws RegisterException
	 */
	public SpectralClusteringRProgram(Repository repository)
			throws RegisterException {
		super(repository, new File(FileUtils.buildPath(
				repository.getBasePath(Program.class),
				"SpectralClusteringRProgram.jar")).lastModified(), new File(
				FileUtils.buildPath(repository.getBasePath(Program.class),
						"SpectralClusteringRProgram.jar")));
	}

	/**
	 * The copy constructor of Spectral clustering.
	 * 
	 * @param other
	 *            The object to clone.
	 * 
	 * @throws RegisterException
	 */
	public SpectralClusteringRProgram(SpectralClusteringRProgram other)
			throws RegisterException {
		this(other.repository);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see program.Program#getAlias()
	 */
	@Override
	public String getAlias() {
		return "Spectral Clustering";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see program.r.RProgram#getInvocationFormat()
	 */
	@Override
	public String getInvocationFormat() {
		return "specc(x,centers=%k%)";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.clusteval.program.r.RProgram#getClusterIdsFromExecResult()
	 */
	@Override
	protected float[][] getFuzzyCoeffMatrixFromExecResult()
			throws RserveException, REXPMismatchException {
		REXP result = rEngine.eval("result@.Data");
		int[] clusterIds = result.asIntegers();
		return Clustering.clusterIdsToFuzzyCoeff(clusterIds);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.clusteval.program.r.RProgram#extractDataSetContent(de.clusteval.data
	 * .DataConfig)
	 */
	@Override
	protected Object extractDataSetContent(DataConfig dataConfig) {
		boolean absoluteData = dataConfig.getDatasetConfig().getDataSet()
				.getOriginalDataSet() instanceof AbsoluteDataSet;
		Object content;
		if (absoluteData) {
			AbsoluteDataSet dataSet = (AbsoluteDataSet) (dataConfig
					.getDatasetConfig().getDataSet().getOriginalDataSet());

			DataMatrix dataMatrix = dataSet.getDataSetContent();
			this.ids = dataMatrix.getIds();
			this.x = dataMatrix.getData();
			content = dataMatrix;
		} else {
			RelativeDataSet dataSet = (RelativeDataSet) (dataConfig
					.getDatasetConfig().getDataSet().getInStandardFormat());
			SimilarityMatrix simMatrix = dataSet.getDataSetContent();
			this.ids = dataSet.getIds().toArray(new String[0]);
			this.x = simMatrix.toArray();
			content = simMatrix;
		}
		return content;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.clusteval.program.r.RProgram#beforeExec(de.clusteval.data.DataConfig,
	 * de.clusteval.program.ProgramConfig, java.lang.String[], java.util.Map,
	 * java.util.Map)
	 */
	@Override
	protected void beforeExec(DataConfig dataConfig,
			ProgramConfig programConfig, String[] invocationLine,
			Map<String, String> effectiveParams,
			Map<String, String> internalParams)
			throws RLibraryNotLoadedException, REngineException,
			RNotAvailableException {
		super.beforeExec(dataConfig, programConfig, invocationLine,
				effectiveParams, internalParams);

		boolean absoluteData = dataConfig.getDatasetConfig().getDataSet()
				.getOriginalDataSet() instanceof AbsoluteDataSet;
		if (absoluteData) {
			rEngine.assign("x", x);
			rEngine.eval("rownames(x) <- ids");
		} else {
			rEngine.assign("x", x);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.clusteval.program.r.RProgram#getCompatibleDataSetFormats()
	 */
	@Override
	public Set<DataSetFormat> getCompatibleDataSetFormats()
			throws UnknownDataSetFormatException {
		return new HashSet<DataSetFormat>(DataSetFormat.parseFromString(
				repository, new String[]{"SimMatrixDataSetFormat",
						"MatrixDataSetFormat"}));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.clusteval.program.r.RProgram#getRunResultFormat()
	 */
	@Override
	public RunResultFormat getRunResultFormat()
			throws UnknownRunResultFormatException {
		return RunResultFormat.parseFromString(repository,
				"TabSeparatedRunResultFormat");
	}

	@Override
	public Context getContext() throws UnknownContextException {
		return Context.parseFromString(repository, "ClusteringContext");
	}
}
