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
import java.util.Set;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.Rserve.RserveException;

import de.clusteval.cluster.Clustering;
import de.clusteval.context.Context;
import de.clusteval.context.UnknownContextException;
import de.clusteval.data.dataset.format.DataSetFormat;
import de.clusteval.data.dataset.format.UnknownDataSetFormatException;
import de.clusteval.framework.RLibraryRequirement;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.program.Program;
import de.clusteval.run.result.format.RunResultFormat;
import de.clusteval.run.result.format.UnknownRunResultFormatException;
import file.FileUtils;

/**
 * This class is an implementation of Self Organizing Maps using the R-framework
 * implementation of the package <b>kohonen</b> in method <b>som</b>.
 * 
 * @author Christian Wiwie
 * 
 */
@RLibraryRequirement(requiredRLibraries = {"kohonen"})
public class SOMClusteringRProgram extends RelativeDataRProgram {

	/**
	 * @param repository
	 * @throws RegisterException
	 */
	public SOMClusteringRProgram(Repository repository)
			throws RegisterException {
		super(repository, new File(FileUtils.buildPath(
				repository.getBasePath(Program.class),
				"SOMClusteringRProgram.jar")).lastModified(), new File(
				FileUtils.buildPath(repository.getBasePath(Program.class),
						"SOMClusteringRProgram.jar")));
	}

	/**
	 * The copy constructor of SOM.
	 * 
	 * @param other
	 *            The object to clone.
	 * 
	 * @throws RegisterException
	 */
	public SOMClusteringRProgram(SOMClusteringRProgram other)
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
		return "Self Organizing Maps";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see program.r.RProgram#getInvocationFormat()
	 */
	@Override
	public String getInvocationFormat() {
		return "som(x,keep.data=T,grid=somgrid(%x%,%y%))";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.clusteval.program.r.RProgram#getClusterIdsFromExecResult()
	 */
	@Override
	protected float[][] getFuzzyCoeffMatrixFromExecResult()
			throws RserveException, REXPMismatchException {
		REXP result = rEngine.eval("result$unit.classif");
		int[] clusterIds = result.asIntegers();
		return Clustering.clusterIdsToFuzzyCoeff(clusterIds);
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
				repository, new String[]{"SimMatrixDataSetFormat"}));
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
