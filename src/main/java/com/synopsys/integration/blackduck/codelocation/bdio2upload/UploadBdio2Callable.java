package com.synopsys.integration.blackduck.codelocation.bdio2upload;

import java.io.IOException;
import java.util.concurrent.Callable;

import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadOutput;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadTarget;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.model.RequestFactory;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.request.Response;

public class UploadBdio2Callable implements Callable<UploadOutput> {
    private final BlackDuckService blackDuckService;
    private final UploadTarget uploadTarget;

    public UploadBdio2Callable(BlackDuckService blackDuckService, UploadTarget uploadTarget) {
        this.blackDuckService = blackDuckService;
        this.uploadTarget = uploadTarget;
    }

    @Override
    public UploadOutput call() {
        try {
            String uri = blackDuckService.getUri(BlackDuckService.SCAN_DATA_PATH);
            Request request = RequestFactory.createCommonPostRequestBuilder(uploadTarget.getUploadFile()).uri(uri).mimeType(uploadTarget.getMediaType()).build();
            try (Response response = blackDuckService.execute(request)) {
                String responseString = response.getContentString();
                return UploadOutput.SUCCESS(uploadTarget.getCodeLocationName(), responseString);
            } catch (IOException e) {
                return UploadOutput.FAILURE(uploadTarget.getCodeLocationName(), e.getMessage(), e);
            }
        } catch (Exception e) {
            return UploadOutput.FAILURE(uploadTarget.getCodeLocationName(), "Failed to upload file: " + uploadTarget.getUploadFile().getAbsolutePath() + " because " + e.getMessage(), e);
        }
    }

}
