package com.synopsys.integration.blackduck.bdio2;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.blackduck.bdio2.model.BdioFileContent;
import com.synopsys.integration.blackduck.service.request.BlackDuckRequestBuilderEditor;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.exception.IntegrationRestException;
import com.synopsys.integration.rest.response.Response;

class Bdio2RetryAwareStreamUploaderTest {

    @Test
    void testStartRetriable() throws IntegrationException, InterruptedException {
        BlackDuckRequestBuilderEditor editor = Mockito.mock(BlackDuckRequestBuilderEditor.class);
        BdioFileContent bdioFileContent = Mockito.mock(BdioFileContent.class);
        Bdio2RetryAwareStreamUploader bdio2RetryAwareStreamUploader = mockBdio2RetryAwareStreamUploaderThrows512OnStart(editor, bdioFileContent);
        try {
            bdio2RetryAwareStreamUploader.start(bdioFileContent, editor, System.currentTimeMillis(), System.currentTimeMillis() + 10000);
            Assertions.fail("Expected RetriableBdioUploadException");
        } catch (RetriableBdioUploadException e) {
            // expected
        }
    }
    
    @Test
    void testStartRetryableWithRetryAfterHeader() throws IntegrationException, RetriableBdioUploadException, InterruptedException {
        BlackDuckRequestBuilderEditor editor = Mockito.mock(BlackDuckRequestBuilderEditor.class);
        BdioFileContent bdioFileContent = Mockito.mock(BdioFileContent.class);
        
        Bdio2StreamUploader bdio2StreamUploader = Mockito.mock(Bdio2StreamUploader.class);
        Response response = Mockito.mock(Response.class);
        
        Mockito.when(bdio2StreamUploader.start(bdioFileContent, editor)).thenReturn(response);
        Mockito.when(response.isStatusCodeSuccess()).thenReturn(false);
        
        Mockito.when(response.getHeaderValue("retry-after"))
            .thenReturn("1")
            .thenReturn(null);
        
        Mockito.when(response.getStatusCode())
            .thenReturn(429)
            .thenReturn(200);
        
        Bdio2RetryAwareStreamUploader bdio2RetryAwareStreamUploader = new Bdio2RetryAwareStreamUploader(bdio2StreamUploader);
        
        bdio2RetryAwareStreamUploader.start(bdioFileContent, editor, System.currentTimeMillis(), System.currentTimeMillis() + 10000);
        
        // We should make two calls as the first is a 429 which we retry for and the second is a 200.
        Mockito.verify(bdio2StreamUploader, Mockito.times(2)).start(bdioFileContent, editor);
    }

    @Test
    void testAppendRetriable() throws IntegrationException {
        HttpUrl httpUrl = Mockito.mock(HttpUrl.class);
        BdioFileContent bdioFileContent = Mockito.mock(BdioFileContent.class);
        BlackDuckRequestBuilderEditor editor = Mockito.mock(BlackDuckRequestBuilderEditor.class);
        Bdio2RetryAwareStreamUploader bdio2RetryAwareStreamUploader = mockBdio2RetryAwareStreamUploaderThrows512OnAppend(httpUrl, bdioFileContent, editor);
        try {
            bdio2RetryAwareStreamUploader.append(httpUrl, 1, bdioFileContent, editor);
            Assertions.fail("Expected RetriableBdioUploadException");
        } catch (RetriableBdioUploadException e) {
            // expected
        }
    }

    @Test
    void testFinishRetriable() throws IntegrationException {
        HttpUrl httpUrl = Mockito.mock(HttpUrl.class);
        //BdioFileContent bdioFileContent = Mockito.mock(BdioFileContent.class);
        BlackDuckRequestBuilderEditor editor = Mockito.mock(BlackDuckRequestBuilderEditor.class);
        Bdio2RetryAwareStreamUploader bdio2RetryAwareStreamUploader = mockBdio2RetryAwareStreamUploaderThrows512OnFinish(httpUrl, editor);
        try {
            bdio2RetryAwareStreamUploader.finish(httpUrl, 1, editor);
            Assertions.fail("Expected RetriableBdioUploadException");
        } catch (RetriableBdioUploadException e) {
            // expected
        }
    }

    @Test
    void testStartNonRetriable() throws IntegrationException, RetriableBdioUploadException, InterruptedException {
        BdioFileContent bdioFileContent = Mockito.mock(BdioFileContent.class);
        BlackDuckRequestBuilderEditor editor = Mockito.mock(BlackDuckRequestBuilderEditor.class);
        Bdio2RetryAwareStreamUploader bdio2RetryAwareStreamUploader = mockBdio2RetryAwareStreamUploaderThrow404OnStart(bdioFileContent, editor);
        try {
            bdio2RetryAwareStreamUploader.start(bdioFileContent, editor, System.currentTimeMillis(), System.currentTimeMillis() + 10000);
            Assertions.fail("Expected RetriableBdioUploadException");
        } catch (IntegrationException e) {
            // expected
        }
    }

    @Test
    void testNoExceptionStatusCodeAnalysisRetryable() throws IntegrationException, InterruptedException {
        Bdio2RetryAwareStreamUploader bdio2RetryAwareStreamUploader = mockBdio2RetryAwareStreamUploaderMinimal();
        Response response400 = Mockito.mock(Response.class);
        Mockito.when(response400.isStatusCodeSuccess()).thenReturn(false);
        Mockito.when(response400.getStatusCode()).thenReturn(400);
        try {
            bdio2RetryAwareStreamUploader.onErrorThrowRetryableOrFailure(response400);
            Assertions.fail("Expected RetriableBdioUploadException");
        } catch (RetriableBdioUploadException e) {
            // expected
        }
    }

    @Test
    void testNoExceptionStatusCodeAnalysisSuccess() throws IntegrationException, RetriableBdioUploadException, InterruptedException {
        Bdio2RetryAwareStreamUploader bdio2RetryAwareStreamUploader = mockBdio2RetryAwareStreamUploaderMinimal();
        Response response200 = Mockito.mock(Response.class);
        Mockito.when(response200.isStatusCodeSuccess()).thenReturn(true);
        Mockito.when(response200.getStatusCode()).thenReturn(200);
        bdio2RetryAwareStreamUploader.onErrorThrowRetryableOrFailure(response200);
    }

    @Test
    void testNoExceptionStatusCodeAnalysisNonRetryable() throws IntegrationException, RetriableBdioUploadException, InterruptedException {
        Bdio2RetryAwareStreamUploader bdio2RetryAwareStreamUploader = mockBdio2RetryAwareStreamUploaderMinimal();
        Response response404 = Mockito.mock(Response.class);
        Mockito.when(response404.isStatusCodeSuccess()).thenReturn(false);
        Mockito.when(response404.getStatusCode()).thenReturn(404);
        try {
            bdio2RetryAwareStreamUploader.onErrorThrowRetryableOrFailure(response404);
            Assertions.fail("Expected IntegrationException");
        } catch (IntegrationException e) {
            // expected
        }
    }

    @NotNull
    private Bdio2RetryAwareStreamUploader mockBdio2RetryAwareStreamUploaderThrow404OnStart(BdioFileContent bdioFileContent, BlackDuckRequestBuilderEditor editor)
        throws IntegrationException {
        Bdio2StreamUploader bdio2StreamUploader = Mockito.mock(Bdio2StreamUploader.class);
        Bdio2RetryAwareStreamUploader bdio2RetryAwareStreamUploader = new Bdio2RetryAwareStreamUploader(bdio2StreamUploader);
        IntegrationRestException exception404 = Mockito.mock(IntegrationRestException.class);
        Mockito.when(bdio2StreamUploader.start(bdioFileContent, editor)).thenThrow(exception404);
        Mockito.when(exception404.getHttpStatusCode()).thenReturn(404);
        return bdio2RetryAwareStreamUploader;
    }

    @NotNull
    private Bdio2RetryAwareStreamUploader mockBdio2RetryAwareStreamUploaderMinimal() {
        Bdio2StreamUploader bdio2StreamUploader = Mockito.mock(Bdio2StreamUploader.class);
        Bdio2RetryAwareStreamUploader bdio2RetryAwareStreamUploader = new Bdio2RetryAwareStreamUploader(bdio2StreamUploader);
        return bdio2RetryAwareStreamUploader;
    }

    @NotNull
    private Bdio2RetryAwareStreamUploader mockBdio2RetryAwareStreamUploaderThrows512OnStart(BlackDuckRequestBuilderEditor editor, BdioFileContent bdioFileContent)
        throws IntegrationException {
        Bdio2StreamUploader bdio2StreamUploader = Mockito.mock(Bdio2StreamUploader.class);
        IntegrationRestException exception512 = Mockito.mock(IntegrationRestException.class);
        Mockito.when(exception512.getHttpStatusCode()).thenReturn(512);
        Mockito.when(bdio2StreamUploader.start(bdioFileContent, editor)).thenThrow(exception512);
        return new Bdio2RetryAwareStreamUploader(bdio2StreamUploader);
    }

    @NotNull
    private Bdio2RetryAwareStreamUploader mockBdio2RetryAwareStreamUploaderThrows512OnAppend(HttpUrl httpUrl, BdioFileContent bdioFileContent, BlackDuckRequestBuilderEditor editor)
        throws IntegrationException {
        Bdio2StreamUploader bdio2StreamUploader = Mockito.mock(Bdio2StreamUploader.class);
        IntegrationRestException exception512 = Mockito.mock(IntegrationRestException.class);
        Mockito.when(bdio2StreamUploader.append(httpUrl, 1, bdioFileContent, editor)).thenThrow(exception512);
        Mockito.when(exception512.getHttpStatusCode()).thenReturn(512);
        return new Bdio2RetryAwareStreamUploader(bdio2StreamUploader);
    }

    @NotNull
    private Bdio2RetryAwareStreamUploader mockBdio2RetryAwareStreamUploaderThrows512OnFinish(HttpUrl httpUrl, BlackDuckRequestBuilderEditor editor)
        throws IntegrationException {
        Bdio2StreamUploader bdio2StreamUploader = Mockito.mock(Bdio2StreamUploader.class);
        IntegrationRestException exception512 = Mockito.mock(IntegrationRestException.class);
        Mockito.when(bdio2StreamUploader.finish(httpUrl, 1, editor)).thenThrow(exception512);
        Mockito.when(exception512.getHttpStatusCode()).thenReturn(512);
        return new Bdio2RetryAwareStreamUploader(bdio2StreamUploader);
    }
}
