/**
 * 
 */
package de.clusteval.cluster.quality;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;

import utils.ArraysExt;
import utils.SimilarityMatrix;
import de.clusteval.cluster.Cluster;
import de.clusteval.cluster.ClusterItem;
import de.clusteval.cluster.Clustering;
import de.clusteval.data.DataConfig;
import de.clusteval.data.dataset.RelativeDataSet;
import de.clusteval.framework.MyRengine;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;

/**
 * @author Christian Wiwie
 */
public class SilhouetteValueRClusteringQualityMeasure
		extends
			ClusteringQualityMeasureR {

	/**
	 * @param repo
	 * @param register
	 * @param changeDate
	 * @param absPath
	 * @throws RegisterException
	 */
	public SilhouetteValueRClusteringQualityMeasure(Repository repo,
			boolean register, long changeDate, File absPath)
			throws RegisterException {
		super(repo, register, changeDate, absPath);
	}

	/**
	 * The copy constructor for this measure.
	 * 
	 * @param other
	 *            The object to clone.
	 * @throws RegisterException
	 */
	public SilhouetteValueRClusteringQualityMeasure(
			final SilhouetteValueRClusteringQualityMeasure other)
			throws RegisterException {
		super(other);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cluster.quality.ClusteringQualityMeasure#getRequiredRlibraries()
	 */
	@Override
	public Set<String> getRequiredRlibraries() {
		return new HashSet<String>(Arrays.asList(new String[]{"cluster"}));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cluster.quality.ClusteringQualityMeasure#getAlias()
	 */
	@Override
	public String getAlias() {
		return "Silhouette Value (R)";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cluster.quality.ClusteringQualityMeasure#getQualityOfClustering(cluster
	 * .Clustering, data.goldstandard.GoldStandard)
	 */
	@SuppressWarnings("unused")
	@Override
	public ClusteringQualityMeasureValue getQualityOfClusteringHelper(
			final Clustering clustering, Clustering gsClustering,
			final DataConfig dataConfig, final MyRengine rEngine)
			throws IllegalArgumentException, REngineException,
			REXPMismatchException {

		if (clustering.getClusters().size() < 2)
			return ClusteringQualityMeasureValue.getForDouble(-1.0);

		RelativeDataSet dataSet = (RelativeDataSet) (dataConfig
				.getDatasetConfig().getDataSet().getInStandardFormat());

		SimilarityMatrix simMatrix = dataSet.getDataSetContent();

		/*
		 * Create an array with all the cluster ids for every item
		 */
		Map<Cluster, Integer> clusterToId = new HashMap<Cluster, Integer>();
		for (Cluster cl : clustering.getClusters())
			clusterToId.put(cl, clusterToId.size() + 1);

		int[] clusterIds = new int[simMatrix.getIds().size()];
		Map<String, Integer> keyToId = simMatrix.getIds();
		Iterator<ClusterItem> itemIter = clustering.getClusterItems()
				.iterator();
		while (itemIter.hasNext()) {
			ClusterItem item = itemIter.next();
			/*
			 * TODO: Take the first one, does not work for fuzzy clusters
			 */
			clusterIds[keyToId.get(item.getId())] = clusterToId.get(item
					.getFuzzyClusters().keySet().iterator().next());
		}
		double[][] similarities = simMatrix.toArray();
		/*
		 * Convert to dissimilarities
		 */
		similarities = ArraysExt
				.subtract(simMatrix.getMaxValue(), similarities);

		/*
		 * Pass the arrays to R
		 */
		double result;
		rEngine.assign("clusterIds", clusterIds);
		rEngine.eval("names(clusterIds) <- as.character(1:"
				+ similarities.length + ")");

		rEngine.eval("sim <- c()");
		for (int i = 0; i < similarities.length; i++) {
			rEngine.assign("sim_" + i, similarities[i]);
			rEngine.eval("sim <- rbind(sim,sim_" + i + ")");
			rEngine.eval("remove(sim_" + i + ")");
		}
		rEngine.eval("rownames(sim) <- as.character(1:" + similarities.length
				+ ")");
		rEngine.eval("colnames(sim) <- as.character(1:" + similarities.length
				+ ")");

		rEngine.eval("library(cluster)");
		rEngine.eval("sil <- silhouette(x=clusterIds,dmatrix=sim)");
		REXP exp = rEngine.eval("summary(sil)$avg.width");
		if (exp != null)
			result = exp.asDouble();
		else
			result = this.getMinimum();

		return ClusteringQualityMeasureValue.getForDouble(result);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cluster.quality.ClusteringQualityMeasure#getMinimum()
	 */
	@Override
	public double getMinimum() {
		return -1.0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cluster.quality.ClusteringQualityMeasure#getMaximum()
	 */
	@Override
	public double getMaximum() {
		return 1.0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cluster.quality.ClusteringQualityMeasure#requiresGoldstandard()
	 */
	@Override
	public boolean requiresGoldstandard() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cluster.quality.ClusteringQualityMeasure#isBetterThanHelper(cluster.quality
	 * .ClusteringQualityMeasureValue,
	 * cluster.quality.ClusteringQualityMeasureValue)
	 */
	@Override
	protected boolean isBetterThanHelper(
			ClusteringQualityMeasureValue quality1,
			ClusteringQualityMeasureValue quality2) {
		return quality1.getValue() > quality2.getValue();
	}
}