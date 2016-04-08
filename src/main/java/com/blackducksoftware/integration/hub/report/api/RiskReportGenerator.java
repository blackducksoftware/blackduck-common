/*******************************************************************************
 * Black Duck Software Suite SDK
 * Copyright (C) 2016 Black Duck Software, Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *******************************************************************************/
package com.blackducksoftware.integration.hub.report.api;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.HubSupportHelper;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.logging.IntLogger;
import com.blackducksoftware.integration.hub.meta.MetaLink;
import com.blackducksoftware.integration.hub.polling.HubEventPolling;

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
		final String reportUrl = hubReportGenerationInfo.getService()
				.generateHubReport(hubReportGenerationInfo.getVersion(), ReportFormatEnum.JSON);

		final ReportInformationItem reportInfo = hubEventPolling.isReportFinishedGenerating(reportUrl, hubReportGenerationInfo.getMaximumWaitTime());

		final List<MetaLink> links = reportInfo.get_meta().getLinks();

		MetaLink contentLink = null;
		for (final MetaLink link : links) {
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

		hubReportGenerationInfo.getService().deleteHubReport(reportUrl);

		return hubRiskReportData;
	}

	public HubEventPolling getHubEventPolling(final HubIntRestService service){
		return new HubEventPolling(service);
	}

}
