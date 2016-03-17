package com.blackducksoftware.integration.hub;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

public class HubScanJobConfig {
    public static final int DEFAULT_MEMORY_IN_MEGABYTES = 4096;

    public static final int DEFAULT_REPORT_WAIT_TIME_IN_MINUTES = 5;

    private String projectName;

    private String version;

    private String phase;

    private String distribution;

    private boolean shouldGenerateRiskReport;

    private int maxWaitTimeForRiskReport;

    private int scanMemory;

    private List<String> scanTargetPaths = new ArrayList<String>();

    private String workingDirectory;

    public HubScanJobConfig() {
    }

    public HubScanJobConfig(String projectName, String version, String phase, String distribution) {
        setProjectName(projectName);
        setVersion(version);
        setPhase(phase);
        setDistribution(distribution);
    }

    public HubScanJobConfig(String projectName, String version, String phase, String distribution, int scanMemory, boolean shouldGenerateRiskReport,
            int maxWaitTimeForRiskReport) {
        setProjectName(projectName);
        setVersion(version);
        setPhase(phase);
        setDistribution(distribution);
        setScanMemory(scanMemory);
        setShouldGenerateRiskReport(shouldGenerateRiskReport);
        setMaxWaitTimeForRiskReport(maxWaitTimeForRiskReport);
    }

    public HubScanJobConfig(String projectName, String version, String phase, String distribution, String scanMemory, String shouldGenerateRiskReport,
            String maxWaitTimeForRiskReport) {
        setProjectName(projectName);
        setVersion(version);
        setPhase(phase);
        setDistribution(distribution);
        setScanMemory(scanMemory);
        setShouldGenerateRiskReport(shouldGenerateRiskReport);
        setMaxWaitTimeForRiskReport(maxWaitTimeForRiskReport);
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        if (StringUtils.isNotBlank(projectName)) {
            this.projectName = projectName.trim();
        } else {
            this.projectName = null;
        }
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        if (StringUtils.isNotBlank(version)) {
            this.version = version.trim();
        } else {
            this.version = null;
        }
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public String getDistribution() {
        return distribution;
    }

    public void setDistribution(String distribution) {
        this.distribution = distribution;
    }

    public boolean isShouldGenerateRiskReport() {
        return shouldGenerateRiskReport;
    }

    public void setShouldGenerateRiskReport(boolean shouldGenerateRiskReport) {
        if (null == projectName || null == version) {
            // Dont want to generate the report if they have not provided a Project name or version
            this.shouldGenerateRiskReport = false;
        } else {
            this.shouldGenerateRiskReport = shouldGenerateRiskReport;
        }
    }

    public void setShouldGenerateRiskReport(String shouldGenerateRiskReport) {
        boolean shouldGenerateRiskReportValue = Boolean.valueOf(shouldGenerateRiskReport);
        setShouldGenerateRiskReport(shouldGenerateRiskReportValue);
    }

    public int getMaxWaitTimeForRiskReport() {
        return maxWaitTimeForRiskReport;
    }

    public void setMaxWaitTimeForRiskReport(int maxWaitTimeForRiskReport) {
        this.maxWaitTimeForRiskReport = maxWaitTimeForRiskReport;
        if (maxWaitTimeForRiskReport <= 0) {
            this.maxWaitTimeForRiskReport = DEFAULT_REPORT_WAIT_TIME_IN_MINUTES;
        } else {
            this.maxWaitTimeForRiskReport = maxWaitTimeForRiskReport;
        }
    }

    public void setMaxWaitTimeForRiskReport(String maxWaitTimeForRiskReport) {
        int maxWaitTimeForRiskReportValue = NumberUtils.toInt(maxWaitTimeForRiskReport, DEFAULT_REPORT_WAIT_TIME_IN_MINUTES);
        setMaxWaitTimeForRiskReport(maxWaitTimeForRiskReportValue);
    }

    public int getScanMemory() {
        return scanMemory;
    }

    public void setScanMemory(int scanMemory) {
        if (scanMemory <= 0) {
            this.scanMemory = DEFAULT_MEMORY_IN_MEGABYTES;
        } else {
            this.scanMemory = scanMemory;
        }
    }

    public void setScanMemory(String scanMemory) {
        int scanMemoryValue = NumberUtils.toInt(scanMemory, DEFAULT_MEMORY_IN_MEGABYTES);
        setScanMemory(scanMemoryValue);
    }

    public List<String> getScanTargetPaths() {
        return scanTargetPaths;
    }

    public void setScanTargetPaths(List<String> scanTargetPaths) {
        this.scanTargetPaths = scanTargetPaths;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

}
