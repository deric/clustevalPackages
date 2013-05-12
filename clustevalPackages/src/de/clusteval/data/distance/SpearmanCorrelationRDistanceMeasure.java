/**
 * 
 */
package de.clusteval.data.distance;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;

import de.clusteval.framework.MyRengine;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;

/**
 * @author Christian Wiwie
 * 
 */
public class SpearmanCorrelationRDistanceMeasure extends DistanceMeasureR {

	/**
	 * @param repository
	 * @param register
	 * @param changeDate
	 * @param absPath
	 * @throws RegisterException
	 */
	public SpearmanCorrelationRDistanceMeasure(Repository repository,
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
	public SpearmanCorrelationRDistanceMeasure(
			final SpearmanCorrelationRDistanceMeasure other)
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
	 * @see utils.Distance#getDistance(double[], double[])
	 */
	@Override
	public double getDistanceHelper(double[] point1, double[] point2,
			final MyRengine rEngine) throws REXPMismatchException,
			REngineException {
		rEngine.assign("p1", point1);
		rEngine.assign("p2", point2);
		double result = rEngine.eval("cor(p1,p2,method='spearman')").asDouble();
		// convert to distance
		return 1.0 - Math.abs(result);
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
	public double[][] getDistancesHelper(double[][] matrix,
			final MyRengine rEngine) throws REngineException,
			REXPMismatchException {
		rEngine.assign("matrix", matrix);
		REXP result = rEngine.eval("1-abs(cor(t(matrix),method='spearman'))");
		return result.asDoubleMatrix();
	}
}
