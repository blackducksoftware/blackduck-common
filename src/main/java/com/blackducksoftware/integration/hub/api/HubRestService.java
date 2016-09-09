package com.blackducksoftware.integration.hub.api;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.restlet.data.Method;

import com.blackducksoftware.integration.hub.api.item.HubItem;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class HubRestService<T extends HubItem> {
	private final RestConnection restConnection;
	private final Gson gson;
	private final JsonParser jsonParser;

	public HubRestService(final RestConnection restConnection, final Gson gson, final JsonParser jsonParser) {
		this.restConnection = restConnection;
		this.gson = gson;
		this.jsonParser = jsonParser;
	}

	public List<T> getAll(final Type type, final JsonObject jsonObject, final HubRequest hubRequest)
			throws BDRestException, IOException, URISyntaxException {
		final List<T> allItems = new ArrayList<>();
		final int totalCount = getTotalCount(jsonObject);
		List<T> items = getItems(type, jsonObject);
		allItems.addAll(items);

		while (allItems.size() < totalCount) {
			final int currentOffset = hubRequest.getOffset();
			final int increasedOffset = currentOffset + items.size();

			hubRequest.setOffset(increasedOffset);
			final JsonObject nextResponse = hubRequest.executeForResponseJson();
			items = getItems(type, nextResponse);
			allItems.addAll(items);
		}

		return allItems;
	}

	public List<T> getItems(final Type type, final JsonObject jsonObject) {
		final List<T> items = gson.fromJson(jsonObject.get("items"), type);
		return items;
	}

	private int getTotalCount(final JsonObject jsonObject) {
		final int totalCount = jsonObject.get("totalCount").getAsInt();
		return totalCount;
	}

	public List<T> getItems(final Type type, final String url) throws IOException, URISyntaxException, BDRestException {
		final HubRequest itemRequest = new HubRequest(getRestConnection(), getJsonParser());
		itemRequest.setMethod(Method.GET);
		itemRequest.setUrl(url);

		final String response = itemRequest.executeForResponseString();
		return getGson().fromJson(response, type);
	}

	public T getItem(final Type type, final String url) throws IOException, BDRestException, URISyntaxException {
		final HubRequest itemRequest = new HubRequest(getRestConnection(), getJsonParser());
		itemRequest.setMethod(Method.GET);
		itemRequest.setUrl(url);

		final String response = itemRequest.executeForResponseString();
		return getGson().fromJson(response, type);
	}

	public RestConnection getRestConnection() {
		return restConnection;
	}

	public Gson getGson() {
		return gson;
	}

	public JsonParser getJsonParser() {
		return jsonParser;
	}
}
