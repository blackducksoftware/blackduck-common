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

import java.net.CookieHandler;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.restlet.engine.header.HeaderConstants;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.restlet.util.Series;

import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.global.HubProxyInfo;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.log.LogLevel;
import com.blackducksoftware.integration.util.AuthenticatorUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.OkHttpClient;

/**
 * Manages the low-level details of communicating with the server via REST.
 *
 * @author sbillings
 *
 */
public abstract class OkRestConnection {
    public static final String JSON_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSX";

    private URL baseUrl;

    private int timeout = 120;

    private HubProxyInfo hubProxyInfo;

    private IntLogger logger;

    private final Gson gson = new GsonBuilder().setDateFormat(JSON_DATE_FORMAT).create();

    private final JsonParser jsonParser = new JsonParser();

    private final OkHttpClient.Builder builder = new OkHttpClient.Builder();

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

    public OkRestConnection() {
        this(null);
    }

    public OkRestConnection(final IntLogger logger) {
        if (logger != null) {
            setLogger(logger);
        }
        setTimeout(timeout); // just in case setTimeout() is never called
    }

    public IntLogger getLogger() {
        return logger;
    }

    public void setLogger(final IntLogger logger) {
        this.logger = logger;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(final int timeout) {
        if (timeout <= 0) {
            throw new IllegalArgumentException("Timeout must be greater than zero.");
        }
        this.timeout = timeout;
        logMessage(LogLevel.DEBUG, "Setting connectTimeout to: " + timeout + "s on client context");
    }

    public URL getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(final URL baseUrl) {
        this.baseUrl = baseUrl;
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
     * @throws HubIntegrationException
     */
    public <T> T httpGetFromAbsoluteUrl(final Class<T> modelClass, final String url)
            throws HubIntegrationException {
        final ClientResource resource = createClientResource(url);
        try {
            resource.setMethod(Method.GET);
            handleRequest(resource);

            logMessage(LogLevel.DEBUG, "Resource: " + resource);
            final int responseCode = getResponseStatusCode(resource);
            if (isSuccess(responseCode)) {
                return parseResponse(modelClass, resource);
            } else {
                throw new HubIntegrationException(
                        "Error getting resource from " + url + ": " + responseCode + "; " + resource.toString());
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
     * @throws HubIntegrationException
     */
    public <T> T httpGetFromRelativeUrl(final Class<T> modelClass, final List<String> urlSegments,
            final Set<AbstractMap.SimpleEntry<String, String>> queryParameters)
            throws HubIntegrationException {

        final ClientResource resource = createClientResource(urlSegments, queryParameters);
        try {
            resource.setMethod(Method.GET);
            handleRequest(resource);

            logMessage(LogLevel.DEBUG, "Resource: " + resource);
            final int responseCode = getResponseStatusCode(resource);

            if (isSuccess(responseCode)) {
                return parseResponse(modelClass, resource);
            } else {
                throw new HubIntegrationException(
                        "Error getting resource from relative url segments " + urlSegments + " and query parameters "
                                + queryParameters + "; errorCode: " + responseCode + "; " + resource.toString());
            }
        } finally {
            releaseResource(resource);
        }
    }

    public abstract void connect() throws HubIntegrationException;

    public OkHttpClient createClient() throws HubIntegrationException {
        return createClient(getBaseUrl());
    }

    public OkHttpClient createClient(final URL providedUrl) throws HubIntegrationException {
        builder.connectTimeout(timeout, TimeUnit.SECONDS);
        builder.writeTimeout(timeout, TimeUnit.SECONDS);
        builder.readTimeout(timeout, TimeUnit.SECONDS);
        builder.proxy(getHubProxyInfo().getProxy(providedUrl));

        return null;
    }

    public OkHttpClient createClient(final List<String> urlSegments,
            final Set<AbstractMap.SimpleEntry<String, String>> queryParameters) throws HubIntegrationException {
        final ClientResource resource = createClient();

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

    public void handleRequest(final ClientResource resource) throws HubIntegrationException {
        final boolean debugLogging = isDebugLogging();
        if (debugLogging) {
            logMessage(LogLevel.TRACE, "Resource : " + resource.toString());
            logRestletRequestOrResponse(resource.getRequest());
        }

        final CookieHandler originalCookieHandler = CookieHandler.getDefault();
        try {
            if (originalCookieHandler != null) {
                if (debugLogging) {
                    logMessage(LogLevel.TRACE, "Setting Cookie Handler to NULL");
                }
                CookieHandler.setDefault(null);
            }
            resource.handle();
        } catch (final ResourceException e) {
            throw new HubIntegrationException("Problem connecting to the Hub server provided.", e);
        } finally {
            if (originalCookieHandler != null) {
                if (debugLogging) {
                    logMessage(LogLevel.TRACE, "Setting Original Cookie Handler : " + originalCookieHandler.toString());
                }
                CookieHandler.setDefault(originalCookieHandler);
            }
        }

        if (debugLogging) {
            logRestletRequestOrResponse(resource.getResponse());
            logMessage(LogLevel.TRACE, "Status Code : " + resource.getResponse().getStatus().getCode());
        }
    }

    private boolean isDebugLogging() {
        return logger != null && logger.getLogLevel() == LogLevel.TRACE;
    }

    public String readResponseAsString(final Response response) {
        return response.getEntityAsText();
    }

    private void logRestletRequestOrResponse(final Message requestOrResponse) {
        if (isDebugLogging()) {
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
                final Series<? extends NamedValue> responseheaders = (Series<? extends NamedValue>) requestOrResponse
                        .getAttributes().get(HeaderConstants.ATTRIBUTE_HEADERS);
                if (responseheaders != null) {
                    logMessage(LogLevel.TRACE, requestOrResponseName + " headers : ");
                    for (final NamedValue header : responseheaders) {
                        if (header == null) {
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

    private <T> T parseResponse(final Class<T> modelClass, final ClientResource resource) {
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
            throws HubIntegrationException {

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
            throws HubIntegrationException {
        final Set<SimpleEntry<String, String>> queryParameters = new HashSet<>();
        return httpPostFromRelativeUrl(urlSegments, queryParameters, content);
    }

    public String httpPostFromRelativeUrl(final List<String> urlSegments,
            final Set<AbstractMap.SimpleEntry<String, String>> queryParameters, final Representation content)
            throws HubIntegrationException {

        final ClientResource resource = createClientResource(urlSegments, queryParameters);
        try {
            resource.setMethod(Method.POST);
            resource.getRequest().setEntity(content);
            return handleHttpPost(resource);
        } finally {
            releaseResource(resource);
        }
    }

    public String handleHttpPost(final ClientResource resource)
            throws HubIntegrationException {
        handleRequest(resource);

        logMessage(LogLevel.DEBUG, "Resource: " + resource);
        final int responseCode = getResponseStatusCode(resource);

        if (isSuccess(responseCode)) {
            if (resource.getResponse().getAttributes() == null
                    || resource.getResponse().getAttributes().get(HeaderConstants.ATTRIBUTE_HEADERS) == null) {
                throw new HubIntegrationException(
                        "Could not get the response headers after creating the resource.");
            }

            if (responseCode != 201) {
                return "";
            } else {
                @SuppressWarnings("unchecked")
                final Series<? extends NamedValue> responseHeaders = (Series<? extends NamedValue>) resource
                        .getResponse().getAttributes().get(HeaderConstants.ATTRIBUTE_HEADERS);
                final NamedValue resourceUrl = responseHeaders.getFirst("location", true);
                if (resourceUrl == null) {
                    throw new HubIntegrationException("Could not get the resource URL from the response headers.");
                }
                final String value = (String) resourceUrl.getValue();
                return value;
            }
        } else {
            throw new HubIntegrationException(
                    "There was a problem creating the resource. Error Code: " + responseCode);
        }
    }

    protected OkHttpClient.Builder getBuilder() {
        return builder;
    }

    public Gson getGson() {
        return gson;
    }

    public JsonParser getJsonParser() {
        return jsonParser;
    }

    public HubProxyInfo getHubProxyInfo() {
        return hubProxyInfo;
    }

    public void setHubProxyInfo(HubProxyInfo hubProxyInfo) {
        this.hubProxyInfo = hubProxyInfo;
    }

}
