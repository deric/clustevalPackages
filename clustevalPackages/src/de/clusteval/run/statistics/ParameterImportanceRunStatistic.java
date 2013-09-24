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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import utils.Pair;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;

/**
 * @author Christian Wiwie
 * 
 */
public class ParameterImportanceRunStatistic extends RunStatistic {

	protected Map<Pair<String, String>, Map<String, Double>> parameterImportances;

	/**
	 * @param repo
	 * @param register
	 * @param changeDate
	 * @param absPath
	 * @throws RegisterException
	 * 
	 */
	public ParameterImportanceRunStatistic(Repository repo, boolean register,
			long changeDate, File absPath) throws RegisterException {
		super(repo, register, changeDate, absPath);
	}

	/**
	 * @param repo
	 * @param register
	 * @param changeDate
	 * @param absPath
	 * @param parameterNames
	 * @param parameterImportances
	 * @throws RegisterException
	 */
	public ParameterImportanceRunStatistic(
			Repository repo,
			boolean register,
			long changeDate,
			File absPath,
			final Map<Pair<String, String>, Map<String, Double>> parameterImportances)
			throws RegisterException {
		super(repo, register, changeDate, absPath);
		this.parameterImportances = parameterImportances;
	}

	/**
	 * The copy constructor for this statistic.
	 * 
	 * @param other
	 *            The object to clone.
	 * @throws RegisterException
	 */
	public ParameterImportanceRunStatistic(
			final ParameterImportanceRunStatistic other)
			throws RegisterException {
		super(other);
		if (other.parameterImportances != null)
			this.parameterImportances = new HashMap<Pair<String, String>, Map<String, Double>>(
					other.parameterImportances);
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
		return "Parameter Importance";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.Statistic#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Pair<String, String> p : this.parameterImportances.keySet()) {
			for (String measure : this.parameterImportances.get(p).keySet()) {
				sb.append(p.getFirst());
				sb.append("\t");
				sb.append(p.getSecond());
				sb.append("\t");
				sb.append(measure);
				sb.append("\t");
				sb.append(parameterImportances.get(p).get(measure));
				sb.append(System.getProperty("line.separator"));
			}
		}
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.Statistic#parseFromString(java.lang.String)
	 */
	@Override
	public void parseFromString(String contents) {
		// int pos = contents.indexOf(System.getProperty("line.separator"));
		// String names = contents.substring(0, pos);
		// this.parameterNames = names.split("\t");
		// String impStr = contents.substring(pos + 1);
		// String[] importances = impStr.split("\t");
		// this.parameterImportances = new HashMap<String, Double>();
		// for (int i = 0; i < this.parameterNames.length; i++) {
		// this.parameterImportances.put(this.parameterNames[i],
		// Double.valueOf(importances[i]));
		// }
		// TODO
	}

}
