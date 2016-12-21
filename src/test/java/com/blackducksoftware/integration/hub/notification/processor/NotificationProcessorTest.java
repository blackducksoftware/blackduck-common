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
package com.blackducksoftware.integration.hub.notification.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.api.component.version.ComponentVersion;
import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.api.notification.VulnerabilitySourceQualifiedId;
import com.blackducksoftware.integration.hub.api.vulnerability.VulnerabilityItem;
import com.blackducksoftware.integration.hub.api.vulnerability.VulnerabilityRequestService;
import com.blackducksoftware.integration.hub.dataservice.notification.item.NotificationContentItem;
import com.blackducksoftware.integration.hub.dataservice.notification.item.PolicyOverrideContentItem;
import com.blackducksoftware.integration.hub.dataservice.notification.item.PolicyViolationClearedContentItem;
import com.blackducksoftware.integration.hub.dataservice.notification.item.PolicyViolationContentItem;
import com.blackducksoftware.integration.hub.dataservice.notification.item.VulnerabilityContentItem;
import com.blackducksoftware.integration.hub.notification.processor.event.NotificationEvent;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubRequestService;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.log.IntBufferedLogger;
import com.blackducksoftware.integration.log.IntLogger;

public class NotificationProcessorTest {

    private final EventTestUtil testUtil = new EventTestUtil();

    private MetaService metaService;

    @Before
    public void init() throws Exception {
        final RestConnection restConnection = new MockRestConnection();
        final HubServicesFactory factory = new HubServicesFactory(restConnection);
        final IntLogger logger = new IntBufferedLogger();
        metaService = factory.createMetaService(logger);
    }

    public MockProcessor createMockedNotificationProcessor() {
        final VulnerabilityRequestService vulnerabilityRequestService = Mockito.mock(VulnerabilityRequestService.class);
        final HubRequestService hubRequestService = Mockito.mock(HubRequestService.class);
        final MockProcessor processor = new MockProcessor(hubRequestService, vulnerabilityRequestService, metaService);
        return processor;
    }

    public MockProcessor createMockedNotificationProcessor(List<VulnerabilityItem> vulnerabilityList) throws Exception {
        final ComponentVersion compVersion = Mockito.mock(ComponentVersion.class);
        Mockito.when(compVersion.getJson()).thenReturn(createComponentJson());
        final VulnerabilityRequestService vulnerabilityRequestService = Mockito.mock(VulnerabilityRequestService.class);
        final HubRequestService hubRequestService = Mockito.mock(HubRequestService.class);
        Mockito.when(hubRequestService.getItem(Mockito.anyString(), Mockito.eq(ComponentVersion.class))).thenReturn(compVersion);
        Mockito.when(vulnerabilityRequestService.getComponentVersionVulnerabilities(Mockito.anyString())).thenReturn(vulnerabilityList);
        final MockProcessor processor = new MockProcessor(hubRequestService, vulnerabilityRequestService, metaService);
        return processor;
    }

    private String createComponentJson() {
        return "{ \"_meta\": { \"href\": \"" + EventTestUtil.COMPONENT_VERSION_URL + "\","
                + "\"links\": [ {"
                + "\"rel\": \"vulnerabilities\","
                + "\"href\": \"" + EventTestUtil.COMPONENT_VERSION_URL + "\"},{"
                + "\"rel\":\"vulnerable-components\","
                + "\"href\": \"" + EventTestUtil.COMPONENT_VERSION_URL + "\""
                + "}]}}";
    }

    private void assertPolicyDataValid(final Collection<NotificationEvent<?>> eventList, NotificationCategoryEnum categoryType) {
        int ruleIndex = 1;
        for (final NotificationEvent<?> event : eventList) {
            assertEquals(EventTestUtil.PROJECT_NAME, event.getNotificationContent().getProjectVersion().getProjectName());
            assertEquals(EventTestUtil.PROJECT_VERSION_NAME, event.getNotificationContent().getProjectVersion().getProjectVersionName());
            final Set<ItemEntry> dataSet = event.getDataSet();

            final ItemEntry componentKey = new ItemEntry(ItemTypeEnum.COMPONENT.name(), EventTestUtil.COMPONENT);
            assertTrue(dataSet.contains(componentKey));

            final ItemEntry versionKey = new ItemEntry("", EventTestUtil.VERSION);
            assertTrue(dataSet.contains(versionKey));

            final ItemEntry ruleKey = new ItemEntry(ItemTypeEnum.RULE.name(), EventTestUtil.PREFIX_RULE + ruleIndex);
            assertTrue(dataSet.contains(ruleKey));
            ruleIndex++;
        }
    }

    @Test
    public void testPolicyViolationAdd() throws Exception {
        final SortedSet<NotificationContentItem> notifications = new TreeSet<>();
        notifications.add(
                testUtil.createPolicyViolation(new Date(), EventTestUtil.PROJECT_NAME, EventTestUtil.PROJECT_VERSION_NAME, EventTestUtil.COMPONENT,
                        EventTestUtil.VERSION));
        final Collection<NotificationEvent<?>> eventList = createMockedNotificationProcessor().process(notifications);

        assertPolicyDataValid(eventList, NotificationCategoryEnum.POLICY_VIOLATION);
    }

    @Test
    public void testPolicyViolationOverride() throws Exception {
        final SortedSet<NotificationContentItem> notifications = new TreeSet<>();
        notifications.add(
                testUtil.createPolicyOverride(new Date(), EventTestUtil.PROJECT_NAME, EventTestUtil.PROJECT_VERSION_NAME, EventTestUtil.COMPONENT,
                        EventTestUtil.VERSION));
        final Collection<NotificationEvent<?>> eventList = createMockedNotificationProcessor().process(notifications);
        assertFalse(eventList.isEmpty());
        assertPolicyDataValid(eventList, NotificationCategoryEnum.POLICY_VIOLATION_OVERRIDE);
    }

    @Test
    public void testPolicyViolationCleared() throws Exception {
        final SortedSet<NotificationContentItem> notifications = new TreeSet<>();
        notifications.add(
                testUtil.createPolicyCleared(new Date(), EventTestUtil.PROJECT_NAME, EventTestUtil.PROJECT_VERSION_NAME, EventTestUtil.COMPONENT,
                        EventTestUtil.VERSION));
        final Collection<NotificationEvent<?>> eventList = createMockedNotificationProcessor().process(notifications);
        assertFalse(eventList.isEmpty());
        assertPolicyDataValid(eventList, NotificationCategoryEnum.POLICY_VIOLATION_CLEARED);
    }

    @Test
    public void testPolicyViolationAndOverride() throws Exception {
        final SortedSet<NotificationContentItem> notifications = new TreeSet<>();
        DateTime dateTime = new DateTime();
        final PolicyViolationContentItem policyViolation = testUtil.createPolicyViolation(dateTime.toDate(), EventTestUtil.PROJECT_NAME,
                EventTestUtil.PROJECT_VERSION_NAME, EventTestUtil.COMPONENT, EventTestUtil.VERSION);
        notifications.add(policyViolation);
        dateTime = dateTime.plusSeconds(1);
        final PolicyOverrideContentItem policyOverride = testUtil.createPolicyOverride(dateTime.toDate(), EventTestUtil.PROJECT_NAME,
                EventTestUtil.PROJECT_VERSION_NAME, EventTestUtil.COMPONENT, EventTestUtil.VERSION);
        notifications.add(policyOverride);
        final Collection<NotificationEvent<?>> eventList = createMockedNotificationProcessor().process(notifications);
        assertTrue(eventList.isEmpty());
    }

    @Test
    public void testPolicyViolationAndCleared() throws Exception {
        final SortedSet<NotificationContentItem> notifications = new TreeSet<>();
        DateTime dateTime = new DateTime();
        final PolicyViolationContentItem policyViolation = testUtil.createPolicyViolation(dateTime.toDate(), EventTestUtil.PROJECT_NAME,
                EventTestUtil.PROJECT_VERSION_NAME, EventTestUtil.COMPONENT, EventTestUtil.VERSION);
        notifications.add(policyViolation);
        dateTime = dateTime.plusSeconds(1);
        final PolicyViolationClearedContentItem policyCleared = testUtil.createPolicyCleared(dateTime.toDate(), EventTestUtil.PROJECT_NAME,
                EventTestUtil.PROJECT_VERSION_NAME, EventTestUtil.COMPONENT, EventTestUtil.VERSION);
        notifications.add(policyCleared);
        final Collection<NotificationEvent<?>> eventList = createMockedNotificationProcessor().process(notifications);
        assertTrue(eventList.isEmpty());
    }

    @Test
    public void testPolicyViolationAndClearedAndViolated() throws Exception {
        final SortedSet<NotificationContentItem> notifications = new TreeSet<>();
        DateTime dateTime = new DateTime();
        PolicyViolationContentItem policyViolation = testUtil.createPolicyViolation(dateTime.toDate(), EventTestUtil.PROJECT_NAME,
                EventTestUtil.PROJECT_VERSION_NAME, EventTestUtil.COMPONENT, EventTestUtil.VERSION);
        notifications.add(policyViolation);
        dateTime = dateTime.plusSeconds(1);
        final PolicyViolationClearedContentItem policyCleared = testUtil.createPolicyCleared(dateTime.toDate(), EventTestUtil.PROJECT_NAME,
                EventTestUtil.PROJECT_VERSION_NAME, EventTestUtil.COMPONENT, EventTestUtil.VERSION);
        notifications.add(policyCleared);
        dateTime = dateTime.plusSeconds(1);
        policyViolation = testUtil.createPolicyViolation(dateTime.toDate(), EventTestUtil.PROJECT_NAME, EventTestUtil.PROJECT_VERSION_NAME,
                EventTestUtil.COMPONENT,
                EventTestUtil.VERSION);
        notifications.add(policyViolation);
        final Collection<NotificationEvent<?>> eventList = createMockedNotificationProcessor().process(notifications);
        assertPolicyDataValid(eventList, NotificationCategoryEnum.POLICY_VIOLATION);
    }

    @Test
    public void testPolicyViolationAndOverrideAndViolated() throws Exception {
        final SortedSet<NotificationContentItem> notifications = new TreeSet<>();
        DateTime dateTime = new DateTime();
        PolicyViolationContentItem policyViolation = testUtil.createPolicyViolation(dateTime.toDate(), EventTestUtil.PROJECT_NAME,
                EventTestUtil.PROJECT_VERSION_NAME, EventTestUtil.COMPONENT, EventTestUtil.VERSION);
        notifications.add(policyViolation);
        dateTime = dateTime.plusSeconds(1);
        final PolicyOverrideContentItem policyCleared = testUtil.createPolicyOverride(dateTime.toDate(), EventTestUtil.PROJECT_NAME,
                EventTestUtil.PROJECT_VERSION_NAME, EventTestUtil.COMPONENT, EventTestUtil.VERSION);
        notifications.add(policyCleared);
        dateTime = dateTime.plusSeconds(1);
        policyViolation = testUtil.createPolicyViolation(dateTime.toDate(), EventTestUtil.PROJECT_NAME, EventTestUtil.PROJECT_VERSION_NAME,
                EventTestUtil.COMPONENT,
                EventTestUtil.VERSION);
        notifications.add(policyViolation);
        final Collection<NotificationEvent<?>> eventList = createMockedNotificationProcessor().process(notifications);
        assertPolicyDataValid(eventList, NotificationCategoryEnum.POLICY_VIOLATION);
    }

    @Test
    public void testComplexPolicyOverride() throws Exception {
        final SortedSet<NotificationContentItem> notifications = new TreeSet<>();
        DateTime dateTime = new DateTime();
        PolicyViolationContentItem policyViolation = testUtil.createPolicyViolation(dateTime.toDate(), EventTestUtil.PROJECT_NAME,
                EventTestUtil.PROJECT_VERSION_NAME, EventTestUtil.COMPONENT, EventTestUtil.VERSION);
        notifications.add(policyViolation);
        dateTime = dateTime.plusSeconds(1);
        policyViolation = testUtil.createPolicyViolation(dateTime.toDate(), EventTestUtil.PROJECT_NAME, EventTestUtil.PROJECT_VERSION_NAME,
                EventTestUtil.COMPONENT,
                EventTestUtil.VERSION);
        notifications.add(policyViolation);
        dateTime = dateTime.plusSeconds(1);
        final PolicyOverrideContentItem policyOverride = testUtil.createPolicyOverride(dateTime.toDate(), EventTestUtil.PROJECT_NAME,
                EventTestUtil.PROJECT_VERSION_NAME, EventTestUtil.COMPONENT, EventTestUtil.VERSION);
        notifications.add(policyOverride);
        dateTime = dateTime.plusSeconds(1);
        policyViolation = testUtil.createPolicyViolation(dateTime.toDate(), EventTestUtil.PROJECT_NAME, EventTestUtil.PROJECT_VERSION_NAME,
                EventTestUtil.COMPONENT,
                EventTestUtil.VERSION);
        notifications.add(policyViolation);
        dateTime = dateTime.plusSeconds(1);
        final PolicyViolationClearedContentItem policyCleared = testUtil.createPolicyCleared(dateTime.toDate(), EventTestUtil.PROJECT_NAME,
                EventTestUtil.PROJECT_VERSION_NAME, EventTestUtil.COMPONENT, EventTestUtil.VERSION);
        notifications.add(policyCleared);
        final Collection<NotificationEvent<?>> eventList = createMockedNotificationProcessor().process(notifications);
        assertPolicyDataValid(eventList, NotificationCategoryEnum.POLICY_VIOLATION);
    }

    @Test
    public void testVulnerabilityAdded() throws Exception {
        final SortedSet<NotificationContentItem> notifications = new TreeSet<>();
        final List<VulnerabilitySourceQualifiedId> vulnerabilities = new LinkedList<>();
        vulnerabilities.add(new VulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.HIGH_VULN_ID));
        vulnerabilities.add(new VulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.MEDIUM_VULN_ID));
        vulnerabilities.add(new VulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.LOW_VULN_ID));
        final List<VulnerabilityItem> vulnerabilityList = testUtil.createVulnerabiltyItemList(vulnerabilities);
        final DateTime dateTime = new DateTime();
        final List<VulnerabilitySourceQualifiedId> emptyVulnSourceList = Collections.emptyList();
        final VulnerabilityContentItem vulnerability = testUtil.createVulnerability(dateTime.toDate(), EventTestUtil.PROJECT_NAME,
                EventTestUtil.PROJECT_VERSION_NAME, EventTestUtil.COMPONENT, EventTestUtil.VERSION, vulnerabilities, emptyVulnSourceList,
                emptyVulnSourceList);
        notifications.add(vulnerability);
        final Collection<NotificationEvent<?>> eventList = createMockedNotificationProcessor(vulnerabilityList).process(notifications);

        for (final NotificationEvent<?> event : eventList) {

            final Set<ItemEntry> dataSet = event.getDataSet();
            final ItemEntry componentKey = new ItemEntry(ItemTypeEnum.COMPONENT.name(), EventTestUtil.COMPONENT);
            assertTrue(dataSet.contains(componentKey));

            final ItemEntry versionKey = new ItemEntry("", EventTestUtil.VERSION);
            assertTrue(dataSet.contains(versionKey));
        }
    }

    @Test
    public void testVulnerabilityUpdated() throws Exception {
        final SortedSet<NotificationContentItem> notifications = new TreeSet<>();
        final List<VulnerabilitySourceQualifiedId> vulnerabilities = new LinkedList<>();
        vulnerabilities.add(new VulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.HIGH_VULN_ID));
        vulnerabilities.add(new VulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.MEDIUM_VULN_ID));
        vulnerabilities.add(new VulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.LOW_VULN_ID));
        final List<VulnerabilityItem> vulnerabilityList = testUtil.createVulnerabiltyItemList(vulnerabilities);

        final DateTime dateTime = new DateTime();
        final List<VulnerabilitySourceQualifiedId> emptyVulnSourceList = Collections.emptyList();
        final VulnerabilityContentItem vulnerability = testUtil.createVulnerability(dateTime.toDate(), EventTestUtil.PROJECT_NAME,
                EventTestUtil.PROJECT_VERSION_NAME, EventTestUtil.COMPONENT, EventTestUtil.VERSION, emptyVulnSourceList, vulnerabilities,
                emptyVulnSourceList);
        notifications.add(vulnerability);
        final Collection<NotificationEvent<?>> eventList = createMockedNotificationProcessor(vulnerabilityList).process(notifications);

        for (final NotificationEvent<?> event : eventList) {
            final Set<ItemEntry> dataSet = event.getDataSet();
            final ItemEntry componentKey = new ItemEntry(ItemTypeEnum.COMPONENT.name(), EventTestUtil.COMPONENT);
            assertTrue(dataSet.contains(componentKey));

            final ItemEntry versionKey = new ItemEntry("", EventTestUtil.VERSION);
            assertTrue(dataSet.contains(versionKey));
        }
    }

    @Test
    public void testVulnerabilityDeleted() throws Exception {
        final SortedSet<NotificationContentItem> notifications = new TreeSet<>();
        final List<VulnerabilitySourceQualifiedId> vulnerabilities = new LinkedList<>();
        vulnerabilities.add(new VulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.HIGH_VULN_ID));
        vulnerabilities.add(new VulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.MEDIUM_VULN_ID));
        vulnerabilities.add(new VulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.LOW_VULN_ID));

        final DateTime dateTime = new DateTime();
        final List<VulnerabilitySourceQualifiedId> emptyVulnSourceList = Collections.emptyList();
        final VulnerabilityContentItem vulnerability = testUtil.createVulnerability(dateTime.toDate(), EventTestUtil.PROJECT_NAME,
                EventTestUtil.PROJECT_VERSION_NAME, EventTestUtil.COMPONENT, EventTestUtil.VERSION, emptyVulnSourceList,
                emptyVulnSourceList,
                vulnerabilities);
        notifications.add(vulnerability);
        final Collection<NotificationEvent<?>> eventList = createMockedNotificationProcessor().process(notifications);
        assertTrue(eventList.isEmpty());
    }

    @Test
    public void testVulnAddedAndDeleted() throws Exception {
        final SortedSet<NotificationContentItem> notifications = new TreeSet<>();
        final List<VulnerabilitySourceQualifiedId> vulnerabilities = new LinkedList<>();
        vulnerabilities.add(new VulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.HIGH_VULN_ID));
        vulnerabilities.add(new VulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.MEDIUM_VULN_ID));
        vulnerabilities.add(new VulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.LOW_VULN_ID));

        final DateTime dateTime = new DateTime();
        final List<VulnerabilitySourceQualifiedId> emptyVulnSourceList = Collections.emptyList();
        final VulnerabilityContentItem vulnerability = testUtil.createVulnerability(dateTime.toDate(), EventTestUtil.PROJECT_NAME,
                EventTestUtil.PROJECT_VERSION_NAME, EventTestUtil.COMPONENT, EventTestUtil.VERSION, vulnerabilities, emptyVulnSourceList,
                vulnerabilities);
        notifications.add(vulnerability);
        final Collection<NotificationEvent<?>> eventList = createMockedNotificationProcessor().process(notifications);
        assertTrue(eventList.isEmpty());
    }

    @Test
    public void testComplexVulnerability() throws Exception {
        final SortedSet<NotificationContentItem> notifications = new TreeSet<>();
        DateTime dateTime = new DateTime();

        final List<VulnerabilitySourceQualifiedId> resultVulnList = new ArrayList<>(2);
        resultVulnList.add(new VulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.HIGH_VULN_ID));
        resultVulnList.add(new VulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.MEDIUM_VULN_ID));
        final List<VulnerabilityItem> vulnerabilityList = testUtil.createVulnerabiltyItemList(resultVulnList);

        final List<VulnerabilitySourceQualifiedId> added = new ArrayList<>(3);
        added.add(new VulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.HIGH_VULN_ID));
        added.add(new VulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.MEDIUM_VULN_ID));
        added.add(new VulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.LOW_VULN_ID));

        final List<VulnerabilitySourceQualifiedId> updated = new ArrayList<>(4);
        updated.add(new VulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.HIGH_VULN_ID));
        updated.add(new VulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.MEDIUM_VULN_ID2));
        updated.add(new VulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.LOW_VULN_ID2));
        updated.add(new VulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.LOW_VULN_ID1));

        final List<VulnerabilitySourceQualifiedId> deleted = Collections.emptyList();
        dateTime = dateTime.plusSeconds(1);
        final VulnerabilityContentItem vulnerability = testUtil.createVulnerability(dateTime.toDate(), EventTestUtil.PROJECT_NAME,
                EventTestUtil.PROJECT_VERSION_NAME, EventTestUtil.COMPONENT, EventTestUtil.VERSION, added, updated, deleted);
        notifications.add(vulnerability);

        final Collection<NotificationEvent<?>> eventList = createMockedNotificationProcessor(vulnerabilityList).process(notifications);
        assertFalse(eventList.isEmpty());
        for (final NotificationEvent<?> event : eventList) {

            final Set<ItemEntry> dataSet = event.getDataSet();
            final ItemEntry componentKey = new ItemEntry(ItemTypeEnum.COMPONENT.name(), EventTestUtil.COMPONENT);
            assertTrue(dataSet.contains(componentKey));

            final ItemEntry versionKey = new ItemEntry("", EventTestUtil.VERSION);
            assertTrue(dataSet.contains(versionKey));
        }
    }

    @Test
    public void testComplexVulnerabilityMulti() throws Exception {
        final SortedSet<NotificationContentItem> notifications = new TreeSet<>();
        DateTime dateTime = new DateTime();

        final List<VulnerabilitySourceQualifiedId> resultVulnList = new ArrayList<>(2);
        resultVulnList.add(new VulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.HIGH_VULN_ID));
        resultVulnList.add(new VulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.MEDIUM_VULN_ID));
        final List<VulnerabilityItem> vulnerabilityList = testUtil.createVulnerabiltyItemList(resultVulnList);

        final List<VulnerabilitySourceQualifiedId> added1 = new LinkedList<>();
        added1.add(new VulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.HIGH_VULN_ID));
        added1.add(new VulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.MEDIUM_VULN_ID));
        added1.add(new VulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.LOW_VULN_ID));

        final List<VulnerabilitySourceQualifiedId> updated1 = new LinkedList<>();
        updated1.add(new VulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.HIGH_VULN_ID));
        updated1.add(new VulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.MEDIUM_VULN_ID2));
        updated1.add(new VulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.LOW_VULN_ID2));
        updated1.add(new VulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.LOW_VULN_ID1));

        final List<VulnerabilitySourceQualifiedId> deleted1 = Collections.emptyList();
        dateTime = dateTime.plusSeconds(1);
        final VulnerabilityContentItem vulnerability = testUtil.createVulnerability(dateTime.toDate(), EventTestUtil.PROJECT_NAME,
                EventTestUtil.PROJECT_VERSION_NAME, EventTestUtil.COMPONENT, EventTestUtil.VERSION, added1, updated1, deleted1);
        notifications.add(vulnerability);

        final List<VulnerabilitySourceQualifiedId> added2 = new LinkedList<>();
        added1.add(new VulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.HIGH_VULN_ID));
        added1.add(new VulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.MEDIUM_VULN_ID));
        added1.add(new VulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.LOW_VULN_ID));

        final List<VulnerabilitySourceQualifiedId> updated2 = new LinkedList<>();
        updated1.add(new VulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.HIGH_VULN_ID));
        updated1.add(new VulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.MEDIUM_VULN_ID2));
        updated1.add(new VulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.LOW_VULN_ID2));
        updated1.add(new VulnerabilitySourceQualifiedId(EventTestUtil.VULN_SOURCE, EventTestUtil.LOW_VULN_ID1));

        final List<VulnerabilitySourceQualifiedId> deleted2 = Collections.emptyList();
        dateTime = dateTime.plusSeconds(1);
        final VulnerabilityContentItem vulnerability2 = testUtil.createVulnerability(dateTime.toDate(), EventTestUtil.PROJECT_NAME,
                EventTestUtil.PROJECT_VERSION_NAME, EventTestUtil.COMPONENT, EventTestUtil.VERSION, added2, updated2, deleted2);
        notifications.add(vulnerability2);

        final Collection<NotificationEvent<?>> eventList = createMockedNotificationProcessor(vulnerabilityList).process(notifications);
        assertFalse(eventList.isEmpty());
        for (final NotificationEvent<?> event : eventList) {
            final Set<ItemEntry> dataSet = event.getDataSet();
            final ItemEntry componentKey = new ItemEntry(ItemTypeEnum.COMPONENT.name(), EventTestUtil.COMPONENT);
            assertTrue(dataSet.contains(componentKey));

            final ItemEntry versionKey = new ItemEntry("", EventTestUtil.VERSION);
            assertTrue(dataSet.contains(versionKey));
        }
    }
}
