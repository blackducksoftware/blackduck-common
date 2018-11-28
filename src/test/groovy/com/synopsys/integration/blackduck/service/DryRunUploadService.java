package com.synopsys.integration.blackduck.service;

import java.io.File;

import com.synopsys.integration.blackduck.api.core.BlackDuckPath;
import com.synopsys.integration.blackduck.service.model.RequestFactory;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.request.Response;

public class DryRunUploadService extends DataService {
    public DryRunUploadService(final BlackDuckService blackDuckService, final IntLogger logger) {
        super(blackDuckService, logger);
    }

    public DryRunUploadResponse uploadDryRunFile(final File dryRunFile) throws Exception {
        final String uri = blackDuckService.getUri(new BlackDuckPath("/api/v1/scans"));
        final Request request = RequestFactory.createCommonPostRequestBuilder(dryRunFile).uri(uri).build();
        try (Response response = blackDuckService.execute(request)) {
            final String responseString = response.getContentString();
            final DryRunUploadResponse uploadResponse = blackDuckService.getGson().fromJson(responseString, DryRunUploadResponse.class);
            uploadResponse.setJson(responseString);
            return uploadResponse;
        }
    }

}
