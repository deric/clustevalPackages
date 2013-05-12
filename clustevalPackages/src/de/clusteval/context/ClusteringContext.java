package de.clusteval.context;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import de.clusteval.data.dataset.format.DataSetFormat;
import de.clusteval.data.dataset.format.UnknownDataSetFormatException;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.run.result.format.RunResultFormat;
import de.clusteval.run.result.format.UnknownRunResultFormatException;

/**
 * This is the default context of the framework, concerning clustering tasks.
 * 
 * @author Christian Wiwie
 * 
 */
public class ClusteringContext extends Context {

	/**
	 * @param repository
	 * @param register
	 * @param changeDate
	 * @param absPath
	 * @throws RegisterException
	 */
	public ClusteringContext(Repository repository, boolean register,
			long changeDate, File absPath) throws RegisterException {
		super(repository, register, changeDate, absPath);
	}

	/**
	 * @param other
	 * @throws RegisterException
	 */
	public ClusteringContext(final ClusteringContext other)
			throws RegisterException {
		super(other);
	}

	@Override
	public String getName() {
		return "Clustering context";
	}

	@Override
	public Set<String> getRequiredJavaClassFullNames() {
		return new HashSet<String>(Arrays.asList(new String[]{
				"de.clusteval.data.dataset.format.SimMatrixDataSetFormat",
				"de.clusteval.run.result.format.TabSeparatedRunResultFormat"}));
	}

	@Override
	public DataSetFormat getStandardInputFormat() {
		try {
			// take the newest version
			return DataSetFormat.parseFromString(repository,
					"SimMatrixDataSetFormat");
		} catch (UnknownDataSetFormatException e) {
			e.printStackTrace();
			// should not occur, because we checked this in the repository using
			// #getRequiredJavaClassFullNames()
		}
		return null;
	}

	@Override
	public RunResultFormat getStandardOutputFormat() {
		try {
			// take the newest version
			return RunResultFormat.parseFromString(repository,
					"TabSeparatedRunResultFormat");
		} catch (UnknownRunResultFormatException e) {
			e.printStackTrace();
			// should not occur, because we checked this in the repository using
			// #getRequiredJavaClassFullNames()
		}
		return null;
	}

}
