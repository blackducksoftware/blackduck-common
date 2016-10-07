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
package com.blackducksoftware.integration.hub.dataservices.policystatus;

import java.io.IOException;
import java.net.URISyntaxException;

import com.blackducksoftware.integration.hub.api.policy.PolicyStatusItem;
import com.blackducksoftware.integration.hub.api.policy.PolicyStatusRestService;
import com.blackducksoftware.integration.hub.api.project.ProjectItem;
import com.blackducksoftware.integration.hub.api.project.ProjectRestService;
import com.blackducksoftware.integration.hub.api.project.ProjectVersionRestService;
import com.blackducksoftware.integration.hub.api.version.ReleaseItem;
import com.blackducksoftware.integration.hub.dataservices.AbstractDataService;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.exception.MissingUUIDException;
import com.blackducksoftware.integration.hub.exception.ProjectDoesNotExistException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.Gson;
import com.google.gson.JsonParser;

public class PolicyStatusDataService extends AbstractDataService {
	private final ProjectRestService projectRestService;
	private final ProjectVersionRestService projectVersionRestService;
	private final PolicyStatusRestService policyStatusRestService;

	public PolicyStatusDataService(final RestConnection restConnection, final Gson gson, final JsonParser jsonParser,
			final ProjectRestService projectRestService, final ProjectVersionRestService projectVersionRestService,
			final PolicyStatusRestService policyStatusRestService) {
		super(restConnection, gson, jsonParser);
		this.projectRestService = projectRestService;
		this.projectVersionRestService = projectVersionRestService;
		this.policyStatusRestService = policyStatusRestService;
	}

	@Deprecated
	public PolicyStatusItem getPolicyStatusForProjectAndVersion(final String projectName, final String projectVersion)
			throws IOException, URISyntaxException, BDRestException, ProjectDoesNotExistException,
			HubIntegrationException, MissingUUIDException {
		final ProjectItem projectItem = projectRestService.getProjectByName(projectName);
		final String projectId = projectItem.getProjectId().toString();

		final ReleaseItem releaseItem = projectVersionRestService
				.getProjectVersionByName(projectItem.getProjectId().toString(), projectVersion);
		final String versionId = releaseItem.getVersionId().toString();

		return policyStatusRestService.getPolicyStatusItem(projectId, versionId);
	}

}
