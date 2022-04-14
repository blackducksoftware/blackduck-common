/*
 * blackduck-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.bdio2;

import java.util.Arrays;
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
import com.synopsys.integration.wait.WaitJob;
import com.synopsys.integration.wait.WaitJobConfig;

public class Bdio2FileUploadService extends DataService {
    private static final String FILE_NAME_BDIO_HEADER_JSONLD = "bdio-header.jsonld";
    private static final int BD_WAIT_AND_RETRY_INTERVAL = 30000;
    private static final String UPLOAD_WAIT_JOB_TASK_NAME = "bdio upload";

    private final Bdio2ContentExtractor bdio2Extractor;
    private final Bdio2StreamUploader bdio2Uploader;

    public Bdio2FileUploadService(BlackDuckApiClient blackDuckApiClient, ApiDiscovery apiDiscovery,
        IntLogger logger, Bdio2ContentExtractor bdio2Extractor, Bdio2StreamUploader bdio2Uploader) {
        super(blackDuckApiClient, apiDiscovery, logger);
        this.bdio2Extractor = bdio2Extractor;
        this.bdio2Uploader = bdio2Uploader;
    }

    public HttpUrl uploadFile(UploadTarget uploadTarget, long timeout) throws IntegrationException, InterruptedException {
        logger.debug(String.format("Uploading BDIO file %s", uploadTarget.getUploadFile()));
        List<BdioFileContent> bdioFileContentList = bdio2Extractor.extractContent(uploadTarget.getUploadFile());
        return uploadFiles(bdioFileContentList, uploadTarget.getProjectAndVersion().orElse(null), timeout);
    }

    private HttpUrl uploadFiles(List<BdioFileContent> bdioFiles, @Nullable NameVersion nameVersion, long timeout) throws IntegrationException, InterruptedException {
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
            editor = builder -> {
                builder
                    .addHeader(Bdio2StreamUploader.PROJECT_NAME_HEADER, nameVersion.getName())
                    .addHeader(Bdio2StreamUploader.VERSION_NAME_HEADER, nameVersion.getVersion());
            };
        }

        WaitJobConfig waitJobConfig = new WaitJobConfig(logger, UPLOAD_WAIT_JOB_TASK_NAME, timeout, System.currentTimeMillis(), BD_WAIT_AND_RETRY_INTERVAL);
        Bdio2UploadWaitJobCondition bdio2UploadWaitJobCondition = new Bdio2UploadWaitJobCondition(bdio2Uploader, header, remainingFiles, editor, count);
        Bdio2UploadWaitJobCompleter bdio2UploadWaitJobCompleter = new Bdio2UploadWaitJobCompleter(bdio2UploadWaitJobCondition);
        WaitJob<Bdio2UploadResult> uploadWaitJob = new WaitJob<>(waitJobConfig, bdio2UploadWaitJobCondition, bdio2UploadWaitJobCompleter);

        return uploadWaitJob.waitFor().getUploadUrl();
    }

}
