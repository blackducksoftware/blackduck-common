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
package com.blackducksoftware.integration.hub.notification.processor.event;

import java.util.Set;

import com.blackducksoftware.integration.hub.dataservice.notification.item.NotificationContentItem;
import com.blackducksoftware.integration.hub.notification.processor.ItemEntry;
import com.blackducksoftware.integration.hub.notification.processor.NotificationCategoryEnum;

public abstract class NotificationEvent<T extends NotificationContentItem> {
    private NotificationCategoryEnum categoryType;

    private final T notificationContent;

    private Set<ItemEntry> dataSet;

    private String eventKey;

    public NotificationEvent(final NotificationCategoryEnum categoryType, T notificationContent) {
        this.categoryType = categoryType;
        this.notificationContent = notificationContent;
    }

    public void init() {
        dataSet = generateDataSet();
        eventKey = generateEventKey();
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

    public abstract Set<ItemEntry> generateDataSet();

    public abstract String generateEventKey();

    public abstract int countCategoryItems();

    public NotificationCategoryEnum getCategoryType() {
        return categoryType;
    }

    public void setCategoryType(NotificationCategoryEnum categoryType) {
        this.categoryType = categoryType;
    }

    public T getNotificationContent() {
        return notificationContent;
    }

    public Set<ItemEntry> getDataSet() {
        return dataSet;
    }

    public String getEventKey() {
        return eventKey;
    }
}
