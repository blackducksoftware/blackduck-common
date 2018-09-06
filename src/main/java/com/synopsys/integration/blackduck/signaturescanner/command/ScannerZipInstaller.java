/**
 * hub-common
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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
 */
package com.synopsys.integration.blackduck.signaturescanner.command;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.blackduck.configuration.HubServerConfig;
import com.synopsys.integration.blackduck.exception.HubIntegrationException;
import com.synopsys.integration.blackduck.rest.BlackduckRestConnection;
import com.synopsys.integration.exception.EncryptionException;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.connection.RestConnection;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.request.Response;
import com.synopsys.integration.util.CleanupZipExpander;
import com.synopsys.integration.util.OperatingSystemType;

public class ScannerZipInstaller {
    public static final String DEFAULT_SIGNATURE_SCANNER_DOWNLOAD_URL_SUFFIX = "download/scan.cli.zip";
    public static final String WINDOWS_SIGNATURE_SCANNER_DOWNLOAD_URL_SUFFIX = "download/scan.cli-windows.zip";
    public static final String MAC_SIGNATURE_SCANNER_DOWNLOAD_URL_SUFFIX = "download/scan.cli-macosx.zip";

    public static final String BLACK_DUCK_SIGNATURE_SCANNER_INSTALL_DIRECTORY = "Black_Duck_Scan_Installation";
    public static final String VERSION_FILENAME = "blackDuckVersion.txt";

    private final IntLogger logger;
    private final RestConnection restConnection;
    private final CleanupZipExpander cleanupZipExpander;
    private final ScanPathsUtility scanPathsUtility;
    private final String blackDuckServerUrl;
    private final OperatingSystemType operatingSystemType;

    public static ScannerZipInstaller defaultUtility(final IntLogger logger, final HubServerConfig hubServerConfig, final OperatingSystemType operatingSystemType) throws EncryptionException {
        final ScanPathsUtility scanPathsUtility = new ScanPathsUtility(logger, operatingSystemType);
        return defaultUtility(logger, hubServerConfig, scanPathsUtility, operatingSystemType);
    }

    public static ScannerZipInstaller defaultUtility(final IntLogger logger, final HubServerConfig hubServerConfig, final ScanPathsUtility scanPathsUtility, final OperatingSystemType operatingSystemType) throws EncryptionException {
        final BlackduckRestConnection restConnection = hubServerConfig.createRestConnection(logger);
        final CleanupZipExpander cleanupZipExpander = new CleanupZipExpander(logger);
        return new ScannerZipInstaller(logger, restConnection, cleanupZipExpander, scanPathsUtility, hubServerConfig.getHubUrl().toString(), operatingSystemType);
    }

    public ScannerZipInstaller(final IntLogger logger, final RestConnection restConnection, final CleanupZipExpander cleanupZipExpander, final ScanPathsUtility scanPathsUtility, final String blackDuckServerUrl,
            final OperatingSystemType operatingSystemType) {
        if (StringUtils.isBlank(blackDuckServerUrl)) {
            throw new IllegalArgumentException("A Black Duck server url must be provided.");
        }

        this.logger = logger;
        this.restConnection = restConnection;
        this.cleanupZipExpander = cleanupZipExpander;
        this.scanPathsUtility = scanPathsUtility;
        this.blackDuckServerUrl = blackDuckServerUrl;
        this.operatingSystemType = operatingSystemType;
    }

    /**
     * The Black Duck Signature Scanner will be download if it has not
     * previously been downloaded or if it has been updated on the server. The
     * absolute path to the install location will be returned if it was
     * downloaded or found successfully, otherwise an Optional.empty will be
     * returned and the log will contain details concerning the failure.
     */
    public void installOrUpdateScanner(final File installDirectory) throws HubIntegrationException {
        final File scannerExpansionDirectory = new File(installDirectory, BLACK_DUCK_SIGNATURE_SCANNER_INSTALL_DIRECTORY);
        scannerExpansionDirectory.mkdirs();

        File versionFile = null;
        try {
            versionFile = retrieveVersionFile(scannerExpansionDirectory);
        } catch (final IOException e) {
            throw new HubIntegrationException("Trying to install the scanner but could not create the version file: " + e.getMessage());
        }

        final String downloadUrl = getDownloadUrl();
        try {
            downloadIfModified(scannerExpansionDirectory, versionFile, downloadUrl);
        } catch (final Exception e) {
            throw new HubIntegrationException("The Black Duck Signature Scanner could not be downloaded successfully: " + e.getMessage());
        }

        logger.info("The Black Duck Signature Scanner downloaded/found successfully: " + installDirectory.getAbsolutePath());
    }

    private File retrieveVersionFile(final File scannerExpansionDirectory) throws IOException {
        final File versionFile = new File(scannerExpansionDirectory, VERSION_FILENAME);
        if (!versionFile.exists()) {
            logger.info("The version file has not been created yet so creating it now.");
            versionFile.createNewFile();
            versionFile.setLastModified(0L);
        }

        return versionFile;
    }

    private String getDownloadUrl() {
        final StringBuilder url = new StringBuilder(blackDuckServerUrl);
        if (!blackDuckServerUrl.endsWith("/")) {
            url.append("/");
        }

        if (OperatingSystemType.MAC == operatingSystemType) {
            url.append(MAC_SIGNATURE_SCANNER_DOWNLOAD_URL_SUFFIX);
        } else if (OperatingSystemType.WINDOWS == operatingSystemType) {
            url.append(WINDOWS_SIGNATURE_SCANNER_DOWNLOAD_URL_SUFFIX);
        } else {
            url.append(DEFAULT_SIGNATURE_SCANNER_DOWNLOAD_URL_SUFFIX);
        }

        return url.toString();
    }

    private void downloadIfModified(final File scannerExpansionDirectory, final File versionFile, final String downloadUrl) throws IOException, IntegrationException, ArchiveException {
        final long lastTimeDownloaded = versionFile.lastModified();
        logger.debug(String.format("last time downloaded: %d", lastTimeDownloaded));

        final Request downloadRequest = new Request.Builder(downloadUrl).build();
        final Optional<Response> optionalResponse = restConnection.executeGetRequestIfModifiedSince(downloadRequest, lastTimeDownloaded);
        if (optionalResponse.isPresent()) {
            final Response response = optionalResponse.get();
            try {
                logger.info("Downloading the Black Duck Signature Scanner.");
                try (InputStream responseStream = response.getContent()) {
                    cleanupZipExpander.expand(responseStream, scannerExpansionDirectory);
                }
                final long lastModifiedOnServer = response.getLastModified();
                versionFile.setLastModified(lastModifiedOnServer);

                final ScanPaths scanPaths = scanPathsUtility.determineSignatureScannerPaths(scannerExpansionDirectory.getParentFile());
                final File javaExecutable = new File(scanPaths.getPathToJavaExecutable());
                final File oneJar = new File(scanPaths.getPathToOneJar());
                final File scanExecutable = new File(scanPaths.getPathToScanExecutable());
                javaExecutable.setExecutable(true);
                oneJar.setExecutable(true);
                scanExecutable.setExecutable(true);

                logger.info(String.format("Black Duck Signature Scanner downloaded successfully."));
            } finally {
                response.close();
            }
        } else {
            logger.debug("The Black Duck Signature Scanner has not been modified since it was last downloaded - skipping download.");
        }
    }

}
