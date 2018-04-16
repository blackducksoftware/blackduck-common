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
import java.util.Optional;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.RestConstants;
import com.blackducksoftware.integration.hub.api.core.HubPathMultipleResponses;
import com.blackducksoftware.integration.hub.api.generated.discovery.ApiDiscovery;
import com.blackducksoftware.integration.hub.api.generated.enumeration.NotificationType;
import com.blackducksoftware.integration.hub.api.generated.view.UserView;
import com.blackducksoftware.integration.hub.api.view.ImprovedCommonNotificationState;
import com.blackducksoftware.integration.hub.api.view.ImprovedNotificationView;
import com.blackducksoftware.integration.hub.api.view.ImprovedUserNotificationView;
import com.blackducksoftware.integration.hub.api.view.PolicyOverrideNotificationView;
import com.blackducksoftware.integration.hub.api.view.ReducedNotificationView;
import com.blackducksoftware.integration.hub.api.view.ReducedUserNotificationView;
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
import com.blackducksoftware.integration.hub.notification.content.LicenseLimitNotificationContent;
import com.blackducksoftware.integration.hub.notification.content.NotificationContent;
import com.blackducksoftware.integration.hub.notification.content.PolicyOverrideNotificationContent;
import com.blackducksoftware.integration.hub.notification.content.RuleViolationClearedNotificationContent;
import com.blackducksoftware.integration.hub.notification.content.RuleViolationNotificationContent;
import com.blackducksoftware.integration.hub.notification.content.VulnerabilityNotificationContent;
import com.blackducksoftware.integration.hub.request.Request;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.parallel.processor.ParallelResourceProcessor;
import com.blackducksoftware.integration.parallel.processor.ParallelResourceProcessorResults;
import com.google.gson.JsonObject;

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

    public List<ImprovedNotificationView> getImprovedNotifications(final List<ReducedNotificationView> reducedNotifications) {
        final List<ImprovedNotificationView> improvedViews = reducedNotifications
                .stream()
                .map(reducedView -> {
                    final ImprovedNotificationView improvedView = hubService.getGson().fromJson(reducedView.json, ImprovedNotificationView.class);
                    populateImprovedView(improvedView, reducedView.json);
                    return improvedView;
                }).collect(Collectors.toList());

        return improvedViews;
    }

    public List<ImprovedUserNotificationView> getImprovedUserNotifications(final List<ReducedUserNotificationView> reducedUserNotifications) {
        final List<ImprovedUserNotificationView> improvedViews = reducedUserNotifications
                .stream()
                .map(reducedView -> {
                    final ImprovedUserNotificationView improvedView = hubService.getGson().fromJson(reducedView.json, ImprovedUserNotificationView.class);
                    populateImprovedView(improvedView, reducedView.json);
                    return improvedView;
                }).collect(Collectors.toList());

        return improvedViews;
    }

    public List<ReducedNotificationView> getAllNotifications(final Date startDate, final Date endDate) throws IntegrationException {
        final Request.Builder requestBuilder = createNotificationRequestBuilder(startDate, endDate);
        final HubPathMultipleResponses<ReducedNotificationView> notificationLinkResponse = new HubPathMultipleResponses<>(ApiDiscovery.NOTIFICATIONS_LINK, ReducedNotificationView.class);
        final List<ReducedNotificationView> allNotificationItems = hubService.getResponses(notificationLinkResponse, requestBuilder, true, typeMap);
        return allNotificationItems;
    }

    public List<ReducedUserNotificationView> getAllUserNotifications(final UserView user, final Date startDate, final Date endDate) throws IntegrationException {
        final Request.Builder requestBuilder = createNotificationRequestBuilder(startDate, endDate);
        final String userNotificationsUri = hubService.getFirstLink(user, UserView.NOTIFICATIONS_LINK);
        requestBuilder.uri(userNotificationsUri);

        final List<ReducedUserNotificationView> allUserNotificationItems = hubService.getResponses(requestBuilder, ReducedUserNotificationView.class, true);
        return allUserNotificationItems;
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

    private void populateImprovedView(final ImprovedCommonNotificationState improvedView, final String reducedViewJson) {
        // have to populate the json field directly since the transformer normally does that and the transformer isn't used here
        improvedView.setJson(reducedViewJson);
        final Optional<NotificationContent> notificationContent = parseNotificationContent(reducedViewJson, improvedView.getType());
        if (notificationContent.isPresent()) {
            improvedView.setNotificationContent(notificationContent.get());
        }
    }

    private Optional<NotificationContent> parseNotificationContent(final String notificationJson, final NotificationType type) {
        final JsonObject jsonObject = hubService.getJsonParser().parse(notificationJson).getAsJsonObject();
        if (type == NotificationType.LICENSE_LIMIT) {
            return Optional.of(hubService.getGson().fromJson(jsonObject.get("content"), LicenseLimitNotificationContent.class));
        } else if (type == NotificationType.POLICY_OVERRIDE) {
            return Optional.of(hubService.getGson().fromJson(jsonObject.get("content"), PolicyOverrideNotificationContent.class));
        } else if (type == NotificationType.RULE_VIOLATION) {
            return Optional.of(hubService.getGson().fromJson(jsonObject.get("content"), RuleViolationNotificationContent.class));
        } else if (type == NotificationType.RULE_VIOLATION_CLEARED) {
            return Optional.of(hubService.getGson().fromJson(jsonObject.get("content"), RuleViolationClearedNotificationContent.class));
        } else if (type == NotificationType.VULNERABILITY) {
            return Optional.of(hubService.getGson().fromJson(jsonObject.get("content"), VulnerabilityNotificationContent.class));
        }
        return Optional.empty();
    }

    private Request.Builder createNotificationRequestBuilder(final Date startDate, final Date endDate) {
        final SimpleDateFormat sdf = new SimpleDateFormat(RestConstants.JSON_DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        final String startDateString = sdf.format(startDate);
        final String endDateString = sdf.format(endDate);

        return new Request.Builder().addQueryParameter("startDate", startDateString).addQueryParameter("endDate", endDateString);
    }

}
