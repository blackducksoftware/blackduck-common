package com.blackducksoftware.integration.hub.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
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
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.suite.sdk.logging.IntLogger;

public class CLIInstaller {
    public static final String VERSION_FILE_NAME = "hubVersion.txt";

    public static final String CLI_UNZIP_DIR = "Hub_Scan_Installation";

    private final File directoryToInstallTo;

    private final String localHostName;

    private String proxyHost;

    private Integer proxyPort;

    private String proxyUserName;

    private String proxyPassword;

    public CLIInstaller(File directoryToInstallTo, String localHostName) {
        if (directoryToInstallTo == null) {
            throw new IllegalArgumentException("You must provided a directory to install the CLI to.");
        }
        if (StringUtils.isBlank(localHostName)) {
            throw new IllegalArgumentException("You must provided the hostName of the machine this is running on.");
        }
        this.directoryToInstallTo = directoryToInstallTo;
        this.localHostName = localHostName;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public Integer getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(Integer proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String getProxyUserName() {
        return proxyUserName;
    }

    public void setProxyUserName(String proxyUserName) {
        this.proxyUserName = proxyUserName;
    }

    public String getProxyPassword() {
        return proxyPassword;
    }

    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

    public String getLocalHostName() {
        return localHostName;
    }

    public File getDirectoryToInstallTo() {
        return directoryToInstallTo;
    }

    public File getCLIInstallDir() {
        return new File(directoryToInstallTo, CLI_UNZIP_DIR);
    }

    public File getCLIHome() {
        File cliHome = getCLIInstallDir();
        File[] installDirFiles = cliHome.listFiles();
        if (installDirFiles.length == 1) {
            return installDirFiles[0];
        } else {
            return null;
        }
    }

    public void performInstallation(IntLogger logger, HubIntRestService restService) throws IOException,
            InterruptedException, BDRestException, URISyntaxException, HubIntegrationException {
        String cliDownloadUrl = getCLIDownloadUrl(logger, restService);
        if (StringUtils.isNotBlank(cliDownloadUrl)) {
            customInstall(new URL(cliDownloadUrl), restService.getHubVersion(), logger);
        } else {
            logger.error("Could not find the correct Hub CLI download URL.");
        }
    }

    public String getCLIDownloadUrl(IntLogger logger, HubIntRestService restService) throws IOException, InterruptedException {
        try {
            HubSupportHelper hubSupport = new HubSupportHelper();

            hubSupport.checkHubSupport(restService, logger);
            //
            // if (SystemUtils.IS_OS_MAC_OSX && hubSupport.isJreProvidedSupport()) {
            // return HubSupportHelper.getOSXCLIWrapperLink(restService.getBaseUrl());
            // } else if (SystemUtils.IS_OS_WINDOWS && hubSupport.isJreProvidedSupport()) {
            // return HubSupportHelper.getWindowsCLIWrapperLink(restService.getBaseUrl());
            // } else {
            return HubSupportHelper.getLinuxCLIWrapperLink(restService.getBaseUrl());
            // }
        } catch (URISyntaxException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    private boolean customInstall(URL archive, String hubVersion, IntLogger logger) throws IOException, InterruptedException, HubIntegrationException {
        try {
            if (!directoryToInstallTo.exists() && !directoryToInstallTo.mkdirs()) {
                throw new HubIntegrationException("Could not create the directory : " + directoryToInstallTo.getCanonicalPath());
            }

            boolean cliMismatch = true;
            // For some reason the Hub returns the Version inside ""'s
            hubVersion = hubVersion.replace("\"", "");
            File hubVersionFile = new File(directoryToInstallTo, VERSION_FILE_NAME);
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

            File cliInstallDirectory = getCLIInstallDir();
            if (cliInstallDirectory.exists() && cliInstallDirectory.listFiles().length > 0) {
                if (!cliMismatch)
                {
                    return false; // already up to date
                }

                // delete directory contents
                deleteFilesRecursive(cliInstallDirectory.listFiles());
            } else {
                cliInstallDirectory.mkdir();
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
                logger.error("Skipping installation of " + archive + " to " + directoryToInstallTo.getCanonicalPath() + ": " + ioe.toString());
                return false;
            }

            // if (connection instanceof HttpURLConnection
            // && ((HttpURLConnection) connection).getResponseCode() == HttpURLConnection.HTTP_NOT_MODIFIED) {
            // // This may be useful if the Hub gets the Cli download to return the correct modified headers
            // // and if they separate the CLI updates from the Hub releases
            // return false;
            // }

            logger.info("Unpacking " + archive.toString() + " to " + cliInstallDirectory.getCanonicalPath() + " on " + getLocalHostName());

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
            throw new IOException("Failed to install " + archive + " to " + directoryToInstallTo.getCanonicalPath(), e);
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
            copyInputStreamToFile(in, tmpFile);
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

    private void copyInputStreamToFile(InputStream in, File f) throws IOException {
        FileOutputStream fos = new FileOutputStream(f);
        try {
            org.apache.commons.io.IOUtils.copy(in, fos);
        } finally {
            org.apache.commons.io.IOUtils.closeQuietly(fos);
        }
    }

    /**
     * Returns the executable file of the installation
     *
     *
     * @return File
     * @throws IOException
     * @throws InterruptedException
     */
    public File getProvidedJavaHome() throws IOException, InterruptedException {
        File cliHomeFile = getCLIHome();
        if (cliHomeFile == null) {
            return null;
        }
        File[] files = cliHomeFile.listFiles();
        if (files != null && files.length > 0) {
            File jreFolder = null;
            for (File directory : files) {
                if ("jre".equalsIgnoreCase(directory.getName())) {
                    jreFolder = directory;
                    break;
                }
            }
            if (jreFolder != null) {
                File javaExec = new File(jreFolder, "bin");
                if (SystemUtils.IS_OS_WINDOWS) {
                    javaExec = new File(javaExec, "java.exe");
                } else {
                    javaExec = new File(javaExec, "java");
                }
                if (javaExec.exists()) {
                    return jreFolder;
                }
            }
        }
        return null;
    }

    /**
     * Checks if the executable exists
     *
     * @param channel
     *            VirtualChannel to find the executable on master or slave
     *
     * @return true if executable is found, false otherwise
     * @throws IOException
     * @throws InterruptedException
     */
    public boolean getCLIExists(IntLogger logger) throws IOException, InterruptedException {
        File cliHomeFile = getCLIHome();
        // find the lib folder in the iScan directory
        logger.debug("BlackDuck scan directory: " + cliHomeFile.getCanonicalPath());
        File[] files = cliHomeFile.listFiles();
        if (files != null) {
            logger.debug("directories in the BlackDuck scan directory: " + files.length);
            if (files.length > 0) {
                File libFolder = null;
                for (File directory : files) {
                    if ("lib".equalsIgnoreCase(directory.getName())) {
                        libFolder = directory;
                        break;
                    }
                }
                if (libFolder == null) {
                    logger.error("Could not find the lib directory of the CLI.");
                    return false;
                }
                logger.debug("BlackDuck scan lib directory: " + libFolder.getCanonicalPath());
                FilenameFilter nameFilter = new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.matches("scan.cli.*.jar");
                    }
                };
                File[] cliFiles = libFolder.listFiles(nameFilter);
                File hubScanJar = null;
                if (cliFiles == null || cliFiles.length == 0) {
                    return false;
                } else {
                    for (File file : cliFiles) {
                        logger.debug("BlackDuck scan lib file: " + file.getCanonicalPath());
                        if (file.getName().contains("scan.cli")) {
                            hubScanJar = file;
                            break;
                        }
                    }
                }
                if (hubScanJar == null) {
                    return false;
                }
                return hubScanJar.exists();
            } else {
                logger.error("No files found in the BlackDuck scan directory.");
                return false;
            }
        } else {
            logger.error("No files found in the BlackDuck scan directory.");
            return false;
        }

    }

    /**
     * Returns the executable file of the installation
     *
     * @param channel
     *            VirtualChannel to find the executable on master or slave
     *
     * @return File
     * @throws IOException
     * @throws InterruptedException
     */
    public File getCLI() throws IOException, InterruptedException {
        File cliHomeFile = getCLIHome();
        File[] files = cliHomeFile.listFiles();
        if (files != null && files.length > 0) {
            File libFolder = null;
            for (File directory : files) {
                if ("lib".equalsIgnoreCase(directory.getName())) {
                    libFolder = directory;
                    break;
                }
            }
            if (libFolder == null) {
                return null;
            }
            FilenameFilter nameFilter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.matches("scan.cli.*.jar");
                }
            };
            File[] cliFiles = libFolder.listFiles(nameFilter);
            File cliFile = null;
            if (cliFiles == null) {
                return null;
            } else {
                for (File file : cliFiles) {
                    if (file.getName().contains("scan.cli")) {
                        cliFile = file;
                        break;
                    }
                }
            }
            return cliFile;
        } else {
            return null;
        }
    }

    public File getOneJarFile() {
        File cliHomeFile = getCLIHome();
        File oneJarFile = new File(cliHomeFile, "lib");

        oneJarFile = new File(oneJarFile, "cache");

        oneJarFile = new File(oneJarFile, "scan.cli.impl-standalone.jar");
        return oneJarFile;
    }
}
