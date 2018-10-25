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

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.synopsys.integration.blackduck.summary.Result;

public class CodeLocationCreationStatus {
    private final Set<String> codeLocationNames;
    private final Result result;

    public CodeLocationCreationStatus(final Set<String> codeLocationNames, final Result result) {
        this.codeLocationNames = codeLocationNames;
        this.result = result;
    }

    public CodeLocationCreationStatus(final List<? extends CodeLocationCreationStatus> statuses) {
        codeLocationNames = statuses
                                    .stream()
                                    .map(output -> output.getCodeLocationNames())
                                    .flatMap(Collection::stream)
                                    .collect(Collectors.toSet());

        final boolean allSuccess = statuses
                                           .stream()
                                           .map(output -> output.getResult())
                                           .allMatch(result -> Result.SUCCESS == result);
        result = allSuccess ? Result.SUCCESS : Result.FAILURE;
    }

    public Set<String> getCodeLocationNames() {
        return codeLocationNames;
    }

    public Result getResult() {
        return result;
    }

}
