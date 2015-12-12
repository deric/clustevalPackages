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
package de.clusteval.run.statistics;

import java.io.File;

import de.wiwie.wiutils.utils.ArraysExt;
import de.wiwie.wiutils.utils.StringExt;
import cern.colt.matrix.tlong.LongMatrix2D;
import cern.colt.matrix.tlong.impl.SparseLongMatrix2D;
import de.clusteval.framework.RLibraryRequirement;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;

/**
 * @author Christian Wiwie
 * 
 */
@RLibraryRequirement(requiredRLibraries = {"lattice"})
public class CooccurrenceRunStatistic extends RunStatistic {

	protected String[] ids;

	protected LongMatrix2D cooccurrenceMatrix;

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
			final LongMatrix2D cooccurrenceMatrix) throws RegisterException {
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
			this.cooccurrenceMatrix = other.cooccurrenceMatrix.copy();
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
