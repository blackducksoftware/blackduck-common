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

import com.blackducksoftware.integration.log.IntLogger;
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

    private final String thirdPartyVersion;

    private final String pluginVersion;

    public HubScanConfig(final String projectName, final String version, final String phase,
            final String distribution, final File workingDirectory, final int scanMemory,
            final boolean shouldGenerateRiskReport, final int maxWaitTimeForBomUpdate,
            final ImmutableList<String> scanTargetPaths, final boolean dryRun, final File toolsDir, final String thirdPartyVersion,
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
        final StringBuilder builder = new StringBuilder();
        builder.append("HubScanJobConfig [projectName=");
        builder.append(projectName);
        builder.append(", version=");
        builder.append(version);
        builder.append(", phase=");
        builder.append(phase);
        builder.append(", distribution=");
        builder.append(distribution);
        builder.append(", workingDirectory=");
        builder.append(workingDirectory);
        builder.append(", shouldGenerateRiskReport=");
        builder.append(shouldGenerateRiskReport);
        builder.append(", maxWaitTimeForBomUpdate=");
        builder.append(maxWaitTimeForBomUpdate);
        builder.append(", scanMemory=");
        builder.append(scanMemory);
        builder.append(", scanTargetPaths=");
        builder.append(scanTargetPaths);
        builder.append(", dryRun=");
        builder.append(dryRun);
        builder.append(", toolsDir=");
        builder.append(toolsDir);
        builder.append(", thirdPartyVersion=");
        builder.append(thirdPartyVersion);
        builder.append(", pluginVersion=");
        builder.append(pluginVersion);
        builder.append("]");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((distribution == null) ? 0 : distribution.hashCode());
        result = prime * result + (dryRun ? 1231 : 1237);
        result = prime * result + maxWaitTimeForBomUpdate;
        result = prime * result + ((phase == null) ? 0 : phase.hashCode());
        result = prime * result + ((projectName == null) ? 0 : projectName.hashCode());
        result = prime * result + scanMemory;
        result = prime * result + ((scanTargetPaths == null) ? 0 : scanTargetPaths.hashCode());
        result = prime * result + (shouldGenerateRiskReport ? 1231 : 1237);
        result = prime * result + ((version == null) ? 0 : version.hashCode());
        result = prime * result + ((workingDirectory == null) ? 0 : workingDirectory.hashCode());
        result = prime * result + ((toolsDir == null) ? 0 : toolsDir.hashCode());
        result = prime * result + ((thirdPartyVersion == null) ? 0 : thirdPartyVersion.hashCode());
        result = prime * result + ((pluginVersion == null) ? 0 : pluginVersion.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof HubScanConfig)) {
            return false;
        }
        final HubScanConfig other = (HubScanConfig) obj;
        if (distribution == null) {
            if (other.distribution != null) {
                return false;
            }
        } else if (!distribution.equals(other.distribution)) {
            return false;
        }
        if (dryRun != other.dryRun) {
            return false;
        }
        if (maxWaitTimeForBomUpdate != other.maxWaitTimeForBomUpdate) {
            return false;
        }
        if (phase == null) {
            if (other.phase != null) {
                return false;
            }
        } else if (!phase.equals(other.phase)) {
            return false;
        }
        if (projectName == null) {
            if (other.projectName != null) {
                return false;
            }
        } else if (!projectName.equals(other.projectName)) {
            return false;
        }
        if (scanMemory != other.scanMemory) {
            return false;
        }
        if (scanTargetPaths == null) {
            if (other.scanTargetPaths != null) {
                return false;
            }
        } else if (!scanTargetPaths.equals(other.scanTargetPaths)) {
            return false;
        }
        if (shouldGenerateRiskReport != other.shouldGenerateRiskReport) {
            return false;
        }
        if (version == null) {
            if (other.version != null) {
                return false;
            }
        } else if (!version.equals(other.version)) {
            return false;
        }
        if (workingDirectory == null) {
            if (other.workingDirectory != null) {
                return false;
            }
        } else if (!workingDirectory.equals(other.workingDirectory)) {
            return false;
        }
        if (toolsDir == null) {
            if (other.toolsDir != null) {
                return false;
            }
        } else if (!toolsDir.equals(other.toolsDir)) {
            return false;
        }
        if (thirdPartyVersion == null) {
            if (other.thirdPartyVersion != null) {
                return false;
            }
        } else if (!thirdPartyVersion.equals(other.thirdPartyVersion)) {
            return false;
        }
        if (pluginVersion == null) {
            if (other.pluginVersion != null) {
                return false;
            }
        } else if (!pluginVersion.equals(other.pluginVersion)) {
            return false;
        }
        return true;
    }

}
