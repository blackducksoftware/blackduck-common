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
package com.blackducksoftware.integration.hub.notification.processor.event;

import java.util.Map;

import com.blackducksoftware.integration.hub.notification.processor.NotificationCategoryEnum;

public class NotificationEvent {
    public final static String DATA_SET_KEY_NOTIFICATION_CONTENT = "notificationContentItem";

    private NotificationCategoryEnum categoryType;
    private final Map<String, Object> dataSet;
    private final String eventKey;

    public NotificationEvent(final String eventKey, final NotificationCategoryEnum categoryType, final Map<String, Object> dataSet) {
        this.eventKey = eventKey;
        this.categoryType = categoryType;
        this.dataSet = dataSet;
    }

    public String hashString(final String origString) {
        String hashString;
        if (origString == null) {
            hashString = "";
        } else {
            hashString = String.valueOf(origString.hashCode());
        }
        return hashString;
    }

    public NotificationCategoryEnum getCategoryType() {
        return categoryType;
    }

    public void setCategoryType(final NotificationCategoryEnum categoryType) {
        this.categoryType = categoryType;
    }

    public Map<String, Object> getDataSet() {
        return dataSet;
    }

    public String getEventKey() {
        return eventKey;
    }

    public boolean isPolicyEvent() {
        switch (getCategoryType()) {
        case POLICY_VIOLATION:
        case POLICY_VIOLATION_CLEARED:
        case POLICY_VIOLATION_OVERRIDE:
            return true;

        case HIGH_VULNERABILITY:
        case MEDIUM_VULNERABILITY:
        case LOW_VULNERABILITY:
        case VULNERABILITY:
            return false;

        default:
            throw new IllegalArgumentException("Unrecognized notification type: " + getCategoryType());
        }
    }
}
