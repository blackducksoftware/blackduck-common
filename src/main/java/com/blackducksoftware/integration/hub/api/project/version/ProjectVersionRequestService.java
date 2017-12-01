/**
 * Hub Common
 *
 * Copyright (C) 2017 Black Duck Software, Inc.
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
package com.blackducksoftware.integration.hub.api.project.version;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.exception.DoesNotExistException;
import com.blackducksoftware.integration.hub.model.request.ProjectVersionRequest;
import com.blackducksoftware.integration.hub.model.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.model.view.ProjectView;
import com.blackducksoftware.integration.hub.request.HubPagedRequest;
import com.blackducksoftware.integration.hub.request.HubRequest;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubResponseService;

import okhttp3.Response;

public class ProjectVersionRequestService extends HubResponseService {

    public ProjectVersionRequestService(final RestConnection restConnection) {
        super(restConnection);
    }

    public ProjectVersionView getProjectVersion(final ProjectView project, final String projectVersionName) throws IntegrationException {
        final String versionsUrl = metaService.getFirstLink(project, MetaService.VERSIONS_LINK);
        final HubPagedRequest hubPagedRequest = getHubRequestFactory().createPagedRequest(100, versionsUrl);
        if (StringUtils.isNotBlank(projectVersionName)) {
            hubPagedRequest.q = String.format("versionName:%s", projectVersionName);
        }

        final List<ProjectVersionView> allProjectVersionMatchingItems = getAllItems(hubPagedRequest, ProjectVersionView.class);
        for (final ProjectVersionView projectVersion : allProjectVersionMatchingItems) {
            if (projectVersionName.equals(projectVersion.versionName)) {
                return projectVersion;
            }
        }

        throw new DoesNotExistException(String.format("Could not find the version: %s for project: %s", projectVersionName, project.name));
    }

    public List<ProjectVersionView> getAllProjectVersions(final ProjectView project) throws IntegrationException {
        return getAllItemsFromLink(project, MetaService.VERSIONS_LINK, ProjectVersionView.class);
    }

    public List<ProjectVersionView> getAllProjectVersions(final String versionsUrl) throws IntegrationException {
        final List<ProjectVersionView> allProjectVersionItems = getAllItems(versionsUrl, ProjectVersionView.class);
        return allProjectVersionItems;
    }

    public String createHubVersion(final ProjectView project, final ProjectVersionRequest version) throws IntegrationException {
        return createHubVersion(metaService.getFirstLink(project, MetaService.VERSIONS_LINK), version);
    }

    public String createHubVersion(final String versionsUrl, final ProjectVersionRequest version) throws IntegrationException {

        final HubRequest hubRequest = getHubRequestFactory().createRequest(versionsUrl);
        Response response = null;
        try {
            response = hubRequest.executePost(getGson().toJson(version));
            return response.header("location");
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

}
