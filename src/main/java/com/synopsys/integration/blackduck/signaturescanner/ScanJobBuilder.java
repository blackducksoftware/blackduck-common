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
package com.synopsys.integration.blackduck.signaturescanner;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.blackduck.configuration.HubServerConfig;
import com.synopsys.integration.blackduck.signaturescanner.command.ScanTarget;
import com.synopsys.integration.blackduck.signaturescanner.command.SnippetMatching;
import com.synopsys.integration.rest.proxy.ProxyInfo;

public class ScanJobBuilder {
    public static final int DEFAULT_MEMORY_IN_MEGABYTES = 4096;
    public static final int MINIMUM_MEMORY_IN_MEGABYTES = 256;

    private File installDirectory;
    private File outputDirectory;
    private boolean cleanupOutput;

    private int scanMemoryInMegabytes = DEFAULT_MEMORY_IN_MEGABYTES;
    private boolean dryRun;
    private boolean debug;
    private boolean verbose = true;
    private String scanCliOpts;
    private String additionalScanArguments;

    private SnippetMatching snippetMatching;

    private URL blackDuckUrl;
    private String blackDuckUsername;
    private String blackDuckPassword;
    private String blackDuckApiToken;
    private boolean shouldUseProxy;
    private ProxyInfo proxyInfo;
    private boolean alwaysTrustServerCertificate;

    private String projectName;
    private String projectVersionName;

    private List<ScanTarget> scanTargets = new ArrayList<>();

    public ScanJob build() throws IllegalArgumentException {
        assertValid();

        return new ScanJob(installDirectory, outputDirectory, cleanupOutput, scanMemoryInMegabytes, dryRun, debug, verbose, scanCliOpts, additionalScanArguments, snippetMatching, blackDuckUrl, blackDuckUsername,
                blackDuckPassword, blackDuckApiToken, shouldUseProxy, proxyInfo, alwaysTrustServerCertificate, projectName, projectVersionName, scanTargets);
    }

    public void assertValid() throws IllegalArgumentException {
        final String errorMessage = createErrorMessage();
        if (!errorMessage.isEmpty()) {
            throw new IllegalStateException(errorMessage);
        }
    }

    public boolean isValid() {
        return createErrorMessage().isEmpty();
    }

    public String createErrorMessage() {
        final List<String> errorMessages = new ArrayList<>();

        if (scanTargets == null || scanTargets.size() < 1) {
            errorMessages.add("At least one target path must be provided.");
        } else {
            for (final ScanTarget scanTarget : scanTargets) {
                try {
                    new File(scanTarget.getPath()).getCanonicalPath();
                } catch (final IOException e) {
                    errorMessages.add(String.format("The target path: %s is not valid since its canonical path could not be determined: %s.", scanTarget.getPath(), e.getMessage()));
                }
                if (scanTarget.getExclusionPatterns() != null && scanTarget.getExclusionPatterns().size() > 0) {
                    for (final String exclusionPattern : scanTarget.getExclusionPatterns()) {
                        if (StringUtils.isNotBlank(exclusionPattern)) {
                            if (!exclusionPattern.startsWith("/") || !exclusionPattern.endsWith("/") || exclusionPattern.contains("**")) {
                                errorMessages.add("The exclusion pattern: " + exclusionPattern + " is not valid. An exclusion pattern must start and end with a forward slash (/) and may not contain double asterisks (**).");
                            }
                        }
                    }
                }
            }
        }

        if (blackDuckUrl != null) {
            if (StringUtils.isBlank(blackDuckApiToken) && (StringUtils.isBlank(blackDuckUsername) || StringUtils.isBlank(blackDuckPassword))) {
                errorMessages.add("Either an api token or a username and password is required.");
            }
            if (shouldUseProxy && proxyInfo == null) {
                errorMessages.add("If a proxy should be used, the details must be provided.");
            }
        }

        if (scanMemoryInMegabytes < MINIMUM_MEMORY_IN_MEGABYTES) {
            errorMessages.add(String.format("The minimum amount of memory for the scan is %d MB.", MINIMUM_MEMORY_IN_MEGABYTES));
        }

        if (!StringUtils.isAllBlank(projectName, projectVersionName) && (StringUtils.isBlank(projectName) || StringUtils.isBlank(projectVersionName))) {
            errorMessages.add("Both projectName and projectVersionName must be provided or omitted together");
        }

        return StringUtils.join(errorMessages, ' ');
    }

    public ScanJobBuilder fromHubServerConfig(final HubServerConfig hubServerConfig) {
        if (null == hubServerConfig) {
            shouldUseProxy = false;
            proxyInfo = ProxyInfo.NO_PROXY_INFO;
            blackDuckUrl = null;
            blackDuckUsername = null;
            blackDuckPassword = null;
            blackDuckApiToken = null;
            alwaysTrustServerCertificate = false;
        } else {
            shouldUseProxy = hubServerConfig.shouldUseProxyForHub();
            proxyInfo = hubServerConfig.getProxyInfo();
            blackDuckUrl = hubServerConfig.getBlackDuckUrl();
            if (hubServerConfig.usingApiToken()) {
                blackDuckApiToken = hubServerConfig.getApiToken();
            } else {
                blackDuckUsername = hubServerConfig.getCredentials().getUsername();
                blackDuckPassword = hubServerConfig.getCredentials().getPassword();
            }
            alwaysTrustServerCertificate = hubServerConfig.isAlwaysTrustServerCertificate();
        }
        return this;
    }

    public ScanJobBuilder addTarget(final ScanTarget scanTarget) {
        scanTargets.add(scanTarget);
        return this;
    }

    public ScanJobBuilder addTargets(final List<ScanTarget> scanTargets) {
        this.scanTargets.addAll(scanTargets);
        return this;
    }

    public ScanJobBuilder projectAndVersionNames(final String projectName, final String projectVersionName) {
        this.projectName = projectName;
        this.projectVersionName = projectVersionName;
        return this;
    }

    public File getInstallDirectory() {
        return installDirectory;
    }

    public ScanJobBuilder installDirectory(final File installDirectory) {
        this.installDirectory = installDirectory;
        return this;
    }

    public File getOutputDirectory() {
        return outputDirectory;
    }

    public ScanJobBuilder outputDirectory(final File outputDirectory) {
        this.outputDirectory = outputDirectory;
        return this;
    }

    public boolean isCleanupOutput() {
        return cleanupOutput;
    }

    public ScanJobBuilder cleanupOutput(final boolean cleanupOutput) {
        this.cleanupOutput = cleanupOutput;
        return this;
    }

    public int getScanMemoryInMegabytes() {
        return scanMemoryInMegabytes;
    }

    public ScanJobBuilder scanMemoryInMegabytes(final int scanMemoryInMegabytes) {
        this.scanMemoryInMegabytes = scanMemoryInMegabytes;
        return this;
    }

    public boolean isDryRun() {
        return dryRun;
    }

    public ScanJobBuilder dryRun(final boolean dryRun) {
        this.dryRun = dryRun;
        return this;
    }

    public boolean isDebug() {
        return debug;
    }

    public ScanJobBuilder debug(final boolean debug) {
        this.debug = debug;
        return this;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public ScanJobBuilder verbose(final boolean verbose) {
        this.verbose = verbose;
        return this;
    }

    public String getScanCliOpts() {
        return scanCliOpts;
    }

    public ScanJobBuilder scanCliOpts(final String scanCliOpts) {
        this.scanCliOpts = scanCliOpts;
        return this;
    }

    public String getAdditionalScanArguments() {
        return additionalScanArguments;
    }

    public ScanJobBuilder additionalScanArguments(final String additionalScanArguments) {
        this.additionalScanArguments = additionalScanArguments;
        return this;
    }

    public SnippetMatching getSnippetMatching() {
        return snippetMatching;
    }

    public ScanJobBuilder snippetMatching(final SnippetMatching snippetMatching) {
        this.snippetMatching = snippetMatching;
        return this;
    }

    public URL getBlackDuckUrl() {
        return blackDuckUrl;
    }

    public ScanJobBuilder blackDuckUrl(final URL blackDuckUrl) {
        this.blackDuckUrl = blackDuckUrl;
        return this;
    }

    public String getBlackDuckUsername() {
        return blackDuckUsername;
    }

    public ScanJobBuilder blackDuckUsername(final String blackDuckUsername) {
        this.blackDuckUsername = blackDuckUsername;
        return this;
    }

    public String getBlackDuckPassword() {
        return blackDuckPassword;
    }

    public ScanJobBuilder blackDuckPassword(final String blackDuckPassword) {
        this.blackDuckPassword = blackDuckPassword;
        return this;
    }

    public String getBlackDuckApiToken() {
        return blackDuckApiToken;
    }

    public ScanJobBuilder blackDuckApiToken(final String blackDuckApiToken) {
        this.blackDuckApiToken = blackDuckApiToken;
        return this;
    }

    public boolean isShouldUseProxy() {
        return shouldUseProxy;
    }

    public ScanJobBuilder shouldUseProxy(final boolean shouldUseProxy) {
        this.shouldUseProxy = shouldUseProxy;
        return this;
    }

    public ProxyInfo getProxyInfo() {
        return proxyInfo;
    }

    public ScanJobBuilder proxyInfo(final ProxyInfo proxyInfo) {
        this.proxyInfo = proxyInfo;
        return this;
    }

    public boolean isAlwaysTrustServerCertificate() {
        return alwaysTrustServerCertificate;
    }

    public ScanJobBuilder alwaysTrustServerCertificate(final boolean alwaysTrustServerCertificate) {
        this.alwaysTrustServerCertificate = alwaysTrustServerCertificate;
        return this;
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

    public ScanJobBuilder simpleScanTargets(final List<ScanTarget> scanTargets) {
        this.scanTargets = scanTargets;
        return this;
    }

}
