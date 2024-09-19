/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.codelocation.signaturescanner.command;

import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.log.IntLogger;
import com.blackduck.integration.rest.proxy.ProxyInfo;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;


public class ScanCommand {
    private final String scheme;
    private final String host;
    private final int port;

    private final String targetPath;
    private final String name;
    private final Set<String> excludePatterns;
    private final BlackDuckOnlineProperties blackDuckOnlineProperties;

    private final String blackDuckUsername;
    private final String blackDuckPassword;
    private final String blackDuckApiToken;
    private final File signatureScannerInstallDirectory;
    private final File outputDirectory;
    private final int scanMemoryInMegabytes;
    private final String scanCliOpts;
    private final String additionalScanArguments;
    private final boolean runInsecure;
    private final boolean dryRun;
    private final ProxyInfo proxyInfo;
    private final String projectName;
    private final String versionName;
    private final IndividualFileMatching individualFileMatching;
    private final boolean debug;
    private final boolean verbose;
    private final boolean isRapid;
    private final ReducedPersistence reducedPersistence;
    @Nullable
    private final String correlationId;
    private final String bomCompareMode;
    private final boolean csvArchive;

    private List<String> command;

    private Map<String, List<Integer>> commandKeysToKeyRelatedIndices;

    public ScanCommand(File signatureScannerInstallDirectory, File outputDirectory, boolean dryRun, ProxyInfo proxyInfo, String scanCliOpts, int scanMemoryInMegabytes, String scheme, String host, String blackDuckApiToken,
        String blackDuckUsername, String blackDuckPassword, int port, boolean runInsecure, String name, BlackDuckOnlineProperties blackDuckOnlineProperties, IndividualFileMatching individualFileMatching, Set<String> excludePatterns,
        String additionalScanArguments, String targetPath, boolean verbose, boolean debug, String projectName, String versionName, boolean isRapid,
        ReducedPersistence reducedPersistence,
        @Nullable String correlationId, String bomCompareMode, boolean csvArchive) {
        this.signatureScannerInstallDirectory = signatureScannerInstallDirectory;
        this.outputDirectory = outputDirectory;
        this.dryRun = dryRun;
        this.proxyInfo = proxyInfo;
        this.scanCliOpts = scanCliOpts;
        this.scanMemoryInMegabytes = scanMemoryInMegabytes;
        this.scheme = scheme;
        this.host = host;
        this.blackDuckApiToken = blackDuckApiToken;
        this.blackDuckUsername = blackDuckUsername;
        this.blackDuckPassword = blackDuckPassword;
        this.port = port;
        this.runInsecure = runInsecure;
        this.name = name;
        this.blackDuckOnlineProperties = blackDuckOnlineProperties;
        this.individualFileMatching = individualFileMatching;
        this.excludePatterns = excludePatterns;
        this.additionalScanArguments = additionalScanArguments;
        this.targetPath = targetPath;
        this.verbose = verbose;
        this.debug = debug;
        this.projectName = projectName;
        this.versionName = versionName;
        this.isRapid = isRapid;
        this.reducedPersistence = reducedPersistence;
        this.correlationId = correlationId;
        this.bomCompareMode = bomCompareMode;
        this.csvArchive = csvArchive;
    }

    public List<String> createCommandForProcessBuilder(IntLogger logger, ScanPaths scannerPaths, String specificRunOutputDirectoryPath) throws IllegalArgumentException, IntegrationException {
        command = new ArrayList<>();
        commandKeysToKeyRelatedIndices = new HashMap<>();

        logger.debug("Using this java installation : " + scannerPaths.getPathToJavaExecutable());

        scannerPaths.addJavaAndOnePathArguments(command);

        if (proxyInfo.shouldUseProxy()) {
            populateProxyDetails();
        }

        populateScanCliOpts();

        appendSingleArgument("-Xmx" + scanMemoryInMegabytes + "m");
        scannerPaths.addScanExecutableArguments(command);

        appendSingleArgument("--no-prompt");

        if (!dryRun) {
            populateOnlineProperties(logger);
        } else {
            populateOfflineProperties(logger, specificRunOutputDirectoryPath);
        }

        if (verbose) {
            appendSingleArgument("-v");
        }
        if (debug) {
            appendSingleArgument("--debug");
        }
        
        if (csvArchive) {
            appendKeyValuePair("--outputFormat", "csv");
        }

        appendKeyValuePair("--logDir", specificRunOutputDirectoryPath);

        // Only add the statusWriteDir option if Black Duck supports the statusWriteDir option
        // The scanStatusDirectoryPath is the same as the log directory path
        // The CLI will create a subdirectory for the status files
        appendKeyValuePair("--statusWriteDir", specificRunOutputDirectoryPath);

        populateProjectAndVersion();

        if (StringUtils.isNotBlank(name)) {
            appendKeyValuePair("--name", name);
        }

        populateExcludePatterns();

        if (null != individualFileMatching) {
            appendKeyValuePair("--individualFileMatching", individualFileMatching.toString());
        }

        if (isRapid) {
            appendSingleArgument("--no-persistence");
            
            // --no-persistence-mode should never be used without --no-persistence so
            // only set it in this block.
            appendKeyValuePair("--no-persistence-mode", bomCompareMode);
        }

        populateReducedPersistence();

        if (StringUtils.isNotBlank(correlationId)) {
            appendKeyValuePair("--correlationId", correlationId);
        }

        ScanCommandArgumentParser parser = new ScanCommandArgumentParser();
        populateAdditionalScanArgumentsAndRemoveOverridden(parser);

        return command;
    }

    private void appendSingleArgument(String arg) {
        command.add(arg);
    }

    private void appendKeyValuePair(String key, String value) {
        if (!commandKeysToKeyRelatedIndices.containsKey(key)) {
            commandKeysToKeyRelatedIndices.put(key, new ArrayList<>());
        }
        List<Integer> indecesList = commandKeysToKeyRelatedIndices.get(key);

        indecesList.add(command.size());     // track the index of the key
        indecesList.add(command.size() + 1); // track the index of the value

        appendSingleArgument(key);
        appendSingleArgument(value);
    }

    private void populateReducedPersistence() {
        if (reducedPersistence != null) {
            if (reducedPersistence.equals(ReducedPersistence.DISCARD_UNMATCHED)) {
                appendSingleArgument("--discard-unmatched-files");
            }
            if (reducedPersistence.equals(ReducedPersistence.RETAIN_UNMATCHED)) {
                appendSingleArgument("--retain-unmatched-files");
            }
        }
    }

    private void populateAdditionalScanArgumentsAndRemoveOverridden(ScanCommandArgumentParser parser) throws IntegrationException {
        Set<Integer> overriddenArgIndices = new HashSet<>();

        List<String> arguments = parser.parse(additionalScanArguments);

        for (String argument : arguments) {
            if (StringUtils.isNotBlank(argument)) {
                if (commandKeysToKeyRelatedIndices.containsKey(argument)) {
                    overriddenArgIndices.addAll(commandKeysToKeyRelatedIndices.get(argument));
                }
                appendSingleArgument(argument);
            }
        }

        if (!overriddenArgIndices.isEmpty()) {
            List<String> updatedCommand = new ArrayList<>();
            for (int i = 0; i < command.size(); i++) {
                if (!overriddenArgIndices.contains(i)) {
                    updatedCommand.add(command.get(i));
                }
            }
            command = updatedCommand;
        }
    }

    private void populateExcludePatterns() {
        if (excludePatterns != null) {
            for (String exclusionPattern : excludePatterns) {
                if (StringUtils.isNotBlank(exclusionPattern)) {
                    appendKeyValuePair("--exclude", exclusionPattern);
                }
            }
        }
    }

    private void populateProjectAndVersion() {
        if (StringUtils.isNotBlank(projectName) && StringUtils.isNotBlank(versionName)) {
            appendKeyValuePair("--project", projectName);
            appendKeyValuePair("--release", versionName);
        }
    }

    private void populateOfflineProperties(IntLogger logger, String specificRunOutputDirectoryPath) {
        logger.info("You have configured this signature scan to run in dry run mode - no results will be submitted to Black Duck.");
        blackDuckOnlineProperties.warnIfOnlineIsNeeded(logger::warn);

        // The dryRunWriteDir is the same as the log directory path
        // The CLI will create a subdirectory for the json files
        appendKeyValuePair("--dryRunWriteDir", specificRunOutputDirectoryPath);
    }

    private void populateOnlineProperties(IntLogger logger) {
        appendKeyValuePair("--scheme", scheme);
        appendKeyValuePair("--host", host);

        logger.debug("Using the Black Duck hostname : '" + host + "'");

        if (StringUtils.isEmpty(blackDuckApiToken)) {
            appendKeyValuePair("--username", blackDuckUsername);
        }

        int blackDuckPort = port;
        if (blackDuckPort > 0) {
            appendKeyValuePair("--port", Integer.toString(blackDuckPort));
        } else {
            logger.warn("Could not find a port to use for the Server.");
        }

        if (runInsecure) {
            appendSingleArgument("--insecure");
        }

        blackDuckOnlineProperties.addOnlineCommands(command);
    }

    private void populateScanCliOpts() {
        if (StringUtils.isNotBlank(scanCliOpts)) {
            for (String scanOpt : scanCliOpts.split(" ")) {
                if (StringUtils.isNotBlank(scanOpt)) {
                    appendSingleArgument(scanOpt);
                }
            }
        }
    }

    private void populateProxyDetails() {
        ProxyInfo blackDuckProxyInfo = proxyInfo;
        String proxyHost = blackDuckProxyInfo.getHost().orElse(null);
        int proxyPort = blackDuckProxyInfo.getPort();
        String proxyUsername = blackDuckProxyInfo.getUsername().orElse(null);
        String proxyPassword = blackDuckProxyInfo.getPassword().orElse(null);
        String proxyNtlmDomain = blackDuckProxyInfo.getNtlmDomain().orElse(null);
        String proxyNtlmWorkstation = blackDuckProxyInfo.getNtlmWorkstation().orElse(null);

        appendSingleArgument("-Dhttp.proxyHost=" + proxyHost);

        appendSingleArgument("-Dhttp.proxyPort=" + Integer.toString(proxyPort));

        if (StringUtils.isNotBlank(proxyUsername) && StringUtils.isNotBlank(proxyPassword)) {
            appendSingleArgument("-Dhttp.proxyUser=" + proxyUsername);
            appendSingleArgument("-Dhttp.proxyPassword=" + proxyPassword);
        } else {
            // CLI will ignore the proxy host and port if there are no credentials
            appendSingleArgument("-Dhttp.proxyUser=user");
            appendSingleArgument("-Dhttp.proxyPassword=password");
        }
        if (StringUtils.isNotBlank(proxyNtlmDomain)) {
            appendSingleArgument("-Dhttp.auth.ntlm.domain=" + proxyNtlmDomain);
        }
        if (StringUtils.isNotBlank(proxyNtlmWorkstation)) {
            appendSingleArgument("-Dblackduck.http.auth.ntlm.workstation=" + proxyNtlmWorkstation);
        }
    }

    public File getSignatureScannerInstallDirectory() {
        return signatureScannerInstallDirectory;
    }

    public File getOutputDirectory() {
        return outputDirectory;
    }

    public boolean isDryRun() {
        return dryRun;
    }

    public ProxyInfo getProxyInfo() {
        return proxyInfo;
    }

    public String getScanCliOpts() {
        return scanCliOpts;
    }

    public int getScanMemoryInMegabytes() {
        return scanMemoryInMegabytes;
    }

    public String getScheme() {
        return scheme;
    }

    public String getHost() {
        return host;
    }

    public String getBlackDuckApiToken() {
        return blackDuckApiToken;
    }

    public String getBlackDuckUsername() {
        return blackDuckUsername;
    }

    public String getBlackDuckPassword() {
        return blackDuckPassword;
    }

    public int getPort() {
        return port;
    }

    public boolean isRunInsecure() {
        return runInsecure;
    }

    public String getName() {
        return name;
    }

    public boolean isSnippetMatching() {
        return blackDuckOnlineProperties.isSnippetMatching();
    }

    public boolean isSnippetMatchingOnly() {
        return blackDuckOnlineProperties.isSnippetMatchingOnly();
    }

    public boolean isUploadSource() {
        return blackDuckOnlineProperties.isUploadSource();
    }

    public boolean isLicenseSearch() {
        return blackDuckOnlineProperties.isLicenseSearch();
    }

    public boolean isCopyrightSearch() {
        return blackDuckOnlineProperties.isCopyrightSearch();
    }

    public IndividualFileMatching getIndividualFileMatching() {
        return individualFileMatching;
    }

    public Set<String> getExcludePatterns() {
        return excludePatterns;
    }

    public String getAdditionalScanArguments() {
        return additionalScanArguments;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public boolean isDebug() {
        return debug;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getVersionName() {
        return versionName;
    }

    public boolean isRapid() {
        return isRapid;
    }
    
    public ReducedPersistence getReducedPersistence() {
        return reducedPersistence;
    }

}
