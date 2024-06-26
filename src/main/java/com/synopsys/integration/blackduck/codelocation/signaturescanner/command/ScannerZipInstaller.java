/*
 * blackduck-common
 *
 * Copyright (c) 2024 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.codelocation.signaturescanner.command;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.cert.Certificate;

import com.synopsys.integration.blackduck.api.core.BlackDuckResponse;
import com.synopsys.integration.blackduck.api.core.response.UrlSingleResponse;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;
import com.synopsys.integration.blackduck.http.client.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.request.BlackDuckRequest;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.io.FileUtils;

import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.blackduck.http.client.SignatureScannerClient;
import com.synopsys.integration.blackduck.keystore.KeyStoreHelper;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.response.Response;
import com.synopsys.integration.util.CleanupZipExpander;
import com.synopsys.integration.util.OperatingSystemType;

public class ScannerZipInstaller implements ScannerInstaller {
    private static final String PLATFORM_PARAMETER_KEY = "platforms";
    private static final String MAC_PLATFORM_PARAMETER_VALUE = "macosx";
    private static final String LINUX_PLATFORM_PARAMETER_VALUE = "linux";
    private static final String WINDOWS_PLATFORM_PARAMETER_VALUE = "windows";

    private static final String SCAN_CLI_TOOL_DOWNLOAD_URL = "api/tools/scan.cli.zip/versions/latest/";

    public static final String BLACK_DUCK_SIGNATURE_SCANNER_INSTALL_DIRECTORY = "Black_Duck_Scan_Installation";
    public static final String SCAN_CLI_VERSION_FILENAME = "scan-cli-version.txt";

    private final IntLogger logger;
    private final SignatureScannerClient signatureScannerClient; // TOME maybe nuke?
    private final BlackDuckHttpClient blackDuckHttpClient;
    private final CleanupZipExpander cleanupZipExpander;
    private final ScanPathsUtility scanPathsUtility;
    private final KeyStoreHelper keyStoreHelper;
    private final HttpUrl blackDuckServerUrl;
    private final OperatingSystemType operatingSystemType;
    private final File installDirectory;

    public ScannerZipInstaller(
        IntLogger logger,
        SignatureScannerClient signatureScannerClient,
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
        this.signatureScannerClient = signatureScannerClient;
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

        File scannerExpansionDirectory = new File(installDirectory, ScannerZipInstaller.BLACK_DUCK_SIGNATURE_SCANNER_INSTALL_DIRECTORY);
        scannerExpansionDirectory.mkdirs();

        File versionFile = new File(scannerExpansionDirectory, ScannerZipInstaller.SCAN_CLI_VERSION_FILENAME);
        HttpUrl downloadUrl = getDownloadUrl();

        try {
            String scannerVersion;
            if (!versionFile.exists()) {
                logger.info("No version file exists, assuming this is new installation and the latest signature scanner should be downloaded.");
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

    private HttpUrl getDownloadUrl() throws BlackDuckIntegrationException {
        StringBuilder url = new StringBuilder(blackDuckServerUrl.string());
        if (!blackDuckServerUrl.string().endsWith("/")) {
            url.append("/");
            url.append(SCAN_CLI_TOOL_DOWNLOAD_URL);
        }

        String platform;
        if (OperatingSystemType.MAC == operatingSystemType) {
            platform = MAC_PLATFORM_PARAMETER_VALUE;
        } else if (OperatingSystemType.WINDOWS == operatingSystemType) {
            platform = WINDOWS_PLATFORM_PARAMETER_VALUE;
        } else {
            platform = LINUX_PLATFORM_PARAMETER_VALUE;
        }

        url.append(PLATFORM_PARAMETER_KEY + "/" + platform);

        try {
            return new HttpUrl(url.toString());
        } catch (IntegrationException e) {
            throw new BlackDuckIntegrationException(String.format("The Black Duck Signature Scanner url (%s) is not valid.", url));
        }
    }

    /**
     * Calls /api/tools to download the latest available version of the Signature Scanner tool. If we provide the
     * Accept-Version header, but it is blank or doesn’t match the version offered by the server, then Black Duck
     * provides the bits for download. If the Accept-Version header matches the version offered by the server, then the
     * we already have the same version and do not need to redownload.
     * @param scannerExpansionDirectory
     * @param downloadUrl
     * @param localScannerVersion
     * @return The version of scan-cli that will be used for Signature Scanning.
     * @throws IOException, IntegrationException, ArchiveException
     */
    private String downloadSignatureScanner(File scannerExpansionDirectory, HttpUrl downloadUrl, String localScannerVersion) throws IOException, IntegrationException, ArchiveException {
        logger.debug(String.format("Downloading scan-cli to '%s' from '%s'.", scannerExpansionDirectory.getAbsolutePath(), downloadUrl));
        BlackDuckRequestBuilder requestBuilder = new BlackDuckRequestBuilder()
                .url(downloadUrl)
                .addHeader("Accept-Version", localScannerVersion);

        BlackDuckRequest<BlackDuckResponse, UrlSingleResponse<BlackDuckResponse>> downloadRequest = BlackDuckRequest.createSingleRequest(
                requestBuilder,
                downloadUrl,
                BlackDuckResponse.class
        );

        try (Response response = blackDuckHttpClient.execute(downloadRequest)) {
            if (response.isStatusCodeSuccess()) {
                logger.info("Downloading the Black Duck Signature Scanner.");
                // if response is 200 OK then we got the bits for download so do the unzipping.
                try (InputStream responseStream = response.getContent()) {
                    logger.info(String.format(
                            "If your Black Duck server has changed, the contents of %s may change which could involve deleting files - please do not place items in the expansion directory as this directory is assumed to be under blackduck-common control.",
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

                Certificate serverCertificate = signatureScannerClient.getServerCertificate(); // TOME not sure if this is needed?
                keyStoreHelper.updateKeyStoreWithServerCertificate(downloadUrl.url().getHost(), serverCertificate, scanPaths.getPathToCacerts());

                logger.info("Black Duck Signature Scanner downloaded successfully.");
                String latestScannerVersion = response.getHeaderValue("Version");
                logger.info(String.format("The signature scanner should be downloaded. Locally installed signature scanner is %s, but the latest version is %s", localScannerVersion, latestScannerVersion));
                return latestScannerVersion;
            } else if (response.getStatusCode() == 304) {
                // If no need to update, response is HTTP 304 Not modified
                 logger.debug("Local signature scanner version is up to date - skipping download.");
                return localScannerVersion;
            } else {
                throw new IntegrationException();
            }
        }
    }

}
