/*
 * blackduck-common
 *
 * Copyright (c) 2024 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.codelocation.signaturescanner.command;

import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpUrl;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

public abstract class ApiScannerInstaller implements ScannerInstaller {
    public static final String BLACK_DUCK_SIGNATURE_SCANNER_INSTALL_DIRECTORY = "Black_Duck_Scan_Installation";
    private final IntLogger logger;
    private final HttpUrl blackDuckServerUrl;
    private final ScanPathsUtility scanPathsUtility;
    private final File installDirectory;
    private final String signatureScannerInstallDirectory;
    private final String scanCliVersionFileName;

    protected ApiScannerInstaller( IntLogger logger,  HttpUrl blackDuckServerUrl,  ScanPathsUtility scanPathsUtility,  File installDirectory, String signatureScannerInstallDirectory,  String scanCliVersionFileName) {
        if (null == blackDuckServerUrl) {
            throw new IllegalArgumentException("A Black Duck server url must be provided.");
        }
        this.logger = logger;
        this.blackDuckServerUrl = blackDuckServerUrl;
        this.scanPathsUtility = scanPathsUtility;
        this.installDirectory = installDirectory;
        this.signatureScannerInstallDirectory = signatureScannerInstallDirectory;
        this.scanCliVersionFileName = scanCliVersionFileName;
    }

    protected abstract void postInstall(File scannerExpansionDirectory) throws BlackDuckIntegrationException;

    protected abstract HttpUrl getDownloadUrl() throws BlackDuckIntegrationException;

    protected abstract String downloadSignatureScanner(File scannerExpansionDirectory, HttpUrl downloadUrl, String localScannerVersion) throws IOException, IntegrationException, ArchiveException;

    /**
     * The Black Duck Signature Scanner (scan-cli) will be downloaded if it has not
     * previously been downloaded or if it has been updated on the server. The
     * absolute path to the install location will be returned if it was
     * downloaded or found successfully, otherwise an Optional.empty will be
     * returned and the log will contain details concerning the failure.
     */
    public File installOrUpdateScanner() throws BlackDuckIntegrationException {
        File installDirectory = getInstallDirectory();
        if (installDirectory.exists()) {
            try {
                ScanPaths scanPaths = getScanPathsUtility().searchForScanPaths(installDirectory);
                if (!scanPaths.isManagedByLibrary()) {
                    return installDirectory;
                }
            } catch (BlackDuckIntegrationException ignored) {
                // a valid scanPaths could not be found so we will need to attempt an install
            }
        }

        File scannerExpansionDirectory = new File(installDirectory, getSignatureScannerInstallDirectory());
        boolean directoryMade = scannerExpansionDirectory.mkdirs();
        if (directoryMade) {
            getLogger().debug("Created scanner expansion directory: " + scannerExpansionDirectory.getAbsolutePath());
        } else {
            getLogger().error("Could not create scanner expansion directory: " + scannerExpansionDirectory.getAbsolutePath());
        }

        File versionFile = new File(scannerExpansionDirectory, getScanCliVersionFileName());
        HttpUrl downloadUrl = getDownloadUrl();
        try {
            String scannerVersion;
            if (!versionFile.exists()) {
                getLogger().info("No version file exists, assuming this is new installation and the signature scanner should be downloaded.");
                scannerVersion = downloadSignatureScanner(scannerExpansionDirectory, downloadUrl, "");
                // Version file creation should happen after successful download
                getLogger().debug("The version file has not been created yet so creating it now.");
                FileUtils.writeStringToFile(versionFile, scannerVersion, Charset.defaultCharset());
                return installDirectory;
            }
            // A version file exists, so we have to compare to determine if a download should occur.
            String localScannerVersion = FileUtils.readFileToString(versionFile, Charset.defaultCharset());
            getLogger().debug(String.format("Locally installed signature scanner version: %s", localScannerVersion));
            scannerVersion = downloadSignatureScanner(scannerExpansionDirectory, downloadUrl, localScannerVersion);
            // Update version file if needed
            if (!localScannerVersion.equals(scannerVersion)) {
                FileUtils.writeStringToFile(versionFile, scannerVersion, Charset.defaultCharset());
            }
        } catch (Exception e) {
            throw new BlackDuckIntegrationException("The Black Duck Signature Scanner could not be downloaded successfully: " + e.getMessage(), e);
        } finally {
            postInstall(scannerExpansionDirectory);
        }

        getLogger().info("The Black Duck Signature Scanner downloaded/found successfully: " + installDirectory.getAbsolutePath());
        return installDirectory;
    }

    protected ScanPaths setExecutableFilePermissions(File scannerExpansionDirectory) throws BlackDuckIntegrationException {
        ScanPaths scanPaths = getScanPathsUtility().searchForScanPaths(scannerExpansionDirectory.getParentFile());
        List<String> executablePaths = Arrays.asList(
            scanPaths.getPathToJavaExecutable(),
            scanPaths.getPathToOneJar(),
            scanPaths.getPathToScanExecutable());
        executablePaths.stream()
            .map(File::new)
            .forEach(this::makeFileExecutable);
        return scanPaths;
    }

    private void makeFileExecutable(File executableFile) {
        boolean result = executableFile.setExecutable(true);
        if(result) {
            getLogger().debug(String.format("File %s now executable", executableFile));
        } else {
            getLogger().error(String.format("Failed to make file %s executable", executableFile));
        }
    }

    public IntLogger getLogger() {
        return logger;
    }

    public HttpUrl getBlackDuckServerUrl() {
        return blackDuckServerUrl;
    }

    public ScanPathsUtility getScanPathsUtility() {
        return scanPathsUtility;
    }

    public File getInstallDirectory() {
        return installDirectory;
    }

    public String getSignatureScannerInstallDirectory() {
        return signatureScannerInstallDirectory;
    }

    public String getScanCliVersionFileName() {
        return scanCliVersionFileName;
    }
}
