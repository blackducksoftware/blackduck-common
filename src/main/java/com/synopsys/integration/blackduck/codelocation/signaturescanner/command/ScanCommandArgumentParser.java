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

import com.synopsys.integration.exception.IntegrationArgumentException;
import com.synopsys.integration.exception.IntegrationException;

public class ScanCommandArgumentParser {
    public List<String> parse(String command) throws IntegrationException {
        if (StringUtils.isBlank(command)) {
            return new ArrayList<>();
        }

        //TODO - maybe make this a validate method or something like 'enforceMatchedQuotes'
        if (!hasEvenNumberOfNonEscapedQuotes(command)) {
            throw new IntegrationException(String.format("Unable to parse signature scanner arguments due to unbalanced quotes in command: %s", command));
        }

        List<String> parsedArguments = new ArrayList<>(); // TODO- list implementation shouldn't differ within method
        String[] splitBySpaces = command.split("\\s");
        boolean inQuotes = false;
        StringBuilder quotedArgument = null;
        for (String text : splitBySpaces) {
            if (text.startsWith("\"") && !inQuotes) {
                inQuotes = true;
                quotedArgument = new StringBuilder();
            }
            if (inQuotes) {
                if (quotedArgument.length() > 0) {
                    // we must be dealing with a quoted argument that has a space.  to preserve the original argument, add a space and then append the rest
                    quotedArgument.append(" ");
                }
                quotedArgument.append(text);
                if (text.endsWith("\"") && !text.endsWith("\\\"")) {
                    // we are only concerned with non-escaped quotes (escaped characters will just be passed along to scanner)
                    inQuotes = false;
                    parsedArguments.add(quotedArgument.toString());
                }
            } else {
                parsedArguments.add(text);
            }
        }
        String originalCheck = StringUtils.join(parsedArguments, " ");
        if (!originalCheck.equals(command)) {
            throw new IntegrationException("AHHHHH"); // TODO - change this message...maybe
        }
        return parsedArguments;
    }

    private boolean isParseable(String command) throws IntegrationException {
        if (StringUtils.isBlank(command)) {
            return false;
        }
        return hasEvenNumberOfNonEscapedQuotes(command);
    }

    private boolean hasEvenNumberOfNonEscapedQuotes(String command) throws IntegrationException {
        int numberOfNonEscapedQuotes = 0;
        char quote = '"';
        char backslash = '\\';
        char last = 0;
        for (char current : command.toCharArray()) {
            if (current == quote && last != backslash) {
                numberOfNonEscapedQuotes++;
            }
            last = current;
        }

        return (numberOfNonEscapedQuotes % 2) == 0;
    }
}
