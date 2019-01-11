/**
 * blackduck-common
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.blackduck.codelocation.bdioupload;

import java.util.Set;

import com.synopsys.integration.blackduck.codelocation.CodeLocationCreationData;
import com.synopsys.integration.blackduck.codelocation.CodeLocationCreationService;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.DataService;
import com.synopsys.integration.blackduck.service.model.NotificationTaskRange;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;

public class BdioUploadService extends DataService {
    private final UploadRunner uploadRunner;
    private final CodeLocationCreationService codeLocationCreationService;

    public BdioUploadService(final BlackDuckService blackDuckService, final IntLogger logger, final UploadRunner uploadRunner, final CodeLocationCreationService codeLocationCreationService) {
        super(blackDuckService, logger);
        this.uploadRunner = uploadRunner;
        this.codeLocationCreationService = codeLocationCreationService;
    }

    public BdioUploadCodeLocationCreationRequest createUploadRequest(final UploadBatch uploadBatch) {
        return new BdioUploadCodeLocationCreationRequest(uploadRunner, uploadBatch);
    }

    public CodeLocationCreationData<UploadBatchOutput> uploadBdio(final BdioUploadCodeLocationCreationRequest uploadRequest) throws IntegrationException {
        return codeLocationCreationService.createCodeLocations(uploadRequest);
    }

    public CodeLocationCreationData<UploadBatchOutput> uploadBdio(final UploadTarget uploadTarget) throws IntegrationException {
        final UploadBatch uploadBatch = new UploadBatch();
        uploadBatch.addUploadTarget(uploadTarget);
        final BdioUploadCodeLocationCreationRequest uploadRequest = new BdioUploadCodeLocationCreationRequest(uploadRunner, uploadBatch);

        return uploadBdio(uploadRequest);
    }

    public UploadBatchOutput uploadBdioAndWait(final BdioUploadCodeLocationCreationRequest uploadRequest, final long timeoutInSeconds) throws IntegrationException, InterruptedException {
        return codeLocationCreationService.createCodeLocationsAndWait(uploadRequest, timeoutInSeconds);
    }

    public UploadBatchOutput uploadBdioAndWait(final UploadTarget uploadTarget, final long timeoutInSeconds) throws IntegrationException, InterruptedException {
        final UploadBatch uploadBatch = new UploadBatch();
        uploadBatch.addUploadTarget(uploadTarget);
        final BdioUploadCodeLocationCreationRequest uploadRequest = new BdioUploadCodeLocationCreationRequest(uploadRunner, uploadBatch);

        return uploadBdioAndWait(uploadRequest, timeoutInSeconds);
    }

    public void waitForBdioUpload(final NotificationTaskRange notificationTaskRange, final Set<String> codeLocationNames, final long timeoutInSeconds) throws IntegrationException, InterruptedException {
        codeLocationCreationService.waitForCodeLocations(notificationTaskRange, codeLocationNames, timeoutInSeconds);
    }

}
