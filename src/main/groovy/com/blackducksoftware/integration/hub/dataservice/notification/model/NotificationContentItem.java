/**
 * Hub Common
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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
 */
package com.blackducksoftware.integration.hub.dataservice.notification.model;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import com.blackducksoftware.integration.hub.dataservice.model.ProjectVersionModel;
import com.blackducksoftware.integration.hub.model.view.ComponentVersionView;

public class NotificationContentItem implements Comparable<NotificationContentItem> {
    private final ProjectVersionModel projectVersion;

    private final String componentName;

    private final ComponentVersionView componentVersion;

    private final String componentVersionUrl;

    private String componentIssueLink;

    // We need createdAt (from the enclosing notificationItem) so we can order
    // them after
    // they are collected multi-threaded
    public final Date createdAt;

    public NotificationContentItem(final Date createdAt, final ProjectVersionModel projectVersion,
            final String componentName,
            final ComponentVersionView componentVersion,
            final String componentVersionUrl, final String componentIssueUrl) {
        this.createdAt = createdAt;
        this.projectVersion = projectVersion;
        this.componentName = componentName;
        this.componentVersion = componentVersion;
        this.componentVersionUrl = componentVersionUrl;
        this.componentIssueLink = componentIssueUrl;
    }

    public ProjectVersionModel getProjectVersion() {
        return projectVersion;
    }

    public String getComponentName() {
        return componentName;
    }

    public ComponentVersionView getComponentVersion() {
        return componentVersion;
    }

    public String getComponentVersionUrl() {
        return componentVersionUrl;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getComponentIssueLink() {
        return componentIssueLink;
    }

    public void setComponentIssueLink(final String componentIssueLink) {
        this.componentIssueLink = componentIssueLink;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, RecursiveToStringStyle.JSON_STYLE);
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
        final String thisProjectVersionString = StringUtils.join(getProjectVersion().getProjectName(), getProjectVersion()
                .getProjectVersionName(), getComponentName(), getComponentVersionName(getComponentVersion()));
        final String otherProjectVersionString = StringUtils.join(o.getProjectVersion().getProjectName(), o
                .getProjectVersion().getProjectVersionName(), o.getComponentName(), getComponentVersionName(o.getComponentVersion()));
        return thisProjectVersionString.compareTo(otherProjectVersionString);
    }

    private String getComponentVersionName(final ComponentVersionView componentVersion) {
        if (componentVersion == null) {
            return "";
        }
        return componentVersion.versionName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((componentIssueLink == null) ? 0 : componentIssueLink.hashCode());
        result = prime * result + ((componentName == null) ? 0 : componentName.hashCode());
        result = prime * result + ((componentVersion == null) ? 0 : componentVersion.hashCode());
        result = prime * result + ((componentVersionUrl == null) ? 0 : componentVersionUrl.hashCode());
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
        if (componentIssueLink == null) {
            if (other.componentIssueLink != null) {
                return false;
            }
        } else if (!componentIssueLink.equals(other.componentIssueLink)) {
            return false;
        }
        if (componentName == null) {
            if (other.componentName != null) {
                return false;
            }
        } else if (!componentName.equals(other.componentName)) {
            return false;
        }
        if (componentVersion == null) {
            if (other.componentVersion != null) {
                return false;
            }
        } else if (!componentVersion.equals(other.componentVersion)) {
            return false;
        }
        if (componentVersionUrl == null) {
            if (other.componentVersionUrl != null) {
                return false;
            }
        } else if (!componentVersionUrl.equals(other.componentVersionUrl)) {
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
