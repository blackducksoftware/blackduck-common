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
package com.blackducksoftware.integration.hub.notification.processor;

import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.blackducksoftware.integration.hub.api.core.ResourceMetadata;
import com.blackducksoftware.integration.hub.api.generated.enumeration.VulnerabilityV2Cvss2AccessComplexityType;
import com.blackducksoftware.integration.hub.api.generated.enumeration.VulnerabilityV2SourceType;
import com.blackducksoftware.integration.hub.api.generated.view.ComponentVersionView;
import com.blackducksoftware.integration.hub.api.generated.view.PolicyRuleView;
import com.blackducksoftware.integration.hub.api.generated.view.VulnerabilityV1View;
import com.blackducksoftware.integration.hub.notification.content.VulnerabilitySourceQualifiedId;
import com.blackducksoftware.integration.hub.throwaway.PolicyOverrideContentItem;
import com.blackducksoftware.integration.hub.throwaway.PolicyViolationClearedContentItem;
import com.blackducksoftware.integration.hub.throwaway.PolicyViolationContentItem;
import com.blackducksoftware.integration.hub.throwaway.ProjectVersionModel;
import com.blackducksoftware.integration.hub.throwaway.VulnerabilityContentItem;

public class EventTestUtil {
    public static final String DESCRIPTION = "description";
    public static final String LOW_VULN_PREFIX = "low";
    public static final String MEDIUM_VULN_PREFIX = "medium";
    public static final String HIGH_VULN_PREFIX = "high";
    public static final String UPDATED_BY = "me";
    public static final String CREATED_BY = "you";
    public static final String UPDATED_AT = "now";
    public static final String CREATED_AT = "then";
    public static final String RULE_NAME_2 = "Rule 2";
    public static final String RULE_NAME_1 = "Rule 1";
    public static final String LOW_VULN_ID1 = "low_vuln_id1";
    public static final String LOW_VULN_ID2 = "low_vuln_id2";
    public static final String MEDIUM_VULN_ID2 = "medium_vuln_id2";
    public static final String LOW_VULN_ID = "low_vuln_id";
    public static final String MEDIUM_VULN_ID = "medium_vuln_id";
    public static final String HIGH_VULN_ID = "high_vuln_id";
    public static final String VULN_SOURCE = "vuln_source";
    public static final String POLICY_RULE_2_HREF_URL = "http://a.hub.server/policy/rule2/url";
    public static final String POLICY_RULE_1_HREF_URL = "http://a.hub.server/policy/rule1/url";
    public static final String VERSIONS_URL_SEGMENT = "/versions/";
    public static final String COMPONENT_URL_PREFIX = "http://localhost/api/components/";
    public static final String PROJECT_VERSION_URL_PREFIX = "http://a.hub.server/project/";
    public static final String PROJECT_VERSION_URL_SEGMENT = "/version/";
    public static final String COMPONENT_VERSION_URL = "http://a.hub.server/components/component/version";
    public static final String LAST_NAME = "LastName";
    public static final String FIRST_NAME = "FirstName";
    public static final String PREFIX_RULE = "Rule ";
    public static final String VERSION = "Version";
    public static final String COMPONENT = "Component";
    public static final String VERSION2 = "Version2";
    public static final String COMPONENT2 = "Component2";
    public static final String PROJECT_VERSION_NAME = "ProjectVersionName";
    public static final String PROJECT_NAME = "ProjectName";
    public static final String PROJECT_VERSION_NAME2 = "ProjectVersionName2";
    public static final String PROJECT_NAME2 = "ProjectName2";
    public static final String COMPONENT_ID = "component_id";
    public static final String COMPONENT_VERSION_ID = "component_version_id";
    public static final List<String> ALLOW_LIST = Collections.emptyList();

    public List<VulnerabilityV1View> createVulnerabiltyItemList(final List<VulnerabilitySourceQualifiedId> vulnSourceList) {
        final List<VulnerabilityV1View> vulnerabilityList = new ArrayList<>(vulnSourceList.size());
        for (final VulnerabilitySourceQualifiedId vulnSource : vulnSourceList) {
            final String vulnId = vulnSource.vulnerabilityId;
            VulnerabilityV2Cvss2AccessComplexityType severity = null;
            if (vulnId.startsWith(HIGH_VULN_PREFIX)) {
                severity = VulnerabilityV2Cvss2AccessComplexityType.HIGH;
            } else if (vulnId.startsWith(MEDIUM_VULN_PREFIX)) {
                severity = VulnerabilityV2Cvss2AccessComplexityType.MEDIUM;
            } else if (vulnId.startsWith(LOW_VULN_PREFIX)) {
                severity = VulnerabilityV2Cvss2AccessComplexityType.LOW;
            }
            if (severity != null) {
                vulnerabilityList.add(createVulnerability(vulnId, severity));
            }
        }
        return vulnerabilityList;
    }

    public VulnerabilityV1View createVulnerability(final String vulnId, final VulnerabilityV2Cvss2AccessComplexityType severity) {
        final VulnerabilityV1View item = new VulnerabilityV1View();
        item.vulnerabilityName = vulnId;
        item.description = "A vulnerability";
        item.vulnerabilityPublishedDate = new Date();
        item.vulnerabilityUpdatedDate = new Date();
        item.baseScore = new BigDecimal("10.0");
        item.impactSubscore = new BigDecimal("5.0");
        item.exploitabilitySubscore = new BigDecimal("1.0");
        item.source = VulnerabilityV2SourceType.NVD;
        item.severity = severity.name();
        item.accessVector = "";
        item.accessComplexity = "";
        item.authentication = "";
        item.confidentialityImpact = "";
        item.integrityImpact = "";
        item.availabilityImpact = "";
        item.cweId = vulnId;
        return item;
    }

    public PolicyRuleView createPolicyRule(final String name, final String description, final String createdBy, final String updatedBy, final String href) {
        final PolicyRuleView rule = new PolicyRuleView();
        rule.json = createPolicyRuleJSon(href);
        rule._meta = createPolicyRuleMeta(href);
        rule.name = name;
        rule.description = description;
        rule.enabled = true;
        rule.overridable = true;
        rule.expression = null;
        rule.createdAt = new Date();
        rule.createdBy = createdBy;
        rule.updatedAt = new Date();
        rule.updatedBy = updatedBy;
        return rule;
    }

    public String createPolicyRuleJSon(final String href) {
        return "{ \"_meta\": { \"href\": \"" + href + "\" }}";
    }

    public ResourceMetadata createPolicyRuleMeta(final String href) {
        final ResourceMetadata meta = new ResourceMetadata();
        meta.href = href;
        return meta;
    }

    public PolicyOverrideContentItem createPolicyOverride(final Date createdTime, final String projectName, final String projectVersionName, final String componentName, final String componentVersion) throws URISyntaxException {
        final ComponentVersionView fullComponentVersion = createComponentVersionMock(componentVersion);
        final String projectVersionUrl = PROJECT_VERSION_URL_PREFIX + projectName + PROJECT_VERSION_URL_SEGMENT + projectVersionName;
        final ProjectVersionModel projectVersion = new ProjectVersionModel();
        projectVersion.setProjectName(projectName);
        projectVersion.setProjectVersionName(projectVersionName);
        projectVersion.setUrl(projectVersionUrl);
        final String componentUrl = COMPONENT_URL_PREFIX + componentName;
        final String componentVersionUrl = COMPONENT_URL_PREFIX + componentName + VERSIONS_URL_SEGMENT + componentVersion;
        final List<PolicyRuleView> policyRuleList = new ArrayList<>();
        policyRuleList.add(createPolicyRule(RULE_NAME_1, DESCRIPTION, CREATED_BY, UPDATED_BY, POLICY_RULE_1_HREF_URL));
        policyRuleList.add(createPolicyRule(RULE_NAME_2, DESCRIPTION, CREATED_BY, UPDATED_BY, POLICY_RULE_2_HREF_URL));
        final String componentIssueUrl = "";
        final PolicyOverrideContentItem item = new PolicyOverrideContentItem(createdTime, projectVersion, componentName, fullComponentVersion, componentUrl, componentVersionUrl, policyRuleList, FIRST_NAME, LAST_NAME, componentIssueUrl);
        return item;
    }

    private ComponentVersionView createComponentVersionMock(final String componentVersion) {
        ComponentVersionView fullComponentVersion;
        fullComponentVersion = new ComponentVersionView();
        fullComponentVersion.versionName = componentVersion;
        return fullComponentVersion;
    }

    public PolicyViolationClearedContentItem createPolicyCleared(final Date createdTime, final String projectName, final String projectVersionName, final String componentName, final String componentVersion) throws URISyntaxException {
        final ComponentVersionView fullComponentVersion = createComponentVersionMock(componentVersion);
        final String projectVersionUrl = PROJECT_VERSION_URL_PREFIX + projectName + PROJECT_VERSION_URL_SEGMENT + projectVersionName;
        final ProjectVersionModel projectVersion = new ProjectVersionModel();
        projectVersion.setProjectName(projectName);
        projectVersion.setProjectVersionName(projectVersionName);
        projectVersion.setUrl(projectVersionUrl);
        final String componentUrl = COMPONENT_URL_PREFIX + componentName;
        final String componentVersionUrl = COMPONENT_URL_PREFIX + componentName + VERSIONS_URL_SEGMENT + componentVersion;
        final List<PolicyRuleView> policyRuleList = new ArrayList<>();
        policyRuleList.add(createPolicyRule(RULE_NAME_1, DESCRIPTION, CREATED_BY, UPDATED_BY, POLICY_RULE_1_HREF_URL));
        policyRuleList.add(createPolicyRule(RULE_NAME_2, DESCRIPTION, CREATED_BY, UPDATED_BY, POLICY_RULE_2_HREF_URL));
        final String componentIssueUrl = "";
        final PolicyViolationClearedContentItem item = new PolicyViolationClearedContentItem(createdTime, projectVersion, componentName, fullComponentVersion, componentUrl, componentVersionUrl, policyRuleList, componentIssueUrl);
        return item;
    }

    public PolicyViolationContentItem createPolicyViolation(final Date createdTime, final String projectName, final String projectVersionName, final String componentName, final String componentVersion) throws URISyntaxException {
        final ComponentVersionView fullComponentVersion = createComponentVersionMock(componentVersion);
        final String projectVersionUrl = PROJECT_VERSION_URL_PREFIX + projectName + PROJECT_VERSION_URL_SEGMENT + projectVersionName;
        final ProjectVersionModel projectVersion = new ProjectVersionModel();
        projectVersion.setProjectName(projectName);
        projectVersion.setProjectVersionName(projectVersionName);
        projectVersion.setUrl(projectVersionUrl);
        final String componentUrl = COMPONENT_URL_PREFIX + componentName;
        final String componentVersionUrl = COMPONENT_URL_PREFIX + componentName + VERSIONS_URL_SEGMENT + componentVersion;
        final List<PolicyRuleView> policyRuleList = new ArrayList<>();
        policyRuleList.add(createPolicyRule(RULE_NAME_1, DESCRIPTION, CREATED_BY, UPDATED_BY, POLICY_RULE_1_HREF_URL));
        policyRuleList.add(createPolicyRule(RULE_NAME_2, DESCRIPTION, CREATED_BY, UPDATED_BY, POLICY_RULE_2_HREF_URL));
        final String componentIssueUrl = "";
        final PolicyViolationContentItem item = new PolicyViolationContentItem(createdTime, projectVersion, componentName, fullComponentVersion, componentUrl, componentVersionUrl, policyRuleList, componentIssueUrl);
        return item;
    }

    public VulnerabilityContentItem createVulnerability(final Date createdTime, final String projectName, final String projectVersionName, final String componentName, final String componentVersion,
            final List<VulnerabilitySourceQualifiedId> added, final List<VulnerabilitySourceQualifiedId> updated, final List<VulnerabilitySourceQualifiedId> deleted) throws URISyntaxException {
        final ComponentVersionView fullComponentVersion = createComponentVersionMock(componentVersion);
        final String projectVersionUrl = PROJECT_VERSION_URL_PREFIX + projectName + PROJECT_VERSION_URL_SEGMENT + projectVersionName;
        final ProjectVersionModel projectVersion = new ProjectVersionModel();
        projectVersion.setProjectName(projectName);
        projectVersion.setProjectVersionName(projectVersionName);
        projectVersion.setUrl(projectVersionUrl);
        final String componentVersionUrl = COMPONENT_URL_PREFIX + componentName + VERSIONS_URL_SEGMENT + componentVersion;
        final String componentIssueUrl = "";
        final VulnerabilityContentItem item = new VulnerabilityContentItem(createdTime, projectVersion, componentName, fullComponentVersion, componentVersionUrl, added, updated, deleted, componentIssueUrl);
        return item;
    }
}
