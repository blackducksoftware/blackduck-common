package com.blackducksoftware.integration.hub.api;

import java.util.List;

import com.blackducksoftware.integration.hub.api.policy.PolicyStatusItem;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

public class PolicyStatusRestService extends HubRestService<PolicyStatusItem> {
	public PolicyStatusRestService(final RestConnection restConnection, final Gson gson, final JsonParser jsonParser) {
		super(restConnection, gson, jsonParser, new TypeToken<PolicyStatusItem>() {
		}.getType(), new TypeToken<List<PolicyStatusItem>>() {
		}.getType());
	}

}
