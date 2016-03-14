package com.blackducksoftware.integration.hub;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.response.VersionComparison;
import com.blackducksoftware.integration.suite.sdk.logging.IntLogger;

public class HubSupportHelper {

    private boolean logOptionSupport = false;

    private boolean cliMappingSupport = false;

    private boolean cliStatusReturnSupport = false;

    private boolean jreProvidedSupport = false;

    public boolean isLogOptionSupport() {
        return logOptionSupport;
    }

    private void setLogOptionSupport(boolean logOptionSupport) {
        this.logOptionSupport = logOptionSupport;
    }

    public boolean isCliMappingSupport() {
        return cliMappingSupport;
    }

    private void setCliMappingSupport(boolean cliMappingSupport) {
        this.cliMappingSupport = cliMappingSupport;
    }

    public boolean isCliStatusReturnSupport() {
        return cliStatusReturnSupport;
    }

    private void setCliStatusReturnSupport(boolean cliStatusReturnSupport) {
        this.cliStatusReturnSupport = cliStatusReturnSupport;
    }

    public boolean isJreProvidedSupport() {
        return jreProvidedSupport;
    }

    public boolean isPolicyApiSupport() {
        // Policy support as well as jre support were added in the same version
        return jreProvidedSupport;
    }

    public boolean isCliStatusDirOptionSupport() {
        // the --statusWriteDir option was added in the same version as the jreProvidedSupport
        return jreProvidedSupport;
    }

    private void setJreProvidedSupport(boolean jreProvidedSupport) {
        this.jreProvidedSupport = jreProvidedSupport;
    }

    /**
     * This will check the Hub server to see which options this version of the Hub supports. You can use the get methods
     * in this class after this method has run to get the supported options.
     *
     * @param service
     *            HubIntRestService
     * @param logger
     *            IntLogger
     * @throws IOException
     * @throws URISyntaxException
     */
    public void checkHubSupport(HubIntRestService service, IntLogger logger) throws IOException, URISyntaxException {
        try {
            String hubServerVersion = service.getHubVersion();

            if (compareVersion(hubServerVersion, "3.0.0", service)) {
                // The cli did not come packaged with a jre until 3.0.0
                setJreProvidedSupport(true);
                setCliStatusReturnSupport(true);
                setCliMappingSupport(true);
                setLogOptionSupport(true);

            } else if (compareVersion(hubServerVersion, "2.3.0", service)) {
                // The cli did not return correct status codes until 2.3.0
                setJreProvidedSupport(false);
                setCliStatusReturnSupport(true);
                setCliMappingSupport(true);
                setLogOptionSupport(true);
            } else if (compareVersion(hubServerVersion, "2.2.0", service)) {
                // The cli did not support mapping scans to versions until 2.2.0
                setJreProvidedSupport(false);
                setCliStatusReturnSupport(false);
                setCliMappingSupport(true);
                setLogOptionSupport(true);
            } else {
                // The logDir option and this version api weren't added until Hub version 2.0.1
                // So if this api call works we know the log option is supported
                setJreProvidedSupport(false);
                setCliStatusReturnSupport(false);
                setCliMappingSupport(false);
                setLogOptionSupport(true);
            }
        } catch (BDRestException e) {
            ResourceException resEx = null;
            if (e.getCause() != null && e.getCause() instanceof ResourceException) {
                resEx = (ResourceException) e.getCause();
            }
            if (resEx != null && resEx.getStatus().equals(Status.CLIENT_ERROR_NOT_FOUND)) {
                // The Hub server is version 2.0.0 and the version endpoint does not exist
                setJreProvidedSupport(false);
                setCliStatusReturnSupport(false);
                setCliMappingSupport(false);
                setLogOptionSupport(false);
                return;
            } else if (resEx != null) {
                if (logger != null) {
                    logger.error(resEx.getMessage());
                }
            }
            if (logger != null) {
                logger.error(e.getMessage());
            }
        }
    }

    /**
     * This method will check the provided version against the actual version of the Hub server.
     * If the provided version is less than or equal to the server version we return true.
     * If the provided version is greater than the server version we return false.
     *
     *
     * @param service
     *            String, must contain the 3 parts of the version X.X.X
     * @param testVersion
     *            String, must contain the 3 parts of the version X.X.X
     * @return
     * @throws IOException
     * @throws BDRestException
     * @throws URISyntaxException
     */
    private boolean compareVersion(String hubServerVersion, String testVersion, HubIntRestService service) throws IOException, BDRestException,
            URISyntaxException {
        try {
            String[] splitServerVersion = hubServerVersion.split("\\.");
            String[] splitTestVersion = testVersion.split("\\.");

            Integer[] serverVersionParts = new Integer[splitServerVersion.length];
            Integer[] testVersionParts = new Integer[splitTestVersion.length];
            boolean isServerSnapshot = false;
            for (int i = 0; i < splitServerVersion.length; i++) {
                String currentServerPart = splitServerVersion[i];
                String currentTestVersionPart = splitTestVersion[i];
                if (currentServerPart.contains("-SNAPSHOT")) {
                    isServerSnapshot = true;
                    currentServerPart = currentServerPart.replace("-SNAPSHOT", "");
                }
                serverVersionParts[i] = Integer.valueOf(currentServerPart);
                testVersionParts[i] = Integer.valueOf(currentTestVersionPart);
            }

            if (serverVersionParts[0] > testVersionParts[0]) {
                // Major part of the server version was greater,
                // so we know it supports whatever feature we are testing for
                return true;
            } else if (serverVersionParts[0] < testVersionParts[0]) {
                // Major part of the server version was less than the one provided,
                // so we know it does not support whatever feature we are testing for
                return false;
            }

            if (serverVersionParts[1] > testVersionParts[1]) {
                // Minor part of the server version was greater,
                // so we know it supports whatever feature we are testing for
                return true;
            } else if (serverVersionParts[1] < testVersionParts[1]) {
                // Minor part of the server version was less than the one provided,
                // so we know it does not support whatever feature we are testing for
                return false;
            }

            if (serverVersionParts[2] > testVersionParts[2]) {
                // Fix version part of the server version was greater,
                // so we know it supports whatever feature we are testing for
                return true;
            } else if (serverVersionParts[2] < testVersionParts[2]) {
                // Fix version part of the server version was less than the one provided,
                // so we know it does not support whatever feature we are testing for
                return false;
            }

            // The versions are identical, check if the server is a SNAPSHOT
            if (isServerSnapshot) {
                // We assume the SNAPSHOT version is less than the released version
                return false;
            }

        } catch (NumberFormatException e) {
            return fallBackComparison(testVersion, service);
        } catch (ArrayIndexOutOfBoundsException e) {
            return fallBackComparison(testVersion, service);
        }

        return true;
    }

    /**
     * We are parsing the versions incorrectly so we let the Hub server compare the test version to the server version.
     * We return true if the testVersion is less than or equal to the server version
     * We return false if the testVersion is greater than the server version.
     *
     * @param testVersion
     *            String
     * @param service
     *            HubIntRestService
     * @return
     * @throws IOException
     * @throws BDRestException
     * @throws URISyntaxException
     */
    private boolean fallBackComparison(String testVersion, HubIntRestService service) throws IOException, BDRestException, URISyntaxException {
        VersionComparison comparison = service.compareWithHubVersion(testVersion);
        if (comparison.getNumericResult() <= 0) {
            return true;
        } else {
            return false;
        }
    }

    private static String getCLIWrapperLink(String hubUrl) throws IllegalArgumentException {
        if (StringUtils.isBlank(hubUrl)) {
            throw new IllegalArgumentException("You must provide a valid Hub URL in order to get the correct link.");
        }
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(hubUrl);
        if (!hubUrl.endsWith("/")) {
            urlBuilder.append("/");
        }
        urlBuilder.append("download");
        urlBuilder.append("/");
        return urlBuilder.toString();
    }

    public static String getLinuxCLIWrapperLink(String hubUrl) throws IllegalArgumentException {
        String baseUrl = getCLIWrapperLink(hubUrl);
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(baseUrl);
        urlBuilder.append("scan.cli.zip");
        return urlBuilder.toString();
    }

    public static String getWindowsCLIWrapperLink(String hubUrl) throws IllegalArgumentException {
        String baseUrl = getCLIWrapperLink(hubUrl);
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(baseUrl);
        // FIXME change to the correct link, when that information is known
        // FIXME fix the unit tests too
        urlBuilder.append("scan.cli.zip");
        return urlBuilder.toString();
    }

    public static String getOSXCLIWrapperLink(String hubUrl) throws IllegalArgumentException {
        String baseUrl = getCLIWrapperLink(hubUrl);
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(baseUrl);
        // FIXME change to the correct link, when that information is known
        // FIXME fix the unit tests too
        urlBuilder.append("scan.cli.zip");
        return urlBuilder.toString();
    }
}
