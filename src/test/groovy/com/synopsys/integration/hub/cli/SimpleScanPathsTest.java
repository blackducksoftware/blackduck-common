package com.synopsys.integration.hub.cli;

import java.io.File;

import org.junit.Test;

import com.synopsys.integration.hub.cli.simple.SimpleScanPaths;
import com.synopsys.integration.hub.cli.simple.SimpleScanPathsUtility;
import com.synopsys.integration.test.TestLogger;

public class SimpleScanPathsTest {
    private final SimpleScanPathsUtility simpleScanPathsUtility = new SimpleScanPathsUtility();

    @Test
    public void testSignatureScannerPathsInstalledByUs() throws Exception {
        final SimpleScanPaths simpleScanPaths = simpleScanPathsUtility.determineSignatureScannerPaths(new TestLogger(), new File("/Users/ekerwin/working/scan_install"));
        System.out.println(simpleScanPaths.getPathToJavaExecutable());
        System.out.println(simpleScanPaths.getPathToOneJar());
        System.out.println(simpleScanPaths.getPathToScanExecutable());
    }

    @Test
    public void testSignatureScannerPathsInstalledByThem() throws Exception {
        final SimpleScanPaths simpleScanPaths = simpleScanPathsUtility.determineSignatureScannerPaths(new TestLogger(), new File("/Users/ekerwin/working/scan_install/Hub_Scan_Installation/scan.cli-4.8.0"));
        System.out.println(simpleScanPaths.getPathToJavaExecutable());
        System.out.println(simpleScanPaths.getPathToOneJar());
        System.out.println(simpleScanPaths.getPathToScanExecutable());
    }

}
