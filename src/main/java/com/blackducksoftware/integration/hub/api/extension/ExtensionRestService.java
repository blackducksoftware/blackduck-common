package com.blackducksoftware.integration.hub.api.extension;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.List;

import com.blackducksoftware.integration.hub.api.HubItemRestService;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

public class ExtensionRestService extends HubItemRestService<ExtensionItem> {

	private static final Type TYPE_TOKEN_ITEM = new TypeToken<ExtensionItem>() {
	}.getType();
	private static final Type TYPE_TOKEN_LIST = new TypeToken<List<ExtensionItem>>() {
	}.getType();

	public ExtensionRestService(final RestConnection restConnection, final Gson gson, final JsonParser jsonParser) {
		super(restConnection, gson, jsonParser, TYPE_TOKEN_ITEM, TYPE_TOKEN_LIST);
	}

	public ExtensionItem getExtensionItem(final String extensionUrl)
			throws IOException, BDRestException, URISyntaxException {
		return getItem(extensionUrl);
	}
}
