package com.synopsys.integration.blackduck.scan;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import org.junit.jupiter.api.Test;

public class ScanBdio2ReaderTest {
    @Test
    public void testFileDirectory() throws Exception {
        try {
            File bdioFile = new File(getClass().getResource("/bdio/scans/").getFile());
            ScanBdio2Reader reader = new ScanBdio2Reader();
            reader.readBdio2File(bdioFile);
            fail();
        } catch (IllegalArgumentException ex) {
            // pass
        }
    }

    @Test
    public void testFileMissing() throws Exception {
        try {
            File bdioFile = new File("/bdio/scans/badPath.bdio");
            ScanBdio2Reader reader = new ScanBdio2Reader();
            reader.readBdio2File(bdioFile);
            fail();
        } catch (IllegalArgumentException ex) {
            // pass
        }
    }

    @Test
    public void testExtensionInvalid() throws Exception {
        File bdioFile = Files.createTempFile("badExtension", "txt").toFile();
        bdioFile.deleteOnExit();
        try {
            ScanBdio2Reader reader = new ScanBdio2Reader();
            reader.readBdio2File(bdioFile);
            fail();
        } catch (IllegalArgumentException ex) {
            // pass
        }
    }

    @Test
    public void testReadValidFile() throws Exception {
        File bdioFile = new File(getClass().getResource("/bdio/scans/developerScanTest.bdio").getFile());
        ScanBdio2Reader reader = new ScanBdio2Reader();
        List<ScanBdioContent> contents = reader.readBdio2File(bdioFile);
        assertFalse(contents.isEmpty());
    }
}
