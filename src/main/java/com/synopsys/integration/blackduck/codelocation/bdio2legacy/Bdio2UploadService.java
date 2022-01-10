/*
 * blackduck-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.codelocation.bdio2legacy;

import java.util.Set;

import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.codelocation.CodeLocationCreationData;
import com.synopsys.integration.blackduck.codelocation.CodeLocationCreationRequest;
import com.synopsys.integration.blackduck.codelocation.CodeLocationCreationService;
import com.synopsys.integration.blackduck.codelocation.upload.UploadBatch;
import com.synopsys.integration.blackduck.codelocation.upload.UploadBatchOutput;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.DataService;
import com.synopsys.integration.blackduck.service.model.NotificationTaskRange;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.util.NameVersion;

public class Bdio2UploadService extends DataService {
    private final UploadBdio2BatchRunner uploadBdio2BatchRunner;
    private final CodeLocationCreationService codeLocationCreationService;

    public Bdio2UploadService(BlackDuckApiClient blackDuckApiClient, ApiDiscovery apiDiscovery, IntLogger logger, UploadBdio2BatchRunner uploadBdio2BatchRunner,
        CodeLocationCreationService codeLocationCreationService) {
        super(blackDuckApiClient, apiDiscovery, logger);
        this.uploadBdio2BatchRunner = uploadBdio2BatchRunner;
        this.codeLocationCreationService = codeLocationCreationService;
    }

    public Bdio2UploadCodeLocationCreationRequest createUploadRequest(UploadBatch uploadBatch) {
        return new Bdio2UploadCodeLocationCreationRequest(uploadBdio2BatchRunner, uploadBatch);
    }

    public CodeLocationCreationData<UploadBatchOutput> uploadBdio(CodeLocationCreationRequest<UploadBatchOutput> uploadRequest) throws IntegrationException {
        return codeLocationCreationService.createCodeLocations(uploadRequest);
    }

    public CodeLocationCreationData<UploadBatchOutput> uploadBdio(UploadBatch uploadBatch) throws IntegrationException {
        Bdio2UploadCodeLocationCreationRequest uploadRequest = createUploadRequest(uploadBatch);
        return uploadBdio(uploadRequest);
    }

    public UploadBatchOutput uploadBdioAndWait(CodeLocationCreationRequest<UploadBatchOutput> uploadRequest, long timeoutInSeconds) throws IntegrationException, InterruptedException {
        return codeLocationCreationService.createCodeLocationsAndWait(uploadRequest, timeoutInSeconds);
    }

    public UploadBatchOutput uploadBdioAndWait(UploadBatch uploadBatch, long timeoutInSeconds) throws IntegrationException, InterruptedException {
        Bdio2UploadCodeLocationCreationRequest uploadRequest = createUploadRequest(uploadBatch);
        return uploadBdioAndWait(uploadRequest, timeoutInSeconds);
    }

    public void waitForBdioUpload(NotificationTaskRange notificationTaskRange, NameVersion projectAndVersion, Set<String> codeLocationNames, int expectedNotificationCount, long timeoutInSeconds)
        throws IntegrationException, InterruptedException {
        codeLocationCreationService.waitForCodeLocations(notificationTaskRange, projectAndVersion, codeLocationNames, expectedNotificationCount, timeoutInSeconds);
    }

}
