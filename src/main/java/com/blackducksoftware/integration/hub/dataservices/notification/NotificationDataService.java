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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import com.blackducksoftware.integration.hub.api.component.ComponentVersionRestService;
import com.blackducksoftware.integration.hub.api.notification.NotificationItem;
import com.blackducksoftware.integration.hub.api.notification.NotificationRestService;
import com.blackducksoftware.integration.hub.api.notification.PolicyOverrideNotificationItem;
import com.blackducksoftware.integration.hub.api.notification.RuleViolationClearedNotificationItem;
import com.blackducksoftware.integration.hub.api.notification.RuleViolationNotificationItem;
import com.blackducksoftware.integration.hub.api.notification.VulnerabilityNotificationItem;
import com.blackducksoftware.integration.hub.api.policy.PolicyRestService;
import com.blackducksoftware.integration.hub.api.project.ProjectVersionRestService;
import com.blackducksoftware.integration.hub.api.version.VersionBomPolicyRestService;
import com.blackducksoftware.integration.hub.api.vulnerabilities.VulnerabilityRestService;
import com.blackducksoftware.integration.hub.dataservices.AbstractDataService;
import com.blackducksoftware.integration.hub.dataservices.notification.items.NotificationContentItem;
import com.blackducksoftware.integration.hub.dataservices.notification.items.PolicyNotificationFilter;
import com.blackducksoftware.integration.hub.dataservices.notification.items.ProjectAggregateBuilder;
import com.blackducksoftware.integration.hub.dataservices.notification.items.ProjectAggregateData;
import com.blackducksoftware.integration.hub.dataservices.notification.transformer.NotificationCounter;
import com.blackducksoftware.integration.hub.dataservices.notification.transformer.PolicyViolationClearedTransformer;
import com.blackducksoftware.integration.hub.dataservices.notification.transformer.PolicyViolationOverrideTransformer;
import com.blackducksoftware.integration.hub.dataservices.notification.transformer.PolicyViolationTransformer;
import com.blackducksoftware.integration.hub.dataservices.notification.transformer.VulnerabilityTransformer;
import com.blackducksoftware.integration.hub.dataservices.parallel.ParallelResourceProcessor;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.log.IntLogger;
import com.google.gson.Gson;
import com.google.gson.JsonParser;

public class NotificationDataService extends AbstractDataService {
	private final NotificationRestService notificationService;
	private final ProjectVersionRestService projectVersionService;
	private final PolicyRestService policyService;
	private final VersionBomPolicyRestService bomVersionPolicyService;
	private final ComponentVersionRestService componentVersionService;
	private PolicyNotificationFilter policyFilter = null;
	private final VulnerabilityRestService vulnerabilityRestService;
	private final ParallelResourceProcessor<NotificationContentItem, NotificationItem> parallelProcessor;

	public NotificationDataService(final IntLogger logger, final RestConnection restConnection, final Gson gson,
			final JsonParser jsonParser) {
		this(logger, restConnection, gson, jsonParser, null);
	}

	public NotificationDataService(final IntLogger logger, final RestConnection restConnection, final Gson gson,
			final JsonParser jsonParser, final PolicyNotificationFilter policyFilter) {
		super(restConnection, gson, jsonParser);
		this.policyFilter = policyFilter;

		notificationService = new NotificationRestService(restConnection, gson, jsonParser);
		projectVersionService = new ProjectVersionRestService(restConnection, gson, jsonParser);
		policyService = new PolicyRestService(restConnection, gson, jsonParser);
		bomVersionPolicyService = new VersionBomPolicyRestService(restConnection, gson, jsonParser);
		componentVersionService = new ComponentVersionRestService(restConnection, gson, jsonParser);
		vulnerabilityRestService = new VulnerabilityRestService(restConnection, gson, jsonParser);
		parallelProcessor = new ParallelResourceProcessor<>(logger);
		populateTransformerMap();
	}

	private void populateTransformerMap() {

		parallelProcessor.addTransform(RuleViolationNotificationItem.class,
				new PolicyViolationTransformer(notificationService, projectVersionService, policyService,
						bomVersionPolicyService, componentVersionService, policyFilter));
		parallelProcessor.addTransform(PolicyOverrideNotificationItem.class,
				new PolicyViolationOverrideTransformer(notificationService, projectVersionService, policyService,
						bomVersionPolicyService, componentVersionService, policyFilter));
		parallelProcessor.addTransform(VulnerabilityNotificationItem.class,
				new VulnerabilityTransformer(notificationService, projectVersionService, policyService,
						bomVersionPolicyService, componentVersionService));
		parallelProcessor.addTransform(RuleViolationClearedNotificationItem.class,
				new PolicyViolationClearedTransformer(notificationService, projectVersionService, policyService,
						bomVersionPolicyService, componentVersionService, policyFilter));
	}

	public SortedSet<NotificationContentItem> getAllNotifications(final Date startDate, final Date endDate)
			throws IOException, URISyntaxException, BDRestException {
		final SortedSet<NotificationContentItem> contentList = new TreeSet<>();
		final List<NotificationItem> itemList = notificationService.getAllNotifications(startDate, endDate);
		contentList.addAll(parallelProcessor.process(itemList));
		return contentList;
	}

	public List<ProjectAggregateData> getNotificationCounts(final Date startDate, final Date endDate)
			throws IOException, URISyntaxException, BDRestException, InterruptedException {
		final Map<String, ProjectAggregateBuilder> projectCounterMap = new ConcurrentHashMap<>();
		final NotificationCounter counter = new NotificationCounter(projectCounterMap);
		final Set<NotificationContentItem> itemList = getAllNotifications(startDate, endDate);
		for (final NotificationContentItem item : itemList) {
			counter.count(item);
		}

		final List<ProjectAggregateData> dataList = new ArrayList<>();
		for (final Map.Entry<String, ProjectAggregateBuilder> entry : projectCounterMap.entrySet()) {
			ProjectAggregateBuilder builder = entry.getValue().updateDateRange(startDate, endDate);
			builder = builder.updateVulnerabilitiesRestService(vulnerabilityRestService);
			dataList.add(builder.build());
		}
		return dataList;
	}
}
