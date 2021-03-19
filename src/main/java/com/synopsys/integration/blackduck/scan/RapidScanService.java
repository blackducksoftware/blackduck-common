/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.scan;

import java.util.LinkedList;
import java.util.List;

import com.synopsys.integration.blackduck.api.manual.view.DeveloperScanComponentResultView;
import com.synopsys.integration.blackduck.bdio2.stream.Bdio2FileUploadService;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadBatch;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadTarget;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpUrl;

public class RapidScanService {
    public static final int DEFAULT_WAIT_INTERVAL_IN_SECONDS = 30;
    public static final String CONTENT_TYPE = "application/vnd.blackducksoftware.developer-scan-1-ld-2+json";
    private Bdio2FileUploadService bdio2FileUploadService;
    private RapidScanWaiter rapidScanWaiter;

    public RapidScanService(final Bdio2FileUploadService bdio2FileUploadService, final RapidScanWaiter rapidScanWaiter) {
        this.bdio2FileUploadService = bdio2FileUploadService;
        this.rapidScanWaiter = rapidScanWaiter;
    }

    public List<DeveloperScanComponentResultView> performScan(UploadBatch uploadBatch, long timeoutInSeconds) throws IntegrationException, InterruptedException {
        return performScan(uploadBatch, timeoutInSeconds, DEFAULT_WAIT_INTERVAL_IN_SECONDS);
    }

    public List<DeveloperScanComponentResultView> performScan(UploadBatch uploadBatch, long timeoutInSeconds, int waitIntervalInSeconds) throws IntegrationException, InterruptedException {
        List<DeveloperScanComponentResultView> scanResults = new LinkedList<>();
        List<UploadTarget> uploadTargets = uploadBatch.getUploadTargets();
        for (UploadTarget uploadTarget : uploadTargets) {
            scanResults.addAll(performScan(uploadTarget, timeoutInSeconds, waitIntervalInSeconds));
        }
        return scanResults;
    }

    public List<DeveloperScanComponentResultView> performScan(UploadTarget bdio2File, long timeoutInSeconds) throws IntegrationException, InterruptedException {
        return performScan(bdio2File, timeoutInSeconds, DEFAULT_WAIT_INTERVAL_IN_SECONDS);
    }

    public List<DeveloperScanComponentResultView> performScan(UploadTarget bdio2File, long timeoutInSeconds, int waitIntervalInSeconds) throws IntegrationException, InterruptedException {
        HttpUrl url = bdio2FileUploadService.uploadFile(bdio2File);
        return rapidScanWaiter.checkScanResult(url, timeoutInSeconds, waitIntervalInSeconds);
    }
}
