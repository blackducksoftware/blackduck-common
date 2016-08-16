package com.blackducksoftware.integration.hub.api;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.restlet.data.Method;

import com.blackducksoftware.integration.hub.api.notification.NotificationItem;
import com.blackducksoftware.integration.hub.api.notification.PolicyOverrideNotificationItem;
import com.blackducksoftware.integration.hub.api.notification.RuleViolationNotificationItem;
import com.blackducksoftware.integration.hub.api.notification.VulnerabilityNotificationItem;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.json.RuntimeTypeAdapterFactory;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

public class NotificationRestService extends HubRestService<NotificationItem> {
	private final List<String> getNotificationSegments = Arrays.asList("api", "notifications");
	private final Type notificationItemListType = new TypeToken<List<NotificationItem>>() {
	}.getType();
	private final SimpleDateFormat dateFormatter;

	private final static Gson createGsonInstance() {
		final GsonBuilder gsonBuilder = new GsonBuilder();

		final RuntimeTypeAdapterFactory<NotificationItem> modelClassTypeAdapter = RuntimeTypeAdapterFactory
				.of(NotificationItem.class, "type");
		// When new notification types need to be supported this method needs to
		// register the new notification type to be supported here.
		modelClassTypeAdapter.registerSubtype(VulnerabilityNotificationItem.class, "VULNERABILITY");
		modelClassTypeAdapter.registerSubtype(RuleViolationNotificationItem.class, "RULE_VIOLATION");
		modelClassTypeAdapter.registerSubtype(PolicyOverrideNotificationItem.class, "POLICY_OVERRIDE");
		gsonBuilder.registerTypeAdapterFactory(modelClassTypeAdapter);
		gsonBuilder.setDateFormat(RestConnection.JSON_DATE_FORMAT);
		return gsonBuilder.create();
	}

	public NotificationRestService(final RestConnection restConnection, final JsonParser jsonParser) {
		super(restConnection, createGsonInstance(), jsonParser);
		dateFormatter = new SimpleDateFormat(RestConnection.JSON_DATE_FORMAT);
		dateFormatter.setTimeZone(java.util.TimeZone.getTimeZone("Zulu"));
	}

	public List<NotificationItem> getNotifications(final Date startDate, final Date endDate)
			throws IOException, URISyntaxException, BDRestException {
		return getNotifications(startDate, endDate, -1);
	}

	public List<NotificationItem> getNotifications(final Date startDate, final Date endDate, final int limit)
			throws IOException, URISyntaxException, BDRestException {
		final String startDateString = dateFormatter.format(startDate);
		final String endDateString = dateFormatter.format(endDate);

		final HubRequest userRequest = new HubRequest(getRestConnection(), getJsonParser());
		userRequest.setMethod(Method.GET);
		userRequest.addUrlSegments(getNotificationSegments);
		userRequest.addQueryParameter("startDate", startDateString);
		userRequest.addQueryParameter("endDate", endDateString);
		if (limit > 0) {
			userRequest.addQueryParameter("limit", String.valueOf(limit));
		}
		final JsonObject jsonObject = userRequest.executeForResponseJson();
		final List<NotificationItem> allNotificationItems = getAll(notificationItemListType, jsonObject, userRequest);
		return allNotificationItems;
	}
}
