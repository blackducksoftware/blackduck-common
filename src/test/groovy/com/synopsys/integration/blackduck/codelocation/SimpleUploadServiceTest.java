package com.synopsys.integration.blackduck.codelocation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.blackduck.codelocation.bdioupload.SimpleUploadService;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadBatch;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadBatchOutput;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadOutput;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadRunner;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadTarget;
import com.synopsys.integration.blackduck.exception.HubIntegrationException;

public class SimpleUploadServiceTest {

    @Test
    public void testUpload() throws HubIntegrationException {
        UploadRunner uploadRunner = Mockito.mock(UploadRunner.class);

        SimpleUploadService simpleUploadService = new SimpleUploadService(uploadRunner);

        File toUpload = Mockito.mock(File.class);

        List<UploadOutput> results = new ArrayList<>();
        results.add(UploadOutput.SUCCESS("example", "content" ));

        Mockito.when(uploadRunner.executeUploads(Mockito.any())).thenReturn(new UploadBatchOutput(results));

        UploadBatchOutput result = simpleUploadService.uploadFile("example", toUpload);

        Assert.assertEquals(1, result.getOutputs().size());

        Assert.assertEquals("example", result.getOutputs().get(0).getCodeLocationName());
        Assert.assertEquals(Result.SUCCESS, result.getOutputs().get(0).getResult());

    }
}
