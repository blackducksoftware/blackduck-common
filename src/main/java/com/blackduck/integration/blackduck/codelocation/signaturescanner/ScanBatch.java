/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.codelocation.signaturescanner;

import com.blackduck.integration.blackduck.codelocation.signaturescanner.command.*;
import com.blackduck.integration.blackduck.exception.BlackDuckIntegrationException;
import com.blackduck.integration.builder.Buildable;
import com.blackduck.integration.rest.HttpUrl;
import com.blackduck.integration.rest.proxy.ProxyInfo;
import com.blackduck.integration.util.IntEnvironmentVariables;
import com.blackduck.integration.util.Stringable;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ScanBatch extends Stringable implements Buildable {
    public static ScanBatchBuilder newBuilder() {
        return new ScanBatchBuilder();
    }

    private final boolean cleanupOutput;
    private final HttpUrl blackDuckUrl;

    private final BlackDuckOnlineProperties blackDuckOnlineProperties;
    private final List<ScanTarget> scanTargets;

    private final String blackDuckUsername;
    private final String blackDuckPassword;
    private final String blackDuckApiToken;
    private final File outputDirectory;
    private final int scanMemoryInMegabytes;
    private final String scanCliOpts;
    private final String additionalScanArguments;
    private final boolean runInsecure;
    private final boolean dryRun;
    private final ProxyInfo proxyInfo;
    private final String projectName;
    private final String projectVersionName;
    private final IndividualFileMatching individualFileMatching;
    private final boolean debug;
    private final boolean verbose;
    private final boolean isRapid;
    private final ReducedPersistence reducedPersistence;
    @Nullable
    private final String correlationId;
    private final String bomCompareMode;
    private final boolean csvArchive;

    public ScanBatch(File outputDirectory, boolean cleanupOutput, int scanMemoryInMegabytes, boolean dryRun, boolean debug, boolean verbose,
        String scanCliOpts, String additionalScanArguments, BlackDuckOnlineProperties blackDuckOnlineProperties, IndividualFileMatching individualFileMatching, HttpUrl blackDuckUrl,
        String blackDuckUsername, String blackDuckPassword, String blackDuckApiToken, ProxyInfo proxyInfo, boolean runInsecure, String projectName, String projectVersionName,
        List<ScanTarget> scanTargets, boolean isRapid, ReducedPersistence reducedPersistence, @Nullable String correlationId, String bomCompareMode, boolean csvArchive) {
        this.outputDirectory = outputDirectory;
        this.cleanupOutput = cleanupOutput;
        this.scanMemoryInMegabytes = scanMemoryInMegabytes;
        this.dryRun = dryRun;
        this.debug = debug;
        this.verbose = verbose;
        this.scanCliOpts = scanCliOpts;
        this.additionalScanArguments = additionalScanArguments;
        this.blackDuckOnlineProperties = blackDuckOnlineProperties;
        this.individualFileMatching = individualFileMatching;
        this.blackDuckUrl = blackDuckUrl;
        this.blackDuckUsername = blackDuckUsername;
        this.blackDuckPassword = blackDuckPassword;
        this.blackDuckApiToken = blackDuckApiToken;
        this.proxyInfo = proxyInfo;
        this.runInsecure = runInsecure;
        this.projectName = projectName;
        this.projectVersionName = projectVersionName;
        this.scanTargets = scanTargets;
        this.isRapid = isRapid;
        this.reducedPersistence = reducedPersistence;
        this.correlationId = correlationId;
        this.bomCompareMode = bomCompareMode;
        this.csvArchive = csvArchive;
    }

    /**
     * The default install directory will be used if the batch does not already have an install directory.
     */
    public List<ScanCommand> createScanCommands(File signatureScannerInstallDirectory, ScanPathsUtility scanPathsUtility, IntEnvironmentVariables intEnvironmentVariables) throws BlackDuckIntegrationException {
        String scanCliOptsToUse = scanCliOpts;
        if (null != intEnvironmentVariables && StringUtils.isBlank(scanCliOptsToUse)) {
            String scanCliOptsEnvironment = intEnvironmentVariables.getValue("SCAN_CLI_OPTS");
            if (StringUtils.isNotBlank(scanCliOptsEnvironment)) {
                scanCliOptsToUse = scanCliOptsEnvironment;
            }
        }
        boolean commandDryRun = blackDuckUrl == null || dryRun;
        String commandScheme = null;
        String commandHost = null;
        int commandPort = 0;
        if (!commandDryRun) {
            URL url = blackDuckUrl.url();
            commandScheme = url.getProtocol();
            commandHost = url.getHost();
            if (url.getPort() > 0) {
                commandPort = url.getPort();
            } else if (url.getDefaultPort() > 0) {
                commandPort = url.getDefaultPort();
            }
        }
        List<ScanCommand> scanCommands = new ArrayList<>();
        for (ScanTarget scanTarget : scanTargets) {
            addToScanCommands(signatureScannerInstallDirectory, scanPathsUtility, scanCliOptsToUse, commandDryRun, blackDuckOnlineProperties, commandScheme, commandHost, commandPort, scanCommands, scanTarget);
        }

        return scanCommands;
    }

    private void addToScanCommands(File signatureScannerInstallDirectory, ScanPathsUtility scanPathsUtility, String scanCliOptsToUse, boolean commandDryRun, BlackDuckOnlineProperties blackDuckOnlineProperties,
        String commandScheme, String commandHost, int commandPort, List<ScanCommand> scanCommands, ScanTarget scanTarget) throws BlackDuckIntegrationException {
        File commandOutputDirectory = scanTarget.determineCommandOutputDirectory(scanPathsUtility, outputDirectory);
        ScanCommand scanCommand = new ScanCommand(signatureScannerInstallDirectory, commandOutputDirectory, commandDryRun, proxyInfo, scanCliOptsToUse, scanMemoryInMegabytes, commandScheme, commandHost,
            blackDuckApiToken, blackDuckUsername, blackDuckPassword, commandPort, runInsecure, scanTarget.getCodeLocationName(), blackDuckOnlineProperties,
            individualFileMatching, scanTarget.getExclusionPatterns(), additionalScanArguments, scanTarget.getPath(), verbose, debug, projectName, projectVersionName, isRapid, reducedPersistence, correlationId,
            bomCompareMode, csvArchive);
        scanCommands.add(scanCommand);
    }

    public File getOutputDirectory() {
        return outputDirectory;
    }

    public boolean isCleanupOutput() {
        return cleanupOutput;
    }

    public int getScanMemoryInMegabytes() {
        return scanMemoryInMegabytes;
    }

    public boolean isDryRun() {
        return dryRun;
    }

    public boolean isDebug() {
        return debug;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public String getScanCliOpts() {
        return scanCliOpts;
    }

    public String getAdditionalScanArguments() {
        return additionalScanArguments;
    }

    public SnippetMatching getSnippetMatchingMode() {
        return blackDuckOnlineProperties.getSnippetMatchingMode();
    }

    public boolean isUploadSource() {
        return blackDuckOnlineProperties.isUploadSource();
    }

    public boolean isLicenseSearch() {
        return blackDuckOnlineProperties.isLicenseSearch();
    }

    public IndividualFileMatching getIndividualFileMatching() {
        return individualFileMatching;
    }

    public HttpUrl getBlackDuckUrl() {
        return blackDuckUrl;
    }

    public String getBlackDuckUsername() {
        return blackDuckUsername;
    }

    public String getBlackDuckPassword() {
        return blackDuckPassword;
    }

    public String getBlackDuckApiToken() {
        return blackDuckApiToken;
    }

    public ProxyInfo getProxyInfo() {
        return proxyInfo;
    }

    public boolean isRunInsecure() {
        return runInsecure;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getProjectVersionName() {
        return projectVersionName;
    }

    public List<ScanTarget> getScanTargets() {
        return scanTargets;
    }

    public boolean isRapid() {
        return isRapid;
    }
    
    public ReducedPersistence getReducedPersistence() {
        return reducedPersistence;
    }

    @Nullable
    public String getCorrelationId() {
        return correlationId;
    }
    
    public boolean isCsvArchive() {
    	return csvArchive;
    }
}
