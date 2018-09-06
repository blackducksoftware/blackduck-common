package com.synopsys.integration.blackduck.signaturescanner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.synopsys.integration.blackduck.signaturescanner.command.ScanPaths;
import com.synopsys.integration.blackduck.signaturescanner.command.ScanPathsUtility;
import com.synopsys.integration.blackduck.signaturescanner.command.ScannerZipInstaller;
import com.synopsys.integration.test.tool.TestLogger;

public class ScanPathsTest {
    private final ScanPathsUtility macScanPathsUtility = new ScanPathsUtility(new TestLogger(), OperatingSystemType.MAC);
    private final ScanPathsUtility windowsScanPathsUtility = new ScanPathsUtility(new TestLogger(), OperatingSystemType.WINDOWS);
    private final ScanPathsUtility linuxScanPathsUtility = new ScanPathsUtility(new TestLogger(), OperatingSystemType.LINUX);

    private File tempDirectory;
    private File macSetup;
    private File windowsSetup;
    private File linuxSetup;

    @Before
    public void createFileSetups() throws Exception {
        final Path tempDirectoryPath = Files.createTempDirectory("scan_setups");
        tempDirectory = tempDirectoryPath.toFile();

        createMacSetup(tempDirectory);
        createWindowsSetup(tempDirectory);
        createLinuxSetup(tempDirectory);
    }

    @After
    public void deleteTempDirectory() {
        FileUtils.deleteQuietly(tempDirectory);
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

}
