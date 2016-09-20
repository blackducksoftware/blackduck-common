package com.blackducksoftware.integration.hub.api;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import org.restlet.data.Method;

import com.blackducksoftware.integration.hub.api.policy.PolicyRule;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

public class PolicyRestService extends HubRestService<PolicyRule> {
	private final List<String> getPolicyRuleSegments = Arrays.asList(UrlConstants.SEGMENT_API,
			UrlConstants.SEGMENT_POLICY_RULES);

	public PolicyRestService(final RestConnection restConnection, final Gson gson, final JsonParser jsonParser) {
		super(restConnection, gson, jsonParser, new TypeToken<PolicyRule>() {
		}.getType(), new TypeToken<List<PolicyRule>>() {
		}.getType());
	}

	public PolicyRule getPolicyRule(final String policyUrl) throws IOException, BDRestException, URISyntaxException {
		return getItem(policyUrl);
	}

	public List<PolicyRule> getAllPolicyRules() throws IOException, BDRestException, URISyntaxException {
		final HubRequest policyRuleItemRequest = new HubRequest(getRestConnection(), getJsonParser());
		policyRuleItemRequest.setMethod(Method.GET);
		policyRuleItemRequest.setLimit(100);
		policyRuleItemRequest.addUrlSegments(getPolicyRuleSegments);

		final JsonObject jsonObject = policyRuleItemRequest.executeForResponseJson();
		final List<PolicyRule> allPolicyRuleItems = getAll(jsonObject, policyRuleItemRequest);
		return allPolicyRuleItems;
	}

}
