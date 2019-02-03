package com.synopsys.integration.blackduck.codelocation;

import java.io.File;

import com.synopsys.integration.blackduck.api.core.BlackDuckPath;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.DataService;
import com.synopsys.integration.blackduck.service.model.RequestFactory;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.request.Response;

public class DryRunUploadService extends DataService {
    public DryRunUploadService(BlackDuckService blackDuckService, IntLogger logger) {
        super(blackDuckService, logger);
    }

    public DryRunUploadResponse uploadDryRunFile(File dryRunFile) throws Exception {
        String uri = blackDuckService.getUri(new BlackDuckPath("/api/v1/scans"));
        Request request = RequestFactory.createCommonPostRequestBuilder(dryRunFile).uri(uri).build();
        try (Response response = blackDuckService.execute(request)) {
            String responseString = response.getContentString();
            DryRunUploadResponse uploadResponse = blackDuckService.getGson().fromJson(responseString, DryRunUploadResponse.class);
            uploadResponse.setJson(responseString);
            return uploadResponse;
        }
    }

}
