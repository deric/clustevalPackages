/**
 * 
 */
package de.clusteval.cluster.quality;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import utils.SimilarityMatrix;
import de.clusteval.cluster.Cluster;
import de.clusteval.cluster.ClusterItem;
import de.clusteval.cluster.Clustering;
import de.clusteval.data.DataConfig;
import de.clusteval.data.dataset.RelativeDataSet;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;

/**
 * @author Christian Wiwie
 */
public class SilhouetteValueClusteringQualityMeasure
		extends
			ClusteringQualityMeasure {

	/**
	 * @param repo
	 * @param register
	 * @param changeDate
	 * @param absPath
	 * @throws RegisterException
	 */
	public SilhouetteValueClusteringQualityMeasure(Repository repo,
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
	public SilhouetteValueClusteringQualityMeasure(
			final SilhouetteValueClusteringQualityMeasure other)
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
		return new HashSet<String>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cluster.quality.ClusteringQualityMeasure#getAlias()
	 */
	@Override
	public String getAlias() {
		return "Silhouette Value (Java)";
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
	public ClusteringQualityMeasureValue getQualityOfClustering(
			final Clustering clustering, Clustering gsClustering,
			final DataConfig dataConfig) throws IllegalArgumentException {

		if (clustering.getClusters().size() < 2)
			return ClusteringQualityMeasureValue.getForDouble(-1.0);

		RelativeDataSet dataSet = (RelativeDataSet) (dataConfig
				.getDatasetConfig().getDataSet().getInStandardFormat());

		SimilarityMatrix simMatrix = dataSet.getDataSetContent();

		double result = 0.0;

		for (ClusterItem item : clustering.getClusterItems())
			result += getQualityOfDatum(simMatrix, clustering, item);
		result /= clustering.getClusterItems().size();

		return ClusteringQualityMeasureValue.getForDouble(result);
	}

	private double getQualityOfDatum(final SimilarityMatrix simMatrix,
			final Clustering clustering, final ClusterItem item) {
		Set<Cluster> ownClusters = item.getFuzzyClusters().keySet();
		Set<Cluster> otherClusters = new HashSet<Cluster>(
				clustering.getClusters());
		otherClusters.removeAll(ownClusters);

		double a;
		if (ownClusters.iterator().next().size() > 1)
			a = getAverageDissimilarityToFuzzyClusters(simMatrix, item,
					ownClusters);
		else
			return 0.0;
		double b = Double.MAX_VALUE;
		for (Cluster other : otherClusters)
			b = Math.min(
					b,
					getAverageDissimilarityToFuzzyCluster(simMatrix, item,
							other));

		/*
		 * Avoid division by zero in extreme cases.
		 */
		return (a == b && a == 0 ? 0 : (b - a) / Math.max(a, b));
	}

	private double getAverageDissimilarityToFuzzyClusters(
			final SimilarityMatrix simMatrix, final ClusterItem item,
			final Set<Cluster> clusters) {
		double result = 0.0;

		for (Cluster cl : clusters) {
			result += getAverageDissimilarityToFuzzyCluster(simMatrix, item, cl);
		}

		return result;
	}

	private double getAverageDissimilarityToFuzzyCluster(
			final SimilarityMatrix simMatrix, final ClusterItem item,
			final Cluster fuzzyCluster) {
		double result = 0.0;

		String itemId = item.getId();
		for (Map.Entry<ClusterItem, Float> other : fuzzyCluster.getFuzzyItems()
				.entrySet()) {
			ClusterItem otherItem = other.getKey();
			if (otherItem.equals(item))
				continue;
			result += (simMatrix.getMaxValue() - simMatrix.getSimilarity(
					itemId, otherItem.getId())) * other.getValue();
		}
		// TODO
		if (fuzzyCluster.contains(item)) {
			result /= fuzzyCluster.fuzzySize() - 1;
			// result *= fuzzyCluster.getFuzzyItems().get(item);
		} else
			result /= fuzzyCluster.fuzzySize();

		return result;
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
