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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.blackducksoftware.integration.hub.api.generated.view.PolicyRuleView;
import com.blackducksoftware.integration.hub.dataservice.notification.model.PolicyViolationContentItem;
import com.blackducksoftware.integration.hub.notification.processor.event.NotificationEvent;

public class ListProcessorCacheTest {
    private final EventTestUtil testUtil = new EventTestUtil();

    @Test
    public void testEventAdd() throws Exception {
        final PolicyViolationContentItem item = testUtil.createPolicyViolation(new Date(), EventTestUtil.PROJECT_NAME, EventTestUtil.PROJECT_VERSION_NAME,
                EventTestUtil.COMPONENT, EventTestUtil.VERSION);
        final PolicyRuleView policyRule = item.getPolicyRuleList().get(0);
        final Map<String, Object> dataSet = new HashMap<>();
        dataSet.put("policyRule", policyRule);
        final NotificationEvent event = new NotificationEvent("1", NotificationCategoryEnum.POLICY_VIOLATION,
                dataSet);

        final List<NotificationEvent> eventList = new ArrayList<>();
        final ListProcessorCache cache = new ListProcessorCache();
        eventList.add(event);
        eventList.add(event);
        eventList.add(event);

        cache.addEvent(event);
        cache.addEvent(event);
        cache.addEvent(event);
        assertEquals(eventList.size(), cache.getEvents().size());
        int index = 0;
        for (final NotificationEvent cachedEvent : cache.getEvents()) {
            assertEquals(eventList.get(index), cachedEvent);
            index++;
        }
    }

    @Test
    public void testEventRemove() throws Exception {
        final PolicyViolationContentItem item = testUtil.createPolicyViolation(new Date(), EventTestUtil.PROJECT_NAME, EventTestUtil.PROJECT_VERSION_NAME,
                EventTestUtil.COMPONENT,
                EventTestUtil.VERSION);
        final PolicyRuleView policyRule = item.getPolicyRuleList().get(0);
        final Map<String, Object> dataSet = new HashMap<>();
        dataSet.put("policyRule", policyRule);
        final NotificationEvent event = new NotificationEvent("1", NotificationCategoryEnum.POLICY_VIOLATION,
                dataSet);
        final NotificationEvent removeEvent = new NotificationEvent("1", NotificationCategoryEnum.POLICY_VIOLATION,
                dataSet);
        final List<NotificationEvent> eventList = new ArrayList<>();
        final ListProcessorCache cache = new ListProcessorCache();
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
        for (final NotificationEvent cachedEvent : cache.getEvents()) {
            if (cachedEvent.equals(removeEvent)) {
                found = true;
            }
        }
        assertFalse(found);
    }
}
