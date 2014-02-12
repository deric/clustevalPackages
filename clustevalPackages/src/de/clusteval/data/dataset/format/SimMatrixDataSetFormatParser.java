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
package de.clusteval.data.dataset.format;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import utils.SimilarityMatrix;
import utils.parse.SimFileMatrixParser;
import utils.parse.SimFileParser.SIM_FILE_FORMAT;
import utils.parse.TextFileParser.OUTPUT_MODE;
import de.clusteval.data.dataset.DataSet;
import de.clusteval.data.dataset.DataSetAttributeFilterer;
import de.clusteval.data.dataset.RelativeDataSet;
import de.clusteval.framework.ClustevalBackendServer;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.utils.FormatVersion;

/**
 * @author Christian Wiwie
 */
@FormatVersion(version = 1)
public class SimMatrixDataSetFormatParser extends DataSetFormatParser {

	/*
	 * (non-Javadoc)
	 * 
	 * @see data.dataset.format.DataSetFormat#parseDataSet(data.dataset.DataSet)
	 */
	@Override
	protected SimilarityMatrix parse(DataSet dataSet)
			throws IllegalArgumentException, IOException,
			InvalidDataSetFormatVersionException {

		File sourceFile = ClustevalBackendServer.getCommonFile(new File(dataSet
				.getAbsolutePath()));
		synchronized (sourceFile) {
			// TODO: symmetry
			final SimFileMatrixParser p;
			try {
				p = new SimFileMatrixParser(dataSet.getAbsolutePath(),
						SIM_FILE_FORMAT.MATRIX_HEADER, null, null, null,
						OUTPUT_MODE.BURST, SIM_FILE_FORMAT.MATRIX_HEADER);
				p.process();
				return p.getSimilarities();
			} catch (IOException e) {
				throw new InvalidDataSetFormatVersionException(e.getMessage());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.clusteval.data.dataset.format.DataSetFormatParser#convertToStandardFormat
	 * (de.clusteval.data.dataset.DataSet,
	 * de.clusteval.data.dataset.format.ConversionInputToStandardConfiguration)
	 */
	@SuppressWarnings("unused")
	@Override
	protected DataSet convertToStandardFormat(DataSet dataSet,
			ConversionInputToStandardConfiguration config) throws IOException,
			InvalidDataSetFormatVersionException, RegisterException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.clusteval.data.dataset.format.DataSetFormatParser#convertToThisFormat
	 * (de.clusteval.data.dataset.DataSet,
	 * de.clusteval.data.dataset.format.DataSetFormat,
	 * de.clusteval.data.dataset.format.ConversionConfiguration)
	 */
	@SuppressWarnings("unused")
	@Override
	protected DataSet convertToThisFormat(DataSet dataSet,
			DataSetFormat dataSetFormat, ConversionConfiguration config)
			throws IOException, InvalidDataSetFormatVersionException,
			RegisterException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.clusteval.data.dataset.format.DataSetFormatParser#writeToFile(de.clusteval
	 * .data.dataset.DataSet)
	 */
	@Override
	protected void writeToFileHelper(DataSet dataSet, BufferedWriter writer)
			throws IOException {
		RelativeDataSet absDataSet = (RelativeDataSet) dataSet;
		SimilarityMatrix matrix = absDataSet.getDataSetContent();

		// create sorted id array
		Map<String, Integer> idMap = matrix.getIds();
		String[] ids = new String[idMap.keySet().size()];
		for (Map.Entry<String, Integer> entry : idMap.entrySet())
			ids[entry.getValue()] = entry.getKey();

		// add header line with ids
		for (String id : ids) {
			writer.append("\t");
			writer.append(id);
		}
		// we write it line-was, such that we only keep the string for one line
		// in the memory. otherwise we might encounter problems with huge
		// datasets.
		writer.append(System.getProperty("line.separator"));
		for (int i = 0; i < matrix.getRows(); i++) {
			StringBuilder sb = new StringBuilder();
			sb.append(ids[i]);
			sb.append("\t");
			for (int j = 0; j < matrix.getColumns(); j++) {
				sb.append(matrix.getSimilarity(i, j) + "\t");
			}
			sb.deleteCharAt(sb.length() - 1);
			if (i < matrix.getRows() - 1)
				sb.append(System.getProperty("line.separator"));
			writer.append(sb.toString());
		}
	}
}
