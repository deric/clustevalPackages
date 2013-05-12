/**
 * 
 */
package de.clusteval.data.dataset.format;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import utils.ArraysExt;
import utils.SimilarityMatrix;
import utils.parse.SimFileMatrixParser;
import utils.parse.SimFileParser.SIM_FILE_FORMAT;
import utils.parse.SimilarityFileNormalizer;
import utils.parse.TextFileParser;
import utils.parse.TextFileParser.OUTPUT_MODE;
import de.clusteval.data.dataset.DataSet;
import de.clusteval.data.dataset.DataSetAttributeFilterer;
import de.clusteval.data.dataset.RelativeDataSet;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.utils.FormatVersion;

/**
 * @author Christian Wiwie
 * 
 */
@FormatVersion(version = 1)
public class TransClustSimMatrixDataSetFormatParser extends DataSetFormatParser {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * data.dataset.format.DataSetFormatParser#convertToStandardFormat(data.
	 * dataset.DataSet)
	 */
	@Override
	public DataSet convertToStandardFormat(DataSet dataSet,
			ConversionInputToStandardConfiguration config) throws IOException,
			InvalidDataSetFormatVersionException, RegisterException,
			UnknownDataSetFormatException {
		switch (dataSet.getDataSetFormat().getVersion()) {
			case 1 :
				return convertToStandardFormat_v1(dataSet, config);
			default :
				throw new InvalidDataSetFormatVersionException("Version "
						+ dataSet.getDataSetFormat().getVersion()
						+ " is unknown for DataSetFormat "
						+ dataSet.getDataSetFormat());
		}
	}

	@SuppressWarnings("unused")
	protected DataSet convertToStandardFormat_v1(DataSet dataSet,
			ConversionInputToStandardConfiguration config) throws IOException,
			RegisterException, UnknownDataSetFormatException {
		// check if file already exists
		String resultFileName = dataSet.getAbsolutePath();
		resultFileName = removeResultFileNameSuffix(resultFileName);
		resultFileName += ".SimMatrix";
		final String resultFile = resultFileName;

		if (!(new File(resultFile).exists())) {
			this.log.debug("Converting input file...");
			final TransClustSimMatrixConverter p = new TransClustSimMatrixConverter(
					dataSet.getAbsolutePath(), resultFile);
			p.process();

			// normalize similarities to [0,1]
			if (this.normalize) {
				new SimilarityFileNormalizer(resultFile,
						SIM_FILE_FORMAT.MATRIX_HEADER, resultFile + ".tmp", 1.0)
						.process();
				new File(resultFile).delete();
				new File(resultFile + ".tmp").renameTo(new File(resultFile));
			}
		}
		return new RelativeDataSet(dataSet.getRepository(), false,
				System.currentTimeMillis(), new File(resultFileName),
				(RelativeDataSetFormat) DataSetFormat.parseFromString(
						dataSet.getRepository(), "SimMatrixDataSetFormat"),
				dataSet.getDataSetType());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * data.dataset.format.DataSetFormatParser#convertToThisFormat(data.dataset
	 * .DataSet)
	 */
	@SuppressWarnings("unused")
	@Override
	protected DataSet convertToThisFormat(DataSet dataSet,
			DataSetFormat dataSetFormat, ConversionConfiguration config) {
		return null;
	}

	class TransClustSimMatrixConverter extends TextFileParser {

		protected Map<String, Integer> keyToId;
		protected Map<Integer, String> idToKey;
		protected int proteinCount = -1;
		protected double[][] similarities;
		protected double maxSimilarity = -Double.MAX_VALUE;

		/**
		 * @param absFilePath
		 * @param outputPath
		 * @throws IOException
		 * 
		 */
		public TransClustSimMatrixConverter(final String absFilePath,
				final String outputPath) throws IOException {
			super(absFilePath, null, null, false, outputPath, OUTPUT_MODE.BURST);
			this.setLockTargetFile(true);
			this.skipEmptyLines = true;
			this.keyToId = new HashMap<String, Integer>();
			this.idToKey = new HashMap<Integer, String>();
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
			if (this.currentLine == 0) {
				proteinCount = Integer.valueOf(value[0]);
				this.similarities = new double[proteinCount][proteinCount];
			} else if (this.currentLine <= proteinCount) {
				int pos = this.keyToId.keySet().size();
				this.keyToId.put(value[0], pos);
				this.idToKey.put(pos, value[0]);
				if (this.currentLine == proteinCount) {
					this.setSplitChar("\t");
					this.splitLines = true;
					this.valueColumns = ArraysExt.range(0, proteinCount - 1);
				}
			} else {
				int i = (int) (this.currentLine - proteinCount);
				for (int j = 0; j < value.length; j++) {
					double sim = Double.valueOf(value[j]);
					this.similarities[i - 1][i + j] = sim;
					this.similarities[i + j][i - 1] = sim;
					if (sim > this.maxSimilarity)
						this.maxSimilarity = sim;
				}
				this.valueColumns = ArraysExt.range(0, proteinCount - 1 - i);
				/*
				 * Last line, set all self-similarities to the maximal value
				 */
				if (this.currentLine == 2 * proteinCount - 1) {
					for (int x = 0; x < this.similarities.length; x++) {
						this.similarities[x][x] = this.maxSimilarity;
					}
				}
			}
		}

		@Override
		protected String getBurstOutput() {
			// print header
			StringBuilder sb = new StringBuilder();
			for (int j = 0; j < this.similarities.length; j++) {
				sb.append("\t");
				sb.append(this.idToKey.get(j));
			}
			sb.append("\n");

			for (int i = 0; i < this.similarities.length; i++) {
				sb.append(this.idToKey.get(i));
				sb.append("\t");
				for (int j = 0; j < this.similarities[i].length; j++) {
					sb.append(this.similarities[i][j] + "");
					sb.append("\t");
				}
				sb.deleteCharAt(sb.length() - 1);
				sb.append("\n");
			}
			return sb.toString();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see data.dataset.format.DataSetFormatParser#parse(data.dataset.DataSet)
	 */
	@Override
	protected SimilarityMatrix parse(DataSet dataSet) throws IOException,
			InvalidDataSetFormatVersionException {
		switch (dataSet.getDataSetFormat().getVersion()) {
			case 1 :
				return parse_v1(dataSet);
			default :
				throw new InvalidDataSetFormatVersionException("Version "
						+ dataSet.getDataSetFormat().getVersion()
						+ " is unknown for DataSetFormat "
						+ dataSet.getDataSetFormat());
		}
	}

	protected SimilarityMatrix parse_v1(DataSet dataSet) throws IOException {

		/*
		 * Remove dataset attributes from file and write the result to
		 * dataSet.getAbsolutePath() + ".strip"
		 */
		DataSetAttributeFilterer filterer = new DataSetAttributeFilterer(
				dataSet.getAbsolutePath());
		filterer.process();

		// check if file already exists
		String resultFileName = dataSet.getAbsolutePath();
		resultFileName = removeResultFileNameSuffix(resultFileName);
		resultFileName += ".SimMatrix";
		final String resultFile = resultFileName;

		if (!(new File(resultFile).exists())) {
			this.log.debug("Converting input file...");
			final TransClustSimMatrixConverter p = new TransClustSimMatrixConverter(
					dataSet.getAbsolutePath() + ".strip", resultFile);
			p.process();
		}

		final SimFileMatrixParser p = new SimFileMatrixParser(resultFileName,
				SIM_FILE_FORMAT.MATRIX_HEADER, null, null, resultFile + ".tmp",
				OUTPUT_MODE.BURST, SIM_FILE_FORMAT.MATRIX_HEADER);
		new File(resultFile + ".tmp").delete();
		p.process();
		return p.getSimilarities();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.clusteval.data.dataset.format.DataSetFormatParser#writeToFile(de.clusteval
	 * .data.dataset.DataSet)
	 */
	@SuppressWarnings("unused")
	@Override
	protected void writeToFileHelper(DataSet dataSet, BufferedWriter writer) {
		return;
	}
}
