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
package com.blackducksoftware.integration.hub.service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.RestConstants;
import com.blackducksoftware.integration.hub.api.core.HubPathMultipleResponses;
import com.blackducksoftware.integration.hub.api.generated.discovery.ApiDiscovery;
import com.blackducksoftware.integration.hub.api.view.PolicyOverrideNotificationView;
import com.blackducksoftware.integration.hub.api.view.ReducedNotificationView;
import com.blackducksoftware.integration.hub.api.view.RuleViolationClearedNotificationView;
import com.blackducksoftware.integration.hub.api.view.RuleViolationNotificationView;
import com.blackducksoftware.integration.hub.api.view.VulnerabilityNotificationView;
import com.blackducksoftware.integration.hub.notification.NotificationContentItem;
import com.blackducksoftware.integration.hub.notification.NotificationResults;
import com.blackducksoftware.integration.hub.notification.PolicyNotificationFilter;
import com.blackducksoftware.integration.hub.notification.PolicyViolationClearedTransformer;
import com.blackducksoftware.integration.hub.notification.PolicyViolationOverrideTransformer;
import com.blackducksoftware.integration.hub.notification.PolicyViolationTransformer;
import com.blackducksoftware.integration.hub.notification.ReducedNotificationViewResults;
import com.blackducksoftware.integration.hub.notification.VulnerabilityTransformer;
import com.blackducksoftware.integration.hub.request.Request;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.parallel.processor.ParallelResourceProcessor;
import com.blackducksoftware.integration.parallel.processor.ParallelResourceProcessorResults;

public class NotificationService extends DataService {
    private final Map<String, Class<? extends ReducedNotificationView>> typeMap = new HashMap<>();

    private final PolicyNotificationFilter policyNotificationFilter;

    public NotificationService(final HubService hubService) {
        this(hubService, null);
    }

    public NotificationService(final HubService hubService, final PolicyNotificationFilter policyNotificationFilter) {
        super(hubService);
        this.policyNotificationFilter = policyNotificationFilter;
        typeMap.put("VULNERABILITY", VulnerabilityNotificationView.class);
        typeMap.put("RULE_VIOLATION", RuleViolationNotificationView.class);
        typeMap.put("POLICY_OVERRIDE", PolicyOverrideNotificationView.class);
        typeMap.put("RULE_VIOLATION_CLEARED", RuleViolationClearedNotificationView.class);
    }

    public NotificationResults getAllNotificationResults(final Date startDate, final Date endDate) throws IntegrationException {
        final List<ReducedNotificationView> itemList = getAllNotifications(startDate, endDate);
        final NotificationResults results = processNotificationsInParallel(itemList);
        return results;
    }

    public List<ReducedNotificationView> getAllNotifications(final Date startDate, final Date endDate) throws IntegrationException {
        final SimpleDateFormat sdf = new SimpleDateFormat(RestConstants.JSON_DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        final String startDateString = sdf.format(startDate);
        final String endDateString = sdf.format(endDate);

        final Request.Builder requestBuilder = new Request.Builder().addQueryParameter("startDate", startDateString).addQueryParameter("endDate", endDateString);
        final HubPathMultipleResponses<ReducedNotificationView> notificationLinkResponse = new HubPathMultipleResponses<>(ApiDiscovery.NOTIFICATIONS_LINK, ReducedNotificationView.class);
        final List<ReducedNotificationView> allNotificationItems = hubService.getResponses(notificationLinkResponse, requestBuilder, true, typeMap);
        return allNotificationItems;
    }

    public ReducedNotificationViewResults getAllNotificationViewResults(final Date startDate, final Date endDate) throws IntegrationException {
        final List<ReducedNotificationView> allNotificationItems = getAllNotifications(startDate, endDate);
        if (allNotificationItems == null || allNotificationItems.isEmpty()) {
            return new ReducedNotificationViewResults(allNotificationItems, null, null);
        }

        final SimpleDateFormat sdf = new SimpleDateFormat(RestConstants.JSON_DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        // we know that the first notification in the list is the most current
        final Date latestCreatedAtDate = allNotificationItems.get(0).createdAt;
        final String latestCreatedAtString = sdf.format(latestCreatedAtDate);

        return new ReducedNotificationViewResults(allNotificationItems, latestCreatedAtDate, latestCreatedAtString);
    }

    private NotificationResults processNotificationsInParallel(final List<ReducedNotificationView> itemList) {
        final SortedSet<NotificationContentItem> contentList = new TreeSet<>();
        final List<Exception> exceptionList = new LinkedList<>();
        NotificationResults results;
        try (ParallelResourceProcessor<NotificationContentItem, ReducedNotificationView> parallelProcessor = createProcessor(logger)) {
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

    private ParallelResourceProcessor<NotificationContentItem, ReducedNotificationView> createProcessor(final IntLogger logger) {
        final ParallelResourceProcessor<NotificationContentItem, ReducedNotificationView> parallelProcessor = new ParallelResourceProcessor<>(logger);
        parallelProcessor.addTransformer(RuleViolationNotificationView.class, new PolicyViolationTransformer(hubService, policyNotificationFilter));
        parallelProcessor.addTransformer(PolicyOverrideNotificationView.class, new PolicyViolationOverrideTransformer(hubService, policyNotificationFilter));
        parallelProcessor.addTransformer(VulnerabilityNotificationView.class, new VulnerabilityTransformer(hubService));
        parallelProcessor.addTransformer(RuleViolationClearedNotificationView.class, new PolicyViolationClearedTransformer(hubService, policyNotificationFilter));
        return parallelProcessor;
    }

}
