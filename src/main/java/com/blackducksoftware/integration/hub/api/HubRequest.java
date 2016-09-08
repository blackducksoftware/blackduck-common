package com.blackducksoftware.integration.hub.api;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
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

	private Method method;
	private String url;
	private final List<String> urlSegments = new ArrayList<>();
	private int limit = 10;
	private final Map<String, String> queryParameters = new HashMap<>();
	private int offset = 0;

	public HubRequest(final RestConnection restConnection, final JsonParser jsonParser) {
		this.restConnection = restConnection;
		this.jsonParser = jsonParser;
	}

	public JsonObject executeForResponseJson() throws IOException, URISyntaxException, BDRestException {
		final ClientResource clientResource = buildClientResource(restConnection);
		try {
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
		} finally {
			releaseResource(clientResource);
		}
	}

	public String executeForResponseString() throws IOException, URISyntaxException, BDRestException {
		final ClientResource clientResource = buildClientResource(restConnection);
		try {
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
		} finally {
			releaseResource(clientResource);
		}
	}

	private void releaseResource(final ClientResource resource) {
		if (resource.getResponse() != null) {
			resource.getResponse().release();
		}
		resource.release();
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
		if (limit <= 0) {
			limit = 10;
		}

		// if offset is not provided, the default is 0
		if (offset < 0) {
			offset = 0;
		}
		queryParameters.put(UrlConstants.QUERY_LIMIT, String.valueOf(limit));
		queryParameters.put(UrlConstants.QUERY_OFFSET, String.valueOf(offset));

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

	public int getLimit() {
		return limit;
	}

	public void setLimit(final int limit) {
		this.limit = limit;
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

	public int getOffset() {
		return offset;
	}

	public void setOffset(final int offset) {
		this.offset = offset;
	}

}
