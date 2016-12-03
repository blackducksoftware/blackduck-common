/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
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
 *******************************************************************************/
package com.blackducksoftware.integration.hub.scan;

import java.io.File;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.phone.home.enums.ThirdPartyName;
import com.google.common.collect.ImmutableList;

public class HubScanConfig {
    private final String projectName;

    private final String version;

    private final String phase;

    private final String distribution;

    private final File workingDirectory;

    private final boolean shouldGenerateRiskReport;

    private final int maxWaitTimeForBomUpdate;

    private final int scanMemory;

    private final ImmutableList<String> scanTargetPaths;

    private final boolean dryRun;

    private final File toolsDir;

    private final ThirdPartyName thirdPartyName;

    private final String thirdPartyVersion;

    private final String pluginVersion;

    public HubScanConfig(final String projectName, final String version, final String phase,
            final String distribution, final File workingDirectory, final int scanMemory,
            final boolean shouldGenerateRiskReport, final int maxWaitTimeForBomUpdate,
            final ImmutableList<String> scanTargetPaths, final boolean dryRun, final File toolsDir, final ThirdPartyName thirdPartyName,
            final String thirdPartyVersion,
            final String pluginVersion) {
        this.projectName = projectName;
        this.version = version;
        this.phase = phase;
        this.distribution = distribution;
        this.workingDirectory = workingDirectory;
        this.shouldGenerateRiskReport = shouldGenerateRiskReport;
        this.maxWaitTimeForBomUpdate = maxWaitTimeForBomUpdate;
        this.scanMemory = scanMemory;
        this.scanTargetPaths = scanTargetPaths;
        this.dryRun = dryRun;
        this.toolsDir = toolsDir;
        this.thirdPartyName = thirdPartyName;
        this.thirdPartyVersion = thirdPartyVersion;
        this.pluginVersion = pluginVersion;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getVersion() {
        return version;
    }

    public String getPhase() {
        return phase;
    }

    public String getDistribution() {
        return distribution;
    }

    public File getWorkingDirectory() {
        return workingDirectory;
    }

    public boolean isShouldGenerateRiskReport() {
        return shouldGenerateRiskReport;
    }

    public int getMaxWaitTimeForBomUpdate() {
        return maxWaitTimeForBomUpdate;
    }

    public long getMaxWaitTimeForBomUpdateInMilliseconds() {
        return maxWaitTimeForBomUpdate * 60 * 1000;
    }

    public int getScanMemory() {
        return scanMemory;
    }

    public List<String> getScanTargetPaths() {
        return scanTargetPaths;
    }

    public boolean isDryRun() {
        return dryRun;
    }

    public File getToolsDir() {
        return toolsDir;
    }

    public ThirdPartyName getThirdPartyName() {
        return thirdPartyName;
    }

    public String getThirdPartyVersion() {
        return thirdPartyVersion;
    }

    public String getPluginVersion() {
        return pluginVersion;
    }

    public void print(final IntLogger logger) {
        logger.alwaysLog("--> Using Working Directory : " + getWorkingDirectory().getAbsolutePath());
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
        logger.alwaysLog("--> Scan Memory : " + getScanMemory());
        logger.alwaysLog("--> Dry Run : " + isDryRun());

        logger.alwaysLog("--> Should Generate Report : " + isShouldGenerateRiskReport());
        logger.alwaysLog("--> Maximum wait time for Bom Update (s) : " + getMaxWaitTimeForBomUpdate());
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
