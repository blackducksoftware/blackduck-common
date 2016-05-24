package com.blackducksoftware.integration.hub.item;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.restlet.resource.ClientResource;

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
    private Gson gson;
    private RestConnection restConnection;
    private final TypeToken<T> requestListTypeToken;

    public HubItemListParser(RestConnection restConnection, Class<T> baseType,
	    TypeToken<T> requestListTypeToken,
	    Map<String, Class<? extends T>> typeNameToSubclassMap) {

	this.restConnection = restConnection;
	this.requestListTypeToken = requestListTypeToken;

	RuntimeTypeAdapterFactory<T> modelClassTypeAdapter = (RuntimeTypeAdapterFactory<T>) RuntimeTypeAdapterFactory
		.of(baseType, "type");
	for (String typeName : typeNameToSubclassMap.keySet()) {
	    modelClassTypeAdapter.registerSubtype(
		    typeNameToSubclassMap.get(typeName), typeName);
	}
	GsonBuilder gsonBuilder = new GsonBuilder();
	gsonBuilder.registerTypeAdapterFactory(modelClassTypeAdapter);
	gson = gsonBuilder.setDateFormat(RestConnection.JSON_DATE_FORMAT)
		.create();
    }

    public List<T> parseNotificationItemList(List<String> urlSegments,
	    Set<AbstractMap.SimpleEntry<String, String>> queryParameters)
	    throws IOException, URISyntaxException,
	    ResourceDoesNotExistException {

	List<T> items = new ArrayList<T>();

	// TODO: Change to use non reusable resource approach
	ClientResource resource = restConnection.getResource(urlSegments,
		queryParameters);

	System.out.println("Resource: " + resource);
	int responseCode = RestConnection.getResponseStatusCode(resource);

	if (RestConnection.isSuccess(responseCode)) {
	    final String response = RestConnection
		    .readResponseAsString(resource.getResponse());

	    JsonParser parser = new JsonParser();
	    JsonObject json = parser.parse(response).getAsJsonObject();
	    HubItemList notificationResponse = gson.fromJson(json,
		    HubItemList.class);
	    System.out.println(notificationResponse);
	    JsonArray array = json.get("items").getAsJsonArray();
	    for (JsonElement elem : array) {
		T genericItem = gson.fromJson(elem,
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
