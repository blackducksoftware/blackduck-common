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
package com.blackducksoftware.integration.hub.dataservice.notification;

import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.generated.view.NotificationView;
import com.blackducksoftware.integration.hub.api.generated.view.UserView;
import com.blackducksoftware.integration.hub.api.notification.NotificationService;
import com.blackducksoftware.integration.hub.api.policy.PolicyService;
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionService;
import com.blackducksoftware.integration.hub.api.view.MetaHandler;
import com.blackducksoftware.integration.hub.api.view.PolicyOverrideNotificationView;
import com.blackducksoftware.integration.hub.api.view.RuleViolationClearedNotificationView;
import com.blackducksoftware.integration.hub.api.view.RuleViolationNotificationView;
import com.blackducksoftware.integration.hub.api.view.VulnerabilityNotificationView;
import com.blackducksoftware.integration.hub.dataservice.notification.model.NotificationContentItem;
import com.blackducksoftware.integration.hub.dataservice.notification.model.PolicyNotificationFilter;
import com.blackducksoftware.integration.hub.dataservice.notification.transformer.PolicyViolationClearedTransformer;
import com.blackducksoftware.integration.hub.dataservice.notification.transformer.PolicyViolationOverrideTransformer;
import com.blackducksoftware.integration.hub.dataservice.notification.transformer.PolicyViolationTransformer;
import com.blackducksoftware.integration.hub.dataservice.notification.transformer.VulnerabilityTransformer;
import com.blackducksoftware.integration.hub.dataservice.parallel.ParallelResourceProcessor;
import com.blackducksoftware.integration.hub.dataservice.parallel.ParallelResourceProcessorResults;
import com.blackducksoftware.integration.hub.service.HubService;
import com.blackducksoftware.integration.log.IntLogger;

public class NotificationDataService {
    private final HubService hubResponseService;
    private final NotificationService notificationRequestService;
    private final ProjectVersionService projectVersionRequestService;
    private final PolicyService policyRequestService;
    private final PolicyNotificationFilter policyNotificationFilter;
    private final ParallelResourceProcessor<NotificationContentItem, NotificationView> parallelProcessor;
    private final MetaHandler metaService;

    public NotificationDataService(final IntLogger logger, final HubService hubResponseService, final NotificationService notificationRequestService, final ProjectVersionService projectVersionRequestService,
            final PolicyService policyRequestService) {
        this(logger, hubResponseService, notificationRequestService, projectVersionRequestService, policyRequestService, null);
    }

    public NotificationDataService(final IntLogger logger, final HubService hubResponseService, final NotificationService notificationRequestService, final ProjectVersionService projectVersionRequestService,
            final PolicyService policyRequestService, final PolicyNotificationFilter policyNotificationFilter) {
        this.hubResponseService = hubResponseService;
        this.notificationRequestService = notificationRequestService;
        this.projectVersionRequestService = projectVersionRequestService;
        this.policyRequestService = policyRequestService;
        this.policyNotificationFilter = policyNotificationFilter;
        this.parallelProcessor = new ParallelResourceProcessor<>(logger);
        this.metaService = new MetaHandler(logger);
        populateTransformerMap(logger);
    }

    private void populateTransformerMap(final IntLogger logger) {
        parallelProcessor.addTransform(RuleViolationNotificationView.class,
                new PolicyViolationTransformer(hubResponseService, logger, notificationRequestService, projectVersionRequestService, policyRequestService, policyNotificationFilter, metaService));
        parallelProcessor.addTransform(PolicyOverrideNotificationView.class,
                new PolicyViolationOverrideTransformer(hubResponseService, logger, notificationRequestService, projectVersionRequestService, policyRequestService, policyNotificationFilter, metaService));
        parallelProcessor.addTransform(VulnerabilityNotificationView.class, new VulnerabilityTransformer(hubResponseService, notificationRequestService, projectVersionRequestService, policyRequestService, metaService, logger));
        parallelProcessor.addTransform(RuleViolationClearedNotificationView.class,
                new PolicyViolationClearedTransformer(hubResponseService, logger, notificationRequestService, projectVersionRequestService, policyRequestService, policyNotificationFilter, metaService));
    }

    public NotificationResults getAllNotifications(final Date startDate, final Date endDate) throws IntegrationException {
        final SortedSet<NotificationContentItem> contentList = new TreeSet<>();
        final List<NotificationView> itemList = notificationRequestService.getAllNotifications(startDate, endDate);
        final ParallelResourceProcessorResults<NotificationContentItem> processorResults = parallelProcessor.process(itemList);
        contentList.addAll(processorResults.getResults());
        final NotificationResults results = new NotificationResults(contentList, processorResults.getExceptions());
        return results;
    }

    public NotificationResults getUserNotifications(final Date startDate, final Date endDate, final UserView user) throws IntegrationException {
        final SortedSet<NotificationContentItem> contentList = new TreeSet<>();
        final List<NotificationView> itemList = notificationRequestService.getUserNotifications(startDate, endDate, user);
        final ParallelResourceProcessorResults<NotificationContentItem> processorResults = parallelProcessor.process(itemList);
        contentList.addAll(processorResults.getResults());
        final NotificationResults results = new NotificationResults(contentList, processorResults.getExceptions());
        return results;
    }

}
