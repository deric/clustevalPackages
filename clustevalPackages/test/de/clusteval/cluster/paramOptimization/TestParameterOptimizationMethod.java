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
package de.clusteval.cluster.paramOptimization;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.wiwie.wiutils.utils.SimilarityMatrix.NUMBER_PRECISION;
import ch.qos.logback.classic.Level;
import de.clusteval.cluster.quality.ClusteringQualitySet;
import de.clusteval.context.Context;
import de.clusteval.context.UnknownContextException;
import de.clusteval.data.DataConfig;
import de.clusteval.data.dataset.DataSet;
import de.clusteval.data.dataset.RelativeDataSet;
import de.clusteval.data.dataset.format.ConversionInputToStandardConfiguration;
import de.clusteval.data.dataset.format.ConversionStandardToInputConfiguration;
import de.clusteval.data.dataset.format.DataSetFormat;
import de.clusteval.data.dataset.format.InvalidDataSetFormatVersionException;
import de.clusteval.data.dataset.format.UnknownDataSetFormatException;
import de.clusteval.data.distance.DistanceMeasure;
import de.clusteval.data.distance.UnknownDistanceMeasureException;
import de.clusteval.data.preprocessing.DataPreprocessor;
import de.clusteval.framework.ClustevalBackendServer;
import de.clusteval.framework.repository.InvalidRepositoryException;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.framework.repository.RepositoryAlreadyExistsException;
import de.clusteval.framework.repository.config.RepositoryConfigNotFoundException;
import de.clusteval.framework.repository.config.RepositoryConfigurationException;
import de.clusteval.program.ParameterSet;
import de.clusteval.program.ProgramConfig;
import de.clusteval.program.ProgramParameter;
import de.clusteval.run.result.RunResultParseException;
import de.clusteval.utils.FormatConversionException;
import de.clusteval.utils.InternalAttributeException;
import de.clusteval.utils.RNotAvailableException;

/**
 * @author Christian Wiwie
 * 
 */
public class TestParameterOptimizationMethod {

	Repository repo;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		ClustevalBackendServer.logLevel(Level.INFO);
		repo = new Repository(new File("testCaseRepository").getAbsolutePath(),
				null);
		repo.initialize();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		repo.terminateSupervisorThread();
	}

	/**
	 * @throws FileNotFoundException
	 * @throws RepositoryAlreadyExistsException
	 * @throws InvalidRepositoryException
	 * @throws RepositoryConfigNotFoundException
	 * @throws RepositoryConfigurationException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws RegisterException
	 */
	@Test
	public void testLayered() throws FileNotFoundException,
			RepositoryAlreadyExistsException, InvalidRepositoryException,
			RepositoryConfigNotFoundException,
			RepositoryConfigurationException, SecurityException,
			IllegalArgumentException, RegisterException, InterruptedException {

		ClustevalBackendServer.logLevel(Level.INFO);

		List<ProgramParameter<?>> params = new ArrayList<ProgramParameter<?>>();
		params.add(repo.getStaticObjectWithName(ProgramConfig.class,
				"TransClust_2").getParameterForName("T"));

		LayeredDivisiveParameterOptimizationMethod method = new LayeredDivisiveParameterOptimizationMethod(
				repo, false, System.currentTimeMillis(), new File("bla"), null,
				null, null, params, null, 100, false);
		method.remainingIterationCount = 100;

		Assert.assertEquals(100, method.remainingIterationCount);

		Assert.assertEquals(100, method.getTotalIterationCount());

		Assert.assertEquals(10, method.layerCount);

		int newIterations = method.getNextIterationsPerLayer();
		Assert.assertEquals(10, newIterations);
		Assert.assertEquals(90, method.remainingIterationCount);

		newIterations = method.getNextIterationsPerLayer();
		Assert.assertEquals(10, newIterations);
		Assert.assertEquals(80, method.remainingIterationCount);

		newIterations = method.getNextIterationsPerLayer();
		Assert.assertEquals(10, newIterations);
		Assert.assertEquals(70, method.remainingIterationCount);

		newIterations = method.getNextIterationsPerLayer();
		Assert.assertEquals(10, newIterations);
		Assert.assertEquals(60, method.remainingIterationCount);

		newIterations = method.getNextIterationsPerLayer();
		Assert.assertEquals(10, newIterations);
		Assert.assertEquals(50, method.remainingIterationCount);

		newIterations = method.getNextIterationsPerLayer();
		Assert.assertEquals(10, newIterations);
		Assert.assertEquals(40, method.remainingIterationCount);

		newIterations = method.getNextIterationsPerLayer();
		Assert.assertEquals(10, newIterations);
		Assert.assertEquals(30, method.remainingIterationCount);

		newIterations = method.getNextIterationsPerLayer();
		Assert.assertEquals(10, newIterations);
		Assert.assertEquals(20, method.remainingIterationCount);

		newIterations = method.getNextIterationsPerLayer();
		Assert.assertEquals(10, newIterations);
		Assert.assertEquals(10, method.remainingIterationCount);

		newIterations = method.getNextIterationsPerLayer();
		Assert.assertEquals(10, newIterations);
		Assert.assertEquals(0, method.remainingIterationCount);
	}

	/**
	 * @throws FileNotFoundException
	 * @throws RepositoryAlreadyExistsException
	 * @throws InvalidRepositoryException
	 * @throws RepositoryConfigNotFoundException
	 * @throws RepositoryConfigurationException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws RegisterException
	 * @throws InternalAttributeException
	 */
	@Test
	public void testLayered2() throws FileNotFoundException,
			RepositoryAlreadyExistsException, InvalidRepositoryException,
			RepositoryConfigNotFoundException,
			RepositoryConfigurationException, SecurityException,
			IllegalArgumentException, RegisterException,
			InternalAttributeException, InterruptedException {

		ClustevalBackendServer.logLevel(Level.INFO);

		List<ProgramParameter<?>> params = new ArrayList<ProgramParameter<?>>();
		params.add(repo.getStaticObjectWithName(ProgramConfig.class,
				"TransClust_2").getParameterForName("T"));
		params.add(repo.getStaticObjectWithName(ProgramConfig.class,
				"TransClust_2").getParameterForName("T"));

		LayeredDivisiveParameterOptimizationMethod method = new LayeredDivisiveParameterOptimizationMethod(
				repo, false, System.currentTimeMillis(), new File("bla"), null,
				null, null, params, null, 100, false);
		method.remainingIterationCount = 100;

		Assert.assertEquals(100, method.remainingIterationCount);

		Assert.assertEquals(100, method.getTotalIterationCount());

		Assert.assertEquals(10, method.layerCount);

		int newIterations = method.getNextIterationsPerLayer();
		Assert.assertEquals(9, newIterations);
		Assert.assertEquals(91, method.remainingIterationCount);

		method.currentLayer++;

		newIterations = method.getNextIterationsPerLayer();
		Assert.assertEquals(9, newIterations);
		Assert.assertEquals(82, method.remainingIterationCount);

		method.currentLayer++;

		newIterations = method.getNextIterationsPerLayer();
		Assert.assertEquals(9, newIterations);
		Assert.assertEquals(73, method.remainingIterationCount);

		method.currentLayer++;

		newIterations = method.getNextIterationsPerLayer();
		Assert.assertEquals(9, newIterations);
		Assert.assertEquals(64, method.remainingIterationCount);

		method.currentLayer++;

		newIterations = method.getNextIterationsPerLayer();
		Assert.assertEquals(9, newIterations);
		Assert.assertEquals(55, method.remainingIterationCount);

		method.currentLayer++;

		newIterations = method.getNextIterationsPerLayer();
		Assert.assertEquals(9, newIterations);
		Assert.assertEquals(46, method.remainingIterationCount);

		method.currentLayer++;

		newIterations = method.getNextIterationsPerLayer();
		Assert.assertEquals(9, newIterations);
		Assert.assertEquals(37, method.remainingIterationCount);

		method.currentLayer++;

		newIterations = method.getNextIterationsPerLayer();
		Assert.assertEquals(9, newIterations);
		Assert.assertEquals(28, method.remainingIterationCount);

		method.currentLayer++;

		newIterations = method.getNextIterationsPerLayer();
		Assert.assertEquals(9, newIterations);
		Assert.assertEquals(19, method.remainingIterationCount);

		method.currentLayer++;

		newIterations = method.getNextIterationsPerLayer();
		Assert.assertEquals(9, newIterations);
		Assert.assertEquals(0, method.remainingIterationCount);
	}

	/**
	 * @throws FileNotFoundException
	 * @throws RepositoryAlreadyExistsException
	 * @throws InvalidRepositoryException
	 * @throws RepositoryConfigNotFoundException
	 * @throws RepositoryConfigurationException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws RegisterException
	 * @throws InternalAttributeException
	 */
	@Test
	public void testLayered2ParamsWith1Options() throws FileNotFoundException,
			RepositoryAlreadyExistsException, InvalidRepositoryException,
			RepositoryConfigNotFoundException,
			RepositoryConfigurationException, SecurityException,
			IllegalArgumentException, RegisterException,
			InternalAttributeException, InterruptedException {

		ClustevalBackendServer.logLevel(Level.INFO);

		List<ProgramParameter<?>> params = new ArrayList<ProgramParameter<?>>();
		params.add(repo.getStaticObjectWithName(ProgramConfig.class,
				"TransClust_2").getParameterForName("T"));

		ProgramParameter<?> withOptions = repo
				.getStaticObjectWithName(ProgramConfig.class, "TransClust_2")
				.getParameterForName("T").clone();
		withOptions.setOptions(new String[]{"1", "2", "3", "4", "5", "6", "7"});
		params.add(withOptions);

		LayeredDivisiveParameterOptimizationMethod method = new LayeredDivisiveParameterOptimizationMethod(
				repo, false, System.currentTimeMillis(), new File("bla"), null,
				null, null, params, null, 1001, false);
		method.remainingIterationCount = 1001;

		Assert.assertEquals(1001, method.remainingIterationCount);

		Assert.assertEquals(1001, method.getTotalIterationCount());

		Assert.assertEquals(31, method.layerCount);

		int newIterations;
		for (int i = 0; i < 30; i++) {
			newIterations = method.getNextIterationsPerLayer();
			Assert.assertEquals(28, newIterations);
			Assert.assertEquals(1001 - (i + 1) * 28,
					method.remainingIterationCount);

			method.currentLayer++;
		}

		newIterations = method.getNextIterationsPerLayer();
		Assert.assertEquals(28, newIterations);
		Assert.assertEquals(0, method.remainingIterationCount);
	}

	/**
	 * @throws RepositoryAlreadyExistsException
	 * @throws InvalidRepositoryException
	 * @throws RepositoryConfigNotFoundException
	 * @throws RepositoryConfigurationException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws RegisterException
	 * @throws InternalAttributeException
	 * @throws ParameterSetAlreadyEvaluatedException
	 * @throws NoParameterSetFoundException
	 * @throws ParameterOptimizationException
	 * @throws RunResultParseException
	 * @throws UnknownDistanceMeasureException
	 * @throws IOException
	 * @throws FormatConversionException
	 * @throws UnknownContextException
	 * @throws RNotAvailableException
	 * @throws InvalidDataSetFormatVersionException
	 * @throws UnknownDataSetFormatException
	 */
	@Test
	public void testAPDivisiveWithNonTerminating()
			throws RepositoryAlreadyExistsException,
			InvalidRepositoryException, RepositoryConfigNotFoundException,
			RepositoryConfigurationException, SecurityException,
			IllegalArgumentException, RegisterException,
			InternalAttributeException, InterruptedException,
			NoParameterSetFoundException,
			ParameterSetAlreadyEvaluatedException, RunResultParseException,
			ParameterOptimizationException,
			InvalidDataSetFormatVersionException, RNotAvailableException,
			UnknownContextException, FormatConversionException, IOException,
			UnknownDistanceMeasureException, UnknownDataSetFormatException {

		ClustevalBackendServer.logLevel(Level.INFO);

		ProgramConfig programConfig = repo.getStaticObjectWithName(
				ProgramConfig.class, "APcluster_1");
		DataConfig dataConfig = repo.getStaticObjectWithName(DataConfig.class,
				"DS1");
		DataSet ds = dataConfig.getDatasetConfig().getDataSet();
		DataSetFormat internal = DataSetFormat.parseFromString(repo,
				"SimMatrixDataSetFormat");
		ds = ds.preprocessAndConvertTo(
				Context.parseFromString(repo, "ClusteringContext"),
				internal,
				new ConversionInputToStandardConfiguration(DistanceMeasure
						.parseFromString(repo, "EuclidianDistanceMeasure"),
						NUMBER_PRECISION.DOUBLE,
						new ArrayList<DataPreprocessor>(),
						new ArrayList<DataPreprocessor>()),
				new ConversionStandardToInputConfiguration());
		ds.loadIntoMemory();
		if (ds instanceof RelativeDataSet) {
			RelativeDataSet dataSet = (RelativeDataSet) ds;
			dataConfig
					.getRepository()
					.getInternalDoubleAttribute(
							"$("
									+ dataConfig.getDatasetConfig()
											.getDataSet().getOriginalDataSet()
											.getAbsolutePath()
									+ ":minSimilarity)")
					.setValue(dataSet.getDataSetContent().getMinValue());
			dataConfig
					.getRepository()
					.getInternalDoubleAttribute(
							"$("
									+ dataConfig.getDatasetConfig()
											.getDataSet().getOriginalDataSet()
											.getAbsolutePath()
									+ ":maxSimilarity)")
					.setValue(dataSet.getDataSetContent().getMaxValue());
			dataConfig
					.getRepository()
					.getInternalDoubleAttribute(
							"$("
									+ dataConfig.getDatasetConfig()
											.getDataSet().getOriginalDataSet()
											.getAbsolutePath()
									+ ":meanSimilarity)")
					.setValue(dataSet.getDataSetContent().getMean());
		}
		dataConfig
				.getRepository()
				.getInternalIntegerAttribute(
						"$("
								+ dataConfig.getDatasetConfig().getDataSet()
										.getOriginalDataSet().getAbsolutePath()
								+ ":numberOfElements)")
				.setValue(ds.getIds().size());
		ds.unloadFromMemory();

		List<ProgramParameter<?>> params = new ArrayList<ProgramParameter<?>>();
		params.add(programConfig.getParameterForName("preference"));

		ProgramParameter<?> withOptions = programConfig.getParameterForName(
				"preference").clone();
		params.add(withOptions);
		withOptions = programConfig.getParameterForName("convits").clone();
		params.add(withOptions);
		withOptions = programConfig.getParameterForName("maxits").clone();
		params.add(withOptions);
		withOptions = programConfig.getParameterForName("dampfact").clone();
		params.add(withOptions);

		String completeFile = "testCaseRepository/results/11_20_2012-12_45_04_all_vs_DS1/clusters/APcluster_1_DS1.results.qual.complete";

		APParameterOptimizationMethod method = new APParameterOptimizationMethod(
				repo, false, System.currentTimeMillis(),
				new File(completeFile), null, programConfig, dataConfig,
				params, null, 1001, false);
		method.reset(new File(completeFile));

		ParameterSet parameterSet;
		for (int i = 0; i < 100; i++) {

			if (!method.hasNext())
				break;
			parameterSet = method.next();

			System.out.println(parameterSet);
			method.giveFeedbackNotTerminated(parameterSet,
					new ClusteringQualitySet());
		}
	}
}
