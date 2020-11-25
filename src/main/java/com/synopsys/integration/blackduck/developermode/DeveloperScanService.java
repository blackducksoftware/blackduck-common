/**
 * blackduck-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.blackduck.developermode;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FilenameUtils;

import com.synopsys.integration.blackduck.api.manual.view.BomMatchDeveloperView;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.exception.IntegrationException;

public class DeveloperScanService {
    public static final int DEFAULT_WAIT_INTERVAL_IN_SECONDS = 1; // TODO update to 60
    private static final String DEFAULT_SCAN_TYPE = "BlackDuckCommon";
    private static final String FILE_NAME_BDIO_HEADER_JSONLD = "bdio-header.jsonld";

    private DeveloperScanWaiter developerScanWaiter;
    private DeveloperModeBdio2Uploader bdio2Uploader;

    public DeveloperScanService(DeveloperModeBdio2Uploader bdio2Uploader, DeveloperScanWaiter developerScanWaiter) {
        this.developerScanWaiter = developerScanWaiter;
        this.bdio2Uploader = bdio2Uploader;
    }

    public List<BomMatchDeveloperView> performDeveloperScan(File bdio2File, long timeoutInSeconds) throws IntegrationException, InterruptedException {
        return performDeveloperScan(DEFAULT_SCAN_TYPE, bdio2File, timeoutInSeconds, DEFAULT_WAIT_INTERVAL_IN_SECONDS);
    }

    public List<BomMatchDeveloperView> performDeveloperScan(String scanType, File bdio2File, long timeoutInSeconds, int waitIntervalInSeconds) throws IntegrationException, InterruptedException {
        validateBdioFile(bdio2File);
        List<DeveloperModeBdioContent> developerModeBdioContentList = new ArrayList<>();
        try (ZipFile zipFile = new ZipFile(bdio2File)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                //TODO if not a JSONLD file skip
                developerModeBdioContentList.add(readEntryContent(zipFile, entry));
            }
        } catch (IOException ex) {
            throw new IntegrationException(String.format("Exception unzipping BDIO file. Path: %s", bdio2File.getAbsolutePath()), ex);
        }
        return uploadFilesAndWait(scanType, developerModeBdioContentList, timeoutInSeconds, waitIntervalInSeconds);
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

    private DeveloperModeBdioContent readEntryContent(ZipFile zipFile, ZipEntry entry) throws IntegrationException {
        String entryContent;
        byte[] buffer = new byte[1024];
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
        return new DeveloperModeBdioContent(entry.getName(), entryContent);
    }

    private List<BomMatchDeveloperView> uploadFilesAndWait(String scanType, List<DeveloperModeBdioContent> bdioFiles, long timeoutInSeconds, int waitIntervalInSeconds) throws IntegrationException, InterruptedException {
        if (bdioFiles.isEmpty()) {
            throw new IllegalArgumentException("BDIO files cannot be empty.");
        }
        DeveloperModeBdioContent header = bdioFiles.stream()
                                              .filter(content -> content.getFileName().equals(FILE_NAME_BDIO_HEADER_JSONLD))
                                              .findFirst()
                                              .orElseThrow(() -> new BlackDuckIntegrationException("Cannot find BDIO header file" + FILE_NAME_BDIO_HEADER_JSONLD + "."));

        List<DeveloperModeBdioContent> remainingFiles = bdioFiles.stream()
                                                            .filter(content -> !content.getFileName().equals(FILE_NAME_BDIO_HEADER_JSONLD))
                                                            .collect(Collectors.toList());
        UUID scanId = UUID.randomUUID();
        int count = remainingFiles.size();
        bdio2Uploader.startUpload(scanId, count, scanType, header);
        for (DeveloperModeBdioContent content : remainingFiles) {
            bdio2Uploader.uploadChunk(scanId, scanType, content);
        }

        return developerScanWaiter.checkScanResult(scanId, timeoutInSeconds, waitIntervalInSeconds);
    }
}
