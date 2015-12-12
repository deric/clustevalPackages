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

import de.wiwie.wiutils.utils.ArraysExt;
import de.wiwie.wiutils.utils.SimilarityMatrix;
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
 * 
 */
@RLibraryRequirement(requiredRLibraries = {"clv"})
public class RandIndexRClusteringQualityMeasure
		extends
			ClusteringQualityMeasureR {

	/**
	 * @param repo
	 * @param register
	 * @param changeDate
	 * @param absPath
	 * @throws RegisterException
	 */
	public RandIndexRClusteringQualityMeasure(Repository repo,
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
	public RandIndexRClusteringQualityMeasure(
			final RandIndexRClusteringQualityMeasure other)
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
		return "Rand Index (R)";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cluster.quality.ClusteringQualityMeasure#getQualityOfClustering(cluster
	 * .Clustering, data.DataConfig)
	 */
	@Override
	public ClusteringQualityMeasureValue getQualityOfClusteringHelper(
			Clustering clustering, Clustering gsClustering,
			DataConfig dataConfig, final MyRengine rEngine)
			throws REngineException, REXPMismatchException,
			InterruptedException {

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

		/*
		 * Create an array with all the goldstandard ids for every item
		 */
		Map<Cluster, Integer> gsClusterToId = new HashMap<Cluster, Integer>();
		for (Cluster cl : gsClustering.getClusters())
			gsClusterToId.put(cl, gsClusterToId.size() + 1);

		int[] gsClusterIds = new int[simMatrix.getIds().size()];
		keyToId = simMatrix.getIds();
		itemIter = gsClustering.getClusterItems().iterator();
		while (itemIter.hasNext()) {
			ClusterItem item = itemIter.next();
			/*
			 * TODO: Take the first one, does not work for fuzzy clusters
			 */
			gsClusterIds[keyToId.get(item.getId())] = gsClusterToId.get(item
					.getFuzzyClusters().keySet().iterator().next());
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
		rEngine.assign("clusterIds", clusterIds);
		rEngine.eval("names(clusterIds) <- as.character(1:"
				+ similarities.length + ")");
		rEngine.assign("goldstandardIds", gsClusterIds);
		rEngine.eval("names(goldstandardIds) <- as.character(1:"
				+ similarities.length + ")");

		rEngine.assign("sim", similarities);
		rEngine.eval("rownames(sim) <- as.character(1:" + similarities.length
				+ ")");
		rEngine.eval("colnames(sim) <- as.character(1:" + similarities.length
				+ ")");

		rEngine.eval("library(clv)");
		rEngine.eval("stdext <- std.ext(clusterIds, goldstandardIds)");
		rEngine.eval("rand <- clv.Rand(stdext)");
		REXP exp = rEngine.eval("rand");
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.clusteval.cluster.quality.ClusteringQualityMeasure#
	 * supportsFuzzyClusterings()
	 */
	@Override
	public boolean supportsFuzzyClusterings() {
		return false;
	}
}
