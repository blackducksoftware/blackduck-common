/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.service.dataservice;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.synopsys.integration.blackduck.api.generated.enumeration.ProjectVersionComponentPolicyStatusType;
import com.synopsys.integration.blackduck.api.generated.enumeration.ReportFormatType;
import com.synopsys.integration.blackduck.api.generated.view.CodeLocationView;
import com.synopsys.integration.blackduck.api.generated.view.ComponentPolicyRulesView;
import com.synopsys.integration.blackduck.api.generated.deprecated.view.PolicyStatusView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionComponentView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.ReportView;
import com.synopsys.integration.blackduck.api.generated.enumeration.ReportType;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.blackduck.exception.RiskReportException;
import com.synopsys.integration.blackduck.http.BlackDuckRequestFactory;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.DataService;
import com.synopsys.integration.blackduck.service.model.BomComponent;
import com.synopsys.integration.blackduck.service.model.PolicyRule;
import com.synopsys.integration.blackduck.service.model.ReportData;
import com.synopsys.integration.blackduck.service.model.pdf.FontLoader;
import com.synopsys.integration.blackduck.service.model.pdf.RiskReportPdfWriter;
import com.synopsys.integration.blackduck.service.model.pdf.RiskReportWriter;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.exception.IntegrationRestException;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.response.Response;
import com.synopsys.integration.util.IntegrationEscapeUtil;

public class ReportService extends DataService {
    public final static long DEFAULT_TIMEOUT = 1000L * 60 * 5;

    private final ProjectService projectDataService;
    private final IntegrationEscapeUtil escapeUtil;
    private final long timeoutInMilliseconds;
    private final HttpUrl blackDuckBaseUrl;
    private final Gson gson;

    public ReportService(Gson gson, HttpUrl blackDuckBaseUrl, BlackDuckApiClient blackDuckApiClient, BlackDuckRequestFactory blackDuckRequestFactory, IntLogger logger, ProjectService projectDataService, IntegrationEscapeUtil escapeUtil) {
        this(gson, blackDuckBaseUrl, blackDuckApiClient, blackDuckRequestFactory, logger, projectDataService, escapeUtil, ReportService.DEFAULT_TIMEOUT);
    }

    public ReportService(Gson gson, HttpUrl blackDuckBaseUrl, BlackDuckApiClient blackDuckApiClient, BlackDuckRequestFactory blackDuckRequestFactory, IntLogger logger, ProjectService projectDataService,
        IntegrationEscapeUtil escapeUtil, long timeoutInMilliseconds) {
        super(blackDuckApiClient, blackDuckRequestFactory, logger);
        this.projectDataService = projectDataService;
        this.escapeUtil = escapeUtil;

        long timeout = timeoutInMilliseconds;
        if (timeoutInMilliseconds <= 0l) {
            timeout = ReportService.DEFAULT_TIMEOUT;
            this.logger.alwaysLog(timeoutInMilliseconds + "ms is not a valid BOM wait time, using : " + timeout + "ms instead");
        }
        this.timeoutInMilliseconds = timeout;
        this.gson = gson;
        this.blackDuckBaseUrl = blackDuckBaseUrl;
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
        String escapedProjectName = escapeUtil.replaceWithUnderscore(projectName);
        String escapedProjectVersionName = escapeUtil.replaceWithUnderscore(projectVersionName);
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
        HttpUrl originalProjectUrl = project.getHref();
        HttpUrl originalVersionUrl = version.getHref();
        ReportData reportData = new ReportData();
        reportData.setProjectName(project.getName());
        reportData.setProjectURL(getReportProjectUrl(originalProjectUrl));
        reportData.setProjectVersion(version.getVersionName());
        reportData.setProjectVersionURL(getReportVersionUrl(originalVersionUrl, false));
        reportData.setPhase(version.getPhase().toString());
        reportData.setDistribution(version.getDistribution().toString());
        List<BomComponent> components = new ArrayList<>();
        logger.trace("Getting the Report Contents using the Aggregate Bom Rest Server");
        List<ProjectVersionComponentView> bomEntries = blackDuckApiClient.getAllResponses(version, ProjectVersionView.COMPONENTS_LINK_RESPONSE);
        boolean policyFailure = false;
        for (ProjectVersionComponentView ProjectVersionComponentView : bomEntries) {
            String policyStatus = ProjectVersionComponentView.getApprovalStatus().toString();
            if (StringUtils.isBlank(policyStatus)) {
                HttpUrl componentPolicyStatusURL = null;
                if (!StringUtils.isBlank(ProjectVersionComponentView.getComponentVersion())) {
                    componentPolicyStatusURL = getComponentPolicyURL(originalVersionUrl, ProjectVersionComponentView.getComponentVersion());
                } else {
                    componentPolicyStatusURL = getComponentPolicyURL(originalVersionUrl, ProjectVersionComponentView.getComponent());
                }
                if (!policyFailure) {
                    // FIXME if we could check if Black Duck has the policy module we could remove a lot of the mess
                    try {
                        PolicyStatusView bomPolicyStatus = blackDuckApiClient.getResponse(componentPolicyStatusURL, PolicyStatusView.class);
                        policyStatus = bomPolicyStatus.getApprovalStatus().toString();
                    } catch (IntegrationException e) {
                        policyFailure = true;
                        logger.debug("Could not get the component policy status, the Black Duck policy module is not enabled");
                    }
                }
            }

            BomComponent component = createBomComponentFromBomComponentView(ProjectVersionComponentView);
            component.setPolicyStatus(policyStatus);
            populatePolicyRuleInfo(component, ProjectVersionComponentView);
            components.add(component);
        }
        reportData.setComponents(components);
        LocalDateTime dateTime = getDateTimeOfLatestScanForProjectVersion(version, project.getName());
        reportData.setDateTimeOfLatestScan(dateTime);

        return reportData;
    }

    private LocalDateTime getDateTimeOfLatestScanForProjectVersion(ProjectVersionView projectVersion, String projectName) throws IntegrationException {
        List<CodeLocationView> codeLocations = blackDuckApiClient.getAllResponses(projectVersion, ProjectVersionView.CODELOCATIONS_LINK_RESPONSE);
        if (codeLocations.isEmpty()) {
            logger.info(String.format("Could not find any code locations for %s - %s", projectName, projectVersion.getVersionName()));
            return null;
        }

        Date dateOfLatestScan = Collections.max(codeLocations.stream()
                                                    .map(CodeLocationView::getUpdatedAt)
                                                    .collect(Collectors.toList()));

        return convertDateToLocalDateTime(dateOfLatestScan);
    }

    private LocalDateTime convertDateToLocalDateTime(Date date) {
        return date.toInstant()
                   .atZone(ZoneId.systemDefault())
                   .toLocalDateTime();
    }

    public void createReportFiles(File outputDirectory, ProjectView project, ProjectVersionView version) throws IntegrationException {
        ReportData reportData = getRiskReportData(project, version);
        createReportFiles(outputDirectory, reportData);
    }

    public void createReportFiles(File outputDirectory, ReportData reportData) throws BlackDuckIntegrationException {
        try {
            logger.trace("Creating Risk Report Files in : " + outputDirectory.getCanonicalPath());
            RiskReportWriter writer = new RiskReportWriter();
            writer.createHtmlReportFiles(gson, outputDirectory, reportData);
        } catch (RiskReportException | IOException e) {
            throw new BlackDuckIntegrationException(e.getMessage(), e);
        }
    }

    public File createReportPdfFile(File outputDirectory, ProjectView project, ProjectVersionView version) throws IntegrationException {
        return createReportPdfFile(outputDirectory, project, version, document -> PDType1Font.HELVETICA, document -> PDType1Font.HELVETICA_BOLD);
    }

    public File createReportPdfFile(File outputDirectory, ProjectView project, ProjectVersionView version, FontLoader fontLoader, FontLoader boldFontLoader) throws IntegrationException {
        ReportData reportData = getRiskReportData(project, version);
        return createReportPdfFile(outputDirectory, reportData, fontLoader, boldFontLoader);
    }

    public File createReportPdfFile(File outputDirectory, ReportData reportData) throws BlackDuckIntegrationException {
        return createReportPdfFile(outputDirectory, reportData, document -> PDType1Font.HELVETICA, document -> PDType1Font.HELVETICA_BOLD);
    }

    public File createReportPdfFile(File outputDirectory, ReportData reportData, FontLoader fontLoader, FontLoader boldFontLoader) throws BlackDuckIntegrationException {
        try {
            logger.trace("Creating Risk Report Pdf in : " + outputDirectory.getCanonicalPath());
            RiskReportPdfWriter writer = new RiskReportPdfWriter(logger, fontLoader, boldFontLoader, Color.BLACK, 10.0f);
            File pdfFile = writer.createPDFReportFile(outputDirectory, reportData);
            logger.trace("Created Risk Report Pdf : " + pdfFile.getCanonicalPath());
            return pdfFile;
        } catch (RiskReportException | IOException e) {
            throw new BlackDuckIntegrationException(e.getMessage(), e);
        }
    }

    private HttpUrl getComponentPolicyURL(HttpUrl versionURL, String componentURL) throws IntegrationException {
        String componentVersionSegments = componentURL.substring(componentURL.indexOf("components"));
        return new HttpUrl(versionURL.string() + "/" + componentVersionSegments + "/" + "policy-status");
    }

    private BomComponent createBomComponentFromBomComponentView(ProjectVersionComponentView bomEntry) {
        BomComponent component = new BomComponent();
        component.setComponentName(bomEntry.getComponentName());
        component.setComponentURL(bomEntry.getComponent());
        component.setComponentVersion(bomEntry.getComponentVersionName());
        component.setComponentVersionURL(bomEntry.getComponentVersion());
        component.setLicense(bomEntry.getLicenses().get(0).getLicenseDisplay());
        component.addSecurityRiskProfile(bomEntry.getSecurityRiskProfile());
        component.addLicenseRiskProfile(bomEntry.getLicenseRiskProfile());
        component.addOperationalRiskProfile(bomEntry.getOperationalRiskProfile());

        return component;
    }

    public void populatePolicyRuleInfo(BomComponent component, ProjectVersionComponentView bomEntry) throws IntegrationException {
        if (bomEntry != null && bomEntry.getApprovalStatus() != null) {
            ProjectVersionComponentPolicyStatusType status = bomEntry.getApprovalStatus();
            if (status == ProjectVersionComponentPolicyStatusType.IN_VIOLATION) {
                List<ComponentPolicyRulesView> rules = blackDuckApiClient.getAllResponses(bomEntry, ProjectVersionComponentView.POLICY_RULES_LINK_RESPONSE);
                List<PolicyRule> rulesViolated = new ArrayList<>();
                for (ComponentPolicyRulesView policyRuleView : rules) {
                    PolicyRule ruleViolated = new PolicyRule(policyRuleView.getName(), policyRuleView.getDescription());
                    rulesViolated.add(ruleViolated);
                }
                component.setPolicyRulesViolated(rulesViolated);
            }
        }
    }

    private String getReportProjectUrl(HttpUrl projectURL) {
        if (projectURL == null) {
            return null;
        }
        String projectId = projectURL.string().substring(projectURL.string().lastIndexOf("/") + 1);
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(blackDuckBaseUrl);
        urlBuilder.append("#");
        urlBuilder.append("projects/id:");
        urlBuilder.append(projectId);

        return urlBuilder.toString();
    }

    private String getReportVersionUrl(HttpUrl versionURL, boolean isComponent) {
        if (versionURL == null) {
            return null;
        }
        String versionId = versionURL.string().substring(versionURL.string().lastIndexOf("/") + 1);
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(blackDuckBaseUrl);
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
    public String generateBlackDuckNoticesReport(ProjectVersionView version, ReportFormatType reportFormat) throws InterruptedException, IntegrationException {
        if (version.hasLink(ProjectVersionView.LICENSEREPORTS_LINK)) {
            try {
                logger.debug("Starting the Notices Report generation.");
                HttpUrl reportUrl = startGeneratingBlackDuckNoticesReport(version, reportFormat);

                logger.debug("Waiting for the Notices Report to complete.");
                ReportView reportInfo = isReportFinishedGenerating(reportUrl);

                HttpUrl contentUrl = reportInfo.getFirstLink(ReportView.CONTENT_LINK);
                if (contentUrl == null) {
                    throw new BlackDuckIntegrationException("Could not find content link for the report at : " + reportUrl);
                }

                logger.debug("Getting the Notices Report content.");
                String noticesReport = getNoticesReportContent(contentUrl);
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

    public HttpUrl startGeneratingBlackDuckNoticesReport(ProjectVersionView version, ReportFormatType reportFormat) throws IntegrationException {
        HttpUrl reportUrl = version.getFirstLink(ProjectVersionView.LICENSEREPORTS_LINK);

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("reportFormat", reportFormat.toString());
        jsonObject.addProperty("reportType", ReportType.VERSION_LICENSE.toString());

        String json = blackDuckApiClient.convertToJson(jsonObject);
        Request request = blackDuckRequestFactory.createCommonPostRequestBuilder(reportUrl, json).build();
        return blackDuckApiClient.executePostRequestAndRetrieveURL(request);
    }

    /**
     * Checks the report URL every 5 seconds until the report has a finished time available, then we know it is done being generated. Throws BlackDuckIntegrationException after 30 minutes if the report has not been generated yet.
     */
    public ReportView isReportFinishedGenerating(HttpUrl reportUrl) throws InterruptedException, IntegrationException {
        long startTime = System.currentTimeMillis();
        long elapsedTime = 0;
        Date timeFinished = null;
        ReportView reportInfo = null;

        while (timeFinished == null) {
            reportInfo = blackDuckApiClient.getResponse(reportUrl, ReportView.class);
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

    public String getNoticesReportContent(HttpUrl reportContentUrl) throws IntegrationException {
        JsonElement fileContent = getReportContentJson(reportContentUrl);
        return fileContent.getAsString();
    }

    private JsonElement getReportContentJson(HttpUrl reportContentUri) throws IntegrationException {
        try (Response response = blackDuckApiClient.get(reportContentUri)) {
            String jsonResponse = response.getContentString();

            JsonObject json = gson.fromJson(jsonResponse, JsonObject.class);
            JsonElement content = json.get("reportContent");
            JsonArray reportConentArray = content.getAsJsonArray();
            JsonObject reportFile = reportConentArray.get(0).getAsJsonObject();
            return reportFile.get("fileContent");
        } catch (IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    public void deleteBlackDuckReport(HttpUrl reportUri) throws IntegrationException {
        blackDuckApiClient.delete(reportUri);
    }

}
