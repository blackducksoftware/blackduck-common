package com.synopsys.integration.blackduck.bdio2;

import java.util.Collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.blackduck.bdio2.model.BdioFileContent;
import com.synopsys.integration.blackduck.exception.BlackDuckApiException;
import com.synopsys.integration.blackduck.service.request.BlackDuckRequestBuilderEditor;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.exception.IntegrationTimeoutException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.rest.HttpMethod;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.exception.IntegrationRestException;
import com.synopsys.integration.rest.response.DefaultResponse;
import com.synopsys.integration.rest.response.Response;
import com.synopsys.integration.wait.ResilientJobConfig;
import com.synopsys.integration.wait.ResilientJobExecutor;
import com.synopsys.integration.wait.tracker.WaitIntervalTracker;
import com.synopsys.integration.wait.tracker.WaitIntervalTrackerFactory;

public class Bdio2UploadJobTest {
    private final IntLogger logger = new Slf4jIntLogger(LoggerFactory.getLogger(this.getClass()));
    private final int timeout = 10;
    private final int waitInterval = 2;

    @Test
    public void testRetryOnFailedHeaderUpload() throws IntegrationException, RetriableBdioUploadException {
        Bdio2RetryAwareStreamUploader bdio2StreamUploader = getUploaderThatThrowsRetriableOnStart();
        Bdio2UploadJob bdio2UploadJob = getUploadJob(bdio2StreamUploader);
        ResilientJobExecutor jobExecutor = getJobExecutor();
        Assertions.assertThrows(IntegrationTimeoutException.class, () -> jobExecutor.executeJob(bdio2UploadJob));
    }

    @Test
    public void testRetryOnFailedChunkUpload() throws IntegrationException, RetriableBdioUploadException {
        Bdio2RetryAwareStreamUploader bdio2StreamUploader = getUploaderThatThrowsRetriableOnAppend();
        Bdio2UploadJob bdio2UploadJob = getUploadJob(bdio2StreamUploader);
        ResilientJobExecutor jobExecutor = getJobExecutor();
        Assertions.assertThrows(IntegrationTimeoutException.class, () -> jobExecutor.executeJob(bdio2UploadJob));
    }

    @Test
    public void testRetryOnFailedFinish() throws IntegrationException, RetriableBdioUploadException {
        Bdio2RetryAwareStreamUploader bdio2StreamUploader = getUploaderThatThrowsRetriableOnFinish();
        Bdio2UploadJob bdio2UploadJob = getUploadJob(bdio2StreamUploader);
        ResilientJobExecutor jobExecutor = getJobExecutor();
        Assertions.assertThrows(IntegrationTimeoutException.class, () -> jobExecutor.executeJob(bdio2UploadJob));
    }

    private Bdio2RetryAwareStreamUploader getUploaderThatThrowsRetriableOnStart() throws IntegrationException, RetriableBdioUploadException {
        Bdio2RetryAwareStreamUploader bdio2StreamUploader = Mockito.mock(Bdio2RetryAwareStreamUploader.class);
        Mockito.when(bdio2StreamUploader.start(Mockito.any(), Mockito.any())).thenThrow(new RetriableBdioUploadException());
        return bdio2StreamUploader;
    }

    private Bdio2RetryAwareStreamUploader getUploaderThatThrowsRetriableOnAppend() throws IntegrationException, RetriableBdioUploadException {
        Bdio2RetryAwareStreamUploader bdio2StreamUploader = Mockito.mock(Bdio2RetryAwareStreamUploader.class);
        Response successResponse = Mockito.mock(Response.class);
        Mockito.when(successResponse.isStatusCodeSuccess()).thenReturn(true);
        Mockito.when(successResponse.getHeaderValue("location")).thenReturn("https://server.blackduck.com/api/endpoint/scanId");
        Mockito.when(bdio2StreamUploader.start(Mockito.any(), Mockito.any())).thenReturn(successResponse);
        Mockito.when(bdio2StreamUploader.append(Mockito.any(HttpUrl.class), Mockito.anyInt(), Mockito.any(BdioFileContent.class), Mockito.any(BlackDuckRequestBuilderEditor.class))).thenThrow(new RetriableBdioUploadException());
        return bdio2StreamUploader;
    }

    private Bdio2RetryAwareStreamUploader getUploaderThatThrowsRetriableOnFinish() throws IntegrationException, RetriableBdioUploadException {
        Bdio2RetryAwareStreamUploader bdio2StreamUploader = Mockito.mock(Bdio2RetryAwareStreamUploader.class);
        Response successResponse = Mockito.mock(Response.class);
        Mockito.when(successResponse.isStatusCodeSuccess()).thenReturn(true);
        Mockito.when(successResponse.getHeaderValue("location")).thenReturn("https://server.blackduck.com/api/endpoint/scanId");
        Mockito.when(bdio2StreamUploader.start(Mockito.any(), Mockito.any())).thenReturn(successResponse);
        Mockito.when(bdio2StreamUploader.append(Mockito.any(HttpUrl.class), Mockito.anyInt(), Mockito.any(BdioFileContent.class), Mockito.any(BlackDuckRequestBuilderEditor.class))).thenReturn(successResponse);
        Mockito.when(bdio2StreamUploader.finish(Mockito.any(), Mockito.anyInt(), Mockito.any())).thenThrow(new RetriableBdioUploadException());
        return bdio2StreamUploader;
    }


    private ResilientJobExecutor getJobExecutor() {
        WaitIntervalTracker waitIntervalTracker = WaitIntervalTrackerFactory.createConstant(timeout, waitInterval);
        ResilientJobConfig jobConfig = new ResilientJobConfig(logger, System.currentTimeMillis(), waitIntervalTracker);
        return new ResilientJobExecutor(jobConfig);
    }

    private Bdio2UploadJob getUploadJob(Bdio2RetryAwareStreamUploader bdio2StreamUploader) {
        BdioFileContent header = new BdioFileContent("bdio-header.jsonld", "");
        BdioFileContent entry = new BdioFileContent("bdio-entry-00.jsonld", "");
        BlackDuckRequestBuilderEditor editor = Mockito.mock(BlackDuckRequestBuilderEditor.class);
        return new Bdio2UploadJob(bdio2StreamUploader, header, Collections.singletonList(entry), editor, 2, true, true);
    }
}
