/*
 * blackduck-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.bdio2;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.exception.IntegrationTimeoutException;
import com.synopsys.integration.wait.WaitJobCompleter;

public class Bdio2UploadWaitJobCompleter implements WaitJobCompleter<Bdio2UploadResult> {
    private Bdio2UploadWaitJobCondition bdio2UploadWaitJobCondition;

    public Bdio2UploadWaitJobCompleter(final Bdio2UploadWaitJobCondition bdio2UploadWaitJobCondition) {
        this.bdio2UploadWaitJobCondition = bdio2UploadWaitJobCondition;
    }

    @Override
    public Bdio2UploadResult complete() throws IntegrationException {
        return new Bdio2UploadResult(bdio2UploadWaitJobCondition.getUploadUrl());
    }

    @Override
    public Bdio2UploadResult handleTimeout() throws IntegrationTimeoutException {
        throw new IntegrationTimeoutException("Bdio upload FAILED due to timeout.");
    }
}
