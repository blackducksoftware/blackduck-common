/**
 * blackduck-common
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
package com.synopsys.integration.blackduck.service.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

public class ProjectNameVersionGuesser {
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    public ProjectNameVersionGuess guessNameAndVersion(final String fullString) {
        String guessedName = "";
        String guessedVersion = "";

        int index = -1;
        if (fullString.contains("-")) {
            index = findIndexBeforeNumeric(fullString, "-", 0);
        } else if (fullString.contains(".")) {
            index = findIndexBeforeNumeric(fullString, ".", 0);
        }

        if (index > 0) {
            guessedName = fullString.substring(0, index);
            guessedVersion = fullString.substring(index + 1);
        }

        if (StringUtils.isBlank(guessedName) || StringUtils.isBlank(guessedVersion)) {
            guessedName = fullString;
            guessedVersion = getDefaultVersionGuess();
        }

        return new ProjectNameVersionGuess(guessedName, guessedVersion);
    }

    public String getDefaultVersionGuess() {
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT);
        final String defaultVersionGuess = simpleDateFormat.format(new Date());
        return defaultVersionGuess;
    }

    private int findIndexBeforeNumeric(final String fullString, final String separator, final int fromIndex) {
        final int index = fullString.indexOf(separator, fromIndex);
        if (index == -1) {
            return -1;
        } else if (index + 1 < fullString.length() && StringUtils.isNumeric(fullString.substring(index + 1, index + 2))) {
            return index;
        } else {
            return findIndexBeforeNumeric(fullString, separator, index + 1);
        }
    }

}
