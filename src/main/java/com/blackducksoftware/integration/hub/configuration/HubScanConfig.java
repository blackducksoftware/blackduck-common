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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.blackducksoftware.integration.hub.cli.SignatureScanConfig;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.util.Stringable;

public class HubScanConfig extends Stringable {
    private final CommonScanConfig commonScanConfig;
    private final boolean cleanupLogsOnSuccess;
    private final Map<String, String> targetToCodeLocationName;
    private final Map<String, Set<String>> targetToExclusionPatterns;
    private final Set<String> scanTargetPaths;

    public HubScanConfig(final CommonScanConfig commonScanConfig, final Set<String> scanTargetPaths, final boolean cleanupLogsOnSuccess,
            final Map<String, Set<String>> targetToExclusionPatterns,
            final Map<String, String> targetToCodeLocationName) {
        this.commonScanConfig = commonScanConfig;
        this.scanTargetPaths = scanTargetPaths;
        this.cleanupLogsOnSuccess = cleanupLogsOnSuccess;
        this.targetToExclusionPatterns = targetToExclusionPatterns;
        this.targetToCodeLocationName = targetToCodeLocationName;
    }

    public List<SignatureScanConfig> createSignatureScanConfigs() {
        final List<SignatureScanConfig> signatureScanConfigs = new ArrayList<>();
        for (final String scanTarget : scanTargetPaths) {
            String[] exclusionPatterns = new String[0];
            final Set<String> patterns = targetToExclusionPatterns.get(scanTarget);
            if (null != patterns && !patterns.isEmpty()) {
                exclusionPatterns = patterns.toArray(new String[patterns.size()]);
            }
            final SignatureScanConfig signatureScanConfig = new SignatureScanConfig(commonScanConfig, targetToCodeLocationName.get(scanTarget), exclusionPatterns, scanTarget);
            signatureScanConfigs.add(signatureScanConfig);
        }
        return signatureScanConfigs;
    }

    public CommonScanConfig getCommonScanConfig() {
        return commonScanConfig;
    }

    public Set<String> getScanTargetPaths() {
        return scanTargetPaths;
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

    public void print(final IntLogger logger) {
        try {
            logger.alwaysLog("--> Using Working Directory: " + commonScanConfig.getWorkingDirectory().getCanonicalPath());
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

        logger.alwaysLog("--> Scan Memory: " + commonScanConfig.getScanMemory());
        logger.alwaysLog("--> Dry Run: " + commonScanConfig.isDryRun());
        logger.alwaysLog("--> Clean-up logs on success: " + isCleanupLogsOnSuccess());
        logger.alwaysLog("--> Enable Snippet Mode: " + commonScanConfig.isSnippetModeEnabled());
        logger.alwaysLog("--> Additional Scan Arguments: " + commonScanConfig.getAdditionalScanArguments());
    }

}
