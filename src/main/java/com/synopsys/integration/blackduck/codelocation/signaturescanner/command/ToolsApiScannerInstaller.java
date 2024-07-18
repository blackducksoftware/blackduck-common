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
import java.nio.file.Files;

import com.synopsys.integration.blackduck.api.core.BlackDuckResponse;
import com.synopsys.integration.blackduck.api.core.response.UrlSingleResponse;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;
import com.synopsys.integration.blackduck.http.client.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.request.BlackDuckRequest;
import com.synopsys.integration.blackduck.version.BlackDuckVersion;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.io.FileUtils;

import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.response.Response;
import com.synopsys.integration.util.CleanupZipExpander;
import com.synopsys.integration.util.OperatingSystemType;

public class ToolsApiScannerInstaller extends ApiScannerInstaller {
    // The tools API for downloading the scan-cli is called on by Detect for BD versions 2024.7.0 or newer
    public static final BlackDuckVersion MIN_BLACK_DUCK_VERSION = new BlackDuckVersion(2024, 7, 0);

    private static final String LATEST_SCAN_CLI_TOOL_DOWNLOAD_URL = "api/tools/scan.cli.zip/versions/latest/";
    private static final String PLATFORM_PARAMETER_KEY = "platforms";
    private static final String MAC_PLATFORM_PARAMETER_VALUE = "macosx";
    private static final String LINUX_PLATFORM_PARAMETER_VALUE = "linux";
    private static final String WINDOWS_PLATFORM_PARAMETER_VALUE = "windows";

    public static final String SCAN_CLI_VERSION_FILENAME = "scan-cli-version.txt";

    private final BlackDuckHttpClient blackDuckHttpClient;
    private final CleanupZipExpander cleanupZipExpander;
    private final OperatingSystemType operatingSystemType;

    public ToolsApiScannerInstaller(
            IntLogger logger,
            BlackDuckHttpClient blackDuckHttpClient,
            CleanupZipExpander cleanupZipExpander,
            ScanPathsUtility scanPathsUtility,
            HttpUrl blackDuckServerUrl,
            OperatingSystemType operatingSystemType,
            File installDirectory
    ) {
        super(logger, blackDuckServerUrl, scanPathsUtility, installDirectory, ApiScannerInstaller.BLACK_DUCK_SIGNATURE_SCANNER_INSTALL_DIRECTORY, ToolsApiScannerInstaller.SCAN_CLI_VERSION_FILENAME);
        this.blackDuckHttpClient = blackDuckHttpClient;
        this.cleanupZipExpander = cleanupZipExpander;
        this.operatingSystemType = operatingSystemType;
    }

    protected HttpUrl getDownloadUrl() throws BlackDuckIntegrationException {
        HttpUrl blackDuckServerUrl = getBlackDuckServerUrl();
        StringBuilder url = new StringBuilder(blackDuckServerUrl.string());
        if (!blackDuckServerUrl.string().endsWith("/")) {
            url.append("/");
            url.append(LATEST_SCAN_CLI_TOOL_DOWNLOAD_URL);
        }

        String platform;
        if (OperatingSystemType.MAC == operatingSystemType) {
            platform = MAC_PLATFORM_PARAMETER_VALUE;
        } else if (OperatingSystemType.WINDOWS == operatingSystemType) {
            platform = WINDOWS_PLATFORM_PARAMETER_VALUE;
        } else {
            platform = LINUX_PLATFORM_PARAMETER_VALUE;
        }

        url.append(PLATFORM_PARAMETER_KEY);
        url.append("/");
        url.append(platform);

        try {
            return new HttpUrl(url.toString());
        } catch (IntegrationException e) {
            throw new BlackDuckIntegrationException(String.format("The Black Duck Signature Scanner url (%s) is not valid.", url));
        }
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

        BlackDuckRequest<BlackDuckResponse, UrlSingleResponse<BlackDuckResponse>> downloadRequest = BlackDuckRequest.createSingleRequest(
                requestBuilder,
                downloadUrl,
                BlackDuckResponse.class
        );

        try (Response response = blackDuckHttpClient.execute(downloadRequest)) {
            if (response.isStatusCodeSuccess()) {
                String latestScannerVersion = response.getHeaderValue("Version");
                if (!localScannerVersion.isEmpty()) {
                    getLogger().info(String.format("The signature scanner should be downloaded. Locally installed signature scanner is %s, but the latest version is %s", localScannerVersion, latestScannerVersion));
                }
                getLogger().info("Downloading the Black Duck Signature Scanner.");
                // if response is 200 OK then we got the bits for download so do the unzipping.
                try (InputStream responseStream = response.getContent()) {
                    getLogger().info(String.format(
                            "If the Signature Scanner on your Black Duck server has changed, the contents of %s may change which could involve deleting files - please do not place items in the expansion directory as this directory is assumed to be under blackduck-common control.",
                            scannerExpansionDirectory.getAbsolutePath()));
                    cleanupZipExpander.expand(responseStream, scannerExpansionDirectory);
                }

                setExecutableFilePermissions(scannerExpansionDirectory);

                getLogger().info("Black Duck Signature Scanner downloaded successfully.");
                return latestScannerVersion;
            } else if (response.getStatusCode() == 304) {
                // If no need to update, response is HTTP 304 Not modified
                getLogger().debug("Locally installed Signature Scanner version is up to date - skipping download.");
                return localScannerVersion;
            } else {
                getLogger().debug("Unable to download Signature Scanner. Response code: " + response.getStatusCode() + " " + response.getStatusMessage());
                throw new IntegrationException("Unable to download Black Duck Signature Scanner. Response code: " + response.getStatusCode() + " " + response.getStatusMessage());
            }
        }
    }

    @Override
    protected void postInstall(File scannerExpansionDirectory) throws BlackDuckIntegrationException {
        // Deletes legacy version file that may be left behind if client had previously installed scan-cli using ZipApiScannerInstaller
        try {
            File oldVersionFile = new File(scannerExpansionDirectory, ZipApiScannerInstaller.VERSION_FILENAME);
            Files.deleteIfExists(oldVersionFile.toPath());
        } catch (Exception e) {
            getLogger().trace("Could not delete old Signature Scanner version file.");
        }
    }
}
