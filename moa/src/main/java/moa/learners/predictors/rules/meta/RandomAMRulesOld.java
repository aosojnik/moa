/*
 *    RandomRules.java
 *    Copyright (C) 2014 University of Waikato, Hamilton, New Zealand
 *    @author Albert Bifet (abifet at cs dot waikato dot ac dot nz)
 *
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package moa.learners.predictors.rules.meta;

import java.util.Arrays;

import com.github.javacliparser.FlagOption;
import com.github.javacliparser.FloatOption;
import com.github.javacliparser.IntOption;
import com.github.javacliparser.MultiChoiceOption;
import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.InstancesHeader;
import com.yahoo.labs.samoa.instances.predictions.MultiTargetRegressionPrediction;
import com.yahoo.labs.samoa.instances.predictions.Prediction;

import moa.core.Measurement;
import moa.core.MiscUtils;
import moa.learners.predictors.AbstractMultiTargetRegressor;
import moa.learners.predictors.MultiTargetRegressor;
import moa.learners.predictors.Regressor;
import moa.learners.predictors.rules.AMRulesRegressorOld;
import moa.learners.predictors.rules.AbstractAMRules;
import moa.learners.predictors.rules.core.voting.ErrorWeightedVote;
import moa.learners.predictors.rules.core.voting.Vote;
import moa.options.ClassOption;

public class RandomAMRulesOld extends AbstractMultiTargetRegressor implements MultiTargetRegressor {

	public IntOption VerbosityOption = new IntOption("verbosity", 'v',
			"Output Verbosity Control Level. 1 (Less) to 2 (More)", 1, 1, 2);

	private static final long serialVersionUID = 1L;

	public ClassOption baseLearnerOption = new ClassOption("baseLearner", 'l', "Regressor to train.",
			AbstractAMRules.class, AMRulesRegressorOld.class.getName());

	public IntOption ensembleSizeOption = new IntOption("ensembleSize", 's', "The number of models in the bag.", 10, 1,
			Integer.MAX_VALUE);

	public FloatOption numAttributesPercentageOption = new FloatOption("numAttributesPercentage", 'n',
			"The number of attributes to use per model.", 63.2, 0, 100);

	public FlagOption useBaggingOption = new FlagOption("useBagging", 'p', "Use Bagging.");

	public ClassOption votingFunctionOption = new ClassOption("votingFunction", 'V', "Voting Function.",
			ErrorWeightedVote.class, "UniformWeightedVote");

	public MultiChoiceOption votingTypeOption = new MultiChoiceOption("votingTypeOption", 'C',
			"Select whether the base learner error is computed as the overall error os only the error of the rules that cover the example.",
			new String[] { "Overall", "Only rules covered" }, new String[] { "Overall", "Covered" }, 0);

	public FloatOption fadingErrorFactorOption = new FloatOption("fadingErrorFactor", 'e',
			"Fading error factor for the accumulated error", 0.99, 0, 1);

	protected AbstractAMRules[] ensemble;
	protected double[] sumError;
	protected double[] nError;

	protected boolean isRegression;

	@Override
	public void resetLearningImpl() {
		this.classifierRandom.setSeed(this.randomSeedOption.getValue());
		int n = this.ensembleSizeOption.getValue();
		this.ensemble = new AbstractAMRules[n];
		sumError = new double[n];
		nError = new double[n];

		// Classifier baseLearner = (Classifier)
		// getPreparedClassOption(this.baseLearnerOption);
		AbstractAMRules baseLearner = (AbstractAMRules) getPreparedClassOption(this.baseLearnerOption);
		baseLearner.setAttributesPercentage(numAttributesPercentageOption.getValue());
		baseLearner.resetLearning();
		for (int i = 0; i < this.ensemble.length; i++) {
			this.ensemble[i] = (AbstractAMRules) baseLearner.copy();
			this.ensemble[i].setRandomSeed(this.classifierRandom.nextInt());
		}
		this.isRegression = (baseLearner instanceof Regressor);
	}

	@Override
	public void trainOnInstanceImpl(Instance instance) {
		double factor = this.fadingErrorFactorOption.getValue();
		for (int i = 0; i < this.ensemble.length; i++) {
			Instance inst = instance.copy();
			int k = 1;
			if (this.useBaggingOption.isSet()) {
				k = MiscUtils.poisson(1.0, this.classifierRandom);
			}
			if (k > 0) {
				// Instance weightedInst = transformInstance(inst,i);
				inst.setWeight(inst.weight() * k);

				// estimate error
				double error = Math
						.abs(inst.classValue() - ensemble[i].getPredictionForInstance(inst).asDoubleArray()[0]);
				sumError[i] = error * inst.weight() + sumError[i] * factor;
				nError[i] = inst.weight() + nError[i] * factor;
				// train learner
				this.ensemble[i].trainOnInstance(inst);
			}
		}
	}

	@Override
	public Prediction getPredictionForInstance(Instance inst) {
		double[] votes = null;
		// ErrorWeightedVote combinedVote = (ErrorWeightedVote)((ErrorWeightedVote)
		// votingTypeOption.getPreMaterializedObject()).copy();
		ErrorWeightedVote combinedVote = (ErrorWeightedVote) ((ErrorWeightedVote) getPreparedClassOption(
				this.votingFunctionOption)).copy();
		StringBuilder sb = null;
		if (VerbosityOption.getValue() > 1)
			sb = new StringBuilder();

		for (int i = 0; i < this.ensemble.length; i++) {
			// transformInstance method visibility changed from private to protected in
			// RandomRules
			Vote v = this.ensemble[i].getVotes(inst);
			if (VerbosityOption.getValue() > 1)
				sb.append(Arrays.toString(v.getVote()) + ", " + " E: " + v.getError() + " ");
			if (this.isRegression == false && v.sumVoteDistrib() != 0.0) {
				v.normalize();
			}
			if (this.votingTypeOption.getChosenIndex() == 0) {// Overall error estimation
				combinedVote.addVote(v.getVote(), this.sumError[i] / this.nError[i]);
			} else // Error estimation over the rules that cover the example
				combinedVote.addVote(v.getVote(), v.getError());
		}
		votes = combinedVote.computeWeightedVote();
		if (VerbosityOption.getValue() > 1) {
			sb.append(Arrays.toString(votes) + ", ").append(inst.classValue());
			System.out.println(sb.toString());
		}
		return new MultiTargetRegressionPrediction(votes);
	}

	@Override
	public boolean isRandomizable() {
		return true;
	}

	@Override
	public void getModelDescription(StringBuilder out, int indent) {
	}

	@Override
	protected Measurement[] getModelMeasurementsImpl() {
		Measurement[] baseLearnerMeasurements = ((AbstractAMRules) getPreparedClassOption(this.baseLearnerOption))
				.getModelMeasurements();
		int nMeasurements = baseLearnerMeasurements.length;
		Measurement[] m = new Measurement[nMeasurements + 1];

		for (int i = 0; i < baseLearnerMeasurements.length; i++)
			m[i + 1] = baseLearnerMeasurements[i];

		int ensembleSize = 0;
		if (this.ensemble != null) {
			ensembleSize = this.ensemble.length;
			for (int i = 0; i < nMeasurements; i++) {
				double value = 0;
				for (int j = 0; j < ensembleSize; ++j) {
					value += ensemble[j].getModelMeasurements()[i].getValue();
				}
				m[i + 1] = new Measurement("Avg " + baseLearnerMeasurements[i].getName(), value / ensembleSize);
			}
		}

		m[0] = new Measurement("ensemble size", ensembleSize);

		return m;
	}

//	public Classifier[] getSubClassifiers() {
//		return this.ensemble; //.clone();
//	}

	protected int[][] listAttributes;
	protected int numAttributes;
	protected InstancesHeader[] dataset;

	@Override
	public String getPurposeString() {
		return "WeightedRandomRules";
	}

}
