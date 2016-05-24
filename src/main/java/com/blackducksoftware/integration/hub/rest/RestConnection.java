package com.blackducksoftware.integration.hub.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
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

import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.ResourceDoesNotExistException;
import com.blackducksoftware.integration.hub.logging.IntLogger;
import com.blackducksoftware.integration.hub.logging.LogLevel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class RestConnection {
    private final String baseUrl;
    private Series<Cookie> cookies;
    private int timeout = 120000;
    private IntLogger logger;
    private String proxyUsername;
    private String proxyPassword;
    public static final String JSON_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    public RestConnection(String baseUrl) {
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
     * Gets the cookie for the Authorized connection to the Hub server. Returns
     * the response code from the connection.
     *
     */
    public int setCookies(final String hubUserName, final String hubPassword)
	    throws URISyntaxException, BDRestException {

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
		throw new BDRestException("Could not establish connection to '"
			+ getBaseUrl() + "' . Failed to retrieve cookies",
			resource);
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
	    throw new BDRestException(resource.getResponse().getStatus()
		    .toString(), resource);
	}

	return resource.getResponse().getStatus().getCode();
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
     */
    public <T> T getFromAbsoluteUrl(Class<T> modelClass, String url)
	    throws ResourceDoesNotExistException, URISyntaxException,
	    IOException {

	ClientResource resource = getResource(url);

	logMessage(LogLevel.DEBUG, "Resource: " + resource);
	int responseCode = getResponseStatusCode(resource);
	if (isSuccess(responseCode)) {
	    return parseResponse(modelClass, resource);
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

	ClientResource resource = getResource(getBaseUrl(), urlSegments,
		queryParameters);

	logMessage(LogLevel.DEBUG, "Resource: " + resource);
	int responseCode = getResponseStatusCode(resource);

	if (isSuccess(responseCode)) {
	    return parseResponse(modelClass, resource);
	} else {
	    throw new ResourceDoesNotExistException(
		    "Error getting resource from relative url segments "
			    + urlSegments + " and query parameters "
			    + queryParameters + "; errorCode: " + responseCode
			    + "; " + resource.toString(), resource);
	}
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

    public void handleRequest(final ClientResource resource,
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

    public static int getResponseStatusCode(ClientResource resource) {
	return resource.getResponse().getStatus().getCode();
    }

    /**
     * This method exists for code symmetry with the other createReference()
     * method.
     */
    // TODO get rid of this
    static Reference createReference(String url) {
	return new Reference(url);
    }

    // TODO get rid of this
    public static Reference createReference(String baseUrl,
	    List<String> urlSegments,
	    Set<AbstractMap.SimpleEntry<String, String>> queryParameters) {
	Reference queryRef = new Reference(baseUrl);
	for (String urlSegment : urlSegments) {
	    queryRef.addSegment(urlSegment);
	}
	for (AbstractMap.SimpleEntry<String, String> queryParameter : queryParameters) {
	    queryRef.addQueryParameter(queryParameter.getKey(),
		    queryParameter.getValue());
	}
	return queryRef;
    }

    public static boolean isSuccess(int responseCode) {
	return responseCode == 200 || responseCode == 204
		|| responseCode == 202;
    }

    static <T> T parseResponse(Class<T> modelClass, ClientResource resource)
	    throws IOException {
	final String response = readResponseAsString(resource.getResponse());
	Gson gson = new GsonBuilder().setDateFormat(JSON_DATE_FORMAT).create();
	JsonParser parser = new JsonParser();
	JsonObject json = parser.parse(response).getAsJsonObject();
	T modelObject = gson.fromJson(json, modelClass);
	return modelObject;
    }

    public static String readResponseAsString(final Response response)
	    throws IOException {
	final StringBuilder sb = new StringBuilder();
	final Reader reader = response.getEntity().getReader();
	final BufferedReader bufReader = new BufferedReader(reader);
	try {
	    String line;
	    while ((line = bufReader.readLine()) != null) {
		sb.append(line);
		sb.append("\n");
	    }
	} finally {
	    bufReader.close();
	}
	return sb.toString();
    }

    public ClientResource getResource(String url) throws URISyntaxException {
	// TODO does this overlap with a legacy method??
	ClientResource resource = createClientResource();
	Reference reference = new Reference(url);
	return getResource(resource, reference);
    }

    public ClientResource getResource(String baseUrl, List<String> urlSegments,
	    Set<AbstractMap.SimpleEntry<String, String>> queryParameters)
	    throws URISyntaxException {
	ClientResource resource = createClientResource();
	Reference reference = createReference(baseUrl, urlSegments,
		queryParameters);
	return getResource(resource, reference);
    }

    public static ClientResource getResource(ClientResource resource,
	    Reference reference) throws URISyntaxException {
	resource.setMethod(Method.GET);
	resource.setReference(reference);
	resource.handle();
	return resource;
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
}
