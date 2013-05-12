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
			/*
			 * Remove attributes from the dataset
			 */
			if (!new File(dataSet.getAbsolutePath() + ".strip").exists()) {
				DataSetAttributeFilterer filterer = new DataSetAttributeFilterer(
						dataSet.getAbsolutePath());
				filterer.process();
			}
			final SimFileMatrixParser p;
			try {
				p = new SimFileMatrixParser(dataSet.getAbsolutePath()
						+ ".strip", SIM_FILE_FORMAT.MATRIX_HEADER, null, null,
						dataSet.getAbsolutePath() + ".tmp", OUTPUT_MODE.BURST,
						SIM_FILE_FORMAT.MATRIX_HEADER);
				p.process();
				new File(dataSet.getAbsolutePath() + ".tmp").delete();
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
		double[][] coords = matrix.toArray();

		// create sorted id array
		Map<String, Integer> idMap = matrix.getIds();
		String[] ids = new String[idMap.keySet().size()];
		for (Map.Entry<String, Integer> entry : idMap.entrySet())
			ids[entry.getValue()] = entry.getKey();

		StringBuilder sb = new StringBuilder();
		// add header line with ids
		for (String id : ids) {
			sb.append("\t");
			sb.append(id);
		}
		sb.append(System.getProperty("line.separator"));
		for (int i = 0; i < coords.length; i++) {
			sb.append(ids[i]);
			sb.append("\t");
			for (int j = 0; j < coords[i].length; j++) {
				sb.append(coords[i][j] + "\t");
			}
			sb.deleteCharAt(sb.length() - 1);
			sb.append(System.getProperty("line.separator"));
		}
		sb.deleteCharAt(sb.length() - 1);
		writer.append(sb.toString());
	}
}
