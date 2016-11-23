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
package com.blackducksoftware.integration.hub.api.notification;

import static com.blackducksoftware.integration.hub.api.UrlConstants.SEGMENT_API;
import static com.blackducksoftware.integration.hub.api.UrlConstants.SEGMENT_NOTIFICATIONS;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.restlet.data.Method;

import com.blackducksoftware.integration.hub.api.HubItemRestService;
import com.blackducksoftware.integration.hub.api.HubPagedRequest;
import com.blackducksoftware.integration.hub.api.user.UserItem;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.UnexpectedHubResponseException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

public class NotificationRestService extends HubItemRestService<NotificationItem> {
    private static final List<String> NOTIFICATIONS_SEGMENTS = Arrays.asList(SEGMENT_API, SEGMENT_NOTIFICATIONS);

    private static final Type ITEM_TYPE = new TypeToken<NotificationItem>() {
    }.getType();

    private static final Type ITEM_LIST_TYPE = new TypeToken<List<NotificationItem>>() {
    }.getType();

    private final Map<String, Class<? extends NotificationItem>> typeMap = new HashMap<>();

    public NotificationRestService(final RestConnection restConnection) {
        super(restConnection, ITEM_TYPE, ITEM_LIST_TYPE);

        typeMap.put("VULNERABILITY", VulnerabilityNotificationItem.class);
        typeMap.put("RULE_VIOLATION", RuleViolationNotificationItem.class);
        typeMap.put("POLICY_OVERRIDE", PolicyOverrideNotificationItem.class);
        typeMap.put("RULE_VIOLATION_CLEARED", RuleViolationClearedNotificationItem.class);
    }

    public List<NotificationItem> getAllNotifications(final Date startDate, final Date endDate)
            throws IOException, URISyntaxException, BDRestException {
        final SimpleDateFormat sdf = new SimpleDateFormat(RestConnection.JSON_DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        final String startDateString = sdf.format(startDate);
        final String endDateString = sdf.format(endDate);

        final HubPagedRequest notificationItemRequest = new HubPagedRequest(getRestConnection());
        notificationItemRequest.setMethod(Method.GET);
        notificationItemRequest.setLimit(100);
        notificationItemRequest.addUrlSegments(NOTIFICATIONS_SEGMENTS);
        notificationItemRequest.addQueryParameter("startDate", startDateString);
        notificationItemRequest.addQueryParameter("endDate", endDateString);

        final JsonObject jsonObject = notificationItemRequest.executeForResponseJson();
        final List<NotificationItem> allNotificationItems = getAll(jsonObject, notificationItemRequest);
        return allNotificationItems;
    }

    public List<NotificationItem> getUserNotifications(final Date startDate, final Date endDate, final UserItem user)
            throws UnexpectedHubResponseException, IOException, URISyntaxException, BDRestException {
        final SimpleDateFormat sdf = new SimpleDateFormat(RestConnection.JSON_DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        final String startDateString = sdf.format(startDate);
        final String endDateString = sdf.format(endDate);

        final HubPagedRequest notificationItemRequest = new HubPagedRequest(getRestConnection());
        notificationItemRequest.setMethod(Method.GET);
        notificationItemRequest.setLimit(100);
        notificationItemRequest.setUrl(user.getLink("notifications"));
        notificationItemRequest.addQueryParameter("startDate", startDateString);
        notificationItemRequest.addQueryParameter("endDate", endDateString);

        final JsonObject jsonObject = notificationItemRequest.executeForResponseJson();
        final List<NotificationItem> allNotificationItems = getAll(jsonObject, notificationItemRequest);
        return allNotificationItems;
    }

    @Override
    public List<NotificationItem> getItems(final JsonObject jsonObject) {
        final JsonArray jsonArray = jsonObject.get("items").getAsJsonArray();
        final List<NotificationItem> allNotificationItems = new ArrayList<>(jsonArray.size());
        for (final JsonElement jsonElement : jsonArray) {
            final String type = jsonElement.getAsJsonObject().get("type").getAsString();
            Class<? extends NotificationItem> clazz = NotificationItem.class;
            if (typeMap.containsKey(type)) {
                clazz = typeMap.get(type);
            }
            allNotificationItems.add(getRestConnection().getGson().fromJson(jsonElement, clazz));
        }

        return allNotificationItems;
    }

}
