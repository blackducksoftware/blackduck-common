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

import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.notification.NotificationService;
import com.blackducksoftware.integration.hub.api.policy.PolicyService;
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionService;
import com.blackducksoftware.integration.hub.api.view.MetaHandler;
import com.blackducksoftware.integration.hub.dataservice.notification.model.NotificationContentItem;
import com.blackducksoftware.integration.hub.dataservice.notification.model.PolicyNotificationFilter;
import com.blackducksoftware.integration.hub.dataservice.notification.transformer.PolicyViolationClearedTransformer;
import com.blackducksoftware.integration.hub.dataservice.notification.transformer.PolicyViolationOverrideTransformer;
import com.blackducksoftware.integration.hub.dataservice.notification.transformer.PolicyViolationTransformer;
import com.blackducksoftware.integration.hub.dataservice.notification.transformer.VulnerabilityTransformer;
import com.blackducksoftware.integration.hub.dataservice.parallel.ParallelResourceProcessor;
import com.blackducksoftware.integration.hub.dataservice.parallel.ParallelResourceProcessorResults;
import com.blackducksoftware.integration.hub.model.view.NotificationView;
import com.blackducksoftware.integration.hub.model.view.PolicyOverrideNotificationView;
import com.blackducksoftware.integration.hub.model.view.RuleViolationClearedNotificationView;
import com.blackducksoftware.integration.hub.model.view.RuleViolationNotificationView;
import com.blackducksoftware.integration.hub.model.view.UserView;
import com.blackducksoftware.integration.hub.model.view.VulnerabilityNotificationView;
import com.blackducksoftware.integration.hub.service.HubService;
import com.blackducksoftware.integration.log.IntLogger;

public class NotificationDataService {
    private final HubService hubResponseService;
    private final NotificationService notificationRequestService;
    private final ProjectVersionService projectVersionRequestService;
    private final PolicyService policyRequestService;
    private final PolicyNotificationFilter policyNotificationFilter;
    private final MetaHandler metaService;
    private final IntLogger logger;

    public NotificationDataService(final IntLogger logger, final HubService hubResponseService, final NotificationService notificationRequestService, final ProjectVersionService projectVersionRequestService,
            final PolicyService policyRequestService) {
        this(logger, hubResponseService, notificationRequestService, projectVersionRequestService, policyRequestService, null);
    }

    public NotificationDataService(final IntLogger logger, final HubService hubResponseService, final NotificationService notificationRequestService, final ProjectVersionService projectVersionRequestService,
            final PolicyService policyRequestService, final PolicyNotificationFilter policyNotificationFilter) {
        this.logger = logger;
        this.hubResponseService = hubResponseService;
        this.notificationRequestService = notificationRequestService;
        this.projectVersionRequestService = projectVersionRequestService;
        this.policyRequestService = policyRequestService;
        this.policyNotificationFilter = policyNotificationFilter;
        this.metaService = new MetaHandler(logger);
    }

    private ParallelResourceProcessor<NotificationContentItem, NotificationView> createProcessor(final IntLogger logger) {
        final ParallelResourceProcessor<NotificationContentItem, NotificationView> parallelProcessor = new ParallelResourceProcessor<>(logger);
        parallelProcessor.addTransform(RuleViolationNotificationView.class,
                new PolicyViolationTransformer(hubResponseService, logger, notificationRequestService, projectVersionRequestService, policyRequestService, policyNotificationFilter, metaService));
        parallelProcessor.addTransform(PolicyOverrideNotificationView.class,
                new PolicyViolationOverrideTransformer(hubResponseService, logger, notificationRequestService, projectVersionRequestService, policyRequestService, policyNotificationFilter, metaService));
        parallelProcessor.addTransform(VulnerabilityNotificationView.class, new VulnerabilityTransformer(hubResponseService, notificationRequestService, projectVersionRequestService, policyRequestService, metaService, logger));
        parallelProcessor.addTransform(RuleViolationClearedNotificationView.class,
                new PolicyViolationClearedTransformer(hubResponseService, logger, notificationRequestService, projectVersionRequestService, policyRequestService, policyNotificationFilter, metaService));
        return parallelProcessor;
    }

    public NotificationResults getAllNotifications(final Date startDate, final Date endDate) throws IntegrationException {
        final SortedSet<NotificationContentItem> contentList = new TreeSet<>();
        final List<Exception> exceptionList = new LinkedList<>();
        NotificationResults results;
        final List<NotificationView> itemList = notificationRequestService.getAllNotifications(startDate, endDate);
        try (ParallelResourceProcessor<NotificationContentItem, NotificationView> parallelProcessor = createProcessor(logger)) {
            final ParallelResourceProcessorResults<NotificationContentItem> processorResults = parallelProcessor.process(itemList);
            contentList.addAll(processorResults.getResults());
            exceptionList.addAll(processorResults.getExceptions());
        } catch (final IOException ex) {
            logger.debug("Error closing processor", ex);
            exceptionList.add(ex);
        } finally {
            results = new NotificationResults(contentList, exceptionList);
        }

        return results;
    }

    public NotificationResults getUserNotifications(final Date startDate, final Date endDate, final UserView user) throws IntegrationException {
        final SortedSet<NotificationContentItem> contentList = new TreeSet<>();
        final List<Exception> exceptionList = new LinkedList<>();
        final NotificationResults results;
        final List<NotificationView> itemList = notificationRequestService.getUserNotifications(startDate, endDate, user);
        try (ParallelResourceProcessor<NotificationContentItem, NotificationView> parallelProcessor = createProcessor(logger)) {
            final ParallelResourceProcessorResults<NotificationContentItem> processorResults = parallelProcessor.process(itemList);
            contentList.addAll(processorResults.getResults());
            exceptionList.addAll(processorResults.getExceptions());
        } catch (final IOException ex) {
            logger.debug("Error closing processor", ex);
            exceptionList.add(ex);
        } finally {
            results = new NotificationResults(contentList, exceptionList);
        }

        return results;
    }
}
