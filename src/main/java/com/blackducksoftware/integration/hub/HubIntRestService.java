package com.blackducksoftware.integration.hub;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
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
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.restlet.Context;
import org.restlet.Response;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.Cookie;
import org.restlet.data.CookieSetting;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.restlet.util.Series;

import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.response.AutoCompleteItem;
import com.blackducksoftware.integration.hub.response.ProjectItem;
import com.blackducksoftware.integration.hub.response.ReleaseItem;
import com.blackducksoftware.integration.hub.response.ReleasesList;
import com.blackducksoftware.integration.hub.response.VersionComparison;
import com.blackducksoftware.integration.hub.response.mapping.AssetReferenceItem;
import com.blackducksoftware.integration.hub.response.mapping.EntityItem;
import com.blackducksoftware.integration.hub.response.mapping.EntityTypeEnum;
import com.blackducksoftware.integration.suite.sdk.logging.IntLogger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class HubIntRestService {
    private Series<Cookie> cookies;

    private final String baseUrl;

    private IntLogger logger;

    private String proxyUsername;

    private String proxyPassword;

    public HubIntRestService(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void setLogger(IntLogger logger) {
        this.logger = logger;
    }

    public String getBaseUrl() {
        return baseUrl;
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
    private ClientResource createClientResource(String url) throws URISyntaxException {
        url = url.replaceAll(" ", "%20");
        Context context = new Context();

        // the socketTimeout parameter is used in the httpClient extension that we do not use
        // We can probably remove this parameter
        context.getParameters().add("socketTimeout", "120000");

        context.getParameters().add("socketConnectTimeoutMs", "120000");
        context.getParameters().add("readTimeout", "120000");
        // Should throw timeout exception after 2 minutes

        ClientResource resource = new ClientResource(context, new URI(url));
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

        Authenticator.setDefault(new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return null;
            }
        });
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
     */
    public int setCookies(String hubUserName, String hubPassword) throws HubIntegrationException, URISyntaxException, BDRestException {
        return setCookies(hubUserName, hubPassword, null, 0);
    }

    /**
     * Gets the cookie for the Authorized connection to the Hub server. Returns the response code from the connection.
     *
     * @param hubUserName
     *            String the Username for the Hub server
     * @param hubPassword
     *            String the Password for the Hub server
     * @param proxyChallengeRequest
     *            ChallengeRequest proxyChallenge to get the correct authentication
     * @param attempt
     *            Integer authentication attempt number
     *
     * @return int Status code
     * @throws MalformedURLException
     * @throws HubIntegrationException
     * @throws URISyntaxException
     * @throws BDRestException
     */
    private int setCookies(String hubUserName, String hubPassword, ChallengeRequest proxyChallengeRequest, int attempt) throws HubIntegrationException,
            URISyntaxException, BDRestException {
        String url = getBaseUrl() + "/j_spring_security_check?j_username=" + hubUserName + "&j_password=" + hubPassword;
        ClientResource resource = createClientResource(url);

        if (proxyChallengeRequest != null) {
            // This should replace the authenticator for the proxy authentication
            // BUT it doesn't work for Digest authentication
            parseChallengeRequestRawValue(proxyChallengeRequest);
            resource.setProxyChallengeResponse(new ChallengeResponse(proxyChallengeRequest.getScheme(), null,
                    proxyUsername, proxyPassword.toCharArray(), null, proxyChallengeRequest.getRealm(), null,
                    null, proxyChallengeRequest.getDigestAlgorithm(), null, null, proxyChallengeRequest.getServerNonce(),
                    0, 0L));
        }
        resource.setMethod(Method.POST);
        try {

            EmptyRepresentation rep = new EmptyRepresentation();

            resource.post(rep);
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
        } catch (ResourceException e) {
            if (!resource.getProxyChallengeRequests().isEmpty() && StringUtils.isNotBlank(proxyUsername) && StringUtils.isNotBlank(proxyPassword)) {

                ChallengeRequest newChallengeRequest = resource.getProxyChallengeRequests().get(0);
                if (attempt < 2) {
                    return setCookies(hubUserName, hubPassword, newChallengeRequest, attempt + 1);
                } else {
                    throw new BDRestException("Too many proxy authentication attempts.", e, resource);
                }
            }
            throw e;
        }

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

        return getProjectMatches(hubProjectName, null, 0);
    }

    /**
     * Retrieves a list of Hub Projects that may match the hubProjectName
     *
     * @param hubProjectName
     *            String
     * @param proxyChallengeRequest
     *            ChallengeRequest proxyChallenge to get the correct authentication
     * @param attempt
     *            Integer authentication attempt number
     *
     * @return List<<AutoCompleteItem>>
     * @throws IOException
     * @throws BDRestException
     * @throws URISyntaxException
     */
    private List<AutoCompleteItem> getProjectMatches(String hubProjectName, ChallengeRequest proxyChallengeRequest, int attempt) throws IOException,
            BDRestException, URISyntaxException {
        // hubProjectName = URLEncoder.encode(hubProjectName, "UTF-8");
        String url = getBaseUrl() + "/api/v1/autocomplete/PROJECT?text=" + hubProjectName + "&limit=30&ownership=0";
        ClientResource resource = createClientResource(url);
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
            resource.getRequest().setCookies(getCookies());
            resource.setMethod(Method.GET);
            resource.get();
            int responseCode = resource.getResponse().getStatus().getCode();

            if (responseCode == 200 || responseCode == 204 || responseCode == 202) {
                Response resp = resource.getResponse();
                Reader reader = resp.getEntity().getReader();
                BufferedReader bufReader = new BufferedReader(reader);
                StringBuilder sb = new StringBuilder();
                String line = bufReader.readLine();
                while (line != null) {
                    sb.append(line + "\n");
                    line = bufReader.readLine();
                }
                bufReader.close();

                Gson gson = new GsonBuilder().create();
                return gson.fromJson(sb.toString(), new TypeToken<List<AutoCompleteItem>>() {
                }.getType());

            } else {
                throw new BDRestException("Could not connect to the Hub server with the Given Url and credentials. Error Code: " + responseCode, resource);
            }
        } catch (ResourceException e) {
            if (!resource.getProxyChallengeRequests().isEmpty() && StringUtils.isNotBlank(proxyUsername) && StringUtils.isNotBlank(proxyPassword)) {

                ChallengeRequest newChallengeRequest = resource.getProxyChallengeRequests().get(0);
                if (attempt < 2) {
                    return getProjectMatches(hubProjectName, newChallengeRequest, attempt + 1);
                } else {
                    throw new BDRestException("Too many proxy authentication attempts.", e, resource);
                }
            }
            throw new BDRestException("Problem connecting to the Hub server provided.", e, resource);
        }

    }

    /**
     * Gets the Hub Project that is specified by the projectId
     *
     * @param projectId
     *            String
     *
     * @return ProjectItem
     * @throws IOException
     * @throws BDRestException
     * @throws URISyntaxException
     */
    public ProjectItem getProjectById(String projectId) throws IOException,
            BDRestException, URISyntaxException {
        return getProjectById(projectId, null, 0);
    }

    /**
     * Gets the Hub Project that is specified by the projectId
     *
     * @param projectId
     *            String
     * @param proxyChallengeRequest
     *            ChallengeRequest proxyChallenge to get the correct authentication
     * @param attempt
     *            Integer authentication attempt number
     *
     * @return ProjectItem
     * @throws IOException
     * @throws BDRestException
     * @throws URISyntaxException
     */
    private ProjectItem getProjectById(String projectId, ChallengeRequest proxyChallengeRequest, int attempt) throws IOException,
            BDRestException, URISyntaxException {

        String url = getBaseUrl() + "/api/v1/projects/" + projectId;
        ClientResource resource = createClientResource(url);
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
            resource.getRequest().setCookies(getCookies());
            resource.setMethod(Method.GET);
            resource.get();
            int responseCode = resource.getResponse().getStatus().getCode();

            if (responseCode == 200 || responseCode == 204 || responseCode == 202) {
                Response resp = resource.getResponse();
                Reader reader = resp.getEntity().getReader();
                BufferedReader bufReader = new BufferedReader(reader);
                StringBuilder sb = new StringBuilder();
                String line = bufReader.readLine();
                while (line != null) {
                    sb.append(line + "\n");
                    line = bufReader.readLine();
                }
                bufReader.close();
                // logger.info(sb.toString());
                Gson gson = new GsonBuilder().create();
                return gson.fromJson(sb.toString(), ProjectItem.class);

            } else {
                throw new BDRestException("Could not connect to the Hub server with the Given Url and credentials. Error Code: " + responseCode, resource);
            }
        } catch (ResourceException e) {
            if (!resource.getProxyChallengeRequests().isEmpty() && StringUtils.isNotBlank(proxyUsername) && StringUtils.isNotBlank(proxyPassword)) {

                ChallengeRequest newChallengeRequest = resource.getProxyChallengeRequests().get(0);
                if (attempt < 2) {
                    return getProjectById(projectId, newChallengeRequest, attempt + 1);
                } else {
                    throw new BDRestException("Too many proxy authentication attempts.", e, resource);
                }
            }
            throw new BDRestException("Problem connecting to the Hub server provided.", e, resource);
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
     */
    public ProjectItem getProjectByName(String projectName) throws IOException, BDRestException,
            URISyntaxException {
        return getProjectByName(projectName, null, 0);
    }

    /**
     * Gets the Project that is specified by the projectName
     *
     * @param projectName
     *            String
     * @param proxyChallengeRequest
     *            ChallengeRequest proxyChallenge to get the correct authentication
     * @param attempt
     *            Integer authentication attempt number
     *
     * @return
     * @throws IOException
     * @throws BDRestException
     * @throws URISyntaxException
     */
    private ProjectItem getProjectByName(String projectName, ChallengeRequest proxyChallengeRequest, int attempt) throws IOException, BDRestException,
            URISyntaxException {
        // hubProjectName = URLEncoder.encode(hubProjectName, "UTF-8");
        String url = getBaseUrl() + "/api/v1/projects?name=" + projectName;
        ClientResource resource = createClientResource(url);
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
            resource.getRequest().setCookies(getCookies());
            resource.setMethod(Method.GET);
            resource.get();
            int responseCode = resource.getResponse().getStatus().getCode();

            if (responseCode == 200 || responseCode == 204 || responseCode == 202) {
                Response resp = resource.getResponse();
                Reader reader = resp.getEntity().getReader();
                BufferedReader bufReader = new BufferedReader(reader);
                StringBuilder sb = new StringBuilder();
                String line = bufReader.readLine();
                while (line != null) {
                    sb.append(line + "\n");
                    line = bufReader.readLine();
                }
                bufReader.close();
                Gson gson = new GsonBuilder().create();
                return gson.fromJson(sb.toString(), ProjectItem.class);

            } else {
                throw new BDRestException("This Project does not exist or there is a problem connecting to the Hub server", resource);
            }
        } catch (ResourceException e) {
            if (!resource.getProxyChallengeRequests().isEmpty() && StringUtils.isNotBlank(proxyUsername) && StringUtils.isNotBlank(proxyPassword)) {

                ChallengeRequest newChallengeRequest = resource.getProxyChallengeRequests().get(0);
                if (attempt < 2) {
                    return getProjectByName(projectName, newChallengeRequest, attempt + 1);
                } else {
                    throw new BDRestException("Too many proxy authentication attempts.", e, resource);
                }
            }
            throw new BDRestException("This Project does not exist or there is a problem connecting to the Hub server", e, resource);
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
        String url = null;
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

                url = baseUrl + "/api/v1/scanlocations?host=" + hostname + "&path=" + targetPath;
                logger.debug(
                        "Checking for the scan location with Host name: '" + hostname + "' and Path: '" + targetPath +
                                "'");

                resource = createClientResource(url);
                if (proxyChallengeRequest != null) {
                    // This should replace the authenticator for the proxy authentication
                    // BUT it doesn't work for Digest authentication
                    parseChallengeRequestRawValue(proxyChallengeRequest);
                    resource.setProxyChallengeResponse(new ChallengeResponse(proxyChallengeRequest.getScheme(), null,
                            proxyUsername, proxyPassword.toCharArray(), null, proxyChallengeRequest.getRealm(), null,
                            null, proxyChallengeRequest.getDigestAlgorithm(), null, null, proxyChallengeRequest.getServerNonce(),
                            0, 0L));
                }

                resource.getRequest().setCookies(getCookies());
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
            throw new BDRestException("Problem connecting to the Hub server provided.", e, resource);
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
        mapScansToProjectVersion(scanLocationIds, versionId, null, 0);
    }

    /**
     * If the scan Id has not already been mapped to the Version then it will make that mapping, otherwise it will not
     * perform the mapping.
     *
     * @param scanLocationIds
     *            Map<<String, Boolean>>
     * @param versionId
     *            String
     * @param proxyChallengeRequest
     *            ChallengeRequest proxyChallenge to get the correct authentication
     * @param attempt
     *            Integer authentication attempt number
     *
     * @throws BDRestException
     * @throws URISyntaxException
     * @throws MalformedURLException
     */
    private void mapScansToProjectVersion(Map<String, Boolean> scanLocationIds, String
            versionId, ChallengeRequest proxyChallengeRequest, int attempt) throws BDRestException, URISyntaxException {
        String url = getBaseUrl() + "/api/v1/assetreferences";
        ClientResource resource = createClientResource(url);
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
            if (!scanLocationIds.isEmpty()) {
                for (Entry<String, Boolean> scanId : scanLocationIds.entrySet()) {
                    if (!scanId.getValue()) {
                        // This scan location has not yet been mapped to the project/version
                        logger.debug(
                                "Mapping the scan location with id: '" + scanId.getKey() + "', to the Version with Id: '" + versionId +
                                        "'.");

                        resource.getRequest().setCookies(getCookies());
                        resource.setMethod(Method.POST);

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
                        resource.post(stringRep);
                        int responseCode = resource.getResponse().getStatus().getCode();

                        // HashMap<String, Object> responseMap = new HashMap<String, Object>();
                        if (responseCode == 201) {
                            // Successful mapping
                            logger.debug(
                                    "Successfully mapped the scan with id: '" + scanId.getKey() + "', to the Version with Id: '" + versionId
                                            + "'.");
                        } else {
                            throw new BDRestException("Could not connect to the Hub server with the Given Url and credentials. Error Code: " + responseCode,
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
        } catch (ResourceException e) {
            if (!resource.getProxyChallengeRequests().isEmpty() && StringUtils.isNotBlank(proxyUsername) && StringUtils.isNotBlank(proxyPassword)) {

                ChallengeRequest newChallengeRequest = resource.getProxyChallengeRequests().get(0);
                if (attempt < 2) {
                    mapScansToProjectVersion(scanLocationIds, versionId, newChallengeRequest, attempt + 1);
                } else {
                    throw new BDRestException("Too many proxy authentication attempts.", e, resource);
                }
            }
            throw new BDRestException("Problem connecting to the Hub server provided.", e, resource);
        }

    }

    /**
     * Gets the list of Versions for the specified Project
     *
     * @param projectId
     *            String
     *
     * @return List<<ReleaseItem>>
     * @throws IOException
     * @throws BDRestException
     * @throws URISyntaxException
     */
    public List<ReleaseItem> getVersionsForProject(String projectId) throws IOException,
            BDRestException, URISyntaxException {
        return getVersionsForProject(projectId, null, 0);
    }

    /**
     * Gets the list of Versions for the specified Project
     *
     * @param projectId
     *            String
     * @param proxyChallengeRequest
     *            ChallengeRequest proxyChallenge to get the correct authentication
     * @param attempt
     *            Integer authentication attempt number
     *
     * @return List<<ReleaseItem>>
     * @throws IOException
     * @throws BDRestException
     * @throws URISyntaxException
     */
    private List<ReleaseItem> getVersionsForProject(String projectId, ChallengeRequest proxyChallengeRequest, int attempt) throws IOException,
            BDRestException, URISyntaxException {

        String url = getBaseUrl() + "/api/v1/projects/" + projectId + "/releases";
        ClientResource resource = createClientResource(url);
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
            resource.getRequest().setCookies(getCookies());
            resource.setMethod(Method.GET);
            resource.get();
            int responseCode = resource.getResponse().getStatus().getCode();

            if (responseCode == 200 || responseCode == 204 || responseCode == 202) {

                Response resp = resource.getResponse();
                Reader reader = resp.getEntity().getReader();
                BufferedReader bufReader = new BufferedReader(reader);
                StringBuilder sb = new StringBuilder();
                String line = bufReader.readLine();
                while (line != null) {
                    sb.append(line + "\n");
                    line = bufReader.readLine();
                }
                bufReader.close();
                Gson gson = new GsonBuilder().create();
                // have to turn it into the ReleasesList object because of the way they formatted the json
                ReleasesList releasesList = gson.fromJson(sb.toString(), ReleasesList.class);

                return releasesList.getItems();

            } else {
                throw new BDRestException("Could not connect to the Hub server with the Given Url and credentials. Error Code: " + responseCode, resource);
            }
        } catch (ResourceException e) {
            if (!resource.getProxyChallengeRequests().isEmpty() && StringUtils.isNotBlank(proxyUsername) && StringUtils.isNotBlank(proxyPassword)) {

                ChallengeRequest newChallengeRequest = resource.getProxyChallengeRequests().get(0);
                if (attempt < 2) {
                    return getVersionsForProject(projectId, newChallengeRequest, attempt + 1);
                } else {
                    throw new BDRestException("Too many proxy authentication attempts.", e, resource);
                }
            }
            throw new BDRestException("Problem connecting to the Hub server provided.", e, resource);
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
        return createHubProject(projectName, null, 0);
    }

    /**
     * Creates a Hub Project with the specified name.
     *
     * @param projectName
     *            String
     * @param proxyChallengeRequest
     *            ChallengeRequest proxyChallenge to get the correct authentication
     * @param attempt
     *            Integer authentication attempt number
     *
     * @return (String) ProjectId
     * @throws IOException
     * @throws BDRestException
     * @throws URISyntaxException
     */
    private String createHubProject(String projectName, ChallengeRequest proxyChallengeRequest, int attempt) throws IOException, BDRestException,
            URISyntaxException {
        // projectName = URLEncoder.encode(projectName, "UTF-8");
        String url = getBaseUrl() + "/api/v1/projects";
        ClientResource resource = createClientResource(url);
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
            resource.getRequest().setCookies(getCookies());
            resource.setMethod(Method.POST);

            ProjectItem newProject = new ProjectItem();
            newProject.setName(projectName);

            Gson gson = new GsonBuilder().create();
            StringRepresentation stringRep = new StringRepresentation(gson.toJson(newProject));
            stringRep.setMediaType(MediaType.APPLICATION_JSON);

            resource.post(stringRep);
            int responseCode = resource.getResponse().getStatus().getCode();

            if (responseCode == 201) {

                Response resp = resource.getResponse();
                Reader reader = resp.getEntity().getReader();
                BufferedReader bufReader = new BufferedReader(reader);
                StringBuilder sb = new StringBuilder();
                String line = bufReader.readLine();
                while (line != null) {
                    sb.append(line + "\n");
                    line = bufReader.readLine();
                }
                bufReader.close();
                ProjectItem project = gson.fromJson(sb.toString(), ProjectItem.class);
                return project.getId();

            } else {

                throw new BDRestException("Could not connect to the Hub server with the Given Url and credentials. Error Code: " + responseCode, resource);
            }
        } catch (ResourceException e) {
            if (!resource.getProxyChallengeRequests().isEmpty() && StringUtils.isNotBlank(proxyUsername) && StringUtils.isNotBlank(proxyPassword)) {

                ChallengeRequest newChallengeRequest = resource.getProxyChallengeRequests().get(0);
                if (attempt < 2) {
                    return createHubProject(projectName, newChallengeRequest, attempt + 1);
                } else {
                    throw new BDRestException("Too many proxy authentication attempts.", e, resource);
                }
            }
            throw new BDRestException("Problem connecting to the Hub server provided.", e, resource);
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
     * @return (String) VersionId
     * @throws IOException
     * @throws BDRestException
     * @throws URISyntaxException
     */
    public String createHubVersion(String projectVersion, String projectId, String phase, String dist) throws
            IOException, BDRestException, URISyntaxException {
        return createHubVersion(projectVersion, projectId, phase, dist, null, 0);
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
     * @param proxyChallengeRequest
     *            ChallengeRequest proxyChallenge to get the correct authentication
     * @param attempt
     *            Integer authentication attempt number
     *
     * @return (String) VersionId
     * @throws IOException
     * @throws BDRestException
     * @throws URISyntaxException
     */
    private String createHubVersion(String projectVersion, String projectId, String phase, String dist, ChallengeRequest proxyChallengeRequest, int attempt)
            throws
            IOException, BDRestException, URISyntaxException {
        // projectVersion = URLEncoder.encode(projectVersion, "UTF-8");
        String url = getBaseUrl() + "/api/v1/releases";
        ClientResource resource = createClientResource(url);

        if (proxyChallengeRequest != null) {
            // This should replace the authenticator for the proxy authentication
            // BUT it doesn't work for Digest authentication
            parseChallengeRequestRawValue(proxyChallengeRequest);
            resource.setProxyChallengeResponse(new ChallengeResponse(proxyChallengeRequest.getScheme(), null,
                    proxyUsername, proxyPassword.toCharArray(), null, proxyChallengeRequest.getRealm(), null,
                    null, proxyChallengeRequest.getDigestAlgorithm(), null, null, proxyChallengeRequest.getServerNonce(),
                    0, 0L));
        }

        int responseCode;
        try {
            ReleaseItem newRelease = new ReleaseItem();
            newRelease.setProjectId(projectId);
            newRelease.setVersion(projectVersion);
            newRelease.setPhase(phase);
            newRelease.setDistribution(dist);

            resource.getRequest().setCookies(getCookies());
            resource.setMethod(Method.POST);

            Gson gson = new GsonBuilder().create();
            StringRepresentation stringRep = new StringRepresentation(gson.toJson(newRelease));
            stringRep.setMediaType(MediaType.APPLICATION_JSON);

            resource.post(stringRep);
            responseCode = resource.getResponse().getStatus().getCode();

            if (responseCode == 201) {

                Response resp = resource.getResponse();
                Reader reader = resp.getEntity().getReader();
                BufferedReader bufReader = new BufferedReader(reader);
                StringBuilder sb = new StringBuilder();
                String line = bufReader.readLine();
                while (line != null) {
                    sb.append(line + "\n");
                    line = bufReader.readLine();
                }
                bufReader.close();
                ReleaseItem release = gson.fromJson(sb.toString(), ReleaseItem.class);
                return release.getId();
            } else {
                throw new BDRestException("Could not connect to the Hub server with the Given Url and credentials. Error Code: " + responseCode, resource);
            }

        } catch (ResourceException e) {
            if (!resource.getProxyChallengeRequests().isEmpty() && StringUtils.isNotBlank(proxyUsername) && StringUtils.isNotBlank(proxyPassword)) {

                ChallengeRequest newChallengeRequest = resource.getProxyChallengeRequests().get(0);
                if (attempt < 2) {
                    return createHubVersion(projectVersion, projectId, phase, dist, newChallengeRequest, attempt + 1);
                } else {
                    throw new BDRestException("Too many proxy authentication attempts.", e, resource);
                }
            }
            throw new BDRestException("Problem connecting to the Hub server provided.", e, resource);
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
        return createHubProjectAndVersion(projectName, versionName, phase, dist, null, 0);
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
     * @param proxyChallengeRequest
     *            ChallengeRequest proxyChallenge to get the correct authentication
     * @param attempt
     *            Integer authentication attempt number
     *
     * @return (String) ProjectId
     * @throws IOException
     * @throws BDRestException
     * @throws URISyntaxException
     */
    private String createHubProjectAndVersion(String projectName, String versionName, String phase, String dist, ChallengeRequest proxyChallengeRequest,
            int attempt) throws IOException, BDRestException,
            URISyntaxException {
        // projectName = URLEncoder.encode(projectName, "UTF-8");
        String url = getBaseUrl() + "/api/v1/projects";
        ClientResource resource = createClientResource(url);
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
            ReleaseItem newRelease = new ReleaseItem();
            newRelease.setVersion(versionName);
            newRelease.setPhase(phase);
            newRelease.setDistribution(dist);

            resource.getRequest().setCookies(getCookies());
            resource.setMethod(Method.POST);

            ProjectItem newProject = new ProjectItem();
            newProject.setName(projectName);
            newProject.setReleaseItem(newRelease);

            Gson gson = new GsonBuilder().create();
            StringRepresentation stringRep = new StringRepresentation(gson.toJson(newProject));
            stringRep.setMediaType(MediaType.APPLICATION_JSON);

            resource.post(stringRep);
            int responseCode = resource.getResponse().getStatus().getCode();

            if (responseCode == 201) {

                Response resp = resource.getResponse();
                Reader reader = resp.getEntity().getReader();
                BufferedReader bufReader = new BufferedReader(reader);
                StringBuilder sb = new StringBuilder();
                String line = bufReader.readLine();
                while (line != null) {
                    sb.append(line + "\n");
                    line = bufReader.readLine();
                }
                bufReader.close();
                ProjectItem project = gson.fromJson(sb.toString(), ProjectItem.class);
                return project.getId();

            } else {

                throw new BDRestException("Could not connect to the Hub server with the Given Url and credentials. Error Code: " + responseCode, resource);
            }
        } catch (ResourceException e) {
            if (!resource.getProxyChallengeRequests().isEmpty() && StringUtils.isNotBlank(proxyUsername) && StringUtils.isNotBlank(proxyPassword)) {

                ChallengeRequest newChallengeRequest = resource.getProxyChallengeRequests().get(0);
                if (attempt < 2) {
                    return createHubProjectAndVersion(projectName, versionName, phase, dist, newChallengeRequest, attempt + 1);
                } else {
                    throw new BDRestException("Too many proxy authentication attempts.", e, resource);
                }
            }
            throw new BDRestException("Problem connecting to the Hub server provided.", e, resource);
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
        return getHubVersion(null, 0);
    }

    /**
     * Retrieves the version of the Hub server
     *
     * @param proxyChallengeRequest
     *            ChallengeRequest proxyChallenge to get the correct authentication
     * @param attempt
     *            Integer authentication attempt number
     *
     * @return String
     * @throws IOException
     * @throws BDRestException
     * @throws URISyntaxException
     */
    private String getHubVersion(ChallengeRequest proxyChallengeRequest, int attempt) throws IOException, BDRestException, URISyntaxException {

        String url = getBaseUrl() + "/api/v1/current-version";
        ClientResource resource = createClientResource(url);
        if (proxyChallengeRequest != null) {
            // This should replace the authenticator for the proxy authentication
            // BUT it doesn't work for Digest authentication
            parseChallengeRequestRawValue(proxyChallengeRequest);
            resource.setProxyChallengeResponse(new ChallengeResponse(proxyChallengeRequest.getScheme(), null,
                    proxyUsername, proxyPassword.toCharArray(), null, proxyChallengeRequest.getRealm(), null,
                    null, proxyChallengeRequest.getDigestAlgorithm(), null, null, proxyChallengeRequest.getServerNonce(),
                    0, 0L));
        }
        int responseCode = 0;
        try {
            resource.getRequest().setCookies(getCookies());
            resource.setMethod(Method.GET);
            resource.get();
            responseCode = resource.getResponse().getStatus().getCode();

            if (responseCode == 200 || responseCode == 204 || responseCode == 202) {
                Response resp = resource.getResponse();
                return resp.getEntityAsText();
            } else {
                throw new BDRestException("Could not connect to the Hub server with the Given Url and credentials. Error Code: " + responseCode, resource);
            }
        } catch (ResourceException e) {
            if (!resource.getProxyChallengeRequests().isEmpty() && StringUtils.isNotBlank(proxyUsername) && StringUtils.isNotBlank(proxyPassword)) {

                ChallengeRequest newChallengeRequest = resource.getProxyChallengeRequests().get(0);
                if (attempt < 2) {
                    return getHubVersion(newChallengeRequest, attempt + 1);
                } else {
                    throw new BDRestException("Too many proxy authentication attempts.", e, resource);
                }
            }
            throw new BDRestException("Could not connect to the Hub server with the Given Url and credentials. Error Code: ", e, resource);
        }
    }

    /**
     * Compares the specified version with the actual version of the Hub server.
     *
     * @param version
     *            String
     * @param proxyChallengeRequest
     *            ChallengeRequest proxyChallenge to get the correct authentication
     * @param attempt
     *            Integer authentication attempt number
     *
     * @return VersionComparison
     * @throws IOException
     * @throws BDRestException
     * @throws URISyntaxException
     */
    public VersionComparison compareWithHubVersion(String version) throws IOException,
            BDRestException, URISyntaxException {
        return compareWithHubVersion(version, null, 0);
    }

    /**
     * Compares the specified version with the actual version of the Hub server.
     *
     * @param version
     *            String
     * @param proxyChallengeRequest
     *            ChallengeRequest proxyChallenge to get the correct authentication
     * @param attempt
     *            Integer authentication attempt number
     *
     * @return VersionComparison
     * @throws IOException
     * @throws BDRestException
     * @throws URISyntaxException
     */
    private VersionComparison compareWithHubVersion(String version, ChallengeRequest proxyChallengeRequest, int attempt) throws IOException,
            BDRestException, URISyntaxException {

        String url = getBaseUrl() + "/api/v1/current-version-comparison?version=" + version;
        ClientResource resource = createClientResource(url);
        if (proxyChallengeRequest != null) {
            // This should replace the authenticator for the proxy authentication
            // BUT it doesn't work for Digest authentication
            parseChallengeRequestRawValue(proxyChallengeRequest);
            resource.setProxyChallengeResponse(new ChallengeResponse(proxyChallengeRequest.getScheme(), null,
                    proxyUsername, proxyPassword.toCharArray(), null, proxyChallengeRequest.getRealm(), null,
                    null, proxyChallengeRequest.getDigestAlgorithm(), null, null, proxyChallengeRequest.getServerNonce(),
                    0, 0L));
        }
        int responseCode = 0;
        try {
            resource.getRequest().setCookies(getCookies());
            resource.setMethod(Method.GET);
            resource.get();
            responseCode = resource.getResponse().getStatus().getCode();

            if (responseCode == 200 || responseCode == 204 || responseCode == 202) {

                Response resp = resource.getResponse();
                Reader reader = resp.getEntity().getReader();
                BufferedReader bufReader = new BufferedReader(reader);
                StringBuilder sb = new StringBuilder();
                String line = bufReader.readLine();
                while (line != null) {
                    sb.append(line + "\n");
                    line = bufReader.readLine();
                }
                bufReader.close();
                Gson gson = new GsonBuilder().create();
                VersionComparison comparison = gson.fromJson(sb.toString(), VersionComparison.class);
                return comparison;
            } else {
                throw new BDRestException("Could not connect to the Hub server with the Given Url and credentials. Error Code: " + responseCode, resource);
            }
        } catch (ResourceException e) {
            if (!resource.getProxyChallengeRequests().isEmpty() && StringUtils.isNotBlank(proxyUsername) && StringUtils.isNotBlank(proxyPassword)) {

                ChallengeRequest newChallengeRequest = resource.getProxyChallengeRequests().get(0);
                if (attempt < 2) {
                    return compareWithHubVersion(version, newChallengeRequest, attempt + 1);
                } else {
                    throw new BDRestException("Too many proxy authentication attempts.", e, resource);
                }
            }
            throw new BDRestException("Could not connect to the Hub server with the Given Url and credentials. Error Code: ", e, resource);
        }
    }

}
