package com.blackducksoftware.integration.hub.item;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.resource.ClientResource;

import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.exception.ItemIsNotInCacheException;
import com.blackducksoftware.integration.hub.exception.ResourceDoesNotExistException;
import com.blackducksoftware.integration.hub.logging.IntLogger;
import com.blackducksoftware.integration.hub.logging.LogLevel;
import com.blackducksoftware.integration.hub.util.RestletUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ItemService {
    private Map<String, JsonElement> itemJsonCache = new HashMap<String, JsonElement>(); // URL
											 // ->
											 // Json
											 // Element
											 // cache
    private Gson gson;
    private final ClientResource reUsableResource;
    private final HubIntRestService hub;
    private IntLogger logger;

    public ItemService(HubIntRestService hub) throws URISyntaxException {
	this.hub = hub;
	reUsableResource = hub.createClientResource();
	reUsableResource.setMethod(Method.GET);
	gson = new GsonBuilder().setDateFormat(RestletUtil.JSON_DATE_FORMAT)
		.create();
    }

    public void setLogger(final IntLogger logger) {
	this.logger = logger;
    }

    public <T> T getAndCacheItemsFromRelativeUrl(Class<T> modelClass,
	    List<String> urlSegments,
	    Set<AbstractMap.SimpleEntry<String, String>> queryParameters)
	    throws ResourceDoesNotExistException, URISyntaxException,
	    IOException {

	Reference queryRef = RestletUtil.createReference(hub.getBaseUrl(),
		urlSegments, queryParameters);
	ClientResource resource = RestletUtil.getResource(reUsableResource,
		queryRef);
	logMessage(LogLevel.DEBUG, "Resource: " + resource);
	int responseCode = RestletUtil.getResponseStatusCode(resource);

	if (RestletUtil.isSuccess(responseCode)) {
	    final String response = RestletUtil.readResponseAsString(resource
		    .getResponse());

	    JsonParser parser = new JsonParser();
	    JsonObject json = parser.parse(response).getAsJsonObject();
	    T modelObject = gson.fromJson(json, modelClass);

	    JsonArray array = json.get("items").getAsJsonArray();
	    for (JsonElement elem : array) {
		Item genericItem = gson.fromJson(elem, Item.class);
		String itemUrl = genericItem.getMeta().getHref();
		logMessage(LogLevel.DEBUG, "Caching: Key: " + itemUrl
			+ "; Value: " + elem.toString());
		itemJsonCache.put(itemUrl, elem);
	    }

	    return modelObject;
	} else {
	    throw new ResourceDoesNotExistException(
		    "Error getting resource from relative url segments "
			    + urlSegments + " and query parameters "
			    + queryParameters + "; errorCode: " + responseCode,
		    resource);
	}
    }

    public <T extends Item> T getItemFromCache(Class<T> itemClass,
	    String itemUrl) throws ItemIsNotInCacheException {
	if (!itemJsonCache.containsKey(itemUrl)) {
	    throw new ItemIsNotInCacheException(
		    "Item with URL "
			    + itemUrl
			    + " is not in cache. Make sure it was fetched via getAndCacheItemsFromRelativeUrl(...)");
	}
	T item = gson.fromJson(itemJsonCache.get(itemUrl), itemClass);
	return item;

    }

    public void clearItemCache() {
	itemJsonCache = new HashMap<String, JsonElement>();
    }

    private void logMessage(final LogLevel level, final String txt) {
	if (logger != null) {
	    if (level == LogLevel.ERROR) {
		logger.error(txt);
	    } else if (level == LogLevel.WARN) {
		logger.warn(txt);
	    } else if (level == LogLevel.INFO) {
		logger.info(txt);
	    } else if (level == LogLevel.DEBUG) {
		logger.debug(txt);
	    } else if (level == LogLevel.TRACE) {
		logger.trace(txt);
	    }
	}
    }
}
