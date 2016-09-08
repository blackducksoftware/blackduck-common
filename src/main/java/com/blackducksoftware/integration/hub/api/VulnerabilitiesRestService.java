package com.blackducksoftware.integration.hub.api;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.List;

import org.restlet.data.Method;

import com.blackducksoftware.integration.hub.api.vulnerabilities.VulnerabilityItem;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

public class VulnerabilitiesRestService extends HubRestService<VulnerabilityItem> {

	private final Type vulnerabilityType = new TypeToken<VulnerabilityItem>() {
	}.getType();

	private final Type vulnerabilityListType = new TypeToken<List<VulnerabilityItem>>() {
	}.getType();

	public VulnerabilitiesRestService(final RestConnection restConnection, final Gson gson,
			final JsonParser jsonParser) {
		super(restConnection, gson, jsonParser);
	}

	public List<VulnerabilityItem> getComponentVersionVulnerabilitiesByOrigin(final String componentId,
			final String versionId, final String originId) throws IOException, URISyntaxException, BDRestException {
		final HubRequest itemRequest = new HubRequest(getRestConnection(), getJsonParser());
		itemRequest.setMethod(Method.GET);
		itemRequest.addUrlSegment(UrlConstants.SEGMENT_API);
		itemRequest.addUrlSegment(UrlConstants.SEGMENT_COMPONENTS);
		itemRequest.addUrlSegment(componentId);
		itemRequest.addUrlSegment(UrlConstants.SEGMENT_VERSIONS);
		itemRequest.addUrlSegment(versionId);
		itemRequest.addUrlSegment(UrlConstants.SEGMENT_ORIGIN);
		itemRequest.addUrlSegment(originId);
		itemRequest.addUrlSegment(UrlConstants.SEGMENT_VULNERABILITIES);

		final JsonObject jsonObject = itemRequest.executeForResponseJson();
		final List<VulnerabilityItem> allItems = getAll(vulnerabilityListType, jsonObject, itemRequest);
		return allItems;
	}

	public List<VulnerabilityItem> getComponentVersionVulnerabilities(final String componentId, final String versionId)
			throws IOException, URISyntaxException, BDRestException {
		final HubRequest itemRequest = new HubRequest(getRestConnection(), getJsonParser());
		itemRequest.setMethod(Method.GET);
		itemRequest.addUrlSegment(UrlConstants.SEGMENT_API);
		itemRequest.addUrlSegment(UrlConstants.SEGMENT_COMPONENTS);
		itemRequest.addUrlSegment(componentId);
		itemRequest.addUrlSegment(UrlConstants.SEGMENT_VERSIONS);
		itemRequest.addUrlSegment(versionId);
		itemRequest.addUrlSegment(UrlConstants.SEGMENT_VULNERABILITIES);

		final JsonObject jsonObject = itemRequest.executeForResponseJson();
		final List<VulnerabilityItem> allItems = getAll(vulnerabilityListType, jsonObject, itemRequest);
		return allItems;
	}

	public List<VulnerabilityItem> getComponentVulnerabilities(final String componentId)
			throws IOException, URISyntaxException, BDRestException {
		final HubRequest itemRequest = new HubRequest(getRestConnection(), getJsonParser());
		itemRequest.setMethod(Method.GET);
		itemRequest.addUrlSegment(UrlConstants.SEGMENT_API);
		itemRequest.addUrlSegment(UrlConstants.SEGMENT_COMPONENTS);
		itemRequest.addUrlSegment(componentId);
		itemRequest.addUrlSegment(UrlConstants.SEGMENT_VULNERABILITIES);

		final JsonObject jsonObject = itemRequest.executeForResponseJson();
		final List<VulnerabilityItem> allItems = getAll(vulnerabilityListType, jsonObject, itemRequest);
		return allItems;
	}

	public List<VulnerabilityItem> getVulnerability(final String vulnerabilityId)
			throws IOException, URISyntaxException, BDRestException {
		final HubRequest itemRequest = new HubRequest(getRestConnection(), getJsonParser());
		itemRequest.setMethod(Method.GET);
		itemRequest.addUrlSegment(UrlConstants.SEGMENT_API);
		itemRequest.addUrlSegment(UrlConstants.SEGMENT_VULNERABILITIES);
		itemRequest.addUrlSegment(vulnerabilityId);

		final JsonObject jsonObject = itemRequest.executeForResponseJson();
		final List<VulnerabilityItem> allItems = getAll(vulnerabilityListType, jsonObject, itemRequest);
		return allItems;
	}

	public List<VulnerabilityItem> getVulnerabilityListByUrl(final String url)
			throws IOException, URISyntaxException, BDRestException {
		return getItems(vulnerabilityListType, url);
	}

	public VulnerabilityItem getVulnerabilityByUrl(final String url)
			throws IOException, BDRestException, URISyntaxException {
		return getItem(vulnerabilityType, url);
	}
}
