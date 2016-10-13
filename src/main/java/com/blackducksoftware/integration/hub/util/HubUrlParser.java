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
package com.blackducksoftware.integration.hub.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.hub.exception.MissingUUIDException;

public class HubUrlParser {

	public static String getBaseUrl(final String url) throws URISyntaxException {
		final URI uri = new URI(url);
		final String derivedUrlPrefix = StringUtils.join(uri.getScheme(), "://", uri.getAuthority(), "/");
		return derivedUrlPrefix;
	}

	public static String getRelativeUrl(final String url) throws URISyntaxException {
		if (url == null) {
			return null;
		}
		final String baseUrl = getBaseUrl(url);
		final URI baseUri = new URI(baseUrl);
		final URI origUri = new URI(url);
		final URI relativeUri = baseUri.relativize(origUri);
		return relativeUri.toString();
	}

	// Project Url :
	// http://eng-hub-valid03.dc1.lan/api/projects/a3b48f57-9c00-453f-8672-804e08c317f2
	// Version Url :
	// http://eng-hub-valid03.dc1.lan/api/projects/a3b48f57-9c00-453f-8672-804e08c317f2/versions/7d4fdbed-936b-468f-af7f-826dfc072c5b
	// Rule URL :
	// http://eng-hub-valid03.dc1.lan/api/policy-rules/138d0d0f-45b5-4e51-8a32-42ed8946434c
	public static UUID getUUIDFromURL(final String identifier, final URL url) throws MissingUUIDException {
		if (StringUtils.isBlank(identifier)) {
			throw new IllegalArgumentException("No identifier was provided.");
		}
		if (url == null) {
			throw new IllegalArgumentException("No URL was provided to parse.");
		}
		return getUUIDFromURLString(identifier, url.toString());
	}

	public static UUID getUUIDFromURLString(final String identifier, final String url) throws MissingUUIDException {
		if (StringUtils.isBlank(identifier)) {
			throw new IllegalArgumentException("No identifier was provided.");
		}
		if (url == null) {
			throw new IllegalArgumentException("No url String was provided to parse.");
		}
		UUID uuid = null;

		final String[] urlParts = url.split("/");

		for (int i = 0; i < urlParts.length; i++) {
			if (urlParts[i].equalsIgnoreCase(identifier)) {
				try {
					uuid = UUID.fromString(urlParts[i + 1]);
				} catch (final IllegalArgumentException e) {
				}
				break;
			}
		}

		if (uuid == null) {
			throw new MissingUUIDException("The String provided : " + url
					+ ", does not contain any UUID's for the specified identifer : " + identifier);
		}

		return uuid;
	}

}
