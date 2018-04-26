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
package com.blackducksoftware.integration.hub.throwaway;

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
import java.util.stream.Collectors;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.RestConstants;
import com.blackducksoftware.integration.hub.api.core.HubPathMultipleResponses;
import com.blackducksoftware.integration.hub.api.generated.discovery.ApiDiscovery;
import com.blackducksoftware.integration.hub.api.generated.view.NotificationUserView;
import com.blackducksoftware.integration.hub.request.Request;
import com.blackducksoftware.integration.hub.service.DataService;
import com.blackducksoftware.integration.hub.service.HubResponseTransformer;
import com.blackducksoftware.integration.hub.service.HubResponsesTransformer;
import com.blackducksoftware.integration.hub.service.HubService;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.parallel.processor.ParallelResourceProcessor;
import com.blackducksoftware.integration.parallel.processor.ParallelResourceProcessorResults;
import com.google.gson.JsonObject;

public class OldNotificationService extends DataService {
    private final Map<String, Class<? extends ReducedNotificationView>> typeMap = new HashMap<>();

    private final PolicyNotificationFilter policyNotificationFilter;
    private final HubResponseTransformer hubResponseTransformer;
    private final HubResponsesTransformer hubResponsesTransformer;

    public OldNotificationService(final HubService hubService) {
        this(hubService, null);
    }

    public OldNotificationService(final HubService hubService, final PolicyNotificationFilter policyNotificationFilter) {
        super(hubService);
        this.policyNotificationFilter = policyNotificationFilter;
        typeMap.put("VULNERABILITY", VulnerabilityNotificationView.class);
        typeMap.put("RULE_VIOLATION", RuleViolationNotificationView.class);
        typeMap.put("POLICY_OVERRIDE", PolicyOverrideNotificationView.class);
        typeMap.put("RULE_VIOLATION_CLEARED", RuleViolationClearedNotificationView.class);
        hubResponseTransformer = new HubResponseTransformer(hubService.getRestConnection());
        hubResponsesTransformer = new HubResponsesTransformer(hubService.getRestConnection(), hubResponseTransformer);
    }

    public OldNotificationResults getAllNotificationResults(final Date startDate, final Date endDate) throws IntegrationException {
        final List<ReducedNotificationView> itemList = getAllNotifications(startDate, endDate);
        final OldNotificationResults results = processNotificationsInParallel(itemList);
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

    public OldNotificationResults getAllUserNotificationResults(final List<NotificationUserView> notificationUserViews) throws IntegrationException {
        // until NotificationResults is reworked, this smoke-and-mirrors approach gets it done (for now) :(
        final List<ReducedNotificationView> notificationViews = notificationUserViews.stream().map(this::convertUserNotificationView).collect(Collectors.toList());
        final OldNotificationResults results = processNotificationsInParallel(notificationViews);
        return results;
    }

    private OldNotificationResults processNotificationsInParallel(final List<ReducedNotificationView> itemList) {
        final SortedSet<NotificationContentItem> contentList = new TreeSet<>();
        final List<Exception> exceptionList = new LinkedList<>();
        OldNotificationResults results;
        try (ParallelResourceProcessor<NotificationContentItem, ReducedNotificationView> parallelProcessor = createProcessor(logger)) {
            final ParallelResourceProcessorResults<NotificationContentItem> processorResults = parallelProcessor.process(itemList);
            contentList.addAll(processorResults.getResults());
            exceptionList.addAll(processorResults.getExceptions());
        } catch (final IOException ex) {
            logger.debug("Error closing processor", ex);
            exceptionList.add(ex);
        } finally {
            results = new OldNotificationResults(contentList, exceptionList);
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

    // this is a terrible hack to keep NotificationResults around a bit longer so that hub-jira can move forward
    private ReducedNotificationView convertUserNotificationView(final NotificationUserView notificationUserView) {
        Class<? extends ReducedNotificationView> actualClass = ReducedNotificationView.class;
        final JsonObject elementObject = hubService.getJsonParser().parse(notificationUserView.json).getAsJsonObject();
        if (elementObject.has("type")) {
            final String type = elementObject.get("type").getAsString();
            if (typeMap.containsKey(type)) {
                actualClass = typeMap.get(type);
            }
        }
        final ReducedNotificationView item = hubResponseTransformer.getResponseAs(elementObject, actualClass);
        return item;
    }

}
