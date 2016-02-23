package com.blackducksoftware.integration.hub;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.restlet.Context;
import org.restlet.Response;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.Cookie;
import org.restlet.data.CookieSetting;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.engine.header.Header;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.restlet.util.Series;

import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.exception.ProjectDoesNotExistException;
import com.blackducksoftware.integration.hub.report.api.VersionReport;
import com.blackducksoftware.integration.hub.response.AutoCompleteItem;
import com.blackducksoftware.integration.hub.response.ProjectItem;
import com.blackducksoftware.integration.hub.response.ReleaseItem;
import com.blackducksoftware.integration.hub.response.ReportFormatEnum;
import com.blackducksoftware.integration.hub.response.ReportMetaInformationItem;
import com.blackducksoftware.integration.hub.response.VersionComparison;
import com.blackducksoftware.integration.hub.response.mapping.AssetReferenceItem;
import com.blackducksoftware.integration.hub.response.mapping.EntityItem;
import com.blackducksoftware.integration.hub.response.mapping.EntityTypeEnum;
import com.blackducksoftware.integration.hub.response.mapping.ScanHistoryItem;
import com.blackducksoftware.integration.hub.response.mapping.ScanLocationItem;
import com.blackducksoftware.integration.hub.response.mapping.ScanLocationResults;
import com.blackducksoftware.integration.hub.response.mapping.ScanStatus;
import com.blackducksoftware.integration.suite.sdk.logging.IntLogger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

public class HubIntRestService {
    private Series<Cookie> cookies;

    private final String baseUrl;

    private int timeout = 120000;

    private IntLogger logger;

    private String proxyUsername;

    private String proxyPassword;

    public HubIntRestService(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void setTimeout(int timeout) {
        if (timeout == 0) {
            throw new IllegalArgumentException("Can not set the timeout to zero.");
        }
        // the User sets the timeout in seconds, so we translate to ms
        this.timeout = timeout * 1000;
    }

    public void setLogger(IntLogger logger) {
        this.logger = logger;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    private void attemptResetProxyCache() {
        try {
            // works, and resets the cache when using sun classes
            // sun.net.www.protocol.http.AuthCacheValue.setAuthCache(new
            // sun.net.www.protocol.http.AuthCacheImpl());

            // Attempt the same thing using reflection in case they are not using a jdk with sun classes

            Class<?> sunAuthCacheValue;
            Class<?> sunAuthCache;
            Class<?> sunAuthCacheImpl;
            try {
                sunAuthCacheValue = Class.forName("sun.net.www.protocol.http.AuthCacheValue");
                sunAuthCache = Class.forName("sun.net.www.protocol.http.AuthCache");
                sunAuthCacheImpl = Class.forName("sun.net.www.protocol.http.AuthCacheImpl");
            } catch (Exception e) {
                // Must not be using a JDK with sun classes so we abandon this reset since it is sun specific
                return;
            }

            java.lang.reflect.Method m = sunAuthCacheValue.getDeclaredMethod("setAuthCache", sunAuthCache);

            Constructor<?> authCacheImplConstr = sunAuthCacheImpl.getConstructor();
            Object authCachImp = authCacheImplConstr.newInstance();

            m.invoke(null, authCachImp);

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * Sets the Proxy settings that the User may have configured.
     * The proxy settings get set as System properties.
     * I.E. https.proxyHost, https.proxyPort, http.proxyHost, http.proxyPort, http.nonProxyHosts
     *
     */
    public void setProxyProperties(final String proxyHost, final int proxyPort, final List<Pattern> noProxyHosts, final String proxyUsername,
            final String proxyPassword) {

        cleanUpOldProxySettings();

        if (!StringUtils.isBlank(proxyHost) && proxyPort > 0) {
            if (logger != null) {
                logger.debug("Using Proxy : " + proxyHost + ", at Port : " + proxyPort);
            }

            System.setProperty("https.proxyHost", proxyHost);
            System.setProperty("https.proxyPort", Integer.toString(proxyPort));
            System.setProperty("http.proxyHost", proxyHost);
            System.setProperty("http.proxyPort", Integer.toString(proxyPort));

            if (!StringUtils.isBlank(proxyUsername) && !StringUtils.isBlank(proxyPassword)) {
                this.proxyUsername = proxyUsername;
                this.proxyPassword = proxyPassword;

                // Java ignores http.proxyUser. Here's the workaround.
                Authenticator.setDefault(new Authenticator() {
                    // Need this to support digest authentication
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        if (getRequestorType() == RequestorType.PROXY) {
                            return new PasswordAuthentication(proxyUsername, proxyPassword.toCharArray());
                        }
                        return null;
                    }
                });
            }
        }
        if (noProxyHosts != null && !noProxyHosts.isEmpty()) {
            String noProxyHostsString = null;
            for (Pattern pattern : noProxyHosts) {
                if (noProxyHostsString == null) {
                    noProxyHostsString = pattern.toString();
                } else {
                    noProxyHostsString = noProxyHostsString + "|" + pattern.toString();
                }
            }
            if (!StringUtils.isBlank(noProxyHostsString)) {
                System.setProperty("http.nonProxyHosts", noProxyHostsString);
            }
        }
    }

    /**
     * Create the Client Resource
     *
     * @param url
     *            String
     * @return ClientResource
     * @throws URISyntaxException
     * @throws MalformedURLException
     */
    // Make this protected for testing the getCodeLocations, otherwise we need to use
    // Powermockito to stub this to use a mock resource OR we would need to setup code
    // locations on the server and hope they dont get deleted
    protected ClientResource createClientResource() throws URISyntaxException {
        return createClientResource(getBaseUrl());
    }

    /**
     * Create the Client Resource
     *
     * @param url
     *            String
     * @return ClientResource
     * @throws URISyntaxException
     * @throws MalformedURLException
     */
    private ClientResource createClientResource(String providedUrl) throws URISyntaxException {

        Context context = new Context();

        // the socketTimeout parameter is used in the httpClient extension that we do not use
        // We can probably remove this parameter
        String stringTimeout = String.valueOf(timeout);

        context.getParameters().add("socketTimeout", stringTimeout);

        context.getParameters().add("socketConnectTimeoutMs", stringTimeout);
        context.getParameters().add("readTimeout", stringTimeout);
        // Should throw timeout exception after the specified timeout, default is 2 minutes

        ClientResource resource = new ClientResource(context, new URI(providedUrl));
        resource.getRequest().setCookies(getCookies());
        return resource;
    }

    /**
     * Clears the previously set System properties
     * I.E. https.proxyHost, https.proxyPort, http.proxyHost, http.proxyPort, http.nonProxyHosts
     *
     */
    @SuppressWarnings("restriction")
    private void cleanUpOldProxySettings() {
        System.clearProperty("https.proxyHost");
        System.clearProperty("https.proxyPort");
        System.clearProperty("http.proxyHost");
        System.clearProperty("http.proxyPort");
        System.clearProperty("http.nonProxyHosts");

        attemptResetProxyCache();

        Authenticator.setDefault(null);
    }

    public void parseChallengeRequestRawValue(ChallengeRequest proxyChallengeRequest) {
        if (proxyChallengeRequest == null || StringUtils.isBlank(proxyChallengeRequest.getRawValue())) {
            return;
        }
        String rawValue = proxyChallengeRequest.getRawValue();

        String[] splitRawValue = rawValue.split(",");
        for (String currentValue : splitRawValue) {
            currentValue = currentValue.trim();
            if (StringUtils.isBlank(proxyChallengeRequest.getRealm()) && currentValue.startsWith("realm=")) {
                String realm = currentValue.substring("realm=".length());
                proxyChallengeRequest.setRealm(realm);
            } else if (StringUtils.isBlank(proxyChallengeRequest.getServerNonce()) && currentValue.startsWith("nonce=")) {
                String nonce = currentValue.substring("nonce=".length());
                proxyChallengeRequest.setServerNonce(nonce);
            } else if ((proxyChallengeRequest.getQualityOptions() == null || proxyChallengeRequest.getQualityOptions().isEmpty())
                    && currentValue.startsWith("qop=")) {
                String qop = currentValue.substring("qop=".length());
                List<String> qualityOptions = new ArrayList<String>();
                qualityOptions.add(qop);
                proxyChallengeRequest.setQualityOptions(qualityOptions);
            } else if (currentValue.startsWith("stale=")) {
                String stale = currentValue.substring("stale=".length());
                proxyChallengeRequest.setStale(Boolean.valueOf(stale));
            }
        }
    }

    /**
     * Gets the cookie for the Authorized connection to the Hub server. Returns the response code from the connection.
     *
     * @param hubUserName
     *            String the Username for the Hub server
     * @param hubPassword
     *            String the Password for the Hub server
     *
     * @return int Status code
     * @throws MalformedURLException
     * @throws HubIntegrationException
     * @throws URISyntaxException
     * @throws BDRestException
     */
    public int setCookies(String hubUserName, String hubPassword) throws HubIntegrationException,
            URISyntaxException, BDRestException {

        ClientResource resource = createClientResource();
        resource.addSegment("j_spring_security_check");
        resource.addQueryParameter("j_username", hubUserName);
        resource.addQueryParameter("j_password", hubPassword);

        resource.setMethod(Method.POST);

        EmptyRepresentation rep = new EmptyRepresentation();
        resource.getRequest().setEntity(rep);
        handleRequest(resource, null, 0);
        if (cookies == null) {
            Series<CookieSetting> cookieSettings = resource.getResponse().getCookieSettings();
            if (cookieSettings == null || cookieSettings.size() == 0) {
                throw new HubIntegrationException("Could not establish connection to '" + getBaseUrl() + "' . Failed to retrieve cookies");
            }

            Series<Cookie> requestCookies = resource.getRequest().getCookies();
            for (CookieSetting ck : cookieSettings) {
                Cookie cookie = new Cookie();
                cookie.setName(ck.getName());
                cookie.setDomain(ck.getDomain());
                cookie.setPath(ck.getPath());
                cookie.setValue(ck.getValue());
                cookie.setVersion(ck.getVersion());
                requestCookies.add(cookie);
            }

            cookies = requestCookies;
        }
        // else {
        // cookies already set
        // }

        return resource.getResponse().getStatus().getCode();
    }

    public Series<Cookie> getCookies() {
        return cookies;
    }

    /**
     * Retrieves a list of Hub Projects that may match the hubProjectName
     *
     * @param hubProjectName
     *            String
     *
     * @return List<<AutoCompleteItem>>
     * @throws IOException
     * @throws BDRestException
     * @throws URISyntaxException
     */
    public List<AutoCompleteItem> getProjectMatches(String hubProjectName) throws IOException,
            BDRestException, URISyntaxException {
        ClientResource resource = createClientResource();
        resource.addSegment("api");
        resource.addSegment("v1");
        resource.addSegment("autocomplete");
        resource.addSegment("PROJECT");
        resource.addQueryParameter("text", hubProjectName);
        resource.addQueryParameter("limit", "30");
        resource.addQueryParameter("ownership", "0");

        resource.setMethod(Method.GET);
        handleRequest(resource, null, 0);
        int responseCode = resource.getResponse().getStatus().getCode();

        if (responseCode == 200 || responseCode == 204 || responseCode == 202) {
            String response = readResponseAsString(resource.getResponse());

            Gson gson = new GsonBuilder().create();
            return gson.fromJson(response, new TypeToken<List<AutoCompleteItem>>() {
            }.getType());

        } else {
            throw new BDRestException("There was a problem getting the project matches. Error Code: " + responseCode, resource);
        }

    }

    /**
     * Gets the Hub Project that is specified by the projectId
     *
     * @param projectId
     *            String
     * @return ProjectItem
     * @throws IOException
     * @throws BDRestException
     * @throws URISyntaxException
     */
    public ProjectItem getProjectById(String projectId) throws IOException,
            BDRestException, URISyntaxException {
        ClientResource resource = createClientResource();
        resource.addSegment("api");
        resource.addSegment("v1");
        resource.addSegment("projects");
        resource.addSegment(projectId);

        resource.setMethod(Method.GET);
        handleRequest(resource, null, 0);
        int responseCode = resource.getResponse().getStatus().getCode();

        if (responseCode == 200 || responseCode == 204 || responseCode == 202) {
            String response = readResponseAsString(resource.getResponse());
            // logger.info(response);
            Gson gson = new GsonBuilder().create();
            return gson.fromJson(response, ProjectItem.class);

        } else {
            throw new BDRestException("There was a problem getting the project for this Id. Error Code: " + responseCode, resource);
        }
    }

    /**
     * Gets the Project that is specified by the projectName
     *
     * @param projectName
     *            String
     *
     * @return
     * @throws IOException
     * @throws BDRestException
     * @throws URISyntaxException
     * @throws ProjectDoesNotExistException
     */
    public ProjectItem getProjectByName(String projectName) throws IOException, BDRestException,
            URISyntaxException, ProjectDoesNotExistException {
        ClientResource resource = createClientResource();
        resource.addSegment("api");
        resource.addSegment("v1");
        resource.addSegment("projects");
        resource.addQueryParameter("name", projectName);
        resource.setMethod(Method.GET);
        handleRequest(resource, null, 0);
        int responseCode = resource.getResponse().getStatus().getCode();

        if (responseCode == 200 || responseCode == 204 || responseCode == 202) {
            String response = readResponseAsString(resource.getResponse());
            Gson gson = new GsonBuilder().create();
            return gson.fromJson(response, ProjectItem.class);

        } else if (responseCode == 404) {
            throw new ProjectDoesNotExistException("This Project does not exist.", resource);
        } else {
            throw new BDRestException("There was a problem getting a Project by this name.", resource);
        }
    }

    /**
     * Gets the scan Id for each scan target, it searches the list of scans and gets the latest scan Id for the scan
     * matching the hostname and path.
     * Returns a Map of scan Id's, and a Boolean to mark if that has already been mapped to this version or not
     *
     * @param hostname
     *            String
     * @param scanTargets
     *            List<<String>>
     * @param versionId
     *            String
     *
     * @return Map<<String, Boolean>>
     * @throws UnknownHostException
     * @throws InterruptedException
     * @throws BDRestException
     * @throws HubIntegrationException
     * @throws URISyntaxException
     * @throws MalformedURLException
     */
    public Map<String, Boolean> getScanLocationIds(String hostname, List<String>
            scanTargets, String versionId)
            throws UnknownHostException,
            InterruptedException, BDRestException, HubIntegrationException, URISyntaxException {
        return getScanLocationIds(hostname, scanTargets, versionId, null, 0);
    }

    /**
     * Gets the scan Id for each scan target, it searches the list of scans and gets the latest scan Id for the scan
     * matching the hostname and path.
     * Returns a Map of scan Id's, and a Boolean to mark if that has already been mapped to this version or not
     *
     * @param hostname
     *            String
     * @param scanTargets
     *            List<<String>>
     * @param versionId
     *            String
     * @param proxyChallengeRequest
     *            ChallengeRequest proxyChallenge to get the correct authentication
     * @param attempt
     *            Integer authentication attempt number
     *
     * @return Map<<String, Boolean>>
     * @throws UnknownHostException
     * @throws InterruptedException
     * @throws BDRestException
     * @throws HubIntegrationException
     * @throws URISyntaxException
     * @throws MalformedURLException
     */
    private Map<String, Boolean> getScanLocationIds(String hostname, List<String>
            scanTargets, String versionId, ChallengeRequest proxyChallengeRequest, int attempt)
            throws UnknownHostException,
            InterruptedException, BDRestException, HubIntegrationException, URISyntaxException {
        HashMap<String, Boolean> scanLocationIds = new HashMap<String, Boolean>();
        ClientResource resource = null;
        try {
            for (String targetPath : scanTargets) {
                // Scan paths in the Hub only use '/' not '\'
                if (targetPath.contains("\\")) {
                    targetPath = targetPath.replace("\\", "/");
                }
                // and it always starts with a '/'
                if (!targetPath.startsWith("/")) {
                    targetPath = "/" + targetPath;
                }

                logger.debug(
                        "Checking for the scan location with Host name: '" + hostname + "' and Path: '" + targetPath +
                                "'");

                resource = createClientResource();
                resource.addSegment("api");
                resource.addSegment("v1");
                resource.addSegment("scanlocations");
                resource.addQueryParameter("host", hostname);
                resource.addQueryParameter("path", targetPath);

                if (proxyChallengeRequest != null) {
                    // This should replace the authenticator for the proxy authentication
                    // BUT it doesn't work for Digest authentication
                    parseChallengeRequestRawValue(proxyChallengeRequest);
                    resource.setProxyChallengeResponse(new ChallengeResponse(proxyChallengeRequest.getScheme(), null,
                            proxyUsername, proxyPassword.toCharArray(), null, proxyChallengeRequest.getRealm(), null,
                            null, proxyChallengeRequest.getDigestAlgorithm(), null, null, proxyChallengeRequest.getServerNonce(),
                            0, 0L));
                }

                resource.setMethod(Method.GET);

                ScanLocationHandler handler = new ScanLocationHandler(logger);

                handler.getScanLocationIdWithRetry(resource, targetPath, versionId, scanLocationIds);

            }
        } catch (ResourceException e) {
            if (!resource.getProxyChallengeRequests().isEmpty() && StringUtils.isNotBlank(proxyUsername) && StringUtils.isNotBlank(proxyPassword)) {

                ChallengeRequest newChallengeRequest = resource.getProxyChallengeRequests().get(0);
                if (attempt < 2) {
                    return getScanLocationIds(hostname, scanTargets, versionId, newChallengeRequest, attempt + 1);
                } else {
                    throw new BDRestException("Too many proxy authentication attempts.", e, resource);
                }
            }
            throw new BDRestException("There was a problem getting the scan locations.", e, resource);
        }
        return scanLocationIds;
    }

    /**
     * If the scan Id has not already been mapped to the Version then it will make that mapping, otherwise it will not
     * perform the mapping.
     *
     * @param scanLocationIds
     *            Map<<String, Boolean>>
     * @param versionId
     *            String
     *
     * @throws BDRestException
     * @throws URISyntaxException
     * @throws MalformedURLException
     */
    public void mapScansToProjectVersion(Map<String, Boolean> scanLocationIds, String
            versionId) throws BDRestException, URISyntaxException {
        ClientResource resource = createClientResource();
        resource.addSegment("api");
        resource.addSegment("v1");
        resource.addSegment("assetreferences");
        if (!scanLocationIds.isEmpty()) {
            for (Entry<String, Boolean> scanId : scanLocationIds.entrySet()) {
                if (!scanId.getValue()) {
                    // This scan location has not yet been mapped to the project/version
                    logger.debug(
                            "Mapping the scan location with id: '" + scanId.getKey() + "', to the Version with Id: '" + versionId +
                                    "'.");

                    AssetReferenceItem assetReference = new AssetReferenceItem();

                    EntityItem ownerEntity = new EntityItem();
                    ownerEntity.setEntityType(EntityTypeEnum.RL.name());
                    ownerEntity.setEntityId(versionId);

                    EntityItem assetEntity = new EntityItem();

                    assetEntity.setEntityType(EntityTypeEnum.CL.name());
                    assetEntity.setEntityId(scanId.getKey());

                    assetReference.setOwnerEntityKey(ownerEntity);
                    assetReference.setAssetEntityKey(assetEntity);

                    Gson gson = new GsonBuilder().create();

                    logger.debug("Asset reference mapping object : " + gson.toJson(assetReference));
                    StringRepresentation stringRep = new StringRepresentation(gson.toJson(assetReference));
                    stringRep.setMediaType(MediaType.APPLICATION_JSON);

                    resource.setMethod(Method.POST);
                    handleRequest(resource, null, 0);
                    int responseCode = resource.getResponse().getStatus().getCode();

                    // HashMap<String, Object> responseMap = new HashMap<String, Object>();
                    if (responseCode == 201) {
                        // Successful mapping
                        logger.debug(
                                "Successfully mapped the scan with id: '" + scanId.getKey() + "', to the Version with Id: '" + versionId
                                        + "'.");
                    } else {
                        throw new BDRestException("There was a problem mapping the scan location to the specified version. Error Code: " + responseCode,
                                resource);
                    }
                } else {
                    logger.debug(
                            "The scan location with id: '" + scanId.getKey() + "', is already mapped to the Version with Id: '" +
                                    versionId + "'.");
                }
            }
        }
        else {
            logger.debug("Could not find any scan Id's to map to the Version.");
        }
    }

    /**
     * Gets the list of Versions for the specified Project
     *
     * @param projectId
     *            String
     *
     *
     * @return List<<ReleaseItem>>
     * @throws IOException
     * @throws BDRestException
     * @throws URISyntaxException
     */
    public List<ReleaseItem> getVersionsForProject(String projectId) throws IOException,
            BDRestException, URISyntaxException {
        ClientResource resource = createClientResource();
        resource.addSegment("api");
        resource.addSegment("v1");
        resource.addSegment("projects");
        resource.addSegment(projectId);
        resource.addSegment("releases");

        resource.setMethod(Method.GET);
        handleRequest(resource, null, 0);
        int responseCode = resource.getResponse().getStatus().getCode();

        if (responseCode == 200 || responseCode == 204 || responseCode == 202) {
            String response = readResponseAsString(resource.getResponse());
            Gson gson = new GsonBuilder().create();
            JsonObject releaseListJsonObj = gson.fromJson(response, JsonObject.class);

            Type listType = new TypeToken<ArrayList<ReleaseItem>>() {
            }.getType();

            List<ReleaseItem> releasesList = gson.fromJson(releaseListJsonObj.get("items"), listType);

            return releasesList;

        } else {
            throw new BDRestException("There was a problem getting the versions for this Project. Error Code: " + responseCode, resource);
        }
    }

    /**
     * Creates a Hub Project with the specified name.
     *
     * @param projectName
     *            String
     *
     * @return (String) ProjectId
     * @throws IOException
     * @throws BDRestException
     * @throws URISyntaxException
     */
    public String createHubProject(String projectName) throws IOException, BDRestException,
            URISyntaxException {
        ClientResource resource = createClientResource();
        resource.addSegment("api");
        resource.addSegment("v1");
        resource.addSegment("projects");

        resource.setMethod(Method.POST);

        ProjectItem newProject = new ProjectItem();
        newProject.setName(projectName);

        Gson gson = new GsonBuilder().create();
        StringRepresentation stringRep = new StringRepresentation(gson.toJson(newProject));
        stringRep.setMediaType(MediaType.APPLICATION_JSON);

        resource.getRequest().setEntity(stringRep);
        handleRequest(resource, null, 0);
        int responseCode = resource.getResponse().getStatus().getCode();

        if (responseCode == 201) {

            String response = readResponseAsString(resource.getResponse());
            ProjectItem project = gson.fromJson(response, ProjectItem.class);
            return project.getId();

        } else {

            throw new BDRestException("There was a problem creating this Hub Project. Error Code: " + responseCode, resource);
        }

    }

    /**
     * Creates a new Version in the Project specified, using the phase and distribution provided
     *
     * @param projectVersion
     *            String
     * @param projectId
     *            String
     * @param phase
     *            String
     * @param dist
     *            String
     *
     * @return (String) VersionId
     * @throws IOException
     * @throws BDRestException
     * @throws URISyntaxException
     */
    public String createHubVersion(String projectVersion, String projectId, String phase, String dist)
            throws IOException, BDRestException, URISyntaxException {
        ClientResource resource = createClientResource();
        resource.addSegment("api");
        resource.addSegment("v1");
        resource.addSegment("releases");

        int responseCode;
        ReleaseItem newRelease = new ReleaseItem();
        newRelease.setProjectId(projectId);
        newRelease.setVersion(projectVersion);
        newRelease.setPhase(phase);
        newRelease.setDistribution(dist);

        resource.setMethod(Method.POST);

        Gson gson = new GsonBuilder().create();
        StringRepresentation stringRep = new StringRepresentation(gson.toJson(newRelease));
        stringRep.setMediaType(MediaType.APPLICATION_JSON);

        resource.getRequest().setEntity(stringRep);
        handleRequest(resource, null, 0);
        responseCode = resource.getResponse().getStatus().getCode();

        if (responseCode == 201) {

            String response = readResponseAsString(resource.getResponse());
            ReleaseItem release = gson.fromJson(response, ReleaseItem.class);
            return release.getId();
        } else {
            throw new BDRestException("There was a problem creating this Version for the specified Hub Project. Error Code: " + responseCode, resource);
        }

    }

    /**
     * Creates a Hub Project and version with the specified information.
     *
     * @param projectName
     *            String
     * @param versionName
     *            String
     * @param phase
     *            String
     * @param dist
     *            String
     *
     * @return (String) ProjectId
     * @throws IOException
     * @throws BDRestException
     * @throws URISyntaxException
     */
    public String createHubProjectAndVersion(String projectName, String versionName, String phase, String dist) throws IOException, BDRestException,
            URISyntaxException {
        ClientResource resource = createClientResource();
        resource.addSegment("api");
        resource.addSegment("v1");
        resource.addSegment("projects");

        ReleaseItem newRelease = new ReleaseItem();
        newRelease.setVersion(versionName);
        newRelease.setPhase(phase);
        newRelease.setDistribution(dist);

        resource.setMethod(Method.POST);

        ProjectItem newProject = new ProjectItem();
        newProject.setName(projectName);
        newProject.setReleaseItem(newRelease);

        Gson gson = new GsonBuilder().create();
        StringRepresentation stringRep = new StringRepresentation(gson.toJson(newProject));
        stringRep.setMediaType(MediaType.APPLICATION_JSON);
        resource.getRequest().setEntity(stringRep);
        handleRequest(resource, null, 0);
        int responseCode = resource.getResponse().getStatus().getCode();

        if (responseCode == 201) {

            String response = readResponseAsString(resource.getResponse());
            ProjectItem project = gson.fromJson(response, ProjectItem.class);
            return project.getId();

        } else {

            throw new BDRestException("There was a problem creating the specified Project and Version. Error Code: " + responseCode, resource);
        }

    }

    /**
     * Retrieves the version of the Hub server
     *
     * @return String
     * @throws IOException
     * @throws BDRestException
     * @throws URISyntaxException
     */
    public String getHubVersion() throws IOException, BDRestException, URISyntaxException {
        ClientResource resource = createClientResource();
        resource.addSegment("api");
        resource.addSegment("v1");
        resource.addSegment("current-version");

        int responseCode = 0;

        resource.setMethod(Method.GET);
        handleRequest(resource, null, 0);
        responseCode = resource.getResponse().getStatus().getCode();

        if (responseCode == 200 || responseCode == 204 || responseCode == 202) {
            Response resp = resource.getResponse();
            return resp.getEntityAsText();
        } else {
            throw new BDRestException("There was a problem getting the version of the Hub server. Error Code: " + responseCode, resource);
        }
    }

    /**
     * Compares the specified version with the actual version of the Hub server.
     *
     * @param version
     *            String
     *
     * @return VersionComparison
     * @throws IOException
     * @throws BDRestException
     * @throws URISyntaxException
     */
    public VersionComparison compareWithHubVersion(String version) throws IOException,
            BDRestException, URISyntaxException {

        ClientResource resource = createClientResource();
        resource.addSegment("api");
        resource.addSegment("v1");
        resource.addSegment("current-version-comparison");
        resource.addQueryParameter("version", version);

        int responseCode = 0;

        resource.setMethod(Method.GET);
        handleRequest(resource, null, 0);
        responseCode = resource.getResponse().getStatus().getCode();

        if (responseCode == 200 || responseCode == 204 || responseCode == 202) {

            String response = readResponseAsString(resource.getResponse());
            Gson gson = new GsonBuilder().create();
            VersionComparison comparison = gson.fromJson(response, VersionComparison.class);
            return comparison;
        } else {
            throw new BDRestException("There was a problem comparing the specified version to the version of the Hub server. Error Code: " + responseCode,
                    resource);
        }
    }

    /**
     * Check the code locations with the host specified and the paths provided. Check the history for the scan history
     * that falls between the times provided, if the status of that scan history for all code locations is complete then
     * the bom is up to date with these scan results. Otherwise we try again after 10 sec, and we keep trying until it
     * is up to date or until we hit the maximum wait time.
     * If we find a scan history object that has status cancelled or an error type then we throw an exception.
     *
     *
     * @param timeBeforeScan
     *            DateTime before the Cli was run
     * @param timeAfterScan
     *            DateTime after the Cli was run
     * @param hostname
     *            String hostname where the Cli was run
     * @param scanTargets
     *            List<<String>> the target paths that were scanned
     * @param maximumWait
     *            long, maximum time to wait for the Bom to be updated completely
     * @return True if the bom has been updated with the code locations from this scan
     * @throws InterruptedException
     * @throws BDRestException
     * @throws HubIntegrationException
     * @throws URISyntaxException
     * @throws IOException
     */
    public boolean isBomUpToDate(DateTime timeBeforeScan, DateTime timeAfterScan, String hostname, List<String>
            scanTargets, long maximumWait) throws InterruptedException, BDRestException, HubIntegrationException, URISyntaxException, IOException {
        long startTime = System.currentTimeMillis();
        long elapsedTime = 0;
        while (elapsedTime < maximumWait) {

            List<ScanLocationItem> scanLocationsToCheck = getScanLocations(hostname, scanTargets);
            boolean upToDate = true;
            for (ScanLocationItem currentCodeLocation : scanLocationsToCheck) {
                for (ScanHistoryItem currentScanHistory : currentCodeLocation.getScanList()) {
                    DateTime scanHistoryCreationTime = currentScanHistory.getCreatedOnTime();
                    if (scanHistoryCreationTime != null && scanHistoryCreationTime.isAfter(timeBeforeScan) && scanHistoryCreationTime.isBefore(timeAfterScan)) {
                        // This scan history Item came from the scan we executed
                        if (ScanStatus.isFinishedStatus(currentScanHistory.getStatus())) {
                            if (ScanStatus.isErrorStatus(currentScanHistory.getStatus())) {
                                throw new HubIntegrationException("There was a problem with one of the code locations. Error Status : "
                                        + currentScanHistory.getStatus().name());
                            }
                        } else {
                            // The code location is still updating or matching, etc.
                            upToDate = false;
                        }
                    } else {
                        // This scan history Item did not come from the scan we executed
                        continue;
                    }
                }
            }
            if (upToDate) {
                // The code locations are all finished, so we know the bom has been updated with our scan results
                // So we break out of this loop
                return true;
            }
            // wait 10 seconds before checking the status's again
            Thread.sleep(10000);
            elapsedTime = System.currentTimeMillis() - startTime;
        }
        String formattedTime = String.format("%d minutes", TimeUnit.MILLISECONDS.toMinutes(maximumWait));
        throw new HubIntegrationException("The Bom has not finished updating from the scan within the specified wait time : " + formattedTime);

    }

    /**
     * Gets the code locations that match the host and paths provided
     *
     * @param hostname
     *            String
     * @param scanTargets
     *            List<<String>>
     *
     * @return List<<ScanLocationItem>>
     * @throws InterruptedException
     * @throws BDRestException
     * @throws HubIntegrationException
     * @throws URISyntaxException
     * @throws IOException
     * @throws MalformedURLException
     */
    public List<ScanLocationItem> getScanLocations(String hostname, List<String>
            scanTargets) throws InterruptedException, BDRestException, HubIntegrationException, URISyntaxException, IOException {
        List<ScanLocationItem> codeLocations = new ArrayList<ScanLocationItem>();

        ClientResource resource = null;
        for (String targetPath : scanTargets) {
            // Scan paths in the Hub only use '/' not '\'
            if (targetPath.contains("\\")) {
                targetPath = targetPath.replace("\\", "/");
            }
            // and it always starts with a '/'
            if (!targetPath.startsWith("/")) {
                targetPath = "/" + targetPath;
            }

            logger.debug(
                    "Checking for the scan location with Host name: '" + hostname + "' and Path: '" + targetPath +
                            "'");

            resource = createClientResource();
            resource.addSegment("api");
            resource.addSegment("v1");
            resource.addSegment("scanlocations");
            resource.addQueryParameter("host", hostname);
            resource.addQueryParameter("path", targetPath);

            resource.setMethod(Method.GET);

            handleRequest(resource, null, 0);

            int responseCode = resource.getResponse().getStatus().getCode();

            if (responseCode == 200) {
                String response = readResponseAsString(resource.getResponse());
                ScanLocationResults results = new Gson().fromJson(response, ScanLocationResults.class);
                ScanLocationItem currentCodeLocation = getScanLocationMatch(hostname, targetPath, results);
                if (currentCodeLocation == null) {
                    throw new HubIntegrationException("Could not determine the code location for the Host : " + hostname + " and Path : " + targetPath);
                }

                codeLocations.add(currentCodeLocation);
            } else {
                throw new BDRestException("There was a problem getting the code locations for the host and paths provided. Error Code: " + responseCode,
                        resource);
            }

        }
        return codeLocations;
    }

    private ScanLocationItem getScanLocationMatch(String hostname, String scanTarget, ScanLocationResults results) {
        String targetPath = scanTarget;

        if (targetPath.endsWith("/")) {
            targetPath = targetPath.substring(0, targetPath.length() - 1);
        }

        for (ScanLocationItem scanMatch : results.getItems()) {

            String path = scanMatch.getPath().trim();

            // Remove trailing slash from both strings
            if (path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }
            logger.trace("Comparing target : '" + targetPath + "' with path : '" + path + "'.");
            if (targetPath.equals(path)) {
                logger.trace("MATCHED!");
                return scanMatch;
            }
        }

        return null;
    }

    /**
     * Generates a new Hub report for the specified version.
     *
     * @param versionId
     *            String
     * @param reportFormat
     *            ReportFormateEnum
     *
     *
     * @return (String) ReportUrl
     * @throws IOException
     * @throws BDRestException
     * @throws URISyntaxException
     */
    public String generateHubReport(String versionId, ReportFormatEnum reportFormat) throws IOException, BDRestException,
            URISyntaxException {
        if (ReportFormatEnum.UNKNOWN == reportFormat) {
            throw new IllegalArgumentException("Can not generate a report of format : " + reportFormat);
        }

        ClientResource resource = createClientResource();
        resource.addSegment("api");
        resource.addSegment("versions");
        resource.addSegment(versionId);
        resource.addSegment("reports");

        resource.setMethod(Method.POST);

        JsonObject json = new JsonObject();
        json.addProperty("reportFormat", reportFormat.name());

        Gson gson = new GsonBuilder().create();
        StringRepresentation stringRep = new StringRepresentation(gson.toJson(json));
        stringRep.setMediaType(MediaType.APPLICATION_JSON);
        resource.getRequest().setEntity(stringRep);
        handleRequest(resource, null, 0);

        int responseCode = resource.getResponse().getStatus().getCode();

        if (responseCode == 201) {
            if (resource.getResponse().getAttributes() == null || resource.getResponse().getAttributes().get(HeaderConstants.ATTRIBUTE_HEADERS) == null) {
                throw new BDRestException("Could not get the response headers after creating the report.");
            }
            Series<Header> responseHeaders = (Series<Header>) resource.getResponse().getAttributes().get(HeaderConstants.ATTRIBUTE_HEADERS);
            Header reportUrl = responseHeaders.getFirst("location", true);

            if (reportUrl == null || StringUtils.isBlank(reportUrl.getValue())) {
                throw new BDRestException("Could not get the report URL from the response headers.");
            }

            return reportUrl.getValue();

        } else {
            throw new BDRestException("There was a problem generating a report for this Version. Error Code: " + responseCode, resource);
        }

    }

    /**
     * Get the links from the Report Url
     *
     * @param reportUrl
     *            String
     *
     * @return (ReportMetaInformationItem) report meta information
     * @throws IOException
     * @throws BDRestException
     * @throws URISyntaxException
     */
    public ReportMetaInformationItem getReportLinks(String reportUrl) throws IOException, BDRestException,
            URISyntaxException {

        ClientResource resource = createClientResource(reportUrl);

        Series<Header> requestHeaders = (Series<Header>) resource.getRequestAttributes().get(HeaderConstants.ATTRIBUTE_HEADERS);
        if (requestHeaders == null) {
            requestHeaders = new Series(Header.class);
            resource.getRequestAttributes().put(HeaderConstants.ATTRIBUTE_HEADERS, requestHeaders);
        }
        requestHeaders.add(new Header("Accept", MediaType.APPLICATION_JSON.toString()));

        // Restlet 2.3.4 and higher
        // resource.accept(MediaType.APPLICATION_JSON);

        resource.setMethod(Method.GET);

        handleRequest(resource, null, 0);
        int responseCode = resource.getResponse().getStatus().getCode();

        if (responseCode == 200) {
            String response = readResponseAsString(resource.getResponse());

            return new Gson().fromJson(response, ReportMetaInformationItem.class);
        } else {
            throw new BDRestException("There was a problem getting the links for the specified report. Error Code: " + responseCode, resource);
        }

    }

    /**
     * Gets the content of the report
     *
     * @param reportUrl
     *            String
     *
     * @return (VersionReport) report content
     * @throws IOException
     * @throws BDRestException
     * @throws URISyntaxException
     */
    public VersionReport getReportContent(String reportContentUrl) throws IOException, BDRestException,
            URISyntaxException {

        ClientResource resource = createClientResource(reportContentUrl);

        resource.setMethod(Method.GET);

        handleRequest(resource, null, 0);
        int responseCode = resource.getResponse().getStatus().getCode();

        if (responseCode == 200) {
            String response = readResponseAsString(resource.getResponse());

            Gson gson = new GsonBuilder().create();

            // FIXME make this less unstable if there are changes, in the response
            // For some reason the Hub responds with this weird json structure
            // EX:
            // {
            // "reportContent": [
            // {
            // "fileName": "CITestProject/CITestVersion1/version.json",
            // "fileContent": {
            // "detailedReleaseSummary": {
            // ...
            // },
            // "detailedCodeLocations": [],
            // "aggregateBomViewEntries": [],
            // "detailedVulnerabilities": [],
            // "detailedFileBomViewEntries": []
            // }
            // }
            // ]
            // }

            JsonObject reportResponse = gson.fromJson(response, JsonObject.class);

            JsonArray reportConentArray = gson.fromJson(reportResponse.get("reportContent"), JsonArray.class);

            JsonObject reportFile = (JsonObject) reportConentArray.get(0);

            VersionReport report = gson.fromJson(reportFile.get("fileContent"), VersionReport.class);

            return report;
        } else {
            throw new BDRestException("There was a problem getting the content of this Report. Error Code: " + responseCode, resource);
        }

    }

    private String readResponseAsString(Response response) throws IOException {
        StringBuilder sb = new StringBuilder();
        Reader reader = response.getEntity().getReader();
        BufferedReader bufReader = new BufferedReader(reader);
        try {
            String line = bufReader.readLine();
            while (line != null) {
                sb.append(line + "\n");
                line = bufReader.readLine();
            }
        } finally {
            bufReader.close();
        }
        return sb.toString();
    }

    private void handleRequest(ClientResource resource, ChallengeRequest proxyChallengeRequest,
            int attempt) throws BDRestException {

        if (proxyChallengeRequest != null) {
            // This should replace the authenticator for the proxy authentication
            // BUT it doesn't work for Digest authentication
            parseChallengeRequestRawValue(proxyChallengeRequest);
            resource.setProxyChallengeResponse(new ChallengeResponse(proxyChallengeRequest.getScheme(), null,
                    proxyUsername, proxyPassword.toCharArray(), null, proxyChallengeRequest.getRealm(), null,
                    null, proxyChallengeRequest.getDigestAlgorithm(), null, null, proxyChallengeRequest.getServerNonce(),
                    0, 0L));
        }
        try {
            resource.handle();
        } catch (ResourceException e) {
            if (!resource.getProxyChallengeRequests().isEmpty() && StringUtils.isNotBlank(proxyUsername) && StringUtils.isNotBlank(proxyPassword)) {

                ChallengeRequest newChallengeRequest = resource.getProxyChallengeRequests().get(0);
                if (attempt < 2) {
                    handleRequest(resource, newChallengeRequest, attempt + 1);
                } else {
                    throw new BDRestException("Too many proxy authentication attempts.", e, resource);
                }
            }
            throw new BDRestException("Problem connecting to the Hub server provided.", e, resource);
        }

    }
}
