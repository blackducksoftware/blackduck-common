package com.blackducksoftware.integration.hub.api.extension;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.List;

import org.restlet.data.Method;

import com.blackducksoftware.integration.hub.api.HubItemRestService;
import com.blackducksoftware.integration.hub.api.HubRequest;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

public class ExtensionUserOptionRestService extends HubItemRestService<UserOptionLinkItem> {

	private static final Type TYPE_TOKEN_ITEM = new TypeToken<UserOptionLinkItem>() {
	}.getType();
	private static final Type TYPE_TOKEN_LIST = new TypeToken<List<UserOptionLinkItem>>() {
	}.getType();

	public ExtensionUserOptionRestService(final RestConnection restConnection, final Gson gson,
			final JsonParser jsonParser) {
		super(restConnection, gson, jsonParser, TYPE_TOKEN_ITEM, TYPE_TOKEN_LIST);
	}

	public List<UserOptionLinkItem> getUserOptions(final String userOptionsUrl)
			throws IOException, URISyntaxException, BDRestException {
		final HubRequest itemRequest = new HubRequest(getRestConnection(), getJsonParser());
		itemRequest.setUrl(userOptionsUrl);
		itemRequest.setMethod(Method.GET);
		itemRequest.setLimit(100);

		final JsonObject jsonObject = itemRequest.executeForResponseJson();
		final List<UserOptionLinkItem> allItems = getAll(jsonObject, itemRequest);
		return allItems;
	}
}
