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
package com.blackducksoftware.integration.hub;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.restlet.Context;
import org.restlet.Response;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.CharacterSet;
import org.restlet.data.Cookie;
import org.restlet.data.CookieSetting;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.engine.header.Header;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.restlet.util.Series;

import com.blackducksoftware.integration.hub.api.VersionComparison;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.exception.MissingPolicyStatusException;
import com.blackducksoftware.integration.hub.exception.ProjectDoesNotExistException;
import com.blackducksoftware.integration.hub.exception.ResourceDoesNotExistException;
import com.blackducksoftware.integration.hub.exception.VersionDoesNotExistException;
import com.blackducksoftware.integration.hub.logging.IntLogger;
import com.blackducksoftware.integration.hub.logging.LogLevel;
import com.blackducksoftware.integration.hub.policy.api.PolicyStatus;
import com.blackducksoftware.integration.hub.project.api.ProjectItem;
import com.blackducksoftware.integration.hub.report.api.ReportFormatEnum;
import com.blackducksoftware.integration.hub.report.api.ReportInformationItem;
import com.blackducksoftware.integration.hub.report.api.VersionReport;
import com.blackducksoftware.integration.hub.scan.api.ScanLocationItem;
import com.blackducksoftware.integration.hub.scan.api.ScanLocationResults;
import com.blackducksoftware.integration.hub.scan.status.ScanStatusToPoll;
import com.blackducksoftware.integration.hub.util.RestletUtil;
import com.blackducksoftware.integration.hub.version.api.ReleaseItem;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

public class HubIntRestService {

    private Series<Cookie> cookies;

    private final String baseUrl;

    private int timeout = 120000;

    private IntLogger logger;

    private String proxyUsername;

    private String proxyPassword;

    private ClientResource reUsableResource;

    public HubIntRestService(final String baseUrl) throws URISyntaxException {
	this.baseUrl = baseUrl;

    }

    public void setTimeout(final int timeout) {
	if (timeout == 0) {
	    throw new IllegalArgumentException(
		    "Can not set the timeout to zero.");
	}
	// the User sets the timeout in seconds, so we translate to ms
	this.timeout = timeout * 1000;
    }

    public void setLogger(final IntLogger logger) {
	this.logger = logger;
    }

    public String getBaseUrl() {
	return baseUrl;
    }

    private void attemptResetProxyCache() {
	try {
	    Class<?> sunAuthCacheValue;
	    Class<?> sunAuthCache;
	    Class<?> sunAuthCacheImpl;
	    try {
		sunAuthCacheValue = Class
			.forName("sun.net.www.protocol.http.AuthCacheValue");
		sunAuthCache = Class
			.forName("sun.net.www.protocol.http.AuthCache");
		sunAuthCacheImpl = Class
			.forName("sun.net.www.protocol.http.AuthCacheImpl");
	    } catch (final Exception e) {
		// Must not be using a JDK with sun classes so we abandon this
		// reset since it is sun specific
		return;
	    }

	    final java.lang.reflect.Method m = sunAuthCacheValue
		    .getDeclaredMethod("setAuthCache", sunAuthCache);

	    final Constructor<?> authCacheImplConstr = sunAuthCacheImpl
		    .getConstructor();
	    final Object authCachImp = authCacheImplConstr.newInstance();

	    m.invoke(null, authCachImp);

	} catch (final Exception e) {
	    logger.error(e.getMessage());
	}
    }

    /**
     * The proxy settings get set as System properties. I.E. https.proxyHost,
     * https.proxyPort, http.proxyHost, http.proxyPort, http.nonProxyHosts
     *
     */
    public void setProxyProperties(final String proxyHost, final int proxyPort,
	    final List<Pattern> noProxyHosts, final String proxyUsername,
	    final String proxyPassword) {

	cleanUpOldProxySettings();

	if (!StringUtils.isBlank(proxyHost) && proxyPort > 0) {
	    if (logger != null) {
		logger.debug("Using Proxy : " + proxyHost + ", at Port : "
			+ proxyPort);
	    }

	    System.setProperty("https.proxyHost", proxyHost);
	    System.setProperty("https.proxyPort", Integer.toString(proxyPort));
	    System.setProperty("http.proxyHost", proxyHost);
	    System.setProperty("http.proxyPort", Integer.toString(proxyPort));

	    if (!StringUtils.isBlank(proxyUsername)
		    && !StringUtils.isBlank(proxyPassword)) {
		this.proxyUsername = proxyUsername;
		this.proxyPassword = proxyPassword;

		// Java ignores http.proxyUser. Here's the workaround.
		Authenticator.setDefault(new Authenticator() {
		    // Need this to support digest authentication
		    @Override
		    protected PasswordAuthentication getPasswordAuthentication() {
			if (getRequestorType() == RequestorType.PROXY) {
			    return new PasswordAuthentication(proxyUsername,
				    proxyPassword.toCharArray());
			}
			return null;
		    }
		});
	    }
	}
	if (noProxyHosts != null && !noProxyHosts.isEmpty()) {
	    String noProxyHostsString = null;
	    for (final Pattern pattern : noProxyHosts) {
		if (noProxyHostsString == null) {
		    noProxyHostsString = pattern.toString();
		} else {
		    noProxyHostsString = noProxyHostsString + "|"
			    + pattern.toString();
		}
	    }
	    if (!StringUtils.isBlank(noProxyHostsString)) {
		System.setProperty("http.nonProxyHosts", noProxyHostsString);
	    }
	}
    }

    public ClientResource createClientResource() throws URISyntaxException {
	return createClientResource(getBaseUrl());
    }

    public ClientResource createClientResource(final String providedUrl)
	    throws URISyntaxException {

	final Context context = new Context();

	// the socketTimeout parameter is used in the httpClient extension that
	// we do not use
	// We can probably remove this parameter
	final String stringTimeout = String.valueOf(timeout);

	context.getParameters().add("socketTimeout", stringTimeout);

	context.getParameters().add("socketConnectTimeoutMs", stringTimeout);
	context.getParameters().add("readTimeout", stringTimeout);
	// Should throw timeout exception after the specified timeout, default
	// is 2 minutes

	final ClientResource resource = new ClientResource(context, new URI(
		providedUrl));
	resource.getRequest().setCookies(getCookies());
	return resource;
    }

    /**
     * Clears the previously set System properties I.E. https.proxyHost,
     * https.proxyPort, http.proxyHost, http.proxyPort, http.nonProxyHosts
     *
     */
    private void cleanUpOldProxySettings() {
	System.clearProperty("https.proxyHost");
	System.clearProperty("https.proxyPort");
	System.clearProperty("http.proxyHost");
	System.clearProperty("http.proxyPort");
	System.clearProperty("http.nonProxyHosts");

	attemptResetProxyCache();

	Authenticator.setDefault(null);
    }

    public void parseChallengeRequestRawValue(
	    final ChallengeRequest proxyChallengeRequest) {
	if (proxyChallengeRequest == null
		|| StringUtils.isBlank(proxyChallengeRequest.getRawValue())) {
	    return;
	}
	final String rawValue = proxyChallengeRequest.getRawValue();

	final String[] splitRawValue = rawValue.split(",");
	for (final String currentValue : splitRawValue) {
	    final String trimmedCurrentValue = currentValue.trim();
	    if (StringUtils.isBlank(proxyChallengeRequest.getRealm())
		    && trimmedCurrentValue.startsWith("realm=")) {
		final String realm = trimmedCurrentValue.substring("realm="
			.length());
		proxyChallengeRequest.setRealm(realm);
	    } else if (StringUtils.isBlank(proxyChallengeRequest
		    .getServerNonce())
		    && trimmedCurrentValue.startsWith("nonce=")) {
		final String nonce = trimmedCurrentValue.substring("nonce="
			.length());
		proxyChallengeRequest.setServerNonce(nonce);
	    } else if ((proxyChallengeRequest.getQualityOptions() == null || proxyChallengeRequest
		    .getQualityOptions().isEmpty())
		    && trimmedCurrentValue.startsWith("qop=")) {
		final String qop = trimmedCurrentValue.substring("qop="
			.length());
		final List<String> qualityOptions = new ArrayList<String>();
		qualityOptions.add(qop);
		proxyChallengeRequest.setQualityOptions(qualityOptions);
	    } else if (trimmedCurrentValue.startsWith("stale=")) {
		final String stale = trimmedCurrentValue.substring("stale="
			.length());
		proxyChallengeRequest.setStale(Boolean.valueOf(stale));
	    }
	}
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

    /**
     * Gets the cookie for the Authorized connection to the Hub server. Returns
     * the response code from the connection.
     *
     */
    public int setCookies(final String hubUserName, final String hubPassword)
	    throws HubIntegrationException, URISyntaxException, BDRestException {

	final ClientResource resource = createClientResource();
	resource.addSegment("j_spring_security_check");
	resource.setMethod(Method.POST);

	final StringRepresentation stringRep = new StringRepresentation(
		"j_username=" + hubUserName + "&j_password=" + hubPassword);
	stringRep.setCharacterSet(CharacterSet.UTF_8);
	stringRep.setMediaType(MediaType.APPLICATION_WWW_FORM);
	resource.getRequest().setEntity(stringRep);

	logMessage(LogLevel.TRACE, "Cookies before auth : ");
	if (cookies != null) {
	    for (final Cookie ck : cookies) {
		logMessage(
			LogLevel.TRACE,
			"Cookie, name = " + ck.getName() + " , domain = "
				+ ck.getDomain() + " , path = " + ck.getPath()
				+ " , value = " + ck.getValue()
				+ " , version = " + ck.getVersion());
	    }
	} else {
	    logMessage(LogLevel.TRACE,
		    "Current 'Cookies' is null (none have ever been set yet).");
	}

	logMessage(LogLevel.TRACE, "Resource : " + resource.toString());
	logMessage(LogLevel.TRACE, "Request : "
		+ resource.getRequest().toString());

	if (!resource.getRequest().getAttributes().isEmpty()) {
	    logMessage(LogLevel.TRACE, "Request attributes : ");
	    for (final Entry<String, Object> requestAtt : resource.getRequest()
		    .getAttributes().entrySet()) {
		logMessage(LogLevel.TRACE,
			"Attribute key : " + requestAtt.getKey());
		logMessage(LogLevel.TRACE,
			"Attribute value : " + requestAtt.getValue());
		logMessage(LogLevel.TRACE, "");
	    }
	    logMessage(LogLevel.TRACE, "Request headers : ");
	    final Series<Header> requestheaders = (Series<Header>) resource
		    .getRequest().getAttributes()
		    .get(HeaderConstants.ATTRIBUTE_HEADERS);
	    if (requestheaders != null) {
		logMessage(LogLevel.TRACE, "Request headers : ");
		for (final Header header : requestheaders) {
		    if (null == header) {
			logMessage(LogLevel.TRACE, "received a null header");
		    } else {
			logMessage(LogLevel.TRACE,
				"Header name : " + header.getName());
			logMessage(LogLevel.TRACE,
				"Header value : " + header.getValue());
			logMessage(LogLevel.TRACE, "");
		    }
		}
	    } else {
		logMessage(LogLevel.TRACE, "Request headers : NONE");
	    }
	} else {
	    logMessage(LogLevel.TRACE,
		    "Request does not have any attributes/headers.");
	}

	handleRequest(resource, null, 0);

	logMessage(LogLevel.TRACE, "Response : "
		+ resource.getResponse().toString());

	if (!resource.getResponse().getAttributes().isEmpty()) {
	    logMessage(LogLevel.TRACE, "Response attributes : ");
	    for (final Entry<String, Object> requestAtt : resource
		    .getResponse().getAttributes().entrySet()) {
		logMessage(LogLevel.TRACE,
			"Attribute key : " + requestAtt.getKey());
		logMessage(LogLevel.TRACE,
			"Attribute value : " + requestAtt.getValue());
		logMessage(LogLevel.TRACE, "");
	    }
	    final Series<Header> responseheaders = (Series<Header>) resource
		    .getResponse().getAttributes()
		    .get(HeaderConstants.ATTRIBUTE_HEADERS);
	    if (responseheaders != null) {
		logMessage(LogLevel.TRACE, "Response headers : ");
		for (final Header header : responseheaders) {
		    if (null == header) {
			logMessage(LogLevel.TRACE, "received a null header");
		    } else {
			logMessage(LogLevel.TRACE,
				"Header name : " + header.getName());
			logMessage(LogLevel.TRACE,
				"Header value : " + header.getValue());
			logMessage(LogLevel.TRACE, "");
		    }
		}
	    } else {
		logMessage(LogLevel.TRACE, "Response headers : NONE");
	    }
	} else {
	    logMessage(LogLevel.TRACE,
		    "Response does not have any attributes/headers.");
	}

	logMessage(LogLevel.TRACE, "Status Code : "
		+ resource.getResponse().getStatus().getCode());

	final int statusCode = resource.getResponse().getStatus().getCode();
	if (statusCode == 204) {
	    final Series<CookieSetting> cookieSettings = resource.getResponse()
		    .getCookieSettings();
	    if (cookieSettings != null) {
		logMessage(LogLevel.TRACE, "Set-Cookies returned : "
			+ cookieSettings.size());
	    } else {
		logMessage(LogLevel.TRACE, "Set-Cookies returned : NULL");
	    }

	    final Series<Cookie> requestCookies = resource.getRequest()
		    .getCookies();
	    if (cookieSettings != null && !cookieSettings.isEmpty()) {
		for (final CookieSetting ck : cookieSettings) {
		    if (ck == null) {
			continue;
		    }
		    logMessage(LogLevel.TRACE,
			    "Set-Cookie, name = " + ck.getName()
				    + " , domain = " + ck.getDomain()
				    + " , path = " + ck.getPath()
				    + " , value = " + ck.getValue()
				    + " , version = " + ck.getVersion());

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
		throw new HubIntegrationException(
			"Could not establish connection to '" + getBaseUrl()
				+ "' . Failed to retrieve cookies");
	    }

	    cookies = requestCookies;

	    logMessage(LogLevel.TRACE, "Cookies after auth : ");
	    if (cookies != null) {
		for (final Cookie ck : cookies) {
		    logMessage(LogLevel.TRACE, "Cookie, name = " + ck.getName()
			    + " , domain = " + ck.getDomain() + " , path = "
			    + ck.getPath() + " , value = " + ck.getValue()
			    + " , version = " + ck.getVersion());
		}
	    } else {
		logMessage(LogLevel.TRACE, "New 'Cookies' are null.");
	    }
	} else {
	    throw new HubIntegrationException(resource.getResponse()
		    .getStatus().toString());
	}
	reUsableResource = createClientResource(); // Now that cookies are set,
						   // we can create this
	return resource.getResponse().getStatus().getCode();
    }

    public Series<Cookie> getCookies() {
	return cookies;
    }

    /**
     * Retrieves a list of Hub Projects that may match the hubProjectName
     *
     */
    public List<ProjectItem> getProjectMatches(final String projectName)
	    throws IOException, BDRestException, URISyntaxException {
	final ClientResource resource = createClientResource();
	resource.addSegment("api");
	resource.addSegment("projects");
	resource.addQueryParameter("q", "name:" + projectName);
	resource.addQueryParameter("limit", "15");
	resource.setMethod(Method.GET);
	handleRequest(resource, null, 0);
	final int responseCode = resource.getResponse().getStatus().getCode();

	if (RestletUtil.isSuccess(responseCode)) {
	    final String response = RestletUtil.readResponseAsString(resource
		    .getResponse());
	    final Gson gson = new GsonBuilder().create();
	    final JsonParser parser = new JsonParser();
	    final JsonObject json = parser.parse(response).getAsJsonObject();
	    return gson.fromJson(json.get("items"),
		    new TypeToken<List<ProjectItem>>() {
		    }.getType());

	} else {
	    throw new BDRestException(
		    "There was a problem getting the project matches. Error Code: "
			    + responseCode, resource);
	}

    }

    /**
     * Gets the Project that is specified by the projectName
     *
     */
    public ProjectItem getProjectByName(final String projectName)
	    throws IOException, BDRestException, URISyntaxException,
	    ProjectDoesNotExistException {
	final ClientResource resource = createClientResource();
	resource.addSegment("api");
	resource.addSegment("projects");
	resource.addQueryParameter("q", "name:" + projectName);
	resource.addQueryParameter("limit", "15");
	resource.setMethod(Method.GET);
	handleRequest(resource, null, 0);
	final int responseCode = resource.getResponse().getStatus().getCode();

	if (RestletUtil.isSuccess(responseCode)) {
	    final String response = RestletUtil.readResponseAsString(resource
		    .getResponse());
	    final Gson gson = new GsonBuilder().create();
	    final JsonParser parser = new JsonParser();
	    final JsonObject json = parser.parse(response).getAsJsonObject();
	    final List<ProjectItem> projects = gson.fromJson(json.get("items"),
		    new TypeToken<List<ProjectItem>>() {
		    }.getType());

	    for (final ProjectItem project : projects) {
		if (project.getName().equals(projectName)) {
		    return project;
		}
	    }
	    throw new ProjectDoesNotExistException(
		    "This Project does not exist. Project : " + projectName,
		    resource);
	} else if (responseCode == 404)

	{
	    throw new ProjectDoesNotExistException(
		    "This Project does not exist. Project : " + projectName,
		    resource);
	} else {
	    throw new BDRestException(
		    "There was a problem getting a Project by this name. Project : "
			    + projectName, resource);
	}
    }

    public ProjectItem getProject(final String projectUrl) throws IOException,
	    BDRestException, URISyntaxException {
	final ClientResource resource = createClientResource(projectUrl);
	resource.setMethod(Method.GET);
	handleRequest(resource, null, 0);
	final int responseCode = resource.getResponse().getStatus().getCode();

	if (RestletUtil.isSuccess(responseCode)) {
	    final String response = RestletUtil.readResponseAsString(resource
		    .getResponse());
	    final Gson gson = new GsonBuilder().create();
	    return gson.fromJson(response, ProjectItem.class);

	} else {
	    throw new BDRestException(
		    "There was a problem getting the project. Error Code: "
			    + responseCode, resource);
	}

    }

    public ReleaseItem getProjectVersion(final String versionUrl)
	    throws IOException, BDRestException, URISyntaxException {
	final ClientResource resource = createClientResource(versionUrl);
	resource.setMethod(Method.GET);
	handleRequest(resource, null, 0);
	final int responseCode = resource.getResponse().getStatus().getCode();

	if (RestletUtil.isSuccess(responseCode)) {
	    final String response = RestletUtil.readResponseAsString(resource
		    .getResponse());
	    final Gson gson = new GsonBuilder().create();
	    return gson.fromJson(response, ReleaseItem.class);

	} else {
	    throw new BDRestException(
		    "There was a problem getting the version. Error Code: "
			    + responseCode, resource);
	}

    }

    /**
     * Gets the list of Versions for the specified Project
     *
     */
    public ReleaseItem getVersion(final ProjectItem project,
	    final String versionName) throws IOException, BDRestException,
	    URISyntaxException, VersionDoesNotExistException {
	final List<ReleaseItem> versions = getVersionsForProject(project);
	for (final ReleaseItem version : versions) {
	    if (version.getVersionName().equals(versionName)) {
		return version;
	    }
	}
	throw new VersionDoesNotExistException(
		"This Version does not exist. Project : " + project.getName()
			+ " Version : " + versionName);
    }

    /**
     * Gets the list of Versions for the specified Project
     *
     */
    public List<ReleaseItem> getVersionsForProject(final ProjectItem project)
	    throws IOException, BDRestException, URISyntaxException {
	final ClientResource resource = createClientResource(project
		.getLink(ProjectItem.VERSION_LINK));
	resource.addQueryParameter("limit", "10000000");
	resource.setMethod(Method.GET);
	handleRequest(resource, null, 0);
	final int responseCode = resource.getResponse().getStatus().getCode();

	if (RestletUtil.isSuccess(responseCode)) {
	    final String response = RestletUtil.readResponseAsString(resource
		    .getResponse());

	    final Gson gson = new GsonBuilder().create();
	    final JsonParser parser = new JsonParser();
	    final JsonObject json = parser.parse(response).getAsJsonObject();
	    final List<ReleaseItem> versions = gson.fromJson(json.get("items"),
		    new TypeToken<List<ReleaseItem>>() {
		    }.getType());

	    return versions;

	} else {
	    throw new BDRestException(
		    "There was a problem getting the versions for this Project. Error Code: "
			    + responseCode, resource);
	}
    }

    /**
     * Creates a Hub Project with the specified name.
     *
     * @return the project URL.
     */
    public String createHubProject(final String projectName)
	    throws IOException, BDRestException, URISyntaxException {
	final ClientResource resource = createClientResource();
	resource.addSegment("api");
	resource.addSegment("projects");
	resource.setMethod(Method.POST);

	final ProjectItem newProject = new ProjectItem(projectName, null, null);
	final Gson gson = new GsonBuilder().create();
	final StringRepresentation stringRep = new StringRepresentation(
		gson.toJson(newProject));
	stringRep.setMediaType(MediaType.APPLICATION_JSON);
	stringRep.setCharacterSet(CharacterSet.UTF_8);
	resource.getRequest().setEntity(stringRep);
	handleRequest(resource, null, 0);
	final int responseCode = resource.getResponse().getStatus().getCode();

	if (responseCode == 201) {
	    if (resource.getResponse().getAttributes() == null
		    || resource.getResponse().getAttributes()
			    .get(HeaderConstants.ATTRIBUTE_HEADERS) == null) {
		throw new BDRestException(
			"Could not get the response headers after creating the Project.",
			resource);
	    }
	    @SuppressWarnings("unchecked")
	    final Series<Header> responseHeaders = (Series<Header>) resource
		    .getResponse().getAttributes()
		    .get(HeaderConstants.ATTRIBUTE_HEADERS);
	    final Header projectUrl = responseHeaders
		    .getFirst("location", true);

	    if (projectUrl == null
		    || StringUtils.isBlank(projectUrl.getValue())) {
		throw new BDRestException(
			"Could not get the project URL from the response headers.",
			resource);
	    }
	    return projectUrl.getValue();
	} else {

	    throw new BDRestException(
		    "There was a problem creating this Hub Project. Error Code: "
			    + responseCode, resource);
	}

    }

    /**
     * Creates a new Version in the Project specified, using the phase and
     * distribution provided.
     *
     * @return the version URL
     *
     */
    public String createHubVersion(final ProjectItem project,
	    final String versionName, final String phase, final String dist)
	    throws IOException, BDRestException, URISyntaxException {
	final ClientResource resource = createClientResource(project
		.getLink(ProjectItem.VERSION_LINK));

	int responseCode;
	final ReleaseItem newRelease = new ReleaseItem(versionName, phase,
		dist, null, null);

	resource.setMethod(Method.POST);

	final Gson gson = new GsonBuilder().create();
	final StringRepresentation stringRep = new StringRepresentation(
		gson.toJson(newRelease));
	stringRep.setMediaType(MediaType.APPLICATION_JSON);
	stringRep.setCharacterSet(CharacterSet.UTF_8);
	resource.getRequest().setEntity(stringRep);
	handleRequest(resource, null, 0);
	responseCode = resource.getResponse().getStatus().getCode();

	if (responseCode == 201) {
	    if (resource.getResponse().getAttributes() == null
		    || resource.getResponse().getAttributes()
			    .get(HeaderConstants.ATTRIBUTE_HEADERS) == null) {
		throw new BDRestException(
			"Could not get the response headers after creating the Version.",
			resource);
	    }
	    @SuppressWarnings("unchecked")
	    final Series<Header> responseHeaders = (Series<Header>) resource
		    .getResponse().getAttributes()
		    .get(HeaderConstants.ATTRIBUTE_HEADERS);
	    final Header versionUrl = responseHeaders
		    .getFirst("location", true);

	    if (versionUrl == null
		    || StringUtils.isBlank(versionUrl.getValue())) {
		throw new BDRestException(
			"Could not get the version URL from the response headers.",
			resource);
	    }
	    return versionUrl.getValue();
	} else {
	    throw new BDRestException(
		    "There was a problem creating this Version for the specified Hub Project. Error Code: "
			    + responseCode, resource);
	}

    }

    /**
     * Retrieves the version of the Hub server
     */
    public String getHubVersion() throws IOException, BDRestException,
	    URISyntaxException {
	final ClientResource resource = createClientResource();
	resource.addSegment("api");
	resource.addSegment("v1");
	resource.addSegment("current-version");

	int responseCode = 0;

	resource.setMethod(Method.GET);
	handleRequest(resource, null, 0);
	responseCode = resource.getResponse().getStatus().getCode();

	if (RestletUtil.isSuccess(responseCode)) {
	    final Response resp = resource.getResponse();
	    return resp.getEntityAsText();
	} else {
	    throw new BDRestException(
		    "There was a problem getting the version of the Hub server. Error Code: "
			    + responseCode, resource);
	}
    }

    /**
     * Compares the specified version with the actual version of the Hub server.
     *
     */
    public VersionComparison compareWithHubVersion(final String version)
	    throws IOException, BDRestException, URISyntaxException {

	final ClientResource resource = createClientResource();
	resource.addSegment("api");
	resource.addSegment("v1");
	resource.addSegment("current-version-comparison");
	resource.addQueryParameter("version", version);

	int responseCode = 0;

	resource.setMethod(Method.GET);
	handleRequest(resource, null, 0);
	responseCode = resource.getResponse().getStatus().getCode();

	if (RestletUtil.isSuccess(responseCode)) {

	    final String response = RestletUtil.readResponseAsString(resource
		    .getResponse());
	    final Gson gson = new GsonBuilder().create();
	    final VersionComparison comparison = gson.fromJson(response,
		    VersionComparison.class);
	    return comparison;
	} else {
	    throw new BDRestException(
		    "There was a problem comparing the specified version to the version of the Hub server. Error Code: "
			    + responseCode, resource);
	}
    }

    /**
     * Gets the code locations that match the host and paths provided
     *
     * @deprecated with Hub 3.0 should use the status files from the CLI
     *             instead. The CLI option is --statusWriteDir
     */
    @Deprecated
    public List<ScanLocationItem> getScanLocations(final String hostname,
	    final List<String> scanTargets) throws InterruptedException,
	    BDRestException, HubIntegrationException, URISyntaxException,
	    IOException {
	final List<ScanLocationItem> codeLocations = new ArrayList<ScanLocationItem>();
	ClientResource resource = null;
	for (final String targetPath : scanTargets) {
	    String correctedTargetPath = targetPath;

	    // Scan paths in the Hub only use '/' not '\'
	    if (correctedTargetPath.contains("\\")) {
		correctedTargetPath = correctedTargetPath.replace("\\", "/");
	    }
	    // and it always starts with a '/'
	    if (!correctedTargetPath.startsWith("/")) {
		correctedTargetPath = "/" + correctedTargetPath;
	    }

	    resource = createClientResource();
	    resource.addSegment("api");
	    resource.addSegment("v1");
	    resource.addSegment("scanlocations");
	    resource.addQueryParameter("host", hostname);
	    resource.addQueryParameter("path", correctedTargetPath);

	    resource.setMethod(Method.GET);

	    handleRequest(resource, null, 0);

	    final int responseCode = resource.getResponse().getStatus()
		    .getCode();

	    if (responseCode == 200) {
		final String response = RestletUtil
			.readResponseAsString(resource.getResponse());
		final ScanLocationResults results = new Gson().fromJson(
			response, ScanLocationResults.class);
		final ScanLocationItem currentCodeLocation = getScanLocationMatch(
			hostname, correctedTargetPath, results);
		if (currentCodeLocation == null) {
		    throw new HubIntegrationException(
			    "Could not determine the code location for the Host : "
				    + hostname + " and Path : "
				    + correctedTargetPath);
		}

		codeLocations.add(currentCodeLocation);
	    } else {
		throw new BDRestException(
			"There was a problem getting the code locations for the host and paths provided. Error Code: "
				+ responseCode, resource);
	    }

	}
	return codeLocations;
    }

    private ScanLocationItem getScanLocationMatch(final String hostname,
	    final String scanTarget, final ScanLocationResults results) {
	String targetPath = scanTarget;

	if (targetPath.endsWith("/")) {
	    targetPath = targetPath.substring(0, targetPath.length() - 1);
	}

	for (final ScanLocationItem scanMatch : results.getItems()) {

	    String path = scanMatch.getPath().trim();

	    // Remove trailing slash from both strings
	    if (path.endsWith("/")) {
		path = path.substring(0, path.length() - 1);
	    }
	    if (targetPath.equals(path)) {
		return scanMatch;
	    }
	}

	return null;
    }

    /**
     * Generates a new Hub report for the specified version.
     *
     * @return the Report URL
     *
     */
    public String generateHubReport(final ReleaseItem version,
	    final ReportFormatEnum reportFormat) throws IOException,
	    BDRestException, URISyntaxException {
	if (ReportFormatEnum.UNKNOWN == reportFormat) {
	    throw new IllegalArgumentException(
		    "Can not generate a report of format : " + reportFormat);
	}

	final ClientResource resource = createClientResource(version
		.getLink(ReleaseItem.VERSION_REPORT_LINK));

	resource.setMethod(Method.POST);

	final JsonObject json = new JsonObject();
	json.addProperty("reportFormat", reportFormat.name());

	final Gson gson = new GsonBuilder().create();
	final StringRepresentation stringRep = new StringRepresentation(
		gson.toJson(json));
	stringRep.setMediaType(MediaType.APPLICATION_JSON);
	stringRep.setCharacterSet(CharacterSet.UTF_8);
	resource.getRequest().setEntity(stringRep);
	handleRequest(resource, null, 0);

	final int responseCode = resource.getResponse().getStatus().getCode();

	if (responseCode == 201) {
	    if (resource.getResponse().getAttributes() == null
		    || resource.getResponse().getAttributes()
			    .get(HeaderConstants.ATTRIBUTE_HEADERS) == null) {
		throw new BDRestException(
			"Could not get the response headers after creating the report.",
			resource);
	    }
	    @SuppressWarnings("unchecked")
	    final Series<Header> responseHeaders = (Series<Header>) resource
		    .getResponse().getAttributes()
		    .get(HeaderConstants.ATTRIBUTE_HEADERS);
	    final Header reportUrl = responseHeaders.getFirst("location", true);

	    if (reportUrl == null || StringUtils.isBlank(reportUrl.getValue())) {
		throw new BDRestException(
			"Could not get the report URL from the response headers.",
			resource);
	    }

	    return reportUrl.getValue();

	} else {
	    throw new BDRestException(
		    "There was a problem generating a report for this Version. Error Code: "
			    + responseCode, resource);
	}
    }

    public int deleteHubReport(final String reportUrl) throws IOException,
	    BDRestException, URISyntaxException {

	final ClientResource resource = createClientResource(reportUrl);
	resource.setMethod(Method.DELETE);
	handleRequest(resource, null, 0);

	final int responseCode = resource.getResponse().getStatus().getCode();
	if (responseCode != 204) {
	    throw new BDRestException(
		    "There was a problem deleting this report. Error Code: "
			    + responseCode, resource);
	}
	return responseCode;
    }

    public ReportInformationItem getReportInformation(final String reportUrl)
	    throws IOException, BDRestException, URISyntaxException {

	final ClientResource resource = createClientResource(reportUrl);

	@SuppressWarnings("unchecked")
	Series<Header> requestHeaders = (Series<Header>) resource
		.getRequestAttributes().get(HeaderConstants.ATTRIBUTE_HEADERS);
	if (requestHeaders == null) {
	    requestHeaders = new Series<Header>(Header.class);
	    resource.getRequestAttributes().put(
		    HeaderConstants.ATTRIBUTE_HEADERS, requestHeaders);
	}
	requestHeaders.add(new Header("Accept", MediaType.APPLICATION_JSON
		.toString()));

	// Restlet 2.3.4 and higher
	// resource.accept(MediaType.APPLICATION_JSON);

	resource.setMethod(Method.GET);

	handleRequest(resource, null, 0);
	final int responseCode = resource.getResponse().getStatus().getCode();

	if (responseCode == 200) {
	    final String response = RestletUtil.readResponseAsString(resource
		    .getResponse());

	    return new Gson().fromJson(response, ReportInformationItem.class);
	} else {
	    throw new BDRestException(
		    "There was a problem getting the links for the specified report. Error Code: "
			    + responseCode, resource);
	}

    }

    /**
     * Gets the content of the report
     *
     */
    public VersionReport getReportContent(final String reportContentUrl)
	    throws IOException, BDRestException, URISyntaxException {

	final ClientResource resource = createClientResource(reportContentUrl);

	resource.setMethod(Method.GET);

	handleRequest(resource, null, 0);
	final int responseCode = resource.getResponse().getStatus().getCode();

	if (responseCode == 200) {
	    final String response = RestletUtil.readResponseAsString(resource
		    .getResponse());

	    final Gson gson = new GsonBuilder().create();

	    final JsonObject reportResponse = gson.fromJson(response,
		    JsonObject.class);
	    final JsonArray reportConentArray = gson.fromJson(
		    reportResponse.get("reportContent"), JsonArray.class);
	    final JsonObject reportFile = (JsonObject) reportConentArray.get(0);

	    final VersionReport report = gson.fromJson(
		    reportFile.get("fileContent"), VersionReport.class);

	    return report;
	} else {
	    throw new BDRestException(
		    "There was a problem getting the content of this Report. Error Code: "
			    + responseCode, resource);
	}

    }

    /**
     * Generates a new Hub report for the specified version.
     *
     */
    public PolicyStatus getPolicyStatus(final String policyStatusUrl)
	    throws IOException, BDRestException, URISyntaxException,
	    MissingPolicyStatusException {
	if (StringUtils.isBlank(policyStatusUrl)) {
	    throw new IllegalArgumentException("Missing the policy status URL.");
	}
	final ClientResource resource = createClientResource(policyStatusUrl);

	resource.setMethod(Method.GET);

	handleRequest(resource, null, 0);

	final int responseCode = resource.getResponse().getStatus().getCode();

	if (responseCode == 200) {
	    final String response = RestletUtil.readResponseAsString(resource
		    .getResponse());

	    final Gson gson = new GsonBuilder().create();
	    final PolicyStatus status = gson.fromJson(response,
		    PolicyStatus.class);
	    return status;
	}
	if (responseCode == 404) {
	    throw new MissingPolicyStatusException(
		    "There was no policy status found for this version. The BOM may be empty.");
	} else {
	    throw new BDRestException(
		    "There was a problem getting the policy status. Error Code: "
			    + responseCode, resource);
	}
    }

    /**
     * Gets the content of the scanStatus at the provided url
     */
    public ScanStatusToPoll checkScanStatus(final String scanStatusUrl)
	    throws IOException, BDRestException, URISyntaxException {

	final ClientResource resource = createClientResource(scanStatusUrl);

	resource.setMethod(Method.GET);

	handleRequest(resource, null, 0);
	final int responseCode = resource.getResponse().getStatus().getCode();

	if (responseCode == 200) {
	    final String response = RestletUtil.readResponseAsString(resource
		    .getResponse());

	    final Gson gson = new GsonBuilder().create();

	    final ScanStatusToPoll status = gson.fromJson(response,
		    ScanStatusToPoll.class);
	    return status;
	} else {
	    throw new BDRestException(
		    "There was a problem getting the scan status. Error Code: "
			    + responseCode, resource);
	}

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
     */
    public <T> T getFromAbsoluteUrl(Class<T> modelClass, String url)
	    throws ResourceDoesNotExistException, URISyntaxException,
	    IOException {

	Reference queryRef = RestletUtil.createReference(url);
	ClientResource resource = RestletUtil.getResource(reUsableResource,
		queryRef);

	logMessage(LogLevel.DEBUG, "Resource: " + resource);
	int responseCode = RestletUtil.getResponseStatusCode(resource);
	if (RestletUtil.isSuccess(responseCode)) {
	    return RestletUtil.parseResponse(modelClass, resource);
	} else {
	    throw new ResourceDoesNotExistException(
		    "Error getting resource from " + url + ": " + responseCode
			    + "; " + resource.toString(), resource);
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
     */
    public <T> T getFromRelativeUrl(Class<T> modelClass,
	    List<String> urlSegments,
	    Set<AbstractMap.SimpleEntry<String, String>> queryParameters)
	    throws IOException, ResourceDoesNotExistException,
	    URISyntaxException {

	Reference queryRef = RestletUtil.createReference(getBaseUrl(),
		urlSegments, queryParameters);
	ClientResource resource = RestletUtil.getResource(reUsableResource,
		queryRef);

	logMessage(LogLevel.DEBUG, "Resource: " + resource);
	int responseCode = RestletUtil.getResponseStatusCode(resource);

	if (RestletUtil.isSuccess(responseCode)) {
	    return RestletUtil.parseResponse(modelClass, resource);
	} else {
	    throw new ResourceDoesNotExistException(
		    "Error getting resource from relative url segments "
			    + urlSegments + " and query parameters "
			    + queryParameters + "; errorCode: " + responseCode
			    + "; " + resource.toString(), resource);
	}
    }

    private void handleRequest(final ClientResource resource,
	    final ChallengeRequest proxyChallengeRequest, final int attempt)
	    throws BDRestException {
	if (proxyChallengeRequest != null) {
	    // This should replace the authenticator for the proxy
	    // authentication
	    // BUT it doesn't work for Digest authentication
	    parseChallengeRequestRawValue(proxyChallengeRequest);
	    resource.setProxyChallengeResponse(new ChallengeResponse(
		    proxyChallengeRequest.getScheme(), null, proxyUsername,
		    proxyPassword.toCharArray(), null, proxyChallengeRequest
			    .getRealm(), null, null, proxyChallengeRequest
			    .getDigestAlgorithm(), null, null,
		    proxyChallengeRequest.getServerNonce(), 0, 0L));
	}
	try {
	    resource.handle();
	} catch (final ResourceException e) {
	    if (resource.getProxyChallengeRequests() != null
		    && !resource.getProxyChallengeRequests().isEmpty()
		    && StringUtils.isNotBlank(proxyUsername)
		    && StringUtils.isNotBlank(proxyPassword)) {

		final ChallengeRequest newChallengeRequest = resource
			.getProxyChallengeRequests().get(0);
		if (attempt < 2) {
		    handleRequest(resource, newChallengeRequest, attempt + 1);
		} else {
		    throw new BDRestException(
			    "Too many proxy authentication attempts.", e,
			    resource);
		}
	    }
	    throw new BDRestException(
		    "Problem connecting to the Hub server provided.", e,
		    resource);
	}
    }

}
