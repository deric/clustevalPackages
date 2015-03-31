/**
 * 
 */
package de.clusteval.run.result.postprocessing;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.clusteval.cluster.Cluster;
import de.clusteval.cluster.ClusterItem;
import de.clusteval.cluster.Clustering;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;

/**
 * @author Christian Wiwie
 *
 */
public class FuzzyCoefficientThresholdRunResultPostprocessor
		extends
			RunResultPostprocessor {

	/**
	 * @param repository
	 * @param register
	 * @param changeDate
	 * @param absPath
	 * @param parameters
	 * @throws RegisterException
	 */
	public FuzzyCoefficientThresholdRunResultPostprocessor(
			Repository repository, boolean register, long changeDate,
			File absPath, RunResultPostprocessorParameters parameters)
			throws RegisterException {
		super(repository, register, changeDate, absPath, parameters);
	}

	/**
	 * @param other
	 * @throws RegisterException
	 */
	public FuzzyCoefficientThresholdRunResultPostprocessor(
			FuzzyCoefficientThresholdRunResultPostprocessor other)
			throws RegisterException {
		super(other);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.clusteval.run.result.postprocessing.RunResultPostprocessor#postprocess
	 * (de.clusteval.cluster.Clustering)
	 */
	@Override
	public Clustering postprocess(Clustering clustering) {
		// do fuzzy preprocessing (if parameter is set)
		Clustering clusteringPreprocessed = clustering.clone();

		Set<ClusterItem> itemsToBeRemoved = new HashSet<ClusterItem>();

		// by default we include all fuzzy coefficients;
		double threshold = parameters.containsKey("threshold") ? Double
				.valueOf(parameters.get("threshold")) : 0.0;
		// iterate over elements; only keep those fuzzy coefficients >=
		// threshold and readjust the remaining ones
		for (ClusterItem item : clusteringPreprocessed.getClusterItems()) {
			Map<Cluster, Float> coeffs = item.getFuzzyClusters();

			// identify clusters with coeff < threshold
			Set<Cluster> toBeRemoved = new HashSet<Cluster>();
			float subtracted = 0.0f;
			for (Map.Entry<Cluster, Float> e : coeffs.entrySet()) {
				if (e.getValue() < threshold) {
					subtracted += e.getValue();
					toBeRemoved.add(e.getKey());
				}
			}

			// remove those clusters
			for (Cluster cl : toBeRemoved)
				cl.remove(item);

			// read coeffs for remaining clusters
			if (coeffs.isEmpty())
				itemsToBeRemoved.add(item);
			else {
				float toAdd = subtracted / coeffs.size();
				for (Cluster cl : new HashSet<Cluster>(coeffs.keySet())) {
					float newCoeff = coeffs.get(cl) + toAdd;
					cl.remove(item);
					cl.add(item, newCoeff);
				}
			}
		}

		for (ClusterItem item : itemsToBeRemoved)
			clusteringPreprocessed.removeClusterItem(item);
		return clusteringPreprocessed;
	}

}
