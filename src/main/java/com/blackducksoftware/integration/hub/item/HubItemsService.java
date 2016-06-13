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

/**
 * Gets a list of items (such as a list of) from the hub, and get a list of
 * objects back. The type of each object is driven by the "type" field in each
 * returned item.
 *
 * @author sbillings
 *
 * @param <T>
 *            The common parent class of all items (typically HubItem.class, or,
 *            where possible, a subclass of it)
 */
public class HubItemsService<T> {
	private final Gson gson;
	private final RestConnection restConnection;
	private final TypeToken<T> requestListTypeToken;

	/**
	 * Construct a service for a given type of list.
	 *
	 * @param restConnection
	 * @param baseType
	 *            The common parent class of all items.
	 * @param requestListTypeToken
	 *            A Gson type token used to convert each item to an object
	 */
	public HubItemsService(final RestConnection restConnection, final TypeToken<T> requestListTypeToken) {
		this(restConnection, null, requestListTypeToken, null);
	}

	/**
	 * Construct a service for a given type of list.
	 *
	 * @param restConnection
	 * @param baseType
	 *            The common parent class of all items.
	 * @param requestListTypeToken
	 *            A Gson type token used to convert each item to an object
	 */
	public HubItemsService(final RestConnection restConnection, final Class<T> baseType,
			final TypeToken<T> requestListTypeToken) {
		this(restConnection, baseType, requestListTypeToken, null);
	}

	/**
	 * Construct a service for a given type of list.
	 *
	 * @param restConnection
	 * @param baseType
	 *            The common parent class of all items.
	 * @param requestListTypeToken
	 *            A Gson type token used to convert each item to an object
	 * @param typeNameToSubclassMap
	 *            A mapping of type field values to item subclass types
	 */
	public HubItemsService(final RestConnection restConnection, final Class<T> baseType,
			final TypeToken<T> requestListTypeToken,
			final Map<String, Class<? extends T>> typeNameToSubclassMap) {

		this.restConnection = restConnection;
		this.requestListTypeToken = requestListTypeToken;

		final GsonBuilder gsonBuilder = new GsonBuilder();

		if (baseType != null) {
			final RuntimeTypeAdapterFactory<T> modelClassTypeAdapter = RuntimeTypeAdapterFactory
					.of(baseType, "type");
			if (typeNameToSubclassMap != null) {
				for (final String typeName : typeNameToSubclassMap.keySet()) {
					modelClassTypeAdapter.registerSubtype(
							typeNameToSubclassMap.get(typeName), typeName);
				}
			}
			gsonBuilder.registerTypeAdapterFactory(modelClassTypeAdapter);
		}
		gson = gsonBuilder.setDateFormat(RestConnection.JSON_DATE_FORMAT)
				.create();
	}


	/**
	 * Get (from the Hub, via a relative URL) a polymorphic item list.
	 *
	 * @param urlSegments
	 *            Used to construct the relative URL.
	 * @param queryParameters
	 *            Used to construct the relative URL.
	 * @return The list of objects corresponding to the items.
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws ResourceDoesNotExistException
	 * @throws BDRestException
	 */
	public List<T> httpGetItemList(final List<String> urlSegments,
			final Set<AbstractMap.SimpleEntry<String, String>> queryParameters)
					throws IOException, URISyntaxException,
					ResourceDoesNotExistException, BDRestException {

		final List<T> items = new ArrayList<T>();
		final ClientResource resource = restConnection.createClientResource(urlSegments,
				queryParameters);
		resource.setMethod(Method.GET);
		restConnection.handleRequest(resource);
		final int responseCode = restConnection.getResponseStatusCode(resource);

		if (restConnection.isSuccess(responseCode)) {
			final JsonArray array = parseJsonArray(resource);

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

	private JsonArray parseJsonArray(final ClientResource resource) throws IOException {
		final String response = restConnection
				.readResponseAsString(resource.getResponse());

		final JsonParser parser = new JsonParser();
		final JsonObject json = parser.parse(response).getAsJsonObject();
		final JsonArray array = json.get("items").getAsJsonArray();
		return array;
	}

}
