/**
 * 
 */
package de.clusteval.run.statistics;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;

import utils.ArraysExt;
import utils.Pair;
import utils.StringExt;

/**
 * @author Christian Wiwie
 * 
 */
public class LinearModelRidgeRunDataStatistic extends RunDataStatistic {

	protected List<String> dataStatistics;

	protected Map<Pair<String, String>, double[]> coefficients;

	/**
	 * @param repo
	 * @param register
	 * @param changeDate
	 * @param absPath
	 * @throws RegisterException
	 * 
	 */
	public LinearModelRidgeRunDataStatistic(Repository repo, boolean register,
			long changeDate, File absPath) throws RegisterException {
		super(repo, register, changeDate, absPath);
		this.dataStatistics = new ArrayList<String>();
		this.coefficients = new HashMap<Pair<String, String>, double[]>();
	}

	/**
	 * @param repo
	 * @param register
	 * @param changeDate
	 * @param absPath
	 * @param dataStatistics
	 * @param coefficients
	 * @throws RegisterException
	 */
	public LinearModelRidgeRunDataStatistic(Repository repo, boolean register,
			long changeDate, File absPath, final List<String> dataStatistics,
			final Map<Pair<String, String>, double[]> coefficients)
			throws RegisterException {
		super(repo, register, changeDate, absPath);
		this.dataStatistics = dataStatistics;
		this.coefficients = coefficients;
	}

	/**
	 * The copy constructor for this statistic.
	 * 
	 * @param other
	 *            The object to clone.
	 * @throws RegisterException
	 */
	public LinearModelRidgeRunDataStatistic(
			final LinearModelRidgeRunDataStatistic other)
			throws RegisterException {
		super(other);
		this.dataStatistics = new ArrayList<String>(other.dataStatistics);
		this.coefficients = cloneCoefficients(other.coefficients);
	}

	/**
	 * @param coefficients
	 * @return
	 */
	private Map<Pair<String, String>, double[]> cloneCoefficients(
			Map<Pair<String, String>, double[]> coefficients) {
		final Map<Pair<String, String>, double[]> result = new HashMap<Pair<String, String>, double[]>();

		for (Map.Entry<Pair<String, String>, double[]> entry : coefficients
				.entrySet()) {

			Pair<String, String> oldKey = entry.getKey();

			result.put(
					new Pair<String, String>(oldKey.getFirst(), oldKey
							.getSecond()), entry.getValue().clone());
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.Statistic#getRequiredRlibraries()
	 */
	@Override
	public Set<String> getRequiredRlibraries() {
		return new HashSet<String>(Arrays.asList(new String[]{"MASS"}));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.Statistic#getAlias()
	 */
	@Override
	public String getAlias() {
		return "Linear Model (Ridge Regression)";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.Statistic#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(StringExt.paste("\t",
				ArraysExt.toString(dataStatistics.toArray(new String[0]))));
		for (Pair<String, String> key : this.coefficients.keySet()) {
			sb.append(System.getProperty("line.separator"));
			sb.append(key.getFirst());
			sb.append("\t");
			sb.append(key.getSecond());
			sb.append("\t");
			sb.append(ArraysExt.toSeparatedString(coefficients.get(key), '\t'));
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
		String[] split = contents.split(System.getProperty("line.separator"));
		this.dataStatistics = Arrays.asList(split[0].split("\t"));
		for (int i = 1; i < split.length; i++) {
			int pos1 = split[i].indexOf("\t");
			int pos2 = split[i].indexOf("\t", pos1 + 1);
			this.coefficients.put(Pair.getPair(split[i].substring(0, pos1),
					split[i].substring(pos1 + 1, pos2)), ArraysExt
					.doublesFromSeparatedString(split[i].substring(pos2 + 1)));
		}
	}
}
