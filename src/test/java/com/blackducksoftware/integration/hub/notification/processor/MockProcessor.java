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

import java.util.Collection;
import java.util.LinkedList;

import com.blackducksoftware.integration.hub.api.vulnerability.VulnerabilityRequestService;
import com.blackducksoftware.integration.hub.dataservice.notification.item.PolicyOverrideContentItem;
import com.blackducksoftware.integration.hub.dataservice.notification.item.PolicyViolationClearedContentItem;
import com.blackducksoftware.integration.hub.dataservice.notification.item.PolicyViolationContentItem;
import com.blackducksoftware.integration.hub.dataservice.notification.item.VulnerabilityContentItem;
import com.blackducksoftware.integration.hub.notification.processor.event.NotificationEvent;
import com.blackducksoftware.integration.hub.notification.processor.event.PolicyEvent;
import com.blackducksoftware.integration.hub.service.HubRequestService;

public class MockProcessor extends NotificationProcessor<Collection<NotificationEvent<?>>> {

    public MockProcessor(HubRequestService hubRequestService, VulnerabilityRequestService vulnerabilityRequestService) {
        final MapProcessorCache<PolicyEvent> policyCache = new MapProcessorCache<>();
        final VulnerabilityCache vulnerabilityCache = new VulnerabilityCache(hubRequestService, vulnerabilityRequestService);
        getCacheList().add(policyCache);
        getCacheList().add(vulnerabilityCache);
        getProcessorMap().put(PolicyViolationContentItem.class, new PolicyViolationProcessor(policyCache));
        getProcessorMap().put(PolicyViolationClearedContentItem.class, new PolicyViolationClearedProcessor(policyCache));
        getProcessorMap().put(PolicyOverrideContentItem.class, new PolicyOverrideProcessor(policyCache));
        getProcessorMap().put(VulnerabilityContentItem.class,
                new VulnerabilityProcessor(vulnerabilityCache));

    }

    @Override
    public Collection<NotificationEvent<?>> processEvents(Collection<NotificationEvent<?>> eventCollection) {
        final Collection<NotificationEvent<?>> dataList = new LinkedList<>();
        for (final NotificationEvent<?> entry : eventCollection) {
            dataList.add(entry);
        }

        return dataList;
    }
}
