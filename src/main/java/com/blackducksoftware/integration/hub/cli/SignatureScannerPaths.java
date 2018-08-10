package com.blackducksoftware.integration.hub.cli;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.SystemUtils;

import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.log.IntLogger;

public class SignatureScannerPaths {
    private static final String JAVA_PATH_FORMAT = "bin" + File.separator + "%s";
    private static final String WINDOWS_JAVA_PATH = String.format(JAVA_PATH_FORMAT, "java.exe");
    private static final String OTHER_JAVA_PATH = String.format(JAVA_PATH_FORMAT, "java");
    private static final String STANDALONE_JAR_PATH = "cache" + File.separator + "scan.cli.impl-standalone.jar";

    private static final FileFilter EXCLUDE_NON_SCAN_CLI_DIRECTORIES_FILTER = file -> !file.isHidden() && !file.getName().contains("windows") && file.isDirectory();
    private static final FileFilter JRE_DIRECTORY_FILTER = file -> "jre".equalsIgnoreCase(file.getName()) && file.isDirectory();
    private static final FileFilter LIB_DIRECTORY_FILTER = file -> "lib".equalsIgnoreCase(file.getName()) && file.isDirectory();
    private static final FileFilter SCAN_CLI_JAR_FILE_FILTER = file -> file.getName().startsWith("scan.cli") && file.getName().endsWith(".jar") && file.isFile();

    private final IntLogger logger;
    private final File installDirectory;
    private final File jreContentsDirectory;
    private final File libDirectory;

    private final String pathToJavaExecutable;
    private final String pathToOneJar;
    private final String pathToScanExecutable;

    /**
     * The directory can either be the directory that contains Hub_Scan_Installation, or the directory that contains the bin, jre, lib (etc) directories.
     * @param logger
     * @param directory
     * @throws HubIntegrationException
     */
    public SignatureScannerPaths(final IntLogger logger, final File directory) throws HubIntegrationException {
        this.logger = logger;
        if (directory == null || !directory.isDirectory()) {
            throw new IllegalArgumentException(String.format("%s is not a valid directory", directory.getAbsolutePath()));
        }

        final boolean createdDirectories = directory.mkdirs();
        if (createdDirectories) {
            logger.info(String.format("The directory structure didn't exist so it was created: %s", directory.getAbsolutePath()));
        }
        if (!directory.exists()) {
            throw new IllegalArgumentException(String.format("%s could not be created and/or does not exist.", directory.getAbsolutePath()));
        }

        final File[] hubScanInstallationDirectories = directory.listFiles(file -> CLILocation.CLI_UNZIP_DIR.equals(file.getName()));
        if (hubScanInstallationDirectories.length == 1) {
            installDirectory = findFirstFilteredFile(hubScanInstallationDirectories[0], EXCLUDE_NON_SCAN_CLI_DIRECTORIES_FILTER, "No scan.cli directories could be found in %s.");
        } else {
            installDirectory = directory;
        }

        jreContentsDirectory = findFirstFilteredFile(installDirectory, JRE_DIRECTORY_FILTER, "Could not find the 'jre' directory in %s.");
        libDirectory = findFirstFilteredFile(installDirectory, LIB_DIRECTORY_FILTER, "Could not find the 'lib' directory in %s.");

        pathToJavaExecutable = findPathToJavaExe();
        pathToOneJar = findPathToStandaloneJar();
        pathToScanExecutable = findPathToScanCliJar();
    }

    private String findPathToJavaExe() throws HubIntegrationException {
        File jreContents = jreContentsDirectory;
        final List<String> filenames = Arrays.asList(jreContents.list());
        if (filenames.contains("Contents")) {
            jreContents = new File(jreContents, "Contents");
            jreContents = new File(jreContents, "Home");
        }

        File javaExe = new File(jreContents, OTHER_JAVA_PATH);
        if (SystemUtils.IS_OS_WINDOWS) {
            javaExe = new File(jreContents, WINDOWS_JAVA_PATH);
        }

        if (!javaExe.exists() || !javaExe.isFile()) {
            throw new HubIntegrationException(String.format("The java executable does not exist at: %s", javaExe.getAbsolutePath()));
        }

        javaExe.setExecutable(true);
        return javaExe.getAbsolutePath();
    }

    private String findPathToStandaloneJar() throws HubIntegrationException {
        final File standaloneJarFile = new File(libDirectory, STANDALONE_JAR_PATH);

        if (!standaloneJarFile.exists() || !standaloneJarFile.isFile()) {
            throw new HubIntegrationException(String.format("The standalone jar does not exist at: %s", standaloneJarFile.getAbsolutePath()));
        }
        standaloneJarFile.setExecutable(true);

        return standaloneJarFile.getAbsolutePath();
    }

    private String findPathToScanCliJar() throws HubIntegrationException {
        final File scanCliJarFile = findFirstFilteredFile(libDirectory, SCAN_CLI_JAR_FILE_FILTER, "");

        if (!scanCliJarFile.exists() || !scanCliJarFile.isFile()) {
            throw new HubIntegrationException(String.format("The scan.cli jar does not exist at: %s", scanCliJarFile.getAbsolutePath()));
        }
        scanCliJarFile.setExecutable(true);

        return scanCliJarFile.getAbsolutePath();
    }

    private File findFirstFilteredFile(final File directory, final FileFilter fileFilter, final String notFoundMessageFormat) throws HubIntegrationException {
        final File[] potentialItems = directory.listFiles(fileFilter);
        if (potentialItems == null || potentialItems.length < 1) {
            throw new HubIntegrationException(String.format(notFoundMessageFormat, directory.getAbsolutePath()));
        }

        return potentialItems[0];
    }

    public String getPathToJavaExecutable() {
        return pathToJavaExecutable;
    }

    public String getPathToOneJar() {
        return pathToOneJar;
    }

    public String getPathToScanExecutable() {
        return pathToScanExecutable;
    }

}
