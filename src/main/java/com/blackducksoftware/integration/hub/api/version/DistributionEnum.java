/**
 * Hub Common
 *
 * Copyright (C) 2016 Black Duck Software, Inc..
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
package com.blackducksoftware.integration.hub.api.version;

public enum DistributionEnum {

    EXTERNAL("External"),
    SAAS("SaaS"),
    INTERNAL("Internal"),
    OPENSOURCE("Open Source"),
    UNKNOWNDISTRIBUTION("Unknown Distribution");

    private final String displayValue;

    private DistributionEnum(final String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    public static DistributionEnum getDistributionByDisplayValue(final String displayValue) {
        for (final DistributionEnum currentEnum : DistributionEnum.values()) {
            if (currentEnum.getDisplayValue().equalsIgnoreCase(displayValue)) {
                return currentEnum;
            }
        }
        return DistributionEnum.UNKNOWNDISTRIBUTION;
    }

    public static DistributionEnum getDistributionEnum(final String distribution) {
        if (distribution == null) {
            return DistributionEnum.UNKNOWNDISTRIBUTION;
        }
        DistributionEnum distributionEnum;
        try {
            distributionEnum = DistributionEnum.valueOf(distribution.toUpperCase());
        } catch (final IllegalArgumentException e) {
            // ignore expection
            distributionEnum = UNKNOWNDISTRIBUTION;
        }
        return distributionEnum;
    }
}
