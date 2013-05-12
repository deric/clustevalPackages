/**
 * 
 */
package de.clusteval.run.result.format;

import java.io.IOException;
import java.util.Map;

/**
 * @author Christian Wiwie
 * 
 */
public class TransClustRunResultFormatParser extends RunResultFormatParser {

	/**
	 * @param internalParams
	 * @param params
	 * @param absFilePath
	 * @throws IOException
	 */
	public TransClustRunResultFormatParser(
			final Map<String, String> internalParams,
			final Map<String, String> params, final String absFilePath)
			throws IOException {
		super(internalParams, params, absFilePath);
		this.countLines();
	}

	@Override
	protected void processLine(@SuppressWarnings("unused") String[] key,
			@SuppressWarnings("unused") String[] value) {
	}

	@Override
	protected String getLineOutput(@SuppressWarnings("unused") String[] key,
			String[] value) {
		StringBuilder sb = new StringBuilder();
		if (this.currentLine == 0) {
			sb.append("T");
			sb.append("\t");
			sb.append("Clustering");
			sb.append(System.getProperty("line.separator"));
		}
		sb.append(value[0]);
		sb.append(this.outSplit);
		sb.append(value[2].replaceAll(",", ":1.0,").replaceAll(";", ":1.0;"));
		sb.append(":1.0");
		sb.append(System.getProperty("line.separator"));
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see run.result.format.RunResultFormatParser#convertToStandardFormat()
	 */
	@Override
	public void convertToStandardFormat() throws IOException {
		this.process();
	}
}