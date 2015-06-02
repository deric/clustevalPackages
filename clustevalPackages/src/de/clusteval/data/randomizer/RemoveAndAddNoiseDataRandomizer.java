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

import utils.Pair;
import utils.SimilarityMatrix;
import de.clusteval.cluster.Clustering;
import de.clusteval.data.dataset.AbsoluteDataSet;
import de.clusteval.data.dataset.DataMatrix;
import de.clusteval.data.dataset.DataSet;
import de.clusteval.data.dataset.RelativeDataSet;
import de.clusteval.data.dataset.DataSet.WEBSITE_VISIBILITY;
import de.clusteval.data.dataset.format.AbsoluteDataSetFormat;
import de.clusteval.data.dataset.format.DataSetFormat;
import de.clusteval.data.dataset.format.InvalidDataSetFormatVersionException;
import de.clusteval.data.dataset.format.RelativeDataSetFormat;
import de.clusteval.data.dataset.format.UnknownDataSetFormatException;
import de.clusteval.data.dataset.type.DataSetType;
import de.clusteval.data.dataset.type.UnknownDataSetTypeException;
import de.clusteval.data.goldstandard.GoldStandard;
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
	protected Pair<DataSet, GoldStandard> randomizeDataConfig() {
		if (this.dataConfig.getDatasetConfig().getDataSet() instanceof AbsoluteDataSet) {
			final AbsoluteDataSet dataSet = (AbsoluteDataSet) this.dataConfig
					.getDatasetConfig().getDataSet();
			final GoldStandard goldStandard = this.dataConfig
					.getGoldstandardConfig().getGoldstandard();

			try {
				MyRengine rEngine = repository.getRengineForCurrentThread();
				try {
					// the new dataset into file
					File dataSetFile = new File(dataConfig.getDatasetConfig()
							.getDataSet().getAbsolutePath()
							+ "_"
							+ this.uniqueId
							+ "_"
							+ getDataSetFileNamePostFix());

					File goldStandardFile = new File(dataConfig
							.getGoldstandardConfig().getGoldstandard()
							.getAbsolutePath()
							+ "_"
							+ this.uniqueId
							+ "_"
							+ getDataSetFileNamePostFix());

					// writer header
					String newAlias = this.uniqueId
							+ "_"
							+ dataConfig.getDatasetConfig().getDataSet()
									.getAlias() + getDataSetFileNamePostFix();

					if (!this.onlySimulate) {
						dataSet.loadIntoMemory();
						DataMatrix matrix = dataSet.getDataSetContent();
						String[] ids = matrix.getIds();
						goldStandard.loadIntoMemory();
						Clustering gsClustering = goldStandard.getClustering();
						String[] gs = new String[ids.length];
						for (int i = 0; i < ids.length; i++)
							gs[i] = gsClustering
									.getClusterForItem(
											gsClustering
													.getClusterItemWithId(ids[i]))
									.keySet().iterator().next().getId();

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
						rEngine.eval("c <- round(nrow(origDs)*remove);");
						rEngine.eval("removeIds <- sample(1:nrow(origDs), c);");
						rEngine.eval("c <- round(nrow(origDs)*noise);");
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
								+ "  removedGs <- as.matrix(origGs[-r,]);"
								+ "}");
						rEngine.eval("n <- noiseElems;");
						rEngine.eval("class(n) <- \"numeric\";");
						rEngine.eval("newDs <- rbind(removedDs,n);");
						rEngine.eval("newGs <- removedGs;");
						double[][] coords = rEngine.eval("newDs")
								.asDoubleMatrix();
						String[] newGs = rEngine.eval("newGs").asStrings();
						String[] newIds = rEngine.eval("rownames(newDs)")
								.asStrings();

						// write dataset file
						BufferedWriter writer = new BufferedWriter(
								new FileWriter(dataSetFile));
						writer.append("// alias = " + newAlias);
						writer.newLine();
						writer.append("// dataSetFormat = MatrixDataSetFormat");
						writer.newLine();
						writer.append("// dataSetType = SyntheticDataSetType");
						writer.newLine();
						writer.append("// dataSetFormatVersion = 1");
						writer.newLine();
						for (int row = 0; row < coords.length; row++) {
							writer.append(newIds[row]);
							for (int col = 0; col < coords[row].length; col++) {
								writer.append(String.format("\t%s",
										coords[row][col]));
							}
							writer.newLine();
						}
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
					}

					AbsoluteDataSet newDs = new AbsoluteDataSet(
							this.repository, false, dataSetFile.lastModified(),
							dataSetFile, this.onlySimulate ? newAlias
									+ System.currentTimeMillis() : newAlias,
							(AbsoluteDataSetFormat) DataSetFormat
									.parseFromString(repository,
											"MatrixDataSetFormat"),
							DataSetType.parseFromString(repository,
									"SyntheticDataSetType"),
							WEBSITE_VISIBILITY.HIDE);

					GoldStandard newGsObject = new GoldStandard(
							this.repository, goldStandardFile.lastModified(),
							goldStandardFile);

					return new Pair<DataSet, GoldStandard>(newDs, newGsObject);

				} catch (IOException e) {
					e.printStackTrace();
				} catch (UnknownDataSetFormatException e) {
					e.printStackTrace();
				} catch (RegisterException e) {
					e.printStackTrace();
				} catch (UnknownDataSetTypeException e) {
					e.printStackTrace();
				} catch (REngineException e) {
					e.printStackTrace();
				} catch (REXPMismatchException e) {
					e.printStackTrace();
				} catch (InvalidDataSetFormatVersionException e1) {
					e1.printStackTrace();
				} catch (IllegalArgumentException e1) {
					e1.printStackTrace();
				} catch (UnknownGoldStandardFormatException e) {
					e.printStackTrace();
				} finally {
					rEngine.clear();
					dataSet.unloadFromMemory();
					goldStandard.unloadFromMemory();
				}
			} catch (RserveException e2) {
				e2.printStackTrace();
			}

			return null;
		}
		//
		// relative data set
		//
		final RelativeDataSet dataSet = (RelativeDataSet) this.dataConfig
				.getDatasetConfig().getDataSet();
		final GoldStandard goldStandard = this.dataConfig
				.getGoldstandardConfig().getGoldstandard();

		try {
			MyRengine rEngine = repository.getRengineForCurrentThread();
			try {
				// the new dataset into file
				File dataSetFile = new File(FileUtils.buildPath(dataConfig
						.getDatasetConfig().getDataSet().getAbsolutePath()
						+ "_"
						+ this.uniqueId
						+ "_"
						+ getDataSetFileNamePostFix()));
				File goldStandardFile = new File(dataConfig
						.getGoldstandardConfig().getGoldstandard()
						.getAbsolutePath()
						+ "_"
						+ this.uniqueId
						+ "_"
						+ getDataSetFileNamePostFix());
				// writer header
				String newAlias = this.uniqueId + "_"
						+ dataConfig.getDatasetConfig().getDataSet().getAlias()
						+ getDataSetFileNamePostFix();

				if (!this.onlySimulate) {
					dataSet.loadIntoMemory();
					SimilarityMatrix matrix = dataSet.getDataSetContent();
					String[] ids = matrix.getIdsArray();
					goldStandard.loadIntoMemory();
					Clustering gsClustering = goldStandard.getClustering();
					String[] gs = new String[ids.length];
					for (int i = 0; i < ids.length; i++)
						gs[i] = gsClustering
								.getClusterForItem(
										gsClustering
												.getClusterItemWithId(ids[i]))
								.keySet().iterator().next().getId();

					rEngine.assign("origDs", matrix.toArray());
					rEngine.assign("origGs", gs);
					rEngine.assign("ids", ids);
					rEngine.eval("origGs <- as.matrix(origGs)");
					rEngine.eval("rownames(origDs) <- ids");
					rEngine.eval("colnames(origDs) <- ids");
					rEngine.eval("rownames(origGs)  <- ids");

					rEngine.eval("remove <- " + this.removePercentage);
					rEngine.eval("noise <- " + this.addPercentage);

					rEngine.eval("origGs.unique <- unique(origGs)");
					rEngine.eval("minMaxSim <- c(min(origDs),max(origDs))");
					rEngine.eval("minMaxDiagElems <- c(min(diag(origDs)),max(diag(origDs)))");
					rEngine.eval("c <- round(nrow(origDs)*remove);");
					rEngine.eval("removeIds <- sample(1:nrow(origDs), c);");
					rEngine.eval("if (length(removeIds) == 0) {"
							+ "  removedDs <- origDs;"
							+ "  removedGs <- origGs;" + "} else {"
							+ "  removedDs <- origDs[-removeIds,-removeIds];"
							+ "  removedGs <- as.matrix(origGs[-removeIds,]);"
							+ "}");
					rEngine.eval("c <- round(nrow(origDs)*noise);");
					rEngine.eval("if (c == 0) { noiseElems <- c(); newDs <- removedDs; } else {"
							+ "  noiseElems <- t(sapply(1:c, function(x) {"
							+ "		r <- sapply(1:nrow(removedDs),function(x){"
							+ "			runif(1, minMaxSim[1], minMaxSim[2])"
							+ "		});"
							+ "		tmp <- sapply(1:c,function(y){"
							+ "			if (x == y)"
							+ "				runif(1, minMaxDiagElems[1], minMaxDiagElems[2])"
							+ "			else"
							+ "				runif(1, minMaxSim[1], minMaxSim[2])"
							+ "		});"
							+ "		r <- c(r,tmp);"
							+ "  }));"
							+ "  rownames(noiseElems) <- paste(\"noise\",1:nrow(noiseElems),sep=\"\");"
							+ "  class(noiseElems) <- \"numeric\";"
							+ "  newDs <- cbind(rbind(removedDs,noiseElems[,1:(ncol(noiseElems)-c)]),t(noiseElems));"
							+ "}");
					rEngine.eval("newGs <- removedGs;");
					double[][] coords = rEngine.eval("newDs").asDoubleMatrix();
					String[] newGs = rEngine.eval("newGs").asStrings();
					String[] newIds = rEngine.eval("rownames(newDs)")
							.asStrings();

					// write dataset file
					BufferedWriter writer = new BufferedWriter(new FileWriter(
							dataSetFile));
					writer.append("// alias = " + newAlias);
					writer.newLine();
					writer.append("// dataSetFormat = SimMatrixDataSetFormat");
					writer.newLine();
					writer.append("// dataSetType = SyntheticDataSetType");
					writer.newLine();
					writer.append("// dataSetFormatVersion = 1");
					writer.newLine();
					for (String id : newIds)
						writer.append(String.format("\t%s", id));
					writer.newLine();
					for (int row = 0; row < coords.length; row++) {
						writer.append(newIds[row]);
						for (int col = 0; col < coords.length; col++) {
							writer.append(String.format("\t%s",
									coords[row][col]));
						}
						writer.newLine();
					}
					writer.close();

					// write goldstandard file
					writer = new BufferedWriter(
							new FileWriter(goldStandardFile));
					for (int row = 0; row < newGs.length; row++) {
						writer.append(newIds[row] + "\t" + newGs[row] + ":1.0");
						writer.newLine();
					}
					writer.close();
				}

				RelativeDataSet newDs = new RelativeDataSet(this.repository,
						false, dataSetFile.lastModified(), dataSetFile,
						this.onlySimulate ? newAlias
								+ System.currentTimeMillis() : newAlias,
						(RelativeDataSetFormat) DataSetFormat.parseFromString(
								repository, "SimMatrixDataSetFormat"),
						DataSetType.parseFromString(repository,
								"SyntheticDataSetType"),
						WEBSITE_VISIBILITY.HIDE);

				GoldStandard newGsObject = new GoldStandard(this.repository,
						goldStandardFile.lastModified(), goldStandardFile);

				return new Pair<DataSet, GoldStandard>(newDs, newGsObject);

			} catch (IOException e) {
				e.printStackTrace();
			} catch (UnknownDataSetFormatException e) {
				e.printStackTrace();
			} catch (RegisterException e) {
				e.printStackTrace();
			} catch (UnknownDataSetTypeException e) {
				e.printStackTrace();
			} catch (REngineException e) {
				e.printStackTrace();
			} catch (REXPMismatchException e) {
				e.printStackTrace();
			} catch (InvalidDataSetFormatVersionException e1) {
				e1.printStackTrace();
			} catch (IllegalArgumentException e1) {
				e1.printStackTrace();
			} catch (UnknownGoldStandardFormatException e) {
				e.printStackTrace();
			} finally {
				rEngine.clear();
				dataSet.unloadFromMemory();
				goldStandard.unloadFromMemory();
			}
		} catch (RserveException e) {
			e.printStackTrace();
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.clusteval.data.randomizer.DataRandomizer#getDataSetFileNamePostFix()
	 */
	@Override
	protected String getDataSetFileNamePostFix() {
		return "remove_" + removePercentage + "_noise_" + addPercentage;
	}
}
