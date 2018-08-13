package com.synopsys.integration.hub.cli.simple;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Optional;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.hub.configuration.HubServerConfig;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.connection.RestConnection;
import com.synopsys.integration.rest.connection.UnauthenticatedRestConnection;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.request.Response;
import com.synopsys.integration.util.CleanupZipExpander;

public class SimpleScanDownloadUtility {
    public static final String LINUX_DOWNLOAD_URL = "/swip_cli-linux64.zip";
    public static final String WINDOWS_DOWNLOAD_URL = "/swip_cli-win64.zip";
    public static final String MAC_DOWNLOAD_URL = "/swip_cli-macosx.zip";

    public static final String BLACK_DUCK_CLI_INSTALL_DIRECTORY = "Black_Duck_Scan_Installation";
    public static final String VERSION_FILENAME = "blackDuckVersion.txt";

    private final IntLogger logger;
    private final RestConnection restConnection;
    private final CleanupZipExpander cleanupZipExpander;
    private final String blackDuckServerUrl;
    private final File installDirectory;

    public static SimpleScanDownloadUtility defaultUtility(final IntLogger logger, final HubServerConfig hubServerConfig, final File downloadTargetDirectory) {
        final UnauthenticatedRestConnection restConnection = new UnauthenticatedRestConnection(logger, hubServerConfig.getHubUrl(), hubServerConfig.getProxyInfo());
        final CleanupZipExpander cleanupZipExpander = new CleanupZipExpander(logger);
        return new SimpleScanDownloadUtility(logger, restConnection, cleanupZipExpander, hubServerConfig.getHubUrl().toString(), downloadTargetDirectory);
    }

    public SimpleScanDownloadUtility(final IntLogger logger, final RestConnection restConnection, final CleanupZipExpander cleanupZipExpander, final String blackDuckServerUrl, final File downloadTargetDirectory) {
        if (StringUtils.isBlank(blackDuckServerUrl)) {
            throw new IllegalArgumentException("A Black Duck server url must be provided.");
        }

        this.logger = logger;
        this.restConnection = restConnection;
        this.cleanupZipExpander = cleanupZipExpander;
        this.blackDuckServerUrl = blackDuckServerUrl;
        installDirectory = new File(downloadTargetDirectory, BLACK_DUCK_CLI_INSTALL_DIRECTORY);

        installDirectory.mkdirs();
        if (!installDirectory.exists() || !installDirectory.isDirectory() || !installDirectory.canWrite()) {
            throw new IllegalArgumentException("The provided directory must exist and be writable.");
        }
    }

    /**
     * The Black Duck CLI will be download if it has not previously been
     * downloaded or if it has been updated on the server. The absolute path to
     * the install location will be returned if it was downloaded or found
     * successfully, otherwise an Optional.empty will be returned and the log
     * will contain details concerning the failure.
     */
    public Optional<String> retrieveSwipCliExecutablePath() {
        File versionFile = null;
        try {
            versionFile = retrieveVersionFile();
        } catch (final IOException e) {
            logger.error("Could not create the version file: " + e.getMessage());
            return Optional.empty();
        }

        final String downloadUrl = getDownloadUrl();
        return retrieveSwipCliExecutablePath(versionFile, downloadUrl);
    }

    public Optional<String> retrieveSwipCliExecutablePath(final File versionFile, final String downloadUrl) {
        File binDirectory = null;
        try {
            binDirectory = downloadIfModified(versionFile, downloadUrl);
        } catch (final Exception e) {
            logger.error("The Swip CLI could not be downloaded successfully: " + e.getMessage());
        }

        if (binDirectory != null && binDirectory.exists() && binDirectory.isDirectory()) {
            try {
                final File swipCliExecutable = getSwipCli(binDirectory);
                logger.info("Swip CLI downloaded/found successfully: " + swipCliExecutable.getCanonicalPath());
                return Optional.of(swipCliExecutable.getCanonicalPath());
            } catch (final Exception e) {
                logger.error("The Swip CLI executable could not be found: " + e.getMessage());
            }
        }

        return Optional.empty();
    }

    public File retrieveVersionFile() throws IOException {
        final File versionFile = new File(installDirectory, VERSION_FILENAME);
        if (!versionFile.exists()) {
            logger.info("The version file has not been created yet so creating it now.");
            versionFile.createNewFile();
            versionFile.setLastModified(0L);
        }

        return versionFile;
    }

    public String getDownloadUrl() {
        if (SystemUtils.IS_OS_MAC) {
            return swipServerUrl + MAC_DOWNLOAD_URL;
        } else if (SystemUtils.IS_OS_WINDOWS) {
            return swipServerUrl + WINDOWS_DOWNLOAD_URL;
        } else {
            return swipServerUrl + LINUX_DOWNLOAD_URL;
        }
    }

    private File downloadIfModified(final File versionFile, final String downloadUrl) throws IOException, IntegrationException, ArchiveException {
        final long lastTimeDownloaded = versionFile.lastModified();
        logger.debug(String.format("last time downloaded: %d", lastTimeDownloaded));

        final Request downloadRequest = new Request.Builder(downloadUrl).build();
        final Optional<Response> optionalResponse = restConnection.executeGetRequestIfModifiedSince(downloadRequest, lastTimeDownloaded);
        if (optionalResponse.isPresent()) {
            final Response response = optionalResponse.get();
            try {
                logger.info("Downloading the Black Duck CLI.");
                try (InputStream responseStream = response.getContent()) {
                    cleanupZipExpander.expand(responseStream, installDirectory);
                }
                final long lastModifiedOnServer = response.getLastModified();
                versionFile.setLastModified(lastModifiedOnServer);

                logger.info(String.format("Swip CLI downloaded successfully."));

                return binDirectory;
            } finally {
                response.close();
            }
        } else {
            logger.debug("The Black Duck CLI has not been modified since it was last downloaded - skipping download.");
            return getBinDirectory();
        }
    }

    // since we know that we only allow a single directory in installDirectory,
    // that single directory IS the expanded archive
    private File getBinDirectory() throws IntegrationException {
        final File[] directories = installDirectory.listFiles(file -> file.isDirectory());
        if (directories == null || directories.length != 1) {
            throw new IntegrationException(String.format("The %s directory should only be modified by swip-common. Please delete all files from that directory and try again.", SWIP_CLI_INSTALL_DIRECTORY));
        }

        final File swipCliDirectory = directories[0];
        final File bin = new File(swipCliDirectory, "bin");

        return bin;
    }

    private void makeBinFilesExecutable(final File binDirectory) {
        Arrays.stream(binDirectory.listFiles()).forEach(file -> {
            file.setExecutable(true);
        });
    }

    private File getSwipCli(final File binDirectory) throws IntegrationException {
        String swipCliFilename = "swip_cli";
        if (SystemUtils.IS_OS_WINDOWS) {
            swipCliFilename += ".exe";
        }

        final File swipCli = new File(binDirectory, swipCliFilename);

        if (!swipCli.exists() || !swipCli.isFile() || !(swipCli.length() > 0L)) {
            throw new IntegrationException("The swip_cli does not appear to have been downloaded correctly - be sure to download it first.");
        }

        return swipCli;
    }

}
