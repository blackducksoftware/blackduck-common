package com.blackducksoftware.integration.hub.api;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;

import com.blackducksoftware.integration.hub.api.component.BomComponentVersionPolicyStatus;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

public class VersionBomPolicyRestService extends HubRestService<BomComponentVersionPolicyStatus> {
	private final Type bomComponentVersionStatus = new TypeToken<BomComponentVersionPolicyStatus>() {
	}.getType();

	public VersionBomPolicyRestService(final RestConnection restConnection, final Gson gson,
			final JsonParser jsonParser) {
		super(restConnection, gson, jsonParser);
	}

	public BomComponentVersionPolicyStatus getPolicyStatus(final String policyStatusUrl)
			throws IOException, BDRestException, URISyntaxException {
		return getItem(bomComponentVersionStatus, policyStatusUrl);
	}
}
