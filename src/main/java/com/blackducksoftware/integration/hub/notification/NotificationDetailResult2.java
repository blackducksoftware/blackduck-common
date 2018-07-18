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
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.blackducksoftware.integration.hub.api.UriSingleResponse;
import com.blackducksoftware.integration.hub.api.core.HubResponse;
import com.blackducksoftware.integration.hub.api.enumeration.NotificationTypeGrouping;
import com.blackducksoftware.integration.hub.api.generated.enumeration.NotificationStateRequestStateType;
import com.blackducksoftware.integration.hub.api.generated.enumeration.NotificationType;
import com.blackducksoftware.integration.hub.notification.content.detail.LicenseLimitNotificationContentDetail;
import com.blackducksoftware.integration.hub.notification.content.detail.NotificationContentDetail2;
import com.blackducksoftware.integration.hub.notification.content.detail.PolicyNotificationContentDetail;
import com.blackducksoftware.integration.hub.notification.content.detail.ProjectNotificationContentDetail;
import com.blackducksoftware.integration.hub.notification.content.detail.VulnerabilityNotificationContentDetail;

public class NotificationDetailResult2 {
    private final Map<NotificationTypeGrouping, List<? extends NotificationContentDetail2>> notificationDetails;
    private final NotificationStateRequestStateType notificationState;
    private final String contentType;
    private final Date createdAt;
    private final NotificationType type;

    // @formatter:off
    public NotificationDetailResult2(
             final Map<NotificationTypeGrouping, List<? extends NotificationContentDetail2>> notificationDetails
            ,final NotificationStateRequestStateType notificationState
            ,final String contentType
            ,final Date createdAt
            ,final NotificationType type
            ) {
        this.notificationDetails = notificationDetails;
        this.notificationState = notificationState;
        this.contentType = contentType;
        this.createdAt = createdAt;
        this.type = type;
    }
    // @formatter:on

    public List<NotificationContentDetail2> getBomEditDetails() {
        // TODO add bom edit content detail
        return (List<NotificationContentDetail2>) notificationDetails.get(NotificationTypeGrouping.BOM_EDIT);
    }

    public List<LicenseLimitNotificationContentDetail> getLicenseDetails() {
        return (List<LicenseLimitNotificationContentDetail>) notificationDetails.get(NotificationTypeGrouping.LICENSE);
    }

    public List<PolicyNotificationContentDetail> getPolicyDetails() {
        return (List<PolicyNotificationContentDetail>) notificationDetails.get(NotificationTypeGrouping.POLICY);
    }

    public List<VulnerabilityNotificationContentDetail> getVulnerabilityDetails() {
        return (List<VulnerabilityNotificationContentDetail>) notificationDetails.get(NotificationTypeGrouping.VULNERABILITY);
    }

    public List<ProjectNotificationContentDetail> getProjectDetails() {
        return Stream.concat(getPolicyDetails().stream(), getVulnerabilityDetails().stream()).collect(Collectors.toList());
    }

    public Optional<NotificationStateRequestStateType> getNotificationState() {
        return Optional.ofNullable(notificationState);
    }

    public String getContentType() {
        return contentType;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public NotificationType getType() {
        return type;
    }

    public List<UriSingleResponse<? extends HubResponse>> getAllLinks() {
        final List<UriSingleResponse<? extends HubResponse>> uriResponses = new ArrayList<>();
        notificationDetails.values().forEach(detailList -> {
            detailList.forEach(detail -> {
                uriResponses.addAll(detail.getPresentLinks());
            });
        });
        return uriResponses;
    }

}
