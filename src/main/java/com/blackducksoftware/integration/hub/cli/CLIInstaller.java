package com.blackducksoftware.integration.hub.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;

import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.HubSupportHelper;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.suite.sdk.logging.IntLogger;

public class CLIInstaller {
    public static final String VERSION_FILE_NAME = "hubVersion.txt";

    public static final String CLI_UNZIP_DIR = "hubVersion.txt";

    private String proxyHost;

    private Integer proxyPort;

    private String proxyUserName;

    private String proxyPassword;

    public void performInstallation(File directoryToInstallTo, IntLogger logger, HubIntRestService restService) throws IOException,
            InterruptedException, BDRestException, URISyntaxException {
        String cliDownloadUrl = getCLIDownloadUrl(logger, restService);
        if (StringUtils.isNotBlank(cliDownloadUrl)) {
            customInstall(directoryToInstallTo, new URL(restService.getBaseUrl()), restService.getHubVersion(), logger);
        } else {
            logger.error("Could not find the correct Hub CLI download URL.");
        }
    }

    public String getCLIDownloadUrl(IntLogger logger, HubIntRestService restService) throws IOException, InterruptedException {
        try {
            HubSupportHelper hubSupport = new HubSupportHelper();

            hubSupport.checkHubSupport(restService, logger);

            if (SystemUtils.IS_OS_MAC_OSX && hubSupport.isJreProvidedSupport()) {
                return HubSupportHelper.getOSXCLIWrapperLink(restService.getBaseUrl());
            } else if (SystemUtils.IS_OS_WINDOWS && hubSupport.isJreProvidedSupport()) {
                return HubSupportHelper.getWindowsCLIWrapperLink(restService.getBaseUrl());
            } else {
                return HubSupportHelper.getLinuxCLIWrapperLink(restService.getBaseUrl());
            }
        } catch (URISyntaxException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    private boolean customInstall(File directory, URL archive, String hubVersion, IntLogger logger) throws IOException, InterruptedException {
        try {
            if (!directory.exists()) {
                directory.mkdirs();
            }

            boolean cliMismatch = true;
            // For some reason the Hub returns the Version inside ""'s
            hubVersion = hubVersion.replace("\"", "");
            File hubVersionFile = new File(directory, VERSION_FILE_NAME);
            if (hubVersionFile.exists()) {
                String storedHubVersion = IOUtils.toString(new FileInputStream(hubVersionFile));
                if (hubVersion.equals(storedHubVersion)) {
                    cliMismatch = false;
                } else {
                    hubVersionFile.delete();
                }
            }
            if (cliMismatch) {
                hubVersionFile.createNewFile();
                FileWriter writer = new FileWriter(hubVersionFile);
                writer.write(hubVersion);
                writer.close();
            }
            URLConnection connection = null;
            try {
                Proxy proxy = null;

                if (StringUtils.isNotBlank(proxyHost)) {
                    proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
                }
                if (proxy != null) {

                    if (StringUtils.isNotBlank(proxyUserName) && StringUtils.isNotBlank(proxyPassword)) {
                        Authenticator.setDefault(
                                new Authenticator() {
                                    @Override
                                    public PasswordAuthentication getPasswordAuthentication() {
                                        return new PasswordAuthentication(
                                                proxyUserName,
                                                proxyPassword.toCharArray());
                                    }
                                }
                                );
                    } else {
                        Authenticator.setDefault(null);
                    }
                }
                if (proxy != null) {
                    connection = archive.openConnection(proxy);
                } else {
                    connection = archive.openConnection();
                }
                connection.connect();
            } catch (IOException ioe) {
                logger.error("Skipping installation of " + archive + " to " + directory.getCanonicalPath() + ": " + ioe.toString());
                return false;
            }

            if (connection instanceof HttpURLConnection
                    && ((HttpURLConnection) connection).getResponseCode() == HttpURLConnection.HTTP_NOT_MODIFIED) {
                return false;
            }

            if (!cliMismatch)
            {
                return false; // already up to date
            }
            File cliInstallDirectory = new File(directory, CLI_UNZIP_DIR);
            if (cliInstallDirectory.exists()) {
                // delete directory contents
                deleteFilesRecursive(cliInstallDirectory.listFiles());
            } else {
                cliInstallDirectory.mkdir();
            }

            logger.info("Unpacking " + archive.toString() + " to " + cliInstallDirectory.getCanonicalPath() + " on " + "TODO GET MACHINE NAME");

            InputStream in = connection.getInputStream();
            CountingInputStream cis = new CountingInputStream(in);

            try {
                unzip(cliInstallDirectory, cis, logger);
            } catch (IOException e) {
                throw new IOException(String.format("Failed to unpack %s (%d bytes read of total %d)",
                        archive, cis.getByteCount(), connection.getContentLength()), e);
            }
            return true;
        } catch (IOException e) {
            throw new IOException("Failed to install " + archive + " to " + directory.getCanonicalPath(), e);
        }
    }

    public void deleteFilesRecursive(File[] files) {
        if (files != null && files.length > 0) {
            for (File currentFile : files) {
                if (currentFile != null && currentFile.exists()) {
                    if (currentFile.isDirectory()) {
                        deleteFilesRecursive(currentFile.listFiles());
                        currentFile.delete();
                    } else {
                        currentFile.delete();
                    }
                }
            }
        }
    }

    private void unzip(File dir, InputStream in, IntLogger logger) throws IOException {
        File tmpFile = File.createTempFile("tmpzip", null); // uses java.io.tmpdir
        try {
            FileOutputStream fos = new FileOutputStream(tmpFile);
            try {
                org.apache.commons.io.IOUtils.copy(in, fos);
            } finally {
                org.apache.commons.io.IOUtils.closeQuietly(fos);
            }
            unzip(dir, tmpFile, logger);
        } finally {
            tmpFile.delete();
        }
    }

    private void unzip(File dir, File zipFile, IntLogger logger) throws IOException {
        dir = dir.getAbsoluteFile(); // without absolutization, getParentFile below seems to fail
        ZipFile zip = new ZipFile(zipFile);
        @SuppressWarnings("unchecked")
        Enumeration<ZipEntry> entries = zip.getEntries();

        try {
            while (entries.hasMoreElements()) {
                ZipEntry e = entries.nextElement();
                File f = new File(dir, e.getName());
                if (e.isDirectory()) {
                    f.mkdirs();
                } else {
                    File p = f.getParentFile();
                    if (p != null) {
                        p.mkdirs();
                    }
                    InputStream input = zip.getInputStream(e);
                    try {
                        FileOutputStream fos = new FileOutputStream(f);
                        try {
                            org.apache.commons.io.IOUtils.copy(input, fos);
                        } finally {
                            org.apache.commons.io.IOUtils.closeQuietly(fos);
                        }
                    } finally {
                        input.close();
                    }
                    f.setWritable(true);
                    f.setReadable(true);
                    f.setLastModified(e.getTime());
                }
            }
        } finally {
            zip.close();
        }
    }
}
