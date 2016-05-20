package com.blackducksoftware.integration.hub.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URISyntaxException;
import java.util.AbstractMap;
import java.util.List;
import java.util.Set;

import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.resource.ClientResource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class RestletUtil {
    public static final String JSON_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    public static int getResponseStatusCode(ClientResource resource) {
	return resource.getResponse().getStatus().getCode();
    }

    /**
     * This method exists for code symmetry with the other createReference()
     * method.
     */
    public static Reference createReference(String url) {
	return new Reference(url);
    }

    public static Reference createReference(String baseUrl,
	    List<String> urlSegments,
	    Set<AbstractMap.SimpleEntry<String, String>> queryParameters) {
	Reference queryRef = new Reference(baseUrl);
	for (String urlSegment : urlSegments) {
	    queryRef.addSegment(urlSegment);
	}
	for (AbstractMap.SimpleEntry<String, String> queryParameter : queryParameters) {
	    queryRef.addQueryParameter(queryParameter.getKey(),
		    queryParameter.getValue());
	}
	return queryRef;
    }

    public static boolean isSuccess(int responseCode) {
	return responseCode == 200 || responseCode == 204
		|| responseCode == 202;
    }

    public static <T> T parseResponse(Class<T> modelClass,
	    ClientResource resource) throws IOException {
	final String response = readResponseAsString(resource.getResponse());
	Gson gson = new GsonBuilder().setDateFormat(JSON_DATE_FORMAT).create();
	JsonParser parser = new JsonParser();
	JsonObject json = parser.parse(response).getAsJsonObject();
	T modelObject = gson.fromJson(json, modelClass);
	return modelObject;
    }

    public static String readResponseAsString(final Response response)
	    throws IOException {
	final StringBuilder sb = new StringBuilder();
	final Reader reader = response.getEntity().getReader();
	final BufferedReader bufReader = new BufferedReader(reader);
	try {
	    String line;
	    while ((line = bufReader.readLine()) != null) {
		sb.append(line);
		sb.append("\n");
	    }
	} finally {
	    bufReader.close();
	}
	return sb.toString();
    }

    public static ClientResource getResource(ClientResource resource,
	    Reference queryRef) throws URISyntaxException {
	resource.setMethod(Method.GET);
	resource.setReference(queryRef);
	resource.handle();
	return resource;
    }
}
