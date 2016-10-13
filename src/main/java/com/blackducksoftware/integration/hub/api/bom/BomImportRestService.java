/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package com.blackducksoftware.integration.hub.api.bom;

import static com.blackducksoftware.integration.hub.api.UrlConstants.SEGMENT_API;
import static com.blackducksoftware.integration.hub.api.UrlConstants.SEGMENT_BOM_IMPORT;
import static com.blackducksoftware.integration.hub.api.UrlConstants.SEGMENT_V1;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.restlet.data.MediaType;
import org.restlet.representation.FileRepresentation;

import com.blackducksoftware.integration.hub.api.HubRestService;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.ResourceDoesNotExistException;
import com.blackducksoftware.integration.hub.rest.RestConnection;

public class BomImportRestService extends HubRestService {
	private static final List<String> BOM_IMPORT_SEGMENTS = Arrays.asList(SEGMENT_API, SEGMENT_V1, SEGMENT_BOM_IMPORT);

	public BomImportRestService(final RestConnection restConnection) {
		super(restConnection);
	}

	public void importBomFile(final File file, final String mediaType)
			throws IOException, ResourceDoesNotExistException, URISyntaxException, BDRestException {
		final Set<SimpleEntry<String, String>> queryParameters = new HashSet<>();
		final FileRepresentation content = new FileRepresentation(file, new MediaType(mediaType));

		getRestConnection().httpPostFromRelativeUrl(BOM_IMPORT_SEGMENTS, queryParameters, content);
	}

}
