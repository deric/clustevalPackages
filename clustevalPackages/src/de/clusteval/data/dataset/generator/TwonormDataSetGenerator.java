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

import de.clusteval.data.dataset.AbsoluteDataSet;
import de.clusteval.data.dataset.DataSet;
import de.clusteval.data.dataset.format.AbsoluteDataSetFormat;
import de.clusteval.data.dataset.format.DataSetFormat;
import de.clusteval.data.dataset.format.UnknownDataSetFormatException;
import de.clusteval.data.dataset.type.DataSetType;
import de.clusteval.data.dataset.type.UnknownDataSetTypeException;
import de.clusteval.data.goldstandard.GoldStandard;
import de.clusteval.framework.RLibraryRequirement;
import de.clusteval.framework.repository.MyRengine;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import file.FileUtils;

/**
 * @author Christian Wiwie
 * 
 */
@RLibraryRequirement(requiredRLibraries = {"mlbench"})
public class TwonormDataSetGenerator extends DataSetGenerator {

	protected int numberOfPoints;

	protected int numberDimensions;

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
	public TwonormDataSetGenerator(Repository repository, boolean register,
			long changeDate, File absPath) throws RegisterException {
		super(repository, register, changeDate, absPath);
	}

	/**
	 * @param other
	 * @throws RegisterException
	 */
	public TwonormDataSetGenerator(DataSetGenerator other)
			throws RegisterException {
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

		OptionBuilder.withArgName("d");
		OptionBuilder.hasArg();
		OptionBuilder.withDescription("The number of dimensions.");
		option = OptionBuilder.create("d");
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
		if (cmd.getArgList().size() > 0)
			throw new ParseException("Unknown parameters: "
					+ Arrays.toString(cmd.getArgs()));

		if (cmd.hasOption("n"))
			this.numberOfPoints = Integer.parseInt(cmd.getOptionValue("n"));
		else
			this.numberOfPoints = 100;

		if (cmd.hasOption("d"))
			this.numberDimensions = Integer.parseInt(cmd.getOptionValue("d"));
		else
			this.numberDimensions = 2;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see data.dataset.generator.DataSetGenerator#generateDataSet()
	 */
	@Override
	protected DataSet generateDataSet() throws DataSetGenerationException {
		try {
			MyRengine rEngine = repository.getRengineForCurrentThread();
			rEngine.eval("library(mlbench)");
			rEngine.eval("result <- mlbench.twonorm(n=" + this.numberOfPoints
					+ ",d=" + this.numberDimensions + ");");
			double[][] coords = rEngine.eval("result$x").asDoubleMatrix();
			classes = rEngine.eval("result$classes").asIntegers();

			// create the target file
			File dataSetFile = new File(FileUtils.buildPath(
					this.repository.getBasePath(DataSet.class),
					this.getFolderName(), this.getFileName()));

			try {
				// dataset file
				BufferedWriter writer = new BufferedWriter(new FileWriter(
						dataSetFile));
				// writer header
				writer.append("// alias = " + getAlias());
				writer.newLine();
				writer.append("// dataSetFormat = MatrixDataSetFormat");
				writer.newLine();
				writer.append("// dataSetType = SyntheticDataSetType");
				writer.newLine();
				writer.append("// dataSetFormatVersion = 1");
				writer.newLine();
				for (int row = 0; row < coords.length; row++) {
					StringBuilder sb = new StringBuilder();
					sb.append((row + 1));
					sb.append("\t");
					for (int i = 0; i < coords[row].length; i++) {
						sb.append(coords[row][i] + "\t");
					}
					sb.deleteCharAt(sb.length() - 1);
					writer.append(sb.toString());
					writer.newLine();
				}
				writer.close();

				return new AbsoluteDataSet(this.repository, true,
						dataSetFile.lastModified(), dataSetFile, getAlias(),
						(AbsoluteDataSetFormat) DataSetFormat.parseFromString(
								repository, "MatrixDataSetFormat"),
						DataSetType.parseFromString(repository,
								"SyntheticDataSetType"));

			} catch (IOException e) {
				e.printStackTrace();
			} catch (UnknownDataSetFormatException e) {
				e.printStackTrace();
			} catch (RegisterException e) {
				e.printStackTrace();
			} catch (UnknownDataSetTypeException e) {
				e.printStackTrace();
			}

		} catch (RserveException e) {
			e.printStackTrace();
		} catch (REXPMismatchException e) {
			e.printStackTrace();
		}
		throw new DataSetGenerationException(
				"The dataset could not be generated!");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see data.dataset.generator.DataSetGenerator#generateGoldStandard()
	 */
	@Override
	protected GoldStandard generateGoldStandard()
			throws GoldStandardGenerationException {

		try {
			// goldstandard file
			File goldStandardFile = new File(FileUtils.buildPath(
					this.repository.getBasePath(GoldStandard.class),
					this.getFolderName(), this.getFileName()));
			BufferedWriter writer = new BufferedWriter(new FileWriter(
					goldStandardFile));
			for (int row = 0; row < classes.length; row++) {
				writer.append((row + 1) + "\t" + classes[row] + ":1.0");
				writer.newLine();
			}
			writer.close();

			return new GoldStandard(repository,
					goldStandardFile.lastModified(), goldStandardFile);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (RegisterException e) {
			e.printStackTrace();
		}
		throw new GoldStandardGenerationException(
				"The goldstandard could not be generated!");
	}
}