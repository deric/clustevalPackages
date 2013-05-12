/**
 * 
 */
package de.clusteval.data.dataset.type;

import java.io.File;

import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;


/**
 * @author Christian Wiwie
 * 
 */
public class OtherDataSetType extends DataSetType {

	/**
	 * @param repository
	 * @param register
	 * @param changeDate
	 * @param absPath
	 * @throws RegisterException
	 * 
	 */
	public OtherDataSetType(final Repository repository,
			final boolean register, final long changeDate, final File absPath)
			throws RegisterException {
		super(repository, register, changeDate, absPath);
	}

	/**
	 * The copy constructor for this type.
	 * 
	 * @param other
	 *            The object to clone.
	 * @throws RegisterException
	 */
	public OtherDataSetType(
			final OtherDataSetType other)
			throws RegisterException {
		super(other);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see data.dataset.type.DataSetType#getAlias()
	 */
	@Override
	public String getAlias() {
		return "Other";
	}

}
