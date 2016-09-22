package com.blackducksoftware.integration.hub.api.factory;

import com.blackducksoftware.integration.hub.api.CodeLocationRestService;
import com.blackducksoftware.integration.hub.api.ComponentVersionRestService;
import com.blackducksoftware.integration.hub.api.NotificationRestService;
import com.blackducksoftware.integration.hub.api.PolicyRestService;
import com.blackducksoftware.integration.hub.api.PolicyStatusRestService;
import com.blackducksoftware.integration.hub.api.ProjectRestService;
import com.blackducksoftware.integration.hub.api.ProjectVersionRestService;
import com.blackducksoftware.integration.hub.api.ScanSummaryRestService;
import com.blackducksoftware.integration.hub.api.UserRestService;
import com.blackducksoftware.integration.hub.api.VersionBomPolicyRestService;
import com.blackducksoftware.integration.hub.api.VulnerabilityRestService;
import com.blackducksoftware.integration.hub.dataservices.notification.NotificationDataService;
import com.blackducksoftware.integration.hub.dataservices.policystatus.PolicyStatusDataService;
import com.blackducksoftware.integration.hub.dataservices.scan.ScanStatusDataService;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.Gson;
import com.google.gson.JsonParser;

public class ServiceFactory {
	private RestConnection restConnection;
	private Gson gson;
	private JsonParser jsonParser;

	private CodeLocationRestService codeLocationRestService;
	private ComponentVersionRestService componentVersionRestService;
	private NotificationRestService notificationRestService;
	private PolicyRestService policyRestService;
	private PolicyStatusRestService policyStatusRestService;
	private ProjectRestService projectRestService;
	private ProjectVersionRestService projectVersionRestService;
	private ScanSummaryRestService scanSummaryRestService;
	private UserRestService userRestService;
	private VersionBomPolicyRestService versionBomPolicyRestService;
	private VulnerabilityRestService vulnerabilityRestService;

	private NotificationDataService notificationDataService;
	private PolicyStatusDataService policyStatusDataService;
	private ScanStatusDataService scanStatusDataService;

	public ServiceFactory(final RestConnection restConnection, final Gson gson, final JsonParser jsonParser) {
		this.restConnection = restConnection;
		this.gson = gson;
		this.jsonParser = jsonParser;

		this.codeLocationRestService = new CodeLocationRestService(restConnection, gson, jsonParser);
		this.componentVersionRestService = new ComponentVersionRestService(restConnection, gson, jsonParser);
		this.notificationRestService = new NotificationRestService(restConnection, gson, jsonParser);
		this.policyRestService = new PolicyRestService(restConnection, gson, jsonParser);
		this.policyStatusRestService = new PolicyStatusRestService(restConnection, gson, jsonParser);
		this.projectRestService = new ProjectRestService(restConnection, gson, jsonParser);
		this.projectVersionRestService = new ProjectVersionRestService(restConnection, gson, jsonParser);
		this.scanSummaryRestService = new ScanSummaryRestService(restConnection, gson, jsonParser);
		this.userRestService = new UserRestService(restConnection, gson, jsonParser);
		this.versionBomPolicyRestService = new VersionBomPolicyRestService(restConnection, gson, jsonParser);
		this.vulnerabilityRestService = new VulnerabilityRestService(restConnection, gson, jsonParser);

		this.notificationDataService = new NotificationDataService(restConnection, gson, jsonParser);
		this.policyStatusDataService = new PolicyStatusDataService(restConnection, gson, jsonParser, projectRestService,
				projectVersionRestService, policyStatusRestService);
		this.scanStatusDataService = new ScanStatusDataService(restConnection, gson, jsonParser, projectRestService,
				projectVersionRestService, codeLocationRestService, scanSummaryRestService);
	}

	public RestConnection getRestConnection() {
		return restConnection;
	}

	public void setRestConnection(final RestConnection restConnection) {
		this.restConnection = restConnection;
	}

	public Gson getGson() {
		return gson;
	}

	public void setGson(final Gson gson) {
		this.gson = gson;
	}

	public JsonParser getJsonParser() {
		return jsonParser;
	}

	public void setJsonParser(final JsonParser jsonParser) {
		this.jsonParser = jsonParser;
	}

	public CodeLocationRestService getCodeLocationRestService() {
		return codeLocationRestService;
	}

	public void setCodeLocationRestService(final CodeLocationRestService codeLocationRestService) {
		this.codeLocationRestService = codeLocationRestService;
	}

	public ComponentVersionRestService getComponentVersionRestService() {
		return componentVersionRestService;
	}

	public void setComponentVersionRestService(final ComponentVersionRestService componentVersionRestService) {
		this.componentVersionRestService = componentVersionRestService;
	}

	public NotificationRestService getNotificationRestService() {
		return notificationRestService;
	}

	public void setNotificationRestService(final NotificationRestService notificationRestService) {
		this.notificationRestService = notificationRestService;
	}

	public PolicyRestService getPolicyRestService() {
		return policyRestService;
	}

	public void setPolicyRestService(final PolicyRestService policyRestService) {
		this.policyRestService = policyRestService;
	}

	public PolicyStatusRestService getPolicyStatusRestService() {
		return policyStatusRestService;
	}

	public void setPolicyStatusRestService(final PolicyStatusRestService policyStatusRestService) {
		this.policyStatusRestService = policyStatusRestService;
	}

	public ProjectRestService getProjectRestService() {
		return projectRestService;
	}

	public void setProjectRestService(final ProjectRestService projectRestService) {
		this.projectRestService = projectRestService;
	}

	public ProjectVersionRestService getProjectVersionRestService() {
		return projectVersionRestService;
	}

	public void setProjectVersionRestService(final ProjectVersionRestService projectVersionRestService) {
		this.projectVersionRestService = projectVersionRestService;
	}

	public ScanSummaryRestService getScanSummaryRestService() {
		return scanSummaryRestService;
	}

	public void setScanSummaryRestService(final ScanSummaryRestService scanSummaryRestService) {
		this.scanSummaryRestService = scanSummaryRestService;
	}

	public UserRestService getUserRestService() {
		return userRestService;
	}

	public void setUserRestService(final UserRestService userRestService) {
		this.userRestService = userRestService;
	}

	public VersionBomPolicyRestService getVersionBomPolicyRestService() {
		return versionBomPolicyRestService;
	}

	public void setVersionBomPolicyRestService(final VersionBomPolicyRestService versionBomPolicyRestService) {
		this.versionBomPolicyRestService = versionBomPolicyRestService;
	}

	public VulnerabilityRestService getVulnerabilityRestService() {
		return vulnerabilityRestService;
	}

	public void setVulnerabilityRestService(final VulnerabilityRestService vulnerabilityRestService) {
		this.vulnerabilityRestService = vulnerabilityRestService;
	}

	public NotificationDataService getNotificationDataService() {
		return notificationDataService;
	}

	public void setNotificationDataService(final NotificationDataService notificationDataService) {
		this.notificationDataService = notificationDataService;
	}

	public PolicyStatusDataService getPolicyStatusDataService() {
		return policyStatusDataService;
	}

	public void setPolicyStatusDataService(final PolicyStatusDataService policyStatusDataService) {
		this.policyStatusDataService = policyStatusDataService;
	}

	public ScanStatusDataService getScanStatusDataService() {
		return scanStatusDataService;
	}

	public void setScanStatusDataService(final ScanStatusDataService scanStatusDataService) {
		this.scanStatusDataService = scanStatusDataService;
	}

}
