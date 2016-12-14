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

import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackducksoftware.integration.hub.api.policy.PolicyRule;
import com.blackducksoftware.integration.hub.dataservice.notification.item.NotificationContentItem;
import com.blackducksoftware.integration.hub.dataservice.notification.item.PolicyViolationClearedContentItem;
import com.blackducksoftware.integration.hub.notification.processor.event.PolicyEvent;
import com.blackducksoftware.integration.hub.notification.processor.event.ProcessingAction;

public class PolicyViolationClearedProcessor extends NotificationSubProcessor<PolicyEvent> {
    private final Logger logger = LoggerFactory.getLogger(PolicyViolationProcessor.class);

    public PolicyViolationClearedProcessor(final MapProcessorCache<PolicyEvent> cache) {
        super(cache);
    }

    @Override
    public void process(final NotificationContentItem notification) {
        if (notification instanceof PolicyViolationClearedContentItem) {
            final PolicyViolationClearedContentItem policyViolationCleared = (PolicyViolationClearedContentItem) notification;
            for (final PolicyRule rule : policyViolationCleared.getPolicyRuleList()) {
                try {
                    final PolicyEvent event = new PolicyEvent(ProcessingAction.REMOVE, NotificationCategoryEnum.POLICY_VIOLATION, policyViolationCleared, rule);
                    if (getCache().hasEvent(event.getEventKey())) {
                        getCache().removeEvent(event);
                    } else {
                        event.setAction(ProcessingAction.ADD);
                        event.setCategoryType(NotificationCategoryEnum.POLICY_VIOLATION_CLEARED);
                        getCache().addEvent(event);
                    }
                } catch (final URISyntaxException e) {
                    logger.error("Error processing policy violation cleared item {} ", policyViolationCleared, e);
                }
            }
        }
    }
}
