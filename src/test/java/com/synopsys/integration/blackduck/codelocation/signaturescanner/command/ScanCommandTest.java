package com.synopsys.integration.blackduck.codelocation.signaturescanner.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import com.synopsys.integration.blackduck.TimingExtension;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.ScanBatch;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.ScanBatchBuilder;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.BufferedIntLogger;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.LogLevel;
import com.synopsys.integration.log.PrintStreamIntLogger;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.util.IntEnvironmentVariables;
import com.synopsys.integration.util.OperatingSystemType;

@ExtendWith(TimingExtension.class)
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
        intEnvironmentVariables = IntEnvironmentVariables.includeSystemEnv();
        scanPathsUtility = new ScanPathsUtility(logger, intEnvironmentVariables, OperatingSystemType.determineFromSystem());

        scanBatchBuilder = ScanBatch.newBuilder();
        populateBuilder(scanBatchBuilder);
    }

    @AfterEach
    public void tearDown() throws IOException {
        FileUtils.deleteQuietly(tempDirectory.toFile());
    }

    @Test
    public void testSnippetScan() throws IntegrationException {
        scanBatchBuilder.snippetMatching(SnippetMatching.SNIPPET_MATCHING);
        List<String> commandList = createCommandList();
        assertSnippetCommands(commandList, true, false, false);
    }

    @Test
    public void testSnippetOnlyScan() throws IntegrationException {
        scanBatchBuilder.snippetMatching(SnippetMatching.SNIPPET_MATCHING_ONLY);
        List<String> commandList = createCommandList();
        assertSnippetCommands(commandList, false, true, false);
    }

    @Test
    public void testFullSnippetScan() throws IntegrationException {
        scanBatchBuilder.snippetMatching(SnippetMatching.FULL_SNIPPET_MATCHING);
        List<String> commandList = createCommandList();
        assertSnippetCommands(commandList, true, false, true);
    }

    @Test
    public void testFullSnippetOnlyScan() throws IntegrationException {
        scanBatchBuilder.snippetMatching(SnippetMatching.FULL_SNIPPET_MATCHING_ONLY);
        List<String> commandList = createCommandList();
        assertSnippetCommands(commandList, false, true, true);
    }

    @Test
    public void testSnippetScanWithUploadSource() throws IntegrationException {
        scanBatchBuilder.uploadSource(SnippetMatching.FULL_SNIPPET_MATCHING_ONLY, true);
        List<String> commandList = createCommandList();
        assertSnippetCommands(commandList, false, true, true);
        assertUploadSource(commandList, true);
    }

    @Test
    public void testSnippetScanWithoutUploadSource() throws IntegrationException {
        scanBatchBuilder.uploadSource(SnippetMatching.FULL_SNIPPET_MATCHING_ONLY, false);
        List<String> commandList = createCommandList();
        assertSnippetCommands(commandList, false, true, true);
        assertUploadSource(commandList, false);
    }

    @Test
    public void testSnippetScanLoggingWhenDryRun() throws IntegrationException {
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
    public void testWithoutLicenseSearch() throws IntegrationException {
        scanBatchBuilder.licenseSearch(false);
        List<String> commandList = createCommandList();
        assertLicenseSearch(commandList, false);
        assertUploadSource(commandList, false);
    }

    @Test
    public void testLicenseSearch() throws IntegrationException {
        scanBatchBuilder.licenseSearch(true);
        List<String> commandList = createCommandList();
        assertLicenseSearch(commandList, true);
        assertUploadSource(commandList, false);
    }

    @Test
    public void testLicenseSearchWithUploadSource() throws IntegrationException {
        scanBatchBuilder.licenseSearch(true);
        scanBatchBuilder.uploadSource(true);
        List<String> commandList = createCommandList();
        assertLicenseSearch(commandList, true);
        assertUploadSource(commandList, true);
    }

    @Test
    public void testLicenseSearchDryRun() throws IntegrationException {
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
    public void testIndividualFileMatchingNotSet() throws IntegrationException {
        scanBatchBuilder.individualFileMatching(null);
        List<String> commandList = createCommandList();
        assertIndividualFileMatching(commandList, null);
    }

    @Test
    public void testIndividualFileMatchingValidValue() throws IntegrationException {
        scanBatchBuilder.individualFileMatching(IndividualFileMatching.BINARY);
        List<String> commandList = createCommandList();
        assertIndividualFileMatching(commandList, IndividualFileMatching.BINARY);
    }
    
    @Test
    public void testIsRapidSignatureScan() throws IntegrationException {
    	scanBatchBuilder.rapid(true);
    	List<String> commandList = createCommandList();
    	assertEquals(true, commandList.contains("--no-persistence"));
    }
    
    @Test
    public void testIsNotRapidSignatureScan() throws IntegrationException {
    	scanBatchBuilder.rapid(false);
    	List<String> commandList = createCommandList();
    	assertEquals(false, commandList.contains("--no-persistence"));
    }

    private void populateBuilder(ScanBatchBuilder scanBatchBuilder) {
        try {
            scanBatchBuilder.blackDuckUrl(new HttpUrl("http://fakeserver.com"));
            scanBatchBuilder.blackDuckApiToken("fake_token");
        } catch (IntegrationException e) {
            e.printStackTrace();
        }
        scanBatchBuilder.addTarget(ScanTarget.createBasicTarget("fake_file_path"));
        scanBatchBuilder.outputDirectory(tempDirectory.toFile());
    }

    private List<String> createCommandList() throws IntegrationException {
        ScanBatch scanBatch = scanBatchBuilder.build();
        ScanCommand scanCommand = assertCommand(scanBatch);

        List<String> commandList = scanCommand.createCommandForProcessBuilder(logger, Mockito.mock(ScanPaths.class), scanCommand.getOutputDirectory().getAbsolutePath());
        commandList.stream().forEach(System.out::println);

        return commandList;
    }

    private void assertLogging(ScanCommand scanCommand, int expectedPostLogSize) throws IntegrationException {
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
