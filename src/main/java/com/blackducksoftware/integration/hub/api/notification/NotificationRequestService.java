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
package com.blackducksoftware.integration.hub.api.notification;

import static com.blackducksoftware.integration.hub.api.UrlConstants.SEGMENT_API;
import static com.blackducksoftware.integration.hub.api.UrlConstants.SEGMENT_NOTIFICATIONS;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.io.IOUtils;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.model.view.NotificationView;
import com.blackducksoftware.integration.hub.model.view.PolicyOverrideNotificationView;
import com.blackducksoftware.integration.hub.model.view.RuleViolationClearedNotificationView;
import com.blackducksoftware.integration.hub.model.view.RuleViolationNotificationView;
import com.blackducksoftware.integration.hub.model.view.UserView;
import com.blackducksoftware.integration.hub.model.view.VulnerabilityNotificationView;
import com.blackducksoftware.integration.hub.request.HubPagedRequest;
import com.blackducksoftware.integration.hub.request.HubRequestFactory;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubResponseService;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.Response;

public class NotificationRequestService extends HubResponseService {
    private static final List<String> NOTIFICATIONS_SEGMENTS = Arrays.asList(SEGMENT_API, SEGMENT_NOTIFICATIONS);

    private final Map<String, Class<? extends NotificationView>> typeMap = new HashMap<>();
    private final HubRequestFactory hubRequestFactory;
    private final JsonParser jsonParser;

    public NotificationRequestService(final RestConnection restConnection) {
        super(restConnection);
        this.hubRequestFactory = new HubRequestFactory(restConnection);
        this.jsonParser = restConnection.jsonParser;
        typeMap.put("VULNERABILITY", VulnerabilityNotificationView.class);
        typeMap.put("RULE_VIOLATION", RuleViolationNotificationView.class);
        typeMap.put("POLICY_OVERRIDE", PolicyOverrideNotificationView.class);
        typeMap.put("RULE_VIOLATION_CLEARED", RuleViolationClearedNotificationView.class);
    }

    public List<NotificationView> getAllNotifications(final Date startDate, final Date endDate) throws IntegrationException {
        final SimpleDateFormat sdf = new SimpleDateFormat(RestConnection.JSON_DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        final String startDateString = sdf.format(startDate);
        final String endDateString = sdf.format(endDate);

        final HubPagedRequest hubPagedRequest = hubRequestFactory.createPagedRequest(100, NOTIFICATIONS_SEGMENTS);
        hubPagedRequest.addQueryParameter("startDate", startDateString);
        hubPagedRequest.addQueryParameter("endDate", endDateString);

        final List<NotificationView> allNotificationItems = getAllItems(hubPagedRequest);
        return allNotificationItems;
    }

    public List<NotificationView> getUserNotifications(final Date startDate, final Date endDate, final UserView user) throws IntegrationException {
        final SimpleDateFormat sdf = new SimpleDateFormat(RestConnection.JSON_DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        final String startDateString = sdf.format(startDate);
        final String endDateString = sdf.format(endDate);
        final String url = getFirstLink(user, MetaService.NOTIFICATIONS_LINK);

        final HubPagedRequest hubPagedRequest = hubRequestFactory.createPagedRequest(100, url);
        hubPagedRequest.addQueryParameter("startDate", startDateString);
        hubPagedRequest.addQueryParameter("endDate", endDateString);

        final List<NotificationView> allNotificationItems = getAllItems(hubPagedRequest);
        return allNotificationItems;
    }

    public List<NotificationView> getItems(final JsonObject jsonObject) throws IntegrationException {
        final LinkedList<NotificationView> itemList = new LinkedList<>();
        final JsonElement itemsElement = jsonObject.get("items");
        final JsonArray itemsArray = itemsElement.getAsJsonArray();
        for (final JsonElement element : itemsArray) {
            final String type = element.getAsJsonObject().get("type").getAsString();
            Class<? extends NotificationView> notificationClass = NotificationView.class;
            if (typeMap.containsKey(type)) {
                notificationClass = typeMap.get(type);
            }
            final NotificationView item = getItemAs(element, notificationClass);
            itemList.add(item);
        }
        return itemList;
    }

    /**
     * Will NOT make further paged requests to get the full list of items
     */
    public List<NotificationView> getItems(final HubPagedRequest hubPagedRequest) throws IntegrationException {
        Response response = null;
        try {
            response = hubPagedRequest.executeGet();
            final String jsonResponse = readResponseString(response);

            final JsonObject jsonObject = jsonParser.parse(jsonResponse).getAsJsonObject();
            return getItems(jsonObject);
        } finally {
            IOUtils.closeQuietly(response);
        }
    }

    /**
     * Will make further paged requests to get the full list of items
     */
    public List<NotificationView> getAllItems(final HubPagedRequest hubPagedRequest) throws IntegrationException {
        final LinkedList<NotificationView> allItems = new LinkedList<>();
        int totalCount = 0;
        int currentOffset = hubPagedRequest.offset;
        Response response = null;
        try {
            response = hubPagedRequest.executeGet();
            final String jsonResponse = readResponseString(response);

            final JsonObject jsonObject = jsonParser.parse(jsonResponse).getAsJsonObject();
            totalCount = jsonObject.get("totalCount").getAsInt();
            allItems.addAll(getItems(jsonObject));
            while (currentOffset < totalCount) {
                currentOffset = currentOffset + hubPagedRequest.limit;
                hubPagedRequest.offset = currentOffset;
                allItems.addAll(getItems(hubPagedRequest));
            }
        } finally {
            IOUtils.closeQuietly(response);
        }

        return allItems;
    }

}
