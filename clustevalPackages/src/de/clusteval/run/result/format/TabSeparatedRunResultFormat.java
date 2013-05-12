/**
 * 
 */
package de.clusteval.run.result.format;

import java.io.File;

import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;


/**
 * The Class InternalRunResultFormat.
 * 
 * @author Christian Wiwie
 */
public class TabSeparatedRunResultFormat extends RunResultFormat {

	/**
	 * @param repo
	 * @param register
	 * @param changeDate
	 * @param absPath
	 * @throws RegisterException
	 */
	public TabSeparatedRunResultFormat(Repository repo, boolean register,
			long changeDate, File absPath) throws RegisterException {
		super(repo, register, changeDate, absPath);
	}

	/**
	 * The copy constructor for this format.
	 * 
	 * @param other
	 *            The object to clone.
	 * @throws RegisterException
	 */
	public TabSeparatedRunResultFormat(final TabSeparatedRunResultFormat other)
			throws RegisterException {
		super(other);
	}

}
