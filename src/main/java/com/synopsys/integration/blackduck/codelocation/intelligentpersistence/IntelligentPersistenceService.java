/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.codelocation.intelligentpersistence;

import java.util.Set;

import com.synopsys.integration.blackduck.codelocation.CodeLocationCreationData;
import com.synopsys.integration.blackduck.codelocation.CodeLocationCreationRequest;
import com.synopsys.integration.blackduck.codelocation.CodeLocationCreationService;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadBatch;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadBatchOutput;
import com.synopsys.integration.blackduck.http.BlackDuckRequestFactory;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.DataService;
import com.synopsys.integration.blackduck.service.model.NotificationTaskRange;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.util.NameVersion;

public class IntelligentPersistenceService extends DataService {
    public static final String CONTENT_TYPE = "application/vnd.blackducksoftware.intelligent-persistence-scan-1-ld-2+json";
    private final IntelligentPersistenceBatchRunner uploadBatchRunner;
    private final CodeLocationCreationService codeLocationCreationService;

    public IntelligentPersistenceService(final BlackDuckApiClient blackDuckApiClient, final BlackDuckRequestFactory blackDuckRequestFactory,
        final IntLogger logger, final IntelligentPersistenceBatchRunner uploadBatchRunner, final CodeLocationCreationService codeLocationCreationService) {
        super(blackDuckApiClient, blackDuckRequestFactory, logger);
        this.uploadBatchRunner = uploadBatchRunner;
        this.codeLocationCreationService = codeLocationCreationService;
    }

    public IntelligentPersistenceCodeLocationCreationRequest createUploadRequest(UploadBatch uploadBatch) {
        return new IntelligentPersistenceCodeLocationCreationRequest(uploadBatchRunner, uploadBatch);
    }

    public CodeLocationCreationData<UploadBatchOutput> uploadBdio(CodeLocationCreationRequest<UploadBatchOutput> uploadRequest) throws IntegrationException {
        return codeLocationCreationService.createCodeLocations(uploadRequest);
    }

    public CodeLocationCreationData<UploadBatchOutput> uploadBdio(UploadBatch uploadBatch) throws IntegrationException {
        IntelligentPersistenceCodeLocationCreationRequest uploadRequest = createUploadRequest(uploadBatch);
        return uploadBdio(uploadRequest);
    }

    public UploadBatchOutput uploadBdioAndWait(CodeLocationCreationRequest<UploadBatchOutput> uploadRequest, long timeoutInSeconds) throws IntegrationException, InterruptedException {
        return codeLocationCreationService.createCodeLocationsAndWait(uploadRequest, timeoutInSeconds);
    }

    public UploadBatchOutput uploadBdioAndWait(UploadBatch uploadBatch, long timeoutInSeconds) throws IntegrationException, InterruptedException {
        IntelligentPersistenceCodeLocationCreationRequest uploadRequest = createUploadRequest(uploadBatch);
        return uploadBdioAndWait(uploadRequest, timeoutInSeconds);
    }

    public void waitForBdioUpload(NotificationTaskRange notificationTaskRange, NameVersion projectAndVersion, Set<String> codeLocationNames, int expectedNotificationCount, long timeoutInSeconds)
        throws IntegrationException, InterruptedException {
        codeLocationCreationService.waitForCodeLocations(notificationTaskRange, projectAndVersion, codeLocationNames, expectedNotificationCount, timeoutInSeconds);
    }
}
