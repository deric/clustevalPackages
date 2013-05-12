/**
 * 
 */
package de.clusteval.run.statistics;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RserveException;

import utils.ArraysExt;
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
import de.clusteval.framework.MyRengine;
import de.clusteval.framework.repository.InvalidRepositoryException;
import de.clusteval.framework.repository.NoRepositoryFoundException;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.framework.repository.RepositoryAlreadyExistsException;
import de.clusteval.framework.repository.config.RepositoryConfigNotFoundException;
import de.clusteval.framework.repository.config.RepositoryConfigurationException;
import de.clusteval.program.NoOptimizableProgramParameterException;
import de.clusteval.program.ParameterSet;
import de.clusteval.program.UnknownProgramParameterException;
import de.clusteval.program.UnknownProgramTypeException;
import de.clusteval.program.r.UnknownRProgramException;
import de.clusteval.run.InvalidRunModeException;
import de.clusteval.run.RunException;
import de.clusteval.run.result.ParameterOptimizationResult;
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
			UnknownContextException, IncompatibleContextException {

		List<ParameterOptimizationResult> results = new ArrayList<ParameterOptimizationResult>();

		ParameterOptimizationResult
				.parseFromRunResultFolder(
						this.repository,
						new File(FileUtils.buildPath(
								this.repository.getRunResultBasePath(),
								this.uniqueRunIdentifiers)), results, true,
						true, false);
		try {
			for (ParameterOptimizationResult result : results)
				result.loadIntoMemory();

			// determine common ids
			List<ClusterItem> ids = new ArrayList<ClusterItem>();
			if (results.size() > 0
					&& results.get(0).getOptimalClustering() != null) {
				ids.addAll(results.get(0).getOptimalClustering()
						.getClusterItems());
			}
			for (ParameterOptimizationResult result : results) {
				ids.retainAll(result.getOptimalClustering().getClusterItems());
			}

			SparseLongMatrix2D sparseMatrix = new SparseLongMatrix2D(
					ids.size(), ids.size());

			// TODO: check for fuzzy?
			for (ParameterOptimizationResult result : results) {
				for (ParameterSet paramSet : result.getParameterSets()) {
					Clustering cl = result.getClustering(paramSet);

					if (cl == null)
						continue;

					for (int i = 0; i < ids.size(); i++) {
						for (int j = i; j < ids.size(); j++) {
							Map<Cluster, Float> cl1 = cl.getClusterForItem(ids
									.get(i));
							Map<Cluster, Float> cl2 = cl.getClusterForItem(ids
									.get(j));
							if (cl1 != null && cl2 != null && cl1.equals(cl2)) {
								sparseMatrix.set(i, j,
										sparseMatrix.get(i, j) + 1);
								sparseMatrix.set(j, i, sparseMatrix.get(i, j));
							}
						}
					}
				}
			}

			return new CooccurrenceRunStatistic(repository, false, changeDate,
					absPath,
					ArraysExt.toString(ids.toArray(new ClusterItem[0])),
					sparseMatrix);
		} finally {
			for (ParameterOptimizationResult result : results)
				result.unloadFromMemory();
		}
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
		SparseLongMatrix2D matrix = lastResult.cooccurrenceMatrix;
		try {
			MyRengine rEngine = new MyRengine("");
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
				rEngine.close();
			}
		} catch (REngineException e) {
			e.printStackTrace();
		}
	}
}
