package com.blackducksoftware.integration.hub.dataservices;

import com.blackducksoftware.integration.hub.api.bom.BomImportRestService;
import com.blackducksoftware.integration.hub.api.codelocation.CodeLocationRestService;
import com.blackducksoftware.integration.hub.api.component.ComponentVersionRestService;
import com.blackducksoftware.integration.hub.api.extension.ExtensionRestService;
import com.blackducksoftware.integration.hub.api.notification.NotificationRestService;
import com.blackducksoftware.integration.hub.api.policy.PolicyRestService;
import com.blackducksoftware.integration.hub.api.policy.PolicyStatusRestService;
import com.blackducksoftware.integration.hub.api.project.ProjectRestService;
import com.blackducksoftware.integration.hub.api.project.ProjectVersionRestService;
import com.blackducksoftware.integration.hub.api.scan.ScanSummaryRestService;
import com.blackducksoftware.integration.hub.api.user.UserRestService;
import com.blackducksoftware.integration.hub.api.version.VersionBomPolicyRestService;
import com.blackducksoftware.integration.hub.api.vulnerabilities.VulnerabilityRestService;
import com.blackducksoftware.integration.hub.dataservices.extension.ExtensionConfigDataService;
import com.blackducksoftware.integration.hub.dataservices.notification.NotificationDataService;
import com.blackducksoftware.integration.hub.dataservices.notification.items.PolicyNotificationFilter;
import com.blackducksoftware.integration.hub.dataservices.policystatus.PolicyStatusDataService;
import com.blackducksoftware.integration.hub.dataservices.scan.ScanStatusDataService;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.log.IntLogger;
import com.google.gson.Gson;
import com.google.gson.JsonParser;

public class DataServicesFactory {
	private final RestConnection restConnection;
	final Gson gson = new Gson();
	final JsonParser jsonParser = new JsonParser();

	private final BomImportRestService bomImportRestService;
	private final CodeLocationRestService codeLocationRestService;
	private final ComponentVersionRestService componentVersionRestService;
	private final NotificationRestService notificationRestService;
	private final PolicyRestService policyRestService;
	private final PolicyStatusRestService policyStatusRestService;
	private final ProjectRestService projectRestService;
	private final ProjectVersionRestService projectVersionRestService;
	private final ScanSummaryRestService scanSummaryRestService;
	private final UserRestService userRestService;
	private final VersionBomPolicyRestService versionBomPolicyRestService;
	private final VulnerabilityRestService vulnerabilityRestService;
	private final ExtensionRestService extensionRestService;

	public DataServicesFactory(final RestConnection restConnection) {
		this.restConnection = restConnection;

		bomImportRestService = new BomImportRestService(restConnection);
		codeLocationRestService = new CodeLocationRestService(restConnection, gson, jsonParser);
		componentVersionRestService = new ComponentVersionRestService(restConnection, gson, jsonParser);
		notificationRestService = new NotificationRestService(restConnection, gson, jsonParser);
		policyRestService = new PolicyRestService(restConnection, gson, jsonParser);
		policyStatusRestService = new PolicyStatusRestService(restConnection, gson, jsonParser);
		projectRestService = new ProjectRestService(restConnection, gson, jsonParser);
		projectVersionRestService = new ProjectVersionRestService(restConnection, gson, jsonParser);
		scanSummaryRestService = new ScanSummaryRestService(restConnection, gson, jsonParser);
		userRestService = new UserRestService(restConnection, gson, jsonParser);
		versionBomPolicyRestService = new VersionBomPolicyRestService(restConnection, gson, jsonParser);
		vulnerabilityRestService = new VulnerabilityRestService(restConnection, gson, jsonParser);
		extensionRestService = new ExtensionRestService(restConnection, gson, jsonParser);
	}

	public PolicyStatusDataService createPolicyStatusDataService() {
		return new PolicyStatusDataService(restConnection, gson, jsonParser, projectRestService,
				projectVersionRestService, policyStatusRestService);
	}

	public ScanStatusDataService createScanStatusDataService() {
		return new ScanStatusDataService(restConnection, gson, jsonParser, projectRestService,
				projectVersionRestService, codeLocationRestService, scanSummaryRestService);
	}

	public NotificationDataService createNotificationDataService(final IntLogger logger) {
		return new NotificationDataService(logger, restConnection, gson, jsonParser);
	}

	public NotificationDataService createNotificationDataService(final IntLogger logger,
			final PolicyNotificationFilter policyNotificationFilter) {
		return new NotificationDataService(logger, restConnection, gson, jsonParser, policyNotificationFilter);
	}

	public ExtensionConfigDataService createExtensionConfigDataService(final IntLogger logger) {
		return new ExtensionConfigDataService(logger, restConnection, gson, jsonParser, userRestService,
				extensionRestService);
	}

	public RestConnection getRestConnection() {
		return restConnection;
	}

	public Gson getGson() {
		return gson;
	}

	public JsonParser getJsonParser() {
		return jsonParser;
	}

	public BomImportRestService getBomImportRestService() {
		return bomImportRestService;
	}

	public CodeLocationRestService getCodeLocationRestService() {
		return codeLocationRestService;
	}

	public ComponentVersionRestService getComponentVersionRestService() {
		return componentVersionRestService;
	}

	public NotificationRestService getNotificationRestService() {
		return notificationRestService;
	}

	public PolicyRestService getPolicyRestService() {
		return policyRestService;
	}

	public PolicyStatusRestService getPolicyStatusRestService() {
		return policyStatusRestService;
	}

	public ProjectRestService getProjectRestService() {
		return projectRestService;
	}

	public ProjectVersionRestService getProjectVersionRestService() {
		return projectVersionRestService;
	}

	public ScanSummaryRestService getScanSummaryRestService() {
		return scanSummaryRestService;
	}

	public UserRestService getUserRestService() {
		return userRestService;
	}

	public VersionBomPolicyRestService getVersionBomPolicyRestService() {
		return versionBomPolicyRestService;
	}

	public VulnerabilityRestService getVulnerabilityRestService() {
		return vulnerabilityRestService;
	}
}
