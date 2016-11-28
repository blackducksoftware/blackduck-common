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
package com.blackducksoftware.integration.hub.dataservices.notification.transformer;

import java.util.List;

import com.blackducksoftware.integration.hub.api.component.version.ComponentVersionRequestService;
import com.blackducksoftware.integration.hub.api.notification.NotificationItem;
import com.blackducksoftware.integration.hub.api.notification.NotificationRequestService;
import com.blackducksoftware.integration.hub.api.policy.PolicyRequestService;
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionRequestService;
import com.blackducksoftware.integration.hub.api.version.VersionBomPolicyRequestService;
import com.blackducksoftware.integration.hub.dataservices.ItemTransform;
import com.blackducksoftware.integration.hub.dataservices.notification.items.NotificationContentItem;
import com.blackducksoftware.integration.hub.exception.HubItemTransformException;

public abstract class AbstractNotificationTransformer
        implements ItemTransform<List<NotificationContentItem>, NotificationItem> {
    private final NotificationRequestService notificationService;

    private final ProjectVersionRequestService projectVersionService;

    private final PolicyRequestService policyService;

    private final VersionBomPolicyRequestService bomVersionPolicyService;

    private final ComponentVersionRequestService componentVersionService;

    public AbstractNotificationTransformer(final NotificationRequestService notificationService,
            final ProjectVersionRequestService projectVersionService, final PolicyRequestService policyService,
            final VersionBomPolicyRequestService bomVersionPolicyService,
            final ComponentVersionRequestService componentVersionService) {
        this.notificationService = notificationService;
        this.projectVersionService = projectVersionService;
        this.policyService = policyService;
        this.bomVersionPolicyService = bomVersionPolicyService;
        this.componentVersionService = componentVersionService;
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

    public ComponentVersionRequestService getComponentVersionService() {
        return componentVersionService;
    }

    @Override
    public abstract List<NotificationContentItem> transform(NotificationItem item) throws HubItemTransformException;

}
