/**
 * Hub Common
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
import java.util.Map;

import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.dataservice.notification.model.NotificationContentItem;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.notification.processor.event.NotificationEvent;

public abstract class NotificationSubProcessor {

    private final SubProcessorCache cache;

    private final MetaService metaService;

    public NotificationSubProcessor(final SubProcessorCache cache, final MetaService metaService) {
        this.cache = cache;
        this.metaService = metaService;
    }

    public abstract void process(NotificationContentItem notification) throws HubIntegrationException;

    public abstract String generateEventKey(Map<String, Object> dataMap) throws HubIntegrationException;

    public abstract Map<String, Object> generateDataSet(Map<String, Object> inputData);

    public String hashString(final String origString) {
        String hashString;
        if (origString == null) {
            hashString = "";
        } else {
            hashString = String.valueOf(origString.hashCode());
        }
        return hashString;
    }

    public Collection<NotificationEvent> getEvents() throws HubIntegrationException {
        return cache.getEvents();
    }

    public SubProcessorCache getCache() {
        return cache;
    }

    public MetaService getMetaService() {
        return metaService;
    }
}
