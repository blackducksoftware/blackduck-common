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
	private String url;

	public String getProjectName() {
		return projectName;
	}

	public String getProjectVersionName() {
		return projectVersionName;
	}

	public String getUrl() {
		return url;
	}

	public String getRelativeUrl() {
		return "relativeUrl"; // TODO
	}

	public void setProjectName(final String projectName) {
		this.projectName = projectName;
	}

	public void setProjectVersionName(final String projectVersionName) {
		this.projectVersionName = projectVersionName;
	}

	public void setUrl(final String url) {
		this.url = url;
	}

	@Deprecated
	public UUID getProjectId() throws MissingUUIDException {
		if (getUrl() == null) {
			return null;
		}
		return HubUrlParser.getUUIDFromURLString(PROJECT_URL_IDENTIFIER, getUrl());
	}

	@Deprecated
	public UUID getVersionId() throws MissingUUIDException {
		if (getUrl() == null) {
			return null;
		}
		return HubUrlParser.getUUIDFromURLString(VERSION_URL_IDENTIFIER, getUrl());
	}

	@Override
	public String toString() {
		return "ProjectVersion [projectName=" + projectName + ", projectVersionName=" + projectVersionName
				+ ", projectVersionLink=" + getUrl() + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((projectName == null) ? 0 : projectName.hashCode());
		result = prime * result + ((projectVersionName == null) ? 0 : projectVersionName.hashCode());
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
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ProjectVersion other = (ProjectVersion) obj;
		if (projectName == null) {
			if (other.projectName != null) {
				return false;
			}
		} else if (!projectName.equals(other.projectName)) {
			return false;
		}
		if (projectVersionName == null) {
			if (other.projectVersionName != null) {
				return false;
			}
		} else if (!projectVersionName.equals(other.projectVersionName)) {
			return false;
		}
		return true;
	}

}
