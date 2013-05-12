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
import utils.StringExt;

/**
 * @author Christian Wiwie
 * 
 */
public class ClassSizeDistributionDataStatistic extends DataStatistic {

	protected String[] classLabels;
	protected double[] distribution;

	/**
	 * @param repository
	 * @param register
	 * @param changeDate
	 * @param absPath
	 * @throws RegisterException
	 * 
	 */
	public ClassSizeDistributionDataStatistic(final Repository repository,
			final boolean register, final long changeDate, final File absPath)
			throws RegisterException {
		this(repository, register, changeDate, absPath, null, null);
	}

	/**
	 * @param repository
	 * @param register
	 * @param changeDate
	 * @param absPath
	 * @param classLabels
	 * @param distribution
	 * @throws RegisterException
	 */
	public ClassSizeDistributionDataStatistic(final Repository repository,
			final boolean register, final long changeDate, final File absPath,
			final String[] classLabels, final double[] distribution)
			throws RegisterException {
		super(repository, register, changeDate, absPath);
		this.classLabels = classLabels;
		this.distribution = distribution;
	}

	/**
	 * The copy constructor for this statistic.
	 * 
	 * @param other
	 *            The object to clone.
	 * @throws RegisterException
	 */
	public ClassSizeDistributionDataStatistic(
			final ClassSizeDistributionDataStatistic other)
			throws RegisterException {
		super(other);
		if (other.classLabels != null)
			this.classLabels = other.classLabels;
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
		return "Class Size Distribution";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see data.statistics.DataStatistic#requiresGoldStandard()
	 */
	@Override
	public boolean requiresGoldStandard() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see data.statistics.DataStatistic#toString()
	 */
	@Override
	public String toString() {
		return StringExt.paste("\t", classLabels)
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
		this.classLabels = StringExt.split(lines[0], "\t");
		this.distribution = ArraysExt
				.doublesFromSeparatedString(lines[1], '\t');
	}
}
