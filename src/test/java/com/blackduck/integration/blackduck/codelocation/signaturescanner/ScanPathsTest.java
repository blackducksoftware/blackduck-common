package com.blackduck.integration.blackduck.codelocation.signaturescanner;

import com.blackduck.integration.blackduck.TimingExtension;
import com.blackduck.integration.blackduck.codelocation.signaturescanner.command.ScanPaths;
import com.blackduck.integration.blackduck.codelocation.signaturescanner.command.ScanPathsUtility;
import com.blackduck.integration.blackduck.codelocation.signaturescanner.command.ZipApiScannerInstaller;
import com.blackduck.integration.log.BufferedIntLogger;
import com.blackduck.integration.util.IntEnvironmentVariables;
import com.blackduck.integration.util.OperatingSystemType;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.blackduck.integration.blackduck.codelocation.signaturescanner.command.ScanPathsUtility.BDS_JAVA_HOME;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(TimingExtension.class)
public class ScanPathsTest {
    // Example ...BlackDuckScanOutput\2018-09-16_15-38-37-050_1
    private static final String DATE_AND_THREAD_SPECIFIC_NAME = ".*\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}-\\d{3}_\\S+";

    private IntEnvironmentVariables intEnvironmentVariables = IntEnvironmentVariables.includeSystemEnv();

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
        Path tempDirectoryPath = Files.createTempDirectory("scan_setups");
        tempDirectory = tempDirectoryPath.toFile();

        createMacSetup(tempDirectory);
        createWindowsSetup(tempDirectory);
        createLinuxSetup(tempDirectory);

        Path bdsJavaHomeDirectoryPath = Files.createTempDirectory("bds_java_home");
        bdsJavaHomeDirectory = bdsJavaHomeDirectoryPath.toFile();

        createWindowsJvm(bdsJavaHomeDirectory);
        createOtherJvm(bdsJavaHomeDirectory);
    }

    @AfterEach
    public void deleteTempDirectory() {
        FileUtils.deleteQuietly(tempDirectory);
        FileUtils.deleteQuietly(bdsJavaHomeDirectory);
        intEnvironmentVariables = IntEnvironmentVariables.includeSystemEnv();
    }

    @Test
    public void testSignatureScannerPathsInstalledByUs() throws Exception {
        ScanPaths macScanPaths = macScanPathsUtility.searchForScanPaths(macSetup);
        ScanPaths windowsScanPaths = windowsScanPathsUtility.searchForScanPaths(windowsSetup);
        ScanPaths linuxScanPaths = linuxScanPathsUtility.searchForScanPaths(linuxSetup);

        assertScanPathsOk(macScanPaths, true);
        assertScanPathsOk(windowsScanPaths, true);
        assertScanPathsOk(linuxScanPaths, true);
    }

    @Test
    public void testSignatureScannerPathsInstalledByThem() throws Exception {
        ScanPaths macScanPaths = macScanPathsUtility.searchForScanPaths(new File(macSetup, getScannerPath()));
        ScanPaths windowsScanPaths = windowsScanPathsUtility.searchForScanPaths(new File(windowsSetup, getScannerPath()));
        ScanPaths linuxScanPaths = linuxScanPathsUtility.searchForScanPaths(new File(linuxSetup, getScannerPath()));

        assertScanPathsOk(macScanPaths, false);
        assertScanPathsOk(windowsScanPaths, false);
        assertScanPathsOk(linuxScanPaths, false);
    }

    @Test
    public void testCreateSpecificRunOutputDirectory() throws Exception {
        File macSpecificRunOutputDirectory = macScanPathsUtility.createSpecificRunOutputDirectory(new File(macSetup, getScannerPath()));
        File windowsSpecificRunOutputDirectory = windowsScanPathsUtility.createSpecificRunOutputDirectory(new File(windowsSetup, getScannerPath()));
        File linuxSpecificRunOutputDirectory = linuxScanPathsUtility.createSpecificRunOutputDirectory(new File(linuxSetup, getScannerPath()));

        assertTrue(macSpecificRunOutputDirectory.getAbsolutePath().matches(ScanPathsTest.DATE_AND_THREAD_SPECIFIC_NAME));
        assertTrue(windowsSpecificRunOutputDirectory.getAbsolutePath().matches(ScanPathsTest.DATE_AND_THREAD_SPECIFIC_NAME));
        assertTrue(linuxSpecificRunOutputDirectory.getAbsolutePath().matches(ScanPathsTest.DATE_AND_THREAD_SPECIFIC_NAME));
    }

    @Test
    public void testJavaExecutablePathWithPackagedJre() throws Exception {
        intEnvironmentVariables.put(BDS_JAVA_HOME, null);

        ScanPaths macScanPaths = macScanPathsUtility.searchForScanPaths(macSetup);
        ScanPaths windowsScanPaths = windowsScanPathsUtility.searchForScanPaths(windowsSetup);
        ScanPaths linuxScanPaths = linuxScanPathsUtility.searchForScanPaths(linuxSetup);

        assertThat(macScanPaths.getPathToJavaExecutable(), startsWith(tempDirectory + File.separator + "mac_setup" + File.separator + getScannerPath()));
        assertThat(windowsScanPaths.getPathToJavaExecutable(), startsWith(tempDirectory + File.separator + "windows_setup" + File.separator + getScannerPath()));
        assertThat(linuxScanPaths.getPathToJavaExecutable(), startsWith(tempDirectory + File.separator + "linux_setup" + File.separator + getScannerPath()));
    }

    @Test
    public void testJavaExecutablePathWithBdsJavaHome() throws Exception {
        intEnvironmentVariables.put(BDS_JAVA_HOME, bdsJavaHomeDirectory.getAbsolutePath());

        ScanPaths macScanPaths = macScanPathsUtility.searchForScanPaths(macSetup);
        ScanPaths windowsScanPaths = windowsScanPathsUtility.searchForScanPaths(windowsSetup);
        ScanPaths linuxScanPaths = linuxScanPathsUtility.searchForScanPaths(linuxSetup);

        assertThat(macScanPaths.getPathToJavaExecutable(), is(bdsJavaHomeDirectory + File.separator + "bin" + File.separator + "java"));
        assertThat(windowsScanPaths.getPathToJavaExecutable(), is(bdsJavaHomeDirectory + File.separator + "bin" + File.separator + "java.exe"));
        assertThat(linuxScanPaths.getPathToJavaExecutable(), is(bdsJavaHomeDirectory + File.separator + "bin" + File.separator + "java"));
    }

    private void assertScanPathsOk(ScanPaths scanPaths, boolean managedByLibrary) {
        assertTrue(StringUtils.isNotBlank(scanPaths.getPathToJavaExecutable()));
        assertTrue(StringUtils.isNotBlank(scanPaths.getPathToOneJar()));
        assertTrue(StringUtils.isNotBlank(scanPaths.getPathToScanExecutable()));
        assertEquals(managedByLibrary, scanPaths.isManagedByLibrary());
    }

    private String getScannerPath() {
        return ZipApiScannerInstaller.BLACK_DUCK_SIGNATURE_SCANNER_INSTALL_DIRECTORY + File.separator + "scanner";
    }

    private void createMacSetup(File tempDirectory) throws IOException {
        macSetup = new File(tempDirectory, "mac_setup");
        createBasicSetup(macSetup);

        File icon = new File(macSetup, getScannerPath() + File.separator + "icon");
        icon.mkdirs();

        File jre = new File(macSetup, getScannerPath() + File.separator + "jre");
        File javaBin = new File(jre, "Contents" + File.separator + "Home" + File.separator + "bin");
        javaBin.mkdirs();

        File javaExe = new File(javaBin, "java");
        javaExe.createNewFile();
    }

    private void createWindowsSetup(File tempDirectory) throws IOException {
        windowsSetup = new File(tempDirectory, "windows_setup");
        createBasicSetup(windowsSetup);

        File jre = new File(windowsSetup, getScannerPath() + File.separator + "jre");
        File javaBin = new File(jre, "bin");
        javaBin.mkdirs();

        File javaExe = new File(javaBin, "java.exe");
        javaExe.createNewFile();
    }

    private void createLinuxSetup(File tempDirectory) throws IOException {
        linuxSetup = new File(tempDirectory, "linux_setup");
        createBasicSetup(linuxSetup);

        File jre = new File(linuxSetup, getScannerPath() + File.separator + "jre");
        File javaBin = new File(jre, "bin");
        javaBin.mkdirs();

        File javaExe = new File(javaBin, "java");
        javaExe.createNewFile();
    }

    private void createBasicSetup(File installDirectory) throws IOException {
        File scanDirectory = new File(installDirectory, getScannerPath());
        scanDirectory.mkdirs();

        File bin = new File(scanDirectory, "bin");
        bin.mkdirs();
        File jre = new File(scanDirectory, "jre");
        jre.mkdirs();
        File lib = new File(scanDirectory, "lib");
        File cache = new File(lib, "cache");
        cache.mkdirs();

        File standaloneJar = new File(lib, "scan.cli-x.y.z-standalone.jar");
        standaloneJar.createNewFile();

        File implStandaloneJar = new File(cache, "scan.cli.impl-standalone.jar");
        implStandaloneJar.createNewFile();
    }

    private void createWindowsJvm(File tempDirectory) throws IOException {
        File javaBin = new File(tempDirectory, "bin");
        javaBin.mkdirs();

        File javaExe = new File(javaBin, "java.exe");
        javaExe.createNewFile();
    }

    private void createOtherJvm(File tempDirectory) throws IOException {
        File javaBin = new File(tempDirectory, "bin");
        javaBin.mkdirs();

        File javaExe = new File(javaBin, "java");
        javaExe.createNewFile();
    }

}
