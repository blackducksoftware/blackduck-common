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
package com.blackducksoftware.integration.hub.api.report;

import java.util.List;

import org.joda.time.DateTime;

import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.api.project.ProjectItem;
import com.blackducksoftware.integration.hub.api.version.ReleaseItem;

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
