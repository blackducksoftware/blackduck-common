/*
 * Copyright (C) 2016 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.hub.cli;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;

import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.global.HubProxyInfo;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.util.AuthenticatorUtil;
import com.blackducksoftware.integration.util.CIEnvironmentVariables;

public class CLIDownloadService {
    private final IntLogger logger;

    public CLIDownloadService(IntLogger logger) {
        this.logger = logger;
    }

    public void performInstallation(HubProxyInfo hubProxyInfo, final CLILocation cliLocation, final CIEnvironmentVariables ciEnvironmentVariables,
            String hubUrl, String hubVersion, final String localHostName)
            throws IOException, InterruptedException, BDRestException, URISyntaxException, HubIntegrationException, IllegalArgumentException,
            EncryptionException {
        if (StringUtils.isBlank(localHostName)) {
            throw new IllegalArgumentException("You must provided the hostName of the machine this is running on.");
        }

        final String cliDownloadUrl = cliLocation.getCLIDownloadUrl(logger, hubUrl);
        if (StringUtils.isNotBlank(cliDownloadUrl)) {
            customInstall(hubProxyInfo, cliLocation, ciEnvironmentVariables, new URL(cliDownloadUrl), hubVersion, localHostName);
        } else {
            logger.error("Could not find the correct Hub CLI download URL.");
        }
    }

    public void customInstall(HubProxyInfo hubProxyInfo, CLILocation cliLocation, CIEnvironmentVariables ciEnvironmentVariables, final URL archive,
            String hubVersion, final String localHostName)
            throws IOException, InterruptedException, HubIntegrationException, IllegalArgumentException, EncryptionException {
        boolean cliMismatch = true;
        try {
            final File hubVersionFile = cliLocation.createHubVersionFile();
            if (hubVersionFile.exists()) {
                final String storedHubVersion = IOUtils.toString(new FileReader(hubVersionFile));
                if (hubVersion.equals(storedHubVersion)) {
                    cliMismatch = false;
                } else {
                    hubVersionFile.delete();
                    hubVersionFile.createNewFile();
                }
            }
            final File cliInstallDirectory = cliLocation.getCLIInstallDir();
            if (!cliInstallDirectory.exists()) {
                cliMismatch = true;
            }

            if (cliMismatch) {
                logger.debug("Attempting to download the Hub CLI.");
                final FileWriter writer = new FileWriter(hubVersionFile);
                writer.write(hubVersion);
                writer.close();
                hubVersionFile.setLastModified(0L);
            }
            final long cliTimestamp = hubVersionFile.lastModified();

            URLConnection connection = null;
            try {
                Proxy proxy = null;
                if (hubProxyInfo != null) {
                    String proxyHost = hubProxyInfo.getHost();
                    int proxyPort = hubProxyInfo.getPort();
                    String proxyUsername = hubProxyInfo.getUsername();
                    String proxyPassword = hubProxyInfo.getDecryptedPassword();

                    if (StringUtils.isNotBlank(proxyHost) && proxyPort > 0) {
                        proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
                    }
                    if (proxy != null) {
                        if (StringUtils.isNotBlank(proxyUsername) && StringUtils.isNotBlank(proxyPassword)) {
                            AuthenticatorUtil.setAuthenticator(proxyUsername, proxyPassword);
                        } else {
                            AuthenticatorUtil.resetAuthenticator();
                        }
                    }
                }
                if (proxy != null) {
                    connection = archive.openConnection(proxy);
                } else {
                    connection = archive.openConnection();
                }
                connection.setIfModifiedSince(cliTimestamp);
                connection.connect();
            } catch (final IOException ioe) {
                logger.error("Skipping installation of " + archive + " to " + cliLocation.getCanonicalPath() + ": "
                        + ioe.toString());
                return;
            }

            if (connection instanceof HttpURLConnection
                    && ((HttpURLConnection) connection).getResponseCode() == HttpURLConnection.HTTP_NOT_MODIFIED) {
                // CLI has not been modified
                return;
            }

            final long sourceTimestamp = connection.getLastModified();

            if (cliInstallDirectory.exists() && cliInstallDirectory.listFiles().length > 0) {
                if (!cliMismatch && sourceTimestamp == cliTimestamp) {
                    logger.debug("The current Hub CLI is up to date.");
                    return;
                }
                for (final File file : cliInstallDirectory.listFiles()) {
                    FileUtils.deleteDirectory(file);
                }
            } else {
                cliInstallDirectory.mkdir();
            }
            logger.debug("Updating the Hub CLI.");
            hubVersionFile.setLastModified(sourceTimestamp);

            logger.info("Unpacking " + archive.toString() + " to " + cliInstallDirectory.getCanonicalPath() + " on "
                    + localHostName);

            final CountingInputStream cis = new CountingInputStream(connection.getInputStream());
            try {
                unzip(cliInstallDirectory, cis, logger);
                updateJreSecurity(logger, cliLocation, ciEnvironmentVariables);
            } catch (final IOException e) {
                throw new IOException(String.format("Failed to unpack %s (%d bytes read of total %d)", archive,
                        cis.getByteCount(), connection.getContentLength()), e);
            }
        } catch (final IOException e) {
            throw new IOException("Failed to install " + archive + " to " + cliLocation.getCanonicalPath(), e);
        }
    }

    private void updateJreSecurity(final IntLogger logger, CLILocation cliLocation, CIEnvironmentVariables ciEnvironmentVariables) throws IOException {
        final String cacertsFilename = "cacerts";
        if (ciEnvironmentVariables.containsKey(CIEnvironmentVariables.BDS_CACERTS_OVERRIDE)) {
            final File securityDirectory = cliLocation.getJreSecurityDirectory();
            if (securityDirectory == null) {
                // the cli might not have the jre included
                return;
            }
            final String customCacertsPath = ciEnvironmentVariables
                    .getValue(CIEnvironmentVariables.BDS_CACERTS_OVERRIDE);
            final File customCacerts = new File(customCacertsPath);

            final File cacerts = new File(securityDirectory, cacertsFilename);
            final File cacertsBackup = new File(securityDirectory, cacertsFilename + System.currentTimeMillis());

            try {
                FileUtils.moveFile(cacerts, cacertsBackup);
                FileUtils.copyFile(customCacerts, cacerts);
            } catch (final IOException e) {
                logger.error("Could not copy the custom cacerts file from: " + customCacertsPath + " to: "
                        + cacerts.getAbsolutePath() + " msg: " + e.getMessage());
                throw e;
            }
        }
    }

    private void unzip(final File dir, final InputStream in, final IntLogger logger) throws IOException {
        // uses java.io.tmpdir
        final File tmpFile = File.createTempFile("tmpzip", null);

        try {
            copyInputStreamToFile(in, tmpFile);
            unzip(dir, tmpFile, logger);
        } finally {
            tmpFile.delete();
        }
    }

    private void unzip(File dir, final File zipFile, final IntLogger logger) throws IOException {
        // without getAbsoluteFile, getParentFile below seems to fail
        dir = dir.getAbsoluteFile();
        final ZipFile zip = new ZipFile(zipFile);
        final Enumeration<ZipEntry> entries = zip.getEntries();
        try {
            while (entries.hasMoreElements()) {
                final ZipEntry e = entries.nextElement();
                final File f = new File(dir, e.getName());
                if (e.isDirectory()) {
                    f.mkdirs();
                } else {
                    final File p = f.getParentFile();
                    if (p != null) {
                        p.mkdirs();
                    }
                    final InputStream input = zip.getInputStream(e);
                    try {
                        copyInputStreamToFile(input, f);
                    } finally {
                        input.close();
                    }
                    f.setLastModified(e.getTime());
                }
            }
        } finally {
            zip.close();
        }
    }

    private void copyInputStreamToFile(final InputStream in, final File f) throws IOException {
        final FileOutputStream fos = new FileOutputStream(f);
        try {
            org.apache.commons.io.IOUtils.copy(in, fos);
        } finally {
            org.apache.commons.io.IOUtils.closeQuietly(fos);
        }
    }

}
