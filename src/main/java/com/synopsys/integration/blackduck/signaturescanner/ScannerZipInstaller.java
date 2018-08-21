/**
 * hub-common
 * <p>
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 * <p>
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.blackduck.signaturescanner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import com.synopsys.integration.blackduck.configuration.HubServerConfig;
import com.synopsys.integration.blackduck.rest.BlackduckRestConnection;
import com.synopsys.integration.exception.EncryptionException;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.connection.RestConnection;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.request.Response;
import com.synopsys.integration.util.CleanupZipExpander;

public class ScannerZipInstaller {
    public static final String DEFAULT_SIGNATURE_SCANNER_DOWNLOAD_URL_SUFFIX = "download/scan.cli.zip";
    public static final String WINDOWS_SIGNATURE_SCANNER_DOWNLOAD_URL_SUFFIX = "download/scan.cli-windows.zip";
    public static final String MAC_SIGNATURE_SCANNER_DOWNLOAD_URL_SUFFIX = "download/scan.cli-macosx.zip";

    public static final String BLACK_DUCK_SIGNATURE_SCANNER_INSTALL_DIRECTORY = "Black_Duck_Scan_Installation";
    public static final String VERSION_FILENAME = "blackDuckVersion.txt";

    private final IntLogger logger;
    private final RestConnection restConnection;
    private final CleanupZipExpander cleanupZipExpander;
    private final String blackDuckServerUrl;

    public static ScannerZipInstaller defaultUtility(final IntLogger logger, final HubServerConfig hubServerConfig) throws EncryptionException {
        final BlackduckRestConnection restConnection = hubServerConfig.createRestConnection(logger);
        final CleanupZipExpander cleanupZipExpander = new CleanupZipExpander(logger);
        return new ScannerZipInstaller(logger, restConnection, cleanupZipExpander, hubServerConfig.getHubUrl().toString());
    }

    public ScannerZipInstaller(final IntLogger logger, final RestConnection restConnection, final CleanupZipExpander cleanupZipExpander, final String blackDuckServerUrl) {
        if (StringUtils.isBlank(blackDuckServerUrl)) {
            throw new IllegalArgumentException("A Black Duck server url must be provided.");
        }

        this.logger = logger;
        this.restConnection = restConnection;
        this.cleanupZipExpander = cleanupZipExpander;
        this.blackDuckServerUrl = blackDuckServerUrl;
    }

    /**
     * The Black Duck Signature Scanner will be download if it has not
     * previously been downloaded or if it has been updated on the server. The
     * absolute path to the install location will be returned if it was
     * downloaded or found successfully, otherwise an Optional.empty will be
     * returned and the log will contain details concerning the failure.
     */
    public Optional<String> retrieveBlackDuckScanInstallPath(final File installDirectory) {
        installDirectory.mkdirs();

        File versionFile = null;
        try {
            versionFile = retrieveVersionFile(installDirectory);
        } catch (final IOException e) {
            logger.error("Could not create the version file: " + e.getMessage());
            return Optional.empty();
        }

        final String downloadUrl = getDownloadUrl();
        return retrieveBlackDuckScanInstallPath(installDirectory, versionFile, downloadUrl);
    }

    public Optional<String> retrieveBlackDuckScanInstallPath(final File installDirectory, final File versionFile, final String downloadUrl) {
        installDirectory.mkdirs();

        try {
            downloadIfModified(installDirectory, versionFile, downloadUrl);
        } catch (final Exception e) {
            logger.error("The Black Duck Signature Scanner could not be downloaded successfully: " + e.getMessage());
        }

        if (installDirectory != null && installDirectory.exists() && installDirectory.isDirectory()) {
            logger.info("The Black Duck Signature Scanner downloaded/found successfully: " + installDirectory.getAbsolutePath());
            return Optional.of(installDirectory.getAbsolutePath());
        }

        logger.error("The Black Duck Signature Scanner executable could not be found or downloaded - check the logs for any errors.");
        return Optional.empty();
    }

    public File retrieveVersionFile(final File installDirectory) throws IOException {
        installDirectory.mkdirs();

        final File versionFile = new File(installDirectory, VERSION_FILENAME);
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

        if (SystemUtils.IS_OS_MAC) {
            url.append(MAC_SIGNATURE_SCANNER_DOWNLOAD_URL_SUFFIX);
        } else if (SystemUtils.IS_OS_WINDOWS) {
            url.append(WINDOWS_SIGNATURE_SCANNER_DOWNLOAD_URL_SUFFIX);
        } else {
            url.append(DEFAULT_SIGNATURE_SCANNER_DOWNLOAD_URL_SUFFIX);
        }

        return url.toString();
    }

    private void downloadIfModified(final File installDirectory, final File versionFile, final String downloadUrl) throws IOException, IntegrationException, ArchiveException {
        final long lastTimeDownloaded = versionFile.lastModified();
        logger.debug(String.format("last time downloaded: %d", lastTimeDownloaded));

        final Request downloadRequest = new Request.Builder(downloadUrl).build();
        final Optional<Response> optionalResponse = restConnection.executeGetRequestIfModifiedSince(downloadRequest, lastTimeDownloaded);
        if (optionalResponse.isPresent()) {
            final Response response = optionalResponse.get();
            try {
                logger.info("Downloading the Black Duck Signature Scanner.");
                try (InputStream responseStream = response.getContent()) {
                    cleanupZipExpander.expand(responseStream, installDirectory);
                }
                final long lastModifiedOnServer = response.getLastModified();
                versionFile.setLastModified(lastModifiedOnServer);

                logger.info(String.format("Black Duck Signature Scanner downloaded successfully."));
            } finally {
                response.close();
            }
        } else {
            logger.debug("The Black Duck Signature Scanner has not been modified since it was last downloaded - skipping download.");
        }
    }

}
