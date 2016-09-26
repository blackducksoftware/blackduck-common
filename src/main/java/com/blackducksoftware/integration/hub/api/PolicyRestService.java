package com.blackducksoftware.integration.hub.api;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.ArrayList;
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
	public static final Type TYPE_TOKEN_ITEM = new TypeToken<PolicyRule>() {
	}.getType();
	public static final Type TYPE_TOKEN_LIST = new TypeToken<List<PolicyRule>>() {
	}.getType();

	private final List<String> getPolicyRuleSegments = Arrays.asList(UrlConstants.SEGMENT_API,
			UrlConstants.SEGMENT_POLICY_RULES);

	public PolicyRestService(final RestConnection restConnection, final Gson gson, final JsonParser jsonParser) {
		super(restConnection, gson, jsonParser, TYPE_TOKEN_ITEM, TYPE_TOKEN_LIST);
	}

	public PolicyRule getPolicyRule(final String policyUrl) throws IOException, BDRestException, URISyntaxException {
		return getItem(policyUrl);
	}

	public PolicyRule getPolicyRuleById(final String policyRuleId)
			throws IOException, BDRestException, URISyntaxException {
		final List<String> urlSegments = new ArrayList<>();
		urlSegments.addAll(getPolicyRuleSegments);
		urlSegments.add(policyRuleId);
		final PolicyRule rule = getItem(urlSegments);
		return rule;
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
