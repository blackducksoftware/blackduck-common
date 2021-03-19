/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.scan;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import com.synopsys.integration.blackduck.bdio2.stream.Bdio2FileUploadService;
import com.synopsys.integration.blackduck.codelocation.CodeLocationCreationData;
import com.synopsys.integration.blackduck.codelocation.CodeLocationCreationService;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadBatch;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadBatchOutput;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadOutput;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadTarget;
import com.synopsys.integration.blackduck.codelocation.intelligentpersistence.IntelligentPersistenceCallable;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.blackduck.http.BlackDuckRequestFactory;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.DataService;
import com.synopsys.integration.blackduck.service.model.NotificationTaskRange;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;

public class IntelligentPersistenceScanService extends DataService {
    public static final String CONTENT_TYPE = "application/vnd.blackducksoftware.intelligent-persistence-scan-1-ld-2+json";

    private ExecutorService executorService;
    private Bdio2FileUploadService bdio2FileUploadService;
    private CodeLocationCreationService codeLocationCreationService;

    public IntelligentPersistenceScanService(final BlackDuckApiClient blackDuckApiClient, final BlackDuckRequestFactory blackDuckRequestFactory,
        final IntLogger logger, final ExecutorService executorService, final Bdio2FileUploadService bdio2FileUploadService, final CodeLocationCreationService codeLocationCreationService) {
        super(blackDuckApiClient, blackDuckRequestFactory, logger);
        this.executorService = executorService;
        this.bdio2FileUploadService = bdio2FileUploadService;
        this.codeLocationCreationService = codeLocationCreationService;
    }

    public CodeLocationCreationData<UploadBatchOutput> performScan(UploadBatch uploadBatch) throws IntegrationException {
        NotificationTaskRange notificationTaskRange = codeLocationCreationService.calculateCodeLocationRange();
        UploadBatchOutput batchOutput = uploadTargets(uploadBatch);
        return new CodeLocationCreationData<>(notificationTaskRange, batchOutput);
    }

    public void performScan(UploadTarget uploadTarget) throws IntegrationException {
        bdio2FileUploadService.uploadFile(uploadTarget);
    }

    public UploadBatchOutput performScanAndWait(UploadBatch uploadBatch, long timeoutInSeconds) throws IntegrationException, InterruptedException {
        return performScanAndWait(uploadBatch, timeoutInSeconds, CodeLocationCreationService.DEFAULT_WAIT_INTERVAL_IN_SECONDS);
    }

    public UploadBatchOutput performScanAndWait(UploadBatch uploadBatch, long timeoutInSeconds, int waitIntervalInSeconds) throws IntegrationException, InterruptedException {
        CodeLocationCreationData<UploadBatchOutput> uploadResults = performScan(uploadBatch);
        NotificationTaskRange notificationTaskRange = uploadResults.getNotificationTaskRange();
        UploadBatchOutput output = uploadResults.getOutput();
        codeLocationCreationService.waitForCodeLocations(notificationTaskRange, output.getProjectAndVersion(), output.getSuccessfulCodeLocationNames(), output.getExpectedNotificationCount(), timeoutInSeconds, waitIntervalInSeconds);
        return output;
    }

    private UploadBatchOutput uploadTargets(UploadBatch uploadBatch) throws BlackDuckIntegrationException {
        List<UploadOutput> uploadOutputs = new ArrayList<>();

        try {
            List<IntelligentPersistenceCallable> callables = createCallables(uploadBatch);
            List<Future<UploadOutput>> submitted = new ArrayList<>();
            for (IntelligentPersistenceCallable callable : callables) {
                submitted.add(executorService.submit(callable));
            }
            for (Future<UploadOutput> future : submitted) {
                UploadOutput uploadOutput = future.get();
                uploadOutputs.add(uploadOutput);
            }
        } catch (Exception e) {
            throw new BlackDuckIntegrationException(String.format("Encountered a problem uploading a file: %s", e.getMessage()), e);
        }

        return new UploadBatchOutput(uploadOutputs);
    }

    private List<IntelligentPersistenceCallable> createCallables(UploadBatch uploadBatch) {
        List<IntelligentPersistenceCallable> callables = uploadBatch.getUploadTargets()
                                                             .stream()
                                                             .map(uploadTarget -> new IntelligentPersistenceCallable(bdio2FileUploadService, uploadTarget))
                                                             .collect(Collectors.toList());

        return callables;
    }
}
