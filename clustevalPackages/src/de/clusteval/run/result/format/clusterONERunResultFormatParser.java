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
package de.clusteval.run.result.format;

import java.io.IOException;
import java.util.Map;

import utils.StringExt;

/**
 * @author Christian Wiwie
 * 
 */
public class clusterONERunResultFormatParser extends RunResultFormatParser {

	/**
	 * @param internalParams
	 * @param params
	 * @param absFilePath
	 * @throws IOException
	 */
	public clusterONERunResultFormatParser(final Map<String, String> internalParams,
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
			String s = null;
			String d = null;
			for (String pa : params.keySet())
				if (pa.equals("s")) {
					s = params.get(pa);
				} else if (pa.equals("d")) {
					d = params.get(pa);
				}

			sb.append("s,d");
			sb.append("\t");
			sb.append("Clustering");
			sb.append(System.getProperty("line.separator"));

			sb.append(s);
			sb.append(",");
			sb.append(d);
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
