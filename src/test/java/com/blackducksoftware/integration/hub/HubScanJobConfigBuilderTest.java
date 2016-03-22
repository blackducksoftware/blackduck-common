package com.blackducksoftware.integration.hub;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

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
        List<String> outputList = logger.getOutputList();
        String outputString = logger.getOutputString();
        assertEquals("Too many/not enough messages expected: \n" + outputString, expectedMessages.size(), outputList.size());

        for (String expectedMessage : expectedMessages) {
            assertTrue(outputList.contains(expectedMessage));
        }
    }

    @Test
    public void testEmptyConfigIsInvalid() throws HubIntegrationException, IOException {
        expectedMessages.add("No Project name or Version were found. Any scans run will not be mapped to a Version.");
        expectedMessages.add("The minimum amount of memory for the scan is 256 MB.");
        expectedMessages.add("No scan targets configured.");

        thrown.expect(HubIntegrationException.class);

        HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder();
        builder.build(logger);
    }

    @Test
    public void testValidConfig() throws HubIntegrationException, IOException {
        HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder();

        setBuilderDefaults(builder);

        builder.build(logger);
    }

    @Test
    public void testConfigInvalidWithProjectNameNoVersion() throws HubIntegrationException, IOException {
        expectedMessages.add("No Version was found.");

        thrown.expect(HubIntegrationException.class);

        HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder();

        setBuilderDefaults(builder);

        builder.setVersion("");

        builder.build(logger);
    }

    @Test
    public void testConfigInvalidWithVersionNoProjectName() throws HubIntegrationException, IOException {
        expectedMessages.add("No Project name was found.");

        thrown.expect(HubIntegrationException.class);

        HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder();

        setBuilderDefaults(builder);

        builder.setProjectName("");

        builder.build(logger);
    }

    @Test
    public void testConfigValidGeneratingReport() throws HubIntegrationException, IOException {
        HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder();

        setBuilderDefaults(builder);

        builder.setShouldGenerateRiskReport(true);
        builder.setMaxWaitTimeForRiskReport(5);

        builder.build(logger);
    }

    @Test
    public void testConfigInvalidGeneratingReportInvalidWaitTime() throws HubIntegrationException, IOException {
        expectedMessages.add("The maximum wait time for the Risk Report must be greater than 0.");

        thrown.expect(HubIntegrationException.class);

        HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder();

        setBuilderDefaults(builder);

        builder.setShouldGenerateRiskReport(true);
        builder.setMaxWaitTimeForRiskReport(0);

        builder.build(logger);
    }

    @Test
    public void testConfigInvalidGeneratingReportNeedProjectNameOrVersion() throws HubIntegrationException, IOException {
        expectedMessages.add("No Project name or Version were found. Any scans run will not be mapped to a Version.");
        expectedMessages.add("To generate the Risk Report, you need to provide a Project name or version.");

        thrown.expect(HubIntegrationException.class);

        HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder();

        setBuilderDefaults(builder);

        builder.setShouldGenerateRiskReport(true);
        builder.setMaxWaitTimeForRiskReport(5);
        builder.setProjectName(" ");
        builder.setVersion(null);

        builder.build(logger);
    }

    @Test
    public void testNullScanTargetsInvalid() throws HubIntegrationException, IOException {
        expectedMessages.add("Can not scan null target.");

        thrown.expect(HubIntegrationException.class);

        HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder();

        setBuilderDefaults(builder);

        builder.addScanTargetPath(null);

        builder.build(logger);
    }

    @Test
    public void testInvalidWithTargetsOutsideWorkingDirectory() throws HubIntegrationException, IOException {
        expectedMessages.add("Can not scan targets outside the working directory.");

        thrown.expect(HubIntegrationException.class);

        String relativeClasspathResourcePath = "com/blackducksoftware/integration/hub/existingFileForTestingScanPaths.txt";
        URL url = HubScanJobConfigBuilder.class.getClassLoader().getResource(relativeClasspathResourcePath);
        File existingFile = new File(url.getFile());
        String absolutePath = existingFile.getAbsolutePath();

        HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder();

        setBuilderDefaultsBasic(builder);

        builder.addScanTargetPath(absolutePath);

        builder.build(logger);
    }

    @Test
    public void testConfigValidWithExistingFiles() throws HubIntegrationException, IOException {
        String relativeClasspathResourcePath = "com/blackducksoftware/integration/hub/existingFileForTestingScanPaths.txt";
        URL url = HubScanJobConfigBuilder.class.getClassLoader().getResource(relativeClasspathResourcePath);
        File existingFile = new File(url.getFile());
        String absolutePath = existingFile.getAbsolutePath();
        String workingDirectoryPath = absolutePath.replace(relativeClasspathResourcePath, "");

        HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder();

        setBuilderDefaultsBasic(builder);

        builder.setWorkingDirectory(workingDirectoryPath);
        builder.addScanTargetPath(absolutePath);

        builder.build(logger);
    }

    @Test
    public void testConfigInvalidWithNonExistingFiles() throws HubIntegrationException, IOException {
        String nonExistingFilePath = "giraffe";
        File nonExistingFile = new File(nonExistingFilePath);
        expectedMessages.add("The scan target '" + nonExistingFile.getAbsolutePath() + "' does not exist.");
        expectedMessages.add("Can not scan targets outside the working directory.");

        thrown.expect(HubIntegrationException.class);

        HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder();

        setBuilderDefaultsBasic(builder);

        builder.addScanTargetPath(nonExistingFilePath);

        builder.build(logger);
    }

    private void setBuilderDefaults(HubScanJobConfigBuilder builder) {
        setBuilderDefaultsBasic(builder);

        builder.addScanTargetPath("testPath");
        builder.disableScanTargetPathExistenceCheck();
    }

    private void setBuilderDefaultsBasic(HubScanJobConfigBuilder builder) {
        builder.setProjectName("projectName");
        builder.setVersion("version");
        builder.setPhase("phase");
        builder.setDistribution("distribution");
        builder.setWorkingDirectory("workingDirectory");
        builder.setScanMemory(512);
    }

}
