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
import com.blackducksoftware.integration.hub.api.generated.discovery.ApiDiscovery;
import com.blackducksoftware.integration.hub.api.generated.view.NotificationView;
import com.blackducksoftware.integration.hub.api.generated.view.UserView;
import com.blackducksoftware.integration.hub.api.view.PolicyOverrideNotificationView;
import com.blackducksoftware.integration.hub.api.view.RuleViolationClearedNotificationView;
import com.blackducksoftware.integration.hub.api.view.RuleViolationNotificationView;
import com.blackducksoftware.integration.hub.api.view.VulnerabilityNotificationView;
import com.blackducksoftware.integration.hub.notification.NotificationContentItem;
import com.blackducksoftware.integration.hub.notification.NotificationResults;
import com.blackducksoftware.integration.hub.notification.PolicyNotificationFilter;
import com.blackducksoftware.integration.hub.notification.PolicyViolationClearedTransformer;
import com.blackducksoftware.integration.hub.notification.PolicyViolationOverrideTransformer;
import com.blackducksoftware.integration.hub.notification.PolicyViolationTransformer;
import com.blackducksoftware.integration.hub.notification.VulnerabilityTransformer;
import com.blackducksoftware.integration.hub.request.Request;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.parallel.processor.ParallelResourceProcessor;
import com.blackducksoftware.integration.parallel.processor.ParallelResourceProcessorResults;

public class NotificationService extends DataService {
    private final Map<String, Class<? extends NotificationView>> typeMap = new HashMap<>();

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

    private ParallelResourceProcessor<NotificationContentItem, NotificationView> createProcessor(final IntLogger logger) {
        final ParallelResourceProcessor<NotificationContentItem, NotificationView> parallelProcessor = new ParallelResourceProcessor<>(logger);
        parallelProcessor.addTransformer(RuleViolationNotificationView.class, new PolicyViolationTransformer(hubService, policyNotificationFilter));
        parallelProcessor.addTransformer(PolicyOverrideNotificationView.class, new PolicyViolationOverrideTransformer(hubService, policyNotificationFilter));
        parallelProcessor.addTransformer(VulnerabilityNotificationView.class, new VulnerabilityTransformer(hubService));
        parallelProcessor.addTransformer(RuleViolationClearedNotificationView.class, new PolicyViolationClearedTransformer(hubService, policyNotificationFilter));
        return parallelProcessor;
    }

    public NotificationResults getAllNotificationResults(final Date startDate, final Date endDate) throws IntegrationException {
        final List<NotificationView> itemList = getAllNotifications(startDate, endDate);
        final NotificationResults results = processNotificationsInParallel(itemList);
        return results;
    }

    public NotificationResults getUserNotificationResults(final Date startDate, final Date endDate, final UserView user) throws IntegrationException {
        final List<NotificationView> itemList = getUserNotifications(startDate, endDate, user);
        final NotificationResults results = processNotificationsInParallel(itemList);
        return results;
    }

    public List<NotificationView> getAllNotifications(final Date startDate, final Date endDate) throws IntegrationException {
        final SimpleDateFormat sdf = new SimpleDateFormat(RestConnection.JSON_DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        final String startDateString = sdf.format(startDate);
        final String endDateString = sdf.format(endDate);

        final Request.Builder requestBuilder = new Request.Builder().addQueryParameter("startDate", startDateString).addQueryParameter("endDate", endDateString);
        final List<NotificationView> allNotificationItems = hubService.getResponsesFromPath(ApiDiscovery.NOTIFICATIONS_LINK_RESPONSE, requestBuilder, true, typeMap);
        return allNotificationItems;
    }

    public List<NotificationView> getUserNotifications(final Date startDate, final Date endDate, final UserView user) throws IntegrationException {
        final SimpleDateFormat sdf = new SimpleDateFormat(RestConnection.JSON_DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        final String startDateString = sdf.format(startDate);
        final String endDateString = sdf.format(endDate);

        final Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("startDate", startDateString);
        queryParameters.put("endDate", endDateString);

        final Request.Builder requestBuilder = new Request.Builder().addQueryParameter("startDate", startDateString).addQueryParameter("endDate", endDateString);
        final List<NotificationView> allNotificationItems = hubService.getResponses(user, ApiDiscovery.NOTIFICATIONS_LINK_RESPONSE, requestBuilder, true, typeMap);

        return allNotificationItems;
    }

    private NotificationResults processNotificationsInParallel(final List<NotificationView> itemList) {
        final SortedSet<NotificationContentItem> contentList = new TreeSet<>();
        final List<Exception> exceptionList = new LinkedList<>();
        NotificationResults results;
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
