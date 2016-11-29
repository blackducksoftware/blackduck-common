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
package com.blackducksoftware.integration.hub.dataservices.report;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.blackducksoftware.integration.hub.api.HubRestService;
import com.blackducksoftware.integration.hub.api.project.ProjectItem;
import com.blackducksoftware.integration.hub.api.project.ProjectRestService;
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionItem;
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionRestService;
import com.blackducksoftware.integration.hub.api.report.HubRiskReportData;
import com.blackducksoftware.integration.hub.api.report.ReportCategoriesEnum;
import com.blackducksoftware.integration.hub.api.report.ReportFormatEnum;
import com.blackducksoftware.integration.hub.api.report.ReportRestService;
import com.blackducksoftware.integration.hub.api.report.RiskReportResourceCopier;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.exception.ProjectDoesNotExistException;
import com.blackducksoftware.integration.hub.exception.UnexpectedHubResponseException;
import com.blackducksoftware.integration.hub.rest.RestConnection;

public class RiskReportDataService extends HubRestService {

    private final ProjectRestService projectRestService;

    private final ProjectVersionRestService projectVersionRestService;

    private final ReportRestService reportRestService;

    public RiskReportDataService(final RestConnection restConnection, final ProjectRestService projectRestService,
            final ProjectVersionRestService projectVersionRestService, final ReportRestService reportRestService) {
        super(restConnection);
        this.projectRestService = projectRestService;
        this.projectVersionRestService = projectVersionRestService;
        this.reportRestService = reportRestService;
    }

    public void createRiskReport(final File outputDirectory, String projectName, String projectVersionName)
            throws IOException, BDRestException, URISyntaxException, ProjectDoesNotExistException, HubIntegrationException, InterruptedException,
            UnexpectedHubResponseException {
        final ReportCategoriesEnum[] categories = { ReportCategoriesEnum.VERSION, ReportCategoriesEnum.COMPONENTS };
        createRiskReport(outputDirectory, projectName, projectVersionName, categories);
    }

    public void createRiskReport(final File outputDirectory, String projectName, String projectVersionName, ReportCategoriesEnum[] categories)
            throws IOException, BDRestException, URISyntaxException, ProjectDoesNotExistException, HubIntegrationException,
            InterruptedException,
            UnexpectedHubResponseException {
        ProjectItem project = projectRestService.getProjectByName(projectName);
        ProjectVersionItem version = projectVersionRestService.getProjectVersion(project, projectVersionName);
        HubRiskReportData riskreportData = reportRestService.generateHubReport(version, ReportFormatEnum.JSON, categories);
        RiskReportResourceCopier copier = new RiskReportResourceCopier(outputDirectory.getCanonicalPath());
        List<File> writtenFiles = copier.copy();
        File htmlFile = null;
        for (File file : writtenFiles) {
            if (file.getName().equals(RiskReportResourceCopier.RISK_REPORT_HTML_FILE_NAME)) {
                htmlFile = file;
                break;
            }
        }
        if (htmlFile == null) {
            throw new FileNotFoundException("Could not find the file : " + RiskReportResourceCopier.RISK_REPORT_HTML_FILE_NAME
                    + ", the report files must not have been copied into the report directory.");
        }
        String htmlFileString = FileUtils.readFileToString(htmlFile, "UTF-8");
        String reportString = getRestConnection().getGson().toJson(riskreportData);
        htmlFileString = htmlFileString.replace(RiskReportResourceCopier.JSON_TOKEN_TO_REPLACE, reportString);
        FileUtils.writeStringToFile(htmlFile, htmlFileString, "UTF-8");
    }
}
