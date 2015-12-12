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
package de.clusteval.data.statistics;

import java.io.File;

import de.wiwie.wiutils.utils.ArraysExt;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;

/**
 * @author Christian Wiwie
 * 
 */
public class NodeDegreeDistributionDataStatistic extends DataStatistic {

	protected double[] xlabels;
	protected double[] distribution;

	/**
	 * @param repository
	 * @param register
	 * @param changeDate
	 * @param absPath
	 * @throws RegisterException
	 * 
	 */
	public NodeDegreeDistributionDataStatistic(final Repository repository,
			final boolean register, final long changeDate, final File absPath)
			throws RegisterException {
		this(repository, register, changeDate, absPath, null, null);
	}

	/**
	 * @param repository
	 * @param register
	 * @param changeDate
	 * @param absPath
	 * @param xlabels
	 * @param distribution
	 * @throws RegisterException
	 */
	public NodeDegreeDistributionDataStatistic(final Repository repository,
			final boolean register, final long changeDate, final File absPath,
			final double[] xlabels, final double[] distribution)
			throws RegisterException {
		super(repository, register, changeDate, absPath);
		this.xlabels = xlabels;
		this.distribution = distribution;
	}

	/**
	 * The copy constructor for this statistic.
	 * 
	 * @param other
	 *            The object to clone.
	 * @throws RegisterException
	 */
	public NodeDegreeDistributionDataStatistic(
			final NodeDegreeDistributionDataStatistic other)
			throws RegisterException {
		super(other);
		if (other.xlabels != null)
			this.xlabels = other.xlabels.clone();
		if (other.distribution != null)
			this.distribution = other.distribution.clone();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.Statistic#getAlias()
	 */
	@Override
	public String getAlias() {
		return "Node Degree Distribution";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see data.statistics.DataStatistic#requiresGoldStandard()
	 */
	@Override
	public boolean requiresGoldStandard() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see data.statistics.DataStatistic#toString()
	 */
	@Override
	public String toString() {
		return ArraysExt.toSeparatedString(xlabels, '\t')
				+ System.getProperty("line.separator")
				+ ArraysExt.toSeparatedString(distribution, '\t');
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.Statistic#parseFromString(java.lang.String)
	 */
	@Override
	public void parseFromString(String contents) {
		String[] lines = contents.split(System.getProperty("line.separator"));
		this.xlabels = ArraysExt.doublesFromSeparatedString(lines[0], '\t');
		this.distribution = ArraysExt
				.doublesFromSeparatedString(lines[1], '\t');
	}
}
