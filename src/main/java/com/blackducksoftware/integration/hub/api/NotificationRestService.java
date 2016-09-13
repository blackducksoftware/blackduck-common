package com.blackducksoftware.integration.hub.api;

import java.io.IOException;
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
	private final List<String> getNotificationSegments = Arrays.asList(UrlConstants.SEGMENT_API,
			UrlConstants.SEGMENT_NOTIFICATIONS);

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
		super(restConnection, createGsonInstance(), jsonParser, new TypeToken<NotificationItem>() {
		}.getType(), new TypeToken<List<NotificationItem>>() {
		}.getType());
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

}
