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
package de.clusteval.data.distance;

import java.io.File;

import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;

import de.clusteval.data.dataset.format.ConversionInputToStandardConfiguration;
import de.clusteval.framework.repository.MyRengine;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;

/**
 * @author Christian Wiwie
 * 
 */
public class PearsonCorrelationRDistanceMeasure extends DistanceMeasureR {

	/**
	 * @param repository
	 * @param register
	 * @param changeDate
	 * @param absPath
	 * @throws RegisterException
	 */
	public PearsonCorrelationRDistanceMeasure(Repository repository,
			boolean register, long changeDate, File absPath)
			throws RegisterException {
		super(repository, register, changeDate, absPath);
	}

	/**
	 * The copy constructor for this measure.
	 * 
	 * @param other
	 *            The object to clone.
	 * @throws RegisterException
	 */
	public PearsonCorrelationRDistanceMeasure(
			final PearsonCorrelationRDistanceMeasure other)
			throws RegisterException {
		super(other);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.Distance#getDistance(double[], double[])
	 */
	@Override
	public double getDistanceHelper(double[] point1, double[] point2,
			final MyRengine rEngine) throws REXPMismatchException,
			REngineException, InterruptedException {
		rEngine.assign("p1", point1);
		rEngine.assign("p2", point2);
		double result = rEngine.eval("cor(p1,p2,method='pearson')").asDouble();
		// convert to distance
		return 1.0 - (result + 1) / 2;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see data.distance.DistanceMeasure#supportsMatrix()
	 */
	@Override
	public boolean supportsMatrix() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.clusteval.data.distance.DistanceMeasure#isSymmetric()
	 */
	@Override
	public boolean isSymmetric() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see data.distance.DistanceMeasure#getDistances(double[][])
	 */
	@Override
	public double[][] getDistancesHelper(
			ConversionInputToStandardConfiguration config, double[][] matrix,
			final MyRengine rEngine, int firstRow, int lastRow)
			throws REngineException, REXPMismatchException,
			InterruptedException {
		return rEngine
				.eval(String
						.format("1-(cor(cbind(matrix.t[,%d:%d]), cbind(matrix.t), method='pearson')+1)/2",
								firstRow, lastRow)).asDoubleMatrix();
	}
}
