/*
 *    MultiTargetMeanRegressor.java
 *    Copyright (C) 2017 University of Porto, Portugal
 *    @author J. Duarte, J. Gama
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 *
 */
package moa.learners.predictors.core.functions;

import com.yahoo.labs.samoa.instances.Instance;

import moa.learners.predictors.MultiTargetRegressor;
import moa.learners.predictors.rules.functions.TargetMean;
import moa.options.ClassOption;

/**
 * Target mean regressor
 *
 */

public class MultiTargetMeanRegressor extends AbstractAMRulesFunctionBasicMlLearner
		implements MultiTargetRegressor, AMRulesFunction {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public MultiTargetMeanRegressor() {
		super();
		regressorOption = new ClassOption("baseLearner", 'l', "TargetMean", TargetMean.class, "TargetMean");
	}

	@Override
	public String getPurposeString() {
		return "Uses an ensemble of rules.TargetMean to perform multitarget regression.\n"
				+ "Extends BasicMultiTargetRegressor by allowing only rules.TargetMean";
	}

	@Override
	public void resetWithMemory() {
		for (int i = 0; i < this.regressors.size(); i++) {
			TargetMean tm = new TargetMean((TargetMean) this.regressors.get(i));
			double mean = tm.getPredictionForInstance((Instance) null).asDoubleArray()[0];
			tm.reset(mean, 1);
			this.regressors.set(i, tm);
		}
	}

}
