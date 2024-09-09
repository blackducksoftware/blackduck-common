/*
 * blackduck-common
 *
 * Copyright (c) 2024 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.codelocation.signaturescanner.command;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.cert.Certificate;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.blackduck.integration.blackduck.exception.BlackDuckIntegrationException;
import com.blackduck.integration.blackduck.http.client.SignatureScannerClient;
import com.blackduck.integration.blackduck.keystore.KeyStoreHelper;
import com.blackduck.integration.blackduck.service.dataservice.BlackDuckRegistrationService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.response.Response;
import com.synopsys.integration.util.CleanupZipExpander;
import com.synopsys.integration.util.OperatingSystemType;

public class ZipApiScannerInstaller extends ApiScannerInstaller {
    public static final String DEFAULT_SIGNATURE_SCANNER_DOWNLOAD_URL_SUFFIX = "download/scan.cli.zip";
    public static final String WINDOWS_SIGNATURE_SCANNER_DOWNLOAD_URL_SUFFIX = "download/scan.cli-windows.zip";
    public static final String MAC_SIGNATURE_SCANNER_DOWNLOAD_URL_SUFFIX = "download/scan.cli-macosx.zip";


    private final IntLogger logger;
    private final SignatureScannerClient signatureScannerClient;
    private final BlackDuckRegistrationService blackDuckRegistrationService;
    private final CleanupZipExpander cleanupZipExpander;
    private final ScanPathsUtility scanPathsUtility;
    private final KeyStoreHelper keyStoreHelper;
    private final HttpUrl blackDuckServerUrl;
    private final OperatingSystemType operatingSystemType;
    private final File installDirectory;

    public ZipApiScannerInstaller(
        IntLogger logger,
        SignatureScannerClient signatureScannerClient,
        BlackDuckRegistrationService blackDuckRegistrationService,
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
        this.blackDuckRegistrationService = blackDuckRegistrationService;
        this.cleanupZipExpander = cleanupZipExpander;
        this.scanPathsUtility = scanPathsUtility;
        this.keyStoreHelper = keyStoreHelper;
        this.blackDuckServerUrl = blackDuckServerUrl;
        this.operatingSystemType = operatingSystemType;
        this.installDirectory = installDirectory;
    }

    /**
     * The Black Duck Signature Scanner will be downloaded if it has not
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
            String connectedBlackDuckVersion = StringUtils.trim(blackDuckRegistrationService.getBlackDuckServerData().getVersion());

            if (!versionFile.exists()) {
                logger.info("No version file exists, assuming this is new installation and the signature scanner should be downloaded.");
                downloadSignatureScanner(scannerExpansionDirectory, downloadUrl, "");
                // Version file creation should happen after successful download
                logger.debug("The version file has not been created yet so creating it now.");
                FileUtils.writeStringToFile(versionFile, connectedBlackDuckVersion, Charset.defaultCharset());
                return installDirectory;
            }

            // A version file exists, so we have to compare to determine if a download should occur.
            String localScannerVersion = FileUtils.readFileToString(versionFile, Charset.defaultCharset());
            logger.debug(String.format("Locally installed signature scanner version: %s", localScannerVersion));

            if (!connectedBlackDuckVersion.equals(localScannerVersion)) {
                logger.info(String.format("The signature scanner should be downloaded. Locally installed signature scanner is %s, but the connected Black Duck version is %s", localScannerVersion, connectedBlackDuckVersion));
                downloadSignatureScanner(scannerExpansionDirectory, downloadUrl, "");
                FileUtils.writeStringToFile(versionFile, connectedBlackDuckVersion, Charset.defaultCharset());
            } else {
                logger.debug("The Black Duck Signature Scanner version matches the connected Black Duck version - skipping download.");
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

        if (OperatingSystemType.MAC == operatingSystemType) {
            url.append(ZipApiScannerInstaller.MAC_SIGNATURE_SCANNER_DOWNLOAD_URL_SUFFIX);
        } else if (OperatingSystemType.WINDOWS == operatingSystemType) {
            url.append(ZipApiScannerInstaller.WINDOWS_SIGNATURE_SCANNER_DOWNLOAD_URL_SUFFIX);
        } else {
            url.append(ZipApiScannerInstaller.DEFAULT_SIGNATURE_SCANNER_DOWNLOAD_URL_SUFFIX);
        }

        try {
            return new HttpUrl(url.toString());
        } catch (IntegrationException e) {
            throw new BlackDuckIntegrationException(String.format("The Black Duck Signature Scanner url (%s) is not valid.", url.toString()));
        }
    }

    protected String downloadSignatureScanner(File scannerExpansionDirectory, HttpUrl downloadUrl, String localScannerVersion) throws IOException, IntegrationException, ArchiveException {
        Request downloadRequest = new Request.Builder(downloadUrl).build();
        try (Response response = signatureScannerClient.executeGetRequest(downloadRequest)) {
            logger.info("Downloading the Black Duck Signature Scanner.");
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

            Certificate serverCertificate = signatureScannerClient.getServerCertificate();

            keyStoreHelper.updateKeyStoreWithServerCertificate(downloadUrl.url().getHost(), serverCertificate, scanPaths.getPathToCacerts());

            logger.info("Black Duck Signature Scanner downloaded successfully.");
        }
        return "";
    }

}
