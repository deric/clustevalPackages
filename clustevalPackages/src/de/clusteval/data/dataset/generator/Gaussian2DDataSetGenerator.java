/**
 * 
 */
package de.clusteval.data.dataset.generator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
import de.clusteval.framework.MyRengine;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import file.FileUtils;

/**
 * @author Christian Wiwie
 * 
 */
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
	public Gaussian2DDataSetGenerator(Repository repository, boolean register,
			long changeDate, File absPath) throws RegisterException {
		super(repository, register, changeDate, absPath);
	}

	/**
	 * The copy constructor of this class.
	 * 
	 * @param other
	 *            The object to clone.
	 * @throws RegisterException
	 */
	public Gaussian2DDataSetGenerator(Gaussian2DDataSetGenerator other)
			throws RegisterException {
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
		OptionBuilder
				.withDescription("The radius of the circle on which the gaussians are located.");
		Option option = OptionBuilder.create("r");
		options.addOption(option);

		OptionBuilder.withArgName("sd");
		OptionBuilder.hasArg();
		OptionBuilder
				.withDescription("The standard deviation of the gaussians.");
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
			throw new ParseException("Unknown parameters: "
					+ Arrays.toString(cmd.getArgs()));

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
			this.standardDeviations = Double.parseDouble(cmd
					.getOptionValue("sd"));
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
	protected DataSet generateDataSet() throws DataSetGenerationException {
		try {
			MyRengine rEngine = new MyRengine("");
			rEngine.eval("library(mlbench)");
			rEngine.eval("result <- mlbench.2dnormals(n=" + this.numberOfPoints
					+ ",cl=" + this.numberOfGaussians + ",r=" + this.radius
					+ ",sd=" + this.standardDeviations + ");");
			double[][] coords = rEngine.eval("result$x").asDoubleMatrix();
			classes = rEngine.eval("result$classes").asIntegers();

			// create the target file
			File dataSetFile = new File(FileUtils.buildPath(
					this.repository.getDataSetBasePath(), this.getFolderName(),
					this.getFileName()));

			try {
				// dataset file
				BufferedWriter writer = new BufferedWriter(new FileWriter(
						dataSetFile));
				// writer header
				writer.append("// dataSetFormat = MatrixDataSetFormat");
				writer.newLine();
				writer.append("// dataSetType = SyntheticDataSetType");
				writer.newLine();
				writer.append("// dataSetFormatVersion = 1");
				writer.newLine();
				for (int row = 0; row < coords.length; row++) {
					writer.append((row + 1) + "\t" + coords[row][0] + "\t"
							+ coords[row][1]);
					writer.newLine();
				}
				writer.close();

				return new AbsoluteDataSet(this.repository, true,
						dataSetFile.lastModified(), dataSetFile,
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
				"The dataset (or goldstandard) could not be generated!");
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
					this.repository.getGoldStandardBasePath(),
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see data.dataset.generator.DataSetGenerator#getRequiredRlibraries()
	 */
	@Override
	public Set<String> getRequiredRlibraries() {
		return new HashSet<String>(Arrays.asList(new String[]{"mlbench"}));
	}

}
