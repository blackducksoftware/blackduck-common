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
package com.synopsys.integration.blackduck.codelocation;

import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.blackduck.codelocation.signaturescanner.ScanBatch;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.ScanBatchManager;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.ScanBatchOutput;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScanTarget;
import com.synopsys.integration.blackduck.exception.HubIntegrationException;

public class SignatureScannerCodeLocationCreationRequest extends CodeLocationCreationRequest<ScanBatchOutput> {
    private final ScanBatchManager scanBatchManager;
    private final ScanBatch scanBatch;

    public SignatureScannerCodeLocationCreationRequest(final ScanBatchManager scanBatchManager, final ScanBatch scanBatch) {
        this.scanBatchManager = scanBatchManager;
        this.scanBatch = scanBatch;
    }

    @Override
    public Set<String> getCodeLocationNames() {
        return scanBatch.getScanTargets()
                       .stream()
                       .map(ScanTarget::getCodeLocationName)
                       .filter(StringUtils::isBlank)
                       .collect(Collectors.toSet());
    }

    @Override
    public ScanBatchOutput executeRequest() throws HubIntegrationException {
        return scanBatchManager.executeScans(scanBatch);
    }

}
