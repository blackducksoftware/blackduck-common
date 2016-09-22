package com.blackducksoftware.integration.hub.dataservices.policystatus;

import java.io.IOException;
import java.net.URISyntaxException;

import com.blackducksoftware.integration.hub.api.PolicyStatusRestService;
import com.blackducksoftware.integration.hub.api.ProjectRestService;
import com.blackducksoftware.integration.hub.api.ProjectVersionRestService;
import com.blackducksoftware.integration.hub.api.policy.PolicyStatusItem;
import com.blackducksoftware.integration.hub.api.project.ProjectItem;
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
