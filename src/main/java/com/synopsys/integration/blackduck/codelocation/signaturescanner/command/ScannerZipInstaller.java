/**
 * blackduck-common
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
package com.synopsys.integration.blackduck.codelocation.signaturescanner.command;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.blackduck.rest.BlackDuckHttpClient;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.client.IntHttpClient;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.request.Response;
import com.synopsys.integration.util.CleanupZipExpander;
import com.synopsys.integration.util.IntEnvironmentVariables;
import com.synopsys.integration.util.OperatingSystemType;

public class ScannerZipInstaller {
    public static final String DEFAULT_SIGNATURE_SCANNER_DOWNLOAD_URL_SUFFIX = "download/scan.cli.zip";
    public static final String WINDOWS_SIGNATURE_SCANNER_DOWNLOAD_URL_SUFFIX = "download/scan.cli-windows.zip";
    public static final String MAC_SIGNATURE_SCANNER_DOWNLOAD_URL_SUFFIX = "download/scan.cli-macosx.zip";

    public static final String BLACK_DUCK_SIGNATURE_SCANNER_INSTALL_DIRECTORY = "Black_Duck_Scan_Installation";
    public static final String VERSION_FILENAME = "blackDuckVersion.txt";

    private final IntLogger logger;
    private final IntHttpClient intHttpClient;
    private final CleanupZipExpander cleanupZipExpander;
    private final ScanPathsUtility scanPathsUtility;
    private final String blackDuckServerUrl;
    private final OperatingSystemType operatingSystemType;

    /**
     * @deprecated Please create the SignatureScannerService in BlackDuckServicesFactory
     */
    @Deprecated
    public static ScannerZipInstaller defaultUtility(IntLogger logger, BlackDuckServerConfig blackDuckServerConfig, IntEnvironmentVariables intEnvironmentVariables, OperatingSystemType operatingSystemType) {
        ScanPathsUtility scanPathsUtility = new ScanPathsUtility(logger, intEnvironmentVariables, operatingSystemType);
        return ScannerZipInstaller.defaultUtility(logger, blackDuckServerConfig, scanPathsUtility, operatingSystemType);
    }

    /**
     * @deprecated Please create the SignatureScannerService in BlackDuckServicesFactory
     */
    @Deprecated
    public static ScannerZipInstaller defaultUtility(IntLogger logger, BlackDuckServerConfig blackDuckServerConfig, ScanPathsUtility scanPathsUtility, OperatingSystemType operatingSystemType) {
        BlackDuckHttpClient blackDuckHttpClient = blackDuckServerConfig.createBlackDuckHttpClient(logger);
        CleanupZipExpander cleanupZipExpander = new CleanupZipExpander(logger);
        return new ScannerZipInstaller(logger, blackDuckHttpClient, cleanupZipExpander, scanPathsUtility, blackDuckServerConfig.getBlackDuckUrl().toString(), operatingSystemType);
    }

    public ScannerZipInstaller(IntLogger logger, IntHttpClient intHttpClient, CleanupZipExpander cleanupZipExpander, ScanPathsUtility scanPathsUtility, String blackDuckServerUrl,
            OperatingSystemType operatingSystemType) {
        if (StringUtils.isBlank(blackDuckServerUrl)) {
            throw new IllegalArgumentException("A Black Duck server url must be provided.");
        }

        this.logger = logger;
        this.intHttpClient = intHttpClient;
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
    public void installOrUpdateScanner(File installDirectory) throws BlackDuckIntegrationException {
        File scannerExpansionDirectory = new File(installDirectory, ScannerZipInstaller.BLACK_DUCK_SIGNATURE_SCANNER_INSTALL_DIRECTORY);
        scannerExpansionDirectory.mkdirs();

        File versionFile = null;
        try {
            versionFile = retrieveVersionFile(scannerExpansionDirectory);
        } catch (IOException e) {
            throw new BlackDuckIntegrationException("Trying to install the scanner but could not create the version file: " + e.getMessage());
        }

        String downloadUrl = getDownloadUrl();
        try {
            downloadIfModified(scannerExpansionDirectory, versionFile, downloadUrl);
        } catch (Exception e) {
            throw new BlackDuckIntegrationException("The Black Duck Signature Scanner could not be downloaded successfully: " + e.getMessage());
        }

        logger.info("The Black Duck Signature Scanner downloaded/found successfully: " + installDirectory.getAbsolutePath());
    }

    private File retrieveVersionFile(File scannerExpansionDirectory) throws IOException {
        File versionFile = new File(scannerExpansionDirectory, ScannerZipInstaller.VERSION_FILENAME);
        if (!versionFile.exists()) {
            logger.info("The version file has not been created yet so creating it now.");
            versionFile.createNewFile();
            versionFile.setLastModified(0L);
        }

        return versionFile;
    }

    private String getDownloadUrl() {
        StringBuilder url = new StringBuilder(blackDuckServerUrl);
        if (!blackDuckServerUrl.endsWith("/")) {
            url.append("/");
        }

        if (OperatingSystemType.MAC == operatingSystemType) {
            url.append(ScannerZipInstaller.MAC_SIGNATURE_SCANNER_DOWNLOAD_URL_SUFFIX);
        } else if (OperatingSystemType.WINDOWS == operatingSystemType) {
            url.append(ScannerZipInstaller.WINDOWS_SIGNATURE_SCANNER_DOWNLOAD_URL_SUFFIX);
        } else {
            url.append(ScannerZipInstaller.DEFAULT_SIGNATURE_SCANNER_DOWNLOAD_URL_SUFFIX);
        }

        return url.toString();
    }

    private void downloadIfModified(File scannerExpansionDirectory, File versionFile, String downloadUrl) throws IOException, IntegrationException, ArchiveException {
        long lastTimeDownloaded = versionFile.lastModified();
        logger.debug(String.format("last time downloaded: %d", lastTimeDownloaded));

        Request downloadRequest = new Request.Builder(downloadUrl).build();
        Optional<Response> optionalResponse = intHttpClient.executeGetRequestIfModifiedSince(downloadRequest, lastTimeDownloaded);
        if (optionalResponse.isPresent()) {
            Response response = optionalResponse.get();
            try {
                logger.info("Downloading the Black Duck Signature Scanner.");
                try (InputStream responseStream = response.getContent()) {
                    cleanupZipExpander.expand(responseStream, scannerExpansionDirectory);
                }
                long lastModifiedOnServer = response.getLastModified();
                versionFile.setLastModified(lastModifiedOnServer);

                ScanPaths scanPaths = scanPathsUtility.determineSignatureScannerPaths(scannerExpansionDirectory.getParentFile());
                File javaExecutable = new File(scanPaths.getPathToJavaExecutable());
                File oneJar = new File(scanPaths.getPathToOneJar());
                File scanExecutable = new File(scanPaths.getPathToScanExecutable());
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
