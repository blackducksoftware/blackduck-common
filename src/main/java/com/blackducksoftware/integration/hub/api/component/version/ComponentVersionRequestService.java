package com.blackducksoftware.integration.hub.api.component.version;

import static com.blackducksoftware.integration.hub.api.UrlConstants.SEGMENT_API;
import static com.blackducksoftware.integration.hub.api.UrlConstants.SEGMENT_COMPONENTS;
import static com.blackducksoftware.integration.hub.api.UrlConstants.SEGMENT_VERSIONS;

import java.util.LinkedList;
import java.util.List;

import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.request.HubPagedRequest;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubParameterizedRequestService;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ComponentVersionRequestService extends HubParameterizedRequestService<License> {

	public ComponentVersionRequestService(RestConnection restConnection) {
		super(restConnection, License.class);
	}

	public List<License> getAllLicenses(final String componentId, final String versionId) throws HubIntegrationException{
		//create list of url segments
		final LinkedList<String> urlSegments = new LinkedList<String>();
		urlSegments.add(SEGMENT_API);
		urlSegments.add(SEGMENT_COMPONENTS);
		urlSegments.add(componentId);
		urlSegments.add(SEGMENT_VERSIONS);
		urlSegments.add(versionId);
		
		//Create request from url segments
		final HubPagedRequest hubPagedRequest = getHubRequestFactory().createGetPagedRequest(urlSegments);
		
		//Call helper method to retrieve licenses from request
		final List<License> allLicenses = getAllLicenses(hubPagedRequest);
		return allLicenses;
	}
	
	public List<License> getAllLicenses(HubPagedRequest hubPagedRequest) throws HubIntegrationException{
		//Create JSON object from response
		final JsonObject jsonObject = hubPagedRequest.executeGetForResponseJson();
		
		//Create List of licenses
		final LinkedList<License> licenses = new LinkedList<License>();
		//Populate list from JSON object
		final JsonElement licenseElement = jsonObject.get("license");
		final JsonObject licenseObject = licenseElement.getAsJsonObject();
		final JsonElement licensesElement = licenseObject.get("licenses");
		final JsonArray licensesArray = licensesElement.getAsJsonArray();
		for(int i=0; i<licensesArray.size(); i++){
			final JsonElement element = licensesArray.get(i);
			final License l = getItem(element, License.class);
			licenses.add(l);
		}
		return licenses;
		
	}
}
