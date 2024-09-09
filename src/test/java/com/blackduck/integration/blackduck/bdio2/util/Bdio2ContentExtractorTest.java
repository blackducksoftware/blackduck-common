package com.blackduck.integration.blackduck.bdio2.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import com.blackduck.integration.blackduck.bdio2.util.Bdio2ContentExtractor;
import org.junit.jupiter.api.Test;

import com.blackduck.integration.blackduck.bdio2.model.BdioFileContent;

public class Bdio2ContentExtractorTest {
    @Test
    public void testFileDirectory() throws Exception {
        try {
            File bdioFile = new File(getClass().getResource("/bdio/scans/").getFile());
            Bdio2ContentExtractor reader = new Bdio2ContentExtractor();
            reader.extractContent(bdioFile);
            fail();
        } catch (IllegalArgumentException ex) {
            // pass
        }
    }

    @Test
    public void testFileMissing() throws Exception {
        try {
            File bdioFile = new File("/bdio/scans/badPath.bdio");
            Bdio2ContentExtractor reader = new Bdio2ContentExtractor();
            reader.extractContent(bdioFile);
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
            Bdio2ContentExtractor reader = new Bdio2ContentExtractor();
            reader.extractContent(bdioFile);
            fail();
        } catch (IllegalArgumentException ex) {
            // pass
        }
    }

    @Test
    public void testReadValidFile() throws Exception {
        File bdioFile = new File(getClass().getResource("/bdio/scans/developerScanTest.bdio").getFile());
        Bdio2ContentExtractor reader = new Bdio2ContentExtractor();
        List<BdioFileContent> contents = reader.extractContent(bdioFile);
        assertFalse(contents.isEmpty());
    }
}
