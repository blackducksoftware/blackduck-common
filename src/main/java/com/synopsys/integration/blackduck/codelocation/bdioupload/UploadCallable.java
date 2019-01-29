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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

import org.apache.commons.io.FileUtils;

import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.model.RequestFactory;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.request.Response;

public class UploadCallable implements Callable<UploadOutput> {
    private final BlackDuckService blackDuckService;
    private final UploadTarget uploadTarget;

    public UploadCallable(BlackDuckService blackDuckService, UploadTarget uploadTarget) {
        this.blackDuckService = blackDuckService;
        this.uploadTarget = uploadTarget;
    }

    @Override
    public UploadOutput call() {
        try {
            String jsonPayload;
            try {
                jsonPayload = FileUtils.readFileToString(uploadTarget.getUploadFile(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                return UploadOutput.FAILURE(uploadTarget.getCodeLocationName(), "Failed to initially read file: " + uploadTarget.getUploadFile().getAbsolutePath() + " because " + e.getMessage(), e);
            }

            String uri = blackDuckService.getUri(BlackDuckService.BOMIMPORT_PATH);
            Request request = RequestFactory.createCommonPostRequestBuilder(jsonPayload).uri(uri).mimeType(uploadTarget.getMediaType()).build();
            try (Response response = blackDuckService.execute(request)) {
                String responseString = response.getContentString();
                return UploadOutput.SUCCESS(uploadTarget.getCodeLocationName(), responseString);
            } catch (IOException e) {
                return UploadOutput.FAILURE(uploadTarget.getCodeLocationName(), e.getMessage(), e);
            }
        } catch (Exception e) {
            return UploadOutput.FAILURE(uploadTarget.getCodeLocationName(), "Failed to upload file: " + uploadTarget.getUploadFile().getAbsolutePath() + " because " + e.getMessage(), e);
        }
    }

}
