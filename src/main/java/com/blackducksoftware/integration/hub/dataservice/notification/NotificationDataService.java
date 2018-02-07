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
import com.blackducksoftware.integration.hub.dataservice.notification.model.NotificationContentItem;
import com.blackducksoftware.integration.hub.dataservice.notification.model.PolicyNotificationFilter;
import com.blackducksoftware.integration.hub.dataservice.notification.transformer.PolicyViolationClearedTransformer;
import com.blackducksoftware.integration.hub.dataservice.notification.transformer.PolicyViolationOverrideTransformer;
import com.blackducksoftware.integration.hub.dataservice.notification.transformer.PolicyViolationTransformer;
import com.blackducksoftware.integration.hub.dataservice.notification.transformer.VulnerabilityTransformer;
import com.blackducksoftware.integration.hub.dataservice.parallel.ParallelResourceProcessor;
import com.blackducksoftware.integration.hub.dataservice.parallel.ParallelResourceProcessorResults;
import com.blackducksoftware.integration.hub.request.PagedRequest;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubService;
import com.blackducksoftware.integration.log.IntLogger;

public class NotificationDataService extends HubService {
    private final Map<String, Class<? extends NotificationView>> typeMap = new HashMap<>();

    private final HubService hubResponseService;
    private final PolicyNotificationFilter policyNotificationFilter;
    private final ParallelResourceProcessor<NotificationContentItem, NotificationView> parallelProcessor;
    private final MetaHandler metaService;

    public NotificationDataService(final RestConnection restConnection, final HubService hubResponseService) {
        this(restConnection, hubResponseService, null);
    }

    public NotificationDataService(final RestConnection restConnection, final HubService hubResponseService,
            final PolicyNotificationFilter policyNotificationFilter) {
        super(restConnection);
        this.hubResponseService = hubResponseService;
        this.policyNotificationFilter = policyNotificationFilter;
        this.parallelProcessor = new ParallelResourceProcessor<>(restConnection.logger);
        this.metaService = new MetaHandler(restConnection.logger);
        populateTransformerMap(restConnection.logger);
        typeMap.put("VULNERABILITY", VulnerabilityNotificationView.class);
        typeMap.put("RULE_VIOLATION", RuleViolationNotificationView.class);
        typeMap.put("POLICY_OVERRIDE", PolicyOverrideNotificationView.class);
        typeMap.put("RULE_VIOLATION_CLEARED", RuleViolationClearedNotificationView.class);
    }

    private void populateTransformerMap(final IntLogger logger) {
        parallelProcessor.addTransform(RuleViolationNotificationView.class,
                new PolicyViolationTransformer(hubResponseService, logger, policyNotificationFilter, metaService));
        parallelProcessor.addTransform(PolicyOverrideNotificationView.class,
                new PolicyViolationOverrideTransformer(hubResponseService, logger, policyNotificationFilter, metaService));
        parallelProcessor.addTransform(VulnerabilityNotificationView.class, new VulnerabilityTransformer(hubResponseService, metaService, logger));
        parallelProcessor.addTransform(RuleViolationClearedNotificationView.class,
                new PolicyViolationClearedTransformer(hubResponseService, logger, policyNotificationFilter, metaService));
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

        final Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("startDate", startDateString);
        queryParameters.put("endDate", endDateString);

        final String uri = getHubRequestFactory().pieceTogetherURI(getHubBaseUrl(), ApiDiscovery.NOTIFICATIONS_LINK);
        final PagedRequest pagedRequest = getHubRequestFactory().createGetPagedRequest(uri, queryParameters);

        final List<NotificationView> allNotificationItems = getAllResponses(pagedRequest, NotificationView.class, typeMap);
        return allNotificationItems;
    }

    public List<NotificationView> getUserNotifications(final Date startDate, final Date endDate, final UserView user) throws IntegrationException {
        final SimpleDateFormat sdf = new SimpleDateFormat(RestConnection.JSON_DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        final String startDateString = sdf.format(startDate);
        final String endDateString = sdf.format(endDate);
        final String uri = getFirstLink(user, MetaHandler.NOTIFICATIONS_LINK);

        final Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("startDate", startDateString);
        queryParameters.put("endDate", endDateString);

        final PagedRequest pagedRequest = getHubRequestFactory().createGetPagedRequest(uri, queryParameters);

        final List<NotificationView> allNotificationItems = getAllResponses(pagedRequest, NotificationView.class, typeMap);
        return allNotificationItems;
    }

}
