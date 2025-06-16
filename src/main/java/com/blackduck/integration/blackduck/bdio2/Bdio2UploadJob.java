/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.bdio2;

import com.blackduck.integration.blackduck.bdio2.model.BdioFileContent;
import com.blackduck.integration.blackduck.service.request.BlackDuckRequestBuilderEditor;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.exception.IntegrationTimeoutException;
import com.blackduck.integration.rest.HttpUrl;
import com.blackduck.integration.rest.response.Response;
import com.blackduck.integration.wait.ResilientJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Bdio2UploadJob implements ResilientJob<Bdio2UploadResult> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String UPLOAD_JOB_NAME = "bdio upload";
    private static final String INTELLIGENT_PERSISTANCE_API_ENDPOINT = "api/intelligent-persistence-scans/";

    private final Bdio2RetryAwareStreamUploader bdio2RetryAwareStreamUploader;
    private final BdioFileContent header;
    private final List<BdioFileContent> bdioEntries;
    private final BlackDuckRequestBuilderEditor editor;
    private final int count;
    private final boolean shouldUploadEntries;
    private final boolean shouldFinishUpload;

    private HttpUrl uploadUrl;
    private String scanId;
    private boolean complete;
    private HttpUrl blackDuckUrl;
    private long startTime;
    private long timeout;

    public Bdio2UploadJob(
        Bdio2RetryAwareStreamUploader bdio2RetryAwareStreamUploader,
        BdioFileContent header,
        List<BdioFileContent> bdioEntries,
        BlackDuckRequestBuilderEditor editor,
        int count,
        boolean onlyUploadHeader,
        boolean shouldFinishUpload,
        long startTime,
        long timeout, 
        String scanId,
        HttpUrl blackDuckUrl
    ) {
        this.bdio2RetryAwareStreamUploader = bdio2RetryAwareStreamUploader;
        this.header = header;
        this.bdioEntries = bdioEntries;
        this.editor = editor;
        this.count = count;
        this.shouldUploadEntries = onlyUploadHeader;
        this.shouldFinishUpload = shouldFinishUpload;
        this.startTime = startTime;
        this.timeout = timeout;
        this.scanId = scanId;
        this.blackDuckUrl = blackDuckUrl;
    }

    @Override
    public void attemptJob() throws IntegrationException {
        try {
            if(isLegacyWorkFlow()){
                Response headerResponse = bdio2RetryAwareStreamUploader.start(header, editor, startTime, timeout);
                bdio2RetryAwareStreamUploader.onErrorThrowRetryableOrFailure(headerResponse);
                uploadUrl = new HttpUrl(headerResponse.getHeaderValue("location"));
                scanId = parseScanIdFromUploadUrl(uploadUrl.string());
            } else {
                uploadUrl = new HttpUrl(blackDuckUrl + INTELLIGENT_PERSISTANCE_API_ENDPOINT + scanId);
            }
            complete = true;
            if (shouldUploadEntries) {
                logger.debug(String.format("Starting upload to %s", uploadUrl.string()));
                for (BdioFileContent content : bdioEntries) {
                    Response chunkResponse = bdio2RetryAwareStreamUploader.append(uploadUrl, count, content, editor);
                    bdio2RetryAwareStreamUploader.onErrorThrowRetryableOrFailure(chunkResponse);
                }
            }
            if (shouldFinishUpload) {
                Response finishResponse = bdio2RetryAwareStreamUploader.finish(uploadUrl, count, editor);
                bdio2RetryAwareStreamUploader.onErrorThrowRetryableOrFailure(finishResponse);
            }
        } catch (RetriableBdioUploadException | InterruptedException e) {
            complete = false;
        }
    }

    private String parseScanIdFromUploadUrl(String uploadUrl) {
        String[] pieces = uploadUrl.split("/");
        return pieces[pieces.length - 1];
    }

    private boolean isLegacyWorkFlow() {
        return scanId == null || scanId.isEmpty() || blackDuckUrl == null || blackDuckUrl.toString().isEmpty();
    }

    @Override
    public boolean wasJobCompleted() {
        return complete;
    }

    @Override
    public Bdio2UploadResult onTimeout() throws IntegrationTimeoutException {
        throw new IntegrationTimeoutException("Not able to upload BDIO due to timeout.");
    }

    @Override
    public Bdio2UploadResult onCompletion() {
        return new Bdio2UploadResult(uploadUrl, scanId);
    }

    @Override
    public String getName() {
        return UPLOAD_JOB_NAME;
    }
}
