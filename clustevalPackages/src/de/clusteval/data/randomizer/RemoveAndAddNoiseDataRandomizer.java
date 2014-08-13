/**
 * 
 */
package de.clusteval.data.randomizer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RserveException;

import de.clusteval.cluster.Clustering;
import de.clusteval.data.DataConfig;
import de.clusteval.data.dataset.AbsoluteDataSet;
import de.clusteval.data.dataset.DataMatrix;
import de.clusteval.data.dataset.DataSetConfig;
import de.clusteval.data.dataset.RelativeDataSet;
import de.clusteval.data.dataset.format.AbsoluteDataSetFormat;
import de.clusteval.data.dataset.format.DataSetFormat;
import de.clusteval.data.dataset.format.InvalidDataSetFormatVersionException;
import de.clusteval.data.dataset.format.UnknownDataSetFormatException;
import de.clusteval.data.dataset.type.DataSetType;
import de.clusteval.data.dataset.type.UnknownDataSetTypeException;
import de.clusteval.data.goldstandard.GoldStandard;
import de.clusteval.data.goldstandard.GoldStandardConfig;
import de.clusteval.data.goldstandard.format.UnknownGoldStandardFormatException;
import de.clusteval.framework.repository.MyRengine;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import file.FileUtils;

/**
 * @author Christian Wiwie
 *
 */
public class RemoveAndAddNoiseDataRandomizer extends DataRandomizer {

	protected double removePercentage;

	protected double addPercentage;

	/**
	 * @param other
	 * @throws RegisterException
	 */
	public RemoveAndAddNoiseDataRandomizer(RemoveAndAddNoiseDataRandomizer other)
			throws RegisterException {
		super(other);
	}

	/**
	 * @param repository
	 * @param register
	 * @param changeDate
	 * @param absPath
	 * @throws RegisterException
	 */
	public RemoveAndAddNoiseDataRandomizer(Repository repository,
			boolean register, long changeDate, File absPath)
			throws RegisterException {
		super(repository, register, changeDate, absPath);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.clusteval.data.dataset.randomizer.DataRandomizer#getOptions()
	 */
	@Override
	protected Options getOptions() {
		Options options = new Options();

		OptionBuilder.withArgName("remove");
		OptionBuilder.isRequired();
		OptionBuilder.hasArg();
		OptionBuilder.withDescription("The percentage of objects to remove.");
		Option option = OptionBuilder.create("remove");
		options.addOption(option);

		OptionBuilder.withArgName("add");
		OptionBuilder.isRequired();
		OptionBuilder.hasArg();
		OptionBuilder
				.withDescription("The percentage of random objects to add.");
		option = OptionBuilder.create("add");
		options.addOption(option);

		return options;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.clusteval.data.dataset.randomizer.DataRandomizer#handleOptions(org
	 * .apache.commons.cli.CommandLine)
	 */
	@Override
	protected void handleOptions(CommandLine cmd) throws ParseException {
		if (cmd.getArgList().size() > 0)
			throw new ParseException("Unknown parameters: "
					+ Arrays.toString(cmd.getArgs()));

		if (cmd.hasOption("remove"))
			this.removePercentage = Double.parseDouble(cmd
					.getOptionValue("remove"));
		else
			this.removePercentage = 0.05;

		if (cmd.hasOption("add"))
			this.addPercentage = Double.parseDouble(cmd.getOptionValue("add"));
		else
			this.addPercentage = 0.05;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.clusteval.data.dataset.randomizer.DataRandomizer#randomizeDataConfig()
	 */
	@Override
	protected DataConfig randomizeDataConfig() {
		if (this.dataConfig.getDatasetConfig().getDataSet() instanceof RelativeDataSet)
			throw new IllegalArgumentException(
					"This randomizer is only applicable to absolute coordinates");
		final AbsoluteDataSet dataSet = (AbsoluteDataSet) this.dataConfig
				.getDatasetConfig().getDataSet();
		final GoldStandard goldStandard = this.dataConfig
				.getGoldstandardConfig().getGoldstandard();

		String postFix = "_remove_" + removePercentage + "_noise_"
				+ addPercentage;
		try {
			dataSet.loadIntoMemory();
			DataMatrix matrix = dataSet.getDataSetContent();
			String[] ids = matrix.getIds();
			goldStandard.loadIntoMemory();
			Clustering gsClustering = goldStandard.getClustering();
			String[] gs = new String[gsClustering.size()];
			for (int i = 0; i < ids.length; i++)
				gs[i] = gsClustering
						.getClusterForItem(
								gsClustering.getClusterItemWithId(ids[i]))
						.keySet().iterator().next().getId();

			try {
				MyRengine rEngine = repository.getRengineForCurrentThread();
				try {
					rEngine.assign("origDs", matrix.getData());
					rEngine.assign("origGs", gs);
					rEngine.assign("ids", ids);
					rEngine.eval("origGs <- as.matrix(origGs)");
					rEngine.eval("rownames(origDs) <- ids");
					rEngine.eval("rownames(origGs)  <- ids");

					rEngine.eval("remove <- " + this.removePercentage);
					rEngine.eval("noise <- " + this.addPercentage);

					rEngine.eval("origGs.unique <- unique(origGs)");
					rEngine.eval("colExtr <- apply(origDs,MARGIN=2,function(x) { return (c(min(x),max(x)))})");
					rEngine.eval("c <- nrow(origDs)*remove;");
					rEngine.eval("removeIds <- sample(1:nrow(origDs), c);");
					rEngine.eval("c <- nrow(origDs)*noise;");
					rEngine.eval("if (c == 0) { noiseElems <- c() } else {"
							+ "  noiseElems <- t(sapply(1:c, function(x) {"
							+ "    r <- apply(colExtr,MARGIN=2,function(x){"
							+ "      runif(1, x[1], x[2])"
							+ "    });"
							+ "  }));"
							+ "  rownames(noiseElems) <- paste(\"noise\",1:nrow(noiseElems),sep=\"\");"
							+ "}");
					rEngine.eval("r <- removeIds;");
					rEngine.eval("if (length(r) == 0) {"
							+ "  removedDs <- origDs;"
							+ "  removedGs <- origGs;" + "} else {"
							+ "  removedDs <- origDs[-r,];"
							+ "  removedGs <- as.matrix(origGs[-r,]);" + "}");
					rEngine.eval("n <- noiseElems;");
					rEngine.eval("class(n) <- \"numeric\";");
					rEngine.eval("newDs <- rbind(removedDs,n);");
					rEngine.eval("newGs <- removedGs;");
					double[][] coords = rEngine.eval("newDs").asDoubleMatrix();
					String[] newGs = rEngine.eval("newGs").asStrings();
					String[] newIds = rEngine.eval("rownames(newDs)")
							.asStrings();

					// the new dataset into file
					File dataSetFile = new File(dataConfig.getDatasetConfig()
							.getDataSet().getAbsolutePath()
							+ postFix);
					File goldStandardFile = new File(dataConfig
							.getGoldstandardConfig().getGoldstandard()
							.getAbsolutePath()
							+ postFix);

					try {
						// write dataset file
						BufferedWriter writer = new BufferedWriter(
								new FileWriter(dataSetFile));
						// writer header
						String newAlias = dataConfig.getDatasetConfig()
								.getDataSet().getAlias()
								+ postFix;
						writer.append("// alias = " + newAlias);
						writer.newLine();
						writer.append("// dataSetFormat = MatrixDataSetFormat");
						writer.newLine();
						writer.append("// dataSetType = SyntheticDataSetType");
						writer.newLine();
						writer.append("// dataSetFormatVersion = 1");
						writer.newLine();
						for (int row = 0; row < coords.length; row++) {
							writer.append(newIds[row] + "\t" + coords[row][0]
									+ "\t" + coords[row][1]);
							writer.newLine();
						}
						writer.close();

						AbsoluteDataSet newDs = new AbsoluteDataSet(
								this.repository, true,
								dataSetFile.lastModified(), dataSetFile,
								newAlias,
								(AbsoluteDataSetFormat) DataSetFormat
										.parseFromString(repository,
												"MatrixDataSetFormat"),
								DataSetType.parseFromString(repository,
										"SyntheticDataSetType"));

						// write dataset config file
						writer = new BufferedWriter(new FileWriter(
								FileUtils.buildPath(repository
										.getBasePath(DataSetConfig.class),
										this.dataConfig.getDatasetConfig()
												.toString()
												+ postFix
												+ ".dsconfig")));
						writer.append("datasetName = " + newDs.getMajorName());
						writer.newLine();
						writer.append("datasetFile = " + newDs.getMinorName());
						writer.newLine();
						writer.close();

						// write goldstandard file
						writer = new BufferedWriter(new FileWriter(
								goldStandardFile));
						for (int row = 0; row < newGs.length; row++) {
							writer.append(newIds[row] + "\t" + newGs[row]
									+ ":1.0");
							writer.newLine();
						}
						writer.close();

						GoldStandard newGsObject = new GoldStandard(
								this.repository,
								goldStandardFile.lastModified(),
								goldStandardFile);

						// write goldstandard config file
						writer = new BufferedWriter(new FileWriter(
								FileUtils.buildPath(repository
										.getBasePath(GoldStandardConfig.class),
										this.dataConfig.getGoldstandardConfig()
												.toString()
												+ postFix
												+ ".gsconfig")));
						writer.append("goldstandardName = "
								+ newGsObject.getMajorName());
						writer.newLine();
						writer.append("goldstandardFile = "
								+ newGsObject.getMinorName());
						writer.newLine();
						writer.close();

						// write data config file
						writer = new BufferedWriter(new FileWriter(
								FileUtils.buildPath(repository
										.getBasePath(DataConfig.class),
										this.dataConfig.getName() + postFix
												+ ".dataconfig")));
						writer.append("datasetConfig = "
								+ new File(this.dataConfig.getDatasetConfig()
										.toString() + postFix));
						writer.newLine();
						writer.append("goldstandardConfig = "
								+ new File(this.dataConfig
										.getGoldstandardConfig().toString()
										+ postFix));
						writer.newLine();
						writer.close();

					} catch (IOException e) {
						e.printStackTrace();
					} catch (UnknownDataSetFormatException e) {
						e.printStackTrace();
					} catch (RegisterException e) {
						e.printStackTrace();
					} catch (UnknownDataSetTypeException e) {
						e.printStackTrace();
					}
				} catch (REngineException e) {
					e.printStackTrace();
				} catch (REXPMismatchException e) {
					e.printStackTrace();
				} finally {
					rEngine.clear();
				}
			} catch (RserveException e) {
				e.printStackTrace();
			}
		} catch (InvalidDataSetFormatVersionException e1) {
			e1.printStackTrace();
		} catch (IllegalArgumentException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (UnknownDataSetFormatException e) {
			e.printStackTrace();
		} catch (UnknownGoldStandardFormatException e) {
			e.printStackTrace();
		} finally {
			dataSet.unloadFromMemory();
			goldStandard.unloadFromMemory();
		}
		return null;
	}
}
