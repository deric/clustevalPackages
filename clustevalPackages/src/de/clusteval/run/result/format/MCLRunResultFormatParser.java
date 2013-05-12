/**
 * 
 */
package de.clusteval.run.result.format;

import java.io.IOException;
import java.util.Map;

import utils.StringExt;

/**
 * @author Christian Wiwie
 * 
 */
public class MCLRunResultFormatParser extends RunResultFormatParser {

	/**
	 * @param internalParams
	 * @param params
	 * @param absFilePath
	 * @throws IOException
	 */
	public MCLRunResultFormatParser(final Map<String, String> internalParams,
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
		if (currentLine == 0) {
			String I = null;
			for (String pa : params.keySet())
				if (pa.equals("I")) {
					I = params.get(pa);
					break;
				}

			sb.append("I");
			sb.append("\t");
			sb.append("Clustering");
			sb.append(System.getProperty("line.separator"));

			sb.append(I);
			sb.append("\t");
		}
		sb.append(StringExt.paste(",", StringExt.append(value, ":1.0")));
		if (currentLine < getTotalLineCount() - 1)
			sb.append(";");
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * run.result.format.RunResultFormatParser#convertToStandardFormat(run.result
	 * .RunResult)
	 */
	@Override
	public void convertToStandardFormat() throws IOException {
		this.process();
	}
}