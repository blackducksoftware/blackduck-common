/*
 * blackduck-common
 *
 * Copyright (c) 2023 Synopsys, Inc.
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
import com.synopsys.integration.blackduck.version.BlackDuckVersion;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.util.NameVersion;
import com.synopsys.integration.wait.ResilientJobConfig;
import com.synopsys.integration.wait.ResilientJobExecutor;
import com.synopsys.integration.wait.tracker.WaitIntervalTracker;
import com.synopsys.integration.wait.tracker.WaitIntervalTrackerFactory;

public class Bdio2FileUploadService extends DataService {
    private static final String FILE_NAME_BDIO_HEADER_JSONLD = "bdio-header.jsonld";
    private static final int BD_WAIT_AND_RETRY_INTERVAL = 30;

    private final Bdio2ContentExtractor bdio2Extractor;
    private final Bdio2RetryAwareStreamUploader bdio2RetryAwareStreamUploader;

    public Bdio2FileUploadService(
        BlackDuckApiClient blackDuckApiClient,
        ApiDiscovery apiDiscovery,
        IntLogger logger,
        Bdio2ContentExtractor bdio2Extractor,
        Bdio2RetryAwareStreamUploader bdio2RetryAwareStreamUploader
    ) {
        super(blackDuckApiClient, apiDiscovery, logger);
        this.bdio2Extractor = bdio2Extractor;
        this.bdio2RetryAwareStreamUploader = bdio2RetryAwareStreamUploader;
    }

    public Bdio2UploadResult uploadFile(UploadTarget uploadTarget, long timeout, long clientStartTime) throws IntegrationException, InterruptedException {
        return uploadFile(uploadTarget, timeout, true, true, clientStartTime);
    }

    public Bdio2UploadResult uploadFile(UploadTarget uploadTarget, long timeout, boolean shouldUploadEntries, boolean shouldFinishUpload, long clientStartTime)
        throws IntegrationException, InterruptedException {
        logger.debug(String.format("Uploading BDIO file %s", uploadTarget.getUploadFile()));
        List<BdioFileContent> bdioFileContentList = bdio2Extractor.extractContent(uploadTarget.getUploadFile());
        return uploadFiles(bdioFileContentList, uploadTarget.getProjectAndVersion().orElse(null), timeout, shouldUploadEntries, shouldFinishUpload, clientStartTime);
    }

    private Bdio2UploadResult uploadFiles(List<BdioFileContent> bdioFiles, @Nullable NameVersion nameVersion, long timeout, boolean shouldUploadEntries, boolean shouldFinishUpload, long clientStartTime)
        throws IntegrationException, InterruptedException {
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
        if (nameVersion != null && !useOnlyBdioHeaders()) {
            editor = builder -> builder
                .addHeader(Bdio2StreamUploader.PROJECT_NAME_HEADER, nameVersion.getName())
                .addHeader(Bdio2StreamUploader.VERSION_NAME_HEADER, nameVersion.getVersion());
        }

        WaitIntervalTracker waitIntervalTracker = WaitIntervalTrackerFactory.createConstant(timeout, BD_WAIT_AND_RETRY_INTERVAL);
        ResilientJobConfig jobConfig = new ResilientJobConfig(logger, System.currentTimeMillis(), waitIntervalTracker);
        Bdio2UploadJob bdio2UploadJob = new Bdio2UploadJob(bdio2RetryAwareStreamUploader, header, remainingFiles, editor, count, shouldUploadEntries, shouldFinishUpload, timeout, clientStartTime);
        ResilientJobExecutor jobExecutor = new ResilientJobExecutor(jobConfig);

        return jobExecutor.executeJob(bdio2UploadJob);
    }

    // BlackDuck servers 2023.4.1 and later can use only BDIO header information for 
    // project names and version names and do not need the REST headers which can have issues
    // with non-ASCII characters.
	private boolean useOnlyBdioHeaders() {
		if (blackDuckApiClient.getBlackDuckVersion().isPresent()) {
			BlackDuckVersion currentBlackDuckVersion = blackDuckApiClient.getBlackDuckVersion().get();
			BlackDuckVersion requiresBdioHeadersVersion = new BlackDuckVersion(2023, 4, 1);
			
			return currentBlackDuckVersion.isAtLeast(requiresBdioHeadersVersion);
		}
        	
		return false;
	}

}
