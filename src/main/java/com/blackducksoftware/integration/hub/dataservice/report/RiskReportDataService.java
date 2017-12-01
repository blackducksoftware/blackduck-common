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
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.HubSupportHelper;
import com.blackducksoftware.integration.hub.api.aggregate.bom.AggregateBomRequestService;
import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.api.project.ProjectRequestService;
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionRequestService;
import com.blackducksoftware.integration.hub.api.report.AggregateBomViewEntry;
import com.blackducksoftware.integration.hub.api.report.ReportCategoriesEnum;
import com.blackducksoftware.integration.hub.api.report.ReportRequestService;
import com.blackducksoftware.integration.hub.api.report.VersionReport;
import com.blackducksoftware.integration.hub.capability.HubCapabilitiesEnum;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.model.enumeration.BomComponentPolicyStatusApprovalStatusEnum;
import com.blackducksoftware.integration.hub.model.enumeration.ReportFormatEnum;
import com.blackducksoftware.integration.hub.model.enumeration.RiskCountEnum;
import com.blackducksoftware.integration.hub.model.view.BomComponentPolicyStatusView;
import com.blackducksoftware.integration.hub.model.view.PolicyRuleView;
import com.blackducksoftware.integration.hub.model.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.model.view.ProjectView;
import com.blackducksoftware.integration.hub.model.view.VersionBomComponentView;
import com.blackducksoftware.integration.hub.model.view.components.RiskCountView;
import com.blackducksoftware.integration.hub.report.RiskReportWriter;
import com.blackducksoftware.integration.hub.report.api.BomComponent;
import com.blackducksoftware.integration.hub.report.api.PolicyRule;
import com.blackducksoftware.integration.hub.report.api.ReportData;
import com.blackducksoftware.integration.hub.report.exception.RiskReportException;
import com.blackducksoftware.integration.hub.report.pdf.PDFBoxWriter;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubResponseService;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.util.IntegrationEscapeUtil;

public class RiskReportDataService extends HubResponseService {

    private final IntLogger logger;

    private final ProjectRequestService projectRequestService;

    private final ProjectVersionRequestService projectVersionRequestService;

    private final ReportRequestService reportRequestService;

    private final AggregateBomRequestService bomRequestService;

    private final HubSupportHelper hubSupportHelper;

    private final IntegrationEscapeUtil escapeUtil;

    public RiskReportDataService(final IntLogger logger, final RestConnection restConnection, final ProjectRequestService projectRequestService, final ProjectVersionRequestService projectVersionRequestService,
            final ReportRequestService reportRequestService, final AggregateBomRequestService bomRequestService, final HubSupportHelper hubSupportHelper, final IntegrationEscapeUtil escapeUtil) {
        super(restConnection);
        this.logger = logger;
        this.projectRequestService = projectRequestService;
        this.projectVersionRequestService = projectVersionRequestService;
        this.reportRequestService = reportRequestService;
        this.bomRequestService = bomRequestService;
        this.hubSupportHelper = hubSupportHelper;
        this.escapeUtil = escapeUtil;
    }

    public String getNoticesReportData(final String projectName, final String projectVersionName) throws IntegrationException {
        final ProjectView project = projectRequestService.getProjectByName(projectName);
        final ProjectVersionView version = projectVersionRequestService.getProjectVersion(project, projectVersionName);
        return getNoticesReportData(project, version);
    }

    public String getNoticesReportData(final ProjectView project, final ProjectVersionView version) throws IntegrationException {
        logger.trace("Getting the Notices Report Contents using the Report Rest Server");
        return reportRequestService.generateHubNoticesReport(version, ReportFormatEnum.TEXT);
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
        final ProjectView project = projectRequestService.getProjectByName(projectName);
        final ProjectVersionView version = projectVersionRequestService.getProjectVersion(project, projectVersionName);
        return getRiskReportData(project, version);
    }

    public ReportData getRiskReportData(final ProjectView project, final ProjectVersionView version) throws IntegrationException {
        final String originalProjectUrl = metaService.getHref(project);
        final String originalVersionUrl = metaService.getHref(version);
        final ReportData reportData = new ReportData();
        reportData.setProjectName(project.name);
        reportData.setProjectURL(getReportProjectUrl(originalProjectUrl));
        reportData.setProjectVersion(version.versionName);
        reportData.setProjectVersionURL(getReportVersionUrl(originalVersionUrl, false));
        reportData.setPhase(version.phase.toString());
        reportData.setDistribution(version.distribution.toString());
        final List<BomComponent> components = new ArrayList<>();
        if (hubSupportHelper.hasCapability(HubCapabilitiesEnum.AGGREGATE_BOM_REST_SERVER)) {
            logger.trace("Getting the Report Contents using the Aggregate Bom Rest Server");
            final List<VersionBomComponentView> bomEntries = bomRequestService.getBomEntries(version);
            boolean policyFailure = false;
            for (final VersionBomComponentView bomEntry : bomEntries) {
                final BomComponent component = createBomComponentFromBomComponentView(bomEntry);
                String policyStatus = bomEntry.approvalStatus;
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
                            final BomComponentPolicyStatusView bomPolicyStatus = getItem(componentPolicyStatusURL, BomComponentPolicyStatusView.class);
                            policyStatus = bomPolicyStatus.approvalStatus.toString();
                        } catch (final IntegrationException e) {
                            policyFailure = true;
                            logger.debug("Could not get the component policy status, the Hub policy module is not enabled");
                        }
                    }
                }
                component.setPolicyStatus(policyStatus);
                addPolicyRuleInfo(component, bomEntry);
                components.add(component);
            }
        } else {
            logger.trace("Getting the Report Contents using the Report Rest Server");
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
        final String componentVersionSegments = componentURL.substring(componentURL.indexOf(MetaService.COMPONENTS_LINK));
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
        component.setComponentName(bomEntry.componentName);
        component.setComponentURL(getReportProjectUrl(bomEntry.component));
        component.setComponentVersion(bomEntry.componentVersionName);
        component.setComponentVersionURL(getReportVersionUrl(bomEntry.componentVersion, true));
        component.setLicense(bomEntry.licenses.get(0).licenseDisplay);
        if (bomEntry.securityRiskProfile != null && bomEntry.securityRiskProfile.counts != null && !bomEntry.securityRiskProfile.counts.isEmpty()) {
            for (final RiskCountView count : bomEntry.securityRiskProfile.counts) {
                if (count.countType == RiskCountEnum.HIGH && count.count > 0) {
                    component.setSecurityRiskHighCount(count.count);
                } else if (count.countType == RiskCountEnum.MEDIUM && count.count > 0) {
                    component.setSecurityRiskMediumCount(count.count);
                } else if (count.countType == RiskCountEnum.LOW && count.count > 0) {
                    component.setSecurityRiskLowCount(count.count);
                }
            }
        }
        if (bomEntry.licenseRiskProfile != null && bomEntry.licenseRiskProfile.counts != null && !bomEntry.licenseRiskProfile.counts.isEmpty()) {
            for (final RiskCountView count : bomEntry.licenseRiskProfile.counts) {
                if (count.countType == RiskCountEnum.HIGH && count.count > 0) {
                    component.setLicenseRiskHighCount(count.count);
                } else if (count.countType == RiskCountEnum.MEDIUM && count.count > 0) {
                    component.setLicenseRiskMediumCount(count.count);
                } else if (count.countType == RiskCountEnum.LOW && count.count > 0) {
                    component.setLicenseRiskLowCount(count.count);
                }
            }
        }
        if (bomEntry.operationalRiskProfile != null && bomEntry.operationalRiskProfile.counts != null && !bomEntry.operationalRiskProfile.counts.isEmpty()) {
            for (final RiskCountView count : bomEntry.operationalRiskProfile.counts) {
                if (count.countType == RiskCountEnum.HIGH && count.count > 0) {
                    component.setOperationalRiskHighCount(count.count);
                } else if (count.countType == RiskCountEnum.MEDIUM && count.count > 0) {
                    component.setOperationalRiskMediumCount(count.count);
                } else if (count.countType == RiskCountEnum.LOW && count.count > 0) {
                    component.setOperationalRiskLowCount(count.count);
                }
            }
        }
        return component;
    }

    public void addPolicyRuleInfo(final BomComponent component, final VersionBomComponentView bomEntry) throws IntegrationException {
        if (bomEntry != null && StringUtils.isNotBlank(bomEntry.approvalStatus)) {
            final BomComponentPolicyStatusApprovalStatusEnum status = BomComponentPolicyStatusApprovalStatusEnum.valueOf(bomEntry.approvalStatus);
            if (status == BomComponentPolicyStatusApprovalStatusEnum.IN_VIOLATION) {
                final String policyRuleLink = metaService.getFirstLink(bomEntry, MetaService.POLICY_RULES_LINK);
                final List<PolicyRuleView> rules = getAllItems(policyRuleLink, PolicyRuleView.class);
                final List<PolicyRule> rulesViolated = new ArrayList<>();
                for (final PolicyRuleView policyRuleView : rules) {
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
}
