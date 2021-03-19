/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.bdio2.stream;

import java.util.List;
import java.util.stream.Collectors;

import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadTarget;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.blackduck.http.BlackDuckRequestFactory;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.DataService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpUrl;

public class Bdio2FileUploadService extends DataService {
    private static final String FILE_NAME_BDIO_HEADER_JSONLD = "bdio-header.jsonld";

    private Bdio2ContentExtractor bdio2Reader;
    private Bdio2StreamUploadService bdio2Uploader;

    public Bdio2FileUploadService(final BlackDuckApiClient blackDuckApiClient, final BlackDuckRequestFactory blackDuckRequestFactory,
        final IntLogger logger, final Bdio2ContentExtractor bdio2Reader, final Bdio2StreamUploadService bdio2Uploader) {
        super(blackDuckApiClient, blackDuckRequestFactory, logger);
        this.bdio2Reader = bdio2Reader;
        this.bdio2Uploader = bdio2Uploader;
    }

    public HttpUrl uploadFile(UploadTarget uploadTarget) throws IntegrationException {
        List<BdioFileContent> bdioFileContentList = bdio2Reader.readBdio2File(uploadTarget.getUploadFile());
        return uploadFiles(bdioFileContentList);
    }

    private HttpUrl uploadFiles(List<BdioFileContent> bdioFiles) throws IntegrationException {
        if (bdioFiles.isEmpty()) {
            throw new IllegalArgumentException("BDIO files cannot be empty.");
        }
        BdioFileContent header = bdioFiles.stream()
                                     .filter(content -> content.getFileName().equals(FILE_NAME_BDIO_HEADER_JSONLD))
                                     .findFirst()
                                     .orElseThrow(() -> new BlackDuckIntegrationException("Cannot find BDIO header file" + FILE_NAME_BDIO_HEADER_JSONLD + "."));

        List<BdioFileContent> remainingFiles = bdioFiles.stream()
                                                   .filter(content -> !content.getFileName().equals(FILE_NAME_BDIO_HEADER_JSONLD))
                                                   .collect(Collectors.toList());
        int count = remainingFiles.size();
        HttpUrl url = bdio2Uploader.start(header);
        for (BdioFileContent content : remainingFiles) {
            bdio2Uploader.append(url, count, content);
        }
        bdio2Uploader.finish(url, count);

        return url;
    }

}
