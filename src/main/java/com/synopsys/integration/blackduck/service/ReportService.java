/**
 * blackduck-common
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
import com.synopsys.integration.blackduck.api.generated.view.PolicyRuleView;
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

    public ReportService(BlackDuckService blackDuckService, IntLogger logger, ProjectService projectDataService, IntegrationEscapeUtil escapeUtil) {
        this(blackDuckService, logger, projectDataService, escapeUtil, ReportService.DEFAULT_TIMEOUT);
    }

    public ReportService(BlackDuckService blackDuckService, IntLogger logger, ProjectService projectDataService, IntegrationEscapeUtil escapeUtil, long timeoutInMilliseconds) {
        super(blackDuckService, logger);
        this.projectDataService = projectDataService;
        this.escapeUtil = escapeUtil;

        long timeout = timeoutInMilliseconds;
        if (timeoutInMilliseconds <= 0l) {
            timeout = ReportService.DEFAULT_TIMEOUT;
            this.logger.alwaysLog(timeoutInMilliseconds + "ms is not a valid BOM wait time, using : " + timeout + "ms instead");
        }
        this.timeoutInMilliseconds = timeout;
    }

    public String getNoticesReportData(ProjectView project, ProjectVersionView version) throws InterruptedException, IntegrationException {
        logger.trace("Getting the Notices Report Contents using the Report Rest Server");
        return generateBlackDuckNoticesReport(version, ReportFormatType.TEXT);
    }

    public File createNoticesReportFile(File outputDirectory, ProjectView project, ProjectVersionView version) throws InterruptedException, IntegrationException {
        return createNoticesReportFile(outputDirectory, getNoticesReportData(project, version), project.getName(), version.getVersionName());
    }

    private File createNoticesReportFile(File outputDirectory, String noticesReportContent, String projectName, String projectVersionName) throws BlackDuckIntegrationException {
        if (noticesReportContent == null) {
            return null;
        }
        String escapedProjectName = escapeUtil.escapeForUri(projectName);
        String escapedProjectVersionName = escapeUtil.escapeForUri(projectVersionName);
        File noticesReportFile = new File(outputDirectory, escapedProjectName + "_" + escapedProjectVersionName + "_Black_Duck_Notices_Report.txt");
        if (noticesReportFile.exists()) {
            noticesReportFile.delete();
        }
        try (FileWriter writer = new FileWriter(noticesReportFile)) {
            logger.trace("Creating Notices Report in : " + outputDirectory.getCanonicalPath());
            writer.write(noticesReportContent);
            logger.trace("Created Notices Report : " + noticesReportFile.getCanonicalPath());
            return noticesReportFile;
        } catch (IOException e) {
            throw new BlackDuckIntegrationException(e.getMessage(), e);
        }
    }

    public ReportData getRiskReportData(ProjectView project, ProjectVersionView version) throws IntegrationException {
        String originalProjectUrl = project.getHref().orElse(null);
        String originalVersionUrl = version.getHref().orElse(null);
        ReportData reportData = new ReportData();
        reportData.setProjectName(project.getName());
        reportData.setProjectURL(getReportProjectUrl(originalProjectUrl));
        reportData.setProjectVersion(version.getVersionName());
        reportData.setProjectVersionURL(getReportVersionUrl(originalVersionUrl, false));
        reportData.setPhase(version.getPhase().toString());
        reportData.setDistribution(version.getDistribution().toString());
        List<BomComponent> components = new ArrayList<>();
        logger.trace("Getting the Report Contents using the Aggregate Bom Rest Server");
        List<VersionBomComponentView> bomEntries = blackDuckService.getAllResponses(version, ProjectVersionView.COMPONENTS_LINK_RESPONSE);
        boolean policyFailure = false;
        for (VersionBomComponentView bomEntry : bomEntries) {
            BomComponent component = createBomComponentFromBomComponentView(bomEntry);
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
                        PolicyStatusView bomPolicyStatus = blackDuckService.getResponse(componentPolicyStatusURL, PolicyStatusView.class);
                        policyStatus = bomPolicyStatus.getApprovalStatus().toString();
                    } catch (IntegrationException e) {
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

    public void createReportFiles(File outputDirectory, ProjectView project, ProjectVersionView version) throws IntegrationException {
        ReportData reportData = getRiskReportData(project, version);
        createReportFiles(outputDirectory, reportData);
    }

    public void createReportFiles(File outputDirectory, ReportData reportData) throws BlackDuckIntegrationException {
        try {
            logger.trace("Creating Risk Report Files in : " + outputDirectory.getCanonicalPath());
            RiskReportWriter writer = new RiskReportWriter();
            writer.createHtmlReportFiles(blackDuckService.getGson(), outputDirectory, reportData);
        } catch (RiskReportException | IOException e) {
            throw new BlackDuckIntegrationException(e.getMessage(), e);
        }
    }

    public File createReportPdfFile(File outputDirectory, ProjectView project, ProjectVersionView version) throws IntegrationException {
        ReportData reportData = getRiskReportData(project, version);
        return createReportPdfFile(outputDirectory, reportData);
    }

    public File createReportPdfFile(File outputDirectory, ReportData reportData) throws BlackDuckIntegrationException {
        try {
            logger.trace("Creating Risk Report Pdf in : " + outputDirectory.getCanonicalPath());
            RiskReportPdfWriter writer = new RiskReportPdfWriter(logger);
            File pdfFile = writer.createPDFReportFile(outputDirectory, reportData);
            logger.trace("Created Risk Report Pdf : " + pdfFile.getCanonicalPath());
            return pdfFile;
        } catch (RiskReportException | IOException e) {
            throw new BlackDuckIntegrationException(e.getMessage(), e);
        }
    }

    private String getComponentPolicyURL(String versionURL, String componentURL) {
        String componentVersionSegments = componentURL.substring(componentURL.indexOf("components"));
        return versionURL + "/" + componentVersionSegments + "/" + "policy-status";
    }

    private BomComponent createBomComponentFromBomComponentView(VersionBomComponentView bomEntry) {
        BomComponent component = new BomComponent();
        component.setComponentName(bomEntry.getComponentName());
        component.setComponentURL(getReportComponentUrl(bomEntry.getComponent()));
        component.setComponentVersion(bomEntry.getComponentVersionName());
        component.setComponentVersionURL(getReportComponentVersionUrl(bomEntry.getComponent(), bomEntry.getComponentVersion()));
        component.setLicense(bomEntry.getLicenses().get(0).getLicenseDisplay());
        if (bomEntry.getSecurityRiskProfile() != null && bomEntry.getSecurityRiskProfile().getCounts() != null && !bomEntry.getSecurityRiskProfile().getCounts().isEmpty()) {
            for (RiskCountView count : bomEntry.getSecurityRiskProfile().getCounts()) {
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
            for (RiskCountView count : bomEntry.getLicenseRiskProfile().getCounts()) {
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
            for (RiskCountView count : bomEntry.getOperationalRiskProfile().getCounts()) {
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

    public void populatePolicyRuleInfo(BomComponent component, VersionBomComponentView bomEntry) throws IntegrationException {
        if (bomEntry != null && bomEntry.getApprovalStatus() != null) {
            PolicySummaryStatusType status = bomEntry.getApprovalStatus();
            if (status == PolicySummaryStatusType.IN_VIOLATION) {
                List<PolicyRuleView> rules = blackDuckService.getAllResponses(bomEntry, VersionBomComponentView.POLICY_RULES_LINK_RESPONSE);
                List<PolicyRule> rulesViolated = new ArrayList<>();
                for (PolicyRuleView policyRuleView : rules) {
                    PolicyRule ruleViolated = new PolicyRule(policyRuleView.getName(), policyRuleView.getDescription());
                    rulesViolated.add(ruleViolated);
                }
                component.setPolicyRulesViolated(rulesViolated);
            }
        }
    }

    private String getBaseUrl() {
        return blackDuckService.getBlackDuckBaseUrl().toString();
    }

    private String getReportProjectUrl(String projectURL) {
        if (projectURL == null) {
            return null;
        }
        String projectId = projectURL.substring(projectURL.lastIndexOf("/") + 1);
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(getBaseUrl());
        urlBuilder.append("#");
        urlBuilder.append("projects/id:");
        urlBuilder.append(projectId);

        return urlBuilder.toString();
    }

    private String getReportVersionUrl(String versionURL, boolean isComponent) {
        if (versionURL == null) {
            return null;
        }
        String versionId = versionURL.substring(versionURL.lastIndexOf("/") + 1);
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(getBaseUrl());
        urlBuilder.append("#");
        urlBuilder.append("versions/id:");
        urlBuilder.append(versionId);
        if (!isComponent) {
            urlBuilder.append("/view:bom");
        }
        return urlBuilder.toString();
    }

    private String getReportComponentUrl(String componentUrl) {
        if (componentUrl == null) {
            return null;
        }
        String componentId = componentUrl.substring(componentUrl.lastIndexOf("/") + 1);
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(getBaseUrl());
        urlBuilder.append("/api/components/");
        urlBuilder.append(componentId);

        return urlBuilder.toString();
    }

    private String getReportComponentVersionUrl(String componentUrl, String versionUrl) {
        if (componentUrl == null || versionUrl == null) {
            return null;
        }
        String componentId = componentUrl.substring(componentUrl.lastIndexOf("/") + 1);
        String versionId = versionUrl.substring(versionUrl.lastIndexOf("/") + 1);
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(getBaseUrl());
        urlBuilder.append("/api/components/");
        urlBuilder.append(componentId);
        urlBuilder.append("/versions/");
        urlBuilder.append(versionId);

        return urlBuilder.toString();
    }

    /**
     * Assumes the BOM has already been updated
     */
    public String generateBlackDuckNoticesReport(ProjectVersionView version, ReportFormatType reportFormat) throws InterruptedException, IntegrationException {
        if (version.hasLink(ProjectVersionView.LICENSEREPORTS_LINK)) {
            try {
                logger.debug("Starting the Notices Report generation.");
                String reportUrl = startGeneratingBlackDuckNoticesReport(version, reportFormat);

                logger.debug("Waiting for the Notices Report to complete.");
                ReportView reportInfo = isReportFinishedGenerating(reportUrl);

                String contentLink = reportInfo.getFirstLink(ReportView.CONTENT_LINK).orElse(null);

                if (contentLink == null) {
                    throw new BlackDuckIntegrationException("Could not find content link for the report at : " + reportUrl);
                }

                logger.debug("Getting the Notices Report content.");
                String noticesReport = getNoticesReportContent(contentLink);
                logger.debug("Finished retrieving the Notices Report.");
                logger.debug("Cleaning up the Notices Report on the server.");
                deleteBlackDuckReport(reportUrl);
                return noticesReport;
            } catch (IntegrationRestException e) {
                if (e.getHttpStatusCode() == 402) {
                    // unlike the policy module, the licenseReports link is still present when the module is not enabled
                    logger.warn("Can not create the notice report, the Black Duck notice module is not enabled.");
                } else {
                    throw e;
                }
            }
        } else {
            logger.warn("Can not create the notice report, the Black Duck notice module is not enabled.");
        }
        return null;
    }

    public String startGeneratingBlackDuckNoticesReport(ProjectVersionView version, ReportFormatType reportFormat) throws IntegrationException {
        String reportUri = version.getFirstLink(ProjectVersionView.LICENSEREPORTS_LINK).orElse(null);

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("reportFormat", reportFormat.toString());
        jsonObject.addProperty("reportType", ReportType.VERSION_LICENSE.toString());

        String json = blackDuckService.convertToJson(jsonObject);
        Request request = RequestFactory.createCommonPostRequestBuilder(json).uri(reportUri).build();
        return blackDuckService.executePostRequestAndRetrieveURL(request);
    }

    /**
     * Checks the report URL every 5 seconds until the report has a finished time available, then we know it is done being generated. Throws BlackDuckIntegrationException after 30 minutes if the report has not been generated yet.
     */
    public ReportView isReportFinishedGenerating(String reportUri) throws InterruptedException, IntegrationException {
        long startTime = System.currentTimeMillis();
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
                String formattedTime = String.format("%d minutes", TimeUnit.MILLISECONDS.toMinutes(timeoutInMilliseconds));
                throw new BlackDuckIntegrationException("The Report has not finished generating in : " + formattedTime);
            }
            // Retry every 5 seconds
            Thread.sleep(5000);
            elapsedTime = System.currentTimeMillis() - startTime;
        }
        return reportInfo;
    }

    public String getNoticesReportContent(String reportContentUri) throws IntegrationException {
        JsonElement fileContent = getReportContentJson(reportContentUri);
        return fileContent.getAsString();
    }

    private JsonElement getReportContentJson(String reportContentUri) throws IntegrationException {
        try (Response response = blackDuckService.get(reportContentUri)) {
            String jsonResponse = response.getContentString();

            JsonObject json = blackDuckService.getGson().fromJson(jsonResponse, JsonObject.class);
            JsonElement content = json.get("reportContent");
            JsonArray reportConentArray = content.getAsJsonArray();
            JsonObject reportFile = reportConentArray.get(0).getAsJsonObject();
            return reportFile.get("fileContent");
        } catch (IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    public void deleteBlackDuckReport(String reportUri) throws IntegrationException {
        blackDuckService.delete(reportUri);
    }

}
