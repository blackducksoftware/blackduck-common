package com.synopsys.integration.blackduck.codelocation.signaturescanner.command;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import com.synopsys.integration.log.BufferedIntLogger;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.blackduck.codelocation.signaturescanner.ScanBatch;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.ScanBatchBuilder;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.LogLevel;
import com.synopsys.integration.log.PrintStreamIntLogger;
import com.synopsys.integration.util.IntEnvironmentVariables;
import com.synopsys.integration.util.OperatingSystemType;

public class ScanCommandTest {
    Path tempDirectory;
    ScanBatchBuilder scanBatchBuilder;
    IntLogger logger;
    IntEnvironmentVariables intEnvironmentVariables;
    ScanPathsUtility scanPathsUtility;

    @BeforeEach
    public void setup() throws IOException {
        tempDirectory = Files.createTempDirectory("scan_command_test");
        System.out.println(tempDirectory.toString());

        logger = new PrintStreamIntLogger(System.out, LogLevel.INFO);
        intEnvironmentVariables = new IntEnvironmentVariables();
        scanPathsUtility = new ScanPathsUtility(logger, intEnvironmentVariables, OperatingSystemType.determineFromSystem());

        scanBatchBuilder = ScanBatch.newBuilder();
        populateBuilder(scanBatchBuilder);
    }

    @AfterEach
    public void tearDown() throws IOException {
        FileUtils.deleteQuietly(tempDirectory.toFile());
    }

    @Test
    public void testSnippetScan() throws BlackDuckIntegrationException {
        scanBatchBuilder.snippetMatching(SnippetMatching.SNIPPET_MATCHING);
        List<String> commandList = createCommandList();
        assertSnippetCommands(commandList, true, false, false);
    }

    @Test
    public void testSnippetOnlyScan() throws BlackDuckIntegrationException {
        scanBatchBuilder.snippetMatching(SnippetMatching.SNIPPET_MATCHING_ONLY);
        List<String> commandList = createCommandList();
        assertSnippetCommands(commandList, false, true, false);
    }

    @Test
    public void testFullSnippetScan() throws BlackDuckIntegrationException {
        scanBatchBuilder.snippetMatching(SnippetMatching.FULL_SNIPPET_MATCHING);
        List<String> commandList = createCommandList();
        assertSnippetCommands(commandList, true, false, true);
    }

    @Test
    public void testFullSnippetOnlyScan() throws BlackDuckIntegrationException {
        scanBatchBuilder.snippetMatching(SnippetMatching.FULL_SNIPPET_MATCHING_ONLY);
        List<String> commandList = createCommandList();
        assertSnippetCommands(commandList, false, true, true);
    }

    @Test
    public void testSnippetScanWithUploadSource() throws BlackDuckIntegrationException {
        scanBatchBuilder.uploadSource(SnippetMatching.FULL_SNIPPET_MATCHING_ONLY, true);
        List<String> commandList = createCommandList();
        assertSnippetCommands(commandList, false, true, true);
        assertUploadSource(commandList, true);
    }

    @Test
    public void testSnippetScanWithoutUploadSource() throws BlackDuckIntegrationException {
        scanBatchBuilder.uploadSource(SnippetMatching.FULL_SNIPPET_MATCHING_ONLY, false);
        List<String> commandList = createCommandList();
        assertSnippetCommands(commandList, false, true, true);
        assertUploadSource(commandList, false);
    }

    @Test
    public void testSnippetScanLoggingWhenDryRun() throws BlackDuckIntegrationException {
        scanBatchBuilder.snippetMatching(SnippetMatching.FULL_SNIPPET_MATCHING);

        ScanBatch scanBatch = scanBatchBuilder.build();
        ScanCommand scanCommand = assertCommand(scanBatch);

        assertLogging(scanCommand, 0);
        BufferedIntLogger bufferedLogger;

        scanBatchBuilder.dryRun(true);
        scanBatch = scanBatchBuilder.build();
        scanCommand = assertCommand(scanBatch);

        assertLogging(scanCommand, 1);
    }

    @Test
    public void testWithoutLicenseSearch() throws BlackDuckIntegrationException {
        scanBatchBuilder.licenseSearch(false);
        List<String> commandList = createCommandList();
        assertLicenseSearch(commandList, false);
        assertUploadSource(commandList, false);
    }

    @Test
    public void testLicenseSearch() throws BlackDuckIntegrationException {
        scanBatchBuilder.licenseSearch(true);
        List<String> commandList = createCommandList();
        assertLicenseSearch(commandList, true);
        assertUploadSource(commandList, false);
    }

    @Test
    public void testLicenseSearchWithUploadSource() throws BlackDuckIntegrationException {
        scanBatchBuilder.licenseSearch(true);
        scanBatchBuilder.uploadSource(true);
        List<String> commandList = createCommandList();
        assertLicenseSearch(commandList, true);
        assertUploadSource(commandList, true);
    }

    @Test
    public void testLicenseSearchDryRun() throws BlackDuckIntegrationException {
        scanBatchBuilder.licenseSearch(true);

        ScanBatch scanBatch = scanBatchBuilder.build();
        ScanCommand scanCommand = assertCommand(scanBatch);

        assertLogging(scanCommand, 0);
        BufferedIntLogger bufferedLogger;

        scanBatchBuilder.dryRun(true);
        scanBatch = scanBatchBuilder.build();
        scanCommand = assertCommand(scanBatch);

        assertLogging(scanCommand, 1);
    }

    @Test
    public void testIndividualFileMatchingNotSet() throws BlackDuckIntegrationException {
        scanBatchBuilder.individualFileMatching(null);
        List<String> commandList = createCommandList();
        assertIndividualFileMatching(commandList, null);
    }

    @Test
    public void testIndividualFileMatchingValidValue() throws BlackDuckIntegrationException {
        scanBatchBuilder.individualFileMatching(IndividualFileMatching.BINARY);
        List<String> commandList = createCommandList();
        assertIndividualFileMatching(commandList, IndividualFileMatching.BINARY);
    }

    private void populateBuilder(ScanBatchBuilder scanBatchBuilder) {
        try {
            scanBatchBuilder.blackDuckUrl(new URL("http://fakeserver.com"));
            scanBatchBuilder.blackDuckApiToken("fake_token");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        scanBatchBuilder.addTarget(ScanTarget.createBasicTarget("fake_file_path"));
        scanBatchBuilder.outputDirectory(tempDirectory.toFile());
    }

    private List<String> createCommandList() throws BlackDuckIntegrationException {
        ScanBatch scanBatch = scanBatchBuilder.build();
        ScanCommand scanCommand = assertCommand(scanBatch);

        List<String> commandList = scanCommand.createCommandForProcessBuilder(logger, Mockito.mock(ScanPaths.class), scanCommand.getOutputDirectory().getAbsolutePath());
        commandList.stream().forEach(System.out::println);

        return commandList;
    }

    private void assertLogging(ScanCommand scanCommand, int expectedPostLogSize) {
        BufferedIntLogger bufferedLogger = new BufferedIntLogger();
        assertEquals(0, bufferedLogger.getOutputList(LogLevel.WARN).size());
        scanCommand.createCommandForProcessBuilder(bufferedLogger, Mockito.mock(ScanPaths.class), scanCommand.getOutputDirectory().getAbsolutePath());
        assertEquals(expectedPostLogSize, bufferedLogger.getOutputList(LogLevel.WARN).size());
    }

    private ScanCommand assertCommand(ScanBatch scanBatch) throws BlackDuckIntegrationException {
        List<ScanCommand> scanCommands = scanBatch.createScanCommands(null, scanPathsUtility, intEnvironmentVariables);
        assertEquals(1, scanCommands.size());
        return scanCommands.get(0);
    }

    private void assertSnippetCommands(List<String> commandList, boolean containsSnippetMatching, boolean containsSnippetMatchingOnly, boolean containsFullSnippetScan) {
        assertEquals(containsSnippetMatching, commandList.contains("--snippet-matching"));
        assertEquals(containsSnippetMatchingOnly, commandList.contains("--snippet-matching-only"));
        assertEquals(containsFullSnippetScan, commandList.contains("--full-snippet-scan"));
    }

    private void assertLicenseSearch(List<String> commandList, boolean licenseSearch) {
        assertEquals(licenseSearch, commandList.contains("--license-search"));
    }

    private void assertUploadSource(List<String> commandList, boolean uploadSource) {
        assertEquals(uploadSource, commandList.contains("--upload-source"));
    }

    private void assertIndividualFileMatching(List<String> commandList, IndividualFileMatching individualFileMatching) {
        Optional<String> isIndividualFileMatching = commandList
                .stream()
                .filter(s -> s.contains("individualFileMatching"))
                .findAny();
        if (null == individualFileMatching) {
            assertFalse(isIndividualFileMatching.isPresent());
        } else {
            assertEquals("--individualFileMatching=" + individualFileMatching, isIndividualFileMatching.get());
        }
    }

}
