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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.blackduck.bdio2.model.BdioFileContent;
import com.synopsys.integration.blackduck.service.request.BlackDuckRequestBuilderEditor;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.exception.IntegrationTimeoutException;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.exception.IntegrationRestException;
import com.synopsys.integration.rest.response.Response;
import com.synopsys.integration.wait.ResilientJob;

public class Bdio2UploadJob implements ResilientJob<Bdio2UploadResult> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final List<Integer> NON_RETRYABLE_EXIT_CODES = Arrays.asList(401, 402, 403, 404, 500);
    private static final String UPLOAD_JOB_NAME = "bdio upload";

    private final Bdio2StreamUploader bdio2Uploader;
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
        Bdio2StreamUploader bdio2Uploader,
        BdioFileContent header,
        List<BdioFileContent> bdioEntries,
        BlackDuckRequestBuilderEditor editor,
        int count,
        boolean onlyUploadHeader,
        boolean shouldFinishUpload
    ) {
        this.bdio2Uploader = bdio2Uploader;
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
            Response headerResponse = executeUploadStart();
            complete = true;
            throwIfRetryableExitCode(headerResponse);
            uploadUrl = new HttpUrl(headerResponse.getHeaderValue("location"));
            scanId = parseScanIdFromUploadUrl(uploadUrl.string());
            if (shouldUploadEntries) {
                logger.debug(String.format("Starting upload to %s", uploadUrl.string()));
                for (BdioFileContent content : bdioEntries) {
                    Response chunkResponse = executeUploadAppend(content);
                    throwIfRetryableExitCode(chunkResponse);
                }
            }
            if (shouldFinishUpload) {
                throwIfRetryableExitCode(executeUploadFinish());
            }
        } catch (RetriableBdioUploadException e) {
            complete = false;
        }
    }

    //////////////////////
    // TODO these 4 deserve their own class (or a better approach; the exceptions make it challenging)
    private Response executeUploadStart()
        throws RetriableBdioUploadException, IntegrationException {
        logger.trace("Executing BDIO upload start operation");
        try {
            return bdio2Uploader.start(header, editor);
        } catch (IntegrationRestException e) {
            return translateRetryableExceptions(e);
        }
    }

    private Response executeUploadAppend(BdioFileContent content)
        throws RetriableBdioUploadException, IntegrationException {
        logger.trace("Executing BDIO upload append operation");
        Response response = null;
        try {
            response = bdio2Uploader.append(uploadUrl, count, content, editor);
        } catch (IntegrationRestException e) {
            translateRetryableExceptions(e);
        }
        return response;
    }

    private Response executeUploadFinish()
        throws RetriableBdioUploadException, IntegrationException {
        logger.trace("Executing BDIO upload finish operation");
        Response response = null;
        try {
            response = bdio2Uploader.finish(uploadUrl, count, editor);
        } catch (IntegrationRestException e) {
            translateRetryableExceptions(e);
        }
        return response;
    }

    private Response translateRetryableExceptions(final IntegrationRestException e) throws RetriableBdioUploadException, IntegrationRestException {
        if (isRetryableExitCode(e.getHttpStatusCode())) {
            throw new RetriableBdioUploadException();
        }
        throw e;
    }
    ///////////////////

    // If response is unsuccessful, throw a recoverable RetriableBdioUploadException unless we get a failure status code in which case we throw an unrecoverable exception
    private void throwIfRetryableExitCode(Response response) throws IntegrationException, RetriableBdioUploadException {
        if (!response.isStatusCodeSuccess()) {
            if (isRetryableExitCode(response.getStatusCode())) {
                throw new RetriableBdioUploadException();
            }
            throw new IntegrationException(String.format("Bdio upload failed with non-retryable exit code: %d", response.getStatusCode()));
        }
    }

    private boolean isRetryableExitCode(int exitCode) {
        if (NON_RETRYABLE_EXIT_CODES.contains(exitCode)) {
            return false;
        }
        return true;
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
