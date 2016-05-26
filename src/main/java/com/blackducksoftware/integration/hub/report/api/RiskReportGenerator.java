/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
