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
package com.blackducksoftware.integration.hub.dataservice.bom;

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
import com.blackducksoftware.integration.log.IntLogger;

public class AggregateBomDataService extends HubRequestService {

    private final ProjectRequestService projectRequestService;

    private final ProjectVersionRequestService projectVersionRequestService;

    private final ReportRequestService reportRequestService;

    public AggregateBomDataService(final IntLogger logger, final RestConnection restConnection, final ProjectRequestService projectRequestService,
            final ProjectVersionRequestService projectVersionRequestService, final ReportRequestService reportRequestService) {
        super(restConnection);
        this.projectRequestService = projectRequestService;
        this.projectVersionRequestService = projectVersionRequestService;
        this.reportRequestService = reportRequestService;

    }

    public HubRiskReportData createRiskReport(final String projectName, final String projectVersionName)
            throws HubIntegrationException {
        final ReportCategoriesEnum[] categories = { ReportCategoriesEnum.VERSION, ReportCategoriesEnum.COMPONENTS };
        return createRiskReport(projectName, projectVersionName, categories);
    }

    public HubRiskReportData createRiskReport(final String projectName, final String projectVersionName,
            final ReportCategoriesEnum[] categories) throws HubIntegrationException {
        final ProjectItem project = projectRequestService.getProjectByName(projectName);
        final ProjectVersionItem version = projectVersionRequestService.getProjectVersion(project, projectVersionName);
        return reportRequestService.generateHubReport(version, ReportFormatEnum.JSON, categories);
    }

    public HubRiskReportData createRiskReport(final ProjectVersionItem version)
            throws HubIntegrationException {
        final ReportCategoriesEnum[] categories = { ReportCategoriesEnum.VERSION, ReportCategoriesEnum.COMPONENTS };
        return createRiskReport(version, categories);
    }

    public HubRiskReportData createRiskReport(final ProjectVersionItem version,
            final ReportCategoriesEnum[] categories) throws HubIntegrationException {
        return reportRequestService.generateHubReport(version, ReportFormatEnum.JSON, categories);
    }

    public void createRiskReportFiles(final File outputDirectory, final String projectName, final String projectVersionName) throws HubIntegrationException {
        final ReportCategoriesEnum[] categories = { ReportCategoriesEnum.VERSION, ReportCategoriesEnum.COMPONENTS };
        createRiskReportFiles(outputDirectory, projectName, projectVersionName, categories);
    }

    public void createRiskReportFiles(final File outputDirectory, final String projectName, final String projectVersionName,
            final ReportCategoriesEnum[] categories) throws HubIntegrationException {
        final HubRiskReportData riskreportData = createRiskReport(projectName, projectVersionName, categories);
        createRiskReportFiles(outputDirectory, riskreportData);
    }

    public void createRiskReportFiles(final File outputDirectory, final ProjectVersionItem version) throws HubIntegrationException {
        final ReportCategoriesEnum[] categories = { ReportCategoriesEnum.VERSION, ReportCategoriesEnum.COMPONENTS };
        createRiskReportFiles(outputDirectory, version, categories);
    }

    public void createRiskReportFiles(final File outputDirectory, final ProjectVersionItem version,
            final ReportCategoriesEnum[] categories) throws HubIntegrationException {
        final HubRiskReportData riskreportData = createRiskReport(version, categories);
        createRiskReportFiles(outputDirectory, riskreportData);
    }

    public void createRiskReportFiles(final File outputDirectory, final HubRiskReportData riskreportData) throws HubIntegrationException {
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
