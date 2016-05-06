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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.blackducksoftware.integration.hub.builder.HubScanJobConfigBuilder;
import com.blackducksoftware.integration.hub.builder.ValidationResult;
import com.blackducksoftware.integration.hub.builder.ValidationResults;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;

public class HubScanJobConfigBuilderTest {
	private List<String> expectedMessages;
	private List<String> actualMessages;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void setUp() {
		expectedMessages = new ArrayList<String>();
		actualMessages = new ArrayList<String>();
	}

	@After
	public void tearDown() {
		assertEquals("Too many/not enough messages expected: \n" + actualMessages.size(), expectedMessages.size(),
				actualMessages.size());

		for (final String expectedMessage : expectedMessages) {
			assertTrue("Did not find the expected message : " + expectedMessage,
					actualMessages.contains(expectedMessage));
		}
	}

	private List<String> getMessages(final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result) {

		final List<String> messageList = new ArrayList<String>();
		final Map<HubScanJobFieldEnum, List<ValidationResult>> resultMap = result.getResultMap();
		for (final HubScanJobFieldEnum key : resultMap.keySet()) {
			final List<ValidationResult> resultList = resultMap.get(key);

			for (final ValidationResult item : resultList) {
				final String message = item.getMessage();

				if (StringUtils.isNotBlank(message)) {
					messageList.add(item.getMessage());
				}
			}
		}
		return messageList;
	}

	@Test
	public void testEmptyConfigValidations() throws HubIntegrationException, IOException {
		expectedMessages.add("No Project name or Version were found. Any scans run will not be mapped to a Version.");
		expectedMessages.add("No Project name or Version were found. Any scans run will not be mapped to a Version.");
		expectedMessages.add("The minimum amount of memory for the scan is 256 MB.");
		expectedMessages.add("The maximum wait time for the BOM Update must be greater than 0.");

		final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder(true);
		final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result = new ValidationResults<HubScanJobFieldEnum, HubScanJobConfig>();

		builder.validateProjectAndVersion(result);
		builder.validateScanMemory(result);
		builder.validateScanTargetPaths(result);
		builder.validateMaxWaitTimeForBomUpdate(result);
		builder.validateShouldGenerateRiskReport(result);

		assertFalse(result.isSuccess());
		actualMessages = getMessages(result);
	}

	@Test
	public void testValidateProjectAndVersionNoVersion() throws HubIntegrationException, IOException {
		expectedMessages.add("No Version was found.");

		final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder(true);
		builder.setProjectName("TestProject");
		final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result = new ValidationResults<HubScanJobFieldEnum, HubScanJobConfig>();

		builder.validateProjectAndVersion(result);
		assertFalse(result.isSuccess());
		actualMessages = getMessages(result);
	}

	@Test
	public void testValidateProjectAndVersionNoProject() throws HubIntegrationException, IOException {
		expectedMessages.add("No Project name was found.");

		final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder(true);
		builder.setVersion("Version");
		final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result = new ValidationResults<HubScanJobFieldEnum, HubScanJobConfig>();

		builder.validateProjectAndVersion(result);
		assertFalse(result.isSuccess());
		actualMessages = getMessages(result);
	}

	@Test
	public void testRiskReportValidationsNoProjectNameOrVersion() throws HubIntegrationException, IOException {
		expectedMessages.add("To generate the Risk Report, you need to provide a Project name or version.");

		final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder(true);
		builder.setShouldGenerateRiskReport(true);
		final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result = new ValidationResults<HubScanJobFieldEnum, HubScanJobConfig>();

		builder.validateShouldGenerateRiskReport(result);
		assertFalse(result.isSuccess());
		actualMessages = getMessages(result);
	}

	@Test
	public void testRiskReportValidationsNoVersion() throws HubIntegrationException, IOException {
		expectedMessages.add("To generate the Risk Report, you need to provide a Project name or version.");

		final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder(true);
		builder.setShouldGenerateRiskReport(true);
		builder.setProjectName("TestProject");
		final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result = new ValidationResults<HubScanJobFieldEnum, HubScanJobConfig>();

		builder.validateShouldGenerateRiskReport(result);
		assertFalse(result.isSuccess());
		actualMessages = getMessages(result);
	}

	@Test
	public void testRiskReportValidationsNoProject() throws HubIntegrationException, IOException {
		expectedMessages.add("To generate the Risk Report, you need to provide a Project name or version.");

		final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder(true);
		builder.setShouldGenerateRiskReport(true);
		builder.setVersion("Version");
		final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result = new ValidationResults<HubScanJobFieldEnum, HubScanJobConfig>();

		builder.validateShouldGenerateRiskReport(result);
		assertFalse(result.isSuccess());
		actualMessages = getMessages(result);
	}

	@Test
	public void testValidateMaxWaitTimeForRiskReport() throws HubIntegrationException, IOException {
		expectedMessages.add("The maximum wait time for the BOM Update must be greater than 0.");

		final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder(true);
		builder.setShouldGenerateRiskReport(true);
		final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result = new ValidationResults<HubScanJobFieldEnum, HubScanJobConfig>();

		builder.validateMaxWaitTimeForBomUpdate(result);
		assertFalse(result.isSuccess());
		actualMessages = getMessages(result);
	}

	@Test
	public void testValidateMaxWaitTimeForRiskReportValid() throws HubIntegrationException, IOException {
		final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder(true);
		builder.setShouldGenerateRiskReport(true);
		builder.setMaxWaitTimeForBomUpdate(HubScanJobConfigBuilder.DEFAULT_BOM_UPDATE_WAIT_TIME_IN_MINUTES);
		final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result = new ValidationResults<HubScanJobFieldEnum, HubScanJobConfig>();

		builder.validateMaxWaitTimeForBomUpdate(result);
		assertTrue(result.isSuccess());
	}

	@Test
	public void testValidateScanTargetPathsNullTarget() throws HubIntegrationException, IOException {
		final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder(true);

		setBuilderDefaults(builder);

		builder.addScanTargetPath(null);
		final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result = new ValidationResults<HubScanJobFieldEnum, HubScanJobConfig>();

		builder.validateScanTargetPaths(result);
		assertTrue(result.isSuccess());
	}

	@Test
	public void testValidateScanTargetPathsOutsideWorkingDirectory() throws HubIntegrationException, IOException {
		expectedMessages.add("Can not scan targets outside the working directory.");

		final String relativeClasspathResourcePath = "com/blackducksoftware/integration/hub/existingFileForTestingScanPaths.txt";
		final URL url = HubScanJobConfigBuilder.class.getClassLoader().getResource(relativeClasspathResourcePath);
		final File existingFile = new File(URLDecoder.decode(url.getFile(), "UTF-8"));
		final String absolutePath = existingFile.getAbsolutePath();

		final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder(true);

		setBuilderDefaultsBasic(builder);

		builder.addScanTargetPath(absolutePath);
		final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result = new ValidationResults<HubScanJobFieldEnum, HubScanJobConfig>();

		builder.validateScanTargetPaths(result);
		assertFalse(result.isSuccess());
		actualMessages = getMessages(result);
	}

	@Test
	public void testValidateScanTargetPathsWithExistingFiles() throws HubIntegrationException, IOException {
		final String relativeClasspathResourcePath = "com/blackducksoftware/integration/hub/existingFileForTestingScanPaths.txt";
		final URL url = HubScanJobConfigBuilder.class.getClassLoader().getResource(relativeClasspathResourcePath);
		final File existingFile = new File(URLDecoder.decode(url.getFile(), "UTF-8"));
		final String absolutePath = existingFile.getAbsolutePath();
		final String workingDirectoryPath = absolutePath.replace(relativeClasspathResourcePath, "");

		final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder(true);

		setBuilderDefaultsBasic(builder);

		builder.setWorkingDirectory(workingDirectoryPath);
		builder.addScanTargetPath(absolutePath);
		final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result = new ValidationResults<HubScanJobFieldEnum, HubScanJobConfig>();

		builder.validateScanTargetPaths(result);
		assertTrue(result.isSuccess());
		actualMessages = getMessages(result);
	}

	@Test
	public void testEmptyConfigIsInvalid() throws HubIntegrationException, IOException {
		expectedMessages.add("No Project name or Version were found. Any scans run will not be mapped to a Version.");

		final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder(true);
		final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result = builder.build();
		actualMessages = getMessages(result);
	}

	@Test
	public void testValidConfig() throws HubIntegrationException, IOException {
		final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder(true);

		setBuilderDefaults(builder);

		final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result = builder.build();
		assertTrue(result.isSuccess());
	}

	@Test
	public void testConfigInvalidWithProjectNameNoVersion() throws HubIntegrationException, IOException {
		expectedMessages.add("No Version was found.");

		thrown.expect(HubIntegrationException.class);

		final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder(true);

		setBuilderDefaults(builder);

		builder.setVersion("");

		final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result = builder.build();
		assertFalse(result.isSuccess());
		actualMessages = getMessages(result);
	}

	@Test
	public void testConfigInvalidWithVersionNoProjectName() throws HubIntegrationException, IOException {
		expectedMessages.add("No Project name was found.");

		thrown.expect(HubIntegrationException.class);

		final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder(true);

		setBuilderDefaults(builder);

		builder.setProjectName("");

		final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result = builder.build();
		assertFalse(result.isSuccess());
		actualMessages = getMessages(result);
	}

	@Test
	public void testConfigValidGeneratingReport() throws HubIntegrationException, IOException {
		final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder(true);

		setBuilderDefaults(builder);

		builder.setShouldGenerateRiskReport(true);
		builder.setMaxWaitTimeForBomUpdate(5);

		final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result = builder.build();
		assertTrue(result.isSuccess());
	}

	@Test
	public void testConfigInvalidGeneratingReportInvalidWaitTime() throws HubIntegrationException, IOException {
		final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder(true);

		setBuilderDefaults(builder);

		builder.setShouldGenerateRiskReport(true);
		builder.setMaxWaitTimeForBomUpdate(0);

		final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result = builder.build();
		assertTrue(result.isSuccess());
	}

	@Test
	public void testConfigInvalidGeneratingReportNeedProjectNameOrVersion()
			throws HubIntegrationException, IOException {
		expectedMessages.add("No Project name or Version were found. Any scans run will not be mapped to a Version.");
		expectedMessages.add("To generate the Risk Report, you need to provide a Project name or version.");

		final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder(true);

		setBuilderDefaults(builder);

		builder.setShouldGenerateRiskReport(true);
		builder.setMaxWaitTimeForBomUpdate(5);
		builder.setProjectName(" ");
		builder.setVersion(null);

		final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result = builder.build();
		assertFalse(result.isSuccess());
		actualMessages = getMessages(result);
	}

	@Test
	public void testNullScanTargets() throws HubIntegrationException, IOException {
		final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder(true);

		setBuilderDefaults(builder);

		builder.addScanTargetPath(null);

		final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result = builder.build();
		assertTrue(result.isSuccess());
	}

	@Test
	public void testInvalidWithTargetsOutsideWorkingDirectory() throws HubIntegrationException, IOException {
		expectedMessages.add("Can not scan targets outside the working directory.");

		thrown.expect(HubIntegrationException.class);

		final String relativeClasspathResourcePath = "com/blackducksoftware/integration/hub/existingFileForTestingScanPaths.txt";
		final URL url = HubScanJobConfigBuilder.class.getClassLoader().getResource(relativeClasspathResourcePath);
		final File existingFile = new File(URLDecoder.decode(url.getFile(), "UTF-8"));
		final String absolutePath = existingFile.getAbsolutePath();

		final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder(true);

		setBuilderDefaultsBasic(builder);

		builder.addScanTargetPath(absolutePath);

		final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result = builder.build();
		assertFalse(result.isSuccess());
		actualMessages = getMessages(result);
	}

	@Test
	public void testConfigValidWithExistingFiles() throws HubIntegrationException, IOException {
		final String relativeClasspathResourcePath = "com/blackducksoftware/integration/hub/existingFileForTestingScanPaths.txt";
		final URL url = HubScanJobConfigBuilder.class.getClassLoader().getResource(relativeClasspathResourcePath);
		final File existingFile = new File(URLDecoder.decode(url.getFile(), "UTF-8"));
		final String absolutePath = existingFile.getAbsolutePath();
		final String workingDirectoryPath = absolutePath.replace(relativeClasspathResourcePath, "");

		final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder(true);

		setBuilderDefaultsBasic(builder);

		builder.setWorkingDirectory(workingDirectoryPath);
		builder.addScanTargetPath(absolutePath);

		final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result = builder.build();
		assertTrue(result.isSuccess());
	}

	@Test
	public void testConfigValidWithEmptyProjectNameAndVersion() throws HubIntegrationException, IOException {
		expectedMessages.add("No Project name or Version were found. Any scans run will not be mapped to a Version.");

		final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder(true);

		setBuilderDefaults(builder);

		builder.setProjectName(" ");
		builder.setVersion(" ");

		final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result = builder.build();
		assertFalse(result.isSuccess());
		actualMessages = getMessages(result);
	}

	@Test
	public void testConfigInvalidWithNonExistingFiles() throws HubIntegrationException, IOException {
		final String nonExistingFilePath = "giraffe";
		final File nonExistingFile = new File(nonExistingFilePath);
		expectedMessages.add("The scan target '" + nonExistingFile.getAbsolutePath() + "' does not exist.");
		expectedMessages.add("Can not scan targets outside the working directory.");

		final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder(true);

		setBuilderDefaultsBasic(builder);

		builder.addScanTargetPath(nonExistingFilePath);

		final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result = builder.build();
		assertFalse(result.isSuccess());
		actualMessages = getMessages(result);
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
