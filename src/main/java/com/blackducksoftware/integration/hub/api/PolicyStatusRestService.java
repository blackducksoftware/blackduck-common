package com.blackducksoftware.integration.hub.api;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.restlet.data.Method;

import com.blackducksoftware.integration.hub.api.policy.PolicyStatusItem;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

public class PolicyStatusRestService extends HubRestService<PolicyStatusItem> {
	public static final Type TYPE_TOKEN_ITEM = new TypeToken<PolicyStatusItem>() {
	}.getType();
	public static final Type TYPE_TOKEN_LIST = new TypeToken<List<PolicyStatusItem>>() {
	}.getType();

	public PolicyStatusRestService(final RestConnection restConnection, final Gson gson, final JsonParser jsonParser) {
		super(restConnection, gson, jsonParser, TYPE_TOKEN_ITEM, TYPE_TOKEN_LIST);
	}

	public PolicyStatusItem getPolicyStatusItem(final String projectId, final String versionId)
			throws IOException, URISyntaxException, BDRestException {
		final List<String> urlSegments = new ArrayList<>();
		urlSegments.add(UrlConstants.SEGMENT_API);
		urlSegments.add(UrlConstants.SEGMENT_PROJECTS);
		urlSegments.add(projectId);
		urlSegments.add(UrlConstants.SEGMENT_VERSIONS);
		urlSegments.add(versionId);
		urlSegments.add(UrlConstants.SEGMENT_POLICY_STATUS);

		final HubRequest policyStatusItemRequest = new HubRequest(getRestConnection(), getJsonParser());
		policyStatusItemRequest.setMethod(Method.GET);
		policyStatusItemRequest.setLimit(1);
		policyStatusItemRequest.addUrlSegments(urlSegments);

		final JsonObject jsonObject = policyStatusItemRequest.executeForResponseJson();
		final PolicyStatusItem policyStatusItem = getItem(jsonObject, PolicyStatusItem.class);
		return policyStatusItem;
	}

}
