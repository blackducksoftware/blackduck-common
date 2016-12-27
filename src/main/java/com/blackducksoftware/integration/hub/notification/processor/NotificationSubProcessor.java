/**
 * Hub Common
 *
 * Copyright (C) 2016 Black Duck Software, Inc..
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

import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.dataservice.notification.item.NotificationContentItem;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.notification.processor.event.NotificationEvent;

public abstract class NotificationSubProcessor<T extends NotificationEvent<?>> {

    private final MapProcessorCache<T> cache;

    private final MetaService metaService;

    public NotificationSubProcessor(final MapProcessorCache<T> cache, final MetaService metaService) {
        this.cache = cache;
        this.metaService = metaService;
    }

    public Collection<T> getEvents() throws HubIntegrationException {
        return cache.getEvents();
    }

    public MapProcessorCache<T> getCache() {
        return cache;
    }

    public MetaService getMetaService() {
        return metaService;
    }

    public abstract void process(NotificationContentItem notification) throws HubIntegrationException;
}
