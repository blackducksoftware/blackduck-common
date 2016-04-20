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
package com.blackducksoftware.integration.hub.global;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.util.TestLogger;

public class HubServerConfigBuilderTest {
	private List<String> expectedMessages;

	private TestLogger logger;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void setUp() {
		expectedMessages = new ArrayList<String>();
		logger = new TestLogger();
	}

	@After
	public void tearDown() {
		final List<String> outputList = logger.getOutputList();
		final String outputString = logger.getOutputString();
		assertEquals("Too many/not enough messages expected: \n" + outputString, expectedMessages.size(), outputList.size());

		for (final String expectedMessage : expectedMessages) {
			assertTrue("Did not find the expected message : " + expectedMessage, outputList.contains(expectedMessage));
		}
	}

	@Test
	public void testEmptyConfigValidations() throws HubIntegrationException, IOException {
		expectedMessages.add("No Project name or Version were found. Any scans run will not be mapped to a Version.");
		expectedMessages.add("The minimum amount of memory for the scan is 256 MB.");
		expectedMessages.add("The maximum wait time for the BOM Update must be greater than 0.");

		final HubServerConfigBuilder builder = new HubServerConfigBuilder();
		assertTrue(builder.validateProjectAndVersion(logger));

		assertTrue(builder.validateProjectAndVersion(logger));
		assertTrue(!builder.validateScanMemory(logger));
		assertTrue(builder.validateScanTargetPaths(logger));
		assertTrue(!builder.validateMaxWaitTimeForBomUpdate(logger));
		assertTrue(builder.validateShouldGenerateRiskReport(logger));
	}



	@Test
	public void testEmptyConfigIsInvalid() throws HubIntegrationException, IOException {
		expectedMessages.add("No Project name or Version were found. Any scans run will not be mapped to a Version.");

		final HubServerConfigBuilder builder = new HubServerConfigBuilder();
		builder.build(logger);
	}

	@Test
	public void testValidConfig() throws HubIntegrationException, IOException {
		final HubServerConfigBuilder builder = new HubServerConfigBuilder();

		setBuilderDefaults(builder);

		builder.build(logger);
	}



	private void setBuilderDefaults(final HubServerConfigBuilder builder) {
		setBuilderDefaultsBasic(builder);

		builder.addScanTargetPath("testPath");
		builder.disableScanTargetPathExistenceCheck();
	}

	private void setBuilderDefaultsBasic(final HubServerConfigBuilder builder) {
		builder.setProjectName("projectName");
		builder.setVersion("version");
		builder.setPhase("phase");
		builder.setDistribution("distribution");
		builder.setWorkingDirectory("workingDirectory");
		builder.setScanMemory(512);
	}

}
