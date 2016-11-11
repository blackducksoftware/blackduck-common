/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
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
 *******************************************************************************/
package com.blackducksoftware.integration.hub.dataservices.notifications.transforms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.api.component.version.BomComponentVersionPolicyStatus;
import com.blackducksoftware.integration.hub.api.component.version.ComponentVersion;
import com.blackducksoftware.integration.hub.api.component.version.ComponentVersionRestService;
import com.blackducksoftware.integration.hub.api.component.version.ComponentVersionStatus;
import com.blackducksoftware.integration.hub.api.notification.NotificationRestService;
import com.blackducksoftware.integration.hub.api.notification.RuleViolationNotificationContent;
import com.blackducksoftware.integration.hub.api.notification.RuleViolationNotificationItem;
import com.blackducksoftware.integration.hub.api.policy.PolicyRestService;
import com.blackducksoftware.integration.hub.api.policy.PolicyRule;
import com.blackducksoftware.integration.hub.api.project.ReleaseItemRestService;
import com.blackducksoftware.integration.hub.api.version.ReleaseItem;
import com.blackducksoftware.integration.hub.api.version.VersionBomPolicyRestService;
import com.blackducksoftware.integration.hub.dataservices.notification.items.NotificationContentItem;
import com.blackducksoftware.integration.hub.dataservices.notification.items.PolicyNotificationFilter;
import com.blackducksoftware.integration.hub.dataservices.notification.items.PolicyViolationContentItem;
import com.blackducksoftware.integration.hub.dataservices.notification.transformer.PolicyViolationTransformer;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.MissingUUIDException;
import com.blackducksoftware.integration.hub.exception.NotificationServiceException;

public class PolicyViolationTransformTest {
    private final static String PROJECT_NAME = "test project";

    private final static String PROJECT_VERSION = "0.1.0";

    private final static String COMPONENT_NAME = "component 1";

    private final static String COMPONENT_VERSION = "0.9.8";

    private final static String COMPONENT_VERSION_URL = "http://hub.blackducksoftware.com/api/projects/"
            + UUID.randomUUID()
            + "/versions/" + UUID.randomUUID() + "/";

    private final static String POLICY_NAME = "Policy Name";

    private static final String PROJECT_VERSION_LINK = "http://hub.bds.com/api/projects/1234/versions/5678";

    private final static String POLICY_LINK = "url1";

    private NotificationRestService notificationService;

    private ReleaseItemRestService projectVersionService;

    private PolicyRestService policyService;

    private VersionBomPolicyRestService bomVersionPolicyService;

    private ComponentVersionRestService componentVersionService;

    private PolicyViolationTransformer transformer;

    private NotificationRestService createNotificationService() {
        final NotificationRestService service = Mockito.mock(NotificationRestService.class);
        return service;
    }

    private ReleaseItemRestService createProjectVersionService()
            throws IOException, BDRestException, URISyntaxException {

        final ReleaseItemRestService service = Mockito.mock(ReleaseItemRestService.class);
        final ReleaseItem releaseItem = Mockito.mock(ReleaseItem.class);
        Mockito.when(releaseItem.getVersionName()).thenReturn(PROJECT_VERSION);
        Mockito.when(service.getItem(Mockito.anyString())).thenReturn(releaseItem);
        return service;
    }

    private PolicyRestService createPolicyService() throws IOException, BDRestException, URISyntaxException {
        final PolicyRule rule = Mockito.mock(PolicyRule.class);
        Mockito.when(rule.getName()).thenReturn(POLICY_NAME);
        final PolicyRestService service = Mockito.mock(PolicyRestService.class);
        Mockito.when(service.getItem(Mockito.anyString())).thenReturn(rule);
        return service;
    }

    private VersionBomPolicyRestService createBomVersionService()
            throws IOException, BDRestException, URISyntaxException {
        final List<String> policyRuleList = new ArrayList<>();
        policyRuleList.add(POLICY_LINK);
        final BomComponentVersionPolicyStatus status = Mockito.mock(BomComponentVersionPolicyStatus.class);
        Mockito.when(status.getLinks(Mockito.anyString())).thenReturn(policyRuleList);
        final VersionBomPolicyRestService service = Mockito.mock(VersionBomPolicyRestService.class);
        Mockito.when(service.getItem(Mockito.anyString())).thenReturn(status);
        return service;
    }

    private ComponentVersionRestService createComponentVersionService()
            throws NotificationServiceException, IOException, BDRestException, URISyntaxException {

        final ComponentVersion componentVersion = Mockito.mock(ComponentVersion.class);
        Mockito.when(componentVersion.getVersionName()).thenReturn(COMPONENT_VERSION);
        final ComponentVersionRestService service = Mockito.mock(ComponentVersionRestService.class);
        Mockito.when(service.getItem(Mockito.anyString())).thenReturn(componentVersion);
        return service;
    }

    @Before
    public void initTest() throws Exception {
        notificationService = createNotificationService();
        projectVersionService = createProjectVersionService();
        policyService = createPolicyService();
        bomVersionPolicyService = createBomVersionService();
        componentVersionService = createComponentVersionService();
    }

    private RuleViolationNotificationItem createNotificationItem() throws MissingUUIDException {
        final RuleViolationNotificationItem item = Mockito.mock(RuleViolationNotificationItem.class);
        final RuleViolationNotificationContent content = Mockito.mock(RuleViolationNotificationContent.class);
        Mockito.when(content.getProjectVersionLink()).thenReturn(PROJECT_VERSION_LINK);
        final List<ComponentVersionStatus> versionStatusList = new ArrayList<>();
        final ComponentVersionStatus status = Mockito.mock(ComponentVersionStatus.class);
        Mockito.when(status.getComponentName()).thenReturn(COMPONENT_NAME);
        Mockito.when(status.getBomComponentVersionPolicyStatusLink()).thenReturn("PolicyRule");
        Mockito.when(status.getComponentVersionLink())
                .thenReturn(COMPONENT_VERSION_URL);
        versionStatusList.add(status);
        Mockito.when(item.getContent()).thenReturn(content);
        Mockito.when(content.getProjectName()).thenReturn(PROJECT_NAME);
        Mockito.when(content.getComponentVersionStatuses()).thenReturn(versionStatusList);
        return item;
    }

    @Test
    public void testTransform() throws Exception {
        transformer = new PolicyViolationTransformer(notificationService, projectVersionService, policyService,
                bomVersionPolicyService, componentVersionService, new PolicyNotificationFilter(null));

        final List<NotificationContentItem> itemList = transformer.transform(createNotificationItem());
        assertTrue(itemList.size() > 0);
        for (final NotificationContentItem item : itemList) {
            final PolicyViolationContentItem contentItem = (PolicyViolationContentItem) item;
            assertEquals(PROJECT_NAME, contentItem.getProjectVersion().getProjectName());
            assertEquals(PROJECT_VERSION, contentItem.getProjectVersion().getProjectVersionName());
            assertEquals(COMPONENT_NAME, contentItem.getComponentName());
            assertEquals(COMPONENT_VERSION, contentItem.getComponentVersion());
            assertEquals(COMPONENT_VERSION_URL, contentItem.getComponentVersionUrl());
            assertEquals(1, contentItem.getPolicyRuleList().size());
            assertEquals(POLICY_NAME, contentItem.getPolicyRuleList().get(0).getName());
        }
    }

    @Test
    public void testTransformFilteredPolicies() throws Exception {
        final List<String> ruleLinksToInclude = new ArrayList<>();
        ruleLinksToInclude.add("FakeRule");

        transformer = new PolicyViolationTransformer(notificationService, projectVersionService, policyService,
                bomVersionPolicyService, componentVersionService, new PolicyNotificationFilter(ruleLinksToInclude));

        final List<NotificationContentItem> itemList = transformer.transform(createNotificationItem());
        assertTrue(itemList.size() == 0);
    }

    @Test
    public void testTransformFilteredPoliciesIncluded() throws Exception {
        final List<String> ruleLinksToInclude = new ArrayList<>();
        ruleLinksToInclude.add(POLICY_LINK);

        transformer = new PolicyViolationTransformer(notificationService, projectVersionService, policyService,
                bomVersionPolicyService, componentVersionService, new PolicyNotificationFilter(ruleLinksToInclude));

        final List<NotificationContentItem> itemList = transformer.transform(createNotificationItem());
        assertTrue(itemList.size() > 0);
        for (final NotificationContentItem item : itemList) {
            final PolicyViolationContentItem contentItem = (PolicyViolationContentItem) item;
            assertEquals(PROJECT_NAME, contentItem.getProjectVersion().getProjectName());
            assertEquals(PROJECT_VERSION, contentItem.getProjectVersion().getProjectVersionName());
            assertEquals(COMPONENT_NAME, contentItem.getComponentName());
            assertEquals(COMPONENT_VERSION, contentItem.getComponentVersion());
            assertEquals(COMPONENT_VERSION_URL, contentItem.getComponentVersionUrl());
            assertEquals(1, contentItem.getPolicyRuleList().size());
            assertEquals(POLICY_NAME, contentItem.getPolicyRuleList().get(0).getName());
        }
    }
}
