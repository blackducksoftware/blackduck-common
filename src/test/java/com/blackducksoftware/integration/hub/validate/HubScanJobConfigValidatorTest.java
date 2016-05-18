/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package com.blackducksoftware.integration.hub.validate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.blackducksoftware.integration.hub.util.TestJobConfigValidator;

public class HubScanJobConfigValidatorTest {

	private TestJobConfigValidator validator;

	@Before
	public void init() {
		validator = new TestJobConfigValidator();
	}

	@Test
	public void testValidScanMemory() throws Exception {
		assertTrue(validator.validateScanMemory("4096"));
		assertTrue(validator.validateScanMemory("256"));
		assertTrue(validator.validateScanMemory("8192"));
	}

	@Test
	public void testInvalidScanMemory() throws Exception {
		assertFalse(validator.validateScanMemory("0"));
		assertFalse(validator.validateScanMemory("-512"));
		assertFalse(validator.validateScanMemory("128"));
		assertFalse(validator.validateScanMemory(null));
		assertFalse(validator.validateScanMemory("abcdefg123"));

	}

	@Test
	public void testValidMaxWaitTimeforBOM() throws Exception {
		assertTrue(validator.validateMaxWaitTimeForBomUpdate("10"));
		assertTrue(validator.validateMaxWaitTimeForBomUpdate("2"));
	}

	@Test
	public void testInvalidMaxWaitTimeforBOM() throws Exception {
		assertFalse(validator.validateMaxWaitTimeForBomUpdate("0"));
		assertFalse(validator.validateMaxWaitTimeForBomUpdate("-512"));
		assertFalse(validator.validateMaxWaitTimeForBomUpdate("1"));
		assertFalse(validator.validateMaxWaitTimeForBomUpdate(null));
		assertFalse(validator.validateMaxWaitTimeForBomUpdate("abcdefg123"));
	}
}
