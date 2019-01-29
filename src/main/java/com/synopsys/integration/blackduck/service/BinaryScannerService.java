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
package com.synopsys.integration.blackduck.service;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import com.synopsys.integration.blackduck.service.model.RequestFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.request.Response;

public class BinaryScannerService extends DataService {
    public BinaryScannerService(BlackDuckService blackDuckService, IntLogger logger) {
        super(blackDuckService, logger);
    }

    public void scanBinary(File binaryFile, String projectName, String projectVersion, String codeLocationName) throws IntegrationException, IOException, URISyntaxException {
        Map<String, String> textParts = new HashMap<>();
        textParts.put("projectName", projectName);
        textParts.put("version", projectVersion);
        textParts.put("codeLocationName", codeLocationName);

        Map<String, File> binaryParts = new HashMap<>();
        binaryParts.put("fileupload", binaryFile);

        Request.Builder requestBuilder = RequestFactory.createCommonPostRequestBuilder(binaryParts, textParts);
        try (Response response = blackDuckService.execute(BlackDuckService.UPLOADS_PATH, requestBuilder)) {
            logger.debug("Response: " + response.toString());
            logger.debug("Response: " + response.getStatusMessage().toString());
            logger.debug("Response: " + response.getStatusCode().toString());
            logger.debug("Response: " + response.getContentString());
            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                logger.info("Status code OK");
            } else {
                logger.error("Unknown status code: " + response.getStatusCode());
                throw new IntegrationException("Unkown status code when uploading binary scan: " + response.getStatusCode() + ", " + response.getStatusMessage());
            }
        }
    }

}
