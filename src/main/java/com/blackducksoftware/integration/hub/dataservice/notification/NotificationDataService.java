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
package com.blackducksoftware.integration.hub.dataservice.notification;

import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.api.notification.NotificationItem;
import com.blackducksoftware.integration.hub.api.notification.NotificationRequestService;
import com.blackducksoftware.integration.hub.api.notification.PolicyOverrideNotificationItem;
import com.blackducksoftware.integration.hub.api.notification.RuleViolationClearedNotificationItem;
import com.blackducksoftware.integration.hub.api.notification.RuleViolationNotificationItem;
import com.blackducksoftware.integration.hub.api.notification.VulnerabilityNotificationItem;
import com.blackducksoftware.integration.hub.api.policy.PolicyRequestService;
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionRequestService;
import com.blackducksoftware.integration.hub.api.user.UserItem;
import com.blackducksoftware.integration.hub.api.version.VersionBomPolicyRequestService;
import com.blackducksoftware.integration.hub.dataservice.notification.model.NotificationContentItem;
import com.blackducksoftware.integration.hub.dataservice.notification.model.PolicyNotificationFilter;
import com.blackducksoftware.integration.hub.dataservice.notification.transformer.PolicyViolationClearedTransformer;
import com.blackducksoftware.integration.hub.dataservice.notification.transformer.PolicyViolationOverrideTransformer;
import com.blackducksoftware.integration.hub.dataservice.notification.transformer.PolicyViolationTransformer;
import com.blackducksoftware.integration.hub.dataservice.notification.transformer.VulnerabilityTransformer;
import com.blackducksoftware.integration.hub.dataservice.parallel.ParallelResourceProcessor;
import com.blackducksoftware.integration.hub.dataservice.parallel.ParallelResourceProcessorResults;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubRequestService;
import com.blackducksoftware.integration.log.IntLogger;

public class NotificationDataService extends HubRequestService {
    private final NotificationRequestService notificationRequestService;

    private final ProjectVersionRequestService projectVersionRequestService;

    private final PolicyRequestService policyRequestService;

    private final VersionBomPolicyRequestService versionBomPolicyRequestService;

    private final HubRequestService hubRequestService;

    private final PolicyNotificationFilter policyNotificationFilter;

    private final ParallelResourceProcessor<NotificationContentItem, NotificationItem> parallelProcessor;

    private final MetaService metaService;

    public NotificationDataService(final IntLogger logger, final RestConnection restConnection, final NotificationRequestService notificationRequestService,
            final ProjectVersionRequestService projectVersionRequestService, final PolicyRequestService policyRequestService,
            final VersionBomPolicyRequestService versionBomPolicyRequestService,
            final HubRequestService hubRequestService, final MetaService metaService) {
        this(logger, restConnection, notificationRequestService, projectVersionRequestService, policyRequestService, versionBomPolicyRequestService,
                hubRequestService, null, metaService);
    }

    public NotificationDataService(final IntLogger logger, final RestConnection restConnection, final NotificationRequestService notificationRequestService,
            final ProjectVersionRequestService projectVersionRequestService, final PolicyRequestService policyRequestService,
            final VersionBomPolicyRequestService versionBomPolicyRequestService,
            final HubRequestService hubRequestService, final PolicyNotificationFilter policyNotificationFilter, final MetaService metaService) {
        super(restConnection);
        this.notificationRequestService = notificationRequestService;
        this.projectVersionRequestService = projectVersionRequestService;
        this.policyRequestService = policyRequestService;
        this.versionBomPolicyRequestService = versionBomPolicyRequestService;
        this.hubRequestService = hubRequestService;
        this.policyNotificationFilter = policyNotificationFilter;
        this.parallelProcessor = new ParallelResourceProcessor<>(logger);
        this.metaService = metaService;
        populateTransformerMap(logger);
    }

    private void populateTransformerMap(final IntLogger logger) {
        parallelProcessor.addTransform(RuleViolationNotificationItem.class,
                new PolicyViolationTransformer(logger, notificationRequestService, projectVersionRequestService, policyRequestService,
                        versionBomPolicyRequestService, hubRequestService, policyNotificationFilter, metaService));
        parallelProcessor.addTransform(PolicyOverrideNotificationItem.class,
                new PolicyViolationOverrideTransformer(logger, notificationRequestService, projectVersionRequestService, policyRequestService,
                        versionBomPolicyRequestService, hubRequestService, policyNotificationFilter, metaService));
        parallelProcessor.addTransform(VulnerabilityNotificationItem.class,
                new VulnerabilityTransformer(notificationRequestService, projectVersionRequestService, policyRequestService,
                        versionBomPolicyRequestService, hubRequestService, metaService,
                        logger));
        parallelProcessor.addTransform(RuleViolationClearedNotificationItem.class,
                new PolicyViolationClearedTransformer(logger, notificationRequestService, projectVersionRequestService, policyRequestService,
                        versionBomPolicyRequestService, hubRequestService, policyNotificationFilter, metaService));
    }

    public NotificationResults getAllNotifications(final Date startDate, final Date endDate) throws HubIntegrationException {
        final SortedSet<NotificationContentItem> contentList = new TreeSet<>();
        final List<NotificationItem> itemList = notificationRequestService.getAllNotifications(startDate, endDate);
        final ParallelResourceProcessorResults<NotificationContentItem> processorResults = parallelProcessor.process(itemList);
        contentList.addAll(processorResults.getResults());
        final NotificationResults results = new NotificationResults(contentList, processorResults.getExceptions());
        return results;
    }

    public NotificationResults getUserNotifications(final Date startDate, final Date endDate, final UserItem user)
            throws HubIntegrationException {
        final SortedSet<NotificationContentItem> contentList = new TreeSet<>();
        final List<NotificationItem> itemList = notificationRequestService.getUserNotifications(startDate, endDate, user);
        final ParallelResourceProcessorResults<NotificationContentItem> processorResults = parallelProcessor.process(itemList);
        contentList.addAll(processorResults.getResults());
        final NotificationResults results = new NotificationResults(contentList, processorResults.getExceptions());
        return results;
    }

}
