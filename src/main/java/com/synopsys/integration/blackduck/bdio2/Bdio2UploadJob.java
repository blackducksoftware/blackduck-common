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
import com.synopsys.integration.rest.response.Response;
import com.synopsys.integration.wait.ResilientJob;

public class Bdio2UploadJob implements ResilientJob<Bdio2UploadResult> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final List<Integer> BD_FAILURE_EXIT_CODES = Arrays.asList(401, 402, 403, 404, 500);
    private static final String UPLOAD_JOB_NAME = "bdio upload";

    private final Bdio2StreamUploader bdio2Uploader;
    private final BdioFileContent header;
    private final List<BdioFileContent> bdioEntries;
    private final BlackDuckRequestBuilderEditor editor;
    private final int count;

    private HttpUrl uploadUrl;
    private boolean complete;

    public Bdio2UploadJob(
        final Bdio2StreamUploader bdio2Uploader,
        final BdioFileContent header,
        final List<BdioFileContent> bdioEntries,
        final BlackDuckRequestBuilderEditor editor,
        final int count
    ) {
        this.bdio2Uploader = bdio2Uploader;
        this.header = header;
        this.bdioEntries = bdioEntries;
        this.editor = editor;
        this.count = count;
    }

    @Override
    public void attemptJob() throws IntegrationException {
        Response headerResponse = bdio2Uploader.start(header, editor);
        complete = true;
        try {
            throwIfResponseUnsuccessful(headerResponse);
            uploadUrl = new HttpUrl(headerResponse.getHeaderValue("location"));
            logger.debug(String.format("Starting upload to %s", uploadUrl.string()));
            for (BdioFileContent content : bdioEntries) {
                Response chunkResponse = bdio2Uploader.append(uploadUrl, count, content, editor);
                throwIfResponseUnsuccessful(chunkResponse);
            }
            throwIfResponseUnsuccessful(bdio2Uploader.finish(uploadUrl, count, editor));
        } catch (RetriableBdioUploadException e) {
            complete = false;
        }
    }

    // If response is unsuccessful, throw a recoverable RetriableBdioUploadException unless we get a failure status code in which case we throw an unrecoverable exception
    private void throwIfResponseUnsuccessful(Response response) throws IntegrationException, RetriableBdioUploadException {
        if (!response.isStatusCodeSuccess()) {
            if (BD_FAILURE_EXIT_CODES.contains(response.getStatusCode())) {
                throw new IntegrationException(String.format("Bdio upload failed with exit code: %d", response.getStatusCode()));
            }
            throw new RetriableBdioUploadException();
        }
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
        return new Bdio2UploadResult(uploadUrl);
    }

    @Override
    public String getName() {
        return UPLOAD_JOB_NAME;
    }
}
