package com.synopsys.integration.blackduck.bdio2;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.blackduck.bdio2.model.BdioFileContent;
import com.synopsys.integration.blackduck.service.request.BlackDuckRequestBuilderEditor;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.exception.IntegrationRestException;
import com.synopsys.integration.rest.response.Response;

public class Bdio2RetryAwareStreamUploaderTest {

    @Test
    void test() throws IntegrationException, RetriableBdioUploadException {
        Bdio2StreamUploader bdio2StreamUploader = Mockito.mock(Bdio2StreamUploader.class);
        Bdio2RetryAwareStreamUploader bdio2RetryAwareStreamUploader = new Bdio2RetryAwareStreamUploader(bdio2StreamUploader);

        BdioFileContent header = Mockito.mock(BdioFileContent.class);
        BlackDuckRequestBuilderEditor editor = Mockito.mock(BlackDuckRequestBuilderEditor.class);

        // bdio2StreamUploader.start(header, editor);
        Response response512 = Mockito.mock(Response.class);
        IntegrationRestException exception512 = Mockito.mock(IntegrationRestException.class);
        Mockito.when(bdio2StreamUploader.start(header, editor)).thenThrow(exception512);
        // e.getHttpStatusCode()
        Mockito.when(exception512.getHttpStatusCode()).thenReturn(512);
        bdio2RetryAwareStreamUploader.start(header, editor);
    }
}
