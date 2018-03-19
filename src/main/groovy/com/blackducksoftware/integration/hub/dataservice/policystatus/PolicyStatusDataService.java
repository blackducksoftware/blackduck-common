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
package com.blackducksoftware.integration.hub.dataservice.policystatus;

import java.util.List;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.api.project.ProjectRequestService;
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionRequestService;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.model.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.model.view.ProjectView;
import com.blackducksoftware.integration.hub.model.view.VersionBomPolicyStatusView;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubResponseService;

public class PolicyStatusDataService extends HubResponseService {
    private final ProjectRequestService projectRequestService;

    private final ProjectVersionRequestService projectVersionRequestService;

    private final MetaService metaService;

    public PolicyStatusDataService(final RestConnection restConnection, final ProjectRequestService projectRequestService,
            final ProjectVersionRequestService projectVersionRequestService, final MetaService metaService) {
        super(restConnection);
        this.metaService = metaService;
        this.projectRequestService = projectRequestService;
        this.projectVersionRequestService = projectVersionRequestService;
    }

    public VersionBomPolicyStatusView getPolicyStatusForProjectAndVersion(final String projectName,
            final String projectVersionName) throws IntegrationException {
        final ProjectView projectItem = projectRequestService.getProjectByName(projectName);
        final String versionsUrl = metaService.getFirstLink(projectItem, MetaService.VERSIONS_LINK);

        final List<ProjectVersionView> projectVersions = projectVersionRequestService.getAllProjectVersions(versionsUrl);
        final String policyStatusUrl = findPolicyStatusUrlFromVersions(projectVersions, projectVersionName);

        return getItem(policyStatusUrl, VersionBomPolicyStatusView.class);
    }

    public VersionBomPolicyStatusView getPolicyStatusForVersion(final ProjectVersionView version) throws IntegrationException {
        final String policyStatusUrl = metaService.getFirstLink(version, MetaService.POLICY_STATUS_LINK);
        return getItem(policyStatusUrl, VersionBomPolicyStatusView.class);
    }

    private String findPolicyStatusUrlFromVersions(final List<ProjectVersionView> projectVersions, final String projectVersionName)
            throws HubIntegrationException {
        for (final ProjectVersionView version : projectVersions) {
            if (projectVersionName.equals(version.versionName)) {
                final String policyStatusLink = metaService.getFirstLink(version, MetaService.POLICY_STATUS_LINK);
                return policyStatusLink;
            }
        }

        return null;
    }

}
