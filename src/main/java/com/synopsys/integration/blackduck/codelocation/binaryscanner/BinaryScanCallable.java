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
package com.synopsys.integration.blackduck.codelocation.binaryscanner;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.model.RequestFactory;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.response.Response;
import com.synopsys.integration.util.NameVersion;

public class BinaryScanCallable implements Callable<BinaryScanOutput> {
    private final BlackDuckService blackDuckService;
    private final BinaryScan binaryScan;
    private final NameVersion projectAndVersion;
    private final String codeLocationName;

    public BinaryScanCallable(BlackDuckService blackDuckService, BinaryScan binaryScan) {
        this.blackDuckService = blackDuckService;
        this.binaryScan = binaryScan;
        this.projectAndVersion = new NameVersion(binaryScan.getProjectName(), binaryScan.getProjectVersion());
        this.codeLocationName = binaryScan.getCodeLocationName();
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
                return BinaryScanOutput.FROM_RESPONSE(projectAndVersion, codeLocationName, response);
            }
        } catch (Exception e) {
            String errorMessage = String.format("Failed to upload binary file: %s because %s", binaryScan.getBinaryFile().getAbsolutePath(), e.getMessage());
            return BinaryScanOutput.FAILURE(projectAndVersion, codeLocationName, errorMessage, e);
        }
    }

}
