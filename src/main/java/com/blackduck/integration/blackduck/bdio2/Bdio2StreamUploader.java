/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.bdio2;

import com.blackduck.integration.blackduck.api.core.BlackDuckPath;
import com.blackduck.integration.blackduck.api.core.BlackDuckResponse;
import com.blackduck.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.blackduck.integration.blackduck.bdio2.model.BdioFileContent;
import com.blackduck.integration.blackduck.http.BlackDuckRequestBuilder;
import com.blackduck.integration.blackduck.service.BlackDuckApiClient;
import com.blackduck.integration.blackduck.service.request.BlackDuckRequestBuilderEditor;
import com.blackduck.integration.blackduck.service.request.BlackDuckResponseRequest;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.log.IntLogger;
import com.blackduck.integration.rest.HttpUrl;
import com.blackduck.integration.rest.response.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;

import java.nio.charset.StandardCharsets;

public class Bdio2StreamUploader {
    // IDETECT-2756
    public static final String PROJECT_NAME_HEADER = "X-BD-PROJECT-NAME";
    public static final String VERSION_NAME_HEADER = "X-BD-VERSION-NAME";

    public static final String HEADER_CONTENT_TYPE = "Content-type";
    public static final String HEADER_X_BD_MODE = "X-BD-MODE";
    public static final String HEADER_X_BD_DOCUMENT_COUNT = "X-BD-DOCUMENT-COUNT";

    private final BlackDuckApiClient blackDuckApiClient;
    private final ApiDiscovery apiDiscovery;
    private final IntLogger logger;
    private final BlackDuckPath<BlackDuckResponse> scanPath;
    private final String contentType;

    public Bdio2StreamUploader(BlackDuckApiClient blackDuckApiClient, ApiDiscovery apiDiscovery, IntLogger logger, BlackDuckPath<BlackDuckResponse> scanPath, String contentType) {
        this.blackDuckApiClient = blackDuckApiClient;
        this.apiDiscovery = apiDiscovery;
        this.logger = logger;
        this.scanPath = scanPath;
        this.contentType = contentType;
    }

    public Response start(BdioFileContent header, BlackDuckRequestBuilderEditor editor) throws IntegrationException {
        HttpUrl url = apiDiscovery.metaSingleResponse(scanPath).getUrl();
        BlackDuckResponseRequest request = new BlackDuckRequestBuilder()
            .postString(header.getContent(), ContentType.create(contentType, StandardCharsets.UTF_8))
            .addHeader(HEADER_CONTENT_TYPE, contentType)
            .apply(editor)
            .buildBlackDuckResponseRequest(url);
        return blackDuckApiClient.executeAndRetrieveResponse(request);
    }

    public Response append(HttpUrl url, int count, BdioFileContent bdioFileContent, BlackDuckRequestBuilderEditor editor) throws IntegrationException {
        logger.debug(String.format("Appending file %s, to %s with count %d", bdioFileContent.getFileName(), url.toString(), count));
        BlackDuckResponseRequest request = new BlackDuckRequestBuilder()
            .putString(bdioFileContent.getContent(), ContentType.create(contentType, StandardCharsets.UTF_8))
            .addHeader(HEADER_CONTENT_TYPE, contentType)
            .addHeader(HEADER_X_BD_MODE, "append")
            .addHeader(HEADER_X_BD_DOCUMENT_COUNT, String.valueOf(count))
            .apply(editor)
            .buildBlackDuckResponseRequest(url);
        return blackDuckApiClient.execute(request);  // 202 accepted
    }

    public Response finish(HttpUrl url, int count, BlackDuckRequestBuilderEditor editor) throws IntegrationException {
        logger.debug(String.format("Finishing upload to %s with count %d", url.toString(), count));
        BlackDuckResponseRequest request = new BlackDuckRequestBuilder()
            .putString(StringUtils.EMPTY, ContentType.create(contentType, StandardCharsets.UTF_8))
            .addHeader(HEADER_CONTENT_TYPE, contentType)
            .addHeader(HEADER_X_BD_MODE, "finish")
            .addHeader(HEADER_X_BD_DOCUMENT_COUNT, String.valueOf(count))
            .apply(editor)
            .buildBlackDuckResponseRequest(url);
        return blackDuckApiClient.execute(request);
    }
}
