package com.synopsys.integration.blackduck.bdio2;

import java.util.Collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.blackduck.bdio2.model.BdioFileContent;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.exception.IntegrationTimeoutException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.rest.response.DefaultResponse;
import com.synopsys.integration.rest.response.Response;
import com.synopsys.integration.wait.ResilientJobConfig;
import com.synopsys.integration.wait.ResilientJobExecutor;

public class Bdio2UploadJobTest {
    private final IntLogger logger = new Slf4jIntLogger(LoggerFactory.getLogger(this.getClass()));
    private final int timeout = 10;
    private final int waitInterval = 2;

    @Test
    public void testRetryOnFailedHeaderUpload() throws IntegrationException, InterruptedException {
        Bdio2StreamUploader bdio2StreamUploader = getUploaderThatGets429OnStart();
        Bdio2UploadJob bdio2UploadJob = getUploadJob(bdio2StreamUploader);
        ResilientJobExecutor jobExecutor = getJobExecutor();
        Assertions.assertThrows(IntegrationTimeoutException.class, () -> jobExecutor.executeJob(bdio2UploadJob));
    }

    private ResilientJobExecutor getJobExecutor() {
        ResilientJobConfig jobConfig = new ResilientJobConfig(logger, timeout, System.currentTimeMillis(), waitInterval);
        return new ResilientJobExecutor(jobConfig);
    }

    private Bdio2UploadJob getUploadJob(Bdio2StreamUploader bdio2StreamUploader) {
        BdioFileContent header = new BdioFileContent("bdio-header.jsonld", "");
        BdioFileContent entry = new BdioFileContent("bdio-entry-00.jsonld", "");
        return new Bdio2UploadJob(bdio2StreamUploader, header, Collections.singletonList(entry), null, 2, true, true);
    }

    private Bdio2StreamUploader getUploaderThatGets429OnStart() throws IntegrationException {
        Bdio2StreamUploader bdio2StreamUploader = Mockito.mock(Bdio2StreamUploader.class);
        Response response = Mockito.mock(DefaultResponse.class);
        Mockito.when(response.getStatusCode()).thenReturn(429);
        Mockito.when(bdio2StreamUploader.start(Mockito.any(), Mockito.any())).thenReturn(response);

        return bdio2StreamUploader;
    }

    @Test
    public void testRetryOnFailedChunkUpload() throws IntegrationException, InterruptedException {
        Bdio2StreamUploader bdio2StreamUploader = getUploaderThatGets429OnAppend();
        Bdio2UploadJob bdio2UploadJob = getUploadJob(bdio2StreamUploader);
        ResilientJobExecutor jobExecutor = getJobExecutor();
        Assertions.assertThrows(IntegrationTimeoutException.class, () -> jobExecutor.executeJob(bdio2UploadJob));
    }

    private Bdio2StreamUploader getUploaderThatGets429OnAppend() throws IntegrationException {
        Bdio2StreamUploader bdio2StreamUploader = Mockito.mock(Bdio2StreamUploader.class);

        Response failedResponse = Mockito.mock(DefaultResponse.class);
        Mockito.when(failedResponse.getStatusCode()).thenReturn(429);
        Response successfulResponse = Mockito.mock(DefaultResponse.class);
        Mockito.when(successfulResponse.getStatusCode()).thenReturn(200);

        Mockito.when(bdio2StreamUploader.start(Mockito.any(), Mockito.any())).thenReturn(successfulResponse);
        Mockito.when(bdio2StreamUploader.append(Mockito.any(), Mockito.anyInt(), Mockito.any(), Mockito.any())).thenReturn(failedResponse);

        return bdio2StreamUploader;
    }
}
