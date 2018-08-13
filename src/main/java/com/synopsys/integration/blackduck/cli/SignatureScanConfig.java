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
package com.synopsys.integration.blackduck.cli;

import com.synopsys.integration.blackduck.configuration.CommonScanConfig;

public class SignatureScanConfig {
    private final CommonScanConfig commonScanConfig;
    private final String codeLocationAlias;
    private final String[] excludePatterns;
    private final String scanTarget;

    public SignatureScanConfig(final CommonScanConfig commonScanConfig, final String codeLocationAlias, final String[] excludePatterns, final String scanTarget) {
        this.commonScanConfig = commonScanConfig;
        this.codeLocationAlias = codeLocationAlias;
        this.excludePatterns = excludePatterns;
        this.scanTarget = scanTarget;
    }

    public CommonScanConfig getCommonScanConfig() {
        return commonScanConfig;
    }

    public String getCodeLocationAlias() {
        return codeLocationAlias;
    }

    public String[] getExcludePatterns() {
        return excludePatterns;
    }

    public String getScanTarget() {
        return scanTarget;
    }
}
