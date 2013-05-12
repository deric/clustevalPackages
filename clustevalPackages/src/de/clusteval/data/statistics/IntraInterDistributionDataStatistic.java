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
public class IntraInterDistributionDataStatistic extends DataStatistic {

	protected double[] xlabels;
	protected double[] intraDistribution;
	protected double[] interDistribution;

	/**
	 * @param repository
	 * @param register
	 * @param changeDate
	 * @param absPath
	 * @throws RegisterException
	 * 
	 */
	public IntraInterDistributionDataStatistic(final Repository repository,
			final boolean register, final long changeDate, final File absPath)
			throws RegisterException {
		this(repository, register, changeDate, absPath, null, null, null);
	}

	/**
	 * @param repository
	 * @param register
	 * @param changeDate
	 * @param absPath
	 * @param xlabels
	 * @param intraDistribution
	 * @param interDistribution
	 * @throws RegisterException
	 */
	public IntraInterDistributionDataStatistic(final Repository repository,
			final boolean register, final long changeDate, final File absPath,
			final double[] xlabels, final double[] intraDistribution,
			final double[] interDistribution) throws RegisterException {
		super(repository, register, changeDate, absPath);
		this.xlabels = xlabels;
		this.intraDistribution = intraDistribution;
		this.interDistribution = interDistribution;
	}

	/**
	 * The copy constructor for this statistic.
	 * 
	 * @param other
	 *            The object to clone.
	 * @throws RegisterException
	 */
	public IntraInterDistributionDataStatistic(
			final IntraInterDistributionDataStatistic other)
			throws RegisterException {
		super(other);
		if (other.xlabels != null)
			this.xlabels = other.xlabels.clone();
		if (other.intraDistribution != null)
			this.intraDistribution = other.intraDistribution.clone();
		if (other.interDistribution != null)
			this.interDistribution = other.interDistribution.clone();
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
		return "Intra-/Inter Similarity Distribution";
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
		return ArraysExt.toSeparatedString(xlabels, '\t')
				+ System.getProperty("line.separator")
				+ ArraysExt.toSeparatedString(intraDistribution, '\t')
				+ System.getProperty("line.separator")
				+ ArraysExt.toSeparatedString(interDistribution, '\t');
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
		this.intraDistribution = ArraysExt.doublesFromSeparatedString(lines[1],
				'\t');
		this.interDistribution = ArraysExt.doublesFromSeparatedString(lines[1],
				'\t');
	}
}
