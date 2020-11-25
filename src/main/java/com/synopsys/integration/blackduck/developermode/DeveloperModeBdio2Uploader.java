package com.synopsys.integration.blackduck.developermode;

import java.util.UUID;

import com.synopsys.integration.blackduck.http.BlackDuckRequestFactory;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.request.Request;

public class DeveloperModeBdio2Uploader {
    private static final String CONTENT_TYPE = "application/vnd.blackducksoftware.developer-scan-ld-1+json";
    private static final String HEADER_X_BD_MODE = "X-BD-MODE";
    private static final String HEADER_X_BD_PASSTHRU = "X-BD-PASSTHRU";
    private static final String HEADER_X_BD_SCAN_ID = "X-BD-SCAN-ID";
    private static final String HEADER_X_BD_DOCUMENT_COUNT = "X-BD-DOCUMENT-COUNT";
    private static final String HEADER_X_BD_SCAN_TYPE = "X-BD-SCAN-TYPE";
    private BlackDuckApiClient blackDuckApiClient;
    private BlackDuckRequestFactory blackDuckRequestFactory;

    public DeveloperModeBdio2Uploader(final BlackDuckApiClient blackDuckApiClient, final BlackDuckRequestFactory blackDuckRequestFactory) {
        this.blackDuckApiClient = blackDuckApiClient;
        this.blackDuckRequestFactory = blackDuckRequestFactory;
    }

    public void startUpload(UUID scanId, int count, String scanType, DeveloperModeBdioContent header) throws IntegrationException {
        HttpUrl url = blackDuckApiClient.getUrl(BlackDuckApiClient.SCAN_DATA_PATH);
        Request request = blackDuckRequestFactory
                              .createCommonPostRequestBuilder(url, header.getContent())
                              .acceptMimeType(CONTENT_TYPE)
                              .addHeader("Content-type", CONTENT_TYPE)
                              .addHeader(HEADER_X_BD_MODE, "start")
                              .addHeader(HEADER_X_BD_PASSTHRU, "ignoredButRequired")
                              .addHeader(HEADER_X_BD_SCAN_ID, scanId.toString())
                              .addHeader(HEADER_X_BD_DOCUMENT_COUNT, String.valueOf(count))
                              .addHeader(HEADER_X_BD_SCAN_TYPE, scanType)
                              .build();

        blackDuckApiClient.execute(request);
    }

    public void uploadChunk(UUID scanId, String scanType, DeveloperModeBdioContent developerModeBdioContent) throws IntegrationException {
        HttpUrl url = blackDuckApiClient.getUrl(BlackDuckApiClient.SCAN_DATA_PATH);
        Request request = blackDuckRequestFactory
                              .createCommonPostRequestBuilder(url, developerModeBdioContent.getContent())
                              .acceptMimeType(CONTENT_TYPE)
                              .addHeader("Content-type", CONTENT_TYPE)
                              .addHeader(HEADER_X_BD_MODE, "append")
                              .addHeader(HEADER_X_BD_PASSTHRU, "ignoredButRequired")
                              .addHeader(HEADER_X_BD_SCAN_ID, scanId.toString())
                              .addHeader(HEADER_X_BD_SCAN_TYPE, scanType)
                              .build();
        blackDuckApiClient.execute(request);
    }
}
