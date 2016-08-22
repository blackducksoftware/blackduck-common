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
package com.blackducksoftware.integration.hub.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.net.CookieHandler;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Message;
import org.restlet.Response;
import org.restlet.data.CharacterSet;
import org.restlet.data.Cookie;
import org.restlet.data.CookieSetting;
import org.restlet.data.Header;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.restlet.util.Series;

import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.EncryptionException;
import com.blackducksoftware.integration.hub.exception.ResourceDoesNotExistException;
import com.blackducksoftware.integration.hub.global.HubCredentials;
import com.blackducksoftware.integration.hub.global.HubProxyInfo;
import com.blackducksoftware.integration.hub.logging.IntLogger;
import com.blackducksoftware.integration.hub.logging.LogLevel;
import com.blackducksoftware.integration.hub.util.AuthenticatorUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Manages the low-level details of communicating with the server via REST.
 *
 * @author sbillings
 *
 */
public class RestConnection {
	public static final String JSON_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSX";

	private final String baseUrl;
	private Series<Cookie> cookies;
	private int timeout = 120000;
	private IntLogger logger;
	private final Gson gson = new GsonBuilder().setDateFormat(JSON_DATE_FORMAT).create();
	private final Client client;

	public static Date parseDateString(final String dateString) throws ParseException {
		final SimpleDateFormat sdf = new SimpleDateFormat(JSON_DATE_FORMAT);
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		return sdf.parse(dateString);
	}

	public static String formatDate(final Date date) {
		final SimpleDateFormat sdf = new SimpleDateFormat(JSON_DATE_FORMAT);
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		return sdf.format(date);
	}

	public RestConnection(final String baseUrl) {
		this.baseUrl = baseUrl;
		final Context context = new Context();
		final List<Protocol> protocolList = new ArrayList<>();
		protocolList.add(Protocol.HTTP);
		protocolList.add(Protocol.HTTPS);
		client = new Client(context, protocolList);
	}

	public void setLogger(final IntLogger logger) {
		this.logger = logger;
	}

	public void setTimeout(final int timeout) {
		if (timeout == 0) {
			throw new IllegalArgumentException("Can not set the timeout to zero.");
		}
		// the User sets the timeout in seconds, so we translate to ms
		this.timeout = timeout * 1000;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	/**
	 * The proxy settings get set as System properties. I.E. https.proxyHost,
	 * https.proxyPort, http.proxyHost, http.proxyPort, http.nonProxyHosts
	 *
	 */
	public void setProxyProperties(final HubProxyInfo proxyInfo) {
		cleanUpOldProxySettings();

		if (!StringUtils.isBlank(proxyInfo.getHost()) && proxyInfo.getPort() > 0) {
			if (logger != null) {
				logger.debug("Using Proxy : " + proxyInfo.getHost() + ", at Port : " + proxyInfo.getPort());
			}

			System.setProperty("http.proxyHost", proxyInfo.getHost());
			System.setProperty("http.proxyPort", Integer.toString(proxyInfo.getPort()));

			try {
				if (!StringUtils.isBlank(proxyInfo.getUsername())
						&& !StringUtils.isBlank(proxyInfo.getDecryptedPassword())) {

					AuthenticatorUtil.setAuthenticator(proxyInfo.getUsername(), proxyInfo.getDecryptedPassword());
				}
			} catch (final Exception e) {
				if (logger != null) {
					logger.error(e);
				}
			}
		}
		if (!StringUtils.isBlank(proxyInfo.getIgnoredProxyHosts())) {
			System.setProperty("http.nonProxyHosts", proxyInfo.getIgnoredProxyHosts().replaceAll(",", "|"));
		}
	}

	/**
	 * The proxy settings get set as System properties. I.E. https.proxyHost,
	 * https.proxyPort, http.proxyHost, http.proxyPort, http.nonProxyHosts
	 *
	 */
	public void setProxyProperties(final String proxyHost, final int proxyPort, final List<Pattern> noProxyHosts,
			final String proxyUsername, final String proxyPassword) {

		HubCredentials proxyCredentials = null;
		try {
			proxyCredentials = new HubCredentials(proxyUsername, proxyPassword);
		} catch (final IllegalArgumentException e) {
			if (logger != null) {
				logger.error(e);
			}
		} catch (final EncryptionException e) {
			if (logger != null) {
				logger.error(e);
			}
		}
		String noProxyHostsString = null;
		if (noProxyHosts != null && !noProxyHosts.isEmpty()) {
			for (final Pattern pattern : noProxyHosts) {
				if (noProxyHostsString == null) {
					noProxyHostsString = pattern.toString();
				} else {
					noProxyHostsString = noProxyHostsString + "|" + pattern.toString();
				}
			}
		}

		final HubProxyInfo proxyInfo = new HubProxyInfo(proxyHost, proxyPort, proxyCredentials, noProxyHostsString);
		setProxyProperties(proxyInfo);

	}

	/**
	 * Gets the cookie for the Authorized connection to the Hub server. Returns
	 * the response code from the connection.
	 *
	 */
	public int setCookies(final String hubUserName, final String hubPassword)
			throws URISyntaxException, BDRestException {
		final ClientResource resource = createClientResource();
		try {
			resource.addSegment("j_spring_security_check");
			resource.setMethod(Method.POST);

			final StringRepresentation stringRep = new StringRepresentation(
					"j_username=" + hubUserName + "&j_password=" + hubPassword);
			stringRep.setCharacterSet(CharacterSet.UTF_8);
			stringRep.setMediaType(MediaType.APPLICATION_WWW_FORM);
			resource.getRequest().setEntity(stringRep);

			handleRequest(resource);

			final int statusCode = resource.getResponse().getStatus().getCode();
			if (isSuccess(statusCode)) {
				final Series<CookieSetting> cookieSettings = resource.getResponse().getCookieSettings();
				final Series<Cookie> requestCookies = resource.getRequest().getCookies();
				if (cookieSettings != null && !cookieSettings.isEmpty()) {
					for (final CookieSetting ck : cookieSettings) {
						if (ck == null) {
							continue;
						}
						final Cookie cookie = new Cookie();
						cookie.setName(ck.getName());
						cookie.setDomain(ck.getDomain());
						cookie.setPath(ck.getPath());
						cookie.setValue(ck.getValue());
						cookie.setVersion(ck.getVersion());
						requestCookies.add(cookie);
					}
				}

				if (requestCookies == null || requestCookies.size() == 0) {
					throw new BDRestException(
							"Could not establish connection to '" + getBaseUrl() + "' . Failed to retrieve cookies",
							resource);
				}
				cookies = requestCookies;
			} else {
				throw new BDRestException(resource.getResponse().getStatus().toString(), resource);
			}
			return statusCode;
		} finally {
			releaseResource(resource);
		}
	}

	public Series<Cookie> getCookies() {
		return cookies;
	}

	/**
	 * Get a resource from via an absolute URL.
	 *
	 * @param modelClass
	 *            The type of the returned object.
	 * @param url
	 *            The absolute URL for the resource.
	 * @return The resource gotten from the Hub.
	 * @throws ResourceDoesNotExistException
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws BDRestException
	 */
	public <T> T httpGetFromAbsoluteUrl(final Class<T> modelClass, final String url)
			throws ResourceDoesNotExistException, URISyntaxException, IOException, BDRestException {
		final ClientResource resource = createClientResource(url);
		try {
			resource.setMethod(Method.GET);
			handleRequest(resource);

			logMessage(LogLevel.DEBUG, "Resource: " + resource);
			final int responseCode = getResponseStatusCode(resource);
			if (isSuccess(responseCode)) {
				return parseResponse(modelClass, resource);
			} else {
				throw new ResourceDoesNotExistException(
						"Error getting resource from " + url + ": " + responseCode + "; " + resource.toString(),
						resource);
			}
		} finally {
			releaseResource(resource);
		}
	}

	/**
	 * Get a resource via a relative URL.
	 *
	 * This method uses (and, if necessary, initializes) the re-usable
	 * ClientResource object.
	 *
	 * @param modelClass
	 *            The type of the returned object.
	 * @param urlSegments
	 *            URL segments to add to the base Hub URL.
	 * @param queryParameters
	 *            Query parameters to add to the URL.
	 * @return The resource gotten from the Hub.
	 * @throws IOException
	 * @throws ResourceDoesNotExistException
	 * @throws URISyntaxException
	 * @throws BDRestException
	 */
	public <T> T httpGetFromRelativeUrl(final Class<T> modelClass, final List<String> urlSegments,
			final Set<AbstractMap.SimpleEntry<String, String>> queryParameters)
			throws IOException, ResourceDoesNotExistException, URISyntaxException, BDRestException {

		final ClientResource resource = createClientResource(urlSegments, queryParameters);
		try {
			resource.setMethod(Method.GET);
			handleRequest(resource);

			logMessage(LogLevel.DEBUG, "Resource: " + resource);
			final int responseCode = getResponseStatusCode(resource);

			if (isSuccess(responseCode)) {
				return parseResponse(modelClass, resource);
			} else {
				throw new ResourceDoesNotExistException(
						"Error getting resource from relative url segments " + urlSegments + " and query parameters "
								+ queryParameters + "; errorCode: " + responseCode + "; " + resource.toString(),
						resource);
			}
		} finally {
			releaseResource(resource);
		}
	}

	private void releaseResource(final ClientResource resource) {
		if (resource.getResponse() != null) {
			resource.getResponse().release();
		}
		resource.release();
	}

	public ClientResource createClientResource() throws URISyntaxException {
		return createClientResource(getBaseUrl());
	}

	public ClientResource createClientResource(final String providedUrl) throws URISyntaxException {

		final String stringTimeout = String.valueOf(timeout);
		// final String idleCheckInterval = String.valueOf(timeout / 2);

		client.getContext().getParameters().add("socketTimeout", stringTimeout);

		client.getContext().getParameters().add("socketConnectTimeoutMs", stringTimeout);
		client.getContext().getParameters().add("readTimeout", stringTimeout);

		// set the connection pool parameters for the httpClient. Since we are
		// connecting to one hub instance then the maxConnections per host can
		// be equal to the maxTotalConnections. If this rest connection object
		// connects to more than one hub instance then the maxConnectionsPerHost
		// would need to be divided by the number of hub instances.
		client.getContext().getParameters().add("maxConnectionsPerHost", "100");
		client.getContext().getParameters().add("maxTotalConnections", "100");
		// Should throw timeout exception after the specified timeout, default
		// is 2 minutes
		final ClientResource resource = new ClientResource(client.getContext(), new URI(providedUrl));
		resource.setNext(client);
		resource.getRequest().setCookies(getCookies());
		return resource;
	}

	public ClientResource createClientResource(final List<String> urlSegments,
			final Set<AbstractMap.SimpleEntry<String, String>> queryParameters) throws URISyntaxException {
		final ClientResource resource = createClientResource();

		for (final String urlSegment : urlSegments) {
			resource.addSegment(urlSegment);
		}
		for (final AbstractMap.SimpleEntry<String, String> queryParameter : queryParameters) {
			resource.addQueryParameter(queryParameter.getKey(), queryParameter.getValue());
		}
		return resource;
	}

	public int getResponseStatusCode(final ClientResource resource) {
		return resource.getResponse().getStatus().getCode();
	}

	public boolean isSuccess(final int responseCode) {
		return responseCode >= 200 && responseCode < 300;
	}

	public void handleRequest(final ClientResource resource) throws BDRestException {
		logMessage(LogLevel.TRACE, "Resource : " + resource.toString());
		logRestletRequestOrResponse(resource.getRequest());

		final CookieHandler originalCookieHandler = CookieHandler.getDefault();
		try {
			if (originalCookieHandler != null) {
				logMessage(LogLevel.TRACE, "Setting Cookie Handler to NULL");
				CookieHandler.setDefault(null);
			}
			resource.handle();
		} catch (final ResourceException e) {
			throw new BDRestException("Problem connecting to the Hub server provided.", e, resource);
		} finally {
			if (originalCookieHandler != null) {
				logMessage(LogLevel.TRACE, "Setting Original Cookie Handler : " + originalCookieHandler.toString());
				CookieHandler.setDefault(originalCookieHandler);
			}
		}

		logRestletRequestOrResponse(resource.getResponse());

		logMessage(LogLevel.TRACE, "Status Code : " + resource.getResponse().getStatus().getCode());
	}

	public String readResponseAsString(final Response response) throws IOException {
		final StringBuilder sb = new StringBuilder();
		final Reader reader = response.getEntity().getReader();
		try (final BufferedReader bufReader = new BufferedReader(reader)) {
			String line;
			while ((line = bufReader.readLine()) != null) {
				sb.append(line);
				sb.append("\n");
			}
		}
		return sb.toString();
	}

	private void logRestletRequestOrResponse(final Message requestOrResponse) {
		if (logger != null && logger.getLogLevel() == LogLevel.TRACE) {
			final String requestOrResponseName = requestOrResponse.getClass().getSimpleName();
			logMessage(LogLevel.TRACE, requestOrResponseName + " : " + requestOrResponse.toString());

			if (!requestOrResponse.getAttributes().isEmpty()) {
				logMessage(LogLevel.TRACE, requestOrResponseName + " attributes : ");
				for (final Entry<String, Object> requestAtt : requestOrResponse.getAttributes().entrySet()) {
					logMessage(LogLevel.TRACE, "Attribute key : " + requestAtt.getKey());
					logMessage(LogLevel.TRACE, "Attribute value : " + requestAtt.getValue());
					logMessage(LogLevel.TRACE, "");
				}
				@SuppressWarnings("unchecked")
				final Series<Header> responseheaders = (Series<Header>) requestOrResponse.getAttributes()
						.get(HeaderConstants.ATTRIBUTE_HEADERS);
				if (responseheaders != null) {
					logMessage(LogLevel.TRACE, requestOrResponseName + " headers : ");
					for (final Header header : responseheaders) {
						if (null == header) {
							logMessage(LogLevel.TRACE, "received a null header");
						} else {
							logMessage(LogLevel.TRACE, "Header name : " + header.getName());
							logMessage(LogLevel.TRACE, "Header value : " + header.getValue());
							logMessage(LogLevel.TRACE, "");
						}
					}
				} else {
					logMessage(LogLevel.TRACE, requestOrResponseName + " headers : NONE");
				}
			} else {
				logMessage(LogLevel.TRACE, requestOrResponseName + " does not have any attributes/headers.");
			}
		}
	}

	/**
	 * Clears the previously set System properties I.E. https.proxyHost,
	 * https.proxyPort, http.proxyHost, http.proxyPort, http.nonProxyHosts
	 *
	 */
	private void cleanUpOldProxySettings() {
		System.clearProperty("http.proxyHost");
		System.clearProperty("http.proxyPort");
		System.clearProperty("http.nonProxyHosts");

		AuthenticatorUtil.resetAuthenticator();
	}

	private <T> T parseResponse(final Class<T> modelClass, final ClientResource resource) throws IOException {
		final String response = readResponseAsString(resource.getResponse());
		final JsonParser parser = new JsonParser();
		final JsonObject json = parser.parse(response).getAsJsonObject();

		final T modelObject = gson.fromJson(json, modelClass);
		return modelObject;
	}

	private void logMessage(final LogLevel level, final String txt) {
		if (logger != null) {
			if (level == LogLevel.ERROR) {
				logger.error(txt);
			} else if (level == LogLevel.WARN) {
				logger.warn(txt);
			} else if (level == LogLevel.INFO) {
				logger.info(txt);
			} else if (level == LogLevel.DEBUG) {
				logger.debug(txt);
			} else if (level == LogLevel.TRACE) {
				logger.trace(txt);
			}
		}
	}

	@Override
	public String toString() {
		return "RestConnection [baseUrl=" + baseUrl + "]";
	}

	public String httpPostFromAbsoluteUrl(final String url, final Representation content)
			throws ResourceDoesNotExistException, URISyntaxException, IOException, BDRestException {

		final ClientResource resource = createClientResource(url);
		try {
			resource.setMethod(Method.POST);
			resource.getRequest().setEntity(content);
			return handleHttpPost(resource);
		} finally {
			releaseResource(resource);
		}
	}

	public String httpPostFromRelativeUrl(final List<String> urlSegments, final Representation content)
			throws IOException, ResourceDoesNotExistException, URISyntaxException, BDRestException {
		final Set<SimpleEntry<String, String>> queryParameters = new HashSet<>();
		return httpPostFromRelativeUrl(urlSegments, queryParameters, content);
	}

	public String httpPostFromRelativeUrl(final List<String> urlSegments,
			final Set<AbstractMap.SimpleEntry<String, String>> queryParameters, final Representation content)
			throws IOException, ResourceDoesNotExistException, URISyntaxException, BDRestException {

		final ClientResource resource = createClientResource(urlSegments, queryParameters);
		try {
			resource.setMethod(Method.POST);
			resource.getRequest().setEntity(content);
			return handleHttpPost(resource);
		} finally {
			releaseResource(resource);
		}
	}

	private String handleHttpPost(final ClientResource resource)
			throws IOException, ResourceDoesNotExistException, URISyntaxException, BDRestException {
		handleRequest(resource);

		logMessage(LogLevel.DEBUG, "Resource: " + resource);
		final int responseCode = getResponseStatusCode(resource);

		if (isSuccess(responseCode)) {
			if (resource.getResponse().getAttributes() == null
					|| resource.getResponse().getAttributes().get(HeaderConstants.ATTRIBUTE_HEADERS) == null) {
				throw new ResourceDoesNotExistException(
						"Could not get the response headers after creating the resource.", resource);
			}

			if (responseCode != 201) {
				return "";
			} else {
				@SuppressWarnings("unchecked")
				final Series<Header> responseHeaders = (Series<Header>) resource.getResponse().getAttributes()
						.get(HeaderConstants.ATTRIBUTE_HEADERS);
				final Header resourceUrl = responseHeaders.getFirst("location", true);

				if (resourceUrl == null || StringUtils.isBlank(resourceUrl.getValue())) {
					throw new ResourceDoesNotExistException("Could not get the resource URL from the response headers.",
							resource);
				}
				return resourceUrl.getValue();
			}
		} else {
			throw new ResourceDoesNotExistException(
					"There was a problem creating the resource. Error Code: " + responseCode, resource);
		}
	}

}
