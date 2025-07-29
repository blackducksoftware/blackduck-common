/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.codelocation.signaturescanner.command;

import com.blackduck.integration.blackduck.api.core.BlackDuckResponse;
import com.blackduck.integration.blackduck.api.core.response.UrlSingleResponse;
import com.blackduck.integration.blackduck.exception.BlackDuckIntegrationException;
import com.blackduck.integration.blackduck.http.BlackDuckRequestBuilder;
import com.blackduck.integration.blackduck.http.client.BlackDuckHttpClient;
import com.blackduck.integration.blackduck.keystore.KeyStoreHelper;
import com.blackduck.integration.blackduck.service.request.BlackDuckRequest;
import com.blackduck.integration.blackduck.version.BlackDuckVersion;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.log.IntLogger;
import com.blackduck.integration.rest.HttpUrl;
import com.blackduck.integration.rest.response.Response;
import com.blackduck.integration.util.CleanupZipExpander;
import com.blackduck.integration.util.OperatingSystemType;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLHandshakeException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.cert.Certificate;

public class ToolsApiScannerInstaller extends ApiScannerInstaller {
    // The tools API for downloading the scan-cli is called on by Detect for BD versions 2024.7.0 or newer
    public static final BlackDuckVersion MIN_BLACK_DUCK_VERSION = new BlackDuckVersion(2024, 7, 0);

    private static final String LATEST_SCAN_CLI_TOOL_DOWNLOAD_URL = "api/tools/scan.cli.zip/versions/latest/";
    private static final String PLATFORM_PARAMETER_KEY = "platforms";
    private static final String MAC_PLATFORM_PARAMETER_VALUE = "macosx";
    private static final String LINUX_PLATFORM_PARAMETER_VALUE = "linux";
    private static final String WINDOWS_PLATFORM_PARAMETER_VALUE = "windows";
    private static final String ALPINE_PLATFORM_PARAMETER_VALUE = "alpine_linux";

    private final IntLogger logger;
    private final BlackDuckHttpClient blackDuckHttpClient;
    private final CleanupZipExpander cleanupZipExpander;
    private final ScanPathsUtility scanPathsUtility;
    private final KeyStoreHelper keyStoreHelper;
    private final HttpUrl blackDuckServerUrl;
    private final OperatingSystemType operatingSystemType;
    private final File installDirectory;

    public ToolsApiScannerInstaller(
            IntLogger logger,
            BlackDuckHttpClient blackDuckHttpClient,
            CleanupZipExpander cleanupZipExpander,
            ScanPathsUtility scanPathsUtility,
            KeyStoreHelper keyStoreHelper,
            HttpUrl blackDuckServerUrl,
            OperatingSystemType operatingSystemType,
            File installDirectory
    ) {
        if (null == blackDuckServerUrl) {
            throw new IllegalArgumentException("A Black Duck server url must be provided.");
        }

        this.logger = logger;
        this.blackDuckHttpClient = blackDuckHttpClient;
        this.cleanupZipExpander = cleanupZipExpander;
        this.scanPathsUtility = scanPathsUtility;
        this.keyStoreHelper = keyStoreHelper;
        this.blackDuckServerUrl = blackDuckServerUrl;
        this.operatingSystemType = operatingSystemType;
        this.installDirectory = installDirectory;
    }

    /**
     * The Black Duck Signature Scanner (scan-cli) will be downloaded if it has not
     * previously been downloaded or if it has been updated on the server. The
     * absolute path to the install location will be returned if it was
     * downloaded or found successfully, otherwise an Optional.empty will be
     * returned and the log will contain details concerning the failure.
     */
    @Override
    public File installOrUpdateScanner() throws BlackDuckIntegrationException {
        if (installDirectory.exists()) {
            try {
                ScanPaths scanPaths = scanPathsUtility.searchForScanPaths(installDirectory);
                if (!scanPaths.isManagedByLibrary()) {
                    return installDirectory;
                }
            } catch (BlackDuckIntegrationException ignored) {
                // a valid scanPaths could not be found so we will need to attempt an install
            }
        }

        File scannerExpansionDirectory = new File(installDirectory, BLACK_DUCK_SIGNATURE_SCANNER_INSTALL_DIRECTORY);
        scannerExpansionDirectory.mkdirs();

        File versionFile = new File(scannerExpansionDirectory, VERSION_FILENAME);
        HttpUrl downloadUrl = getDownloadUrl();

        try {
            String scannerVersion;
            if (!versionFile.exists()) {
                logger.info("No version file exists, assuming this is new installation and the signature scanner should be downloaded.");
                scannerVersion = downloadSignatureScanner(scannerExpansionDirectory, downloadUrl, "");
                // Version file creation should happen after successful download
                logger.debug("The version file has not been created yet so creating it now.");
                FileUtils.writeStringToFile(versionFile, scannerVersion, Charset.defaultCharset());
                return installDirectory;
            }
            // A version file exists, so we have to compare to determine if a download should occur.
            String localScannerVersion = FileUtils.readFileToString(versionFile, Charset.defaultCharset());
            logger.debug(String.format("Locally installed signature scanner version: %s", localScannerVersion));
            // We will call the tool download API and update our local signature scanner only if it happens to be outdated.
            scannerVersion = downloadSignatureScanner(scannerExpansionDirectory, downloadUrl, localScannerVersion);
            // Update version file if needed
            if (localScannerVersion != scannerVersion) {
                FileUtils.writeStringToFile(versionFile, scannerVersion, Charset.defaultCharset());
            }
        } catch (Exception e) {
            throw new BlackDuckIntegrationException("The Black Duck Signature Scanner could not be downloaded successfully: " + e.getMessage(), e);
        }

        logger.info("The Black Duck Signature Scanner downloaded/found successfully: " + installDirectory.getAbsolutePath());
        return installDirectory;
    }

    protected HttpUrl getDownloadUrl() throws BlackDuckIntegrationException {
        StringBuilder url = new StringBuilder(blackDuckServerUrl.string());
        if (!blackDuckServerUrl.string().endsWith("/")) {
            url.append("/");
        }
        url.append(LATEST_SCAN_CLI_TOOL_DOWNLOAD_URL);

        String platform;

        //we give precedence to env variable which will be provided via to customers who want to specifically set the variable to tell us if this is alpine_linux
        //Documentation will say to only use that for alpine and no other OS to keep it simple, corresponding check will be done in Detect and passed on  here
        // if the code goes to else if conditions that means env variable was not provided, and we will fall back to regular approach
        if(OperatingSystemType.ALPINE_LINUX == operatingSystemType) {
            platform = ALPINE_PLATFORM_PARAMETER_VALUE;
        } else if (OperatingSystemType.MAC == operatingSystemType) {
            platform = MAC_PLATFORM_PARAMETER_VALUE;
        } else if (OperatingSystemType.WINDOWS == operatingSystemType) {
            platform = WINDOWS_PLATFORM_PARAMETER_VALUE;
        } else if (OperatingSystemType.LINUX == operatingSystemType && isAlpineLinux()) {
            platform = ALPINE_PLATFORM_PARAMETER_VALUE;
        } else {
            platform = LINUX_PLATFORM_PARAMETER_VALUE;
        }

        if(SystemUtils.OS_ARCH.equals("arm64")) {
            platform = platform + "_arm64";
        }

        url.append(PLATFORM_PARAMETER_KEY + "/" + platform);

        try {
            return new HttpUrl(url.toString());
        } catch (IntegrationException e) {
            throw new BlackDuckIntegrationException(String.format("The Black Duck Signature Scanner url (%s) is not valid.", url));
        }
    }

    private boolean isAlpineLinux() {
        if(new File("/etc/alpine-release").exists()) {
            return true;
        } else if(new File("/etc/os-release").exists()) {
            try {
                String osRelease = new String(Files.readAllBytes(Paths.get("/etc/os-release")));
                return osRelease.toLowerCase().contains("alpine");
            } catch (IOException e) {
                logger.trace("There was a problem reading the os-release file", e);
                return false;
            }
        } else if(new File("/usr/lib/os-release").exists()) {
            try {
                String osRelease = new String(Files.readAllBytes(Paths.get("/usr/lib/os-release")));
                return osRelease.toLowerCase().contains("alpine");
            } catch (IOException e) {
                logger.trace("There was a problem reading the os-release file", e);
                return false;
            }
        }
        return false;
    }

    /**
     * Calls /api/tools to download the latest available version of the Signature Scanner tool. If we provide the
     * Version header, but it is blank or doesn’t match the version offered by the server, then Black Duck
     * provides the bits for download. If the Version header matches the version offered by the server, the response is 304
     * meaning we already have the latest version and do not need to download.
     * @param scannerExpansionDirectory
     * @param downloadUrl
     * @param localScannerVersion
     * @return The version of scan-cli that will be used for Signature Scanning.
     * @throws IOException, IntegrationException, ArchiveException
     */
    protected String downloadSignatureScanner(File scannerExpansionDirectory, HttpUrl downloadUrl, String localScannerVersion) throws IOException, IntegrationException, ArchiveException {
        BlackDuckRequestBuilder requestBuilder = new BlackDuckRequestBuilder()
                .url(downloadUrl)
                .addHeader("Version", localScannerVersion);
        if (!localScannerVersion.isEmpty()) {
            requestBuilder.addHeader("Accept-Version", "ANY");
        }

        BlackDuckRequest<BlackDuckResponse, UrlSingleResponse<BlackDuckResponse>> downloadRequest = BlackDuckRequest.createSingleRequest(
                requestBuilder,
                downloadUrl,
                BlackDuckResponse.class
        );

        try (Response response = blackDuckHttpClient.execute(downloadRequest)) {
            if (response.isStatusCodeSuccess()) {
                String latestScannerVersion = response.getHeaderValue("Version");
                if (!localScannerVersion.isEmpty()) {
                    logger.info(String.format("The signature scanner should be downloaded. Locally installed signature scanner is %s, but the latest version is %s", localScannerVersion, latestScannerVersion));
                }
                logger.info("Downloading the Black Duck Signature Scanner.");
                // if response is 200 OK then we got the bits for download so do the unzipping.
                try (InputStream responseStream = response.getContent()) {
                    logger.info(String.format(
                            "If the Signature Scanner on your Black Duck server has changed, the contents of %s may change which could involve deleting files - please do not place items in the expansion directory as this directory is assumed to be under blackduck-common control.",
                            scannerExpansionDirectory.getAbsolutePath()));
                    cleanupZipExpander.expand(responseStream, scannerExpansionDirectory);
                }
                ScanPaths scanPaths = scanPathsUtility.searchForScanPaths(scannerExpansionDirectory.getParentFile());
                File javaExecutable = new File(scanPaths.getPathToJavaExecutable());
                File oneJar = new File(scanPaths.getPathToOneJar());
                File scanExecutable = new File(scanPaths.getPathToScanExecutable());
                javaExecutable.setExecutable(true);
                oneJar.setExecutable(true);
                scanExecutable.setExecutable(true);


                connectAndGetServerCertificate(downloadUrl, scanPaths);

                logger.info("Black Duck Signature Scanner downloaded successfully.");
                return latestScannerVersion;
            } else if (response.getStatusCode() == 304) {
                // If no need to update, response is HTTP 304 Not modified
                logger.debug("Locally installed Signature Scanner version is up to date - skipping download.");

                ScanPaths scanPaths = scanPathsUtility.searchForScanPaths(scannerExpansionDirectory.getParentFile());
                connectAndGetServerCertificate(downloadUrl, scanPaths);

                return localScannerVersion;
            } else {
                logger.debug("Unable to download Signature Scanner. Response code: " + response.getStatusCode() + " " + response.getStatusMessage());
                throw new IntegrationException("Unable to download Black Duck Signature Scanner. Response code: " + response.getStatusCode() + " " + response.getStatusMessage());
            }
        }
    }

    private void connectAndGetServerCertificate(HttpUrl httpsServer, ScanPaths scanPaths) {
        HttpsURLConnection httpsConnection = null;
        try {
            httpsConnection = (HttpsURLConnection) httpsServer.url().openConnection();
            httpsConnection.connect();
            Certificate[] certificates = httpsConnection.getServerCertificates();
            httpsConnection.disconnect();
            if (certificates.length > 0) {
                keyStoreHelper.updateKeyStoreWithServerCertificate(httpsServer.url().getHost(), certificates[0], scanPaths.getPathToCacerts());
            } else {
                throw new IOException();
            }
        } catch (SSLHandshakeException e) {
            logger.warn("Automatically trusting server certificates - not recommended for production use.");
        } catch (IOException e) {
            logger.errorAndDebug("Could not get Black Duck server certificate which is required for managing the local keystore - communicating to the server will have to be configured manually: " + e.getMessage(), e);
        }
    }
}
