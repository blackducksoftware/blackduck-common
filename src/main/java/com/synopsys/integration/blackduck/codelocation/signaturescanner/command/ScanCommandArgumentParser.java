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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

public class ScanCommandArgumentParser {

    public List<String> parse(String command) {
        if (!isParseable(command)) {
            return new ArrayList<>();
        }

        List<String> parsedArguments = new LinkedList<>();
        String[] splitBySpaces = command.split("\\s");
        boolean inQuotes = false;
        StringBuilder currentArgument = null;
        for (String text : splitBySpaces) {
            if (text.startsWith("\"")) {
                inQuotes = true;
                currentArgument = new StringBuilder();
            }
            if (inQuotes) {
                if (currentArgument.length() > 0) {
                    // we must be dealing with a quoted argument that has a space.  to preserve the original argument, add a space and then append the rest
                    currentArgument.append(" ");
                }
                currentArgument.append(text);
                if (text.endsWith("\"") && !text.endsWith("\\\"")) {
                    inQuotes = false;
                    parsedArguments.add(currentArgument.toString());
                }
            } else {
                parsedArguments.add(text);
            }
        }
        return parsedArguments;
    }

    private boolean isParseable(String command) {
        if (StringUtils.isBlank(command)) {
            return false;
        }
        return hasEvenNumberOfNonEscapedQuotes(command);
    }

    private boolean hasEvenNumberOfNonEscapedQuotes(String commandLine) {
        int numberOfNonEscapedQuotes = 0;
        char quote = '"';
        char backSlash = '\\';
        char last = 0;
        for (char current : commandLine.toCharArray()) {
            if (current == quote && last != backSlash) {
                numberOfNonEscapedQuotes++;
            }
            last = current;
        }
        return (numberOfNonEscapedQuotes % 2) == 0;
    }
}
