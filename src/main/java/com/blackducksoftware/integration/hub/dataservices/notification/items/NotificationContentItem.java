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
package com.blackducksoftware.integration.hub.dataservices.notification.items;

import java.net.URISyntaxException;
import java.util.Date;

import com.blackducksoftware.integration.hub.api.project.ProjectVersion;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.common.base.Joiner;

public class NotificationContentItem implements Comparable<NotificationContentItem> {
	private final ProjectVersion projectVersion;
	private final String componentName;
	private final String componentVersion;

	private final String componentVersionUrl;

	// We need createdAt (from the enclosing notificationItem) so we can order
	// them after
	// they are collected multi-threaded
	public final Date createdAt;

	public NotificationContentItem(final Date createdAt, final ProjectVersion projectVersion,
			final String componentName,
 final String componentVersion, final String componentVersionUrl) {
		this.createdAt = createdAt;
		this.projectVersion = projectVersion;
		this.componentName = componentName;
		this.componentVersion = componentVersion;
		this.componentVersionUrl = componentVersionUrl;
	}

	public ProjectVersion getProjectVersion() {
		return projectVersion;
	}

	public String getComponentName() {
		return componentName;
	}

	public String getComponentVersion() {
		return componentVersion;
	}

	public String getComponentVersionUrl() {
		return componentVersionUrl;
	}

	public String getComponentVersionRelativeUrl() throws URISyntaxException {
		return RestConnection.getRelativeUrl(componentVersionUrl);
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("NotificationContentItem [projectVersion=");
		builder.append(projectVersion);
		builder.append(", componentName=");
		builder.append(componentName);
		builder.append(", componentVersion=");
		builder.append(componentVersion);
		builder.append(", componentVersionUrl=");
		builder.append(componentVersionUrl);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int compareTo(final NotificationContentItem o) {
		if (equals(o)) {
			return 0;
		}

		final int createdAtComparison = getCreatedAt().compareTo(o.getCreatedAt());
		if (createdAtComparison != 0) {
			// If createdAt times are different, use createdAt to compare
			return createdAtComparison;
		}

		// Identify same-time non-equal items as non-equal
		final Joiner joiner = Joiner.on(":").skipNulls();
		final String thisProjectVersionString = joiner.join(getProjectVersion().getProjectName(), getProjectVersion()
				.getProjectVersionName(), getComponentVersionUrl());
		final String otherProjectVersionString = joiner.join(o.getProjectVersion().getProjectName(), o
				.getProjectVersion().getProjectVersionName(), o.getComponentVersionUrl().toString());

		return thisProjectVersionString.compareTo(otherProjectVersionString);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getComponentVersionUrl() == null) ? 0 : getComponentVersionUrl().hashCode());
		result = prime * result + ((createdAt == null) ? 0 : createdAt.hashCode());
		result = prime * result + ((projectVersion == null) ? 0 : projectVersion.hashCode());
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
		final NotificationContentItem other = (NotificationContentItem) obj;
		if (getComponentVersionUrl() == null) {
			if (other.getComponentVersionUrl() != null) {
				return false;
			}
		} else if (!getComponentVersionUrl().equals(other.getComponentVersionUrl())) {
			return false;
		}
		if (createdAt == null) {
			if (other.createdAt != null) {
				return false;
			}
		} else if (!createdAt.equals(other.createdAt)) {
			return false;
		}
		if (projectVersion == null) {
			if (other.projectVersion != null) {
				return false;
			}
		} else if (!projectVersion.equals(other.projectVersion)) {
			return false;
		}
		return true;
	}


}
