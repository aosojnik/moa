package moa.learners.predictors.meta.classifiers;

import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.predictions.ClassificationPrediction;
import com.yahoo.labs.samoa.instances.predictions.Prediction;

import moa.core.DoubleVector;
import moa.learners.predictors.Classifier;
import moa.learners.predictors.meta.AbstractOzaBoost;

public class OzaBoost extends AbstractOzaBoost<Classifier> implements Classifier {

	private static final long serialVersionUID = 1L;

	public OzaBoost() {
		super(Classifier.class, "trees.HoeffdingTree");
	}

	@Override
	public double updateWeight(int i, Instance inst, double lambda_d) {
		if (this.ensemble.get(i).correctlyClassifies(inst)) {
			this.scms[i] += lambda_d;
			lambda_d *= this.trainingWeightSeenByModel / (2 * this.scms[i]);
		} else {
			this.swms[i] += lambda_d;
			lambda_d *= this.trainingWeightSeenByModel / (2 * this.swms[i]);
		}
		return lambda_d;
	}

	@Override
	public Prediction combinePredictions(Prediction[] predictions, double[] weights) {
		DoubleVector combinedVote = new DoubleVector();
		for (int i = 0; i < predictions.length; i++) {
			if (weights[i] > 0.0) {
				DoubleVector vote = predictions[i].asDoubleVector();
				if (vote.sumOfValues() > 0.0) {
					vote.normalize();
					vote.scaleValues(weights[i]);
					combinedVote.addValues(vote);
				}
			}
		}
		return new ClassificationPrediction(combinedVote.getArrayRef());
	}

}
