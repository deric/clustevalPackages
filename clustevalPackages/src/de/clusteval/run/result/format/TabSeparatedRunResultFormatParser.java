/**
 * 
 */
package de.clusteval.run.result.format;

import java.io.IOException;
import java.util.Map;

/**
 * This class is a dummy class and is not going to be used.
 * 
 * @author Christian Wiwie
 * 
 */
public class TabSeparatedRunResultFormatParser extends RunResultFormatParser {

	/**
	 * @param internalParams
	 * @param params
	 * @param absFilePath
	 * @throws IOException
	 */
	public TabSeparatedRunResultFormatParser(
			final Map<String, String> internalParams,
			final Map<String, String> params, final String absFilePath)
			throws IOException {
		super(internalParams, params, absFilePath);
	}

	@SuppressWarnings("unused")
	@Override
	protected void processLine(String[] key, String[] value) {
	}

	@SuppressWarnings("unused")
	@Override
	protected String getLineOutput(String[] key, String[] value) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * run.result.format.RunResultFormatParser#convertToStandardFormat(run.result
	 * .RunResult)
	 */
	@Override
	public void convertToStandardFormat() {
	}
}