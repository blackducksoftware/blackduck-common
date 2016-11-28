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
package com.blackducksoftware.integration.hub.dataservices.notification;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import com.blackducksoftware.integration.hub.api.HubRestService;
import com.blackducksoftware.integration.hub.api.component.version.ComponentVersionRestService;
import com.blackducksoftware.integration.hub.api.notification.NotificationItem;
import com.blackducksoftware.integration.hub.api.notification.NotificationRestService;
import com.blackducksoftware.integration.hub.api.notification.PolicyOverrideNotificationItem;
import com.blackducksoftware.integration.hub.api.notification.RuleViolationClearedNotificationItem;
import com.blackducksoftware.integration.hub.api.notification.RuleViolationNotificationItem;
import com.blackducksoftware.integration.hub.api.notification.VulnerabilityNotificationItem;
import com.blackducksoftware.integration.hub.api.policy.PolicyRestService;
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionRestService;
import com.blackducksoftware.integration.hub.api.user.UserItem;
import com.blackducksoftware.integration.hub.api.version.VersionBomPolicyRestService;
import com.blackducksoftware.integration.hub.dataservices.notification.items.NotificationContentItem;
import com.blackducksoftware.integration.hub.dataservices.notification.items.PolicyNotificationFilter;
import com.blackducksoftware.integration.hub.dataservices.notification.transformer.PolicyViolationClearedTransformer;
import com.blackducksoftware.integration.hub.dataservices.notification.transformer.PolicyViolationOverrideTransformer;
import com.blackducksoftware.integration.hub.dataservices.notification.transformer.PolicyViolationTransformer;
import com.blackducksoftware.integration.hub.dataservices.notification.transformer.VulnerabilityTransformer;
import com.blackducksoftware.integration.hub.dataservices.parallel.ParallelResourceProcessor;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.UnexpectedHubResponseException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.log.IntLogger;

public class NotificationDataService extends HubRestService<String> {
    private final NotificationRestService notificationRestService;

    private final ProjectVersionRestService projectVersionRestService;

    private final PolicyRestService policyRestService;

    private final VersionBomPolicyRestService versionBomPolicyRestService;

    private final ComponentVersionRestService componentVersionRestService;

    private final PolicyNotificationFilter policyNotificationFilter;

    private final ParallelResourceProcessor<NotificationContentItem, NotificationItem> parallelProcessor;

    public NotificationDataService(IntLogger logger, RestConnection restConnection, NotificationRestService notificationRestService,
            ProjectVersionRestService projectVersionRestService, PolicyRestService policyRestService, VersionBomPolicyRestService versionBomPolicyRestService,
            ComponentVersionRestService componentVersionRestService) {
        this(logger, restConnection, notificationRestService, projectVersionRestService, policyRestService, versionBomPolicyRestService,
                componentVersionRestService, null);
    }

    public NotificationDataService(IntLogger logger, RestConnection restConnection, NotificationRestService notificationRestService,
            ProjectVersionRestService projectVersionRestService, PolicyRestService policyRestService, VersionBomPolicyRestService versionBomPolicyRestService,
            ComponentVersionRestService componentVersionRestService, PolicyNotificationFilter policyNotificationFilter) {
        super(restConnection, String.class);
        this.notificationRestService = notificationRestService;
        this.projectVersionRestService = projectVersionRestService;
        this.policyRestService = policyRestService;
        this.versionBomPolicyRestService = versionBomPolicyRestService;
        this.componentVersionRestService = componentVersionRestService;
        this.policyNotificationFilter = policyNotificationFilter;
        this.parallelProcessor = new ParallelResourceProcessor<>(logger);

        populateTransformerMap();
    }

    private void populateTransformerMap() {
        parallelProcessor.addTransform(RuleViolationNotificationItem.class,
                new PolicyViolationTransformer(notificationRestService, projectVersionRestService, policyRestService,
                        versionBomPolicyRestService, componentVersionRestService, policyNotificationFilter));
        parallelProcessor.addTransform(PolicyOverrideNotificationItem.class,
                new PolicyViolationOverrideTransformer(notificationRestService, projectVersionRestService, policyRestService,
                        versionBomPolicyRestService, componentVersionRestService, policyNotificationFilter));
        parallelProcessor.addTransform(VulnerabilityNotificationItem.class,
                new VulnerabilityTransformer(notificationRestService, projectVersionRestService, policyRestService,
                        versionBomPolicyRestService, componentVersionRestService));
        parallelProcessor.addTransform(RuleViolationClearedNotificationItem.class,
                new PolicyViolationClearedTransformer(notificationRestService, projectVersionRestService, policyRestService,
                        versionBomPolicyRestService, componentVersionRestService, policyNotificationFilter));
    }

    public SortedSet<NotificationContentItem> getAllNotifications(final Date startDate, final Date endDate)
            throws IOException, URISyntaxException, BDRestException {
        final SortedSet<NotificationContentItem> contentList = new TreeSet<>();
        final List<NotificationItem> itemList = notificationRestService.getAllNotifications(startDate, endDate);
        contentList.addAll(parallelProcessor.process(itemList));
        return contentList;
    }

    public SortedSet<NotificationContentItem> getUserNotifications(final Date startDate, final Date endDate, UserItem user)
            throws IOException, URISyntaxException, BDRestException, UnexpectedHubResponseException {
        final SortedSet<NotificationContentItem> contentList = new TreeSet<>();
        final List<NotificationItem> itemList = notificationRestService.getUserNotifications(startDate, endDate, user);
        contentList.addAll(parallelProcessor.process(itemList));
        return contentList;
    }

}
