/**
 * blackduck-common
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
package com.synopsys.integration.blackduck.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.synopsys.integration.blackduck.api.generated.component.RiskCountView;
import com.synopsys.integration.blackduck.api.generated.enumeration.PolicySummaryStatusType;
import com.synopsys.integration.blackduck.api.generated.enumeration.ReportFormatType;
import com.synopsys.integration.blackduck.api.generated.enumeration.ReportType;
import com.synopsys.integration.blackduck.api.generated.enumeration.RiskCountType;
import com.synopsys.integration.blackduck.api.generated.view.PolicyRuleViewV2;
import com.synopsys.integration.blackduck.api.generated.view.PolicyStatusView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.ReportView;
import com.synopsys.integration.blackduck.api.generated.view.VersionBomComponentView;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.blackduck.exception.RiskReportException;
import com.synopsys.integration.blackduck.service.model.BomComponent;
import com.synopsys.integration.blackduck.service.model.PolicyRule;
import com.synopsys.integration.blackduck.service.model.ReportData;
import com.synopsys.integration.blackduck.service.model.RequestFactory;
import com.synopsys.integration.blackduck.service.model.pdf.RiskReportPdfWriter;
import com.synopsys.integration.blackduck.service.model.pdf.RiskReportWriter;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.exception.IntegrationRestException;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.request.Response;
import com.synopsys.integration.util.IntegrationEscapeUtil;

public class ReportService extends DataService {
    public final static long DEFAULT_TIMEOUT = 1000L * 60 * 5;

    private final ProjectService projectDataService;
    private final IntegrationEscapeUtil escapeUtil;
    private final long timeoutInMilliseconds;

    public ReportService(final BlackDuckService blackDuckService, final IntLogger logger, final ProjectService projectDataService, final IntegrationEscapeUtil escapeUtil) {
        this(blackDuckService, logger, projectDataService, escapeUtil, DEFAULT_TIMEOUT);
    }

    public ReportService(final BlackDuckService blackDuckService, final IntLogger logger, final ProjectService projectDataService, final IntegrationEscapeUtil escapeUtil, final long timeoutInMilliseconds) {
        super(blackDuckService, logger);
        this.projectDataService = projectDataService;
        this.escapeUtil = escapeUtil;

        long timeout = timeoutInMilliseconds;
        if (timeoutInMilliseconds <= 0l) {
            timeout = DEFAULT_TIMEOUT;
            this.logger.alwaysLog(timeoutInMilliseconds + "ms is not a valid BOM wait time, using : " + timeout + "ms instead");
        }
        this.timeoutInMilliseconds = timeout;
    }

    public String getNoticesReportData(final ProjectView project, final ProjectVersionView version) throws InterruptedException, IntegrationException {
        logger.trace("Getting the Notices Report Contents using the Report Rest Server");
        return generateBlackDuckNoticesReport(version, ReportFormatType.TEXT);
    }

    public File createNoticesReportFile(final File outputDirectory, final ProjectView project, final ProjectVersionView version) throws InterruptedException, IntegrationException {
        return createNoticesReportFile(outputDirectory, getNoticesReportData(project, version), project.getName(), version.getVersionName());
    }

    private File createNoticesReportFile(final File outputDirectory, final String noticesReportContent, final String projectName, final String projectVersionName) throws BlackDuckIntegrationException {
        if (noticesReportContent == null) {
            return null;
        }
        final String escapedProjectName = escapeUtil.escapeForUri(projectName);
        final String escapedProjectVersionName = escapeUtil.escapeForUri(projectVersionName);
        final File noticesReportFile = new File(outputDirectory, escapedProjectName + "_" + escapedProjectVersionName + "_Black_Duck_Notices_Report.txt");
        if (noticesReportFile.exists()) {
            noticesReportFile.delete();
        }
        try (FileWriter writer = new FileWriter(noticesReportFile)) {
            logger.trace("Creating Notices Report in : " + outputDirectory.getCanonicalPath());
            writer.write(noticesReportContent);
            logger.trace("Created Notices Report : " + noticesReportFile.getCanonicalPath());
            return noticesReportFile;
        } catch (final IOException e) {
            throw new BlackDuckIntegrationException(e.getMessage(), e);
        }
    }

    public ReportData getRiskReportData(final ProjectView project, final ProjectVersionView version) throws IntegrationException {
        final String originalProjectUrl = project.getHref().orElse(null);
        final String originalVersionUrl = version.getHref().orElse(null);
        final ReportData reportData = new ReportData();
        reportData.setProjectName(project.getName());
        reportData.setProjectURL(getReportProjectUrl(originalProjectUrl));
        reportData.setProjectVersion(version.getVersionName());
        reportData.setProjectVersionURL(getReportVersionUrl(originalVersionUrl, false));
        reportData.setPhase(version.getPhase().toString());
        reportData.setDistribution(version.getDistribution().toString());
        final List<BomComponent> components = new ArrayList<>();
        logger.trace("Getting the Report Contents using the Aggregate Bom Rest Server");
        final List<VersionBomComponentView> bomEntries = blackDuckService.getAllResponses(version, ProjectVersionView.COMPONENTS_LINK_RESPONSE);
        boolean policyFailure = false;
        for (final VersionBomComponentView bomEntry : bomEntries) {
            final BomComponent component = createBomComponentFromBomComponentView(bomEntry);
            String policyStatus = bomEntry.getApprovalStatus().toString();
            if (StringUtils.isBlank(policyStatus)) {
                String componentPolicyStatusURL = null;
                if (!StringUtils.isBlank(bomEntry.getComponentVersion())) {
                    componentPolicyStatusURL = getComponentPolicyURL(originalVersionUrl, bomEntry.getComponentVersion());
                } else {
                    componentPolicyStatusURL = getComponentPolicyURL(originalVersionUrl, bomEntry.getComponent());
                }
                if (!policyFailure) {
                    // FIXME if we could check if Black Duck has the policy module we could remove a lot of the mess
                    try {
                        final PolicyStatusView bomPolicyStatus = blackDuckService.getResponse(componentPolicyStatusURL, PolicyStatusView.class);
                        policyStatus = bomPolicyStatus.getApprovalStatus().toString();
                    } catch (final IntegrationException e) {
                        policyFailure = true;
                        logger.debug("Could not get the component policy status, the Black Duck policy module is not enabled");
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

    public void createReportFiles(final File outputDirectory, final ProjectView project, final ProjectVersionView version) throws IntegrationException {
        final ReportData reportData = getRiskReportData(project, version);
        createReportFiles(outputDirectory, reportData);
    }

    public void createReportFiles(final File outputDirectory, final ReportData reportData) throws BlackDuckIntegrationException {
        try {
            logger.trace("Creating Risk Report Files in : " + outputDirectory.getCanonicalPath());
            final RiskReportWriter writer = new RiskReportWriter();
            writer.createHtmlReportFiles(blackDuckService.getGson(), outputDirectory, reportData);
        } catch (final RiskReportException | IOException e) {
            throw new BlackDuckIntegrationException(e.getMessage(), e);
        }
    }

    public File createReportPdfFile(final File outputDirectory, final ProjectView project, final ProjectVersionView version) throws IntegrationException {
        final ReportData reportData = getRiskReportData(project, version);
        return createReportPdfFile(outputDirectory, reportData);
    }

    public File createReportPdfFile(final File outputDirectory, final ReportData reportData) throws BlackDuckIntegrationException {
        try {
            logger.trace("Creating Risk Report Pdf in : " + outputDirectory.getCanonicalPath());
            final RiskReportPdfWriter writer = new RiskReportPdfWriter(logger);
            final File pdfFile = writer.createPDFReportFile(outputDirectory, reportData);
            logger.trace("Created Risk Report Pdf : " + pdfFile.getCanonicalPath());
            return pdfFile;
        } catch (final RiskReportException | IOException e) {
            throw new BlackDuckIntegrationException(e.getMessage(), e);
        }
    }

    private String getComponentPolicyURL(final String versionURL, final String componentURL) {
        final String componentVersionSegments = componentURL.substring(componentURL.indexOf("components"));
        return versionURL + "/" + componentVersionSegments + "/" + "policy-status";
    }

    private BomComponent createBomComponentFromBomComponentView(final VersionBomComponentView bomEntry) {
        final BomComponent component = new BomComponent();
        component.setComponentName(bomEntry.getComponentName());
        component.setComponentURL(getReportProjectUrl(bomEntry.getComponent()));
        component.setComponentVersion(bomEntry.getComponentVersionName());
        component.setComponentVersionURL(getReportVersionUrl(bomEntry.getComponentVersion(), true));
        component.setLicense(bomEntry.getLicenses().get(0).getLicenseDisplay());
        if (bomEntry.getSecurityRiskProfile() != null && bomEntry.getSecurityRiskProfile().getCounts() != null && !bomEntry.getSecurityRiskProfile().getCounts().isEmpty()) {
            for (final RiskCountView count : bomEntry.getSecurityRiskProfile().getCounts()) {
                if (count.getCountType() == RiskCountType.HIGH && count.getCount() > 0) {
                    component.setSecurityRiskHighCount(count.getCount());
                } else if (count.getCountType() == RiskCountType.MEDIUM && count.getCount() > 0) {
                    component.setSecurityRiskMediumCount(count.getCount());
                } else if (count.getCountType() == RiskCountType.LOW && count.getCount() > 0) {
                    component.setSecurityRiskLowCount(count.getCount());
                }
            }
        }
        if (bomEntry.getLicenseRiskProfile() != null && bomEntry.getLicenseRiskProfile().getCounts() != null && !bomEntry.getLicenseRiskProfile().getCounts().isEmpty()) {
            for (final RiskCountView count : bomEntry.getLicenseRiskProfile().getCounts()) {
                if (count.getCountType() == RiskCountType.HIGH && count.getCount() > 0) {
                    component.setLicenseRiskHighCount(count.getCount());
                } else if (count.getCountType() == RiskCountType.MEDIUM && count.getCount() > 0) {
                    component.setLicenseRiskMediumCount(count.getCount());
                } else if (count.getCountType() == RiskCountType.LOW && count.getCount() > 0) {
                    component.setLicenseRiskLowCount(count.getCount());
                }
            }
        }
        if (bomEntry.getOperationalRiskProfile() != null && bomEntry.getOperationalRiskProfile().getCounts() != null && !bomEntry.getOperationalRiskProfile().getCounts().isEmpty()) {
            for (final RiskCountView count : bomEntry.getOperationalRiskProfile().getCounts()) {
                if (count.getCountType() == RiskCountType.HIGH && count.getCount() > 0) {
                    component.setOperationalRiskHighCount(count.getCount());
                } else if (count.getCountType() == RiskCountType.MEDIUM && count.getCount() > 0) {
                    component.setOperationalRiskMediumCount(count.getCount());
                } else if (count.getCountType() == RiskCountType.LOW && count.getCount() > 0) {
                    component.setOperationalRiskLowCount(count.getCount());
                }
            }
        }
        return component;
    }

    public void populatePolicyRuleInfo(final BomComponent component, final VersionBomComponentView bomEntry) throws IntegrationException {
        if (bomEntry != null && bomEntry.getApprovalStatus() != null) {
            final PolicySummaryStatusType status = bomEntry.getApprovalStatus();
            if (status == PolicySummaryStatusType.IN_VIOLATION) {
                final List<PolicyRuleViewV2> rules = blackDuckService.getAllResponses(bomEntry, VersionBomComponentView.POLICY_RULES_LINK_RESPONSE);
                final List<PolicyRule> rulesViolated = new ArrayList<>();
                for (final PolicyRuleViewV2 policyRuleView : rules) {
                    final PolicyRule ruleViolated = new PolicyRule(policyRuleView.getName(), policyRuleView.getDescription());
                    rulesViolated.add(ruleViolated);
                }
                component.setPolicyRulesViolated(rulesViolated);
            }
        }
    }

    private String getBaseUrl() {
        return blackDuckService.getBlackDuckBaseURL().toString();
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
     */
    public String generateBlackDuckNoticesReport(final ProjectVersionView version, final ReportFormatType reportFormat) throws InterruptedException, IntegrationException {
        if (version.hasLink(ProjectVersionView.LICENSEREPORTS_LINK)) {
            try {
                logger.debug("Starting the Notices Report generation.");
                final String reportUrl = startGeneratingBlackDuckNoticesReport(version, reportFormat);

                logger.debug("Waiting for the Notices Report to complete.");
                final ReportView reportInfo = isReportFinishedGenerating(reportUrl);

                final String contentLink = reportInfo.getFirstLink(ReportView.CONTENT_LINK).orElse(null);

                if (contentLink == null) {
                    throw new BlackDuckIntegrationException("Could not find content link for the report at : " + reportUrl);
                }

                logger.debug("Getting the Notices Report content.");
                final String noticesReport = getNoticesReportContent(contentLink);
                logger.debug("Finished retrieving the Notices Report.");
                logger.debug("Cleaning up the Notices Report on the server.");
                deleteBlackDuckReport(reportUrl);
                return noticesReport;
            } catch (final IntegrationRestException e) {
                if (e.getHttpStatusCode() == 402) {
                    // unlike the policy module, the licenseReports link is still present when the module is not enabled
                    logger.warn("Can not post the notice report, the Black Duck notice module is not enabled.");
                } else {
                    throw e;
                }
            }
        } else {
            logger.warn("Can not post the notice report, the Black Duck notice module is not enabled.");
        }
        return null;
    }

    public String startGeneratingBlackDuckNoticesReport(final ProjectVersionView version, final ReportFormatType reportFormat) throws IntegrationException {
        final String reportUri = version.getFirstLink(ProjectVersionView.LICENSEREPORTS_LINK).orElse(null);

        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("reportFormat", reportFormat.toString());
        jsonObject.addProperty("reportType", ReportType.VERSION_LICENSE.toString());

        final String json = blackDuckService.convertToJson(jsonObject);
        final Request request = RequestFactory.createCommonPostRequestBuilder(json).uri(reportUri).build();
        return blackDuckService.executePostRequestAndRetrieveURL(request);
    }

    /**
     * Checks the report URL every 5 seconds until the report has a finished time available, then we know it is done being generated. Throws BlackDuckIntegrationException after 30 minutes if the report has not been generated yet.
     */
    public ReportView isReportFinishedGenerating(final String reportUri) throws InterruptedException, IntegrationException {
        final long startTime = System.currentTimeMillis();
        long elapsedTime = 0;
        Date timeFinished = null;
        ReportView reportInfo = null;

        while (timeFinished == null) {
            reportInfo = blackDuckService.getResponse(reportUri, ReportView.class);
            timeFinished = reportInfo.getFinishedAt();
            if (timeFinished != null) {
                break;
            }
            if (elapsedTime >= timeoutInMilliseconds) {
                final String formattedTime = String.format("%d minutes", TimeUnit.MILLISECONDS.toMinutes(timeoutInMilliseconds));
                throw new BlackDuckIntegrationException("The Report has not finished generating in : " + formattedTime);
            }
            // Retry every 5 seconds
            Thread.sleep(5000);
            elapsedTime = System.currentTimeMillis() - startTime;
        }
        return reportInfo;
    }

    public String getNoticesReportContent(final String reportContentUri) throws IntegrationException {
        final JsonElement fileContent = getReportContentJson(reportContentUri);
        return fileContent.getAsString();
    }

    private JsonElement getReportContentJson(final String reportContentUri) throws IntegrationException {
        try (Response response = blackDuckService.get(reportContentUri)) {
            final String jsonResponse = response.getContentString();

            final JsonObject json = blackDuckService.getGson().fromJson(jsonResponse, JsonObject.class);
            final JsonElement content = json.get("reportContent");
            final JsonArray reportConentArray = content.getAsJsonArray();
            final JsonObject reportFile = reportConentArray.get(0).getAsJsonObject();
            return reportFile.get("fileContent");
        } catch (final IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    public void deleteBlackDuckReport(final String reportUri) throws IntegrationException {
        blackDuckService.delete(reportUri);
    }

}
