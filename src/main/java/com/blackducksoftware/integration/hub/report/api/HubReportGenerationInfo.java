/*******************************************************************************
 * Black Duck Software Suite SDK
 * Copyright (C) 2016 Black Duck Software, Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *******************************************************************************/
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

	public void setProject(final ProjectItem project) {
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
