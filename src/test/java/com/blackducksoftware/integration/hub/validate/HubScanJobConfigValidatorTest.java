/*******************************************************************************
 * Black Duck Software Suite SDK
 * Copyright (C) 2016 Black Duck Software, Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
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
