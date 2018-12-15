package com.synopsys.integration.blackduck.codelocation.signaturescanner;

import static com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScanPathsUtility.BDS_JAVA_HOME;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScanPaths;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScanPathsUtility;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScannerZipInstaller;
import com.synopsys.integration.log.BufferedIntLogger;
import com.synopsys.integration.util.IntEnvironmentVariables;
import com.synopsys.integration.util.OperatingSystemType;

public class ScanPathsTest {
    // Example ...BlackDuckScanOutput\2018-09-16_15-38-37-050_1
    private static final String DATE_AND_THREAD_SPECIFIC_NAME = ".*\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}-\\d{3}_\\S+";

    private IntEnvironmentVariables intEnvironmentVariables = new IntEnvironmentVariables();

    private final ScanPathsUtility macScanPathsUtility = new ScanPathsUtility(new BufferedIntLogger(), intEnvironmentVariables, OperatingSystemType.MAC);
    private final ScanPathsUtility windowsScanPathsUtility = new ScanPathsUtility(new BufferedIntLogger(), intEnvironmentVariables, OperatingSystemType.WINDOWS);
    private final ScanPathsUtility linuxScanPathsUtility = new ScanPathsUtility(new BufferedIntLogger(), intEnvironmentVariables, OperatingSystemType.LINUX);

    private File tempDirectory;
    private File bdsJavaHomeDirectory;
    private File macSetup;
    private File windowsSetup;
    private File linuxSetup;

    @BeforeEach
    public void createFileSetups() throws Exception {
        final Path tempDirectoryPath = Files.createTempDirectory("scan_setups");
        tempDirectory = tempDirectoryPath.toFile();

        createMacSetup(tempDirectory);
        createWindowsSetup(tempDirectory);
        createLinuxSetup(tempDirectory);

        final Path bdsJavaHomeDirectoryPath = Files.createTempDirectory("bds_java_home");
        bdsJavaHomeDirectory = bdsJavaHomeDirectoryPath.toFile();

        createWindowsJvm(bdsJavaHomeDirectory);
        createOtherJvm(bdsJavaHomeDirectory);
    }

    @AfterEach
    public void deleteTempDirectory() {
        FileUtils.deleteQuietly(tempDirectory);
        FileUtils.deleteQuietly(bdsJavaHomeDirectory);
        intEnvironmentVariables = new IntEnvironmentVariables();
    }

    @Test
    public void testSignatureScannerPathsInstalledByUs() throws Exception {
        final ScanPaths macScanPaths = macScanPathsUtility.determineSignatureScannerPaths(macSetup);
        final ScanPaths windowsScanPaths = windowsScanPathsUtility.determineSignatureScannerPaths(windowsSetup);
        final ScanPaths linuxScanPaths = linuxScanPathsUtility.determineSignatureScannerPaths(linuxSetup);

        assertScanPathsOk(macScanPaths, true);
        assertScanPathsOk(windowsScanPaths, true);
        assertScanPathsOk(linuxScanPaths, true);
    }

    @Test
    public void testSignatureScannerPathsInstalledByThem() throws Exception {
        final ScanPaths macScanPaths = macScanPathsUtility.determineSignatureScannerPaths(new File(macSetup, getScannerPath()));
        final ScanPaths windowsScanPaths = windowsScanPathsUtility.determineSignatureScannerPaths(new File(windowsSetup, getScannerPath()));
        final ScanPaths linuxScanPaths = linuxScanPathsUtility.determineSignatureScannerPaths(new File(linuxSetup, getScannerPath()));

        assertScanPathsOk(macScanPaths, false);
        assertScanPathsOk(windowsScanPaths, false);
        assertScanPathsOk(linuxScanPaths, false);
    }

    @Test
    public void testCreateSpecificRunOutputDirectory() throws Exception {
        final File macSpecificRunOutputDirectory = macScanPathsUtility.createSpecificRunOutputDirectory(new File(macSetup, getScannerPath()));
        final File windowsSpecificRunOutputDirectory = windowsScanPathsUtility.createSpecificRunOutputDirectory(new File(windowsSetup, getScannerPath()));
        final File linuxSpecificRunOutputDirectory = linuxScanPathsUtility.createSpecificRunOutputDirectory(new File(linuxSetup, getScannerPath()));

        assertTrue(macSpecificRunOutputDirectory.getAbsolutePath().matches(DATE_AND_THREAD_SPECIFIC_NAME));
        assertTrue(windowsSpecificRunOutputDirectory.getAbsolutePath().matches(DATE_AND_THREAD_SPECIFIC_NAME));
        assertTrue(linuxSpecificRunOutputDirectory.getAbsolutePath().matches(DATE_AND_THREAD_SPECIFIC_NAME));
    }

    @Test
    public void testJavaExecutablePathWithPackagedJre() throws Exception {
        intEnvironmentVariables.put(BDS_JAVA_HOME, null);

        final ScanPaths macScanPaths = macScanPathsUtility.determineSignatureScannerPaths(macSetup);
        final ScanPaths windowsScanPaths = windowsScanPathsUtility.determineSignatureScannerPaths(windowsSetup);
        final ScanPaths linuxScanPaths = linuxScanPathsUtility.determineSignatureScannerPaths(linuxSetup);

        assertThat(macScanPaths.getPathToJavaExecutable(), startsWith(tempDirectory + File.separator + "mac_setup" + File.separator + getScannerPath()));
        assertThat(windowsScanPaths.getPathToJavaExecutable(), startsWith(tempDirectory + File.separator + "windows_setup" + File.separator + getScannerPath()));
        assertThat(linuxScanPaths.getPathToJavaExecutable(), startsWith(tempDirectory + File.separator + "linux_setup" + File.separator + getScannerPath()));
    }

    @Test
    public void testJavaExecutablePathWithBdsJavaHome() throws Exception {
        intEnvironmentVariables.put(BDS_JAVA_HOME, bdsJavaHomeDirectory.getAbsolutePath());

        final ScanPaths macScanPaths = macScanPathsUtility.determineSignatureScannerPaths(macSetup);
        final ScanPaths windowsScanPaths = windowsScanPathsUtility.determineSignatureScannerPaths(windowsSetup);
        final ScanPaths linuxScanPaths = linuxScanPathsUtility.determineSignatureScannerPaths(linuxSetup);

        assertThat(macScanPaths.getPathToJavaExecutable(), is(bdsJavaHomeDirectory + File.separator + "bin" + File.separator + "java"));
        assertThat(windowsScanPaths.getPathToJavaExecutable(), is(bdsJavaHomeDirectory + File.separator + "bin" + File.separator + "java.exe"));
        assertThat(linuxScanPaths.getPathToJavaExecutable(), is(bdsJavaHomeDirectory + File.separator + "bin" + File.separator + "java"));
    }

    private void assertScanPathsOk(final ScanPaths scanPaths, final boolean managedByLibrary) {
        assertTrue(StringUtils.isNotBlank(scanPaths.getPathToJavaExecutable()));
        assertTrue(StringUtils.isNotBlank(scanPaths.getPathToOneJar()));
        assertTrue(StringUtils.isNotBlank(scanPaths.getPathToScanExecutable()));
        assertEquals(managedByLibrary, scanPaths.isManagedByLibrary());
    }

    private String getScannerPath() {
        return ScannerZipInstaller.BLACK_DUCK_SIGNATURE_SCANNER_INSTALL_DIRECTORY + File.separator + "scanner";
    }

    private void createMacSetup(final File tempDirectory) throws IOException {
        macSetup = new File(tempDirectory, "mac_setup");
        createBasicSetup(macSetup);

        final File icon = new File(macSetup, getScannerPath() + File.separator + "icon");
        icon.mkdirs();

        final File jre = new File(macSetup, getScannerPath() + File.separator + "jre");
        final File javaBin = new File(jre, "Contents" + File.separator + "Home" + File.separator + "bin");
        javaBin.mkdirs();

        final File javaExe = new File(javaBin, "java");
        javaExe.createNewFile();
    }

    private void createWindowsSetup(final File tempDirectory) throws IOException {
        windowsSetup = new File(tempDirectory, "windows_setup");
        createBasicSetup(windowsSetup);

        final File jre = new File(windowsSetup, getScannerPath() + File.separator + "jre");
        final File javaBin = new File(jre, "bin");
        javaBin.mkdirs();

        final File javaExe = new File(javaBin, "java.exe");
        javaExe.createNewFile();
    }

    private void createLinuxSetup(final File tempDirectory) throws IOException {
        linuxSetup = new File(tempDirectory, "linux_setup");
        createBasicSetup(linuxSetup);

        final File jre = new File(linuxSetup, getScannerPath() + File.separator + "jre");
        final File javaBin = new File(jre, "bin");
        javaBin.mkdirs();

        final File javaExe = new File(javaBin, "java");
        javaExe.createNewFile();
    }

    private void createBasicSetup(final File installDirectory) throws IOException {
        final File scanDirectory = new File(installDirectory, getScannerPath());
        scanDirectory.mkdirs();

        final File bin = new File(scanDirectory, "bin");
        bin.mkdirs();
        final File jre = new File(scanDirectory, "jre");
        jre.mkdirs();
        final File lib = new File(scanDirectory, "lib");
        final File cache = new File(lib, "cache");
        cache.mkdirs();

        final File standaloneJar = new File(lib, "scan.cli-x.y.z-standalone.jar");
        standaloneJar.createNewFile();

        final File implStandaloneJar = new File(cache, "scan.cli.impl-standalone.jar");
        implStandaloneJar.createNewFile();
    }

    private void createWindowsJvm(final File tempDirectory) throws IOException {
        final File javaBin = new File(tempDirectory, "bin");
        javaBin.mkdirs();

        final File javaExe = new File(javaBin, "java.exe");
        javaExe.createNewFile();
    }

    private void createOtherJvm(final File tempDirectory) throws IOException {
        final File javaBin = new File(tempDirectory, "bin");
        javaBin.mkdirs();

        final File javaExe = new File(javaBin, "java");
        javaExe.createNewFile();
    }

}
