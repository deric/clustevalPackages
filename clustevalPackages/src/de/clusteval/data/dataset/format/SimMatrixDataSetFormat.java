package de.clusteval.data.dataset.format;

import java.io.File;

import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.utils.FormatVersion;

/**
 * @author Christian Wiwie
 * 
 */
@FormatVersion(version = 1)
public class SimMatrixDataSetFormat extends RelativeDataSetFormat {

	/**
	 * Instantiates a new sim matrix data set format.
	 * 
	 * @param repo
	 * @param register
	 * @param changeDate
	 * @param absPath
	 * 
	 * @param version
	 * @throws RegisterException
	 * 
	 */
	public SimMatrixDataSetFormat(final Repository repo,
			final boolean register, final long changeDate, final File absPath,
			final int version) throws RegisterException {
		super(repo, register, changeDate, absPath, version);
	}

	/**
	 * The copy constructor for this format.
	 * 
	 * @param other
	 *            The object to clone.
	 * @throws RegisterException
	 */
	public SimMatrixDataSetFormat(final SimMatrixDataSetFormat other)
			throws RegisterException {
		super(other);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see data.dataset.format.DataSetFormat#getAlias()
	 */
	@Override
	public String getAlias() {
		return "Similarity Matrix";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.clusteval.data.dataset.format.DataSetFormat#getDataSetFormatParser()
	 */
	@Override
	protected DataSetFormatParser getDataSetFormatParser() {
		return new SimMatrixDataSetFormatParser();
	}
}
