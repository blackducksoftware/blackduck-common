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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.blackducksoftware.integration.hub.dataservice.notification.item.PolicyViolationContentItem;
import com.blackducksoftware.integration.hub.notification.processor.event.PolicyEvent;

public class ListProcessorCacheTest {

    private final EventTestUtil testUtil = new EventTestUtil();

    @Test
    public void testEventAdd() throws Exception {
        final PolicyViolationContentItem item = testUtil.createPolicyViolation(new Date(), EventTestUtil.PROJECT_NAME, EventTestUtil.PROJECT_VERSION_NAME,
                EventTestUtil.COMPONENT,
                EventTestUtil.VERSION);
        final PolicyEvent event = new PolicyEvent(ProcessingActionEnum.ADD, NotificationCategoryEnum.POLICY_VIOLATION, item, item.getPolicyRuleList().get(0));

        final List<PolicyEvent> eventList = new ArrayList<>();
        final ListProcessorCache<PolicyEvent> cache = new ListProcessorCache<>();
        eventList.add(event);
        eventList.add(event);
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
        final PolicyEvent event = new PolicyEvent(ProcessingActionEnum.ADD, NotificationCategoryEnum.POLICY_VIOLATION, item, item.getPolicyRuleList().get(0));
        final PolicyEvent removeEvent = new PolicyEvent(ProcessingActionEnum.ADD, NotificationCategoryEnum.POLICY_VIOLATION, item,
                item.getPolicyRuleList().get(0));
        final List<PolicyEvent> eventList = new ArrayList<>();
        final ListProcessorCache<PolicyEvent> cache = new ListProcessorCache<>();
        eventList.add(event);
        eventList.add(event);
        eventList.add(removeEvent);
        eventList.add(event);

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
}
