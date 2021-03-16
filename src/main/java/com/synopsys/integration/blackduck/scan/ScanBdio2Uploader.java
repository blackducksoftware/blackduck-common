/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.scan;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.blackduck.http.BlackDuckRequestFactory;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.request.Request;

public class ScanBdio2Uploader {
    private static final String HEADER_CONTENT_TYPE = "Content-type";
    private static final String HEADER_X_BD_MODE = "X-BD-MODE";
    private static final String HEADER_X_BD_DOCUMENT_COUNT = "X-BD-DOCUMENT-COUNT";

    private final BlackDuckApiClient blackDuckApiClient;
    private final BlackDuckRequestFactory blackDuckRequestFactory;
    private final String contentType;

    public ScanBdio2Uploader(final BlackDuckApiClient blackDuckApiClient, final BlackDuckRequestFactory blackDuckRequestFactory, String contentType) {
        this.blackDuckApiClient = blackDuckApiClient;
        this.blackDuckRequestFactory = blackDuckRequestFactory;
        this.contentType = contentType;
    }

    public HttpUrl start(ScanBdioContent header) throws IntegrationException {
        HttpUrl url = blackDuckApiClient.getUrl(BlackDuckApiClient.SCAN_DEVELOPER_MODE_PATH);
        Request request = blackDuckRequestFactory
                              .createCommonPostRequestBuilder(url, header.getContent())
                              .addHeader(HEADER_CONTENT_TYPE, contentType)
                              .build();

        return blackDuckApiClient.executePostRequestAndRetrieveURL(request);
    }

    public void append(HttpUrl url, int count, ScanBdioContent scanBdioContent) throws IntegrationException {
        Request request = blackDuckRequestFactory
                              .createCommonPutRequestBuilder(url, scanBdioContent.getContent())
                              .addHeader(HEADER_CONTENT_TYPE, contentType)
                              .addHeader(HEADER_X_BD_MODE, "append")
                              .addHeader(HEADER_X_BD_DOCUMENT_COUNT, String.valueOf(count))
                              .build();
        blackDuckApiClient.execute(request);  // 202 accepted
    }

    public void finish(HttpUrl url, int count) throws IntegrationException {
        Request request = blackDuckRequestFactory
                              .createCommonPutRequestBuilder(url, StringUtils.EMPTY)
                              .addHeader(HEADER_CONTENT_TYPE, contentType)
                              .addHeader(HEADER_X_BD_MODE, "finish")
                              .addHeader(HEADER_X_BD_DOCUMENT_COUNT, String.valueOf(count))
                              .build();
        blackDuckApiClient.execute(request);
    }
}
