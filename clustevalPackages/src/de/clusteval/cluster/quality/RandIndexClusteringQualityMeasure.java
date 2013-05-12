/**
 * 
 */
package de.clusteval.cluster.quality;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import de.clusteval.cluster.ClusterItem;
import de.clusteval.cluster.Clustering;
import de.clusteval.data.DataConfig;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;

/**
 * @author Christian Wiwie
 * 
 */
public class RandIndexClusteringQualityMeasure extends ClusteringQualityMeasure {

	/**
	 * @param repo
	 * @param register
	 * @param changeDate
	 * @param absPath
	 * @throws RegisterException
	 */
	public RandIndexClusteringQualityMeasure(Repository repo, boolean register,
			long changeDate, File absPath) throws RegisterException {
		super(repo, register, changeDate, absPath);
	}

	/**
	 * The copy constructor for this measure.
	 * 
	 * @param other
	 *            The object to clone.
	 * @throws RegisterException
	 */
	public RandIndexClusteringQualityMeasure(
			final RandIndexClusteringQualityMeasure other)
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
		return "Rand Index";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cluster.quality.ClusteringQualityMeasure#getQualityOfClustering(cluster
	 * .Clustering, data.DataConfig)
	 */
	@SuppressWarnings("unused")
	@Override
	public ClusteringQualityMeasureValue getQualityOfClustering(
			Clustering clustering, Clustering gsClustering,
			DataConfig dataConfig) {

		double tp = 0.0;
		double fp = 0.0;
		double tn = 0.0;
		double fn = 0.0;

		/*
		 * Ensure, that goldstandard contains only objects, that are also in the
		 * dataset. Otherwise precision will be calculated incorrectly, because
		 * it directly depends on the number of items in a cluster in the
		 * goldstandard.
		 */
		Set<ClusterItem> gsClusterItems = new HashSet<ClusterItem>(
				gsClustering.getClusterItems());
		gsClusterItems.retainAll(clustering.getClusterItems());

		ClusterItem[] items = gsClusterItems.toArray(new ClusterItem[0]);
		/*
		 * Iterate over all pairs
		 */
		for (int i = 0; i < items.length; i++) {
			ClusterItem first = items[i];
			for (int j = i + 1; j < items.length; j++) {
				ClusterItem second = items[j];

				// TODO: no fuzzy support yet
				boolean gsSame = first
						.getFuzzyClusters()
						.keySet()
						.iterator()
						.next()
						.equals(second.getFuzzyClusters().keySet().iterator()
								.next());
				boolean clusteringSame = clustering
						.getClusterForItem(first)
						.keySet()
						.iterator()
						.next()
						.equals(clustering.getClusterForItem(second).keySet()
								.iterator().next());
				if (gsSame && clusteringSame)
					tp++;
				else if (gsSame && !clusteringSame)
					fn++;
				else if (!gsSame && !clusteringSame)
					tn++;
				else if (!gsSame && clusteringSame)
					fp++;
			}
		}

		return ClusteringQualityMeasureValue.getForDouble((tp + tn)
				/ (tp + tn + fp + fn));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cluster.quality.ClusteringQualityMeasure#getMinimum()
	 */
	@Override
	public double getMinimum() {
		return 0.0;
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
		return true;
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
