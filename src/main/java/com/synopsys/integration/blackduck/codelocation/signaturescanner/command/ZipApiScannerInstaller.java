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
import java.security.cert.Certificate;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.blackduck.http.client.SignatureScannerClient;
import com.synopsys.integration.blackduck.keystore.KeyStoreHelper;
import com.synopsys.integration.blackduck.service.dataservice.BlackDuckRegistrationService;
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

    public static final String VERSION_FILENAME = "blackDuckVersion.txt";

    private final SignatureScannerClient signatureScannerClient;
    private final BlackDuckRegistrationService blackDuckRegistrationService;
    private final CleanupZipExpander cleanupZipExpander;
    private final KeyStoreHelper keyStoreHelper;
    private final OperatingSystemType operatingSystemType;

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
        super(logger, blackDuckServerUrl, scanPathsUtility, installDirectory,ApiScannerInstaller.BLACK_DUCK_SIGNATURE_SCANNER_INSTALL_DIRECTORY, ZipApiScannerInstaller.VERSION_FILENAME);

        this.signatureScannerClient = signatureScannerClient;
        this.blackDuckRegistrationService = blackDuckRegistrationService;
        this.cleanupZipExpander = cleanupZipExpander;
        this.keyStoreHelper = keyStoreHelper;
        this.operatingSystemType = operatingSystemType;
    }

    protected HttpUrl getDownloadUrl() throws BlackDuckIntegrationException {
        HttpUrl blackDuckServerUrl = getBlackDuckServerUrl();
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
            getLogger().info("Downloading the Black Duck Signature Scanner.");
            try (InputStream responseStream = response.getContent()) {
                getLogger().info(String.format(
                    "If your Black Duck server has changed, the contents of %s may change which could involve deleting files - please do not place items in the expansion directory as this directory is assumed to be under blackduck-common control.",
                    scannerExpansionDirectory.getAbsolutePath()));
                cleanupZipExpander.expand(responseStream, scannerExpansionDirectory);
            }

            ScanPaths scanPaths = setExecutableFilePermissions(scannerExpansionDirectory);

            Certificate serverCertificate = signatureScannerClient.getServerCertificate();

            keyStoreHelper.updateKeyStoreWithServerCertificate(downloadUrl.url().getHost(), serverCertificate, scanPaths.getPathToCacerts());

            getLogger().info("Black Duck Signature Scanner downloaded successfully.");
        }
        return StringUtils.trim(blackDuckRegistrationService.getBlackDuckServerData().getVersion());
    }

    @Override
    protected void postInstall(final File scannerExpansionDirectory) throws BlackDuckIntegrationException {
        // no tasks to perform post installation.
    }
}
