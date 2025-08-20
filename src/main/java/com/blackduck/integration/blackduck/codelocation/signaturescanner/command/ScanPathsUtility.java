/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.codelocation.signaturescanner.command;

import com.blackduck.integration.blackduck.exception.BlackDuckIntegrationException;
import com.blackduck.integration.function.ThrowingSupplier;
import com.blackduck.integration.log.IntLogger;
import com.blackduck.integration.util.IntEnvironmentVariables;
import com.blackduck.integration.util.OperatingSystemType;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ScanPathsUtility {
    public static final String STANDARD_OUT_FILENAME = "CLI_Output.txt";

    public static final String BDS_JAVA_HOME = "BDS_JAVA_HOME";

    private static final String JAVA_PATH_FORMAT = "bin" + File.separator + "%s";
    private static final String WINDOWS_JAVA_PATH = String.format(JAVA_PATH_FORMAT, "java.exe");
    private static final String OTHER_JAVA_PATH = String.format(JAVA_PATH_FORMAT, "java");
    private static final String CACERTS_PATH = "lib" + File.separator + "security" + File.separator + "cacerts";
    private static final String STANDALONE_JAR_PATH = "cache" + File.separator + "scan.cli.impl-standalone.jar";
    private static final String METADATA_FILE_NAME = "metadata.json";

    private static final FileFilter EXCLUDE_NON_SCAN_CLI_DIRECTORIES_FILTER = file -> !file.isHidden() && !file.getName().contains("windows") && file.isDirectory();
    private static final FileFilter JRE_DIRECTORY_FILTER = file -> "jre".equalsIgnoreCase(file.getName()) && file.isDirectory();
    private static final FileFilter LIB_DIRECTORY_FILTER = file -> "lib".equalsIgnoreCase(file.getName()) && file.isDirectory();
    private static final FileFilter SCAN_CLI_JAR_FILE_FILTER = file -> file.getName().startsWith("scan.cli") && file.getName().endsWith(".jar") && file.isFile();
    private static final FileFilter METADATA_FILE_FILTER = file -> file.getName().equals(METADATA_FILE_NAME);

    // this will allow for multiple threads to always get a unique number
    private final AtomicInteger defaultMultiThreadingId = new AtomicInteger(0);

    private final IntLogger logger;
    private final IntEnvironmentVariables intEnvironmentVariables;
    private final OperatingSystemType operatingSystemType;

    private File scanCliMetadataFile;

    public ScanPathsUtility(final IntLogger logger, final IntEnvironmentVariables intEnvironmentVariables, final OperatingSystemType operatingSystemType) {
        this.logger = logger;
        this.intEnvironmentVariables = intEnvironmentVariables;
        this.operatingSystemType = operatingSystemType;
    }

    /**
     * The directory can either be the directory that contains Black_Duck_Scan_Installation, or the directory that contains the bin, jre, lib (etc) directories.
     * @param directory
     * @throws BlackDuckIntegrationException
     */
    public ScanPaths searchForScanPaths(final File directory) throws BlackDuckIntegrationException {
        if (directory == null) {
            throw new IllegalArgumentException("null is not a valid directory");
        }

        if (!directory.isDirectory()) {
            throw new IllegalArgumentException(String.format("%s is not a valid directory", directory.getAbsolutePath()));
        }

        if (!directory.exists()) {
            throw new IllegalArgumentException(String.format("%s does not exist.", directory.getAbsolutePath()));
        }

        boolean managedByLibrary = false;
        File installDirectory = directory;
        final File[] blackDuckScanInstallationDirectories = directory.listFiles(file -> ZipApiScannerInstaller.BLACK_DUCK_SIGNATURE_SCANNER_INSTALL_DIRECTORY.equals(file.getName()));
        if (blackDuckScanInstallationDirectories.length == 1) {
            logger.debug("The directory structure was likely created by the installer");
            installDirectory = findFirstFilteredFile(blackDuckScanInstallationDirectories[0], EXCLUDE_NON_SCAN_CLI_DIRECTORIES_FILTER, "No scan.cli directories could be found in %s.");
            managedByLibrary = true;
        } else {
            logger.debug(String.format("The directory structure was likely created manually - be sure the jre folder exists in: %s", installDirectory.getAbsolutePath()));
        }

        final File jreContentsDirectory = findFirstFilteredFile(installDirectory, JRE_DIRECTORY_FILTER, "Could not find the 'jre' directory in %s.");

        final String pathToCacerts = findPathToCacerts(jreContentsDirectory);

        final String pathToJavaExecutable = determinePathToJavaExecutable(jreContentsDirectory);

        final File libDirectory = findFirstFilteredFile(installDirectory, LIB_DIRECTORY_FILTER, "Could not find the 'lib' directory in %s.");

        final String pathToOneJar = findPathToStandaloneJar(libDirectory);
        final String pathToScanExecutable = findPathToScanCliJar(libDirectory);

        scanCliMetadataFile = new File(installDirectory, METADATA_FILE_NAME);
    
        return new ScanPaths(pathToJavaExecutable, pathToCacerts, pathToOneJar, pathToScanExecutable, managedByLibrary);
    }

    public File createSpecificRunOutputDirectory(final File generalOutputDirectory) throws BlackDuckIntegrationException {
        final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss-SSS").withZone(ZoneOffset.UTC);
        final String timeStringPrefix = Instant.now().atZone(ZoneOffset.UTC).format(dateTimeFormatter);

        final int uniqueThreadIdSuffix = defaultMultiThreadingId.incrementAndGet();
        return createRunOutputDirectory(generalOutputDirectory, timeStringPrefix, Integer.toString(uniqueThreadIdSuffix));
    }

    public File createRunOutputDirectory(final File generalOutputDirectory, final String userProvidedPrefix, final String userProvidedUniqueSuffix) throws BlackDuckIntegrationException {
        final String signatureScanOutputDirectoryName = "BlackDuckScanOutput";
        final File signatureScanOutputDirectory = new File(generalOutputDirectory, signatureScanOutputDirectoryName);

        final String uniqueOutputDirectoryName = userProvidedPrefix + "_" + userProvidedUniqueSuffix;

        final File specificRunOutputDirectory = new File(signatureScanOutputDirectory, uniqueOutputDirectoryName);
        if (!specificRunOutputDirectory.exists() && !specificRunOutputDirectory.mkdirs()) {
            throw new BlackDuckIntegrationException(String.format("Could not create the %s directory!", specificRunOutputDirectory.getAbsolutePath()));
        }

        final File bdIgnoreLogsFile = new File(generalOutputDirectory, ".bdignore");
        if (!bdIgnoreLogsFile.exists()) {
            try {
                if (!bdIgnoreLogsFile.createNewFile()) {
                    throw new BlackDuckIntegrationException(String.format("Could not create the %s file!", bdIgnoreLogsFile.getAbsolutePath()));
                }
                final String exclusionPattern = "/" + signatureScanOutputDirectoryName + "/";
                Files.write(bdIgnoreLogsFile.toPath(), exclusionPattern.getBytes());
            } catch (final IOException e) {
                throw new BlackDuckIntegrationException(String.format("Unexpected error creating the .bdignore file in the %s directory: %s", bdIgnoreLogsFile.getAbsolutePath(), e.getMessage()));
            }
        }

        return specificRunOutputDirectory;
    }

    public File createStandardOutFile(final File specificRunOutputDirectory) throws IOException {
        final File standardOutFile = new File(specificRunOutputDirectory, STANDARD_OUT_FILENAME);
        standardOutFile.createNewFile();

        return standardOutFile;
    }

    private String findPathToJavaExe(final File jreContentsDirectory) throws BlackDuckIntegrationException {
        File jdkBase = getJdkBase(jreContentsDirectory);

        File javaExe = new File(jdkBase, OTHER_JAVA_PATH);
        if (OperatingSystemType.WINDOWS == operatingSystemType) {
            javaExe = new File(jdkBase, WINDOWS_JAVA_PATH);
        }

        if (!javaExe.exists() || !javaExe.isFile()) {
            throw new BlackDuckIntegrationException(String.format("The java executable does not exist at: %s", javaExe.getAbsolutePath()));
        }

        return javaExe.getAbsolutePath();
    }

    private String findPathToCacerts(final File jreContentsDirectory) {
        File jdkBase = getJdkBase(jreContentsDirectory);
        File cacerts = new File(jdkBase, CACERTS_PATH);

        return cacerts.getAbsolutePath();
    }

    @NotNull
    private File getJdkBase(final File jreContentsDirectory) {
        File jdkBase = jreContentsDirectory;
        final List<String> filenames = Arrays.asList(jdkBase.list());
        if (filenames.contains("Contents")) {
            jdkBase = new File(jdkBase, "Contents");
            jdkBase = new File(jdkBase, "Home");
        }
        return jdkBase;
    }

    private String findPathToStandaloneJar(final File libDirectory) throws BlackDuckIntegrationException {
        final File standaloneJarFile = new File(libDirectory, STANDALONE_JAR_PATH);

        if (!standaloneJarFile.exists() || !standaloneJarFile.isFile()) {
            throw new BlackDuckIntegrationException(String.format("The standalone jar does not exist at: %s", standaloneJarFile.getAbsolutePath()));
        }

        return standaloneJarFile.getAbsolutePath();
    }

    private String findPathToScanCliJar(final File libDirectory) throws BlackDuckIntegrationException {
        final File scanCliJarFile = findFirstFilteredFile(libDirectory, SCAN_CLI_JAR_FILE_FILTER, "");

        if (!scanCliJarFile.exists() || !scanCliJarFile.isFile()) {
            throw new BlackDuckIntegrationException(String.format("The scan.cli jar does not exist at: %s", scanCliJarFile.getAbsolutePath()));
        }

        return scanCliJarFile.getAbsolutePath();
    }

    private File findFirstFilteredFile(final File directory, final FileFilter fileFilter, final String notFoundMessageFormat) throws BlackDuckIntegrationException {
        final File[] potentialItems = directory.listFiles(fileFilter);
        if (potentialItems == null || potentialItems.length < 1) {
            throw new BlackDuckIntegrationException(String.format(notFoundMessageFormat, directory.getAbsolutePath()));
        }

        return potentialItems[0];
    }

    private String determinePathToJavaExecutable(File jreContentsDirectory) throws BlackDuckIntegrationException {
        ThrowingSupplier<String, BlackDuckIntegrationException> javaHomeSupplier = () -> findPathToJavaExe(jreContentsDirectory);

        final String bdsJavaHome = intEnvironmentVariables.getValue(BDS_JAVA_HOME);
        if (StringUtils.isNotBlank(bdsJavaHome)) {
            File bdsJavaHomeDirectory = new File(bdsJavaHome);
            if (bdsJavaHomeDirectory.exists() && bdsJavaHomeDirectory.isDirectory()) {
                javaHomeSupplier = () -> findPathToJavaExe(bdsJavaHomeDirectory);
            } else {
                String warningFormat = "The environment variable %s is set, but it cannot be used as a valid directory. It will be ignored and should either be corrected, or unset in the environment to avoid this warning.";
                logger.warn(String.format(warningFormat, BDS_JAVA_HOME));
            }
        }

        return javaHomeSupplier.get();
    }

    public File getMetadataFile() {
        return scanCliMetadataFile;
    }
}
