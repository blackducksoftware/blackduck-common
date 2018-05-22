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
package com.blackducksoftware.integration.hub.notification;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.blackducksoftware.integration.hub.api.UriSingleResponse;
import com.blackducksoftware.integration.hub.api.core.HubResponse;
import com.blackducksoftware.integration.hub.service.bucket.HubBucket;

public class NotificationDetailResults extends NotificationResults<NotificationDetailResult> {
    private final List<NotificationDetailResult> resultList;
    private final HubBucket hubBucket;

    public NotificationDetailResults(final List<NotificationDetailResult> resultList, final Optional<Date> latestNotificationCreatedAtDate, final Optional<String> latestNotificationCreatedAtString, final HubBucket hubBucket) {
        super(latestNotificationCreatedAtDate, latestNotificationCreatedAtString);
        this.resultList = resultList;
        this.hubBucket = hubBucket;
    }

    public List<UriSingleResponse<? extends HubResponse>> getAllLinks() {
        final List<UriSingleResponse<? extends HubResponse>> uriResponses = new ArrayList<>();
        resultList.forEach(result -> {
            result.getNotificationContentDetails().forEach(contentDetail -> {
                uriResponses.addAll(contentDetail.getPresentLinks());
            });
        });

        return uriResponses;
    }

    @Override
    public List<NotificationDetailResult> getResults() {
        return resultList;
    }

    public HubBucket getHubBucket() {
        return hubBucket;
    }

    @Override
    public boolean isEmpty() {
        return resultList == null || resultList.isEmpty();
    }

}
