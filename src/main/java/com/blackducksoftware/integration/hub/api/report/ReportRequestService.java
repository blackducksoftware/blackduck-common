/**
 * Hub Common
 *
 * Copyright (C) 2017 Black Duck Software, Inc.
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
 */
package com.blackducksoftware.integration.hub.api.report;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionItem;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.request.HubRequest;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubParameterizedRequestService;
import com.blackducksoftware.integration.log.IntLogger;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ReportRequestService extends HubParameterizedRequestService<ReportInformationItem> {
    public final static long DEFAULT_TIMEOUT = 1000 * 60 * 5;

    private final IntLogger logger;

    private final MetaService metaService;

    private final long timeoutInMilliseconds;

    public ReportRequestService(final RestConnection restConnection, final IntLogger logger, final MetaService metaService) {
        this(restConnection, logger, metaService, DEFAULT_TIMEOUT);
    }

    public ReportRequestService(final RestConnection restConnection, final IntLogger logger, final MetaService metaService, final long timeoutInMilliseconds) {
        super(restConnection, ReportInformationItem.class);
        this.logger = logger;
        this.metaService = metaService;

        long timeout = timeoutInMilliseconds;
        if (timeoutInMilliseconds <= 0l) {
            timeout = DEFAULT_TIMEOUT;
            logger.alwaysLog(timeoutInMilliseconds + "ms is not a valid BOM wait time, using : " + timeout + "ms instead");
        }
        this.timeoutInMilliseconds = timeout;
    }

    /**
     * Generates a new Hub report for the specified version.
     *
     * @return the Report URL
     * @throws HubIntegrationException
     */
    public String startGeneratingHubReport(final ProjectVersionItem version, final ReportFormatEnum reportFormat, final ReportCategoriesEnum[] categories)
            throws IntegrationException {
        if (ReportFormatEnum.UNKNOWN == reportFormat) {
            throw new IllegalArgumentException("Can not generate a report of format : " + reportFormat);
        }

        final JsonObject json = new JsonObject();
        json.addProperty("reportFormat", reportFormat.name());

        if (categories != null) {
            final JsonArray categoriesJson = new JsonArray();
            for (final ReportCategoriesEnum category : categories) {
                categoriesJson.add(category.name());
            }
            json.add("categories", categoriesJson);
        }

        final HubRequest hubRequest = new HubRequest(getRestConnection());

        hubRequest.setUrl(getVersionReportLink(version));

        final String location = hubRequest.executePost(getRestConnection().getGson().toJson(json));

        return location;
    }

    public void deleteHubReport(final String reportUrl) throws IntegrationException {
        final HubRequest hubRequest = new HubRequest(getRestConnection());
        hubRequest.setUrl(reportUrl);
        hubRequest.executeDelete();
    }

    /**
     * Gets the content of the report
     *
     * @throws HubIntegrationException
     */
    public VersionReport getReportContent(final String reportContentUrl) throws IntegrationException {
        final HubRequest hubRequest = getHubRequestFactory().createRequest(reportContentUrl);

        final JsonObject json = hubRequest.executeGetForResponseJson();
        final JsonElement content = json.get("reportContent");
        final JsonArray reportConentArray = content.getAsJsonArray();
        final JsonObject reportFile = reportConentArray.get(0).getAsJsonObject();

        final VersionReport report = getRestConnection().getGson().fromJson(reportFile.get("fileContent"), VersionReport.class);

        return report;
    }

    /**
     * Checks the report URL every 5 seconds until the report has a finished
     * time available, then we know it is done being generated. Throws
     * HubIntegrationException after 30 minutes if the report has not been
     * generated yet.
     */
    public ReportInformationItem isReportFinishedGenerating(final String reportUrl)
            throws IntegrationException {
        final long startTime = System.currentTimeMillis();
        long elapsedTime = 0;
        Date timeFinished = null;
        ReportInformationItem reportInfo = null;

        while (timeFinished == null) {
            final HubRequest hubRequest = getHubRequestFactory().createRequest(reportUrl);
            reportInfo = getItem(hubRequest);
            timeFinished = reportInfo.getFinishedAt();
            if (timeFinished != null) {
                break;
            }
            if (elapsedTime >= timeoutInMilliseconds) {
                final String formattedTime = String.format("%d minutes", TimeUnit.MILLISECONDS.toMinutes(timeoutInMilliseconds));
                throw new HubIntegrationException("The Report has not finished generating in : " + formattedTime);
            }
            // Retry every 5 seconds
            try {
                Thread.sleep(5000);
            } catch (final InterruptedException e) {
                throw new HubIntegrationException("The thread waiting for the report generation was interrupted", e);
            }
            elapsedTime = System.currentTimeMillis() - startTime;
        }
        return reportInfo;
    }

    /**
     * Assumes the BOM has already been updated
     *
     * @throws HubIntegrationException
     */
    public VersionReport generateHubReport(final ProjectVersionItem version, final ReportFormatEnum reportFormat,
            final ReportCategoriesEnum[] categories) throws IntegrationException {
        logger.debug("Starting the Report generation.");
        final String reportUrl = startGeneratingHubReport(version, reportFormat, categories);

        logger.debug("Waiting for the Report to complete.");
        final ReportInformationItem reportInfo = isReportFinishedGenerating(reportUrl);

        final String contentLink = metaService.getFirstLink(reportInfo, MetaService.CONTENT_LINK);

        if (contentLink == null) {
            throw new HubIntegrationException("Could not find content link for the report at : " + reportUrl);
        }

        logger.debug("Getting the Report content.");
        final VersionReport report = getReportContent(contentLink);
        logger.debug("Finished retrieving the Report.");
        logger.debug("Cleaning up the Report on the server.");
        deleteHubReport(reportUrl);

        return report;
    }

    private String getVersionReportLink(final ProjectVersionItem version) throws HubIntegrationException {
        final String versionLink = metaService.getFirstLink(version, MetaService.VERSION_REPORT_LINK);
        return versionLink;
    }

}
