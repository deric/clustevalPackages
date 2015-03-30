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
import java.io.Serializable;
import java.util.Map;

import de.clusteval.cluster.Cluster;
import de.clusteval.cluster.ClusterItem;
import de.clusteval.cluster.Clustering;
import de.clusteval.data.DataConfig;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;

/**
 * The Class F2ClusteringQualityMeasure.
 * 
 * @author Christian Wiwie
 */
public class PairwiseF2ClusteringQualityMeasure
		extends
			ClusteringQualityMeasure implements Serializable {

	/**
	 * @param repo
	 * @param register
	 * @param changeDate
	 * @param absPath
	 * @throws RegisterException
	 */
	public PairwiseF2ClusteringQualityMeasure(Repository repo,
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
	public PairwiseF2ClusteringQualityMeasure(
			final PairwiseF2ClusteringQualityMeasure other)
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
		return "Pairwise F2-Score";
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -4954352721817575007L;

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
			final DataConfig dataConfig) {

		final float proteins = clustering.size();

		double fmeasure = 0;

		for (Cluster gsCluster : gsClustering) {
			final float proteinsInReference = gsCluster.fuzzySize();
			final double maxValue = findMax(clustering, gsCluster);
			fmeasure += (maxValue * proteinsInReference);
		}
		fmeasure /= proteins;

		return ClusteringQualityMeasureValue.getForDouble(fmeasure);
	}

	/**
	 * Find max.
	 * 
	 * @param proteinsInReference
	 *            the proteins in reference
	 * @param clustering
	 *            the clustering
	 * @param gsCluster
	 *            the gs cluster
	 * @return the double
	 */
	private static double findMax(final Clustering clustering,
			final Cluster gsCluster) {
		double max = 0;
		double maxCommon = 0;

		for (Cluster cluster : clustering) {
			double dummy = 0;
			double common = 0;

			// performance reasons
			if (gsCluster.size() < cluster.size()) {
				common = calculateCommonProteins(gsCluster, cluster);
			} else {
				common = calculateCommonProteins(cluster, gsCluster);
			}
			dummy = (2 * common)
					/ (gsCluster.fuzzySize() + cluster.fuzzySize());
			if (dummy > max) {
				max = dummy;
				maxCommon = common;
			}
		}
		if (maxCommon == 0 && gsCluster.size() == 1) {
			return 1;
		}
		return max;
	}

	/**
	 * Calculate common proteins.
	 * 
	 * @param c1
	 *            the c1
	 * @param c2
	 *            the c2
	 * @return the float
	 */
	private static float calculateCommonProteins(final Cluster c1,
			final Cluster c2) {
		float common = 0;

		Map<ClusterItem, Float> items = c1.getFuzzyItems();

		for (ClusterItem item : items.keySet()) {
			if (c2.contains(item)) {
				common += items.get(item);
			}
		}
		return common;
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

	/* (non-Javadoc)
	 * @see de.clusteval.cluster.quality.ClusteringQualityMeasure#supportsFuzzyClusterings()
	 */
	@Override
	public boolean supportsFuzzyClusterings() {
		return false;
	}
}
