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

import com.blackducksoftware.integration.hub.api.policy.PolicyRule;
import com.blackducksoftware.integration.hub.dataservice.notification.item.NotificationContentItem;
import com.blackducksoftware.integration.hub.dataservice.notification.item.PolicyViolationContentItem;
import com.blackducksoftware.integration.hub.notification.processor.event.PolicyEvent;
import com.blackducksoftware.integration.hub.notification.processor.event.ProcessingAction;

public class PolicyViolationProcessor extends NotificationSubProcessor<PolicyEvent> {

    public PolicyViolationProcessor(final MapProcessorCache<PolicyEvent> cache) {
        super(cache);
    }

    @Override
    public void process(final NotificationContentItem notification) {
        if (notification instanceof PolicyViolationContentItem) {
            final PolicyViolationContentItem policyViolationContentItem = (PolicyViolationContentItem) notification;
            for (final PolicyRule rule : policyViolationContentItem.getPolicyRuleList()) {
                final PolicyEvent event = new PolicyEvent(ProcessingAction.ADD, NotificationCategoryEnum.POLICY_VIOLATION, policyViolationContentItem,
                        rule);
                getCache().addEvent(event);

            }
        }
    }
}
