/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.bdio2;

import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;

import com.synopsys.integration.blackduck.api.core.BlackDuckPath;
import com.synopsys.integration.blackduck.api.core.BlackDuckResponse;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.bdio2.model.BdioFileContent;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.request.BlackDuckResponseRequest;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpUrl;

public class Bdio2StreamUploader {
    private static final String HEADER_CONTENT_TYPE = "Content-type";
    private static final String HEADER_X_BD_MODE = "X-BD-MODE";
    private static final String HEADER_X_BD_DOCUMENT_COUNT = "X-BD-DOCUMENT-COUNT";

    private final BlackDuckApiClient blackDuckApiClient;
    private final ApiDiscovery apiDiscovery;
    private final IntLogger logger;
    private final BlackDuckPath scanPath;
    private final String contentType;

    public Bdio2StreamUploader(BlackDuckApiClient blackDuckApiClient, ApiDiscovery apiDiscovery, IntLogger logger, BlackDuckPath<BlackDuckResponse> scanPath,
        String contentType) {
        this.blackDuckApiClient = blackDuckApiClient;
        this.apiDiscovery = apiDiscovery;
        this.logger = logger;
        this.scanPath = scanPath;
        this.contentType = contentType;
    }

    public HttpUrl start(BdioFileContent header) throws IntegrationException {
        HttpUrl url = apiDiscovery.metaSingleResponse(scanPath).getUrl();
        BlackDuckResponseRequest request = new BlackDuckRequestBuilder()
                                               .postString(header.getContent(), ContentType.create(contentType, StandardCharsets.UTF_8))
                                               .addHeader(HEADER_CONTENT_TYPE, contentType)
                                               .buildBlackDuckResponseRequest(url);
        HttpUrl responseUrl = blackDuckApiClient.executePostRequestAndRetrieveURL(request);
        logger.debug(String.format("Starting upload to %s", responseUrl.toString()));
        return responseUrl;
    }

    public void append(HttpUrl url, int count, BdioFileContent bdioFileContent) throws IntegrationException {
        logger.debug(String.format("Appending file %s, to %s with count %d", bdioFileContent.getFileName(), url.toString(), count));
        BlackDuckResponseRequest request = new BlackDuckRequestBuilder()
                                               .putString(bdioFileContent.getContent(), ContentType.create(contentType, StandardCharsets.UTF_8))
                                               .addHeader(HEADER_CONTENT_TYPE, contentType)
                                               .addHeader(HEADER_X_BD_MODE, "append")
                                               .addHeader(HEADER_X_BD_DOCUMENT_COUNT, String.valueOf(count))
                                               .buildBlackDuckResponseRequest(url);
        blackDuckApiClient.execute(request);  // 202 accepted
    }

    public void finish(HttpUrl url, int count) throws IntegrationException {
        logger.debug(String.format("Finishing upload to %s with count %d", url.toString(), count));
        BlackDuckResponseRequest request = new BlackDuckRequestBuilder()
                                               .putString(StringUtils.EMPTY, ContentType.create(contentType, StandardCharsets.UTF_8))
                                               .addHeader(HEADER_CONTENT_TYPE, contentType)
                                               .addHeader(HEADER_X_BD_MODE, "finish")
                                               .addHeader(HEADER_X_BD_DOCUMENT_COUNT, String.valueOf(count))
                                               .buildBlackDuckResponseRequest(url);
        blackDuckApiClient.execute(request);
    }

}
