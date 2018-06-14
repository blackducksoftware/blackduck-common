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
package com.blackducksoftware.integration.hub.configuration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.blackducksoftware.integration.hub.cli.SignatureScanConfig;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.util.Stringable;

public class HubScanConfig extends Stringable {
    private final String additionalScanArguments;
    private final boolean cleanupLogsOnSuccess;
    private final Map<String, String> targetToCodeLocationName;
    private final boolean debug;
    private final boolean dryRun;
    private final Map<String, Set<String>> targetToExclusionPatterns;
    private final int scanMemory;
    private final Set<String> scanTargetPaths;
    private final boolean snippetModeEnabled;
    private final File toolsDir;
    private final File workingDirectory;
    private final boolean verbose;

    public HubScanConfig(final File workingDirectory, final int scanMemory, final Set<String> scanTargetPaths, final boolean dryRun, final File toolsDir, final boolean cleanupLogsOnSuccess,
            final Map<String, Set<String>> targetToExclusionPatterns,
            final Map<String, String> targetToCodeLocationName, final boolean snippetModeEnabled, final String additionalScanParameters) {
        this(workingDirectory, scanMemory, scanTargetPaths, dryRun, toolsDir, cleanupLogsOnSuccess, targetToExclusionPatterns, targetToCodeLocationName, false, true, snippetModeEnabled,
                additionalScanParameters);
    }

    public HubScanConfig(final File workingDirectory, final int scanMemory, final Set<String> scanTargetPaths, final boolean dryRun, final File toolsDir, final boolean cleanupLogsOnSuccess,
            final Map<String, Set<String>> targetToExclusionPatterns,
            final Map<String, String> targetToCodeLocationName, final boolean debug, final boolean verbose, final boolean snippetModeEnabled,
            final String additionalScanArguments) {
        this.workingDirectory = workingDirectory;
        this.scanMemory = scanMemory;
        this.scanTargetPaths = scanTargetPaths;
        this.dryRun = dryRun;
        this.toolsDir = toolsDir;
        this.cleanupLogsOnSuccess = cleanupLogsOnSuccess;
        this.targetToExclusionPatterns = targetToExclusionPatterns;
        this.targetToCodeLocationName = targetToCodeLocationName;
        this.debug = debug;
        this.verbose = verbose;
        this.snippetModeEnabled = snippetModeEnabled;
        this.additionalScanArguments = additionalScanArguments;
    }

    public List<SignatureScanConfig> createSignatureScanConfigs() {
        final List<SignatureScanConfig> signatureScanConfigs = new ArrayList<>();
        for (final String scanTarget : scanTargetPaths) {
            String[] exclusionPatterns = new String[0];
            final Set<String> patterns = targetToExclusionPatterns.get(scanTarget);
            if (null != patterns && !patterns.isEmpty()) {
                exclusionPatterns = patterns.toArray(new String[patterns.size()]);
            }
            final SignatureScanConfig signatureScanConfig = new SignatureScanConfig(additionalScanArguments, targetToCodeLocationName.get(scanTarget), debug, dryRun, exclusionPatterns, scanMemory, scanTarget,
                    snippetModeEnabled, toolsDir, workingDirectory, verbose);
            signatureScanConfigs.add(signatureScanConfig);
        }
        return signatureScanConfigs;
    }

    public File getWorkingDirectory() {
        return workingDirectory;
    }

    public int getScanMemory() {
        return scanMemory;
    }

    public Set<String> getScanTargetPaths() {
        return scanTargetPaths;
    }

    public boolean isDryRun() {
        return dryRun;
    }

    public File getToolsDir() {
        return toolsDir;
    }

    public boolean isCleanupLogsOnSuccess() {
        return cleanupLogsOnSuccess;
    }

    public Map<String, Set<String>> getTargetToExclusionPatterns() {
        return targetToExclusionPatterns;
    }

    public Map<String, String> getTargetToCodeLocationName() {
        return targetToCodeLocationName;
    }

    public boolean isDebug() {
        return debug;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public boolean isSnippetModeEnabled() {
        return snippetModeEnabled;
    }

    public String getAdditionalScanArguments() {
        return additionalScanArguments;
    }

    public void print(final IntLogger logger) {
        try {
            logger.alwaysLog("--> Using Working Directory: " + getWorkingDirectory().getCanonicalPath());
        } catch (final IOException e) {
            logger.alwaysLog("Extremely unlikely exception getting the canonical path: " + e.getMessage());
        }
        logger.alwaysLog("--> Scanning the following targets:");
        if (scanTargetPaths != null) {
            for (final String target : scanTargetPaths) {
                final String codeLocationName = getTargetToCodeLocationName().get(target);
                logger.alwaysLog(String.format("--> Target: %s", target));
                logger.alwaysLog(String.format("    --> Code Location Name: %s", codeLocationName));
                final Set<String> excludePatterns = getTargetToExclusionPatterns().get(target);
                if (excludePatterns != null && !excludePatterns.isEmpty()) {
                    logger.alwaysLog("--> Directory Exclusion Patterns:");
                    for (final String exclusionPattern : excludePatterns) {
                        logger.alwaysLog(String.format("--> Exclusion Pattern: %s", exclusionPattern));
                    }
                }
            }
        } else {
            logger.alwaysLog("--> null");
        }

        logger.alwaysLog("--> Scan Memory: " + getScanMemory());
        logger.alwaysLog("--> Dry Run: " + isDryRun());
        logger.alwaysLog("--> Clean-up logs on success: " + isCleanupLogsOnSuccess());
        logger.alwaysLog("--> Enable Snippet Mode: " + isSnippetModeEnabled());
        logger.alwaysLog("--> Additional Scan Arguments: " + getAdditionalScanArguments());
    }

}
