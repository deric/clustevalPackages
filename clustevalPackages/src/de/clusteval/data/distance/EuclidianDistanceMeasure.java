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
import java.security.InvalidParameterException;

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
public class EuclidianDistanceMeasure extends DistanceMeasureR {

	/**
	 * @param repository
	 * @param register
	 * @param changeDate
	 * @param absPath
	 * @throws RegisterException
	 */
	public EuclidianDistanceMeasure(Repository repository, boolean register,
			long changeDate, File absPath) throws RegisterException {
		super(repository, register, changeDate, absPath);
	}

	/**
	 * The copy constructor for this measure.
	 * 
	 * @param other
	 *            The object to clone.
	 * @throws RegisterException
	 */
	public EuclidianDistanceMeasure(final EuclidianDistanceMeasure other)
			throws RegisterException {
		super(other);
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
	 * @see
	 * de.clusteval.data.distance.DistanceMeasureR#getDistanceHelper(double[],
	 * double[], de.clusteval.framework.repository.MyRengine)
	 */
	@Override
	protected double getDistanceHelper(double[] point1, double[] point2,
			MyRengine rEngine) throws REngineException, REXPMismatchException {
		double result = 0.0;
		if (point1.length != point2.length)
			throw new InvalidParameterException(
					"The dimensions of the points need to be the same.");
		for (int i = 0; i < point1.length; i++) {
			result += Math.pow(point1[i] - point2[i], 2.0);
		}
		result = Math.sqrt(result);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.clusteval.data.distance.DistanceMeasureR#getDistancesHelper(de.clusteval
	 * .data.dataset.format.ConversionInputToStandardConfiguration, double[][],
	 * de.clusteval.framework.repository.MyRengine, int)
	 */
	@Override
	protected double[][] getDistancesHelper(
			ConversionInputToStandardConfiguration config, double[][] matrix,
			MyRengine rEngine, int firstRow, int lastRow)
			throws REngineException, REXPMismatchException {
		return rEngine
				.eval(String
						.format("proxy::dist(rbind(matrix[%d:%d,]), rbind(matrix), method='Euclidean')",
								firstRow, lastRow)).asDoubleMatrix();
	}
}
