/**
 * Hub Common
 *
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
 */
package com.blackducksoftware.integration.hub.notification.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.api.policy.PolicyRule;
import com.blackducksoftware.integration.hub.dataservice.notification.item.PolicyViolationContentItem;
import com.blackducksoftware.integration.hub.notification.processor.event.PolicyEvent;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.log.IntBufferedLogger;
import com.blackducksoftware.integration.log.IntLogger;

public class MapProcessorCacheTest {

    private final EventTestUtil testUtil = new EventTestUtil();

    private MetaService metaService;

    @Before
    public void init() throws Exception {
        final RestConnection restConnection = new MockRestConnection();
        final HubServicesFactory factory = new HubServicesFactory(restConnection);
        final IntLogger logger = new IntBufferedLogger();
        metaService = factory.createMetaService(logger);
    }

    @Test
    public void testEventAdd() throws Exception {
        final PolicyViolationContentItem item = testUtil.createPolicyViolation(new Date(), EventTestUtil.PROJECT_NAME, EventTestUtil.PROJECT_VERSION_NAME,
                EventTestUtil.COMPONENT,
                EventTestUtil.VERSION);
        final PolicyRule policyRule = item.getPolicyRuleList().get(0);
        final PolicyEvent event = new PolicyEvent(NotificationCategoryEnum.POLICY_VIOLATION, item, policyRule,
                metaService.getHref(policyRule));

        final List<PolicyEvent> eventList = new ArrayList<>();
        final MapProcessorCache<PolicyEvent> cache = new MapProcessorCache<>();
        eventList.add(event);

        cache.addEvent(event);
        cache.addEvent(event);
        cache.addEvent(event);
        assertEquals(eventList.size(), cache.getEvents().size());
        int index = 0;
        for (final PolicyEvent cachedEvent : cache.getEvents()) {
            assertEquals(eventList.get(index), cachedEvent);
            index++;
        }
    }

    @Test
    public void testEventRemove() throws Exception {
        final PolicyViolationContentItem item = testUtil.createPolicyViolation(new Date(), EventTestUtil.PROJECT_NAME, EventTestUtil.PROJECT_VERSION_NAME,
                EventTestUtil.COMPONENT,
                EventTestUtil.VERSION);

        final PolicyViolationContentItem removeItem = testUtil.createPolicyViolation(new Date(), EventTestUtil.PROJECT_NAME2,
                EventTestUtil.PROJECT_VERSION_NAME2,
                EventTestUtil.COMPONENT2,
                EventTestUtil.VERSION2);
        final PolicyRule policyRule1 = item.getPolicyRuleList().get(0);
        final PolicyRule policyRule2 = removeItem.getPolicyRuleList().get(0);
        final PolicyEvent event = new PolicyEvent(NotificationCategoryEnum.POLICY_VIOLATION, item, policyRule1,
                metaService.getHref(policyRule1));
        final PolicyEvent removeEvent = new PolicyEvent(NotificationCategoryEnum.POLICY_VIOLATION, removeItem, policyRule2,
                metaService.getHref(policyRule2));
        final List<PolicyEvent> eventList = new ArrayList<>();
        final MapProcessorCache<PolicyEvent> cache = new MapProcessorCache<>();
        eventList.add(event);
        eventList.add(removeEvent);

        cache.addEvent(event);
        cache.addEvent(event);
        cache.addEvent(removeEvent);
        cache.addEvent(event);
        cache.removeEvent(removeEvent);
        assertEquals(eventList.size() - 1, cache.getEvents().size());
        boolean found = false;
        for (final PolicyEvent cachedEvent : cache.getEvents()) {
            if (cachedEvent.equals(removeEvent)) {
                found = true;
            }
        }
        assertFalse(found);
    }

    @Test
    public void testRemoveViaKey() throws Exception {
        final PolicyViolationContentItem item = testUtil.createPolicyViolation(new Date(), EventTestUtil.PROJECT_NAME, EventTestUtil.PROJECT_VERSION_NAME,
                EventTestUtil.COMPONENT,
                EventTestUtil.VERSION);

        final PolicyViolationContentItem removeItem = testUtil.createPolicyViolation(new Date(), EventTestUtil.PROJECT_NAME2,
                EventTestUtil.PROJECT_VERSION_NAME2,
                EventTestUtil.COMPONENT2,
                EventTestUtil.VERSION2);
        final PolicyRule policyRule1 = item.getPolicyRuleList().get(0);
        final PolicyRule policyRule2 = removeItem.getPolicyRuleList().get(0);
        final PolicyEvent event = new PolicyEvent(NotificationCategoryEnum.POLICY_VIOLATION, item, policyRule1,
                metaService.getHref(policyRule1));
        final PolicyEvent removeEvent = new PolicyEvent(NotificationCategoryEnum.POLICY_VIOLATION, removeItem,
                policyRule2, metaService.getHref(policyRule2));
        final List<PolicyEvent> eventList = new ArrayList<>();
        final MapProcessorCache<PolicyEvent> cache = new MapProcessorCache<>();
        eventList.add(event);
        eventList.add(removeEvent);

        cache.addEvent(event);
        cache.addEvent(event);
        cache.addEvent(removeEvent);
        cache.addEvent(event);
        cache.removeEvent(removeEvent.getEventKey());
        assertEquals(eventList.size() - 1, cache.getEvents().size());
        boolean found = false;
        for (final PolicyEvent cachedEvent : cache.getEvents()) {
            if (cachedEvent.equals(removeEvent)) {
                found = true;
            }
        }
        assertFalse(found);
    }

    @Test
    public void testHasEvent() throws Exception {
        final PolicyViolationContentItem item = testUtil.createPolicyViolation(new Date(), EventTestUtil.PROJECT_NAME, EventTestUtil.PROJECT_VERSION_NAME,
                EventTestUtil.COMPONENT,
                EventTestUtil.VERSION);

        final PolicyViolationContentItem item2 = testUtil.createPolicyViolation(new Date(), EventTestUtil.PROJECT_NAME2,
                EventTestUtil.PROJECT_VERSION_NAME2,
                EventTestUtil.COMPONENT2,
                EventTestUtil.VERSION2);
        final PolicyRule policyRule1 = item.getPolicyRuleList().get(0);
        final PolicyRule policyRule2 = item2.getPolicyRuleList().get(0);
        final PolicyEvent event = new PolicyEvent(NotificationCategoryEnum.POLICY_VIOLATION, item, policyRule1,
                metaService.getHref(policyRule1));
        final PolicyEvent event2 = new PolicyEvent(NotificationCategoryEnum.POLICY_VIOLATION, item2,
                policyRule2, metaService.getHref(policyRule2));
        final MapProcessorCache<PolicyEvent> cache = new MapProcessorCache<>();

        cache.addEvent(event);

        assertTrue(cache.hasEvent(event.getEventKey()));
        assertFalse(cache.hasEvent(event2.getEventKey()));
        cache.addEvent(event2);
        assertTrue(cache.hasEvent(event.getEventKey()));
        assertTrue(cache.hasEvent(event2.getEventKey()));
    }

    @Test
    public void testGetEvent() throws Exception {
        final PolicyViolationContentItem item = testUtil.createPolicyViolation(new Date(), EventTestUtil.PROJECT_NAME, EventTestUtil.PROJECT_VERSION_NAME,
                EventTestUtil.COMPONENT,
                EventTestUtil.VERSION);

        final PolicyViolationContentItem item2 = testUtil.createPolicyViolation(new Date(), EventTestUtil.PROJECT_NAME2,
                EventTestUtil.PROJECT_VERSION_NAME2,
                EventTestUtil.COMPONENT2,
                EventTestUtil.VERSION2);
        final PolicyRule policyRule1 = item.getPolicyRuleList().get(0);
        final PolicyRule policyRule2 = item2.getPolicyRuleList().get(0);
        final PolicyEvent event = new PolicyEvent(NotificationCategoryEnum.POLICY_VIOLATION, item, policyRule1,
                metaService.getHref(policyRule1));
        final PolicyEvent event2 = new PolicyEvent(NotificationCategoryEnum.POLICY_VIOLATION, item2,
                policyRule2, metaService.getHref(policyRule2));
        final MapProcessorCache<PolicyEvent> cache = new MapProcessorCache<>();

        cache.addEvent(event);
        cache.addEvent(event2);
        assertEquals(2, cache.getEvents().size());
        boolean foundEvent1 = false;
        boolean foundEvent2 = false;
        for (final PolicyEvent cachedEvent : cache.getEvents()) {
            if (cachedEvent.equals(event)) {
                foundEvent1 = true;
            }

            if (cachedEvent.equals(event2)) {
                foundEvent2 = true;
            }
        }
        assertTrue(foundEvent1 && foundEvent2);
    }
}
