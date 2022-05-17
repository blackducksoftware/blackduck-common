/*
 * blackduck-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.bdio2;

import java.util.List;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.bdio2.model.BdioFileContent;
import com.synopsys.integration.blackduck.bdio2.util.Bdio2ContentExtractor;
import com.synopsys.integration.blackduck.codelocation.upload.UploadTarget;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.DataService;
import com.synopsys.integration.blackduck.service.request.BlackDuckRequestBuilderEditor;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.response.Response;
import com.synopsys.integration.util.NameVersion;

public class Bdio2FileUploadService extends DataService {
    private static final String FILE_NAME_BDIO_HEADER_JSONLD = "bdio-header.jsonld";

    private final Bdio2ContentExtractor bdio2Extractor;
    private final Bdio2StreamUploader bdio2Uploader;

    public Bdio2FileUploadService(
        BlackDuckApiClient blackDuckApiClient,
        ApiDiscovery apiDiscovery,
        IntLogger logger,
        Bdio2ContentExtractor bdio2Extractor,
        Bdio2StreamUploader bdio2Uploader
    ) {
        super(blackDuckApiClient, apiDiscovery, logger);
        this.bdio2Extractor = bdio2Extractor;
        this.bdio2Uploader = bdio2Uploader;
    }

    public Bdio2UploadResult uploadFile(UploadTarget uploadTarget) throws IntegrationException {
        logger.debug(String.format("Uploading BDIO file %s", uploadTarget.getUploadFile()));
        List<BdioFileContent> bdioFileContentList = bdio2Extractor.extractContent(uploadTarget.getUploadFile());
        return uploadFiles(bdioFileContentList, uploadTarget.getProjectAndVersion().orElse(null));
    }

    private Bdio2UploadResult uploadFiles(List<BdioFileContent> bdioFiles, @Nullable NameVersion nameVersion) throws IntegrationException {
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
        logger.debug("BDIO upload file count = " + count);

        BlackDuckRequestBuilderEditor editor = noOp -> {};
        if (nameVersion != null) {
            editor = builder -> builder
                .addHeader(Bdio2StreamUploader.PROJECT_NAME_HEADER, nameVersion.getName())
                .addHeader(Bdio2StreamUploader.VERSION_NAME_HEADER, nameVersion.getVersion());
        }

        Response headerResponse = bdio2Uploader.start(header, editor);
        HttpUrl url = new HttpUrl(headerResponse.getHeaderValue("location"));
        String scanId = parseScanIdFromScanUrl(url.toString());
        for (BdioFileContent content : remainingFiles) {
            bdio2Uploader.append(url, count, content, editor);
        }
        bdio2Uploader.finish(url, count, editor);

        return new Bdio2UploadResult(url, scanId);
    }

    private String parseScanIdFromScanUrl(String scanUrl) {
        String[] urlPieces = scanUrl.split("/");
        return urlPieces[urlPieces.length-1];
    }

}
