/**
 * 
 */
package de.clusteval.data.statistics;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;


/**
 * @author Christian Wiwie
 * 
 */
public class GraphMinCutRDataStatistic extends DoubleValueDataStatistic {

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.Statistic#getRequiredRlibraries()
	 */
	@Override
	public Set<String> getRequiredRlibraries() {
		return new HashSet<String>(Arrays.asList(new String[]{"igraph"}));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.Statistic#getAlias()
	 */
	@Override
	public String getAlias() {
		return "Graph Min-Cut";
	}

	/**
	 * @param repository
	 * @param register
	 * @param changeDate
	 * @param absPath
	 * @throws RegisterException
	 * 
	 */
	public GraphMinCutRDataStatistic(final Repository repository,
			final boolean register, final long changeDate, final File absPath)
			throws RegisterException {
		super(repository, register, changeDate, absPath, 0.0);
	}

	/**
	 * @param repository
	 * @param register
	 * @param changeDate
	 * @param absPath
	 * @param value
	 * @throws RegisterException
	 */
	public GraphMinCutRDataStatistic(final Repository repository,
			final boolean register, final long changeDate, final File absPath,
			final double value) throws RegisterException {
		super(repository, register, changeDate, absPath, value);
	}

	/**
	 * The copy constructor for this statistic.
	 * 
	 * @param other
	 *            The object to clone.
	 * @throws RegisterException
	 */
	public GraphMinCutRDataStatistic(final GraphMinCutRDataStatistic other)
			throws RegisterException {
		super(other);
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

}
