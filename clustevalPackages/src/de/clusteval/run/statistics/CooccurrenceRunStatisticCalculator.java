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
package de.clusteval.run.statistics;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.ConfigurationException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RserveException;

import utils.ArraysExt;
import cern.colt.matrix.tlong.LongMatrix1D;
import cern.colt.matrix.tlong.LongMatrix1DProcedure;
import cern.colt.matrix.tlong.LongMatrix2D;
import cern.colt.matrix.tlong.impl.SparseLongMatrix2D;
import de.clusteval.cluster.Cluster;
import de.clusteval.cluster.ClusterItem;
import de.clusteval.cluster.Clustering;
import de.clusteval.cluster.paramOptimization.IncompatibleParameterOptimizationMethodException;
import de.clusteval.cluster.paramOptimization.InvalidOptimizationParameterException;
import de.clusteval.cluster.paramOptimization.UnknownParameterOptimizationMethodException;
import de.clusteval.cluster.quality.UnknownClusteringQualityMeasureException;
import de.clusteval.context.IncompatibleContextException;
import de.clusteval.context.UnknownContextException;
import de.clusteval.data.DataConfigNotFoundException;
import de.clusteval.data.DataConfigurationException;
import de.clusteval.data.dataset.DataSetConfigNotFoundException;
import de.clusteval.data.dataset.DataSetConfigurationException;
import de.clusteval.data.dataset.DataSetNotFoundException;
import de.clusteval.data.dataset.IncompatibleDataSetConfigPreprocessorException;
import de.clusteval.data.dataset.NoDataSetException;
import de.clusteval.data.dataset.format.UnknownDataSetFormatException;
import de.clusteval.data.dataset.type.UnknownDataSetTypeException;
import de.clusteval.data.distance.UnknownDistanceMeasureException;
import de.clusteval.data.goldstandard.GoldStandardConfigNotFoundException;
import de.clusteval.data.goldstandard.GoldStandardConfigurationException;
import de.clusteval.data.goldstandard.GoldStandardNotFoundException;
import de.clusteval.data.goldstandard.format.UnknownGoldStandardFormatException;
import de.clusteval.data.preprocessing.UnknownDataPreprocessorException;
import de.clusteval.data.statistics.UnknownDataStatisticException;
import de.clusteval.framework.repository.InvalidRepositoryException;
import de.clusteval.framework.repository.MyRengine;
import de.clusteval.framework.repository.NoRepositoryFoundException;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.framework.repository.RepositoryAlreadyExistsException;
import de.clusteval.framework.repository.config.RepositoryConfigNotFoundException;
import de.clusteval.framework.repository.config.RepositoryConfigurationException;
import de.clusteval.program.NoOptimizableProgramParameterException;
import de.clusteval.program.ParameterSet;
import de.clusteval.program.UnknownParameterType;
import de.clusteval.program.UnknownProgramParameterException;
import de.clusteval.program.UnknownProgramTypeException;
import de.clusteval.program.r.UnknownRProgramException;
import de.clusteval.run.InvalidRunModeException;
import de.clusteval.run.RunException;
import de.clusteval.run.result.ParameterOptimizationResult;
import de.clusteval.run.result.RunResult;
import de.clusteval.run.result.RunResultParseException;
import de.clusteval.run.result.format.UnknownRunResultFormatException;
import de.clusteval.utils.InvalidConfigurationFileException;
import file.FileUtils;

/**
 * @author Christian Wiwie
 * 
 */
public class CooccurrenceRunStatisticCalculator
		extends
			RunStatisticCalculator<CooccurrenceRunStatistic> {

	/**
	 * @param repository
	 * @param changeDate
	 * @param absPath
	 * @param uniqueRunIdentifier
	 * @throws RegisterException
	 */
	public CooccurrenceRunStatisticCalculator(Repository repository,
			long changeDate, File absPath, final String uniqueRunIdentifier)
			throws RegisterException {
		super(repository, changeDate, absPath, uniqueRunIdentifier);
	}

	/**
	 * The copy constructor for this statistic calculator.
	 * 
	 * @param other
	 *            The object to clone.
	 * @throws RegisterException
	 */
	public CooccurrenceRunStatisticCalculator(
			final CooccurrenceRunStatisticCalculator other)
			throws RegisterException {
		super(other);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see run.statistics.RunStatisticCalculator#calculateResult()
	 */
	@Override
	protected CooccurrenceRunStatistic calculateResult()
			throws UnknownGoldStandardFormatException,
			UnknownDataSetFormatException, IllegalArgumentException,
			IOException, GoldStandardConfigurationException,
			DataSetConfigurationException, DataSetNotFoundException,
			DataSetConfigNotFoundException,
			GoldStandardConfigNotFoundException, DataConfigurationException,
			DataConfigNotFoundException, UnknownRunResultFormatException,
			UnknownClusteringQualityMeasureException, InvalidRunModeException,
			UnknownParameterOptimizationMethodException,
			NoOptimizableProgramParameterException,
			UnknownProgramParameterException,
			InvalidConfigurationFileException,
			RepositoryAlreadyExistsException, InvalidRepositoryException,
			NoRepositoryFoundException, GoldStandardNotFoundException,
			InvalidOptimizationParameterException, RunException,
			UnknownDataStatisticException, UnknownProgramTypeException,
			UnknownRProgramException,
			IncompatibleParameterOptimizationMethodException,
			UnknownDistanceMeasureException, UnknownRunStatisticException,
			RepositoryConfigNotFoundException,
			RepositoryConfigurationException, ConfigurationException,
			RegisterException, UnknownDataSetTypeException, NoDataSetException,
			UnknownRunDataStatisticException, RunResultParseException,
			UnknownDataPreprocessorException,
			IncompatibleDataSetConfigPreprocessorException,
			UnknownContextException, IncompatibleContextException,
			UnknownParameterType, InterruptedException {

		List<ParameterOptimizationResult> results = new ArrayList<ParameterOptimizationResult>();

		ParameterOptimizationResult
				.parseFromRunResultFolder(
						this.repository,
						new File(FileUtils.buildPath(
								this.repository.getBasePath(RunResult.class),
								this.uniqueRunIdentifiers)), results, true,
						true, false);

		// keep ids common to all results
		Set<ClusterItem> setIds = new HashSet<ClusterItem>(results.get(0)
				.getOptimalClustering().getClusterItems());
		// set those ids to null later, which are not part of all clusterings.
		// we keep this array to know for the columns/rows in sparseMatrix,
		// whether we need them later
		ClusterItem[] ids = setIds.toArray(new ClusterItem[0]);

		LongMatrix2D sparseMatrix = new SparseLongMatrix2D(ids.length,
				ids.length);

		// TODO: check for fuzzy?
		for (ParameterOptimizationResult result : results) {
			this.log.info("Processing result: " + result);
			result.loadIntoMemory();

			Set<ClusterItem> items = result.getOptimalClustering()
					.getClusterItems();
			for (int i = 0; i < ids.length; i++)
				if (!items.contains(ids[i]))
					ids[i] = null;
			setIds.retainAll(items);

			try {
				for (ParameterSet paramSet : result.getParameterSets()) {
					this.log.info("Processing parameter set: " + paramSet);
					Clustering cl = result.getClustering(paramSet);

					if (cl == null)
						continue;

					for (Cluster cluster : cl.getClusters()) {
						List<ClusterItem> clusterItems = new ArrayList<ClusterItem>(
								cluster.getFuzzyItems().keySet());
						for (int i = 0; i < clusterItems.size(); i++)
							for (int j = i; j < clusterItems.size(); j++) {
								sparseMatrix.set(i, j,
										sparseMatrix.get(i, j) + 1);
								sparseMatrix.set(j, i, sparseMatrix.get(i, j));
							}
					}
				}
			} finally {
				result.unloadFromMemory();
			}
		}

		int[] whichIds = new int[setIds.size()];
		int pos = 0;
		for (int i = 0; i < ids.length; i++)
			if (ids[i] != null)
				whichIds[pos++] = i;

		sparseMatrix = sparseMatrix.viewSelection(whichIds, whichIds);

		// keep only those rows/columns (ids) in the sparseMatrix which are part
		// of all clusterings (not null in ids array)

		return new CooccurrenceRunStatistic(repository, false, changeDate,
				absPath,
				ArraysExt.toString(setIds.toArray(new ClusterItem[0])),
				sparseMatrix);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see run.statistics.RunStatisticCalculator#getStatistic()
	 */
	@Override
	public CooccurrenceRunStatistic getStatistic() {
		return this.lastResult;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.StatisticCalculator#writeOutputTo(java.io.File)
	 */
	@Override
	public void writeOutputTo(File absFolderPath) {
		LongMatrix2D matrix = lastResult.cooccurrenceMatrix;
		try {
			MyRengine rEngine = repository.getRengineForCurrentThread();
			try {
				rEngine.assign("ids", lastResult.ids);
				rEngine.assign("coocc",
						ArraysExt.toDoubleArray(matrix.toArray()));
				rEngine.eval("rownames(coocc) <- ids");
				rEngine.eval("colnames(coocc) <- ids");
				rEngine.eval("hclustSorted <- ids[hclust(dist(coocc))$order]");
				rEngine.eval("cooccSorted <- coocc[hclustSorted,hclustSorted]");
				// rEngine.eval("library(ggplot2)");
				rEngine.eval("library(lattice)");
				// TODO
				rEngine.eval("png(filename='" + absFolderPath.getAbsolutePath()
						+ ".png',width=" + lastResult.ids.length * 16
						+ ",height=" + lastResult.ids.length * 16
						+ ",units='px');");
				// rEngine.eval("plot <- ggfluctuation(as.table(cooccSorted), type='colour')+scale_fill_gradient(low='red',high='yellow')+opts(panel.grid.major = theme_blank(),panel.background = theme_blank(),panel.grid.minor = theme_blank(),axis.text.x=theme_text(size=8,angle=-45,vjust=1,hjust=0),axis.text.y= theme_text(size=8));");//+geom_text(aes(label=c(cooccSorted)),size=1);");
				// rEngine.eval("ggsave(filename='"
				// + absFolderPath.getAbsolutePath()
				// + ".png', plot=plot);");
				rEngine.eval("print(levelplot(cooccSorted,xlab='',ylab='',col.regions=colorRampPalette(c('red','yellow')),scales=list(x=list(rot=90))));");
				// rEngine.eval("heatmap(cooccSorted,Rowv=NA,Colv=NA,scale='none')");
				rEngine.eval("graphics.off(); ");
			} catch (RserveException e) {
				e.printStackTrace();
			} finally {
				rEngine.clear();
			}
		} catch (REngineException e) {
			e.printStackTrace();
		}
	}
}
