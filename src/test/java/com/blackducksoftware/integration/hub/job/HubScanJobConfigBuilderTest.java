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
import org.junit.Test;

import com.blackducksoftware.integration.builder.ValidationResult;
import com.blackducksoftware.integration.builder.ValidationResults;
import com.blackducksoftware.integration.hub.builder.HubScanJobConfigBuilder;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;

public class HubScanJobConfigBuilderTest {
    private List<String> expectedMessages;

    private List<String> actualMessages;

    @Before
    public void setUp() {
        expectedMessages = new ArrayList<>();
        actualMessages = new ArrayList<>();
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

        final List<String> messageList = new ArrayList<>();
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
        expectedMessages.add("No scan memory was specified.");
        expectedMessages.add("No maximum wait time for the Bom Update found.");

        final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder(true);
        final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result = new ValidationResults<>();

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
        final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result = new ValidationResults<>();

        builder.validateProjectAndVersion(result);
        assertFalse(result.isSuccess());
        actualMessages = getMessages(result);
    }

    @Test
    public void testValidateProjectAndVersionNoProject() throws HubIntegrationException, IOException {
        expectedMessages.add("No Project name was found.");

        final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder(true);
        builder.setVersion("Version");
        final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result = new ValidationResults<>();

        builder.validateProjectAndVersion(result);
        assertFalse(result.isSuccess());
        actualMessages = getMessages(result);
    }

    @Test
    public void testRiskReportValidationsNoProjectNameOrVersion() throws HubIntegrationException, IOException {
        expectedMessages.add("To generate the Risk Report, you need to provide a Project name or version.");

        final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder(true);
        builder.setShouldGenerateRiskReport(true);
        final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result = new ValidationResults<>();

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
        final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result = new ValidationResults<>();

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
        final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result = new ValidationResults<>();

        builder.validateShouldGenerateRiskReport(result);
        assertFalse(result.isSuccess());
        actualMessages = getMessages(result);
    }

    @Test
    public void testValidateMaxWaitTimeForRiskReport() throws HubIntegrationException, IOException {
        expectedMessages.add("No maximum wait time for the Bom Update found.");

        final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder(true);
        builder.setShouldGenerateRiskReport(true);
        final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result = new ValidationResults<>();

        builder.validateMaxWaitTimeForBomUpdate(result);
        assertFalse(result.isSuccess());
        actualMessages = getMessages(result);
    }

    @Test
    public void testValidateMaxWaitTimeForRiskReportValid() throws HubIntegrationException, IOException {
        final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder(true);
        builder.setShouldGenerateRiskReport(true);
        builder.setMaxWaitTimeForBomUpdate(HubScanJobConfigBuilder.DEFAULT_BOM_UPDATE_WAIT_TIME_IN_MINUTES);
        final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result = new ValidationResults<>();

        builder.validateMaxWaitTimeForBomUpdate(result);
        assertTrue(result.isSuccess());
    }

    @Test
    public void testValidateScanTargetPathsNullTarget() throws HubIntegrationException, IOException {
        final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder(true);

        setBuilderDefaults(builder);

        builder.addScanTargetPath(null);
        final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result = new ValidationResults<>();

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
        final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result = new ValidationResults<>();

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
        final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result = new ValidationResults<>();

        builder.validateScanTargetPaths(result);
        assertTrue(result.isSuccess());
        actualMessages = getMessages(result);
    }

    @Test
    public void testEmptyConfigIsInvalid() throws HubIntegrationException, IOException {
        expectedMessages.add("No Project name or Version were found. Any scans run will not be mapped to a Version.");

        final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder(true);
        final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result = builder.buildResults();
        actualMessages = getMessages(result);
    }

    @Test
    public void testValidConfig() throws HubIntegrationException, IOException {
        final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder(true);

        setBuilderDefaults(builder);

        final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result = builder.buildResults();
        assertTrue(result.isSuccess());
    }

    @Test
    public void testInValidConfigWithDefaults() throws HubIntegrationException, IOException {
        final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder(true);

        builder.setProjectName("projectName");
        builder.setVersion("version");
        builder.setPhase("phase");
        builder.setDistribution("distribution");
        builder.setWorkingDirectory("workingDirectory");
        builder.setScanMemory("-23525");
        builder.setShouldGenerateRiskReport(true);
        builder.setMaxWaitTimeForBomUpdate("cats");
        builder.addScanTargetPath("testPath");
        builder.disableScanTargetPathExistenceCheck();

        final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result = builder.buildResults();
        assertTrue(result.isSuccess());

        actualMessages = getMessages(result);
    }

    @Test
    public void testInValidConfig() throws HubIntegrationException, IOException {
        expectedMessages.add("The String : cats , is not an Integer.");
        expectedMessages.add("The minimum amount of memory for the scan is 256 MB.");

        final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder(false);

        builder.setProjectName("projectName");
        builder.setVersion("version");
        builder.setPhase("phase");
        builder.setDistribution("distribution");
        builder.setWorkingDirectory("workingDirectory");
        builder.setScanMemory("-23525");
        builder.setShouldGenerateRiskReport(true);
        builder.setMaxWaitTimeForBomUpdate("cats");
        builder.addScanTargetPath("testPath");
        builder.disableScanTargetPathExistenceCheck();

        final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result = builder.buildResults();
        assertFalse(result.isSuccess());

        actualMessages = getMessages(result);
    }

    @Test
    public void testConfigInvalidWithProjectNameNoVersion() throws HubIntegrationException, IOException {
        expectedMessages.add("No Version was found.");

        final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder(true);

        setBuilderDefaults(builder);

        builder.setVersion("");

        final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result = builder.buildResults();
        assertFalse(result.isSuccess());
        actualMessages = getMessages(result);
    }

    @Test
    public void testConfigInvalidWithVersionNoProjectName() throws HubIntegrationException, IOException {
        expectedMessages.add("No Project name was found.");

        final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder(true);

        setBuilderDefaults(builder);

        builder.setProjectName("");

        final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result = builder.buildResults();
        assertFalse(result.isSuccess());
        actualMessages = getMessages(result);
    }

    @Test
    public void testConfigValidGeneratingReport() throws HubIntegrationException, IOException {
        final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder(true);

        setBuilderDefaults(builder);

        builder.setShouldGenerateRiskReport(true);
        builder.setMaxWaitTimeForBomUpdate(5);

        final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result = builder.buildResults();
        assertTrue(result.isSuccess());
    }

    @Test
    public void testConfigInvalidGeneratingReportInvalidWaitTime() throws HubIntegrationException, IOException {
        final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder(true);

        setBuilderDefaults(builder);

        builder.setShouldGenerateRiskReport(true);
        builder.setMaxWaitTimeForBomUpdate(0);

        final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result = builder.buildResults();
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

        final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result = builder.buildResults();
        assertFalse(result.isSuccess());
        actualMessages = getMessages(result);
    }

    @Test
    public void testNullScanTargets() throws HubIntegrationException, IOException {
        final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder(true);

        setBuilderDefaults(builder);

        builder.addScanTargetPath(null);

        final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result = builder.buildResults();
        assertTrue(result.isSuccess());
    }

    @Test
    public void testInvalidWithTargetsOutsideWorkingDirectory() throws HubIntegrationException, IOException {
        expectedMessages.add("Can not scan targets outside the working directory.");

        final String relativeClasspathResourcePath = "com/blackducksoftware/integration/hub/existingFileForTestingScanPaths.txt";
        final URL url = HubScanJobConfigBuilder.class.getClassLoader().getResource(relativeClasspathResourcePath);
        final File existingFile = new File(URLDecoder.decode(url.getFile(), "UTF-8"));
        final String absolutePath = existingFile.getAbsolutePath();

        final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder(true);

        setBuilderDefaultsBasic(builder);

        builder.addScanTargetPath(absolutePath);

        final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result = builder.buildResults();
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

        final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result = builder.buildResults();
        assertTrue(result.isSuccess());
    }

    @Test
    public void testConfigValidWithEmptyProjectNameAndVersion() throws HubIntegrationException, IOException {
        expectedMessages.add("No Project name or Version were found. Any scans run will not be mapped to a Version.");

        final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder(true);

        setBuilderDefaults(builder);

        builder.setProjectName(" ");
        builder.setVersion(" ");

        final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result = builder.buildResults();
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

        final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result = builder.buildResults();
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
