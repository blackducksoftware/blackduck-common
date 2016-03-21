package com.blackducksoftware.integration.hub;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

public class HubScanJobConfigBuilder {
    private String projectName;

    private String version;

    private String phase;

    private String distribution;

    private String workingDirectory;

    private boolean shouldGenerateRiskReport;

    private int maxWaitTimeForRiskReport;

    private int scanMemory;

    private List<String> scanTargetPaths = new ArrayList<String>();

    public HubScanJobConfig build() {
        return new HubScanJobConfig(projectName, version, phase, distribution, workingDirectory, scanMemory, shouldGenerateRiskReport,
                maxWaitTimeForRiskReport, scanTargetPaths);
    }

    public void setProjectName(String projectName) {
        this.projectName = StringUtils.trimToNull(projectName);
    }

    public void setVersion(String version) {
        this.version = StringUtils.trimToNull(version);
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public void setDistribution(String distribution) {
        this.distribution = distribution;
    }

    public void setShouldGenerateRiskReport(boolean shouldGenerateRiskReport) {
        this.shouldGenerateRiskReport = shouldGenerateRiskReport;
    }

    public void setShouldGenerateRiskReport(String shouldGenerateRiskReport) {
        boolean shouldGenerateRiskReportValue = Boolean.valueOf(shouldGenerateRiskReport);
        setShouldGenerateRiskReport(shouldGenerateRiskReportValue);
    }

    public void setMaxWaitTimeForRiskReport(int maxWaitTimeForRiskReport) {
        this.maxWaitTimeForRiskReport = maxWaitTimeForRiskReport;
    }

    public void setMaxWaitTimeForRiskReport(String maxWaitTimeForRiskReport) {
        int maxWaitTimeForRiskReportValue = NumberUtils.toInt(maxWaitTimeForRiskReport);
        setMaxWaitTimeForRiskReport(maxWaitTimeForRiskReportValue);
    }

    public void setScanMemory(int scanMemory) {
        this.scanMemory = scanMemory;
    }

    public void setScanMemory(String scanMemory) {
        int scanMemoryValue = NumberUtils.toInt(scanMemory);
        setScanMemory(scanMemoryValue);
    }

    public void addScanTargetPath(String scanTargetPath) {
        scanTargetPaths.add(scanTargetPath);
    }

    public void addAllScanTargetPaths(List<String> scanTargetPaths) {
        this.scanTargetPaths.addAll(scanTargetPaths);
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

}
