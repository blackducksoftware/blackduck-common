/**
 * blackduck-common
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
package com.synopsys.integration.blackduck.codelocation.binaryscanner;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.model.RequestFactory;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.request.Response;

public class BinaryScanCallable implements Callable<BinaryScanOutput> {
    private final BlackDuckService blackDuckService;
    private final BinaryScan binaryScan;

    public BinaryScanCallable(BlackDuckService blackDuckService, BinaryScan binaryScan) {
        this.blackDuckService = blackDuckService;
        this.binaryScan = binaryScan;
    }

    @Override
    public BinaryScanOutput call() {
        try {
            Map<String, String> textParts = new HashMap<>();
            textParts.put("projectName", binaryScan.getProjectName());
            textParts.put("version", binaryScan.getProjectVersion());
            textParts.put("codeLocationName", binaryScan.getCodeLocationName());

            Map<String, File> binaryParts = new HashMap<>();
            binaryParts.put("fileupload", binaryScan.getBinaryFile());

            Request.Builder requestBuilder = RequestFactory.createCommonPostRequestBuilder(binaryParts, textParts);
            try (Response response = blackDuckService.execute(BlackDuckService.UPLOADS_PATH, requestBuilder)) {
                return BinaryScanOutput.FROM_RESPONSE(binaryScan.getCodeLocationName(), response);
            }
        } catch (Exception e) {
            return BinaryScanOutput.FAILURE(binaryScan.getCodeLocationName(), "Failed to upload binary file: " + binaryScan.getBinaryFile().getAbsolutePath() + " because " + e.getMessage(), e);
        }
    }

}
