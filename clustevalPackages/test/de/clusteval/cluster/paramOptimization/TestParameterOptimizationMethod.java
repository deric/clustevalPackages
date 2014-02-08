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
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.qos.logback.classic.Level;
import de.clusteval.framework.ClustevalBackendServer;
import de.clusteval.framework.repository.InvalidRepositoryException;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.framework.repository.RepositoryAlreadyExistsException;
import de.clusteval.framework.repository.config.RepositoryConfigNotFoundException;
import de.clusteval.framework.repository.config.RepositoryConfigurationException;
import de.clusteval.program.ProgramConfig;
import de.clusteval.program.ProgramParameter;
import de.clusteval.utils.InternalAttributeException;

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
		params.add(repo.getStaticObjectWithName(ProgramConfig.class, "TransClust_2")
				.getParameterForName("T"));

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
		params.add(repo.getStaticObjectWithName(ProgramConfig.class, "TransClust_2")
				.getParameterForName("T"));
		params.add(repo.getStaticObjectWithName(ProgramConfig.class, "TransClust_2")
				.getParameterForName("T"));

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
}
