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

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.view.MetaHandler;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.model.enumeration.ReportFormatEnum;
import com.blackducksoftware.integration.hub.model.enumeration.ReportTypeEnum;
import com.blackducksoftware.integration.hub.model.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.model.view.ReportView;
import com.blackducksoftware.integration.hub.request.HubRequest;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.rest.exception.IntegrationRestException;
import com.blackducksoftware.integration.hub.service.HubService;
import com.blackducksoftware.integration.log.IntLogger;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import okhttp3.Response;

public class ReportService extends HubService {
    public final static long DEFAULT_TIMEOUT = 1000 * 60 * 5;

    private final IntLogger logger;
    private final long timeoutInMilliseconds;

    public ReportService(final RestConnection restConnection, final IntLogger logger) {
        this(restConnection, logger, DEFAULT_TIMEOUT);
    }

    public ReportService(final RestConnection restConnection, final IntLogger logger, final long timeoutInMilliseconds) {
        super(restConnection);
        this.logger = logger;

        long timeout = timeoutInMilliseconds;
        if (timeoutInMilliseconds <= 0l) {
            timeout = DEFAULT_TIMEOUT;
            logger.alwaysLog(timeoutInMilliseconds + "ms is not a valid BOM wait time, using : " + timeout + "ms instead");
        }
        this.timeoutInMilliseconds = timeout;
    }

    public String startGeneratingHubReport(final ProjectVersionView version, final ReportFormatEnum reportFormat, final ReportCategoriesEnum[] categories) throws IntegrationException {
        return startGeneratingHubReport(getFirstLink(version, MetaHandler.VERSION_REPORT_LINK), ReportTypeEnum.VERSION, reportFormat, categories);
    }

    public String startGeneratingHubNoticesReport(final ProjectVersionView version, final ReportFormatEnum reportFormat) throws IntegrationException {
        return startGeneratingHubReport(getFirstLink(version, MetaHandler.VERSION_NOTICES_REPORT_LINK), ReportTypeEnum.VERSION_LICENSE, reportFormat, null);
    }

    private String startGeneratingHubReport(final String reportUrl, final ReportTypeEnum reportType, final ReportFormatEnum reportFormat, final ReportCategoriesEnum[] categories) throws IntegrationException {
        final JsonObject json = new JsonObject();
        json.addProperty("reportFormat", reportFormat.toString());
        json.addProperty("reportType", reportType.toString());

        if (categories != null) {
            final JsonArray categoriesJson = new JsonArray();
            for (final ReportCategoriesEnum category : categories) {
                categoriesJson.add(category.name());
            }
            json.add("categories", categoriesJson);
        }

        final HubRequest hubRequest = getHubRequestFactory().createRequest(reportUrl);
        Response response = null;
        try {
            response = hubRequest.executePost(getGson().toJson(json));
            return response.header("location");
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    public void deleteHubReport(final String reportUrl) throws IntegrationException {
        final HubRequest hubRequest = getHubRequestFactory().createRequest(reportUrl);
        hubRequest.executeDelete();
    }

    public VersionReport getReportContent(final String reportContentUrl) throws IntegrationException {
        final JsonElement fileContent = getReportContentJson(reportContentUrl);
        final VersionReport report = getGson().fromJson(fileContent, VersionReport.class);
        return report;
    }

    public String getNoticesReportContent(final String reportContentUrl) throws IntegrationException {
        final JsonElement fileContent = getReportContentJson(reportContentUrl);
        return fileContent.getAsString();
    }

    private JsonElement getReportContentJson(final String reportContentUrl) throws IntegrationException {
        final HubRequest hubRequest = getHubRequestFactory().createRequest(reportContentUrl);
        Response response = null;
        try {
            response = hubRequest.executeGet();
            final String jsonResponse = response.body().string();

            final JsonObject json = getJsonParser().parse(jsonResponse).getAsJsonObject();
            final JsonElement content = json.get("reportContent");
            final JsonArray reportConentArray = content.getAsJsonArray();
            final JsonObject reportFile = reportConentArray.get(0).getAsJsonObject();
            return reportFile.get("fileContent");
        } catch (final IOException e) {
            throw new HubIntegrationException(e);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    /**
     * Checks the report URL every 5 seconds until the report has a finished time available, then we know it is done being generated. Throws HubIntegrationException after 30 minutes if the report has not been generated yet.
     */
    public ReportView isReportFinishedGenerating(final String reportUrl) throws IntegrationException {
        final long startTime = System.currentTimeMillis();
        long elapsedTime = 0;
        Date timeFinished = null;
        ReportView reportInfo = null;

        while (timeFinished == null) {
            final HubRequest hubRequest = getHubRequestFactory().createRequest(reportUrl);
            Response response = null;
            try {
                response = hubRequest.executeGet();
                final String jsonResponse = response.body().string();
                reportInfo = getViewAs(jsonResponse, ReportView.class);
            } catch (final IOException e) {
                throw new HubIntegrationException(e);
            } finally {
                if (response != null) {
                    response.close();
                }
            }
            timeFinished = reportInfo.finishedAt;
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
     */
    public VersionReport generateHubReport(final ProjectVersionView version, final ReportFormatEnum reportFormat, final ReportCategoriesEnum[] categories) throws IntegrationException {
        logger.debug("Starting the Report generation.");
        final String reportUrl = startGeneratingHubReport(version, reportFormat, categories);

        logger.debug("Waiting for the Report to complete.");
        final ReportView reportInfo = isReportFinishedGenerating(reportUrl);

        final String contentLink = getFirstLink(reportInfo, MetaHandler.CONTENT_LINK);

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

    /**
     * Assumes the BOM has already been updated
     *
     */
    public String generateHubNoticesReport(final ProjectVersionView version, final ReportFormatEnum reportFormat) throws IntegrationException {
        if (hasLink(version, MetaHandler.VERSION_NOTICES_REPORT_LINK)) {
            try {
                logger.debug("Starting the Notices Report generation.");
                final String reportUrl = startGeneratingHubNoticesReport(version, reportFormat);

                logger.debug("Waiting for the Notices Report to complete.");
                final ReportView reportInfo = isReportFinishedGenerating(reportUrl);

                final String contentLink = getFirstLink(reportInfo, MetaHandler.CONTENT_LINK);

                if (contentLink == null) {
                    throw new HubIntegrationException("Could not find content link for the report at : " + reportUrl);
                }

                logger.debug("Getting the Notices Report content.");
                final String noticesReport = getNoticesReportContent(contentLink);
                logger.debug("Finished retrieving the Notices Report.");
                logger.debug("Cleaning up the Notices Report on the server.");
                deleteHubReport(reportUrl);
                return noticesReport;
            } catch (final IntegrationRestException e) {
                if (e.getHttpStatusCode() == 402) {
                    // unlike the policy module, the licenseReports link is still present when the module is not enabled
                    logger.warn("Can not create the notice report, the Hub notice module is not enabled.");
                } else {
                    throw e;
                }
            }
        } else {
            logger.warn("Can not create the notice report, the Hub notice module is not enabled.");
        }
        return null;
    }

}
