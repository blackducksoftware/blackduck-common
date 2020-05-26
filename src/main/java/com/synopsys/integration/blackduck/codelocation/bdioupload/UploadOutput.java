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
package com.synopsys.integration.blackduck.codelocation.bdioupload;

import java.util.Optional;

import com.synopsys.integration.blackduck.codelocation.CodeLocationOutput;
import com.synopsys.integration.blackduck.codelocation.Result;
import com.synopsys.integration.util.NameVersion;

public class UploadOutput extends CodeLocationOutput {
    private final String response;

    public static UploadOutput SUCCESS(final NameVersion projectAndVersion, final String codeLocationName, final String response) {
        return new UploadOutput(projectAndVersion, codeLocationName, Result.SUCCESS, response, null, null);
    }

    public static UploadOutput FAILURE(final NameVersion projectAndVersion, final String codeLocationName, final String errorMessage, final Exception exception) {
        return new UploadOutput(projectAndVersion, codeLocationName, Result.FAILURE, null, errorMessage, exception);
    }

    public static UploadOutput FAILURE(final NameVersion projectAndVersion, final String codeLocationName, final String response, final String errorMessage, final Exception exception) {
        return new UploadOutput(projectAndVersion, codeLocationName, Result.FAILURE, response, errorMessage, exception);
    }

    private UploadOutput(final NameVersion projectAndVersion, final String codeLocationName, final Result result, final String response, final String errorMessage, final Exception exception) {
        super(result, projectAndVersion, codeLocationName, 1, errorMessage, exception);
        this.response = response;
    }

    public Optional<String> getResponse() {
        return Optional.ofNullable(response);
    }

}
