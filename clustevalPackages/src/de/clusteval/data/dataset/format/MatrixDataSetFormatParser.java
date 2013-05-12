/**
 * 
 */
package de.clusteval.data.dataset.format;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import utils.ArraysExt;
import utils.Pair;
import utils.SimilarityMatrix;
import utils.parse.TextFileParser;
import de.clusteval.data.dataset.AbsoluteDataSet;
import de.clusteval.data.dataset.DataMatrix;
import de.clusteval.data.dataset.DataSet;
import de.clusteval.data.dataset.DataSetAttributeParser;
import de.clusteval.data.dataset.RelativeDataSet;
import de.clusteval.data.distance.DistanceMeasure;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.utils.FormatVersion;
import de.clusteval.utils.RNotAvailableException;

/**
 * @author Christian Wiwie
 * 
 */
@FormatVersion(version = 1)
public class MatrixDataSetFormatParser extends DataSetFormatParser {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * data.dataset.format.DataSetFormatParser#convertToStandardFormat(data.
	 * dataset.DataSet)
	 */
	@Override
	protected DataSet convertToStandardFormat(DataSet dataSet,
			ConversionInputToStandardConfiguration config) throws IOException,
			RegisterException, UnknownDataSetFormatException,
			InvalidParameterException, RNotAvailableException {

		File targetFile = new File(dataSet.getAbsolutePath() + ".SimMatrix");

		RelativeDataSet newDataSet = new RelativeDataSet(
				dataSet.getRepository(), false, System.currentTimeMillis(),
				targetFile,
				(RelativeDataSetFormat) DataSetFormat.parseFromString(
						dataSet.getRepository(), "SimMatrixDataSetFormat"),
				dataSet.getDataSetType());

		if (!targetFile.exists()) {
			MatrixParser parser = new MatrixParser(dataSet.getAbsolutePath());
			parser.process();
			List<Pair<String, double[]>> coords = parser.getCoordinates();
			double[][] coordsMatrix = new double[coords.size()][];
			String[] ids = new String[coords.size()];
			for (int i = 0; i < coordsMatrix.length; i++) {
				coordsMatrix[i] = coords.get(i).getSecond();
				ids[i] = coords.get(i).getFirst();
			}

			DistanceMeasure dist = config
					.getDistanceMeasureAbsoluteToRelative();

			double[][] distances = null;
			if (dist.supportsMatrix()) {
				distances = dist.getDistances(coordsMatrix);
			}
			// 31.01.2013: Some measures require R for the
			// getDistances(double[][]) operation. In these cases, the return
			// type is null.
			if (distances == null) {
				distances = new double[coords.size()][coords.size()];

				for (int i = 0; i < distances.length; i++) {
					for (int j = i; j < distances.length; j++) {
						double result = dist.getDistance(coords.get(i)
								.getSecond(), coords.get(j).getSecond());
						distances[i][j] = result;
						distances[j][i] = result;
					}
				}
			}

			/*
			 * changed 23.09.2012 removed scaling and put max in subtract as
			 * first parameter
			 */
			// distances = ArraysExt.scaleBy(distances,
			// ArraysExt.max(distances));

			distances = ArraysExt.subtract(ArraysExt.max(distances), distances);

			if (this.normalize) {
				distances = ArraysExt.subtract(distances,
						ArraysExt.min(distances));
				distances = ArraysExt.scaleBy(distances,
						ArraysExt.max(distances));
			}

			SimilarityMatrix matrix = new SimilarityMatrix(ids, distances);
			newDataSet.setDataSetContent(matrix);
			newDataSet.writeToFile(false);
			newDataSet.unloadFromMemory();
		}
		return newDataSet;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * data.dataset.format.DataSetFormatParser#convertToThisFormat(data.dataset
	 * .DataSet, data.dataset.format.DataSetFormat)
	 */
	@SuppressWarnings("unused")
	@Override
	protected DataSet convertToThisFormat(DataSet dataSet,
			DataSetFormat dataSetFormat, ConversionConfiguration config)
			throws InvalidDataSetFormatVersionException {
		throw new InvalidDataSetFormatVersionException(
				"Cannot convert to this format");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see data.dataset.format.DataSetFormatParser#parse(data.dataset.DataSet)
	 */
	@Override
	protected DataMatrix parse(DataSet dataSet) throws IOException {
		MatrixParser parser = new MatrixParser(dataSet.getAbsolutePath());
		parser.process();
		List<Pair<String, double[]>> coords = parser.getCoordinates();
		String[] ids = new String[coords.size()];
		double[][] data = new double[coords.size()][];
		for (int i = 0; i < coords.size(); i++) {
			data[i] = coords.get(i).getSecond();
			ids[i] = coords.get(i).getFirst();
		}
		return new DataMatrix(ids, data);
	}

	class MatrixParser extends TextFileParser {

		protected List<Pair<String, double[]>> idToCoordinates;

		/**
		 * @param absFilePath
		 * @throws IOException
		 */
		public MatrixParser(String absFilePath) throws IOException {
			super(absFilePath, new int[0], new int[0]);
			this.setLockTargetFile(true);
			this.idToCoordinates = new ArrayList<Pair<String, double[]>>();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see utils.parse.TextFileParser#checkLine(java.lang.String)
		 */
		@Override
		protected boolean checkLine(String line) {
			return !DataSetAttributeParser.attributeLinePrefixPattern.matcher(
					line).matches();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see utils.parse.TextFileParser#processLine(java.lang.String[],
		 * java.lang.String[])
		 */
		@SuppressWarnings("unused")
		@Override
		protected void processLine(String[] key, String[] value) {
			double[] coords = new double[value.length - 1];
			for (int i = 1; i < value.length; i++)
				coords[i - 1] = Double.valueOf(value[i]);
			this.idToCoordinates.add(Pair.getPair(value[0], coords));
		}

		public List<Pair<String, double[]>> getCoordinates() {
			return this.idToCoordinates;
		}
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
		AbsoluteDataSet absDataSet = (AbsoluteDataSet) dataSet;
		DataMatrix dataMatrix = absDataSet.getDataSetContent();
		String[] ids = dataMatrix.getIds();
		double[][] coords = dataMatrix.getData();

		StringBuilder sb = new StringBuilder();
		for (int row = 0; row < coords.length; row++) {
			sb.append(ids[row]);
			sb.append("\t");
			for (int col = 0; col < coords[row].length; col++) {
				sb.append(coords[row][col]);
				sb.append("\t");
			}
			sb.deleteCharAt(sb.length() - 1);

			sb.append(System.getProperty("line.separator"));
		}
		writer.append(sb.toString());
	}
}