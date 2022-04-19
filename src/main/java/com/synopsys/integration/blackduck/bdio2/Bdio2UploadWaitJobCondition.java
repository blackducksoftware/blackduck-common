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
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.blackduck.bdio2.model.BdioFileContent;
import com.synopsys.integration.blackduck.service.request.BlackDuckRequestBuilderEditor;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.response.Response;
import com.synopsys.integration.wait.WaitJobCondition;

public class Bdio2UploadWaitJobCondition implements WaitJobCondition {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final List<Integer> SUCCESSFUL_REQUEST_STATUS_CODES = Arrays.asList(200, 201, 202, 203, 204);
    private static final List<Integer> BD_SANCTIONED_FAILURE_EXIT_CODES = Arrays.asList(401, 402, 403, 404, 500);

    private Bdio2StreamUploader bdio2Uploader;
    private BdioFileContent header;
    private List<BdioFileContent> bdioEntries;
    private BlackDuckRequestBuilderEditor editor;
    private int count;

    private HttpUrl uploadUrl;

    public Bdio2UploadWaitJobCondition(
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
    public boolean isComplete() throws IntegrationException {
        Response headerResponse = bdio2Uploader.start(header, editor);
        HttpUrl url = new HttpUrl(headerResponse.getHeaderValue("location"));
        uploadUrl = url;
        if (!responseSuccessful(headerResponse)) {
            return false;
        }
        logger.debug(String.format("Starting upload to %s", url.string()));
        for (BdioFileContent content : bdioEntries) {
            Response chunkResponse = bdio2Uploader.append(url, count, content, editor);
            if (!responseSuccessful(chunkResponse)) {
                return false;
            }
        }
        return responseSuccessful(bdio2Uploader.finish(url, count, editor));
    }

    // Return true if we get a 200, otherwise return false unless we get a sanctioned failure code
    private boolean responseSuccessful(Response response) throws IntegrationException {
        if (SUCCESSFUL_REQUEST_STATUS_CODES.contains(response.getStatusCode())) {
            return true;
        } else {
            if (BD_SANCTIONED_FAILURE_EXIT_CODES.contains(response.getStatusCode())) {
                throw new IntegrationException(String.format("Bdio upload failed with exit code: %d", response.getStatusCode()));
            }
            return false;
        }
    }

    public HttpUrl getUploadUrl() {
        return uploadUrl;
    }
}
