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
import java.util.HashSet;
import java.util.Set;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RserveException;

import de.clusteval.framework.MyRengine;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;

import utils.ArraysExt;

/**
 * @author Christian Wiwie
 * 
 */
public class AbsoluteDistanceMeasure extends DistanceMeasure {

	/**
	 * @param repository
	 * @param register
	 * @param changeDate
	 * @param absPath
	 * @throws RegisterException
	 */
	public AbsoluteDistanceMeasure(Repository repository, boolean register,
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
	public AbsoluteDistanceMeasure(
			final AbsoluteDistanceMeasure other)
			throws RegisterException {
		super(other);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see data.distance.DistanceMeasure#getRequiredRlibraries()
	 */
	@Override
	public Set<String> getRequiredRlibraries() {
		return new HashSet<String>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see data.distance.DistanceMeasure#getDistance(double[], double[])
	 */
	@Override
	public double getDistance(double[] point1, double[] point2)
			throws InvalidParameterException {
		return ArraysExt.sum(ArraysExt.abs(ArraysExt.subtract(point1, point2)));
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
	 * @see data.distance.DistanceMeasure#getDistances(double[][])
	 */
	@Override
	public double[][] getDistances(double[][] matrix)
			throws InvalidParameterException {
		try {
			MyRengine rEngine = new MyRengine("");
			try {
				rEngine.assign("matrix", matrix);
				REXP result = rEngine
						.eval("as.matrix(dist(matrix, method='manhattan'))");
				return result.asDoubleMatrix();
			} catch (REngineException e) {
				e.printStackTrace();
			} catch (REXPMismatchException e) {
				e.printStackTrace();
			} finally {
				rEngine.close();
			}
		} catch (RserveException e) {
			e.printStackTrace();
		}
		return null;
	}

}
