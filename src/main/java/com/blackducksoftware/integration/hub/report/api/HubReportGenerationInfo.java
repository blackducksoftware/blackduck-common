package com.blackducksoftware.integration.hub.report.api;

import java.util.List;

import org.joda.time.DateTime;

import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.project.api.ProjectItem;
import com.blackducksoftware.integration.hub.version.api.ReleaseItem;

public class HubReportGenerationInfo {
	private HubIntRestService service;

	private ProjectItem project;

	private ReleaseItem version;

	private String hostname;

	private List<String> scanTargets;

	private long maximumWaitTime;

	private DateTime beforeScanTime;

	private DateTime afterScanTime;

	private String scanStatusDirectory;

	public HubIntRestService getService() {
		return service;
	}

	public ProjectItem getProject() {
		return project;
	}

	public ReleaseItem getVersion() {
		return version;
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

	public void setService(final HubIntRestService service) {
		this.service = service;
	}

	public void setProjectId(final ProjectItem project) {
		this.project = project;
	}

	public void setVersion(final ReleaseItem version) {
		this.version = version;
	}

	public void setHostname(final String hostname) {
		this.hostname = hostname;
	}

	public void setScanTargets(final List<String> scanTargets) {
		this.scanTargets = scanTargets;
	}

	public void setMaximumWaitTime(final long maximumWaitTime) {
		this.maximumWaitTime = maximumWaitTime;
	}

	public void setBeforeScanTime(final DateTime beforeScanTime) {
		this.beforeScanTime = beforeScanTime;
	}

	public void setAfterScanTime(final DateTime afterScanTime) {
		this.afterScanTime = afterScanTime;
	}

	public String getScanStatusDirectory() {
		return scanStatusDirectory;
	}

	public void setScanStatusDirectory(final String scanStatusDirectory) {
		this.scanStatusDirectory = scanStatusDirectory;
	}

}
