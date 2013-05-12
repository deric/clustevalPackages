/**
 * 
 */
package de.clusteval.run.statistics;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import utils.ArraysExt;
import utils.StringExt;
import cern.colt.matrix.tlong.impl.SparseLongMatrix2D;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;

/**
 * @author Christian Wiwie
 * 
 */
public class CooccurrenceRunStatistic extends RunStatistic {

	protected String[] ids;

	protected SparseLongMatrix2D cooccurrenceMatrix;

	/**
	 * @param repo
	 * @param register
	 * @param changeDate
	 * @param absPath
	 * @throws RegisterException
	 * 
	 */
	public CooccurrenceRunStatistic(Repository repo, boolean register,
			long changeDate, File absPath) throws RegisterException {
		super(repo, register, changeDate, absPath);
	}

	/**
	 * @param repo
	 * @param register
	 * @param changeDate
	 * @param absPath
	 * @param ids
	 * @param cooccurrenceMatrix
	 * @throws RegisterException
	 */
	public CooccurrenceRunStatistic(Repository repo, boolean register,
			long changeDate, File absPath, final String[] ids,
			final SparseLongMatrix2D cooccurrenceMatrix)
			throws RegisterException {
		super(repo, register, changeDate, absPath);
		this.ids = ids;
		this.cooccurrenceMatrix = cooccurrenceMatrix;
	}

	/**
	 * The copy constructor for this statistic.
	 * 
	 * @param other
	 *            The object to clone.
	 * @throws RegisterException
	 */
	public CooccurrenceRunStatistic(final CooccurrenceRunStatistic other)
			throws RegisterException {
		super(other);
		if (other.ids != null)
			this.ids = other.ids.clone();
		if (other.cooccurrenceMatrix != null)
			this.cooccurrenceMatrix = (SparseLongMatrix2D) other.cooccurrenceMatrix
					.copy();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.Statistic#getRequiredRlibraries()
	 */
	@Override
	public Set<String> getRequiredRlibraries() {
		return new HashSet<String>(Arrays.asList(new String[]{"lattice"}));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.Statistic#getAlias()
	 */
	@Override
	public String getAlias() {
		return "Cooccurrence Matrix";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.Statistic#toString()
	 */
	@Override
	public String toString() {
		return StringExt.paste(",", ids)
				+ System.getProperty("line.separator")
				+ ArraysExt.toSeparatedString(cooccurrenceMatrix.toArray(),
						'\t', '\n');
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.Statistic#parseFromString(java.lang.String)
	 */
	@Override
	public void parseFromString(String contents) {
		int pos = contents.indexOf(System.getProperty("line.separator"));
		String ids = contents.substring(0, pos);
		this.ids = ids.split(",");
		this.cooccurrenceMatrix = new SparseLongMatrix2D(
				ArraysExt.long2DFromSeparatedString(
						contents.substring(pos + 1), '\t'));
	}

}
