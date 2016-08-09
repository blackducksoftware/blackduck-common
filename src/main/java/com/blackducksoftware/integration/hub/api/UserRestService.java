package com.blackducksoftware.integration.hub.api;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import org.restlet.data.Method;

import com.blackducksoftware.integration.hub.api.user.UserItem;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

public class UserRestService extends HubRestService<UserItem> {
	private final List<String> getUsersSegments = Arrays.asList("api", "users");
	private final Type userItemListType = new TypeToken<List<UserItem>>() {
	}.getType();

	public UserRestService(final RestConnection restConnection, final Gson gson, final JsonParser jsonParser) {
		super(restConnection, gson, jsonParser);
	}

	public List<UserItem> getAllUsers() throws URISyntaxException, BDRestException, IOException {
		final HubRequest userRequest = new HubRequest(getRestConnection(), getJsonParser());
		userRequest.setMethod(Method.GET);
		userRequest.addUrlSegments(getUsersSegments);
		userRequest.addQueryParameter("limit", "100");

		final JsonObject jsonObject = userRequest.executeForResponseJson();
		final List<UserItem> allUserItems = getAll(userItemListType, jsonObject, userRequest);
		return allUserItems;
	}

}
