/**
 * hub-common
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
package com.synopsys.integration.blackduck.cli.simple;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.SystemUtils;

import com.synopsys.integration.blackduck.cli.CLILocation;
import com.synopsys.integration.blackduck.exception.HubIntegrationException;
import com.synopsys.integration.log.IntLogger;

public class SimpleScanPathsUtility {
    private static final String JAVA_PATH_FORMAT = "bin" + File.separator + "%s";
    private static final String WINDOWS_JAVA_PATH = String.format(JAVA_PATH_FORMAT, "java.exe");
    private static final String OTHER_JAVA_PATH = String.format(JAVA_PATH_FORMAT, "java");
    private static final String STANDALONE_JAR_PATH = "cache" + File.separator + "scan.cli.impl-standalone.jar";

    private static final FileFilter EXCLUDE_NON_SCAN_CLI_DIRECTORIES_FILTER = file -> !file.isHidden() && !file.getName().contains("windows") && file.isDirectory();
    private static final FileFilter JRE_DIRECTORY_FILTER = file -> "jre".equalsIgnoreCase(file.getName()) && file.isDirectory();
    private static final FileFilter LIB_DIRECTORY_FILTER = file -> "lib".equalsIgnoreCase(file.getName()) && file.isDirectory();
    private static final FileFilter SCAN_CLI_JAR_FILE_FILTER = file -> file.getName().startsWith("scan.cli") && file.getName().endsWith(".jar") && file.isFile();

    /**
     * The directory can either be the directory that contains Hub_Scan_Installation, or the directory that contains the bin, jre, lib (etc) directories.
     * @param logger
     * @param directory
     * @throws HubIntegrationException
     */
    public SimpleScanPaths determineSignatureScannerPaths(final IntLogger logger, final File directory) throws HubIntegrationException {
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

        File installDirectory = directory;
        final File[] hubScanInstallationDirectories = directory.listFiles(file -> CLILocation.CLI_UNZIP_DIR.equals(file.getName()));
        if (hubScanInstallationDirectories.length == 1) {
            installDirectory = findFirstFilteredFile(hubScanInstallationDirectories[0], EXCLUDE_NON_SCAN_CLI_DIRECTORIES_FILTER, "No scan.cli directories could be found in %s.");
        }

        final File jreContentsDirectory = findFirstFilteredFile(installDirectory, JRE_DIRECTORY_FILTER, "Could not find the 'jre' directory in %s.");
        final File libDirectory = findFirstFilteredFile(installDirectory, LIB_DIRECTORY_FILTER, "Could not find the 'lib' directory in %s.");

        final String pathToJavaExecutable = findPathToJavaExe(jreContentsDirectory);
        final String pathToOneJar = findPathToStandaloneJar(libDirectory);
        final String pathToScanExecutable = findPathToScanCliJar(libDirectory);

        return new SimpleScanPaths(pathToJavaExecutable, pathToOneJar, pathToScanExecutable);
    }

    public File createSpecificRunOutputDirectory(final File generalOutputDirectory) throws IOException, HubIntegrationException {
        final String signatureScanOutputDirectoryName = "HubScanOutput";
        final File signatureScanOutputDirectory = new File(generalOutputDirectory, signatureScanOutputDirectoryName);

        final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss-SSS").withZone(ZoneOffset.UTC);
        final String timeString = Instant.now().atZone(ZoneOffset.UTC).format(dateTimeFormatter);
        final String uniqueOutputDirectoryName = timeString + "_" + Thread.currentThread().getId();

        final File specificRunOutputDirectory = new File(signatureScanOutputDirectory, uniqueOutputDirectoryName);
        if (!specificRunOutputDirectory.exists() && !specificRunOutputDirectory.mkdirs()) {
            throw new HubIntegrationException(String.format("Could not create the %s directory!", specificRunOutputDirectory.getAbsolutePath()));
        }

        final File bdIgnoreLogsFile = new File(generalOutputDirectory, ".bdignore");
        if (!bdIgnoreLogsFile.exists()) {
            if (!bdIgnoreLogsFile.createNewFile()) {
                throw new HubIntegrationException(String.format("Could not create the %s file!", bdIgnoreLogsFile.getAbsolutePath()));
            }
            final String exclusionPattern = "/" + signatureScanOutputDirectoryName + "/";
            Files.write(bdIgnoreLogsFile.toPath(), exclusionPattern.getBytes());
        }

        return specificRunOutputDirectory;
    }

    private String findPathToJavaExe(final File jreContentsDirectory) throws HubIntegrationException {
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

        return setExecutableAndGetAbsolutePath(javaExe);
    }

    private String findPathToStandaloneJar(final File libDirectory) throws HubIntegrationException {
        final File standaloneJarFile = new File(libDirectory, STANDALONE_JAR_PATH);

        if (!standaloneJarFile.exists() || !standaloneJarFile.isFile()) {
            throw new HubIntegrationException(String.format("The standalone jar does not exist at: %s", standaloneJarFile.getAbsolutePath()));
        }

        return setExecutableAndGetAbsolutePath(standaloneJarFile);
    }

    private String findPathToScanCliJar(final File libDirectory) throws HubIntegrationException {
        final File scanCliJarFile = findFirstFilteredFile(libDirectory, SCAN_CLI_JAR_FILE_FILTER, "");

        if (!scanCliJarFile.exists() || !scanCliJarFile.isFile()) {
            throw new HubIntegrationException(String.format("The scan.cli jar does not exist at: %s", scanCliJarFile.getAbsolutePath()));
        }

        return setExecutableAndGetAbsolutePath(scanCliJarFile);
    }

    private File findFirstFilteredFile(final File directory, final FileFilter fileFilter, final String notFoundMessageFormat) throws HubIntegrationException {
        final File[] potentialItems = directory.listFiles(fileFilter);
        if (potentialItems == null || potentialItems.length < 1) {
            throw new HubIntegrationException(String.format(notFoundMessageFormat, directory.getAbsolutePath()));
        }

        return potentialItems[0];
    }

    private String setExecutableAndGetAbsolutePath(final File file) {
        file.setExecutable(true);
        return file.getAbsolutePath();
    }

}
