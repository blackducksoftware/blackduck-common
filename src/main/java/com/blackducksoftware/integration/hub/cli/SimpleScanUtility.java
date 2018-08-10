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
package com.blackducksoftware.integration.hub.cli;

import static java.lang.ProcessBuilder.Redirect.PIPE;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.hub.configuration.HubServerConfig;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.exception.ScanFailedException;
import com.blackducksoftware.integration.hub.service.model.ScannerSplitStream;
import com.blackducksoftware.integration.hub.service.model.StreamRedirectThread;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.util.IntEnvironmentVariables;

public class SimpleScanUtility {
    private final IntLogger logger;
    private final SignatureScannerPaths signatureScannerPaths;
    private final SimpleScanData simpleScanData;

    private File logDirectory;

    public SimpleScanUtility(final IntLogger logger, final SimpleScanData simpleScanData) throws HubIntegrationException {
        this.logger = logger;
        this.simpleScanData = simpleScanData;
        signatureScannerPaths = new SignatureScannerPaths(logger, simpleScanData.getInstallDirectory());
    }

    public SimpleScanUtility(final IntLogger logger, final SimpleScanData simpleScanData, final SignatureScannerPaths signatureScannerPaths) {
        this.logger = logger;
        this.simpleScanData = simpleScanData;
        this.signatureScannerPaths = signatureScannerPaths;
    }

    public SimpleScanUtility(final IntLogger logger, final HubServerConfig hubServerConfig, final IntEnvironmentVariables intEnvironmentVariables, final SignatureScanConfig signatureScanConfig, final String projectName,
            final String versionName) throws EncryptionException, HubIntegrationException {
        this.logger = logger;
        simpleScanData = new SimpleScanData(hubServerConfig, signatureScanConfig, intEnvironmentVariables, projectName, versionName);
        signatureScannerPaths = new SignatureScannerPaths(logger, simpleScanData.getInstallDirectory());
    }

    public void setupAndExecuteScan() throws IllegalArgumentException, IOException, EncryptionException, InterruptedException, HubIntegrationException {
        final String logDirectoryPath;
        try {
            populateLogDirectory(simpleScanData);
            logDirectoryPath = logDirectory.getCanonicalPath();
        } catch (final IOException e) {
            throw new HubIntegrationException("Exception creating the log directory for the cli scan: " + e.getMessage(), e);
        }

        final SimpleScanCommand simpleScanCommand = new SimpleScanCommand(logger, signatureScannerPaths, simpleScanData, logDirectoryPath);
        executeScan(simpleScanData, simpleScanCommand);
    }

    /**
     * If running in an environment that handles process creation, this method should be overridden to construct a process to execute the scan in the environment-specific way.
     * @throws IOException
     * @throws HubIntegrationException
     */
    private void executeScan(final SimpleScanData simpleScanData, final SimpleScanCommand simpleScanCommand) throws IllegalArgumentException, IOException, InterruptedException, ScanFailedException {
        simpleScanCommand.printCommand(logger);
        final List<String> cmd = simpleScanCommand.getCmd();

        final File standardOutFile = getStandardOutputFile();
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
            logger.info("You can view the BlackDuck Scan CLI logs at : '" + logDirectory.getCanonicalPath() + "'");

            if (returnCode != 0) {
                throw new ScanFailedException("The scan failed with return code : " + returnCode);
            }
        }
    }

    /**
     * This method can be overridden to provide a more appropriate directory name for the logs of a specific scan execution.
     */
    public String getSpecificScanExecutionLogDirectory() {
        final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss-SSS").withZone(ZoneOffset.UTC);
        final String timeString = Instant.now().atZone(ZoneOffset.UTC).format(dateTimeFormatter);
        final String uniqueLogDirectoryName = timeString + "_" + Thread.currentThread().getId();
        return uniqueLogDirectoryName;
    }

    private void populateLogDirectory(final SimpleScanData simpleScanData) throws IOException {
        final String logDirectoryName = "HubScanLogs";
        final File logsDirectory = new File(simpleScanData.getOutputDirectory(), logDirectoryName);
        final String specificScanExecutionLogDirectory = getSpecificScanExecutionLogDirectory();

        logDirectory = new File(logsDirectory, specificScanExecutionLogDirectory);
        if (!logDirectory.exists() && !logDirectory.mkdirs()) {
            throw new IOException(String.format("Could not create the %s directory!", logDirectory.getAbsolutePath()));
        }
        final File bdIgnoreLogsFile = new File(simpleScanData.getOutputDirectory(), ".bdignore");
        if (!bdIgnoreLogsFile.exists()) {
            if (!bdIgnoreLogsFile.createNewFile()) {
                throw new IOException(String.format("Could not create the %s file!", bdIgnoreLogsFile.getAbsolutePath()));
            }
            final String exclusionPattern = "/" + logDirectoryName + "/";
            Files.write(bdIgnoreLogsFile.toPath(), exclusionPattern.getBytes());
        }
    }

    public IntLogger getLogger() {
        return logger;
    }

    public File getLogDirectory() {
        return logDirectory;
    }

    public File getStatusDirectory() {
        return new File(logDirectory, "status");
    }

    public File getDataDirectory() {
        return new File(logDirectory, "data");
    }

    public File getCLILogDirectory() {
        return new File(logDirectory, "log");
    }

    public File getStandardOutputFile() {
        return new File(logDirectory, "CLI_Output.txt");
    }

    public File getScanSummaryFile() {
        final File scanStatusDirectory = getStatusDirectory();
        if (null != scanStatusDirectory) {
            final File[] scanSummaryFiles = scanStatusDirectory.listFiles((FilenameFilter) (dir, name) -> FilenameUtils.wildcardMatchOnSystem(name, "*.json"));
            if (null != scanSummaryFiles) {
                if (scanSummaryFiles.length == 0) {
                    logger.error("There were no status files found in " + scanStatusDirectory.getAbsolutePath());
                    return null;
                } else if (scanSummaryFiles.length > 1) {
                    logger.error(String.format("There were should have only been 1 status file in '%s' but there are %s", scanStatusDirectory.getAbsolutePath(), scanSummaryFiles.length));
                }
                return scanSummaryFiles[0];
            }
        }
        return null;
    }

    public File getDryRunFile() {
        final File dataDirectory = getDataDirectory();
        if (null != dataDirectory) {
            final File[] dryRunFiles = dataDirectory.listFiles((FilenameFilter) (dir, name) -> FilenameUtils.wildcardMatchOnSystem(name, "*.json"));
            if (null != dryRunFiles) {
                if (dryRunFiles.length == 0) {
                    logger.error("There were no dry run files found in " + dataDirectory.getAbsolutePath());
                    return null;
                } else if (dryRunFiles.length > 1) {
                    logger.error(String.format("There were should have only been 1 dry run in '%s' but there are %s", dataDirectory.getAbsolutePath(), dryRunFiles.length));
                }
                return dryRunFiles[0];
            }
        }
        return null;
    }

}
