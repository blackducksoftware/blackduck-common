package com.blackducksoftware.integration.hub.api;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.restlet.data.MediaType;
import org.restlet.representation.FileRepresentation;

import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.ResourceDoesNotExistException;
import com.blackducksoftware.integration.hub.rest.RestConnection;

public class BomImportRestService {
	private final RestConnection restConnection;

	public BomImportRestService(final RestConnection restConnection) {
		this.restConnection = restConnection;
	}

	public void importBomFile(final File file, final String mediaType)
			throws IOException, ResourceDoesNotExistException, URISyntaxException, BDRestException {
		final List<String> urlSegments = new ArrayList<>();
		urlSegments.add("api");
		urlSegments.add("v1");
		urlSegments.add("bom-import");
		final Set<SimpleEntry<String, String>> queryParameters = new HashSet<>();
		final FileRepresentation content = new FileRepresentation(file, new MediaType(mediaType));

		restConnection.httpPostFromRelativeUrl(urlSegments, queryParameters, content);
	}

}
