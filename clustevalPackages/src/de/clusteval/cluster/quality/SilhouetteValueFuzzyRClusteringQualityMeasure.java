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
package de.clusteval.cluster.quality;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
import de.clusteval.framework.RLibraryRequirement;
import de.clusteval.framework.repository.MyRengine;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;

/**
 * @author Christian Wiwie
 */
@RLibraryRequirement(requiredRLibraries = {"cluster"})
public class SilhouetteValueFuzzyRClusteringQualityMeasure
		extends
			ClusteringQualityMeasureR {

	/**
	 * @param repo
	 * @param register
	 * @param changeDate
	 * @param absPath
	 * @throws RegisterException
	 */
	public SilhouetteValueFuzzyRClusteringQualityMeasure(Repository repo,
			boolean register, long changeDate, File absPath,
			ClusteringQualityMeasureParameters parameters)
			throws RegisterException {
		super(repo, register, changeDate, absPath, parameters);
	}

	/**
	 * The copy constructor for this measure.
	 * 
	 * @param other
	 *            The object to clone.
	 * @throws RegisterException
	 */
	public SilhouetteValueFuzzyRClusteringQualityMeasure(
			final SilhouetteValueFuzzyRClusteringQualityMeasure other)
			throws RegisterException {
		super(other);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cluster.quality.ClusteringQualityMeasure#getAlias()
	 */
	@Override
	public String getAlias() {
		return "Silhouette Value Fuzzy (R)";
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
			REXPMismatchException, InterruptedException {

		if (clustering.getClusters().size() < 2)
			return ClusteringQualityMeasureValue.getForDouble(-1.0);

		// fuzzyfactor
		double alpha = parameters.containsKey("alpha") ? Double
				.valueOf(parameters.get("alpha")) : 1.0;

		RelativeDataSet dataSet = (RelativeDataSet) (dataConfig
				.getDatasetConfig().getDataSet().getInStandardFormat());

		SimilarityMatrix simMatrix = dataSet.getDataSetContent();

		/*
		 * Create an array with all the cluster ids for every item
		 */
		Map<Cluster, Integer> clusterToId = new HashMap<Cluster, Integer>();
		for (Cluster cl : clustering.getClusters())
			clusterToId.put(cl, clusterToId.size() + 1);

		double[][] fuzzyCoeffs = new double[simMatrix.getIds().size()][2];
		int[][] fuzzyClusters = new int[simMatrix.getIds().size()][2];

		Map<String, Integer> keyToId = simMatrix.getIds();
		Iterator<ClusterItem> itemIter = clustering.getClusterItems()
				.iterator();
		while (itemIter.hasNext()) {
			ClusterItem item = itemIter.next();

			int itemIndex = keyToId.get(item.getId());

			for (Map.Entry<Cluster, Float> e : item.getFuzzyClusters()
					.entrySet()) {

				int clusterIndex = clusterToId.get(e.getKey());

				// maintain clusters with two largest fuzzy coefficient
				if (e.getValue() > fuzzyCoeffs[itemIndex][0]) {
					fuzzyCoeffs[itemIndex][1] = fuzzyCoeffs[itemIndex][0];
					fuzzyClusters[itemIndex][1] = fuzzyClusters[itemIndex][0];

					fuzzyCoeffs[itemIndex][0] = e.getValue();
					fuzzyClusters[itemIndex][0] = clusterIndex;
				} else if (e.getValue() > fuzzyCoeffs[itemIndex][1]) {
					fuzzyCoeffs[itemIndex][1] = e.getValue();
					fuzzyClusters[itemIndex][1] = clusterIndex;
				}
			}

		}
		double[][] similarities = simMatrix.toArray();
		/*
		 * Convert to dissimilarities
		 */
		similarities = ArraysExt.subtract(simMatrix.getMaxValue(),
				similarities, true);

		/*
		 * Pass the arrays to R
		 */
		double result;
		rEngine.assign("fuzzyCoeffs", fuzzyCoeffs);
		rEngine.assign("fuzzyClusters", fuzzyClusters);
		rEngine.eval("alpha <- " + alpha);
		rEngine.eval("clusterIds <- fuzzyClusters[,1]");
		rEngine.eval("names(clusterIds) <- as.character(1:"
				+ similarities.length + ")");

		rEngine.assign("sim", similarities);
		rEngine.eval("rownames(sim) <- as.character(1:" + similarities.length
				+ ")");
		rEngine.eval("colnames(sim) <- as.character(1:" + similarities.length
				+ ")");

		rEngine.eval("library(cluster)");
		rEngine.eval("sil <- silhouette(x=clusterIds,dmatrix=sim)");
		REXP exp = rEngine
				.eval("sum((fuzzyCoeffs[,1]-fuzzyCoeffs[,2])**alpha * sil[,3])/sum((fuzzyCoeffs[,1]-fuzzyCoeffs[,2])**alpha)");
		if (exp != null) {
			result = exp.asDouble();
		} else
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.clusteval.cluster.quality.ClusteringQualityMeasure#
	 * supportsFuzzyClusterings()
	 */
	@Override
	public boolean supportsFuzzyClusterings() {
		return true;
	}
}
