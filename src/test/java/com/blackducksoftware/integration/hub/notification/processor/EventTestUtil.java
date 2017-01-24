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

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.api.component.version.ComponentVersion;
import com.blackducksoftware.integration.hub.api.notification.VulnerabilitySourceQualifiedId;
import com.blackducksoftware.integration.hub.api.policy.PolicyRule;
import com.blackducksoftware.integration.hub.api.vulnerability.SeverityEnum;
import com.blackducksoftware.integration.hub.api.vulnerability.VulnerabilityItem;
import com.blackducksoftware.integration.hub.dataservice.model.ProjectVersion;
import com.blackducksoftware.integration.hub.dataservice.notification.model.PolicyOverrideContentItem;
import com.blackducksoftware.integration.hub.dataservice.notification.model.PolicyViolationClearedContentItem;
import com.blackducksoftware.integration.hub.dataservice.notification.model.PolicyViolationContentItem;
import com.blackducksoftware.integration.hub.dataservice.notification.model.VulnerabilityContentItem;
import com.blackducksoftware.integration.hub.meta.MetaAllowEnum;

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

    public static final List<MetaAllowEnum> ALLOW_LIST = Collections.emptyList();

    public List<VulnerabilityItem> createVulnerabiltyItemList(final List<VulnerabilitySourceQualifiedId> vulnSourceList) {
        final List<VulnerabilityItem> vulnerabilityList = new ArrayList<>(vulnSourceList.size());
        for (final VulnerabilitySourceQualifiedId vulnSource : vulnSourceList) {
            final String vulnId = vulnSource.getVulnerabilityId();
            SeverityEnum severity = SeverityEnum.UNKNOWN;
            if (vulnId.startsWith(HIGH_VULN_PREFIX)) {
                severity = SeverityEnum.HIGH;
            } else if (vulnId.startsWith(MEDIUM_VULN_PREFIX)) {
                severity = SeverityEnum.MEDIUM;
            } else if (vulnId.startsWith(LOW_VULN_PREFIX)) {
                severity = SeverityEnum.LOW;
            }
            vulnerabilityList.add(createVulnerability(vulnId, severity));
        }
        return vulnerabilityList;
    }

    public VulnerabilityItem createVulnerability(final String vulnId, final SeverityEnum severity) {
        final VulnerabilityItem item = Mockito.mock(VulnerabilityItem.class);
        Mockito.when(item.getVulnerabilityName()).thenReturn(vulnId);
        Mockito.when(item.getDescription()).thenReturn("A vulnerability");
        Mockito.when(item.getVulnerabilityPublishedDate()).thenReturn("today");
        Mockito.when(item.getVulnerabilityUpdatedDate()).thenReturn("a minute ago");
        Mockito.when(item.getBaseScore()).thenReturn(10.0);
        Mockito.when(item.getImpactSubscore()).thenReturn(5.0);
        Mockito.when(item.getExploitabilitySubscore()).thenReturn(1.0);
        Mockito.when(item.getSource()).thenReturn("");
        Mockito.when(item.getSeverity()).thenReturn(severity.name());
        Mockito.when(item.getAccessVector()).thenReturn("");
        Mockito.when(item.getAccessComplexity()).thenReturn("");
        Mockito.when(item.getAuthentication()).thenReturn("");
        Mockito.when(item.getConfidentialityImpact()).thenReturn("");
        Mockito.when(item.getIntegrityImpact()).thenReturn("");
        Mockito.when(item.getAvailabilityImpact()).thenReturn("");
        Mockito.when(item.getCweId()).thenReturn(vulnId);
        return item;
    }

    public PolicyRule createPolicyRule(final String name, final String description, final String createdBy, final String updatedBy, final String href) {
        final PolicyRule rule = Mockito.mock(PolicyRule.class);
        Mockito.when(rule.getName()).thenReturn(name);
        Mockito.when(rule.getDescription()).thenReturn(description);
        Mockito.when(rule.getEnabled()).thenReturn(true);
        Mockito.when(rule.getOverridable()).thenReturn(true);
        Mockito.when(rule.getExpression()).thenReturn(null);
        Mockito.when(rule.getCreatedAt()).thenReturn(new Date());
        Mockito.when(rule.getCreatedBy()).thenReturn(createdBy);
        Mockito.when(rule.getUpdatedAt()).thenReturn(new Date());
        Mockito.when(rule.getUpdatedBy()).thenReturn(updatedBy);
        Mockito.when(rule.getJson()).thenReturn(createPolicyRuleJSon(href));
        return rule;
    }

    public String createPolicyRuleJSon(final String href) {
        return "{ \"_meta\": { \"href\": \"" + href + "\" }}";
    }

    public PolicyOverrideContentItem createPolicyOverride(final Date createdTime, final String projectName,
            final String projectVersionName, final String componentName, final String componentVersion)
            throws URISyntaxException {
        final ComponentVersion fullComponentVersion = createComponentVersionMock(componentVersion);
        final String projectVersionUrl = PROJECT_VERSION_URL_PREFIX + projectName + PROJECT_VERSION_URL_SEGMENT + projectVersionName;
        final ProjectVersion projectVersion = new ProjectVersion();
        projectVersion.setProjectName(projectName);
        projectVersion.setProjectVersionName(projectVersionName);
        projectVersion.setUrl(projectVersionUrl);
        final String componentUrl = COMPONENT_URL_PREFIX + componentName;
        final String componentVersionUrl = COMPONENT_URL_PREFIX + componentName + VERSIONS_URL_SEGMENT
                + componentVersion;
        final List<PolicyRule> policyRuleList = new ArrayList<>();
        policyRuleList.add(createPolicyRule(RULE_NAME_1, DESCRIPTION, CREATED_BY, UPDATED_BY, POLICY_RULE_1_HREF_URL));
        policyRuleList.add(createPolicyRule(RULE_NAME_2, DESCRIPTION, CREATED_BY, UPDATED_BY, POLICY_RULE_2_HREF_URL));
        final PolicyOverrideContentItem item = new PolicyOverrideContentItem(createdTime, projectVersion, componentName,
                fullComponentVersion, componentUrl, componentVersionUrl, policyRuleList, FIRST_NAME, LAST_NAME);
        return item;
    }

    private ComponentVersion createComponentVersionMock(final String componentVersion) {
        ComponentVersion fullComponentVersion;
        fullComponentVersion = Mockito.mock(ComponentVersion.class);
        Mockito.when(fullComponentVersion.getVersionName()).thenReturn(componentVersion);
        return fullComponentVersion;
    }

    public PolicyViolationClearedContentItem createPolicyCleared(final Date createdTime, final String projectName,
            final String projectVersionName, final String componentName, final String componentVersion)
            throws URISyntaxException {
        final ComponentVersion fullComponentVersion = createComponentVersionMock(componentVersion);
        final String projectVersionUrl = PROJECT_VERSION_URL_PREFIX + projectName + PROJECT_VERSION_URL_SEGMENT + projectVersionName;
        final ProjectVersion projectVersion = new ProjectVersion();
        projectVersion.setProjectName(projectName);
        projectVersion.setProjectVersionName(projectVersionName);
        projectVersion.setUrl(projectVersionUrl);
        final String componentUrl = COMPONENT_URL_PREFIX + componentName;
        final String componentVersionUrl = COMPONENT_URL_PREFIX + componentName + VERSIONS_URL_SEGMENT
                + componentVersion;
        final List<PolicyRule> policyRuleList = new ArrayList<>();
        policyRuleList.add(createPolicyRule(RULE_NAME_1, DESCRIPTION, CREATED_BY, UPDATED_BY, POLICY_RULE_1_HREF_URL));
        policyRuleList.add(createPolicyRule(RULE_NAME_2, DESCRIPTION, CREATED_BY, UPDATED_BY, POLICY_RULE_2_HREF_URL));
        final PolicyViolationClearedContentItem item = new PolicyViolationClearedContentItem(createdTime,
                projectVersion, componentName, fullComponentVersion, componentUrl, componentVersionUrl, policyRuleList);
        return item;
    }

    public PolicyViolationContentItem createPolicyViolation(final Date createdTime, final String projectName,
            final String projectVersionName, final String componentName, final String componentVersion)
            throws URISyntaxException {
        final ComponentVersion fullComponentVersion = createComponentVersionMock(componentVersion);
        final String projectVersionUrl = PROJECT_VERSION_URL_PREFIX + projectName + PROJECT_VERSION_URL_SEGMENT + projectVersionName;
        final ProjectVersion projectVersion = new ProjectVersion();
        projectVersion.setProjectName(projectName);
        projectVersion.setProjectVersionName(projectVersionName);
        projectVersion.setUrl(projectVersionUrl);
        final String componentUrl = COMPONENT_URL_PREFIX + componentName;
        final String componentVersionUrl = COMPONENT_URL_PREFIX + componentName + VERSIONS_URL_SEGMENT
                + componentVersion;
        final List<PolicyRule> policyRuleList = new ArrayList<>();
        policyRuleList.add(createPolicyRule(RULE_NAME_1, DESCRIPTION, CREATED_BY, UPDATED_BY, POLICY_RULE_1_HREF_URL));
        policyRuleList.add(createPolicyRule(RULE_NAME_2, DESCRIPTION, CREATED_BY, UPDATED_BY, POLICY_RULE_2_HREF_URL));
        final PolicyViolationContentItem item = new PolicyViolationContentItem(createdTime, projectVersion,
                componentName, fullComponentVersion, componentUrl, componentVersionUrl, policyRuleList);
        return item;
    }

    public VulnerabilityContentItem createVulnerability(final Date createdTime, final String projectName,
            final String projectVersionName, final String componentName, final String componentVersion,
            final List<VulnerabilitySourceQualifiedId> added, final List<VulnerabilitySourceQualifiedId> updated,
            final List<VulnerabilitySourceQualifiedId> deleted) throws URISyntaxException {
        final ComponentVersion fullComponentVersion = createComponentVersionMock(componentVersion);
        final String projectVersionUrl = PROJECT_VERSION_URL_PREFIX + projectName + PROJECT_VERSION_URL_SEGMENT + projectVersionName;
        final ProjectVersion projectVersion = new ProjectVersion();
        projectVersion.setProjectName(projectName);
        projectVersion.setProjectVersionName(projectVersionName);
        projectVersion.setUrl(projectVersionUrl);
        final String componentVersionUrl = COMPONENT_URL_PREFIX + componentName + VERSIONS_URL_SEGMENT
                + componentVersion;
        final VulnerabilityContentItem item = new VulnerabilityContentItem(createdTime, projectVersion, componentName,
                fullComponentVersion, componentVersionUrl, added, updated, deleted);
        return item;
    }
}
