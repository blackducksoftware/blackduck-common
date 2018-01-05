/**
 * hub-common
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.notification.processor.event.NotificationEvent;

public class MapProcessorCache implements SubProcessorCache {
    private final Map<String, NotificationEvent> eventMap = new LinkedHashMap<>(500);

    @Override
    public boolean hasEvent(String eventKey) {
        return eventMap.containsKey(eventKey);
    }

    @Override
    public void addEvent(final NotificationEvent event) {
        final String key = event.getEventKey();
        if (!eventMap.containsKey(key)) {
            eventMap.put(key, event);
        } else {
            final NotificationEvent storedEvent = eventMap.get(key);
            final Map<String, Object> storedEventDataMap = storedEvent.getDataSet();
            final Map<String, Object> eventDataMap = event.getDataSet();
            storedEventDataMap.putAll(eventDataMap);
        }
    }

    public void removeEvent(final String eventKey) {
        if (eventMap.containsKey(eventKey)) {
            eventMap.remove(eventKey);
        }
    }

    @Override
    public void removeEvent(final NotificationEvent event) {
        final String key = event.getEventKey();
        removeEvent(key);
    }

    public NotificationEvent getEvent(final String eventKey) {
        return eventMap.get(eventKey);
    }

    @Override
    public Collection<NotificationEvent> getEvents() throws HubIntegrationException {
        return eventMap.values();
    }

    public Map<String, NotificationEvent> getEventMap() {
        return eventMap;
    }
}
