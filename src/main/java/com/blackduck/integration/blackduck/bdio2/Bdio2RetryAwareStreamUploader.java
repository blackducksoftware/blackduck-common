/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.bdio2;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackduck.integration.blackduck.bdio2.model.BdioFileContent;
import com.blackduck.integration.blackduck.exception.BlackDuckIntegrationException;
import com.blackduck.integration.blackduck.service.request.BlackDuckRequestBuilderEditor;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.exception.IntegrationRestException;
import com.synopsys.integration.rest.response.Response;

public class Bdio2RetryAwareStreamUploader {
    private static final List<Integer> NON_RETRYABLE_EXIT_CODES = Arrays.asList(401, 402, 403, 404, 409);
    private static final Integer TOO_MANY_REQUESTS_CODE = 429;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Bdio2StreamUploader bdio2StreamUploader;

    public Bdio2RetryAwareStreamUploader(Bdio2StreamUploader bdio2StreamUploader) {
        this.bdio2StreamUploader = bdio2StreamUploader;
    }

    public Response start(BdioFileContent header, BlackDuckRequestBuilderEditor editor, long clientStartTime, long clientTimeout)
        throws RetriableBdioUploadException, IntegrationException, InterruptedException {
        logger.trace("Executing BDIO upload start operation; non-retryable status codes: {}", NON_RETRYABLE_EXIT_CODES);
        try {
            Response response = bdio2StreamUploader.start(header, editor);
            
            // Retry if BlackDuck specifically told us how long to wait and we don't exceed the client timeout.
            if (!response.isStatusCodeSuccess() && isRetryableExitCode(response.getStatusCode())) {
                String retryAfterInSeconds = response.getHeaderValue("retry-after");
                
                if (null != retryAfterInSeconds && !retryAfterInSeconds.equals("0")) {
                    long retryAfterInMillis = Integer.parseInt(retryAfterInSeconds) * 1000;
                    
                    if (isClientTimeoutExceededBy(clientStartTime, retryAfterInMillis, clientTimeout)) {
                        throw new BlackDuckIntegrationException("Client timeout exceeded or will be exceeded due to server being busy.");
                    }
                    logger.debug("Received code {}. Waiting {} milliseconds to retry BDIO upload start operation.", response.getStatusCode(), retryAfterInMillis);
                    Thread.sleep(retryAfterInMillis);
                    return start(header, editor, clientStartTime, clientTimeout);
                } else if (response.getStatusCode() == TOO_MANY_REQUESTS_CODE){
                	throw new BlackDuckIntegrationException("The server is busy and did not specify retrying the request or provide a retry period.");
                }
            }
            
            return response;
        } catch (IntegrationRestException e) {
            return translateRetryableExceptions(e);
        }
    }

    public Response append(HttpUrl uploadUrl, int count, BdioFileContent content, BlackDuckRequestBuilderEditor editor)
        throws RetriableBdioUploadException, IntegrationException {
        logger.trace("Executing BDIO upload append operation");
        Response response = null;
        try {
            response = bdio2StreamUploader.append(uploadUrl, count, content, editor);
        } catch (IntegrationRestException e) {
            translateRetryableExceptions(e);
        }
        return response;
    }

    public Response finish(HttpUrl uploadUrl, int count, BlackDuckRequestBuilderEditor editor)
        throws RetriableBdioUploadException, IntegrationException {
        logger.trace("Executing BDIO upload finish operation");
        Response response = null;
        try {
            response = bdio2StreamUploader.finish(uploadUrl, count, editor);
        } catch (IntegrationRestException e) {
            translateRetryableExceptions(e);
        }
        return response;
    }

    public void onErrorThrowRetryableOrFailure(Response response) throws IntegrationException, RetriableBdioUploadException {
        if (!response.isStatusCodeSuccess()) {
            if (isRetryableExitCode(response.getStatusCode())) {
                logger.trace("Response status code {} is retryable", response.getStatusCode());
                throw new RetriableBdioUploadException();
            }
            logger.trace("Response status code {} is not retryable", response.getStatusCode());
            throw new IntegrationException(String.format("Bdio upload failed with non-retryable exit code: %d", response.getStatusCode()));
        }
        logger.trace("Response status code {} treated as success", response.getStatusCode());
    }
    
    private boolean isClientTimeoutExceededBy(long startTime, long waitInMillis, long clientTimeout) {
        long currentTime = System.currentTimeMillis();
        return (currentTime - startTime + waitInMillis) > (clientTimeout * 1000);
    }

    private Response translateRetryableExceptions(final IntegrationRestException e) throws RetriableBdioUploadException, IntegrationRestException {
        if (isRetryableExitCode(e.getHttpStatusCode())) {
            logger.trace("Response status code {} in caught exception is retryable", e.getHttpStatusCode());
            throw new RetriableBdioUploadException();
        }
        throw e;
    }

    private boolean isRetryableExitCode(int exitCode) {
        if (NON_RETRYABLE_EXIT_CODES.contains(exitCode)) {
            return false;
        }
        return true;
    }
}
