package com.blackducksoftware.integration.hub.dataservices;

import com.blackducksoftware.integration.hub.api.BomImportRestService;
import com.blackducksoftware.integration.hub.api.CodeLocationRestService;
import com.blackducksoftware.integration.hub.api.PolicyStatusRestService;
import com.blackducksoftware.integration.hub.api.ProjectRestService;
import com.blackducksoftware.integration.hub.api.ProjectVersionRestService;
import com.blackducksoftware.integration.hub.api.ScanSummaryRestService;
import com.blackducksoftware.integration.hub.dataservices.notification.NotificationDataService;
import com.blackducksoftware.integration.hub.dataservices.notification.items.PolicyNotificationFilter;
import com.blackducksoftware.integration.hub.dataservices.policystatus.PolicyStatusDataService;
import com.blackducksoftware.integration.hub.dataservices.scan.ScanStatusDataService;
import com.blackducksoftware.integration.hub.logging.IntLogger;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.Gson;
import com.google.gson.JsonParser;

public class DataServicesFactory {
	private final RestConnection restConnection;
	final Gson gson = new Gson();
	final JsonParser jsonParser = new JsonParser();

	private final ProjectRestService projectRestService;
	private final ProjectVersionRestService projectVersionRestService;
	private final PolicyStatusRestService policyStatusRestService;
	private final CodeLocationRestService codeLocationRestService;
	private final ScanSummaryRestService scanSummaryRestService;
	private final BomImportRestService bomImportRestService;

	public DataServicesFactory(final RestConnection restConnection) {
		this.restConnection = restConnection;

		projectRestService = new ProjectRestService(restConnection, gson, jsonParser);
		projectVersionRestService = new ProjectVersionRestService(restConnection, gson, jsonParser);
		policyStatusRestService = new PolicyStatusRestService(restConnection, gson, jsonParser);
		codeLocationRestService = new CodeLocationRestService(restConnection, gson, jsonParser);
		scanSummaryRestService = new ScanSummaryRestService(restConnection, gson, jsonParser);
		bomImportRestService = new BomImportRestService(restConnection);
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

	public RestConnection getRestConnection() {
		return restConnection;
	}

	public Gson getGson() {
		return gson;
	}

	public JsonParser getJsonParser() {
		return jsonParser;
	}

	public ProjectRestService getProjectRestService() {
		return projectRestService;
	}

	public ProjectVersionRestService getProjectVersionRestService() {
		return projectVersionRestService;
	}

	public PolicyStatusRestService getPolicyStatusRestService() {
		return policyStatusRestService;
	}

	public CodeLocationRestService getCodeLocationRestService() {
		return codeLocationRestService;
	}

	public ScanSummaryRestService getScanSummaryRestService() {
		return scanSummaryRestService;
	}

	public BomImportRestService getBomImportRestService() {
		return bomImportRestService;
	}

}
