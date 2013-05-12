/**
 * 
 */
package de.clusteval.data.dataset.format;

import java.io.File;
import java.io.IOException;

import de.clusteval.data.dataset.DataSet;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.utils.FormatVersion;
import file.FileUtils;

/**
 * @author Christian Wiwie
 * 
 */
@FormatVersion(version = 1)
public class BLASTDataSetFormat extends RelativeDataSetFormat {

	/**
	 * @param repo
	 * @param register
	 * @param changeDate
	 * @param absPath
	 * @param version
	 * @throws RegisterException
	 */
	public BLASTDataSetFormat(final Repository repo, final boolean register,
			final long changeDate, final File absPath, final int version)
			throws RegisterException {
		super(repo, register, changeDate, absPath, version);
	}

	/**
	 * The copy constructor for this format.
	 * 
	 * @param other
	 *            The object to clone.
	 * @throws RegisterException
	 */
	public BLASTDataSetFormat(final BLASTDataSetFormat other)
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
		return "BLAST";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * data.dataset.format.DataSetFormat#copyDataSetTo(data.dataset.DataSet,
	 * java.io.File)
	 */
	@Override
	public boolean copyDataSetTo(DataSet dataSet, File copyDestination,
			final boolean overwrite) {
		boolean copied = super.copyDataSetTo(dataSet, copyDestination,
				overwrite);
		if (copied) {
			try {
				org.apache.commons.io.FileUtils.copyFile(
						new File(dataSet.getAbsolutePath() + ".fasta"),
						new File(copyDestination.getAbsolutePath() + ".fasta"));
			} catch (IOException e) {
				copied = false;
			}
			copied = true;
		}
		return copied;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * data.dataset.format.DataSetFormat#copyDataSetToFolder(data.dataset.DataSet
	 * , java.io.File)
	 */
	@Override
	public boolean copyDataSetToFolder(DataSet dataSet,
			File copyFolderDestination, final boolean overwrite) {
		boolean copied = super.copyDataSetToFolder(dataSet,
				copyFolderDestination, overwrite);
		if (copied) {
			try {
				org.apache.commons.io.FileUtils.copyFile(
						new File(dataSet.getAbsolutePath() + ".fasta"),
						new File(FileUtils.buildPath(
								copyFolderDestination.getAbsolutePath(),
								new File(dataSet.getAbsolutePath()).getName()
										+ ".fasta")));
			} catch (IOException e) {
				copied = false;
			}
			copied = true;
		}
		return copied;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.clusteval.data.dataset.format.DataSetFormat#getDataSetFormatParser()
	 */
	@Override
	protected DataSetFormatParser getDataSetFormatParser() {
		return new BLASTDataSetFormatParser();
	}

}
