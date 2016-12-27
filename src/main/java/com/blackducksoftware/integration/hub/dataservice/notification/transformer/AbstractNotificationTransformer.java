/**
 * Hub Common
 *
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
 */
package com.blackducksoftware.integration.hub.dataservice.notification.transformer;

import java.util.List;

import com.blackducksoftware.integration.hub.api.notification.NotificationItem;
import com.blackducksoftware.integration.hub.api.notification.NotificationRequestService;
import com.blackducksoftware.integration.hub.api.policy.PolicyRequestService;
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionRequestService;
import com.blackducksoftware.integration.hub.api.version.VersionBomPolicyRequestService;
import com.blackducksoftware.integration.hub.dataservice.ItemTransform;
import com.blackducksoftware.integration.hub.dataservice.notification.item.NotificationContentItem;
import com.blackducksoftware.integration.hub.exception.HubItemTransformException;
import com.blackducksoftware.integration.hub.service.HubRequestService;

public abstract class AbstractNotificationTransformer
        implements ItemTransform<List<NotificationContentItem>, NotificationItem> {
    private final NotificationRequestService notificationService;

    private final ProjectVersionRequestService projectVersionService;

    private final PolicyRequestService policyService;

    private final VersionBomPolicyRequestService bomVersionPolicyService;

    private final HubRequestService hubRequestService;

    public AbstractNotificationTransformer(final NotificationRequestService notificationService,
            final ProjectVersionRequestService projectVersionService, final PolicyRequestService policyService,
            final VersionBomPolicyRequestService bomVersionPolicyService, HubRequestService hubRequestService) {
        this.notificationService = notificationService;
        this.projectVersionService = projectVersionService;
        this.policyService = policyService;
        this.bomVersionPolicyService = bomVersionPolicyService;
        this.hubRequestService = hubRequestService;
    }

    public NotificationRequestService getNotificationService() {
        return notificationService;
    }

    public ProjectVersionRequestService getProjectVersionService() {
        return projectVersionService;
    }

    public PolicyRequestService getPolicyService() {
        return policyService;
    }

    public VersionBomPolicyRequestService getBomVersionPolicyService() {
        return bomVersionPolicyService;
    }

    public HubRequestService getHubRequestService() {
        return hubRequestService;
    }

    @Override
    public abstract List<NotificationContentItem> transform(NotificationItem item) throws HubItemTransformException;

}
