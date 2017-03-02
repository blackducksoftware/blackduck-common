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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.HubSupportHelper;
import com.blackducksoftware.integration.hub.api.aggregate.bom.AggregateBomRequestService;
import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.api.project.ProjectItem;
import com.blackducksoftware.integration.hub.api.project.ProjectRequestService;
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionItem;
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionRequestService;
import com.blackducksoftware.integration.hub.api.report.AggregateBomViewEntry;
import com.blackducksoftware.integration.hub.api.report.ReportCategoriesEnum;
import com.blackducksoftware.integration.hub.api.report.ReportRequestService;
import com.blackducksoftware.integration.hub.api.report.VersionReport;
import com.blackducksoftware.integration.hub.api.view.BomComponentPolicyStatusView;
import com.blackducksoftware.integration.hub.api.view.RiskCountView;
import com.blackducksoftware.integration.hub.api.view.VersionBomComponentView;
import com.blackducksoftware.integration.hub.capability.HubCapabilitiesEnum;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.model.type.ReportReportFormatEnum;
import com.blackducksoftware.integration.hub.model.type.RiskCountCountEnum;
import com.blackducksoftware.integration.hub.report.RiskReportWriter;
import com.blackducksoftware.integration.hub.report.api.BomComponent;
import com.blackducksoftware.integration.hub.report.api.ReportData;
import com.blackducksoftware.integration.hub.report.exception.RiskReportException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubResponseService;
import com.blackducksoftware.integration.log.IntLogger;

public class RiskReportDataService extends HubResponseService {

    private final IntLogger logger;

    private final ProjectRequestService projectRequestService;

    private final ProjectVersionRequestService projectVersionRequestService;

    private final ReportRequestService reportRequestService;

    private final AggregateBomRequestService bomRequestService;

    private final MetaService metaService;

    private final HubSupportHelper hubSupportHelper;

    public RiskReportDataService(final IntLogger logger, final RestConnection restConnection, final ProjectRequestService projectRequestService,
            final ProjectVersionRequestService projectVersionRequestService, final ReportRequestService reportRequestService,
            final AggregateBomRequestService bomRequestService,
            final MetaService metaService, final HubSupportHelper hubSupportHelper) {
        super(restConnection);
        this.logger = logger;
        this.projectRequestService = projectRequestService;
        this.projectVersionRequestService = projectVersionRequestService;
        this.reportRequestService = reportRequestService;
        this.bomRequestService = bomRequestService;
        this.metaService = metaService;
        this.hubSupportHelper = hubSupportHelper;

    }

    public ReportData getRiskReportData(final String projectName, final String projectVersionName)
            throws IntegrationException {
        final ProjectItem project = projectRequestService.getProjectByName(projectName);
        final ProjectVersionItem version = projectVersionRequestService.getProjectVersion(project, projectVersionName);
        return getRiskReportData(project, version);
    }

    public ReportData getRiskReportData(final ProjectItem project, final ProjectVersionItem version)
            throws IntegrationException {
        final String originalProjectUrl = metaService.getHref(project);
        final String originalVersionUrl = metaService.getHref(version);
        final ReportData reportData = new ReportData();
        reportData.setProjectName(project.getName());
        reportData.setProjectURL(getReportProjectUrl(originalProjectUrl));
        reportData.setProjectVersion(version.getVersionName());
        reportData.setProjectVersionURL(getReportVersionUrl(originalVersionUrl, false));
        reportData.setPhase(version.getPhase().toString());
        reportData.setDistribution(version.getDistribution().toString());
        final List<BomComponent> components = new ArrayList<>();
        if (hubSupportHelper.hasCapability(HubCapabilitiesEnum.AGGREGATE_BOM_REST_SERVER)) {
            logger.trace("Getting the Report Contents using the Aggregate Bom Rest Server");
            final List<VersionBomComponentView> bomEntries = bomRequestService.getBomEntries(version);
            for (final VersionBomComponentView bomEntry : bomEntries) {
                final BomComponent component = createBomComponentFromBomComponentView(bomEntry);
                String componentPolicyStatusURL = null;
                if (!StringUtils.isBlank(bomEntry.getComponentVersion())) {
                    componentPolicyStatusURL = getComponentPolicyURL(originalVersionUrl, bomEntry.getComponentVersion());
                } else {
                    componentPolicyStatusURL = getComponentPolicyURL(originalVersionUrl, bomEntry.getComponent());
                }
                final BomComponentPolicyStatusView bomPolicyStatus = getItem(componentPolicyStatusURL,
                        BomComponentPolicyStatusView.class);
                component.setPolicyStatus(bomPolicyStatus.getApprovalStatus().toString());
                components.add(component);
            }
        } else {
            logger.trace("Getting the Report Contents using the Report Rest Server");
            final ReportCategoriesEnum[] categories = { ReportCategoriesEnum.VERSION, ReportCategoriesEnum.COMPONENTS };
            final VersionReport versionReport = reportRequestService.generateHubReport(version, ReportReportFormatEnum.JSON, categories);
            final List<AggregateBomViewEntry> bomEntries = versionReport.getAggregateBomViewEntries();
            for (final AggregateBomViewEntry bomEntry : bomEntries) {
                final BomComponent component = createBomComponentFromBomViewEntry(versionReport, bomEntry);
                components.add(component);
            }
        }
        reportData.setComponents(components);
        return reportData;
    }

    public void createReportFiles(final File outputDirectory, final String projectName, final String projectVersionName) throws IntegrationException {
        final ReportData reportData = getRiskReportData(projectName, projectVersionName);
        createReportFiles(outputDirectory, reportData);
    }

    public void createReportFiles(final File outputDirectory, final ProjectItem project, final ProjectVersionItem version) throws IntegrationException {
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

    private String getComponentPolicyURL(final String versionURL, final String componentURL) {
        final String componentVersionSegments = componentURL
                .substring(componentURL.indexOf(MetaService.COMPONENTS_LINK));
        return versionURL + "/" + componentVersionSegments + "/" + MetaService.POLICY_STATUS_LINK;
    }

    private BomComponent createBomComponentFromBomViewEntry(final VersionReport report, final AggregateBomViewEntry bomEntry) {
        final BomComponent component = new BomComponent();
        if (bomEntry.getProducerProject() != null) {
            component.setComponentName(bomEntry.getProducerProject().getName());
            component.setComponentURL(report.getComponentUrl(bomEntry));
        }
        if (bomEntry.getProducerReleases() != null && !bomEntry.getProducerReleases().isEmpty()) {
            component.setComponentVersion(bomEntry.getProducerReleases().get(0).getVersion());
            component.setComponentVersionURL(report.getVersionUrl(bomEntry));
        }
        component.setLicense(bomEntry.getLicensesDisplay());
        component.setPolicyStatus(bomEntry.getPolicyApprovalStatusEnum().toString());
        if (bomEntry.getVulnerabilityRisk() != null) {
            component.setSecurityRiskHighCount(bomEntry.getVulnerabilityRisk().getHIGH());
            component.setSecurityRiskMediumCount(bomEntry.getVulnerabilityRisk().getMEDIUM());
            component.setSecurityRiskLowCount(bomEntry.getVulnerabilityRisk().getLOW());
        }
        if (bomEntry.getLicenseRisk() != null) {
            component.setLicenseRiskHighCount(bomEntry.getLicenseRisk().getHIGH());
            component.setLicenseRiskMediumCount(bomEntry.getLicenseRisk().getMEDIUM());
            component.setLicenseRiskLowCount(bomEntry.getLicenseRisk().getLOW());
        }
        if (bomEntry.getOperationalRisk() != null) {
            component.setOperationalRiskHighCount(bomEntry.getOperationalRisk().getHIGH());
            component.setOperationalRiskMediumCount(bomEntry.getOperationalRisk().getMEDIUM());
            component.setOperationalRiskLowCount(bomEntry.getOperationalRisk().getLOW());
        }

        return component;
    }

    private BomComponent createBomComponentFromBomComponentView(final VersionBomComponentView bomEntry) {
        final BomComponent component = new BomComponent();
        component.setComponentName(bomEntry.getComponentName());
        component.setComponentURL(getReportProjectUrl(bomEntry.getComponent()));
        component.setComponentVersion(bomEntry.getComponentVersionName());
        component.setComponentVersionURL(getReportVersionUrl(bomEntry.getComponentVersion(), true));

        if (bomEntry.getSecurityRiskProfile() != null && bomEntry.getSecurityRiskProfile().getCounts() != null
                && !bomEntry.getSecurityRiskProfile().getCounts().isEmpty()) {
            for (final RiskCountView count : bomEntry.getSecurityRiskProfile().getCounts()) {
                if (count.getCountType() == RiskCountCountEnum.HIGH && count.getCount() > 0) {
                    component.setSecurityRiskHighCount(count.getCount());
                } else if (count.getCountType() == RiskCountCountEnum.MEDIUM && count.getCount() > 0) {
                    component.setSecurityRiskMediumCount(count.getCount());
                } else if (count.getCountType() == RiskCountCountEnum.LOW && count.getCount() > 0) {
                    component.setSecurityRiskLowCount(count.getCount());
                }
            }
        }
        if (bomEntry.getLicenseRiskProfile() != null && bomEntry.getLicenseRiskProfile().getCounts() != null
                && !bomEntry.getLicenseRiskProfile().getCounts().isEmpty()) {
            for (final RiskCountView count : bomEntry.getLicenseRiskProfile().getCounts()) {
                if (count.getCountType() == RiskCountCountEnum.HIGH && count.getCount() > 0) {
                    component.setLicenseRiskHighCount(count.getCount());
                } else if (count.getCountType() == RiskCountCountEnum.MEDIUM && count.getCount() > 0) {
                    component.setLicenseRiskMediumCount(count.getCount());
                } else if (count.getCountType() == RiskCountCountEnum.LOW && count.getCount() > 0) {
                    component.setLicenseRiskLowCount(count.getCount());
                }
            }
        }
        if (bomEntry.getOperationalRiskProfile() != null && bomEntry.getOperationalRiskProfile().getCounts() != null
                && !bomEntry.getOperationalRiskProfile().getCounts().isEmpty()) {
            for (final RiskCountView count : bomEntry.getOperationalRiskProfile().getCounts()) {
                if (count.getCountType() == RiskCountCountEnum.HIGH && count.getCount() > 0) {
                    component.setOperationalRiskHighCount(count.getCount());
                } else if (count.getCountType() == RiskCountCountEnum.MEDIUM && count.getCount() > 0) {
                    component.setOperationalRiskMediumCount(count.getCount());
                } else if (count.getCountType() == RiskCountCountEnum.LOW && count.getCount() > 0) {
                    component.setOperationalRiskLowCount(count.getCount());
                }
            }
        }
        return component;
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
}
