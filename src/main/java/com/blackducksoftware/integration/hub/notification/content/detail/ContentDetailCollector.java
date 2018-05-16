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
package com.blackducksoftware.integration.hub.notification.content.detail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.blackducksoftware.integration.hub.api.view.CommonNotificationState;
import com.blackducksoftware.integration.hub.notification.NotificationViewResult;
import com.blackducksoftware.integration.hub.notification.content.LicenseLimitNotificationContent;
import com.blackducksoftware.integration.hub.notification.content.NotificationContent;
import com.blackducksoftware.integration.hub.notification.content.PolicyOverrideNotificationContent;
import com.blackducksoftware.integration.hub.notification.content.RuleViolationClearedNotificationContent;
import com.blackducksoftware.integration.hub.notification.content.RuleViolationNotificationContent;
import com.blackducksoftware.integration.hub.notification.content.VulnerabilityNotificationContent;

public class ContentDetailCollector {
    Map<Class<? extends NotificationContent>, NotificationDetailFactory> factoryMap;

    public ContentDetailCollector() {
        factoryMap = new HashMap<>();
        factoryMap.put(RuleViolationNotificationContent.class, new RuleViolationDetailFactory());
        factoryMap.put(RuleViolationClearedNotificationContent.class, new RuleViolationClearedDetailFactory());
        factoryMap.put(PolicyOverrideNotificationContent.class, new PolicyOverrideDetailFactory());
        factoryMap.put(VulnerabilityNotificationContent.class, new VulnerabilityDetailFactory());
        factoryMap.put(LicenseLimitNotificationContent.class, new LicenseLimitDetailFactory());
    }

    public List<NotificationViewResult> collect(final List<CommonNotificationState> commonNotificationStates) {
        final List<NotificationViewResult> resultList = new ArrayList<>();
        commonNotificationStates.forEach(commonNotificationState -> {
            collectDetails(resultList, commonNotificationState);
        });

        return resultList;
    }

    private void collectDetails(final List<NotificationViewResult> resultList, final CommonNotificationState commonNotificationState) {
        final NotificationContent content = commonNotificationState.getContent();
        final Class<?> key = content.getClass();
        if (factoryMap.containsKey(key)) {
            final NotificationDetailFactory factory = factoryMap.get(key);
            final List<NotificationContentDetail> contentDetailList = factory.createDetails(content);
            final NotificationViewResult notificationViewResult = new NotificationViewResult(commonNotificationState, contentDetailList);
            resultList.add(notificationViewResult);
        }
    }
}
