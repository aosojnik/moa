package moa.learners.predictors.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.InstanceInformation;
import com.yahoo.labs.samoa.instances.InstancesHeader;
import com.yahoo.labs.samoa.instances.predictions.Prediction;

import moa.core.AutoExpandVector;
import moa.core.DoubleVector;
import moa.learners.predictors.InstanceLearner;
import moa.learners.predictors.core.attributeclassobservers.AttributeStatisticsObserver;
import moa.learners.predictors.core.attributeclassobservers.NominalStatisticsObserver;
import moa.learners.predictors.core.attributeclassobservers.NumericStatisticsObserver;
import moa.learners.predictors.core.driftdetection.ChangeDetector;
import moa.learners.predictors.core.errormeasurers.MultiLabelErrorMeasurer;
import moa.learners.predictors.core.inputselectors.InputAttributesSelector;
import moa.learners.predictors.core.instancetransformers.InstanceTransformer;
import moa.learners.predictors.core.outputselectors.OutputAttributesSelector;
import moa.learners.predictors.core.splitcriteria.MultiLabelSplitCriterion;
import moa.learners.predictors.rules.core.AttributeExpansionSuggestion;
import moa.learners.predictors.rules.core.anomalydetection.AnomalyDetector;
import moa.options.AbstractOptionHandler;

public abstract class LearningLiteral extends AbstractOptionHandler {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	protected AutoExpandVector<AttributeStatisticsObserver> attributeObservers;

	protected DoubleVector[] literalStatistics;

	protected int[] outputsToLearn;

	protected int[] inputsToLearn;

	protected InstanceLearner learner;

	protected MultiLabelErrorMeasurer errorMeasurer;

	// change detector for each outputsToLearn
	protected ChangeDetector[] changeDetectors;

	protected ChangeDetector changeDetector;

	protected AnomalyDetector anomalyDetector;

	protected MultiLabelSplitCriterion splitCriterion;

	protected double weightSeen;

	protected boolean hasStarted;

	protected LearningLiteral expandedLearningLiteral;

	protected LearningLiteral otherBranchLearningLiteral;

	protected LearningLiteral otherOutputsLearningLiteral;

	protected AttributeExpansionSuggestion bestSuggestion;

	protected NumericStatisticsObserver numericStatisticsObserver;

	protected NominalStatisticsObserver nominalStatisticsObserver;

	protected OutputAttributesSelector outputSelector;

	protected InputAttributesSelector inputSelector;

	protected InstanceInformation instanceInformation;

	protected Random randomGenerator;

	protected boolean[] attributesMask; // TODO: JD Use sparse representation?
	protected double attributesPercentage;

	protected InstanceTransformer instanceTransformer;

	protected InstancesHeader instanceHeader;

	double[] meritPerInput; // for feature

	// Maintain statistics for input and output attributes for standard deviation
	// computation?

	public LearningLiteral() {
	}

	public LearningLiteral(int[] outputsToLearn) {
		this();
		this.outputsToLearn = outputsToLearn.clone();
	}

	abstract public void trainOnInstance(Instance instance);

	public Prediction getPredictionForInstance(Instance instance) {
		Prediction prediction = null;
		if (learner != null) {
			Instance transfInstance = this.instanceTransformer.sourceInstanceToTarget(instance);
			Prediction targetPrediction = learner.getPredictionForInstance(transfInstance);
			prediction = this.instanceTransformer.targetPredictionToSource(targetPrediction);
		}
		return prediction;
	}

	public abstract boolean tryToExpand(double splitConfidence, double tieThresholdOption);

	public boolean updateAndCheckChange(Instance instance) {
		boolean hasChanged = false;
		if (hasStarted) {
			if (changeDetectors == null) {
				changeDetectors = new ChangeDetector[outputsToLearn.length];
				for (int i = 0; i < outputsToLearn.length; i++) {
					changeDetectors[i] = changeDetector.copy();
				}
			}
			Prediction prediction = getPredictionForInstance(instance);
			double[] normalizedErrors = getNormalizedErrors(prediction, instance);
			for (int i = 0; i < outputsToLearn.length; i++) {
				changeDetectors[i].input(normalizedErrors[i]);
				if (changeDetectors[i].getChange()) {
					hasChanged = true;
					break;
				}
			}
		}
		return hasChanged;
	}

	protected abstract double[] getNormalizedErrors(Prediction prediction, Instance inst);

	public boolean updateAndCheckAnomalyDetection(Instance instance) {
		if (hasStarted)
			return anomalyDetector.updateAndCheckAnomalyDetection(instance);
		else
			return false;
	}

	public double getWeightSeenSinceExpansion() {
		return weightSeen;
	}

	public int[] getOutputsToLearn() {
		return outputsToLearn;
	}

	public void setOutputsToLearn(int[] outputsToLearn) {
		this.outputsToLearn = outputsToLearn;
	}

	@Override
	public void getDescription(StringBuilder sb, int indent) {
	}

	public LearningLiteral getExpandedLearningLiteral() {
		return expandedLearningLiteral;
	}

	public LearningLiteral getOtherBranchLearningLiteral() {
		return otherBranchLearningLiteral;
	}

	public double[] getErrors() {
		double[] errors = null;
		if (errorMeasurer != null)
			errors = errorMeasurer.getCurrentErrors();
		return errors;
	}

	public void setSplitCriterion(MultiLabelSplitCriterion splitCriterion) {
		this.splitCriterion = splitCriterion;

	}

	public void setChangeDetector(ChangeDetector changeDetector) {
		this.changeDetectors = null;
		this.changeDetector = changeDetector; // copy()?

	}

	public void setAnomalyDetector(AnomalyDetector anomalyDetector) {
		this.anomalyDetector = anomalyDetector; // copy()?

	}

	public void setNumericObserverOption(NumericStatisticsObserver numericStatisticsObserver) {
		if (this.attributeObservers != null) {
			for (AttributeStatisticsObserver obs : this.attributeObservers)
				if (obs instanceof NumericStatisticsObserver)
					obs = null;
		}
		this.numericStatisticsObserver = numericStatisticsObserver;
	}

	public void setLearner(InstanceLearner learner) {
		this.learner = learner;
	}

	public void setErrorMeasurer(MultiLabelErrorMeasurer errorMeasurer) {
		this.errorMeasurer = errorMeasurer;

	}

	public AttributeExpansionSuggestion getBestSuggestion() {
		return bestSuggestion;
	}

	public static double computeHoeffdingBound(double range, double confidence, double n) {
		return Math.sqrt(((range * range) * Math.log(1.0 / confidence)) / (2.0 * n));
	}

	public void setOutputAttributesSelector(OutputAttributesSelector outputSelector) {
		this.outputSelector = outputSelector;

	}

	public void setNominalObserverOption(NominalStatisticsObserver nominalStatisticsObserver) {
		this.nominalStatisticsObserver = nominalStatisticsObserver;
	}

	public void setRandomGenerator(Random random) {
		this.randomGenerator = random;

	}

	public void setAttributesPercentage(double attributesPercentage) {
		this.attributesPercentage = attributesPercentage;
	}

	protected int initializeAttibutesMask(Instance inst) {
		int numInputAttributes = inst.numInputAttributes();
		int numAttributesSelected = (int) Math.round(numInputAttributes * attributesPercentage / 100);

		attributesMask = new boolean[numInputAttributes];
		ArrayList<Integer> indices = new ArrayList<>(numInputAttributes);
		for (int i = 0; i < numInputAttributes; i++)
			indices.add(i);
		if (numInputAttributes != numAttributesSelected)
			Collections.shuffle(indices, this.randomGenerator);

		for (int i = 0; i < numAttributesSelected; ++i)
			attributesMask[indices.get(i)] = true;

		return numAttributesSelected;
	}

	public void setInputAttributesSelector(InputAttributesSelector inputSelector) {
		this.inputSelector = inputSelector;
	}

	abstract public String getStaticOutput(InstancesHeader instanceInformation);

	public int[] getInputsToLearn() {
		return this.inputsToLearn;
	}

	public void setInstanceTransformer(InstanceTransformer instanceTransformer) {
		this.instanceTransformer = instanceTransformer;

	}

	public LearningLiteral getOtherOutputsLearningLiteral() {
		return otherOutputsLearningLiteral;
	}

	public void setInstanceInformation(InstanceInformation instanceInformation) {
		this.instanceInformation = instanceInformation;
	}

	public double[] getMeritInputAttributes() {
		return meritPerInput;
	}

	public boolean[] getAttributeMask() {
		return attributesMask;
	}
	// abstract public void resetLearning();
}
