/**
 * blackduck-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.blackduck.codelocation.bdio2upload;

import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadOutput;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadTarget;
import com.synopsys.integration.blackduck.http.RequestFactory;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.response.Response;
import com.synopsys.integration.util.NameVersion;

import java.io.IOException;
import java.util.concurrent.Callable;

public class UploadBdio2Callable implements Callable<UploadOutput> {
    private final BlackDuckService blackDuckService;
    private final RequestFactory requestFactory;
    private final UploadTarget uploadTarget;
    private final NameVersion projectAndVersion;
    private final String codeLocationName;

    public UploadBdio2Callable(BlackDuckService blackDuckService, RequestFactory requestFactory, UploadTarget uploadTarget) {
        this.blackDuckService = blackDuckService;
        this.requestFactory = requestFactory;
        this.uploadTarget = uploadTarget;
        this.projectAndVersion = uploadTarget.getProjectAndVersion();
        this.codeLocationName = uploadTarget.getCodeLocationName();
    }

    @Override
    public UploadOutput call() {
        try {
            HttpUrl url = blackDuckService.getUrl(BlackDuckService.SCAN_DATA_PATH);
            Request request = requestFactory
                    .createCommonPostRequestBuilder(url, uploadTarget.getUploadFile())
                    .acceptMimeType(uploadTarget.getMediaType()).build();
            try (Response response = blackDuckService.execute(request)) {
                String responseString = response.getContentString();
                return UploadOutput.SUCCESS(projectAndVersion, codeLocationName, responseString);
            } catch (IOException e) {
                return UploadOutput.FAILURE(projectAndVersion, codeLocationName, e.getMessage(), e);
            }
        } catch (Exception e) {
            String errorMessage = String.format("Failed to upload file: %s because %s", uploadTarget.getUploadFile().getAbsolutePath(), e.getMessage());
            return UploadOutput.FAILURE(projectAndVersion, codeLocationName, errorMessage, e);
        }
    }

}
