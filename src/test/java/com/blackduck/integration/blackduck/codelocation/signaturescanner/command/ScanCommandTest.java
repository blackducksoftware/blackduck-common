package com.blackduck.integration.blackduck.codelocation.signaturescanner.command;

import com.blackduck.integration.blackduck.TimingExtension;
import com.blackduck.integration.blackduck.codelocation.signaturescanner.ScanBatch;
import com.blackduck.integration.blackduck.codelocation.signaturescanner.ScanBatchBuilder;
import com.blackduck.integration.blackduck.exception.BlackDuckIntegrationException;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.log.BufferedIntLogger;
import com.blackduck.integration.log.IntLogger;
import com.blackduck.integration.log.LogLevel;
import com.blackduck.integration.log.PrintStreamIntLogger;
import com.blackduck.integration.rest.HttpUrl;
import com.blackduck.integration.rest.credentials.CredentialsBuilder;
import com.blackduck.integration.rest.proxy.ProxyInfo;
import com.blackduck.integration.rest.proxy.ProxyInfoBuilder;
import com.blackduck.integration.util.IntEnvironmentVariables;
import com.blackduck.integration.util.OperatingSystemType;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

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
    	assertTrue(commandList.contains("--no-persistence"));
    }
    
    @Test
    public void testIsNotRapidSignatureScan() throws IntegrationException {
    	scanBatchBuilder.rapid(false);
    	List<String> commandList = createCommandList();
    	assertFalse(commandList.contains("--no-persistence"));
    }
    
    @Test
    public void testRetainUnmatchedFiles() throws IntegrationException {
    	scanBatchBuilder.reducedPersistence(ReducedPersistence.RETAIN_UNMATCHED);
    	List<String> commandList = createCommandList();
    	assertTrue(commandList.contains("--retain-unmatched-files"));
    }
    
    @Test
    public void testDiscardUnmatchedFiles() throws IntegrationException {
    	scanBatchBuilder.reducedPersistence(ReducedPersistence.DISCARD_UNMATCHED);
    	List<String> commandList = createCommandList();
    	assertTrue(commandList.contains("--discard-unmatched-files"));
    }
    
    @Test
    public void testNoPersistentModeSpecifiedWithRapidModeSpecified() throws IntegrationException {
    	scanBatchBuilder.rapid(true);
    	scanBatchBuilder.bomCompareMode("BOM_COMPARE_STRICT");
    	List<String> commandList = createCommandList();
        assertKeyValuePairIsSet(commandList, "--no-persistence-mode", "BOM_COMPARE_STRICT");
    }

    @Test
    public void testNoPersistenceModeOverridden() throws IntegrationException {
        scanBatchBuilder.rapid(true);
        scanBatchBuilder.bomCompareMode("BOM_COMPARE_STRICT");
        scanBatchBuilder.additionalScanArguments("--no-persistence-mode SOME_OTHER_MODE");
        List<String> commandList = createCommandList();

        assertFalse(commandList.contains("BOM_COMPARE_STRICT"));
        assertKeyValuePairIsSet(commandList, "--no-persistence-mode", "SOME_OTHER_MODE");
    }

    @Test
    public void testNoPersistentModeNotSpecified() throws IntegrationException {
    	scanBatchBuilder.rapid(false);
    	List<String> commandList = createCommandList();
    	assertFalse(commandList.contains("--no-persistence-mode=BOM_COMPARE_STRICT"));
        assertFalse(commandList.contains("--no-persistence-mode"));
        assertFalse(commandList.contains("BOM_COMPARE_STRICT"));
    }
    
    @Test
    public void testNoPersistentModeSpecifiedWithRapidModeNotSpecified() throws IntegrationException {
    	scanBatchBuilder.rapid(false);
    	scanBatchBuilder.bomCompareMode("BOM_COMPARE_STRICT");
    	List<String> commandList = createCommandList();
    	assertFalse(commandList.contains("--no-persistence-mode=BOM_COMPARE_STRICT"));
        assertFalse(commandList.contains("--no-persistence-mode"));
        assertFalse(commandList.contains("BOM_COMPARE_STRICT"));
    }

    @Test
    public void testExcludePatternsSpecified() throws IntegrationException {
        List<String> commandList = createCommandList();

        assertKeyValuePairIsSet(commandList, "--exclude", "/exclude-me/");
        assertKeyValuePairIsSet(commandList, "--exclude", "/me-as-well/");
    }

    @Test
    public void testExcludePatternsOverridden() throws IntegrationException {
        scanBatchBuilder.additionalScanArguments("--exclude /xyz/ --exclude /abc/");
        List<String> commandList = createCommandList();

        assertKeyValuePairIsSet(commandList, "--exclude", "/abc/");
        assertKeyValuePairIsSet(commandList, "--exclude", "/xyz/");

        assertFalse(commandList.contains("/exclude-me/"));
        assertFalse(commandList.contains("/me-as-well/"));
    }

    @Test
    public void testAdditionalArgumentsSpecified() throws IntegrationException {
        scanBatchBuilder.additionalScanArguments("--argX x --argY y --test");
        List<String> commandList = createCommandList();

        assertKeyValuePairIsSet(commandList, "--argX", "x");
        assertKeyValuePairIsSet(commandList, "--argY", "y");
        assertTrue(commandList.contains("--test"));
    }

    @Test
    public void testNoProxyDetailsAreSetByDefault() throws IntegrationException {
        List<String> commandList = createCommandList();

        for (String arg : commandList) {
            assertFalse(arg.contains("-Dhttp.proxy"));
            assertFalse(arg.contains("-Dhttp.auth"));
            assertFalse(arg.contains("-Dblackduck.http"));
        }
    }

    @Test
    public void testProxyDetailsAreSet() throws IntegrationException {
        ProxyInfoBuilder proxyInfoBuilder = ProxyInfo.newBuilder();

        CredentialsBuilder credentialsBuilder = new CredentialsBuilder();
        credentialsBuilder.setUsernameAndPassword("some_username", "some_password");

        proxyInfoBuilder.setCredentials(credentialsBuilder.build());
        proxyInfoBuilder.setHost("some_host");
        proxyInfoBuilder.setNtlmDomain("some_domain");
        proxyInfoBuilder.setNtlmWorkstation("some_workstation");
        proxyInfoBuilder.setPort(443);

        ProxyInfo proxyInfo = proxyInfoBuilder.build();
        scanBatchBuilder.proxyInfo(proxyInfo);

        List<String> commandList = createCommandList();

        assertTrue(commandList.contains("-Dhttp.proxyHost=some_host"));
        assertTrue(commandList.contains("-Dhttp.proxyPort=443"));
        assertTrue(commandList.contains("-Dhttp.proxyUser=some_username"));
        assertTrue(commandList.contains("-Dhttp.proxyPassword=some_password"));
        assertTrue(commandList.contains("-Dhttp.auth.ntlm.domain=some_domain"));
        assertTrue(commandList.contains("-Dblackduck.http.auth.ntlm.workstation=some_workstation"));
    }

    private void populateBuilder(ScanBatchBuilder scanBatchBuilder) {
        try {
            scanBatchBuilder.blackDuckUrl(new HttpUrl("http://fakeserver.com"));
            scanBatchBuilder.blackDuckApiToken("fake_token");
        } catch (IntegrationException e) {
            e.printStackTrace();
        }

        Set<String> exclusionPatterns = new HashSet<>();
        exclusionPatterns.add("/exclude-me/");
        exclusionPatterns.add("/me-as-well/");

        scanBatchBuilder.addTarget(ScanTarget.createBasicTarget("fake_file_path", exclusionPatterns, null));
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
        if (null == individualFileMatching) {
            assertEquals(-1, commandList.indexOf("--individualFileMatching"));
        } else {
            assertKeyValuePairIsSet(commandList, "--individualFileMatching", individualFileMatching.toString());
        }
    }

    private void assertKeyValuePairIsSet(List<String> commandList, String key, String value) {
        for (int i = 0; i < commandList.size() - 1; i++) {
            if (key.equals(commandList.get(i)) && value.equals(commandList.get(i + 1))) {
                return;
            }
        }
        assertTrue(false, "Key/value pair not found");
    }
}
