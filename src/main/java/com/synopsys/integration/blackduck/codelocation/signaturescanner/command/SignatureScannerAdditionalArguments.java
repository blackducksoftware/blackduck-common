/**
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.blackduck.codelocation.signaturescanner.command;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SignatureScannerAdditionalArguments {
    private static final String DRY_RUN_ARGUMENT = "--dryRunWriteDir";
    private static final String SNIPPET_MATCHING_ARGUMENT = "--snippet-matching";
    private static final String SNIPPET_MATCHING_ONLY_ARGUMENT = "--snippet-matching-only";
    private static final String FULL_SNIPPET_SCAN_ARGUMENT = "--full-snippet-scan";
    private static final String UPLOAD_SOURCE_ARGUMENT = "--upload-source";
    private static final String LICENSE_SEARCH_ARGUMENT = "--license-search";
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private ScanCommandArgumentParser scanCommandArgumentParser = new ScanCommandArgumentParser();
    private List<String> arguments;

    public SignatureScannerAdditionalArguments(String argumentsAsString) {
        try {
            this.arguments = scanCommandArgumentParser.parse(argumentsAsString);
        } catch (Exception e) {
            // TODO- where's the best place to catch this?
            logger.error(e.getMessage());
        }
    }

    public boolean containsArgument(String argument) {
        return arguments.contains(argument);
    }

    public boolean containsDryRun() {
        return arguments.contains(DRY_RUN_ARGUMENT);
    }

    public boolean containsOnlineProperty() {
        return (containsArgument(SNIPPET_MATCHING_ARGUMENT) || containsArgument(FULL_SNIPPET_SCAN_ARGUMENT))
            || containsArgument(SNIPPET_MATCHING_ONLY_ARGUMENT) || containsArgument(UPLOAD_SOURCE_ARGUMENT)
            || containsArgument(LICENSE_SEARCH_ARGUMENT);
    }

    public List<String> getArguments() {
        return arguments;
    }

}
