package com.blackducksoftware.integration.hub.api;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import com.blackducksoftware.integration.hub.api.policy.PolicyRule;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

public class PolicyRestService extends HubRestService<PolicyRule> {
	public PolicyRestService(final RestConnection restConnection, final Gson gson, final JsonParser jsonParser) {
		super(restConnection, gson, jsonParser, new TypeToken<PolicyRule>() {
		}.getType(), new TypeToken<List<PolicyRule>>() {
		}.getType());
	}

	public PolicyRule getPolicyRule(final String policyUrl) throws IOException, BDRestException, URISyntaxException {
		return getItem(policyUrl);
	}

}
