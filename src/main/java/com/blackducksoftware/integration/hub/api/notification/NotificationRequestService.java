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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.api.user.UserItem;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.request.HubPagedRequest;
import com.blackducksoftware.integration.hub.request.HubRequestFactory;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.log.IntLogger;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.Response;

public class NotificationRequestService {
    private static final List<String> NOTIFICATIONS_SEGMENTS = Arrays.asList(SEGMENT_API, SEGMENT_NOTIFICATIONS);

    private final Map<String, Class<? extends NotificationItem>> typeMap = new HashMap<>();

    private final MetaService metaService;

    private final HubRequestFactory hubRequestFactory;

    private final JsonParser jsonParser;

    private final Gson gson;

    public NotificationRequestService(final IntLogger logger, final RestConnection restConnection, final MetaService metaService) {
        this.hubRequestFactory = new HubRequestFactory(restConnection);
        this.jsonParser = restConnection.getJsonParser();
        this.gson = restConnection.getGson();
        this.metaService = metaService;
        typeMap.put("VULNERABILITY", VulnerabilityNotificationItem.class);
        typeMap.put("RULE_VIOLATION", RuleViolationNotificationItem.class);
        typeMap.put("POLICY_OVERRIDE", PolicyOverrideNotificationItem.class);
        typeMap.put("RULE_VIOLATION_CLEARED", RuleViolationClearedNotificationItem.class);
    }

    public List<NotificationItem> getAllNotifications(final Date startDate, final Date endDate) throws IntegrationException {
        final SimpleDateFormat sdf = new SimpleDateFormat(RestConnection.JSON_DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        final String startDateString = sdf.format(startDate);
        final String endDateString = sdf.format(endDate);

        final HubPagedRequest hubPagedRequest = hubRequestFactory.createPagedRequest(100, NOTIFICATIONS_SEGMENTS);
        hubPagedRequest.addQueryParameter("startDate", startDateString);
        hubPagedRequest.addQueryParameter("endDate", endDateString);

        final List<NotificationItem> allNotificationItems = getAllItems(hubPagedRequest);
        return allNotificationItems;
    }

    public List<NotificationItem> getUserNotifications(final Date startDate, final Date endDate, final UserItem user) throws IntegrationException {
        final SimpleDateFormat sdf = new SimpleDateFormat(RestConnection.JSON_DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        final String startDateString = sdf.format(startDate);
        final String endDateString = sdf.format(endDate);
        final String url = metaService.getFirstLink(user, MetaService.NOTIFICATIONS_LINK);

        final HubPagedRequest hubPagedRequest = hubRequestFactory.createPagedRequest(100, url);
        hubPagedRequest.addQueryParameter("startDate", startDateString);
        hubPagedRequest.addQueryParameter("endDate", endDateString);

        final List<NotificationItem> allNotificationItems = getAllItems(hubPagedRequest);
        return allNotificationItems;
    }

    public <T extends NotificationItem> T getItemAs(final JsonElement item, final Class<T> clazz) {
        final T hubItem = gson.fromJson(item, clazz);
        hubItem.setJson(item.getAsString());
        return hubItem;
    }

    public List<NotificationItem> getItems(final JsonObject jsonObject) throws IntegrationException {
        final LinkedList<NotificationItem> itemList = new LinkedList<>();
        final JsonElement itemsElement = jsonObject.get("items");
        final JsonArray itemsArray = itemsElement.getAsJsonArray();
        for (final JsonElement element : itemsArray) {
            final String type = element.getAsJsonObject().get("type").getAsString();
            Class<? extends NotificationItem> notificationClass = NotificationItem.class;
            if (typeMap.containsKey(type)) {
                notificationClass = typeMap.get(type);
            }
            final NotificationItem item = getItemAs(element, notificationClass);
            itemList.add(item);
        }
        return itemList;
    }

    /**
     * Will NOT make further paged requests to get the full list of items
     */
    public List<NotificationItem> getItems(final HubPagedRequest hubPagedRequest) throws IntegrationException {
        try (Response response = hubPagedRequest.executeGet()) {
            final String jsonResponse = response.body().string();

            final JsonObject jsonObject = jsonParser.parse(jsonResponse).getAsJsonObject();
            return getItems(jsonObject);
        } catch (final IOException e) {
            throw new HubIntegrationException(e);
        }
    }

    /**
     * Will make further paged requests to get the full list of items
     */
    public List<NotificationItem> getAllItems(final HubPagedRequest hubPagedRequest) throws IntegrationException {
        final LinkedList<NotificationItem> allItems = new LinkedList<>();
        int totalCount = 0;
        int currentOffset = hubPagedRequest.getOffset();
        try (Response response = hubPagedRequest.executeGet()) {
            final String jsonResponse = response.body().string();

            final JsonObject jsonObject = jsonParser.parse(jsonResponse).getAsJsonObject();
            totalCount = jsonObject.get("totalCount").getAsInt();
            allItems.addAll(getItems(jsonObject));
            while (currentOffset < totalCount) {
                currentOffset = currentOffset + hubPagedRequest.getLimit();
                hubPagedRequest.setOffset(currentOffset);
                allItems.addAll(getItems(hubPagedRequest));
            }
        } catch (final IOException e) {
            throw new HubIntegrationException(e);
        }
        return allItems;
    }

}
