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
package com.blackducksoftware.integration.hub.api.project.version;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.hub.api.project.ProjectItem;
import com.blackducksoftware.integration.hub.api.version.DistributionEnum;
import com.blackducksoftware.integration.hub.api.version.PhaseEnum;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.request.HubPagedRequest;
import com.blackducksoftware.integration.hub.request.HubRequest;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubParameterizedRequestService;
import com.google.gson.JsonObject;

public class ProjectVersionRequestService extends HubParameterizedRequestService<ProjectVersionItem> {
    public ProjectVersionRequestService(final RestConnection restConnection) {
        super(restConnection, ProjectVersionItem.class);
    }

    public ProjectVersionItem getProjectVersion(ProjectItem project, String projectVersionName) throws HubIntegrationException {
        final String versionsUrl = project.getLink("versions");
        final HubPagedRequest hubPagedRequest = getHubRequestFactory().createGetPagedRequest(100, versionsUrl);
        if (StringUtils.isNotBlank(projectVersionName)) {
            hubPagedRequest.setQ(String.format("versionName:%s", projectVersionName));
        }

        final List<ProjectVersionItem> allProjectVersionMatchingItems = getAllItems(hubPagedRequest);
        for (final ProjectVersionItem projectVersion : allProjectVersionMatchingItems) {
            if (projectVersionName.equals(projectVersion.getVersionName())) {
                return projectVersion;
            }
        }

        throw new HubIntegrationException(String.format("Could not find the version: %s for project: %s", projectVersionName, project.getName()));
    }

    public List<ProjectVersionItem> getAllProjectVersions(final ProjectItem project) throws HubIntegrationException {
        final String versionsUrl = project.getLink("versions");
        return getAllProjectVersions(versionsUrl);
    }

    public List<ProjectVersionItem> getAllProjectVersions(final String versionsUrl) throws HubIntegrationException {
        final List<ProjectVersionItem> allProjectVersionItems = getAllItems(versionsUrl);
        return allProjectVersionItems;
    }

    public String createHubVersion(final ProjectItem project, final String versionName, final PhaseEnum phase, final DistributionEnum dist)
            throws HubIntegrationException {
        JsonObject json = new JsonObject();
        json.addProperty("versionName", versionName);
        json.addProperty("phase", phase.name());
        json.addProperty("distribution", dist.name());

        final String versionsUrl = project.getLink("versions");

        final HubRequest hubRequest = getHubRequestFactory().createPostRequest(versionsUrl);

        final String location = hubRequest.executePost(getRestConnection().getGson().toJson(phase));

        return location;
    }

}
