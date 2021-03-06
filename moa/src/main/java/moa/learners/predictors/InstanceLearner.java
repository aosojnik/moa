/*
 *    Learner.java
 *    Copyright (C) 2011 University of Waikato, Hamilton, New Zealand
 *    @author Richard Kirkby (rkirkby@cs.waikato.ac.nz)
 *
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package moa.learners.predictors;

import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.predictions.Prediction;

import moa.core.Example;
import moa.learners.MLTask;

/**
 * Learner interface for incremental learning models.
 *
 * @author Richard Kirkby (rkirkby@cs.waikato.ac.nz)
 * @version $Revision: 7 $
 */
public interface InstanceLearner extends MLTask<Example<Instance>> {

	/**
	 * Trains this learner incrementally using the given example.
	 *
	 * @param inst the instance to be used for training
	 */
	void trainOnInstance(Instance example);

	/**
	 * Predicts the class memberships for a given instance. If an instance is
	 * unclassified, the returned array elements must be all zero.
	 *
	 * @param inst the instance to be classified
	 * @return an array containing the estimated membership probabilities of the
	 *         test instance in each class
	 */
	Prediction getPredictionForInstance(Instance testInst);

	InstanceLearner[] getSublearners();
}
