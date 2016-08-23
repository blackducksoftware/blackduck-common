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
package com.blackducksoftware.integration.hub.api.project;

import java.util.UUID;

import com.blackducksoftware.integration.hub.exception.MissingUUIDException;
import com.blackducksoftware.integration.hub.util.HubUrlParser;
import com.google.gson.annotations.SerializedName;

public class ProjectVersion {
	public static final String PROJECT_URL_IDENTIFIER = "projects";
	public static final String VERSION_URL_IDENTIFIER = "versions";
	private String projectName;
	private String projectVersionName;

	@SerializedName("projectVersion")
	private String projectVersionLink;

	public String getProjectName() {
		return projectName;
	}

	public String getProjectVersionName() {
		return projectVersionName;
	}

	public String getProjectVersionLink() {
		return projectVersionLink;
	}

	public void setProjectName(final String projectName) {
		this.projectName = projectName;
	}

	public void setProjectVersionName(final String projectVersionName) {
		this.projectVersionName = projectVersionName;
	}

	public void setProjectVersionLink(final String projectVersionLink) {
		this.projectVersionLink = projectVersionLink;
	}

	public UUID getProjectId() throws MissingUUIDException {
		if (getProjectVersionLink() == null) {
			return null;
		}
		return HubUrlParser.getUUIDFromURLString(PROJECT_URL_IDENTIFIER, getProjectVersionLink());
	}

	public UUID getVersionId() throws MissingUUIDException {
		if (getProjectVersionLink() == null) {
			return null;
		}
		return HubUrlParser.getUUIDFromURLString(VERSION_URL_IDENTIFIER, getProjectVersionLink());
	}

	@Override
	public String toString() {
		return "ProjectVersion [projectName=" + projectName + ", projectVersionName=" + projectVersionName
				+ ", projectVersionLink=" + projectVersionLink + "]";
	}

}
