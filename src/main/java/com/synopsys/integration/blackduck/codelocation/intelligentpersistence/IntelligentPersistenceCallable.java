/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.codelocation.intelligentpersistence;

import java.util.concurrent.Callable;

import com.synopsys.integration.blackduck.bdio2.Bdio2FileUploadService;
import com.synopsys.integration.blackduck.codelocation.upload.UploadOutput;
import com.synopsys.integration.blackduck.codelocation.upload.UploadTarget;
import com.synopsys.integration.util.NameVersion;

public class IntelligentPersistenceCallable implements Callable<UploadOutput> {
    private Bdio2FileUploadService bdio2FileUploadService;
    private UploadTarget uploadTarget;

    public IntelligentPersistenceCallable(final Bdio2FileUploadService bdio2FileUploadService, final UploadTarget uploadTarget) {
        this.bdio2FileUploadService = bdio2FileUploadService;
        this.uploadTarget = uploadTarget;
    }

    @Override
    public UploadOutput call() {
        NameVersion projectAndVersion = uploadTarget.getProjectAndVersion();
        String codeLocationName = uploadTarget.getCodeLocationName();
        try {
            bdio2FileUploadService.uploadFile(uploadTarget);
            return UploadOutput.SUCCESS(projectAndVersion, codeLocationName, null);
        } catch (Exception ex) {
            String errorMessage = String.format("Failed to upload file: %s because %s", uploadTarget.getUploadFile().getAbsolutePath(), ex.getMessage());
            return UploadOutput.FAILURE(projectAndVersion, codeLocationName, errorMessage, ex);
        }
    }

}
