package com.blackducksoftware.integration.hub.api;

import com.blackducksoftware.integration.hub.rest.RestConnection;

public class HubRestService {
	private final RestConnection restConnection;

	public HubRestService(final RestConnection restConnection) {
		this.restConnection = restConnection;
	}

	public RestConnection getRestConnection() {
		return restConnection;
	}

}
