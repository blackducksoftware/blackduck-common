/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
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
 *******************************************************************************/
package com.blackducksoftware.integration.hub.api.report;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.representation.StringRepresentation;

import com.blackducksoftware.integration.hub.api.HubItemRestService;
import com.blackducksoftware.integration.hub.api.HubRequest;
import com.blackducksoftware.integration.hub.api.project.ProjectItem;
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionItem;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.exception.ResourceDoesNotExistException;
import com.blackducksoftware.integration.hub.exception.UnexpectedHubResponseException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

public class ReportRestService extends HubItemRestService<ReportInformationItem> {
    private static final Type ITEM_TYPE = new TypeToken<ReportInformationItem>() {
    }.getType();

    private static final Type ITEM_LIST_TYPE = new TypeToken<List<ReportInformationItem>>() {
    }.getType();

    public ReportRestService(final RestConnection restConnection, final Gson gson,
            final JsonParser jsonParser) {
        super(restConnection, gson, jsonParser, ITEM_TYPE, ITEM_LIST_TYPE);
    }

    /**
     * Generates a new Hub report for the specified version.
     *
     * @return the Report URL
     *
     */
    public String generateHubReport(final ProjectVersionItem version, final ReportFormatEnum reportFormat,
            final ReportCategoriesEnum[] categories)
            throws IOException, BDRestException, URISyntaxException, UnexpectedHubResponseException {
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

        final HubRequest hubRequest = new HubRequest(getRestConnection(), getJsonParser());

        hubRequest.setMethod(Method.POST);
        hubRequest.setLimit(HubRequest.EXCLUDE_INTEGER_QUERY_PARAMETER);
        hubRequest.setUrl(getVersionReportLink(version));

        final StringRepresentation stringRep = new StringRepresentation(getGson().toJson(json));
        stringRep.setMediaType(MediaType.APPLICATION_JSON);
        stringRep.setCharacterSet(CharacterSet.UTF_8);
        String location = null;
        try {
            location = hubRequest.executePost(stringRep);
        } catch (final ResourceDoesNotExistException ex) {
            throw new BDRestException("There was a problem generating a report for this Version.", ex,
                    ex.getResource());
        }

        return location;
    }

    public void deleteHubReport(final String reportUrl) throws IOException, BDRestException, URISyntaxException {
        final HubRequest hubRequest = new HubRequest(getRestConnection(), getJsonParser());
        hubRequest.setMethod(Method.DELETE);
        hubRequest.setLimit(HubRequest.EXCLUDE_INTEGER_QUERY_PARAMETER);
        hubRequest.setUrl(reportUrl);

        hubRequest.executeDelete();
    }

    /**
     * Gets the content of the report
     *
     */
    public VersionReport getReportContent(final String reportContentUrl)
            throws IOException, BDRestException, URISyntaxException {
        final HubRequest hubRequest = new HubRequest(getRestConnection(), getJsonParser());
        hubRequest.setMethod(Method.GET);
        hubRequest.setLimit(HubRequest.EXCLUDE_INTEGER_QUERY_PARAMETER);
        hubRequest.setUrl(reportContentUrl);

        final JsonObject json = hubRequest.executeForResponseJson();
        final JsonElement content = json.get("reportContent");
        final JsonArray reportConentArray = content.getAsJsonArray();
        final JsonObject reportFile = reportConentArray.get(0).getAsJsonObject();

        final VersionReport report = getGson().fromJson(reportFile.get("fileContent"), VersionReport.class);

        return report;

    }

    /**
     * Checks the report URL every 5 seconds until the report has a finished
     * time available, then we know it is done being generated. Throws
     * HubIntegrationException after 30 minutes if the report has not been
     * generated yet.
     *
     */
    public ReportInformationItem isReportFinishedGenerating(final String reportUrl)
            throws IOException, BDRestException, URISyntaxException, InterruptedException, HubIntegrationException {
        // maximum wait time of 30 minutes
        final long maximumWait = 1000 * 60 * 30;
        return isReportFinishedGenerating(reportUrl, maximumWait);
    }

    /**
     * Checks the report URL every 5 seconds until the report has a finished
     * time available, then we know it is done being generated. Throws
     * HubIntegrationException after the maximum wait if the report has not been
     * generated yet.
     *
     */
    public ReportInformationItem isReportFinishedGenerating(final String reportUrl, final long maximumWait)
            throws IOException, BDRestException, URISyntaxException, InterruptedException, HubIntegrationException {
        final long startTime = System.currentTimeMillis();
        long elapsedTime = 0;
        String timeFinished = null;
        ReportInformationItem reportInfo = null;

        while (timeFinished == null) {
            reportInfo = getItem(reportUrl);
            timeFinished = reportInfo.getFinishedAt();
            if (timeFinished != null) {
                break;
            }
            if (elapsedTime >= maximumWait) {
                final String formattedTime = String.format("%d minutes", TimeUnit.MILLISECONDS.toMinutes(maximumWait));
                throw new HubIntegrationException("The Report has not finished generating in : " + formattedTime);
            }
            // Retry every 5 seconds
            Thread.sleep(5000);
            elapsedTime = System.currentTimeMillis() - startTime;
        }
        return reportInfo;
    }

    private String getVersionReportLink(final ProjectVersionItem version) throws UnexpectedHubResponseException {
        final List<String> versionLinks = version.getLinks(ProjectVersionItem.VERSION_REPORT_LINK);
        if (versionLinks.size() != 1) {
            throw new UnexpectedHubResponseException("The release " + version.getVersionName() + " has "
                    + versionLinks.size() + " " + ProjectItem.VERSION_LINK + " links; expected one");
        }
        final String versionLink = versionLinks.get(0);
        return versionLink;
    }
}
