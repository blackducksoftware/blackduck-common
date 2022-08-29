/*
 * blackduck-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.bdio2;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.blackduck.bdio2.model.BdioFileContent;
import com.synopsys.integration.blackduck.service.request.BlackDuckRequestBuilderEditor;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.exception.IntegrationTimeoutException;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.response.Response;
import com.synopsys.integration.wait.ResilientJob;

public class Bdio2UploadJob implements ResilientJob<Bdio2UploadResult> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String UPLOAD_JOB_NAME = "bdio upload";

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

    public Bdio2UploadJob(
        Bdio2RetryAwareStreamUploader bdio2RetryAwareStreamUploader,
        BdioFileContent header,
        List<BdioFileContent> bdioEntries,
        BlackDuckRequestBuilderEditor editor,
        int count,
        boolean onlyUploadHeader,
        boolean shouldFinishUpload
    ) {
        this.bdio2RetryAwareStreamUploader = bdio2RetryAwareStreamUploader;
        this.header = header;
        this.bdioEntries = bdioEntries;
        this.editor = editor;
        this.count = count;
        this.shouldUploadEntries = onlyUploadHeader;
        this.shouldFinishUpload = shouldFinishUpload;
    }

    @Override
    public void attemptJob() throws IntegrationException {
        try {
            Response headerResponse = bdio2RetryAwareStreamUploader.executeUploadStart(header, editor);
            bdio2RetryAwareStreamUploader.onErrorThrowRetryableOrFailure(headerResponse);
            complete = true;
            uploadUrl = new HttpUrl(headerResponse.getHeaderValue("location"));
            scanId = parseScanIdFromUploadUrl(uploadUrl.string());
            if (shouldUploadEntries) {
                logger.debug(String.format("Starting upload to %s", uploadUrl.string()));
                for (BdioFileContent content : bdioEntries) {
                    Response chunkResponse = bdio2RetryAwareStreamUploader.executeUploadAppend(uploadUrl, count, content, editor);
                    bdio2RetryAwareStreamUploader.onErrorThrowRetryableOrFailure(chunkResponse);
                }
            }
            if (shouldFinishUpload) {
                Response finishResponse = bdio2RetryAwareStreamUploader.executeUploadFinish(uploadUrl, count, editor);
                bdio2RetryAwareStreamUploader.onErrorThrowRetryableOrFailure(finishResponse);
            }
        } catch (RetriableBdioUploadException e) {
            complete = false;
        }
    }

    private String parseScanIdFromUploadUrl(String uploadUrl) {
        String[] pieces = uploadUrl.split("/");
        return pieces[pieces.length - 1];
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
