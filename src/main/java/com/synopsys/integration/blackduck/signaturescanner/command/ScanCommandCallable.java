/**
 * hub-common
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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
 */
package com.synopsys.integration.blackduck.signaturescanner.command;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.blackduck.exception.HubIntegrationException;
import com.synopsys.integration.blackduck.exception.ScanFailedException;
import com.synopsys.integration.blackduck.service.model.ScannerSplitStream;
import com.synopsys.integration.blackduck.service.model.StreamRedirectThread;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.util.IntEnvironmentVariables;

public class ScanCommandCallable implements Callable<ScanCommandOutput> {
    private static final List<String> DRY_RUN_FILES_TO_KEEP = Arrays.asList("data");

    private final IntLogger logger;
    private final ScanPathsUtility scanPathsUtility;
    private final IntEnvironmentVariables intEnvironmentVariables;
    private final ScanCommand scanCommand;
    private final boolean cleanupOutput;

    public ScanCommandCallable(final IntLogger logger, final ScanPathsUtility scanPathsUtility, final IntEnvironmentVariables intEnvironmentVariables, final ScanCommand scanCommand, final boolean cleanupOutput) {
        this.logger = logger;
        this.scanPathsUtility = scanPathsUtility;
        this.intEnvironmentVariables = intEnvironmentVariables;
        this.scanCommand = scanCommand;
        this.cleanupOutput = cleanupOutput;
    }

    @Override
    public ScanCommandOutput call() throws IOException, HubIntegrationException {
        final File specificRunOutputDirectory = scanCommand.getOutputDirectoryCallable().call();

        try {
            final ScanPaths scanPaths = scanPathsUtility.determineSignatureScannerPaths(scanCommand.getInstallDirectory());

            final List<String> cmd = scanCommand.createCommandForProcessBuilder(logger, scanPaths, specificRunOutputDirectory.getAbsolutePath());

            printCommand(cmd);

            final File standardOutFile = scanPathsUtility.createStandardOutFile(specificRunOutputDirectory);
            try (FileOutputStream outputFileStream = new FileOutputStream(standardOutFile)) {
                final ScannerSplitStream splitOutputStream = new ScannerSplitStream(logger, outputFileStream);
                final ProcessBuilder processBuilder = new ProcessBuilder(cmd);
                processBuilder.environment().putAll(intEnvironmentVariables.getVariables());

                if (!scanCommand.isDryRun()) {
                    if (!StringUtils.isEmpty(scanCommand.getApiToken())) {
                        processBuilder.environment().put("BD_HUB_TOKEN", scanCommand.getApiToken());
                    } else {
                        processBuilder.environment().put("BD_HUB_PASSWORD", scanCommand.getPassword());
                    }
                }
                processBuilder.environment().put("BD_HUB_NO_PROMPT", "true");

                final Process hubCliProcess = processBuilder.start();

                // The cli logs go the error stream for some reason
                final StreamRedirectThread redirectThread = new StreamRedirectThread(hubCliProcess.getErrorStream(), splitOutputStream);
                redirectThread.start();

                int returnCode = -1;
                try {
                    returnCode = hubCliProcess.waitFor();

                    // the join method on the redirect thread will wait until the thread is dead
                    // the thread will die when it reaches the end of stream and the run method is finished
                    redirectThread.join();
                } finally {
                    if (hubCliProcess.isAlive()) {
                        hubCliProcess.destroy();
                    }
                    if (redirectThread.isAlive()) {
                        redirectThread.interrupt();
                    }
                }

                splitOutputStream.flush();

                logger.info(IOUtils.toString(hubCliProcess.getInputStream(), StandardCharsets.UTF_8));

                logger.info("Black Duck Signature Scanner return code: " + returnCode);
                logger.info("You can view the logs at: '" + specificRunOutputDirectory.getCanonicalPath() + "'");

                if (returnCode != 0) {
                    throw new ScanFailedException("The scan failed with return code: " + returnCode);
                }
            }
        } catch (final Exception e) {
            final String errorMessage = String.format("There was a problem scanning target '%s': %s", scanCommand.getTargetPath(), e.getMessage());
            return ScanCommandOutput.FAILURE(logger, specificRunOutputDirectory, scanCommand.getTargetPath(), scanCommand.isDryRun(), errorMessage, e);
        }

        if (!scanCommand.isDryRun() && cleanupOutput) {
            FileUtils.deleteQuietly(specificRunOutputDirectory);
        } else if (scanCommand.isDryRun() && cleanupOutput) {
            // delete everything except dry run files
            final File[] outputFiles = specificRunOutputDirectory.listFiles();
            for (final File outputFile : outputFiles) {
                if (!DRY_RUN_FILES_TO_KEEP.contains(outputFile.getName())) {
                    FileUtils.deleteQuietly(outputFile);
                }
            }
        }

        final ScanCommandOutput success = ScanCommandOutput.SUCCESS(logger, specificRunOutputDirectory, scanCommand.getTargetPath(), scanCommand.isDryRun());
        return success;
    }

    /**
     * Code to mask passwords in the logs
     */
    private void printCommand(final List<String> cmd) {
        final List<String> cmdToOutput = new ArrayList<>();
        cmdToOutput.addAll(cmd);

        int passwordIndex = cmdToOutput.indexOf("--password");
        if (passwordIndex > -1) {
            // The User's password will be at the next index
            passwordIndex++;
        }

        int proxyPasswordIndex = -1;
        for (int commandIndex = 0; commandIndex < cmdToOutput.size(); commandIndex++) {
            final String commandParameter = cmdToOutput.get(commandIndex);
            if (commandParameter.contains("-Dhttp.proxyPassword=")) {
                proxyPasswordIndex = commandIndex;
            }
        }

        maskIndex(cmdToOutput, passwordIndex);
        maskIndex(cmdToOutput, proxyPasswordIndex);

        logger.info(String.format("Hub CLI command: %s", StringUtils.join(cmdToOutput, " ")));
    }

    private void maskIndex(final List<String> cmd, final int indexToMask) {
        if (indexToMask > -1) {
            final String cmdToMask = cmd.get(indexToMask);
            final String[] maskedArray = new String[cmdToMask.length()];
            Arrays.fill(maskedArray, "*");
            cmd.set(indexToMask, StringUtils.join(maskedArray));
        }
    }

}
