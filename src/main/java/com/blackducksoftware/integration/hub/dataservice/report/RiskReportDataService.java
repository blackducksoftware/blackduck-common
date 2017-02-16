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
package com.blackducksoftware.integration.hub.dataservice.report;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.blackducksoftware.integration.hub.HubSupportHelper;
import com.blackducksoftware.integration.hub.api.aggregate.bom.AggregateBomRequestService;
import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.api.project.ProjectItem;
import com.blackducksoftware.integration.hub.api.project.ProjectRequestService;
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionItem;
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionRequestService;
import com.blackducksoftware.integration.hub.api.report.AggregateBomViewEntry;
import com.blackducksoftware.integration.hub.api.report.HubRiskReportData;
import com.blackducksoftware.integration.hub.api.report.ReportCategoriesEnum;
import com.blackducksoftware.integration.hub.api.report.ReportFormatEnum;
import com.blackducksoftware.integration.hub.api.report.ReportRequestService;
import com.blackducksoftware.integration.hub.api.report.RiskReportResourceCopier;
import com.blackducksoftware.integration.hub.api.report.VersionReport;
import com.blackducksoftware.integration.hub.api.view.BomComponentPolicyStatusView;
import com.blackducksoftware.integration.hub.api.view.CountTypeEnum;
import com.blackducksoftware.integration.hub.api.view.RiskCountView;
import com.blackducksoftware.integration.hub.api.view.VersionBomComponentView;
import com.blackducksoftware.integration.hub.capability.HubCapabilitiesEnum;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.report.BomComponent;
import com.blackducksoftware.integration.hub.report.ReportData;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubRequestService;
import com.blackducksoftware.integration.log.IntLogger;

public class RiskReportDataService extends HubRequestService {

    private final ProjectRequestService projectRequestService;

    private final ProjectVersionRequestService projectVersionRequestService;

    private final ReportRequestService reportRequestService;

    private final AggregateBomRequestService bomRequestService;

    private final HubRequestService requestService;

    private final MetaService metaService;

    private final HubSupportHelper hubSupportHelper;

    public RiskReportDataService(final IntLogger logger, final RestConnection restConnection, final ProjectRequestService projectRequestService,
            final ProjectVersionRequestService projectVersionRequestService, final ReportRequestService reportRequestService,
            final AggregateBomRequestService bomRequestService, final HubRequestService requestService,
            final MetaService metaService, final HubSupportHelper hubSupportHelper) {
        super(restConnection);
        this.projectRequestService = projectRequestService;
        this.projectVersionRequestService = projectVersionRequestService;
        this.reportRequestService = reportRequestService;
        this.bomRequestService = bomRequestService;
        this.requestService = requestService;
        this.metaService = metaService;
        this.hubSupportHelper = hubSupportHelper;

    }

    public ReportData getRiskReportData(final String projectName, final String projectVersionName)
            throws HubIntegrationException {
        final ProjectItem project = projectRequestService.getProjectByName(projectName);
        final ProjectVersionItem version = projectVersionRequestService.getProjectVersion(project, projectVersionName);
        return getRiskReportData(project, version);
    }

    public ReportData getRiskReportData(final ProjectItem project, final ProjectVersionItem version)
            throws HubIntegrationException {
        final ReportData reportData = new ReportData();
        reportData.setProjectName(project.getName());
        reportData.setProjectURL(metaService.getHref(project));
        reportData.setProjectVersion(version.getVersionName());
        reportData.setProjectVersionURL(metaService.getHref(version));
        reportData.setPhase(version.getPhase());
        reportData.setDistribution(version.getDistribution());
        final List<BomComponent> components = new ArrayList<>();
        if (hubSupportHelper.hasCapability(HubCapabilitiesEnum.AGGREGATE_BOM_REST_SERVER)) {
            final String componentURL = metaService.getFirstLink(version, MetaService.COMPONENTS_LINK);
            final List<VersionBomComponentView> bomEntries = bomRequestService.getBomEntries(componentURL);
            for (final VersionBomComponentView bomEntry : bomEntries) {
                final BomComponent component = createBomComponentFromBomComponentView(bomEntry);
                final BomComponentPolicyStatusView bomPolicyStatus = requestService.getItem(
                        getComponentPolicyURL(reportData.getProjectVersionURL(), component.getComponentVersionURL()),
                        BomComponentPolicyStatusView.class);
                component.setPolicyStatus(bomPolicyStatus.getApprovalStatus());
                components.add(component);
            }
        } else {
            final ReportCategoriesEnum[] categories = { ReportCategoriesEnum.VERSION, ReportCategoriesEnum.COMPONENTS };
            final VersionReport versionReport = reportRequestService.generateHubReport(version, ReportFormatEnum.JSON, categories);
            final List<AggregateBomViewEntry> bomEntries = versionReport.getAggregateBomViewEntries();
            for (final AggregateBomViewEntry bomEntry : bomEntries) {
                final BomComponent component = createBomComponentFromBomViewEntry(versionReport, bomEntry);
                components.add(component);
            }
        }
        reportData.setComponents(components);
        return reportData;
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

    private String getComponentPolicyURL(final String versionURL, final String componentVersionURL) {
        final String componentVersionSegments = componentVersionURL
                .substring(componentVersionURL.indexOf(MetaService.COMPONENTS_LINK) + MetaService.COMPONENTS_LINK.length());
        return versionURL + componentVersionSegments;
    }

    private BomComponent createBomComponentFromBomViewEntry(final VersionReport report, final AggregateBomViewEntry bomEntry) {
        final BomComponent component = new BomComponent();
        component.setComponentName(bomEntry.getProducerProject().getName());
        component.setComponentVersion(bomEntry.getProducerReleases().get(0).getVersion());
        component.setComponentURL(report.getComponentUrl(bomEntry));
        component.setComponentVersionURL(report.getVersionUrl(bomEntry));
        component.setLicense(bomEntry.getLicensesDisplay());
        component.setPolicyStatus(bomEntry.getPolicyApprovalStatusEnum());
        if (bomEntry.getVulnerabilityRisk() != null) {
            component.setHighSecurityRisk(bomEntry.getVulnerabilityRisk().getHIGH());
            component.setMediumSecurityRisk(bomEntry.getVulnerabilityRisk().getMEDIUM());
            component.setLowSecurityRisk(bomEntry.getVulnerabilityRisk().getLOW());
        }
        if (bomEntry.getLicenseRisk() != null) {
            component.setHighLicenseRisk(bomEntry.getLicenseRisk().getHIGH());
            component.setMediumLicenseRisk(bomEntry.getLicenseRisk().getMEDIUM());
            component.setLowLicenseRisk(bomEntry.getLicenseRisk().getLOW());
        }
        if (bomEntry.getOperationalRisk() != null) {
            component.setHighOperationalRisk(bomEntry.getOperationalRisk().getHIGH());
            component.setMediumOperationalRisk(bomEntry.getOperationalRisk().getMEDIUM());
            component.setLowOperationalRisk(bomEntry.getOperationalRisk().getLOW());
        }

        return component;
    }

    private BomComponent createBomComponentFromBomComponentView(final VersionBomComponentView bomEntry) {
        final BomComponent component = new BomComponent();
        component.setComponentName(bomEntry.getComponentName());
        component.setComponentURL(bomEntry.getComponent());
        component.setComponentVersion(bomEntry.getComponentVersionName());
        component.setComponentVersionURL(bomEntry.getComponentVersion());

        if (bomEntry.getSecurityRiskProfile() != null && bomEntry.getSecurityRiskProfile().getCounts() != null
                && !bomEntry.getSecurityRiskProfile().getCounts().isEmpty()) {
            for (final RiskCountView count : bomEntry.getSecurityRiskProfile().getCounts()) {
                if (count.getCountType() == CountTypeEnum.HIGH && count.getCount() > 0) {
                    component.setHighSecurityRisk(count.getCount());
                } else if (count.getCountType() == CountTypeEnum.MEDIUM && count.getCount() > 0) {
                    component.setMediumSecurityRisk(count.getCount());
                } else if (count.getCountType() == CountTypeEnum.LOW && count.getCount() > 0) {
                    component.setLowSecurityRisk(count.getCount());
                }
            }
        }
        if (bomEntry.getLicenseRiskProfile() != null && bomEntry.getLicenseRiskProfile().getCounts() != null
                && !bomEntry.getLicenseRiskProfile().getCounts().isEmpty()) {
            for (final RiskCountView count : bomEntry.getLicenseRiskProfile().getCounts()) {
                if (count.getCountType() == CountTypeEnum.HIGH && count.getCount() > 0) {
                    component.setHighLicenseRisk(count.getCount());
                } else if (count.getCountType() == CountTypeEnum.MEDIUM && count.getCount() > 0) {
                    component.setMediumLicenseRisk(count.getCount());
                } else if (count.getCountType() == CountTypeEnum.LOW && count.getCount() > 0) {
                    component.setLowLicenseRisk(count.getCount());
                }
            }
        }
        if (bomEntry.getOperationalRiskProfile() != null && bomEntry.getOperationalRiskProfile().getCounts() != null
                && !bomEntry.getOperationalRiskProfile().getCounts().isEmpty()) {
            for (final RiskCountView count : bomEntry.getOperationalRiskProfile().getCounts()) {
                if (count.getCountType() == CountTypeEnum.HIGH && count.getCount() > 0) {
                    component.setHighOperationalRisk(count.getCount());
                } else if (count.getCountType() == CountTypeEnum.MEDIUM && count.getCount() > 0) {
                    component.setMediumOperationalRisk(count.getCount());
                } else if (count.getCountType() == CountTypeEnum.LOW && count.getCount() > 0) {
                    component.setLowOperationalRisk(count.getCount());
                }
            }
        }
        return component;
    }
}
