/*******************************************************************************
 * Copyright (c) 2015 Mikkel Hansen.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Mikkel Hansen - initial API and implementation
 ******************************************************************************/
/**
 * 
 */
package de.clusteval.data.dataset.generator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RserveException;

import de.clusteval.data.goldstandard.GoldStandard;
import de.clusteval.framework.RLibraryRequirement;
import de.clusteval.framework.repository.MyRengine;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.wiwie.wiutils.file.FileUtils;
import de.wiwie.wiutils.utils.ArraysExt;

/**
 * @author Christian Wiwie
 * 
 */
@RLibraryRequirement(requiredRLibraries = {"clusterGeneration"})
public class QiuJoeCovarianceClusterDataSetGenerator extends DataSetGenerator {

	protected int numberOfPoints;

	protected boolean clusterSizesDifferent;

	protected int numberClusters;

	protected int numberNonNoisyFeatures;

	protected int numberNoisyFeatures;

	protected double clusterSeparation;

	/**
	 * Temp variable for the goldstandard classes.
	 */
	private int[] classes;

	/**
	 * @param repository
	 * @param register
	 * @param changeDate
	 * @param absPath
	 * @throws RegisterException
	 */
	public QiuJoeCovarianceClusterDataSetGenerator(Repository repository, boolean register, long changeDate,
			File absPath) throws RegisterException {
		super(repository, register, changeDate, absPath);
	}

	/**
	 * @param other
	 * @throws RegisterException
	 */
	public QiuJoeCovarianceClusterDataSetGenerator(DataSetGenerator other) throws RegisterException {
		super(other);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see data.dataset.generator.DataSetGenerator#getOptions()
	 */
	@Override
	protected Options getOptions() {
		Options options = new Options();

		OptionBuilder.withArgName("n");
		OptionBuilder.isRequired();
		OptionBuilder.hasArg();
		OptionBuilder.withDescription("The number of points.");
		Option option = OptionBuilder.create("n");
		options.addOption(option);

		OptionBuilder.withDescription("Make the cluster sizes different.");
		option = OptionBuilder.create("sizes");
		options.addOption(option);

		OptionBuilder.withArgName("k");
		OptionBuilder.isRequired();
		OptionBuilder.hasArg();
		OptionBuilder.withDescription("The number of clusters.");
		option = OptionBuilder.create("k");
		options.addOption(option);

		OptionBuilder.withArgName("noisyfeatures");
		OptionBuilder.isRequired();
		OptionBuilder.hasArg();
		OptionBuilder.withDescription("The number of noisy features.");
		option = OptionBuilder.create("dn");
		options.addOption(option);

		OptionBuilder.withArgName("features");
		OptionBuilder.isRequired();
		OptionBuilder.hasArg();
		OptionBuilder.withDescription("The number of non-noisy (clustered/separated) features.");
		option = OptionBuilder.create("d");
		options.addOption(option);

		OptionBuilder.withArgName("clusterSeparation");
		OptionBuilder.isRequired();
		OptionBuilder.hasArg();
		OptionBuilder.withDescription("The cluster separation (between -1.0 and +1.0).");
		option = OptionBuilder.create("s");
		options.addOption(option);

		return options;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see data.dataset.generator.DataSetGenerator#generatesGoldStandard()
	 */
	@Override
	public boolean generatesGoldStandard() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * data.dataset.generator.DataSetGenerator#handleOptions(org.apache.commons
	 * .cli.CommandLine)
	 */
	@Override
	protected void handleOptions(CommandLine cmd) throws ParseException {
		// TODO: throws an error with non-existing parameter 0????WTF?
		// if (cmd.getArgList().size() > 0)
		// throw new ParseException("Unknown parameters: " +
		// Arrays.toString(cmd.getArgs()));

		if (cmd.hasOption("n"))
			this.numberOfPoints = Integer.parseInt(cmd.getOptionValue("n"));
		else
			this.numberOfPoints = 100;

		if (cmd.hasOption("sizes"))
			this.clusterSizesDifferent = true;
		else
			this.clusterSizesDifferent = false;

		if (cmd.hasOption("k"))
			this.numberClusters = Integer.parseInt(cmd.getOptionValue("k"));
		else
			this.numberClusters = 5;

		if (cmd.hasOption("d"))
			this.numberNonNoisyFeatures = Integer.parseInt(cmd.getOptionValue("d"));
		else
			this.numberNonNoisyFeatures = 5;

		if (cmd.hasOption("dn"))
			this.numberNoisyFeatures = Integer.parseInt(cmd.getOptionValue("dn"));
		else
			this.numberNoisyFeatures = 0;

		if (cmd.hasOption("s"))
			this.clusterSeparation = Double.parseDouble(cmd.getOptionValue("s"));
		else
			this.clusterSeparation = 0.01;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see data.dataset.generator.DataSetGenerator#generateDataSet()
	 */
	@Override
	protected void generateDataSet() throws DataSetGenerationException, InterruptedException {
		try {

			String fileName = "test";
			String covMethod = "c-vine";

			int[] clustSizes;

			if (this.clusterSizesDifferent) {
				clustSizes = new int[this.numberClusters];

				int samplesDivided = (int) Math
						.ceil(this.numberOfPoints / ArraysExt.sum(ArraysExt.range(1, this.numberClusters)));
				for (int i = 0; i < clustSizes.length; i++)
					clustSizes[i] = (i + 1) * samplesDivided;
			} else {
				clustSizes = ArraysExt.rep((int) Math.round((double) this.numberOfPoints / this.numberClusters),
						this.numberClusters);
			}

			MyRengine rEngine = repository.getRengineForCurrentThread();
			rEngine.eval("library(clusterGeneration)");
			rEngine.assign("clustSizes", clustSizes);
			rEngine.eval(String.format(
					"result <- genRandomClust(numClust=%d,sepVal=%f,numNonNoisy=%d,numNoisy=%d,numReplicate=1,fileName='%s',"
							+ "clustszind=3,covMethod='%s',clustSizes=clustSizes,outputDatFlag=FALSE,outputLogFlag=FALSE,outputEmpirical=FALSE,"
							+ "outputInfo=FALSE);",
					this.numberClusters, this.clusterSeparation, this.numberNonNoisyFeatures, this.numberNoisyFeatures,
					fileName, covMethod));

			coords = rEngine.eval(String.format("result$datList$%s_1", fileName)).asDoubleMatrix();
			classes = rEngine.eval(String.format("result$memList$%s_1", fileName)).asIntegers();

		} catch (Exception e) {
			throw new DataSetGenerationException("The dataset could not be generated!");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see data.dataset.generator.DataSetGenerator#generateGoldStandard()
	 */
	@Override
	protected GoldStandard generateGoldStandard() throws GoldStandardGenerationException {

		try {
			// goldstandard file
			File goldStandardFile = new File(FileUtils.buildPath(this.repository.getBasePath(GoldStandard.class),
					this.getFolderName(), this.getFileName()));
			BufferedWriter writer = new BufferedWriter(new FileWriter(goldStandardFile));
			for (int row = 0; row < classes.length; row++) {
				writer.append((row + 1) + "\t" + classes[row] + ":1.0");
				writer.newLine();
			}
			writer.close();

			return new GoldStandard(repository, goldStandardFile.lastModified(), goldStandardFile);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (RegisterException e) {
			e.printStackTrace();
		}
		throw new GoldStandardGenerationException("The goldstandard could not be generated!");
	}
}
