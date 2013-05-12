/**
 * 
 */
package de.clusteval.data.statistics;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;


import utils.ArraysExt;

/**
 * @author Christian Wiwie
 * 
 */
public class SimilarityDistributionDataStatistic extends DataStatistic {

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
	public SimilarityDistributionDataStatistic(final Repository repository,
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
	public SimilarityDistributionDataStatistic(final Repository repository,
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
	public SimilarityDistributionDataStatistic(
			final SimilarityDistributionDataStatistic other)
			throws RegisterException {
		super(other);
		if (other.xlabels != null)
			this.xlabels = other.xlabels;
		if (other.distribution != null)
			this.distribution = other.distribution;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.Statistic#getRequiredRlibraries()
	 */
	@Override
	public Set<String> getRequiredRlibraries() {
		return new HashSet<String>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.Statistic#getAlias()
	 */
	@Override
	public String getAlias() {
		return "Similarity Distribution";
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
