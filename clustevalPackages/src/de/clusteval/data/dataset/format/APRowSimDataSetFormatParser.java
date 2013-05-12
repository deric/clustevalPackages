/**
 * 
 */
package de.clusteval.data.dataset.format;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import utils.SimilarityMatrix;
import utils.parse.SimFileMatrixParser;
import utils.parse.SimFileParser;
import utils.parse.SimFileParser.SIM_FILE_FORMAT;
import utils.parse.TextFileParser.OUTPUT_MODE;
import de.clusteval.data.dataset.DataSet;
import de.clusteval.data.dataset.DataSetAttributeParser;
import de.clusteval.data.dataset.RelativeDataSet;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.utils.FormatVersion;

/**
 * @author Christian Wiwie
 * 
 */
@FormatVersion(version = 1)
public class APRowSimDataSetFormatParser extends DataSetFormatParser {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * data.dataset.format.DataSetFormatParser#convertToStandardFormat(data.
	 * dataset.DataSet)
	 */
	@SuppressWarnings("unused")
	@Override
	protected DataSet convertToStandardFormat(DataSet ds,
			ConversionInputToStandardConfiguration config) {
		return null;
	}

	@Override
	protected DataSet convertToThisFormat(DataSet dataSet,
			DataSetFormat dataSetFormat, ConversionConfiguration config)
			throws IOException, InvalidDataSetFormatVersionException,
			RegisterException, UnknownDataSetFormatException {
		switch (dataSetFormat.getVersion()) {
			case 1 :
				return convertToThisFormat_v1(dataSet, dataSetFormat, config);
			default :
				throw new InvalidDataSetFormatVersionException("Version "
						+ dataSet.getDataSetFormat().getVersion()
						+ " is unknown for DataSetFormat "
						+ dataSet.getDataSetFormat());
		}
	}

	@SuppressWarnings("unused")
	protected DataSet convertToThisFormat_v1(DataSet dataSet,
			DataSetFormat dataSetFormat, ConversionConfiguration config)
			throws IOException, RegisterException,
			UnknownDataSetFormatException {

		// check if file already exists
		String absResultFilePath = dataSet.getAbsolutePath();
		absResultFilePath = removeResultFileNameSuffix(absResultFilePath);
		absResultFilePath += ".APRowSim";
		String resultFile = absResultFilePath;

		if (!(new File(resultFile).exists())) {
			this.log.debug("Converting input file...");
			// replace IDs by numeric values from [1:N]
			final SimFileParser p = new APSimFileConverter(
					dataSet.getAbsolutePath(), SIM_FILE_FORMAT.MATRIX_HEADER,
					null, null, resultFile, OUTPUT_MODE.STREAM,
					SIM_FILE_FORMAT.ID_ID_SIM);
			p.process();
			this.log.debug("Finished converting");
		}
		return new RelativeDataSet(dataSet.getRepository(), false,
				System.currentTimeMillis(), new File(absResultFilePath),
				new APRowSimDataSetFormat(dataSet.getRepository(), false,
						System.currentTimeMillis(),
						new File(absResultFilePath), dataSet.getRepository()
								.getCurrentDataSetFormatVersion(
										APRowSimDataSetFormat.class
												.getSimpleName())),
				dataSet.getDataSetType());
	}

	class APSimFileConverter extends SimFileMatrixParser {

		protected BufferedWriter mappingWriter;

		/**
		 * @param absFilePath
		 * @param simFileFormat
		 * @param absIdFilePath
		 * @param idFileFormat
		 * @param outputFile
		 * @param outputMode
		 * @param outputFormat
		 * @throws IOException
		 */
		public APSimFileConverter(String absFilePath,
				SIM_FILE_FORMAT simFileFormat, String absIdFilePath,
				ID_FILE_FORMAT idFileFormat, String outputFile,
				OUTPUT_MODE outputMode, SIM_FILE_FORMAT outputFormat)
				throws IOException {
			super(absFilePath, simFileFormat, absIdFilePath, idFileFormat,
					outputFile, outputMode, outputFormat);
		}

		@Override
		protected void resetReader() throws IOException {
			super.resetReader();

			if (this.mappingWriter != null) {
				this.mappingWriter.close();
			}
			this.mappingWriter = new BufferedWriter(new FileWriter(new File(
					this.outputFile + ".map")));
		};

		@Override
		protected void closeStreams() throws IOException {
			super.closeStreams();

			if (this.mappingWriter != null) {
				this.mappingWriter.close();
				this.mappingWriter = null;
			}
		};

		@Override
		public void finishProcess() {
			try {
				for (String key : this.idToKey.values()) {
					this.mappingWriter.write(key);
					this.mappingWriter.write(this.outSplit);
					this.mappingWriter.write((this.getIdForKey(key)) + "");
					this.mappingWriter.newLine();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		};

		@Override
		public int getIdForKey(String key) {
			return super.getIdForKey(key) + 1;
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

		@Override
		protected String getLineOutput(String[] key, String[] value) {
			if (this.outputFormat.equals(SIM_FILE_FORMAT.ID_ID_SIM)) {
				StringBuilder sb = new StringBuilder();
				if (this.currentLine == 0)
					return "";
				for (int i = 0; i < value.length; i++) {
					if (this.getIdForKey(key[0]) == i + 1)
						continue;
					sb.append(this.getIdForKey(key[0]) + this.outSplit
							+ (i + 1) + this.outSplit
							+ this.similarities.getSimilarity(0, i) + "\n");
				}
				return sb.toString();
			}
			return super.getLineOutput(key, value);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see data.dataset.format.DataSetFormatParser#parse(data.dataset.DataSet)
	 */
	@SuppressWarnings("unused")
	@Override
	protected SimilarityMatrix parse(DataSet dataSet) {
		return null;
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
	}
}
