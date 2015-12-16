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
package de.clusteval.data.dataset.generator;

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
import org.rosuda.REngine.Rserve.RserveException;

import de.clusteval.data.goldstandard.GoldStandard;
import de.clusteval.framework.RLibraryRequirement;
import de.clusteval.framework.repository.MyRengine;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.wiwie.wiutils.file.FileUtils;

/**
 * @author Christian Wiwie
 * 
 */
@RLibraryRequirement(requiredRLibraries = {"mlbench"})
public class Gaussian2DDataSetGenerator extends DataSetGenerator {

	protected int numberOfPoints;

	protected int numberOfGaussians;

	protected double radius;

	protected double standardDeviations;

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
	public Gaussian2DDataSetGenerator(Repository repository, boolean register, long changeDate, File absPath)
			throws RegisterException {
		super(repository, register, changeDate, absPath);
	}

	/**
	 * The copy constructor of this class.
	 * 
	 * @param other
	 *            The object to clone.
	 * @throws RegisterException
	 */
	public Gaussian2DDataSetGenerator(Gaussian2DDataSetGenerator other) throws RegisterException {
		super(other);
		this.numberOfGaussians = other.numberOfGaussians;
		this.numberOfPoints = other.numberOfPoints;
		this.radius = other.radius;
		this.standardDeviations = other.standardDeviations;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see data.dataset.generator.DataSetGenerator#getOptions()
	 */
	@Override
	public Options getOptions() {
		Options options = new Options();

		// init valid command line options
		OptionBuilder.withArgName("radius");
		OptionBuilder.hasArg();
		OptionBuilder.withDescription("The radius of the circle on which the gaussians are located.");
		Option option = OptionBuilder.create("r");
		options.addOption(option);

		OptionBuilder.withArgName("sd");
		OptionBuilder.hasArg();
		OptionBuilder.withDescription("The standard deviation of the gaussians.");
		option = OptionBuilder.create("sd");
		options.addOption(option);

		OptionBuilder.withArgName("n");
		OptionBuilder.hasArg();
		OptionBuilder.withDescription("The number of points.");
		option = OptionBuilder.create("n");
		options.addOption(option);

		OptionBuilder.withArgName("cl");
		OptionBuilder.hasArg();
		OptionBuilder.withDescription("The number of gaussians.");
		option = OptionBuilder.create("cl");
		options.addOption(option);
		options.addOption(option);

		return options;
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
		if (cmd.getArgList().size() > 0)
			throw new ParseException("Unknown parameters: " + Arrays.toString(cmd.getArgs()));

		if (cmd.hasOption("cl"))
			this.numberOfGaussians = Integer.parseInt(cmd.getOptionValue("cl"));
		else
			this.numberOfGaussians = 2;

		if (cmd.hasOption("n"))
			this.numberOfPoints = Integer.parseInt(cmd.getOptionValue("n"));
		else
			this.numberOfPoints = 100;

		if (cmd.hasOption("radius"))
			this.radius = Double.parseDouble(cmd.getOptionValue("radius"));
		else
			this.radius = 1.0;

		if (cmd.hasOption("sd"))
			this.standardDeviations = Double.parseDouble(cmd.getOptionValue("sd"));
		else
			this.standardDeviations = 1.0;
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
	 * @see data.dataset.generator.DataSetGenerator#generateDataSet()
	 */
	@Override
	protected void generateDataSet() throws DataSetGenerationException, InterruptedException {
		try {
			MyRengine rEngine = repository.getRengineForCurrentThread();
			rEngine.eval("library(mlbench)");
			rEngine.eval("result <- mlbench.2dnormals(n=" + this.numberOfPoints + ",cl=" + this.numberOfGaussians
					+ ",r=" + this.radius + ",sd=" + this.standardDeviations + ");");
			coords = rEngine.eval("result$x").asDoubleMatrix();
			classes = rEngine.eval("result$classes").asIntegers();

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
