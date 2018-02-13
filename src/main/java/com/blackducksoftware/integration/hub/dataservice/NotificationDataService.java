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
package com.blackducksoftware.integration.hub.dataservice;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.generated.discovery.ApiDiscovery;
import com.blackducksoftware.integration.hub.api.generated.view.NotificationView;
import com.blackducksoftware.integration.hub.api.generated.view.UserView;
import com.blackducksoftware.integration.hub.api.view.MetaHandler;
import com.blackducksoftware.integration.hub.api.view.PolicyOverrideNotificationView;
import com.blackducksoftware.integration.hub.api.view.RuleViolationClearedNotificationView;
import com.blackducksoftware.integration.hub.api.view.RuleViolationNotificationView;
import com.blackducksoftware.integration.hub.api.view.VulnerabilityNotificationView;
import com.blackducksoftware.integration.hub.notification.NotificationContentItem;
import com.blackducksoftware.integration.hub.notification.NotificationResults;
import com.blackducksoftware.integration.hub.notification.ParallelResourceProcessor;
import com.blackducksoftware.integration.hub.notification.ParallelResourceProcessorResults;
import com.blackducksoftware.integration.hub.notification.PolicyNotificationFilter;
import com.blackducksoftware.integration.hub.notification.PolicyViolationClearedTransformer;
import com.blackducksoftware.integration.hub.notification.PolicyViolationOverrideTransformer;
import com.blackducksoftware.integration.hub.notification.PolicyViolationTransformer;
import com.blackducksoftware.integration.hub.notification.VulnerabilityTransformer;
import com.blackducksoftware.integration.hub.rest.GetRequestWrapper;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.log.IntLogger;

public class NotificationDataService extends HubDataService {
    private final Map<String, Class<? extends NotificationView>> typeMap = new HashMap<>();

    private final PolicyNotificationFilter policyNotificationFilter;
    private final ParallelResourceProcessor<NotificationContentItem, NotificationView> parallelProcessor;
    private final MetaHandler metaHandler;

    public NotificationDataService(final RestConnection restConnection) {
        this(restConnection, null);
    }

    public NotificationDataService(final RestConnection restConnection, final PolicyNotificationFilter policyNotificationFilter) {
        super(restConnection);
        this.policyNotificationFilter = policyNotificationFilter;
        this.parallelProcessor = new ParallelResourceProcessor<>(restConnection.logger);
        this.metaHandler = new MetaHandler(restConnection.logger);
        populateTransformerMap(restConnection.logger);
        typeMap.put("VULNERABILITY", VulnerabilityNotificationView.class);
        typeMap.put("RULE_VIOLATION", RuleViolationNotificationView.class);
        typeMap.put("POLICY_OVERRIDE", PolicyOverrideNotificationView.class);
        typeMap.put("RULE_VIOLATION_CLEARED", RuleViolationClearedNotificationView.class);
    }

    private void populateTransformerMap(final IntLogger logger) {
        parallelProcessor.addTransform(RuleViolationNotificationView.class,
                new PolicyViolationTransformer(this, logger, policyNotificationFilter, metaHandler));
        parallelProcessor.addTransform(PolicyOverrideNotificationView.class,
                new PolicyViolationOverrideTransformer(this, logger, policyNotificationFilter, metaHandler));
        parallelProcessor.addTransform(VulnerabilityNotificationView.class, new VulnerabilityTransformer(this, metaHandler, logger));
        parallelProcessor.addTransform(RuleViolationClearedNotificationView.class,
                new PolicyViolationClearedTransformer(this, logger, policyNotificationFilter, metaHandler));
    }

    public NotificationResults getAllNotificationResults(final Date startDate, final Date endDate) throws IntegrationException {
        final SortedSet<NotificationContentItem> contentList = new TreeSet<>();
        final List<NotificationView> itemList = getAllNotifications(startDate, endDate);
        final ParallelResourceProcessorResults<NotificationContentItem> processorResults = parallelProcessor.process(itemList);
        contentList.addAll(processorResults.getResults());
        final NotificationResults results = new NotificationResults(contentList, processorResults.getExceptions());
        return results;
    }

    public NotificationResults getUserNotificationResults(final Date startDate, final Date endDate, final UserView user) throws IntegrationException {
        final SortedSet<NotificationContentItem> contentList = new TreeSet<>();
        final List<NotificationView> itemList = getUserNotifications(startDate, endDate, user);
        final ParallelResourceProcessorResults<NotificationContentItem> processorResults = parallelProcessor.process(itemList);
        contentList.addAll(processorResults.getResults());
        final NotificationResults results = new NotificationResults(contentList, processorResults.getExceptions());
        return results;
    }

    public List<NotificationView> getAllNotifications(final Date startDate, final Date endDate) throws IntegrationException {
        final SimpleDateFormat sdf = new SimpleDateFormat(RestConnection.JSON_DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        final String startDateString = sdf.format(startDate);
        final String endDateString = sdf.format(endDate);

        final GetRequestWrapper requestWrapper = new GetRequestWrapper();
        requestWrapper.addQueryParameter("startDate", startDateString);
        requestWrapper.addQueryParameter("endDate", endDateString);

        final List<NotificationView> allNotificationItems = getResponsesFromLinkResponse(ApiDiscovery.NOTIFICATIONS_LINK_RESPONSE, true, requestWrapper, typeMap);
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

        final GetRequestWrapper requestWrapper = new GetRequestWrapper();
        requestWrapper.addQueryParameter("startDate", startDateString);
        requestWrapper.addQueryParameter("endDate", endDateString);

        final List<NotificationView> allNotificationItems = getResponsesFromLinkResponse(user, ApiDiscovery.NOTIFICATIONS_LINK_RESPONSE, true, requestWrapper, typeMap);
        return allNotificationItems;
    }

}
