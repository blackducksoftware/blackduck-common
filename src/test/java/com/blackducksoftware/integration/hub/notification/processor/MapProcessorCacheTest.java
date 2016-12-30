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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.blackducksoftware.integration.hub.notification.processor.event.NotificationEvent;

public class MapProcessorCacheTest {

    @Test
    public void testEventAdd() throws Exception {
        final Map<String, Object> dataSet = Collections.emptyMap();
        final NotificationEvent event = new NotificationEvent("1", NotificationCategoryEnum.POLICY_VIOLATION,
                dataSet);

        final List<NotificationEvent> eventList = new ArrayList<>();
        final MapProcessorCache<NotificationEvent> cache = new MapProcessorCache<>();
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
        final Map<String, Object> dataSet = Collections.emptyMap();
        final NotificationEvent event = new NotificationEvent("1", NotificationCategoryEnum.POLICY_VIOLATION,
                dataSet);
        final NotificationEvent removeEvent = new NotificationEvent("2", NotificationCategoryEnum.POLICY_VIOLATION,
                dataSet);
        final List<NotificationEvent> eventList = new ArrayList<>();
        final MapProcessorCache<NotificationEvent> cache = new MapProcessorCache<>();
        eventList.add(event);
        eventList.add(removeEvent);

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

    @Test
    public void testRemoveViaKey() throws Exception {
        final Map<String, Object> dataSet = Collections.emptyMap();
        final NotificationEvent event = new NotificationEvent("1", NotificationCategoryEnum.POLICY_VIOLATION,
                dataSet);
        final NotificationEvent removeEvent = new NotificationEvent("2", NotificationCategoryEnum.POLICY_VIOLATION,
                dataSet);
        final List<NotificationEvent> eventList = new ArrayList<>();
        final MapProcessorCache<NotificationEvent> cache = new MapProcessorCache<>();
        eventList.add(event);
        eventList.add(removeEvent);

        cache.addEvent(event);
        cache.addEvent(event);
        cache.addEvent(removeEvent);
        cache.addEvent(event);
        cache.removeEvent(removeEvent.getEventKey());
        assertEquals(eventList.size() - 1, cache.getEvents().size());
        boolean found = false;
        for (final NotificationEvent cachedEvent : cache.getEvents()) {
            if (cachedEvent.equals(removeEvent)) {
                found = true;
            }
        }
        assertFalse(found);
    }

    @Test
    public void testHasEvent() throws Exception {
        final Map<String, Object> dataSet = Collections.emptyMap();
        final NotificationEvent event = new NotificationEvent("1", NotificationCategoryEnum.POLICY_VIOLATION,
                dataSet);
        final NotificationEvent event2 = new NotificationEvent("2", NotificationCategoryEnum.POLICY_VIOLATION, dataSet);
        final MapProcessorCache<NotificationEvent> cache = new MapProcessorCache<>();

        cache.addEvent(event);

        assertTrue(cache.hasEvent(event.getEventKey()));
        assertFalse(cache.hasEvent(event2.getEventKey()));
        cache.addEvent(event2);
        assertTrue(cache.hasEvent(event.getEventKey()));
        assertTrue(cache.hasEvent(event2.getEventKey()));
    }

    @Test
    public void testGetEvent() throws Exception {
        final Map<String, Object> dataSet = Collections.emptyMap();
        final NotificationEvent event = new NotificationEvent("1", NotificationCategoryEnum.POLICY_VIOLATION,
                dataSet);
        final NotificationEvent event2 = new NotificationEvent("2", NotificationCategoryEnum.POLICY_VIOLATION, dataSet);
        final MapProcessorCache<NotificationEvent> cache = new MapProcessorCache<>();

        cache.addEvent(event);
        cache.addEvent(event2);
        assertEquals(2, cache.getEvents().size());
        boolean foundEvent1 = false;
        boolean foundEvent2 = false;
        for (final NotificationEvent cachedEvent : cache.getEvents()) {
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
