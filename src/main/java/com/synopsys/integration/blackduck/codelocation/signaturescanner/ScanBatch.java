/**
 * blackduck-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.blackduck.codelocation.signaturescanner;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.*;
import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.builder.Buildable;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.util.IntEnvironmentVariables;
import com.synopsys.integration.util.Stringable;

public class ScanBatch extends Stringable implements Buildable {
    public static ScanBatchBuilder newBuilder() {
        return new ScanBatchBuilder();
    }

    private final boolean cleanupOutput;
    private final URL blackDuckUrl;

    private final BlackDuckOnlineProperties blackDuckOnlineProperties;
    private final List<ScanTarget> scanTargets;

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
    private final String projectVersionName;
    private final IndividualFileMatching individualFileMatching;
    private final boolean debug;
    private final boolean verbose;

    public ScanBatch(final File signatureScannerInstallDirectory, final File outputDirectory, final boolean cleanupOutput, final int scanMemoryInMegabytes, final boolean dryRun, final boolean debug, final boolean verbose,
            final String scanCliOpts, final String additionalScanArguments, final BlackDuckOnlineProperties blackDuckOnlineProperties, final IndividualFileMatching individualFileMatching, final URL blackDuckUrl, final String blackDuckUsername, final String blackDuckPassword, final String blackDuckApiToken,
            final ProxyInfo proxyInfo, final boolean runInsecure, final String projectName, final String projectVersionName, final List<ScanTarget> scanTargets) {
        this.signatureScannerInstallDirectory = signatureScannerInstallDirectory;
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
    }

    /**
     * The default install directory will be used if the batch does not already have an install directory.
     */
    public List<ScanCommand> createScanCommands(final File defaultInstallDirectory, final ScanPathsUtility scanPathsUtility, final IntEnvironmentVariables intEnvironmentVariables) throws BlackDuckIntegrationException {
        String scanCliOptsToUse = scanCliOpts;
        if (null != intEnvironmentVariables && StringUtils.isBlank(scanCliOptsToUse)) {
            final String scanCliOptsEnvironment = intEnvironmentVariables.getValue("SCAN_CLI_OPTS");
            if (StringUtils.isNotBlank(scanCliOptsEnvironment)) {
                scanCliOptsToUse = scanCliOptsEnvironment;
            }
        }
        final boolean commandDryRun = blackDuckUrl == null || dryRun;
        String commandScheme = null;
        String commandHost = null;
        int commandPort = 0;
        if (!commandDryRun) {
            commandScheme = blackDuckUrl.getProtocol();
            commandHost = blackDuckUrl.getHost();
            if (blackDuckUrl.getPort() > 0) {
                commandPort = blackDuckUrl.getPort();
            } else if (blackDuckUrl.getDefaultPort() > 0) {
                commandPort = blackDuckUrl.getDefaultPort();
            }
        }
        final List<ScanCommand> scanCommands = new ArrayList<>();
        for (final ScanTarget scanTarget : scanTargets) {
            addToScanCommands(defaultInstallDirectory, scanPathsUtility, scanCliOptsToUse, commandDryRun, blackDuckOnlineProperties, commandScheme, commandHost, commandPort, scanCommands, scanTarget);
        }

        return scanCommands;
    }

    private void addToScanCommands(File defaultInstallDirectory, ScanPathsUtility scanPathsUtility, String scanCliOptsToUse, boolean commandDryRun, BlackDuckOnlineProperties blackDuckOnlineProperties, String commandScheme, String commandHost, int commandPort, List<ScanCommand> scanCommands, ScanTarget scanTarget) throws BlackDuckIntegrationException {
        File commandOutputDirectory = scanTarget.determineCommandOutputDirectory(scanPathsUtility, outputDirectory);
        File installDirectoryForCommand = signatureScannerInstallDirectory;
        if (null == installDirectoryForCommand && null != defaultInstallDirectory) {
            installDirectoryForCommand = defaultInstallDirectory;
        }
        final ScanCommand scanCommand = new ScanCommand(installDirectoryForCommand, commandOutputDirectory, commandDryRun, proxyInfo, scanCliOptsToUse, scanMemoryInMegabytes, commandScheme, commandHost,
                blackDuckApiToken, blackDuckUsername, blackDuckPassword, commandPort, runInsecure, scanTarget.getCodeLocationName(), blackDuckOnlineProperties,
                individualFileMatching, scanTarget.getExclusionPatterns(), additionalScanArguments, scanTarget.getPath(), verbose, debug, projectName, projectVersionName);
        scanCommands.add(scanCommand);
    }

    public File getSignatureScannerInstallDirectory() {
        return signatureScannerInstallDirectory;
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

    public URL getBlackDuckUrl() {
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

}
