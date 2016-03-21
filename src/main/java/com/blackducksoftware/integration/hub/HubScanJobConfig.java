package com.blackducksoftware.integration.hub;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.blackducksoftware.integration.hub.exception.HubIntegrationException;

public class HubScanJobConfig {
    public static final int DEFAULT_MEMORY_IN_MEGABYTES = 4096;

    public static final int MINIMUM_MEMORY_IN_MEGABYTES = 256;

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

    public HubScanJobConfig(String projectName, String version, String phase, String distribution, String workingDirectory) {
        setProjectName(projectName);
        setVersion(version);
        setPhase(phase);
        setDistribution(distribution);
        setWorkingDirectory(workingDirectory);
    }

    public HubScanJobConfig(String projectName, String version, String phase, String distribution, String workingDirectory, int scanMemory,
            boolean shouldGenerateRiskReport,
            int maxWaitTimeForRiskReport) {
        setProjectName(projectName);
        setVersion(version);
        setPhase(phase);
        setDistribution(distribution);
        setWorkingDirectory(workingDirectory);
        setScanMemory(scanMemory);
        setShouldGenerateRiskReport(shouldGenerateRiskReport);
        setMaxWaitTimeForRiskReport(maxWaitTimeForRiskReport);
    }

    public HubScanJobConfig(String projectName, String version, String phase, String distribution, String workingDirectory, String scanMemory,
            String shouldGenerateRiskReport,
            String maxWaitTimeForRiskReport) {
        setProjectName(projectName);
        setVersion(version);
        setPhase(phase);
        setDistribution(distribution);
        setWorkingDirectory(workingDirectory);
        setScanMemory(scanMemory);
        setShouldGenerateRiskReport(shouldGenerateRiskReport);
        setMaxWaitTimeForRiskReport(maxWaitTimeForRiskReport);
    }

    public void assertValid() throws HubIntegrationException {
        if (maxWaitTimeForRiskReport <= 0) {
            throw new HubIntegrationException("The maximum wait time for the risk report must be > 0.");
        }

        if (scanMemory < MINIMUM_MEMORY_IN_MEGABYTES) {
            throw new HubIntegrationException("The minimum Hub Scan Memory is 256 MB.");
        }

        if (null == projectName || null == version && shouldGenerateRiskReport) {
            throw new HubIntegrationException("You can not generate the Black Duck Risk Report without providing a Project Name or Version.");
        }
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = StringUtils.trimToNull(projectName);
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = StringUtils.trimToNull(version);
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
        this.shouldGenerateRiskReport = shouldGenerateRiskReport;
    }

    public void setShouldGenerateRiskReport(String shouldGenerateRiskReport) {
        boolean shouldGenerateRiskReportValue = Boolean.valueOf(shouldGenerateRiskReport);
        setShouldGenerateRiskReport(shouldGenerateRiskReportValue);
    }

    public int getMaxWaitTimeForRiskReport() {
        return maxWaitTimeForRiskReport;
    }

    public long getMaxWaitTimeForRiskReportInMilliseconds() {
        return maxWaitTimeForRiskReport * 60 * 1000;
    }

    public void setMaxWaitTimeForRiskReport(int maxWaitTimeForRiskReport) {
        this.maxWaitTimeForRiskReport = maxWaitTimeForRiskReport;
    }

    public void setMaxWaitTimeForRiskReport(String maxWaitTimeForRiskReport) {
        int maxWaitTimeForRiskReportValue = NumberUtils.toInt(maxWaitTimeForRiskReport);
        setMaxWaitTimeForRiskReport(maxWaitTimeForRiskReportValue);
    }

    public int getScanMemory() {
        return scanMemory;
    }

    public void setScanMemory(int scanMemory) {
        this.scanMemory = scanMemory;
    }

    public void setScanMemory(String scanMemory) {
        int scanMemoryValue = NumberUtils.toInt(scanMemory);
        setScanMemory(scanMemoryValue);
    }

    public List<String> getScanTargetPaths() {
        return scanTargetPaths;
    }

    public void addScanTargetPath(String scanTargetPath) {
        scanTargetPaths.add(scanTargetPath);
    }

    public void addAllScanTargetPaths(List<String> scanTargetPaths) {
        this.scanTargetPaths.addAll(scanTargetPaths);
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

}
