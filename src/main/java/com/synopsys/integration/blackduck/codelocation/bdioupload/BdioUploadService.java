package com.synopsys.integration.blackduck.codelocation.bdioupload;

import java.util.Set;

import com.synopsys.integration.blackduck.codelocation.CodeLocationCreationData;
import com.synopsys.integration.blackduck.codelocation.CodeLocationCreationService;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.DataService;
import com.synopsys.integration.blackduck.service.model.NotificationTaskRange;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;

public class BdioUploadService extends DataService {
    private final UploadRunner uploadRunner;
    private final CodeLocationCreationService codeLocationCreationService;

    public BdioUploadService(final BlackDuckService blackDuckService, final IntLogger logger, final UploadRunner uploadRunner, final CodeLocationCreationService codeLocationCreationService) {
        super(blackDuckService, logger);
        this.uploadRunner = uploadRunner;
        this.codeLocationCreationService = codeLocationCreationService;
    }

    public CodeLocationCreationData<UploadBatchOutput> uploadBdio(final BdioUploadCodeLocationCreationRequest uploadRequest) throws IntegrationException {
        return codeLocationCreationService.createCodeLocations(uploadRequest);
    }

    public CodeLocationCreationData<UploadBatchOutput> uploadBdio(final UploadTarget uploadTarget) throws IntegrationException {
        final UploadBatch uploadBatch = new UploadBatch();
        uploadBatch.addUploadTarget(uploadTarget);
        final BdioUploadCodeLocationCreationRequest uploadRequest = new BdioUploadCodeLocationCreationRequest(uploadRunner, uploadBatch);

        return uploadBdio(uploadRequest);
    }

    public UploadBatchOutput uploadBdioAndWait(final BdioUploadCodeLocationCreationRequest uploadRequest, final long timeoutInSeconds) throws IntegrationException, InterruptedException {
        return codeLocationCreationService.createCodeLocationsAndWait(uploadRequest, timeoutInSeconds);
    }

    public UploadBatchOutput uploadBdioAndWait(final UploadTarget uploadTarget, final long timeoutInSeconds) throws IntegrationException, InterruptedException {
        final UploadBatch uploadBatch = new UploadBatch();
        uploadBatch.addUploadTarget(uploadTarget);
        final BdioUploadCodeLocationCreationRequest uploadRequest = new BdioUploadCodeLocationCreationRequest(uploadRunner, uploadBatch);

        return uploadBdioAndWait(uploadRequest, timeoutInSeconds);
    }

    public void waitForBdioUpload(final NotificationTaskRange notificationTaskRange, final Set<String> codeLocationNames, final long timeoutInSeconds) throws IntegrationException, InterruptedException {
        codeLocationCreationService.waitForCodeLocations(notificationTaskRange, codeLocationNames, timeoutInSeconds);
    }

}
