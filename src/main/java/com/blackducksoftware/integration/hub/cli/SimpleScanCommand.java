package com.blackducksoftware.integration.hub.cli;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.rest.proxy.ProxyInfo;

public class SimpleScanCommand {
    private final SimpleScanData simpleScanData;
    private final List<String> cmd = new ArrayList<>();

    public SimpleScanCommand(final IntLogger logger, final SignatureScannerPaths scannerPaths, final SimpleScanData simpleScanData, final String specificRunOutputDirectoryPath) throws HubIntegrationException, EncryptionException {
        this.simpleScanData = simpleScanData;
        setupScanCommand(logger, scannerPaths, specificRunOutputDirectoryPath);
    }

    public List<String> getCmd() {
        return cmd;
    }

    /**
     * Code to mask passwords in the logs
     */
    public void printCommand(final IntLogger logger) {
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

    private void setupScanCommand(final IntLogger logger, final SignatureScannerPaths scannerPaths, final String specificRunOutputDirectoryPath) throws IllegalArgumentException, EncryptionException, HubIntegrationException {
        logger.debug("Using this java installation : " + scannerPaths.getPathToJavaExecutable());

        cmd.add(scannerPaths.getPathToJavaExecutable());
        cmd.add("-Done-jar.silent=true");
        cmd.add("-Done-jar.jar.path=" + scannerPaths.getPathToOneJar());

        if (simpleScanData.shouldUseProxy()) {
            final ProxyInfo hubProxyInfo = simpleScanData.getProxyInfo();
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
        final String scanCliOpts = simpleScanData.getScanCliOpts();
        if (StringUtils.isNotBlank(scanCliOpts)) {
            for (final String scanOpt : scanCliOpts.split(" ")) {
                if (StringUtils.isNotBlank(scanOpt)) {
                    cmd.add(scanOpt);
                }
            }
        }
        cmd.add("-Xmx" + simpleScanData.getScanMemoryInMegabytes() + "m");
        cmd.add("-jar");
        cmd.add(scannerPaths.getPathToScanExecutable());

        cmd.add("--no-prompt");

        if (!simpleScanData.isDryRun()) {
            cmd.add("--scheme");
            cmd.add(simpleScanData.getScheme());
            cmd.add("--host");
            cmd.add(simpleScanData.getHost());
            logger.debug("Using this Hub hostname : '" + simpleScanData.getHost() + "'");

            if (StringUtils.isEmpty(simpleScanData.getApiToken())) {
                cmd.add("--username");
                cmd.add(simpleScanData.getUsername());
            }

            final int hubPort = simpleScanData.getPort();
            if (hubPort > 0) {
                cmd.add("--port");
                cmd.add(Integer.toString(hubPort));
            } else {
                logger.warn("Could not find a port to use for the Server.");
            }

            if (simpleScanData.isRunInsecure()) {
                cmd.add("--insecure");
            }
        }

        if (simpleScanData.isVerbose()) {
            cmd.add("-v");
        }
        if (simpleScanData.isDebug()) {
            cmd.add("--debug");
        }

        cmd.add("--logDir");
        cmd.add(specificRunOutputDirectoryPath);

        if (simpleScanData.isDryRun()) {
            // The dryRunWriteDir is the same as the log directory path
            // The CLI will create a subdirectory for the json files
            cmd.add("--dryRunWriteDir");
            cmd.add(specificRunOutputDirectoryPath);
        }

        // Only add the statusWriteDir option if the Hub supports the statusWriteDir option
        // The scanStatusDirectoryPath is the same as the log directory path
        // The CLI will create a subdirectory for the status files
        cmd.add("--statusWriteDir");
        cmd.add(specificRunOutputDirectoryPath);

        if (StringUtils.isNotBlank(simpleScanData.getProjectName()) && StringUtils.isNotBlank(simpleScanData.getVersionName())) {
            cmd.add("--project");
            cmd.add(simpleScanData.getProjectName());
            cmd.add("--release");
            cmd.add(simpleScanData.getVersionName());
        }

        if (StringUtils.isNotBlank(simpleScanData.getName())) {
            cmd.add("--name");
            cmd.add(simpleScanData.getName());
        }

        if (simpleScanData.isSnippetMatching()) {
            cmd.add("--snippet-matching");
        }

        if (simpleScanData.getExcludePatterns() != null) {
            for (final String exclusionPattern : simpleScanData.getExcludePatterns()) {
                if (StringUtils.isNotBlank(exclusionPattern)) {
                    cmd.add("--exclude");
                    cmd.add(exclusionPattern);
                }
            }
        }
        final String additionalScanArguments = simpleScanData.getAdditionalArguments();
        if (StringUtils.isNotBlank(additionalScanArguments)) {
            for (final String additionalArgument : additionalScanArguments.split(" ")) {
                if (StringUtils.isNotBlank(additionalArgument)) {
                    cmd.add(additionalArgument);
                }
            }
        }

        cmd.add(simpleScanData.getTargetPath());
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
