/*
 * blackduck-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.codelocation.intelligentpersistence;

import java.util.concurrent.Callable;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.blackduck.bdio2.Bdio2FileUploadService;
import com.synopsys.integration.blackduck.codelocation.upload.UploadOutput;
import com.synopsys.integration.blackduck.codelocation.upload.UploadTarget;
import com.synopsys.integration.util.NameVersion;

public class IntelligentPersistenceCallable implements Callable<UploadOutput> {
    private final Bdio2FileUploadService bdio2FileUploadService;
    private final UploadTarget uploadTarget;
    private final long timeout;

    public IntelligentPersistenceCallable(Bdio2FileUploadService bdio2FileUploadService, UploadTarget uploadTarget, long timeout) {
        this.bdio2FileUploadService = bdio2FileUploadService;
        this.uploadTarget = uploadTarget;
        this.timeout = timeout;
    }

    @Override
    public UploadOutput call() {
        @Nullable
        NameVersion projectAndVersion = uploadTarget.getProjectAndVersion().orElse(null);
        String codeLocationName = uploadTarget.getCodeLocationName();
        try {
            bdio2FileUploadService.uploadFile(uploadTarget, timeout);
            return UploadOutput.SUCCESS(projectAndVersion, codeLocationName, null);
        } catch (Exception ex) {
            String errorMessage = String.format("Failed to upload file: %s because %s", uploadTarget.getUploadFile().getAbsolutePath(), ex.getMessage());
            return UploadOutput.FAILURE(projectAndVersion, codeLocationName, errorMessage, ex);
        }
    }

}
