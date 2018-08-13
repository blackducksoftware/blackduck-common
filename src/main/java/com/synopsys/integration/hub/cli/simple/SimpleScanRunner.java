/**
 * hub-common
 * <p>
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 * <p>
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.hub.cli.simple;

import static java.lang.ProcessBuilder.Redirect.PIPE;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.exception.EncryptionException;
import com.synopsys.integration.hub.exception.HubIntegrationException;
import com.synopsys.integration.hub.exception.ScanFailedException;
import com.synopsys.integration.hub.service.model.ScannerSplitStream;
import com.synopsys.integration.hub.service.model.StreamRedirectThread;
import com.synopsys.integration.log.IntLogger;

public class SimpleScanRunner {
    private final IntLogger logger;

    public SimpleScanRunner(final IntLogger logger) {
        this.logger = logger;
    }

    public SimpleScanResult setupAndExecuteScan(final SimpleScanPaths simpleScanPaths, final SimpleScanData simpleScanData, final File specificRunOutputDirectory)
            throws EncryptionException, HubIntegrationException, IOException, InterruptedException {
        final SimpleScanResult simpleScanResult = new SimpleScanResult(logger, specificRunOutputDirectory);
        final SimpleScanCommand simpleScanCommand = new SimpleScanCommand(logger, simpleScanPaths, simpleScanData, specificRunOutputDirectory.getCanonicalPath());

        executeScan(simpleScanData, simpleScanCommand, simpleScanResult);

        return simpleScanResult;
    }

    /**
     * If running in an environment that handles process creation, this method should be overridden to construct a process to execute the scan in the environment-specific way.
     * @throws IOException
     * @throws HubIntegrationException
     */
    private void executeScan(final SimpleScanData simpleScanData, final SimpleScanCommand simpleScanCommand, final SimpleScanResult simpleScanResult) throws IllegalArgumentException, IOException, InterruptedException, ScanFailedException {
        simpleScanCommand.printCommand(logger);
        final List<String> cmd = simpleScanCommand.getCmd();

        final File standardOutFile = simpleScanResult.getStandardOutputFile();
        standardOutFile.createNewFile();
        try (FileOutputStream outputFileStream = new FileOutputStream(standardOutFile)) {
            final ScannerSplitStream splitOutputStream = new ScannerSplitStream(logger, outputFileStream);
            final ProcessBuilder processBuilder = new ProcessBuilder(cmd).redirectError(PIPE).redirectOutput(PIPE);
            processBuilder.environment().putAll(simpleScanData.getAllEnvironmentVariables());

            if (!simpleScanData.isDryRun()) {
                if (!StringUtils.isEmpty(simpleScanData.getApiToken())) {
                    processBuilder.environment().put("BD_HUB_TOKEN", simpleScanData.getApiToken());
                } else {
                    processBuilder.environment().put("BD_HUB_PASSWORD", simpleScanData.getPassword());
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

            logger.info("Hub CLI return code : " + returnCode);
            logger.info("You can view the BlackDuck Scan CLI logs at : '" + simpleScanResult.getLogDirectory().getCanonicalPath() + "'");

            if (returnCode != 0) {
                throw new ScanFailedException("The scan failed with return code : " + returnCode);
            }
        }
    }

}
