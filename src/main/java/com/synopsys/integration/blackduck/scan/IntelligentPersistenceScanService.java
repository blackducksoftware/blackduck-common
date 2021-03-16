/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.scan;

import java.io.File;

import com.synopsys.integration.exception.IntegrationException;

public class IntelligentPersistenceScanService extends AbstractScanService {
    public static final String CONTENT_TYPE = "application/vnd.blackducksoftware.intelligent-persistence-scan-1-ld-2+json";

    public IntelligentPersistenceScanService(final ScanBdio2Reader bdio2Reader, final ScanBdio2Uploader bdio2Uploader) {
        super(bdio2Reader, bdio2Uploader);
    }

    public void performScan(File bdio2File) throws IntegrationException {
        readContentAndUpload(bdio2File);
    }
}
