/*
 * Copyright (C) 2017 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.hub.api.scan;

import static com.blackducksoftware.integration.hub.api.UrlConstants.SEGMENT_API;
import static com.blackducksoftware.integration.hub.api.UrlConstants.SEGMENT_SCANS;
import static com.blackducksoftware.integration.hub.api.UrlConstants.SEGMENT_V1;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.model.response.DryRunUploadResponse;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubResponseService;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DryRunUploadRequestService extends HubResponseService {
    private static final List<String> DRY_RUN_UPLOAD_SEGMENTS = Arrays.asList(SEGMENT_API, SEGMENT_V1, SEGMENT_SCANS);

    private final RestConnection restConnection;

    public DryRunUploadRequestService(final RestConnection restConnection) {
        super(restConnection);
        this.restConnection = restConnection;
    }

    public DryRunUploadResponse uploadDryRunFile(final File dryRunFile) throws IntegrationException {
        final HttpUrl httpUrl = restConnection.createHttpUrl(DRY_RUN_UPLOAD_SEGMENTS);
        final Request request = restConnection.createPostRequest(httpUrl, RequestBody.create(MediaType.parse("application/json"), dryRunFile));
        try (Response response = restConnection.handleExecuteClientCall(request)) {
            String responseString;
            try {
                responseString = response.body().string();
            } catch (final IOException e) {
                throw new IntegrationException(e);
            }
            return getGson().fromJson(responseString, DryRunUploadResponse.class);
        }
    }
}
