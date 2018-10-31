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

import java.util.Optional;

public abstract class CodeLocationOutput {
    private final Result result;
    private final String codeLocationName;
    private final String errorMessage;
    private final Exception exception;

    public CodeLocationOutput(final Result result, final String codeLocationName, final String errorMessage, final Exception exception) {
        this.result = result;
        this.codeLocationName = codeLocationName;
        this.errorMessage = errorMessage;
        this.exception = exception;
    }

    public Result getResult() {
        return result;
    }

    public String getCodeLocationName() {
        return codeLocationName;
    }

    public Optional<String> getErrorMessage() {
        return Optional.ofNullable(errorMessage);
    }

    public Optional<Exception> getException() {
        return Optional.ofNullable(exception);
    }

}
