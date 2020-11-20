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
package com.synopsys.integration.blackduck.developermode;

import java.io.File;
import java.util.List;

import com.synopsys.integration.blackduck.api.manual.view.BomMatchDeveloperView;
import com.synopsys.integration.blackduck.http.BlackDuckRequestFactory;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.DataService;
import com.synopsys.integration.log.IntLogger;

public class DeveloperScanService extends DataService {

    public DeveloperScanService(final BlackDuckApiClient blackDuckApiClient, final BlackDuckRequestFactory blackDuckRequestFactory,
        final IntLogger logger) {
        super(blackDuckApiClient, blackDuckRequestFactory, logger);
    }

    public BomMatchDeveloperView performDeveloperScan(File bdioFile) {
        if (!bdioFile.isFile()) {
            throw new IllegalArgumentException(String.format("bdio file provided is not a file. Path: %s ", bdioFile.getAbsolutePath()));
        }
        if (!bdioFile.exists()) {
            throw new IllegalArgumentException(String.format("bdio file does not exist. Path: %s", bdioFile.getAbsolutePath()));
        }

        if (!bdioFile.toPath().endsWith(".bdio")) {
            throw new IllegalArgumentException(String.format("Unknown file extension. Cannot perform developer scan. Path: %s", bdioFile.getAbsolutePath()));
        }
        startUpload();
        // for each entry in the bdio file upload chunk
        endUpload();

        // poll wait for result;

        return new BomMatchDeveloperView();
    }

    public BomMatchDeveloperView performDeveloperScan(List<File> bdioFiles) {
        if (bdioFiles.isEmpty()) {
            throw new IllegalArgumentException("bdio files cannot be empty.");
        }
        startUpload();
        // for each entry in the bdio file upload chunk
        endUpload();

        // poll wait for result;

        return new BomMatchDeveloperView();
    }

    private void startUpload() {

    }

    private void uploadChunk() {

    }

    private void endUpload() {

    }
}
