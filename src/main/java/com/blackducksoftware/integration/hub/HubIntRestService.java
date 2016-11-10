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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.restlet.data.CharacterSet;
import org.restlet.data.Cookie;
import org.restlet.data.Header;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.util.Series;

import com.blackducksoftware.integration.hub.api.codelocation.CodeLocationRestService;
import com.blackducksoftware.integration.hub.api.component.ComponentVersionRestService;
import com.blackducksoftware.integration.hub.api.notification.NotificationRestService;
import com.blackducksoftware.integration.hub.api.policy.PolicyRestService;
import com.blackducksoftware.integration.hub.api.policy.PolicyStatusItem;
import com.blackducksoftware.integration.hub.api.policy.PolicyStatusRestService;
import com.blackducksoftware.integration.hub.api.project.ProjectItem;
import com.blackducksoftware.integration.hub.api.project.ProjectRestService;
import com.blackducksoftware.integration.hub.api.project.ReleaseItemRestService;
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionItem;
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionRestService;
import com.blackducksoftware.integration.hub.api.report.ReportCategoriesEnum;
import com.blackducksoftware.integration.hub.api.report.ReportFormatEnum;
import com.blackducksoftware.integration.hub.api.report.ReportInformationItem;
import com.blackducksoftware.integration.hub.api.report.VersionReport;
import com.blackducksoftware.integration.hub.api.scan.ScanLocationItem;
import com.blackducksoftware.integration.hub.api.scan.ScanLocationResults;
import com.blackducksoftware.integration.hub.api.scan.ScanSummaryItem;
import com.blackducksoftware.integration.hub.api.scan.ScanSummaryRestService;
import com.blackducksoftware.integration.hub.api.user.UserRestService;
import com.blackducksoftware.integration.hub.api.version.ReleaseItem;
import com.blackducksoftware.integration.hub.api.version.VersionBomPolicyRestService;
import com.blackducksoftware.integration.hub.api.vulnerabilities.VulnerabilityRestService;
import com.blackducksoftware.integration.hub.dataservices.policystatus.PolicyStatusDataService;
import com.blackducksoftware.integration.hub.dataservices.scan.ScanStatusDataService;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.exception.ProjectDoesNotExistException;
import com.blackducksoftware.integration.hub.exception.ResourceDoesNotExistException;
import com.blackducksoftware.integration.hub.exception.UnexpectedHubResponseException;
import com.blackducksoftware.integration.hub.exception.VersionDoesNotExistException;
import com.blackducksoftware.integration.hub.global.HubProxyInfo;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.log.IntLogger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class HubIntRestService {
    private RestConnection restConnection;

    private final Gson gson;

    private final JsonParser jsonParser;

    private CodeLocationRestService codeLocationRestService;

    private ComponentVersionRestService componentVersionRestService;

    private NotificationRestService notificationRestService;

    private PolicyRestService policyRestService;

    private PolicyStatusRestService policyStatusRestService;

    private ProjectRestService projectRestService;

    private ProjectVersionRestService projectVersionRestService;

    private ReleaseItemRestService releaseItemRestService;

    private ScanSummaryRestService scanSummaryRestService;

    private UserRestService userRestService;

    private VersionBomPolicyRestService versionBomPolicyRestService;

    private VulnerabilityRestService vulnerabilityRestService;

    public HubIntRestService(final RestConnection restConnection) throws URISyntaxException {
        this.restConnection = restConnection;

        final GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setDateFormat(RestConnection.JSON_DATE_FORMAT);
        this.gson = gsonBuilder.create();

        this.jsonParser = new JsonParser();

        this.codeLocationRestService = new CodeLocationRestService(restConnection, gson, jsonParser);
        this.componentVersionRestService = new ComponentVersionRestService(restConnection, gson, jsonParser);
        this.notificationRestService = new NotificationRestService(restConnection, gson, jsonParser);
        this.policyRestService = new PolicyRestService(restConnection, gson, jsonParser);
        this.policyStatusRestService = new PolicyStatusRestService(restConnection, gson, jsonParser);
        this.projectRestService = new ProjectRestService(restConnection, gson, jsonParser);
        this.projectVersionRestService = new ProjectVersionRestService(restConnection, gson, jsonParser);
        this.releaseItemRestService = new ReleaseItemRestService(restConnection, gson, jsonParser);
        this.scanSummaryRestService = new ScanSummaryRestService(restConnection, gson, jsonParser);
        this.userRestService = new UserRestService(restConnection, gson, jsonParser);
        this.versionBomPolicyRestService = new VersionBomPolicyRestService(restConnection, gson, jsonParser);
        this.vulnerabilityRestService = new VulnerabilityRestService(restConnection, gson, jsonParser);
    }

    /**
     * @deprecated moved to RestConnection.
     */
    @Deprecated
    public void setTimeout(final int timeout) {
        getRestConnection().setTimeout(timeout);
    }

    /**
     * @deprecated moved to RestConnection.
     */
    @Deprecated
    public void setLogger(final IntLogger logger) {
        getRestConnection().setLogger(logger);
    }

    /**
     * @deprecated moved to RestConnection.
     */
    @Deprecated
    public String getBaseUrl() {
        return getRestConnection().getBaseUrl();
    }

    /**
     * The proxy settings get set as System properties. I.E. https.proxyHost,
     * https.proxyPort, http.proxyHost, http.proxyPort, http.nonProxyHosts
     *
     * @deprecated moved to RestConnection.
     */
    @Deprecated
    public void setProxyProperties(final HubProxyInfo proxyInfo) {
        getRestConnection().setProxyProperties(proxyInfo);
    }

    /**
     * The proxy settings get set as System properties. I.E. https.proxyHost,
     * https.proxyPort, http.proxyHost, http.proxyPort, http.nonProxyHosts
     *
     * @deprecated moved to RestConnection.
     */
    @Deprecated
    public void setProxyProperties(final String proxyHost, final int proxyPort, final List<Pattern> noProxyHosts,
            final String proxyUsername, final String proxyPassword) {
        getRestConnection().setProxyProperties(proxyHost, proxyPort, noProxyHosts, proxyUsername, proxyPassword);
    }

    /**
     * @deprecated moved to RestConnection.
     */
    @Deprecated
    public ClientResource createClientResource() throws URISyntaxException {
        return getRestConnection().createClientResource();
    }

    /**
     * @deprecated moved to RestConnection.
     */
    @Deprecated
    public ClientResource createClientResource(final String providedUrl) throws URISyntaxException {
        return getRestConnection().createClientResource(providedUrl);
    }

    /**
     * Gets the cookie for the Authorized connection to the Hub server. Returns
     * the response code from the connection.
     *
     * @deprecated moved to RestConnection.
     */
    @Deprecated
    public int setCookies(final String hubUserName, final String hubPassword)
            throws URISyntaxException, BDRestException {
        return getRestConnection().setCookies(hubUserName, hubPassword);
    }

    /**
     * @deprecated moved to RestConnection.
     */
    @Deprecated
    public Series<Cookie> getCookies() {
        return getRestConnection().getCookies();
    }

    /**
     * Retrieves a list of Hub Projects that may match the hubProjectName
     *
     */
    public List<ProjectItem> getProjectMatches(final String projectName)
            throws IOException, BDRestException, URISyntaxException {
        return getProjectRestService().getAllProjectMatches(projectName);
    }

    /**
     * Retrieves a list of Hub Projects that may match the hubProjectName,
     * default limit is 10
     *
     */
    public List<ProjectItem> getProjectMatches(final String projectName, final int limit)
            throws IOException, BDRestException, URISyntaxException {
        return getProjectRestService().getProjectMatches(projectName, limit);
    }

    /**
     * Gets the Project that is specified by the projectName
     *
     */
    public ProjectItem getProjectByName(final String projectName)
            throws IOException, BDRestException, URISyntaxException, ProjectDoesNotExistException {
        return getProjectRestService().getProjectByName(projectName);
    }

    public ProjectItem getProject(final String projectUrl) throws IOException, BDRestException, URISyntaxException {
        return getProjectRestService().getItem(projectUrl);
    }

    public ProjectVersionItem getProjectVersion(final String versionUrl)
            throws IOException, BDRestException, URISyntaxException {
        return projectVersionRestService.getItem(versionUrl);
    }

    /**
     * Gets the list of Versions for the specified Project
     *
     */
    public ProjectVersionItem getVersion(final ProjectItem project, final String versionName) throws IOException,
            BDRestException, URISyntaxException, VersionDoesNotExistException, UnexpectedHubResponseException {
        final List<ProjectVersionItem> versions = getProjectVersionsForProject(project);
        for (final ProjectVersionItem version : versions) {
            if (version.getVersionName().equals(versionName)) {
                return version;
            }
        }
        throw new VersionDoesNotExistException(
                "This Version does not exist. Project : " + project.getName() + " Version : " + versionName);
    }

    public List<ProjectVersionItem> getProjectVersionsForProject(final ProjectItem project)
            throws UnexpectedHubResponseException, IOException, URISyntaxException, BDRestException {
        final String versionsUrl = getVersionsUrl(project);
        final List<ProjectVersionItem> allProjectVersions = projectVersionRestService
                .getAllProjectVersions(versionsUrl);
        return allProjectVersions;
    }

    private String getVersionsUrl(final ProjectItem project) throws UnexpectedHubResponseException {
        final List<String> versionLinks = project.getLinks(ProjectItem.VERSION_LINK);
        if (versionLinks.size() != 1) {
            throw new UnexpectedHubResponseException("The project " + project.getName() + " has " + versionLinks.size()
                    + " " + ProjectItem.VERSION_LINK + " links; expected one");
        }
        final String versionLink = versionLinks.get(0);
        return versionLink;
    }

    private String getVersionReportLink(final ProjectVersionItem version) throws UnexpectedHubResponseException {
        final List<String> versionLinks = version.getLinks(ReleaseItem.VERSION_REPORT_LINK);
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
        return getProjectRestService().createHubProject(projectName);
    }

    /**
     * Creates a new Version in the Project specified, using the phase and
     * distribution provided.
     *
     * @return the version URL
     *
     */
    public String createHubVersion(final ProjectItem project, final String versionName, final String phase,
            final String dist) throws IOException, BDRestException, URISyntaxException, UnexpectedHubResponseException {

        return getReleaseItemRestService().createHubVersion(project, versionName, phase, dist);
    }

    /**
     * Gets the code locations that match the host and paths provided
     *
     * @deprecated with Hub 3.0 should use the status files from the CLI
     *             instead. The CLI option is --statusWriteDir
     */
    @Deprecated
    public List<ScanLocationItem> getScanLocations(final String hostname, final List<String> scanTargets)
            throws InterruptedException, BDRestException, HubIntegrationException, URISyntaxException, IOException {
        final List<ScanLocationItem> codeLocations = new ArrayList<>();
        ClientResource resource = null;
        for (final String targetPath : scanTargets) {
            String correctedTargetPath = targetPath;

            // Scan paths in the Hub only use '/' not '\'
            if (correctedTargetPath.contains("\\")) {
                correctedTargetPath = correctedTargetPath.replace("\\", "/");
            }
            // and it always starts with a '/'
            if (!correctedTargetPath.startsWith("/")) {
                correctedTargetPath = "/" + correctedTargetPath;
            }

            resource = getRestConnection().createClientResource();
            try {
                resource.addSegment("api");
                resource.addSegment("v1");
                resource.addSegment("scanlocations");
                resource.addQueryParameter("host", hostname);
                resource.addQueryParameter("path", correctedTargetPath);

                resource.setMethod(Method.GET);

                getRestConnection().handleRequest(resource);

                final int responseCode = resource.getResponse().getStatus().getCode();

                if (getRestConnection().isSuccess(responseCode)) {
                    final String response = getRestConnection().readResponseAsString(resource.getResponse());
                    final ScanLocationResults results = gson.fromJson(response, ScanLocationResults.class);
                    final ScanLocationItem currentCodeLocation = getScanLocationMatch(hostname, correctedTargetPath,
                            results);
                    if (currentCodeLocation == null) {
                        throw new HubIntegrationException("Could not determine the code location for the Host : "
                                + hostname + " and Path : " + correctedTargetPath);
                    }

                    codeLocations.add(currentCodeLocation);
                } else {
                    throw new BDRestException(
                            "There was a problem getting the code locations for the host and paths provided. Error Code: "
                                    + responseCode,
                            resource);
                }
            } finally {
                releaseResource(resource);
            }
        }
        return codeLocations;
    }

    private ScanLocationItem getScanLocationMatch(final String hostname, final String scanTarget,
            final ScanLocationResults results) {
        String targetPath = scanTarget;

        if (targetPath.endsWith("/")) {
            targetPath = targetPath.substring(0, targetPath.length() - 1);
        }

        for (final ScanLocationItem scanMatch : results.getItems()) {
            String path = scanMatch.getPath().trim();

            // Remove trailing slash from both strings
            if (path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }
            if (targetPath.equals(path)) {
                return scanMatch;
            }
        }

        return null;
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

        final StringRepresentation stringRep = new StringRepresentation(gson.toJson(json));
        stringRep.setMediaType(MediaType.APPLICATION_JSON);
        stringRep.setCharacterSet(CharacterSet.UTF_8);
        String location = null;
        try {
            location = getRestConnection().httpPostFromAbsoluteUrl(getVersionReportLink(version), stringRep);
        } catch (final ResourceDoesNotExistException ex) {
            throw new BDRestException("There was a problem generating a report for this Version.", ex,
                    ex.getResource());
        }

        return location;
    }

    public int deleteHubReport(final String reportUrl) throws IOException, BDRestException, URISyntaxException {

        final ClientResource resource = getRestConnection().createClientResource(reportUrl);
        try {
            resource.setMethod(Method.DELETE);
            getRestConnection().handleRequest(resource);

            final int responseCode = resource.getResponse().getStatus().getCode();
            if (!getRestConnection().isSuccess(responseCode)) {
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
        final ClientResource resource = getRestConnection().createClientResource(reportUrl);
        try {
            @SuppressWarnings("unchecked")
            Series<Header> requestHeaders = (Series<Header>) resource.getRequestAttributes()
                    .get(HeaderConstants.ATTRIBUTE_HEADERS);
            if (requestHeaders == null) {
                requestHeaders = new Series<>(Header.class);
                resource.getRequestAttributes().put(HeaderConstants.ATTRIBUTE_HEADERS, requestHeaders);
            }
            requestHeaders.add(new Header("Accept", MediaType.APPLICATION_JSON.toString()));

            // Restlet 2.3.4 and higher
            // resource.accept(MediaType.APPLICATION_JSON);

            resource.setMethod(Method.GET);

            getRestConnection().handleRequest(resource);
            final int responseCode = resource.getResponse().getStatus().getCode();

            if (getRestConnection().isSuccess(responseCode)) {
                final String response = getRestConnection().readResponseAsString(resource.getResponse());
                return gson.fromJson(response, ReportInformationItem.class);
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
        final ClientResource resource = getRestConnection().createClientResource(reportContentUrl);
        try {
            resource.setMethod(Method.GET);

            getRestConnection().handleRequest(resource);
            final int responseCode = resource.getResponse().getStatus().getCode();

            if (getRestConnection().isSuccess(responseCode)) {
                final String response = getRestConnection().readResponseAsString(resource.getResponse());

                final JsonObject json = jsonParser.parse(response).getAsJsonObject();
                final JsonElement content = json.get("reportContent");
                final JsonArray reportConentArray = content.getAsJsonArray();
                final JsonObject reportFile = reportConentArray.get(0).getAsJsonObject();

                final VersionReport report = gson.fromJson(reportFile.get("fileContent"), VersionReport.class);

                return report;
            } else if (responseCode == 412) {
                final String response = getRestConnection().readResponseAsString(resource.getResponse());
                final JsonObject json = jsonParser.parse(response).getAsJsonObject();
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
        return getPolicyStatusRestService().getItem(policyStatusUrl);
    }

    /**
     * Gets the content of the scanStatus at the provided url
     */
    public ScanSummaryItem checkScanStatus(final String scanStatusUrl)
            throws IOException, BDRestException, URISyntaxException {
        return getScanSummaryRestService().getItem(scanStatusUrl);
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
        final ClientResource resource = getRestConnection().createClientResource();
        try {
            resource.addSegment("api");
            resource.addSegment("v1");
            resource.addSegment("registrations");
            resource.setMethod(Method.GET);

            getRestConnection().handleRequest(resource);
            final int responseCode = resource.getResponse().getStatus().getCode();
            if (responseCode == 200) {
                final String response = getRestConnection().readResponseAsString(resource.getResponse());
                final JsonElement je = jsonParser.parse(response);
                final JsonObject jo = je.getAsJsonObject();
                final JsonElement je2 = jo.get("registrationId");
                final String regId = je2.getAsString();
                return regId;
            } else {
                throw new BDRestException(
                        "There was a problem getting the registration ID. Error Code: " + responseCode, resource);
            }
        } finally {
            releaseResource(resource);
        }
    }

    private void releaseResource(final ClientResource resource) {
        if (resource.getResponse() != null) {
            resource.getResponse().release();
        }
        resource.release();
    }

    public RestConnection getRestConnection() {
        return restConnection;
    }

    public void setRestConnection(final RestConnection restConnection) {
        this.restConnection = restConnection;
    }

    public Gson getGson() {
        return gson;
    }

    public JsonParser getJsonParser() {
        return jsonParser;
    }

    public CodeLocationRestService getCodeLocationRestService() {
        return codeLocationRestService;
    }

    public void setCodeLocationRestService(final CodeLocationRestService codeLocationRestService) {
        this.codeLocationRestService = codeLocationRestService;
    }

    public ComponentVersionRestService getComponentVersionRestService() {
        return componentVersionRestService;
    }

    public void setComponentVersionRestService(final ComponentVersionRestService componentVersionRestService) {
        this.componentVersionRestService = componentVersionRestService;
    }

    public NotificationRestService getNotificationRestService() {
        return notificationRestService;
    }

    public void setNotificationRestService(final NotificationRestService notificationRestService) {
        this.notificationRestService = notificationRestService;
    }

    public PolicyRestService getPolicyRestService() {
        return policyRestService;
    }

    public void setPolicyRestService(final PolicyRestService policyRestService) {
        this.policyRestService = policyRestService;
    }

    public PolicyStatusRestService getPolicyStatusRestService() {
        return policyStatusRestService;
    }

    public void setPolicyStatusRestService(final PolicyStatusRestService policyStatusRestService) {
        this.policyStatusRestService = policyStatusRestService;
    }

    public ProjectRestService getProjectRestService() {
        return projectRestService;
    }

    public void setProjectRestService(final ProjectRestService projectRestService) {
        this.projectRestService = projectRestService;
    }

    public ProjectVersionRestService getProjectVersionRestService() {
        return projectVersionRestService;
    }

    public void setProjectVersionRestService(final ProjectVersionRestService projectVersionRestService) {
        this.projectVersionRestService = projectVersionRestService;
    }

    public ReleaseItemRestService getReleaseItemRestService() {
        return releaseItemRestService;
    }

    public void setReleaseItemRestService(final ReleaseItemRestService releaseItemRestService) {
        this.releaseItemRestService = releaseItemRestService;
    }

    public ScanSummaryRestService getScanSummaryRestService() {
        return scanSummaryRestService;
    }

    public void setScanSummaryRestService(final ScanSummaryRestService scanSummaryRestService) {
        this.scanSummaryRestService = scanSummaryRestService;
    }

    public UserRestService getUserRestService() {
        return userRestService;
    }

    public void setUserRestService(final UserRestService userRestService) {
        this.userRestService = userRestService;
    }

    public VersionBomPolicyRestService getVersionBomPolicyRestService() {
        return versionBomPolicyRestService;
    }

    public void setVersionBomPolicyRestService(final VersionBomPolicyRestService versionBomPolicyRestService) {
        this.versionBomPolicyRestService = versionBomPolicyRestService;
    }

    public VulnerabilityRestService getVulnerabilityRestService() {
        return vulnerabilityRestService;
    }

    public void setVulnerabilityRestService(final VulnerabilityRestService vulnerabilityRestService) {
        this.vulnerabilityRestService = vulnerabilityRestService;
    }

    public PolicyStatusDataService getPolicyStatusDataService() {
        return new PolicyStatusDataService(restConnection, gson, jsonParser, projectRestService,
                projectVersionRestService, policyStatusRestService);
    }

    public ScanStatusDataService getScanStatusDataService() {
        return new ScanStatusDataService(restConnection, gson, jsonParser, projectRestService, projectVersionRestService,
                codeLocationRestService, scanSummaryRestService);
    }

}
