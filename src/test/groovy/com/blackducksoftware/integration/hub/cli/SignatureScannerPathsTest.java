package com.blackducksoftware.integration.hub.cli;

import java.io.File;

import org.junit.Test;

import com.blackducksoftware.integration.test.TestLogger;

public class SignatureScannerPathsTest {
    @Test
    public void testSignatureScannerPathsInstalledByUs() throws Exception {
        final SignatureScannerPaths signatureScannerPaths = new SignatureScannerPaths(new TestLogger(), new File("/Users/ekerwin/working/scan_install"));
        System.out.println(signatureScannerPaths.getPathToJavaExecutable());
        System.out.println(signatureScannerPaths.getPathToOneJar());
        System.out.println(signatureScannerPaths.getPathToScanExecutable());
    }

    @Test
    public void testSignatureScannerPathsInstalledByThem() throws Exception {
        final SignatureScannerPaths signatureScannerPaths = new SignatureScannerPaths(new TestLogger(), new File("/Users/ekerwin/working/scan_install/Hub_Scan_Installation/scan.cli-4.8.0"));
        System.out.println(signatureScannerPaths.getPathToJavaExecutable());
        System.out.println(signatureScannerPaths.getPathToOneJar());
        System.out.println(signatureScannerPaths.getPathToScanExecutable());
    }

}
