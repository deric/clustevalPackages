/**
 * 
 */
package de.clusteval.cluster.quality;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.clusteval.cluster.Cluster;
import de.clusteval.cluster.ClusterItem;
import de.clusteval.cluster.Clustering;
import de.clusteval.data.DataConfig;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;

/**
 * @author Christian Wiwie
 * 
 */
public class VMeasureClusteringQualityMeasure extends ClusteringQualityMeasure {

	/**
	 * @param repo
	 * @param register
	 * @param changeDate
	 * @param absPath
	 * @throws RegisterException
	 */
	public VMeasureClusteringQualityMeasure(Repository repo, boolean register,
			long changeDate, File absPath) throws RegisterException {
		super(repo, register, changeDate, absPath);
	}

	/**
	 * @param other
	 * @throws RegisterException
	 */
	public VMeasureClusteringQualityMeasure(
			VMeasureClusteringQualityMeasure other) throws RegisterException {
		super(other);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.clusteval.cluster.quality.ClusteringQualityMeasure#getQualityOfClustering
	 * (de.clusteval.cluster.Clustering, de.clusteval.data.DataConfig)
	 */
	@SuppressWarnings("unused")
	@Override
	public ClusteringQualityMeasureValue getQualityOfClustering(
			Clustering clustering, Clustering gsClustering,
			DataConfig dataConfig) {

		/*
		 * Ensure, that goldstandard contains only objects, that are also in the
		 * dataset.
		 */
		Set<ClusterItem> gsClusterItems = new HashSet<ClusterItem>(
				gsClustering.getClusterItems());
		Set<ClusterItem> clusterItems = clustering.getClusterItems();
		gsClusterItems.removeAll(clusterItems);
		for (ClusterItem onlyInGs : gsClusterItems)
			gsClustering.removeClusterItem(onlyInGs);

		// class labels
		Map<Cluster, Integer> classLabels = new HashMap<Cluster, Integer>();
		for (Cluster cl : gsClustering.getClusters())
			classLabels.put(cl, classLabels.size());

		// cluster labels
		Map<Cluster, Integer> clusterLabels = new HashMap<Cluster, Integer>();
		for (Cluster cl : clustering.getClusters())
			clusterLabels.put(cl, clusterLabels.size());

		// number classes X number clusters
		double[][] contingency = new double[classLabels.size()][clusterLabels
				.size()];
		for (ClusterItem item : gsClustering.getClusterItems()) {
			Map<Cluster, Float> clusters = clustering.getClusterForItem(item);
			Map<Cluster, Float> classes = gsClustering.getClusterForItem(item);
			for (Cluster cluster : clusters.keySet()) {
				int clusterLabel = clusterLabels.get(cluster);
				for (Cluster clazz : classes.keySet()) {
					int classLabel = classLabels.get(clazz);
					contingency[classLabel][clusterLabel] += Math.min(
							clusters.get(cluster), classes.get(clazz));
				}
			}
		}

		double[] contingency_sum_c = new double[clusterLabels.size()];
		for (int cluster = 0; cluster < contingency_sum_c.length; cluster++)
			for (int clazz = 0; clazz < classLabels.size(); clazz++) {
				contingency_sum_c[cluster] += contingency[clazz][cluster];
			}

		double[] contingency_sum_k = new double[classLabels.size()];
		for (int clazz = 0; clazz < contingency_sum_k.length; clazz++) {
			for (int cluster = 0; cluster < clusterLabels.size(); cluster++)
				contingency_sum_k[clazz] += contingency[clazz][cluster];
		}

		// homogeneity
		double h_c_k = 0.0;
		for (Cluster cluster : clusterLabels.keySet()) {
			for (Cluster clazz : classLabels.keySet()) {
				int clusterLabel = clusterLabels.get(cluster);
				double a_c_k = contingency[classLabels.get(clazz)][clusterLabels
						.get(cluster)];
				if (a_c_k == 0)
					continue;
				h_c_k += -(a_c_k / gsClustering.size() * Math.log(a_c_k
						/ contingency_sum_c[clusterLabel]));
			}
		}

		int n = gsClustering.getClusters().size();
		int m = clustering.getClusters().size();

		double h_c = 0.0;
		for (Cluster clazz : classLabels.keySet()) {
			int classLabel = classLabels.get(clazz);
			h_c += -(contingency_sum_k[classLabel] / gsClustering.size() * Math
					.log(contingency_sum_k[classLabel] / gsClustering.size()));
		}

		double homogeneity = h_c == 0.0 ? 1 : (1 - h_c_k / h_c);

		// completeness
		double h_k_c = 0.0;
		for (Cluster clazz : classLabels.keySet()) {
			int classLabel = classLabels.get(clazz);
			for (Cluster cluster : clusterLabels.keySet()) {
				double a_c_k = contingency[classLabel][clusterLabels
						.get(cluster)];
				if (a_c_k == 0)
					continue;
				h_k_c += -(a_c_k / gsClustering.size() * Math.log(a_c_k
						/ contingency_sum_k[classLabel]));
			}
		}

		double h_k = 0.0;
		for (Cluster cluster : clusterLabels.keySet()) {
			int clusterLabel = clusterLabels.get(cluster);
			h_k += -(contingency_sum_c[clusterLabel] / gsClustering.size() * Math
					.log(contingency_sum_c[clusterLabel] / gsClustering.size()));
		}

		double completeness = h_k == 0.0 ? 1 : (1 - h_k_c / h_k);

		double vmeasure = 2 * homogeneity * completeness
				/ (homogeneity + completeness);

		return ClusteringQualityMeasureValue.getForDouble(vmeasure);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.clusteval.cluster.quality.ClusteringQualityMeasure#isBetterThanHelper
	 * (de.clusteval.cluster.quality.ClusteringQualityMeasureValue,
	 * de.clusteval.cluster.quality.ClusteringQualityMeasureValue)
	 */
	@Override
	protected boolean isBetterThanHelper(
			ClusteringQualityMeasureValue quality1,
			ClusteringQualityMeasureValue quality2) {
		return quality1.getValue() > quality2.getValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.clusteval.cluster.quality.ClusteringQualityMeasure#getMinimum()
	 */
	@Override
	public double getMinimum() {
		return 0.0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.clusteval.cluster.quality.ClusteringQualityMeasure#getMaximum()
	 */
	@Override
	public double getMaximum() {
		return 1.0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.clusteval.cluster.quality.ClusteringQualityMeasure#getRequiredRlibraries
	 * ()
	 */
	@Override
	public Set<String> getRequiredRlibraries() {
		return new HashSet<String>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.clusteval.cluster.quality.ClusteringQualityMeasure#requiresGoldstandard
	 * ()
	 */
	@Override
	public boolean requiresGoldstandard() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.clusteval.cluster.quality.ClusteringQualityMeasure#getAlias()
	 */
	@Override
	public String getAlias() {
		return "V-Measure";
	}

}
