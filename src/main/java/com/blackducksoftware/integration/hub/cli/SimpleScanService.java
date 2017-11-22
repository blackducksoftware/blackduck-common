/**
 * Hub Common
 *
 * Copyright (C) 2017 Black Duck Software, Inc.
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
package com.blackducksoftware.integration.hub.cli;

import static java.lang.ProcessBuilder.Redirect.PIPE;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.HubSupportHelper;
import com.blackducksoftware.integration.hub.ScannerSplitStream;
import com.blackducksoftware.integration.hub.StreamRedirectThread;
import com.blackducksoftware.integration.hub.capability.HubCapabilitiesEnum;
import com.blackducksoftware.integration.hub.certificate.HubCertificateHandler;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.exception.ScanFailedException;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.model.view.ScanSummaryView;
import com.blackducksoftware.integration.hub.proxy.ProxyInfo;
import com.blackducksoftware.integration.hub.scan.HubScanConfig;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.util.CIEnvironmentVariables;
import com.google.gson.Gson;

public class SimpleScanService {
    public static final int DEFAULT_MEMORY = 4096;

    private final Gson gson;
    private final IntLogger logger;
    private final HubServerConfig hubServerConfig;
    private final HubSupportHelper hubSupportHelper;
    private final CIEnvironmentVariables ciEnvironmentVariables;
    private final HubScanConfig hubScanConfig;
    private final String project;
    private final String version;
    private final List<String> cmd = new ArrayList<>();

    private File logDirectory;

    public SimpleScanService(final IntLogger logger, final Gson gson, final HubServerConfig hubServerConfig, final HubSupportHelper hubSupportHelper, final CIEnvironmentVariables ciEnvironmentVariables, final HubScanConfig hubScanConfig,
            final String project, final String version) {
        this.gson = gson;
        this.logger = logger;
        this.hubServerConfig = hubServerConfig;
        this.hubSupportHelper = hubSupportHelper;
        this.ciEnvironmentVariables = ciEnvironmentVariables;
        this.hubScanConfig = hubScanConfig;
        this.project = project;
        this.version = version;
    }

    public void setupAndExecuteScan() throws IllegalArgumentException, EncryptionException, HubIntegrationException {
        final CLILocation cliLocation = new CLILocation(logger, hubScanConfig.getToolsDir());
        setupAndExecuteScan(cliLocation);
    }

    /**
     * This will setup the command-line invocation of the Hub scanner. The workingDirectoryPath is the parent folder of the scan logs and other scan artifacts.
     *
     * @throws EncryptionException
     * @throws IllegalArgumentException
     * @throws HubIntegrationException
     *
     * @throws ScanFailedException
     */
    public void setupAndExecuteScan(final CLILocation cliLocation) throws IllegalArgumentException, EncryptionException, HubIntegrationException {
        String pathToJavaExecutable;
        String pathToOneJar;
        String pathToScanExecutable;
        try {
            pathToJavaExecutable = cliLocation.getProvidedJavaExec().getCanonicalPath();
            pathToOneJar = cliLocation.getOneJarFile().getCanonicalPath();
            pathToScanExecutable = cliLocation.getCLI(logger).getCanonicalPath();
        } catch (final IOException e) {
            throw new HubIntegrationException(String.format("The provided directory %s did not have a Hub CLI.", hubScanConfig.getToolsDir().getAbsolutePath()), e);
        }
        logger.debug("Using this java installation : " + pathToJavaExecutable);

        if (hubServerConfig.isAlwaysTrustServerCertificate() && !hubScanConfig.isDryRun()) {
            try {
                final HubCertificateHandler hubCertificateHandler = new HubCertificateHandler(logger, cliLocation.getJavaHome());
                hubCertificateHandler.importHttpsCertificateForHubServer(hubServerConfig);
            } catch (IOException | IntegrationException e) {
                logger.error("Could not automatically import the certificate to the CLI: " + e.getMessage());
            }
        }

        cmd.add(pathToJavaExecutable);
        cmd.add("-Done-jar.silent=true");
        cmd.add("-Done-jar.jar.path=" + pathToOneJar);

        if (hubServerConfig.shouldUseProxyForHub() && !hubScanConfig.isDryRun()) {
            final ProxyInfo hubProxyInfo = hubServerConfig.getProxyInfo();
            final String proxyHost = hubProxyInfo.getHost();
            final int proxyPort = hubProxyInfo.getPort();
            final String proxyUsername = hubProxyInfo.getUsername();
            final String proxyPassword = hubProxyInfo.getDecryptedPassword();
            cmd.add("-Dhttp.proxyHost=" + proxyHost);
            cmd.add("-Dhttp.proxyPort=" + Integer.toString(proxyPort));
            if (StringUtils.isNotBlank(proxyUsername) && StringUtils.isNotBlank(proxyPassword)) {
                cmd.add("-Dhttp.proxyUser=" + proxyUsername);
                cmd.add("-Dhttp.proxyPassword=" + proxyPassword);
            } else {
                // CLI will ignore the proxy host and port if there are no credentials
                cmd.add("-Dhttp.proxyUser=user");
                cmd.add("-Dhttp.proxyPassword=password");
            }
        }

        cmd.add("-Xmx" + hubScanConfig.getScanMemory() + "m");
        cmd.add("-jar");
        cmd.add(pathToScanExecutable);

        if (hubSupportHelper.hasCapability(HubCapabilitiesEnum.CLI_INSECURE_OPTION)) {
            cmd.add("--no-prompt");
        }

        if (!hubScanConfig.isDryRun()) {
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
        }

        makeVerbose(cmd);

        final String logDirectoryPath;
        try {
            populateLogDirectory();
            logDirectoryPath = logDirectory.getCanonicalPath();
        } catch (final IOException e) {
            throw new HubIntegrationException("Exception creating the log directory for the cli scan: " + e.getMessage(), e);
        }
        cmd.add("--logDir");
        cmd.add(logDirectoryPath);

        if (hubScanConfig.isDryRun()) {
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

        if (hubSupportHelper.hasCapability(HubCapabilitiesEnum.CODE_LOCATION_ALIAS) && StringUtils.isNotBlank(hubScanConfig.getCodeLocationAlias())) {
            cmd.add("--name");
            cmd.add(hubScanConfig.getCodeLocationAlias());
        }

        if (hubSupportHelper.hasCapability(HubCapabilitiesEnum.CLI_SNIPPET_MODE) && hubScanConfig.isSnippetModeEnabled()) {
            cmd.add("--snippet-matching");
        }

        if (hubScanConfig.getExcludePatterns() != null) {
            for (final String exclusionPattern : hubScanConfig.getExcludePatterns()) {
                if (StringUtils.isNotBlank(exclusionPattern)) {
                    cmd.add("--exclude");
                    cmd.add(exclusionPattern);
                }
            }
        }

        for (final String target : hubScanConfig.getScanTargetPaths()) {
            cmd.add(target);
        }

        try {
            executeScan();
        } catch (final IOException e) {
            throw new HubIntegrationException("Exception executing the cli scan: " + e.getMessage(), e);
        }
    }

    /**
     * If running in an environment that handles process creation, this method should be overridden to construct a process to execute the scan in the environment-specific way.
     *
     * @throws IOException
     * @throws HubIntegrationException
     */
    private void executeScan() throws IllegalArgumentException, EncryptionException, IOException, HubIntegrationException {
        printCommand();

        final File standardOutFile = getStandardOutputFile();
        standardOutFile.createNewFile();
        try (FileOutputStream outputFileStream = new FileOutputStream(standardOutFile)) {
            final ScannerSplitStream splitOutputStream = new ScannerSplitStream(logger, outputFileStream);
            final ProcessBuilder processBuilder = new ProcessBuilder(cmd).redirectError(PIPE).redirectOutput(PIPE);

            if (!hubScanConfig.isDryRun()) {
                processBuilder.environment().put("BD_HUB_PASSWORD", hubServerConfig.getGlobalCredentials().getDecryptedPassword());
            }
            processBuilder.environment().put("BD_HUB_NO_PROMPT", "true");

            final String bdioEnvVar = ciEnvironmentVariables.getValue("BD_HUB_DECLARED_COMPONENTS");
            if (StringUtils.isNotBlank(bdioEnvVar)) {
                processBuilder.environment().put("BD_HUB_DECLARED_COMPONENTS", bdioEnvVar);
            }

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
            } catch (final InterruptedException e) {
                throw new HubIntegrationException("The thread waiting for the cli to complete was interrupted: " + e.getMessage(), e);
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
     * For all error cases, return an empty list. If all goes well, return a list of scan summary urls.
     */
    public List<ScanSummaryView> getScanSummaryItems() {
        if (logDirectory == null || !logDirectory.exists()) {
            return Collections.emptyList();
        }
        final File scanStatusDirectory = getStatusDirectory();
        if (!scanStatusDirectory.exists()) {
            return Collections.emptyList();
        }
        final File[] statusFiles = scanStatusDirectory.listFiles();

        if (statusFiles.length != hubScanConfig.getScanTargetPaths().size()) {
            logger.error(String.format("There were %d scans target paths and %d status files.", hubScanConfig.getScanTargetPaths().size(), statusFiles.length));
            return Collections.emptyList();
        }

        final List<ScanSummaryView> scanSummaryItems = new ArrayList<>();
        for (final File currentStatusFile : statusFiles) {
            String fileContent;
            try {
                fileContent = FileUtils.readFileToString(currentStatusFile, "UTF8");
            } catch (final IOException e) {
                logger.error(String.format("There was an exception reading the status file: %s", e.getMessage(), e));
                return Collections.emptyList();
            }
            final ScanSummaryView scanSummaryItem = gson.fromJson(fileContent, ScanSummaryView.class);
            scanSummaryItem.json = fileContent;
            scanSummaryItems.add(scanSummaryItem);
        }

        return scanSummaryItems;
    }

    /**
     * This method can be overridden to provide a more appropriate directory name for the logs of a specific scan execution.
     */
    public String getSpecificScanExecutionLogDirectory() {
        final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd_HH-mm-ss-SSS").withZoneUTC();
        final String timeString = DateTime.now().withZone(DateTimeZone.UTC).toString(dateTimeFormatter);
        return timeString;
    }

    private void populateLogDirectory() throws IOException {
        final String logDirectoryName = "HubScanLogs";
        final File logsDirectory = new File(hubScanConfig.getWorkingDirectory(), logDirectoryName);
        final String specificScanExecutionLogDirectory = getSpecificScanExecutionLogDirectory();

        logDirectory = new File(logsDirectory, specificScanExecutionLogDirectory);
        if (!logDirectory.exists() && !logDirectory.mkdirs()) {
            throw new IOException(String.format("Could not create the %s directory!", logDirectory.getAbsolutePath()));
        }
        final File bdIgnoreLogsFile = new File(hubScanConfig.getWorkingDirectory(), ".bdignore");
        if (bdIgnoreLogsFile.exists()) {
            bdIgnoreLogsFile.delete();
        }
        if (!bdIgnoreLogsFile.createNewFile()) {
            throw new IOException(String.format("Could not create the %s file!", bdIgnoreLogsFile.getAbsolutePath()));
        }
        final String exclusionPattern = "/" + logDirectoryName + "/";
        Files.write(bdIgnoreLogsFile.toPath(), exclusionPattern.getBytes());
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

    private void makeVerbose(final List<String> cmd) {
        if (hubScanConfig.isVerbose()) {
            cmd.add("-v");
        }
        if (hubScanConfig.isDebug()) {
            cmd.add("--debug");
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

    public File[] getScanSummaryFiles() {
        final File scanStatusDirectory = getStatusDirectory();
        return scanStatusDirectory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String name) {
                return FilenameUtils.wildcardMatchOnSystem(name, "*.json");
            }
        });
    }

    public File[] getDryRunFiles() {
        final File dataDirectory = getDataDirectory();
        return dataDirectory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String name) {
                return FilenameUtils.wildcardMatchOnSystem(name, "*.json");
            }
        });
    }

}
