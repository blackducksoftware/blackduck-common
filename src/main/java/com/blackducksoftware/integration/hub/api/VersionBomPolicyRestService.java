package com.blackducksoftware.integration.hub.api;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.List;

import com.blackducksoftware.integration.hub.api.component.BomComponentVersionPolicyStatus;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

public class VersionBomPolicyRestService extends HubRestService<BomComponentVersionPolicyStatus> {
	public static final Type TYPE_TOKEN_ITEM = new TypeToken<BomComponentVersionPolicyStatus>() {
	}.getType();
	public static final Type TYPE_TOKEN_LIST = new TypeToken<List<BomComponentVersionPolicyStatus>>() {
	}.getType();

	public VersionBomPolicyRestService(final RestConnection restConnection, final Gson gson,
			final JsonParser jsonParser) {
		super(restConnection, gson, jsonParser, TYPE_TOKEN_ITEM, TYPE_TOKEN_LIST);
	}

	public BomComponentVersionPolicyStatus getPolicyStatus(final String policyStatusUrl)
			throws IOException, BDRestException, URISyntaxException {
		return getItem(policyStatusUrl);
	}

}
