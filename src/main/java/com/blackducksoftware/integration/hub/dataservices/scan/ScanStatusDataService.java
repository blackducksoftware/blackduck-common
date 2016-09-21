package com.blackducksoftware.integration.hub.dataservices.scan;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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
import com.blackducksoftware.integration.hub.dataservices.AbstractDataService;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.EncryptionException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.exception.UnexpectedHubResponseException;
import com.blackducksoftware.integration.hub.logging.IntLogger;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.Gson;
import com.google.gson.JsonParser;

public class ScanStatusDataService extends AbstractDataService {
	private static final long FIVE_SECONDS = 5000;

	private final IntLogger logger;

	private final ProjectRestService projectRestService;
	private final ProjectVersionRestService projectVersionRestService;
	private final CodeLocationRestService codeLocationRestService;
	private final ScanSummaryRestService scanSummaryRestService;
	private final PolicyStatusRestService policyStatusRestService;

	private String mappedProjectVersionLink;
	private Set<String> scanSummariesLinks;
	private String policyStatusLink;

	public ScanStatusDataService(final RestConnection restConnection, final Gson gson, final JsonParser jsonParser,
			final IntLogger logger)
			throws IllegalArgumentException, URISyntaxException, BDRestException, EncryptionException {
		super(restConnection, gson, jsonParser);
		this.logger = logger;

		projectRestService = new ProjectRestService(restConnection, gson, jsonParser);
		projectVersionRestService = new ProjectVersionRestService(restConnection, gson, jsonParser);
		codeLocationRestService = new CodeLocationRestService(restConnection, gson, jsonParser);
		scanSummaryRestService = new ScanSummaryRestService(restConnection, gson, jsonParser);
		policyStatusRestService = new PolicyStatusRestService(restConnection, gson, jsonParser);
	}

	/**
	 * For a given group, artifact, and version, find the current
	 * PolicyStatusItem. This should be used when a Hub Scan is assumed to have
	 * started just prior to the checkPolicies invocation. Since the Hub Scan
	 * might not start right away, the method will wait at most
	 * scanStartedTimeoutInMilliseconds to find at least one pending scan.
	 *
	 * Once at least one pending scan is found, the method will wait at most
	 * scanFinishedTimeoutInMilliseconds for all the found scans to complete.
	 * Then, when all pending scans are finally complete, the policy status will
	 * be checked, which will include any changes from the completed scans.
	 *
	 * @param groupId
	 * @param artifactId
	 * @param version
	 * @param scanStartedTimeoutInMilliseconds
	 * @param scanFinishedTimeoutInMilliseconds
	 * @return PolicyStatusItem
	 * @throws IllegalArgumentException
	 * @throws URISyntaxException
	 * @throws BDRestException
	 * @throws EncryptionException
	 * @throws IOException
	 * @throws UnexpectedHubResponseException
	 * @throws InterruptedException
	 * @throws HubIntegrationException
	 */
	public PolicyStatusItem checkPolicies(final String groupId, final String artifactId, final String version,
			final long scanStartedTimeoutInMilliseconds, final long scanFinishedTimeoutInMilliseconds)
			throws IllegalArgumentException, URISyntaxException, BDRestException, EncryptionException, IOException,
			UnexpectedHubResponseException, InterruptedException, HubIntegrationException {
		populateApplicableScansAndProjectVersions(groupId, artifactId, version);

		final List<ScanSummaryItem> pendingScanSummaries = getPendingScanSummaries(scanStartedTimeoutInMilliseconds);
		final ScanStatusChecker scanStatusChecker = new ScanStatusChecker(logger, scanSummaryRestService,
				pendingScanSummaries, scanFinishedTimeoutInMilliseconds);
		scanStatusChecker.waitForCompleteScans();

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

	private List<ScanSummaryItem> getPendingScanSummaries() throws IOException, URISyntaxException, BDRestException {
		final List<ScanSummaryItem> allScanSummaries = new ArrayList<>();
		for (final String scanSummaryLink : scanSummariesLinks) {
			allScanSummaries.addAll(scanSummaryRestService.getAllScanSummaryItems(scanSummaryLink));
		}

		final List<ScanSummaryItem> pendingScanSummaries = new ArrayList<>();
		for (final ScanSummaryItem scanSummaryItem : allScanSummaries) {
			if (scanSummaryItem.getStatus().isPending()) {
				pendingScanSummaries.add(scanSummaryItem);
			}
		}

		return pendingScanSummaries;
	}

	private List<ScanSummaryItem> getPendingScanSummaries(final long scanStartedTimeoutInMilliseconds)
			throws IOException, URISyntaxException, BDRestException, InterruptedException, HubIntegrationException {
		List<ScanSummaryItem> pendingScanSummaries = getPendingScanSummaries();
		final long startedTime = System.currentTimeMillis();
		while (!done(pendingScanSummaries, scanStartedTimeoutInMilliseconds, startedTime)) {
			// perhaps the Hub has not started the scan yet - since we expect
			// one to have started, let's wait a bit
			Thread.sleep(FIVE_SECONDS);
			pendingScanSummaries = getPendingScanSummaries();
		}

		return pendingScanSummaries;
	}

	private boolean done(final List<ScanSummaryItem> pendingScanSummaries, final long timeoutInMilliseconds,
			final long startedTime) throws HubIntegrationException {
		if (pendingScanSummaries.size() > 0) {
			return true;
		}

		if (takenTooLong(timeoutInMilliseconds, startedTime)) {
			logger.info("the hub scan took too long to start...");
			final String formattedTime = String.format("%d minutes",
					TimeUnit.MILLISECONDS.toMinutes(timeoutInMilliseconds));
			throw new HubIntegrationException(
					"The Scan has not started within the specified wait time : " + formattedTime);
		}

		return false;
	}

	private boolean takenTooLong(final long timeoutInMilliseconds, final long startedTime) {
		final long elapsed = System.currentTimeMillis() - startedTime;
		return elapsed > timeoutInMilliseconds;
	}

}
