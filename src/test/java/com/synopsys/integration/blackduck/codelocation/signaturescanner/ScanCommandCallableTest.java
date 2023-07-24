package com.synopsys.integration.blackduck.codelocation.signaturescanner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScanCommand;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScanCommandCallable;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScanCommandOutput;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScanPaths;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScanPathsUtility;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScanTarget;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.LogLevel;
import com.synopsys.integration.log.PrintStreamIntLogger;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.util.IntEnvironmentVariables;
import com.synopsys.integration.util.OperatingSystemType;

public class ScanCommandCallableTest {

	@Test
	public void testLongWindowsPath() throws BlackDuckIntegrationException, IOException {
		Path tempDirectory = Files.createTempDirectory("scan_command_test");
		IntLogger logger = new PrintStreamIntLogger(System.out, LogLevel.INFO);

        ScanPaths scanPaths = new ScanPaths("blah", "blah", "blah", "blah", false);
        ScanPathsUtility spiedscanPathsUtility = Mockito.mock(ScanPathsUtility.class);
        Mockito.when(spiedscanPathsUtility.searchForScanPaths(tempDirectory.toFile())).thenReturn(scanPaths);
        Mockito.when(spiedscanPathsUtility.createSpecificRunOutputDirectory(tempDirectory.toFile())).thenReturn(new File(tempDirectory.toFile() + "/a"));
        ScanBatchBuilder scanBatchBuilder = ScanBatch.newBuilder();
        
        IntEnvironmentVariables intEnvironmentVariables = Mockito.mock(IntEnvironmentVariables.class);
        Mockito.when(intEnvironmentVariables.getValue("SCAN_CLI_OPTS")).thenReturn("x".repeat(38000));
		
        Mockito.mockStatic(OperatingSystemType.class);
        Mockito.when(OperatingSystemType.determineFromSystem()).thenReturn(OperatingSystemType.WINDOWS);
        
        try {
            scanBatchBuilder.blackDuckUrl(new HttpUrl("http://fakeserver.com"));
            scanBatchBuilder.blackDuckApiToken("fake_token");
        } catch (IntegrationException e) {
            e.printStackTrace();
        }
        scanBatchBuilder.addTarget(ScanTarget.createBasicTarget("fake_file_path"));
        scanBatchBuilder.outputDirectory(tempDirectory.toFile());
        
        ScanBatch scanBatch = scanBatchBuilder.build();
        List<ScanCommand> scanCommands = scanBatch.createScanCommands(tempDirectory.toFile(), spiedscanPathsUtility, intEnvironmentVariables);
        ScanCommand scanCommand = scanCommands.get(0);

		ScanCommandCallable scanCommandCallable = new ScanCommandCallable(logger, spiedscanPathsUtility, intEnvironmentVariables, scanCommand, false);
		
		ScanCommandOutput output = scanCommandCallable.call();
		
		Assertions.assertTrue(output.getErrorMessage().get().equals("Unable to invoke the scan CLI as the length of the command would exceed the operating system limit."));
	
        FileUtils.deleteQuietly(tempDirectory.toFile());
	}

}
