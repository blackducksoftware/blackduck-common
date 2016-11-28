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
package com.blackducksoftware.integration.hub.cli;

import static java.lang.ProcessBuilder.Redirect.PIPE;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.restlet.engine.io.IoUtils;

import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.hub.HubSupportHelper;
import com.blackducksoftware.integration.hub.ScanExecutor.Result;
import com.blackducksoftware.integration.hub.ScannerSplitStream;
import com.blackducksoftware.integration.hub.StreamRedirectThread;
import com.blackducksoftware.integration.hub.api.HubRestService;
import com.blackducksoftware.integration.hub.api.scan.ScanSummaryItem;
import com.blackducksoftware.integration.hub.capabilities.HubCapabilitiesEnum;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.global.HubProxyInfo;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.util.CIEnvironmentVariables;

public class SimpleScanService extends HubRestService<String> {
    private final IntLogger logger;

    private final HubServerConfig hubServerConfig;

    private final HubSupportHelper hubSupportHelper;

    private final CIEnvironmentVariables ciEnvironmentVariables;

    private final File directoryToInstallTo;

    private final int scanMemory;

    private final boolean verboseRun;

    private final boolean dryRun;

    private final String project;

    private final String version;

    private final List<String> scanTargetPaths;

    private final String workingDirectoryPath;

    private final List<String> cmd = new ArrayList<>();

    private File logDirectory;

    public SimpleScanService(IntLogger logger, RestConnection restConnection, HubServerConfig hubServerConfig, HubSupportHelper hubSupportHelper,
            CIEnvironmentVariables ciEnvironmentVariables, final File directoryToInstallTo, int scanMemory, boolean verboseRun, boolean dryRun, String project,
            String version, List<String> scanTargetPaths, String workingDirectoryPath) {
        super(restConnection, String.class);
        this.logger = logger;
        this.hubServerConfig = hubServerConfig;
        this.hubSupportHelper = hubSupportHelper;
        this.ciEnvironmentVariables = ciEnvironmentVariables;
        this.directoryToInstallTo = directoryToInstallTo;
        this.scanMemory = scanMemory;
        this.verboseRun = verboseRun;
        this.dryRun = dryRun;
        this.project = project;
        this.version = version;
        this.scanTargetPaths = scanTargetPaths;
        this.workingDirectoryPath = workingDirectoryPath;
    }

    /**
     * This will setup the command-line invocation of the Hub scanner. The workingDirectoryPath is the parent folder of
     * the scan logs and other scan artifacts.
     */
    public Result setupAndExecuteScan() throws HubIntegrationException, IOException, IllegalArgumentException, InterruptedException, EncryptionException {
        final CLILocation cliLocation = new CLILocation(directoryToInstallTo);
        final String pathToJavaExecutable = cliLocation.getProvidedJavaExec().getCanonicalPath();
        final String pathToOneJar = cliLocation.getOneJarFile().getCanonicalPath();
        final String pathToScanExecutable = cliLocation.getCLI(logger).getCanonicalPath();
        logger.debug("Using this java installation : " + pathToJavaExecutable);

        cmd.add(pathToJavaExecutable);
        cmd.add("-Done-jar.silent=true");
        cmd.add("-Done-jar.jar.path=" + pathToOneJar);

        if (hubServerConfig.shouldUseProxyForHub()) {
            final HubProxyInfo hubProxyInfo = hubServerConfig.getProxyInfo();
            final String proxyHost = hubProxyInfo.getHost();
            final int proxyPort = hubProxyInfo.getPort();
            final String proxyUsername = hubProxyInfo.getUsername();
            final String proxyPassword = hubProxyInfo.getDecryptedPassword();
            cmd.add("-Dhttp.proxyHost=" + proxyHost);
            cmd.add("-Dhttp.proxyPort=" + Integer.toString(proxyPort));
            if (StringUtils.isNotBlank(proxyUsername) && StringUtils.isNotBlank(proxyPassword)) {
                cmd.add("-Dhttp.proxyUser=" + proxyUsername);
                cmd.add("-Dhttp.proxyPassword=" + proxyPassword);
            }
        }

        cmd.add("-Xmx" + scanMemory + "m");
        cmd.add("-jar");
        cmd.add(pathToScanExecutable);
        cmd.add("--scheme");
        cmd.add(hubServerConfig.getHubUrl().getProtocol());
        cmd.add("--host");
        cmd.add(hubServerConfig.getHubUrl().getHost());
        logger.debug("Using this Hub hostname : '" + hubServerConfig.getHubUrl().getHost() + "'");
        cmd.add("--username");
        cmd.add(hubServerConfig.getGlobalCredentials().getUsername());
        if (!hubSupportHelper.hasCapability(HubCapabilitiesEnum.CLI_PASSWORD_ENVIRONMENT_VARIABLE)) {
            cmd.add("--password");
            cmd.add(hubServerConfig.getGlobalCredentials().getDecryptedPassword());
        }

        final int hubPort = hubServerConfig.getHubUrl().getPort();
        if (hubPort > 0) {
            cmd.add("--port");
            cmd.add(Integer.toString(hubPort));
        } else {
            final int defaultPort = hubServerConfig.getHubUrl().getDefaultPort();
            if (defaultPort > 0) {
                cmd.add("--port");
                cmd.add(Integer.toString(defaultPort));
            } else {
                logger.warn("Could not find a port to use for the Server.");
            }
        }

        if (verboseRun) {
            cmd.add("-v");
        }

        populateLogDirectory();
        final String logDirectoryPath = logDirectory.getCanonicalPath();
        cmd.add("--logDir");
        cmd.add(logDirectoryPath);

        if (dryRun) {
            // The dryRunWriteDir is the same as the log directory path
            // The CLI will create a subdirectory for the json files
            cmd.add("--dryRunWriteDir");
            cmd.add(logDirectoryPath);
        }

        if (hubSupportHelper.hasCapability(HubCapabilitiesEnum.CLI_STATUS_DIRECTORY_OPTION)) {
            // Only add the statusWriteDir option if the Hub supports the statusWriteDir option
            // The scanStatusDirectoryPath is the same as the log directory path
            // The CLI will create a subdirectory for the status files
            cmd.add("--statusWriteDir");
            cmd.add(logDirectoryPath);
        }

        if (StringUtils.isNotBlank(project) && StringUtils.isNotBlank(version)) {
            cmd.add("--project");
            cmd.add(project);
            cmd.add("--release");
            cmd.add(version);
        }

        for (final String target : scanTargetPaths) {
            cmd.add(target);
        }

        return executeScan();
    }

    /**
     * If running in an environment that handles process creation, this method should be overridden to construct a
     * process to execute the scan in the environment-specific way.
     */
    public Result executeScan()
            throws IOException, InterruptedException, IllegalArgumentException, EncryptionException {
        printCommand();

        final File standardOutFile = new File(logDirectory, "CLI_Output.txt");
        standardOutFile.createNewFile();
        try (FileOutputStream outputFileStream = new FileOutputStream(standardOutFile)) {
            final ScannerSplitStream splitOutputStream = new ScannerSplitStream(logger, outputFileStream);
            final ProcessBuilder processBuilder = new ProcessBuilder(cmd).redirectError(PIPE).redirectOutput(PIPE);

            processBuilder.environment().put("BD_HUB_PASSWORD", hubServerConfig.getGlobalCredentials().getDecryptedPassword());

            final String bdioEnvVar = ciEnvironmentVariables.getValue("BD_HUB_DECLARED_COMPONENTS");
            if (StringUtils.isNotBlank(bdioEnvVar)) {
                processBuilder.environment().put("BD_HUB_DECLARED_COMPONENTS", bdioEnvVar);
            }

            final Process hubCliProcess = processBuilder.start();

            // The cli logs go the error stream for some reason
            final StreamRedirectThread redirectThread = new StreamRedirectThread(hubCliProcess.getErrorStream(), splitOutputStream);
            redirectThread.start();

            final int returnCode = hubCliProcess.waitFor();

            // the join method on the redirect thread will wait until the thread is dead
            // the thread will die when it reaches the end of stream and the run method is finished
            redirectThread.join();

            splitOutputStream.flush();
            logger.info(IoUtils.toString((hubCliProcess.getInputStream())));

            logger.info("Hub CLI return code : " + returnCode);
            logger.info("You can view the BlackDuck Scan CLI logs at : '" + logDirectory.getCanonicalPath() + "'");

            if (returnCode == 0) {
                return Result.SUCCESS;
            } else {
                return Result.FAILURE;
            }
        }
    }

    /**
     * For all error cases, return an empty list. If all goes well, return a list of scan summary urls.
     */
    public List<ScanSummaryItem> getScanSummaryItems() {
        if (null == logDirectory) {
            return Collections.emptyList();
        }

        final File scanStatusDirectory = new File(logDirectory, "status");
        final File[] statusFiles = scanStatusDirectory.listFiles();

        if (statusFiles.length != scanTargetPaths.size()) {
            logger.error(String.format("There were %d scans target paths and %d status files.", scanTargetPaths.size(), statusFiles.length));
            return Collections.emptyList();
        }

        final List<ScanSummaryItem> scanSummaryItems = new ArrayList<>();
        for (final File currentStatusFile : statusFiles) {
            String fileContent;
            try {
                fileContent = FileUtils.readFileToString(currentStatusFile, "UTF8");
            } catch (final IOException e) {
                logger.error(String.format("There was an exception reading the status file: %s", e.getMessage(), e));
                return Collections.emptyList();
            }
            final ScanSummaryItem scanSummaryItem = getRestConnection().getGson().fromJson(fileContent, ScanSummaryItem.class);
            scanSummaryItems.add(scanSummaryItem);
        }

        return scanSummaryItems;
    }

    /**
     * This method can be overridden to provide a more appropriate directory name for the logs of a specific scan
     * execution.
     */
    public String getSpecificScanExecutionLogDirectory() {
        final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd_HH-mm-ss-SSS").withZoneUTC();
        final String timeString = DateTime.now().withZone(DateTimeZone.UTC).toString(dateTimeFormatter);
        return timeString;
    }

    private void populateLogDirectory() throws IOException {
        final File logsDirectory = new File(workingDirectoryPath, "HubScanLogs");
        final String specificScanExecutionLogDirectory = getSpecificScanExecutionLogDirectory();

        logDirectory = new File(logsDirectory, specificScanExecutionLogDirectory);
        if (!logDirectory.exists() && !logDirectory.mkdirs()) {
            throw new IOException("Could not create the HubScanLogs directory!");
        }
    }

    /**
     * Code to mask passwords in the logs
     */
    private void printCommand() {
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

        logger.info("Hub CLI command :");
        for (final String current : cmdToOutput) {
            logger.info(current);
        }
    }

    private void maskIndex(final List<String> cmd, final int indexToMask) {
        if (indexToMask > -1) {
            final String cmdToMask = cmd.get(indexToMask);
            final String[] maskedArray = new String[cmdToMask.length()];
            Arrays.fill(maskedArray, "*");
            cmd.set(indexToMask, StringUtils.join(maskedArray));
        }
    }

    public IntLogger getLogger() {
        return logger;
    }

    public List<String> getCmd() {
        return cmd;
    }

    public File getLogDirectory() {
        return logDirectory;
    }

}
