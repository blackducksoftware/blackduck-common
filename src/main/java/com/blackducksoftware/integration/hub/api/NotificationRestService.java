package com.blackducksoftware.integration.hub.api;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.restlet.data.Method;

import com.blackducksoftware.integration.hub.api.notification.NotificationItem;
import com.blackducksoftware.integration.hub.api.notification.PolicyOverrideNotificationItem;
import com.blackducksoftware.integration.hub.api.notification.RuleViolationClearedNotificationItem;
import com.blackducksoftware.integration.hub.api.notification.RuleViolationNotificationItem;
import com.blackducksoftware.integration.hub.api.notification.VulnerabilityNotificationItem;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

public class NotificationRestService extends HubRestService<NotificationItem> {
	private final List<String> getNotificationSegments = Arrays.asList(UrlConstants.SEGMENT_API,
			UrlConstants.SEGMENT_NOTIFICATIONS);
	private final Map<String, Class<? extends NotificationItem>> typeMap = new HashMap<>();

	private final static Gson createGsonInstance() {
		final GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.setDateFormat(RestConnection.JSON_DATE_FORMAT);
		return gsonBuilder.create();
	}

	public NotificationRestService(final RestConnection restConnection, final JsonParser jsonParser) {
		super(restConnection, createGsonInstance(), jsonParser, new TypeToken<NotificationItem>() {
		}.getType(), new TypeToken<List<NotificationItem>>() {
		}.getType());

		typeMap.put("VULNERABILITY", VulnerabilityNotificationItem.class);
		typeMap.put("RULE_VIOLATION", RuleViolationNotificationItem.class);
		typeMap.put("POLICY_OVERRIDE", PolicyOverrideNotificationItem.class);
		typeMap.put("RULE_VIOLATION_CLEARED", RuleViolationClearedNotificationItem.class);
	}

	public List<NotificationItem> getAllNotifications(final Date startDate, final Date endDate)
			throws IOException, URISyntaxException, BDRestException {
		final SimpleDateFormat sdf = new SimpleDateFormat(RestConnection.JSON_DATE_FORMAT);

		final String startDateString = sdf.format(startDate);
		final String endDateString = sdf.format(endDate);

		final HubRequest notificationItemRequest = new HubRequest(getRestConnection(), getJsonParser());
		notificationItemRequest.setMethod(Method.GET);
		notificationItemRequest.setLimit(100);
		notificationItemRequest.addUrlSegments(getNotificationSegments);
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
			allNotificationItems.add(getGson().fromJson(jsonElement, clazz));
		}

		return allNotificationItems;
	}

}
