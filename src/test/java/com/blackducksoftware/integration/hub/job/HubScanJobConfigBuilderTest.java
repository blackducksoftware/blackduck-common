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
package com.blackducksoftware.integration.hub.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.blackducksoftware.integration.hub.builder.HubScanJobConfigBuilder;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.util.TestLogger;

public class HubScanJobConfigBuilderTest {
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

		final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder();
		assertTrue(builder.validateProjectAndVersion(logger));
		assertTrue(!builder.validateScanMemory(logger));
		assertTrue(builder.validateScanTargetPaths(logger));
		assertTrue(!builder.validateMaxWaitTimeForBomUpdate(logger));
		assertTrue(builder.validateShouldGenerateRiskReport(logger));
	}

	@Test
	public void testValidateProjectAndVersionNoVersion() throws HubIntegrationException, IOException {
		expectedMessages.add("No Version was found.");

		final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder();
		builder.setProjectName("TestProject");

		assertTrue(!builder.validateProjectAndVersion(logger));
	}

	@Test
	public void testValidateProjectAndVersionNoProject() throws HubIntegrationException, IOException {
		expectedMessages.add("No Project name was found.");

		final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder();
		builder.setVersion("Version");

		assertTrue(!builder.validateProjectAndVersion(logger));
	}

	@Test
	public void testRiskReportValidationsNoProjectNameOrVersion() throws HubIntegrationException, IOException {
		expectedMessages.add("To generate the Risk Report, you need to provide a Project name or version.");

		final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder();
		builder.setShouldGenerateRiskReport(true);

		assertTrue(!builder.validateShouldGenerateRiskReport(logger));
	}

	@Test
	public void testRiskReportValidationsNoVersion() throws HubIntegrationException, IOException {
		expectedMessages.add("To generate the Risk Report, you need to provide a Project name or version.");

		final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder();
		builder.setShouldGenerateRiskReport(true);
		builder.setProjectName("TestProject");

		assertTrue(!builder.validateShouldGenerateRiskReport(logger));
	}

	@Test
	public void testRiskReportValidationsNoProject() throws HubIntegrationException, IOException {
		expectedMessages.add("To generate the Risk Report, you need to provide a Project name or version.");

		final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder();
		builder.setShouldGenerateRiskReport(true);
		builder.setVersion("Version");

		assertTrue(!builder.validateShouldGenerateRiskReport(logger));
	}

	@Test
	public void testValidateMaxWaitTimeForRiskReport() throws HubIntegrationException, IOException {
		expectedMessages.add("The maximum wait time for the BOM Update must be greater than 0.");

		final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder();
		builder.setShouldGenerateRiskReport(true);

		assertTrue(!builder.validateMaxWaitTimeForBomUpdate(logger));
	}

	@Test
	public void testValidateMaxWaitTimeForRiskReportValid() throws HubIntegrationException, IOException {
		final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder();
		builder.setShouldGenerateRiskReport(true);
		builder.setMaxWaitTimeForBomUpdate(HubScanJobConfigBuilder.DEFAULT_BOM_UPDATE_WAIT_TIME_IN_MINUTES);

		assertTrue(builder.validateMaxWaitTimeForBomUpdate(logger));
	}

	@Test
	public void testValidateScanTargetPathsNullTarget() throws HubIntegrationException, IOException {
		final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder();

		setBuilderDefaults(builder);

		builder.addScanTargetPath(null);

		assertTrue(builder.validateScanTargetPaths(logger));
	}

	@Test
	public void testValidateScanTargetPathsOutsideWorkingDirectory() throws HubIntegrationException, IOException {
		expectedMessages.add("Can not scan targets outside the working directory.");

		final String relativeClasspathResourcePath = "com/blackducksoftware/integration/hub/existingFileForTestingScanPaths.txt";
		final URL url = HubScanJobConfigBuilder.class.getClassLoader().getResource(relativeClasspathResourcePath);
		final File existingFile = new File(URLDecoder.decode(url.getFile(), "UTF-8"));
		final String absolutePath = existingFile.getAbsolutePath();

		final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder();

		setBuilderDefaultsBasic(builder);

		builder.addScanTargetPath(absolutePath);

		assertTrue(!builder.validateScanTargetPaths(logger));
	}

	@Test
	public void testValidateScanTargetPathsWithExistingFiles() throws HubIntegrationException, IOException {
		final String relativeClasspathResourcePath = "com/blackducksoftware/integration/hub/existingFileForTestingScanPaths.txt";
		final URL url = HubScanJobConfigBuilder.class.getClassLoader().getResource(relativeClasspathResourcePath);
		final File existingFile = new File(URLDecoder.decode(url.getFile(), "UTF-8"));
		final String absolutePath = existingFile.getAbsolutePath();
		final String workingDirectoryPath = absolutePath.replace(relativeClasspathResourcePath, "");

		final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder();

		setBuilderDefaultsBasic(builder);

		builder.setWorkingDirectory(workingDirectoryPath);
		builder.addScanTargetPath(absolutePath);

		assertTrue(builder.validateScanTargetPaths(logger));
	}

	@Test
	public void testEmptyConfigIsInvalid() throws HubIntegrationException, IOException {
		expectedMessages.add("No Project name or Version were found. Any scans run will not be mapped to a Version.");

		final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder();
		builder.build(logger);
	}

	@Test
	public void testValidConfig() throws HubIntegrationException, IOException {
		final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder();

		setBuilderDefaults(builder);

		builder.build(logger);
	}

	@Test
	public void testConfigInvalidWithProjectNameNoVersion() throws HubIntegrationException, IOException {
		expectedMessages.add("No Version was found.");

		thrown.expect(HubIntegrationException.class);

		final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder();

		setBuilderDefaults(builder);

		builder.setVersion("");

		builder.build(logger);
	}

	@Test
	public void testConfigInvalidWithVersionNoProjectName() throws HubIntegrationException, IOException {
		expectedMessages.add("No Project name was found.");

		thrown.expect(HubIntegrationException.class);

		final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder();

		setBuilderDefaults(builder);

		builder.setProjectName("");

		builder.build(logger);
	}

	@Test
	public void testConfigValidGeneratingReport() throws HubIntegrationException, IOException {
		final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder();

		setBuilderDefaults(builder);

		builder.setShouldGenerateRiskReport(true);
		builder.setMaxWaitTimeForBomUpdate(5);

		builder.build(logger);
	}

	@Test
	public void testConfigInvalidGeneratingReportInvalidWaitTime() throws HubIntegrationException, IOException {
		final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder();

		setBuilderDefaults(builder);

		builder.setShouldGenerateRiskReport(true);
		builder.setMaxWaitTimeForBomUpdate(0);

		builder.build(logger);
	}

	@Test
	public void testConfigInvalidGeneratingReportNeedProjectNameOrVersion() throws HubIntegrationException, IOException {
		expectedMessages.add("No Project name or Version were found. Any scans run will not be mapped to a Version.");
		expectedMessages.add("To generate the Risk Report, you need to provide a Project name or version.");

		thrown.expect(HubIntegrationException.class);

		final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder();

		setBuilderDefaults(builder);

		builder.setShouldGenerateRiskReport(true);
		builder.setMaxWaitTimeForBomUpdate(5);
		builder.setProjectName(" ");
		builder.setVersion(null);

		builder.build(logger);
	}

	@Test
	public void testNullScanTargets() throws HubIntegrationException, IOException {
		final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder();

		setBuilderDefaults(builder);

		builder.addScanTargetPath(null);

		builder.build(logger);
	}

	@Test
	public void testInvalidWithTargetsOutsideWorkingDirectory() throws HubIntegrationException, IOException {
		expectedMessages.add("Can not scan targets outside the working directory.");

		thrown.expect(HubIntegrationException.class);

		final String relativeClasspathResourcePath = "com/blackducksoftware/integration/hub/existingFileForTestingScanPaths.txt";
		final URL url = HubScanJobConfigBuilder.class.getClassLoader().getResource(relativeClasspathResourcePath);
		final File existingFile = new File(URLDecoder.decode(url.getFile(), "UTF-8"));
		final String absolutePath = existingFile.getAbsolutePath();

		final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder();

		setBuilderDefaultsBasic(builder);

		builder.addScanTargetPath(absolutePath);

		builder.build(logger);
	}

	@Test
	public void testConfigValidWithExistingFiles() throws HubIntegrationException, IOException {
		final String relativeClasspathResourcePath = "com/blackducksoftware/integration/hub/existingFileForTestingScanPaths.txt";
		final URL url = HubScanJobConfigBuilder.class.getClassLoader().getResource(relativeClasspathResourcePath);
		final File existingFile = new File(URLDecoder.decode(url.getFile(), "UTF-8"));
		final String absolutePath = existingFile.getAbsolutePath();
		final String workingDirectoryPath = absolutePath.replace(relativeClasspathResourcePath, "");

		final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder();

		setBuilderDefaultsBasic(builder);

		builder.setWorkingDirectory(workingDirectoryPath);
		builder.addScanTargetPath(absolutePath);

		builder.build(logger);
	}

	@Test
	public void testConfigValidWithEmptyProjectNameAndVersion() throws HubIntegrationException, IOException {
		expectedMessages.add("No Project name or Version were found. Any scans run will not be mapped to a Version.");

		final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder();

		setBuilderDefaults(builder);

		builder.setProjectName(" ");
		builder.setVersion(" ");

		builder.build(logger);
	}

	@Test
	public void testConfigInvalidWithNonExistingFiles() throws HubIntegrationException, IOException {
		final String nonExistingFilePath = "giraffe";
		final File nonExistingFile = new File(nonExistingFilePath);
		expectedMessages.add("The scan target '" + nonExistingFile.getAbsolutePath() + "' does not exist.");
		expectedMessages.add("Can not scan targets outside the working directory.");

		thrown.expect(HubIntegrationException.class);

		final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder();

		setBuilderDefaultsBasic(builder);

		builder.addScanTargetPath(nonExistingFilePath);

		builder.build(logger);
	}

	private void setBuilderDefaults(final HubScanJobConfigBuilder builder) {
		setBuilderDefaultsBasic(builder);

		builder.addScanTargetPath("testPath");
		builder.disableScanTargetPathExistenceCheck();
	}

	private void setBuilderDefaultsBasic(final HubScanJobConfigBuilder builder) {
		builder.setProjectName("projectName");
		builder.setVersion("version");
		builder.setPhase("phase");
		builder.setDistribution("distribution");
		builder.setWorkingDirectory("workingDirectory");
		builder.setScanMemory(512);
	}

}
