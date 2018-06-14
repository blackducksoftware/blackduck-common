/**
 * hub-common
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
package com.blackducksoftware.integration.hub.notification.content.detail;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.blackducksoftware.integration.hub.api.UriSingleResponse;
import com.blackducksoftware.integration.hub.api.core.HubResponse;
import com.blackducksoftware.integration.hub.api.enumeration.NotificationTypeGrouping;
import com.blackducksoftware.integration.hub.api.generated.view.ComponentVersionView;
import com.blackducksoftware.integration.hub.api.generated.view.ComponentView;
import com.blackducksoftware.integration.hub.api.generated.view.ProjectVersionView;

public abstract class ProjectNotificationContentDetail extends NotificationContentDetail2 {
    private final String projectName;
    private final String projectVersionName;
    private final UriSingleResponse<ProjectVersionView> projectVersion;

    private final String componentName;
    private final UriSingleResponse<ComponentView> component;

    private final String componentVersionName;
    private final UriSingleResponse<ComponentVersionView> componentVersion;

    // @formatter:off
    public ProjectNotificationContentDetail(
             final NotificationTypeGrouping notificationTypeGrouping
            ,final String projectName
            ,final String projectVersionName
            ,final String projectVersionUri
            ,final String componentName
            ,final String componentUri
            ,final String componentVersionName
            ,final String componentVersionUri
            ) {
        super(notificationTypeGrouping);
        this.projectName = projectName;
        this.projectVersionName = projectVersionName;
        this.projectVersion = createUriSingleResponse(projectVersionUri, ProjectVersionView.class);
        this.componentName = componentName;
        this.component = createUriSingleResponse(componentUri, ComponentView.class);
        this.componentVersionName = componentVersionName;
        this.componentVersion = createUriSingleResponse(componentVersionUri, ComponentVersionView.class);
    }
    // @formatter:on

    public String getProjectName() {
        return projectName;
    }

    public String getProjectVersionName() {
        return projectVersionName;
    }

    public UriSingleResponse<ProjectVersionView> getProjectVersion() {
        return projectVersion;
    }

    public String getComponentName() {
        return componentName;
    }

    public Optional<UriSingleResponse<ComponentView>> getComponent() {
        return Optional.ofNullable(component);
    }

    public Optional<String> getComponentVersionName() {
        return Optional.ofNullable(componentVersionName);
    }

    public Optional<UriSingleResponse<ComponentVersionView>> getComponentVersion() {
        return Optional.ofNullable(componentVersion);
    }

    @Override
    public List<UriSingleResponse<? extends HubResponse>> getPresentLinks() {
        final List<UriSingleResponse<? extends HubResponse>> presentLinks = new ArrayList<>();
        presentLinks.add(projectVersion);
        if (component != null) {
            presentLinks.add(component);
        }
        if (componentVersion != null) {
            presentLinks.add(componentVersion);
        }
        return presentLinks;
    }

}
