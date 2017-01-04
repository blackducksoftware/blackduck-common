/**
 * Hub Common
 *
 * Copyright (C) 2017 Black Duck Software, Inc.
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
package com.blackducksoftware.integration.hub.api.report.risk;

import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

public class RiskCounts {
    private final int HIGH;

    private final int MEDIUM;

    private final int LOW;

    private final int OK;

    private final int UNKNOWN;

    public RiskCounts(final int HIGH, final int MEDIUM, final int LOW, final int OK, final int UNKNOWN) {
        this.HIGH = HIGH;
        this.MEDIUM = MEDIUM;
        this.LOW = LOW;
        this.OK = OK;
        this.UNKNOWN = UNKNOWN;
    }

    public int getHIGH() {
        return HIGH;
    }

    public int getMEDIUM() {
        return MEDIUM;
    }

    public int getLOW() {
        return LOW;
    }

    public int getOK() {
        return OK;
    }

    public int getUNKNOWN() {
        return UNKNOWN;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + HIGH;
        result = prime * result + LOW;
        result = prime * result + MEDIUM;
        result = prime * result + OK;
        result = prime * result + UNKNOWN;
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof RiskCounts)) {
            return false;
        }
        final RiskCounts other = (RiskCounts) obj;
        if (HIGH != other.HIGH) {
            return false;
        }
        if (LOW != other.LOW) {
            return false;
        }
        if (MEDIUM != other.MEDIUM) {
            return false;
        }
        if (OK != other.OK) {
            return false;
        }
        if (UNKNOWN != other.UNKNOWN) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, RecursiveToStringStyle.JSON_STYLE);
    }

}
