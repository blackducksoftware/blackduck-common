/*
 * blackduck-common
 *
 * Copyright (c) 2024 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.codelocation.intelligentpersistence;

import java.util.concurrent.Callable;

import com.blackduck.integration.blackduck.bdio2.Bdio2FileUploadService;
import com.blackduck.integration.blackduck.bdio2.Bdio2UploadResult;
import org.jetbrains.annotations.Nullable;

import com.blackduck.integration.blackduck.codelocation.upload.UploadOutput;
import com.blackduck.integration.blackduck.codelocation.upload.UploadTarget;
import com.synopsys.integration.util.NameVersion;

public class IntelligentPersistenceCallable implements Callable<UploadOutput> {
    private final Bdio2FileUploadService bdio2FileUploadService;
    private final UploadTarget uploadTarget;
    private final long timeout;
    private final long clientStartTime;

    public IntelligentPersistenceCallable(Bdio2FileUploadService bdio2FileUploadService, UploadTarget uploadTarget, long timeout, long clientStartTime) {
        this.bdio2FileUploadService = bdio2FileUploadService;
        this.uploadTarget = uploadTarget;
        this.timeout = timeout;
        this.clientStartTime = clientStartTime;
    }

    @Override
    public UploadOutput call() {
        @Nullable
        NameVersion projectAndVersion = uploadTarget.getProjectAndVersion().orElse(null);
        String codeLocationName = uploadTarget.getCodeLocationName();
        try {
            Bdio2UploadResult result = bdio2FileUploadService.uploadFile(uploadTarget, timeout, clientStartTime);
            return UploadOutput.SUCCESS(projectAndVersion, codeLocationName, null, result.getScanId());
        } catch (Exception ex) {
            String errorMessage = String.format("Failed to upload file: %s because %s", uploadTarget.getUploadFile().getAbsolutePath(), ex.getMessage());
            return UploadOutput.FAILURE(projectAndVersion, codeLocationName, errorMessage, ex);
        }
    }

}
