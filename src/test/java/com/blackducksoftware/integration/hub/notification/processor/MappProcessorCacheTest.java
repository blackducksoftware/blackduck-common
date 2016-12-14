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
package com.blackducksoftware.integration.hub.notification.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.blackducksoftware.integration.hub.dataservice.notification.item.PolicyViolationContentItem;
import com.blackducksoftware.integration.hub.notification.processor.event.PolicyEvent;
import com.blackducksoftware.integration.hub.notification.processor.event.ProcessingAction;

public class MappProcessorCacheTest {

    private final EventTestUtil testUtil = new EventTestUtil();

    @Test
    public void testEventAdd() throws Exception {
        final PolicyViolationContentItem item = testUtil.createPolicyViolation(new Date(), EventTestUtil.PROJECT_NAME, EventTestUtil.PROJECT_VERSION_NAME,
                EventTestUtil.COMPONENT,
                EventTestUtil.VERSION);
        final PolicyEvent event = new PolicyEvent(ProcessingAction.ADD, NotificationCategoryEnum.POLICY_VIOLATION, item, item.getPolicyRuleList().get(0));

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
        final PolicyEvent event = new PolicyEvent(ProcessingAction.ADD, NotificationCategoryEnum.POLICY_VIOLATION, item, item.getPolicyRuleList().get(0));
        final PolicyEvent removeEvent = new PolicyEvent(ProcessingAction.REMOVE, NotificationCategoryEnum.POLICY_VIOLATION, removeItem,
                removeItem.getPolicyRuleList().get(0));
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
        final PolicyEvent event = new PolicyEvent(ProcessingAction.ADD, NotificationCategoryEnum.POLICY_VIOLATION, item, item.getPolicyRuleList().get(0));
        final PolicyEvent removeEvent = new PolicyEvent(ProcessingAction.REMOVE, NotificationCategoryEnum.POLICY_VIOLATION, removeItem,
                removeItem.getPolicyRuleList().get(0));
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
        final PolicyEvent event = new PolicyEvent(ProcessingAction.ADD, NotificationCategoryEnum.POLICY_VIOLATION, item, item.getPolicyRuleList().get(0));
        final PolicyEvent event2 = new PolicyEvent(ProcessingAction.REMOVE, NotificationCategoryEnum.POLICY_VIOLATION, item2,
                item2.getPolicyRuleList().get(0));
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
        final PolicyEvent event = new PolicyEvent(ProcessingAction.ADD, NotificationCategoryEnum.POLICY_VIOLATION, item, item.getPolicyRuleList().get(0));
        final PolicyEvent event2 = new PolicyEvent(ProcessingAction.REMOVE, NotificationCategoryEnum.POLICY_VIOLATION, item2,
                item2.getPolicyRuleList().get(0));
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
