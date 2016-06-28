package com.blackducksoftware.integration.hub.util;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.blackducksoftware.integration.hub.exception.MissingUUIDException;

public class HubUrlParser {

	// Project Url :
	// http://eng-hub-valid03.dc1.lan/api/projects/a3b48f57-9c00-453f-8672-804e08c317f2
	// Version Url :
	// http://eng-hub-valid03.dc1.lan/api/projects/a3b48f57-9c00-453f-8672-804e08c317f2/versions/7d4fdbed-936b-468f-af7f-826dfc072c5b
	// Rule URL :
	// http://eng-hub-valid03.dc1.lan/api/policy-rules/138d0d0f-45b5-4e51-8a32-42ed8946434c

	public static List<UUID> getUUIDsFromURL(final URL url) throws MissingUUIDException {
		if (url == null) {
			throw new IllegalArgumentException("No URL was provided to parse.");
		}
		return getUUIDsFromURLString(url.toString());
	}

	public static List<UUID> getUUIDsFromURLString(final String url) throws MissingUUIDException {
		if (url == null) {
			throw new IllegalArgumentException("No url String was provided to parse.");
		}
		final List<UUID> uuids = new ArrayList<UUID>();

		final String[] urlParts = url.split("/");
		for (final String urlPart : urlParts) {
			try {
				uuids.add(UUID.fromString(urlPart));
			} catch (final IllegalArgumentException e) {
				// ignore errors for the parts of the URL that are not UUID's
			}
		}

		if(uuids.isEmpty()){
			throw new MissingUUIDException("The String provided does not contain any UUID's.");
		}

		return uuids;
	}
}
