/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.scan;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.synopsys.integration.blackduck.codelocation.CodeLocationCreationService;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadBatch;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadTarget;
import com.synopsys.integration.blackduck.service.model.NotificationTaskRange;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.util.NameVersion;

public class IntelligentPersistenceScanService extends AbstractScanService {
    public static final String CONTENT_TYPE = "application/vnd.blackducksoftware.intelligent-persistence-scan-1-ld-2+json";

    private CodeLocationCreationService codeLocationCreationService;

    public IntelligentPersistenceScanService(final ScanBdio2Reader bdio2Reader, final ScanBdio2Uploader bdio2Uploader, CodeLocationCreationService codeLocationCreationService) {
        super(bdio2Reader, bdio2Uploader);
        this.codeLocationCreationService = codeLocationCreationService;
    }

    public void performScan(UploadBatch uploadBatch) throws IntegrationException {
        List<UploadTarget> uploadTargets = uploadBatch.getUploadTargets();
        for (UploadTarget uploadTarget : uploadTargets) {
            performScan(uploadTarget.getUploadFile());
        }
    }

    public void performScan(File bdio2File) throws IntegrationException {
        readContentAndUpload(bdio2File);
    }

    public void performScanAndWait(UploadBatch uploadBatch, long timeoutInSeconds) throws IntegrationException, InterruptedException {
        performScanAndWait(uploadBatch, timeoutInSeconds, CodeLocationCreationService.DEFAULT_WAIT_INTERVAL_IN_SECONDS);
    }

    public void performScanAndWait(UploadBatch uploadBatch, long timeoutInSeconds, int waitIntervalInSeconds) throws IntegrationException, InterruptedException {
        NotificationTaskRange notificationTaskRange = codeLocationCreationService.calculateCodeLocationRange();
        Map<NameVersion, Set<String>> projectCodeLocationsMap = new HashMap<>();
        for (UploadTarget uploadTarget : uploadBatch.getUploadTargets()) {
            performScan(uploadTarget.getUploadFile());
            Set<String> codeLocationNames = projectCodeLocationsMap.computeIfAbsent(uploadTarget.getProjectAndVersion(), ignoredKey -> new LinkedHashSet<>());
            codeLocationNames.add(uploadTarget.getCodeLocationName());
        }
        for (Map.Entry<NameVersion, Set<String>> entry : projectCodeLocationsMap.entrySet()) {
            codeLocationCreationService.waitForCodeLocations(notificationTaskRange, entry.getKey(), entry.getValue(), entry.getValue().size(), timeoutInSeconds, waitIntervalInSeconds);
        }
    }
}
