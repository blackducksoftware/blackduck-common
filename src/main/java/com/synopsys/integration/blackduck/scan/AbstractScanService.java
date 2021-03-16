/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.scan;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpUrl;

public class AbstractScanService {
    private static final String FILE_NAME_BDIO_HEADER_JSONLD = "bdio-header.jsonld";

    private ScanBdio2Reader bdio2Reader;
    private ScanBdio2Uploader bdio2Uploader;

    public AbstractScanService(final ScanBdio2Reader bdio2Reader, final ScanBdio2Uploader bdio2Uploader) {
        this.bdio2Reader = bdio2Reader;
        this.bdio2Uploader = bdio2Uploader;
    }

    protected HttpUrl readContentAndUpload(File bdio2File) throws IntegrationException {
        List<ScanBdioContent> scanBdioContentList = bdio2Reader.readBdio2File(bdio2File);
        return uploadFiles(scanBdioContentList);
    }

    private HttpUrl uploadFiles(List<ScanBdioContent> bdioFiles) throws IntegrationException {
        if (bdioFiles.isEmpty()) {
            throw new IllegalArgumentException("BDIO files cannot be empty.");
        }
        ScanBdioContent header = bdioFiles.stream()
                                     .filter(content -> content.getFileName().equals(FILE_NAME_BDIO_HEADER_JSONLD))
                                     .findFirst()
                                     .orElseThrow(() -> new BlackDuckIntegrationException("Cannot find BDIO header file" + FILE_NAME_BDIO_HEADER_JSONLD + "."));

        List<ScanBdioContent> remainingFiles = bdioFiles.stream()
                                                   .filter(content -> !content.getFileName().equals(FILE_NAME_BDIO_HEADER_JSONLD))
                                                   .collect(Collectors.toList());
        int count = remainingFiles.size();
        HttpUrl url = bdio2Uploader.start(header);
        for (ScanBdioContent content : remainingFiles) {
            bdio2Uploader.append(url, count, content);
        }
        bdio2Uploader.finish(url, count);

        return url;
    }

}
