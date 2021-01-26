package com.synopsys.integration.blackduck.developermode;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import org.junit.jupiter.api.Test;

public class RapidScanBdio2ReaderTest {
    @Test
    public void testFileDirectory() throws Exception {
        try {
            File bdioFile = new File(getClass().getResource("/bdio/developer_scan/").getFile());
            RapidScanBdio2Reader reader = new RapidScanBdio2Reader();
            reader.readBdio2File(bdioFile);
            fail();
        } catch (IllegalArgumentException ex) {
            // pass
        }
    }

    @Test
    public void testFileMissing() throws Exception {
        try {
            File bdioFile = new File("/bdio/developer_scan/badPath.bdio");
            RapidScanBdio2Reader reader = new RapidScanBdio2Reader();
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
            RapidScanBdio2Reader reader = new RapidScanBdio2Reader();
            reader.readBdio2File(bdioFile);
            fail();
        } catch (IllegalArgumentException ex) {
            // pass
        }
    }

    @Test
    public void testReadValidFile() throws Exception {
        File bdioFile = new File(getClass().getResource("/bdio/developer_scan/developerScanTest.bdio").getFile());
        RapidScanBdio2Reader reader = new RapidScanBdio2Reader();
        List<DeveloperModeBdioContent> contents = reader.readBdio2File(bdioFile);
        assertFalse(contents.isEmpty());
    }
}
