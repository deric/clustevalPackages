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

import de.clusteval.cluster.Clustering;
import de.clusteval.context.Context;
import de.clusteval.context.UnknownContextException;
import de.clusteval.data.DataConfig;
import de.clusteval.data.dataset.format.DataSetFormat;
import de.clusteval.data.dataset.format.UnknownDataSetFormatException;
import de.clusteval.framework.RLibraryNotLoadedException;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.program.Program;
import de.clusteval.program.ProgramConfig;
import de.clusteval.run.result.format.RunResultFormat;
import de.clusteval.run.result.format.UnknownRunResultFormatException;
import de.clusteval.utils.RNotAvailableException;
import de.wiwie.wiutils.file.FileUtils;

/**
 * @author Christian Wiwie
 * 
 */
public class ClusterDPClusteringRProgram extends RelativeDataRProgram {

	/**
	 * @param repository
	 * @throws RegisterException
	 */
	public ClusterDPClusteringRProgram(Repository repository)
			throws RegisterException {
		super(repository, new File(FileUtils.buildPath(
				repository.getBasePath(Program.class),
				"ClusterDPClusteringRProgram.jar")).lastModified(), new File(
				FileUtils.buildPath(repository.getBasePath(Program.class),
						"ClusterDPClusteringRProgram.jar")));
	}

	/**
	 * @param rProgram
	 * @throws RegisterException
	 */
	public ClusterDPClusteringRProgram(ClusterDPClusteringRProgram rProgram)
			throws RegisterException {
		this(rProgram.repository);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.clusteval.program.r.AbsoluteAndRelativeDataRProgram#beforeExec(de.
	 * clusteval.data.DataConfig, de.clusteval.program.ProgramConfig,
	 * java.lang.String[], java.util.Map, java.util.Map)
	 */
	@Override
	protected void beforeExec(DataConfig dataConfig,
			ProgramConfig programConfig, String[] invocationLine,
			Map<String, String> effectiveParams,
			Map<String, String> internalParams)
			throws RLibraryNotLoadedException, REngineException,
			RNotAvailableException, InterruptedException {
		super.beforeExec(dataConfig, programConfig, invocationLine,
				effectiveParams, internalParams);

		// define function for this program
		this.rEngine
				.eval("clusterdp <- function(dc, k) {"
						+ "  d <- as.matrix(x);"
						+ "  ND <- nrow(d);"
						+ "  rho <- c();"
						+ "  for (  i in 1 : ND ) {"
						+ "    rho[i]<-0.;"
						+ "  };"
						+ "  for (  i in 1:(ND-1) ) {"
						+ "    for (  j in (i+1):ND ) {"
						+ "       e <- exp(-(d[i,j]/dc)*(d[i,j]/dc));"
						+ "       rho[i]<-rho[i]+e;"
						+ "       rho[j]<-rho[j]+e;"
						+ "    };"
						+ "  };"
						+ "  "
						+ "  maxd<-max(max(d));"
						+ "  "
						+ "  tmp <- sort(rho,decreasing=T, index.return=T);"
						+ "  rho.sorted <- tmp$x;"
						+ "  ordrho <- tmp$ix;"
						+ "  "
						+ "  delta <- c();"
						+ "  nneigh <- c();"
						+ "  "
						+ "  delta[ordrho[1]]<--1.;"
						+ "  nneigh[ordrho[1]]<-1;"
						+ "  "
						+ "  for (  ii in 2 : ND ) {"
						+ "     delta[ordrho[ii]]<-maxd;"
						+ "     for (  jj in 1 : (ii-1) ) {"
						+ "       if(d[ordrho[ii],ordrho[jj]]<delta[ordrho[ii]]) {"
						+ "          delta[ordrho[ii]]<-d[ordrho[ii],ordrho[jj]];"
						+ "          nneigh[ordrho[ii]]<-ordrho[jj];"
						+ "       };"
						+ "     };"
						+ "  };"
						+ "  delta[ordrho[1]]<-max(delta);"
						+ "  rhodelta <- c();"
						+ "  for (i in 1:ND) { rhodelta[i]=rho[i]*delta[i];};"
						+ "  ordrhodelta <- sort(rhodelta,decreasing=T, index.return=T);"
						+ "  cl <- c();" + "  icl <- c();"
						+ "  for (i in 1:ND) {" + "    cl[i]=-1;" + "  };"
						+ "  for (i in 1:k) {"
						+ "    cl[ordrhodelta$ix[i]] <- i;" + "      icl[i]=i;"
						+ "  };" + "  " + "  for (  i in 1 : ND ) {"
						+ "    if (cl[ordrho[i]]==-1)"
						+ "      cl[ordrho[i]]<-cl[nneigh[ordrho[i]]];"
						+ "  };" + "  result <- cl;" + "};" + "return (0);");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.clusteval.program.r.RProgram#getInvocationFormat()
	 */
	@Override
	public String getInvocationFormat() {
		return "clusterdp(%dc%, %k%)";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.clusteval.program.Program#getAlias()
	 */
	@Override
	public String getAlias() {
		return "clusterdp";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.clusteval.program.r.RProgram#getClusterIdsFromExecResult()
	 */
	@Override
	protected float[][] getFuzzyCoeffMatrixFromExecResult()
			throws RserveException, REXPMismatchException, InterruptedException {
		REXP result = rEngine.eval("result");
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
