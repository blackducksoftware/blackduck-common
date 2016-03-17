package com.blackducksoftware.integration.hub.report.api;

import java.util.List;

import org.joda.time.DateTime;

import com.blackducksoftware.integration.hub.HubIntRestService;

public class HubReportGenerationInfo {
    private HubIntRestService service;

    private String projectId;

    private String versionId;

    private String hostname;

    private List<String> scanTargets;

    private long maximumWaitTime;

    private DateTime beforeScanTime;

    private DateTime afterScanTime;

    public HubIntRestService getService() {
        return service;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getVersionId() {
        return versionId;
    }

    public String getHostname() {
        return hostname;
    }

    public List<String> getScanTargets() {
        return scanTargets;
    }

    public long getMaximumWaitTime() {
        return maximumWaitTime;
    }

    public DateTime getBeforeScanTime() {
        return beforeScanTime;
    }

    public DateTime getAfterScanTime() {
        return afterScanTime;
    }

    public void setService(HubIntRestService service) {
        this.service = service;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setScanTargets(List<String> scanTargets) {
        this.scanTargets = scanTargets;
    }

    public void setMaximumWaitTime(long maximumWaitTime) {
        this.maximumWaitTime = maximumWaitTime;
    }

    public void setBeforeScanTime(DateTime beforeScanTime) {
        this.beforeScanTime = beforeScanTime;
    }

    public void setAfterScanTime(DateTime afterScanTime) {
        this.afterScanTime = afterScanTime;
    }

}
