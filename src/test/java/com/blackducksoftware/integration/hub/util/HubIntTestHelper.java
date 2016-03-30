package com.blackducksoftware.integration.hub.util;

import org.apache.commons.lang3.StringUtils;
import org.restlet.data.Cookie;
import org.restlet.data.Method;
import org.restlet.resource.ClientResource;
import org.restlet.util.Series;

import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.exception.BDRestException;

public class HubIntTestHelper extends HubIntRestService {

	public HubIntTestHelper(final String baseUrl) {
		super(baseUrl);
	}

	/**
	 * Delete HubProject. For test purposes only!
	 *
	 */
	public boolean deleteHubProject(final String projectId) throws BDRestException {
		if (StringUtils.isEmpty(projectId)) {
			return false;
		}

		final Series<Cookie> cookies = getCookies();
		final String url = getBaseUrl() + "/api/v1/projects/" + projectId;
		final ClientResource resource = new ClientResource(url);

		resource.getRequest().setCookies(cookies);
		resource.setMethod(Method.DELETE);

		resource.delete();
		final int responseCode = resource.getResponse().getStatus().getCode();

		if (responseCode != 204) {
			throw new BDRestException("Could not connect to the Hub server with the Given Url and credentials. Error Code: " + responseCode, resource);
		} else {
			return true;
		}
	}

}
