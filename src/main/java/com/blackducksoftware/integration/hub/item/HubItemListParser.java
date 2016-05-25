package com.blackducksoftware.integration.hub.item;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.restlet.data.Method;
import org.restlet.resource.ClientResource;

import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.ResourceDoesNotExistException;
import com.blackducksoftware.integration.hub.json.RuntimeTypeAdapterFactory;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

public class HubItemListParser<T> {
	private final Gson gson;
	private final RestConnection restConnection;
	private final TypeToken<T> requestListTypeToken;

	public HubItemListParser(final RestConnection restConnection, final Class<T> baseType,
			final TypeToken<T> requestListTypeToken,
			final Map<String, Class<? extends T>> typeNameToSubclassMap) {

		this.restConnection = restConnection;
		this.requestListTypeToken = requestListTypeToken;

		final RuntimeTypeAdapterFactory<T> modelClassTypeAdapter = RuntimeTypeAdapterFactory
				.of(baseType, "type");
		for (final String typeName : typeNameToSubclassMap.keySet()) {
			modelClassTypeAdapter.registerSubtype(
					typeNameToSubclassMap.get(typeName), typeName);
		}
		final GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapterFactory(modelClassTypeAdapter);
		gson = gsonBuilder.setDateFormat(RestConnection.JSON_DATE_FORMAT)
				.create();
	}

	public List<T> parseItemList(final List<String> urlSegments,
			final Set<AbstractMap.SimpleEntry<String, String>> queryParameters)
					throws IOException, URISyntaxException,
					ResourceDoesNotExistException, BDRestException {

		final List<T> items = new ArrayList<T>();

		// TODO: Change to use non reusable resource approach
		final ClientResource resource = restConnection.createClientResource(urlSegments,
				queryParameters);
		resource.setMethod(Method.GET);
		restConnection.handleRequest(resource);

		System.out.println("Resource: " + resource);
		final int responseCode = restConnection.getResponseStatusCode(resource);

		if (restConnection.isSuccess(responseCode)) {
			final String response = restConnection
					.readResponseAsString(resource.getResponse());

			final JsonParser parser = new JsonParser();
			final JsonObject json = parser.parse(response).getAsJsonObject();
			final HubItemList notificationResponse = gson.fromJson(json,
					HubItemList.class);
			System.out.println(notificationResponse);
			final JsonArray array = json.get("items").getAsJsonArray();
			for (final JsonElement elem : array) {
				final T genericItem = gson.fromJson(elem,
						requestListTypeToken.getType());

				items.add(genericItem);

			}
		} else {
			throw new ResourceDoesNotExistException(
					"Error getting resource from relative url segments "
							+ urlSegments + " and query parameters "
							+ queryParameters + "; errorCode: " + responseCode
							+ "; " + resource, resource);
		}
		return items;
	}

}
