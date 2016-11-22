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
package com.blackducksoftware.integration.hub;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;

import com.blackducksoftware.integration.hub.api.HubServicesFactory;
import com.blackducksoftware.integration.hub.api.policy.PolicyStatusItem;
import com.blackducksoftware.integration.hub.api.project.ProjectItem;
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionItem;
import com.blackducksoftware.integration.hub.api.report.ReportCategoriesEnum;
import com.blackducksoftware.integration.hub.api.report.ReportFormatEnum;
import com.blackducksoftware.integration.hub.api.report.ReportInformationItem;
import com.blackducksoftware.integration.hub.api.report.VersionReport;
import com.blackducksoftware.integration.hub.api.scan.ScanSummaryItem;
import com.blackducksoftware.integration.hub.api.version.DistributionEnum;
import com.blackducksoftware.integration.hub.api.version.PhaseEnum;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.ProjectDoesNotExistException;
import com.blackducksoftware.integration.hub.exception.ResourceDoesNotExistException;
import com.blackducksoftware.integration.hub.exception.UnexpectedHubResponseException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

public class HubIntRestService {
    private HubServicesFactory hubServicesFactory;

    public HubIntRestService(final RestConnection restConnection) throws URISyntaxException {
        this.hubServicesFactory = new HubServicesFactory(restConnection);
    }

    /**
     * Retrieves a list of Hub Projects that may match the hubProjectName
     *
     */
    public List<ProjectItem> getProjectMatches(final String projectName)
            throws IOException, BDRestException, URISyntaxException {
        return hubServicesFactory.createProjectRestService().getAllProjectMatches(projectName);
    }

    /**
     * Retrieves a list of Hub Projects that may match the hubProjectName,
     * default limit is 10
     *
     */
    public List<ProjectItem> getProjectMatches(final String projectName, final int limit)
            throws IOException, BDRestException, URISyntaxException {
        return hubServicesFactory.createProjectRestService().getProjectMatches(projectName, limit);
    }

    /**
     * Gets the Project that is specified by the projectName
     *
     */
    public ProjectItem getProjectByName(final String projectName)
            throws IOException, BDRestException, URISyntaxException, ProjectDoesNotExistException {
        return hubServicesFactory.createProjectRestService().getProjectByName(projectName);
    }

    public ProjectItem getProject(final String projectUrl) throws IOException, BDRestException, URISyntaxException {
        return hubServicesFactory.createProjectRestService().getItem(projectUrl);
    }

    public ProjectVersionItem getProjectVersion(final String versionUrl)
            throws IOException, BDRestException, URISyntaxException {
        return hubServicesFactory.createProjectVersionRestService().getItem(versionUrl);
    }

    /**
     * Gets the list of Versions for the specified Project
     *
     */
    public ProjectVersionItem getVersion(final ProjectItem project, final String versionName) throws IOException,
            BDRestException, URISyntaxException, UnexpectedHubResponseException {
        return hubServicesFactory.createProjectVersionRestService().getProjectVersion(project, versionName);
    }

    public List<ProjectVersionItem> getProjectVersionsForProject(final ProjectItem project)
            throws UnexpectedHubResponseException, IOException, URISyntaxException, BDRestException {
        return hubServicesFactory.createProjectVersionRestService().getAllProjectVersions(project);
    }

    private String getVersionReportLink(final ProjectVersionItem version) throws UnexpectedHubResponseException {
        final List<String> versionLinks = version.getLinks("versionReport");
        if (versionLinks.size() != 1) {
            throw new UnexpectedHubResponseException("The release " + version.getVersionName() + " has "
                    + versionLinks.size() + " " + ProjectItem.VERSION_LINK + " links; expected one");
        }
        final String versionLink = versionLinks.get(0);
        return versionLink;
    }

    /**
     * Creates a Hub Project with the specified name.
     *
     * @return the project URL.
     */
    public String createHubProject(final String projectName) throws IOException, BDRestException, URISyntaxException {
        return hubServicesFactory.createProjectRestService().createHubProject(projectName);
    }

    public void deleteHubProject(final ProjectItem project) throws IOException, BDRestException, URISyntaxException {
        String projectUrl = project.getMeta().getHref();
        deleteHubProject(projectUrl);
    }

    /**
     * Delete HubProject. For test purposes only!
     *
     * @throws URISyntaxException
     * @throws BDRestException
     * @throws IOException
     *
     */
    public void deleteHubProject(final String projectUrl) throws IOException, BDRestException, URISyntaxException {
        hubServicesFactory.createProjectRestService().deleteItem(projectUrl);
    }

    /**
     * Creates a new Version in the Project specified, using the phase and
     * distribution provided.
     *
     * @return the version URL
     *
     */
    public String createHubVersion(final ProjectItem project, final String versionName, final PhaseEnum phase,
            final DistributionEnum dist) throws IOException, BDRestException, URISyntaxException, UnexpectedHubResponseException {

        return hubServicesFactory.createProjectVersionRestService().createHubVersion(project, versionName, phase, dist);
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

        final StringRepresentation stringRep = new StringRepresentation(hubServicesFactory.getRestConnection().getGson().toJson(json));
        stringRep.setMediaType(MediaType.APPLICATION_JSON);
        stringRep.setCharacterSet(CharacterSet.UTF_8);
        String location = null;
        try {
            location = hubServicesFactory.getRestConnection().httpPostFromAbsoluteUrl(getVersionReportLink(version), stringRep);
        } catch (final ResourceDoesNotExistException ex) {
            throw new BDRestException("There was a problem generating a report for this Version.", ex,
                    ex.getResource());
        }

        return location;
    }

    public int deleteHubReport(final String reportUrl) throws IOException, BDRestException, URISyntaxException {
        final ClientResource resource = hubServicesFactory.getRestConnection().createClientResource(reportUrl);
        try {
            resource.setMethod(Method.DELETE);
            hubServicesFactory.getRestConnection().handleRequest(resource);

            final int responseCode = resource.getResponse().getStatus().getCode();
            if (!hubServicesFactory.getRestConnection().isSuccess(responseCode)) {
                throw new BDRestException("There was a problem deleting this report. Error Code: " + responseCode,
                        resource);
            }
            return responseCode;
        } finally {
            releaseResource(resource);
        }
    }

    public ReportInformationItem getReportInformation(final String reportUrl)
            throws IOException, BDRestException, URISyntaxException {
        final ClientResource resource = hubServicesFactory.getRestConnection().createClientResource(reportUrl);
        try {
            // Restlet 2.3.3 and lower use
            // @SuppressWarnings("unchecked")
            // Series<Header> requestHeaders = (Series<Header>) resource.getRequestAttributes()
            // .get(HeaderConstants.ATTRIBUTE_HEADERS);
            // if (requestHeaders == null) {
            // requestHeaders = new Series<>(Header.class);
            // resource.getRequestAttributes().put(HeaderConstants.ATTRIBUTE_HEADERS, requestHeaders);
            // }
            // requestHeaders.add(new Header("Accept", MediaType.APPLICATION_JSON.toString()));

            // Restlet 2.3.4 and higher
            resource.accept(MediaType.APPLICATION_JSON);

            resource.setMethod(Method.GET);

            hubServicesFactory.getRestConnection().handleRequest(resource);
            final int responseCode = resource.getResponse().getStatus().getCode();

            if (hubServicesFactory.getRestConnection().isSuccess(responseCode)) {
                final String response = hubServicesFactory.getRestConnection().readResponseAsString(resource.getResponse());
                return hubServicesFactory.getRestConnection().getGson().fromJson(response, ReportInformationItem.class);
            } else {
                throw new BDRestException(
                        "There was a problem getting the links for the specified report. Error Code: " + responseCode,
                        resource);
            }
        } finally {
            releaseResource(resource);
        }
    }

    /**
     * Gets the content of the report
     *
     */
    public VersionReport getReportContent(final String reportContentUrl)
            throws IOException, BDRestException, URISyntaxException {
        final ClientResource resource = hubServicesFactory.getRestConnection().createClientResource(reportContentUrl);
        try {
            resource.setMethod(Method.GET);

            hubServicesFactory.getRestConnection().handleRequest(resource);
            final int responseCode = resource.getResponse().getStatus().getCode();

            if (hubServicesFactory.getRestConnection().isSuccess(responseCode)) {
                final String response = hubServicesFactory.getRestConnection().readResponseAsString(resource.getResponse());

                final JsonObject json = hubServicesFactory.getRestConnection().getJsonParser().parse(response).getAsJsonObject();
                final JsonElement content = json.get("reportContent");
                final JsonArray reportConentArray = content.getAsJsonArray();
                final JsonObject reportFile = reportConentArray.get(0).getAsJsonObject();

                final VersionReport report = hubServicesFactory.getRestConnection().getGson().fromJson(reportFile.get("fileContent"), VersionReport.class);

                return report;
            } else if (responseCode == 412) {
                final String response = hubServicesFactory.getRestConnection().readResponseAsString(resource.getResponse());
                final JsonObject json = hubServicesFactory.getRestConnection().getJsonParser().parse(response).getAsJsonObject();
                final String errorMessage = json.get("errorMessage").getAsString();
                throw new BDRestException(errorMessage + " Error Code: " + responseCode, resource);
            } else {
                throw new BDRestException(
                        "There was a problem getting the content of this Report. Error Code: " + responseCode,
                        resource);
            }
        } finally {
            releaseResource(resource);
        }
    }

    public PolicyStatusItem getPolicyStatus(final String policyStatusUrl)
            throws IOException, BDRestException, URISyntaxException {
        if (StringUtils.isBlank(policyStatusUrl)) {
            throw new IllegalArgumentException("Missing the policy status URL.");
        }
        return hubServicesFactory.createPolicyStatusRestService().getItem(policyStatusUrl);
    }

    /**
     * Gets the content of the scanStatus at the provided url
     */
    public ScanSummaryItem checkScanStatus(final String scanStatusUrl)
            throws IOException, BDRestException, URISyntaxException {
        return hubServicesFactory.createScanSummaryRestService().getItem(scanStatusUrl);
    }

    /**
     *
     * @return registration id Registration ID of the hub instance
     * @throws URISyntaxException
     * @throws BDRestException
     * @throws IOException
     *
     *             Returns the registration ID of the hub instance
     */
    public String getRegistrationId() throws URISyntaxException, BDRestException, IOException, JsonSyntaxException {
        return hubServicesFactory.createHubRegistrationRestService().getRegistrationId();
    }

    private void releaseResource(final ClientResource resource) {
        if (resource.getResponse() != null) {
            resource.getResponse().release();
        }
        resource.release();
    }

    public HubServicesFactory getHubServicesFactory() {
        return hubServicesFactory;
    }

}
