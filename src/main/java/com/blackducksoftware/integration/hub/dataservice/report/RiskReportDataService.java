/*
 * Copyright (C) 2016 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.hub.dataservice.report;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.blackducksoftware.integration.hub.api.project.ProjectItem;
import com.blackducksoftware.integration.hub.api.project.ProjectRequestService;
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionItem;
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionRequestService;
import com.blackducksoftware.integration.hub.api.report.HubRiskReportData;
import com.blackducksoftware.integration.hub.api.report.ReportCategoriesEnum;
import com.blackducksoftware.integration.hub.api.report.ReportFormatEnum;
import com.blackducksoftware.integration.hub.api.report.ReportRequestService;
import com.blackducksoftware.integration.hub.api.report.RiskReportResourceCopier;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubRequestService;

public class RiskReportDataService extends HubRequestService {
    private final ProjectRequestService projectRequestService;

    private final ProjectVersionRequestService projectVersionRequestService;

    private final ReportRequestService reportRequestService;

    public RiskReportDataService(final RestConnection restConnection, final ProjectRequestService projectRequestService,
            final ProjectVersionRequestService projectVersionRequestService, final ReportRequestService reportRequestService) {
        super(restConnection);
        this.projectRequestService = projectRequestService;
        this.projectVersionRequestService = projectVersionRequestService;
        this.reportRequestService = reportRequestService;
    }

    public HubRiskReportData createRiskReport(String projectName, String projectVersionName) throws HubIntegrationException {
        final ReportCategoriesEnum[] categories = { ReportCategoriesEnum.VERSION, ReportCategoriesEnum.COMPONENTS };
        return createRiskReport(projectName, projectVersionName, ReportRequestService.MAXIMUM_WAIT, categories);
    }

    public HubRiskReportData createRiskReport(String projectName, String projectVersionName, long maximumWaitInMilliSeconds) throws HubIntegrationException {
        final ReportCategoriesEnum[] categories = { ReportCategoriesEnum.VERSION, ReportCategoriesEnum.COMPONENTS };
        return createRiskReport(projectName, projectVersionName, maximumWaitInMilliSeconds, categories);
    }

    public HubRiskReportData createRiskReport(String projectName, String projectVersionName, long maximumWaitInMilliSeconds,
            ReportCategoriesEnum[] categories) throws HubIntegrationException {
        final ProjectItem project = projectRequestService.getProjectByName(projectName);
        final ProjectVersionItem version = projectVersionRequestService.getProjectVersion(project, projectVersionName);
        return reportRequestService.generateHubReport(version, ReportFormatEnum.JSON, categories, maximumWaitInMilliSeconds);
    }

    public void createRiskReportFiles(final File outputDirectory, String projectName, String projectVersionName) throws HubIntegrationException {
        final ReportCategoriesEnum[] categories = { ReportCategoriesEnum.VERSION, ReportCategoriesEnum.COMPONENTS };
        createRiskReportFiles(outputDirectory, projectName, projectVersionName, ReportRequestService.MAXIMUM_WAIT, categories);
    }

    public void createRiskReportFiles(final File outputDirectory, String projectName, String projectVersionName, long maximumWait)
            throws HubIntegrationException {
        final ReportCategoriesEnum[] categories = { ReportCategoriesEnum.VERSION, ReportCategoriesEnum.COMPONENTS };
        createRiskReportFiles(outputDirectory, projectName, projectVersionName, maximumWait, categories);
    }

    public void createRiskReportFiles(final File outputDirectory, String projectName, String projectVersionName, long maximumWaitInMilliSeconds,
            ReportCategoriesEnum[] categories) throws HubIntegrationException {
        final HubRiskReportData riskreportData = createRiskReport(projectName, projectVersionName, maximumWaitInMilliSeconds, categories);
        try {
            final RiskReportResourceCopier copier = new RiskReportResourceCopier(outputDirectory.getCanonicalPath());
            File htmlFile = null;
            try {
                final List<File> writtenFiles = copier.copy();
                for (final File file : writtenFiles) {
                    if (file.getName().equals(RiskReportResourceCopier.RISK_REPORT_HTML_FILE_NAME)) {
                        htmlFile = file;
                        break;
                    }
                }
            } catch (final URISyntaxException e) {
                throw new HubIntegrationException("Couldn't create the report: " + e.getMessage(), e);
            }
            if (htmlFile == null) {
                throw new HubIntegrationException("Could not find the file : " + RiskReportResourceCopier.RISK_REPORT_HTML_FILE_NAME
                        + ", the report files must not have been copied into the report directory.");
            }
            String htmlFileString = FileUtils.readFileToString(htmlFile, "UTF-8");
            final String reportString = getRestConnection().getGson().toJson(riskreportData);
            htmlFileString = htmlFileString.replace(RiskReportResourceCopier.JSON_TOKEN_TO_REPLACE, reportString);
            FileUtils.writeStringToFile(htmlFile, htmlFileString, "UTF-8");
        } catch (final IOException e) {
            throw new HubIntegrationException("Couldn't create the report: " + e.getMessage(), e);
        }
    }
}
