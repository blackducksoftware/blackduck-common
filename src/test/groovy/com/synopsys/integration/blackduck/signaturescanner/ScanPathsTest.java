package com.synopsys.integration.blackduck.signaturescanner;

import java.io.File;

import org.junit.Test;

import com.synopsys.integration.test.TestLogger;

public class ScanPathsTest {
    private final ScanPathsUtility scanPathsUtility = new ScanPathsUtility(new TestLogger());

    @Test
    public void testSignatureScannerPathsInstalledByUs() throws Exception {
        final ScanPaths scanPaths = scanPathsUtility.determineSignatureScannerPaths(new File("/Users/ekerwin/working/scan_install"));
        System.out.println(scanPaths.getPathToJavaExecutable());
        System.out.println(scanPaths.getPathToOneJar());
        System.out.println(scanPaths.getPathToScanExecutable());
    }

    @Test
    public void testSignatureScannerPathsInstalledByThem() throws Exception {
        final ScanPaths scanPaths = scanPathsUtility.determineSignatureScannerPaths(new File("/Users/ekerwin/working/scan_install/Hub_Scan_Installation/scan.cli-4.8.0"));
        System.out.println(scanPaths.getPathToJavaExecutable());
        System.out.println(scanPaths.getPathToOneJar());
        System.out.println(scanPaths.getPathToScanExecutable());
    }

}
