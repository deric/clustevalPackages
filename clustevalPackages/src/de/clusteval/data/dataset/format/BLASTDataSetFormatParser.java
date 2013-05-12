/**
 * 
 */
package de.clusteval.data.dataset.format;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import utils.SimilarityMatrix;
import utils.parse.SimFileMatrixParser;
import utils.parse.SimFileParser.ID_FILE_FORMAT;
import utils.parse.SimFileParser.SIM_FILE_FORMAT;
import utils.parse.SimilarityFileNormalizer;
import utils.parse.TextFileParser.OUTPUT_MODE;
import de.clusteval.data.dataset.DataSet;
import de.clusteval.data.dataset.DataSetAttributeFilterer;
import de.clusteval.data.dataset.RelativeDataSet;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.utils.FormatVersion;
import de.costmatrixcreation.main.Args;
import de.costmatrixcreation.main.Config;
import de.costmatrixcreation.main.Creator;

/**
 * @author Christian Wiwie
 * 
 */
@FormatVersion(version = 1)
public class BLASTDataSetFormatParser extends DataSetFormatParser {

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
			InvalidDataSetFormatVersionException, RegisterException,
			UnknownDataSetFormatException {
		switch (dataSet.getDataSetFormat().getVersion()) {
			case 1 :
				return convertToStandardFormat_v1(dataSet, config);
			default :
				throw new InvalidDataSetFormatVersionException("Version "
						+ dataSet.getDataSetFormat().getVersion()
						+ " is unknown for DataSetFormat "
						+ dataSet.getDataSetFormat().getClass().getSimpleName());
		}
	}

	@SuppressWarnings("unused")
	protected DataSet convertToStandardFormat_v1(DataSet dataSet,
			ConversionInputToStandardConfiguration config) throws IOException,
			RegisterException, UnknownDataSetFormatException {

		String resultFileName = dataSet.getAbsolutePath();
		resultFileName = removeResultFileNameSuffix(resultFileName);
		resultFileName += ".SimMatrix";

		final String resultFile = resultFileName;

		if (!(new File(resultFile).exists())) {
			this.log.debug("Converting input file...");

			final HashMap<Integer, String> proteins2integers;
			final HashMap<String, Integer> integers2proteins;
			/*
			 * Avoid overwriting of other threads
			 */
			synchronized (Config.source) {
				Config.init(new Args(new String[0]));
				Config.source = Config.BLAST;
				Config.gui = false;
				Config.fastaFile = dataSet.getAbsolutePath().replace(".strip",
						"")
						+ ".fasta";
				Config.blastFile = dataSet.getAbsolutePath();
				Config.similarityFile = dataSet.getAbsolutePath() + ".sim";
				Config.costModel = 0; // BeH
				proteins2integers = new HashMap<Integer, String>();
				integers2proteins = new HashMap<String, Integer>();
				Creator c = new Creator();
				c.run(proteins2integers, integers2proteins);
			}

			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
					dataSet.getAbsolutePath() + ".id")));
			for (String p : integers2proteins.keySet()) {
				bw.write(p);
				bw.newLine();
			}
			bw.close();

			SimFileMatrixParser parser = new SimFileMatrixParser(
					Config.similarityFile, SIM_FILE_FORMAT.ID_ID_SIM,
					dataSet.getAbsolutePath() + ".id", ID_FILE_FORMAT.ID);
			parser.process();
			SimilarityMatrix sims = parser.getSimilarities();

			bw = new BufferedWriter(new FileWriter(new File(
					Config.similarityFile)));
			String[] proteins = integers2proteins.keySet().toArray(
					new String[0]);
			for (int i = 0; i < sims.getRows(); i++) {
				for (int j = 0; j < sims.getColumns(); j++) {
					bw.write(proteins[i]);
					bw.write("\t");
					bw.write(proteins[j]);
					bw.write("\t");
					bw.write(sims.getSimilarity(i, j) + "");
					bw.newLine();
				}
			}
			bw.close();

			final SimFileMatrixParser p = new SimFileMatrixParser(
					Config.similarityFile, SIM_FILE_FORMAT.ID_ID_SIM, null,
					null, resultFile, OUTPUT_MODE.BURST,
					SIM_FILE_FORMAT.MATRIX_HEADER);
			p.process();
			if (this.normalize) {
				new SimilarityFileNormalizer(resultFile,
						SIM_FILE_FORMAT.MATRIX_HEADER, resultFile + ".tmp", 1.0)
						.process();
				new File(resultFile).delete();
				new File(resultFile + ".tmp").renameTo(new File(resultFile));
			}

			this.log.debug("done");
		}
		RelativeDataSetFormat targetFormat = (RelativeDataSetFormat) DataSetFormat
				.parseFromString(dataSet.getRepository(),
						"SimMatrixDataSetFormat");
		return new RelativeDataSet(dataSet.getRepository(), false,
				System.currentTimeMillis(), new File(resultFileName),
				targetFormat, dataSet.getDataSetType());
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
		if (!new File(dataSet.getAbsolutePath() + ".strip").exists()) {
			DataSetAttributeFilterer filterer = new DataSetAttributeFilterer(
					dataSet.getAbsolutePath());
			filterer.process();
		}

		// check if file already exists
		String resultFileName = dataSet.getAbsolutePath();
		resultFileName += ".sim";
		final String resultFile = resultFileName;

		if (!(new File(resultFile).exists())) {
			this.log.debug("Converting input file...");

			Config.init(new Args(new String[0]));
			Config.source = Config.BLAST;
			Config.gui = false;
			Config.fastaFile = dataSet.getAbsolutePath() + ".fasta";
			Config.blastFile = dataSet.getAbsolutePath() + ".strip";
			Config.similarityFile = dataSet.getAbsolutePath() + ".sim";
			Config.costModel = 0; // BeH
			final HashMap<Integer, String> proteins2integers = new HashMap<Integer, String>();
			final HashMap<String, Integer> integers2proteins = new HashMap<String, Integer>();
			Creator c = new Creator();
			c.run(proteins2integers, integers2proteins);

			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
					dataSet.getAbsolutePath() + ".id")));
			for (String p : integers2proteins.keySet()) {
				bw.write(p);
				bw.newLine();
			}
			bw.close();
			this.log.debug("done");
		}

		SimFileMatrixParser parser = new SimFileMatrixParser(
				dataSet.getAbsolutePath() + ".sim", SIM_FILE_FORMAT.ID_ID_SIM,
				dataSet.getAbsolutePath() + ".id", ID_FILE_FORMAT.ID);
		parser.process();

		// delete .sim and .id file
		if (new File(resultFile).exists()) {
			new File(resultFile).delete();
		}
		if (new File(dataSet.getAbsolutePath() + ".id").exists()) {
			new File(dataSet.getAbsolutePath() + ".id").delete();
		}

		return parser.getSimilarities();
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
