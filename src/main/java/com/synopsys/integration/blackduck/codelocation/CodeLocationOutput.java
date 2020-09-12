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
package com.synopsys.integration.blackduck.codelocation;

import java.util.Optional;

import com.synopsys.integration.util.NameVersion;

public abstract class CodeLocationOutput {
    private final Result result;
    private final NameVersion projectAndVersion;
    private final String codeLocationName;
    private final int expectedNotificationCount;
    private final String errorMessage;
    private final Exception exception;

    public CodeLocationOutput(Result result, NameVersion projectAndVersion, String codeLocationName, int expectedNotificationCount, String errorMessage, Exception exception) {
        this.result = result;
        this.projectAndVersion = projectAndVersion;
        this.codeLocationName = codeLocationName;
        this.expectedNotificationCount = expectedNotificationCount;
        this.errorMessage = errorMessage;
        this.exception = exception;
    }

    public Result getResult() {
        return result;
    }

    public NameVersion getProjectAndVersion() {
        return projectAndVersion;
    }

    public String getCodeLocationName() {
        return codeLocationName;
    }

    public int getExpectedNotificationCount() {
        return expectedNotificationCount;
    }

    public Optional<String> getErrorMessage() {
        return Optional.ofNullable(errorMessage);
    }

    public Optional<Exception> getException() {
        return Optional.ofNullable(exception);
    }

}
