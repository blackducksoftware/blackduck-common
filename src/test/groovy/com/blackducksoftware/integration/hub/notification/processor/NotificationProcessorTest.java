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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.api.core.ResourceLink;
import com.blackducksoftware.integration.hub.api.core.ResourceMetadata;
import com.blackducksoftware.integration.hub.api.generated.view.ComponentVersionView;
import com.blackducksoftware.integration.hub.api.generated.view.VulnerabilityV1View;
import com.blackducksoftware.integration.hub.api.view.MetaHandler;
import com.blackducksoftware.integration.hub.notification.content.VulnerabilitySourceQualifiedId;
import com.blackducksoftware.integration.hub.throwaway.ItemTypeEnum;
import com.blackducksoftware.integration.hub.throwaway.NotificationCategoryEnum;
import com.blackducksoftware.integration.hub.throwaway.NotificationContentItem;
import com.blackducksoftware.integration.hub.throwaway.NotificationEvent;
import com.blackducksoftware.integration.hub.throwaway.PolicyOverrideContentItem;
import com.blackducksoftware.integration.hub.throwaway.PolicyViolationClearedContentItem;
import com.blackducksoftware.integration.hub.throwaway.PolicyViolationContentItem;
import com.blackducksoftware.integration.hub.throwaway.VulnerabilityContentItem;
import com.blackducksoftware.integration.log.IntBufferedLogger;
import com.blackducksoftware.integration.log.IntLogger;

public class NotificationProcessorTest {
    private final EventTestUtil testUtil = new EventTestUtil();

    private MetaHandler metaService;

    @Before
    public void init() throws Exception {
        final IntLogger logger = new IntBufferedLogger();
        metaService = new MetaHandler(logger);
    }

    public MockProcessor createMockedNotificationProcessor() {
        final MockProcessor processor = new MockProcessor(metaService);
        return processor;
    }

    public MockProcessor createMockedNotificationProcessor(final List<VulnerabilityV1View> vulnerabilityList) throws Exception {
        final ComponentVersionView compVersion = Mockito.mock(ComponentVersionView.class);
        compVersion.json = createComponentJson();
        compVersion._meta = createComponentMeta();
        final MockProcessor processor = new MockProcessor(metaService);
        return processor;
    }

    private String createComponentJson() {
        return "{ \"_meta\": { \"href\": \"" + EventTestUtil.COMPONENT_VERSION_URL + "\"," + "\"links\": [ {" + "\"rel\": \"vulnerabilities\"," + "\"href\": \"" + EventTestUtil.COMPONENT_VERSION_URL + "\"},{"
                + "\"rel\":\"vulnerable-components\"," + "\"href\": \"" + EventTestUtil.COMPONENT_VERSION_URL + "\"" + "}]}}";
    }

    private ResourceMetadata createComponentMeta() {
        final ResourceMetadata meta = new ResourceMetadata();
        meta.href = EventTestUtil.COMPONENT_VERSION_URL;

        final ResourceLink vulnerabilityLink = new ResourceLink();
        vulnerabilityLink.rel = "vulnerabilities";
        vulnerabilityLink.href = EventTestUtil.COMPONENT_VERSION_URL;

        final ResourceLink vulnerableComponentLink = new ResourceLink();
        vulnerableComponentLink.rel = "vulnerable-components";
        vulnerableComponentLink.href = EventTestUtil.COMPONENT_VERSION_URL;

        final List<ResourceLink> links = new ArrayList<>();
        links.add(vulnerableComponentLink);
        links.add(vulnerabilityLink);

        meta.links = links;

        return meta;
    }

    private void assertPolicyDataValid(final Collection<NotificationEvent> eventList, final NotificationCategoryEnum categoryType) {
        int ruleIndex = 1;
        for (final NotificationEvent event : eventList) {
            final Map<String, Object> dataSet = event.getDataSet();

            final String componentKey = ItemTypeEnum.COMPONENT.name();
            assertTrue(dataSet.containsKey(componentKey));
            assertEquals(EventTestUtil.COMPONENT, dataSet.get(componentKey));

            final String versionKey = ItemTypeEnum.VERSION.name();
            assertTrue(dataSet.containsKey(versionKey));
            assertEquals(EventTestUtil.VERSION, dataSet.get(versionKey));

            final String ruleKey = ItemTypeEnum.RULE.name();
            assertTrue(dataSet.containsKey(ruleKey));
            assertEquals(EventTestUtil.PREFIX_RULE + ruleIndex, dataSet.get(ruleKey));
            ruleIndex++;
        }
    }

    @Test
    public void testPolicyViolationAdd() throws Exception {
        final SortedSet<NotificationContentItem> notifications = new TreeSet<>();
        notifications.add(testUtil.createPolicyViolation(new Date(), EventTestUtil.PROJECT_NAME, EventTestUtil.PROJECT_VERSION_NAME, EventTestUtil.COMPONENT, EventTestUtil.VERSION));
        final Collection<NotificationEvent> eventList = createMockedNotificationProcessor().process(notifications);

        assertPolicyDataValid(eventList, NotificationCategoryEnum.POLICY_VIOLATION);
    }

    @Test
    public void testPolicyViolationOverride() throws Exception {
        final SortedSet<NotificationContentItem> notifications = new TreeSet<>();
        notifications.add(testUtil.createPolicyOverride(new Date(), EventTestUtil.PROJECT_NAME, EventTestUtil.PROJECT_VERSION_NAME, EventTestUtil.COMPONENT, EventTestUtil.VERSION));
        final Collection<NotificationEvent> eventList = createMockedNotificationProcessor().process(notifications);
        assertFalse(eventList.isEmpty());
        assertPolicyDataValid(eventList, NotificationCategoryEnum.POLICY_VIOLATION_OVERRIDE);
    }

    @Test
    public void testPolicyViolationCleared() throws Exception {
        final SortedSet<NotificationContentItem> notifications = new TreeSet<>();
        notifications.add(testUtil.createPolicyCleared(new Date(), EventTestUtil.PROJECT_NAME, EventTestUtil.PROJECT_VERSION_NAME, EventTestUtil.COMPONENT, EventTestUtil.VERSION));
        final Collection<NotificationEvent> eventList = createMockedNotificationProcessor().process(notifications);
        assertFalse(eventList.isEmpty());
        assertPolicyDataValid(eventList, NotificationCategoryEnum.POLICY_VIOLATION_CLEARED);
    }

    @Test
    public void testPolicyViolationAndOverride() throws Exception {
        final SortedSet<NotificationContentItem> notifications = new TreeSet<>();
        Instant now = Instant.now();
        final PolicyViolationContentItem policyViolation = testUtil.createPolicyViolation(Date.from(now), EventTestUtil.PROJECT_NAME, EventTestUtil.PROJECT_VERSION_NAME, EventTestUtil.COMPONENT, EventTestUtil.VERSION);
        notifications.add(policyViolation);
        now = now.plusSeconds(1);
        final PolicyOverrideContentItem policyOverride = testUtil.createPolicyOverride(Date.from(now), EventTestUtil.PROJECT_NAME, EventTestUtil.PROJECT_VERSION_NAME, EventTestUtil.COMPONENT, EventTestUtil.VERSION);
        notifications.add(policyOverride);
        final Collection<NotificationEvent> eventList = createMockedNotificationProcessor().process(notifications);
        assertTrue(eventList.isEmpty());
    }

    @Test
    public void testPolicyViolationAndCleared() throws Exception {
        final SortedSet<NotificationContentItem> notifications = new TreeSet<>();
        Instant now = Instant.now();
        final PolicyViolationContentItem policyViolation = testUtil.createPolicyViolation(Date.from(now), EventTestUtil.PROJECT_NAME, EventTestUtil.PROJECT_VERSION_NAME, EventTestUtil.COMPONENT, EventTestUtil.VERSION);
        notifications.add(policyViolation);
        now = now.plusSeconds(1);
        final PolicyViolationClearedContentItem policyCleared = testUtil.createPolicyCleared(Date.from(now), EventTestUtil.PROJECT_NAME, EventTestUtil.PROJECT_VERSION_NAME, EventTestUtil.COMPONENT, EventTestUtil.VERSION);
        notifications.add(policyCleared);
        final Collection<NotificationEvent> eventList = createMockedNotificationProcessor().process(notifications);
        assertTrue(eventList.isEmpty());
    }

    @Test
    public void testPolicyViolationAndClearedAndViolated() throws Exception {
        final SortedSet<NotificationContentItem> notifications = new TreeSet<>();
        Instant now = Instant.now();
        PolicyViolationContentItem policyViolation = testUtil.createPolicyViolation(Date.from(now), EventTestUtil.PROJECT_NAME, EventTestUtil.PROJECT_VERSION_NAME, EventTestUtil.COMPONENT, EventTestUtil.VERSION);
        notifications.add(policyViolation);
        now = now.plusSeconds(1);
        final PolicyViolationClearedContentItem policyCleared = testUtil.createPolicyCleared(Date.from(now), EventTestUtil.PROJECT_NAME, EventTestUtil.PROJECT_VERSION_NAME, EventTestUtil.COMPONENT, EventTestUtil.VERSION);
        notifications.add(policyCleared);
        now = now.plusSeconds(1);
        policyViolation = testUtil.createPolicyViolation(Date.from(now), EventTestUtil.PROJECT_NAME, EventTestUtil.PROJECT_VERSION_NAME, EventTestUtil.COMPONENT, EventTestUtil.VERSION);
        notifications.add(policyViolation);
        final Collection<NotificationEvent> eventList = createMockedNotificationProcessor().process(notifications);
        assertPolicyDataValid(eventList, NotificationCategoryEnum.POLICY_VIOLATION);
    }

    @Test
    public void testPolicyViolationAndOverrideAndViolated() throws Exception {
        final SortedSet<NotificationContentItem> notifications = new TreeSet<>();
        Instant now = Instant.now();
        PolicyViolationContentItem policyViolation = testUtil.createPolicyViolation(Date.from(now), EventTestUtil.PROJECT_NAME, EventTestUtil.PROJECT_VERSION_NAME, EventTestUtil.COMPONENT, EventTestUtil.VERSION);
        notifications.add(policyViolation);
        now = now.plusSeconds(1);
        final PolicyOverrideContentItem policyCleared = testUtil.createPolicyOverride(Date.from(now), EventTestUtil.PROJECT_NAME, EventTestUtil.PROJECT_VERSION_NAME, EventTestUtil.COMPONENT, EventTestUtil.VERSION);
        notifications.add(policyCleared);
        now = now.plusSeconds(1);
        policyViolation = testUtil.createPolicyViolation(Date.from(now), EventTestUtil.PROJECT_NAME, EventTestUtil.PROJECT_VERSION_NAME, EventTestUtil.COMPONENT, EventTestUtil.VERSION);
        notifications.add(policyViolation);
        final Collection<NotificationEvent> eventList = createMockedNotificationProcessor().process(notifications);
        assertPolicyDataValid(eventList, NotificationCategoryEnum.POLICY_VIOLATION);
    }

    @Test
    public void testComplexPolicyOverride() throws Exception {
        final SortedSet<NotificationContentItem> notifications = new TreeSet<>();
        Instant now = Instant.now();
        PolicyViolationContentItem policyViolation = testUtil.createPolicyViolation(Date.from(now), EventTestUtil.PROJECT_NAME, EventTestUtil.PROJECT_VERSION_NAME, EventTestUtil.COMPONENT, EventTestUtil.VERSION);
        notifications.add(policyViolation);
        now = now.plusSeconds(1);
        policyViolation = testUtil.createPolicyViolation(Date.from(now), EventTestUtil.PROJECT_NAME, EventTestUtil.PROJECT_VERSION_NAME, EventTestUtil.COMPONENT, EventTestUtil.VERSION);
        notifications.add(policyViolation);
        now = now.plusSeconds(1);
        final PolicyOverrideContentItem policyOverride = testUtil.createPolicyOverride(Date.from(now), EventTestUtil.PROJECT_NAME, EventTestUtil.PROJECT_VERSION_NAME, EventTestUtil.COMPONENT, EventTestUtil.VERSION);
        notifications.add(policyOverride);
        now = now.plusSeconds(1);
        policyViolation = testUtil.createPolicyViolation(Date.from(now), EventTestUtil.PROJECT_NAME, EventTestUtil.PROJECT_VERSION_NAME, EventTestUtil.COMPONENT, EventTestUtil.VERSION);
        notifications.add(policyViolation);
        now = now.plusSeconds(1);
        final PolicyViolationClearedContentItem policyCleared = testUtil.createPolicyCleared(Date.from(now), EventTestUtil.PROJECT_NAME, EventTestUtil.PROJECT_VERSION_NAME, EventTestUtil.COMPONENT, EventTestUtil.VERSION);
        notifications.add(policyCleared);
        final Collection<NotificationEvent> eventList = createMockedNotificationProcessor().process(notifications);
        assertPolicyDataValid(eventList, NotificationCategoryEnum.POLICY_VIOLATION);
    }

    @Test
    public void testVulnerabilityAdded() throws Exception {
        final SortedSet<NotificationContentItem> notifications = new TreeSet<>();
        final List<VulnerabilitySourceQualifiedId> vulnerabilities = new LinkedList<>();
        vulnerabilities.add(createVulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.HIGH_VULN_ID));
        vulnerabilities.add(createVulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.MEDIUM_VULN_ID));
        vulnerabilities.add(createVulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.LOW_VULN_ID));
        final List<VulnerabilityV1View> vulnerabilityList = testUtil.createVulnerabiltyItemList(vulnerabilities);
        final Instant now = Instant.now();
        final List<VulnerabilitySourceQualifiedId> emptyVulnSourceList = Collections.emptyList();
        final VulnerabilityContentItem vulnerability = testUtil.createVulnerability(Date.from(now), EventTestUtil.PROJECT_NAME, EventTestUtil.PROJECT_VERSION_NAME, EventTestUtil.COMPONENT, EventTestUtil.VERSION, vulnerabilities,
                emptyVulnSourceList, emptyVulnSourceList);
        notifications.add(vulnerability);
        final Collection<NotificationEvent> eventList = createMockedNotificationProcessor(vulnerabilityList).process(notifications);

        for (final NotificationEvent event : eventList) {
            final Map<String, Object> dataSet = event.getDataSet();
            final String componentKey = ItemTypeEnum.COMPONENT.name();
            assertTrue(dataSet.containsKey(componentKey));
            assertEquals(EventTestUtil.COMPONENT, dataSet.get(componentKey));

            final String versionKey = ItemTypeEnum.VERSION.name();
            assertTrue(dataSet.containsKey(versionKey));
            assertEquals(EventTestUtil.VERSION, dataSet.get(versionKey));
        }
    }

    @Test
    public void testVulnerabilityUpdated() throws Exception {
        final SortedSet<NotificationContentItem> notifications = new TreeSet<>();
        final List<VulnerabilitySourceQualifiedId> vulnerabilities = new LinkedList<>();
        vulnerabilities.add(createVulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.HIGH_VULN_ID));
        vulnerabilities.add(createVulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.MEDIUM_VULN_ID));
        vulnerabilities.add(createVulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.LOW_VULN_ID));
        final List<VulnerabilityV1View> vulnerabilityList = testUtil.createVulnerabiltyItemList(vulnerabilities);

        final Instant now = Instant.now();
        final List<VulnerabilitySourceQualifiedId> emptyVulnSourceList = Collections.emptyList();
        final VulnerabilityContentItem vulnerability = testUtil.createVulnerability(Date.from(now), EventTestUtil.PROJECT_NAME, EventTestUtil.PROJECT_VERSION_NAME, EventTestUtil.COMPONENT, EventTestUtil.VERSION, emptyVulnSourceList,
                vulnerabilities, emptyVulnSourceList);
        notifications.add(vulnerability);
        final Collection<NotificationEvent> eventList = createMockedNotificationProcessor(vulnerabilityList).process(notifications);

        for (final NotificationEvent event : eventList) {
            final Map<String, Object> dataSet = event.getDataSet();
            final String componentKey = ItemTypeEnum.COMPONENT.name();
            assertTrue(dataSet.containsKey(componentKey));
            assertEquals(EventTestUtil.COMPONENT, dataSet.get(componentKey));

            final String versionKey = ItemTypeEnum.VERSION.name();
            assertTrue(dataSet.containsKey(versionKey));
            assertEquals(EventTestUtil.VERSION, dataSet.get(versionKey));
        }
    }

    @Test
    public void testVulnerabilityDeleted() throws Exception {
        final SortedSet<NotificationContentItem> notifications = new TreeSet<>();
        final List<VulnerabilitySourceQualifiedId> vulnerabilities = new LinkedList<>();
        vulnerabilities.add(createVulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.HIGH_VULN_ID));
        vulnerabilities.add(createVulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.MEDIUM_VULN_ID));
        vulnerabilities.add(createVulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.LOW_VULN_ID));

        final Instant now = Instant.now();
        final List<VulnerabilitySourceQualifiedId> emptyVulnSourceList = Collections.emptyList();
        final VulnerabilityContentItem vulnerability = testUtil.createVulnerability(Date.from(now), EventTestUtil.PROJECT_NAME, EventTestUtil.PROJECT_VERSION_NAME, EventTestUtil.COMPONENT, EventTestUtil.VERSION, emptyVulnSourceList,
                emptyVulnSourceList, vulnerabilities);
        notifications.add(vulnerability);
        final Collection<NotificationEvent> eventList = createMockedNotificationProcessor().process(notifications);
        assertTrue(eventList.isEmpty());
    }

    @Test
    public void testVulnAddedAndDeleted() throws Exception {
        final SortedSet<NotificationContentItem> notifications = new TreeSet<>();
        final List<VulnerabilitySourceQualifiedId> vulnerabilities = new LinkedList<>();
        vulnerabilities.add(createVulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.HIGH_VULN_ID));
        vulnerabilities.add(createVulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.MEDIUM_VULN_ID));
        vulnerabilities.add(createVulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.LOW_VULN_ID));

        final Instant now = Instant.now();
        final List<VulnerabilitySourceQualifiedId> emptyVulnSourceList = Collections.emptyList();
        final VulnerabilityContentItem vulnerability = testUtil.createVulnerability(Date.from(now), EventTestUtil.PROJECT_NAME, EventTestUtil.PROJECT_VERSION_NAME, EventTestUtil.COMPONENT, EventTestUtil.VERSION, vulnerabilities,
                emptyVulnSourceList, vulnerabilities);
        notifications.add(vulnerability);
        final Collection<NotificationEvent> eventList = createMockedNotificationProcessor().process(notifications);
        assertTrue(eventList.isEmpty());
    }

    @Test
    public void testComplexVulnerability() throws Exception {
        final SortedSet<NotificationContentItem> notifications = new TreeSet<>();
        Instant now = Instant.now();

        final List<VulnerabilitySourceQualifiedId> resultVulnList = new ArrayList<>(2);
        resultVulnList.add(createVulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.HIGH_VULN_ID));
        resultVulnList.add(createVulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.MEDIUM_VULN_ID));
        final List<VulnerabilityV1View> vulnerabilityList = testUtil.createVulnerabiltyItemList(resultVulnList);

        final List<VulnerabilitySourceQualifiedId> added = new ArrayList<>(3);
        added.add(createVulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.HIGH_VULN_ID));
        added.add(createVulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.MEDIUM_VULN_ID));
        added.add(createVulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.LOW_VULN_ID));

        final List<VulnerabilitySourceQualifiedId> updated = new ArrayList<>(4);
        updated.add(createVulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.HIGH_VULN_ID));
        updated.add(createVulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.MEDIUM_VULN_ID2));
        updated.add(createVulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.LOW_VULN_ID2));
        updated.add(createVulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.LOW_VULN_ID1));

        final List<VulnerabilitySourceQualifiedId> deleted = Collections.emptyList();
        now = now.plusSeconds(1);
        final VulnerabilityContentItem vulnerability = testUtil.createVulnerability(Date.from(now), EventTestUtil.PROJECT_NAME, EventTestUtil.PROJECT_VERSION_NAME, EventTestUtil.COMPONENT, EventTestUtil.VERSION, added, updated, deleted);
        notifications.add(vulnerability);

        final Collection<NotificationEvent> eventList = createMockedNotificationProcessor(vulnerabilityList).process(notifications);
        assertFalse(eventList.isEmpty());
        for (final NotificationEvent event : eventList) {
            final Map<String, Object> dataSet = event.getDataSet();
            final String componentKey = ItemTypeEnum.COMPONENT.name();
            assertTrue(dataSet.containsKey(componentKey));
            assertEquals(EventTestUtil.COMPONENT, dataSet.get(componentKey));

            final String versionKey = ItemTypeEnum.VERSION.name();
            assertTrue(dataSet.containsKey(versionKey));
            assertEquals(EventTestUtil.VERSION, dataSet.get(versionKey));
        }
    }

    @Test
    public void testComplexVulnerabilityMulti() throws Exception {
        final SortedSet<NotificationContentItem> notifications = new TreeSet<>();
        Instant now = Instant.now();

        final List<VulnerabilitySourceQualifiedId> resultVulnList = new ArrayList<>(2);
        resultVulnList.add(createVulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.HIGH_VULN_ID));
        resultVulnList.add(createVulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.MEDIUM_VULN_ID));
        final List<VulnerabilityV1View> vulnerabilityList = testUtil.createVulnerabiltyItemList(resultVulnList);

        final List<VulnerabilitySourceQualifiedId> added1 = new LinkedList<>();
        added1.add(createVulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.HIGH_VULN_ID));
        added1.add(createVulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.MEDIUM_VULN_ID));
        added1.add(createVulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.LOW_VULN_ID));

        final List<VulnerabilitySourceQualifiedId> updated1 = new LinkedList<>();
        updated1.add(createVulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.HIGH_VULN_ID));
        updated1.add(createVulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.MEDIUM_VULN_ID2));
        updated1.add(createVulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.LOW_VULN_ID2));
        updated1.add(createVulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.LOW_VULN_ID1));

        final List<VulnerabilitySourceQualifiedId> deleted1 = Collections.emptyList();
        now = now.plusSeconds(1);
        final VulnerabilityContentItem vulnerability = testUtil.createVulnerability(Date.from(now), EventTestUtil.PROJECT_NAME, EventTestUtil.PROJECT_VERSION_NAME, EventTestUtil.COMPONENT, EventTestUtil.VERSION, added1, updated1,
                deleted1);
        notifications.add(vulnerability);

        final List<VulnerabilitySourceQualifiedId> added2 = new LinkedList<>();
        added1.add(createVulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.HIGH_VULN_ID));
        added1.add(createVulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.MEDIUM_VULN_ID));
        added1.add(createVulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.LOW_VULN_ID));

        final List<VulnerabilitySourceQualifiedId> updated2 = new LinkedList<>();
        updated1.add(createVulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.HIGH_VULN_ID));
        updated1.add(createVulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.MEDIUM_VULN_ID2));
        updated1.add(createVulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.LOW_VULN_ID2));
        updated1.add(createVulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.LOW_VULN_ID1));

        final List<VulnerabilitySourceQualifiedId> deleted2 = Collections.emptyList();
        now = now.plusSeconds(1);
        final VulnerabilityContentItem vulnerability2 = testUtil.createVulnerability(Date.from(now), EventTestUtil.PROJECT_NAME, EventTestUtil.PROJECT_VERSION_NAME, EventTestUtil.COMPONENT, EventTestUtil.VERSION, added2, updated2,
                deleted2);
        notifications.add(vulnerability2);

        final Collection<NotificationEvent> eventList = createMockedNotificationProcessor(vulnerabilityList).process(notifications);
        assertFalse(eventList.isEmpty());
        for (final NotificationEvent event : eventList) {
            final Map<String, Object> dataSet = event.getDataSet();
            final String componentKey = ItemTypeEnum.COMPONENT.name();
            assertTrue(dataSet.containsKey(componentKey));
            assertEquals(EventTestUtil.COMPONENT, dataSet.get(componentKey));

            final String versionKey = ItemTypeEnum.VERSION.name();
            assertTrue(dataSet.containsKey(versionKey));
            assertEquals(EventTestUtil.VERSION, dataSet.get(versionKey));
        }
    }

    private VulnerabilitySourceQualifiedId createVulnerabilitySourceQualifiedId(final String source, final String id) {
        final VulnerabilitySourceQualifiedId vulnerabilitySourceQualifiedId = new VulnerabilitySourceQualifiedId();
        vulnerabilitySourceQualifiedId.source = source;
        vulnerabilitySourceQualifiedId.vulnerabilityId = id;
        return vulnerabilitySourceQualifiedId;
    }

}
