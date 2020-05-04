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
package com.synopsys.integration.blackduck.service;

import java.io.File;
import java.io.IOException;

import com.synopsys.integration.blackduck.codelocation.binaryscanner.BinaryScan;
import com.synopsys.integration.blackduck.codelocation.binaryscanner.BinaryScanCallable;
import com.synopsys.integration.blackduck.codelocation.binaryscanner.BinaryScanOutput;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;

/**
 * @deprecated Please use BinaryScanUploadService instead.
 */
@Deprecated
public class BinaryScannerService extends DataService {
    public static final String RESPONSE = "Response: ";

    public BinaryScannerService(BlackDuckService blackDuckService, IntLogger logger) {
        super(blackDuckService, logger);
    }

    public BinaryScanOutput scanBinary(File binaryFile, String projectName, String projectVersion, String codeLocationName) throws IntegrationException, IOException {
        BinaryScan binaryScan = new BinaryScan(binaryFile, projectName, projectVersion, codeLocationName);
        BinaryScanCallable binaryScanCallable = new BinaryScanCallable(blackDuckService, binaryScan);
        BinaryScanOutput binaryScanOutput = binaryScanCallable.call();

        logger.debug(String.format("%s%s", RESPONSE, binaryScanOutput.getResponse()));
        logger.debug(String.format("%s%s", RESPONSE, binaryScanOutput.getStatusMessage()));
        logger.debug(String.format("%s%s", RESPONSE, binaryScanOutput.getStatusCode()));
        logger.debug(String.format("%s%s", RESPONSE, binaryScanOutput.getContentString()));
        if (binaryScanOutput.getStatusCode() >= 200 && binaryScanOutput.getStatusCode() < 300) {
            logger.info("Status code OK");
        } else {
            logger.error("Unknown status code: " + binaryScanOutput.getStatusCode());
            throw new IntegrationException("Unknown status code when uploading binary scan: " + binaryScanOutput.getStatusCode() + ", " + binaryScanOutput.getStatusMessage());
        }

        return binaryScanOutput;
    }

}
