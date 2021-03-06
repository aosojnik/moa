/*
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
 */

/**
 * BOLETest.java
 * Copyright (C) 2015 Santos, Barros
 */
package moa.learners.predictors.meta;

import junit.framework.Test;
import junit.framework.TestSuite;
import moa.learners.predictors.AbstractClassifierTestCase;
import moa.learners.predictors.Classifier;
import moa.learners.predictors.meta.BOLE;

/**
 * Tests the BOLE classifier.
 *
 * @author Silas G. T. C. Santos (sgtcs@cin.ufpe.br)
 * @version $Revision$
 */
public class BOLETest extends AbstractClassifierTestCase {

	/**
	 * Constructs the test case. Called by subclasses.
	 *
	 * @param name the name of the test
	 */
	public BOLETest(String name) {
		super(name);
		this.setNumberTests(1);
	}

	/**
	 * Returns the classifier setups to use in the regression test.
	 *
	 * @return the setups
	 */
	@Override
	protected Classifier[] getLearnerSetups() {
		return new Classifier[] { new BOLE(), };
	}

	/**
	 * Returns a test suite.
	 *
	 * @return the test suite
	 */
	public static Test suite() {
		return new TestSuite(BOLETest.class);
	}

	/**
	 * Runs the test from commandline.
	 *
	 * @param args ignored
	 */
	public static void main(String[] args) {
		runTest(suite());
	}
}
