package com.blackducksoftware.integration.hub.api;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.resource.ClientResource;

import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class HubRequest {
	private final RestConnection restConnection;
	private final JsonParser jsonParser;

	public Method method;
	public String url;
	public List<String> urlSegments = new ArrayList<>();
	public Map<String, String> queryParameters = new HashMap<>();

	public HubRequest(final RestConnection restConnection, final JsonParser jsonParser) {
		this.restConnection = restConnection;
		this.jsonParser = jsonParser;
	}

	public JsonObject executeForResponseJson() throws IOException, URISyntaxException, BDRestException {
		final ClientResource clientResource = buildClientResource(restConnection);
		restConnection.handleRequest(clientResource);

		final Response response = clientResource.getResponse();
		final int responseCode = response.getStatus().getCode();
		if (restConnection.isSuccess(responseCode)) {
			final String responseString = restConnection.readResponseAsString(response);
			final JsonObject jsonObject = jsonParser.parse(responseString).getAsJsonObject();
			return jsonObject;
		} else {
			final String message = String.format("Request was not successful. (responseCode: %s)", responseCode);
			throw new BDRestException(message, clientResource);
		}
	}

	public String executeForResponseString() throws IOException, URISyntaxException, BDRestException {
		final ClientResource clientResource = buildClientResource(restConnection);
		restConnection.handleRequest(clientResource);

		final Response response = clientResource.getResponse();
		final int responseCode = response.getStatus().getCode();
		if (restConnection.isSuccess(responseCode)) {
			final String responseString = restConnection.readResponseAsString(response);
			return responseString;
		} else {
			final String message = String.format("Request was not successful. (responseCode: %s)", responseCode);
			throw new BDRestException(message, clientResource);
		}
	}

	private ClientResource buildClientResource(final RestConnection restConnection) throws URISyntaxException {
		final ClientResource resource;
		if (StringUtils.isNotBlank(url)) {
			resource = restConnection.createClientResource(url);
		} else {
			resource = restConnection.createClientResource();
		}

		for (final String segment : urlSegments) {
			resource.addSegment(segment);
		}

		// if limit is not provided, the default is 10
		if (!queryParameters.containsKey("limit") || NumberUtils.toInt(queryParameters.get("limit")) <= 0) {
			queryParameters.put("limit", "10");
		}

		// if offset is not provided, the default is 0
		if (!queryParameters.containsKey("offset") || NumberUtils.toInt(queryParameters.get("offset")) <= 0) {
			queryParameters.put("offset", "0");
		}
		for (final Map.Entry<String, String> entry : queryParameters.entrySet()) {
			resource.addQueryParameter(entry.getKey(), entry.getValue());
		}

		resource.setMethod(method);
		return resource;
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(final Method method) {
		this.method = method;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(final String url) {
		this.url = url;
	}

	public List<String> getUrlSegments() {
		return urlSegments;
	}

	public void addUrlSegment(final String urlSegment) {
		urlSegments.add(urlSegment);
	}

	public void addUrlSegments(final List<String> urlSegment) {
		urlSegments.addAll(urlSegment);
	}

	public Map<String, String> getQueryParameters() {
		return queryParameters;
	}

	public void addQueryParameter(final String queryParameterName, final String queryParameterValue) {
		queryParameters.put(queryParameterName, queryParameterValue);
	}

	public void addQueryParameters(final Map<String, String> queryParameters) {
		queryParameters.putAll(queryParameters);
	}

}
