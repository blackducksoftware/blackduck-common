/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.bdio2.util;

import com.blackduck.integration.blackduck.bdio2.model.BdioFileContent;
import com.blackduck.integration.exception.IntegrationException;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Bdio2ContentExtractor {

    public List<BdioFileContent> extractContent(File bdio2File) throws IntegrationException {
        validateBdioFile(bdio2File);
        List<BdioFileContent> bdioFileContentList = new ArrayList<>();
        try (ZipFile zipFile = new ZipFile(bdio2File)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String fileExtension = FilenameUtils.getExtension(entry.getName());
                if ("jsonld".equals(fileExtension)) {
                    bdioFileContentList.add(readEntryContent(zipFile, entry));
                }
            }
        } catch (IOException ex) {
            throw new IntegrationException(String.format("Exception unzipping BDIO file. Path: %s", bdio2File.getAbsolutePath()), ex);
        }
        return bdioFileContentList;
    }

    private void validateBdioFile(File bdio2File) throws IllegalArgumentException {
        String absolutePath = bdio2File.getAbsolutePath();
        if (!bdio2File.isFile()) {
            throw new IllegalArgumentException(String.format("bdio file provided is not a file. Path: %s ", absolutePath));
        }
        if (!bdio2File.exists()) {
            throw new IllegalArgumentException(String.format("bdio file does not exist. Path: %s", absolutePath));
        }
        String fileExtension = FilenameUtils.getExtension(absolutePath);
        if (!"bdio".equals(fileExtension)) {
            throw new IllegalArgumentException(String.format("Unknown file extension. Cannot perform developer scan. Path: %s", absolutePath));
        }
    }

    private BdioFileContent readEntryContent(ZipFile zipFile, ZipEntry entry) throws IntegrationException {
        String entryContent;
        byte[] buffer = new byte[4096];
        try (InputStream zipInputStream = zipFile.getInputStream(entry);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream)) {
            int length;
            while ((length = zipInputStream.read(buffer)) > 0) {
                bufferedOutputStream.write(buffer, 0, length);
            }
            bufferedOutputStream.flush();
            entryContent = new String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IntegrationException(String.format("Error reading entry %s", entry.getName()), ex);
        }
        return new BdioFileContent(entry.getName(), entryContent);
    }

}
