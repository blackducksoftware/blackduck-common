package com.blackducksoftware.integration.hub;

import java.util.List;

import com.google.common.collect.ImmutableList;

public class HubScanJobConfig {
    private final String projectName;

    private final String version;

    private final String phase;

    private final String distribution;

    private final String workingDirectory;

    private final boolean shouldGenerateRiskReport;

    private final int maxWaitTimeForRiskReport;

    private final int scanMemory;

    private final ImmutableList<String> scanTargetPaths;

    /**
     * We don't want to instantiate this from anything but the HubScanJobConfigBuilder.
     */
    HubScanJobConfig(String projectName, String version, String phase, String distribution, String workingDirectory, int scanMemory,
            boolean shouldGenerateRiskReport, int maxWaitTimeForRiskReport, ImmutableList<String> scanTargetPaths) {
        this.projectName = projectName;
        this.version = version;
        this.phase = phase;
        this.distribution = distribution;
        this.workingDirectory = workingDirectory;
        this.shouldGenerateRiskReport = shouldGenerateRiskReport;
        this.maxWaitTimeForRiskReport = maxWaitTimeForRiskReport;
        this.scanMemory = scanMemory;
        this.scanTargetPaths = scanTargetPaths;
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

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public boolean isShouldGenerateRiskReport() {
        return shouldGenerateRiskReport;
    }

    public int getMaxWaitTimeForRiskReport() {
        return maxWaitTimeForRiskReport;
    }

    public long getMaxWaitTimeForRiskReportInMilliseconds() {
        return maxWaitTimeForRiskReport * 60 * 1000;
    }

    public int getScanMemory() {
        return scanMemory;
    }

    public List<String> getScanTargetPaths() {
        return scanTargetPaths;
    }

    @Override
    public String toString() {
        return "HubScanJobConfig [projectName=" + projectName + ", version=" + version + ", phase=" + phase + ", distribution=" + distribution
                + ", workingDirectory=" + workingDirectory + ", shouldGenerateRiskReport=" + shouldGenerateRiskReport + ", maxWaitTimeForRiskReport="
                + maxWaitTimeForRiskReport + ", scanMemory=" + scanMemory + ", scanTargetPaths=" + scanTargetPaths + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((distribution == null) ? 0 : distribution.hashCode());
        result = prime * result + maxWaitTimeForRiskReport;
        result = prime * result + ((phase == null) ? 0 : phase.hashCode());
        result = prime * result + ((projectName == null) ? 0 : projectName.hashCode());
        result = prime * result + scanMemory;
        result = prime * result + ((scanTargetPaths == null) ? 0 : scanTargetPaths.hashCode());
        result = prime * result + (shouldGenerateRiskReport ? 1231 : 1237);
        result = prime * result + ((version == null) ? 0 : version.hashCode());
        result = prime * result + ((workingDirectory == null) ? 0 : workingDirectory.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        HubScanJobConfig other = (HubScanJobConfig) obj;
        if (distribution == null) {
            if (other.distribution != null) {
                return false;
            }
        } else if (!distribution.equals(other.distribution)) {
            return false;
        }
        if (maxWaitTimeForRiskReport != other.maxWaitTimeForRiskReport) {
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
        return true;
    }

}
