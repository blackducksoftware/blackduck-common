/**
 * hub-common
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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
package com.blackducksoftware.integration.hub.dataservice;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.generated.component.RiskCountView;
import com.blackducksoftware.integration.hub.api.generated.enumeration.PolicyStatusApprovalStatusType;
import com.blackducksoftware.integration.hub.api.generated.enumeration.ReportFormatType;
import com.blackducksoftware.integration.hub.api.generated.enumeration.ReportType;
import com.blackducksoftware.integration.hub.api.generated.enumeration.RiskCountType;
import com.blackducksoftware.integration.hub.api.generated.view.PolicyRuleViewV2;
import com.blackducksoftware.integration.hub.api.generated.view.PolicyStatusView;
import com.blackducksoftware.integration.hub.api.generated.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.api.generated.view.ProjectView;
import com.blackducksoftware.integration.hub.api.generated.view.ReportView;
import com.blackducksoftware.integration.hub.api.generated.view.VersionBomComponentView;
import com.blackducksoftware.integration.hub.api.view.MetaHandler;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.report.RiskReportWriter;
import com.blackducksoftware.integration.hub.report.api.BomComponent;
import com.blackducksoftware.integration.hub.report.api.PolicyRule;
import com.blackducksoftware.integration.hub.report.api.ReportData;
import com.blackducksoftware.integration.hub.report.exception.RiskReportException;
import com.blackducksoftware.integration.hub.report.pdf.PDFBoxWriter;
import com.blackducksoftware.integration.hub.request.Response;
import com.blackducksoftware.integration.hub.rest.HttpMethod;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.rest.UpdateRequestWrapper;
import com.blackducksoftware.integration.hub.rest.exception.IntegrationRestException;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.util.IntegrationEscapeUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ReportDataService extends HubDataService {
    public final static long DEFAULT_TIMEOUT = 1000 * 60 * 5;

    private final IntLogger logger;
    private final ProjectDataService projectDataService;
    private final IntegrationEscapeUtil escapeUtil;

    private final long timeoutInMilliseconds;

    public ReportDataService(final RestConnection restConnection, final ProjectDataService projectDataService, final IntegrationEscapeUtil escapeUtil) {
        this(restConnection, projectDataService, escapeUtil, DEFAULT_TIMEOUT);
    }

    public ReportDataService(final RestConnection restConnection, final ProjectDataService projectDataService, final IntegrationEscapeUtil escapeUtil, final long timeoutInMilliseconds) {
        super(restConnection);
        this.logger = restConnection.logger;
        this.projectDataService = projectDataService;
        this.escapeUtil = escapeUtil;

        long timeout = timeoutInMilliseconds;
        if (timeoutInMilliseconds <= 0l) {
            timeout = DEFAULT_TIMEOUT;
            logger.alwaysLog(timeoutInMilliseconds + "ms is not a valid BOM wait time, using : " + timeout + "ms instead");
        }
        this.timeoutInMilliseconds = timeout;
    }

    public String getNoticesReportData(final String projectName, final String projectVersionName) throws IntegrationException {
        final ProjectView project = projectDataService.getProjectByName(projectName);
        final ProjectVersionView version = projectDataService.getProjectVersion(project, projectVersionName);
        return getNoticesReportData(project, version);
    }

    public String getNoticesReportData(final ProjectView project, final ProjectVersionView version) throws IntegrationException {
        logger.trace("Getting the Notices Report Contents using the Report Rest Server");
        return generateHubNoticesReport(version, ReportFormatType.TEXT);
    }

    public File createNoticesReportFile(final File outputDirectory, final String projectName, final String projectVersionName) throws IntegrationException {
        return createNoticesReportFile(outputDirectory, getNoticesReportData(projectName, projectVersionName), projectName, projectVersionName);
    }

    public File createNoticesReportFile(final File outputDirectory, final ProjectView project, final ProjectVersionView version) throws IntegrationException {
        return createNoticesReportFile(outputDirectory, getNoticesReportData(project, version), project.name, version.versionName);
    }

    private File createNoticesReportFile(final File outputDirectory, final String noticesReportContent, final String projectName, final String projectVersionName) throws HubIntegrationException {
        if (noticesReportContent == null) {
            return null;
        }
        final String escapedProjectName = escapeUtil.escapeForUri(projectName);
        final String escapedProjectVersionName = escapeUtil.escapeForUri(projectVersionName);
        final File noticesReportFile = new File(outputDirectory, escapedProjectName + "_" + escapedProjectVersionName + "_Hub_Notices_Report.txt");
        if (noticesReportFile.exists()) {
            noticesReportFile.delete();
        }
        try (FileWriter writer = new FileWriter(noticesReportFile)) {
            logger.trace("Creating Notices Report in : " + outputDirectory.getCanonicalPath());
            writer.write(noticesReportContent);
            logger.trace("Created Notices Report : " + noticesReportFile.getCanonicalPath());
            return noticesReportFile;
        } catch (final IOException e) {
            throw new HubIntegrationException(e.getMessage(), e);
        }
    }

    public ReportData getRiskReportData(final String projectName, final String projectVersionName) throws IntegrationException {
        final ProjectView project = projectDataService.getProjectByName(projectName);
        final ProjectVersionView version = projectDataService.getProjectVersion(project, projectVersionName);
        return getRiskReportData(project, version);
    }

    public ReportData getRiskReportData(final ProjectView project, final ProjectVersionView version) throws IntegrationException {
        final String originalProjectUrl = getHref(project);
        final String originalVersionUrl = getHref(version);
        final ReportData reportData = new ReportData();
        reportData.setProjectName(project.name);
        reportData.setProjectURL(getReportProjectUrl(originalProjectUrl));
        reportData.setProjectVersion(version.versionName);
        reportData.setProjectVersionURL(getReportVersionUrl(originalVersionUrl, false));
        reportData.setPhase(version.phase.toString());
        reportData.setDistribution(version.distribution.toString());
        final List<BomComponent> components = new ArrayList<>();
        logger.trace("Getting the Report Contents using the Aggregate Bom Rest Server");
        final List<VersionBomComponentView> bomEntries = getResponsesFromLinkResponse(version, ProjectVersionView.COMPONENTS_LINK_RESPONSE, true);
        boolean policyFailure = false;
        for (final VersionBomComponentView bomEntry : bomEntries) {
            final BomComponent component = createBomComponentFromBomComponentView(bomEntry);
            String policyStatus = bomEntry.approvalStatus.toString();
            if (StringUtils.isBlank(policyStatus)) {
                String componentPolicyStatusURL = null;
                if (!StringUtils.isBlank(bomEntry.componentVersion)) {
                    componentPolicyStatusURL = getComponentPolicyURL(originalVersionUrl, bomEntry.componentVersion);
                } else {
                    componentPolicyStatusURL = getComponentPolicyURL(originalVersionUrl, bomEntry.component);
                }
                if (!policyFailure) {
                    // FIXME if we could check if the Hub has the policy module we could remove a lot of the mess
                    try {
                        final PolicyStatusView bomPolicyStatus = getResponse(componentPolicyStatusURL, PolicyStatusView.class);
                        policyStatus = bomPolicyStatus.approvalStatus.toString();
                    } catch (final IntegrationException e) {
                        policyFailure = true;
                        logger.debug("Could not get the component policy status, the Hub policy module is not enabled");
                    }
                }
            }
            component.setPolicyStatus(policyStatus);
            populatePolicyRuleInfo(component, bomEntry);
            components.add(component);
        }
        reportData.setComponents(components);
        return reportData;
    }

    public void createReportFiles(final File outputDirectory, final String projectName, final String projectVersionName) throws IntegrationException {
        final ReportData reportData = getRiskReportData(projectName, projectVersionName);
        createReportFiles(outputDirectory, reportData);
    }

    public void createReportFiles(final File outputDirectory, final ProjectView project, final ProjectVersionView version) throws IntegrationException {
        final ReportData reportData = getRiskReportData(project, version);
        createReportFiles(outputDirectory, reportData);
    }

    public void createReportFiles(final File outputDirectory, final ReportData reportData) throws HubIntegrationException {
        try {
            logger.trace("Creating Risk Report Files in : " + outputDirectory.getCanonicalPath());
            final RiskReportWriter writer = new RiskReportWriter();
            writer.createHtmlReportFiles(outputDirectory, reportData);
        } catch (final RiskReportException | IOException e) {
            throw new HubIntegrationException(e.getMessage(), e);
        }
    }

    public File createReportPdfFile(final File outputDirectory, final String projectName, final String projectVersionName) throws IntegrationException {
        final ReportData reportData = getRiskReportData(projectName, projectVersionName);
        return createReportPdfFile(outputDirectory, reportData);
    }

    public File createReportPdfFile(final File outputDirectory, final ProjectView project, final ProjectVersionView version) throws IntegrationException {
        final ReportData reportData = getRiskReportData(project, version);
        return createReportPdfFile(outputDirectory, reportData);
    }

    public File createReportPdfFile(final File outputDirectory, final ReportData reportData) throws HubIntegrationException {
        try {
            logger.trace("Creating Risk Report Pdf in : " + outputDirectory.getCanonicalPath());
            final PDFBoxWriter writer = new PDFBoxWriter(logger);
            final File pdfFile = writer.createPDFReportFile(outputDirectory, reportData);
            logger.trace("Created Risk Report Pdf : " + pdfFile.getCanonicalPath());
            return pdfFile;
        } catch (final RiskReportException | IOException e) {
            throw new HubIntegrationException(e.getMessage(), e);
        }
    }

    private String getComponentPolicyURL(final String versionURL, final String componentURL) {
        final String componentVersionSegments = componentURL.substring(componentURL.indexOf(MetaHandler.COMPONENTS_LINK));
        return versionURL + "/" + componentVersionSegments + "/" + MetaHandler.POLICY_STATUS_LINK;
    }

    private BomComponent createBomComponentFromBomComponentView(final VersionBomComponentView bomEntry) {
        final BomComponent component = new BomComponent();
        component.setComponentName(bomEntry.componentName);
        component.setComponentURL(getReportProjectUrl(bomEntry.component));
        component.setComponentVersion(bomEntry.componentVersionName);
        component.setComponentVersionURL(getReportVersionUrl(bomEntry.componentVersion, true));
        component.setLicense(bomEntry.licenses.get(0).licenseDisplay);
        if (bomEntry.securityRiskProfile != null && bomEntry.securityRiskProfile.counts != null && !bomEntry.securityRiskProfile.counts.isEmpty()) {
            for (final RiskCountView count : bomEntry.securityRiskProfile.counts) {
                if (count.countType == RiskCountType.HIGH && count.count > 0) {
                    component.setSecurityRiskHighCount(count.count);
                } else if (count.countType == RiskCountType.MEDIUM && count.count > 0) {
                    component.setSecurityRiskMediumCount(count.count);
                } else if (count.countType == RiskCountType.LOW && count.count > 0) {
                    component.setSecurityRiskLowCount(count.count);
                }
            }
        }
        if (bomEntry.licenseRiskProfile != null && bomEntry.licenseRiskProfile.counts != null && !bomEntry.licenseRiskProfile.counts.isEmpty()) {
            for (final RiskCountView count : bomEntry.licenseRiskProfile.counts) {
                if (count.countType == RiskCountType.HIGH && count.count > 0) {
                    component.setLicenseRiskHighCount(count.count);
                } else if (count.countType == RiskCountType.MEDIUM && count.count > 0) {
                    component.setLicenseRiskMediumCount(count.count);
                } else if (count.countType == RiskCountType.LOW && count.count > 0) {
                    component.setLicenseRiskLowCount(count.count);
                }
            }
        }
        if (bomEntry.operationalRiskProfile != null && bomEntry.operationalRiskProfile.counts != null && !bomEntry.operationalRiskProfile.counts.isEmpty()) {
            for (final RiskCountView count : bomEntry.operationalRiskProfile.counts) {
                if (count.countType == RiskCountType.HIGH && count.count > 0) {
                    component.setOperationalRiskHighCount(count.count);
                } else if (count.countType == RiskCountType.MEDIUM && count.count > 0) {
                    component.setOperationalRiskMediumCount(count.count);
                } else if (count.countType == RiskCountType.LOW && count.count > 0) {
                    component.setOperationalRiskLowCount(count.count);
                }
            }
        }
        return component;
    }

    public void populatePolicyRuleInfo(final BomComponent component, final VersionBomComponentView bomEntry) throws IntegrationException {
        if (bomEntry != null && bomEntry.approvalStatus != null) {
            final PolicyStatusApprovalStatusType status = bomEntry.approvalStatus;
            if (status == PolicyStatusApprovalStatusType.IN_VIOLATION) {
                final List<PolicyRuleViewV2> rules = getResponsesFromLinkResponse(bomEntry, VersionBomComponentView.POLICY_RULES_LINK_RESPONSE, true);
                final List<PolicyRule> rulesViolated = new ArrayList<>();
                for (final PolicyRuleViewV2 policyRuleView : rules) {
                    final PolicyRule ruleViolated = new PolicyRule();
                    ruleViolated.setName(policyRuleView.name);
                    ruleViolated.setDescription(policyRuleView.description);
                    rulesViolated.add(ruleViolated);
                }
                component.setPolicyRulesViolated(rulesViolated);
            }
        }
    }

    private String getBaseUrl() {
        return getHubBaseUrl().toString();
    }

    private String getReportProjectUrl(final String projectURL) {
        if (projectURL == null) {
            return null;
        }
        final String projectId = projectURL.substring(projectURL.lastIndexOf("/") + 1);
        final StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(getBaseUrl());
        urlBuilder.append("#");
        urlBuilder.append("projects/id:");
        urlBuilder.append(projectId);

        return urlBuilder.toString();
    }

    private String getReportVersionUrl(final String versionURL, final boolean isComponent) {
        if (versionURL == null) {
            return null;
        }
        final String versionId = versionURL.substring(versionURL.lastIndexOf("/") + 1);
        final StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(getBaseUrl());
        urlBuilder.append("#");
        urlBuilder.append("versions/id:");
        urlBuilder.append(versionId);
        if (!isComponent) {
            urlBuilder.append("/view:bom");
        }
        return urlBuilder.toString();
    }

    /**
     * Assumes the BOM has already been updated
     *
     */
    public String generateHubNoticesReport(final ProjectVersionView version, final ReportFormatType reportFormat) throws IntegrationException {
        if (hasLink(version, ProjectVersionView.LICENSEREPORTS_LINK)) {
            try {
                logger.debug("Starting the Notices Report generation.");
                final String reportUrl = startGeneratingHubNoticesReport(version, reportFormat);

                logger.debug("Waiting for the Notices Report to complete.");
                final ReportView reportInfo = isReportFinishedGenerating(reportUrl);

                final String contentLink = getFirstLink(reportInfo, ReportView.CONTENT_LINK);

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

    public String startGeneratingHubNoticesReport(final ProjectVersionView version, final ReportFormatType reportFormat) throws IntegrationException {
        final String reportUri = getFirstLink(version, ProjectVersionView.LICENSEREPORTS_LINK);

        final JsonObject json = new JsonObject();
        json.addProperty("reportFormat", reportFormat.toString());
        json.addProperty("reportType", ReportType.VERSION_LICENSE.toString());

        final UpdateRequestWrapper requestWrapper = new UpdateRequestWrapper(HttpMethod.POST, json);
        return executePostRequestAndRetrieveURL(reportUri, requestWrapper);
    }

    /**
     * Checks the report URL every 5 seconds until the report has a finished time available, then we know it is done being generated. Throws HubIntegrationException after 30 minutes if the report has not been generated yet.
     */
    public ReportView isReportFinishedGenerating(final String reportUri) throws IntegrationException {
        final long startTime = System.currentTimeMillis();
        long elapsedTime = 0;
        Date timeFinished = null;
        ReportView reportInfo = null;

        while (timeFinished == null) {
            reportInfo = getResponse(reportUri, ReportView.class);
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

    public String getNoticesReportContent(final String reportContentUri) throws IntegrationException {
        final JsonElement fileContent = getReportContentJson(reportContentUri);
        return fileContent.getAsString();
    }

    private JsonElement getReportContentJson(final String reportContentUri) throws IntegrationException {
        try (Response response = executeGetRequest(reportContentUri)) {
            final String jsonResponse = response.getContentString();

            final JsonObject json = getJsonParser().parse(jsonResponse).getAsJsonObject();
            final JsonElement content = json.get("reportContent");
            final JsonArray reportConentArray = content.getAsJsonArray();
            final JsonObject reportFile = reportConentArray.get(0).getAsJsonObject();
            return reportFile.get("fileContent");
        } catch (final IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    public void deleteHubReport(final String reportUri) throws IntegrationException {
        try (Response response = executeUpdateRequest(reportUri, new UpdateRequestWrapper(HttpMethod.DELETE))) {
        } catch (final IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }
}
