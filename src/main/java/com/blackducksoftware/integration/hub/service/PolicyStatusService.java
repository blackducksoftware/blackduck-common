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
package com.blackducksoftware.integration.hub.service;

import java.util.List;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.generated.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.api.generated.view.ProjectView;
import com.blackducksoftware.integration.hub.api.generated.view.VersionBomPolicyStatusView;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.rest.RestConnection;

public class PolicyStatusService extends HubService {
    private final ProjectService projectDataService;

    public PolicyStatusService(final RestConnection restConnection, final ProjectService projectDataService) {
        super(restConnection);
        this.projectDataService = projectDataService;
    }

    public VersionBomPolicyStatusView getPolicyStatusForProjectAndVersion(final String projectName, final String projectVersionName) throws IntegrationException {
        final ProjectView projectItem = projectDataService.getProjectByName(projectName);

        final List<ProjectVersionView> projectVersions = getResponsesFromLinkResponse(projectItem, ProjectView.VERSIONS_LINK_RESPONSE, true);
        final ProjectVersionView projectVersionView = findMatchingVersion(projectVersions, projectVersionName);

        return getPolicyStatusForVersion(projectVersionView);
    }

    public VersionBomPolicyStatusView getPolicyStatusForVersion(final ProjectVersionView version) throws IntegrationException {
        return getResponseFromLinkResponse(version, ProjectVersionView.POLICY_STATUS_LINK_RESPONSE);
    }

    private ProjectVersionView findMatchingVersion(final List<ProjectVersionView> projectVersions, final String projectVersionName) throws HubIntegrationException {
        for (final ProjectVersionView version : projectVersions) {
            if (projectVersionName.equals(version.versionName)) {
                return version;
            }
        }
        return null;
    }

}
