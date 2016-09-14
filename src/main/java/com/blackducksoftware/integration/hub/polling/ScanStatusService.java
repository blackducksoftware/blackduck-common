package com.blackducksoftware.integration.hub.polling;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.hub.api.CodeLocationRestService;
import com.blackducksoftware.integration.hub.api.PolicyStatusRestService;
import com.blackducksoftware.integration.hub.api.ProjectRestService;
import com.blackducksoftware.integration.hub.api.ProjectVersionRestService;
import com.blackducksoftware.integration.hub.api.ScanSummaryRestService;
import com.blackducksoftware.integration.hub.api.codelocation.CodeLocationItem;
import com.blackducksoftware.integration.hub.api.policy.PolicyStatusItem;
import com.blackducksoftware.integration.hub.api.project.ProjectItem;
import com.blackducksoftware.integration.hub.api.scan.ScanSummaryItem;
import com.blackducksoftware.integration.hub.api.version.ReleaseItem;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.EncryptionException;
import com.blackducksoftware.integration.hub.exception.UnexpectedHubResponseException;
import com.blackducksoftware.integration.hub.global.HubProxyInfo;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.logging.IntLogger;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.Gson;
import com.google.gson.JsonParser;

public class ScanStatusService {
	private static final long FIVE_SECONDS = 5 * 1000;

	private final IntLogger logger;

	private final ProjectRestService projectRestService;
	private final ProjectVersionRestService projectVersionRestService;
	private final CodeLocationRestService codeLocationRestService;
	private final ScanSummaryRestService scanSummaryRestService;
	private final PolicyStatusRestService policyStatusRestService;

	private String mappedProjectVersionLink;
	private Set<String> scanSummariesLinks;
	private String policyStatusLink;

	public ScanStatusService(final HubServerConfig hubServerConfig, final IntLogger logger)
			throws IllegalArgumentException, URISyntaxException, BDRestException, EncryptionException {
		this.logger = logger;

		final RestConnection restConnection = new RestConnection(hubServerConfig.getHubUrl().toString());
		final HubProxyInfo proxyInfo = hubServerConfig.getProxyInfo();
		if (proxyInfo.shouldUseProxyForUrl(hubServerConfig.getHubUrl())) {
			restConnection.setProxyProperties(proxyInfo);
		}

		restConnection.setCookies(hubServerConfig.getGlobalCredentials().getUsername(),
				hubServerConfig.getGlobalCredentials().getDecryptedPassword());

		final Gson gson = new Gson();
		final JsonParser jsonParser = new JsonParser();
		projectRestService = new ProjectRestService(restConnection, gson, jsonParser);
		projectVersionRestService = new ProjectVersionRestService(restConnection, gson, jsonParser);
		codeLocationRestService = new CodeLocationRestService(restConnection, gson, jsonParser);
		scanSummaryRestService = new ScanSummaryRestService(restConnection, gson, jsonParser);
		policyStatusRestService = new PolicyStatusRestService(restConnection, gson, jsonParser);
	}

	public PolicyStatusItem waitForCompleteScanStatus(final String groupId, final String artifactId,
			final String version, final int timeoutInSeconds) throws IllegalArgumentException, URISyntaxException,
			BDRestException, EncryptionException, IOException, UnexpectedHubResponseException, InterruptedException {
		populateApplicableScansAndProjectVersions(groupId, artifactId, version);

		final List<ScanSummaryItem> scanSummaries = new ArrayList<>();
		for (final String scanSummaryLink : scanSummariesLinks) {
			scanSummaries.addAll(scanSummaryRestService.getAllScanSummaryItems(scanSummaryLink));
		}

		final List<ScanSummaryItem> pendingScanSummaries = new ArrayList<>();
		for (final ScanSummaryItem scanSummaryItem : scanSummaries) {
			if (scanSummaryItem.getStatus().isPending()) {
				pendingScanSummaries.add(scanSummaryItem);
			}
		}

		waitUntilPendingScansAreComplete(pendingScanSummaries, timeoutInSeconds, System.currentTimeMillis());

		final PolicyStatusItem policyStatusItem = policyStatusRestService.getItem(policyStatusLink);
		return policyStatusItem;
	}

	private void populateApplicableScansAndProjectVersions(final String groupId, final String artifactId,
			final String version)
			throws IOException, BDRestException, URISyntaxException, UnexpectedHubResponseException {
		final Map<String, Set<String>> mappedProjectVersionToScanSummariesLinks = new HashMap<>();
		final List<CodeLocationItem> codeLocationItems = codeLocationRestService.getImportedCodeLocations(groupId,
				artifactId, version);
		for (final CodeLocationItem codeLocationItem : codeLocationItems) {
			final String scanSummariesLink = codeLocationItem.getLink("scans");
			final String mappedProjectVersionLink = codeLocationItem.getMappedProjectVersion();
			if (!mappedProjectVersionToScanSummariesLinks.containsKey(mappedProjectVersionLink)) {
				mappedProjectVersionToScanSummariesLinks.put(mappedProjectVersionLink, new HashSet<String>());
			}

			mappedProjectVersionToScanSummariesLinks.get(mappedProjectVersionLink).add(scanSummariesLink);
		}

		mappedProjectVersionLink = determineHubProjectAndVersionLink(mappedProjectVersionToScanSummariesLinks.keySet(),
				artifactId, version);
		if (StringUtils.isBlank(mappedProjectVersionLink)) {
			throw new UnexpectedHubResponseException(
					String.format("Couldn't find a mapped project/version for: %s %s.", artifactId, version));
		}

		scanSummariesLinks = mappedProjectVersionToScanSummariesLinks.get(mappedProjectVersionLink);
	}

	private void waitUntilPendingScansAreComplete(final List<ScanSummaryItem> pendingScanSummaries,
			final int timeoutInSeconds, final long startedTime)
			throws InterruptedException, IOException, BDRestException, URISyntaxException {
		if (pendingScanSummaries.isEmpty()) {
			return;
		}

		if (takenTooLong(timeoutInSeconds, startedTime)) {
			logger.info("the hub processing took too long...");
			return;
		}

		Thread.sleep(FIVE_SECONDS);
		final List<ScanSummaryItem> currentPendingScanSummaries = new ArrayList<>();
		for (final ScanSummaryItem scanSummaryItem : pendingScanSummaries) {
			logger.info("waiting for scan: " + scanSummaryItem);
			final String scanSummaryLink = scanSummaryItem.getMeta().getHref();
			final ScanSummaryItem currentScanSummaryItem = scanSummaryRestService.getItem(scanSummaryLink);
			if (currentScanSummaryItem.getStatus().isPending()) {
				currentPendingScanSummaries.add(currentScanSummaryItem);
			}
		}

		waitUntilPendingScansAreComplete(currentPendingScanSummaries, timeoutInSeconds, startedTime);
	}

	private boolean takenTooLong(final int timeoutInSeconds, final long startedTime) {
		final double timeout = timeoutInSeconds * 1.00;
		final double elapsed = (System.currentTimeMillis() - startedTime) / 1000.00;
		return elapsed > timeout;
	}

	private String determineHubProjectAndVersionLink(final Set<String> mappedProjectVersionLinks,
			final String hubProjectName, final String hubProjectVersion)
			throws IOException, BDRestException, URISyntaxException, UnexpectedHubResponseException {
		for (final String mappedProjectVersionLink : mappedProjectVersionLinks) {
			final ReleaseItem releaseItem = projectVersionRestService.getItem(mappedProjectVersionLink);
			if (hubProjectVersion.equals(releaseItem.getVersionName())) {
				final ProjectItem projectItem = projectRestService.getItem(releaseItem.getLink("project"));
				if (hubProjectName.equals(projectItem.getName())) {
					policyStatusLink = releaseItem.getLink("policy-status");
					return mappedProjectVersionLink;
				}
			}
		}

		return null;
	}

}
