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

import org.apache.commons.io.FileUtils;

import com.synopsys.integration.blackduck.api.manual.view.BomMatchDeveloperView;
import com.synopsys.integration.blackduck.http.client.BlackDuckHttpClient;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.body.StringBodyContent;
import com.synopsys.integration.rest.request.Request;

public class DeveloperScanService {
    private static final String CONTENT_TYPE = "application/vnd.blackducksoftware.developer-scan-ld-1+json";
    private static final String HEADER_X_BD_MODE = "X-BD-MODE";
    private static final String HEADER_X_BD_SCAN_ID = "X-BD-SCAN-ID";
    private static final String HEADER_X_BD_DOCUMENT_COUNT = "X-BD-DOCUMENT-COUNT";
    private static final String HEADER_X_BD_SCAN_TYPE = "X-BD-SCAN-TYPE";
    private static final String FILE_NAME_BDIO_HEADER_JSONLD = "bdio-header.jsonld";

    private BlackDuckHttpClient blackDuckHttpClient;

    public DeveloperScanService(final BlackDuckHttpClient blackDuckHttpClient) {
        this.blackDuckHttpClient = blackDuckHttpClient;
    }

    public BomMatchDeveloperView performDeveloperScan(String scanType, File bdioFile) throws IntegrationException {
        if (!bdioFile.isFile()) {
            throw new IllegalArgumentException(String.format("bdio file provided is not a file. Path: %s ", bdioFile.getAbsolutePath()));
        }
        if (!bdioFile.exists()) {
            throw new IllegalArgumentException(String.format("bdio file does not exist. Path: %s", bdioFile.getAbsolutePath()));
        }

        if (!bdioFile.toPath().endsWith(".bdio")) {
            throw new IllegalArgumentException(String.format("Unknown file extension. Cannot perform developer scan. Path: %s", bdioFile.getAbsolutePath()));
        }
        List<BdioContent> bdioContentList = new ArrayList<>();
        try (ZipFile zipFile = new ZipFile(bdioFile)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                bdioContentList.add(readEntryContent(zipFile, entry));
            }
        } catch (IOException ex) {
            throw new IntegrationException(String.format("Exception unzipping BDIO file. Path: %s", bdioFile.getAbsolutePath()), ex);
        }
        return uploadFilesAndWait(scanType, bdioContentList);
    }

    public BomMatchDeveloperView performDeveloperScan(String scanType, List<File> bdioFiles) throws IntegrationException {
        if (bdioFiles.isEmpty()) {
            throw new IllegalArgumentException("bdio files cannot be empty.");
        }

        List<BdioContent> bdioContentList = new ArrayList<>();
        try {
            for (File bdioFile : bdioFiles) {
                String fileContent = FileUtils.readFileToString(bdioFile, StandardCharsets.UTF_8);
                bdioContentList.add(new BdioContent(bdioFile.getName(), fileContent));
            }
        } catch (IOException ex) {
            throw new IntegrationException("Couldn't read BDIO file", ex);
        }
        return uploadFilesAndWait(scanType, bdioContentList);
    }

    private BdioContent readEntryContent(ZipFile zipFile, ZipEntry entry) throws IntegrationException {
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
        return new BdioContent(entry.getName(), entryContent);
    }

    private BomMatchDeveloperView uploadFilesAndWait(String scanType, List<BdioContent> bdioFiles) throws IntegrationException {
        if (bdioFiles.isEmpty()) {
            throw new IllegalArgumentException("BDIO files cannot be empty.");
        }
        BdioContent header = bdioFiles.stream()
                                 .filter(content -> content.getFileName().equals(FILE_NAME_BDIO_HEADER_JSONLD))
                                 .findFirst()
                                 .orElseThrow(() -> new IntegrationException("Cannot find BDIO header file" + FILE_NAME_BDIO_HEADER_JSONLD + "."));

        List<BdioContent> remainingFiles = bdioFiles.stream()
                                               .filter(content -> !content.getFileName().equals(FILE_NAME_BDIO_HEADER_JSONLD))
                                               .collect(Collectors.toList());
        UUID scanId = UUID.randomUUID();
        int count = bdioFiles.size();
        startUpload(scanId, count, scanType, header);
        for (BdioContent content : remainingFiles) {
            uploadChunk(scanId, scanType, content);
        }

        // poll wait for result;

        return new BomMatchDeveloperView();
    }

    private void startUpload(UUID scanId, int count, String scanType, BdioContent header) throws IntegrationException {
        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.addHeader("Content-type", CONTENT_TYPE);
        requestBuilder.addHeader(HEADER_X_BD_MODE, "start");
        requestBuilder.addHeader(HEADER_X_BD_SCAN_ID, scanId.toString());
        requestBuilder.addHeader(HEADER_X_BD_DOCUMENT_COUNT, String.valueOf(count));
        requestBuilder.addHeader(HEADER_X_BD_SCAN_TYPE, scanType);
        StringBodyContent bodyContent = new StringBodyContent(header.getContent());
        requestBuilder.bodyContent(bodyContent);
        blackDuckHttpClient.execute(requestBuilder.build());
    }

    private void uploadChunk(UUID scanId, String scanType, BdioContent bdioContent) throws IntegrationException {
        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.addHeader("Content-type", CONTENT_TYPE);
        requestBuilder.addHeader(HEADER_X_BD_MODE, "append");
        requestBuilder.addHeader(HEADER_X_BD_SCAN_ID, scanId.toString());
        requestBuilder.addHeader(HEADER_X_BD_SCAN_TYPE, scanType);
        StringBodyContent bodyContent = new StringBodyContent(bdioContent.getContent());
        blackDuckHttpClient.execute(requestBuilder.build());
    }

    private class BdioContent {
        private String fileName;
        private String content;

        public BdioContent(final String fileName, final String content) {
            this.fileName = fileName;
            this.content = content;
        }

        public String getFileName() {
            return fileName;
        }

        public String getContent() {
            return content;
        }
    }
}
