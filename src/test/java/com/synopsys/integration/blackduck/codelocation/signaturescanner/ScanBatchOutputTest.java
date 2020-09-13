package com.synopsys.integration.blackduck.codelocation.signaturescanner;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScanCommand;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScanCommandOutput;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScanPathsUtility;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScanTarget;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.SnippetMatching;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.SilentIntLogger;
import com.synopsys.integration.util.IntEnvironmentVariables;
import com.synopsys.integration.util.NameVersion;

public class ScanBatchOutputTest {
    @Test
    public void testCalculatingExpectedNotificationCount() throws Exception {
        ScanBatchBuilder scanBatchBuilder = createBuilder();
        scanBatchBuilder.snippetMatching(SnippetMatching.SNIPPET_MATCHING);

        NameVersion projectAndVersion = extractProjectAndVersion(scanBatchBuilder);
        String[] codeLocationNames = extractCodeLocationNames(scanBatchBuilder);
        List<ScanCommand> scanCommands = createScanCommands(scanBatchBuilder);

        IntLogger silentLogger = new SilentIntLogger();
        List<ScanCommandOutput> scanCommandOutputs = new ArrayList<>();
        scanCommandOutputs.add(ScanCommandOutput.SUCCESS(projectAndVersion, codeLocationNames[0], silentLogger, scanCommands.get(0), ""));
        scanCommandOutputs.add(ScanCommandOutput.FAILURE(projectAndVersion, codeLocationNames[1], silentLogger, scanCommands.get(1), "", 77));
        scanCommandOutputs.add(ScanCommandOutput.SUCCESS(projectAndVersion, codeLocationNames[2], silentLogger, scanCommands.get(2), ""));

        ScanBatchOutput scanBatchOutput = new ScanBatchOutput(scanCommandOutputs);
        assertEquals(new HashSet<>(Arrays.asList(codeLocationNames[0], codeLocationNames[2])), scanBatchOutput.getSuccessfulCodeLocationNames());
        assertEquals(4, scanBatchOutput.getExpectedNotificationCount());
    }

    @Test
    public void testCalculatingExpectedNotificationCountWithoutSnippets() throws Exception {
        ScanBatchBuilder scanBatchBuilder = createBuilder();

        NameVersion projectAndVersion = extractProjectAndVersion(scanBatchBuilder);
        String[] codeLocationNames = extractCodeLocationNames(scanBatchBuilder);
        List<ScanCommand> scanCommands = createScanCommands(scanBatchBuilder);

        IntLogger silentLogger = new SilentIntLogger();
        List<ScanCommandOutput> scanCommandOutputs = new ArrayList<>();
        scanCommandOutputs.add(ScanCommandOutput.SUCCESS(projectAndVersion, codeLocationNames[0], silentLogger, scanCommands.get(0), ""));
        scanCommandOutputs.add(ScanCommandOutput.SUCCESS(projectAndVersion, codeLocationNames[1], silentLogger, scanCommands.get(1), ""));
        scanCommandOutputs.add(ScanCommandOutput.SUCCESS(projectAndVersion, codeLocationNames[2], silentLogger, scanCommands.get(2), ""));

        ScanBatchOutput scanBatchOutput = new ScanBatchOutput(scanCommandOutputs);
        assertEquals(new HashSet<>(Arrays.asList(codeLocationNames)), scanBatchOutput.getSuccessfulCodeLocationNames());
        assertEquals(3, scanBatchOutput.getExpectedNotificationCount());
    }

    private List<ScanCommand> createScanCommands(ScanBatchBuilder scanBatchBuilder) throws BlackDuckIntegrationException {
        ScanBatch scanBatch = scanBatchBuilder.build();
        return scanBatch.createScanCommands(null, Mockito.mock(ScanPathsUtility.class), IntEnvironmentVariables.includeSystemEnv());
    }

    private ScanBatchBuilder createBuilder() {
        final String projectName = "ek-testing-scanner";
        final String versionName = "0.0.1";

        final String targetPath1 = "/Users/ekerwin/Documents/source/integration/libraries/blackduck-common";
        final String targetPath2 = "/Users/ekerwin/Documents/source/integration/libraries/blackduck-common-api";
        final String targetPath3 = "/Users/ekerwin/Documents/source/integration/libraries/integration-common";

        final String installPath = "/Users/ekerwin/working/scan_install";
        final String outputPath = "/Users/ekerwin/working/scan_output";

        ScanBatchBuilder scanBatchBuilder = new ScanBatchBuilder();
        scanBatchBuilder.installDirectory(new File(installPath));
        scanBatchBuilder.outputDirectory(new File(outputPath));
        scanBatchBuilder.projectAndVersionNames(projectName, versionName);

        scanBatchBuilder.addTarget(ScanTarget.createBasicTarget(targetPath1, targetPath1));
        scanBatchBuilder.addTarget(ScanTarget.createBasicTarget(targetPath2, targetPath2));
        scanBatchBuilder.addTarget(ScanTarget.createBasicTarget(targetPath3, targetPath3));

        return scanBatchBuilder;
    }

    private NameVersion extractProjectAndVersion(ScanBatchBuilder scanBatchBuilder) {
        return new NameVersion(scanBatchBuilder.getProjectName(), scanBatchBuilder.getProjectVersionName());
    }

    private String[] extractCodeLocationNames(ScanBatchBuilder scanBatchBuilder) {
        return new String[] { scanBatchBuilder.getScanTargets().get(0).getCodeLocationName(), scanBatchBuilder.getScanTargets().get(1).getCodeLocationName(), scanBatchBuilder.getScanTargets().get(2).getCodeLocationName() };
    }

}
