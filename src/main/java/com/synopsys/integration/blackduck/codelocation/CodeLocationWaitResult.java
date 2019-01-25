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
package com.synopsys.integration.blackduck.codelocation;

import java.util.Optional;
import java.util.Set;

public class CodeLocationWaitResult {
    public enum Status {
        COMPLETE,
        PARTIAL
    }

    private final Status status;
    private final Set<String> codeLocationNames;
    private final String errorMessage;

    public static CodeLocationWaitResult COMPLETE(Set<String> codeLocationNames) {
        return new CodeLocationWaitResult(Status.COMPLETE, codeLocationNames, null);
    }

    public static CodeLocationWaitResult PARTIAL(Set<String> codeLocationNames, String errorMessage) {
        return new CodeLocationWaitResult(Status.PARTIAL, codeLocationNames, errorMessage);
    }

    public CodeLocationWaitResult(Status status, Set<String> codeLocationNames, String errorMessage) {
        this.status = status;
        this.codeLocationNames = codeLocationNames;
        this.errorMessage = errorMessage;
    }

    public Status getStatus() {
        return status;
    }

    public Set<String> getCodeLocationNames() {
        return codeLocationNames;
    }

    public Optional<String> getErrorMessage() {
        return Optional.ofNullable(errorMessage);
    }

}
