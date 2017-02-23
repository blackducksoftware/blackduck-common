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
package com.blackducksoftware.integration.hub.scan;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import com.blackducksoftware.integration.log.IntLogger;

public class HubScanConfig {
    private final String projectName;

    private final String version;

    private final File workingDirectory;

    private final int scanMemory;

    private final Set<String> scanTargetPaths;

    private final boolean dryRun;

    private final File toolsDir;

    private final boolean cleanupLogsOnSuccess;

    private final String[] excludePatterns;

    private final String codeLocationAlias;

    private final boolean unmapPreviousCodeLocations;

    private final boolean deletePreviousCodeLocations;

    public HubScanConfig(final String projectName, final String version, final File workingDirectory,
            final int scanMemory,
            final Set<String> scanTargetPaths, final boolean dryRun, final File toolsDir,
            final boolean cleanupLogsOnSuccess, final String[] excludePatterns, final String codeLocationAlias, final boolean unmapPreviousCodeLocations,
            final boolean deletePreviousCodeLocations) {
        this.projectName = projectName;
        this.version = version;
        this.workingDirectory = workingDirectory;
        this.scanMemory = scanMemory;
        this.scanTargetPaths = scanTargetPaths;
        this.dryRun = dryRun;
        this.toolsDir = toolsDir;
        this.cleanupLogsOnSuccess = cleanupLogsOnSuccess;
        this.excludePatterns = excludePatterns;
        this.codeLocationAlias = codeLocationAlias;
        this.unmapPreviousCodeLocations = unmapPreviousCodeLocations;
        this.deletePreviousCodeLocations = deletePreviousCodeLocations;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getVersion() {
        return version;
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

    public String[] getExcludePatterns() {
        return excludePatterns;
    }

    public String getCodeLocationAlias() {
        return codeLocationAlias;
    }

    public boolean isUnmapPreviousCodeLocations() {
        return unmapPreviousCodeLocations;
    }

    public boolean isDeletePreviousCodeLocations() {
        return deletePreviousCodeLocations;
    }

    public void print(final IntLogger logger) {
        try {
            logger.alwaysLog("--> Using Working Directory : " + getWorkingDirectory().getCanonicalPath());
        } catch (final IOException e) {
            logger.alwaysLog("Extremely unlikely exception getting the canonical path: " + e.getMessage());
        }
        logger.alwaysLog(
                "--> Using Hub Project Name : " + getProjectName() + ", Version : " + getVersion());

        logger.alwaysLog("--> Scanning the following targets  : ");
        if (scanTargetPaths != null) {
            for (final String target : scanTargetPaths) {
                logger.alwaysLog("--> " + target);
            }
        } else {
            logger.alwaysLog("--> null");
        }
        logger.alwaysLog("--> Directory Exclusion Patterns  : ");
        if (excludePatterns != null) {
            for (final String exclusionPattern : excludePatterns) {
                logger.alwaysLog("--> " + exclusionPattern);
            }
        } else {
            logger.alwaysLog("--> null");
        }

        logger.alwaysLog("--> Scan Memory : " + getScanMemory());
        logger.alwaysLog("--> Dry Run : " + isDryRun());
        logger.alwaysLog("--> Clean-up logs on success : " + isCleanupLogsOnSuccess());
        logger.alwaysLog("--> Code Location Name : " + getCodeLocationAlias());
        logger.alwaysLog("--> Un-map previous Code Locations : " + isUnmapPreviousCodeLocations());
        logger.alwaysLog("--> Delete previous Code Locations : " + isDeletePreviousCodeLocations());
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, RecursiveToStringStyle.JSON_STYLE);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this, false);
    }

    @Override
    public boolean equals(final Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj, false);
    }

}
