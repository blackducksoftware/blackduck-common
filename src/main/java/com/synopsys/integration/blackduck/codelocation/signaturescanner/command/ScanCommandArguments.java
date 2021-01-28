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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ScanCommandArguments {
    private List<String> cmd = new LinkedList<>();
    private SignatureScannerPassthroughArguments passthroughArguments ;
    private Set<String> argumentsThatCanBeMultiple;

    public ScanCommandArguments(SignatureScannerPassthroughArguments passthroughArguments) {
        if (passthroughArguments != null) {
            this.passthroughArguments = passthroughArguments;
        } else {
            this.passthroughArguments = new SignatureScannerPassthroughArguments("");
        }
        this.argumentsThatCanBeMultiple = populateArgumentsThatCanBeMultiple();
    }

    public void add(String arg) {
        if (!passthroughArguments.containsArgument(arg) || argumentsThatCanBeMultiple.contains(arg)) {
            cmd.add(arg);
        }
    }

    public List<String> getCommand() {
        return cmd;
    }

    public Set<String> populateArgumentsThatCanBeMultiple() {
        Set<String> argumentsThatCanBeMultiple = new HashSet<>();

        argumentsThatCanBeMultiple.add("--exclude");

        return argumentsThatCanBeMultiple;
    }
}
