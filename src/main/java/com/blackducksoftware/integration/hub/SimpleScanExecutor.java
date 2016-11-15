/*
 * Copyright (C) 2016 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.hub;

import static java.lang.ProcessBuilder.Redirect.PIPE;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.restlet.engine.io.IoUtils;

import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.hub.ScanExecutor.Result;
import com.blackducksoftware.integration.hub.capabilities.HubCapabilitiesEnum;
import com.blackducksoftware.integration.hub.cli.CLILocation;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.global.HubProxyInfo;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.util.CIEnvironmentVariables;

public class SimpleScanExecutor {
    private IntLogger logger;

    private HubServerConfig hubServerConfig;

    private HubSupportHelper hubSupportHelper;

    private CIEnvironmentVariables ciEnvironmentVariables;

    private CLILocation cliLocation;

    private int scanMemory = 4096;

    private boolean verboseRun;

    private boolean dryRun;

    private String project;

    private String version;

    private List<String> scanTargetPaths;

    private String workingDirectoryPath;

    /**
     * The workingDirectoryPath is the parent folder of the scan logs and other scan artifacts.
     */
    public SimpleScanExecutor(IntLogger logger, HubServerConfig hubServerConfig, HubSupportHelper hubSupportHelper,
            CIEnvironmentVariables ciEnvironmentVariables, CLILocation cliLocation, int scanMemory, boolean verboseRun, boolean dryRun, String project,
            String version, List<String> scanTargetPaths, String workingDirectoryPath) {
        this.logger = logger;
        this.hubServerConfig = hubServerConfig;
        this.hubSupportHelper = hubSupportHelper;
        this.ciEnvironmentVariables = ciEnvironmentVariables;
        this.cliLocation = cliLocation;
        this.scanMemory = scanMemory;
        this.verboseRun = verboseRun;
        this.dryRun = dryRun;
        this.project = project;
        this.version = version;
        this.scanTargetPaths = scanTargetPaths;
        this.workingDirectoryPath = workingDirectoryPath;
    }

    public Result setupAndRunScan()
            throws HubIntegrationException, IOException, IllegalArgumentException, InterruptedException, EncryptionException {
        String pathToJavaExecutable = cliLocation.getProvidedJavaExec().getAbsolutePath();
        String pathToOneJar = cliLocation.getOneJarFile().getAbsolutePath();
        String pathToScanExecutable = cliLocation.getCLI(logger).getAbsolutePath();
        logger.debug("Using this java installation : " + pathToJavaExecutable);

        final List<String> cmd = new ArrayList<>();
        cmd.add(pathToJavaExecutable);
        cmd.add("-Done-jar.silent=true");
        cmd.add("-Done-jar.jar.path=" + pathToOneJar);

        if (hubServerConfig.shouldUseProxyForHub()) {
            HubProxyInfo hubProxyInfo = hubServerConfig.getProxyInfo();
            String proxyHost = hubProxyInfo.getHost();
            int proxyPort = hubProxyInfo.getPort();
            String proxyUsername = hubProxyInfo.getUsername();
            String proxyPassword = hubProxyInfo.getDecryptedPassword();
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

        int hubPort = hubServerConfig.getHubUrl().getPort();
        if (hubPort > 0) {
            cmd.add("--port");
            cmd.add(Integer.toString(hubPort));
        } else {
            int defaultPort = hubServerConfig.getHubUrl().getDefaultPort();
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

        final File logDirectory = getLogDirectory();
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

        return executeScan(cmd, logDirectory);
    }

    public Result executeScan(List<String> cmd, File logDirectory) throws IOException, InterruptedException, IllegalArgumentException, EncryptionException {
        printCommand(cmd);

        final File standardOutFile = new File(logDirectory, "CLI_Output.txt");
        standardOutFile.createNewFile();
        try (FileOutputStream outputFileStream = new FileOutputStream(standardOutFile)) {
            ScannerSplitStream splitOutputStream = new ScannerSplitStream(logger, outputFileStream);
            final ProcessBuilder processBuilder = new ProcessBuilder(cmd).redirectError(PIPE).redirectOutput(PIPE);

            processBuilder.environment().put("BD_HUB_PASSWORD", hubServerConfig.getGlobalCredentials().getDecryptedPassword());

            final String bdioEnvVar = ciEnvironmentVariables.getValue("BD_HUB_DECLARED_COMPONENTS");
            if (StringUtils.isNotBlank(bdioEnvVar)) {
                processBuilder.environment().put("BD_HUB_DECLARED_COMPONENTS", bdioEnvVar);
            }

            Process hubCliProcess = processBuilder.start();

            // The cli logs go the error stream for some reason
            StreamRedirectThread redirectThread = new StreamRedirectThread(hubCliProcess.getErrorStream(), splitOutputStream);
            redirectThread.start();

            int returnCode = hubCliProcess.waitFor();

            // the join method on the redirect thread will wait until the thread is dead
            // the thread will die when it reaches the end of stream and the run method is finished
            redirectThread.join();

            splitOutputStream.flush();
            logger.info(IoUtils.toString((hubCliProcess.getInputStream())));

            String outputString = "";
            if (splitOutputStream.hasOutput()) {
                outputString = splitOutputStream.getOutput();
            }

            logger.info("Hub CLI return code : " + returnCode);
            logger.info("You can view the BlackDuck Scan CLI logs at : '" + logDirectory.getAbsolutePath() + "'");

            if (outputString.contains("Finished in") && outputString.contains("with status SUCCESS")) {
                return Result.SUCCESS;
            } else {
                return Result.FAILURE;
            }
        }
    }

    public String getSpecificScanExecutionLogDirectory() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyyMMddHHmmssSSSz");
        String timeString = DateTime.now().withZone(DateTimeZone.UTC).toString(dateTimeFormatter);
        return timeString;
    }

    private File getLogDirectory() throws IOException {
        final File logsDirectory = new File(workingDirectoryPath, "HubScanLogs");
        String specificScanExecutionLogDirectory = getSpecificScanExecutionLogDirectory();

        File logDirectory = new File(logsDirectory, specificScanExecutionLogDirectory);
        if (!logDirectory.exists() && !logDirectory.mkdirs()) {
            throw new IOException("Could not create the HubScanLogs directory!");
        }

        return logDirectory;
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
            String commandParameter = cmdToOutput.get(commandIndex);
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

    public HubServerConfig getHubServerConfig() {
        return hubServerConfig;
    }

    public HubSupportHelper getHubSupportHelper() {
        return hubSupportHelper;
    }

    public CIEnvironmentVariables getCiEnvironmentVariables() {
        return ciEnvironmentVariables;
    }

    public CLILocation getCliLocation() {
        return cliLocation;
    }

    public int getScanMemory() {
        return scanMemory;
    }

    public boolean isVerboseRun() {
        return verboseRun;
    }

    public boolean isDryRun() {
        return dryRun;
    }

    public String getProject() {
        return project;
    }

    public String getVersion() {
        return version;
    }

    public List<String> getScanTargetPaths() {
        return scanTargetPaths;
    }

    public String getWorkingDirectoryPath() {
        return workingDirectoryPath;
    }

}
