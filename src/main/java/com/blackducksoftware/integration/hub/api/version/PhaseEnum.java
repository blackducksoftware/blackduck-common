/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
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
 *******************************************************************************/
package com.blackducksoftware.integration.hub.api.version;

public enum PhaseEnum {
    PLANNING("In Planning"),
    DEVELOPMENT("In Development"),
    RELEASED("Released"),
    DEPRECATED("Deprecated"),
    ARCHIVED("Archived"),
    UNKNOWNPHASE("Unknown Phase");

    private final String displayValue;

    private PhaseEnum(final String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    public static PhaseEnum getPhaseByDisplayValue(final String displayValue) {
        for (final PhaseEnum currentEnum : PhaseEnum.values()) {
            if (currentEnum.getDisplayValue().equalsIgnoreCase(displayValue)) {
                return currentEnum;
            }
        }
        return PhaseEnum.UNKNOWNPHASE;
    }

    public static PhaseEnum getPhaseEnum(final String phase) {
        if (phase == null) {
            return PhaseEnum.UNKNOWNPHASE;
        }
        PhaseEnum phaseEnum;
        try {
            phaseEnum = PhaseEnum.valueOf(phase.toUpperCase());
        } catch (final IllegalArgumentException e) {
            // ignore expection
            phaseEnum = UNKNOWNPHASE;
        }
        return phaseEnum;
    }
}
