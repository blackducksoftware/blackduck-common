/**
 * hub-common
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.commons.io.FileUtils;

import com.synopsys.integration.blackduck.service.HubService;
import com.synopsys.integration.blackduck.service.model.RequestFactory;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.request.Response;

public class UploadCallable implements Callable<UploadOutput> {
    private final HubService hubService;
    private final UploadTarget uploadTarget;

    public UploadCallable(final HubService hubService, final UploadTarget uploadTarget) {
        this.hubService = hubService;
        this.uploadTarget = uploadTarget;
    }

    @Override
    public UploadOutput call() {
        try {
            final String jsonPayload;
            try {
                jsonPayload = FileUtils.readFileToString(uploadTarget.getUploadFile(), StandardCharsets.UTF_8);
            } catch (final IOException e) {
                return UploadOutput.FAILURE("Failed to upload file: " + uploadTarget.getUploadFile().getAbsolutePath() + " because " + e.getMessage(), e);
            }

            final String uri = hubService.getUri(HubService.BOMIMPORT_PATH);
            final Request request = RequestFactory.createCommonPostRequestBuilder(jsonPayload).uri(uri).mimeType(uploadTarget.getMediaType()).build();
            try (Response response = hubService.executeRequest(request)) {
                final String responseString = response.getContentString();
                final Set<String> codeLocationNames = new HashSet<>(Arrays.asList(uploadTarget.getCodeLocationName()));
                return UploadOutput.SUCCESS(codeLocationNames, responseString);
            } catch (final IOException e) {
                return UploadOutput.FAILURE(e.getMessage(), e);
            }
        } catch (final Exception e) {
            return UploadOutput.FAILURE("An unknown error occurred trying to upload a file.", e);
        }
    }

}
