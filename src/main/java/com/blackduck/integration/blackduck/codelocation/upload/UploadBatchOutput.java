/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.codelocation.upload;

import java.util.List;

import com.blackduck.integration.blackduck.codelocation.CodeLocationBatchOutput;

public class UploadBatchOutput extends CodeLocationBatchOutput<UploadOutput> {
    public UploadBatchOutput(final List<UploadOutput> outputs) {
        super(outputs);
    }

}
