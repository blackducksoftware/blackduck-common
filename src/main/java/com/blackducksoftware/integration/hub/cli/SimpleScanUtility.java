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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.hub.configuration.HubScanConfig;
import com.blackducksoftware.integration.hub.configuration.HubServerConfig;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.exception.ScanFailedException;
import com.blackducksoftware.integration.hub.service.model.ScannerSplitStream;
import com.blackducksoftware.integration.hub.service.model.StreamRedirectThread;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.rest.proxy.ProxyInfo;
import com.blackducksoftware.integration.util.IntEnvironmentVariables;
import com.google.gson.Gson;

public class SimpleScanUtility {
    public static final int DEFAULT_MEMORY = 4096;

    private final Gson gson;
    private final IntLogger logger;
    private final HubServerConfig hubServerConfig;
    private final IntEnvironmentVariables intEnvironmentVariables;
    private final HubScanConfig hubScanConfig;
    private final String project;
    private final String version;
    private final List<String> cmd = new ArrayList<>();

    private File logDirectory;

    public SimpleScanUtility(final IntLogger logger, final Gson gson, final HubServerConfig hubServerConfig, final IntEnvironmentVariables intEnvironmentVariables, final HubScanConfig hubScanConfig,
            final String project, final String version) {
        this.gson = gson;
        this.logger = logger;
        this.hubServerConfig = hubServerConfig;
        this.intEnvironmentVariables = intEnvironmentVariables;
        this.hubScanConfig = hubScanConfig;
        this.project = project;
        this.version = version;
    }

    public void setupAndExecuteScan() throws IllegalArgumentException, EncryptionException, InterruptedException, HubIntegrationException {
        final CLILocation cliLocation = new CLILocation(logger, hubScanConfig.getToolsDir());
        setupAndExecuteScan(cliLocation);
    }

    /**
     * This will setup the command-line invocation of the Hub scanner. The workingDirectoryPath is the parent folder of the scan logs and other scan artifacts.
     * @throws EncryptionException
     * @throws IllegalArgumentException
     * @throws HubIntegrationException
     * @throws ScanFailedException
     */
    public void setupAndExecuteScan(final CLILocation cliLocation) throws IllegalArgumentException, EncryptionException, InterruptedException, HubIntegrationException {
        final String pathToJavaExecutable;
        final String pathToOneJar;
        final String pathToScanExecutable;
        try {
            pathToJavaExecutable = cliLocation.getProvidedJavaExec().getCanonicalPath();
            pathToOneJar = cliLocation.getOneJarFile().getCanonicalPath();
            pathToScanExecutable = cliLocation.getCLI(logger).getCanonicalPath();
        } catch (final IOException e) {
            throw new HubIntegrationException(String.format("The provided directory %s did not have a Hub CLI.", hubScanConfig.getToolsDir().getAbsolutePath()), e);
        }
        logger.debug("Using this java installation : " + pathToJavaExecutable);

        cmd.add(pathToJavaExecutable);
        cmd.add("-Done-jar.silent=true");
        cmd.add("-Done-jar.jar.path=" + pathToOneJar);

        if (hubServerConfig.shouldUseProxyForHub() && !hubScanConfig.isDryRun()) {
            final ProxyInfo hubProxyInfo = hubServerConfig.getProxyInfo();
            final String proxyHost = hubProxyInfo.getHost();
            final int proxyPort = hubProxyInfo.getPort();
            final String proxyUsername = hubProxyInfo.getUsername();
            final String proxyPassword = hubProxyInfo.getDecryptedPassword();
            final String proxyNtlmDomain = hubProxyInfo.getNtlmDomain();
            final String proxyNtlmWorkstation = hubProxyInfo.getNtlmWorkstation();
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
            if (StringUtils.isNotBlank(proxyNtlmDomain)) {
                cmd.add("-Dhttp.auth.ntlm.domain=" + proxyNtlmDomain);
            }
            if (StringUtils.isNotBlank(proxyNtlmWorkstation)) {
                cmd.add("-Dblackduck.http.auth.ntlm.workstation=" + proxyNtlmWorkstation);
            }
        }
        final String scanCliOpts = intEnvironmentVariables.getValue("SCAN_CLI_OPTS");
        if (StringUtils.isNotBlank(scanCliOpts)) {
            for (String scanOpt : scanCliOpts.split(" ")) {
                if (StringUtils.isNotBlank(scanOpt)) {
                    cmd.add(scanOpt);
                }
            }
        }
        cmd.add("-Xmx" + hubScanConfig.getScanMemory() + "m");
        cmd.add("-jar");
        cmd.add(pathToScanExecutable);

        cmd.add("--no-prompt");

        if (!hubScanConfig.isDryRun()) {
            cmd.add("--scheme");
            cmd.add(hubServerConfig.getHubUrl().getProtocol());
            cmd.add("--host");
            cmd.add(hubServerConfig.getHubUrl().getHost());
            logger.debug("Using this Hub hostname : '" + hubServerConfig.getHubUrl().getHost() + "'");

            if (StringUtils.isEmpty(hubServerConfig.getApiToken())) {
                cmd.add("--username");
                cmd.add(hubServerConfig.getGlobalCredentials().getUsername());
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

            if (hubServerConfig.isAlwaysTrustServerCertificate()) {
                cmd.add("--insecure");
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

        // Only add the statusWriteDir option if the Hub supports the statusWriteDir option
        // The scanStatusDirectoryPath is the same as the log directory path
        // The CLI will create a subdirectory for the status files
        cmd.add("--statusWriteDir");
        cmd.add(logDirectoryPath);

        if (StringUtils.isNotBlank(project) && StringUtils.isNotBlank(version)) {
            cmd.add("--project");
            cmd.add(project);
            cmd.add("--release");
            cmd.add(version);
        }

        if (StringUtils.isNotBlank(hubScanConfig.getCodeLocationAlias())) {
            cmd.add("--name");
            cmd.add(hubScanConfig.getCodeLocationAlias());
        }

        if (hubScanConfig.isSnippetModeEnabled()) {
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

        if (StringUtils.isNotBlank(hubScanConfig.getAdditionalScanArguments())) {
            for (String additionalArgument : hubScanConfig.getAdditionalScanArguments().split(" ")) {
                if (StringUtils.isNotBlank(additionalArgument)) {
                    cmd.add(additionalArgument);
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
     * @throws IOException
     * @throws HubIntegrationException
     */
    private void executeScan() throws IllegalArgumentException, EncryptionException, IOException, InterruptedException, ScanFailedException {
        printCommand();

        final File standardOutFile = getStandardOutputFile();
        standardOutFile.createNewFile();
        try (FileOutputStream outputFileStream = new FileOutputStream(standardOutFile)) {
            final ScannerSplitStream splitOutputStream = new ScannerSplitStream(logger, outputFileStream);
            final ProcessBuilder processBuilder = new ProcessBuilder(cmd).redirectError(PIPE).redirectOutput(PIPE);
            processBuilder.environment().putAll(intEnvironmentVariables.getVariables());

            if (!hubScanConfig.isDryRun()) {
                if (!StringUtils.isEmpty(hubServerConfig.getApiToken())) {
                    processBuilder.environment().put("BD_HUB_TOKEN", hubServerConfig.getApiToken());
                } else {
                    processBuilder.environment().put("BD_HUB_PASSWORD", hubServerConfig.getGlobalCredentials().getDecryptedPassword());
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
        if (!bdIgnoreLogsFile.exists()) {
            if (!bdIgnoreLogsFile.createNewFile()) {
                throw new IOException(String.format("Could not create the %s file!", bdIgnoreLogsFile.getAbsolutePath()));
            }
            final String exclusionPattern = "/" + logDirectoryName + "/";
            Files.write(bdIgnoreLogsFile.toPath(), exclusionPattern.getBytes());
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

    public List<File> getScanSummaryFiles() {
        final File scanStatusDirectory = getStatusDirectory();
        if (null != scanStatusDirectory) {
            return Arrays.asList(scanStatusDirectory.listFiles((FilenameFilter) (dir, name) -> FilenameUtils.wildcardMatchOnSystem(name, "*.json")));
        }
        return null;
    }

    public List<File> getDryRunFiles() {
        final File dataDirectory = getDataDirectory();
        if (null != dataDirectory) {
            return Arrays.asList(dataDirectory.listFiles((FilenameFilter) (dir, name) -> FilenameUtils.wildcardMatchOnSystem(name, "*.json")));
        }
        return null;
    }

}
