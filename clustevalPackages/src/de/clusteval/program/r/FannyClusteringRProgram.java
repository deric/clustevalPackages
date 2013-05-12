/**
 * 
 */
package de.clusteval.program.r;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.Rserve.RserveException;

import utils.ArraysExt;
import de.clusteval.context.Context;
import de.clusteval.context.UnknownContextException;
import de.clusteval.data.dataset.format.DataSetFormat;
import de.clusteval.data.dataset.format.UnknownDataSetFormatException;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.run.result.format.RunResultFormat;
import de.clusteval.run.result.format.UnknownRunResultFormatException;
import file.FileUtils;

/**
 * @author Christian Wiwie
 * 
 */
public class FannyClusteringRProgram extends RelativeDataRProgram {

	/**
	 * @param repository
	 * @throws RegisterException
	 */
	public FannyClusteringRProgram(Repository repository)
			throws RegisterException {
		super(repository,
				new File(FileUtils.buildPath(repository.getProgramBasePath(),
						"FannyClusteringRProgram.jar")).lastModified(),
				new File(FileUtils.buildPath(repository.getProgramBasePath(),
						"FannyClusteringRProgram.jar")));
	}

	/**
	 * @param rProgram
	 * @throws RegisterException
	 */
	public FannyClusteringRProgram(FannyClusteringRProgram rProgram)
			throws RegisterException {
		this(rProgram.repository);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.clusteval.program.r.RProgram#getRequiredRlibraries()
	 */
	@Override
	public Set<String> getRequiredRlibraries() {
		return new HashSet<String>(Arrays.asList(new String[]{"cluster"}));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.clusteval.program.r.RProgram#getInvocationFormat()
	 */
	@Override
	public String getInvocationFormat() {
		return "fanny(x,k=%k%)";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.clusteval.program.Program#getAlias()
	 */
	@Override
	public String getAlias() {
		return "Fuzzy Clustering (R)";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.clusteval.program.r.RProgram#getClusterIdsFromExecResult()
	 */
	@Override
	protected float[][] getFuzzyCoeffMatrixFromExecResult()
			throws RserveException, REXPMismatchException {
		REXP result = rEngine.eval("result$membership");
		double[][] fuzzyCoeffs = result.asDoubleMatrix();
		return ArraysExt.toFloatArray(fuzzyCoeffs);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.clusteval.program.r.RProgram#getParameterValueForResultFile(java.util
	 * .Map)
	 */
	@Override
	protected String getParameterValueForResultFile(
			Map<String, String> effectiveParams) {
		return effectiveParams.get("k");
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
				repository, new String[]{"MatrixDataSetFormat"}));
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
