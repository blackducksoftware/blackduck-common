package com.blackducksoftware.integration.hub.report.api;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.HubSupportHelper;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.logging.IntLogger;
import com.blackducksoftware.integration.hub.polling.HubEventPolling;
import com.blackducksoftware.integration.hub.report.api.ReportMetaInformationItem.ReportMetaLinkItem;

public class RiskReportGenerator {
	private final HubReportGenerationInfo hubReportGenerationInfo;

	private final HubSupportHelper supportHelper;

	/**
	 * Make sure supportHelper.checkHubSupport() has already been run before passing in the supportHelper.
	 *
	 */
	public RiskReportGenerator(final HubReportGenerationInfo hubReportGenerationInfo, final HubSupportHelper supportHelper) {
		this.hubReportGenerationInfo = hubReportGenerationInfo;
		this.supportHelper = supportHelper;
	}

	public HubRiskReportData generateHubReport(final IntLogger logger) throws IOException, BDRestException, URISyntaxException, InterruptedException,
	HubIntegrationException {
		logger.debug("Waiting for the bom to be updated with the scan results.");
		final HubEventPolling hubEventPolling = getHubEventPolling(hubReportGenerationInfo.getService());

		if (supportHelper.isCliStatusDirOptionSupport()) {
			hubEventPolling.assertBomUpToDate(hubReportGenerationInfo, logger);
		} else {
			hubEventPolling.assertBomUpToDate(hubReportGenerationInfo);
		}

		logger.debug("The bom has been updated, generating the report.");
		final String reportUrl = hubReportGenerationInfo.getService().generateHubReport(hubReportGenerationInfo.getVersionId(), ReportFormatEnum.JSON);

		final ReportMetaInformationItem reportInfo = hubEventPolling.isReportFinishedGenerating(reportUrl, hubReportGenerationInfo.getMaximumWaitTime());

		final List<ReportMetaLinkItem> links = reportInfo.get_meta().getLinks();

		ReportMetaLinkItem contentLink = null;
		for (final ReportMetaLinkItem link : links) {
			if (link.getRel().equalsIgnoreCase("content")) {
				contentLink = link;
				break;
			}
		}
		if (contentLink == null) {
			throw new HubIntegrationException("Could not find content link for the report at : " + reportUrl);
		}

		final HubRiskReportData hubRiskReportData = new HubRiskReportData();
		final VersionReport report = hubReportGenerationInfo.getService().getReportContent(contentLink.getHref());
		hubRiskReportData.setReport(report);
		logger.debug("Finished retrieving the report.");

		hubReportGenerationInfo.getService().deleteHubReport(hubReportGenerationInfo.getVersionId(),
				hubReportGenerationInfo.getService().getReportIdFromReportUrl(reportUrl));

		return hubRiskReportData;
	}

	public HubEventPolling getHubEventPolling(final HubIntRestService service){
		return new HubEventPolling(service);
	}

}
