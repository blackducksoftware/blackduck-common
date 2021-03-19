/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.scan;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.synopsys.integration.blackduck.bdio2.stream.Bdio2FileUploadService;
import com.synopsys.integration.blackduck.codelocation.CodeLocationCreationData;
import com.synopsys.integration.blackduck.codelocation.CodeLocationCreationService;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadBatch;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadBatchOutput;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadOutput;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadTarget;
import com.synopsys.integration.blackduck.service.model.NotificationTaskRange;
import com.synopsys.integration.exception.IntegrationException;

public class IntelligentPersistenceScanService {
    public static final String CONTENT_TYPE = "application/vnd.blackducksoftware.intelligent-persistence-scan-1-ld-2+json";

    private Bdio2FileUploadService bdio2FileUploadService;
    private CodeLocationCreationService codeLocationCreationService;

    public IntelligentPersistenceScanService(final Bdio2FileUploadService bdio2FileUploadService, final CodeLocationCreationService codeLocationCreationService) {
        this.bdio2FileUploadService = bdio2FileUploadService;
        this.codeLocationCreationService = codeLocationCreationService;
    }

    public CodeLocationCreationData<UploadBatchOutput> performScan(UploadBatch uploadBatch) throws IntegrationException {
        NotificationTaskRange notificationTaskRange = codeLocationCreationService.calculateCodeLocationRange();
        List<UploadTarget> uploadTargets = uploadBatch.getUploadTargets();
        List<UploadOutput> uploadOutputs = new ArrayList<>();
        for (UploadTarget uploadTarget : uploadTargets) {
            try {
                performScan(uploadTarget.getUploadFile());
                uploadOutputs.add(UploadOutput.SUCCESS(uploadTarget.getProjectAndVersion(), uploadTarget.getCodeLocationName(), ""));
            } catch (Exception ex) {
                uploadOutputs.add(UploadOutput.FAILURE(uploadTarget.getProjectAndVersion(), uploadTarget.getCodeLocationName(), ex.getMessage(), ex));
            }
        }
        return new CodeLocationCreationData<>(notificationTaskRange, new UploadBatchOutput(uploadOutputs));
    }

    public void performScan(File bdio2File) throws IntegrationException {
        bdio2FileUploadService.uploadFile(bdio2File);
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
}
